package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class CMProps extends Properties
{
    private static CMProps[] props=new CMProps[256];
    private CMProps(){
    	super();
    	char c=Thread.currentThread().getThreadGroup().getName().charAt(0); 
    	if(props==null) props=new CMProps[256];
	    if(props[c]==null) props[c]=this; 
    }
    private static CMProps inst=new CMProps();
    public static CMProps instance(){return inst;}
    
	public static final long serialVersionUID=0;
    public static final int SYSTEM_PKILL=0;
    public static final int SYSTEM_MULTICLASS=1;
    public static final int SYSTEM_PLAYERDEATH=2;
    public static final int SYSTEM_PLAYERFLEE=3;
    public static final int SYSTEM_SHOWDAMAGE=4;
    public static final int SYSTEM_EMAILREQ=5;
    public static final int SYSTEM_ESC0=6;
    public static final int SYSTEM_ESC1=7;
    public static final int SYSTEM_ESC2=8;
    public static final int SYSTEM_ESC3=9;
    public static final int SYSTEM_ESC4=10;
    public static final int SYSTEM_ESC5=11;
    public static final int SYSTEM_ESC6=12;
    public static final int SYSTEM_ESC7=13;
    public static final int SYSTEM_ESC8=14;
    public static final int SYSTEM_ESC9=15;
    public static final int SYSTEM_MSPPATH=16;
    public static final int SYSTEM_BADNAMES=17;
    public static final int SYSTEM_CLANVOTEO=18;
    public static final int SYSTEM_CLANVOTER=19;
    public static final int SYSTEM_CLANVOTED=20;
    public static final int SYSTEM_AUTOPURGE=21;
    public static final int SYSTEM_MUDNAME=22;
    public static final int SYSTEM_MUDVER=23;
    public static final int SYSTEM_MUDSTATUS=24;
    public static final int SYSTEM_MUDPORTS=25;
    public static final int SYSTEM_CORPSEGUARD=26;
    public static final int SYSTEM_INIPATH=27;
    public static final int SYSTEM_MUDBINDADDRESS=28;
    public static final int SYSTEM_MUDDOMAIN=29;
    public static final int SYSTEM_I3EMAIL=30;
    public static final int SYSTEM_PREJUDICE=31;
    public static final int SYSTEM_BUDGET=32;
    public static final int SYSTEM_DEVALUERATE=33;
    public static final int SYSTEM_INVRESETRATE=34;
    public static final int SYSTEM_EMOTEFILTER=35;
    public static final int SYSTEM_SAYFILTER=36;
    public static final int SYSTEM_CHANNELFILTER=37;
    public static final int SYSTEM_CLANTROPPK=38;
    public static final int SYSTEM_MAILBOX=39;
    public static final int SYSTEM_CLANTROPCP=40;
    public static final int SYSTEM_CLANTROPEXP=41;
    public static final int SYSTEM_CLANTROPAREA=42;
    public static final int SYSTEM_COLORSCHEME=43;
    public static final int SYSTEM_SMTPSERVERNAME=44;
    public static final int SYSTEM_EXPCONTACTLINE=45;
    public static final int SYSTEM_AUTOWEATHERPARMS=46;
    public static final int SYSTEM_MXPIMAGEPATH=47;
    public static final int SYSTEM_IGNOREMASK=48;
    public static final int SYSTEM_SIPLET=49;
    public static final int SYSTEM_PREFACTIONS=50; 
    public static final int SYSTEM_AUTOAREAPROPS=51;
    public static final int SYSTEM_MOBDEATH=52;
    public static final int SYSTEM_I3ROUTERS=53;
    public static final int NUM_SYSTEM=54;

    public static final int SYSTEMI_EXPRATE=0;
    public static final int SYSTEMI_SKYSIZE=1;
    public static final int SYSTEMI_MAXSTAT=2;
    public static final int SYSTEMI_EDITORTYPE=3;
    public static final int SYSTEMI_MINCLANMEMBERS=4;
    public static final int SYSTEMI_DAYSCLANDEATH=5;
    public static final int SYSTEMI_MINCLANLEVEL=6;
    public static final int SYSTEMI_MANACOST=7;
    public static final int SYSTEMI_COMMONTRAINCOST=8;
    public static final int SYSTEMI_LANGTRAINCOST=9;
    public static final int SYSTEMI_SKILLTRAINCOST=10;
    public static final int SYSTEMI_COMMONPRACCOST=11;
    public static final int SYSTEMI_LANGPRACCOST=12;
    public static final int SYSTEMI_SKILLPRACCOST=13;
    public static final int SYSTEMI_CLANCOST=14;
    public static final int SYSTEMI_PAGEBREAK=15;
    public static final int SYSTEMI_FOLLOWLEVELDIFF=16;
    public static final int SYSTEMI_LASTPLAYERLEVEL=17;
    public static final int SYSTEMI_CLANENCHCOST=18;
    public static final int SYSTEMI_BASEMAXSTAT=19;
    public static final int SYSTEMI_MANAMINCOST=20;
    //public static final int SYSTEMI_MINMOVETIME=21;
    public static final int SYSTEMI_MANACONSUMETIME=22;
    public static final int SYSTEMI_MANACONSUMEAMT=23;
    public static final int SYSTEMI_MUDBACKLOG=24;
    public static final int SYSTEMI_TICKSPERMUDDAY=25;
    public static final int SYSTEMI_COMBATSYSTEM=26;
    public static final int SYSTEMI_JOURNALLIMIT=27;
    public static final int SYSTEMI_TICKSPERMUDMONTH=28;
    public static final int SYSTEMI_MUDTHEME=29;
    public static final int SYSTEMI_INJPCTCHANCE=30;
    public static final int SYSTEMI_INJPCTHP=31;
    public static final int SYSTEMI_INJPCTHPAMP=32;
    public static final int SYSTEMI_INJPCTCHANCEAMP=33;
    public static final int SYSTEMI_INJMULTIPLIER=34;
    public static final int SYSTEMI_STARTHP=35;
    public static final int SYSTEMI_STARTMANA=36;
    public static final int SYSTEMI_STARTMOVE=37;
    public static final int SYSTEMI_TRIALDAYS=38;
    public static final int SYSTEMI_EQVIEW=39;
    public static final int SYSTEMI_MAXCONNSPERIP=40;
    public static final int SYSTEMI_MAXNEWPERIP=41;
    public static final int SYSTEMI_MAXMAILBOX=42;
    public static final int SYSTEMI_JSCRIPTS=43;
    public static final int SYSTEMI_INJMINLEVEL=44;
    public static final int SYSTEMI_DEFCMDTIME=45;
    public static final int SYSTEMI_DEFCOMCMDTIME=46;
    public static final int SYSTEMI_DEFABLETIME=47;
    public static final int SYSTEMI_DEFCOMABLETIME=48;
    public static final int SYSTEMI_INJBLEEDMINLEVEL=49;
    public static final int SYSTEMI_INJBLEEDPCTHP=50;
    public static final int SYSTEMI_INJBLEEDPCTCHANCE=51;
    public static final int NUMI_SYSTEM=52;

    public static final int SYSTEMB_MOBCOMPRESS=0;
    public static final int SYSTEMB_ITEMDCOMPRESS=1;
    public static final int SYSTEMB_ROOMDCOMPRESS=2;
    public static final int SYSTEMB_MOBDCOMPRESS=3;
    public static final int SYSTEMB_MUDSTARTED=4;
    public static final int SYSTEMB_EMAILFORWARDING=5;
    public static final int SYSTEMB_MOBNOCACHE=6;
    public static final int SYSTEMB_ROOMDNOCACHE=7;
    public static final int SYSTEMB_MUDSHUTTINGDOWN=8;
    public static final int SYSTEMB_ACCOUNTEXPIRATION=9;
    public static final int SYSTEMB_INTRODUCTIONSYSTEM=10;
    public static final int NUMB_SYSTEM=11;
    
    protected String[] sysVars=new String[NUM_SYSTEM];
    protected Integer[] sysInts=new Integer[NUMI_SYSTEM];
    protected Boolean[] sysBools=new Boolean[NUMB_SYSTEM];
    protected Vector sayFilter=new Vector();
    protected Vector channelFilter=new Vector();
    protected Vector emoteFilter=new Vector();
    protected DVector newusersByIP=new DVector(2);
    protected DVector skillMaxManaExceptions=new DVector(2);
    protected DVector skillMinManaExceptions=new DVector(2);
    public int pkillLevelDiff=26;
    
    public boolean loaded=false;

	public CMProps(InputStream in)
	{
    	char c=Thread.currentThread().getThreadGroup().getName().charAt(0); 
	    if(props[c]==null) props[c]=this; 
		try
		{
			this.load(in);
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}
	public CMProps(String filename)
	{
    	char c=Thread.currentThread().getThreadGroup().getName().charAt(0); 
	    if(props[c]==null) props[c]=this; 
		try
		{
			CMFile F=new CMFile(filename,null,false);
			if(F.exists())
			{
				this.load(new ByteArrayInputStream(F.raw()));
				loaded=true;
			}
			else
				loaded=false;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}

    public boolean load(String filename)
    {
        try
        {
            this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
            loaded=true;
        }
        catch(IOException e)
        {
            loaded=false;
        }
        return loaded;
    }
    
	public CMProps(Properties p, String filename)
	{
		super(p);
    	char c=Thread.currentThread().getThreadGroup().getName().charAt(0); 
	    if(props[c]==null) props[c]=this; 
		
		try
		{
            this.load(new ByteArrayInputStream(new CMFile(filename,null,false).raw()));
			loaded=true;
		}
		catch(IOException e)
		{
			loaded=false;
		}
	}


	public static CMProps loadPropPage(String iniFile)
	{
        CMProps page=null;
		if (page==null || !page.loaded)
		{
            page=new CMProps(iniFile);
			if(!page.loaded)
				return null;
		}
		return page;
	}
	/** retrieve a particular .ini file entry as a string
	*
	* <br><br><b>Usage:</b>  String s=propertyGetter(p,"TAG");
	* @param tagToGet	the property tag to retreive.
	* @return String	the value of the .ini file tag
	*/
	public String getStr(String tagToGet)
	{
		String thisTag=this.getProperty(tagToGet);
		if((thisTag==null)&&(props['0']!=null)&&(props['0']!=this))
			return props['0'].getStr(tagToGet);
		if(thisTag==null) return "";
		return thisTag;
	}

	public boolean getBoolean(String tagToGet)
	{
		String thisVal=getStr(tagToGet);
		if(thisVal.toUpperCase().startsWith("T"))
			return true;
		return false;
	}

	/** retrieve a particular .ini file entry as a double
	*
	* <br><br><b>Usage:</b>  int i=propertyGetterOfInteger(p,"TAG");
	* @param tagToGet	the property tag to retreive.
	* @return int	the value of the .ini file tag
	*/
	public double getDouble(String tagToGet)
	{
		try
		{
			return Double.parseDouble(getStr(tagToGet));
		}
		catch(Throwable t)
		{
			return 0.0;
		}
	}
	
	/** retrieve a particular .ini file entry as an integer
	*
	* <br><br><b>Usage:</b>  int i=propertyGetterOfInteger(p,"TAG");
	* @param tagToGet	the property tag to retreive.
	* @return int	the value of the .ini file tag
	*/
	public int getInt(String tagToGet)
	{
		try
		{
			return Integer.parseInt(getStr(tagToGet));
		}
		catch(Throwable t)
		{
			return 0;
		}
	}
	
    private static CMProps p()
    { 
    	char c=Thread.currentThread().getThreadGroup().getName().charAt(0); 
	    return props[c];
	}

    public static int getPKillLevelDiff(){return p().pkillLevelDiff;}

    public static String getVar(int varNum)
    {
        if((varNum<0)||(varNum>=NUM_SYSTEM)) return "";
        if(p().sysVars[varNum]==null) return "";
        return p().sysVars[varNum];
    }

    public static int getIntVar(int varNum)
    {
        if((varNum<0)||(varNum>=NUMI_SYSTEM)) return -1;
        if(p().sysInts[varNum]==null) return -1;
        return p().sysInts[varNum].intValue();
    }

    public static boolean getBoolVar(int varNum)
    {
        if((varNum<0)||(varNum>=NUMB_SYSTEM)) return false;
        if(p().sysBools[varNum]==null) return false;
        return p().sysBools[varNum].booleanValue();
    }

    public static void setBoolVar(int varNum, boolean val)
    {
        if((varNum<0)||(varNum>=NUMB_SYSTEM)) return ;
        p().sysBools[varNum]=new Boolean(val);
    }

    public static void setIntVar(int varNum, int val)
    {
        if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
        p().sysInts[varNum]=new Integer(val);
    }

    public static void setIntVar(int varNum, String val)
    {
        if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
        if(val==null) val="0";
        p().sysInts[varNum]=new Integer(CMath.s_int(val));
    }

    public static void setVar(int varNum, String val, boolean upperFy)
    {
        if(val==null) val="";
        setUpLowVar(varNum,upperFy?val.toUpperCase():val);
    }

    public static void setVar(int varNum, String val)
    {
        if(val==null) val="";
        setUpLowVar(varNum,val.toUpperCase());
    }

    public static void setUpLowVar(int varNum, String val)
    {
        if((varNum<0)||(varNum>=NUM_SYSTEM)) return ;
        if(val==null) val="";
        p().sysVars[varNum]=val;
        switch(varNum)
        {
        case SYSTEM_PKILL:
            {
                int x=val.indexOf("-");
                if(x>0)
                    p().pkillLevelDiff=CMath.s_int(val.substring(x+1));
            }
            break;
        }
    }

    public static int getCountNewUserByIP(String address)
    {
        int count=0;
        DVector DV=p().newusersByIP;
        synchronized(DV)
        {
            for(int i=DV.size()-1;i>=0;i--)
                if(((String)DV.elementAt(i,1)).equalsIgnoreCase(address))
                {
                    if(System.currentTimeMillis()>(((Long)DV.elementAt(i,2)).longValue()))
                    	DV.removeElementAt(i);
                    else
                        count++;
                }
        }
        return count;
    }
    public static void addNewUserByIP(String address)
    {
        DVector DV=p().newusersByIP;
        synchronized(DV)
        {
        	DV.addElement(address,new Long(System.currentTimeMillis()+TimeManager.MILI_DAY));
        }
    }
    
    public static int getMinManaException(String skillID)
    {
        DVector DV=p().skillMinManaExceptions;
    	int x=DV.indexOf(skillID.toUpperCase());
    	if(x<0) return Integer.MIN_VALUE;
    	return ((Integer)DV.elementAt(x,2)).intValue();
    }
    public static int getMaxManaException(String skillID)
    {
        DVector DV=p().skillMaxManaExceptions;
    	int x=DV.indexOf(skillID.toUpperCase());
    	if(x<0) return Integer.MIN_VALUE;
    	return ((Integer)DV.elementAt(x,2)).intValue();
    }

    private static int setExceptionSkillCosts(String val, DVector set)
    {
    	if(val==null) return 0;
    	set.clear();
    	Vector V=CMParms.parseCommas(val,true);
    	String s=null;
    	int endVal=0;
    	for(int v=0;v<V.size();v++)
    	{
    		s=(String)V.elementAt(v);
    		if(CMath.isInteger(s)){ endVal=CMath.s_int(s); continue;}
    		int x=s.indexOf(" ");
    		if(CMath.isInteger(s.substring(x+1).trim()))
    			set.addElement(s.substring(0,x).trim().toUpperCase(),new Integer(CMath.s_int(s.substring(x+1).trim())));
    	}
    	return endVal;
    }
    
    
    public void resetSystemVars()
    {
        setVar(SYSTEM_BADNAMES,getStr("BADNAMES"));
        setVar(SYSTEM_MULTICLASS,getStr("CLASSSYSTEM"));
        setVar(SYSTEM_PKILL,getStr("PLAYERKILL"));
        setVar(SYSTEM_PLAYERDEATH,getStr("PLAYERDEATH"));
        setVar(SYSTEM_MOBDEATH,getStr("MOBDEATH"));
        setVar(SYSTEM_PLAYERFLEE,getStr("FLEE"));
        setVar(SYSTEM_SHOWDAMAGE,getStr("SHOWDAMAGE"));
        setVar(SYSTEM_EMAILREQ,getStr("EMAILREQ"));
        setVar(SYSTEM_ESC0,getStr("ESCAPE0"));
        setVar(SYSTEM_ESC1,getStr("ESCAPE1"));
        setVar(SYSTEM_ESC2,getStr("ESCAPE2"));
        setVar(SYSTEM_ESC3,getStr("ESCAPE3"));
        setVar(SYSTEM_ESC4,getStr("ESCAPE4"));
        setVar(SYSTEM_ESC5,getStr("ESCAPE5"));
        setVar(SYSTEM_ESC6,getStr("ESCAPE6"));
        setVar(SYSTEM_ESC7,getStr("ESCAPE7"));
        setVar(SYSTEM_ESC8,getStr("ESCAPE8"));
        setVar(SYSTEM_ESC9,getStr("ESCAPE9"));
        setVar(SYSTEM_MSPPATH,getStr("SOUNDPATH"),false);
        setVar(SYSTEM_CLANVOTED,getStr("CLANVOTED"));
        setVar(SYSTEM_CLANVOTEO,getStr("CLANVOTEO"));
        setVar(SYSTEM_CLANVOTER,getStr("CLANVOTER"));
        setVar(SYSTEM_AUTOPURGE,getStr("AUTOPURGE"));
        setVar(SYSTEM_CORPSEGUARD,getStr("CORPSEGUARD"));
        setUpLowVar(SYSTEM_MUDDOMAIN,getStr("DOMAIN"));
        setVar(SYSTEM_I3EMAIL,getStr("I3EMAIL"));
        setUpLowVar(SYSTEM_I3ROUTERS,getStr("I3ROUTERS"));
        setVar(SYSTEM_PREJUDICE,getStr("PREJUDICE"));
        setVar(SYSTEM_IGNOREMASK,getStr("IGNOREMASK"));
        setVar(SYSTEM_BUDGET,getStr("BUDGET"));
        setVar(SYSTEM_DEVALUERATE,getStr("DEVALUERATE"));
        setVar(SYSTEM_INVRESETRATE,getStr("INVRESETRATE"));
        setVar(SYSTEM_EMOTEFILTER,getStr("EMOTEFILTER"));
        p().emoteFilter=CMParms.parse((getStr("EMOTEFILTER")).toUpperCase());
        setVar(SYSTEM_SAYFILTER,getStr("SAYFILTER"));
        p().sayFilter=CMParms.parse((getStr("SAYFILTER")).toUpperCase());
        setVar(SYSTEM_CHANNELFILTER,getStr("CHANNELFILTER"));
        p().channelFilter=CMParms.parse((getStr("CHANNELFILTER")).toUpperCase());
        setVar(SYSTEM_CLANTROPAREA,getStr("CLANTROPAREA"));
        setVar(SYSTEM_CLANTROPCP,getStr("CLANTROPCP"));
        setVar(SYSTEM_CLANTROPEXP,getStr("CLANTROPEXP"));
        setVar(SYSTEM_CLANTROPPK,getStr("CLANTROPPK"));
        setVar(SYSTEM_COLORSCHEME,getStr("COLORSCHEME"));
        setVar(SYSTEM_SMTPSERVERNAME,getStr("SMTPSERVERNAME"));
        setVar(SYSTEM_EXPCONTACTLINE,getStr("EXPCONTACTLINE"));
        setVar(SYSTEM_AUTOWEATHERPARMS,getStr("AUTOWEATHERPARMS"));
        setUpLowVar(SYSTEM_AUTOAREAPROPS,getStr("AUTOAREAPROPS"));
        setUpLowVar(SYSTEM_MXPIMAGEPATH,getStr("MXPIMAGEPATH"));
        setBoolVar(SYSTEMB_ACCOUNTEXPIRATION,getStr("ACCOUNTEXPIRATION").equalsIgnoreCase("YES")?true:false);
        setBoolVar(SYSTEMB_INTRODUCTIONSYSTEM,getStr("INTRODUCTIONSYSTEM").equalsIgnoreCase("YES")?true:false);
        setUpLowVar(SYSTEM_PREFACTIONS,getStr("FACTIONS"));
        
        if(CMLib.color()!=null) CMLib.color().clearLookups();
        if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
            setIntVar(SYSTEMI_MANACONSUMEAMT,-100);
        else
        if(getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
            setIntVar(SYSTEMI_MANACONSUMEAMT,-200);
        else
            setIntVar(SYSTEMI_MANACONSUMEAMT,CMath.s_int(getStr("MANACONSUMEAMT").trim()));
        String s=getStr("COMBATSYSTEM");
        if(s.equalsIgnoreCase("queue"))
            setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_QUEUE);
        else
        if(s.equalsIgnoreCase("manual"))
            setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_MANUAL);
        else
            setIntVar(SYSTEMI_COMBATSYSTEM,CombatLibrary.COMBAT_DEFAULT);
        s=getStr("EQVIEW");
        if(s.equalsIgnoreCase("paragraph"))
            setIntVar(SYSTEMI_EQVIEW,2);
        else
        if(s.equalsIgnoreCase("mixed"))
            setIntVar(SYSTEMI_EQVIEW,1);
        else
            setIntVar(SYSTEMI_EQVIEW,0);

        setIntVar(SYSTEMI_MANACONSUMETIME,getStr("MANACONSUMETIME"));
        setIntVar(SYSTEMI_PAGEBREAK,getStr("PAGEBREAK"));
        setIntVar(SYSTEMI_CLANENCHCOST,getStr("CLANENCHCOST"));
        setIntVar(SYSTEMI_FOLLOWLEVELDIFF,getStr("FOLLOWLEVELDIFF"));
        setIntVar(SYSTEMI_EXPRATE,getStr("EXPRATE"));
        setIntVar(SYSTEMI_SKYSIZE,getStr("SKYSIZE"));
        setIntVar(SYSTEMI_MAXSTAT,getStr("MAXSTATS"));
        if(getStr("BASEMAXSTAT").length()==0)
            setIntVar(SYSTEMI_BASEMAXSTAT,18);
        else
            setIntVar(SYSTEMI_BASEMAXSTAT,getStr("BASEMAXSTAT"));
        setIntVar(SYSTEMI_MANACOST,CMProps.setExceptionSkillCosts(getStr("MANACOST"),p().skillMaxManaExceptions));
        setIntVar(SYSTEMI_MANAMINCOST,CMProps.setExceptionSkillCosts(getStr("MANAMINCOST"),p().skillMinManaExceptions));
        setIntVar(SYSTEMI_EDITORTYPE,0);
        if(getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) setIntVar(SYSTEMI_EDITORTYPE,1);
        setIntVar(SYSTEMI_MINCLANMEMBERS,getStr("MINCLANMEMBERS"));
        setIntVar(SYSTEMI_CLANCOST,getStr("CLANCOST"));
        setIntVar(SYSTEMI_DAYSCLANDEATH,getStr("DAYSCLANDEATH"));
        setIntVar(SYSTEMI_MINCLANLEVEL,getStr("MINCLANLEVEL"));
        setIntVar(SYSTEMI_SKILLPRACCOST,getStr("SKILLPRACCOST"));
        setIntVar(SYSTEMI_SKILLTRAINCOST,getStr("SKILLTRAINCOST"));
        setIntVar(SYSTEMI_COMMONPRACCOST,getStr("COMMONPRACCOST"));
        setIntVar(SYSTEMI_COMMONTRAINCOST,getStr("COMMONTRAINCOST"));
        setIntVar(SYSTEMI_LANGPRACCOST,getStr("LANGPRACCOST"));
        setIntVar(SYSTEMI_LANGTRAINCOST,getStr("LANGTRAINCOST"));
        setIntVar(SYSTEMI_LASTPLAYERLEVEL,getStr("LASTPLAYERLEVEL"));
        setIntVar(SYSTEMI_JOURNALLIMIT,getStr("JOURNALLIMIT"));
        setIntVar(SYSTEMI_MUDTHEME,getStr("MUDTHEME"));
        setIntVar(SYSTEMI_TRIALDAYS,getStr("TRIALDAYS"));
        setIntVar(SYSTEMI_MAXCONNSPERIP,getStr("MAXCONNSPERIP"));
        setIntVar(SYSTEMI_MAXNEWPERIP,getStr("MAXNEWPERIP"));
        setIntVar(SYSTEMI_JSCRIPTS,getStr("JSCRIPTS"));
        setIntVar(SYSTEMI_DEFCMDTIME,(int)Math.round(getDouble("DEFCMDTIME")*100.0));
        setIntVar(SYSTEMI_DEFCOMCMDTIME,(int)Math.round(getDouble("DEFCOMCMDTIME")*100.0));
        setIntVar(SYSTEMI_DEFABLETIME,(int)Math.round(getDouble("DEFABLETIME")*100.0));
        setIntVar(SYSTEMI_DEFCOMABLETIME,(int)Math.round(getDouble("DEFCOMABLETIME")*100.0));
        
        Vector V=CMParms.parseCommas(getStr("INJURYSYSTEM"),true);
        
        if(V.size()>0) setIntVar(SYSTEMI_INJPCTCHANCE,CMath.s_int((String)V.elementAt(0)));
        else setIntVar(SYSTEMI_INJPCTCHANCE,100);
        if(V.size()>1) setIntVar(SYSTEMI_INJPCTHP,CMath.s_int((String)V.elementAt(1)));
        else setIntVar(SYSTEMI_INJPCTHP,40);
        if(V.size()>2) setIntVar(SYSTEMI_INJPCTHPAMP,CMath.s_int((String)V.elementAt(2)));
        else setIntVar(SYSTEMI_INJPCTHPAMP,10);
        if(V.size()>3) setIntVar(SYSTEMI_INJPCTCHANCEAMP,CMath.s_int((String)V.elementAt(3)));
        else setIntVar(SYSTEMI_INJPCTCHANCEAMP,100);
        if(V.size()>4) setIntVar(SYSTEMI_INJMULTIPLIER,CMath.s_int((String)V.elementAt(4)));
        else setIntVar(SYSTEMI_INJMULTIPLIER,4);
        if(V.size()>5) setIntVar(SYSTEMI_INJMINLEVEL,CMath.s_int((String)V.elementAt(5)));
        else setIntVar(SYSTEMI_INJMINLEVEL,10);
        if(V.size()>6) setIntVar(SYSTEMI_INJBLEEDMINLEVEL,CMath.s_int((String)V.elementAt(6)));
        else setIntVar(SYSTEMI_INJBLEEDMINLEVEL,15);
        if(V.size()>7) setIntVar(SYSTEMI_INJBLEEDPCTHP,CMath.s_int((String)V.elementAt(7)));
        else setIntVar(SYSTEMI_INJBLEEDPCTHP,20);
        if(V.size()>8) setIntVar(SYSTEMI_INJBLEEDPCTCHANCE,CMath.s_int((String)V.elementAt(8)));
        else setIntVar(SYSTEMI_INJBLEEDPCTCHANCE,100);
        
        String stateVar=getStr("STARTHP");
        if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
            setIntVar(SYSTEMI_STARTHP,CMath.s_int(stateVar));
        stateVar=getStr("STARTMANA");
        if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
            setIntVar(SYSTEMI_STARTMANA,CMath.s_int(stateVar));
        stateVar=getStr("STARTMOVE");
        if((stateVar.length()>0)&&(CMath.isNumber(stateVar)))
            setIntVar(SYSTEMI_STARTMOVE,CMath.s_int(stateVar));

        Directions.ReInitialize(getInt("DIRECTIONS"));

        String disable=getStr("DISABLE");
        if(getVar(SYSTEM_MULTICLASS).equalsIgnoreCase("DISABLED"))
            disable+=", CLASSES";
        CMSecurity.setDisableVars(disable);
        if(getStr("DISABLE").trim().length()>0)
            Log.sysOut("MUD","Disabled subsystems: "+getStr("DISABLE"));
        if(getStr("DEBUG").trim().length()>0)
        {
            Log.sysOut("MUD","Debugging messages: "+getStr("DEBUG"));
        	if(!Log.debugChannelOn())
                Log.errOut("MUD","Debug logging is disabled! Check your DBGMSGS flag!");
        }
        CMSecurity.setDebugVars(getStr("DEBUG"));
        CMSecurity.setSaveFlags(getStr("SAVE"));
    }

    public static String applyINIFilter(String msg, int whichFilter)
    {
        Vector filter=null;
        switch(whichFilter)
        {
        case SYSTEM_EMOTEFILTER: filter=p().emoteFilter; break;
        case SYSTEM_SAYFILTER: filter=p().sayFilter; break;
        case SYSTEM_CHANNELFILTER: filter=p().channelFilter; break;
        }
        if((filter==null)||(filter.size()==0))
            return msg;

        int fdex=0;
        int len=0;
        StringBuffer newMsg=null;
        String upp=msg.toUpperCase();
        String filterStr=null;
        final char[] filterPattern={'%','#','@','*','!','$','&','?'};
        int fpIndex=0;
        for(int f=0;f<filter.size();f++)
        {
        	filterStr=(String)filter.elementAt(f);
        	if(filterStr.length()==0) continue;
            fdex=upp.indexOf(filterStr);
            int ctr=0;
            while((fdex>=0)&&((++ctr)<999))
            {
                len=fdex+filterStr.length();
                if(((fdex==0)
                    ||(Character.isWhitespace(upp.charAt(fdex-1)))
                    ||((fdex>1)&&(upp.charAt(fdex-2)=='^')))
                &&((len==upp.length())
                    ||(!Character.isLetter(upp.charAt(len)))))
                {

                    for(;fdex<len;fdex++)
                        if(!Character.isWhitespace(msg.charAt(fdex)))
                        {
                            if(newMsg==null) newMsg=new StringBuffer(msg);
                            newMsg.setCharAt(fdex,filterPattern[fpIndex]);
                            if((++fpIndex)>=filterPattern.length) fpIndex=0;
                            upp=newMsg.toString().toUpperCase();
                        }
                        else
                            fpIndex=0;
                    fdex=upp.indexOf(filterStr);
                }
                else
                if(fdex<(filterStr.length()-1))
                	fdex=upp.indexOf(filterStr,fdex+1);
                else
                	fdex=-1;
            }
        }
        if(newMsg!=null) return newMsg.toString();
        return msg;
    }


    // this is the sound support method.
    // it builds a valid MSP sound code from built-in web server
    // info, and the info provided.
    public static String msp(String soundName, int volume, int priority)
    {
        if((soundName==null)||(soundName.length()==0)||CMSecurity.isDisabled("MSP")) return "";
        if(getVar(SYSTEM_MSPPATH).length()>0)
            return " !!SOUND("+soundName+" V="+volume+" P="+priority+" U="+getVar(SYSTEM_MSPPATH)+") ";
        return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
    }

    public static String mxpImagePath(String fileName)
    {
        if((fileName==null)||(fileName.trim().length()==0))
            return "";
        if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
        ||(CMSecurity.isDisabled("MXP")))
            return "";
        if(getVar(SYSTEM_MXPIMAGEPATH).endsWith("/"))
            return getVar(SYSTEM_MXPIMAGEPATH);
        return getVar(SYSTEM_MXPIMAGEPATH)+"/";
    }
    
    public static String mxpImage(Environmental E, String parms)
    {
        if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
        ||(CMSecurity.isDisabled("MXP")))
            return "";
        String image=E.image();
        if(image.length()==0) return "";
        String path=mxpImagePath(image);
        if(path.length()==0) return "";
        return "^<IMAGE '"+image+"' URL=\""+path+"\" "+parms+"^>^N";
    }
    
    public static String mxpImage(Environmental E, String parms, String pre, String post)
    {
        if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
        ||(CMSecurity.isDisabled("MXP")))
            return "";
        String image=E.image();
        if(image.length()==0) return "";
        String path=mxpImagePath(image);
        if(path.length()==0) return "";
        return pre+"^<IMAGE '"+image+"' URL=\""+path+"\" "+parms+"^>^N"+post;
    }
    
    public static String getHashedMXPImage(String key)
    {
        Hashtable H=(Hashtable)Resources.getResource("MXP_IMAGES");
        if(H==null) getDefaultMXPImage(null);
        H=(Hashtable)Resources.getResource("MXP_IMAGES");
        if(H==null) return "";
        return getHashedMXPImage(H,key);
        
    }
    public static String getHashedMXPImage(Hashtable H, String key)
    {
        if(H==null) return "";
        String s=(String)H.get(key);
        if(s==null) return null;
        if(s.trim().length()==0) return null;
        if(s.equalsIgnoreCase("NULL")) return "";
        return s;
    }
    
    public static String getDefaultMXPImage(Object O)
    {
        if((getVar(SYSTEM_MXPIMAGEPATH).length()==0)
        ||(CMSecurity.isDisabled("MXP")))
            return "";
        Hashtable H=(Hashtable)Resources.getResource("PARSED: mxp_images.ini");
        if(H==null)
        {
            H=new Hashtable();
            Vector V=Resources.getFileLineVector(new CMFile("resources/mxp_images.ini",null,false).text());
            if((V!=null)&&(V.size()>0))
            {
                String s=null;
                int x=0;
                for(int v=0;v<V.size();v++)
                {
                    s=((String)V.elementAt(v)).trim();
                    if(s.startsWith("//")||s.startsWith(";"))
                        continue;
                    x=s.indexOf("=");
                    if(x<0) continue;
                    if(s.substring(x+1).trim().length()>0)
                        H.put(s.substring(0,x),s.substring(x+1));
                }
            }
            Resources.submitResource("PARSED: mxp_images.ini",H);
        }
        String image=null;
        if(O instanceof Race)
        {
            image=getHashedMXPImage(H,"RACE_"+((Race)O).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"RACECAT_"+((Race)O).racialCategory().toUpperCase().replace(' ','_'));
            if(image==null) image=getHashedMXPImage(H,"RACE_*");
            if(image==null) image=getHashedMXPImage(H,"RACECAT_*");
        }
        else
        if(O instanceof MOB)
        {
            String raceName=((MOB)O).charStats().raceName().toUpperCase();
            Race R=null;
            for(Enumeration e=CMClass.races();e.hasMoreElements();)
            {
                R=(Race)e.nextElement();
                if(raceName.equalsIgnoreCase(R.name()))
                    image=getDefaultMXPImage(R);
            }
            if(image==null)
                image=getDefaultMXPImage(((MOB)O).charStats().getMyRace());
        }
        else
        if(O instanceof Room)
        {
            image=getHashedMXPImage(H,"ROOM_"+((Room)O).ID().toUpperCase());
            if(image==null)
                if(CMath.bset(((Room)O).domainType(),Room.INDOORS))
                    image=getHashedMXPImage(H,"LOCALE_INDOOR_"+Room.indoorDomainDescs[((Room)O).domainType()-Room.INDOORS]);
                else
                    image=getHashedMXPImage(H,"LOCALE_"+Room.outdoorDomainDescs[((Room)O).domainType()]);
            if(image==null) image=getHashedMXPImage(H,"ROOM_*");
            if(image==null) image=getHashedMXPImage(H,"LOCALE_*");
        }
        else
        if(O instanceof Exit)
        {
            image=getHashedMXPImage(H,"EXIT_"+((Exit)O).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"EXIT_"+((Exit)O).doorName().toUpperCase());
            if(image==null)
                if(((Exit)O).hasADoor())
                    image=getHashedMXPImage(H,"EXIT_WITHDOOR");
                else
                    image=getHashedMXPImage(H,"EXIT_OPEN");
            if(image==null) image=getHashedMXPImage(H,"EXIT_*");
        }
        else
        if(O instanceof Rideable)
        {
            image=getHashedMXPImage(H,"RIDEABLE_"+Rideable.RIDEABLE_DESCS[((Rideable)O).rideBasis()]);
            if(image==null) image=getHashedMXPImage(H,"RIDEABLE_*");
        }
        else
        if(O instanceof Shield)
        {
            image=getHashedMXPImage(H,"SHIELD_"+RawMaterial.MATERIAL_DESCS[(((Shield)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"SHIELD_*");
        }
        else
        if(O instanceof Coins)
        {
            image=getHashedMXPImage(H,"COINS_"+RawMaterial.RESOURCE_DESCS[(((Coins)O).material()&RawMaterial.RESOURCE_MASK)]);
            if(image==null) image=getHashedMXPImage(H,"COINS_*");
        }
        else
        if(O instanceof Ammunition)
        {
            image=getHashedMXPImage(H,"AMMO_"+((Ammunition)O).ammunitionType().toUpperCase().replace(' ','_'));
            if(image==null) image=getHashedMXPImage(H,"AMMO_*");
        }
        else
        if(O instanceof CagedAnimal)
        {
            MOB mob=((CagedAnimal)O).unCageMe();
            return getDefaultMXPImage(mob);
        }
        else
        if(O instanceof ClanItem)
        {
            image=getHashedMXPImage(H,"CLAN_"+((ClanItem)O).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"CLAN_"+ClanItem.CI_DESC[((ClanItem)O).ciType()].toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"CLAN_*");
        }
        else
        if(O instanceof DeadBody)
        {
            Race R=((DeadBody)O).charStats().getMyRace();
            if(R!=null)
            {
                image=getHashedMXPImage(H,"CORPSE_"+R.ID().toUpperCase());
                if(image==null) image=getHashedMXPImage(H,"CORPSECAT_"+R.racialCategory().toUpperCase().replace(' ','_'));
            }
            if(image==null) image=getHashedMXPImage(H,"CORPSE_*");
            if(image==null) image=getHashedMXPImage(H,"CORPSECAT_*");
        }
        else
        if(O instanceof RawMaterial)
        {
            image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.RESOURCE_DESCS[(((RawMaterial)O).material()&RawMaterial.RESOURCE_MASK)]);
            image=getHashedMXPImage(H,"RESOURCE_"+RawMaterial.MATERIAL_DESCS[(((RawMaterial)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"RESOURCE_*");
        }
        else
        if(O instanceof Key)
        {
            image=getHashedMXPImage(H,"KEY_"+RawMaterial.RESOURCE_DESCS[(((Key)O).material()&RawMaterial.RESOURCE_MASK)]);
            image=getHashedMXPImage(H,"KEY_"+RawMaterial.MATERIAL_DESCS[(((Key)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"KEY_*");
        }
        else
        if(O instanceof LandTitle)
            image=getHashedMXPImage(H,"ITEM_LANDTITLE");
        else
        if(O instanceof MagicDust)
        {
            Vector V=((MagicDust)O).getSpells();
            if(V.size()>0)
                image=getHashedMXPImage(H,"DUST_"+((Ability)V.firstElement()).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"DUST_*");
        }
        else
        if(O instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
            image=getHashedMXPImage(H,"ITEM_MAP");
        else
        if(O instanceof MusicalInstrument)
        {
            image=getHashedMXPImage(H,"MUSINSTR_"+MusicalInstrument.TYPE_DESC[((MusicalInstrument)O).instrumentType()]);
            if(image==null) image=getHashedMXPImage(H,"MUSINSTR_*");
        }
        else
        if(O instanceof PackagedItems)
            image=getHashedMXPImage(H,"ITEM_PACKAGED");
        else
        if(O instanceof Perfume)
            image=getHashedMXPImage(H,"ITEM_PERFUME");
        else
        if(O instanceof Pill)
        {
            Vector V=((Pill)O).getSpells();
            if(V.size()>0)
                image=getHashedMXPImage(H,"PILL_"+((Ability)V.firstElement()).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"PILL_*");
        }
        else
        if(O instanceof Potion)
        {
            Vector V=((Potion)O).getSpells();
            if(V.size()>0)
                image=getHashedMXPImage(H,"POTION_"+((Ability)V.firstElement()).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"POTION_*");
        }
        else
        if(O instanceof Recipe)
            image=getHashedMXPImage(H,"ITEM_RECIPE");
        else
        if(O instanceof Scroll)
        {
            Vector V=((Scroll)O).getSpells();
            if(V.size()>0)
                image=getHashedMXPImage(H,"SCROLL_"+((Ability)V.firstElement()).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"SCROLL_*");
        }
        else
        if(O instanceof ShipComponent)
        {
            if(H.containsKey("SHIPCOMP_"+((ShipComponent)O).ID().toUpperCase()))
                image=getHashedMXPImage(H,"SHIPCOMP_"+((ShipComponent)O).ID().toUpperCase());
            else
            if(O instanceof ShipComponent.ShipEngine)
                image=getHashedMXPImage(H,"SHIPCOMP_ENGINE");
            else
            if(O instanceof ShipComponent.ShipEnviroControl)
                image=getHashedMXPImage(H,"SHIPCOMP_ENVIRO");
            else
            if(O instanceof ShipComponent.ShipPanel)
                image=getHashedMXPImage(H,"SHIPCOMP_PANEL");
            else
            if(O instanceof ShipComponent.ShipPowerSource)
                image=getHashedMXPImage(H,"SHIPCOMP_POWER");
            else
            if(O instanceof ShipComponent.ShipSensor)
                image=getHashedMXPImage(H,"SHIPCOMP_SENSOR");
            else
            if(O instanceof ShipComponent.ShipWeapon)
                image=getHashedMXPImage(H,"SHIPCOMP_WEAPON");
            if(image==null) image=getHashedMXPImage(H,"SHIPCOMP_*");
        }
        else
        if(O instanceof Software)
            image=getHashedMXPImage(H,"ITEM_SOFTWARE");
        else
        if(O instanceof Armor)
        {
            Armor A=(Armor)O;
            final long[] bits=
            {Item.WORN_TORSO, Item.WORN_FEET, Item.WORN_LEGS, Item.WORN_HANDS, Item.WORN_ARMS,
             Item.WORN_HEAD, Item.WORN_EARS, Item.WORN_EYES, Item.WORN_MOUTH, Item.WORN_NECK,
             Item.WORN_LEFT_FINGER, Item.WORN_LEFT_WRIST, Item.WORN_BACK, Item.WORN_WAIST,
             Item.WORN_ABOUT_BODY, Item.WORN_FLOATING_NEARBY, Item.WORN_HELD, Item.WORN_WIELD};
            final String[] bitdesc=
            {"TORSO","FEET","LEGS","HANDS","ARMS","HEAD","EARS","EYES","MOUTH",
             "NECK","FINGERS","WRIST","BACK","WAIST","BODY","FLOATER","HELD","WIELDED"};
            for(int i=0;i<bits.length;i++)
                if(CMath.bset(A.rawProperLocationBitmap(),bits[i]))
                {
                    image=getHashedMXPImage(H,"ARMOR_"+bitdesc[i]);
                    break;
                }
            if(image==null) image=getHashedMXPImage(H,"ARMOR_*");
        }
        else
        if(O instanceof Weapon)
        {
            image=getHashedMXPImage(H,"WEAPON_"+Weapon.classifictionDescription[((Weapon)O).weaponClassification()]);
            if(image==null) image=getHashedMXPImage(H,"WEAPON_"+Weapon.typeDescription[((Weapon)O).weaponType()]);
            if(image==null) image=getHashedMXPImage(H,"WEAPON_"+((Weapon)O).ammunitionType().toUpperCase().replace(' ','_'));
            if(image==null) image=getHashedMXPImage(H,"WEAPON_*");
        }
        else
        if(O instanceof Wand)
        {
            image=getHashedMXPImage(H,"WAND_"+((Wand)O).ID().toUpperCase());
            if(image==null)
            {
                Ability A=((Wand)O).getSpell();
                if(A!=null) image=getHashedMXPImage(H,"WAND_"+A.ID().toUpperCase());
            }
            if(image==null) image=getHashedMXPImage(H,"WAND_*");
        }
        else
        if(O instanceof Food)
        {
            image=getHashedMXPImage(H,"FOOD_"+((Food)O).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"FOOD_"+RawMaterial.RESOURCE_DESCS[(((Food)O).material()&RawMaterial.RESOURCE_MASK)]);
            if(image==null) image=getHashedMXPImage(H,"FOOD_"+RawMaterial.MATERIAL_DESCS[(((Food)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"FOOD_*");
        }
        else
        if(O instanceof Drink)
        {
            image=getHashedMXPImage(H,"DRINK_"+((Drink)O).ID().toUpperCase());
            if(image==null) image=getHashedMXPImage(H,"DRINK_"+RawMaterial.RESOURCE_DESCS[(((Item)O).material()&RawMaterial.RESOURCE_MASK)]);
            if(image==null) image=getHashedMXPImage(H,"DRINK_"+RawMaterial.MATERIAL_DESCS[(((Item)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"DRINK_*");
        }
        else
        if(O instanceof Light)
        {
            image=getHashedMXPImage(H,"LIGHT_"+((Light)O).ID().toUpperCase());
            image=getHashedMXPImage(H,"LIGHT_"+RawMaterial.MATERIAL_DESCS[(((Light)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"LIGHT_*");
        }
        else
        if(O instanceof Container)
        {
            image=getHashedMXPImage(H,"CONTAINER_"+((Container)O).ID().toUpperCase());
            String lid=((Container)O).hasALid()?"LID_":"";
            if(image==null) image=getHashedMXPImage(H,"CONTAINER_"+lid+RawMaterial.MATERIAL_DESCS[(((Container)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"CONTAINER_"+lid+"*");
        }
        else
        if(O instanceof Electronics)
            image=getHashedMXPImage(H,"ITEM_ELECTRONICS");
        else
        if(O instanceof MiscMagic)
            if(image==null) image=getHashedMXPImage(H,"ITEM_MISCMAGIC");
        if((image==null)&&(O instanceof Item))
        {
            if(image==null) image=getHashedMXPImage(H,"ITEM_"+((Item)O).ID().toUpperCase());
            image=getHashedMXPImage(H,"ITEM_"+RawMaterial.RESOURCE_DESCS[(((Item)O).material()&RawMaterial.RESOURCE_MASK)]);
            image=getHashedMXPImage(H,"ITEM_"+RawMaterial.MATERIAL_DESCS[(((Item)O).material()&RawMaterial.MATERIAL_MASK)>>8]);
            if(image==null) image=getHashedMXPImage(H,"ITEM_*");
        }
        if(image==null) image=getHashedMXPImage(H,"*");
        if(image==null) return "";
        return image;
    }

    public static String msp(String soundName, int priority)
    { return msp(soundName,50,CMLib.dice().roll(1,50,priority));}
    
    public static boolean isTheme(int i)
    {
        return (getIntVar(SYSTEMI_MUDTHEME)&i)>0;
    }

    public static Vector loadEnumerablePage(String iniFile)
    {
        StringBuffer str=new CMFile(iniFile,null,true).text();
        if((str==null)||(str.length()==0)) return new Vector();
        Vector page=Resources.getFileLineVector(str);
        for(int p=0;p<(page.size()-1);p++)
        {
            String s=((String)page.elementAt(p)).trim();
            if(s.startsWith("#")||s.startsWith("!")) continue;
            if((s.endsWith("\\"))&&(!s.endsWith("\\\\")))
            {
                s=s.substring(0,s.length()-1)+((String)page.elementAt(p+1)).trim();
                page.removeElementAt(p+1);
                page.setElementAt(s,p);
                p=p-1;
            }
        }
        return page;
    }

    
}
