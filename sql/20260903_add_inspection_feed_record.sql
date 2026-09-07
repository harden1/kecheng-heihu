-- ----------------------------
-- 镜检投料上传记录表
-- ----------------------------
CREATE TABLE `inspection_feed_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投料记录ID',
  `summary_id` bigint NOT NULL COMMENT '镜检主记录ID',
  `qr_code` varchar(256) NOT NULL COMMENT '扫码二维码',
  `work_order_code` varchar(64) DEFAULT NULL COMMENT '工单号',
  `task_id` bigint NOT NULL COMMENT '黑湖生产任务ID',
  `material_id` bigint DEFAULT NULL COMMENT '库存原料物料ID',
  `inventory_element_id` bigint DEFAULT NULL COMMENT '库存明细ID',
  `feed_amount` decimal(20,6) DEFAULT NULL COMMENT '投料数量',
  `unit_id` bigint DEFAULT NULL COMMENT '投料单位ID',
  `request_json` longtext COMMENT '最近一次批量投料请求JSON',
  `response_json` longtext COMMENT '最近一次黑湖响应JSON',
  `upload_status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/SUCCESS/FAILED/UNKNOWN',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `fail_type` varchar(32) DEFAULT NULL COMMENT '失败类型：FEED_RELATION=获取投料关系无效，INVENTORY=库存明细查询无效，UPLOAD=上传失败',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '手动重传次数',
  `feed_time` datetime DEFAULT NULL COMMENT '上传成功时间',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_summary_task_qr` (`summary_id`,`task_id`,`qr_code`),
  KEY `idx_feed_status_time` (`upload_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='镜检投料上传记录';

-- ----------------------------
-- 投料记录菜单和权限
-- 父级为镜检管理目录（menu_id=2019）
-- ----------------------------

-- 投料记录菜单（menu_id=2080，父级=2019 镜检管理）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2080, '投料记录', 2019, 4, 'feedRecord', 'inspection/feedRecord/index', NULL, '', 1, 0, 'C', '0', '0', 'inspection:feedRecord:list', '#', 'admin', sysdate(), '', NULL, '投料记录菜单');

-- 投料记录查询按钮（menu_id=2081，父级=2080）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2081, '投料记录查询', 2080, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inspection:feedRecord:query', '#', 'admin', sysdate(), '', NULL, '');

-- 投料记录重传按钮（menu_id=2082，父级=2080）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2082, '投料记录重传', 2080, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'inspection:feedRecord:retry', '#', 'admin', sysdate(), '', NULL, '');
