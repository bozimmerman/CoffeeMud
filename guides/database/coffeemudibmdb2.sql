/**
# IBM DB2 Database Creation Script for CoffeeMud
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
	CMFNAM varchar (255) NOT NULL,
	CMDTYP int  NOT NULL,
	CMMODD int NOT NULL,
	CMWHOM varchar (50),
	CMDATA CLOB,
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
	CMUSERID varchar (50)  NOT NULL,
	CMABID varchar (50)  NOT NULL,
	CMABPF int ,
	CMABTX CLOB ,
	PRIMARY KEY (CMUSERID,CMABID)
);

/**
  TABLE CMSTAT - Game Event History Statistics table

  CMSTRT : Start epoch time in millis  
  CMENDT : End epoch time in millis
  CMDATA : XML formatted data for this period
*/
CREATE TABLE CMSTAT (
	CMSTRT int NOT NULL,
	CMENDT int NOT NULL,
	CMDATA CLOB,
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
	CMNAME varchar (100)  NOT NULL,
	CMBYNM varchar (100) ,
	CMSUBJ varchar (255) ,
	CMDESC CLOB ,
	CMOPTN CLOB ,
	CMFLAG int ,
	CMQUAL varchar (255) ,
	CMRESL CLOB ,
	CMEXPI int ,
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
	CMCHID varchar (50) NOT NULL,
	CMUSERID varchar (50)  NOT NULL ,
	CMPASS varchar (50) ,
	CMCLAS varchar (250) ,
	CMSTRE int ,
	CMRACE varchar (250) ,
	CMDEXT int ,
	CMCONS int ,
	CMGEND varchar (50) ,
	CMWISD int ,
	CMINTE int ,
	CMCHAR int ,
	CMHITP int ,
	CMLEVL varchar (50) ,
	CMMANA int ,
	CMMOVE int  ,
	CMDESC CLOB ,
	CMALIG int ,
	CMEXPE int ,
	CMEXLV int ,
	CMWORS varchar (50) ,
	CMPRAC int ,
	CMTRAI int ,
	CMAGEH int ,
	CMGOLD int ,
	CMWIMP int ,
	CMQUES int ,
	CMROID varchar (100) ,
	CMDATE varchar (70) ,
	CMCHAN int ,
	CMATTA int ,
	CMAMOR int ,
	CMDAMG int ,
	CMBTMP integer(8) ,
	CMLEIG varchar (50) ,
	CMHEIT int ,
	CMWEIT int ,
	CMPRPT varchar (250) ,
	CMCOLR varchar (255) ,
	CMLSIP varchar (100) ,
	CMEMAL varchar (255) ,
	CMPFIL CLOB ,
	CMSAVE varchar (150) ,
	CMMXML CLOB ,
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
	CMUSERID varchar (50)  NOT NULL ,
	CMFONM int   NOT NULL,
	CMFOID varchar (50),
	CMFOTX CLOB,
	CMFOLV int,
	CMFOAB int ,
	PRIMARY KEY (CMUSERID,CMFONM)
);

