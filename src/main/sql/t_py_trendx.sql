/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : web3

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 14/01/2024 15:20:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_py_trendx
-- ----------------------------
DROP TABLE IF EXISTS `t_py_trendx`;
CREATE TABLE `t_py_trendx`  (
  `Fsn` int(0) NOT NULL AUTO_INCREMENT,
  `Fuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `Ffull_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `Fnews_id` int(0) NULL DEFAULT NULL,
  `Fis_like` tinyint(0) NULL DEFAULT NULL,
  PRIMARY KEY (`Fsn`) USING BTREE,
  INDEX `idex_uid_nid`(`Fuid`, `Fnews_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15127 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
