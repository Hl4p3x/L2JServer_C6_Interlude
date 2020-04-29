DROP TABLE IF EXISTS `helper_buff_list`;
#----------------------------
# Table structure for helper_buff_list
#----------------------------
CREATE TABLE `helper_buff_list` (
`id` int(11) NOT NULL default '0',
`skill_id` int(10) unsigned NOT NULL default '0',
`name` varchar(25) NOT NULL default '',
`skill_level` int(10) unsigned NOT NULL default '0',
`lower_level` int(10) unsigned NOT NULL default '0',
`upper_level` int(10) unsigned NOT NULL default '0',
`is_magic_class` varchar(5) default NULL,
PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

#----------------------------
# Records for table helper_buff_list
#----------------------------

insert  into helper_buff_list values 
(0, 4322, 'WindWalk', 1, 8, 39, 'false'),
(1, 4323, 'Shield', 1, 11, 39, 'false'),
(2, 4338, 'Life Cubic', 1, 16, 36, 'false'),
(3, 4324, 'Bless the Body', 1, 12, 38, 'false'),
(4, 4325, 'Vampiric Rage', 1, 13, 38, 'false'),
(5, 4326, 'Regeneration', 1, 14, 38, 'false'),
(6, 4327, 'Haste', 1, 15, 37, 'false'),
(7, 4322, 'WindWalk', 1, 8, 39, 'true'),
(8, 4323, 'Shield', 1, 11, 39, 'true'),
(9, 4338, 'Life Cubic', 1, 16, 36, 'true'),
(10, 4328, 'Bless the Soul', 1, 12, 38, 'true'),
(11, 4329, 'Acumen', 1, 13, 38, 'true'),
(12, 4330, 'Concentration', 1, 14, 38, 'true'),
(13, 4331, 'Empower', 1, 15, 37, 'true');