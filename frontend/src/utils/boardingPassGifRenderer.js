// TripGather — 보딩패스 & 여권 스탬프 테마 동선 타임랩스(GIF/MP4/PNG) 렌더러 (#28)

import { calcDistanceKm, projectMercator, spotWhen, todayKey } from './photoExifParser';

const TILE_SIZE = 256;

export const GIF_LAYOUTS = {
  square: {
    GW: 540,
    GH: 540,
    HEADER_H: 48,
    MAP_Y: 48,
    MAP_W: 540,
    MAP_H: 376,
    CAPTION_Y: 424,
    CAPTION_H: 116,
    THUMB: 84,
    CARD_W: 196,
    CARD_H: 230
  },
  wide: {
    GW: 640,
    GH: 360,
    HEADER_H: 40,
    MAP_Y: 40,
    MAP_W: 640,
    MAP_H: 232,
    CAPTION_Y: 272,
    CAPTION_H: 88,
    THUMB: 64,
    CARD_W: 164,
    CARD_H: 192
  },
  story: {
    GW: 432,
    GH: 768,
    HEADER_H: 52,
    MAP_Y: 52,
    MAP_W: 432,
    MAP_H: 564,
    CAPTION_Y: 616,
    CAPTION_H: 152,
    THUMB: 108,
    CARD_W: 212,
    CARD_H: 252
  }
};

export const GIF_QUALITY = {
  normal: { scale: 1, colors: 192, frameMul: 1, delayMul: 1 },
  light: { scale: 0.75, colors: 96, frameMul: 0.65, delayMul: 1.35 }
};

export const GIF_ZOOM = {
  street: { boost: 2, maxZ: 17 },
  area: { boost: 1, maxZ: 15 }
};

export const TRANSPORT_ICONS = {
  plane: '✈️',
  car: '🚗',
  walk: '🥾'
};

const TILE_STYLES = {
  satellite: {
    bg: '#0B192C',
    tileUrl: (z, x, y) =>
      `https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}`
  },
  street: {
    bg: '#E8EEF5',
    tileUrl: (z, x, y) =>
      `https://tile.openstreetmap.org/${z}/${x}/${y}.png`
  },
  topo: {
    bg: '#E2E8F0',
    tileUrl: (z, x, y) =>
      `https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${z}/${y}/${x}`
  }
};

const tileCache = new Map();

function loadTile(styleKey, z, x, y) {
  const n = Math.pow(2, z);
  const wx = ((x % n) + n) % n;
  if (y < 0 || y >= n) return Promise.resolve(null);
  const style = TILE_STYLES[styleKey] || TILE_STYLES.satellite;
  const key = `${styleKey}:${z}/${wx}/${y}`;
  if (tileCache.has(key)) return tileCache.get(key);

  const p = new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    const timer = setTimeout(() => resolve(null), 5000);
    img.onload = () => {
      clearTimeout(timer);
      resolve(img);
    };
    img.onerror = () => {
      clearTimeout(timer);
      resolve(null);
    };
    img.src = style.tileUrl(z, wx, y);
  });
  tileCache.set(key, p);
  return p;
}

