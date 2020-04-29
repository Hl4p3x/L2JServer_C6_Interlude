-- ---------------------------
-- Table structure for grandboss_list
-- ---------------------------

CREATE TABLE IF NOT EXISTS grandboss_list 
(
`player_id` decimal(11,0) NOT NULL,
`zone` decimal(11,0) NOT NULL,
PRIMARY KEY  (`player_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;