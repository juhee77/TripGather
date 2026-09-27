import { describe, it, expect } from 'vitest';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, basename } from 'node:path';

/**
 * 컴포넌트 CSS 격리 검사.
 *
 * CSS import 는 컴포넌트 스코프가 아니라 전역이다.
 * PassportCard.jsx 가 './PassportCard.css' 를 import 하면 그 안의 모든 규칙이 앱 전체에 적용된다.
 *
 * 실제로 PassportCard.css 의 `.info-value { color:#fff }` 가 index.css 의 같은 클래스를 덮어써
 * 라운지 카드와 티켓 카드의 호스트명·날짜·장소가 흰 배경에 흰 글자가 됐다(대비 1.00).
 * 정작 PassportCard 는 그 클래스를 쓰지도 않았다.
 *
 * 같은 클래스 이름이 여러 CSS 파일에서 최상위(비스코프) 규칙으로 정의되면
 * 로드 순서에 따라 조용히 서로를 덮어쓰므로 막는다.
 */

const SRC = new URL('..', import.meta.url).pathname;
/** 앱 전역 스타일을 담당하는 파일 — 여기서의 정의는 의도된 것이다. */
const GLOBAL_SHEETS = new Set(['index.css', 'App.css']);

function walkCss(dir) {
  const out = [];
  for (const name of readdirSync(dir)) {
    if (name === 'node_modules' || name === 'stories') continue;
    const full = join(dir, name);
    if (statSync(full).isDirectory()) out.push(...walkCss(full));
    else if (name.endsWith('.css')) out.push(full);
  }
  return out;
}

/**
 * 최상위(다른 선택자 아래로 스코프되지 않은) 클래스 선택자만 뽑는다.
 * `.a .b` 나 `.a > .b` 의 .b 는 스코프된 것으로 보고 제외한다.
 */
function topLevelClasses(css) {
  const withoutComments = css.replace(/\/\*[\s\S]*?\*\//g, '');
  const found = new Set();

  for (const m of withoutComments.matchAll(/(^|\})\s*([^{}@]+)\{/g)) {
    for (const selector of m[2].split(',')) {
      const s = selector.trim();
      if (!s || s.startsWith('@') || s.startsWith('from') || s.startsWith('to')) continue;
      // 조합자가 있으면 스코프된 규칙 — 전역 충돌 위험이 낮다
      if (/[\s>+~]/.test(s.replace(/^\s+|\s+$/g, ''))) continue;
      const cls = s.match(/^\.([A-Za-z_][\w-]*)/);
      if (cls) found.add(cls[1]);
    }
  }
  return found;
}

describe('컴포넌트 CSS 격리', () => {
  const files = walkCss(SRC);

  it('같은 클래스가 여러 파일에서 최상위로 정의되지 않는다', () => {
    const owners = new Map();
    for (const file of files) {
      for (const cls of topLevelClasses(readFileSync(file, 'utf8'))) {
        if (!owners.has(cls)) owners.set(cls, []);
        owners.get(cls).push(basename(file));
      }
    }

    const collisions = [...owners.entries()]
      .filter(([, where]) => where.length > 1)
      .map(([cls, where]) => `.${cls} — ${where.join(', ')}`);

    expect(
      collisions,
      `여러 파일에서 최상위로 정의된 클래스(로드 순서에 따라 서로 덮어씀):\n  ${collisions.join('\n  ')}`
    ).toEqual([]);
  });

  it('컴포넌트 CSS 는 전역 시트의 클래스를 재정의하지 않는다', () => {
    const globalClasses = new Set();
    for (const file of files) {
      if (GLOBAL_SHEETS.has(basename(file))) {
        for (const cls of topLevelClasses(readFileSync(file, 'utf8'))) globalClasses.add(cls);
      }
    }

    const overrides = [];
    for (const file of files) {
      const name = basename(file);
      if (GLOBAL_SHEETS.has(name)) continue;
      for (const cls of topLevelClasses(readFileSync(file, 'utf8'))) {
        if (globalClasses.has(cls)) {
          overrides.push(`.${cls} — ${relative(SRC, file)} 가 전역 정의를 덮어씀`);
        }
      }
    }

    expect(
      overrides,
      `컴포넌트 CSS 가 전역 클래스를 덮어씁니다. 상위 선택자로 스코프하세요:\n  ${overrides.join('\n  ')}`
    ).toEqual([]);
  });
});
