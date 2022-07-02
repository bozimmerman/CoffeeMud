# MYSQL Database Creation Script for CoffeeMud

/**
  TABLE CMVFS - CoffeeMud Virtual File System table
  CMFNAM : Full path and filename
  CMDTYP : Mask bits, for hidden, directory, etc.
  CMMODD : Modified date/timestamp in millis
  CMWHOM : Who created/modified the file
  CMDATA : The file data, text encoded
*/
CREATE TABLE CMVFS (
	CMFNAM varchar (255),
	CMDTYP int ,
	CMMODD bigint,
	CMWHOM varchar (50) NULL,
	CMDATA longtext NULL
);

ALTER TABLE CMVFS ADD ( UNIQUE KEY (CMFNAM) );

/**
  TABLE CMCHAB - Player Character Abilities
  CMUSERID : Character name
  CMABID   : Ability ID
  CMABPF   : Character Ability proficiency (0-100)
  CMABTX   : Misc Text/Parameters for the Ability
*/
CREATE TABLE CMCHAB (
	CMUSERID varchar (50) NULL ,
	CMABID varchar (50) NULL ,
	CMABPF int NULL ,
	CMABTX longtext NULL
);

ALTER TABLE CMCHAB ADD ( UNIQUE KEY (CMUSERID,CMABID) );

/**
  TABLE CMSTAT - Game Event History Statistics table

  CMSTRT : Start epoch time in millis  
  CMENDT : End epoch time in millis
  CMDATA : XML formatted data for this period
*/
CREATE TABLE CMSTAT (
	CMSTRT bigint,
	CMENDT bigint,
	CMDATA longtext NULL
);

ALTER TABLE CMSTAT ADD ( UNIQUE KEY (CMSTRT) );

/**
  TABLE CMPOLL -- Polling table for votes
  CMNAME: Friendly name of the poll
  CMBYNM: Name of the character that created the poll
  CMSUBJ: The subject of the poll, shown on results
  CMDESC: Description of the poll, shown on voting
  CMOPTN: XML of the available options
  CMFLAG: Flags bitmap, active, etc..
  CMQUAL: Qualifying Zappermask to vote in the poll
  CMRESL: XML of every vote cast, all in one place
  CMEXPI: Expiration date/timestamp in millis
*/
CREATE TABLE CMPOLL (
	CMNAME varchar (100) ,
	CMBYNM varchar (100) NULL ,
	CMSUBJ varchar (255) NULL ,
	CMDESC longtext NULL ,
	CMOPTN longtext NULL ,
	CMFLAG int NULL ,
	CMQUAL varchar (255) NULL ,
	CMRESL longtext NULL,
	CMEXPI bigint NULL
);

ALTER TABLE CMPOLL ADD ( UNIQUE KEY (CMNAME) );

