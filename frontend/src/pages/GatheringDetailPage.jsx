import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MemberStatus } from '../constants/enums';
import { X, Users, MapPin, Calendar, MessageCircle, Send, Trash2, Edit, CheckCircle, XCircle, Share2, Heart, Plane } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch, apiUrl } from '../api/client';
import ModalHeader from '../components/UI/ModalHeader';
import ModalFooter from '../components/UI/ModalFooter';
import FormInput from '../components/UI/FormInput';
import PrimaryButton from '../components/UI/PrimaryButton';
import GatheringFeed from '../components/GatheringFeed';
import ChatRoom from '../components/ChatRoom';

const GatheringDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [gathering, setGathering] = useState(null);
  const [loading, setLoading] = useState(true);

  // Fetch gathering details on load
  const loadGathering = async () => {
    try {
      const res = await authFetch(`/api/gatherings/${id}`);
      if (res.ok) {
        const data = await res.json();
        setGathering(data);
        setEditData(data);
      }
    } catch (err) {
      console.error('Failed to load gathering', err);
    } finally {
      setLoading(false);
    }
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (id) loadGathering();
  }, [id]);

  const onClose = () => navigate(-1);
  const onUpdate = (updated) => { if(updated) { setGathering(updated); setEditData(updated); } else loadGathering(); };
  const onJoin = (updated) => { if(updated) setGathering(updated); else loadGathering(); };
  const onDelete = () => navigate('/gather', { replace: true });
  const { user: currentUser } = useUser();
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [activeTab, setActiveTab] = useState('브리핑'); // '브리핑', '크루', '무전', '갤러리'
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({});

  const isHost = currentUser && (
    (gathering?.host && (
      (typeof gathering.host === 'string' && gathering.host === currentUser.name) ||
      (gathering.host.email === currentUser.email)
    )) ||
    (!gathering?.host && gathering?.linkedItinerary?.authorEmail === currentUser.email)
  );

  const myStatus = gathering?.members?.find(m => m.user.email === currentUser?.email)?.status;
  const isMember = isHost || myStatus === MemberStatus.APPROVED;

  const getHaversineDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371e3; // meters
    const phi1 = lat1 * Math.PI / 180;
    const phi2 = lat2 * Math.PI / 180;
    const deltaPhi = (lat2 - lat1) * Math.PI / 180;
    const deltaLambda = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
              Math.cos(phi1) * Math.cos(phi2) *
              Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // meters
  };

  const handleCheckin = () => {
    const hasLocationCoords = gathering.lat !== null && gathering.lng !== null;

    const requestCheckinApi = async (userLat = null, userLng = null, force = false) => {
      try {
        let queryParams = [];
        if (userLat !== null) queryParams.push(`lat=${userLat}`);
        if (userLng !== null) queryParams.push(`lng=${userLng}`);
        if (force) queryParams.push(`force=true`);
        const queryStr = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';

        const res = await authFetch(`/api/gatherings/${gathering.id}/checkin${queryStr}`, {
          method: "POST"
        });
        if (res.ok) {
          alert("현장 체크인이 완료되었습니다! 50포인트와 여권 마일리지가 적립되었습니다. ✈️");
          loadGathering();
        } else {
          const errText = await res.text();
          alert(`체크인 실패: ${errText || "모임 조건이 충족되지 않았습니다."}`);
        }
      } catch (err) {
        console.error("Error checkin", err);
        alert(`네트워크 오류가 발생했습니다: ${err.message}`);
      }
    };

    if (navigator.geolocation && hasLocationCoords) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const userLat = position.coords.latitude;
          const userLng = position.coords.longitude;
          const distance = getHaversineDistance(userLat, userLng, gathering.lat, gathering.lng);

          if (distance <= 100.0) {
            await requestCheckinApi(userLat, userLng, false);
          } else {
            const msg = `만남 장소와 거리가 너무 멉니다.\n(현재 오차 거리: ${Math.round(distance)}m)\n\n약속 장소 반경 100m 이내에 있어야 체크인이 가능합니다.\n\n데모/테스트 목적으로 강제 체크인을 진행하시겠습니까?`;
            if (window.confirm(msg)) {
              await requestCheckinApi(userLat, userLng, true);
            }
          }
        },
        async (error) => {
          console.warn("GPS 획득 실패", error);
          const msg = "GPS 신호를 획득하지 못했거나 위치 권한이 거부되었습니다.\n\n데모/테스트 목적으로 위치 검증 없이 강제 체크인을 진행하시겠습니까?";
          if (window.confirm(msg)) {
            await requestCheckinApi(null, null, true);
          }
        },
        { enableHighAccuracy: true, timeout: 5000 }
      );
    } else {
      requestCheckinApi(null, null, true);
    }
  };



  const fetchComments = async () => {
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/comments`);
      if (res.ok) {
        const data = await res.json();
        setComments(data);
      }
    } catch (err) {
      console.error("Error fetching comments", err);
    }
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (gathering) {
      fetchComments();
    }
  }, [gathering]);

  const handlePostComment = async () => {
    if (!newComment.trim()) return;
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: newComment, author: currentUser?.name || "익명" }),
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
      const res = await authFetch(`/api/gatherings/${gathering.id}/join`, {
        method: "POST"
      });
      if (res.ok) {
        const updatedGathering = await res.json();
        onJoin(updatedGathering);
        alert("참여 신청을 보냈습니다. 호스트가 승인하면 확정됩니다! 📨");
      } else {
        alert("참여 신청에 실패했습니다.");
      }
    } catch (err) {
      console.error("Error joining gathering", err);
    }
  };

  const handleApprove = async (userId) => {
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/members/${userId}/approve`, {
        method: "POST"
      });
      if (res.ok) {
        alert("멤버를 승인했습니다!");
        onUpdate && onUpdate();
      }
    } catch (err) {
      console.error("Error approving member", err);
    }
  };

  const handleReject = async (userId) => {
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/members/${userId}/reject`, {
        method: "POST"
      });
      if (res.ok) {
        alert("멤버 신청을 거절했습니다.");
        onUpdate && onUpdate();
      }
    } catch (err) {
      console.error("Error rejecting member", err);
    }
  };

  
  const handleLike = async () => {
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/like`, { method: "POST" });
      if (res.ok) {
        onUpdate && onUpdate();
      }
    } catch (err) {
      console.error("Error liking gathering", err);
    }
  };

  const handleShare = async () => {
    const shareData = {
      title: gathering.title,
      text: `${gathering.host?.name || gathering.host}님의 여행에 초대합니다!`,
      url: window.location.href,
    };

    try {
      if (navigator.share) {
        await navigator.share(shareData);
      } else {
        await navigator.clipboard.writeText(window.location.href);
        alert("링크가 클립보드에 복사되었습니다! ✈️");
      }
    } catch (err) {
      console.error("Error sharing", err);
    }
  };

  const handleUpdate = async () => {
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(editData),
      });
      if (res.ok) {
        const updated = await res.json();
        alert("모임 정보가 수정되었습니다! ✨");
        onUpdate && onUpdate(updated);
        setIsEditing(false);
      } else {
        alert("수정에 실패했습니다.");
      }
    } catch (err) {
      console.error("Error updating gathering", err);
    }
  };

  const handleLeave = async () => {
    if (!window.confirm("정말로 이 모임에서 나가시겠습니까?")) return;
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}/leave`, {
        method: "POST"
      });
      if (res.ok) {
        alert("모임에서 탈퇴되었습니다.");
        onUpdate && onUpdate();
        onClose();
      } else {
        alert("탈퇴 처리에 실패했습니다.");
      }
    } catch (err) {
      console.error("Error leaving gathering", err);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("정말로 모임을 삭제하시겠습니까?")) return;
    try {
      const res = await authFetch(`/api/gatherings/${gathering.id}`, {
        method: "DELETE"
      });
      if (res.ok) {
        alert("모임이 삭제되었습니다.");
        onDelete && onDelete();
        onClose();
      } else {
        alert("삭제 처리에 실패했습니다.");
      }
    } catch (err) {
      console.error("Error deleting gathering", err);
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>로딩 중...</div>;
  if (!gathering) return <div style={{ padding: '40px', textAlign: 'center' }}>모임을 찾을 수 없습니다.</div>;

  return (
    <div className="animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', paddingBottom: '100px' }}>
        <ModalHeader 
          title={gathering.title}
          subtitle={`Host: ${typeof gathering.host === 'string' ? gathering.host : gathering.host?.name}`}
          onClose={onClose}
          actions={
            <div style={{ display: 'flex', gap: '8px' }}>
              <button 
                onClick={handleLike} 
                className="icon-circle" 
                style={{ 
                  background: 'var(--bg-color)', 
                  color: (gathering.likedByCurrentUser || false) ? '#FF6B6B' : 'var(--text-muted)' 
                }}
              >
                <Heart size={18} fill={(gathering.likedByCurrentUser || false) ? '#FF6B6B' : 'none'} />
              </button>
              <button onClick={handleShare} className="icon-circle" style={{ background: 'var(--bg-color)', color: 'var(--text-primary)' }}>
                <Share2 size={18} />
              </button>
              {isHost && (
                <>
                  <button onClick={() => setIsEditing(true)} className="icon-circle" style={{ background: 'var(--bg-color)', color: 'var(--primary-orange)' }}>
                    <Edit size={18} />
                  </button>
                  <button onClick={handleDelete} className="icon-circle" style={{ background: 'var(--bg-color)', color: '#FF6B6B' }}>
                    <Trash2 size={18} />
                  </button>
                </>
              )}
              {!isHost && myStatus === MemberStatus.APPROVED && (
                <button onClick={handleLeave} title="모임 나가기" className="icon-circle" style={{ background: 'var(--bg-color)', color: '#FF6B6B' }}>
                  <XCircle size={18} />
                </button>
              )}
            </div>
          }
        />

        <div style={{ padding: '0 24px', borderBottom: '1px solid var(--border-color)' }}>
          {/* New Tabs */}
          <div style={{ display: 'flex', gap: '16px', overflowX: 'auto' }} className="hide-scrollbar">
            {[
              '브리핑', 
              '크루', 
              `무전 (${comments.length})`, 
              '갤러리 📸',
              (isMember || gathering.isChatPublic) ? '채팅 💬' : null
            ].filter(Boolean).map((tab) => {
              const tabName = tab.startsWith('무전') ? '무전' : tab.startsWith('갤러리') ? '갤러리' : tab.startsWith('채팅') ? '채팅' : tab;
              const isActive = activeTab === tabName;
              
              return (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tabName)}
                  style={{
                    padding: '16px 4px',
                    fontSize: '15px',
                    fontWeight: isActive ? 800 : 600,
                    color: isActive ? 'var(--primary-orange)' : 'var(--text-muted)',
                    borderBottom: isActive ? '2px solid var(--primary-orange)' : '2px solid transparent',
                    background: 'none',
                    borderRadius: 0,
                    transition: 'all 0.2s',
                    cursor: 'pointer'
                  }}
                >
                  {tab}
                </button>
              );
            })}
          </div>
        </div>

        {/* Scrollable Content */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '24px' }}>
          {activeTab === '브리핑' && (
            <div className="animate-fade">
              {/* Existing info content remains here */}
              {isEditing ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', padding: '20px', background: 'var(--bg-color)', borderRadius: '20px' }}>
                  <FormInput 
                    label="TITLE"
                    value={editData.title}
                    onChange={e => setEditData({...editData, title: e.target.value})}
                  />
                  <FormInput 
                    label="SCHEDULE"
                    icon={Calendar}
                    value={editData.dates}
                    onChange={e => setEditData({...editData, dates: e.target.value})}
                  />
                  <FormInput 
                    label="LOCATION"
                    icon={MapPin}
                    value={editData.location}
                    onChange={e => setEditData({...editData, location: e.target.value})}
                  />
                  <FormInput 
                    label="MAX PARTICIPANTS"
                    icon={Users}
                    type="number"
                    value={editData.maxJoining}
                    onChange={e => setEditData({...editData, maxJoining: parseInt(e.target.value)})}
                  />

                  {/* Privacy Settings in Edit Mode */}
                  <div style={{ 
                    background: 'var(--highlight-muted)', padding: '20px', borderRadius: '16px', 
                    display: 'flex', flexDirection: 'column', gap: '16px', border: '1px solid var(--border-color)',
                    marginTop: '8px'
                  }}>
                    <h3 style={{ fontSize: '14px', fontWeight: 800, margin: 0, display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span role="img" aria-label="shield">🛡️</span> 프라이버시 설정
                    </h3>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                      {[
                        { key: 'isCommentPublic', label: '댓글 전체 공개', sub: '참여하지 않은 사람도 댓글 가능' },
                        { key: 'isGalleryPublic', label: '갤러리 전체 공개', sub: '누구나 사진첩 조회 가능' },
                        { key: 'isChatPublic', label: '채팅방 전체 공개', sub: '미참여자도 채팅 내용 조회 가능' }
                      ].map(item => (
                        <div key={item.key} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div>
                            <div style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-primary)' }}>{item.label}</div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{item.sub}</div>
                          </div>
                          <button 
                            type="button"
                            onClick={() => setEditData({...editData, [item.key]: !editData[item.key]})}
                            style={{
                              width: '42px', height: '22px', borderRadius: '11px',
                              background: editData[item.key] ? 'var(--primary-orange)' : '#DEE2E6',
                              position: 'relative', border: 'none', transition: 'all 0.2s', cursor: 'pointer'
                            }}
                          >
                            <div style={{
                              width: '16px', height: '16px', borderRadius: '50%', background: 'white',
                              position: 'absolute', top: '3px', left: editData[item.key] ? '23px' : '3px',
                              transition: 'all 0.2s', boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                            }} />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
                    <PrimaryButton onClick={handleUpdate} style={{ flex: 1, height: '54px' }}>저장하기</PrimaryButton>
                    <button onClick={() => setIsEditing(false)} style={{ flex: 1, padding: '14px', background: 'var(--bg-lite)', color: 'var(--text-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px', fontWeight: 800, cursor: 'pointer' }}>
                      취소
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  {gathering.bgImageUrl && (
                    <div style={{
                       height: '180px',
                       borderRadius: '20px',
                       backgroundImage: `url(${gathering.bgImageUrl})`,
                       backgroundSize: 'cover',
                       backgroundPosition: 'center',
                       marginBottom: '24px',
                       boxShadow: '0 8px 16px rgba(0,0,0,0.1)'
                    }} />
                  )}

                  {!gathering.linkedItinerary && (
                    <div style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '6px',
                      background: '#FFF9DB',
                      color: '#F08C00',
                      padding: '8px 16px',
                      borderRadius: '12px',
                      fontSize: '13px',
                      fontWeight: 800,
                      border: '1px solid #FFE066',
                      marginBottom: '16px'
                    }}>
                      <span>🎫</span> 스탠바이 번개 모임 (일정 없음)
                    </div>
                  )}

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', padding: '20px', background: 'var(--bg-color)', borderRadius: '20px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '15px', color: 'var(--text-primary)', fontWeight: 600 }}>
                      <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
                        <Calendar size={18} color="var(--primary-orange)" />
                      </div>
                      <span>{gathering.dates || (gathering.startDate ? `${gathering.startDate}${gathering.endDate ? ' ~ ' + gathering.endDate : ''}` : '미지정')}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '15px', color: 'var(--text-primary)', fontWeight: 600 }}>
                      <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
                        <MapPin size={18} color="#FF6B6B" />
                      </div>
                      <span>{gathering.location}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '15px', color: 'var(--text-primary)', fontWeight: 600 }}>
                      <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
                        <Users size={18} color="#4DABF7" />
                      </div>
                      <span>{gathering.currentJoining} / {gathering.maxJoining} 명 참여 중</span>
                    </div>

                    {gathering.linkedItinerary && (
                      <div 
                        onClick={() => navigate(`/itinerary/${gathering.linkedItinerary.id}`)}
                        style={{ 
                          marginTop: '8px', padding: '16px', background: 'white', borderRadius: '16px', 
                          border: '1px solid var(--border-color)', cursor: 'pointer',
                          display: 'flex', flexDirection: 'column', gap: '8px',
                          transition: 'transform 0.2s'
                        }}
                        onMouseDown={e => e.currentTarget.style.transform = 'scale(0.98)'}
                        onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <span className="label-orange" style={{ margin: 0 }}>LINKED ITINERARY</span>
                          <Plane size={14} color="var(--primary-orange)" />
                        </div>
                        <div style={{ fontSize: '16px', fontWeight: 800, color: 'var(--text-primary)' }}>
                          {gathering.linkedItinerary.title}
                        </div>
                        <div style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 500 }}>
                          {gathering.linkedItinerary.description}
                        </div>
                      </div>
                    )}
                  </div>

                  <button
                    onClick={handleJoin}
                    disabled={isHost || gathering.currentJoining >= gathering.maxJoining || myStatus}
                    style={{
                      width: '100%', padding: '18px',
                      background: isHost ? '#51CF66' : (gathering.currentJoining >= gathering.maxJoining && !myStatus) ? 'var(--text-muted)' : myStatus === MemberStatus.PENDING ? '#FFD43B' : myStatus === MemberStatus.APPROVED ? '#51CF66' : 'var(--primary-gradient)',
                      color: 'white', border: 'none', borderRadius: '20px', fontSize: '17px', fontWeight: 800, marginTop: '24px', cursor: isHost ? 'default' : 'pointer',
                      boxShadow: (!isHost && gathering.currentJoining < gathering.maxJoining && !myStatus) ? '0 10px 20px rgba(255, 92, 0, 0.3)' : 'none'
                    }}
                  >
                    {isHost ? '탑승 확정됨 (호스트)' : 
                      myStatus === MemberStatus.PENDING ? '신청 대기 중...' :
                      myStatus === MemberStatus.APPROVED ? '참여 확정됨!' :
                        myStatus === MemberStatus.REJECTED ? '거절된 모임입니다' :
                          gathering.currentJoining >= gathering.maxJoining ? '마감되었습니다' : '참여 신청하기'}
                  </button>

                  {isMember && !gathering.linkedItinerary && (
                    <div style={{ marginTop: '16px' }}>
                      {gathering.hasCheckedIn ? (
                        <div style={{
                          width: '100%', padding: '18px', background: '#E2E8F0', color: '#64748B',
                          borderRadius: '20px', fontSize: '17px', fontWeight: 800, textAlign: 'center',
                          border: '1px solid #CBD5E1', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px'
                        }}>
                          <span>✅</span> 체크인 완료됨 (+50 PTS)
                        </div>
                      ) : (
                        <button
                          onClick={handleCheckin}
                          style={{
                            width: '100%', padding: '18px', background: '#fab005', color: 'white',
                            border: 'none', borderRadius: '20px', fontSize: '17px', fontWeight: 800,
                            cursor: 'pointer', boxShadow: '0 8px 20px rgba(250, 176, 5, 0.25)',
                            transition: 'all 0.2s', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px'
                          }}
                        >
                          <span>🎫</span> 현장 체크인하기 (+50 PTS)
                        </button>
                      )}
                      <p style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '8px', fontWeight: 500 }}>
                        * 모임 종료 혹은 당일 체크인 시 마일리지 마킹 및 50포인트가 즉시 지급됩니다.
                      </p>
                    </div>
                  )}

                </>
              )}
            </div>
          )}

          {activeTab === '크루' && (
            <div className="animate-fade">
              {isMember ? (
                <>
                  {/* Host Section */}
                  {isHost && (
                    <div style={{ marginBottom: '32px' }}>
                      <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '16px', color: 'var(--text-primary)' }}>참여 신청 관리</h3>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {gathering.members?.filter(m => m.status === MemberStatus.PENDING && (!currentUser || m.user.id !== currentUser.id)).map(req => (
                          <div key={req.user.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', background: 'var(--bg-color)', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                              <div style={{ width: '40px', height: '40px', borderRadius: '20px', background: 'var(--border-color)', overflow: 'hidden' }}>
                                {req.user.profileImageUrl ? <img src={req.user.profileImageUrl} style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#E2E8F0', fontSize: '20px' }}>👤</div>}
                              </div>
                              <span style={{ fontSize: '15px', fontWeight: 700 }}>{req.user.name}</span>
                            </div>
                            <div style={{ display: 'flex', gap: '8px' }}>
                              <button onClick={() => handleApprove(req.user.id)} style={{ width: '36px', height: '36px', background: 'rgba(81, 207, 102, 0.1)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <CheckCircle size={20} color="#51CF66" />
                              </button>
                              <button onClick={() => handleReject(req.user.id)} style={{ width: '36px', height: '36px', background: 'rgba(255, 107, 107, 0.1)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <XCircle size={20} color="#FF6B6B" />
                              </button>
                            </div>
                          </div>
                        ))}
                        {gathering.members?.filter(m => m.status === MemberStatus.PENDING).length === 0 && (
                          <p style={{ fontSize: '14px', color: 'var(--text-muted)', textAlign: 'center', padding: '20px', background: 'var(--bg-color)', borderRadius: '16px' }}>새로운 참가 신청이 없습니다.</p>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Confirmed Members Section */}
                  <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '16px', color: 'var(--text-primary)' }}>참여 중인 멤버</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '12px' }}>
                    {gathering.members?.filter(m => m.status === MemberStatus.APPROVED).map(req => (
                      <div key={req.user.id} style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '10px', background: 'var(--bg-color)', borderRadius: '12px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '16px', background: 'var(--border-color)', overflow: 'hidden' }}>
                          {req.user.profileImageUrl ? <img src={req.user.profileImageUrl} style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#E2E8F0', fontSize: '14px' }}>👤</div>}
                        </div>
                        <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>{req.user.name}</span>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div style={{ textAlign: 'center', padding: '80px 20px', background: 'white', borderRadius: '24px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '48px', marginBottom: '20px' }}>🔒</div>
                  <h4 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '8px' }}>탑승객 전용 구역</h4>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: 600, lineHeight: 1.6 }}>크루 명단은 승인된 탑승객만 확인할 수 있는 보안 사항입니다. 참여 신청 후 승인을 기다려주세요!</p>
                </div>
              )}
            </div>
          )}

          {activeTab === '무전' && (
            <div className="animate-fade" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              {(isMember || gathering.isCommentPublic) ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '80px' }}>
                  {comments.map(c => (
                    <div key={c.id} style={{ display: 'flex', gap: '12px' }}>
                      <div style={{ width: '36px', height: '36px', borderRadius: '18px', background: 'var(--bg-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontWeight: 700, color: 'var(--text-muted)', border: '1px solid var(--border-color)' }}>
                        {c.author.slice(0, 1)}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '4px' }}>
                          <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-primary)' }}>{c.author}</span>
                          <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 500 }}>{new Date(c.createdAt).toLocaleDateString()}</span>
                        </div>
                        <div style={{ background: 'var(--bg-color)', padding: '12px 16px', borderRadius: '0 16px 16px 16px', fontSize: '15px', color: 'var(--text-primary)', display: 'inline-block', border: '1px solid rgba(0,0,0,0.03)' }}>
                          {c.content}
                        </div>
                      </div>
                    </div>
                  ))}
                  {comments.length === 0 && (
                    <div style={{ textAlign: 'center', padding: '60px 0' }}>
                      <p style={{ fontSize: '15px', color: 'var(--text-muted)', fontWeight: 600 }}>궁금한 점을 무전으로 남겨보세요! 💬</p>
                    </div>
                  )}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '80px 20px', background: 'white', borderRadius: '24px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '48px', marginBottom: '20px' }}>📻</div>
                  <h4 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '8px' }}>무전 교신 제한</h4>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: 600, lineHeight: 1.6 }}>라운지 밖에서의 무전은 승인된 크루들끼리만 가능합니다. 참여 신청 후 멤버들과 소통해보세요.</p>
                </div>
              )}
            </div>
          )}

          {activeTab === '갤러리' && (
            <div className="animate-fade" style={{ height: '100%', minHeight: '400px' }}>
              {(isMember || gathering.isGalleryPublic) ? (
                <GatheringFeed gatheringId={gathering.id} currentUser={currentUser} />
              ) : (
                <div style={{ textAlign: 'center', padding: '80px 20px', background: 'white', borderRadius: '24px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '48px', marginBottom: '20px' }}>📷</div>
                  <h4 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '8px' }}>갤러리 접근 제한</h4>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', fontWeight: 600, lineHeight: 1.6 }}>여행의 소중한 기록은 함께 떠나는 크루들만 공유할 수 있습니다.</p>
                </div>
              )}
            </div>
          )}

          {activeTab === '채팅' && (
            <div className="animate-fade" style={{ height: 'calc(100vh - 250px)', position: 'relative' }}>
              <ChatRoom 
                gathering={gathering} 
                onBack={() => setActiveTab('브리핑')} 
                inline={true} 
              />
            </div>
          )}
        </div>

        {/* Comment Input Wrapper - Only shown for Taik tab */}
        {activeTab === '무전' && (isMember || gathering.isCommentPublic) && (
          <div style={{
            padding: '12px 24px 24px 24px', background: 'var(--surface)', borderTop: '1px solid var(--border-color)',
            display: 'flex', gap: '8px'
          }}>
            <input
              type="text" value={newComment} onChange={e => setNewComment(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') handlePostComment(); }}
              placeholder="호스트에게 질문을 남겨주세요..."
              style={{
                flex: 1, padding: '14px 20px', borderRadius: '24px', border: '1px solid var(--border-color)',
                background: 'var(--bg-color)', fontSize: '15px', outline: 'none', fontWeight: 500
              }}
            />
            <button onClick={handlePostComment} style={{
              width: '48px', height: '48px', borderRadius: '24px', background: 'var(--primary-gradient)', color: 'white',
              border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              boxShadow: '0 4px 12px rgba(255, 92, 0, 0.2)'
            }}>
              <Send size={20} style={{ marginLeft: '2px' }} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default GatheringDetailPage;
