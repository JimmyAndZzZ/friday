/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.5.215
 Source Server Type    : MySQL
 Source Server Version : 50724
 Source Host           : 192.168.5.215:3306
 Source Schema         : ss_gateway

 Target Server Type    : MySQL
 Target Server Version : 50724
 File Encoding         : 65001

 Date: 23/04/2024 14:50:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for transaction_test
-- ----------------------------
DROP TABLE IF EXISTS `transaction_test`;
CREATE TABLE `transaction_test`
(
    `id`   int(11)                                                 NOT NULL AUTO_INCREMENT,
    `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 171
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transaction_point
-- ----------------------------
DROP TABLE IF EXISTS `transaction_point`;
CREATE TABLE `transaction_point`
(
    `id`                varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `create_date`       timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status`            char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `timeout`           int(11)                                                NULL DEFAULT NULL,
    `timeout_timestamp` bigint(20)                                             NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hawk_eye_topology_relation
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_topology_relation`;
CREATE TABLE `hawk_eye_topology_relation`
(
    `id`            varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NOT NULL,
    `up`            varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `down`          varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `invoke_remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `invoke_type`   varchar(50) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_id` (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hawk_eye_topology_module
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_topology_module`;
CREATE TABLE `hawk_eye_topology_module`
(
    `id`      varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NOT NULL,
    `machine` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `module`  varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `type`    varchar(50) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `status`  varchar(50) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_id` (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hawk_eye_qps
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_qps`;
CREATE TABLE `hawk_eye_qps`
(
    `id`                 varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NOT NULL,
    `module_name`        varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `create_date`        datetime                                         NULL DEFAULT CURRENT_TIMESTAMP,
    `request_point`      varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `request_attachment` varchar(512) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `protocol`           varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hawk_eye_log_topology_relation
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_log_topology_relation`;
CREATE TABLE `hawk_eye_log_topology_relation`
(
    `id`            varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NOT NULL,
    `up`            varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `down`          varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `trace_id`      varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `invoke_remark` text CHARACTER SET utf8 COLLATE utf8_bin         NULL,
    `invoke_type`   varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `create_date`   datetime                                         NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_id` (`id`) USING BTREE,
    INDEX `trace_id_index` (`trace_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hawk_eye_log_point
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_log_point`;
CREATE TABLE `hawk_eye_log_point`
(
    `id`               varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NOT NULL,
    `trace_id`         varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `class_name`       varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `method_name`      varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `application_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `create_date`      datetime                                         NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hawk_eye_log
-- ----------------------------
DROP TABLE IF EXISTS `hawk_eye_log`;
CREATE TABLE `hawk_eye_log`
(
    `id`          varchar(32) CHARACTER SET utf8 COLLATE utf8_bin   NOT NULL,
    `log_message` text CHARACTER SET utf8 COLLATE utf8_bin          NULL,
    `class_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `method_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `trace_id`    varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `span_id`     varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `level`       varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `param`       varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `result`      varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
    `module_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  NULL DEFAULT NULL,
    `create_date` datetime                                          NULL DEFAULT NULL,
    `modify_date` datetime                                          NULL DEFAULT NULL,
    `is_log`      char(1) CHARACTER SET utf8 COLLATE utf8_bin       NULL DEFAULT NULL,
    `app_id`      bigint(20)                                        NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gateway_service_warn
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_warn`;
CREATE TABLE `gateway_service_warn`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id`  bigint(20)                                              NOT NULL,
    `provider_id` bigint(20)                                              NULL DEFAULT NULL,
    `create_date` datetime                                                NULL DEFAULT NULL,
    `type`        char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `message`     varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `query_index` (`service_id`, `provider_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 249
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_provider
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_provider`;
CREATE TABLE `gateway_service_provider`
(
    `id`          bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `service_id`  bigint(20)                                             NOT NULL,
    `port`        int(11)                                                NULL DEFAULT NULL,
    `weight`      int(11)                                                NULL DEFAULT NULL,
    `status`      char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `ip_address`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `is_manual`   char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `create_date` datetime                                               NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 457
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_method_param
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_method_param`;
CREATE TABLE `gateway_service_method_param`
(
    `id`         bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id` bigint(20)                                              NOT NULL,
    `method_id`  bigint(20)                                              NOT NULL,
    `name`       varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `desc`       varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `param_type` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1846
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_method_open
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_method_open`;
CREATE TABLE `gateway_service_method_open`
(
    `id`               bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id`       bigint(20)                                              NOT NULL,
    `method_id`        bigint(20)                                              NOT NULL,
    `name`             varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `description`      varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `example`          varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `type`             char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `code`             varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `cost_strategy_id` bigint(20)                                              NULL DEFAULT NULL,
    `status`           char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `is_free`          char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 161
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_method_invoke_metrics
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_method_invoke_metrics`;
CREATE TABLE `gateway_service_method_invoke_metrics`
(
    `id`           bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `method_id`    bigint(20)                                             NOT NULL,
    `meter_date`   varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `invoke_count` int(11)                                                NULL DEFAULT NULL,
    `meter_unit`   char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `service_id`   bigint(20)                                             NULL DEFAULT NULL,
    `create_date`  datetime                                               NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 841
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_method
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_method`;
CREATE TABLE `gateway_service_method`
(
    `id`             bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id`     bigint(20)                                              NOT NULL,
    `name`           varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `interface_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `return_type`    varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `param_type`     varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `retry`          int(11)                                                 NULL DEFAULT NULL,
    `timeout`        int(11)                                                 NULL DEFAULT NULL,
    `method_id`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `is_manual`      char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `method_code`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 248
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service_consumer
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service_consumer`;
CREATE TABLE `gateway_service_consumer`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id`  bigint(20)                                              NOT NULL,
    `ip_address`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `create_date` datetime                                                NULL DEFAULT NULL,
    `app_id`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `client_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `provider_id` bigint(20)                                              NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 321
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_service
-- ----------------------------
DROP TABLE IF EXISTS `gateway_service`;
CREATE TABLE `gateway_service`
(
    `id`               bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `application_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `type`             varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `version`          varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `description`      varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `group_name`       varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `create_date`      datetime                                                NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 147
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_route_rule
-- ----------------------------
DROP TABLE IF EXISTS `gateway_route_rule`;
CREATE TABLE `gateway_route_rule`
(
    `id`                 bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `service_id`         bigint(20)                                              NULL DEFAULT NULL,
    `method_id`          bigint(20)                                              NULL DEFAULT NULL,
    `version`            varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `enabled`            char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `is_force`           char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT NULL,
    `priority`           int(11)                                                 NULL DEFAULT NULL,
    `consumer_condition` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `provider_condition` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `name`               varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 114
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_push_channel_sub
-- ----------------------------
DROP TABLE IF EXISTS `gateway_push_channel_sub`;
CREATE TABLE `gateway_push_channel_sub`
(
    `id`           bigint(12)                                             NOT NULL AUTO_INCREMENT,
    `account_id`   bigint(20)                                             NULL DEFAULT NULL,
    `channel_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 193
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_invoke_trace
-- ----------------------------
DROP TABLE IF EXISTS `gateway_invoke_trace`;
CREATE TABLE `gateway_invoke_trace`
(
    `id`                bigint(20)                                             NOT NULL,
    `provider_id`       bigint(20)                                             NULL DEFAULT NULL,
    `service_id`        bigint(20)                                             NULL DEFAULT NULL,
    `method_id`         bigint(20)                                             NULL DEFAULT NULL,
    `invoke_param`      longtext CHARACTER SET utf8 COLLATE utf8_general_ci    NULL,
    `invoke_result`     longtext CHARACTER SET utf8 COLLATE utf8_general_ci    NULL,
    `create_time`       datetime                                               NULL DEFAULT NULL,
    `error_message`     text CHARACTER SET utf8 COLLATE utf8_general_ci        NULL,
    `cost_time`         bigint(20)                                             NULL DEFAULT NULL,
    `is_success`        char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `client_ip_address` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `client_name`       varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `cost`              decimal(10, 4)                                         NULL DEFAULT NULL,
    `app_id`            varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `check_status`      char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_cost_strategy_details
-- ----------------------------
DROP TABLE IF EXISTS `gateway_cost_strategy_details`;
CREATE TABLE `gateway_cost_strategy_details`
(
    `id`              bigint(20)    NOT NULL AUTO_INCREMENT,
    `strategy_id`     bigint(20)    NULL DEFAULT NULL,
    `create_date`     timestamp     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `price`           decimal(6, 4) NULL DEFAULT NULL,
    `threshold_value` int(11)       NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 160
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_cost_strategy
-- ----------------------------
DROP TABLE IF EXISTS `gateway_cost_strategy`;
CREATE TABLE `gateway_cost_strategy`
(
    `id`          bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `name`        varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `create_date` timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `type`        char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `charge_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 158
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_account_order
-- ----------------------------
DROP TABLE IF EXISTS `gateway_account_order`;
CREATE TABLE `gateway_account_order`
(
    `id`                 bigint(12)                                             NOT NULL AUTO_INCREMENT,
    `account_id`         bigint(20)                                             NULL DEFAULT NULL,
    `order_trace_no`     bigint(20)                                             NULL DEFAULT NULL,
    `create_date`        timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status`             char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `amount`             decimal(10, 4)                                         NULL DEFAULT NULL,
    `source`             char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    `trd_order_trace_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `purpose`            char(1) CHARACTER SET utf8 COLLATE utf8_general_ci     NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_account_invoke_count
-- ----------------------------
DROP TABLE IF EXISTS `gateway_account_invoke_count`;
CREATE TABLE `gateway_account_invoke_count`
(
    `id`           bigint(12) NOT NULL AUTO_INCREMENT,
    `account_id`   bigint(20) NULL DEFAULT NULL,
    `invoke_date`  int(11)    NULL DEFAULT NULL,
    `invoke_count` int(11)    NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 191
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_account
-- ----------------------------
DROP TABLE IF EXISTS `gateway_account`;
CREATE TABLE `gateway_account`
(
    `id`          bigint(12)                                              NOT NULL AUTO_INCREMENT,
    `uid`         varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `seckey`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL,
    `create_date` timestamp                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `lvl`         int(11)                                                 NULL DEFAULT 0,
    `title`       varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `status`      char(1) CHARACTER SET utf8 COLLATE utf8_general_ci      NULL DEFAULT '0',
    `balance`     decimal(10, 4)                                          NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 169
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