/**
  TABLE CMCHAR -- Player Character record 
  CMCHID  : Character MOB class ID (usually StdMOB)
  CMUSERID: Character name
  CMPASS  : Password, possibly a password hash
  CMCLAS  : Character class(es)
  CMSTRE  : Strength attribute
  CMRACE  : Character race
  CMDEXT  : Dexterity attribute
  CMCONS  : Constitution attribute
  CMGEND  : Gender (M, F, N)
  CMWISD  : Wisdom attribute
  CMINTE  : Intelligence attribute
  CMCHAR  : Charisma attribute
  CMHITP  : Max hit points
  CMLEVL  : Class level(s) semicolon delimited
  CMMANA  : Max mana
  CMMOVE  : Max moves
  CMDESC  : Readable description
  CMALIG  : Alignment (number)
  CMEXPE  : Total experience points
  CMEXLV  : Experience needed for next level
  CMWORS  : Deity/Worshipped ID
  CMPRAC  : Practice points
  CMTRAI  : Training points
  CMAGEH  : Minutes played
  CMGOLD  : (Deprecated) Amount of gold
  CMWIMP  : Wimpy points 
  CMQUES  : Quest points
  CMROID  : Start/Beacon Room ID 
  CMDATE  : Last connect Date/timestamp
  CMCHAN  : The No-Channel bitmap
  CMATTA  : Base attack attribute
  CMAMOR  : Base armor attribute
  CMDAMG  : Base damage attribute
  CMBTMP  : Miscelleneous mob attribute bitmap
  CMLEIG  : Player mate/leige character ID/name
  CMHEIT  : Character height
  CMWEIT  : Character weight
  CMPRPT  : Prompt string
  CMCOLR  : Personalized color codes, special encoding
  CMLSIP  : Last player connect ip address
  CMEMAL  : Email address
  CMPFIL  : XML Encoded player stat data (lots of stuff)
  CMSAVE  : Saving throws, semicolon delimited
  CMMXML  : XML encoded faction values for the character 
*/
CREATE TABLE CMCHAR (
	CMCHID varchar (50),
	CMUSERID varchar (50) ,
	CMPASS varchar (50) NULL ,
	CMCLAS varchar (250) NULL ,
	CMSTRE int NULL ,
	CMRACE varchar (50) NULL ,
	CMDEXT int NULL ,
	CMCONS int NULL ,
	CMGEND varchar (50) NULL ,
	CMWISD int NULL ,
	CMINTE int NULL ,
	CMCHAR int NULL ,
	CMHITP int NULL ,
	CMLEVL varchar (50) NULL ,
	CMMANA int NULL ,
	CMMOVE int NULL ,
	CMDESC longtext NULL ,
	CMALIG int NULL ,
	CMEXPE int NULL ,
	CMEXLV int NULL ,
	CMWORS varchar (50) NULL ,
	CMPRAC int NULL ,
	CMTRAI int NULL ,
	CMAGEH int NULL ,
	CMGOLD int NULL ,
	CMWIMP int NULL ,
	CMQUES int NULL ,
	CMROID varchar (100) NULL ,
	CMDATE varchar (50) NULL ,
	CMCHAN int NULL ,
	CMATTA int NULL ,
	CMAMOR int NULL ,
	CMDAMG int NULL ,
	CMBTMP int NULL ,
	CMLEIG varchar (50) NULL ,
	CMHEIT int NULL ,
	CMWEIT int NULL ,
	CMPRPT varchar (250) NULL,
	CMCOLR varchar (255) NULL,
	CMLSIP varchar (100) NULL,
	CMEMAL varchar (255) NULL,
	CMPFIL longtext NULL,
	CMSAVE varchar (150) NULL,
	CMMXML longtext NULL
);

ALTER TABLE CMCHAR ADD ( UNIQUE KEY (CMUSERID) );

/**
  TABLE CMCHFO - Character followers
  CMUSERID: Character user name that is being followed
  CMFONM  : Follower ordinal number (0, 1, ..)
  CMFOID  : Class ID of the follower
  CMFOTX  : Misc text/generic xml
  CMFOLV  : MOB level
  CMFOAB  : MOB ability code
*/
CREATE TABLE CMCHFO (
	CMUSERID varchar (50) NULL ,
	CMFONM int NULL ,
	CMFOID varchar (50) NULL ,
	CMFOTX longtext NULL ,
	CMFOLV int NULL ,
	CMFOAB int NULL 
);

ALTER TABLE CMCHFO ADD ( UNIQUE KEY (CMUSERID,CMFONM) );

/**
  TABLE CMCHCL - Character Clan affiliation
  CMUSERID: Character name/id 
  CMCLAN  : Clan name/id
  CMCLRO  : Role of the character in the clan
  CMCLSTS : Various contribution stats, semicolon delimited
*/
CREATE TABLE CMCHCL (
	CMUSERID varchar (50) NULL ,
	CMCLAN varchar (100) NULL ,
	CMCLRO int NULL,
	CMCLSTS varchar (100) NULL
);

ALTER TABLE CMCHCL ADD ( UNIQUE KEY (CMUSERID,CMCLAN) );

