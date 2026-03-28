import axios from 'axios';

import { API_BASE } from '../api/client';
const API_BASE_URL = `${API_BASE}/api`;

const ChatRepository = {
  getChatHistory: async (gatheringId) => {
    const response = await axios.get(`${API_BASE_URL}/chat/${gatheringId}/history`);
    return response.data;
  },

  getDMHistory: async (otherUserEmail) => {
    const response = await axios.get(`${API_BASE_URL}/dm/history/${otherUserEmail}`);
    return response.data;
  },

  markDMAsRead: async (otherUserEmail) => {
    await axios.put(`${API_BASE_URL}/dm/read/${otherUserEmail}`);
  }
};

export default ChatRepository;
