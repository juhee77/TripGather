import React, { useState, useEffect } from 'react';
import { DollarSign, Plus, Trash2, Calculator, Users, Tag } from 'lucide-react';
import { getTripExpenses, addTripExpense, deleteTripExpense, getTripSettlement } from '../api/trips';
import './TripExpenseTab.css';

export default function TripExpenseTab({ tripId, memberCount: initialMemberCount = 1 }) {
  const [expenses, setExpenses] = useState([]);
  const [settlement, setSettlement] = useState(null);
  const [memberCount, setMemberCount] = useState(initialMemberCount);
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);

  const [formData, setFormData] = useState({
    title: '',
    amount: '',
    category: '식비',
    memo: ''
  });

  const fetchExpenseData = async () => {
    try {
      setLoading(true);
      const [expenseList, settlementData] = await Promise.all([
        getTripExpenses(tripId),
        getTripSettlement(tripId, memberCount)
      ]);
      setExpenses(expenseList);
      setSettlement(settlementData);
    } catch (err) {
      console.error('Failed to load expense data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tripId) {
      fetchExpenseData();
    }
  }, [tripId, memberCount]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.amount) return;

    try {
      await addTripExpense({
        tripId,
        title: formData.title,
        amount: parseFloat(formData.amount),
        category: formData.category,
        memo: formData.memo
      });
      setFormData({ title: '', amount: '', category: '식비', memo: '' });
      setShowAddForm(false);
      fetchExpenseData();
    } catch (err) {
      alert('지출 등록에 실패했습니다.');
    }
  };

  const handleDelete = async (expenseId) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      await deleteTripExpense(expenseId);
      fetchExpenseData();
    } catch (err) {
      alert('지출 삭제 실패: 본인이 등록한 지출만 삭제할 수 있습니다.');
    }
  };

  return (
    <div className="trip-expense-container">
      <div className="expense-header">
        <h2><DollarSign className="icon-main" /> 여행 지출 및 N빵 정산</h2>
        <button className="btn-add-expense" onClick={() => setShowAddForm(!showAddForm)}>
          <Plus size={16} /> {showAddForm ? '취소' : '지출 추가'}
        </button>
      </div>

      {showAddForm && (
        <form className="expense-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <input
              type="text"
              placeholder="지출 항목 (예: 흑돼지 저녁)"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
            <input
              type="number"
              placeholder="금액 (원)"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
            />
          </div>
          <div className="form-row">
            <select
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            >
              <option value="식비">🍽️ 식비</option>
              <option value="숙박">🏨 숙박</option>
              <option value="교통">🚗 교통</option>
              <option value="관광/액티비티">🎟️ 관광/액티비티</option>
              <option value="기타">🛒 기타</option>
            </select>
            <input
              type="text"
              placeholder="메모 (선택사항)"
              value={formData.memo}
              onChange={(e) => setFormData({ ...formData, memo: e.target.value })}
            />
          </div>
          <button type="submit" className="btn-submit">등록하기</button>
        </form>
      )}

      {/* 정산 요약 카드 */}
      {settlement && (
        <div className="settlement-card">
          <div className="settlement-header">
            <h3><Calculator className="icon-sub" /> N빵 정산 요약</h3>
            <div className="member-count-selector">
              <Users size={16} />
              <span>정산 인원: </span>
              <input
                type="number"
                min="1"
                value={memberCount}
                onChange={(e) => setMemberCount(Math.max(1, parseInt(e.target.value) || 1))}
              />
              <span>명</span>
            </div>
          </div>

          <div className="settlement-metrics">
            <div className="metric">
              <span className="metric-label">총 지출 금액</span>
              <span className="metric-value">{(settlement.totalAmount || 0).toLocaleString()}원</span>
            </div>
            <div className="metric highlight">
              <span className="metric-label">1인당 부담금</span>
              <span className="metric-value">{(settlement.perPersonAmount || 0).toLocaleString()}원</span>
            </div>
          </div>

          {settlement.payerSummaries && settlement.payerSummaries.length > 0 && (
            <div className="payer-summaries">
              <h4>참여자별 정산 금액</h4>
              <ul>
                {settlement.payerSummaries.map((payer) => (
                  <li key={payer.userId} className="payer-item">
                    <span className="payer-name">{payer.userName}</span>
                    <span className="payer-paid">결제: {payer.totalPaid.toLocaleString()}원</span>
                    <span className={`payer-balance ${payer.balance >= 0 ? 'positive' : 'negative'}`}>
                      {payer.balance >= 0
                        ? `+${payer.balance.toLocaleString()}원 (받기)`
                        : `${payer.balance.toLocaleString()}원 (보내기)`}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* 지출 내역 리스트 */}
      <div className="expense-list-section">
        <h3>지출 내역 ({expenses.length}건)</h3>
        {loading ? (
          <p className="loading-text">불러오는 중...</p>
        ) : expenses.length === 0 ? (
          <p className="empty-text">등록된 지출 내역이 없습니다.</p>
        ) : (
          <div className="expense-grid">
            {expenses.map((expense) => (
              <div key={expense.id} className="expense-item-card">
                <div className="expense-item-top">
                  <span className="category-badge"><Tag size={12} /> {expense.category}</span>
                  <button className="btn-delete" onClick={() => handleDelete(expense.id)}>
                    <Trash2 size={14} />
                  </button>
                </div>
                <h4 className="expense-title">{expense.title}</h4>
                <div className="expense-amount">{(expense.amount || 0).toLocaleString()}원</div>
                <div className="expense-payer">결제자: {expense.payerName}</div>
                {expense.memo && <div className="expense-memo">{expense.memo}</div>}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
