import React, { useState, useEffect } from 'react';
import { authFetch } from '../api/client';
import { Star, Trash2 } from 'lucide-react';
import { useUser } from '../contexts/UserContext';

const ReviewSection = ({ tripId }) => {
  const { user } = useUser();
  const [reviews, setReviews] = useState([]);
  const [content, setContent] = useState('');
  const [rating, setRating] = useState(5);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReviews();
  }, [tripId]);

  const fetchReviews = async () => {
    try {
      const res = await authFetch(`/api/trips/${tripId}/reviews`);
      if (res.ok) setReviews(await res.json());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    try {
      const res = await authFetch(`/api/trips/${tripId}/reviews`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content, rating, category: '기타' })
      });
      if (res.ok) {
        setReviews([await res.json(), ...reviews]);
        setContent('');
        setRating(5);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;
    try {
      const res = await authFetch(`/api/trips/${tripId}/reviews/${reviewId}`, { method: 'DELETE' });
      if (res.ok) {
        setReviews(reviews.filter(r => r.id !== reviewId));
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div>로딩 중...</div>;

  return (
    <div>
      <div style={{ background: 'white', padding: '20px', borderRadius: '16px', border: '1px solid var(--border-color)', marginBottom: '24px' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '16px' }}>리뷰 남기기</h3>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', gap: '4px' }}>
            {[1, 2, 3, 4, 5].map(star => (
              <Star key={star} size={24} 
                color={star <= rating ? '#f59e0b' : 'var(--border-color)'}
                fill={star <= rating ? '#f59e0b' : 'none'}
                onClick={() => setRating(star)}
                style={{ cursor: 'pointer' }}
              />
            ))}
          </div>
          <textarea
            value={content} onChange={e => setContent(e.target.value)}
            placeholder="여행에 대한 리뷰를 남겨주세요."
            style={{ width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid var(--border-color)', minHeight: '80px', outline: 'none', resize: 'none', fontWeight: 600 }}
          />
          <button type="submit" style={{ padding: '12px', borderRadius: '12px', background: 'var(--primary-orange)', color: 'white', border: 'none', fontWeight: 800, cursor: 'pointer' }}>
            등록하기
          </button>
        </form>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {reviews.map(review => (
          <div key={review.id} style={{ background: 'white', padding: '16px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
              <div>
                <span style={{ fontWeight: 800, fontSize: '14px', marginRight: '8px' }}>{review.author?.name || '익명'}</span>
                <span style={{ color: '#f59e0b', fontSize: '13px', fontWeight: 700 }}>★ {review.rating}</span>
              </div>
              {user?.email === review.author?.email && (
                <button onClick={() => handleDelete(review.id)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer' }}>
                  <Trash2 size={16} />
                </button>
              )}
            </div>
            <p style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>{review.content}</p>
          </div>
        ))}
        {reviews.length === 0 && (
          <div style={{ textAlign: 'center', padding: '20px', color: 'var(--text-secondary)', fontWeight: 600 }}>작성된 리뷰가 없습니다.</div>
        )}
      </div>
    </div>
  );
};

export default ReviewSection;
