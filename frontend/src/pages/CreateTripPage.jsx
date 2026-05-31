import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Plane } from 'lucide-react';
import { authFetch } from '../api/client';

const COUNTRIES = [
  { code: 'KR', name: '한국', flag: '🇰🇷' },
  { code: 'JP', name: '일본', flag: '🇯🇵' },
  { code: 'TH', name: '태국', flag: '🇹🇭' },
  { code: 'VN', name: '베트남', flag: '🇻🇳' },
  { code: 'US', name: '미국', flag: '🇺🇸' },
  { code: 'FR', name: '프랑스', flag: '🇫🇷' },
  { code: 'IT', name: '이탈리아', flag: '🇮🇹' },
  { code: 'ES', name: '스페인', flag: '🇪🇸' },
  { code: 'GB', name: '영국', flag: '🇬🇧' },
  { code: 'CN', name: '중국', flag: '🇨🇳' },
  { code: 'TW', name: '대만', flag: '🇹🇼' },
  { code: 'PH', name: '필리핀', flag: '🇵🇭' },
];

const CreateTripPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '', destination: '', country: 'KR',
    startDate: '', endDate: '', bgImageUrl: ''
  });
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) return alert('여행 제목을 입력해주세요.');
    setSaving(true);
    try {
      const res = await authFetch('/api/trips', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      if (res.ok) {
        const trip = await res.json();
        navigate(`/trip/${trip.id}`);
      }
    } catch (err) {
      alert('여행 생성에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const inputStyle = {
    width: '100%', padding: '14px 18px', borderRadius: '14px',
    border: '1px solid var(--border-color)', fontSize: '15px',
    fontWeight: 600, background: 'white', color: 'var(--text-primary)',
    outline: 'none'
  };

  return (
    <div style={{ background: 'var(--bg-lite)', minHeight: '100vh', paddingBottom: '100px' }}>
      <header className="glass page-header" style={{
        display: 'flex', alignItems: 'center', gap: '12px',
        padding: '20px', position: 'sticky', top: 0, zIndex: 100,
        background: 'rgba(255,255,255,0.9)', borderBottom: '1px solid var(--border-color)'
      }}>
        <button onClick={() => navigate(-1)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
          <ArrowLeft size={24} color="var(--text-primary)" />
        </button>
        <div>
          <span className="label-orange">NEW TRIP</span>
          <h1 className="heading-l" style={{ marginTop: '2px' }}>새 여행 만들기</h1>
        </div>
      </header>

      <form onSubmit={handleSubmit} style={{ padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div>
          <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>여행 제목 *</label>
          <input style={inputStyle} placeholder="예: 제주도 힐링 여행"
            value={form.title} onChange={e => setForm({...form, title: e.target.value})} />
        </div>

        <div>
          <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>여행지</label>
          <input style={inputStyle} placeholder="예: 제주도, 오사카, 방콕"
            value={form.destination} onChange={e => setForm({...form, destination: e.target.value})} />
        </div>

        <div>
          <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>국가</label>
          <select style={inputStyle} value={form.country} onChange={e => setForm({...form, country: e.target.value})}>
            {COUNTRIES.map(c => (
              <option key={c.code} value={c.code}>{c.flag} {c.name}</option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <div style={{ flex: 1 }}>
            <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>시작일</label>
            <input type="date" style={inputStyle}
              value={form.startDate} onChange={e => setForm({...form, startDate: e.target.value})} />
          </div>
          <div style={{ flex: 1 }}>
            <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>종료일</label>
            <input type="date" style={inputStyle}
              value={form.endDate} onChange={e => setForm({...form, endDate: e.target.value})} />
          </div>
        </div>

        <button type="submit" className="primary-btn" disabled={saving}
          style={{ padding: '16px', fontSize: '16px', fontWeight: 900, borderRadius: '16px', marginTop: '12px' }}>
          <Plane size={20} style={{ marginRight: '8px' }} />
          {saving ? '생성 중...' : '여행 만들기'}
        </button>
      </form>
    </div>
  );
};

export default CreateTripPage;
