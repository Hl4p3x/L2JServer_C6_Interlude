-- 
-- Table structure for table `teleport`
-- 
DROP TABLE IF EXISTS custom_teleport;
CREATE TABLE custom_teleport (
  Description varchar(75) default NULL,
  id decimal(11,0) NOT NULL default '0',
  loc_x decimal(9,0) default NULL,
  loc_y decimal(9,0) default NULL,
  loc_z decimal(9,0) default NULL,
  price decimal(6,0) default NULL,
  fornoble int(1) NOT NULL default '0',
  PRIMARY KEY  (id)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;