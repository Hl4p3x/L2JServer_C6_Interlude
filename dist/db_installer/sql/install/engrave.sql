CREATE TABLE IF NOT EXISTS `engraved_items` (
  `object_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `engraver_id` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;


CREATE TABLE IF NOT EXISTS `engraved_log` (
  `object_id` int(11) NOT NULL,
  `actiondate` decimal(12,0) NOT NULL,
  `process` varchar(64) NOT NULL,
  `itemName` varchar(64) NOT NULL,
  `form_char` varchar(64) NOT NULL,
  `to_char` varchar(64) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
