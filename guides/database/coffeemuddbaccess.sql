
CREATE TABLE CMVFS (
	CMFNAM varchar (255),
	CMDTYP integer ,
	CMMODD Double,
	CMWHOM varchar (50) NULL,
	CMDATA memo NULL,
	PRIMARY KEY (CMFNAM)
);

CREATE TABLE CMCHAB (
	CMUSERID varchar (50),
	CMABID varchar (50),
	CMABPF integer NULL,
	CMABTX memo NULL,
	PRIMARY KEY (CMUSERID, CMABID)
);

CREATE TABLE CMSTAT (
	CMSTRT Double,
	CMENDT Double,
	CMDATA memo NULL,
	PRIMARY KEY (CMSTRT)
);

CREATE TABLE CMPOLL (
	CMNAME varchar (100) ,
	CMBYNM varchar (100) NULL ,
	CMSUBJ varchar (255) NULL ,
	CMDESC memo NULL ,
	CMOPTN memo NULL ,
	CMFLAG integer NULL ,
	CMQUAL varchar (255) NULL ,
	CMRESL memo NULL,
	CMEXPI Double NULL,
	PRIMARY KEY (CMNAME)
);

CREATE TABLE CMCHAR (
	CMCHID varchar (50),
	CMUSERID varchar (50),
	CMPASS varchar (50) NULL,
	CMCLAS varchar (250) NULL,
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
	CMDESC memo NULL,
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
	CMCOLR varchar (100) NULL,
	CMLSIP varchar (100) NULL,
	CMEMAL varchar (255),
	CMPFIL memo NULL,
	CMSAVE varchar (150) NULL,
	CMMXML memo NULL,
	PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
	CMUSERID varchar (50),
	CMFONM integer,
	CMFOID varchar (50) NULL,
	CMFOTX memo  NULL,
	CMFOLV integer NULL,
	CMFOAB integer NULL,
	PRIMARY KEY (CMUSERID, CMFONM)
);

CREATE TABLE CMCHCL (
	CMUSERID varchar (50) ,
	CMCLAN varchar (100) ,
	CMCLRO integer NULL,
	CMCLSTS varchar (100) NULL,
	PRIMARY KEY(CMUSERID, CMCLAN)
);

CREATE TABLE CMCHIT (
	CMUSERID varchar (50),
	CMITNM varchar (100),
	CMITID varchar (50) NULL,
	CMITTX memo NULL,
	CMITLO varchar (100) NULL,
	CMITWO Double NULL,
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
	CMCHTX memo  NULL,
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
	CMEXTX memo  NULL,
	CMNRID varchar (50) NULL,
	PRIMARY KEY (CMROID, CMDIRE)
);

CREATE TABLE CMROIT (
	CMROID varchar (50),
	CMITNM varchar (100),
	CMITID varchar (50) NULL,
	CMITLO varchar (100) NULL,
	CMITTX memo NULL,
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
	CMDESC2 memo NULL,
	CMROTX memo NULL,
	PRIMARY KEY (CMROID)
);

CREATE TABLE CMQUESTS (
	CMQUESID varchar (50),
	CMQUTYPE varchar (50) NULL,
	CMQFLAGS integer NULL ,
	CMQSCRPT memo NULL,
	CMQWINNS memo NULL,
	PRIMARY KEY (CMQUESID)
);

CREATE TABLE CMAREA (
	CMAREA varchar (50) ,
	CMTYPE varchar (50) ,
	CMCLIM integer NULL ,
	CMSUBS varchar (100) NULL ,
	CMDESC memo NULL ,
	CMROTX memo NULL , 
	CMTECH integer NULL,
PRIMARY KEY (CMAREA)
);


CREATE TABLE CMJRNL (
	CMJKEY varchar (75) ,
	CMJRNL varchar (50) NULL ,
	CMFROM varchar (50) NULL ,
	CMDATE varchar (50) NULL ,
	CMTONM varchar (50) NULL ,
	CMSUBJ varchar (255) NULL ,
	CMPART varchar (75) NULL ,
	CMATTR integer NULL,
	CMDATA varchar (255) NULL ,
	CMUPTM long NULL,
	CMIMGP varchar (50) NULL,
	CMVIEW integer NULL,
	CMREPL integer NULL,
	CMMSGT memo NULL,
	PRIMARY KEY (CMJKEY) 
);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL ASC);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART ASC);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM ASC);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM ASC);

CREATE TABLE CMCLAN (
	CMCLID varchar (100) ,
	CMTYPE integer ,
	CMDESC memo NULL ,
	CMACPT varchar (255) NULL ,
	CMPOLI memo NULL ,
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
	CMPDAT memo NULL ,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

CREATE TABLE CMGRAC (
	CMRCID varchar (50) ,
	CMRDAT memo NULL ,
	CMRCDT Double NULL ,
	PRIMARY KEY (CMRCID)
);

CREATE TABLE CMCCAC (
	CMCCID varchar (50) ,
	CMCDAT memo NULL ,
	PRIMARY KEY (CMCCID)
);

CREATE TABLE CMGAAC (
	CMGAID varchar (50) ,
	CMGAAT memo NULL , 
	CMGACL varchar (50) NULL ,
	PRIMARY KEY (CMGAID)
);

CREATE TABLE CMACCT (
	CMANAM varchar (50) ,
	CMPASS varchar (50) ,
	CMCHRS memo NULL ,
	CMAXML memo  NULL ,
	PRIMARY KEY (CMANAM)
);

CREATE TABLE CMBKLG (
	CMNAME varchar (50),
	CMINDX integer,
	CMDATE long NULL,
	CMDATA memo NULL,
	PRIMARY KEY (CMNAME,CMINDX)
);

CREATE TABLE CMCLIT (
	CMCLID varchar (100),
	CMITNM varchar (100),
	CMITID varchar (50) NULL,
	CMITTX memo NULL,
	CMITLO varchar (100) NULL,
	CMITWO Double NULL,
	CMITUR integer NULL,
	CMITLV integer NULL,
	CMITAB integer NULL,
	CMHEIT integer NULL,
	PRIMARY KEY (CMCLID, CMITNM)
);

