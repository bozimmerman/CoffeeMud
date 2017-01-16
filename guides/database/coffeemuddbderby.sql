CREATE TABLE CMVFS (
	CMFNAM varchar (255) NOT NULL,
	CMDTYP integer NOT NULL,
	CMMODD bigint NOT NULL,
	CMWHOM varchar (50),
	CMDATA CLOB,
	PRIMARY KEY (CMFNAM)
);
CREATE TABLE CMCHAB (
	CMUSERID varchar (50) NOT NULL,
	CMABID varchar (50) NOT NULL,
	CMABPF integer,
	CMABTX CLOB,
	PRIMARY KEY (CMUSERID, CMABID)
);
CREATE TABLE CMSTAT (
	CMSTRT bigint NOT NULL,
	CMENDT bigint NOT NULL,
	CMDATA CLOB,
	PRIMARY KEY (CMSTRT)
);
CREATE TABLE CMPOLL (
	CMNAME varchar (100) NOT NULL,
	CMBYNM varchar (100)  ,
	CMSUBJ varchar (255)  ,
	CMDESC CLOB  ,
	CMOPTN CLOB  ,
	CMFLAG integer  ,
	CMQUAL varchar (255)  ,
	CMRESL CLOB ,
	CMEXPI bigint ,
	PRIMARY KEY (CMNAME)
);
CREATE TABLE CMCHAR (
	CMCHID varchar (50) NOT NULL,
	CMUSERID varchar (50) NOT NULL,
	CMPASS varchar (50),
	CMCLAS varchar (250),
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
	CMDESC CLOB,
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
	CMCOLR varchar (100),
	CMLSIP varchar (100),
	CMEMAL varchar (255),
	CMPFIL CLOB,
	CMSAVE varchar (150),
	CMMXML CLOB,
	PRIMARY KEY (CMUSERID)
);
CREATE TABLE CMCHFO (
	CMUSERID varchar (50) NOT NULL,
	CMFONM integer NOT NULL,
	CMFOID varchar (50),
	CMFOTX CLOB,
	CMFOLV integer,
	CMFOAB integer,
	PRIMARY KEY (CMUSERID, CMFONM)
);
CREATE TABLE CMCHCL (
	CMUSERID varchar (50) NOT NULL,
	CMCLAN varchar (100) NOT NULL,
	CMCLRO integer ,
	CMCLSTS varchar (100),
	PRIMARY KEY(CMUSERID, CMCLAN)
);
CREATE TABLE CMCHIT (
	CMUSERID varchar (50) NOT NULL,
	CMITNM varchar (100) NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO bigint,
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
	CMCHTX CLOB,
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
	CMEXTX CLOB,
	CMNRID varchar (50),
	PRIMARY KEY (CMROID, CMDIRE)
);
CREATE TABLE CMROIT (
	CMROID varchar (50) NOT NULL,
	CMITNM varchar (100) NOT NULL,
	CMITID varchar (50),
	CMITLO varchar (100),
	CMITTX CLOB,
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
	CMDESC2 CLOB,
	CMROTX CLOB,
	PRIMARY KEY (CMROID)
);
CREATE TABLE CMQUESTS (
	CMQUESID varchar (50) NOT NULL,
	CMQUTYPE varchar (50),
	CMQFLAGS integer ,
	CMQSCRPT CLOB,
	CMQWINNS CLOB,
	PRIMARY KEY (CMQUESID)
);
CREATE TABLE CMAREA (
	CMAREA varchar (50) NOT NULL,
	CMTYPE varchar (50) NOT NULL,
	CMCLIM integer,
	CMSUBS varchar (100),
	CMDESC CLOB,
	CMROTX CLOB, 
	CMTECH integer,
	PRIMARY KEY (CMAREA)
);
CREATE TABLE CMJRNL (
	CMJKEY varchar (75) NOT NULL,
	CMJRNL varchar (50),
	CMFROM varchar (50),
	CMDATE varchar (50),
	CMTONM varchar (50),
	CMSUBJ varchar (255),
	CMPART varchar (75),
	CMATTR integer,
	CMDATA varchar (255),
	CMUPTM bigint,
	CMIMGP varchar (50),
	CMVIEW integer,
	CMREPL integer,
	CMMSGT CLOB,
	PRIMARY KEY (CMJKEY) 
);
CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL ASC);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART ASC);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM ASC);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM ASC);
CREATE TABLE CMCLAN (
	CMCLID varchar (100) NOT NULL,
	CMTYPE integer NOT NULL,
	CMDESC CLOB,
	CMACPT varchar (255),
	CMPOLI CLOB,
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
	CMPDAT CLOB,
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);
CREATE TABLE CMGRAC (
	CMRCID varchar (50) NOT NULL,
	CMRDAT CLOB,
	CMRCDT bigint,
	PRIMARY KEY (CMRCID)
);
CREATE TABLE CMCCAC (
	CMCCID varchar (50) NOT NULL,
	CMCDAT CLOB,
	PRIMARY KEY (CMCCID)
);
CREATE TABLE CMGAAC (
	CMGAID varchar (50) NOT NULL,
	CMGAAT CLOB , 
	CMGACL varchar (50) ,
	PRIMARY KEY (CMGAID)
);
CREATE TABLE CMACCT (
	CMANAM varchar (50) NOT NULL,
	CMPASS varchar (50) NOT NULL,
	CMCHRS CLOB ,
	CMAXML CLOB ,
	PRIMARY KEY (CMANAM)
);

CREATE TABLE CMBKLG (
	CMNAME varchar (50) NOT NULL,
	CMINDX integer NOT NULL,
	CMDATE bigint,
	CMDATA CLOB,
	PRIMARY KEY (CMNAME,CMINDX)
);
CREATE TABLE CMCLIT (
	CMCLID varchar (100) NOT NULL,
	CMITNM varchar (100) NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO bigint,
	CMITUR integer,
	CMITLV integer,
	CMITAB integer,
	CMHEIT integer,
	PRIMARY KEY (CMCLID, CMITNM)
);