export function computeFitView(pts, width, height, pad = 64, maxZoom = 15) {
  if (!pts || pts.length === 0) {
    return { centerLat: 33.38, centerLng: 126.55, zoom: 10 };
  }
  let minLat = Infinity;
  let maxLat = -Infinity;
  let minLng = Infinity;
  let maxLng = -Infinity;
  pts.forEach((s) => {
    minLat = Math.min(minLat, s.lat);
    maxLat = Math.max(maxLat, s.lat);
    minLng = Math.min(minLng, s.lng);
    maxLng = Math.max(maxLng, s.lng);
  });
  let zoom = maxZoom;
  for (let z = maxZoom; z >= 2; z--) {
    const p1 = projectMercator(maxLat, minLng, z);
    const p2 = projectMercator(minLat, maxLng, z);
    if (
      Math.abs(p2.x - p1.x) <= width - pad * 2 &&
      Math.abs(p2.y - p1.y) <= height - pad * 2
    ) {
      zoom = z;
      break;
    }
  }
  const c1 = projectMercator(maxLat, minLng, zoom);
  const c2 = projectMercator(minLat, maxLng, zoom);
  const cx = (c1.x + c2.x) / 2;
  const cy = (c1.y + c2.y) / 2;
  const scale = TILE_SIZE * Math.pow(2, zoom);
  const centerLng = (cx / scale) * 360 - 180;
  const n = Math.PI - (2 * Math.PI * cy) / scale;
  const centerLat = (180 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
  return { centerLat, centerLng, zoom };
}

function roundRect(ctx, x, y, w, h, r) {
  const rad = Math.min(r, w / 2, h / 2);
  ctx.beginPath();
  ctx.moveTo(x + rad, y);
  ctx.arcTo(x + w, y, x + w, y + h, rad);
  ctx.arcTo(x + w, y + h, x, y + h, rad);
  ctx.arcTo(x, y + h, x, y, rad);
  ctx.arcTo(x, y, x + w, y, rad);
  ctx.closePath();
}

function drawCoverImg(ctx, img, x, y, w, h) {
  if (!img) return;
  const iw = img.width || img.naturalWidth || w;
  const ih = img.height || img.naturalHeight || h;
  const ir = iw / ih;
  const br = w / h;
  let sx = 0;
  let sy = 0;
  let sw = iw;
  let sh = ih;
  if (ir > br) {
    sw = ih * br;
    sx = (iw - sw) / 2;
  } else {
    sh = iw / br;
    sy = (ih - sh) / 2;
  }
  ctx.drawImage(img, sx, sy, sw, sh, x, y, w, h);
}

function trimText(ctx, text, maxW) {
  const str = String(text || '');
  if (ctx.measureText(str).width <= maxW) return str;
  let s = str;
  while (s.length > 1 && ctx.measureText(`${s}…`).width > maxW) {
    s = s.slice(0, -1);
  }
  return `${s}…`;
}

function easeInOut(t) {
  return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
}

/**
 * MP4 하드웨어/브라우저 인코딩 가능 여부 확인
 */
export function canRecordMp4() {
  if (typeof window !== 'undefined' && typeof window.VideoEncoder === 'function') return true;
  if (typeof MediaRecorder === 'undefined' || !MediaRecorder.isTypeSupported) return false;
  return ['video/mp4;codecs=avc1.42E01E', 'video/mp4', 'video/webm;codecs=vp9', 'video/webm'].some(
    (t) => MediaRecorder.isTypeSupported(t)
  );
}

/**
 * 타임랩스(GIF/MP4) 프레임 스케줄 및 예상 파일 크기·시간을 산출합니다.
 */
export function planTravelGif(spots, format = 'square', options = {}) {
  const L = GIF_LAYOUTS[format] || GIF_LAYOUTS.square;
  const Q = GIF_QUALITY[options.quality || 'normal'] || GIF_QUALITY.normal;
  const Z = GIF_ZOOM[options.zoom || 'street'] || GIF_ZOOM.street;
  const follow = (options.camera || 'follow') === 'follow';
  const video = (options.output || 'gif') === 'mp4';

  const pad = Math.round(Math.min(L.MAP_W, L.MAP_H) * 0.16);
  const overview = computeFitView(spots, L.MAP_W, L.MAP_H, pad, 15);
  const closeZoom = Math.min(Z.maxZ, Math.max(overview.zoom + Z.boost, overview.zoom + 1));

  const scaleFrames = (n) => Math.max(2, Math.round(n * (video ? 1.5 : Q.frameMul)));
  const scaleDelay = (d) => (video ? Math.round(d * 0.65) : Math.round(d * Q.delayMul));

  const moveFrames = scaleFrames(follow ? 9 : 6);
  const zoomFrames = scaleFrames(4);
  const cardInFrames = scaleFrames(4);
  const holdFrames = video ? 8 : 3;
  const finaleHoldFrames = video ? 14 : 5;

  const viewAt = (lat, lng, z) => ({ centerLat: lat, centerLng: lng, zoom: z });
  const lerpView = (a, b, t) => ({
    centerLat: a.centerLat + (b.centerLat - a.centerLat) * t,
    centerLng: a.centerLng + (b.centerLng - a.centerLng) * t,
    zoom: a.zoom
  });

  const plan = [];
  const pushHold = (frame, count, totalDelay) => {
    if (!video) {
      plan.push({ ...frame, delay: scaleDelay(totalDelay) });
    } else {
      const per = Math.max(33, Math.round(totalDelay / count));
      for (let k = 0; k < count; k++) plan.push({ ...frame, delay: per });
    }
  };

  // 인트로 오버뷰
  pushHold({ view: overview, segIdx: 0, segT: 0, activeIdx: 0, cardAlpha: 0 }, holdFrames, 360);

  if (follow && spots.length > 0) {
    for (let f = 1; f <= zoomFrames; f++) {
      const t = easeInOut(f / zoomFrames);
      const z = Math.round(overview.zoom + (closeZoom - overview.zoom) * t);
      plan.push({
        view: lerpView({ ...overview, zoom: z }, viewAt(spots[0].lat, spots[0].lng, z), t),
        segIdx: 0,
        segT: 0,
        activeIdx: 0,
        cardAlpha: 0,
        delay: scaleDelay(75)
      });
    }
  }

  for (let i = 0; i < spots.length; i++) {
    const curView = follow ? viewAt(spots[i].lat, spots[i].lng, closeZoom) : overview;
    for (let f = 1; f <= cardInFrames; f++) {
      plan.push({
        view: curView,
        segIdx: i,
        segT: 0,
        activeIdx: i,
        cardAlpha: f / cardInFrames,
        delay: scaleDelay(70)
      });
    }
    pushHold(
      { view: curView, segIdx: i, segT: 0, activeIdx: i, cardAlpha: 1 },
      holdFrames,
      540
    );

    if (i < spots.length - 1) {
      const nextView = follow ? viewAt(spots[i + 1].lat, spots[i + 1].lng, closeZoom) : overview;
      for (let f = 1; f <= moveFrames; f++) {
        const t = easeInOut(f / moveFrames);
        plan.push({
          view: follow ? lerpView(curView, nextView, t) : overview,
          segIdx: i,
          segT: t,
          activeIdx: t > 0.72 ? i + 1 : i,
          cardAlpha: 0,
          delay: scaleDelay(65)
        });
      }
    }
  }

  if (follow && spots.length > 0) {
    const lastSpot = spots[spots.length - 1];
    for (let f = 1; f <= zoomFrames; f++) {
      const t = easeInOut(f / zoomFrames);
      const z = Math.round(closeZoom + (overview.zoom - closeZoom) * t);
      plan.push({
        view: lerpView(viewAt(lastSpot.lat, lastSpot.lng, z), { ...overview, zoom: z }, t),
        segIdx: spots.length - 1,
        segT: 0,
        activeIdx: spots.length - 1,
        cardAlpha: 0,
        delay: scaleDelay(75)
      });
    }
  }

  // 피날레 스탬프 홀드 프레임 (isFinale: true)
  const lastIdx = Math.max(0, spots.length - 1);
  pushHold(
    {
      view: overview,
      segIdx: lastIdx,
      segT: 0,
      activeIdx: lastIdx,
      cardAlpha: 1,
      isFinale: true
    },
    finaleHoldFrames,
    1050
  );

  let outW = Math.round(L.GW * Q.scale);
  let outH = Math.round(L.GH * Q.scale);
  if (video) {
    outW = Math.round(outW / 2) * 2;
    outH = Math.round(outH / 2) * 2;
  }

  const totalMs = plan.reduce((s, p) => s + p.delay, 0);
  const seconds = totalMs / 1000;
  const estBytes = video
    ? Math.round(seconds * (options.quality === 'light' ? 140000 : 280000))
    : Math.round(plan.length * outW * outH * (options.quality === 'light' ? 0.14 : 0.25));

  return { L, Q, Z, follow, video, overview, closeZoom, plan, outW, outH, seconds, estBytes };
}

export async function drawMapTilesToCanvas(ctx, view, x, y, w, h, styleKey = 'satellite') {
  const z = view.zoom;
  const centerPx = projectMercator(view.centerLat, view.centerLng, z);
  const topLeftPx = { x: centerPx.x - w / 2, y: centerPx.y - h / 2 };
  const startTileX = Math.floor(topLeftPx.x / TILE_SIZE);
  const endTileX = Math.floor((topLeftPx.x + w) / TILE_SIZE);
  const startTileY = Math.floor(topLeftPx.y / TILE_SIZE);
  const endTileY = Math.floor((topLeftPx.y + h) / TILE_SIZE);

  ctx.save();
  ctx.beginPath();
  ctx.rect(x, y, w, h);
  ctx.clip();

  const style = TILE_STYLES[styleKey] || TILE_STYLES.satellite;
  ctx.fillStyle = style.bg;
  ctx.fillRect(x, y, w, h);

  const jobs = [];
  for (let tx = startTileX; tx <= endTileX; tx++) {
    for (let ty = startTileY; ty <= endTileY; ty++) {
      jobs.push(loadTile(styleKey, z, tx, ty).then((img) => ({ img, tx, ty })));
    }
  }
  const results = await Promise.all(jobs);
  results.forEach(({ img, tx, ty }) => {
    const dx = Math.round(x + tx * TILE_SIZE - topLeftPx.x);
    const dy = Math.round(y + ty * TILE_SIZE - topLeftPx.y);
    if (img) {
      ctx.drawImage(img, dx, dy, TILE_SIZE, TILE_SIZE);
    }
  });

  // 가독성을 위한 살짝 어두운 보딩패스 비네팅
  const grad = ctx.createLinearGradient(x, y, x, y + h);
  grad.addColorStop(0, 'rgba(15, 23, 42, 0.28)');
  grad.addColorStop(0.5, 'rgba(15, 23, 42, 0.08)');
  grad.addColorStop(1, 'rgba(15, 23, 42, 0.35)');
  ctx.fillStyle = grad;
  ctx.fillRect(x, y, w, h);
  ctx.restore();

  return (lat, lng) => {
    const p = projectMercator(lat, lng, z);
    return { x: x + (p.x - topLeftPx.x), y: y + (p.y - topLeftPx.y) };
  };
}

/**
 * TripGather 고유 테마: 보딩패스 프레임 + 오렌지 비행 경로 + 여권 스탬프 렌더링
 */
async function renderBoardingFrame(ctx, L, state, spots, meta, styleKey, transportKey) {
  const { GW, GH, HEADER_H, MAP_Y, MAP_W, MAP_H, CAPTION_Y, CAPTION_H, THUMB, CARD_W, CARD_H } = L;

  ctx.fillStyle = '#0F172A';
  ctx.fillRect(0, 0, GW, GH);

  // 1) 지도 영역 타일 및 동선 렌더링
  const toScreen = await drawMapTilesToCanvas(
    ctx,
    state.view,
    0,
    MAP_Y,
    MAP_W,
    MAP_H,
    styleKey
  );
  const pts = spots.map((s) => toScreen(s.lat, s.lng));

  ctx.save();
  ctx.beginPath();
  ctx.rect(0, MAP_Y, MAP_W, MAP_H);
  ctx.clip();

  // 전체 예정 경로 (점선 가이드)
  if (pts.length > 1) {
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.32)';
    ctx.lineWidth = 2.5;
    ctx.setLineDash([5, 6]);
    ctx.beginPath();
    pts.forEach((p, i) => (i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y)));
    ctx.stroke();
    ctx.setLineDash([]);
  }

  // 이동 완료 경로 (--primary-orange #FF5C00 발광 + 화이트 대시)
  const drawProgressed = () => {
    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);
    for (let i = 1; i <= state.segIdx; i++) {
      ctx.lineTo(pts[i].x, pts[i].y);
    }
    if (state.segIdx < pts.length - 1 && state.segT > 0) {
      const a = pts[state.segIdx];
      const b = pts[state.segIdx + 1];
      ctx.lineTo(a.x + (b.x - a.x) * state.segT, a.y + (b.y - a.y) * state.segT);
    }
  };

  if (pts.length > 0 && (state.segIdx > 0 || state.segT > 0)) {
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = 'rgba(255, 92, 0, 0.42)';
    ctx.lineWidth = 11;
    drawProgressed();
    ctx.stroke();

    ctx.strokeStyle = '#FF5C00';
    ctx.lineWidth = 4.5;
    drawProgressed();
    ctx.stroke();

    ctx.strokeStyle = '#FFF7ED';
    ctx.lineWidth = 1.8;
    ctx.setLineDash([6, 6]);
    drawProgressed();
    ctx.stroke();
    ctx.setLineDash([]);
  }

  // 경유지 게이트 핀 마커 (Boarding Gate Badge)
  pts.forEach((p, i) => {
    const visited = i <= state.segIdx || (i === state.segIdx + 1 && state.segT > 0.85);
    const r = i === state.activeIdx ? 15 : 12;
    ctx.save();
    ctx.beginPath();
    ctx.arc(p.x, p.y, r, 0, Math.PI * 2);
    ctx.fillStyle = visited ? '#FF5C00' : 'rgba(15, 23, 42, 0.78)';
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = '#FFFFFF';
    ctx.stroke();

    ctx.fillStyle = '#FFFFFF';
    ctx.font = `800 ${r - 2}px "Pretendard Variable", sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(i + 1), p.x, p.y + 0.5);
    ctx.restore();
  });

  // 이동 아이콘 (비행기 / 자동차 / 도보)
  if (pts.length > 0) {
    let tx = pts[state.segIdx].x;
    let ty = pts[state.segIdx].y;
    if (state.segIdx < pts.length - 1 && state.segT > 0) {
      const a = pts[state.segIdx];
      const b = pts[state.segIdx + 1];
      tx = a.x + (b.x - a.x) * state.segT;
      ty = a.y + (b.y - a.y) * state.segT;
    }
    ctx.save();
    ctx.beginPath();
    ctx.arc(tx, ty, 20, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255, 92, 0, 0.28)';
    ctx.fill();
    ctx.font = '24px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(TRANSPORT_ICONS[transportKey] || '✈️', tx, ty);
    ctx.restore();
  }

  // 활성 지점 폴라로이드 티켓 팝업 카드
  if (state.cardAlpha > 0 && spots[state.activeIdx]) {
    const idx = state.activeIdx;
    const spot = spots[idx];
    const p = pts[idx];
    const cw = CARD_W;
    const ch = CARD_H;
    let cx = p.x - cw / 2;
    let cy = p.y - ch - 24;
    if (cy < MAP_Y + 10) cy = p.y + 24;
    cx = Math.max(12, Math.min(MAP_W - cw - 12, cx));
    cy = Math.max(MAP_Y + 8, Math.min(MAP_Y + MAP_H - ch - 8, cy));

    ctx.save();
    ctx.globalAlpha = state.cardAlpha;
    ctx.shadowColor = 'rgba(15, 23, 42, 0.55)';
    ctx.shadowBlur = 18;
    ctx.fillStyle = '#FFFFFF';
    roundRect(ctx, cx, cy, cw, ch, 14);
    ctx.fill();
    ctx.shadowBlur = 0;

    ctx.strokeStyle = '#FF5C00';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    const pad = 8;
    const imgH = ch - 52;
    ctx.save();
    roundRect(ctx, cx + pad, cy + pad, cw - pad * 2, imgH, 9);
    ctx.clip();
    if (spot.img) {
      drawCoverImg(ctx, spot.img, cx + pad, cy + pad, cw - pad * 2, imgH);
    } else {
      ctx.fillStyle = '#FFF7ED';
      ctx.fillRect(cx + pad, cy + pad, cw - pad * 2, imgH);
    }
    ctx.restore();

    // GATE 배지
    ctx.fillStyle = '#FF5C00';
    roundRect(ctx, cx + pad + 6, cy + pad + 6, 58, 20, 6);
    ctx.fill();
    ctx.fillStyle = '#FFFFFF';
    ctx.font = '800 10px "Pretendard Variable", sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(
      spot.dayNumber ? `D${spot.dayNumber} · #${idx + 1}` : `GATE 0${idx + 1}`,
      cx + pad + 35,
      cy + pad + 16
    );

    ctx.fillStyle = '#0F172A';
    ctx.font = '800 13px "Pretendard Variable", sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(trimText(ctx, spot.name, cw - 18), cx + cw / 2, cy + ch - 26);

    ctx.fillStyle = '#64748B';
    ctx.font = '600 10px "Pretendard Variable", sans-serif';
    ctx.fillText(spotWhen(spot) || 'BOARDING CONFIRMED', cx + cw / 2, cy + ch - 11);
    ctx.restore();
  }

  // 피날레 여권 도장 (Passport Stamp Finale Overlay)
  if (state.isFinale) {
    ctx.save();
    ctx.translate(GW - 112, MAP_Y + MAP_H - 76);
    ctx.rotate(-0.22);
    ctx.strokeStyle = 'rgba(255, 92, 0, 0.92)';
    ctx.fillStyle = 'rgba(15, 23, 42, 0.72)';
    ctx.lineWidth = 3.5;
    roundRect(ctx, -78, -32, 156, 64, 10);
    ctx.fill();
    ctx.stroke();

    ctx.setLineDash([4, 3]);
    ctx.lineWidth = 1.5;
    roundRect(ctx, -72, -26, 144, 52, 7);
    ctx.stroke();
    ctx.setLineDash([]);

    ctx.fillStyle = '#FF5C00';
    ctx.font = '900 15px "Pretendard Variable", sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('★ STAMPED ★', 0, -8);

    ctx.fillStyle = '#FFFFFF';
    ctx.font = '700 10px "Pretendard Variable", sans-serif';
    ctx.fillText(`TRIPGATHER · ${todayKey()}`, 0, 12);
    ctx.restore();
  }

  ctx.restore();

  // 2) 상단 보딩패스 헤더 바
  ctx.fillStyle = '#0F172A';
  ctx.fillRect(0, 0, GW, HEADER_H);
  ctx.fillStyle = '#FF5C00';
  ctx.fillRect(0, 0, 6, HEADER_H);

  ctx.fillStyle = '#FF5C00';
  ctx.font = '800 10px "Pretendard Variable", sans-serif';
  ctx.textAlign = 'left';
  ctx.textBaseline = 'middle';
  ctx.fillText('✈️ BOARDING PASS · TRIPGATHER', 16, Math.round(HEADER_H * 0.34));

  ctx.fillStyle = '#FFFFFF';
  ctx.font = '800 14px "Pretendard Variable", sans-serif';
  ctx.fillText(
    trimText(ctx, meta?.title || '내 여행 동선 타임랩스', GW * 0.56),
    16,
    Math.round(HEADER_H * 0.72)
  );

  ctx.fillStyle = '#FFF7ED';
  ctx.font = '700 11px "Pretendard Variable", sans-serif';
  ctx.textAlign = 'right';
  const totalKmText = `${(meta?.totalKm || 0).toFixed(1)} km · ${spots.length} GATES`;
  ctx.fillText(totalKmText, GW - 16, Math.round(HEADER_H * 0.54));

  // 3) 티켓 절취선(Perforation Line & Semicircular Notches) + 하단 보딩 게이트 캡션
  ctx.fillStyle = '#0F172A';
  ctx.fillRect(0, CAPTION_Y, GW, CAPTION_H);

  // 절취선 점선
  ctx.strokeStyle = 'rgba(255, 92, 0, 0.55)';
  ctx.lineWidth = 1.5;
  ctx.setLineDash([6, 5]);
  ctx.beginPath();
  ctx.moveTo(14, CAPTION_Y);
  ctx.lineTo(GW - 14, CAPTION_Y);
  ctx.stroke();
  ctx.setLineDash([]);

  // 양 끝 반원 펀칭 홈
  ctx.fillStyle = '#FF5C00';
  ctx.beginPath();
  ctx.arc(0, CAPTION_Y, 8, -Math.PI / 2, Math.PI / 2);
  ctx.fill();
  ctx.beginPath();
  ctx.arc(GW, CAPTION_Y, 8, Math.PI / 2, (Math.PI * 3) / 2);
  ctx.fill();

  const cur = spots[state.activeIdx] || spots[0];
  if (!cur) return;

  const pad = Math.round((CAPTION_H - THUMB) / 2);
  const tx = 16;
  const ty = CAPTION_Y + pad;
  ctx.save();
  roundRect(ctx, tx, ty, THUMB, THUMB, 12);
  ctx.clip();
  if (cur.img) {
    drawCoverImg(ctx, cur.img, tx, ty, THUMB, THUMB);
  }
  ctx.restore();
  ctx.strokeStyle = '#FF5C00';
  ctx.lineWidth = 2;
  roundRect(ctx, tx, ty, THUMB, THUMB, 12);
  ctx.stroke();

  const textX = tx + THUMB + 14;
  const textMaxW = GW - textX - 16;
  ctx.textAlign = 'left';

  ctx.fillStyle = '#FF5C00';
  ctx.font = '800 11px "Pretendard Variable", sans-serif';
  const gateBadge = cur.dayNumber
    ? `DAY ${cur.dayNumber} · GATE ${state.activeIdx + 1}/${spots.length}`
    : `BOARDING GATE ${state.activeIdx + 1} OF ${spots.length}`;
  const whenStr = spotWhen(cur);
  ctx.fillText(whenStr ? `${gateBadge}  ·  ${whenStr}` : gateBadge, textX, ty + 14);

  ctx.fillStyle = '#FFFFFF';
  ctx.font = '800 17px "Pretendard Variable", sans-serif';
  ctx.fillText(trimText(ctx, cur.name, textMaxW), textX, ty + 38);

  // 하단 스텝 도트 바
  const barY = ty + THUMB - 10;
  const dotGap = Math.min(20, Math.floor((textMaxW - 20) / Math.max(1, spots.length)));
  spots.forEach((s, i) => {
    const dx = textX + i * dotGap + 5;
    ctx.beginPath();
    ctx.arc(dx, barY, i === state.activeIdx ? 5 : 3.2, 0, Math.PI * 2);
    ctx.fillStyle = i <= state.activeIdx ? '#FF5C00' : 'rgba(255,255,255,0.24)';
    ctx.fill();
  });
}

