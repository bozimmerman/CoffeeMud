package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class CommonStrings extends Scriptable
{
	private CommonStrings(){};
	
	public static String[] clookup=null;
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
	public static final int SYSTEM_WIZINFONAMES=38;
	public static final int SYSTEM_MAILBOX=39;
	public static final int SYSTEM_CLANTROPCP=40;
	public static final int SYSTEM_CLANTROPEXP=41;
	public static final int SYSTEM_CLANTROPAREA=42;
	public static final int NUM_SYSTEM=43;

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
	public static final int SYSTEMI_MINMOVETIME=21;
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
	public static final int NUMI_SYSTEM=35;

	public static final int SYSTEMB_MOBCOMPRESS=0;
	public static final int SYSTEMB_ITEMDCOMPRESS=1;
	public static final int SYSTEMB_ROOMDCOMPRESS=2;
	public static final int SYSTEMB_MOBDCOMPRESS=3;
	public static final int SYSTEMB_MUDSTARTED=4;
	public static final int SYSTEMB_EMAILFORWARDING=5;
	public static final int SYSTEMB_MOBNOCACHE=6;
	public static final int SYSTEMB_ROOMDNOCACHE=7;
	public static final int NUMB_SYSTEM=8;

	private static String[] sysVars=new String[NUM_SYSTEM];
	private static Integer[] sysInts=new Integer[NUMI_SYSTEM];
	private static Boolean[] sysBools=new Boolean[NUMB_SYSTEM];
	private static Vector sayFilter=new Vector();
	private static Vector channelFilter=new Vector();
	private static Vector emoteFilter=new Vector();
	
	public static int pkillLevelDiff=26;

	public static int getPKillLevelDiff(){return pkillLevelDiff;}

	public static String getVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUM_SYSTEM)) return "";
		if(sysVars[varNum]==null) return "";
		return sysVars[varNum];
	}

	public static int getIntVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return -1;
		if(sysInts[varNum]==null) return -1;
		return sysInts[varNum].intValue();
	}

	public static boolean getBoolVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return false;
		if(sysBools[varNum]==null) return false;
		return sysBools[varNum].booleanValue();
	}

	public static void setBoolVar(int varNum, boolean val)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return ;
		sysBools[varNum]=new Boolean(val);
	}

	public static void setIntVar(int varNum, int val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		sysInts[varNum]=new Integer(val);
	}

	public static void setIntVar(int varNum, String val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		if(val==null) val="0";
		sysInts[varNum]=new Integer(Util.s_int(val));
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
		sysVars[varNum]=val;
		switch(varNum)
		{
		case SYSTEM_PKILL:
			{
				int x=val.indexOf("-");
				if(x>0)
					pkillLevelDiff=Util.s_int(val.substring(x+1));
			}
			break;
		}
	}
	
	public static void loadCommonINISettings(INI page)
	{
		setVar(SYSTEM_BADNAMES,page.getStr("BADNAMES"));
		setVar(SYSTEM_MULTICLASS,page.getStr("CLASSSYSTEM"));
		setVar(SYSTEM_PKILL,page.getStr("PLAYERKILL"));
		setVar(SYSTEM_PLAYERDEATH,page.getStr("PLAYERDEATH"));
		setVar(SYSTEM_PLAYERFLEE,page.getStr("FLEE"));
		setVar(SYSTEM_SHOWDAMAGE,page.getStr("SHOWDAMAGE"));
		setVar(SYSTEM_EMAILREQ,page.getStr("EMAILREQ"));
		setVar(SYSTEM_ESC0,page.getStr("ESCAPE0"));
		setVar(SYSTEM_ESC1,page.getStr("ESCAPE1"));
		setVar(SYSTEM_ESC2,page.getStr("ESCAPE2"));
		setVar(SYSTEM_ESC3,page.getStr("ESCAPE3"));
		setVar(SYSTEM_ESC4,page.getStr("ESCAPE4"));
		setVar(SYSTEM_ESC5,page.getStr("ESCAPE5"));
		setVar(SYSTEM_ESC6,page.getStr("ESCAPE6"));
		setVar(SYSTEM_ESC7,page.getStr("ESCAPE7"));
		setVar(SYSTEM_ESC8,page.getStr("ESCAPE8"));
		setVar(SYSTEM_ESC9,page.getStr("ESCAPE9"));
		setVar(SYSTEM_MSPPATH,page.getStr("SOUNDPATH"));
		setVar(SYSTEM_CLANVOTED,page.getStr("CLANVOTED"));
		setVar(SYSTEM_CLANVOTEO,page.getStr("CLANVOTEO"));
		setVar(SYSTEM_CLANVOTER,page.getStr("CLANVOTER"));
		setVar(SYSTEM_AUTOPURGE,page.getStr("AUTOPURGE"));
        setVar(SYSTEM_CORPSEGUARD,page.getStr("CORPSEGUARD"));
		setVar(SYSTEM_MUDDOMAIN,page.getStr("DOMAIN"));
		setVar(SYSTEM_I3EMAIL,page.getStr("I3EMAIL"));
		setVar(SYSTEM_PREJUDICE,page.getStr("PREJUDICE"));
		setVar(SYSTEM_BUDGET,page.getStr("BUDGET"));
		setVar(SYSTEM_DEVALUERATE,page.getStr("DEVALUERATE"));
		setVar(SYSTEM_INVRESETRATE,page.getStr("INVRESETRATE"));
		setVar(SYSTEM_EMOTEFILTER,page.getStr("EMOTEFILTER"));
		emoteFilter=Util.parse((page.getStr("EMOTEFILTER")).toUpperCase());
		setVar(SYSTEM_SAYFILTER,page.getStr("SAYFILTER"));
		sayFilter=Util.parse((page.getStr("SAYFILTER")).toUpperCase());
		setVar(SYSTEM_CHANNELFILTER,page.getStr("CHANNELFILTER"));
		channelFilter=Util.parse((page.getStr("CHANNELFILTER")).toUpperCase());
		setVar(SYSTEM_WIZINFONAMES,page.getStr("WIZINFONAMES"));
		setVar(SYSTEM_CLANTROPAREA,page.getStr("CLANTROPAREA"));
		setVar(SYSTEM_CLANTROPCP,page.getStr("CLANTROPCP"));
		setVar(SYSTEM_CLANTROPEXP,page.getStr("CLANTROPEXP"));
		
		if(page.getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("LEVEL"))
			setIntVar(SYSTEMI_MANACONSUMEAMT,-100);
		else
		if(page.getStr("MANACONSUMEAMT").trim().equalsIgnoreCase("SPELLLEVEL"))
			setIntVar(SYSTEMI_MANACONSUMEAMT,-200);
		else
			setIntVar(SYSTEMI_MANACONSUMEAMT,Util.s_int(page.getStr("MANACONSUMEAMT").trim()));
		String s=page.getStr("COMBATSYSTEM");
		if(s.equalsIgnoreCase("queue")) 
			setIntVar(SYSTEMI_COMBATSYSTEM,1);
		else
			setIntVar(SYSTEMI_COMBATSYSTEM,0);
		
		setIntVar(SYSTEMI_MANACONSUMETIME,page.getStr("MANACONSUMETIME"));
		setIntVar(SYSTEMI_PAGEBREAK,page.getStr("PAGEBREAK"));
		setIntVar(SYSTEMI_MINMOVETIME,page.getStr("MINMOVETIME"));
		setIntVar(SYSTEMI_CLANENCHCOST,page.getStr("CLANENCHCOST"));
		setIntVar(SYSTEMI_FOLLOWLEVELDIFF,page.getStr("FOLLOWLEVELDIFF"));
		setIntVar(SYSTEMI_EXPRATE,page.getStr("EXPRATE"));
		setIntVar(SYSTEMI_SKYSIZE,page.getStr("SKYSIZE"));
		setIntVar(SYSTEMI_MAXSTAT,page.getStr("MAXSTATS"));
		if(page.getStr("BASEMAXSTAT").length()==0)
			setIntVar(SYSTEMI_BASEMAXSTAT,18);
		else
			setIntVar(SYSTEMI_BASEMAXSTAT,page.getStr("BASEMAXSTAT"));
		setIntVar(SYSTEMI_MANACOST,page.getStr("MANACOST"));
		setIntVar(SYSTEMI_MANAMINCOST,page.getStr("MANAMINCOST"));
		setIntVar(SYSTEMI_EDITORTYPE,0);
		if(page.getStr("EDITORTYPE").equalsIgnoreCase("WIZARD")) setIntVar(SYSTEMI_EDITORTYPE,1);
		setIntVar(SYSTEMI_MINCLANMEMBERS,page.getStr("MINCLANMEMBERS"));
		setIntVar(SYSTEMI_CLANCOST,page.getStr("CLANCOST"));
		setIntVar(SYSTEMI_DAYSCLANDEATH,page.getStr("DAYSCLANDEATH"));
		setIntVar(SYSTEMI_MINCLANLEVEL,page.getStr("MINCLANLEVEL"));
		setIntVar(SYSTEMI_SKILLPRACCOST,page.getStr("SKILLPRACCOST"));
		setIntVar(SYSTEMI_SKILLTRAINCOST,page.getStr("SKILLTRAINCOST"));
		setIntVar(SYSTEMI_COMMONPRACCOST,page.getStr("COMMONPRACCOST"));
		setIntVar(SYSTEMI_COMMONTRAINCOST,page.getStr("COMMONTRAINCOST"));
		setIntVar(SYSTEMI_LANGPRACCOST,page.getStr("LANGPRACCOST"));
		setIntVar(SYSTEMI_LANGTRAINCOST,page.getStr("LANGTRAINCOST"));
		setIntVar(SYSTEMI_LASTPLAYERLEVEL,page.getStr("LASTPLAYERLEVEL"));
		setIntVar(SYSTEMI_JOURNALLIMIT,page.getStr("JOURNALLIMIT"));
		setIntVar(SYSTEMI_MUDTHEME,page.getStr("MUDTHEME"));
		Vector V=Util.parseCommas(page.getStr("INJURYSYSTEM"),true);
		if(V.size()!=5)
		{
		    CommonStrings.setIntVar(SYSTEMI_INJPCTCHANCE,100);
		    CommonStrings.setIntVar(SYSTEMI_INJPCTHP,40);
		    CommonStrings.setIntVar(SYSTEMI_INJPCTHPAMP,10);
		    CommonStrings.setIntVar(SYSTEMI_INJPCTCHANCEAMP,100);
		    CommonStrings.setIntVar(SYSTEMI_INJMULTIPLIER,4);
		}
		else
		{
		    CommonStrings.setIntVar(SYSTEMI_INJPCTCHANCE,Util.s_int((String)V.elementAt(0)));
		    CommonStrings.setIntVar(SYSTEMI_INJPCTHP,Util.s_int((String)V.elementAt(1)));
		    CommonStrings.setIntVar(SYSTEMI_INJPCTHPAMP,Util.s_int((String)V.elementAt(2)));
		    CommonStrings.setIntVar(SYSTEMI_INJPCTCHANCEAMP,Util.s_int((String)V.elementAt(3)));
		    CommonStrings.setIntVar(SYSTEMI_INJMULTIPLIER,Util.s_int((String)V.elementAt(4)));
		}
		
		Directions.ReInitialize(page.getInt("DIRECTIONS"));
		
		if(Util.s_int(page.getStr("HOURSINDAY"))>0)
			DefaultTimeClock.globalClock.setHoursInDay(Util.s_int(page.getStr("HOURSINDAY")));
		
		if(Util.s_int(page.getStr("DAYSINMONTH"))>0)
			DefaultTimeClock.globalClock.setDaysInMonth(Util.s_int(page.getStr("DAYSINMONTH")));
		
		String monthsInYear=page.getStr("MONTHSINYEAR");
		if(monthsInYear.trim().length()>0)
			DefaultTimeClock.globalClock.setMonthsInYear(Util.toStringArray(Util.parseCommas(monthsInYear,true)));
		
		DefaultTimeClock.globalClock.setDaysInWeek(Util.toStringArray(Util.parseCommas(page.getStr("DAYSINWEEK"),true)));
		
		if(page.containsKey("YEARDESC"))
			DefaultTimeClock.globalClock.setYearNames(Util.toStringArray(Util.parseCommas(page.getStr("YEARDESC"),true)));
		
		if(page.containsKey("DAWNHR")&&page.containsKey("DAYHR")
			&&page.containsKey("DUSKHR")&&page.containsKey("NIGHTHR"))
		DefaultTimeClock.globalClock.setDawnToDusk(
									Util.s_int(page.getStr("DAWNHR")),
									Util.s_int(page.getStr("DAYHR")),
									Util.s_int(page.getStr("DUSKHR")),
									Util.s_int(page.getStr("NIGHTHR")));
		
		setIntVar(SYSTEMI_TICKSPERMUDDAY,""+((MudHost.TIME_UTILTHREAD_SLEEP*DefaultTimeClock.globalClock.getHoursInDay()/MudHost.TICK_TIME)));
		setIntVar(SYSTEMI_TICKSPERMUDMONTH,""+""+((MudHost.TIME_UTILTHREAD_SLEEP*DefaultTimeClock.globalClock.getHoursInDay()*DefaultTimeClock.globalClock.getDaysInMonth()/MudHost.TICK_TIME)));
		CMSecurity.setDisableVars(page.getStr("DISABLE"));
		if(page.getStr("DISABLE").trim().length()>0)
			Log.sysOut("MUD","Disabled subsystems: "+page.getStr("DISABLE"));
		CMSecurity.setDebugVars(page.getStr("DEBUG"));
		
	}

	public static String[] standardColorLookups()
	{
		if(clookup==null)
		{
			clookup=new String[256];
			// N B H - normal bold highlight underline flash italic
			clookup['N']="^w\033[0m";
			clookup['!']="\033[1m";
			clookup['H']="^c";
			clookup['_']="\033[4m";
			clookup['*']="\033[5m";
			clookup['/']="\033[6m";
			// reset
			clookup['.']="\033[0m";
			clookup['^']="^";
			// F S - fight spell
			clookup['F']="^r";
			clookup['S']="^y";
			// E T Q - emote talk channeltalk channelfore
			clookup['E']="^p";
			clookup['T']="^b";
			clookup['t']="^C";
			clookup['Q']="\033[0;36;44m";
			clookup['q']="^c";
			
			// MXP - tag codes.
			clookup['<']="<";
			clookup['"']="\"";
			clookup['>']=">";
			clookup['&']="&";
			
			// X Y Z - important messages
			clookup['x']="\033[1;36;44m";
			clookup['X']="\033[1;33;44m";
			clookup['Z']="\033[1;33;41m";
			//  R L D d - roomtitle roomdesc(look) Direction door
			clookup['O']="^c";
			clookup['L']="^w";
			clookup['D']="\033[1;36;44m";
			clookup['d']="^b";
			// I M - item, mob
			clookup['I']="^g";
			clookup['M']="^p";

			// h m v - prompt colors - deprecated!!
			clookup['h']="^c";
			clookup['m']="^c";
			clookup['v']="^c";

			// fixed system colors, 1= bright, 0=dark
			clookup['w']="\033[1;37m";
			clookup['g']="\033[1;32m";
			clookup['b']="\033[1;34m";
			clookup['r']="\033[1;31m";
			clookup['y']="\033[1;33m";
			clookup['c']="\033[1;36m";
			clookup['p']="\033[1;35m";
			clookup['W']="\033[0;37m";
			clookup['G']="\033[0;32m";
			clookup['B']="\033[0;34m";
			clookup['R']="\033[0;31m";
			clookup['Y']="\033[0;33m";
			clookup['C']="\033[0;36m";
			clookup['P']="\033[0;35m";
			for(int i=0;i<clookup.length;i++)
			{
				String s=clookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					clookup[i]=clookup[s.charAt(1)];
			}
		}
		return clookup;
	}


	public static String standardHitWord(int type, int damage)
	{
		if(type<0) type=Weapon.TYPE_BURSTING;
		int damnCode=0;
		if(damage<=0) return "annoy(s)";
		else if(damage<=3) damnCode=0; //3
		else if(damage<=6) damnCode=1; //4
		else if(damage<=10) damnCode=2;//5
		else if(damage<=15) damnCode=3; //10
		else if(damage<=25) damnCode=4; //10
		else if(damage<=35) damnCode=5; //15
		else if(damage<=50) damnCode=6; //20
		else if(damage<=70) damnCode=7; //30
		else if(damage<=100) damnCode=8; //30
		else if(damage<=130) damnCode=9; //35
		else if(damage<=165) damnCode=10; //50
		else if(damage<=215) damnCode=11; //75
		else if(damage<=295) damnCode=12; //100
		else if(damage<=395) damnCode=13;
		else damnCode=14;

		switch(damnCode)
		{
			case 7: return "massacre(s)";
			case 8: return "MASSACRE(S)";
			case 9: return "destroy(s)";
			case 10: return "DESTROY(S)";
			case 11: return "obliterate(s)";
			case 12: return "OBLITERATE(S)";
			case 13: return "**OBLITERATE(S)**";
			case 14: return "--==::OBLITERATE(S)::==--";
		default:
			break;
		}
		switch(type)
		{
		case Weapon.TYPE_NATURAL:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "cut(s)";
			case 4: return "hurt(s)";
			case 5: return "rip(s)";
			case 6: return "crunch(es)";
			}
			break;
		case Weapon.TYPE_SLASHING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "wound(s)";
			case 3: return "cut(s)";
			case 4: return "slice(s)";
			case 5: return "gut(s)";
			case 6: return "murder(s)";
			}
			break;
		case Weapon.TYPE_PIERCING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "prick(s)";
			case 3: return "cut(s)";
			case 4: return "stab(s)";
			case 5: return "pierce(s)";
			case 6: return "murder(s)";
			}
			break;
		case Weapon.TYPE_BASHING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "smash(es)";
			case 4: return "bash(es)";
			case 5: return "crush(es)";
			case 6: return "crunch(es)";
			}
			break;
		case Weapon.TYPE_BURNING:
			switch(damnCode)
			{
			case 0: return "warm(s)";
			case 1: return "heat(s)";
			case 2: return "singe(s)";
			case 3: return "burn(s)";
			case 4: return "flame(s)";
			case 5: return "scorch(es)";
			case 6: return "incinerate(s)";
			}
			break;
		case Weapon.TYPE_SHOOT:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "pierce(s)";
			case 4: return "pierce(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			}
			break;
		case Weapon.TYPE_FROSTING:
			switch(damnCode)
			{
			case 0: return "chill(s)";
			case 1: return "cool(s)";
			case 2: return "ice(s)";
			case 3: return "frost(s)";
			case 4: return "blister(s)";
			case 5: return "blast(s)";
			case 6: return "incinerate(s)";
			}
			break;
		case Weapon.TYPE_GASSING:
			switch(damnCode)
			{
			case 0: return "mist(s)";
			case 1: return "gass(es)";
			case 2: return "gass(es)";
			case 3: return "fume(s)";
			case 4: return "choke(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			}
			break;
		case Weapon.TYPE_MELTING:
			switch(damnCode)
			{
			case 0: return "sting(s)";
			case 1: return "sizzle(s)";
			case 2: return "burn(s)";
			case 3: return "scorch(es)";
			case 4: return "dissolve(s)";
			case 5: return "melt(s)";
			case 6: return "melt(s)";
			}
			break;
		case Weapon.TYPE_STRIKING:
			switch(damnCode)
			{
			case 0: return "sting(s)";
			case 1: return "charge(s)";
			case 2: return "singe(s)";
			case 3: return "burn(s)";
			case 4: return "scorch(es)";
			case 5: return "blast(s)";
			case 6: return "incinerate(s)";
			}
			break;
		case Weapon.TYPE_BURSTING:
		default:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "wound(s)";
			case 3: return "cut(s)";
			case 4: return "damage(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			}
			break;
		}
		return "";
	}

	public static final int ARMOR_CEILING=500;
	public static final int ATTACK_CEILING=1000;

	public static String armorStr(int armor){
		return (armor<0)?armorStrs[0]:(
			   (armor>=ARMOR_CEILING)?armorStrs[armorStrs.length-1]+(Util.repeat("!",(armor-ARMOR_CEILING)/100))+" ("+armor+")":(
				armorStrs[(int)Math.round(Math.floor(Util.mul(Util.div(armor,ARMOR_CEILING),armorStrs.length)))]+" ("+armor+")"));}
	public static String fightingProwessStr(int prowess){
		return (prowess<0)?fightStrs[0]:(
			   (prowess>=ATTACK_CEILING)?fightStrs[fightStrs.length-1]+(Util.repeat("!",(prowess-ATTACK_CEILING)/100))+" ("+prowess+")":(
				fightStrs[(int)Math.round(Math.floor(Util.mul(Util.div(prowess,ATTACK_CEILING),fightStrs.length)))]+" ("+prowess+")"));}
	public static String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
	{
		int dex=3;
		switch(weaponClassification)
		{
		case Weapon.CLASS_RANGED: dex=0; break;
		case Weapon.CLASS_THROWN: dex=1; break;
		default:
			switch(weaponType)
			{
			case Weapon.TYPE_SLASHING:
			case Weapon.TYPE_BASHING:
				dex=2; break;
			case Weapon.TYPE_PIERCING:
				dex=4; break;
			case Weapon.TYPE_SHOOT:
				dex=0; break;
			default:
				dex=3;
				break;
			}
			break;
		}
		if(!useExtendedMissString) return missStrs2[dex];
		String str=missStrs1[dex];
		int dexTool=str.indexOf("<TOOLNAME>");
		return str.substring(0,dexTool)+weaponName+str.substring(dexTool+10)+msp("missed.wav",20);
	}


	public static String standardHitString(int weaponClass, int damageAmount,  String weaponName)
	{
		if((weaponName==null)||(weaponName.length()==0))
			weaponClass=Weapon.CLASS_NATURAL;
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+msp("arrow.wav",20);
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and <DAMAGE> <T-HIM-HER>."+msp("arrow.wav",20);
		default:
			return "<S-NAME> <DAMAGE> <T-NAMESELF> with "+weaponName+"."+msp("punch"+Dice.roll(1,4,0)+".wav",20);
		}
	}

	public static boolean isTheme(int i)
	{
	    return (CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDTHEME)&i)>0;
	}
	
	public static String standardMobCondition(MOB mob)
	{
		switch((int)Math.round(Math.floor((Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10)))
		{
		case 0:	return "^r" + mob.name() + "^r is hovering on deaths door!^N";
		case 1:	return "^r" + mob.name() + "^r is covered in blood.^N";
		case 2:	return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		case 3:	return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		case 4:	return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		case 5:	return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		case 6:	return "^p" + mob.name() + "^p is cut and bruised.^N";
		case 7:	return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		case 8:	return "^g" + mob.name() + "^g has a few bruises and scratches.^N";
		case 9:	return "^g" + mob.name() + "^g has a few small bruises.^N";
		default: return "^c" + mob.name() + "^c is in perfect health.^N";
		}
	}

	public static String shortAlignmentStr(int al)
	{
		if(al<350) return "evil";
		else if(al<650)	return "neutral";
		else return "good";
	}

	public static String alignmentStr(int al)
	{
		if(al<50) return "pure evil";
		else if(al<300) return "evil";
		else if(al<425)	return "somewhat evil";
		else if(al<575)	return "pure neutral";
		else if(al<700)	return "somewhat good";
		else if(al<950)	return "good";
		else return "pure goodness";

	}
	private static String lastStr="";
	private static long lastRes=0;
	public static void resistanceMsgs(CMMsg msg, MOB source, MOB target)
	{
		if(msg.value()>0) return;

		if(target.amDead()) return;

		String tool=null;
		String endPart=" from <T-NAME>.";
		if(source==target)
		{
			source=null;
			endPart=".";
		}
		if(msg.tool()!=null)
		{
			if(msg.tool() instanceof Trap)
				endPart=".";
			else
		    if(msg.tool() instanceof Ability)
				tool=((Ability)msg.tool()).name();
		}
		
		String tackOn=null;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_MIND: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart; break;
		case CMMsg.TYP_GAS: tackOn="<S-NAME> resist(s) the "+((tool==null)?"noxious fumes":tool)+endPart; break;
		case CMMsg.TYP_COLD: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart;	break;
		case CMMsg.TYP_ELECTRIC: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart; break;
		case CMMsg.TYP_FIRE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"blast of heat":tool)+endPart; break;
		case CMMsg.TYP_WATER: tackOn="<S-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart;	break;
		case CMMsg.TYP_UNDEAD:	tackOn="<S-NAME> shake(s) off the "+((tool==null)?"evil attack":tool)+endPart; break;
		case CMMsg.TYP_POISON:	tackOn="<S-NAME> shake(s) off the "+((tool==null)?"poison":tool)+endPart; break;
		case CMMsg.TYP_DISEASE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"disease":tool); break;
		case CMMsg.TYP_JUSTICE:break;
		case CMMsg.TYP_CAST_SPELL:	tackOn="<S-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart; break;
		case CMMsg.TYP_PARALYZE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"paralysis":tool)+endPart; break;
		}
		if(tackOn!=null)
		{
			String newStr=target+"/"+source+"/"+tool;
			if(!newStr.equals(lastStr)||((System.currentTimeMillis()-lastRes)>250))
				msg.addTrailerMsg(new FullMsg(target,source,CMMsg.MSG_OK_ACTION,tackOn));
			lastStr=newStr;
			lastRes=System.currentTimeMillis();
		}
		msg.setValue(msg.value()+1);
	}

	// this is the sound support method.
	// it builds a valid MSP sound code from built-in web server
	// info, and the info provided.
	public static String msp(String soundName, int volume, int priority)
	{
		if(getVar(SYSTEM_MSPPATH).length()>0)
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+" U="+getVar(SYSTEM_MSPPATH)+soundName+") ";
		else
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
	}

	public static String msp(String soundName, int priority)
	{ return msp(soundName,50,Dice.roll(1,50,priority));}
	
	public static String applyFilter(String msg, int whichFilter)
	{
		Vector filter=null;
		switch(whichFilter)
		{
		case SYSTEM_EMOTEFILTER: filter=emoteFilter; break;
		case SYSTEM_SAYFILTER: filter=sayFilter; break;
		case SYSTEM_CHANNELFILTER: filter=channelFilter; break;
		}
		if((filter==null)||(filter.size()==0)) 
			return msg;
		
		int fdex=0;
		int len=0;
		StringBuffer newMsg=null;
		String upp=msg.toUpperCase();
		for(int f=0;f<filter.size();f++)
		{
			fdex=upp.indexOf((String)filter.elementAt(f));
			while(fdex>=0)
			{
				len=fdex+((String)filter.elementAt(f)).length();
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
							newMsg.setCharAt(fdex,'*');
							upp=newMsg.toString().toUpperCase();
						}
					fdex=upp.indexOf((String)filter.elementAt(f));
				}
				else
					fdex=-1;
			}
		}
		if(newMsg!=null) return newMsg.toString();
		return msg;
	}

	public static String[] armorStrs={
	"vulnerable",
	"slightly covered",
	"somewhat covered",
	"covered",
	"well covered",
	"very covered",
	"slightly protected",
	"somewhat protected",
	"protected",
	"well protected",
	"very protected",
	"heavily protected",
	"slightly armored",
	"somewhat armored",
	"armored",
	"armored",
	"well armored",
	"very armored",
	"heavily armored",
	"completely armored",
	"totally armored",
	"divinely armored",
	"slightly unhittable",
	"somewhat unhittable",
	"practically unhittable",
	"unhittable",
	"unhittable",
	"totally unhittable",
	"totally unhittable",
	"slightly impenetrable",
	"slightly impenetrable",
	"somewhat impenetrable",
	"somewhat impenetrable",
	"almost impenetrable",
	"almost impenetrable",
	"impenetrable",
	"impenetrable",
	"slightly invincible",
	"slightly invincible",
	"slightly invincible",
	"somewhat invincible",
	"somewhat invincible",
	"somewhat invincible",
	"somewhat invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"invincible!",
	};

	public final static String[] fightStrs={
	"none",
	"novice",
	"initiate",
	"trainee",
	"barely skilled",
	"a little skilled",
	"slightly skilled",
	"somewhat skilled",
	"almost skilled",
	"mostly skilled",
	"simply skilled",
	"skilled",
	"really skilled",
	"obviously skilled",
	"very skilled",
	"extremely skilled",
	"terribly skilled",
	"masterfully skilled",
	"a little dangerous",
	"barely dangerous",
	"slightly dangerous",
	"somewhat dangerous",
	"almost dangerous",
	"mostly dangerous",
	"simply dangerous",
	"dangerous",
	"really dangerous",
	"obviously dangerous",
	"very dangerous",
	"extremely dangerous",
	"terribly dangerous",
	"horribly dangerous",
	"fearfully dangerous",
	"frighteningly dangerous",
	"totally dangerous",
	"entirely dangerous",
	"a novice master I",
	"a novice master II",
	"a novice master III",
	"a master initiate I",
	"a master initiate II",
	"a master initiate III",
	"an apprentice master I",
	"an apprentice master II",
	"an apprentice master III",
	"a master I",
	"a master I",
	"a master II",
	"a master II",
	"a master III",
	"a master III",
	"a master IV",
	"a master IV",
	"a master V",
	"a master V",
	"a master VI",
	"a master VI",
	"a master VII",
	"a master VII",
	"a master VIII",
	"a master VIII",
	"a master IX",
	"a master IX",
	"a master X",
	"a master X",
	"an initiate of death I",
	"an initiate of death II",
	"an initiate of death III",
	"an apprentice of death I",
	"an apprentice of death II",
	"an apprentice of death III",
	"a servant of death I",
	"a servant of death II",
	"a servant of death III",
	"a bringer of death I",
	"a bringer of death II",
	"a bringer of death III",
	"a bringer of death IV",
	"a giver of death I",
	"a giver of death II",
	"a giver of death III",
	"a giver of death V",
	"a giver of death VI",
	"a giver of death VIII",
	"a giver of death X",
	"a dealer of death I",
	"a dealer of death II",
	"a dealer of death III",
	"a dealer of death IV",
	"a dealer of death V",
	"a dealer of death VI",
	"a dealer of death VIII",
	"a dealer of death X",
	"a master of death I",
	"a master of death II",
	"a master of death III",
	"a master of death IV",
	"a master of death V",
	"a master of death VII",
	"a master of death VIII",
	"a master of death IX",
	"a master of death X",
	"a lord of death I",
	"a lord of death II",
	"a lord of death III",
	"a lord of death IV",
	"a lord of death V",
	"a lord of death VI",
	"a lord of death VII",
	"a lord of death VIII",
	"a lord of death IX",
	"a lord of death X",
	"death incarnate!"
	};
	public final static String[] missStrs1={
		"<S-NAME> fire(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", // 0
		"<S-NAME> throw(s) <TOOLNAME> at <T-NAMESELF> and miss(es).", // 1
		"<S-NAME> swing(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", //2
		"<S-NAME> attack(s) <T-NAMESELF> with <TOOLNAME> and miss(es).", //3
		"<S-NAME> lunge(s) at <T-NAMESELF> with <TOOLNAME> and miss(es)." //4
	};
	public final static String[] missStrs2={
		"<S-NAME> fire(s) at <T-NAMESELF> and miss(es).", //0
		"<S-NAME> throw(s) at <T-NAMESELF> and miss(es).", //1
		"<S-NAME> swing(s) at <T-NAMESELF> and miss(es).", //2
		"<S-NAME> attack(s) <T-NAMESELF> and miss(es).",  //3
		"<S-NAME> lunge(s) at <T-NAMESELF> and miss(es)." //4
	};
}
