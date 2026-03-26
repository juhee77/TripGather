import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const useAuthViewModel = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const { login, signup } = useAuth();
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    setError('');
    setSuccessMessage('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(formData.email, formData.password);
        navigate('/gather');
      } else {
        await signup(formData.name, formData.email, formData.password);
        setSuccessMessage('회원가입이 완료되었습니다! 이메일을 확인하여 인증을 완료해주세요.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'An error occurred';
      if (msg === '이메일 인증이 완료되지 않았습니다.') {
        setError('이메일 인증이 필요합니다. 메일함을 확인해주세요.');
      } else {
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  return {
    isLogin,
    formData,
    error,
    successMessage,
    setSuccessMessage,
    loading,
    handleInputChange,
    toggleMode,
    handleSubmit
  };
};
