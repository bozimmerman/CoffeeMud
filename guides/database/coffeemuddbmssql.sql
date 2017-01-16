
CREATE TABLE CMVFS (
	CMFNAM nvarchar (255),
	CMDTYP integer ,
	CMMODD bigint,
	CMWHOM nvarchar (50) NULL,
	CMDATA ntext NULL,
	PRIMARY KEY (CMFNAM)
);

CREATE TABLE CMCHAB (
	CMUSERID nvarchar (50),
	CMABID nvarchar (50),
	CMABPF integer NULL,
	CMABTX ntext NULL,
	PRIMARY KEY (CMUSERID, CMABID)
);

CREATE TABLE CMSTAT (
	CMSTRT bigint,
	CMENDT bigint,
	CMDATA ntext NULL,
PRIMARY KEY (CMSTRT)
);
	
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

CREATE TABLE CMCHAR (
	CMCHID nvarchar (50),
	CMUSERID nvarchar (50),
	CMPASS nvarchar (50) NULL,
	CMCLAS nvarchar (250) NULL,
	CMSTRE integer NULL,
	CMRACE nvarchar (50) NULL,
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
	CMDATE nvarchar (50) NULL,
	CMCHAN integer NULL,
	CMATTA integer NULL,
	CMAMOR integer NULL,
	CMDAMG integer NULL,
	CMBTMP integer NULL,
	CMLEIG nvarchar (50) NULL,
	CMHEIT integer NULL,
	CMWEIT integer NULL,
	CMPRPT nvarchar (250) NULL,
	CMCOLR nvarchar (100) NULL,
	CMLSIP nvarchar (100) NULL,
	CMEMAL nvarchar (255),
	CMPFIL ntext NULL,
	CMSAVE nvarchar (150) NULL,
	CMMXML ntext NULL,
	PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
	CMUSERID nvarchar (50),
	CMFONM integer,
	CMFOID nvarchar (50) NULL,
	CMFOTX ntext  NULL,
	CMFOLV integer NULL,
	CMFOAB integer NULL,
	PRIMARY KEY (CMUSERID, CMFONM)
);

CREATE TABLE CMCHCL (
	CMUSERID nvarchar (50),
	CMCLAN nvarchar (100),
	CMCLRO integer NULL,
	CMCLSTS nvarchar (100) NULL,
	PRIMARY KEY(CMUSERID, CMCLAN)
);

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

CREATE TABLE CMROEX (
	CMROID nvarchar (50),
	CMDIRE integer,
	CMEXID nvarchar (50) NULL,
	CMEXTX ntext  NULL,
	CMNRID nvarchar (50) NULL,
	PRIMARY KEY (CMROID, CMDIRE)
);

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

CREATE TABLE CMROOM (
	CMROID nvarchar (50),
	CMLOID nvarchar (50) NULL,
	CMAREA nvarchar (50) NULL,
	CMDESC1 nvarchar (255) NULL,
	CMDESC2 ntext NULL,
	CMROTX ntext NULL,
	PRIMARY KEY (CMROID)
);

CREATE TABLE CMQUESTS (
	CMQUESID nvarchar (50),
	CMQUTYPE nvarchar (50) NULL,
	CMQFLAGS integer NULL ,
	CMQSCRPT ntext NULL,
	CMQWINNS ntext NULL,
	PRIMARY KEY (CMQUESID)
);

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

CREATE TABLE CMJRNL (
	CMJKEY nvarchar (75) ,
	CMJRNL nvarchar (50) NULL ,
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
	PRIMARY KEY (CMJKEY) 
);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL ASC);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART ASC);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM ASC);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM ASC);

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

CREATE TABLE CMPDAT (
	CMPLID nvarchar (100) ,
	CMSECT nvarchar (100) ,
	CMPKEY nvarchar (100) ,
	CMPDAT ntext NULL ,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

CREATE TABLE CMGRAC (
	CMRCID nvarchar (50) ,
	CMRDAT ntext NULL ,
	CMRCDT bigint NULL ,
	PRIMARY KEY (CMRCID)
);

CREATE TABLE CMCCAC (
	CMCCID nvarchar (50) ,
	CMCDAT ntext NULL ,
	PRIMARY KEY (CMCCID)
);

CREATE TABLE CMGAAC (
	CMGAID nvarchar (50) ,
	CMGAAT ntext NULL , 
	CMGACL nvarchar (50) NULL ,
	PRIMARY KEY (CMGAID)
);

CREATE TABLE CMACCT (
	CMANAM nvarchar (50) ,
	CMPASS nvarchar (50) ,
	CMCHRS ntext NULL ,
	CMAXML ntext  NULL ,
	PRIMARY KEY (CMANAM)
);

CREATE TABLE CMBKLG (
	CMNAME nvarchar (50),
	CMINDX integer,
	CMDATE bigint NULL,
	CMDATA ntext NULL,
	PRIMARY KEY (CMNAME,CMINDX)
);

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
