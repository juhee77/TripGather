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
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const scrollRef = useRef(null);
  const clientRef = useRef(null);

  const fetchHistory = useCallback(async () => {
    if (!gathering?.id) return;
    try {
      const history = await ChatRepository.getChatHistory(gathering.id);
      setMessages(history);
    } catch (err) {
      console.error("History fetch error:", err);
    }
  }, [gathering?.id]);

  // 1. 과거 내역 로드 전용 Effect
  useEffect(() => {
    if (gathering?.id && currentUser) {
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
        client.subscribe(`/topic/chat/${gId}`, (message) => {
          try {
            const newMessage = JSON.parse(message.body);
            setMessages(prev => {
              if (prev.some(m => m.id === newMessage.id && m.id !== undefined)) return prev;
              return [...prev, newMessage];
            });
          } catch (e) {
            console.error("[Chat] Failed to parse message", e);
          }
        });
      },
      onStompError: (frame) => {
        console.error('[Chat] STOMP error', frame.headers['message']);
      },
      onWebSocketClose: () => {
        console.log('[Chat] WebSocket connection closed');
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

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

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
    isHost
  };
};
