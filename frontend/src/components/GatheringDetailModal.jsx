import React, { useState, useEffect } from 'react';
import { X, Users, MapPin, Calendar, MessageCircle, Send } from 'lucide-react';

const GatheringDetailModal = ({ gathering, onClose, onJoin }) => {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");

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
      display: 'flex', justifyContent: 'center', alignItems: 'flex-end'
    }}>
      <div style={{
        background: 'var(--surface)', width: '100%', maxWidth: '480px', height: '85vh', 
        borderTopLeftRadius: '28px', borderTopRightRadius: '28px', 
        display: 'flex', flexDirection: 'column',
        animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
        boxShadow: '0 -10px 40px rgba(0,0,0,0.1)', overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{ padding: '20px 24px', position: 'sticky', top: 0, background: 'var(--surface)', zIndex: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)' }}>
          <h2 style={{ fontSize: '18px', fontWeight: 800 }}>모임 상세 ✨</h2>
          <button onClick={onClose} style={{ padding: '8px', background: 'var(--bg-color)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', border: 'none' }}>
            <X size={20} color="var(--text-main)" />
          </button>
        </div>

        {/* Scrollable Content */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '0 24px 24px 24px' }}>
          {gathering.bgImageUrl && (
            <div style={{ height: '200px', margin: '20px -24px', backgroundImage: `url(${gathering.bgImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }} />
          )}

          <div style={{ marginTop: gathering.bgImageUrl ? '0' : '20px' }}>
            <span style={{ fontSize: '13px', color: 'var(--primary)', fontWeight: 600, background: 'var(--bg-color)', padding: '4px 8px', borderRadius: '8px' }}>{gathering.category}</span>
            <h1 style={{ fontSize: '24px', fontWeight: 800, margin: '12px 0 8px 0', color: 'var(--text-main)' }}>{gathering.title}</h1>
            <p style={{ color: 'var(--text-sub)', fontSize: '15px', fontWeight: 500 }}>Host: {gathering.host}</p>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '24px', padding: '16px', background: 'var(--bg-color)', borderRadius: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '15px', color: 'var(--text-main)', fontWeight: 500 }}>
              <Calendar size={18} color="var(--primary)" /> {gathering.dates}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '15px', color: 'var(--text-main)', fontWeight: 500 }}>
              <MapPin size={18} color="#FF6B6B" /> {gathering.location}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '15px', color: 'var(--text-main)', fontWeight: 500 }}>
              <Users size={18} color="#4DABF7" /> {gathering.currentJoining} / {gathering.maxJoining} 명 참여 중
            </div>
          </div>

          <button onClick={handleJoin} disabled={gathering.currentJoining >= gathering.maxJoining} style={{
            width: '100%', padding: '16px', background: gathering.currentJoining >= gathering.maxJoining ? 'var(--text-sub)' : 'var(--primary)', 
            color: 'white', border: 'none', borderRadius: '16px', fontSize: '16px', fontWeight: 700, marginTop: '24px', cursor: 'pointer'
          }}>
            {gathering.currentJoining >= gathering.maxJoining ? '마감되었습니다' : '참여하기'}
          </button>

          <div style={{ marginTop: '32px', borderTop: '1px solid var(--border)', paddingTop: '24px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <MessageCircle size={18} /> 질문/댓글 ({comments.length})
            </h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '80px' }}>
              {comments.map(c => (
                <div key={c.id} style={{ display: 'flex', gap: '12px' }}>
                  <div style={{ width: '32px', height: '32px', borderRadius: '16px', background: 'var(--border)', flexShrink: 0 }} />
                  <div>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                      <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-main)' }}>{c.author}</span>
                      <span style={{ fontSize: '12px', color: 'var(--text-sub)' }}>{new Date(c.createdAt).toLocaleDateString()}</span>
                    </div>
                    <p style={{ margin: '4px 0 0 0', fontSize: '15px', color: 'var(--text-main)' }}>{c.content}</p>
                  </div>
                </div>
              ))}
              {comments.length === 0 && <p style={{ fontSize: '14px', color: 'var(--text-sub)' }}>첫 번째 댓글을 남겨보세요!</p>}
            </div>
          </div>
        </div>

        {/* Comment Input */}
        <div style={{ 
          position: 'absolute', bottom: 0, left: 0, right: 0, 
          padding: '12px 24px 24px 24px', background: 'var(--surface)', borderTop: '1px solid var(--border)',
          display: 'flex', gap: '8px'
        }}>
          <input 
            type="text" value={newComment} onChange={e => setNewComment(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter') {
                e.preventDefault(); // Prevent form submission if it's trapped somewhere
                handlePostComment();
              }
            }}
            placeholder="댓글을 입력하세요..."
            style={{ 
              flex: 1, padding: '12px 16px', borderRadius: '20px', border: '1px solid var(--border)', 
              background: 'var(--bg-color)', fontSize: '15px', outline: 'none' 
            }}
          />
          <button onClick={handlePostComment} style={{
            width: '44px', height: '44px', borderRadius: '22px', background: 'var(--primary)', color: 'white', 
            border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
          }}>
            <Send size={18} style={{ marginLeft: '2px' }}/>
          </button>
        </div>

      </div>
    </div>
  );
};

export default GatheringDetailModal;
