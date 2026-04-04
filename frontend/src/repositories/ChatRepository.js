import axios from 'axios';
import { API_BASE } from '../api/client';

const API_BASE_URL = `${API_BASE}/api`;

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const ChatRepository = {
  getChatHistory: async (gatheringId) => {
    const response = await axios.get(`${API_BASE_URL}/chat/${gatheringId}/history`, {
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
