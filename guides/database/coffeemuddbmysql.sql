# Connection: SQL
# Host: localhost
# Saved: 2003-04-01 00:29:14
# 
# Connection: SQL
# Host: localhost
# Saved: 2003-04-01 00:27:39
# 
CREATE TABLE CMCHAB (
	CMUSERID char (50) NULL ,
	CMABID char (50) NULL ,
	CMABPF int NULL ,
	CMABTX text NULL
);

ALTER TABLE CMCHAB
	ADD 
	( 
		UNIQUE KEY (CMUSERID,CMABID)
	);

CREATE TABLE CMCHAR (
	CMUSERID char (50) NULL ,
	CMPASS char (50) NULL ,
	CMCLAS char (200) NULL ,
	CMSTRE int NULL ,
	CMRACE char (50) NULL ,
	CMDEXT int NULL ,
	CMCONS int NULL ,
	CMGEND char (50) NULL ,
	CMWISD int NULL ,
	CMINTE int NULL ,
	CMCHAR int NULL ,
	CMHITP int NULL ,
	CMLEVL char (50) NULL ,
	CMMANA int NULL ,
	CMMOVE int NULL ,
	CMDESC char (255) NULL ,
	CMALIG int NULL ,
	CMEXPE int NULL ,
	CMEXLV int NULL ,
	CMWORS char (50) NULL ,
	CMPRAC int NULL ,
	CMTRAI int NULL ,
	CMAGEH int NULL ,
	CMGOLD int NULL ,
	CMWIMP int NULL ,
	CMQUES int NULL ,
	CMROID char (50) NULL ,
	CMDATE datetime NULL ,
	CMCHAN int NULL ,
	CMATTA int NULL ,
	CMAMOR int NULL ,
	CMDAMG int NULL ,
	CMBTMP int NULL ,
	CMLEIG char (50) NULL ,
	CMHEIT int NULL ,
	CMWEIT int NULL ,
	CMPRPT char (250) NULL,
	CMCOLR char (50) NULL,
	CMLSIP char (100) NULL,
	CMCLAN char (100) NULL,
	CMCLRO integer NULL
);

ALTER TABLE CMCHAR
	ADD 
	( 
		UNIQUE KEY (CMUSERID)
	);

CREATE TABLE CMCHFO (
	CMUSERID char (50) NULL ,
	CMFONM int NULL ,
	CMFOID char (50) NULL ,
	CMFOTX text NULL ,
	CMFOLV int NULL ,
	CMFOAB int NULL 
);

ALTER TABLE CMCHFO
	ADD 
	( 
		UNIQUE KEY (CMUSERID,CMFONM)
	);

CREATE TABLE CMCHIT (
	CMUSERID char (50) NULL ,
	CMITNM char (100) NULL ,
	CMITID char (50) NULL ,
	CMITTX text NULL ,
	CMITLO char (100) NULL ,
	CMITWO int NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMCHIT
	ADD 
	( 
		UNIQUE KEY (CMUSERID,CMITNM)
	);

CREATE TABLE CMROCH (
	CMROID char (50) NULL ,
	CMCHNM char (100) NULL ,
	CMCHID char (50) NULL ,
	CMCHTX text NULL ,
	CMCHLV int NULL ,
	CMCHAB int NULL ,
	CMCHRE int NULL ,
	CMCHRI char (100) NULL
);

ALTER TABLE CMROCH 
	ADD 
	( 
		UNIQUE KEY (CMROID,CMCHNM)
	);

CREATE TABLE CMROEX (
	CMROID char (50) NULL ,
	CMDIRE int NULL ,
	CMEXID char (50) NULL ,
	CMEXTX text NULL ,
	CMNRID char (50) NULL 
);

ALTER TABLE CMROEX 
	ADD 
	( 
		UNIQUE KEY (CMROID,CMDIRE)
	);

CREATE TABLE CMROIT (
	CMROID char (50) NULL ,
	CMITNM char (100) NULL ,
	CMITID char (50) NULL ,
	CMITLO char (100) NULL ,
	CMITTX text NULL ,
	CMITRE int NULL ,
	CMITUR int NULL ,
	CMITLV int NULL ,
	CMITAB int NULL ,
	CMHEIT int NULL
);

ALTER TABLE CMROIT 
	ADD 
	( 
		UNIQUE KEY (CMROID,CMITNM)
	);

CREATE TABLE CMROOM (
	CMROID char (50) NULL ,
	CMLOID char (50) NULL ,
	CMAREA char (50) NULL ,
	CMDESC1 char (50) NULL ,
	CMDESC2 text NULL ,
	CMROTX text NULL 
);

ALTER TABLE CMROOM 
	ADD 
	( 
		UNIQUE KEY (CMROID)
	);


CREATE TABLE CMAREA (
	CMAREA char (50) ,
	CMTYPE char (50) ,
	CMCLIM int NULL ,
	CMSUBS char (100) NULL ,
	CMDESC text NULL ,
	CMROTX text NULL ,
	CMTECH int NULL
);

ALTER TABLE CMAREA 
	ADD 
	( 
		UNIQUE KEY (CMAREA)
	);

CREATE TABLE CMJRNL (
	CMJKEY char (50) ,
	CMJRNL char (50) NULL ,
	CMFROM char (50) NULL ,
	CMDATE int  NULL ,
	CMTONM char (50) NULL ,
	CMSUBJ char (100) NULL ,
	CMMSGT text NULL 
);

ALTER TABLE CMJRNL 
	ADD 
	( 
		UNIQUE KEY (CMJKEY)
	);


CREATE TABLE CMCLAN (
	CMCLID char (100) ,
	CMTYPE int ,
	CMDESC text NULL ,
	CMACPT char (255) NULL ,
	CMPOLI text NULL ,
	CMRCLL char (50) NULL ,
	CMDNAT char (50) NULL
);

ALTER TABLE CMCLAN 
	ADD 
	( 
		UNIQUE KEY (CMCLID)
	);

