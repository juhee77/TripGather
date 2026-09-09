import axios from 'axios';
import { API_BASE } from '../api/client';

const API_BASE_URL = `${API_BASE}/api`;

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const ChatRepository = {
  // before 를 주면 해당 메시지 ID 이전의 과거 메시지를 읽는다 (위로 스크롤).
  getChatHistory: async (gatheringId, before = null) => {
    const query = before ? `?before=${before}` : '';
    const response = await axios.get(`${API_BASE_URL}/chat/${gatheringId}/history${query}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  },

  getDMHistory: async (otherUserEmail) => {
    const response = await axios.get(`${API_BASE_URL}/dm/history/${otherUserEmail}`, {
      headers: getAuthHeaders()
    });
    return response.data;
  },

  markDMAsRead: async (otherUserEmail) => {
    await axios.put(`${API_BASE_URL}/dm/read/${otherUserEmail}`, {}, {
      headers: getAuthHeaders()
    });
  },
  
  getDMPartners: async () => {
    const response = await axios.get(`${API_BASE_URL}/dm/partners`, {
      headers: getAuthHeaders()
    });
    return response.data;
  }
};

export default ChatRepository;
