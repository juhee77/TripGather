// TripGather — 사진 EXIF 위치·시간 추출 및 동선 보간·매칭 유틸리티 (#28)

const TILE_SIZE = 256;

/**
 * EXIF 날짜 문자열(YYYY:MM:DD HH:MM:SS)을 Date 객체로 파싱합니다.
 */
export function parseExifDateTime(str) {
  if (!str || typeof str !== 'string') return null;
  const m = str.trim().match(/^(\d{4})[:-](\d{2})[:-](\d{2})\s+(\d{2}):(\d{2}):(\d{2})/);
  if (!m) return null;
  const dt = new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +m[6]);
  return isNaN(dt.getTime()) || dt.getFullYear() < 1980 ? null : dt;
}

/**
 * 도·분·초(DMS) 배열과 방위(N/S/E/W)를 십진수 위경도로 변환합니다.
 */
export function dmsToDecimal(dms, ref) {
  if (!dms || dms.length < 3) return null;
  const toNum = (v) => {
    if (typeof v === 'number') return v;
    if (v && typeof v.numerator === 'number' && typeof v.denominator === 'number') {
      return v.denominator !== 0 ? v.numerator / v.denominator : 0;
    }
    return Number(v) || 0;
  };
  const d = toNum(dms[0]);
  const m = toNum(dms[1]);
  const s = toNum(dms[2]);
  if (!isFinite(d) || !isFinite(m) || !isFinite(s)) return null;
  let dec = d + m / 60 + s / 3600;
  if (ref === 'S' || ref === 'W') dec = -dec;
  return dec;
}

/**
 * 두 위경도 좌표 사이의 Haversine 거리(km)를 계산합니다.
 */
export function calcDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

/**
 * Web Mercator 투영 좌표를 계산합니다.
 */
