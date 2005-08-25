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
	CMABTX longtext NULL
);

ALTER TABLE CMCHAB
	ADD 
	( 
		UNIQUE KEY (CMUSERID,CMABID)
	);

CREATE TABLE CMSTAT (
	CMSTRT bigint,
	CMENDT bigint,
	CMDATA longtext NULL
);

ALTER TABLE CMSTAT
	ADD 
	( 
		UNIQUE KEY (CMSTRT)
	);
	
CREATE TABLE CMPOLL (
	CMNAME char (100) ,
	CMBYNM char (100) NULL ,
	CMSUBJ char (255) NULL ,
	CMDESC longtext NULL ,
	CMOPTN longtext NULL ,
	CMFLAG int NULL ,
	CMQUAL char (255) NULL ,
	CHRESL longtext NULL
);

ALTER TABLE CMPOLL
	ADD 
	( 
		UNIQUE KEY (CMNAME)
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
	CMROID char (100) NULL ,
	CMDATE char (50) NULL ,
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
	CMCLRO integer NULL,
	CMEMAL char (255) NULL,
	CMPFIL longtext NULL,
	CMSAVE char (150) NULL,
	CMMXML longtext NULL
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
	CMFOTX longtext NULL ,
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
	CMITTX longtext NULL ,
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
	CMCHTX longtext NULL ,
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
	CMEXTX longtext NULL ,
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
	CMITTX longtext NULL ,
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
	CMDESC1 char (255) NULL ,
	CMDESC2 longtext NULL ,
	CMROTX longtext NULL 
);

ALTER TABLE CMROOM 
	ADD 
	( 
		UNIQUE KEY (CMROID)
	);


CREATE TABLE CMQUESTS (
	CMQUESID char (50) NULL ,
	CMQUTYPE char (50) NULL ,
	CMQSCRPT longtext NULL ,
	CMQWINNS longtext NULL
);

ALTER TABLE CMQUESTS 
	ADD 
	( 
		UNIQUE KEY (CMQUESID)
	);


CREATE TABLE CMAREA (
	CMAREA char (50) ,
	CMTYPE char (50) ,
	CMCLIM int NULL ,
	CMSUBS char (100) NULL ,
	CMDESC longtext NULL ,
	CMROTX longtext NULL ,
	CMTECH int NULL
);

ALTER TABLE CMAREA 
	ADD 
	( 
		UNIQUE KEY (CMAREA)
	);

CREATE TABLE CMJRNL (
	CMJKEY char (255) ,
	CMJRNL char (50) NULL ,
	CMFROM char (50) NULL ,
	CMDATE char (50) NULL ,
	CMTONM char (50) NULL ,
	CMSUBJ char (255) NULL ,
	CMMSGT longtext NULL 
);

ALTER TABLE CMJRNL 
	ADD 
	( 
		UNIQUE KEY (CMJKEY)
	);


CREATE TABLE CMCLAN (
	CMCLID char (100) ,
	CMTYPE int ,
	CMDESC longtext NULL ,
	CMACPT char (255) NULL ,
	CMPOLI longtext NULL ,
	CMRCLL char (50) NULL ,
	CMDNAT char (50) NULL ,
	CMSTAT int NULL ,
	CMMORG char (50) NULL ,
	CMTROP int NULL
);

ALTER TABLE CMCLAN 
	ADD 
	( 
		UNIQUE KEY (CMCLID)
	);

CREATE TABLE CMPDAT (
	CMPLID char (100) ,
	CMSECT char (100) ,
	CMPKEY char (100) ,
	CMPDAT longtext NULL 
);

ALTER TABLE CMPDAT 
	ADD 
	( 
		UNIQUE KEY (CMPLID,CMSECT,CMPKEY)
	);

CREATE TABLE CMGRAC (
	CMRCID char (50) ,
	CMRDAT longtext NULL 
);

ALTER TABLE CMGRAC 
	ADD 
	( 
		UNIQUE KEY (CMRCID)
	);
	
CREATE TABLE CMCCAC (
	CMCCID char (50) ,
	CMCDAT longtext NULL 
);

ALTER TABLE CMCCAC 
	ADD 
	( 
		UNIQUE KEY (CMCCID)
	);


