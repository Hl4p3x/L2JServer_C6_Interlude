CREATE TABLE IF NOT EXISTS `account_gsdata` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `var`  VARCHAR(255) NOT NULL DEFAULT '',
  `value` text NOT NULL,
  PRIMARY KEY (`account_name`,`var`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;