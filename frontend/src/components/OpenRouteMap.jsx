import React, { useEffect, useRef, useState, useMemo } from 'react';
import {
  OPEN_MAP_TILES,
  ensureLeafletLoaded,
  normalizeRoutePointsForMap,
  calculateTotalRouteKm,
  resolveFallbackCoordinates,
  searchOpenStreetMapPlaces,
  getDayColor
} from '../utils/openMapService';
import './OpenRouteMap.css';

/**
 * 오픈소스 기반(Leaflet + OpenStreetMap / Carto / Esri + Nominatim) 여행 동선 & 위치 지도 컴포넌트
 * - API 키 없이 모든 여정 동선(마커·폴리라인·Day별 필터), 장소 검색, 지도 클릭 좌표 지정을 지원합니다.
 */
const OpenRouteMap = ({
  title = '여행 동선 지도 🗺️',
  subtitle = '',
  routePoints = [],
  fallbackLocationText = '',
  height = 310,
  defaultTile = 'voyager',
  enablePlaceSearch = false,
  searchPlaceholder = '장소·명소 검색 (예: 성산일출봉, 해운대, 강남역)',
  allowMapClickPick = false,
  onSelectPlace = null,
  onPointClick = null,
  showRouteSummaryStrip = true
}) => {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const tileLayerRef = useRef(null);
  const markersLayerRef = useRef(null);

  const [mapReady, setMapReady] = useState(false);
  const [tileKey, setTileKey] = useState(defaultTile);
  const [selectedDay, setSelectedDay] = useState('ALL');
  const [activeStopIdx, setActiveStopIdx] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  const normalizedPoints = useMemo(
    () => normalizeRoutePointsForMap(routePoints),
    [routePoints]
  );

  const availableDays = useMemo(() => {
    const set = new Set(normalizedPoints.map((p) => p.dayNumber || 1));
    return Array.from(set).sort((a, b) => a - b);
  }, [normalizedPoints]);

  const filteredPoints = useMemo(() => {
    if (selectedDay === 'ALL') return normalizedPoints;
    return normalizedPoints.filter((p) => Number(p.dayNumber) === Number(selectedDay));
  }, [normalizedPoints, selectedDay]);

  const totalKm = useMemo(
    () => calculateTotalRouteKm(filteredPoints),
    [filteredPoints]
  );

  const fallbackCenter = useMemo(
    () => resolveFallbackCoordinates(fallbackLocationText),
    [fallbackLocationText]
  );

  // 지도 초기화
  useEffect(() => {
    let cancelled = false;
    ensureLeafletLoaded()
      .then((L) => {
        if (cancelled || !L || !mapContainerRef.current) return;
        if (!mapInstanceRef.current) {
          const initialLat = filteredPoints[0]?.lat ?? fallbackCenter.lat;
          const initialLng = filteredPoints[0]?.lng ?? fallbackCenter.lng;
          const map = L.map(mapContainerRef.current, {
            zoomControl: true,
            attributionControl: true
          }).setView([initialLat, initialLng], filteredPoints.length > 0 ? 12 : 11);

          const tileCfg = OPEN_MAP_TILES[tileKey] || OPEN_MAP_TILES.voyager;
          tileLayerRef.current = L.tileLayer(tileCfg.url, {
            attribution: tileCfg.attribution,
            maxZoom: 19
          }).addTo(map);

          markersLayerRef.current = L.layerGroup().addTo(map);
          mapInstanceRef.current = map;
          setMapReady(true);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
      setMapReady(false);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 지도 클릭으로 좌표/장소 선택 이벤트 바인딩
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!mapReady || !map) return;

    const handleMapClick = (e) => {
      if (!allowMapClickPick || !onSelectPlace) return;
      const lat = Number(e.latlng.lat.toFixed(6));
      const lng = Number(e.latlng.lng.toFixed(6));
      onSelectPlace({
        name: `지도 선택 지점 (${lat.toFixed(3)}, ${lng.toFixed(3)})`,
        address: `위도 ${lat}, 경도 ${lng}`,
        lat,
        lng,
        source: 'map-click'
      });
    };

    map.on('click', handleMapClick);
    return () => {
      map.off('click', handleMapClick);
    };
  }, [mapReady, allowMapClickPick, onSelectPlace]);

  // 타일 스타일 변경
  useEffect(() => {
    const L = window.L;
    const map = mapInstanceRef.current;
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

  // 경유지 마커 및 폴리라인 렌더링
  useEffect(() => {
    const L = window.L;
    const map = mapInstanceRef.current;
    const layer = markersLayerRef.current;
    if (!mapReady || !L || !map || !layer) return;

    layer.clearLayers();

    if (filteredPoints.length === 0) {
      const c = resolveFallbackCoordinates(fallbackLocationText);
      map.setView([c.lat, c.lng], 12);
      if (fallbackLocationText) {
        const singleIcon = L.divIcon({
          className: 'osm-route-map-div-icon',
          html: `<div class="osm-route-map-marker">
            <div class="osm-route-map-marker-bubble">
              <span class="osm-route-map-marker-num">📍</span>
              <span>${String(fallbackLocationText).slice(0, 18)}</span>
            </div>
          </div>`,
          iconSize: [140, 34],
          iconAnchor: [70, 28]
        });
        L.marker([c.lat, c.lng], { icon: singleIcon }).addTo(layer);
      }
      return;
    }

    const latlngs = filteredPoints.map((p) => [p.lat, p.lng]);

    if (latlngs.length >= 2) {
      // 외곽선 + 메인 동선 라인
      L.polyline(latlngs, {
        color: '#0F172A',
        weight: 7,
        opacity: 0.25
      }).addTo(layer);

      L.polyline(latlngs, {
        color: '#FF5C00',
        weight: 4,
        opacity: 0.92,
        dashArray: '8, 6'
      }).addTo(layer);
    }

    filteredPoints.forEach((pt, idx) => {
      const color = getDayColor(pt.dayNumber);
      const safeLabel = String(pt.label || `경유지 ${idx + 1}`)
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
      const thumbHtml = pt.photoUrl
        ? `<img src="${pt.photoUrl}" class="osm-route-map-marker-thumb" alt="" />`
        : `<span class="osm-route-map-marker-num" style="background:${color}">${pt.globalOrder || idx + 1}</span>`;

      const icon = L.divIcon({
        className: 'osm-route-map-div-icon',
        html: `<div class="osm-route-map-marker">
          <div class="osm-route-map-marker-bubble" style="${activeStopIdx === idx ? 'border-color:#FF5C00;transform:scale(1.06);' : ''}">
            ${thumbHtml}
            <span>D${pt.dayNumber} · ${safeLabel.slice(0, 14)}</span>
          </div>
        </div>`,
        iconSize: [160, 36],
        iconAnchor: [80, 30]
      });

      const marker = L.marker([pt.lat, pt.lng], { icon }).addTo(layer);
      const timeInfo = pt.startTime
        ? `<div style="font-size:11px;color:#FF5C00;font-weight:800;margin-top:3px;">✈️ ${pt.startTime}${pt.endTime ? ' ~ ' + pt.endTime : ''}</div>`
        : '';
      const memoInfo = pt.memo
        ? `<div style="font-size:11px;color:#475569;margin-top:4px;">📝 ${String(pt.memo).slice(0, 60)}</div>`
        : '';

      marker.bindPopup(
        `<div style="min-width:150px;font-family:sans-serif;">
          <div style="font-size:10px;font-weight:900;color:${color};">DAY ${pt.dayNumber} · GATE #${pt.sequenceOrder}</div>
          <div style="font-size:13px;font-weight:900;color:#0F172A;margin-top:2px;">${safeLabel}</div>
          ${timeInfo}
          ${memoInfo}
        </div>`
      );

      marker.on('click', () => {
        setActiveStopIdx(idx);
        if (onPointClick) onPointClick(pt, idx);
      });
    });

    if (latlngs.length === 1) {
      map.setView(latlngs[0], 14);
    } else {
      map.fitBounds(L.latLngBounds(latlngs), { padding: [36, 36], maxZoom: 15 });
    }
  }, [mapReady, filteredPoints, fallbackLocationText, activeStopIdx, onPointClick]);

  const handleFocusStop = (pt, idx) => {
    setActiveStopIdx(idx);
    const map = mapInstanceRef.current;
    if (map && Number.isFinite(pt.lat) && Number.isFinite(pt.lng)) {
      map.flyTo([pt.lat, pt.lng], 15, { duration: 0.6 });
    }
    if (onPointClick) onPointClick(pt, idx);
  };

  const handleSearchSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!searchQuery.trim()) return;
    setSearching(true);
    try {
      const results = await searchOpenStreetMapPlaces(searchQuery);
      setSearchResults(results);
    } finally {
      setSearching(false);
    }
  };

  const handlePickSearchResult = (place) => {
    setSearchResults([]);
    setSearchQuery(place.name);
    const map = mapInstanceRef.current;
    if (map && Number.isFinite(place.lat) && Number.isFinite(place.lng)) {
      map.flyTo([place.lat, place.lng], 15, { duration: 0.6 });
    }
    if (onSelectPlace) {
      onSelectPlace(place);
    }
  };

  return (
    <div className="osm-route-map-card">
      <div className="osm-route-map-header">
        <div className="osm-route-map-title-wrap">
          <h4 className="osm-route-map-title">{title}</h4>
          <span className="osm-route-map-badge">
            {filteredPoints.length > 0
              ? `총 ${filteredPoints.length}개 경유지 · ${totalKm} km`
              : subtitle || '오픈소스 실시간 지도'}
          </span>
        </div>

        <div className="osm-route-map-controls">
          {Object.values(OPEN_MAP_TILES).map((t) => (
            <button
              key={t.key}
              type="button"
              className={`osm-route-map-pill ${tileKey === t.key ? 'active' : ''}`}
              onClick={() => setTileKey(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {availableDays.length > 1 && (
        <div className="osm-route-map-day-bar">
          <button
            type="button"
            className={`osm-route-map-pill ${selectedDay === 'ALL' ? 'active' : ''}`}
            onClick={() => setSelectedDay('ALL')}
          >
            전체 동선 ({normalizedPoints.length})
          </button>
          {availableDays.map((day) => (
            <button
              key={day}
              type="button"
              className={`osm-route-map-pill ${Number(selectedDay) === Number(day) ? 'active' : ''}`}
              onClick={() => setSelectedDay(day)}
            >
              DAY {day}
            </button>
          ))}
        </div>
      )}

      {enablePlaceSearch && (
        <div className="osm-route-map-search-bar">
          <div className="osm-route-map-search-row">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleSearchSubmit();
                }
              }}
              placeholder={searchPlaceholder}
              className="osm-route-map-search-input"
            />
            <button
              type="button"
              onClick={handleSearchSubmit}
              className="osm-route-map-search-btn"
              disabled={searching}
            >
              {searching ? '검색 중...' : '🔍 장소 검색'}
            </button>
          </div>

          {searchResults.length > 0 && (
            <div className="osm-route-map-search-results">
              {searchResults.map((place) => (
                <button
                  key={place.id}
                  type="button"
                  className="osm-route-map-search-item"
                  onClick={() => handlePickSearchResult(place)}
                >
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>
                      📍 {place.name}
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                      {place.address}
                    </div>
                  </div>
                  <span className="osm-route-map-badge">+ 선택</span>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      <div className="osm-route-map-canvas-wrap" style={{ height: `${height}px` }}>
        <div ref={mapContainerRef} className="osm-route-map-canvas" />
        {allowMapClickPick && (
          <div className="osm-route-map-empty-overlay">
            <span>👆 지도를 직접 클릭하거나 위 검색창에서 장소를 검색해 좌표를 지정할 수 있습니다.</span>
          </div>
        )}
      </div>

      {showRouteSummaryStrip && filteredPoints.length > 0 && (
        <div className="osm-route-map-strip">
          {filteredPoints.map((pt, idx) => (
            <button
              key={pt.id || `${pt.dayNumber}-${pt.sequenceOrder}-${idx}`}
              type="button"
              className={`osm-route-map-chip ${activeStopIdx === idx ? 'active' : ''}`}
              onClick={() => handleFocusStop(pt, idx)}
            >
              <span
                className="osm-route-map-chip-num"
                style={{ background: getDayColor(pt.dayNumber) }}
              >
                {pt.globalOrder || idx + 1}
              </span>
              <span>{pt.label}</span>
              {pt.segmentKm > 0 && (
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                  +{pt.segmentKm.toFixed(1)}km
                </span>
              )}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default OpenRouteMap;
