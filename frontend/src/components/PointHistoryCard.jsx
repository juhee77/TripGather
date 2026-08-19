import React, { useState, useEffect } from 'react';
import { Coins, ArrowUpRight, ArrowDownLeft, Clock } from 'lucide-react';
import { getPointTransactions } from '../api/user';
import './PointHistoryCard.css';

export default function PointHistoryCard() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPointTransactions()
      .then((data) => {
        setTransactions(data || []);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Failed to fetch point transactions:', err);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div className="point-card-container">
        <div className="point-card-header">
          <h3><Coins size={20} className="icon-gold" /> 포인트 적립/사용 내역</h3>
        </div>
        <p className="point-loading">내역을 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="point-card-container">
      <div className="point-card-header">
        <h3><Coins size={20} className="icon-gold" /> 포인트 적립/사용 내역</h3>
        <span className="point-count-badge">최근 {transactions.length}건</span>
      </div>

      <div className="point-history-list">
        {transactions.length === 0 ? (
          <p className="empty-history">포인트 거래 내역이 없습니다.</p>
        ) : (
          transactions.map((tx) => {
            const isEarn = tx.amount >= 0;
            return (
              <div key={tx.id} className="point-history-item">
                <div className={`point-type-icon ${isEarn ? 'earn' : 'use'}`}>
                  {isEarn ? <ArrowUpRight size={18} /> : <ArrowDownLeft size={18} />}
                </div>

                <div className="point-info">
                  <span className="point-desc">{tx.description}</span>
                  {tx.createdAt && (
                    <span className="point-date">
                      <Clock size={11} /> {new Date(tx.createdAt).toLocaleDateString()}
                    </span>
                  )}
                </div>

                <div className={`point-amount ${isEarn ? 'earn' : 'use'}`}>
                  {isEarn ? `+${tx.amount} P` : `${tx.amount} P`}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
