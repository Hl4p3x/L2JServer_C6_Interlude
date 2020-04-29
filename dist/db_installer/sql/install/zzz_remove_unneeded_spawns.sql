-- This is completely optional (though highly recommended).

-- Remove Mobs from Fortress of Resistance.
DELETE FROM `spawnlist` WHERE `npc_templateid` IN ('35368', '35369', '35370', '35371', '35372', '35373', '35374');

-- Remove Mobs from Devastated Castle.
DELETE FROM `spawnlist` WHERE `npc_templateid` IN ('35410', '35411', '35412', '35413', '35414', '35415', '35416', '35417', '35418');

-- Remove Mobs from Bandit Stronghold.
DELETE FROM `spawnlist` WHERE `npc_templateid` IN ('35428', '35429', '35430', '35431', '35432');
