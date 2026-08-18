import React, { useEffect, useState } from 'react';
import { Award, Lock, CheckCircle2 } from 'lucide-react';
import { getUserBadges } from '../api/user';
import './UserBadges.css';

export default function UserBadges() {
  const [badges, setBadges] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBadges = async () => {
      try {
        setLoading(true);
        const data = await getUserBadges();
        setBadges(data);
      } catch (err) {
        console.error('Failed to load user badges:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchBadges();
  }, []);

  if (loading) {
    return <div className="badges-loading">뱃지 데이터를 불러오는 중...</div>;
  }

  const unlockedCount = badges.filter(b => b.unlocked).length;

  return (
    <div className="user-badges-container">
      <div className="badges-header">
        <h3><Award className="icon-badge-header" /> 여행 업적 뱃지</h3>
        <span className="badge-count-tag">
          <CheckCircle2 size={14} /> {unlockedCount} / {badges.length} 달성
        </span>
      </div>

      <div className="badges-grid">
        {badges.map((badge) => (
          <div
            key={badge.code}
            className={`badge-card ${badge.unlocked ? 'unlocked' : 'locked'}`}
          >
            <div className="badge-icon-wrapper">
              <span className="badge-emoji">{badge.icon}</span>
              {!badge.unlocked && <Lock className="lock-overlay-icon" size={16} />}
            </div>
            <div className="badge-info">
              <h4 className="badge-name">{badge.name}</h4>
              <p className="badge-desc">{badge.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