/**
  TABLE CMCHIT - Character Inventory
  CMUSERID: Character name/id
  CMITNM  : Unique item ID, for updating the item in the DB
  CMITID  : The Item Class ID for the item
  CMITTX  : Misc text/generic xml for the item
  CMITLO  : Unique item ID of the container for the item
  CMITWO  : If being worn, this is the worn loc bitmap
  CMITUR  : Uses remaining (for ammo, wands, etc)
  CMITLV  : Item level
  CMITAB  : Ability code of the item
  CMHEIT  : Height of the item (for wearables)
*/
CREATE TABLE CMCHIT (
	CMUSERID varchar (50) NULL ,
	CMITNM varchar (100) NULL ,
	CMITID varchar (50) NULL ,
	CMITTX longtext NULL ,
	CMITLO varchar (100) NULL ,
	CMITWO bigint NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMCHIT ADD ( UNIQUE KEY (CMUSERID,CMITNM) );

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
  TABLE CMQUESTS - Quest definitions
  CMQUESID: Quest name/ID
  CMQUTYPE: Quest Class ID
  CMQFLAGS: State flags for the quest
  CMQSCRPT: Actual quest script, or path to it
  CMQWINNS: List of players who won the quest
*/
CREATE TABLE CMQUESTS (
	CMQUESID varchar (250) NULL ,
	CMQUTYPE varchar (50) NULL ,
	CMQFLAGS int NULL ,
	CMQSCRPT longtext NULL ,
	CMQWINNS longtext NULL
);

ALTER TABLE CMQUESTS ADD ( UNIQUE KEY (CMQUESID) );

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

/**
  TABLE CMJRNL - Journal Item, Forum, Email, and Command Journal Messages
  CMJKEY: Unique journal key
  CMJRNL: Journal name
  CMFROM: Sender of the message
  CMDATE: Creation/Update date timestamp of the msg
  CMTONM: Recipient of the message, or ALL
  CMSUBJ: Subject of the message
  CMPART: Unique journal key of the parent message (for replies)
  CMATTR: Message attributes (sticky!)
  CMDATA: Not sure, possibly unused
  CMUPTM: Last updated time -- could be view time
  CMIMGP: Image path for the message
  CMVIEW: Number of views for the message
  CMREPL: Cached number of replies for the message
  CMMSGT: Actual message text 
*/
CREATE TABLE CMJRNL (
	CMJKEY varchar (160) ,
	CMJRNL varchar (100) NULL ,
	CMFROM varchar (50) NULL ,
	CMDATE varchar (50) NULL ,
	CMTONM varchar (50) NULL ,
	CMSUBJ varchar (255) NULL ,
	CMPART varchar (75) NULL ,
	CMATTR int NULL,
	CMDATA varchar (255) NULL ,
	CMUPTM bigint NULL,
	CMIMGP varchar (50) NULL,
	CMVIEW int NULL,
	CMREPL int NULL,
	CMMSGT longtext NULL 
);

ALTER TABLE CMJRNL ADD ( UNIQUE KEY (CMJKEY) );

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM);

/**
  TABLE CMCLAN - Clans
  CMCLID: Clan Name/ID
  CMTYPE: Flags, usually 0
  CMDESC: Description of the clans
  CMACPT: Zappermask to be accepted to the clan
  CMPOLI: Politics, clan relations w/ other clans, in XML
  CMRCLL: Recall/Home room ID
  CMDNAT: Donation room ID
  CMSTAT: Activation status
  CMMORG: Morgue room ID
  CMTROP: Trophies mask
*/
CREATE TABLE CMCLAN (
	CMCLID varchar (100) ,
	CMTYPE int ,
	CMDESC longtext NULL ,
	CMACPT varchar (255) NULL ,
	CMPOLI longtext NULL ,
	CMRCLL varchar (50) NULL ,
	CMDNAT varchar (50) NULL ,
	CMSTAT int NULL ,
	CMMORG varchar (50) NULL ,
	CMTROP int NULL
);

ALTER TABLE CMCLAN ADD ( UNIQUE KEY (CMCLID) );

