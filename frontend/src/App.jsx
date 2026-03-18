import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
 import { UserProvider } from './contexts/UserContext';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { NotificationProvider } from './contexts/NotificationContext';
import BottomNav from './components/BottomNav';
import Home from './pages/Home';
import MapPage from './pages/MapPage';
import ChatPage from './pages/ChatPage';
import MyPage from './pages/MyPage';
import LoginPage from './pages/LoginPage';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler';
import './index.css';

/**
 * 로그인 여부에 따라 접근을 제한하는 컴포넌트
 */
function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) return <div>로딩 중...</div>;
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function AppContent() {
  const { isAuthenticated } = useAuth();
  
  return (
    <div className="app-container">
      <Routes>
        <Route path="/login" element={isAuthenticated ? <Navigate to="/gather" replace /> : <LoginPage />} />
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        
        <Route path="/" element={<Navigate to="/gather" replace />} />
        <Route path="/gather" element={<PrivateRoute><Home /></PrivateRoute>} />
        <Route path="/map" element={<PrivateRoute><MapPage /></PrivateRoute>} />
        <Route path="/chat" element={<PrivateRoute><ChatPage /></PrivateRoute>} />
        <Route path="/mypage" element={<PrivateRoute><MyPage /></PrivateRoute>} />
      </Routes>
      {isAuthenticated && <BottomNav />}
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <UserProvider>
        <NotificationProvider>
          <Router>
            <AppContent />
          </Router>
        </NotificationProvider>
      </UserProvider>
    </AuthProvider>
  );
}

export default App;

