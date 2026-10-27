import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, List, Map, Globe, CheckSquare, Star, Plus, Film } from 'lucide-react';
import { authFetch } from '../api/client';
import PackingList from '../components/PackingList';
import TranslatorWidget from '../components/TranslatorWidget';
import ReviewSection from '../components/ReviewSection';
import TravelTimelapseStudio from '../components/TravelTimelapseStudio';
import OpenRouteMap from '../components/OpenRouteMap';
import { getRecommendedSpotsForRegion } from '../utils/openMapService';

const TripHubPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [itinerary, setItinerary] = useState(null);
  const [activeTab, setActiveTab] = useState('일정');
  const [loading, setLoading] = useState(true);
  const [errorStatus, setErrorStatus] = useState(null);

  useEffect(() => {
    fetchTripAndItinerary();
  }, [id]);

  const fetchTripAndItinerary = async () => {
    try {
      setErrorStatus(null);
      const tripRes = await authFetch(`/api/trips/${id}`);
      if (tripRes.ok) {
        const tripData = await tripRes.json();
        setTrip(tripData);
        if (tripData.itineraryId) {
          const itineraryRes = await authFetch(`/api/itineraries/${tripData.itineraryId}`);
          if (itineraryRes.ok) {
            setItinerary(await itineraryRes.json());
          }
        }
      } else {
        setErrorStatus(tripRes.status);
      }
    } catch (err) {
      console.error(err);
      setErrorStatus(500);
    } finally {
      setLoading(false);
    }
  };

  const handleTogglePoint = async (pointIndex) => {
    if (!itinerary) return;
    const sourcePoints = itinerary.routePoints || [];
    const targetPoint = sourcePoints[pointIndex];
    if (!targetPoint) return;

    try {
      if (targetPoint.id != null) {
        const res = await authFetch(`/api/itineraries/${itinerary.id}/points/${targetPoint.id}/toggle`, {
          method: 'PATCH',
        });
        if (res.ok) {
          const updatedPoint = await res.json();
          const nextCompleted = updatedPoint.isCompleted ?? updatedPoint.completed ?? !targetPoint.isCompleted;
          setItinerary((prev) => ({
            ...prev,
            routePoints: (prev?.routePoints || []).map((pt, idx) =>
              idx === pointIndex ? { ...pt, isCompleted: nextCompleted } : pt
            ),
          }));
        }
        return;
      }

      const newPoints = sourcePoints.map((pt, idx) =>
        idx === pointIndex ? { ...pt, isCompleted: !pt.isCompleted } : pt
      );
      const updatedItinerary = { ...itinerary, routePoints: newPoints };
      const res = await authFetch(`/api/itineraries/${itinerary.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedItinerary)
      });
      if (res.ok) {
        setItinerary(await res.json());
      }
    } catch (err) {
      console.error('Failed to toggle completion status:', err);
    }
  };

  if (loading) return <div style={{ padding: '40px 20px', textAlign: 'center', fontWeight: 700, color: 'var(--text-secondary)' }}>여행 정보를 불러오는 중...</div>;
  if (!trip) {
    return (
      <div style={{ background: 'var(--bg-lite)', minHeight: '100vh', paddingBottom: '100px' }}>
        <header className="glass page-header" style={{
          position: 'sticky', top: 0, zIndex: 100, padding: '20px',
          display: 'flex', alignItems: 'center', gap: '12px',
          borderBottom: '1px solid var(--border-color)',
          background: 'rgba(255,255,255,0.9)'
        }}>
          <button
            onClick={() => navigate(-1)}
            aria-label="뒤로 가기"
            style={{ background: 'none', border: 'none', cursor: 'pointer' }}
          >
            <ArrowLeft size={24} color="var(--text-primary)" />
          </button>
          <div>
            <span className="label-orange">TRIP HUB</span>
            <h1 className="heading-l" style={{ marginTop: '2px' }}>여행 허브 안내</h1>
          </div>
        </header>
        <div style={{ padding: '48px 20px', textAlign: 'center' }}>
          <div style={{ background: 'white', borderRadius: '24px', padding: '32px 24px', border: '1px solid var(--border-color)', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
            <div style={{ fontSize: '1.05rem', fontWeight: 900, color: 'var(--text-primary)', marginBottom: '8px' }}>
              {errorStatus === 403 ? '참여 중인 여행만 열람할 수 있습니다.' : '여행 정보를 찾을 수 없습니다.'}
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '20px', lineHeight: 1.5 }}>
              {errorStatus === 403
                ? '이 여행 허브는 승인된 동행 멤버에게만 공개되어 있습니다.'
                : '삭제되었거나 존재하지 않는 여행 경로입니다.'}
            </p>
            <button
              onClick={() => navigate('/gather')}
              style={{
                padding: '12px 24px', borderRadius: '14px', border: 'none',
                background: 'var(--text-primary)', color: 'white', fontWeight: 800, cursor: 'pointer'
              }}
            >
              홈으로 이동
            </button>
          </div>
        </div>
      </div>
    );
  }

  const TABS = [
    { id: '일정', icon: <List size={18} /> },
    { id: '타임랩스', icon: <Film size={18} /> },
    { id: '추천', icon: <Map size={18} /> },
    { id: '번역', icon: <Globe size={18} /> },
    { id: '준비물', icon: <CheckSquare size={18} /> },
    { id: '리뷰', icon: <Star size={18} /> }
  ];

  // 일자별 그룹화
  const groupedByDay = (() => {
    const sourcePoints = itinerary?.routePoints || [];
    const pointsWithIdx = sourcePoints.map((p, index) => ({ ...p, originalIndex: index }));
    const map = {};
    pointsWithIdx.forEach(p => {
      const key = p.dayNumber ?? 1;
      if (!map[key]) map[key] = { dayLabel: p.dayLabel || `Day ${key}`, points: [] };
      map[key].points.push(p);
    });
    return Object.entries(map).sort((a, b) => Number(a[0]) - Number(b[0])).map(([dayNum, data]) => ({
      dayNumber: Number(dayNum),
      ...data
    }));
  })();

  return (
    <div style={{ background: 'var(--bg-lite)', minHeight: '100vh', paddingBottom: '100px' }}>
      {/* Header */}
      <header className="glass page-header" style={{
        position: 'sticky', top: 0, zIndex: 100, padding: '20px',
        display: 'flex', alignItems: 'center', gap: '12px',
        borderBottom: '1px solid var(--border-color)',
        background: 'rgba(255,255,255,0.9)'
      }}>
        <button onClick={() => navigate(-1)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
          <ArrowLeft size={24} color="var(--text-primary)" />
        </button>
        <div>
          <span className="label-orange">TRIP HUB</span>
          <h1 className="heading-l" style={{ marginTop: '2px' }}>{trip.title}</h1>
        </div>
      </header>

      {/* Info Card */}
      <div style={{ padding: '20px' }}>
        <div style={{
          background: 'white', borderRadius: '16px', padding: '16px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.05)', border: '1px solid var(--border-color)'
        }}>
          <div style={{ display: 'flex', gap: '12px', marginBottom: '8px' }}>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-secondary)' }}>
              <MapPin size={16} style={{ display: 'inline', marginRight: '4px' }}/>
              {trip.destination || '미정'}
            </span>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-secondary)' }}>
              <Calendar size={16} style={{ display: 'inline', marginRight: '4px' }}/>
              {trip.startDate} ~ {trip.endDate}
            </span>
          </div>
          <span style={{
            background: 'var(--primary-orange)', color: 'white',
            padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 800
          }}>
            {trip.status}
          </span>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ padding: '0 20px', overflowX: 'auto' }} className="hide-scrollbar">
        <div style={{ display: 'flex', gap: '8px' }}>
          {TABS.map(tab => (
            <button key={tab.id} onClick={() => setActiveTab(tab.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: '6px',
                padding: '10px 16px', borderRadius: '12px',
                background: activeTab === tab.id ? 'var(--text-primary)' : 'white',
                color: activeTab === tab.id ? 'white' : 'var(--text-secondary)',
                border: '1px solid var(--border-color)', fontWeight: 800, whiteSpace: 'nowrap'
              }}>
              {tab.icon} {tab.id}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '20px' }}>
        {activeTab === '일정' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: 800 }}>내 여행 코스</h3>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={() => setActiveTab('타임랩스')}
                  style={{
                    background: 'var(--primary-orange)', color: 'white', border: 'none',
                    padding: '8px 14px', borderRadius: '10px', fontWeight: 800, fontSize: '13px',
                    display: 'flex', alignItems: 'center', gap: '5px', cursor: 'pointer',
                    boxShadow: 'var(--shadow-sm)'
                  }}
                >
                  <Film size={14} /> 보딩패스 GIF 만들기
                </button>
                {trip.itineraryId && (
                  <button onClick={() => navigate(`/itinerary/edit/${trip.itineraryId}`)} style={{
                    background: 'var(--bg-lite)', color: 'var(--text-primary)', border: '1px solid var(--border-color)',
                    padding: '8px 14px', borderRadius: '10px', fontWeight: 800, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer',
                    boxShadow: 'var(--shadow-sm)'
                  }}>
                    <Plus size={14} /> 일정 편집
                  </button>
                )}
              </div>
            </div>

            <OpenRouteMap
              title="내 여행 동선 지도 🗺️"
              routePoints={itinerary?.routePoints || []}
              fallbackLocationText={trip.destination || trip.title}
              height={280}
              showRouteSummaryStrip={true}
            />

            {!itinerary || !itinerary.routePoints || itinerary.routePoints.length === 0 ? (
              <div style={{
                textAlign: 'center', padding: '50px 20px', color: 'var(--text-secondary)',
                background: 'white', borderRadius: '16px', border: '1px dashed var(--border-color)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px'
              }}>
                <span style={{ fontSize: '32px' }}>✈️</span>
                <p style={{ fontWeight: 700, fontSize: '14px', margin: 0 }}>아직 추가된 세부 일정이 없습니다.</p>
                <p style={{ fontSize: '12px', color: 'var(--text-muted)', margin: 0 }}>일정 편집 또는 추천 탭에서 첫 번째 방문지를 등록해 보세요!</p>
                {trip.itineraryId && (
                  <button onClick={() => navigate(`/itinerary/edit/${trip.itineraryId}`)} className="primary-btn" style={{ padding: '8px 16px', fontSize: '13px', borderRadius: '10px', marginTop: '8px' }}>
                    일정 편집하기
                  </button>
                )}
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', background: 'white', padding: '20px', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
                {groupedByDay.map((day, dIdx) => (
                  <div key={day.dayNumber} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {/* Day Title */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{
                        background: 'var(--text-primary)', color: 'white',
                        padding: '4px 10px', borderRadius: '8px', fontSize: '11px', fontWeight: 900
                      }}>
                        DAY {day.dayNumber}
                      </span>
                      <span style={{ fontWeight: 800, color: 'var(--text-primary)', fontSize: '14px' }}>
                        {day.dayLabel}
                      </span>
                    </div>

                    {/* Day Points Timeline */}
                    <div style={{ display: 'flex', flexDirection: 'column', paddingLeft: '10px' }}>
                      {day.points.map((point, pIdx) => {
                        const isLastPoint = dIdx === groupedByDay.length - 1 && pIdx === day.points.length - 1;
                        return (
                          <div key={point.id || pIdx} style={{ display: 'flex', gap: '16px', position: 'relative', paddingBottom: isLastPoint ? '0px' : '20px' }}>
                            {/* Vertical Line */}
                            {!isLastPoint && (
                              <div style={{
                                position: 'absolute', left: '11px', top: '24px', bottom: 0,
                                width: '2px', background: 'var(--border-color)'
                              }} />
                            )}

                            {/* Node Checkbox */}
                            <div
                              onClick={() => handleTogglePoint(point.originalIndex)}
                              style={{
                                width: '24px', height: '24px', borderRadius: '50%',
                                background: point.isCompleted ? 'var(--primary-orange)' : 'var(--bg-lite)',
                                border: `2px solid ${point.isCompleted ? 'var(--primary-orange)' : 'var(--border-color)'}`,
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                cursor: 'pointer', zIndex: 1, transition: 'all 0.2s',
                                color: 'white'
                              }}
                            >
                              {point.isCompleted ? (
                                <span style={{ fontSize: '10px', fontWeight: 900 }}>✓</span>
                              ) : (
                                <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 800 }}>{pIdx + 1}</span>
                              )}
                            </div>

                            {/* Info */}
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1, marginTop: '2px' }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <span style={{
                                  fontWeight: 800, fontSize: '14px',
                                  color: point.isCompleted ? 'var(--text-secondary)' : 'var(--text-primary)',
                                  textDecoration: point.isCompleted ? 'line-through' : 'none'
                                }}>
                                  {point.label}
                                </span>
                              </div>
                              {point.memo && (
                                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                                  {point.memo}
                                </span>
                              )}
                              {point.photoUrl && (
                                <img
                                  src={point.photoUrl}
                                  alt={point.label}
                                  style={{
                                    width: '72px',
                                    height: '72px',
                                    objectFit: 'cover',
                                    borderRadius: '10px',
                                    border: '1.5px solid var(--primary-orange)'
                                  }}
                                />
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
        {activeTab === '타임랩스' && (
          <TravelTimelapseStudio
            itinerary={itinerary}
            tripTitle={trip.title}
            canEdit={true}
            onItineraryUpdated={(updated) => setItinerary(updated)}
          />
        )}
        {activeTab === '추천' && (() => {
          const recommendedSpots = getRecommendedSpotsForRegion(trip.destination || trip.title);
          const handleAddSpotToItinerary = async (spot) => {
            if (!itinerary?.id) {
              alert('연결된 여행 일정이 없습니다.');
              return;
            }
            const existing = itinerary.routePoints || [];
            const maxDay = existing.reduce((m, p) => Math.max(m, p.dayNumber || 1), 1);
            const inLastDay = existing.filter((p) => (p.dayNumber || 1) === maxDay);
            const nextPoint = {
              dayNumber: maxDay,
              dayLabel: `Day ${maxDay}`,
              sequenceOrder: inLastDay.length + 1,
              label: spot.name,
              lat: spot.lat,
              lng: spot.lng,
              memo: spot.desc || spot.address || '',
              isCompleted: false
            };
            const updatedPayload = {
              ...itinerary,
              routePoints: [...existing, nextPoint]
            };
            try {
              const res = await authFetch(`/api/itineraries/${itinerary.id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedPayload)
              });
              if (res.ok) {
                setItinerary(await res.json());
                alert(`✈️ '${spot.name}' 경유지가 Day ${maxDay} 일정에 추가되었습니다!`);
              }
            } catch (err) {
              console.error(err);
            }
          };

          return (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <OpenRouteMap
                title={`${trip.destination || '여행지'} 주변 추천 명소 & 오픈소스 장소 검색 🗺️`}
                routePoints={recommendedSpots.map((s, idx) => ({
                  id: `rec-${idx}`,
                  dayNumber: 1,
                  sequenceOrder: idx + 1,
                  label: s.name,
                  lat: s.lat,
                  lng: s.lng,
                  memo: s.desc
                }))}
                fallbackLocationText={trip.destination || trip.title}
                height={320}
                enablePlaceSearch={true}
                searchPlaceholder="오픈스트리트맵 장소 검색 후 바로 내 일정에 추가..."
                onSelectPlace={(place) => handleAddSpotToItinerary(place)}
              />

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '12px' }}>
                {recommendedSpots.map((spot, idx) => (
                  <div
                    key={idx}
                    style={{
                      background: 'white',
                      border: '1px solid var(--border-color)',
                      borderRadius: '16px',
                      padding: '16px',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-between',
                      gap: '10px',
                      boxShadow: 'var(--shadow-sm)'
                    }}
                  >
                    <div>
                      <span className="osm-route-map-badge">{spot.category} · {spot.region}</span>
                      <h4 style={{ margin: '8px 0 4px', fontSize: '15px', fontWeight: 900, color: 'var(--text-primary)' }}>
                        📍 {spot.name}
                      </h4>
                      <p style={{ margin: 0, fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                        {spot.desc}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleAddSpotToItinerary(spot)}
                      className="osm-route-map-search-btn"
                      style={{ width: '100%' }}
                    >
                      + 내 여행 코스에 추가
                    </button>
                  </div>
                ))}
              </div>
            </div>
          );
        })()}
        {activeTab === '번역' && <TranslatorWidget country={trip.country} />}
        {activeTab === '준비물' && <PackingList tripId={trip.id} />}
        {activeTab === '리뷰' && <ReviewSection tripId={trip.id} />}
      </div>
    </div>
  );
};

export default TripHubPage;
