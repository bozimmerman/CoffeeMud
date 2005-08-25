CREATE TABLE CMCHAB (
CMUSERID varchar (50) NOT NULL,
CMABID varchar (50) NOT NULL,
CMABPF integer,
CMABTX long varchar,
PRIMARY KEY (CMUSERID, CMABID)
);

CREATE TABLE CMSTAT (
	CMSTRT bigint NOT NULL,
	CMENDT bigint NOT NULL,
	CMDATA long varchar,
PRIMARY KEY (CMSTRT)
);
	
CREATE TABLE CMPOLL (
	CMNAME varchar (100) NOT NULL,
	CMBYNM varchar (100)  ,
	CMSUBJ varchar (255)  ,
	CMDESC long varchar  ,
	CMOPTN long varchar  ,
	CMFLAG integer  ,
	CMQUAL varchar (255)  ,
	CHRESL long varchar ,
	CMEXPI bigint ,
	PRIMARY KEY (CMNAME)
);

CREATE TABLE CMCHAR (
CMUSERID varchar (50) NOT NULL,
CMPASS varchar (50),
CMCLAS varchar (200),
CMSTRE integer,
CMRACE varchar (50),
CMDEXT integer,
CMCONS integer,
CMGEND varchar (50),
CMWISD integer,
CMINTE integer,
CMCHAR integer,
CMHITP integer,
CMLEVL varchar (50),
CMMANA integer,
CMMOVE integer,
CMDESC varchar (255),
CMALIG integer,
CMEXPE integer,
CMEXLV integer,
CMWORS varchar (50),
CMPRAC integer,
CMTRAI integer,
CMAGEH integer,
CMGOLD integer,
CMWIMP integer,
CMQUES integer,
CMROID varchar (100),
CMDATE varchar (50),
CMCHAN integer,
CMATTA integer,
CMAMOR integer,
CMDAMG integer,
CMBTMP integer,
CMLEIG varchar (50),
CMHEIT integer,
CMWEIT integer,
CMPRPT varchar (250),
CMCOLR varchar (50),
CMLSIP varchar (100),
CMCLAN varchar (100),
CMCLRO integer,
CMEMAL varchar (255),
CMPFIL long varchar,
CMSAVE varchar (150),
CMMXML long varchar,
PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
CMUSERID varchar (50) NOT NULL,
CMFONM integer NOT NULL,
CMFOID varchar (50),
CMFOTX long varchar,
CMFOLV integer,
CMFOAB integer,
PRIMARY KEY (CMUSERID, CMFONM)
);

CREATE TABLE CMCHIT (
CMUSERID varchar (50) NOT NULL,
CMITNM varchar (100) NOT NULL,
CMITID varchar (50),
CMITTX long varchar,
CMITLO varchar (100),
CMITWO integer,
CMITUR integer,
CMITLV integer,
CMITAB integer,
CMHEIT integer,
PRIMARY KEY (CMUSERID, CMITNM)
);

CREATE TABLE CMROCH (
CMROID varchar (50) NOT NULL,
CMCHNM varchar (100) NOT NULL,
CMCHID varchar (50),
CMCHTX long varchar,
CMCHLV integer,
CMCHAB integer,
CMCHRE integer,
CMCHRI varchar (100) NOT NULL,
PRIMARY KEY (CMROID, CMCHNM)
);

CREATE TABLE CMROEX (
CMROID varchar (50) NOT NULL,
CMDIRE integer NOT NULL,
CMEXID varchar (50),
CMEXTX long varchar,
CMNRID varchar (50),
PRIMARY KEY (CMROID, CMDIRE)
);

CREATE TABLE CMROIT (
CMROID varchar (50) NOT NULL,
CMITNM varchar (100) NOT NULL,
CMITID varchar (50),
CMITLO varchar (100),
CMITTX long varchar,
CMITRE integer,
CMITUR integer,
CMITLV integer,
CMITAB integer,
CMHEIT integer,
PRIMARY KEY (CMROID, CMITNM)
);

CREATE TABLE CMROOM (
CMROID varchar (50) NOT NULL,
CMLOID varchar (50),
CMAREA varchar (50),
CMDESC1 varchar (255),
CMDESC2 long varchar,
CMROTX long varchar,
PRIMARY KEY (CMROID)
);

CREATE TABLE CMQUESTS (
CMQUESID varchar (50) NOT NULL,
CMQUTYPE varchar (50),
CMQSCRPT long varchar,
CMQWINNS long varchar,
PRIMARY KEY (CMQUESID)
);

CREATE TABLE CMAREA (
CMAREA varchar (50) NOT NULL,
CMTYPE varchar (50) NOT NULL,
CMCLIM integer,
CMSUBS varchar (100),
CMDESC long varchar,
CMROTX long varchar, 
CMTECH integer,
PRIMARY KEY (CMAREA)
);


CREATE TABLE CMJRNL (
	CMJKEY varchar (255) NOT NULL,
	CMJRNL varchar (50),
	CMFROM varchar (50),
	CMDATE varchar (50),
	CMTONM varchar (50),
	CMSUBJ varchar (255),
	CMMSGT long varchar,
PRIMARY KEY (CMJKEY) 
);

CREATE TABLE CMCLAN (
	CMCLID varchar (100) NOT NULL,
	CMTYPE integer NOT NULL,
	CMDESC long varchar,
	CMACPT varchar (255),
	CMPOLI long varchar,
	CMRCLL varchar (50),
	CMDNAT varchar (50),
	CMSTAT integer ,
	CMMORG varchar (50) ,
	CMTROP integer ,
	PRIMARY KEY (CMCLID) 
);

CREATE TABLE CMPDAT (
	CMPLID varchar (100) NOT NULL,
	CMSECT varchar (100) NOT NULL,
	CMPKEY varchar (100) NOT NULL,
	CMPDAT long varchar,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

CREATE TABLE CMGRAC (
	CMRCID varchar (50) NOT NULL,
	CMRDAT long varchar,
	PRIMARY KEY (CMRCID)
);

CREATE TABLE CMCCAC (
	CMCCID varchar (50) NOT NULL,
	CMCDAT long varchar,
	PRIMARY KEY (CMCCID)
);