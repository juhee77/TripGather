import React, { useState, useRef } from 'react';
import { X, MapPin, Calendar as CalendarIcon, Users, Type, Camera } from 'lucide-react';

const CATEGORIES = [
  { label: '밥/카페', icon: '🍚' },
  { label: '술자리', icon: '🍻' },
  { label: '운동', icon: '🏃‍♂️' },
  { label: '여행', icon: '✈️' },
  { label: '문화/취미', icon: '🎫' },
];

const QUICK_DATES = ['오늘 저녁', '내일 낮', '이번 주말'];

const CreateGatheringModal = ({ onClose, onCreated }) => {
  const [formData, setFormData] = useState({
    title: '',
    host: 'Jihyun (지현)', 
    location: '',
    dates: '',
    maxJoining: 4,
    category: '밥/카페'
  });
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
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 모임 피드가 다채롭게 보이도록 샘플 그림 무작위 배정 (이미지 업로드 안 한 경우)
    const bgImages = [
      'https://images.unsplash.com/photo-1546872957-3f746681498b?auto=format&fit=crop&q=80&w=600',
      'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&q=80&w=600',
      'https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&q=80&w=600',
      'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?auto=format&fit=crop&q=80&w=600',
    ];
    const defaultBg = bgImages[Math.floor(Math.random() * bgImages.length)];

    const newGathering = {
      ...formData,
      currentJoining: 1,
      bgImageUrl: previewUrl || defaultBg
    };

    try {
      const response = await fetch('http://localhost:8080/api/gatherings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newGathering),
      });

      if (response.ok) {
        const savedGathering = await response.json();
        onCreated(savedGathering); 
        onClose(); 
      }
    } catch (error) {
      console.error("Error creating gathering:", error);
    }
  };

  const inputStyle = {
    width: '100%', padding: '16px 16px 16px 44px', borderRadius: '16px', 
    border: '1px solid var(--border)', background: 'var(--bg-color)', 
    fontSize: '16px', fontWeight: 500, outline: 'none', transition: 'border 0.2s',
    color: 'var(--text-main)'
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
      backgroundColor: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)', zIndex: 1000, 
      display: 'flex', justifyContent: 'center', alignItems: 'flex-end'
    }}>
      <div style={{
        background: 'var(--surface)', width: '100%', maxWidth: '480px', maxHeight: '90vh', overflowY: 'auto',
        borderTopLeftRadius: '28px', borderTopRightRadius: '28px', 
        padding: '30px 24px', animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
        boxShadow: '0 -10px 40px rgba(0,0,0,0.1)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: 800 }}>새로운 모임 열기 🚀</h2>
          <button onClick={onClose} style={{ 
            padding: '8px', background: 'var(--bg-color)', borderRadius: '50%',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <X size={20} color="var(--text-main)" />
          </button>
        </div>
        <p style={{ color: 'var(--text-sub)', fontSize: '15px', marginBottom: '24px' }}>
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
                border: previewUrl ? 'none' : '1px dashed var(--text-sub)',
                display: 'flex', flexDirection: 'column',
                justifyContent: 'center', alignItems: 'center',
                cursor: 'pointer', position: 'relative', overflow: 'hidden'
              }}
            >
              {!previewUrl && (
                <>
                  <Camera size={28} color="var(--text-sub)" style={{ marginBottom: '8px' }}/>
                  <span style={{ fontSize: '14px', color: 'var(--text-sub)', fontWeight: 500 }}>사진을 추가해주세요 (선택)</span>
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
                onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
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
                onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '16px' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>
                <span>언제 만나나요?</span>
              </label>
              
              <div style={{ position: 'relative' }}>
                <CalendarIcon size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                <input 
                  required name="dates" value={formData.dates} onChange={handleChange}
                  placeholder="예: 모레 저녁 7시" 
                  style={inputStyle}
                  onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                  onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
                />
              </div>
              <div style={{ display: 'flex', gap: '6px', marginTop: '10px' }}>
                {QUICK_DATES.map(qd => (
                  <button
                    key={qd} type="button"
                    onClick={() => setFormData(p => ({...p, dates: qd}))}
                    style={{
                      padding: '6px 12px', borderRadius: '12px', fontSize: '13px', fontWeight: 600,
                      background: 'var(--bg-color)', color: 'var(--text-sub)', border: 'none'
                    }}
                  >{qd}</button>
                ))}
              </div>
            </div>
            
            <div style={{ width: '120px' }}>
              <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>최대 인원</label>
              <div style={{ position: 'relative' }}>
                <Users size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                <input 
                  required type="number" min="2" max="20" name="maxJoining" value={formData.maxJoining} onChange={handleChange}
                  style={{ ...inputStyle, paddingLeft: '44px' }}
                  onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                  onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
                />
              </div>
            </div>
          </div>

          <button type="submit" style={{ 
            background: 'var(--primary)', color: 'white',
            border: 'none', cursor: 'pointer',
            height: '60px', borderRadius: '20px', fontSize: '18px', fontWeight: 700, 
            marginTop: '16px', boxShadow: '0 8px 16px rgba(255, 123, 84, 0.3)',
            transition: 'transform 0.1s'
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

export default CreateGatheringModal;
