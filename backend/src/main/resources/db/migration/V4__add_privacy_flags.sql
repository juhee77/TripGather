-- V4: Add privacy flags to Gathering and GatheringPost

-- 1. Add privacy flags to gathering table
ALTER TABLE gathering ADD COLUMN is_gallery_public BOOLEAN DEFAULT FALSE;
ALTER TABLE gathering ADD COLUMN is_chat_public BOOLEAN DEFAULT FALSE;
ALTER TABLE gathering ADD COLUMN is_comment_public BOOLEAN DEFAULT TRUE;

-- 2. Add is_public flag to gathering_post table
ALTER TABLE gathering_post ADD COLUMN is_public BOOLEAN DEFAULT FALSE;
