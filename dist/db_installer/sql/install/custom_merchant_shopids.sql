--
-- Table structure for table `merchant_shopids`
--
DROP TABLE IF EXISTS custom_merchant_shopids;
CREATE TABLE custom_merchant_shopids (
  shop_id decimal(9,0) NOT NULL default '0',
  npc_id varchar(9) default NULL,
  PRIMARY KEY  (shop_id)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;