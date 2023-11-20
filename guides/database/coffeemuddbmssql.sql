/**
# M$ SQL Server Database Creation Script for CoffeeMud
*/
/**
  TABLE CMVFS - CoffeeMud Virtual File System table
  CMFNAM : Full path and filename
  CMDTYP : Mask bits, for hidden, directory, etc.
  CMMODD : Modified date/timestamp in millis
  CMWHOM : Who created/modified the file
  CMDATA : The file data, text encoded
*/
CREATE TABLE CMVFS (
	CMFNAM nvarchar (255),
	CMDTYP integer ,
	CMMODD bigint,
	CMWHOM nvarchar (50) NULL,
	CMDATA ntext NULL,
	PRIMARY KEY (CMFNAM)
);

/**
  TABLE CMCHAB - Player Character Abilities
  CMUSERID : Character name
  CMABID   : Ability ID
  CMABPF   : Character Ability proficiency (0-100)
  CMABTX   : Misc Text/Parameters for the Ability
*/
CREATE TABLE CMCHAB (
	CMUSERID nvarchar (50),
	CMABID nvarchar (50),
	CMABPF integer NULL,
	CMABTX ntext NULL,
	PRIMARY KEY (CMUSERID, CMABID)
);

/**
  TABLE CMSTAT - Game Event History Statistics table

  CMSTRT : Start epoch time in millis  
  CMENDT : End epoch time in millis
  CMDATA : XML formatted data for this period
*/
CREATE TABLE CMSTAT (
	CMSTRT bigint,
	CMENDT bigint,
	CMDATA ntext NULL,
PRIMARY KEY (CMSTRT)
);
	
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
	CMNAME nvarchar (100) ,
	CMBYNM nvarchar (100) NULL ,
	CMSUBJ nvarchar (255) NULL ,
	CMDESC ntext NULL ,
	CMOPTN ntext NULL ,
	CMFLAG integer NULL ,
	CMQUAL nvarchar (255) NULL ,
	CMRESL ntext NULL,
	CMEXPI bigint NULL,
	PRIMARY KEY (CMNAME)
);

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
  CMTRAI  : Training sessions
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
	CMCHID nvarchar (50),
	CMUSERID nvarchar (50),
	CMPASS nvarchar (50) NULL,
	CMCLAS nvarchar (250) NULL,
	CMSTRE integer NULL,
	CMRACE nvarchar (250) NULL,
	CMDEXT integer NULL,
	CMCONS integer NULL,
	CMGEND nvarchar (50) NULL,
	CMWISD integer NULL,
	CMINTE integer NULL,
	CMCHAR integer NULL,
	CMHITP integer NULL,
	CMLEVL nvarchar (50) NULL,
	CMMANA integer NULL,
	CMMOVE integer NULL,
	CMDESC ntext NULL,
	CMALIG integer NULL,
	CMEXPE integer NULL,
	CMEXLV integer NULL,
	CMWORS nvarchar (50) NULL,
	CMPRAC integer NULL,
	CMTRAI integer NULL,
	CMAGEH integer NULL,
	CMGOLD integer NULL,
	CMWIMP integer NULL,
	CMQUES integer NULL,
	CMROID nvarchar (100) NULL,
	CMDATE nvarchar (70) NULL,
	CMCHAN integer NULL,
	CMATTA integer NULL,
	CMAMOR integer NULL,
	CMDAMG integer NULL,
	CMBTMP integer NULL,
	CMLEIG nvarchar (50) NULL,
	CMHEIT integer NULL,
	CMWEIT integer NULL,
	CMPRPT nvarchar (250) NULL,
	CMCOLR nvarchar (255) NULL,
	CMLSIP nvarchar (100) NULL,
	CMEMAL nvarchar (255),
	CMPFIL ntext NULL,
	CMSAVE nvarchar (150) NULL,
	CMMXML ntext NULL,
	PRIMARY KEY (CMUSERID)
);

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
	CMUSERID nvarchar (50),
	CMFONM integer,
	CMFOID nvarchar (50) NULL,
	CMFOTX ntext  NULL,
	CMFOLV integer NULL,
	CMFOAB integer NULL,
	PRIMARY KEY (CMUSERID, CMFONM)
);

