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
import GatheringDetailPage from './pages/GatheringDetailPage';
import CreateGatheringPage from './pages/CreateGatheringPage';
import ItineraryDetailPage from './pages/ItineraryDetailPage';
import TripHubPage from './pages/TripHubPage';
import CreateTripPage from './pages/CreateTripPage';
import ItineraryEditorPage from './pages/ItineraryEditorPage';
import HostDashboardPage from './pages/HostDashboardPage';
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
        <Route path="/create" element={<PrivateRoute><CreateGatheringPage /></PrivateRoute>} />
        <Route path="/gathering/:id" element={<PrivateRoute><GatheringDetailPage /></PrivateRoute>} />
        <Route path="/itinerary/create" element={<PrivateRoute><ItineraryEditorPage /></PrivateRoute>} />
        <Route path="/itinerary/edit/:id" element={<PrivateRoute><ItineraryEditorPage /></PrivateRoute>} />
        <Route path="/itinerary/:id" element={<PrivateRoute><ItineraryDetailPage /></PrivateRoute>} />
        <Route path="/trip/:id" element={<PrivateRoute><TripHubPage /></PrivateRoute>} />
        <Route path="/trip/create" element={<PrivateRoute><CreateTripPage /></PrivateRoute>} />
        <Route path="/map" element={<PrivateRoute><MapPage /></PrivateRoute>} />
        <Route path="/chat" element={<PrivateRoute><ChatPage /></PrivateRoute>} />
        <Route path="/mypage" element={<PrivateRoute><MyPage /></PrivateRoute>} />
        <Route path="/profile/hosting" element={<PrivateRoute><HostDashboardPage /></PrivateRoute>} />
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

