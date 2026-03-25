import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/users';

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
