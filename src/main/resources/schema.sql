-- PostgreSQL schema.sql

DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS direct_messages CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS playlist_subscriptions CASCADE;
DROP TABLE IF EXISTS playlist_contents CASCADE;
DROP TABLE IF EXISTS watching_sessions CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS playlists CASCADE;
DROP TABLE IF EXISTS contents CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- (선택) create-drop 환경에서 schema.sql을 쓰는 경우,
-- Hibernate가 drop을 하더라도 충돌 방지를 위해 IF NOT EXISTS를 걸어두는 편이 안전합니다.

CREATE TABLE IF NOT EXISTS users (
                                     id                  UUID PRIMARY KEY,
                                     created_at          TIMESTAMPTZ NOT NULL,
                                     email               VARCHAR,
                                     name                VARCHAR,
                                     password            VARCHAR NOT NULL,
                                     profile_image_url   TEXT,
                                     role                VARCHAR NOT NULL,   -- USER / ADMIN
                                     locked              BOOLEAN NOT NULL DEFAULT false
);

-- email은 주석에 unique라고 되어 있었으므로 반영
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email
    ON users(email)
    WHERE email IS NOT NULL;


CREATE TABLE IF NOT EXISTS contents (
                                        id              UUID PRIMARY KEY,
                                        created_at      TIMESTAMPTZ NOT NULL,
                                        updated_at      TIMESTAMPTZ,
                                        type            VARCHAR NOT NULL,       -- 0 / 1 / 2
                                        title           VARCHAR,
                                        description     TEXT,
                                        thumbnail_url   TEXT,
                                        average_rating  DOUBLE PRECISION DEFAULT 0.0,
                                        review_count    INT DEFAULT 0,
                                        watcher_count   BIGINT DEFAULT 0
);


