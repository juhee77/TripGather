import React, { useState } from 'react';
import { CheckCircle, Circle, MapPin, Clock, Award } from 'lucide-react';
import { toggleRoutePoint } from '../api/itinerary';
import './RoutePointChecklist.css';

export default function RoutePointChecklist({ itineraryId, routePoints: initialPoints = [] }) {
  const [points, setPoints] = useState(initialPoints);
  const [toastMessage, setToastMessage] = useState(null);

  const handleToggle = async (pointId) => {
    try {
      const updatedPoint = await toggleRoutePoint(itineraryId, pointId);
      setPoints((prev) =>
        prev.map((p) => (p.id === pointId ? { ...p, isCompleted: updatedPoint.isCompleted } : p))
      );

      if (updatedPoint.isCompleted) {
        showToast('📍 체크인 완료! (+20 PTS 적립)');
      }
    } catch (err) {
      console.error('Failed to toggle route point:', err);
    }
  };

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const completedCount = points.filter((p) => p.isCompleted).length;

  return (
    <div className="route-checklist-container">
      {toastMessage && (
        <div className="point-reward-toast">
          <Award className="icon-reward" size={18} />
          <span>{toastMessage}</span>
        </div>
      )}

      <div className="checklist-header">
        <h4><MapPin className="icon-header" /> 여행 경로 체크인</h4>
        <span className="completion-badge">
          {completedCount} / {points.length} 장소 완료
        </span>
      </div>

      <div className="route-point-list">
        {points.length === 0 ? (
          <p className="empty-points">등록된 경로가 없습니다.</p>
        ) : (
          points.map((point) => (
            <div
              key={point.id}
              className={`route-point-item ${point.isCompleted ? 'completed' : ''}`}
              onClick={() => handleToggle(point.id)}
            >
              <div className="point-check-btn">
                {point.isCompleted ? (
                  <CheckCircle className="icon-check active" size={22} />
                ) : (
                  <Circle className="icon-check" size={22} />
                )}
              </div>

              <div className="point-content">
                <div className="point-top-row">
                  <span className="point-day-tag">{point.dayLabel || `Day ${point.dayNumber}`}</span>
                  {(point.startTime || point.endTime) && (
                    <span className="point-time">
                      <Clock size={12} /> {point.startTime} {point.endTime ? `~ ${point.endTime}` : ''}
                    </span>
                  )}
                </div>
                <h5 className="point-label">{point.label}</h5>
              </div>

              <div className="reward-tag">+20P</div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
