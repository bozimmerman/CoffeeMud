# MYSQL Database Creation Script for CoffeeMud

/**
  TABLE CMROCH - Room inhabitant MOBs
  CMROID : Room ID
  CMCHNM : Unique room mob id, for DB updates
  CMCHID : MOB Class ID
  CMCHTX : Misc text/generic xml for the mob
  CMCHLV : Level of the mob
  CMCHAB : Ability code
  CMCHRE : Rejuv/ination time
  CMCHRI : Unique room mob id/item id being ridden
*/
CREATE TABLE CMROCH (
	CMROID varchar (50) NULL ,
	CMCHNM varchar (100) NULL ,
	CMCHID varchar (50) NULL ,
	CMCHTX longtext NULL ,
	CMCHLV int NULL ,
	CMCHAB int NULL ,
	CMCHRE int NULL ,
	CMCHRI varchar (100) NULL
);

ALTER TABLE CMROCH ADD ( UNIQUE KEY (CMROID,CMCHNM) );

/**
  TABLE CMROEX - Room exits
  CMROID : Room ID
  CMDIRE : Direction code (0, 1, etc..)
  CMEXID : Exit Class ID
  CMEXTX : Misc text/generic xml for the exit
  CMNRID : Linked Room ID of the exit
*/
CREATE TABLE CMROEX (
	CMROID varchar (50) NULL ,
	CMDIRE int NULL ,
	CMEXID varchar (50) NULL ,
	CMEXTX longtext NULL ,
	CMNRID varchar (50) NULL 
);

ALTER TABLE CMROEX ADD( UNIQUE KEY (CMROID,CMDIRE) );

/**
  TABLE CMROIT - Room items
  CMROID : Room ID
  CMITNM : Unique item ID, for updating the item in the DB
  CMITID : The Item Class ID for the item
  CMITLO : Unique item ID of the container for the item
  CMITTX : Misc text/generic xml for the item
  CMITRE : Rejuv/ination rate for the item
  CMITUR : Uses remaining (for ammo, wands, etc)
  CMITLV : Item level
  CMITAB : Ability code of the item
  CMHEIT : Height of the item (for wearables)
*/
CREATE TABLE CMROIT (
	CMROID varchar (50) NULL ,
	CMITNM varchar (100) NULL ,
	CMITID varchar (50) NULL ,
	CMITLO varchar (100) NULL ,
	CMITTX longtext NULL ,
	CMITRE int NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMROIT ADD ( UNIQUE KEY (CMROID,CMITNM) );

/**
  TABLE CMROOM - Rooms!
  CMROID : Room ID
  CMLOID : Room Locale Class ID
  CMAREA : Area Name/ID
  CMDESC1: Short display text
  CMDESC2: Long description
  CMROTX : Misc text/XML for the room (effects, etc)
*/
CREATE TABLE CMROOM (
	CMROID varchar (50) NULL ,
	CMLOID varchar (50) NULL ,
	CMAREA varchar (50) NULL ,
	CMDESC1 varchar (255) NULL ,
	CMDESC2 longtext NULL ,
	CMROTX longtext NULL 
);

ALTER TABLE CMROOM ADD ( UNIQUE KEY (CMROID) );

/**
  TABLE CMAREA - Areas
  CMAREA : Area name/ID
  CMTYPE : Area Class ID
  CMCLIM : Climate mask
  CMSUBS : List of player char subops
  CMDESC : Long description of the area
  CMROTX longtext NULL ,
  CMTECH : Tech level (fantasy, heroic, etc)
*/
CREATE TABLE CMAREA (
	CMAREA varchar (50) ,
	CMTYPE varchar (50) ,
	CMCLIM int NULL ,
	CMSUBS varchar (100) NULL ,
	CMDESC longtext NULL ,
	CMROTX longtext NULL ,
	CMTECH int NULL
);

ALTER TABLE CMAREA ADD ( UNIQUE KEY (CMAREA) );


