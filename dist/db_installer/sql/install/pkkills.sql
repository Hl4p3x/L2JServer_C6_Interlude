#----------------------------
# Table structure for pkkills
#----------------------------
CREATE TABLE IF NOT EXISTS `pkkills` (
  `killerId` varchar(45) NOT NULL,
  `killedId` varchar(45) NOT NULL,
  `kills` decimal(11,0) NOT NULL,
  PRIMARY KEY  (`killerId` ,`killedId` )
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;