/**
  TABLE CMPDAT - Player Data (bank accounts, etc)
  CMPLID: Player character name/id
  CMSECT: Data section/category (CONQITEMS, bank chains, etc)
  CMPKEY: Unique player data entry key, for updates
  CMPDAT: Actual data, usually xml, very CMSECT dependent 
*/
CREATE TABLE CMPDAT (
	CMPLID varchar (100) ,
	CMSECT varchar (100) ,
	CMPKEY varchar (255) ,
	CMPDAT longtext NULL 
);

ALTER TABLE CMPDAT ADD ( UNIQUE KEY (CMPLID,CMSECT,CMPKEY) );

/**
  TABLE CMGRAC - Generic Races 
  CMRCID: Race ID
  CMRDAT: Generic Race XML definition
  CMRCDT: Creation date/time (or last time player used this race during load)
*/
CREATE TABLE CMGRAC (
	CMRCID varchar (250) ,
	CMRDAT longtext NULL ,
	CMRCDT bigint NULL
);

ALTER TABLE CMGRAC ADD ( UNIQUE KEY (CMRCID) );

/**
  TABLE CMCCAC - Generic Character Classes
  CMCCID: Char Class ID
  CMCDAT: Generic Character Class XML definition
*/	
CREATE TABLE CMCCAC (
	CMCCID varchar (50) ,
	CMCDAT longtext NULL 
);

ALTER TABLE CMCCAC ADD ( UNIQUE KEY (CMCCID) );

/**
  TABLE CMGAAC - Generic Abilities
  CMGAID: Ability ID
  CMGAAT: Generic Ability XML Definition
  CMGACL: Generic Ability Base Class ID
*/
CREATE TABLE CMGAAC (
	CMGAID varchar (50) ,
	CMGAAT longtext NULL , 
	CMGACL varchar (50) NULL 
);

ALTER TABLE CMGAAC ADD ( UNIQUE KEY (CMGAID) );

/**
  TABLE CMACCT - Player Account
  CMANAM: Account Name
  CMPASS: Account Password (or hash)
  CMCHRS: List of Characters in this account, by name
  CMAXML: Account configuration data in XML format
*/
CREATE TABLE CMACCT (
	CMANAM varchar (50) ,
	CMPASS varchar (50) ,
	CMCHRS longtext NULL ,
	CMAXML longtext NULL 
);

ALTER TABLE CMACCT ADD UNIQUE KEY (CMANAM);

/**
  TABLE CMBKLG - Channel BackLog
  CMNAME: Channel name
  CMINDX: Ordinal value of this backlog message
  CMSNAM: Sub-Name fields, usually a clan ID of some sort
  CMDATE: Date timestamp in millis when posted
  CMDATA: XML encoded CMMsg of the backlog message
*/
CREATE TABLE CMBKLG (
	CMNAME varchar (50),
	CMINDX int,
	CMSNAM int,
	CMDATE bigint NULL,
	CMDATA longtext NULL
);

ALTER TABLE CMBKLG ADD ( UNIQUE KEY (CMNAME,CMINDX) );

/**
  TABLE CMCLIT - Clan World Items
  CMCLID : Clan Name/ID
  CMITNM : Unique item ID, for updating the item in the DB
  CMITID : The Item Class ID for the item
  CMITTX : Misc text/generic xml for the item
  CMITLO : Unique item ID of the container for the item
  CMITWO  : If being worn, this is the worn loc bitmap
  CMITUR : Uses remaining (for ammo, wands, etc)
  CMITLV : Item level
  CMITAB : Ability code of the item
  CMHEIT : Height of the item (for wearables)
*/
CREATE TABLE CMCLIT (
	CMCLID varchar (100) ,
	CMITNM varchar (100) ,
	CMITID varchar (50) NULL ,
	CMITTX longtext NULL ,
	CMITLO varchar (100) NULL ,
	CMITWO bigint NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMCLIT ADD ( UNIQUE KEY (CMCLID,CMITNM) );