let gifencModPromise = null;
function loadGifenc() {
  if (!gifencModPromise) {
    gifencModPromise = import('https://cdn.jsdelivr.net/npm/gifenc@1.0.3/dist/gifenc.esm.js');
  }
  return gifencModPromise;
}

let mp4MuxerPromise = null;
function loadMp4Muxer() {
  if (!mp4MuxerPromise) {
    mp4MuxerPromise = import('https://cdn.jsdelivr.net/npm/mp4-muxer@5.2.1/build/mp4-muxer.mjs');
  }
  return mp4MuxerPromise;
}

async function encodeFramesToMp4(outCanvas, plan, renderFrameAt, onProgress) {
  const W = outCanvas.width;
  const H = outCanvas.height;

  if (typeof window !== 'undefined' && typeof window.VideoEncoder === 'function') {
    try {
      const { Muxer, ArrayBufferTarget } = await loadMp4Muxer();
      const codecs = ['avc1.42001f', 'avc1.42E01E', 'avc1.4d001f'];
      let chosenCodec = null;
      for (const c of codecs) {
        const sup = await window.VideoEncoder.isConfigSupported({
          codec: c,
          width: W,
          height: H,
          bitrate: 2_500_000,
          framerate: 30
        });
        if (sup.supported) {
          chosenCodec = c;
          break;
        }
      }
      if (chosenCodec) {
        const target = new ArrayBufferTarget();
        const muxer = new Muxer({
          target,
          video: { codec: 'avc', width: W, height: H },
          fastStart: 'in-memory'
        });
        const encoder = new window.VideoEncoder({
          output: (chunk, meta) => muxer.addVideoChunk(chunk, meta),
          error: () => {}
        });
        encoder.configure({
          codec: chosenCodec,
          width: W,
          height: H,
          bitrate: 2_500_000,
          framerate: 30
        });
        let timestampUs = 0;
        for (let i = 0; i < plan.length; i++) {
          await renderFrameAt(i);
          const durationUs = Math.max(33, plan[i].delay) * 1000;
          const vf = new window.VideoFrame(outCanvas, {
            timestamp: timestampUs,
            duration: durationUs
          });
          encoder.encode(vf, { keyFrame: i === 0 || i % 30 === 0 });
          vf.close();
          timestampUs += durationUs;
          if (onProgress) onProgress((i + 1) / plan.length, i + 1, plan.length);
          await new Promise((r) => setTimeout(r, 0));
        }
        await encoder.flush();
        muxer.finalize();
        return { blob: new Blob([target.buffer], { type: 'video/mp4' }), ext: 'mp4' };
      }
    } catch {
      // fallback to MediaRecorder
    }
  }

  const mimeTypes = [
    'video/mp4;codecs=avc1.42E01E',
    'video/mp4',
    'video/webm;codecs=vp9',
    'video/webm'
  ];
  const mimeType =
    (typeof MediaRecorder !== 'undefined' &&
      mimeTypes.find((t) => MediaRecorder.isTypeSupported && MediaRecorder.isTypeSupported(t))) ||
    'video/webm';

  const stream = outCanvas.captureStream(30);
  const recorder = new MediaRecorder(stream, { mimeType, videoBitsPerSecond: 2_500_000 });
  const chunks = [];
  recorder.ondataavailable = (e) => {
    if (e.data && e.data.size > 0) chunks.push(e.data);
  };
  const stopped = new Promise((resolve) => {
    recorder.onstop = resolve;
  });
  recorder.start();
  for (let i = 0; i < plan.length; i++) {
    const t0 = performance.now();
    await renderFrameAt(i);
    if (onProgress) onProgress((i + 1) / plan.length, i + 1, plan.length);
    const wait = Math.max(16, plan[i].delay - (performance.now() - t0));
    await new Promise((r) => setTimeout(r, wait));
  }
  recorder.stop();
  await stopped;
  const ext = mimeType.includes('mp4') ? 'mp4' : 'webm';
  return { blob: new Blob(chunks, { type: mimeType }), ext };
}

