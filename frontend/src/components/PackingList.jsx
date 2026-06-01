import React, { useState, useEffect } from 'react';
import { authFetch } from '../api/client';
import { Plus, Trash2, CheckCircle, Circle } from 'lucide-react';

const PackingList = ({ tripId }) => {
  const [items, setItems] = useState([]);
  const [newItemName, setNewItemName] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchItems();
  }, [tripId]);

  const fetchItems = async () => {
    try {
      const res = await authFetch(`/api/trips/${tripId}/packing`);
      if (res.ok) setItems(await res.json());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const initDefaultItems = async () => {
    if (!window.confirm('기본 준비물을 불러오시겠습니까?')) return;
    try {
      const res = await authFetch(`/api/trips/${tripId}/packing/init`, { method: 'POST' });
      if (res.ok) setItems(await res.json());
    } catch (err) {
      console.error(err);
    }
  };

  const addItem = async (e) => {
    e.preventDefault();
    if (!newItemName.trim()) return;
    try {
      const res = await authFetch(`/api/trips/${tripId}/packing`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: newItemName, category: '기타' })
      });
      if (res.ok) {
        setItems([...items, await res.json()]);
        setNewItemName('');
      }
    } catch (err) {
      console.error(err);
    }
  };

  const toggleCheck = async (itemId) => {
    try {
      const res = await authFetch(`/api/trips/${tripId}/packing/${itemId}/toggle`, { method: 'PATCH' });
      if (res.ok) {
        const updated = await res.json();
        setItems(items.map(i => i.id === itemId ? updated : i));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const deleteItem = async (itemId) => {
    try {
      const res = await authFetch(`/api/trips/${tripId}/packing/${itemId}`, { method: 'DELETE' });
      if (res.ok) {
        setItems(items.filter(i => i.id !== itemId));
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div>로딩 중...</div>;

  const groupedItems = items.reduce((acc, item) => {
    if (!acc[item.category]) acc[item.category] = [];
    acc[item.category].push(item);
    return acc;
  }, {});

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '18px', fontWeight: 900 }}>준비물 체크리스트</h3>
      </div>

      <form onSubmit={addItem} style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        <input 
          type="text" value={newItemName} onChange={e => setNewItemName(e.target.value)}
          placeholder="추가할 준비물 입력" 
          style={{
            flex: 1, padding: '12px', borderRadius: '12px', border: '1px solid var(--border-color)',
            outline: 'none', fontSize: '14px', fontWeight: 600
          }}
        />
        <button type="submit" style={{
          padding: '12px', borderRadius: '12px', background: 'var(--text-primary)',
          color: 'white', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center'
        }}>
          <Plus size={20} />
        </button>
      </form>

      {Object.entries(groupedItems).map(([category, catItems]) => (
        <div key={category} style={{ marginBottom: '20px', background: 'white', padding: '16px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
          <h4 style={{ fontSize: '15px', fontWeight: 800, marginBottom: '12px', color: 'var(--text-primary)' }}>{category}</h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {catItems.map(item => (
              <div key={item.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div onClick={() => toggleCheck(item.id)} style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', flex: 1 }}>
                  {item.checked ? <CheckCircle size={20} color="#10b981" /> : <Circle size={20} color="var(--border-color)" />}
                  <span style={{ fontSize: '15px', fontWeight: 600, color: item.checked ? 'var(--text-sub)' : 'var(--text-primary)', textDecoration: item.checked ? 'line-through' : 'none' }}>
                    {item.name}
                  </span>
                </div>
                <button onClick={() => deleteItem(item.id)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer' }}>
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        </div>
      ))}
      
      {items.length === 0 && (
        <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)', fontWeight: 600 }}>
          등록된 준비물이 없습니다.
        </div>
      )}
    </div>
  );
};

export default PackingList;