export function projectMercator(lat, lng, zoom = 0) {
  const scale = TILE_SIZE * Math.pow(2, zoom);
  const x = ((lng + 180) / 360) * scale;
  const sinLat = Math.sin((Math.max(-85, Math.min(85, lat)) * Math.PI) / 180);
  const y = (0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * scale;
  return { x, y };
}

/**
 * Zoom 0 기준 Web Mercator 픽셀 좌표를 위경도로 역변환합니다.
 */
export function unprojectMercator0(x, y) {
  const lng = (x / TILE_SIZE) * 360 - 180;
  const n = Math.PI - (2 * Math.PI * y) / TILE_SIZE;
  const lat = (180 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
  return { lat, lng };
}

/**
 * 촬영 지점 배열을 불변(Immutable) 방식으로 정렬합니다.
 * @param {Array} spots 원본 스팟 배열
 * @param {'time-asc'|'time-desc'|'tsp'|'name'|'reverse'} mode 정렬 모드
 */
export function sortSpots(spots, mode) {
  if (!Array.isArray(spots) || spots.length < 2) {
    return Array.isArray(spots) ? [...spots] : [];
  }
  const copy = spots.map((s) => ({ ...s }));
  if (mode === 'time-asc') {
    return [...copy].sort((a, b) => (a.timeVal || 0) - (b.timeVal || 0));
  }
  if (mode === 'time-desc') {
    return [...copy].sort((a, b) => (b.timeVal || 0) - (a.timeVal || 0));
  }
  if (mode === 'name') {
    return [...copy].sort((a, b) =>
      String(a.name || '').localeCompare(String(b.name || ''), 'ko')
    );
  }
  if (mode === 'reverse') {
    return [...copy].reverse();
  }
  if (mode === 'tsp') {
    const remaining = [...copy];
    const ordered = [remaining.shift()];
    while (remaining.length > 0) {
      const last = ordered[ordered.length - 1];
      let bestIdx = 0;
      let bestDist = Infinity;
      remaining.forEach((cand, i) => {
        const d = calcDistanceKm(last.lat, last.lng, cand.lat, cand.lng);
        if (d < bestDist) {
          bestDist = d;
          bestIdx = i;
        }
      });
      ordered.push(remaining.splice(bestIdx, 1)[0]);
    }
    return ordered;
  }
  return copy;
}

/**
 * 시간 차이를 한국어 문자열(예: '30분 후', '1시간 30분 전')로 포맷합니다.
 */
export function formatTimeDiff(ms) {
  const abs = Math.abs(ms);
  const mins = Math.round(abs / 60000);
  const dir = ms >= 0 ? '후' : '전';
  if (mins < 1) return '같은 시각';
  if (mins < 60) return `${mins}분 ${dir}`;
  const hrs = Math.floor(mins / 60);
  const rem = mins % 60;
  if (hrs < 24) return rem ? `${hrs}시간 ${rem}분 ${dir}` : `${hrs}시간 ${dir}`;
  const days = Math.floor(hrs / 24);
  return `${days}일 ${hrs % 24}시간 ${dir}`;
}

/**
 * 위치 미지정 사진들을 앞뒤 GPS 사진의 촬영 시각 비율로 메르카토르 선형 보간해 추정합니다. (Immutable)
 */
export function estimateUnplacedLocations(placedSpots, unplacedSpots, targetUids = null) {
  const anchors = (placedSpots || [])
    .filter((s) => s.lat != null && s.lng != null && !s.timeFromFile && s.timeVal)
    .sort((a, b) => a.timeVal - b.timeVal);

  const fallbackAnchors =
    anchors.length > 0
      ? anchors
      : (placedSpots || [])
          .filter((s) => s.lat != null && s.lng != null && s.timeVal)
          .sort((a, b) => a.timeVal - b.timeVal);

  if (fallbackAnchors.length === 0) {
    return {
      placedSpots: [...(placedSpots || [])],
      unplacedSpots: [...(unplacedSpots || [])],
      estimatedCount: 0,
      skippedNoTimeCount: 0
    };
  }

  const targetSet = targetUids ? new Set(targetUids) : null;
  let estimatedCount = 0;
  let skippedNoTimeCount = 0;
  const newlyPlaced = [];
  const remainingUnplaced = [];

  for (const spot of unplacedSpots || []) {
    if (targetSet && !targetSet.has(spot.uid)) {
      remainingUnplaced.push({ ...spot });
      continue;
    }
    if (spot.timeFromFile || !spot.timeVal) {
      skippedNoTimeCount += 1;
      remainingUnplaced.push({ ...spot });
      continue;
    }

    const t = spot.timeVal;
    let prev = null;
    let next = null;
    for (const a of fallbackAnchors) {
      if (a.timeVal <= t) prev = a;
      if (a.timeVal >= t && !next) next = a;
    }

    let lat;
    let lng;
    let detail;
    if (prev && next && prev !== next && next.timeVal > prev.timeVal) {
      const ratio = Math.max(0, Math.min(1, (t - prev.timeVal) / (next.timeVal - prev.timeVal)));
      const p1 = projectMercator(prev.lat, prev.lng, 0);
      const p2 = projectMercator(next.lat, next.lng, 0);
      const ll = unprojectMercator0(
        p1.x + (p2.x - p1.x) * ratio,
        p1.y + (p2.y - p1.y) * ratio
      );
      lat = ll.lat;
      lng = ll.lng;
      detail = `${prev.name || '이전 지점'} → ${next.name || '다음 지점'} (${Math.round(ratio * 100)}%)`;
    } else {
      const ref = prev || next;
      const jitter = ((estimatedCount + 1) * 0.00025) * (estimatedCount % 2 === 0 ? 1 : -1);
      lat = ref.lat + jitter;
      lng = ref.lng + jitter;
      detail = `${ref.name || '기준 지점'} 인근 (${formatTimeDiff(t - ref.timeVal)})`;
    }

    newlyPlaced.push({
      ...spot,
      lat,
      lng,
      loc: 'estimated',
      estimateDetail: detail
    });
    estimatedCount += 1;
  }

  const nextPlaced = sortSpots([...(placedSpots || []), ...newlyPlaced], 'time-asc');
  return {
    placedSpots: nextPlaced,
    unplacedSpots: remainingUnplaced,
    estimatedCount,
    skippedNoTimeCount
  };
}

/**
 * 위치 미지정 사진들을 TripGather 여정(Itinerary)의 특정 RoutePoint 경유지에 원클릭 매칭합니다. (Immutable)
 */
export function matchUnplacedToRoutePoint(placedSpots, unplacedSpots, targetUids, routePoint) {
  if (!routePoint || routePoint.lat == null || routePoint.lng == null) {
    return {
      placedSpots: [...(placedSpots || [])],
      unplacedSpots: [...(unplacedSpots || [])],
      matchedCount: 0
    };
  }
  const targetSet = new Set(targetUids || []);
  const newlyPlaced = [];
  const remainingUnplaced = [];

  for (const spot of unplacedSpots || []) {
    if (!targetSet.has(spot.uid)) {
      remainingUnplaced.push({ ...spot });
      continue;
    }
    newlyPlaced.push({
      ...spot,
      lat: Number(routePoint.lat),
      lng: Number(routePoint.lng),
      loc: 'route',
      routePointId: routePoint.id ?? null,
      dayNumber: routePoint.dayNumber ?? 1,
      sequenceOrder: routePoint.sequenceOrder ?? null,
      name: routePoint.label || spot.name,
      estimateDetail: `DAY ${routePoint.dayNumber || 1} · ${routePoint.label || '경유지'}`
    });
  }

  return {
    placedSpots: [...(placedSpots || []), ...newlyPlaced],
    unplacedSpots: remainingUnplaced,
    matchedCount: newlyPlaced.length
  };
}

/**
 * GPS가 있는 사진이 여정 RoutePoint 반경(기본 0.5km) 이내에 있으면 자동으로 경유지 메타데이터를 연결합니다. (Immutable)
 */
export function autoMatchPhotosToRoutePoints(spots, routePoints, thresholdKm = 0.5) {
  if (!Array.isArray(spots)) return [];
  if (!Array.isArray(routePoints) || routePoints.length === 0) {
    return spots.map((s) => ({ ...s }));
  }
  const validPoints = routePoints.filter((rp) => rp && rp.lat != null && rp.lng != null);
  if (validPoints.length === 0) {
    return spots.map((s) => ({ ...s }));
  }

  return spots.map((spot) => {
    if (spot.lat == null || spot.lng == null) return { ...spot };
    let closest = null;
    let minDist = Infinity;
    for (const rp of validPoints) {
      const d = calcDistanceKm(spot.lat, spot.lng, Number(rp.lat), Number(rp.lng));
      if (d < minDist) {
        minDist = d;
        closest = rp;
      }
    }
    if (closest && minDist <= thresholdKm) {
      return {
        ...spot,
        routePointId: spot.routePointId ?? closest.id ?? null,
        dayNumber: spot.dayNumber ?? closest.dayNumber ?? 1,
        name:
          !spot.name || /^(IMG|DSC|PHOTO|PXL|카카오|사진)[_-]?\d*/i.test(spot.name)
            ? closest.label || spot.name
            : spot.name
      };
    }
    return { ...spot };
  });
}

/**
 * 날짜/시간 표시용 문자열을 반환합니다.
 */
export function spotWhen(spot) {
  if (!spot || !spot.date) return '';
  const dt = spot.date instanceof Date ? spot.date : new Date(spot.date);
  if (isNaN(dt.getTime())) return '';
  const m = String(dt.getMonth() + 1).padStart(2, '0');
  const d = String(dt.getDate()).padStart(2, '0');
  const hh = String(dt.getHours()).padStart(2, '0');
  const mm = String(dt.getMinutes()).padStart(2, '0');
  return `${m}.${d} ${hh}:${mm}`;
}

export function todayKey(d = new Date()) {
  const dt = d instanceof Date ? d : new Date(d);
  return `${dt.getFullYear()}.${String(dt.getMonth() + 1).padStart(2, '0')}.${String(
    dt.getDate()
  ).padStart(2, '0')}`;
}

/**
 * 순수 JS 기반 JPEG EXIF 바이너리 파서 (외부 CDN 의존 없이 GPS·촬영시각 추출)
 */
function parseJpegExifBuffer(arrayBuffer) {
  try {
    const view = new DataView(arrayBuffer);
    if (view.byteLength < 12 || view.getUint16(0, false) !== 0xffd8) return null;
    let offset = 2;
    while (offset + 4 < view.byteLength) {
      const marker = view.getUint16(offset, false);
      offset += 2;
      if (marker === 0xffe1) {
        const length = view.getUint16(offset, false);
        if (offset + 8 >= view.byteLength) return null;
        // 'Exif\0\0' 확인
        if (
          view.getUint32(offset + 2, false) === 0x45786966 &&
          view.getUint16(offset + 6, false) === 0x0000
        ) {
          return readTiffHeader(view, offset + 8, length - 8);
        }
        offset += length;
      } else if ((marker & 0xff00) !== 0xff00) {
        break;
      } else {
        const segLen = view.getUint16(offset, false);
        offset += segLen;
      }
    }
  } catch {
    return null;
  }
  return null;
}

function readTiffHeader(view, tiffStart) {
  if (tiffStart + 8 > view.byteLength) return null;
  const endian = view.getUint16(tiffStart, false);
  const little = endian === 0x4949;
  if (!little && endian !== 0x4d4d) return null;
  if (view.getUint16(tiffStart + 2, little) !== 0x002a) return null;
  const firstIfdOffset = view.getUint32(tiffStart + 4, little);
  const ifd0 = readIfd(view, tiffStart, tiffStart + firstIfdOffset, little);

  let dateStr = ifd0[0x0132] || null; // DateTime
  if (ifd0[0x8769]) {
    const exifIfd = readIfd(view, tiffStart, tiffStart + ifd0[0x8769], little);
    dateStr = exifIfd[0x9003] || exifIfd[0x9004] || dateStr;
  }

  let lat = null;
  let lng = null;
  if (ifd0[0x8825]) {
    const gpsIfd = readIfd(view, tiffStart, tiffStart + ifd0[0x8825], little);
    const latRef = gpsIfd[0x0001] || 'N';
    const latDms = gpsIfd[0x0002];
    const lngRef = gpsIfd[0x0003] || 'E';
    const lngDms = gpsIfd[0x0004];
    const parsedLat = dmsToDecimal(latDms, latRef);
    const parsedLng = dmsToDecimal(lngDms, lngRef);
    if (
      parsedLat != null &&
      parsedLng != null &&
      !(Math.abs(parsedLat) < 1e-5 && Math.abs(parsedLng) < 1e-5)
    ) {
      lat = parsedLat;
      lng = parsedLng;
    }
  }

  return {
    lat,
    lng,
    date: parseExifDateTime(dateStr)
  };
}

function readIfd(view, tiffStart, dirStart, little) {
  const tags = {};
  if (dirStart + 2 > view.byteLength) return tags;
  const entries = view.getUint16(dirStart, little);
  for (let i = 0; i < entries; i++) {
    const entryOffset = dirStart + 2 + i * 12;
    if (entryOffset + 12 > view.byteLength) break;
    const tag = view.getUint16(entryOffset, little);
    const type = view.getUint16(entryOffset + 2, little);
    const numValues = view.getUint32(entryOffset + 4, little);
    const valueOffset = entryOffset + 8;

    if (type === 2) {
      // ASCII
      const strOffset =
        numValues > 4 ? tiffStart + view.getUint32(valueOffset, little) : valueOffset;
      if (strOffset + numValues <= view.byteLength) {
        let s = '';
        for (let n = 0; n < numValues - 1; n++) {
          const ch = view.getUint8(strOffset + n);
          if (ch === 0) break;
          s += String.fromCharCode(ch);
        }
        tags[tag] = s.trim();
      }
    } else if (type === 4 && numValues === 1) {
      // LONG (SubIFD pointer 등)
      tags[tag] = view.getUint32(valueOffset, little);
    } else if (type === 5 && numValues >= 3) {
      // RATIONAL (GPS DMS)
      const ratOffset = tiffStart + view.getUint32(valueOffset, little);
      if (ratOffset + numValues * 8 <= view.byteLength) {
        const arr = [];
        for (let n = 0; n < numValues; n++) {
          const num = view.getUint32(ratOffset + n * 8, little);
          const den = view.getUint32(ratOffset + n * 8 + 4, little);
          arr.push(den !== 0 ? num / den : 0);
        }
        tags[tag] = arr;
      }
    }
  }
  return tags;
}

/**
 * 이미지 파일에서 EXIF(위경도 및 촬영 시각)를 추출합니다.
 */
export async function extractExifFromFile(file) {
  if (!file) return { lat: null, lng: null, date: null };
  try {
    if (typeof file.arrayBuffer === 'function') {
      const buf = await file.arrayBuffer();
      const parsed = parseJpegExifBuffer(buf);
      if (parsed && (parsed.lat != null || parsed.date != null)) {
        return parsed;
      }
    }
  } catch {
    // fallback below
  }

  if (typeof window !== 'undefined' && window.EXIF) {
    return new Promise((resolve) => {
      try {
        window.EXIF.getData(file, function () {
          const latDMS = window.EXIF.getTag(this, 'GPSLatitude');
          const latRef = window.EXIF.getTag(this, 'GPSLatitudeRef') || 'N';
          const lngDMS = window.EXIF.getTag(this, 'GPSLongitude');
          const lngRef = window.EXIF.getTag(this, 'GPSLongitudeRef') || 'E';
          const dtStr =
            window.EXIF.getTag(this, 'DateTimeOriginal') ||
            window.EXIF.getTag(this, 'DateTimeDigitized') ||
            window.EXIF.getTag(this, 'DateTime');
          let lat = dmsToDecimal(latDMS, latRef);
          let lng = dmsToDecimal(lngDMS, lngRef);
          if (lat !== null && lng !== null && Math.abs(lat) < 1e-5 && Math.abs(lng) < 1e-5) {
            lat = null;
            lng = null;
          }
          resolve({ lat, lng, date: parseExifDateTime(dtStr) });
        });
      } catch {
        resolve({ lat: null, lng: null, date: null });
      }
    });
  }
  return { lat: null, lng: null, date: null };
}

/**
 * 캔버스를 이용해 정사각형 썸네일 DataURL을 생성합니다.
 */
export function makeThumbnail(source, edge = 280) {
  const c = document.createElement('canvas');
  c.width = edge;
  c.height = edge;
  const ctx = c.getContext('2d');
  const sw = source.width || source.naturalWidth || edge;
  const sh = source.height || source.naturalHeight || edge;
  const s = Math.min(sw, sh);
  ctx.drawImage(source, (sw - s) / 2, (sh - s) / 2, s, s, 0, 0, edge, edge);
  return c.toDataURL('image/jpeg', 0.85);
}

/**
 * 이미지의 대표 색상(HSL)을 추출합니다 (TripGather 기본 오렌지 폴백).
 */
export function dominantColor(source) {
  try {
    const c = document.createElement('canvas');
    c.width = 24;
    c.height = 24;
    const ctx = c.getContext('2d');
    ctx.drawImage(source, 0, 0, 24, 24);
    const d = ctx.getImageData(0, 0, 24, 24).data;
    let r = 0;
    let g = 0;
    let b = 0;
    let n = 0;
    for (let i = 0; i < d.length; i += 4) {
      const max = Math.max(d[i], d[i + 1], d[i + 2]);
      const min = Math.min(d[i], d[i + 1], d[i + 2]);
      if (max - min < 22 || max < 35 || min > 235) continue;
      r += d[i];
      g += d[i + 1];
      b += d[i + 2];
      n++;
    }
    if (!n) return '#FF5C00';
    r = Math.round(r / n);
    g = Math.round(g / n);
    b = Math.round(b / n);
    const max = Math.max(r, g, b) / 255;
    const min = Math.min(r, g, b) / 255;
    let h = 0;
    const dlt = max - min;
    if (dlt) {
      if (max === r / 255) h = ((g / 255 - b / 255) / dlt) % 6;
      else if (max === g / 255) h = (b / 255 - r / 255) / dlt + 2;
      else h = (r / 255 - g / 255) / dlt + 4;
      h = Math.round(h * 60);
      if (h < 0) h += 360;
    }
    return `hsl(${h}, 82%, 56%)`;
  } catch {
    return '#FF5C00';
  }
}

/**
 * 브라우저 이미지 파일 리사이즈 및 썸네일 생성
 */
export function loadPhotoFile(file, maxEdge = 1400, quality = 0.88, thumbEdge = 300) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      try {
        const scale = Math.min(1, maxEdge / Math.max(img.width, img.height));
        const w = Math.max(1, Math.round(img.width * scale));
        const h = Math.max(1, Math.round(img.height * scale));
        const c = document.createElement('canvas');
        c.width = w;
        c.height = h;
        c.getContext('2d').drawImage(img, 0, 0, w, h);
        const dataUrl = c.toDataURL('image/jpeg', quality);
        const thumbUrl = makeThumbnail(c, thumbEdge);
        const color = dominantColor(c);
        URL.revokeObjectURL(url);
        const scaled = new Image();
        scaled.onload = () =>
          resolve({ img: scaled, dataUrl, thumbUrl, color, width: w, height: h });
        scaled.onerror = reject;
        scaled.src = dataUrl;
      } catch (e) {
        URL.revokeObjectURL(url);
        reject(e);
      }
    };
    img.onerror = (e) => {
      URL.revokeObjectURL(url);
      reject(e);
    };
    img.src = url;
  });
}

