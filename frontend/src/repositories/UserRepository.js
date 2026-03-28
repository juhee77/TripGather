import axios from 'axios';

import { API_BASE } from '../api/client';
const API_BASE_URL = `${API_BASE}/api/users`;

const UserRepository = {
  getMe: async () => {
    const response = await axios.get(`${API_BASE_URL}/me`);
    return response.data;
  },

  getUser: async (id) => {
    const response = await axios.get(`${API_BASE_URL}/${id}`);
    return response.data;
  },

  getAllUsers: async () => {
    const response = await axios.get(API_BASE_URL);
    return response.data;
  },

  updateProfile: async (id, updateData) => {
    const response = await axios.patch(`${API_BASE_URL}/${id}`, updateData);
    return response.data;
  }
};

export default UserRepository;
