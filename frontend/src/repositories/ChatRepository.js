import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

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
