-- --------------------------------
-- Table structure for table `ctf`
-- Created by SqueezeD & Serpent for l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `ctf`;
CREATE TABLE `ctf` (
  `eventName` varchar(255) NOT NULL default '',
  `eventDesc` varchar(255) NOT NULL default '',
  `joiningLocation` varchar(255) NOT NULL default '',
  `minlvl` int(4) NOT NULL default '0',
  `maxlvl` int(4) NOT NULL default '0',
  `npcId` int(8) NOT NULL default '0',
  `npcX` int(11) NOT NULL default '0',
  `npcY` int(11) NOT NULL default '0',
  `npcZ` int(11) NOT NULL default '0',
  `npcHeading` int(11) NOT NULL default '0',
  `rewardId` int(11) NOT NULL default '0',
  `rewardAmount` int(11) NOT NULL default '0',
  `teamsCount` int(4) NOT NULL default '0',
  `joinTime` int(11) NOT NULL default '0',
  `eventTime` int(11) NOT NULL default '0',
  `minPlayers` int(4) NOT NULL default '0',
  `maxPlayers` int(4) NOT NULL default '0',
  `delayForNextEvent` BIGINT NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `ctf` (`eventName`, `eventDesc`, `joiningLocation`, `minlvl`, `maxlvl`, `npcId`, `npcX`, `npcY`, `npcZ`, `npcHeading`, `rewardId`, `rewardAmount`, `teamsCount`, `joinTime`, `eventTime`, `minPlayers`, `maxPlayers`, `delayForNextEvent`) VALUES
('Capture the flag', 'CTF', 'Giran', 1, 80, 70011, 82580, 148552, -3468, 16972, 8752, 1, 2, 5, 5, 2, 50, 300000);