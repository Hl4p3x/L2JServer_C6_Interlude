-- ---------------------------
-- Table structure for grandboss_data
-- ---------------------------
CREATE TABLE IF NOT EXISTS grandboss_data (
  `boss_id` INTEGER NOT NULL DEFAULT 0,
  `loc_x` INTEGER NOT NULL DEFAULT 0,
  `loc_y` INTEGER NOT NULL DEFAULT 0,
  `loc_z` INTEGER NOT NULL DEFAULT 0,
  `heading` INTEGER NOT NULL DEFAULT 0,
  `respawn_time` BIGINT NOT NULL DEFAULT 0,
  `currentHP` DECIMAL(8,0) DEFAULT NULL,
  `currentMP` DECIMAL(8,0) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(`boss_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

INSERT IGNORE INTO `grandboss_data` VALUES 
(29019, 185708,114298,-8221,32768, 0, 13090000, 22197, 0),	-- Antharas
(29020, 115213,16623,10080,41740, 0, 790857, 3347, 0),		-- Baium
(29006, 17726, 108915, -6480, 0, 0, 162561, 575, 0), 		-- Core
(29001, -21610, 181594, -5734, 0, 0, 2179536, 667, 0),  	-- Queen Ant
(29045, 0,0,0,0, 0, 790857, 1859, 0),						-- Frintezza
(29046, 0,0,0,0, 0, 63, 44, 0),								-- Scarlet Van Halisha
(29047, 0,0,0,0, 0, 350000, 85, 0),							-- Scarlet Van Halisha
(29054, 0,0,0,0, 0, 300000, 2000, 0),						-- Benom
(29014, 55024, 17368, -5412, 10126, 0, 325124, 1660, 0),	-- Orfen
(29062, 0,0,0,0, 0, NULL, NULL, 0),
(29022, 55312, 219168, -3223, 0, 0, 858518, 1975, 0),	-- Zaken
(29028, 213004, -114890, -1595, 0, 0, 16660000, 22197, 0);	-- Valakas
-- (25333, 000000,000000,000000,0,	0,193763,3718, 0), -- Anakazel (28) -- (Spawn by Dimensional Instance)
-- (25334, 000000,000000,000000,0,	0,306698,3718, 0), -- Anakazel (38) -- (Spawn by Dimensional Instance)
-- (25335, 000000,000000,000000,0,	0,494363,3718, 0), -- Anakazel (48) -- (Spawn by Dimensional Instance)
-- (25336, 000000,000000,000000,0,	0,705074,3718, 0), -- Anakazel (58) -- (Spawn by Dimensional Instance)
-- (25337, 000000,000000,000000,0,	0,865808,3718, 0), -- Anakazel (68) -- (Spawn by Dimensional Instance)
-- (25338, 000000,000000,000000,0,	0,977523,3718, 0); -- Anakazel (78) -- (Spawn by Dimensional Instance)

INSERT IGNORE INTO `grandboss_data` VALUES 
(29066, 185708, 114298, -8221,32768, 0, 11186000, 1998000, 0), -- Antharas Weak (85)
(29067, 185708, 114298, -8221,32768, 0, 14518000, 1998000, 0), -- Antharas Normal (85)
(29068, 185708, 114298, -8221,32768, 0, 17850000, 1998000, 0); -- Antharas Strong (85)