SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `reward_manager`
-- ----------------------------
DROP TABLE IF EXISTS `reward_manager`;
CREATE TABLE `reward_manager` (
  `ip` varchar(35) DEFAULT NULL,
  `hwid` varchar(255) DEFAULT NULL,
  `expire_time` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