/**
 * 원격 URL 이미지를 스팟 객체용 캔버스 이미지로 로드합니다.
 */
export function loadRemotePhoto(photoUrl) {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      try {
        const thumbUrl = makeThumbnail(img, 280);
        const color = dominantColor(img);
        resolve({ img, dataUrl: photoUrl, thumbUrl, color });
      } catch {
        resolve({ img, dataUrl: photoUrl, thumbUrl: photoUrl, color: '#FF5C00' });
      }
    };
    img.onerror = () => resolve({ img: null, dataUrl: photoUrl, thumbUrl: photoUrl, color: '#FF5C00' });
    img.src = photoUrl;
  });
}

/**
 * 모바일 카메라 즉석 촬영 시 현재 브라우저 GPS 좌표를 획득합니다.
 */
export function captureCurrentGeolocation(timeoutMs = 5000) {
  if (typeof navigator === 'undefined' || !navigator.geolocation) {
    return Promise.resolve(null);
  }
  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      (pos) =>
        resolve({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        }),
      () => resolve(null),
      { enableHighAccuracy: true, timeout: timeoutMs, maximumAge: 60000 }
    );
  });
}

/**
 * 데모 보딩패스 샘플 코스를 생성합니다 (여정 경유지가 없을 때 기본 제주 투어).
 */
