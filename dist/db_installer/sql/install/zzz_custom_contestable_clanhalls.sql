-- Spawn Changes
UPDATE `spawnlist` SET `locx` = '85584', `locy` = '-18224', `locz` = '-1819' WHERE `id` = '58435';
--
-- Wild Beast Reserve
--

-- Clan Hall Changes
UPDATE `clanhall` SET `location` = 'BeastFarm' WHERE `id` = '63';

-- Insert new NPCs
INSERT IGNORE INTO `npc` (`id`,`idTemplate`,`name`,`serverSideName`,`title`,`serverSideTitle`,`class`,`collision_radius`,`collision_height`,`level`,`sex`,`type`,`attackrange`,`hp`,`mp`,`hpreg`,`mpreg`,`str`,`con`,`dex`,`int`,`wit`,`men`,`exp`,`sp`,`patk`,`pdef`,`matk`,`mdef`,`atkspd`,`aggro`,`matkspd`,`rhand`,`lhand`,`armor`,`walkspd`,`runspd`,`faction_id`,`faction_range`,`isUndead`,`absorb_level`,`absorb_type`) VALUES
	(75003,31366,'Cole',1,'Beast Manager',1,'NPC.a_traderD_Mhuman',8.00,25.30,70,'male','ClanHallManager',40,3862,3059,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,55,132,NULL,0,1,0,'LAST_HIT'),
	(75004,31537,'Tom',1,'Beast Keeper',1,'NPC.a_common_peopleC_Mhuman',10.00,24.00,70,'male','Doorman',40,3862,1493,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,55,132,NULL,0,1,0,'LAST_HIT'),
	(75005,31537,'Richard',1,'Beast Keeper',1,'NPC.a_common_peopleC_Mhuman',10.00,24.00,70,'male','Doorman',40,3862,1493,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,55,132,NULL,0,1,0,'LAST_HIT'),
	(75006,31537,'Harry',1,'Beast Keeper',1,'NPC.a_common_peopleC_Mhuman',10.00,24.00,70,'male','Doorman',40,3862,1493,11.85,2.78,40,43,30,21,20,10,0,0,1314,470,780,382,278,0,333,0,0,0,55,132,NULL,0,1,0,'LAST_HIT');

-- Spawns
INSERT IGNORE INTO `spawnlist` VALUES 
	(NULL,'Beast Farm - Clan Hall NPC',1,75003,60916,-94279,-1350,0,0,27932,60,0,'0'),
	(NULL,'Beast Farm - Clan Hall NPC',1,75004,55175,-93025,-1361,0,0,35782,60,0,'0'),
	(NULL,'Beast Farm - Clan Hall NPC',1,75005,55577,-93235,-1359,0,0,15186,60,0,'0'),
	(NULL,'Beast Farm - Clan Hall NPC',1,75006,59950,-94058,-1354,0,0,27391,60,0,'0');

-- Insert new shop ids
INSERT IGNORE INTO merchant_shopids VALUES (175003,'75003');
INSERT IGNORE INTO merchant_shopids VALUES (275003,'75003');
INSERT IGNORE INTO merchant_shopids VALUES (375003,'75003');

-- Insert new buy lists
DELETE FROM `merchant_buylists` WHERE `shop_id` like '%75003';
INSERT INTO `merchant_buylists` (`item_id`,`price`,`shop_id`,`order`,`count`,time) VALUES 
	(6902,12900,175003,1,-1,0),
	(1829,500,175003,2,5,1),
	(5169,300,175003,3,5,1),
	(6643,25,175003,4,-1,0),
	(6644,25,175003,5,-1,0),
	(6902,12900,275003,1,-1,0),
	(1829,500,275003,2,5,1),
	(5858,100000,275003,3,1,2),
	(5169,300,275003,4,10,1),
	(6643,25,275003,5,-1,0),
	(6644,25,275003,6,-1,0),
	(7133,35000,275003,7,1,1),
	(7135,35000,275003,8,1,1),
	(6902,12900,375003,1,-1,0),
	(1829,500,375003,2,5,1),
	(5858,100000,375003,3,3,1),
	(5169,300,375003,4,10,1),
	(6643,25,375003,5,-1,0),
	(6644,25,375003,6,-1,0),
	(7133,35000,375003,7,1,1),
	(7135,35000,375003,8,1,1),
	(7583,500000,375003,9,1,2),
	(7584,500000,375003,10,1,2),
	(7585,500000,375003,11,1,2),
	(6928,180,375003,12,1,1),
	(6935,600,375003,13,1,1),
	(6936,1200,375003,14,1,1),
	(6938,1200,375003,15,1,1),
	(6921,250,375003,16,1,1),
	(6923,100000,375003,17,1,1),
	(7691,100000,375003,18,1,1),
	(6940,4000,375003,19,1,1),
	(6941,4000,375003,20,1,1),
	(6942,4000,375003,21,1,1),
	(6943,4000,375003,22,1,1),
	(6944,4000,375003,23,1,1),
	(6945,4000,375003,24,1,1),
	(6946,4000,375003,25,1,1),
	(6947,4000,375003,26,1,1),
	(6948,4000,375003,27,1,1),
	(6949,4000,375003,28,1,1),
	(6950,4000,375003,29,1,1),
	(6951,4000,375003,30,1,1),
	(6952,4000,375003,31,1,1),
	(6953,4000,375003,32,1,1),
	(7002,29000,375003,33,1,1),
	(7003,29000,375003,34,1,1),
	(7004,29000,375003,35,1,1),
	(7005,29000,375003,36,1,1),
	(7006,29000,375003,37,1,1),
	(7007,29000,375003,38,1,1),
	(7008,29000,375003,39,1,1),
	(7009,29000,375003,40,1,1),
	(7010,29000,375003,41,1,1),
	(7011,29000,375003,42,1,1),
	(7012,29000,375003,43,1,1),
	(7013,29000,375003,44,1,1);

