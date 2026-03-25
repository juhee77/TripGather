import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth';

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
