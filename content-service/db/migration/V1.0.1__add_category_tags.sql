-- 在 content_service 数据库执行
use content_service;

-- 为 books 表添加分类和标签字段
ALTER TABLE books
ADD COLUMN category VARCHAR(100) DEFAULT NULL COMMENT '书籍分类' AFTER status,
ADD COLUMN tags VARCHAR(1000) DEFAULT NULL COMMENT '书籍标签(JSON字符串)' AFTER category;
