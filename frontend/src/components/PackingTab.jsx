import React, { useState, useEffect } from 'react';
import { CheckSquare, Square, Plus, Trash2, CheckCircle2, RefreshCw } from 'lucide-react';
import {
  getPackingItems,
  initDefaultPackingItems,
  addPackingItem,
  togglePackingItem,
  deletePackingItem,
  getPackingProgress
} from '../api/packing';
import './PackingTab.css';

export default function PackingTab({ tripId }) {
  const [items, setItems] = useState([]);
  const [progress, setProgress] = useState({ totalCount: 0, checkedCount: 0, progressPercentage: 0 });
  const [loading, setLoading] = useState(true);
  const [newItemName, setNewItemName] = useState('');
  const [newItemCategory, setNewItemCategory] = useState('필수');

  const fetchData = async () => {
    try {
      setLoading(true);
      const [itemList, progressData] = await Promise.all([
        getPackingItems(tripId),
        getPackingProgress(tripId)
      ]);
      setItems(itemList);
      setProgress(progressData);
    } catch (err) {
      console.error('Failed to load packing items:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tripId) {
      fetchData();
    }
  }, [tripId]);

  const handleInitDefaults = async () => {
    try {
      await initDefaultPackingItems(tripId);
      fetchData();
    } catch (err) {
      alert('기본 준비물 생성 실패');
    }
  };

  const handleAddItem = async (e) => {
    e.preventDefault();
    if (!newItemName.trim()) return;

    try {
      await addPackingItem(tripId, newItemName.trim(), newItemCategory);
      setNewItemName('');
      fetchData();
    } catch (err) {
      alert('준비물 추가 실패');
    }
  };

  const handleToggle = async (itemId) => {
    try {
      await togglePackingItem(tripId, itemId);
      fetchData();
    } catch (err) {
      console.error('Failed to toggle item:', err);
    }
  };

  const handleDelete = async (itemId) => {
    try {
      await deletePackingItem(tripId, itemId);
      fetchData();
    } catch (err) {
      alert('준비물 삭제 실패');
    }
  };

  return (
    <div className="packing-tab-container">
      {/* 프로그레스 바 카드 */}
      <div className="packing-progress-card">
        <div className="progress-info-header">
          <div className="progress-title">
            <CheckCircle2 className="icon-progress" />
            <span>준비물 달성률</span>
          </div>
          <span className="progress-percentage">{progress.progressPercentage}%</span>
        </div>

        <div className="progress-bar-bg">
          <div
            className="progress-bar-fill"
            style={{ width: `${progress.progressPercentage}%` }}
          />
        </div>

        <div className="progress-stats">
          <span>{progress.checkedCount}개 완료 / 총 {progress.totalCount}개</span>
          {progress.totalCount === 0 && (
            <button className="btn-init-defaults" onClick={handleInitDefaults}>
              <RefreshCw size={14} /> 기본 템플릿 생성
            </button>
          )}
        </div>
      </div>

      {/* 새 준비물 추가 폼 */}
      <form className="packing-add-form" onSubmit={handleAddItem}>
        <select
          value={newItemCategory}
          onChange={(e) => setNewItemCategory(e.target.value)}
        >
          <option value="필수">🔴 필수</option>
          <option value="의류">👕 의류</option>
          <option value="세면">🧴 세면</option>
          <option value="전자기기">🔌 전자기기</option>
          <option value="기타">🛒 기타</option>
        </select>
        <input
          type="text"
          placeholder="추가할 준비물 입력 (예: 선글라스)"
          value={newItemName}
          onChange={(e) => setNewItemName(e.target.value)}
          required
        />
        <button type="submit" className="btn-add-item">
          <Plus size={16} /> 추가
        </button>
      </form>

      {/* 준비물 목록 */}
      <div className="packing-items-list">
        {loading ? (
          <p className="loading-text">불러오는 중...</p>
        ) : items.length === 0 ? (
          <p className="empty-text">준비물이 아직 없습니다. 기본 템플릿을 생성해 보세요!</p>
        ) : (
          items.map((item) => (
            <div
              key={item.id}
              className={`packing-item-row ${item.checked ? 'checked' : ''}`}
            >
              <div className="item-left" onClick={() => handleToggle(item.id)}>
                {item.checked ? (
                  <CheckSquare className="icon-checkbox checked" size={20} />
                ) : (
                  <Square className="icon-checkbox" size={20} />
                )}
                <span className="item-category-tag">{item.category}</span>
                <span className="item-name">{item.name}</span>
              </div>
              <button className="btn-delete-item" onClick={() => handleDelete(item.id)}>
                <Trash2 size={16} />
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
