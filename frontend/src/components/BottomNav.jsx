import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Map, MessageCircle, User } from 'lucide-react';
import { useNotification } from '../contexts/NotificationContext';
import './BottomNav.css';

const BottomNav = () => {
  return (
    <nav className="bottom-nav">
      <div className="nav-items">
        <NavLink to="/gather" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <Home size={24} />
          <span>홈</span>
        </NavLink>
        <NavLink to="/map" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <Map size={24} />
          <span>지도</span>
        </NavLink>
        <NavLink to="/chat" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <div className="nav-icon-container">
            <MessageCircle size={24} />
            {useNotification().unreadCount > 0 && (
              <span className="notification-badge">{useNotification().unreadCount}</span>
            )}
          </div>
          <span>채팅/모임</span>
        </NavLink>
        <NavLink to="/mypage" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <User size={24} />
          <span>마이페이지</span>
        </NavLink>
      </div>
    </nav>
  );
};

export default BottomNav;
