import React, { useState, useEffect } from 'react';
import { X, Users, MapPin, Calendar, MessageCircle, Send, Plane } from 'lucide-react';
import './FeedCard.css'; // Reuse some ticket styles

const GatheringDetailModal = ({ gathering, onClose, onJoin }) => {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");

  const dtParts = gathering?.dates ? gathering.dates.split(' ') : ['TBD', ''];
  const pDate = dtParts[0] || 'TBD';
  const pTime = dtParts[1] || '';

  const fetchComments = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/gatherings/${gathering.id}/comments`);
      if (res.ok) {
        const data = await res.json();
        setComments(data);
      }
    } catch (err) {
      console.error("Error fetching comments", err);
    }
  };

  useEffect(() => {
    if (gathering) {
      fetchComments();
    }
  }, [gathering]);

  const handlePostComment = async () => {
    if (!newComment.trim()) return;
    try {
      const res = await fetch(`http://localhost:8080/api/gatherings/${gathering.id}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: newComment, author: "Jihyun (지현)" }),
      });
      if (res.ok) {
        setNewComment("");
        fetchComments();
      } else {
        const errText = await res.text();
        alert(`댓글 안 달아짐: ${res.status} ${errText}`);
      }
    } catch (err) {
      console.error("Error posting comment", err);
      alert(`네트워크/서버 연결 오류: ${err.message}`);
    }
  };

  const handleJoin = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/gatherings/${gathering.id}/join`, {
        method: "POST"
      });
      if (res.ok) {
        const updatedGathering = await res.json();
        onJoin(updatedGathering);
        alert("모임 참여가 완료되었습니다! 🎉");
      } else {
        alert("원활하지 않은 요청입니다.");
      }
    } catch (err) {
      console.error("Error joining gathering", err);
    }
  };

  if (!gathering) return null;

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', zIndex: 1000,
      display: 'flex', justifyContent: 'center', alignItems: 'flex-end',
      padding: '20px 0 0 0'
    }}>
      <div style={{
        background: '#e0e0e0', width: '100%', maxWidth: '480px', height: '90vh',
        borderTopLeftRadius: '28px', borderTopRightRadius: '28px',
        display: 'flex', flexDirection: 'column',
        animation: 'slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
        overflow: 'hidden'
      }}>
        {/* Header Navigation */}
        <div style={{ padding: '16px 24px', background: '#fff', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Plane size={20} /> TRIP AIRLINES
          </h2>
          <button onClick={onClose} style={{ padding: '8px', background: 'var(--bg-color)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none' }}>
            <X size={20} color="var(--text-main)" />
          </button>
        </div>

        {/* Scrollable Content inside grey background to emulate holding a ticket */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>

          {/* Main Boarding Pass Card */}
          <div className="feed-card" style={{ marginBottom: 0 }}>
            {gathering.bgImageUrl && (
              <div style={{ height: '140px', background: `url(${gathering.bgImageUrl}) center/cover` }}></div>
            )}

            <div className="ticket-top" style={{ borderBottom: '2px dashed #e0e0e0', padding: '24px 20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                  <div style={{ fontSize: '11px', color: '#888', fontWeight: 700, letterSpacing: '1px', textTransform: 'uppercase' }}>{gathering.category}</div>
                  <h1 style={{ fontSize: '26px', fontWeight: 800, color: '#222', lineHeight: '1.2', marginTop: '4px' }}>{gathering.title}</h1>
                </div>
              </div>

              <div className="flight-info-grid">
                <div className="info-block">
                  <span className="info-label">Date</span>
                  <span className="info-value">{pDate}</span>
                </div>
                <div className="info-block">
                  <span className="info-label">Time</span>
                  <span className="info-value">{pTime || 'TBD'}</span>
                </div>
                <div className="info-block">
                  <span className="info-label">Location</span>
                  <span className="info-value" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{gathering.location}</span>
                </div>
              </div>

              <div className="flight-info-grid" style={{ marginBottom: 0, marginTop: '20px' }}>
                <div className="info-block" style={{ gridColumn: 'span 2' }}>
                  <span className="info-label">Passenger / Host</span>
                  <div className="host-info" style={{ marginTop: 0, paddingTop: 4, borderTop: 'none' }}>
                    <div className="host-avatar"></div>
                    <span className="host-name">{gathering.host}</span>
                  </div>
                </div>
                <div className="info-block">
                  <span className="info-label">Status</span>
                  <span className="info-value" style={{ color: gathering.currentJoining >= gathering.maxJoining ? '#F44336' : '#2196F3' }}>
                    {gathering.currentJoining}/{gathering.maxJoining} Joined
                  </span>
                </div>
              </div>
            </div>

            <div className="ticket-bottom" style={{ padding: '20px', flexDirection: 'column', gap: '16px' }}>
              <button
                onClick={handleJoin}
                className="btn-board"
                disabled={gathering.currentJoining >= gathering.maxJoining}
                style={{
                  width: '100%',
                  padding: '16px',
                  background: gathering.currentJoining >= gathering.maxJoining ? '#ccc' : 'var(--primary)',
                  boxShadow: gathering.currentJoining >= gathering.maxJoining ? 'none' : '0 8px 20px rgba(255, 123, 84, 0.4)',
                  fontSize: '18px'
                }}>
                {gathering.currentJoining >= gathering.maxJoining ? 'FLIGHT FULL' : 'CONFIRM BOARDING'}
              </button>

              <div className="barcode-area" style={{ width: '100%', alignItems: 'center', marginTop: '8px' }}>
                <div className="barcode-lines" style={{ width: '90%', height: '36px' }}></div>
                <div className="barcode-text">TKT-{gathering.id?.toString().padStart(6, '0')}-AB</div>
              </div>
            </div>
          </div>

          {/* Comments Section */}
          <div style={{ background: '#fff', borderRadius: '16px', padding: '24px', paddingBottom: '100px', boxShadow: '0 4px 15px rgba(0,0,0,0.05)' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <MessageCircle size={18} /> In-Flight Messages ({comments.length})
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {comments.map(c => (
                <div key={c.id} style={{ display: 'flex', gap: '12px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '18px', background: '#eee', flexShrink: 0 }} />
                  <div>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                      <span style={{ fontSize: '14px', fontWeight: 700, color: '#333' }}>{c.author}</span>
                      <span style={{ fontSize: '11px', color: '#888' }}>{new Date(c.createdAt).toLocaleDateString()}</span>
                    </div>
                    <p style={{ margin: '4px 0 0 0', fontSize: '15px', color: '#444', lineHeight: '1.4' }}>{c.content}</p>
                  </div>
                </div>
              ))}
              {comments.length === 0 && <p style={{ fontSize: '14px', color: '#888', textAlign: 'center', padding: '20px 0' }}>Say hello to your fellow travelers!</p>}
            </div>
          </div>
        </div>

        {/* Comment Input */}
        <div style={{
          position: 'absolute', bottom: 0, left: 0, right: 0,
          padding: '16px 20px 24px 20px', background: '#fff', borderTop: '1px solid #eee',
          display: 'flex', gap: '10px', boxShadow: '0 -4px 20px rgba(0,0,0,0.05)'
        }}>
          <input
            type="text" value={newComment} onChange={e => setNewComment(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter') {
                e.preventDefault();
                handlePostComment();
              }
            }}
            placeholder="Write a message..."
            style={{
              flex: 1, padding: '14px 20px', borderRadius: '24px', border: '1px solid #ddd',
              background: '#f8f8f8', fontSize: '15px', outline: 'none'
            }}
            onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
            onBlur={(e) => e.target.style.border = '1px solid #ddd'}
          />
          <button onClick={handlePostComment} style={{
            width: '48px', height: '48px', borderRadius: '24px', background: 'var(--primary)', color: 'white',
            border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            boxShadow: '0 4px 10px rgba(255, 123, 84, 0.3)'
          }}>
            <Send size={18} style={{ marginLeft: '2px' }} />
          </button>
        </div>

      </div>
    </div>
  );
};

export default GatheringDetailModal;

