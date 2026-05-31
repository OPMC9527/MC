-- 使用数据库
USE edulink;

-- ==================== 基础表 ====================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `role` ENUM('ADMIN', 'TEACHER', 'STUDENT', 'PARENT') NOT NULL COMMENT '角色',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(200) COMMENT '头像',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用,1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 班级表
CREATE TABLE IF NOT EXISTS `class` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `class_name` VARCHAR(50) NOT NULL COMMENT '班级名称',
  `grade` VARCHAR(20) NOT NULL COMMENT '年级',
  `head_teacher_id` BIGINT COMMENT '班主任ID',
  `description` TEXT COMMENT '班级描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`head_teacher_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 3. 学生表
CREATE TABLE IF NOT EXISTS `student` (
  `id` BIGINT PRIMARY KEY,
  `student_number` VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `gender` ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
  `birth_date` DATE COMMENT '出生日期',
  `parent_id` BIGINT COMMENT '家长ID',
  FOREIGN KEY (`id`) REFERENCES `sys_user`(`id`),
  FOREIGN KEY (`class_id`) REFERENCES `class`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 4. 成绩表
CREATE TABLE IF NOT EXISTS `score` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `score` DECIMAL(5,2) COMMENT '分数',
  `exam_type` VARCHAR(50) COMMENT '考试类型',
  `exam_date` DATE COMMENT '考试日期',
  `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `student`(`id`),
  INDEX idx_student (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 5. 考勤表
CREATE TABLE IF NOT EXISTS `attendance` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `attendance_date` DATE NOT NULL COMMENT '考勤日期',
  `status` ENUM('PRESENT', 'ABSENT', 'LATE', 'LEAVE') NOT NULL COMMENT '状态',
  `remark` VARCHAR(200) COMMENT '备注',
  `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `student`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- 6. 通知公告表
CREATE TABLE IF NOT EXISTS `notice` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `target_roles` JSON COMMENT '目标角色',
  `is_urgent` TINYINT DEFAULT 0 COMMENT '是否紧急',
  `view_count` INT DEFAULT 0 COMMENT '查看次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`publisher_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 7. 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_type` ENUM('SINGLE', 'GROUP') NOT NULL COMMENT '会话类型',
  `session_name` VARCHAR(100) COMMENT '会话名称',
  `group_id` BIGINT COMMENT '关联班级ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 8. 会话成员表
CREATE TABLE IF NOT EXISTS `chat_member` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`),
  UNIQUE KEY uk_session_user (`session_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

-- 9. 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `sender_name` VARCHAR(50) NOT NULL COMMENT '发送者姓名',
  `sender_role` VARCHAR(20) NOT NULL COMMENT '发送者角色',
  `sender_avatar` VARCHAR(200) COMMENT '发送者头像',
  `message_type` ENUM('TEXT', 'IMAGE', 'FILE') DEFAULT 'TEXT' COMMENT '消息类型',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`),
  INDEX idx_session_time (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 插入示例数据（密码: 123456 的MD5值）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `phone`, `email`, `status`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', '13800138000', 'admin@edulink.com', 1),
(2, 'teacher001', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 'TEACHER', '13800138001', 'zhang@edulink.com', 1),
(3, 'student001', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'STUDENT', '13800138002', 'zhangsan@edulink.com', 1),
(4, 'parent001', 'e10adc3949ba59abbe56e057f20f883e', '张三家长', 'PARENT', '13800138003', 'parent@edulink.com', 1);

-- 插入班级
INSERT INTO `class` (`id`, `class_name`, `grade`, `head_teacher_id`) VALUES
(1, '计算机科学与技术1班', '2024级', 2);

-- 插入学生关联
INSERT INTO `student` (`id`, `student_number`, `class_id`, `gender`, `parent_id`) VALUES
(3, '20240001', 1, 'MALE', 4);

SELECT '数据库初始化完成！' as message;