export function createDemoSpotGraphic(info, idx) {
  const c = document.createElement('canvas');
  c.width = 720;
  c.height = 720;
  const ctx = c.getContext('2d');
  const g = ctx.createLinearGradient(0, 0, 720, 720);
  g.addColorStop(0, info.c1 || '#FF5C00');
  g.addColorStop(1, info.c2 || '#0F172A');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, 720, 720);

  ctx.fillStyle = 'rgba(255,255,255,0.14)';
  ctx.beginPath();
  ctx.arc(540, 170, 95, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = 'rgba(15,23,42,0.42)';
  ctx.fillRect(0, 480, 720, 240);

  ctx.font = '140px sans-serif';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(info.emoji || '✈️', 360, 290);

  ctx.fillStyle = '#FFFFFF';
  ctx.font = '800 44px "Pretendard Variable", sans-serif';
  ctx.fillText(`GATE 0${idx + 1} · ${info.name}`, 360, 565);
  ctx.fillStyle = 'rgba(255,255,255,0.82)';
  ctx.font = '600 24px "Pretendard Variable", sans-serif';
  ctx.fillText('TRIPGATHER BOARDING LOG', 360, 620);

  const dataUrl = c.toDataURL('image/jpeg', 0.88);
  const thumbUrl = makeThumbnail(c, 260);
  return { canvas: c, dataUrl, thumbUrl };
}
