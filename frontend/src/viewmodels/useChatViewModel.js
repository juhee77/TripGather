/* eslint-disable */
import { useState, useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import ChatRepository from '../repositories/ChatRepository';
import { apiUrl } from '../api/client';

export const useChatViewModel = (gathering, currentUser) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('CONNECTING'); // 'CONNECTING', 'CONNECTED', 'DISCONNECTED'
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [showScrollButton, setShowScrollButton] = useState(false);
  const scrollRef = useRef(null);
  const clientRef = useRef(null);
  const isInitialLoad = useRef(true);

  const fetchHistory = useCallback(async () => {
    if (!gathering?.id) return;
    try {
      const history = await ChatRepository.getChatHistory(gathering.id);
      setMessages(history);
      // 최초 히스토리 로드 후 스크롤을 맨 아래로 보내기 위한 처리
      setTimeout(() => {
        if (scrollRef.current) {
          scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
        isInitialLoad.current = false;
      }, 50);
    } catch (err) {
      console.error("History fetch error:", err);
    }
  }, [gathering?.id]);

  // 1. 과거 내역 로드 전용 Effect
  useEffect(() => {
    if (gathering?.id && currentUser) {
      isInitialLoad.current = true;
      fetchHistory();
    }
  }, [gathering?.id, currentUser?.email, fetchHistory]);

  // 2. 웹소켓 연결 관리용 Ref
  const connectionRef = useRef({ gatheringId: null, userEmail: null });

  // 3. 웹소켓 실시간 수신 전용 Effect
  useEffect(() => {
    const gId = gathering?.id;
    const uEmail = currentUser?.email;

    if (!gId || !uEmail) return;

    // 만약 이미 동일한 조건으로 연결되어 있다면 재연결 건너뜀
    if (connectionRef.current.gatheringId === gId && connectionRef.current.userEmail === uEmail && clientRef.current?.active) {
      return;
    }

    console.log(`[Chat] Connecting to WebSocket for gathering:${gId}, user:${uEmail}`);
    setConnectionStatus('CONNECTING');
    connectionRef.current = { gatheringId: gId, userEmail: uEmail };

    const token = localStorage.getItem('token');
    const socket = new SockJS(apiUrl('/ws-stomp'));
    
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('[Chat] WebSocket Connected - Subscribing to', gId);
        setConnectionStatus('CONNECTED');
        client.subscribe(`/topic/chat/${gId}`, (message) => {
          try {
            const newMessage = JSON.parse(message.body);
            setMessages(prev => {
              if (prev.some(m => m.id === newMessage.id && m.id !== undefined)) return prev;
              
              // 새 메시지가 들어왔을 때 스크롤 위치 감지
              if (scrollRef.current) {
                const { scrollTop, scrollHeight, clientHeight } = scrollRef.current;
                // 하단에서 150px 이내에 있는 경우 "가까운 하단"으로 판정
                const isNearBottom = scrollHeight - scrollTop - clientHeight < 150;
                
                if (isNearBottom) {
                  // 자동으로 스크롤 내리기
                  setTimeout(() => {
                    if (scrollRef.current) {
                      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
                    }
                  }, 50);
                } else {
                  // 사용자가 위를 보고 있으면 아래로 스크롤 버튼을 활성화
                  setShowScrollButton(true);
                }
              }
              return [...prev, newMessage];
            });
          } catch (e) {
            console.error("[Chat] Failed to parse message", e);
          }
        });
      },
      onDisconnect: () => {
        console.log('[Chat] WebSocket Disconnected');
        setConnectionStatus('DISCONNECTED');
      },
      onStompError: (frame) => {
        console.error('[Chat] STOMP error', frame.headers['message']);
        setConnectionStatus('DISCONNECTED');
      },
      onWebSocketClose: () => {
        console.log('[Chat] WebSocket connection closed');
        setConnectionStatus('DISCONNECTED');
      }
    });

    client.activate();
    clientRef.current = client;
    setStompClient(client);

    return () => {
      console.log('[Chat] Cleaning up effect for gathering:', gId);
      if (clientRef.current) {
        console.log('[Chat] Deactivating WebSocket connection...');
        clientRef.current.deactivate();
        clientRef.current = null;
        connectionRef.current = { gatheringId: null, userEmail: null };
      }
    };
  }, [gathering?.id, currentUser?.email]);

  // 스크롤 이벤트 감지 핸들러
  const handleScroll = useCallback(() => {
    if (!scrollRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = scrollRef.current;
    // 사용자가 직접 거의 맨 아래까지 내렸다면 새 메시지 버튼 숨김
    if (scrollHeight - scrollTop - clientHeight < 50) {
      setShowScrollButton(false);
    }
  }, []);

  const scrollToBottom = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollTo({
        top: scrollRef.current.scrollHeight,
        behavior: 'smooth'
      });
      setShowScrollButton(false);
    }
  };

  const handleSend = (e) => {
    if (e) e.preventDefault();
    if (!input.trim() || !stompClient) return;

    const chatMessage = {
      content: input,
      senderEmail: currentUser.email,
      senderName: currentUser.name
    };

    stompClient.publish({
      destination: `/app/chat/${gathering.id}/send`,
      body: JSON.stringify(chatMessage)
    });

    setInput('');
    // 내가 보낸 메시지인 경우 즉시 스크롤 아래로 내리기
    setTimeout(() => {
      scrollToBottom();
    }, 50);
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return "";
    try {
      const date = new Date(dateStr);
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: true });
    } catch (_e) {
      return "";
    }
  };

  const isHost = (email) => {
    if (!gathering.host) return false;
    return typeof gathering.host === 'string' ? gathering.host === email : gathering.host.email === email;
  };

  return {
    messages,
    input,
    setInput,
    isDrawerOpen,
    setIsDrawerOpen,
    scrollRef,
    handleSend,
    formatTime,
    isHost,
    connectionStatus,
    showScrollButton,
    handleScroll,
    scrollToBottom
  };
};