/**
 * 전체 보딩패스 동선 타임랩스(GIF 또는 MP4)를 렌더링합니다.
 */
export async function renderTravelTimelapse(spots, meta = {}, options = {}, onProgress = null) {
  const format = options.format || 'square';
  const styleKey = options.styleKey || 'satellite';
  const transportKey = options.transportKey || 'plane';
  const p = planTravelGif(spots, format, options);
  const { L, Q, video, plan, outW, outH } = p;

  let totalKm = 0;
  for (let i = 1; i < spots.length; i++) {
    totalKm += calcDistanceKm(spots[i - 1].lat, spots[i - 1].lng, spots[i].lat, spots[i].lng);
  }
  const fullMeta = { ...meta, totalKm };

  const workCanvas = document.createElement('canvas');
  workCanvas.width = L.GW;
  workCanvas.height = L.GH;
  const workCtx = workCanvas.getContext('2d', { willReadFrequently: true });

  const outCanvas = Q.scale === 1 && !video ? workCanvas : document.createElement('canvas');
  if (outCanvas !== workCanvas) {
    outCanvas.width = outW;
    outCanvas.height = outH;
  }
  const outCtx = outCanvas.getContext('2d', { willReadFrequently: true });

  const renderFrameAt = async (i) => {
    await renderBoardingFrame(workCtx, L, plan[i], spots, fullMeta, styleKey, transportKey);
    if (outCanvas !== workCanvas) {
      outCtx.drawImage(workCanvas, 0, 0, outW, outH);
    }
  };

  if (video) {
    const res = await encodeFramesToMp4(outCanvas, plan, renderFrameAt, onProgress);
    return {
      blob: res.blob,
      ext: res.ext,
      width: outW,
      height: outH,
      frames: plan.length
    };
  }

  const { GIFEncoder, quantize, applyPalette } = await loadGifenc();
  const gif = GIFEncoder();

  for (let i = 0; i < plan.length; i++) {
    await renderFrameAt(i);
    const { data } = outCtx.getImageData(0, 0, outW, outH);
    const palette = quantize(data, Q.colors);
    const index = applyPalette(data, palette);
    gif.writeFrame(index, outW, outH, { palette, delay: plan[i].delay });
    if (onProgress) onProgress((i + 1) / plan.length, i + 1, plan.length);
    await new Promise((r) => setTimeout(r, 0));
  }

  gif.finish();
  const bytes = gif.bytes();
  return {
    blob: new Blob([bytes], { type: 'image/gif' }),
    ext: 'gif',
    width: outW,
    height: outH,
    frames: plan.length
  };
}

