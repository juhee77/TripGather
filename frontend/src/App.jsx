import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import BottomNav from './components/BottomNav';
import Home from './pages/Home';
import MapPage from './pages/MapPage';
import ChatPage from './pages/ChatPage';
import MyPage from './pages/MyPage';
import './index.css';

function App() {
  return (
    <Router>
      <div className="app-container">
        <Routes>
          <Route path="/" element={<Navigate to="/gather" replace />} />
          <Route path="/gather" element={<Home />} />
          <Route path="/map" element={<MapPage />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/mypage" element={<MyPage />} />
        </Routes>
        <BottomNav />
      </div>
    </Router>
  );
}

export default App;
