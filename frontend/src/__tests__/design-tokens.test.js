import { describe, it, expect } from 'vitest';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

/**
 * 디자인 토큰 무결성 검사.
 *
 * 정의되지 않은 CSS 변수를 쓰면 var() 가 무효값이 되어 색이 상속값으로 떨어진다.
 * 에러가 나지 않고 조용히 깨지기 때문에 눈으로는 찾기 어렵다.
 *
 * 실제로 --text-sub(26곳), --primary(7곳), --border(5곳), --shadow-sm, --shadow-lg 가
 * 정의 없이 쓰이고 있었고, background:var(--primary) 인 버튼이 투명해져
 * 흰 글자만 남는 문제가 있었다.
 */

const SRC = new URL('..', import.meta.url).pathname;

function walk(dir, exts) {
  const out = [];
  for (const name of readdirSync(dir)) {
    if (name === 'node_modules' || name === '__tests__') continue;
    const full = join(dir, name);
    if (statSync(full).isDirectory()) out.push(...walk(full, exts));
    else if (exts.some((e) => name.endsWith(e))) out.push(full);
  }
  return out;
}

const cssFiles = walk(SRC, ['.css']);
const sourceFiles = walk(SRC, ['.css', '.js', '.jsx']);

/** 모든 CSS 파일에서 정의된 커스텀 프로퍼티 이름 */
function definedTokens() {
  const defined = new Set();
  for (const file of cssFiles) {
    const css = readFileSync(file, 'utf8');
    for (const m of css.matchAll(/(--[A-Za-z0-9-]+)\s*:/g)) defined.add(m[1]);
  }
  return defined;
}

/** var(--x) 로 참조된 토큰과 그 위치 */
function usedTokens() {
  const used = new Map();
  for (const file of sourceFiles) {
    const text = readFileSync(file, 'utf8');
    text.split('\n').forEach((line, i) => {
      for (const m of line.matchAll(/var\((--[A-Za-z0-9-]+)\)/g)) {
        if (!used.has(m[1])) used.set(m[1], []);
        used.get(m[1]).push(`${relative(SRC, file)}:${i + 1}`);
      }
    });
  }
  return used;
}

describe('디자인 토큰', () => {
  it('참조되는 CSS 변수는 모두 어딘가에 정의되어 있다', () => {
    const defined = definedTokens();
    const used = usedTokens();

    const missing = [...used.entries()]
      .filter(([token]) => !defined.has(token))
      .map(([token, where]) => `${token} (${where.length}곳, 예: ${where[0]})`);

    expect(missing, `정의되지 않은 CSS 변수:\n  ${missing.join('\n  ')}`).toEqual([]);
  });

  it('토큰을 정의한 CSS 파일이 존재한다', () => {
    // 위 검사가 "정의가 하나도 없어서" 통과하는 상황을 막는 안전장치
    expect(definedTokens().size).toBeGreaterThan(10);
  });
});
