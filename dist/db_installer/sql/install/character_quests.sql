-- ----------------------------
-- Table structure for character_quests
-- ----------------------------
CREATE TABLE IF NOT EXISTS `character_quests` (
  `char_id` INT NOT NULL DEFAULT 0,
  `name` VARCHAR(40) NOT NULL DEFAULT '',
  `var`  VARCHAR(20) NOT NULL DEFAULT '',
  `value` VARCHAR(255) ,
  PRIMARY KEY  (`char_id`,`name`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;