
CREATE TABLE CMCHAB (
CMUSERID varchar (50),
CMABID varchar (50),
CMABPF integer NULL,
CMABTX varchar (255) NULL,
PRIMARY KEY (CMUSERID, CMABID)
);

CREATE TABLE CMSTAT (
	CMSTRT bigint,
	CMENDT bigint,
	CMDATA varchar (255) NULL,
PRIMARY KEY (CMSTRT)
);
	
CREATE TABLE CMCHAR (
CMUSERID varchar (50),
CMPASS varchar (50) NULL,
CMCLAS varchar (200) NULL,
CMSTRE integer NULL,
CMRACE varchar (50) NULL,
CMDEXT integer NULL,
CMCONS integer NULL,
CMGEND varchar (50) NULL,
CMWISD integer NULL,
CMINTE integer NULL,
CMCHAR integer NULL,
CMHITP integer NULL,
CMLEVL varchar (50) NULL,
CMMANA integer NULL,
CMMOVE integer NULL,
CMDESC varchar (255) NULL,
CMALIG integer NULL,
CMEXPE integer NULL,
CMEXLV integer NULL,
CMWORS varchar (50) NULL,
CMPRAC integer NULL,
CMTRAI integer NULL,
CMAGEH integer NULL,
CMGOLD integer NULL,
CMWIMP integer NULL,
CMQUES integer NULL,
CMROID varchar (100) NULL,
CMDATE varchar (50) NULL,
CMCHAN integer NULL,
CMATTA integer NULL,
CMAMOR integer NULL,
CMDAMG integer NULL,
CMBTMP integer NULL,
CMLEIG varchar (50) NULL,
CMHEIT integer NULL,
CMWEIT integer NULL,
CMPRPT varchar (250) NULL,
CMCOLR varchar (50) NULL,
CMLSIP varchar (100) NULL,
CMCLAN varchar (100) NULL,
CMCLRO integer NULL,
CMEMAL varchar (255),
CMPFIL varchar (255) NULL,
CMSAVE varchar (150) NULL,
PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
CMUSERID varchar (50),
CMFONM integer,
CMFOID varchar (50) NULL,
CMFOTX varchar (255)  NULL,
CMFOLV integer NULL,
CMFOAB integer NULL,
PRIMARY KEY (CMUSERID, CMFONM)
);

CREATE TABLE CMCHIT (
CMUSERID varchar (50),
CMITNM varchar (100),
CMITID varchar (50) NULL,
CMITTX varchar (255) NULL,
CMITLO varchar (100) NULL,
CMITWO integer NULL,
CMITUR integer NULL,
CMITLV integer NULL,
CMITAB integer NULL,
CMHEIT integer NULL,
PRIMARY KEY (CMUSERID, CMITNM)
);

CREATE TABLE CMROCH (
CMROID varchar (50),
CMCHNM varchar (100),
CMCHID varchar (50) NULL,
CMCHTX varchar (255)  NULL,
CMCHLV integer NULL,
CMCHAB integer NULL,
CMCHRE integer NULL,
CMCHRI varchar (100),
PRIMARY KEY (CMROID, CMCHNM)
);

CREATE TABLE CMROEX (
CMROID varchar (50),
CMDIRE integer,
CMEXID varchar (50) NULL,
CMEXTX varchar (255)  NULL,
CMNRID varchar (50) NULL,
PRIMARY KEY (CMROID, CMDIRE)
);

CREATE TABLE CMROIT (
CMROID varchar (50),
CMITNM varchar (100),
CMITID varchar (50) NULL,
CMITLO varchar (100) NULL,
CMITTX varchar (255) NULL,
CMITRE integer NULL,
CMITUR integer NULL,
CMITLV integer NULL,
CMITAB integer NULL,
CMHEIT integer NULL,
PRIMARY KEY (CMROID, CMITNM)
);

CREATE TABLE CMROOM (
CMROID varchar (50),
CMLOID varchar (50) NULL,
CMAREA varchar (50) NULL,
CMDESC1 varchar (255) NULL,
CMDESC2 varchar (255) NULL,
CMROTX varchar (255) NULL,
PRIMARY KEY (CMROID)
);

CREATE TABLE CMQUESTS (
CMQUESID varchar (50),
CMQUTYPE varchar (50) NULL,
CMQSCRPT varchar (255) NULL,
CMQWINNS varchar (255) NULL,
PRIMARY KEY (CMQUESID)
);

CREATE TABLE CMAREA (
CMAREA varchar (50) ,
CMTYPE varchar (50) ,
CMCLIM integer NULL ,
CMSUBS varchar (100) NULL ,
CMDESC varchar (255) NULL ,
CMROTX varchar (255) NULL , 
CMTECH integer NULL
PRIMARY KEY (CMAREA)
);


CREATE TABLE CMJRNL (
	CMJKEY varchar (255) ,
	CMJRNL varchar (50) NULL ,
	CMFROM varchar (50) NULL ,
	CMDATE varchar (50) NULL ,
	CMTONM varchar (50) NULL ,
	CMSUBJ varchar (255) NULL ,
	CMMSGT varchar (255) NULL,
PRIMARY KEY (CMJKEY) 
);

CREATE TABLE CMCLAN (
	CMCLID varchar (100) ,
	CMTYPE integer ,
	CMDESC varchar (255) NULL ,
	CMACPT varchar (255) NULL ,
	CMPOLI varchar (255) NULL ,
	CMRCLL varchar (50) NULL ,
	CMDNAT varchar (50) NULL ,
	CMSTAT integer NULL ,
	CMMORG varchar (50) NULL ,
	CMTROP integer NULL ,
	PRIMARY KEY (CMCLID) 
);

CREATE TABLE CMPDAT (
	CMPLID varchar (100) ,
	CMSECT varchar (100) ,
	CMPKEY varchar (100) ,
	CMPDAT varchar (255) NULL ,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

CREATE TABLE CMGRAC (
	CMRCID varchar (50) ,
	CMRDAT varchar (255) NULL ,
	PRIMARY KEY (CMRCID)
);

CREATE TABLE CMCCAC (
	CMCCID varchar (50) ,
	CMCDAT varchar (255) NULL ,
	PRIMARY KEY (CMCCID)
);