-- Insert new Teleports
INSERT IGNORE INTO teleport VALUES 
	('Clan Hall -> Rune Castle Town',75004,43835,-47749,-796,500,0),
	('Clan Hall -> Beast Farm',75005,42598,-88832,-3124,0,0);

--
-- Fortress of the Dead
--

-- Insert new NPCs
INSERT IGNORE INTO `npc` (`id`,`idTemplate`,`name`,`serverSideName`,`title`,`serverSideTitle`,`class`,`collision_radius`,`collision_height`,`level`,`sex`,`type`,`attackrange`,`hp`,`mp`,`hpreg`,`mpreg`,`str`,`con`,`dex`,`int`,`wit`,`men`,`exp`,`sp`,`patk`,`pdef`,`matk`,`mdef`,`atkspd`,`aggro`,`matkspd`,`rhand`,`lhand`,`armor`,`walkspd`,`runspd`,`faction_id`,`faction_range`,`isUndead`,`absorb_level`,`absorb_type`) VALUES
	(75007,21591,'Carrie',1,'Undead Matron',1,'Monster2.vampire_wizard',5.50,28.00,73,'female','ClanHallManager',40,4086,3059,13.43,3.09,40,43,30,21,20,10,0,0,1770,415,885,407,278,0,333,99,0,0,50,198,NULL,300,0,0,'LAST_HIT'),
	(75008,21587,'Freddy',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT'),
	(75009,21587,'Hannibal',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT'),
	(75010,21587,'Jason',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT'),
	(75011,21587,'Michael',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT'),
	(75012,21587,'Matt',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT'),
	(75013,21587,'Charles',1,'Undead Butler',1,'Monster2.vampire_soldier_20_bi',10.00,29.00,72,'male','Doorman',40,4013,1565,13.43,3.09,40,43,30,21,20,10,0,0,1710,406,849,399,278,0,333,234,0,0,66,209,NULL,300,0,0,'LAST_HIT');

-- Spawns
INSERT IGNORE INTO `spawnlist` VALUES 
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75007,58770,-27504,579,0,0,33264,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75008,57826,-29529,569,0,0,49467,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75009,57842,-29422,569,0,0,15850,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75010,57888,-26447,593,0,0,49152,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75011,57888,-26333,593,0,0,16838,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75012,56932,-27335,578,0,0,33140,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,75013,57119,-27169,578,0,0,0,60,0,'0'),
	(NULL,'Fortress of the Dead - Clan Hall NPC',1,35638,57022,-28268,607,0,0,48457,60,0,'0');

-- Insert new shop ids
INSERT IGNORE INTO merchant_shopids VALUES (175007,'75007');
INSERT IGNORE INTO merchant_shopids VALUES (275007,'75007');
INSERT IGNORE INTO merchant_shopids VALUES (375007,'75007');

-- Insert new buy lists
DELETE FROM `merchant_buylists` WHERE `shop_id` like '%75007';
INSERT INTO `merchant_buylists` (`item_id`,`price`,`shop_id`,`order`,`count`,time) VALUES 
	(6902,12900,175007,1,-1,0),
	(1829,500,175007,2,5,1),
	(5169,300,175007,3,5,1),
	(6902,12900,275007,1,-1,0),
	(1829,500,275007,2,5,1),
	(5858,100000,275007,3,1,2),
	(5169,300,275007,4,10,1),
	(7134,35000,275007,5,1,1),
	(7133,35000,275007,6,1,1),
	(6902,12900,375007,1,-1,0),
	(1829,500,375007,2,5,1),
	(5858,100000,375007,3,3,1),
	(5169,300,375007,4,10,1),
	(7134,35000,375007,5,1,1),
	(7133,35000,375007,6,1,1),
	(7583,500000,375007,7,1,2),
	(7584,500000,375007,8,1,2),
	(7585,500000,375007,9,1,2),
	(6928,180,375007,10,1,1),
	(6935,600,375007,11,1,1),
	(6936,1200,375007,12,1,1),
	(6938,1200,375007,13,1,1),
	(6921,250,375007,14,1,1),
	(6923,100000,375007,15,1,1),
	(7691,100000,375007,16,1,1),
	(6940,4000,375007,17,1,1),
	(6941,4000,375007,18,1,1),
	(6942,4000,375007,19,1,1),
	(6943,4000,375007,20,1,1),
	(6944,4000,375007,21,1,1),
	(6945,4000,375007,22,1,1),
	(6946,4000,375007,23,1,1),
	(6947,4000,375007,24,1,1),
	(6948,4000,375007,25,1,1),
	(6949,4000,375007,26,1,1),
	(6950,4000,375007,27,1,1),
	(6951,4000,375007,28,1,1),
	(6952,4000,375007,29,1,1),
	(6953,4000,375007,30,1,1),
	(7002,29000,375007,31,1,1),
	(7003,29000,375007,32,1,1),
	(7004,29000,375007,33,1,1),
	(7005,29000,375007,34,1,1),
	(7006,29000,375007,35,1,1),
	(7007,29000,375007,36,1,1),
	(7008,29000,375007,37,1,1),
	(7009,29000,375007,38,1,1),
	(7010,29000,375007,39,1,1),
	(7011,29000,375007,40,1,1),
	(7012,29000,375007,41,1,1),
	(7013,29000,375007,42,1,1);

-- Insert new Teleports
INSERT IGNORE INTO teleport VALUES 
	('Clan Hall -> Forest of the Dead - East Entrance',75006,61868,-48890,-3128,0,0),
	('Clan Hall -> Forest of the Dead - West Entrance',75007,45555,-56081,-3667,0,0);
