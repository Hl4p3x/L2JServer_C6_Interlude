-- -----------------------
-- Table structure for tvt
-- -----------------------
DROP TABLE IF EXISTS `tvt`;
CREATE TABLE `tvt` (
  `eventName` varchar(255) NOT NULL DEFAULT '',
  `eventDesc` varchar(255) NOT NULL DEFAULT '',
  `joiningLocation` varchar(255) NOT NULL DEFAULT '',
  `minlvl` int(4) NOT NULL DEFAULT '0',
  `maxlvl` int(4) NOT NULL DEFAULT '0',
  `npcId` int(8) NOT NULL DEFAULT '0',
  `npcX` int(11) NOT NULL DEFAULT '0',
  `npcY` int(11) NOT NULL DEFAULT '0',
  `npcZ` int(11) NOT NULL DEFAULT '0',
  `npcHeading` int(11) NOT NULL DEFAULT '0',
  `rewardId` int(11) NOT NULL DEFAULT '0',
  `rewardAmount` int(11) NOT NULL DEFAULT '0',
  `teamsCount` int(4) NOT NULL DEFAULT '0',
  `joinTime` int(11) NOT NULL DEFAULT '0',
  `eventTime` int(11) NOT NULL DEFAULT '0',
  `minPlayers` int(4) NOT NULL DEFAULT '0',
  `maxPlayers` int(4) NOT NULL DEFAULT '0',
  `delayForNextEvent` BIGINT NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ---------------------
-- Records for table tvt
-- ---------------------
INSERT INTO `tvt` values 
('TVT', 'A PvP Event', 'Giran', 1, 81, 70010, 82688, 148677, -3469, 0, 8752, 1, 2, 5, 5, 2, 50, 300000);