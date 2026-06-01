import React, { useState } from 'react';
import { Globe, RefreshCw } from 'lucide-react';

const TranslatorWidget = ({ country }) => {
  const [text, setText] = useState('');
  const [translated, setTranslated] = useState('');
  const [loading, setLoading] = useState(false);

  // 이 부분은 실제 연동 전 모의 로직입니다.
  const handleTranslate = () => {
    if (!text.trim()) return;
    setLoading(true);
    setTimeout(() => {
      setTranslated(`[${country} 번역 시뮬레이션] ${text}`);
      setLoading(false);
    }, 600);
  };

  const PRESETS = [
    "이거 얼마예요?",
    "가장 가까운 역이 어디인가요?",
    "화장실은 어디에 있나요?",
    "메뉴판 좀 주시겠어요?"
  ];

  return (
    <div style={{ background: 'white', padding: '20px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
        <Globe size={20} color="var(--primary-orange)" />
        <h3 style={{ fontSize: '16px', fontWeight: 800 }}>간편 번역기 ({country})</h3>
      </div>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px' }}>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="번역할 내용을 입력하세요"
          style={{
            width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid var(--border-color)',
            minHeight: '80px', resize: 'none', outline: 'none', fontSize: '14px', fontWeight: 600
          }}
        />
        <button onClick={handleTranslate} disabled={loading} style={{
          padding: '12px', borderRadius: '12px', background: 'var(--text-primary)',
          color: 'white', border: 'none', fontWeight: 800, cursor: 'pointer',
          display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px'
        }}>
          {loading ? <RefreshCw className="animate-spin" size={18} /> : '번역하기'}
        </button>
      </div>

      {translated && (
        <div style={{ padding: '16px', background: 'var(--bg-lite)', borderRadius: '12px', marginBottom: '20px' }}>
          <p style={{ fontSize: '15px', fontWeight: 700, color: 'var(--primary-orange)' }}>번역 결과</p>
          <p style={{ fontSize: '16px', fontWeight: 800, color: 'var(--text-primary)', marginTop: '8px' }}>
            {translated}
          </p>
        </div>
      )}

      <div>
        <h4 style={{ fontSize: '14px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '12px' }}>자주 쓰는 회화</h4>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {PRESETS.map((preset, idx) => (
            <button key={idx} onClick={() => setText(preset)} style={{
              textAlign: 'left', padding: '12px', borderRadius: '12px',
              background: 'var(--bg-lite)', border: 'none', fontSize: '14px', fontWeight: 600,
              color: 'var(--text-primary)', cursor: 'pointer'
            }}>
              {preset}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TranslatorWidget;
