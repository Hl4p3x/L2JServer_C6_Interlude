-- --------------------------------
-- Table structure for table `dm`
-- Created by SqueezeD from l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `dm`;
CREATE TABLE `dm` (
  `eventName` varchar(255) NOT NULL default '',
  `eventDesc` varchar(255) NOT NULL default '',
  `joiningLocation` varchar(255) NOT NULL default '',
  `minlvl` int(4) NOT NULL default '0',
  `maxlvl` int(4) NOT NULL default '0',
  `npcId` int(8) NOT NULL default '0',
  `npcX` int(11) NOT NULL default '0',
  `npcY` int(11) NOT NULL default '0',
  `npcZ` int(11) NOT NULL default '0',
  		`npcHeading` int(11) NOT NULL DEFAULT '0',
  `rewardId` int(11) NOT NULL default '0',
  `rewardAmount` int(11) NOT NULL default '0',
  		`joinTime` int(11) NOT NULL DEFAULT '0',
  		`eventTime` int(11) NOT NULL DEFAULT '0',
  		`minPlayers` int(11) NOT NULL DEFAULT '0',
  		`maxPlayers` int(11) NOT NULL DEFAULT '0',
  `color` int(11) NOT NULL default '0',
  `playerX` int(11) NOT NULL default '0',
  `playerY` int(11) NOT NULL default '0',
  `playerZ` int(11) NOT NULL default '0',
  		`delayForNextEvent` BIGINT NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `dm` values 
('DM', 'A PvP Event', 'Giran', 1, 81, 70014, 82580, 148552, -3468, 1, 8752, 1, 2, 5, 2, 50, 2552550, 116615, 76200, -2729, 300000);