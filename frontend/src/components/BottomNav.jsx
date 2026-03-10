import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Map, MessageCircle, User } from 'lucide-react';
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
          <MessageCircle size={24} />
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