CREATE TABLE IF NOT EXISTS playlists (
                                         id              UUID PRIMARY KEY,
                                         created_at      TIMESTAMPTZ NOT NULL,
                                         updated_at      TIMESTAMPTZ NOT NULL,
                                         title           VARCHAR NOT NULL,
                                         description     TEXT,
                                         owner_id        UUID NOT NULL,
                                         CONSTRAINT fk_playlists_owner
                                             FOREIGN KEY (owner_id) REFERENCES users(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_playlists_owner_id ON playlists(owner_id);


-- ⚠️ 'with'는 충돌 가능성이 커서 컬럼명 변경 권장
CREATE TABLE IF NOT EXISTS conversations (
                                             id              UUID PRIMARY KEY,
                                             created_at      TIMESTAMPTZ NOT NULL,
                                             updated_at      TIMESTAMPTZ,
                                             with_user_id    UUID NOT NULL,
                                             CONSTRAINT fk_conversations_with_user
                                                 FOREIGN KEY (with_user_id) REFERENCES users(id)
                                                     ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_conversations_with_user_id ON conversations(with_user_id);


CREATE TABLE IF NOT EXISTS direct_messages (
                                               id              UUID PRIMARY KEY,
                                               created_at      TIMESTAMPTZ NOT NULL,
                                               conversation_id UUID NOT NULL,
                                               sender_id       UUID NOT NULL,
                                               receiver_id     UUID NOT NULL,
                                               content         TEXT NOT NULL,
                                               read_at         TIMESTAMPTZ,
                                               CONSTRAINT fk_dm_conversation
                                                   FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                                       ON DELETE CASCADE,
                                               CONSTRAINT fk_dm_sender
                                                   FOREIGN KEY (sender_id) REFERENCES users(id)
                                                       ON DELETE CASCADE,
                                               CONSTRAINT fk_dm_receiver
                                                   FOREIGN KEY (receiver_id) REFERENCES users(id)
                                                       ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_dm_conversation_id ON direct_messages(conversation_id);
CREATE INDEX IF NOT EXISTS ix_dm_receiver_read_at ON direct_messages(receiver_id, read_at);


CREATE TABLE IF NOT EXISTS playlist_contents (
                                                 id              UUID PRIMARY KEY,
                                                 content_id      UUID NOT NULL,
                                                 playlist_id     UUID NOT NULL,
                                                 CONSTRAINT fk_playlist_contents_content
                                                     FOREIGN KEY (content_id) REFERENCES contents(id)
                                                         ON DELETE CASCADE,
                                                 CONSTRAINT fk_playlist_contents_playlist
                                                     FOREIGN KEY (playlist_id) REFERENCES playlists(id)
                                                         ON DELETE CASCADE,
                                                 CONSTRAINT uq_playlist_contents UNIQUE (playlist_id, content_id)
);

CREATE INDEX IF NOT EXISTS ix_playlist_contents_playlist_id ON playlist_contents(playlist_id);
CREATE INDEX IF NOT EXISTS ix_playlist_contents_content_id ON playlist_contents(content_id);


CREATE TABLE IF NOT EXISTS playlist_subscriptions (
                                                      id              UUID PRIMARY KEY,
                                                      user_id         UUID NOT NULL,
                                                      playlist_id     UUID NOT NULL,
                                                      CONSTRAINT fk_playlist_subscriptions_user
                                                          FOREIGN KEY (user_id) REFERENCES users(id)
                                                              ON DELETE CASCADE,
                                                      CONSTRAINT fk_playlist_subscriptions_playlist
                                                          FOREIGN KEY (playlist_id) REFERENCES playlists(id)
                                                              ON DELETE CASCADE,
                                                      CONSTRAINT uq_playlist_subscriptions UNIQUE (user_id, playlist_id)
);

CREATE INDEX IF NOT EXISTS ix_playlist_subscriptions_user_id ON playlist_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS ix_playlist_subscriptions_playlist_id ON playlist_subscriptions(playlist_id);


CREATE TABLE IF NOT EXISTS watching_sessions (
                                                 id              UUID PRIMARY KEY,
                                                 created_at      TIMESTAMPTZ NOT NULL,
                                                 content_id      UUID NOT NULL,
                                                 watcher_id      UUID NOT NULL,
                                                 CONSTRAINT fk_watching_sessions_content
                                                     FOREIGN KEY (content_id) REFERENCES contents(id)
                                                         ON DELETE CASCADE,
                                                 CONSTRAINT fk_watching_sessions_watcher
                                                     FOREIGN KEY (watcher_id) REFERENCES users(id)
                                                         ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_watching_sessions_content_id ON watching_sessions(content_id);
CREATE INDEX IF NOT EXISTS ix_watching_sessions_watcher_id ON watching_sessions(watcher_id);


CREATE TABLE IF NOT EXISTS reviews (
                                       id              UUID PRIMARY KEY,
                                       created_at      TIMESTAMPTZ NOT NULL,
                                       updated_at      TIMESTAMPTZ,
                                       text            TEXT,
                                       rating          DOUBLE PRECISION DEFAULT 0.0, -- 0.0 ~ 5.0
                                       author_id       UUID NOT NULL,
                                       content_id      UUID NOT NULL,
                                       CONSTRAINT fk_reviews_author
                                           FOREIGN KEY (author_id) REFERENCES users(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_reviews_content
                                           FOREIGN KEY (content_id) REFERENCES contents(id)
                                               ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_reviews_content_id ON reviews(content_id);
CREATE INDEX IF NOT EXISTS ix_reviews_author_id ON reviews(author_id);


CREATE TABLE IF NOT EXISTS follows (
                                       id              UUID PRIMARY KEY,
                                       created_at      TIMESTAMPTZ NOT NULL,
                                       updated_at      TIMESTAMPTZ,
                                       follower_id     UUID NOT NULL,
                                       followee_id     UUID NOT NULL,
                                       CONSTRAINT fk_follows_follower
                                           FOREIGN KEY (follower_id) REFERENCES users(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_follows_followee
                                           FOREIGN KEY (followee_id) REFERENCES users(id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT uq_follows UNIQUE (follower_id, followee_id),
                                       CONSTRAINT ck_follows_not_self CHECK (follower_id <> followee_id)
);

CREATE INDEX IF NOT EXISTS ix_follows_follower_id ON follows(follower_id);
CREATE INDEX IF NOT EXISTS ix_follows_followee_id ON follows(followee_id);


CREATE TABLE IF NOT EXISTS notifications (
                                             id              UUID PRIMARY KEY,
                                             created_at      TIMESTAMPTZ NOT NULL,
                                             title           VARCHAR NOT NULL,
                                             content         TEXT NOT NULL,
                                             level           VARCHAR NOT NULL,   -- INFO / WARNING / ERROR
                                             receiver_id     UUID NOT NULL,
                                             CONSTRAINT fk_notifications_receiver
                                                 FOREIGN KEY (receiver_id) REFERENCES users(id)
                                                     ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_notifications_receiver_id ON notifications(receiver_id);


CREATE TABLE IF NOT EXISTS tags (
                                    id              UUID PRIMARY KEY,
                                    name            VARCHAR,
                                    content_id      UUID NOT NULL,
                                    CONSTRAINT fk_tags_content
                                        FOREIGN KEY (content_id) REFERENCES contents(id)
                                            ON DELETE CASCADE,
                                    CONSTRAINT uq_tags UNIQUE (content_id, name)
);

CREATE INDEX IF NOT EXISTS ix_tags_content_id ON tags(content_id);