/**
  TABLE CMCHCL - Character Clan affiliation
  CMUSERID: Character name/id 
  CMCLAN  : Clan name/id
  CMCLRO  : Role of the character in the clan
  CMCLSTS : Various contribution stats, semicolon delimited
*/
CREATE TABLE CMCHCL (
	CMUSERID nvarchar (50),
	CMCLAN nvarchar (100),
	CMCLRO integer NULL,
	CMCLSTS nvarchar (100) NULL,
	PRIMARY KEY(CMUSERID, CMCLAN)
);

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
	CMUSERID nvarchar (50),
	CMITNM nvarchar (100),
	CMITID nvarchar (50) NULL,
	CMITTX ntext NULL,
	CMITLO nvarchar (100) NULL,
	CMITWO bigint NULL,
	CMITUR integer NULL,
	CMITLV integer NULL,
	CMITAB integer NULL,
	CMHEIT integer NULL,
	PRIMARY KEY (CMUSERID, CMITNM)
);

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
	CMROID nvarchar (50),
	CMCHNM nvarchar (100),
	CMCHID nvarchar (50) NULL,
	CMCHTX ntext  NULL,
	CMCHLV integer NULL,
	CMCHAB integer NULL,
	CMCHRE integer NULL,
	CMCHRI nvarchar (100),
	PRIMARY KEY (CMROID, CMCHNM)
);

/**
  TABLE CMROEX - Room exits
  CMROID : Room ID
  CMDIRE : Direction code (0, 1, etc..)
  CMEXID : Exit Class ID
  CMEXTX : Misc text/generic xml for the exit
  CMNRID : Linked Room ID of the exit
*/
CREATE TABLE CMROEX (
	CMROID nvarchar (50),
	CMDIRE integer,
	CMEXID nvarchar (50) NULL,
	CMEXTX ntext  NULL,
	CMNRID nvarchar (50) NULL,
	PRIMARY KEY (CMROID, CMDIRE)
);

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
	CMROID nvarchar (50),
	CMITNM nvarchar (100),
	CMITID nvarchar (50) NULL,
	CMITLO nvarchar (100) NULL,
	CMITTX ntext NULL,
	CMITRE integer NULL,
	CMITUR integer NULL,
	CMITLV integer NULL,
	CMITAB integer NULL,
	CMHEIT integer NULL,
	PRIMARY KEY (CMROID, CMITNM)
);

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
	CMROID nvarchar (50),
	CMLOID nvarchar (50) NULL,
	CMAREA nvarchar (50) NULL,
	CMDESC1 nvarchar (255) NULL,
	CMDESC2 ntext NULL,
	CMROTX ntext NULL,
	PRIMARY KEY (CMROID)
);

/**
  TABLE CMQUESTS - Quest definitions
  CMQUESID: Quest name/ID
  CMQUTYPE: Quest Class ID
  CMQFLAGS: State flags for the quest
  CMQSCRPT: Actual quest script, or path to it
  CMQWINNS: List of players who won the quest
*/
CREATE TABLE CMQUESTS (
	CMQUESID nvarchar (250),
	CMQUTYPE nvarchar (50) NULL,
	CMQFLAGS integer NULL ,
	CMQSCRPT ntext NULL,
	CMQWINNS ntext NULL,
	PRIMARY KEY (CMQUESID)
);

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
	CMAREA nvarchar (50) ,
	CMTYPE nvarchar (50) ,
	CMCLIM integer NULL ,
	CMSUBS nvarchar (100) NULL ,
	CMDESC ntext NULL ,
	CMROTX ntext NULL , 
	CMTECH integer NULL,
	PRIMARY KEY (CMAREA)
);

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
	CMJKEY nvarchar (160) ,
	CMJRNL nvarchar (100) NULL ,
	CMFROM nvarchar (50) NULL ,
	CMDATE nvarchar (50) NULL ,
	CMTONM nvarchar (50) NULL ,
	CMSUBJ nvarchar (255) NULL ,
	CMPART nvarchar (75) NULL ,
	CMATTR integer NULL,
	CMDATA nvarchar (255) NULL ,
	CMUPTM bigint NULL,
	CMIMGP nvarchar (50) NULL,
	CMVIEW integer NULL,
	CMREPL integer NULL,
	CMMSGT ntext NULL,
	CMEXPI bigint NULL,
	PRIMARY KEY (CMJKEY) 
);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL ASC);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART ASC);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM ASC);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM ASC);
CREATE INDEX CMJRNLCMEXPI on CMJRNL (CMEXPI ASC);

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
	CMCLID nvarchar (100) ,
	CMTYPE integer ,
	CMDESC ntext NULL ,
	CMACPT nvarchar (255) NULL ,
	CMPOLI ntext NULL ,
	CMRCLL nvarchar (50) NULL ,
	CMDNAT nvarchar (50) NULL ,
	CMSTAT integer NULL ,
	CMMORG nvarchar (50) NULL ,
	CMTROP integer NULL ,
	PRIMARY KEY (CMCLID) 
);

