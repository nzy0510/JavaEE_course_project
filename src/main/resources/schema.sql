CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS knowledge_atom;

CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255),
    file_extension VARCHAR(32) NOT NULL,
    file_size BIGINT NOT NULL,
    knowledge_category VARCHAR(64) NOT NULL DEFAULT '通用知识',
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING',
    chunk_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    title_path VARCHAR(500),
    content LONGTEXT NOT NULL,
    content_hash CHAR(64) NOT NULL,
    token_estimate INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chunk_document (document_id),
    INDEX idx_chunk_status (status),
    CONSTRAINT fk_chunk_document
        FOREIGN KEY (document_id) REFERENCES knowledge_document(id)
        ON DELETE CASCADE
);
