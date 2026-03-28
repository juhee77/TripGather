import axios from 'axios';

import { API_BASE } from '../api/client';
const API_BASE_URL = `${API_BASE}/api/auth`;

const AuthRepository = {
  login: async (email, password) => {
    const response = await axios.post(`${API_BASE_URL}/login`, { email, password });
    return response.data;
  },
  
  register: async (signupData) => {
    const response = await axios.post(`${API_BASE_URL}/register`, signupData);
    return response.data;
  }
};

export default AuthRepository;
