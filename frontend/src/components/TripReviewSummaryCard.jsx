import React, { useState, useEffect } from 'react';
import { Star, MessageSquare, Plus, Trash2, Award } from 'lucide-react';
import { getTripReviews, createTripReview, deleteTripReview, getTripReviewSummary } from '../api/reviews';
import './TripReviewSummaryCard.css';

export default function TripReviewSummaryCard({ tripId }) {
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState({ totalReviews: 0, averageRating: 0.0 });
  const [selectedCategory, setSelectedCategory] = useState('');
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);

  const [formData, setFormData] = useState({
    content: '',
    rating: 5,
    category: '관광지'
  });

  const fetchData = async () => {
    try {
      setLoading(true);
      const [reviewList, summaryData] = await Promise.all([
        getTripReviews(tripId, selectedCategory),
        getTripReviewSummary(tripId)
      ]);
      setReviews(reviewList);
      setSummary(summaryData);
    } catch (err) {
      console.error('Failed to load reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tripId) {
      fetchData();
    }
  }, [tripId, selectedCategory]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.content.trim()) return;

    try {
      await createTripReview(tripId, formData);
      setFormData({ content: '', rating: 5, category: '관광지' });
      setShowAddForm(false);
      fetchData();
    } catch (err) {
      alert('후기 작성에 실패했습니다.');
    }
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;
    try {
      await deleteTripReview(tripId, reviewId);
      fetchData();
    } catch (err) {
      alert('본인이 작성한 리뷰만 삭제할 수 있습니다.');
    }
  };

  const renderStars = (rating) => {
    return Array.from({ length: 5 }, (_, i) => (
      <Star
        key={i}
        size={16}
        className={i < rating ? 'star-icon filled' : 'star-icon empty'}
      />
    ));
  };

  return (
    <div className="trip-review-card-container">
      {/* 요약 헤더 카드 */}
      <div className="review-summary-header">
        <div className="summary-left">
          <div className="rating-badge-large">
            <Star className="star-main" size={28} />
            <span className="avg-score">{summary.averageRating.toFixed(1)}</span>
          </div>
          <div className="summary-meta">
            <h4>여행 후기 및 리뷰</h4>
            <span className="total-reviews-count">총 {summary.totalReviews}개의 후기</span>
          </div>
        </div>

        <div className="summary-right">
          <span className="point-bonus-tag">
            <Award size={14} /> 후기 작성 시 +100P
          </span>
          <button className="btn-write-review" onClick={() => setShowAddForm(!showAddForm)}>
            <Plus size={16} /> {showAddForm ? '취소' : '후기 쓰기'}
          </button>
        </div>
      </div>

      {/* 후기 작성 폼 */}
      {showAddForm && (
        <form className="review-form" onSubmit={handleSubmit}>
          <div className="form-row-top">
            <div className="rating-select">
              <span>별점: </span>
              {[1, 2, 3, 4, 5].map((star) => (
                <Star
                  key={star}
                  size={20}
                  className={star <= formData.rating ? 'star-icon filled clickable' : 'star-icon empty clickable'}
                  onClick={() => setFormData({ ...formData, rating: star })}
                />
              ))}
            </div>
            <select
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            >
              <option value="관광지">🎟️ 관광지</option>
              <option value="숙소">🏨 숙소</option>
              <option value="맛집">🍽️ 맛집</option>
              <option value="기타">🛒 기타</option>
            </select>
          </div>
          <textarea
            placeholder="여행 후기를 자유롭게 남겨보세요. (예: 위치가 좋고 시설이 깔끔했어요!)"
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            rows={3}
            required
          />
          <button type="submit" className="btn-submit-review">후기 등록 (+100P)</button>
        </form>
      )}

      {/* 카테고리 필터 탭 */}
      <div className="category-filter-tabs">
        {['전체', '관광지', '숙소', '맛집', '기타'].map((cat) => {
          const value = cat === '전체' ? '' : cat;
          return (
            <button
              key={cat}
              className={`cat-tab ${selectedCategory === value ? 'active' : ''}`}
              onClick={() => setSelectedCategory(value)}
            >
              {cat}
            </button>
          );
        })}
      </div>

      {/* 후기 목록 */}
      <div className="review-list">
        {loading ? (
          <p className="loading-text">불러오는 중...</p>
        ) : reviews.length === 0 ? (
          <p className="empty-text">작성된 후기가 없습니다. 첫 후기를 남겨보세요!</p>
        ) : (
          reviews.map((review) => (
            <div key={review.id} className="review-item">
              <div className="review-item-header">
                <div className="author-info">
                  <span className="author-name">{review.authorName}</span>
                  <span className="category-pill">{review.category}</span>
                </div>
                <div className="review-stars-and-action">
                  <div className="stars-row">{renderStars(review.rating)}</div>
                  <button className="btn-delete-review" onClick={() => handleDelete(review.id)}>
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
              <p className="review-content">{review.content}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