/**
 * 한 장의 보딩패스 동선 요약 포스터(PNG)를 생성합니다.
 */
export async function exportBoardingMapPng(spots, meta = {}, styleKey = 'satellite', transportKey = 'plane') {
  const L = GIF_LAYOUTS.square;
  let totalKm = 0;
  for (let i = 1; i < spots.length; i++) {
    totalKm += calcDistanceKm(spots[i - 1].lat, spots[i - 1].lng, spots[i].lat, spots[i].lng);
  }
  const overview = computeFitView(spots, L.MAP_W, L.MAP_H, 68, 15);
  const canvas = document.createElement('canvas');
  canvas.width = L.GW;
  canvas.height = L.GH;
  const ctx = canvas.getContext('2d');
  const lastIdx = Math.max(0, spots.length - 1);

  await renderBoardingFrame(
    ctx,
    L,
    {
      view: overview,
      segIdx: lastIdx,
      segT: 0,
      activeIdx: lastIdx,
      cardAlpha: 1,
      isFinale: true
    },
    spots,
    { ...meta, totalKm },
    styleKey,
    transportKey
  );

  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob), 'image/png');
  });
}

/**
 * 생성된 Blob을 로컬 기기에 다운로드하거나 모바일 공유 시트를 호출합니다.
 */
export async function saveToGallery(blob, filename) {
  if (!blob) return;
  try {
    const file = new File([blob], filename, { type: blob.type });
    if (
      typeof navigator !== 'undefined' &&
      navigator.canShare &&
      navigator.canShare({ files: [file] }) &&
      /Android|iPhone|iPad|iPod/i.test(navigator.userAgent || '')
    ) {
      await navigator.share({ files: [file], title: filename });
      return;
    }
  } catch (e) {
    if (e && e.name === 'AbortError') return;
  }
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 5000);
}
