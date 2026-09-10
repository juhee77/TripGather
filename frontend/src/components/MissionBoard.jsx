import React, { useCallback, useEffect, useState } from 'react';
import {
  Award, Camera, Check, CheckCircle2, Clock, Plus, RotateCcw, Trash2, Users, X
} from 'lucide-react';
import { MissionCompletionStatus } from '../constants/enums';
import { authFetch } from '../api/client';
import {
  approveCompletion, createMission, deleteMission,
  getMissionProgress, getMissions, rejectCompletion, submitMission
} from '../api/missions';
import './MissionBoard.css';

const DEFAULT_REWARD = 50;

/**
 * 모임 미션 보드.
 *
 * 호스트에게는 출제와 심사 화면을, 크루에게는 도전과 인증 화면을 보여준다.
 * 같은 목록 API 하나로 두 역할을 모두 그린다.
 */
export default function MissionBoard({ gatheringId, isHost }) {
  const [missions, setMissions] = useState([]);
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', rewardPoints: DEFAULT_REWARD, requiresPhoto: false });
  const [saving, setSaving] = useState(false);

  const [openSubmitFor, setOpenSubmitFor] = useState(null);
  const [submitForm, setSubmitForm] = useState({ memo: '', photoUrl: null });
  const [uploading, setUploading] = useState(false);

  const load = useCallback(async () => {
    if (!gatheringId) return;
    try {
      setError(null);
      const [missionList, myProgress] = await Promise.all([
        getMissions(gatheringId),
        getMissionProgress(gatheringId),
      ]);
      setMissions(missionList || []);
      setProgress(myProgress || null);
    } catch (err) {
      console.error('[Mission] 목록을 불러오지 못했습니다', err);
      setError('미션을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [gatheringId]);

  useEffect(() => { load(); }, [load]);

  /* ---------------- 호스트: 출제와 심사 ---------------- */

  const handleCreate = async () => {
    if (!form.title.trim()) {
      alert('미션 제목을 입력해주세요.');
      return;
    }
    setSaving(true);
    try {
      await createMission(gatheringId, {
        title: form.title.trim(),
        description: form.description.trim() || null,
        rewardPoints: Number(form.rewardPoints) || DEFAULT_REWARD,
        requiresPhoto: form.requiresPhoto,
      });
      setForm({ title: '', description: '', rewardPoints: DEFAULT_REWARD, requiresPhoto: false });
      setShowForm(false);
      await load();
    } catch (err) {
      console.error('[Mission] 출제 실패', err);
      alert('미션을 등록하지 못했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (missionId) => {
    if (!window.confirm('이 미션을 삭제할까요?')) return;
    try {
      await deleteMission(gatheringId, missionId);
      await load();
    } catch (err) {
      console.error('[Mission] 삭제 실패', err);
      alert('이미 완료한 크루가 있는 미션은 삭제할 수 없습니다.');
    }
  };

  const handleReview = async (completionId, approved) => {
    try {
      if (approved) {
        await approveCompletion(gatheringId, completionId);
      } else {
        await rejectCompletion(gatheringId, completionId);
      }
      await load();
    } catch (err) {
      console.error('[Mission] 심사 실패', err);
      alert('처리하지 못했습니다.');
    }
  };

  /* ---------------- 크루: 인증 ---------------- */

  const openSubmit = (mission) => {
    setOpenSubmitFor(mission.id);
    setSubmitForm({ memo: mission.myMemo || '', photoUrl: mission.myPhotoUrl || null });
  };

  const handlePhotoPick = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await authFetch('/api/files/upload', { method: 'POST', body: formData });
      if (!res.ok) throw new Error('upload failed');
      const data = await res.json();
      setSubmitForm((prev) => ({ ...prev, photoUrl: data.url }));
    } catch (err) {
      console.error('[Mission] 사진 업로드 실패', err);
      alert('사진을 올리지 못했습니다.');
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (mission) => {
    if (mission.requiresPhoto && !submitForm.photoUrl) {
      alert('이 미션은 인증 사진이 필요합니다.');
      return;
    }
    setSaving(true);
    try {
      await submitMission(gatheringId, mission.id, submitForm);
      setOpenSubmitFor(null);
      setSubmitForm({ memo: '', photoUrl: null });
      await load();
    } catch (err) {
      console.error('[Mission] 인증 실패', err);
      alert('인증을 올리지 못했습니다.');
    } finally {
      setSaving(false);
    }
  };

  /* ---------------- 렌더 ---------------- */

  const pendingTotal = missions.reduce((sum, m) => sum + (m.pendingCount || 0), 0);
  const clearedTotal = missions.reduce((sum, m) => sum + (m.approvedCount || 0), 0);

  if (loading) {
    return <div className="mission-empty">미션을 불러오는 중…</div>;
  }
  if (error) {
    return <div className="mission-empty">{error}</div>;
  }

  return (
    <div className="mission-board animate-fade">
      {/*
        호스트는 자기 미션을 깰 수 없으므로 개인 진행도가 영원히 0%다.
        그 막대를 보여주면 기능이 고장난 것처럼 보이니, 호스트에게는 크루 현황을 대신 보여준다.
      */}
      {isHost ? (
        missions.length > 0 && (
          <div className="mission-progress-card">
            <div className="mission-progress-head">
              <span className="mission-progress-title"><Users size={18} /> 크루 미션 현황</span>
              {pendingTotal > 0 && <span className="mission-progress-percent">심사 {pendingTotal}</span>}
            </div>
            <div className="mission-progress-foot">
              <span>미션 {missions.length}개</span>
              <span>크루 완료 {clearedTotal}건</span>
            </div>
          </div>
        )
      ) : (
        progress && progress.totalCount > 0 && (
          <div className="mission-progress-card">
            <div className="mission-progress-head">
              <span className="mission-progress-title"><Award size={18} /> 내 미션 진행도</span>
              <span className="mission-progress-percent">{progress.progressPercentage}%</span>
            </div>
            <div className="mission-progress-bar">
              <div className="mission-progress-fill" style={{ width: `${progress.progressPercentage}%` }} />
            </div>
            <div className="mission-progress-foot">
              <span>{progress.clearedCount} / {progress.totalCount} 미션 완료</span>
              <span>{progress.earnedPoints} PTS 획득</span>
            </div>
          </div>
        )
      )}

      {isHost && (
        showForm ? (
          <div className="mission-form">
            <input
              className="mission-input"
              placeholder="미션 제목 (예: 흑돼지 먹고 인증하기)"
              value={form.title}
              maxLength={100}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
            <textarea
              className="mission-input mission-textarea"
              placeholder="어떻게 해야 완료인지 알려주세요 (선택)"
              value={form.description}
              maxLength={500}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
            <div className="mission-form-row">
              <label className="mission-field">
                보상 포인트
                <input
                  className="mission-input mission-input-sm"
                  type="number" min={0} max={1000}
                  value={form.rewardPoints}
                  onChange={(e) => setForm({ ...form, rewardPoints: e.target.value })}
                />
              </label>
              <label className="mission-checkbox">
                <input
                  type="checkbox"
                  checked={form.requiresPhoto}
                  onChange={(e) => setForm({ ...form, requiresPhoto: e.target.checked })}
                />
                <Camera size={15} /> 사진 인증 필수
              </label>
            </div>
            <div className="mission-form-actions">
              <button className="mission-btn ghost" onClick={() => setShowForm(false)}>취소</button>
              <button className="mission-btn primary" onClick={handleCreate} disabled={saving}>
                {saving ? '등록 중…' : '미션 출제'}
              </button>
            </div>
          </div>
        ) : (
          <button className="mission-add-btn" onClick={() => setShowForm(true)}>
            <Plus size={18} /> 크루에게 미션 내기
          </button>
        )
      )}

      {missions.length === 0 ? (
        <div className="mission-empty">
          {isHost
            ? '아직 낸 미션이 없습니다. 크루가 여행에서 할 일을 하나 걸어보세요.'
            : '호스트가 아직 미션을 내지 않았습니다.'}
        </div>
      ) : (
        <div className="mission-list">
          {missions.map((mission) => (
            <MissionCard
              key={mission.id}
              mission={mission}
              isHost={isHost}
              isSubmitting={openSubmitFor === mission.id}
              submitForm={submitForm}
              setSubmitForm={setSubmitForm}
              uploading={uploading}
              saving={saving}
              onOpenSubmit={() => openSubmit(mission)}
              onCancelSubmit={() => setOpenSubmitFor(null)}
              onPhotoPick={handlePhotoPick}
              onSubmit={() => handleSubmit(mission)}
              onDelete={() => handleDelete(mission.id)}
              onReview={handleReview}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function MissionCard({
  mission, isHost, isSubmitting, submitForm, setSubmitForm, uploading, saving,
  onOpenSubmit, onCancelSubmit, onPhotoPick, onSubmit, onDelete, onReview,
}) {
  const cleared = mission.myStatus === MissionCompletionStatus.APPROVED;
  const waiting = mission.myStatus === MissionCompletionStatus.SUBMITTED;
  const rejected = mission.myStatus === MissionCompletionStatus.REJECTED;

  return (
    <div className={`mission-card ${cleared ? 'cleared' : ''}`}>
      <div className="mission-card-head">
        <div className="mission-card-title">
          {cleared ? <CheckCircle2 size={18} className="icon-cleared" /> : <Award size={18} />}
          <span>{mission.title}</span>
        </div>
        <div className="mission-card-meta">
          <span className="mission-reward">+{mission.rewardPoints} PTS</span>
          {mission.requiresPhoto && <span className="mission-tag"><Camera size={12} /> 사진</span>}
        </div>
      </div>

      {mission.description && <p className="mission-desc">{mission.description}</p>}

      <div className="mission-card-foot">
        <span className="mission-crew-count">
          <Users size={13} /> {mission.approvedCount}명 완료
          {isHost && mission.pendingCount > 0 && (
            <span className="mission-pending-badge">심사 {mission.pendingCount}</span>
          )}
        </span>

        {isHost ? (
          <button className="mission-btn ghost sm" onClick={onDelete}>
            <Trash2 size={14} /> 삭제
          </button>
        ) : cleared ? (
          <span className="mission-status cleared">MISSION CLEAR</span>
        ) : waiting ? (
          <span className="mission-status waiting"><Clock size={13} /> 호스트 확인 중</span>
        ) : (
          <button className="mission-btn primary sm" onClick={onOpenSubmit}>
            {rejected ? <><RotateCcw size={14} /> 다시 인증</> : '인증하기'}
          </button>
        )}
      </div>

      {rejected && !isSubmitting && (
        <p className="mission-rejected-note">호스트가 인증을 반려했습니다. 다시 올릴 수 있습니다.</p>
      )}

      {isSubmitting && (
        <div className="mission-submit-form">
          <textarea
            className="mission-input mission-textarea"
            placeholder="한 줄 남기기 (선택)"
            value={submitForm.memo}
            onChange={(e) => setSubmitForm({ ...submitForm, memo: e.target.value })}
          />
          <label className="mission-photo-pick">
            <input type="file" accept="image/*" hidden onChange={onPhotoPick} />
            <Camera size={15} />
            {uploading ? '올리는 중…' : submitForm.photoUrl ? '사진 다시 고르기' : '인증 사진 첨부'}
            {mission.requiresPhoto && <em>필수</em>}
          </label>
          {submitForm.photoUrl && (
            <img
              className="mission-photo-preview"
              src={submitForm.photoUrl}
              alt="인증 사진 미리보기"
              onError={(e) => { e.currentTarget.style.display = 'none'; }}
            />
          )}
          <div className="mission-form-actions">
            <button className="mission-btn ghost" onClick={onCancelSubmit}>취소</button>
            <button className="mission-btn primary" onClick={onSubmit} disabled={saving || uploading}>
              {saving ? '올리는 중…' : '인증 제출'}
            </button>
          </div>
        </div>
      )}

      {isHost && mission.pendingCompletions?.length > 0 && (
        <div className="mission-review-list">
          {mission.pendingCompletions.map((completion) => (
            <div className="mission-review-item" key={completion.id}>
              {completion.photoUrl && (
                <img
                  className="mission-review-thumb"
                  src={completion.photoUrl}
                  alt=""
                  onError={(e) => { e.currentTarget.style.display = 'none'; }}
                />
              )}
              <div className="mission-review-body">
                <strong>{completion.userName || completion.userEmail}</strong>
                {completion.memo && <p>{completion.memo}</p>}
              </div>
              <div className="mission-review-actions">
                <button className="mission-icon-btn approve" title="승인"
                        onClick={() => onReview(completion.id, true)}>
                  <Check size={16} />
                </button>
                <button className="mission-icon-btn reject" title="반려"
                        onClick={() => onReview(completion.id, false)}>
                  <X size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