/**
  TABLE CMCHCL - Character Clan affiliation
  CMUSERID: Character name/id 
  CMCLAN  : Clan name/id
  CMCLRO  : Role of the character in the clan
  CMCLSTS : Various contribution stats, semicolon delimited
*/
CREATE TABLE CMCHCL (
	CMUSERID varchar (50) NOT NULL,
	CMCLAN varchar (100) NOT NULL,
	CMCLRO int,
	CMCLSTS varchar (100) ,
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
	CMUSERID varchar (50)  NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMUSERID,CMITNM)
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
	CMROID varchar (50) NOT NULL,
	CMCHNM varchar (100)   NOT NULL,
	CMCHID varchar (50),
	CMCHTX CLOB,
	CMCHLV int,
	CMCHAB int,
	CMCHRE int,
	CMCHRI varchar (100) ,
	PRIMARY KEY (CMROID,CMCHNM)
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
	CMROID varchar (50)   NOT NULL,
	CMDIRE int   NOT NULL,
	CMEXID varchar (50),
	CMEXTX CLOB,
	CMNRID varchar (50) ,
	PRIMARY KEY (CMROID,CMDIRE)
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
	CMROID varchar (50) NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITLO varchar (100),
	CMITTX CLOB,
	CMITRE int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMROID,CMITNM)
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
	CMROID varchar (50)  NOT NULL,
	CMLOID varchar (50),
	CMAREA varchar (50),
	CMDESC1 varchar (255),
	CMDESC2 CLOB,
	CMROTX CLOB , 
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
	CMQUESID varchar (250)  NOT NULL,
	CMQUTYPE varchar (50),
	CMQFLAGS int,
	CMQSCRPT CLOB,
	CMQWINNS CLOB ,
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
	CMAREA varchar (50)   NOT NULL,
	CMTYPE varchar (50) ,
	CMCLIM int,
	CMSUBS varchar (100),
	CMDESC CLOB,
	CMROTX CLOB,
	CMTECH int ,
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
	CMJKEY varchar (160)   NOT NULL,
	CMJRNL varchar (100),
	CMFROM varchar (50),
	CMDATE varchar (50),
	CMTONM varchar (50),
	CMSUBJ varchar (100),
	CMPART varchar (75) ,
	CMATTR int,
	CMDATA varchar (255) ,
	CMUPTM integer(8),
	CMIMGP varchar (50),
	CMVIEW int,
	CMREPL int,
	CMMSGT CLOB,
	CMEXPI bigint NULL,
	PRIMARY KEY (CMJKEY)
);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM);
CREATE INDEX CMJRNLCMEXPI on CMJRNL (CMEXPI);

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
	CMCLID varchar (100)   NOT NULL,
	CMTYPE int ,
	CMDESC CLOB,
	CMACPT varchar (255),
	CMPOLI CLOB,
	CMRCLL varchar (50),
	CMDNAT varchar (50),
	CMSTAT int,
	CMMORG varchar (50),
	CMTROP int ,
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
	CMPLID varchar (100)   NOT NULL,
	CMSECT varchar (100)    NOT NULL,
	CMPKEY varchar (255)    NOT NULL,
	CMPDAT CLOB , 
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

/**
  TABLE CMGRAC - Generic Races 
  CMRCID: Race ID
  CMRDAT: Generic Race XML definition
  CMRCDT: Creation date/time (or last time player used this race during load)
*/
CREATE TABLE CMGRAC (
	CMRCID varchar (250)   NOT NULL,
	CMRDAT CLOB ,
	CMRCDT int ,
	PRIMARY KEY (CMRCID)
);

/**
  TABLE CMCCAC - Generic Character Classes
  CMCCID: Char Class ID
  CMCDAT: Generic Character Class XML definition
*/	
CREATE TABLE CMCCAC (
	CMCCID varchar (50)   NOT NULL,
	CMCDAT CLOB,  
	PRIMARY KEY (CMCCID)
);

/**
  TABLE CMGAAC - Generic Abilities
  CMGAID: Ability ID
  CMGAAT: Generic Ability XML Definition
  CMGACL: Generic Ability Base Class ID
*/
CREATE TABLE CMGAAC (
	CMGAID varchar (50)   NOT NULL,
	CMGAAT CLOB , 
	CMGACL varchar (50) ,  
	PRIMARY KEY (CMGAID)
);

/**
  TABLE CMCDAC - Generic Commands
  CMCDID: Command ID
  CMCDAT: Generic Command XML Definition
  CMCDCL: Generic Command Base Class ID
*/
CREATE TABLE CMCDAC (
	CMCDID varchar (50)   NOT NULL,
	CMCDAT CLOB , 
	CMCDCL varchar (50) ,  
	PRIMARY KEY (CMCDID)
);
/**
  TABLE CMACCT - Player Account
  CMANAM: Account Name
  CMPASS: Account Password (or hash)
  CMCHRS: List of Characters in this account, by name
  CMAXML: Account configuration data in XML format
*/
CREATE TABLE CMACCT (
	CMANAM varchar (50) NOT NULL,
	CMPASS varchar (50) NOT NULL,
	CMCHRS CLOB ,
	CMAXML CLOB ,
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
	CMNAME varchar (50) NOT NULL,
	CMINDX int NOT NULL,
	CMSNAM int NOT NULL,
	CMDATE integer(8),
	CMDATA CLOB,
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
	CMCLID varchar (100)  NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMCLID,CMITNM)
);

