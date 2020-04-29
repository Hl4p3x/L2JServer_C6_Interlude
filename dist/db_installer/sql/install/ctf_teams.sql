-- --------------------------------
-- Table structure for ctf_teams
-- Created by SqueezeD & Serpent for l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `ctf_teams`;
CREATE TABLE `ctf_teams` (
  `teamId` int(4) NOT NULL default '0',
  `teamName` varchar(255) NOT NULL default '',
  `teamX` int(11) NOT NULL default '0',
  `teamY` int(11) NOT NULL default '0',
  `teamZ` int(11) NOT NULL default '0',
  `teamColor` int(11) NOT NULL default '0',
  `flagX` int(11) NOT NULL default '0',
  `flagY` int(11) NOT NULL default '0',
  `flagZ` int(11) NOT NULL default '0',
  PRIMARY KEY (`teamId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `ctf_teams` (`teamId`, `teamName`, `teamX`, `teamY`, `teamZ`, `teamColor`, `flagX`, `flagY`, `flagZ`) VALUES
(0, 'Blue', 87357, -145722, -1288, 16711680, 87358, -145979, -1291),
(1, 'Red', 87351, -139984, -1536, 255, 87359, -139584, -1536);