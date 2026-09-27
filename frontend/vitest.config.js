import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // 기본은 Node 환경. DOM 이 필요한 테스트는 파일 상단에
    // `// @vitest-environment jsdom` 으로 개별 지정한다.
    environment: 'node',
    include: ['src/**/*.{test,spec}.{js,jsx}'],
    // Storybook 이 만든 샘플은 테스트 대상이 아니다.
    exclude: ['node_modules/**', 'dist/**', 'src/stories/**'],
  },
});
