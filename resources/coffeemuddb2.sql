
CREATE TABLE CMCHAB (
CMUSERID varchar (50),
CMABID varchar (50),
CMABLVL integer NULL,
CMABAB integer NULL,
CMABUR integer NULL,
CMABPF integer NULL,
PRIMARY KEY (CMUSERID, CMABID)
);

CREATE TABLE CMCHAR (
CMUSERID varchar (50),
CMPASS varchar (50) NULL,
CMCLAS varchar (50) NULL,
CMSTRE integer NULL,
CMRACE varchar (50) NULL,
CMDEXT integer NULL,
CMCONS integer NULL,
CMGEND varchar (50) NULL,
CMWISD integer NULL,
CMINTE integer NULL,
CMCHAR integer NULL,
CMHITP integer NULL,
CMLEVL integer NULL,
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
CMROID varchar (50) NULL,
CMDATE datetime NULL,
CMCHAN integer NULL,
CMATTA integer NULL,
CMAMOR integer NULL,
CMDAMG integer NULL,
CMBTMP integer NULL,
CMLEIG varchar (50) NULL,
CMHEIT integer NULL,
CMWEIT integer NULL,
PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
CMUSERID varchar (50),
CMFONM integer,
CMFOID varchar (50) NULL,
CMFOTX text  NULL,
CMFOLV integer NULL,
CMFOAB integer NULL,
PRIMARY KEY (CMUSERID, CMFONM)
);

CREATE TABLE CMCHIT (
CMUSERID varchar (50),
CMITNM varchar (50),
CMITID varchar (50) NULL,
CMITTX text NULL,
CMITLO varchar (50) NULL,
CMITWO integer NULL,
CMITUR integer NULL,
CMITLV integer NULL,
CMITAB integer NULL,
CMHEIT integer NULL,
PRIMARY KEY (CMUSERID, CMITNM)
);

CREATE TABLE CMROCH (
CMROID varchar (50),
CMCHNM integer,
CMCHID varchar (50) NULL,
CMCHTX text  NULL,
CMCHLV integer NULL,
CMCHAB integer NULL,
CMCHRE integer NULL,
PRIMARY KEY (CMROID, CMCHNM)
);

CREATE TABLE CMROEX (
CMROID varchar (50),
CMDIRE integer,
CMEXID varchar (50) NULL,
CMEXTX text  NULL,
CMNRID varchar (50) NULL,
PRIMARY KEY (CMROID, CMDIRE)
);

CREATE TABLE CMROIT (
CMROID varchar (50),
CMITNM varchar (50),
CMITID varchar (50) NULL,
CMITLO varchar (50) NULL,
CMITTX text NULL,
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
CMDESC1 varchar (50) NULL,
CMDESC2 text NULL,
CMROTX text NULL,
PRIMARY KEY (CMROID)
);

CREATE TABLE CMAREA (
CMAREA varchar (50) NULL ,
CMTYPE varchar (50) NULL ,
CMCLIM integer NULL ,
CMSUBS varchar (100) NULL ,
CMDESC text NULL ,
CMROTX text NULL ,
PRIMARY KEY (CMAREA)
);


CREATE TABLE CMJRNL (
	CMJKEY varchar (50) NULL ,
	CMJRNL varchar (50) NULL ,
	CMFROM varchar (50) NULL ,
	CMDATE integer NULL ,
	CMTONM varchar (50) NULL ,
	CMSUBJ varchar (100) NULL ,
	CMMSGT text NULL,
PRIMARY KEY (CMJKEY) 
);
