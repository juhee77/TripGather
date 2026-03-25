import { useState, useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import ChatRepository from '../repositories/ChatRepository';

export const useChatViewModel = (gathering, currentUser) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const scrollRef = useRef(null);

  const fetchHistory = useCallback(async () => {
    try {
      const history = await ChatRepository.getChatHistory(gathering.id);
      setMessages(history);
    } catch (err) {
      console.error("History fetch error:", err);
    }
  }, [gathering.id]);

  useEffect(() => {
    fetchHistory();

    const token = localStorage.getItem('token');
    const socket = new SockJS('http://localhost:8080/ws-stomp');
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        console.log('Connected to WebSocket');
        client.subscribe(`/topic/chat/${gathering.id}`, (message) => {
          const newMessage = JSON.parse(message.body);
          setMessages(prev => [...prev, newMessage]);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
      },
    });

    client.activate();
    setStompClient(client);

    return () => {
      if (client) client.deactivate();
    };
  }, [gathering.id, fetchHistory]);

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
      senderEmail: currentUser.email
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
    } catch (e) {
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
