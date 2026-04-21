import React, { useState, useEffect, useRef, useCallback } from 'react';
import { authFetch } from '../api/client';
import { Camera, Send, Image as ImageIcon, X } from 'lucide-react';
import Skeleton from './UI/Skeleton';

const GatheringFeed = ({ gatheringId }) => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [isPublic, setIsPublic] = useState(false);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  const fetchPosts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await authFetch(`/api/gatherings/${gatheringId}/posts`);
      if (res.ok) {
        const data = await res.json();
        setPosts(data);
      }
    } catch (err) {
      console.error("Failed to fetch posts:", err);
    } finally {
      setLoading(false);
    }
  }, [gatheringId]);

  useEffect(() => {
    fetchPosts();
  }, [gatheringId, fetchPosts]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      const reader = new FileReader();
      reader.onloadend = () => setImagePreview(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim() && !imageFile) return;

    setUploading(true);
    let imageUrl = null;

    if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);
        try {
            const uploadRes = await authFetch('/api/files/upload', {
                method: 'POST',
                body: formData,
            });
            if (uploadRes.ok) {
                const data = await uploadRes.json();
                imageUrl = data.url;
            }
        } catch (err) {
            console.error("Upload failed", err);
        }
    }

    try {
        const res = await authFetch(`/api/gatherings/${gatheringId}/posts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content, imageUrl, isPublic })
        });
        if (res.ok) {
            setContent('');
            setImageFile(null);
            setImagePreview(null);
            fetchPosts();
        }
    } catch (err) {
        console.error("Post failed", err);
    } finally {
        setUploading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Post Form */}
      <div className="glass" style={{ padding: '16px', marginBottom: '20px', borderRadius: '16px', background: 'white' }}>
        <form onSubmit={handleSubmit}>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="동료들과 공유하고 싶은 순간을 기록하세요..."
            style={{
              width: '100%',
              border: 'none',
              outline: 'none',
              resize: 'none',
              fontSize: '14px',
              minHeight: '60px',
              color: 'var(--text-primary)'
            }}
          />
          
          {imagePreview && (
            <div style={{ position: 'relative', marginTop: '10px', width: '100px', height: '100px' }}>
              <img src={imagePreview} alt="preview" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px' }} />
              <button 
                type="button"
                onClick={() => { setImageFile(null); setImagePreview(null); }}
                style={{ position: 'absolute', top: '-5px', right: '-5px', background: 'rgba(0,0,0,0.5)', color: 'white', borderRadius: '50%', padding: '2px', cursor: 'pointer' }}
              >
                <X size={14} />
              </button>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #f0f0f0' }}>
            <button 
              type="button" 
              onClick={() => fileInputRef.current.click()}
              style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-sub)', fontSize: '13px', fontWeight: 600, background: 'none', border: 'none', cursor: 'pointer' }}
            >
              <Camera size={18} color="var(--primary-orange)" />
              사진 추가
            </button>
            <input type="file" ref={fileInputRef} onChange={handleImageChange} style={{ display: 'none' }} accept="image/*" />
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer', fontSize: '12px', color: 'var(--text-sub)', fontWeight: 600 }}>
                <input 
                  type="checkbox" 
                  checked={isPublic} 
                  onChange={(e) => setIsPublic(e.target.checked)}
                  style={{ cursor: 'pointer' }}
                />
                전체 공개
              </label>
              <button 
                type="submit" 
                disabled={uploading || (!content.trim() && !imageFile)}
                style={{ 
                  background: 'var(--primary-gradient)', 
                  color: 'white', 
                  border: 'none', 
                  borderRadius: 'var(--radius-full)', 
                  padding: '8px 16px', 
                  fontSize: '13px', 
                  fontWeight: 800,
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '6px',
                  cursor: 'pointer',
                  opacity: (uploading || (!content.trim() && !imageFile)) ? 0.5 : 1
                }}
              >
                <Send size={16} />
                공유하기
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Feed List */}
      <div style={{ flex: 1, overflowY: 'auto' }} className="hide-scrollbar">
        {loading ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', padding: '16px' }}>
            {[1, 2, 3].map((i) => (
              <div key={i} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <Skeleton width="36px" height="36px" borderRadius="18px" />
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <Skeleton width="80px" height="12px" />
                    <Skeleton width="40px" height="10px" />
                  </div>
                </div>
                <Skeleton width="100%" height="200px" borderRadius="16px" />
                <Skeleton width="60%" height="14px" />
              </div>
            ))}
          </div>
        ) : posts.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#bbb' }}>
            <ImageIcon size={48} style={{ opacity: 0.2, marginBottom: '12px' }} />
            <p>아직 등록된 스냅샷이 없습니다.<br/>첫 번째 주인공이 되어보세요!</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {posts.map((post, idx) => (
              <div key={post.id} className="animate-fade" style={{ animationDelay: `${idx * 0.1}s` }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                  <div style={{ 
                    width: '36px', height: '36px', borderRadius: '18px', 
                    backgroundImage: `url(${post.authorImageUrl || 'https://via.placeholder.com/150'})`,
                    backgroundSize: 'cover', backgroundPosition: 'center', border: '2px solid white', boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                  }} />
                  <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
                        <div style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>{post.authorName}</div>
                        <div style={{ 
                          fontSize: '10px', 
                          fontWeight: 900, 
                          color: post.isPublic ? 'white' : 'var(--text-muted)', 
                          background: post.isPublic ? 'var(--primary-gradient)' : 'var(--bg-lite)', 
                          padding: '2px 8px', 
                          borderRadius: '6px', 
                          letterSpacing: '0.02em',
                          border: post.isPublic ? 'none' : '1px solid var(--border-color)',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px'
                        }}>
                          {post.isPublic ? (
                            <><span role="img" aria-label="public">🌍</span> 전체 공개</>
                          ) : (
                            <><span role="img" aria-label="private">🔒</span> 크루 전용</>
                          )}
                        </div>
                      </div>
                    <div style={{ fontSize: '11px', color: 'var(--text-sub)' }}>{new Date(post.createdAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}</div>
                  </div>
                </div>
                
                {post.imageUrl && (
                  <div style={{ borderRadius: '16px', overflow: 'hidden', marginBottom: '10px', boxShadow: '0 8px 24px rgba(0,0,0,0.08)' }}>
                    <img src={post.imageUrl} alt="post" style={{ width: '100%', display: 'block' }} />
                  </div>
                )}
                
                {post.content && (
                  <div style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.5, padding: '0 4px' }}>
                    {post.content}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GatheringFeed;
