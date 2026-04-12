import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { X, MapPin, Calendar as CalendarIcon, Users, Type, Camera, Clock, ChevronLeft } from 'lucide-react';
import { authFetch } from '../api/client';
import { useUser } from '../contexts/UserContext';
import gatheringPlaceholder from '../assets/gathering-placeholder.png';

const CATEGORIES = [
  { label: '밥/카페', icon: '🍚' },
  { label: '술자리', icon: '🍻' },
  { label: '운동', icon: '🏃‍♂️' },
  { label: '여행', icon: '✈️' },
  { label: '문화/취미', icon: '🎫' },
];

const DEFAULT_BG = gatheringPlaceholder;

const CreateGatheringPage = () => {
  const navigate = useNavigate();
  const { user: currentUser } = useUser();
  const [formData, setFormData] = useState({
    title: '',
    location: '',
    date: '',
    time: '',
    maxJoining: 4,
    category: '밥/카페'
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const fileInputRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCategorySelect = (cat) => {
    setFormData(prev => ({ ...prev, category: cat }));
  }

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let finalBgImageUrl = null;

    if (selectedFile) {
      const formDataUpload = new FormData();
      formDataUpload.append('file', selectedFile);
      try {
        const uploadRes = await authFetch('/api/files/upload', {
          method: 'POST',
          body: formDataUpload,
        });
        if (uploadRes.ok) {
          const { url } = await uploadRes.json();
          finalBgImageUrl = url;
        } else {
          console.error("Upload failed");
        }
      } catch (err) {
        console.error("Upload error", err);
      }
    }

    const newGathering = {
      ...formData,
      dates: `${formData.date} ${formData.time}`,
      maxJoining: parseInt(formData.maxJoining, 10),
      currentJoining: 1,
      bgImageUrl: finalBgImageUrl || DEFAULT_BG
    };

    try {
      const response = await authFetch('/api/gatherings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newGathering),
      });

      if (response.ok) {
        navigate(-1);
      } else {
        const errText = await response.text();
        alert(`저장에 실패했습니다 (서버 오류 ${response.status}):\n${errText}`);
      }
    } catch (error) {
      console.error("Error creating gathering:", error);
      alert(`네트워크 오류가 발생했습니다: ${error.message}`);
    }
  };

  const inputStyle = {
    width: '100%', padding: '16px 16px 16px 44px', borderRadius: '16px', 
    border: '1px solid var(--border-color)', background: 'var(--bg-color)', 
    fontSize: '16px', fontWeight: 500, outline: 'none', transition: 'border 0.2s',
    color: 'var(--text-primary)'
  };

  return (
    <div className="app-container animate-fade" style={{ background: 'white', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{ 
        display: 'flex', alignItems: 'center', padding: '20px', gap: '16px', background: 'white', 
        position: 'sticky', top: 0, zIndex: 100, borderBottom: '1px solid var(--border-color)',
        borderRadius: '0 0 24px 24px'
      }}>
        <button onClick={() => navigate(-1)} className="icon-circle" style={{ width: '40px', height: '40px', padding: 0 }}>
          <ChevronLeft size={24} color="var(--text-primary)" />
        </button>
        <h1 className="heading-m" style={{ margin: 0, fontSize: '20px' }}>새로운 모임 열기 🚀</h1>
      </header>
      
      <div style={{ flex: 1, padding: '24px', overflowY: 'auto', paddingBottom: '100px' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '15px', marginBottom: '24px' }}>
          관심사가 맞는 동네 이웃들과 함께해요!
        </p>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Image Upload Area */}
          <div>
            <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>커버 이미지</label>
            <div 
              onClick={() => fileInputRef.current.click()}
              style={{
                width: '100%', height: '140px', borderRadius: '16px',
                background: previewUrl ? `url(${previewUrl}) center/cover` : 'var(--bg-color)',
                border: previewUrl ? 'none' : '1px dashed var(--text-muted)',
                display: 'flex', flexDirection: 'column',
                justifyContent: 'center', alignItems: 'center',
                cursor: 'pointer', position: 'relative', overflow: 'hidden'
              }}
            >
              {!previewUrl && (
                <>
                  <Camera size={28} color="var(--text-muted)" style={{ marginBottom: '8px' }}/>
                  <span style={{ fontSize: '14px', color: 'var(--text-muted)', fontWeight: 500 }}>사진을 추가해주세요 (선택)</span>
                </>
              )}
              {previewUrl && (
                <div style={{ position: 'absolute', bottom: '8px', right: '8px', background: 'rgba(0,0,0,0.6)', padding: '6px 10px', borderRadius: '12px', color: 'white', fontSize: '12px', fontWeight: 600 }}>
                  변경
                </div>
              )}
            </div>
            <input 
              type="file" accept="image/*" 
              ref={fileInputRef} style={{ display: 'none' }} 
              onChange={handleImageChange}
            />
          </div>

          {/* Category Selector */}
          <div>
            <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '12px', color: 'var(--text-main)' }}>어떤 모임인가요?</label>
            <div className="hide-scrollbar" style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px', WebkitOverflowScrolling: 'touch' }}>
              {CATEGORIES.map(cat => (
                <button 
                  key={cat.label}
                  type="button"
                  onClick={() => handleCategorySelect(cat.label)}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '6px',
                    padding: '10px 16px', borderRadius: '20px', whiteSpace: 'nowrap',
                    background: formData.category === cat.label ? 'var(--primary)' : 'var(--bg-color)',
                    color: formData.category === cat.label ? 'white' : 'var(--text-main)',
                    fontWeight: formData.category === cat.label ? 700 : 600,
                    transition: 'all 0.2s',
                    border: formData.category === cat.label ? '1px solid var(--primary)' : '1px solid transparent'
                  }}
                >
                  <span style={{ fontSize: '16px' }}>{cat.icon}</span> {cat.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>모임 제목</label>
            <div style={{ position: 'relative' }}>
              <Type size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
              <input 
                required name="title" value={formData.title} onChange={handleChange}
                placeholder="예: 이번 주말 한강 피크닉 갈 사람~" 
                style={inputStyle}
                onFocus={(e) => e.target.style.border = '1px solid var(--primary-orange)'}
                onBlur={(e) => e.target.style.border = '1px solid var(--border-color)'}
              />
            </div>
          </div>
          
          <div>
            <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>만나는 장소</label>
            <div style={{ position: 'relative' }}>
              <MapPin size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
              <input 
                required name="location" value={formData.location} onChange={handleChange}
                placeholder="예: 강남역 10번 출구 앞" 
                style={inputStyle}
                onFocus={(e) => e.target.style.border = '1px solid var(--primary-orange)'}
                onBlur={(e) => e.target.style.border = '1px solid var(--border-color)'}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '16px', flexDirection: 'column' }}>
            <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '-8px', color: 'var(--text-main)' }}>언제 만나나요?</label>
            
            <div style={{ display: 'flex', gap: '12px' }}>
              <div style={{ flex: 1, position: 'relative' }}>
                <CalendarIcon size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                <input 
                  required type="date" name="date" value={formData.date} onChange={handleChange}
                  style={{...inputStyle, paddingLeft: '44px', color: formData.date ? 'var(--text-primary)' : 'transparent', textShadow: formData.date ? 'none' : '0 0 0 var(--text-muted)'}}
                  onFocus={(e) => { e.target.style.border = '1px solid var(--primary-orange)'; e.target.style.color = 'var(--text-primary)'; e.target.style.textShadow = 'none'; }}
                  onBlur={(e) => { e.target.style.border = '1px solid var(--border-color)'; if(!e.target.value) { e.target.style.color = 'transparent'; e.target.style.textShadow = '0 0 0 var(--text-muted)';} }}
                />
              </div>

              <div style={{ flex: 1, position: 'relative' }}>
                <Clock size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                <input 
                  required type="time" name="time" value={formData.time} onChange={handleChange}
                  style={{...inputStyle, paddingLeft: '44px', color: formData.time ? 'var(--text-primary)' : 'transparent', textShadow: formData.time ? 'none' : '0 0 0 var(--text-muted)'}}
                  onFocus={(e) => { e.target.style.border = '1px solid var(--primary-orange)'; e.target.style.color = 'var(--text-primary)'; e.target.style.textShadow = 'none'; }}
                  onBlur={(e) => { e.target.style.border = '1px solid var(--border-color)'; if(!e.target.value) { e.target.style.color = 'transparent'; e.target.style.textShadow = '0 0 0 var(--text-muted)';} }}
                />
              </div>
            </div>
            
            <div style={{ width: '120px' }}>
              <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>최대 인원</label>
              <div style={{ position: 'relative' }}>
                <Users size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                <input 
                  required type="number" min="2" max="20" name="maxJoining" value={formData.maxJoining} onChange={handleChange}
                  style={{ ...inputStyle, paddingLeft: '44px' }}
                  onFocus={(e) => e.target.style.border = '1px solid var(--primary-orange)'}
                  onBlur={(e) => e.target.style.border = '1px solid var(--border-color)'}
                />
              </div>
            </div>
          </div>

          <button type="submit" style={{ 
            background: 'var(--primary-gradient)', color: 'white',
            border: 'none', cursor: 'pointer',
            height: '60px', borderRadius: '20px', fontSize: '18px', fontWeight: 800, 
            marginTop: '16px', boxShadow: '0 8px 24px rgba(255, 92, 0, 0.3)',
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}
          onMouseDown={e => e.currentTarget.style.transform = 'scale(0.98)'}
          onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
          onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
          >
            모집 시작하기
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateGatheringPage;