/**
  TABLE CMPDAT - Player Data (bank accounts, etc)
  CMPLID: Player character name/id
  CMSECT: Data section/category (CONQITEMS, bank chains, etc)
  CMPKEY: Unique player data entry key, for updates
  CMPDAT: Actual data, usually xml, very CMSECT dependent 
*/
CREATE TABLE CMPDAT (
	CMPLID nvarchar (100) ,
	CMSECT nvarchar (100) ,
	CMPKEY nvarchar (255) ,
	CMPDAT ntext NULL ,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

/**
  TABLE CMGRAC - Generic Races 
  CMRCID: Race ID
  CMRDAT: Generic Race XML definition
  CMRCDT: Creation date/time (or last time player used this race during load)
*/
CREATE TABLE CMGRAC (
	CMRCID nvarchar (250) ,
	CMRDAT ntext NULL ,
	CMRCDT bigint NULL ,
	PRIMARY KEY (CMRCID)
);

/**
  TABLE CMCCAC - Generic Character Classes
  CMCCID: Char Class ID
  CMCDAT: Generic Character Class XML definition
*/	
CREATE TABLE CMCCAC (
	CMCCID nvarchar (50) ,
	CMCDAT ntext NULL ,
	PRIMARY KEY (CMCCID)
);

/**
  TABLE CMGAAC - Generic Abilities
  CMGAID: Ability ID
  CMGAAT: Generic Ability XML Definition
  CMGACL: Generic Ability Base Class ID
*/
CREATE TABLE CMGAAC (
	CMGAID nvarchar (50) ,
	CMGAAT ntext NULL , 
	CMGACL nvarchar (50) NULL ,
	PRIMARY KEY (CMGAID)
);

/**
  TABLE CMACCT - Player Account
  CMANAM: Account Name
  CMPASS: Account Password (or hash)
  CMCHRS: List of Characters in this account, by name
  CMAXML: Account configuration data in XML format
*/
CREATE TABLE CMACCT (
	CMANAM nvarchar (50) ,
	CMPASS nvarchar (50) ,
	CMCHRS ntext NULL ,
	CMAXML ntext  NULL ,
	PRIMARY KEY (CMANAM)
);

/**
  TABLE CMBKLG - Channel BackLog
  CMNAME: Channel name
  CMINDX: Ordinal value of this backlog message
  CMSNAM: Sub-Name fields, usually a clan ID of some sort
  CMDATE: Date timestamp in millis when posted
  CMDATA: XML encoded CMMsg of the backlog message
*/
CREATE TABLE CMBKLG (
	CMNAME nvarchar (50),
	CMINDX integer,
	CMSNAM integer,
	CMDATE bigint NULL,
	CMDATA ntext NULL,
	PRIMARY KEY (CMNAME,CMINDX)
);

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
	CMCLID nvarchar (100),
	CMITNM nvarchar (100),
	CMITID nvarchar (50) NULL,
	CMITTX ntext NULL,
	CMITLO nvarchar (100) NULL,
	CMITWO bigint NULL,
	CMITUR integer NULL,
	CMITLV integer NULL,
	CMITAB integer NULL,
	CMHEIT integer NULL,
	PRIMARY KEY (CMCLID, CMITNM)
);
