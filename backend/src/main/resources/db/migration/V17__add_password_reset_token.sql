-- V17: 비밀번호 재설정
--
-- 배경:
-- 자체 가입(local) 사용자가 비밀번호를 잊으면 계정을 되찾을 길이 없었다.
-- 로그인 화면에 "비밀번호 찾기" 가 없고, sendPasswordResetEmail 은 빈 메서드였다.
--
-- 재설정 토큰에는 만료를 둔다. 기존 verificationToken 에는 만료가 없어
-- 한 번 발급된 링크가 영원히 유효한데, 비밀번호를 바꿀 수 있는 토큰에
-- 같은 성질을 주면 메일 한 통이 유출될 때 계정이 영구히 열린다.

ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token_expires_at TIMESTAMP;

-- 토큰으로 사용자를 찾는 조회에 쓴다.
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users (reset_token);
