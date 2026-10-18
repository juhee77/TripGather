import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, MapPin, Plane, Search, Navigation } from 'lucide-react';
import { authFetch } from '../api/client';
import Card from '../components/UI/Card';
import PrimaryButton from '../components/UI/PrimaryButton';
import {
  OPEN_MAP_TILES,
  ensureLeafletLoaded,
  resolveFallbackCoordinates,
  normalizeRoutePointsForMap,
  calculateTotalRouteKm,
  searchOpenStreetMapPlaces
} from '../utils/openMapService';

const MapPage = () => {
  const navigate = useNavigate();
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const tileLayerRef = useRef(null);
  const markersLayerRef = useRef(null);

  const [mapReady, setMapReady] = useState(false);
  const [gatherings, setGatherings] = useState([]);
  const [itineraries, setItineraries] = useState([]);
  const [filterMode, setFilterMode] = useState('ALL'); // ALL | GATHERING | ITINERARY
  const [tileKey, setTileKey] = useState('voyager');
  const [selectedItem, setSelectedItem] = useState(null); // { type: 'gathering' | 'itinerary', data }
  const [joining, setJoining] = useState(false);

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  useEffect(() => {
    authFetch('/api/gatherings')
      .then((res) => (res.ok ? res.json() : []))
      .then((list) => {
        const enriched = (Array.isArray(list) ? list : []).map((g, idx) => {
          if (g.lat && g.lng) return g;
          const fb = resolveFallbackCoordinates(g.location || g.title || '');
          // 겹침 방지를 위한 미세 오프셋
          const jitter = (idx % 5) * 0.0025;
          return { ...g, lat: fb.lat + jitter, lng: fb.lng + jitter };
        });
        setGatherings(enriched);
      })
      .catch((err) => console.error('Error fetching gatherings:', err));

    authFetch('/api/itineraries')
      .then((res) => (res.ok ? res.json() : []))
      .then((list) => {
        const enriched = (Array.isArray(list) ? list : []).map((it) => {
          const pts = normalizeRoutePointsForMap(it.routePoints || []);
          const fb = resolveFallbackCoordinates(it.location || it.title || '');
          return {
            ...it,
            normalizedPoints: pts,
            lat: pts[0]?.lat ?? fb.lat,
            lng: pts[0]?.lng ?? fb.lng,
            totalKm: calculateTotalRouteKm(pts)
          };
        });
        setItineraries(enriched);
      })
      .catch((err) => console.error('Error fetching itineraries:', err));
  }, []);

  // 지도 초기화
  useEffect(() => {
    let cancelled = false;
    ensureLeafletLoaded()
      .then((L) => {
        if (cancelled || !L || !mapContainerRef.current) return;
        if (!mapRef.current) {
          const map = L.map(mapContainerRef.current, {
            zoomControl: false,
            attributionControl: true
          }).setView([36.35, 127.85], 7);

          L.control.zoom({ position: 'bottomright' }).addTo(map);

          const tileCfg = OPEN_MAP_TILES[tileKey] || OPEN_MAP_TILES.voyager;
          tileLayerRef.current = L.tileLayer(tileCfg.url, {
            attribution: tileCfg.attribution,
            maxZoom: 19
          }).addTo(map);

          markersLayerRef.current = L.layerGroup().addTo(map);
          mapRef.current = map;
          setMapReady(true);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
      }
      setMapReady(false);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 타일 변경
  useEffect(() => {
    const L = window.L;
    const map = mapRef.current;
    if (!mapReady || !L || !map) return;
    if (tileLayerRef.current) {
      map.removeLayer(tileLayerRef.current);
    }
    const tileCfg = OPEN_MAP_TILES[tileKey] || OPEN_MAP_TILES.voyager;
    tileLayerRef.current = L.tileLayer(tileCfg.url, {
      attribution: tileCfg.attribution,
      maxZoom: 19
    }).addTo(map);
  }, [mapReady, tileKey]);

  const visibleGatherings = useMemo(
    () => (filterMode === 'ITINERARY' ? [] : gatherings),
    [gatherings, filterMode]
  );

  const visibleItineraries = useMemo(
    () => (filterMode === 'GATHERING' ? [] : itineraries),
    [itineraries, filterMode]
  );

  // 마커 및 선택된 여행 동선 폴리라인 렌더링
  useEffect(() => {
    const L = window.L;
    const map = mapRef.current;
    const layer = markersLayerRef.current;
    if (!mapReady || !L || !map || !layer) return;

    layer.clearLayers();
    const allCoords = [];

    // 1) 모임 마커
    visibleGatherings.forEach((g) => {
      if (!Number.isFinite(Number(g.lat)) || !Number.isFinite(Number(g.lng))) return;
      const lat = Number(g.lat);
      const lng = Number(g.lng);
      allCoords.push([lat, lng]);

      const isSelected = selectedItem?.type === 'gathering' && selectedItem?.data?.id === g.id;
      const safeTitle = String(g.title || '모임').slice(0, 14);
      const icon = L.divIcon({
        className: 'osm-route-map-div-icon',
        html: `<div class="osm-route-map-marker">
          <div class="osm-route-map-marker-bubble" style="background:${isSelected ? '#FF5C00' : '#0F172A'};">
            <span class="osm-route-map-marker-num" style="background:#FF5C00;">👥</span>
            <span>${safeTitle}</span>
          </div>
        </div>`,
        iconSize: [150, 34],
        iconAnchor: [75, 28]
      });

      const marker = L.marker([lat, lng], { icon }).addTo(layer);
      marker.on('click', () => {
        setSelectedItem({ type: 'gathering', data: g });
        map.flyTo([lat, lng], Math.max(map.getZoom(), 13), { duration: 0.6 });
      });
    });

    // 2) 여행 동선(Itinerary) 마커 및 폴리라인
    visibleItineraries.forEach((it) => {
      const pts = it.normalizedPoints || [];
      if (pts.length >= 2) {
        const latlngs = pts.map((p) => [p.lat, p.lng]);
        const isSelected = selectedItem?.type === 'itinerary' && selectedItem?.data?.id === it.id;
        L.polyline(latlngs, {
          color: isSelected ? '#FF5C00' : '#0EA5E9',
          weight: isSelected ? 5 : 3,
          opacity: isSelected ? 0.95 : 0.7,
          dashArray: '6, 6'
        }).addTo(layer);
      }

      if (!Number.isFinite(Number(it.lat)) || !Number.isFinite(Number(it.lng))) return;
      const lat = Number(it.lat);
      const lng = Number(it.lng);
      allCoords.push([lat, lng]);

      const isSelected = selectedItem?.type === 'itinerary' && selectedItem?.data?.id === it.id;
      const safeTitle = String(it.title || '여행 코스').slice(0, 14);
      const icon = L.divIcon({
        className: 'osm-route-map-div-icon',
        html: `<div class="osm-route-map-marker">
          <div class="osm-route-map-marker-bubble" style="background:${isSelected ? '#0EA5E9' : '#1E293B'};">
            <span class="osm-route-map-marker-num" style="background:#0EA5E9;">✈️</span>
            <span>${safeTitle}</span>
          </div>
        </div>`,
        iconSize: [160, 34],
        iconAnchor: [80, 28]
      });

      const marker = L.marker([lat, lng], { icon }).addTo(layer);
      marker.on('click', () => {
        setSelectedItem({ type: 'itinerary', data: it });
        if (pts.length >= 2) {
          map.fitBounds(
            L.latLngBounds(pts.map((p) => [p.lat, p.lng])),
            { padding: [60, 60], maxZoom: 14 }
          );
        } else {
          map.flyTo([lat, lng], 13, { duration: 0.6 });
        }
      });

      // 선택된 여정이면 각 경유지 번호 핀도 함께 표시
      if (isSelected && pts.length > 0) {
        pts.forEach((pt, idx) => {
          const stopIcon = L.divIcon({
            className: 'osm-route-map-div-icon',
            html: `<div class="osm-route-map-marker">
              <div class="osm-route-map-marker-bubble" style="background:#FF5C00;padding:2px 7px;font-size:10px;">
                #${idx + 1} ${String(pt.label || '').slice(0, 10)}
              </div>
            </div>`,
            iconSize: [110, 26],
            iconAnchor: [55, 22]
          });
          L.marker([pt.lat, pt.lng], { icon: stopIcon }).addTo(layer);
        });
      }
    });

    if (!selectedItem && allCoords.length > 0) {
      map.fitBounds(L.latLngBounds(allCoords), { padding: [70, 70], maxZoom: 12 });
    }
  }, [mapReady, visibleGatherings, visibleItineraries, selectedItem]);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    if (!searchQuery.trim()) return;
    const res = await searchOpenStreetMapPlaces(searchQuery);
    setSearchResults(res);
  };

  const handleMoveToPlace = (place) => {
    setSearchResults([]);
    setSearchQuery(place.name);
    if (mapRef.current) {
      mapRef.current.flyTo([place.lat, place.lng], 14, { duration: 0.7 });
    }
  };

  const handleMoveToMyLocation = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        if (mapRef.current) {
          mapRef.current.flyTo([pos.coords.latitude, pos.coords.longitude], 14, { duration: 0.7 });
        }
      },
      () => {
        if (mapRef.current) {
          mapRef.current.flyTo([37.5665, 126.978], 12, { duration: 0.7 });
        }
      }
    );
  };

  const handleJoinGathering = async (gatheringItem) => {
    if (!gatheringItem) return;
    setJoining(true);
    try {
      const res = await authFetch(`/api/gatherings/${gatheringItem.id}/join`, {
        method: 'POST'
      });
      if (res.ok) {
        const updated = await res.json();
        setGatherings((prev) => prev.map((g) => (g.id === updated.id ? { ...updated, lat: g.lat, lng: g.lng } : g)));
        setSelectedItem({ type: 'gathering', data: { ...updated, lat: gatheringItem.lat, lng: gatheringItem.lng } });
        alert('참여 신청이 완료되었습니다!');
      } else {
        alert('참여에 실패했습니다.');
      }
    } catch (err) {
      alert(`오류: ${err.message}`);
    } finally {
      setJoining(false);
    }
  };

  return (
    <div style={{ width: '100vw', height: '100vh', position: 'relative', background: 'var(--bg-color)', overflow: 'hidden' }}>
      {/* 상단 플로팅 헤더 & 검색·필터 바 */}
      <div
        className="glass"
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          zIndex: 500,
          padding: '18px 20px 14px',
          borderBottom: '1px solid var(--border-color)',
          backdropFilter: 'blur(20px)',
          WebkitBackdropFilter: 'blur(20px)',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px'
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
          <div>
            <h1 style={{ fontSize: '21px', fontWeight: 900, margin: 0, color: 'var(--text-primary)' }}>
              오픈소스 여행·모임 지도 🗺️
            </h1>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 700, margin: '2px 0 0' }}>
              모임 <span style={{ color: 'var(--primary-orange)' }}>{gatherings.length}개</span> · 여행 동선 코스{' '}
              <span style={{ color: 'var(--secondary-blue)' }}>{itineraries.length}개</span> 실시간 표시 중
            </p>
          </div>

          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
            {Object.values(OPEN_MAP_TILES).map((t) => (
              <button
                key={t.key}
                type="button"
                onClick={() => setTileKey(t.key)}
                className={`osm-route-map-pill ${tileKey === t.key ? 'active' : ''}`}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>

        {/* 검색창 + 카테고리 필터 */}
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: '6px', flex: 1, minWidth: '220px' }}>
            <div style={{ position: 'relative', flex: 1 }}>
              <Search size={15} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '10px' }} />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="지역·명소 이동 검색 (예: 제주도, 해운대, 성수동)"
                style={{
                  width: '100%',
                  padding: '8px 12px 8px 34px',
                  borderRadius: '12px',
                  border: '1px solid var(--border-color)',
                  background: 'var(--surface)',
                  fontSize: '12px',
                  fontWeight: 700,
                  color: 'var(--text-primary)',
                  outline: 'none'
                }}
              />
            </div>
            <button type="submit" className="osm-route-map-search-btn">
              검색
            </button>
            <button
              type="button"
              onClick={handleMoveToMyLocation}
              className="osm-route-map-pill"
              title="내 주변으로 이동"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}
            >
              <Navigation size={13} /> 내 주변
            </button>
          </form>

          <div style={{ display: 'flex', gap: '6px' }}>
            {[
              { id: 'ALL', label: `전체 (${gatherings.length + itineraries.length})` },
              { id: 'GATHERING', label: `👥 모임 (${gatherings.length})` },
              { id: 'ITINERARY', label: `✈️ 여행 동선 (${itineraries.length})` }
            ].map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setFilterMode(tab.id)}
                className={`osm-route-map-pill ${filterMode === tab.id ? 'active' : ''}`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {searchResults.length > 0 && (
          <div className="osm-route-map-search-results">
            {searchResults.map((place) => (
              <button
                key={place.id}
                type="button"
                className="osm-route-map-search-item"
                onClick={() => handleMoveToPlace(place)}
              >
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>📍 {place.name}</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>{place.address}</div>
                </div>
                <span className="osm-route-map-badge">지도 이동</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 오픈소스 Leaflet 지도 캔버스 */}
      <div ref={mapContainerRef} style={{ width: '100%', height: '100%', zIndex: 1 }} />

      {/* 하단 플로팅 상세 카드 */}
      <div
        style={{
          position: 'absolute',
          bottom: '84px',
          left: '20px',
          right: '20px',
          zIndex: 500,
          transition: 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
          transform: selectedItem ? 'translateY(0)' : 'translateY(120%)',
          opacity: selectedItem ? 1 : 0,
          pointerEvents: selectedItem ? 'auto' : 'none'
        }}
      >
        {selectedItem?.type === 'gathering' && (
          <Card glass={true} padding={false} animate={false} className="animate-fade" style={{ overflow: 'hidden' }}>
            <div style={{ padding: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <span className="label-muted" style={{ color: 'var(--primary-orange)' }}>
                  👥 {selectedItem.data.category || 'GATHERING'} • {selectedItem.data.dates}
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      color: 'var(--text-primary)',
                      fontSize: '13px',
                      fontWeight: 700,
                      background: 'var(--bg-color)',
                      padding: '4px 10px',
                      borderRadius: '20px'
                    }}
                  >
                    <Users size={14} color="var(--primary-orange)" />
                    <span>
                      {selectedItem.data.currentJoining} / {selectedItem.data.maxJoining}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setSelectedItem(null)}
                    className="osm-route-map-pill"
                  >
                    닫기
                  </button>
                </div>
              </div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '6px', color: 'var(--text-primary)' }}>
                {selectedItem.data.title}
              </h3>
              <p
                style={{
                  color: 'var(--text-secondary)',
                  fontSize: '13px',
                  marginBottom: '14px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  fontWeight: 600
                }}
              >
                <MapPin size={14} /> {selectedItem.data.location}
              </p>
              <div style={{ display: 'flex', gap: '10px' }}>
                <PrimaryButton
                  onClick={() => navigate(`/gathering/${selectedItem.data.id}`)}
                  style={{ flex: 1 }}
                >
                  모임 상세·타임랩스 보기
                </PrimaryButton>
                <button
                  type="button"
                  onClick={() => handleJoinGathering(selectedItem.data)}
                  disabled={joining || selectedItem.data.currentJoining >= selectedItem.data.maxJoining}
                  className="osm-route-map-search-btn"
                  style={{ padding: '0 18px', borderRadius: '14px' }}
                >
                  {joining ? '신청 중...' : '참여 신청'}
                </button>
              </div>
            </div>
          </Card>
        )}

        {selectedItem?.type === 'itinerary' && (
          <Card glass={true} padding={false} animate={false} className="animate-fade" style={{ overflow: 'hidden' }}>
            <div style={{ padding: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <span className="label-muted" style={{ color: 'var(--secondary-blue)' }}>
                  ✈️ BOARDING PASS ROUTE • 총 {selectedItem.data.normalizedPoints?.length || 0}곳 ({selectedItem.data.totalKm || 0} km)
                </span>
                <button
                  type="button"
                  onClick={() => setSelectedItem(null)}
                  className="osm-route-map-pill"
                >
                  닫기
                </button>
              </div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '6px', color: 'var(--text-primary)' }}>
                {selectedItem.data.title}
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '12px', fontWeight: 600 }}>
                <Plane size={14} style={{ display: 'inline', marginRight: '4px' }} />
                {(selectedItem.data.normalizedPoints || []).map((p) => p.label).join(' → ')}
              </p>
              <PrimaryButton
                onClick={() => navigate(`/itinerary/${selectedItem.data.id}`)}
                style={{ width: '100%' }}
              >
                여행 동선 지도 & 보딩패스 GIF 스튜디오 열기
              </PrimaryButton>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default MapPage;
