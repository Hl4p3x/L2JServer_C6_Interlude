CREATE TABLE IF NOT EXISTS `olympiad_nobles` (
  `charId` decimal(11,0) NOT NULL default '0',
  `class_id` decimal(3,0) NOT NULL default '0',
  `char_name` varchar(45) NOT NULL default '',
  `olympiad_points` decimal(10,0) NOT NULL default '0',
  `competitions_done` decimal(3,0) NOT NULL default '0',
  `competitions_won` decimal(3,0) NOT NULL default '0',
  `competitions_lost` decimal(3,0) NOT NULL default '0',
  `competitions_drawn` decimal(3,0) NOT NULL default '0',
  PRIMARY KEY  (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `olympiad_nobles_eom` (
  `charId` decimal(11,0) NOT NULL default '0',
  `class_id` decimal(3,0) NOT NULL default '0',
  `char_name` varchar(45) NOT NULL default '',
  `olympiad_points` decimal(10,0) NOT NULL default '0',
  `competitions_done` decimal(3,0) NOT NULL default '0',
  `competitions_won` decimal(3,0) NOT NULL default '0',
  `competitions_lost` decimal(3,0) NOT NULL default '0',
  `competitions_drawn` decimal(3,0) NOT NULL default '0',
  PRIMARY KEY  (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;