-- ----------------------------
-- Table structure for fort_doorupgrade
-- ----------------------------
CREATE TABLE `fort_doorupgrade` (
  `doorId` int(11) NOT NULL default '0',
  `fortId` int(11) NOT NULL,
  `hp` int(11) NOT NULL default '0',
  `pDef` int(11) NOT NULL default '0',
  `mDef` int(11) NOT NULL default '0',
  PRIMARY KEY  (`doorId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;