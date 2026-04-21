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

  useEffect(() => {
    if (!gathering?.id || !currentUser) return;
    fetchHistory();

    const token = localStorage.getItem('token');
    const socket = new SockJS(apiUrl('/ws-stomp'));
    
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Connected to WebSocket - Subscribing to', gathering.id);
        client.subscribe(`/topic/chat/${gathering.id}`, (message) => {
          try {
            const newMessage = JSON.parse(message.body);
            setMessages(prev => {
              // 중복 메시지 방지 체크
              if (prev.some(m => m.id === newMessage.id && m.id !== undefined)) return prev;
              return [...prev, newMessage];
            });
          } catch (e) {
            console.error("Failed to parse message box", e);
          }
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame.headers['message']);
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
      }
    });

    client.activate();
    clientRef.current = client;
    setStompClient(client);

    return () => {
      if (clientRef.current) {
        console.log('Deactivating WebSocket...');
        clientRef.current.deactivate();
        clientRef.current = null;
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
