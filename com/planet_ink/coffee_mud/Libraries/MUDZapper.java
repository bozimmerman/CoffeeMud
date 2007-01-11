package com.planet_ink.coffee_mud.Libraries;
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

import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/* 
   Copyright 2000-2007 Bo Zimmerman

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
public class MUDZapper extends StdLibrary implements MaskingLibrary
{
    public String ID(){return "MUDZapper";}
	public Hashtable zapCodes=new Hashtable();
    
    protected MOB nonCrashingMOB=null;
    protected MOB nonCrashingMOB(){
        if(nonCrashingMOB!=null)
            return nonCrashingMOB;
        nonCrashingMOB=CMClass.getMOB("StdMOB");
        return nonCrashingMOB;
    }

    public String rawMaskHelp(){return DEFAULT_MASK_HELP;}
    
    protected Vector preCompiled(String str)
    {
        Hashtable H=(Hashtable)Resources.getResource("SYSTEM_HASHED_MASKS");
        if(H==null){ H=new Hashtable(); Resources.submitResource("SYSTEM_HASHED_MASKS",H); }
        Vector V=(Vector)H.get(str.toLowerCase().trim());
        if(V==null)
        {
            V=maskCompile(str);
            V.trimToSize();
            H.put(str.toLowerCase().trim(),V);
        }
        return V;
    }
    
    public Hashtable getMaskCodes()
	{
		if(zapCodes.size()==0)
		{
			zapCodes.put("-CLASS",new Integer(0));
			zapCodes.put("-CLASSES",new Integer(0));
			zapCodes.put("-BASECLASS",new Integer(1));
			zapCodes.put("-BASECLASSES",new Integer(1));
			zapCodes.put("-RACE",new Integer(2));
			zapCodes.put("-RACES",new Integer(2));
			zapCodes.put("-ALIGNMENT",new Integer(3));
			zapCodes.put("-ALIGNMENTS",new Integer(3));
			zapCodes.put("-ALIGN",new Integer(3));
			zapCodes.put("-GENDER",new Integer(4));
			zapCodes.put("-GENDERS",new Integer(4));
			zapCodes.put("-LEVEL",new Integer(5));
			zapCodes.put("-LEVELS",new Integer(5));
			zapCodes.put("-CLASSLEVEL",new Integer(6));
			zapCodes.put("-CLASSLEVELS",new Integer(6));
			zapCodes.put("-TATTOOS",new Integer(7));
			zapCodes.put("-TATTOO",new Integer(7));
			zapCodes.put("+TATTOOS",new Integer(8));
			zapCodes.put("+TATTOO",new Integer(8));
			zapCodes.put("-NAME",new Integer(9));
			zapCodes.put("-NAMES",new Integer(9));
			zapCodes.put("-PLAYER",new Integer(10));
			zapCodes.put("-NPC",new Integer(11));
			zapCodes.put("-MOB",new Integer(11));
			zapCodes.put("-RACECAT",new Integer(12));
			zapCodes.put("-RACECATS",new Integer(12));
			zapCodes.put("+RACECAT",new Integer(13));
			zapCodes.put("+RACECATS",new Integer(13));
			zapCodes.put("-CLAN",new Integer(14));
			zapCodes.put("-CLANS",new Integer(14));
			zapCodes.put("+CLAN",new Integer(15));
			zapCodes.put("+CLANS",new Integer(15));
			zapCodes.put("+NAME",new Integer(16));
			zapCodes.put("+NAMES",new Integer(16));
			zapCodes.put("-ANYCLASS",new Integer(17));
			zapCodes.put("+ANYCLASS",new Integer(18));
			zapCodes.put("+ADJSTR",new Integer(19));
			zapCodes.put("+ADJINT",new Integer(20));
			zapCodes.put("+ADJWIS",new Integer(21));
			zapCodes.put("+ADJDEX",new Integer(22));
			zapCodes.put("+ADJCON",new Integer(23));
			zapCodes.put("+ADJCHA",new Integer(24));
			zapCodes.put("+ADJSTRENGTH",new Integer(19));
			zapCodes.put("+ADJINTELLIGENCE",new Integer(20));
			zapCodes.put("+ADJWISDOM",new Integer(21));
			zapCodes.put("+ADJDEXTERITY",new Integer(22));
			zapCodes.put("+ADJCONSTITUTION",new Integer(23));
			zapCodes.put("+ADJCHARISMA",new Integer(24));
			zapCodes.put("-ADJSTR",new Integer(25));
			zapCodes.put("-ADJSTRENGTH",new Integer(25));
			zapCodes.put("-ADJINT",new Integer(26));
			zapCodes.put("-ADJINTELLIGENCE",new Integer(26));
			zapCodes.put("-ADJWIS",new Integer(27));
			zapCodes.put("-ADJWISDOM",new Integer(27));
			zapCodes.put("-ADJDEX",new Integer(28));
			zapCodes.put("-ADJDEXTERITY",new Integer(28));
			zapCodes.put("-ADJCON",new Integer(29));
			zapCodes.put("-ADJCONSTITUTION",new Integer(29));
			zapCodes.put("-ADJCHA",new Integer(30));
			zapCodes.put("-ADJCHARISMA",new Integer(30));
			zapCodes.put("-AREA",new Integer(31));
			zapCodes.put("+AREA",new Integer(32));
			zapCodes.put("+ITEM",new Integer(33));
            zapCodes.put("-ITEM",new Integer(33));
			zapCodes.put("+CLASS",new Integer(34));  // for compiled use ONLY
			zapCodes.put("+ALIGNMENT",new Integer(35));  // for compiled use ONLY
			zapCodes.put("+GENDER",new Integer(36));  // for compiled use ONLY
			zapCodes.put("+LVLGR",new Integer(37));  // for compiled use ONLY
			zapCodes.put("+LVLGE",new Integer(38));  // for compiled use ONLY
			zapCodes.put("+LVLLT",new Integer(39));  // for compiled use ONLY
			zapCodes.put("+LVLLE",new Integer(40));  // for compiled use ONLY
			zapCodes.put("+LVLEQ",new Integer(41));  // for compiled use ONLY
			zapCodes.put("+EFFECTS",new Integer(42));
			zapCodes.put("+EFFECT",new Integer(42));
			zapCodes.put("-EFFECTS",new Integer(43));
			zapCodes.put("-EFFECT",new Integer(43));
			zapCodes.put("-DEITY",new Integer(44));
			zapCodes.put("+DEITY",new Integer(45));
			zapCodes.put("-FACTION",new Integer(46));
			zapCodes.put("+FACTION",new Integer(47));
            zapCodes.put("+WORN",new Integer(48));
            zapCodes.put("-WORN",new Integer(48));
            zapCodes.put("+MATERIAL",new Integer(49));
            zapCodes.put("-MATERIAL",new Integer(50));
            zapCodes.put("+RESOURCE",new Integer(51));
            zapCodes.put("-RESOURCE",new Integer(52));
            zapCodes.put("-JAVACLASS",new Integer(53));
            zapCodes.put("+JAVACLASS",new Integer(54));
            zapCodes.put("+ABILITY",new Integer(55));
            zapCodes.put("-ABILITY",new Integer(56));
            zapCodes.put("+ABLE",new Integer(55));
            zapCodes.put("-ABLE",new Integer(56));
            zapCodes.put("+WORNON",new Integer(57));
            zapCodes.put("-WORNON",new Integer(58));
            zapCodes.put("+VALUE",new Integer(59));
            zapCodes.put("-VALUE",new Integer(60));
            zapCodes.put("+WEIGHT",new Integer(61)); 
            zapCodes.put("-WEIGHT",new Integer(62));
            zapCodes.put("+ARMOR",new Integer(63));
            zapCodes.put("-ARMOR",new Integer(64));
            zapCodes.put("+DAMAGE",new Integer(65));
            zapCodes.put("-DAMAGE",new Integer(66));
            zapCodes.put("+ATTACK",new Integer(67));
            zapCodes.put("-ATTACK",new Integer(68));
            zapCodes.put("+DISPOSITION",new Integer(69));
            zapCodes.put("-DISPOSITION",new Integer(70));
            zapCodes.put("+SENSES",new Integer(71));
            zapCodes.put("-SENSES",new Integer(72));
            zapCodes.put("+HOUR",new Integer(73));
            zapCodes.put("-HOUR",new Integer(74));
            zapCodes.put("+SEASON",new Integer(75));
            zapCodes.put("-SEASON",new Integer(76));
            zapCodes.put("+MONTH",new Integer(77));
            zapCodes.put("-MONTH",new Integer(78));
            zapCodes.put("-SECURITY",new Integer(79));
            zapCodes.put("+SECURITY",new Integer(80));
            zapCodes.put("-SECURITIES",new Integer(79));
            zapCodes.put("+SECURITIES",new Integer(80));
            zapCodes.put("-SEC",new Integer(79));
            zapCodes.put("+SEC",new Integer(80));
            zapCodes.put("-EXPERTISE",new Integer(81));
            zapCodes.put("+EXPERTISE",new Integer(82));
            zapCodes.put("-EXPERTISES",new Integer(81));
            zapCodes.put("+EXPERTISES",new Integer(82));
            zapCodes.put("-SKILL",new Integer(83));
            zapCodes.put("+SKILL",new Integer(84));
            zapCodes.put("-SKILLS",new Integer(83));
            zapCodes.put("+SKILLS",new Integer(84));
            zapCodes.put("+QUALLVL",new Integer(85));
            zapCodes.put("-QUALLVL",new Integer(86));
			zapCodes.put("+STR",new Integer(87));
			zapCodes.put("+INT",new Integer(88));
			zapCodes.put("+WIS",new Integer(89));
			zapCodes.put("+DEX",new Integer(90));
			zapCodes.put("+CON",new Integer(91));
			zapCodes.put("+CHA",new Integer(92));
			zapCodes.put("+STRENGTH",new Integer(87));
			zapCodes.put("+INTELLIGENCE",new Integer(88));
			zapCodes.put("+WISDOM",new Integer(89));
			zapCodes.put("+DEXTERITY",new Integer(90));
			zapCodes.put("+CONSTITUTION",new Integer(91));
			zapCodes.put("+CHARISMA",new Integer(92));
			zapCodes.put("-STR",new Integer(93));
			zapCodes.put("-STRENGTH",new Integer(93));
			zapCodes.put("-INT",new Integer(94));
			zapCodes.put("-INTELLIGENCE",new Integer(94));
			zapCodes.put("-WIS",new Integer(95));
			zapCodes.put("-WISDOM",new Integer(95));
			zapCodes.put("-DEX",new Integer(96));
			zapCodes.put("-DEXTERITY",new Integer(96));
			zapCodes.put("-CON",new Integer(97));
			zapCodes.put("-CONSTITUTION",new Integer(97));
			zapCodes.put("-CHA",new Integer(98));
			zapCodes.put("-CHARISMA",new Integer(98));
            zapCodes.put("+HOME",new Integer(99));
            zapCodes.put("-HOME",new Integer(100));
            zapCodes.put("-SKILLFLAG",new Integer(101));
            zapCodes.put("+SKILLFLAG",new Integer(102));
            zapCodes.put("-SKILLFLAGS",new Integer(101));
            zapCodes.put("+SKILLFLAGS",new Integer(102));
            zapCodes.put("-MAXCLASSLEVEL",new Integer(103));
            zapCodes.put("-MAXCLASSLEVELS",new Integer(103));
            zapCodes.put("+WEATHER",new Integer(104));
            zapCodes.put("-WEATHER",new Integer(105));
            zapCodes.put("+DAY",new Integer(106));
            zapCodes.put("-DAY",new Integer(107));
            zapCodes.put("+SYSOP",new Integer(108));
            zapCodes.put("-SYSOP",new Integer(109));
            zapCodes.put("+SUBOP",new Integer(110));
            zapCodes.put("-SUBOP",new Integer(111));
		}
		return zapCodes;
	}

	public String maskHelp(String CR, String word)
	{
		String copy=new String(rawMaskHelp());
		if((CR!=null)&&(!CR.equalsIgnoreCase("<BR>")))
			copy=CMStrings.replaceAll(copy,"<BR>",CR);
		if((word==null)||(word.length()==0))
			copy=CMStrings.replaceAll(copy,"<WORD>","disallow");
		else
			copy=CMStrings.replaceAll(copy,"<WORD>",word);
		return copy;
	}

	protected Object makeSkillFlagObject(String str)
	{
        Object o=null;
        int x=str.indexOf("&");
        if((x>=0)&&(o==null))
        {
            Vector V=CMParms.parseAny(str,"&",true);
            String s=null;
            for(int v=0;v<V.size();v++)
            {
                s=(String)V.elementAt(v);
                if(s.startsWith("!"))
                {
                    V.setElementAt(s.substring(1),v);
                    V.insertElementAt(new Boolean(false),v);
                    v++;
                }
            }
            Object[] o2=new Object[V.size()];
            for(int v=0;v<V.size();v++)
                if(V.elementAt(v) instanceof String)
                    o2[v]=makeSkillFlagObject((String)V.elementAt(v));
                else
                    o2[v]=V.elementAt(v);
            for(int i=0;i<o2.length;i++)
                if((o2[i]!=null)&&(!(o2[i] instanceof Boolean))) 
                { o=o2; break;}
        }
        if(o==null)
        for(int d=0;d<Ability.ACODE_DESCS.length;d++)
            if(Ability.ACODE_DESCS[d].equals(str))
            {
                o=new Integer(d);
                break;
            }
        if(o==null)
        for(int d=0;d<Ability.DOMAIN_DESCS.length;d++)
            if(Ability.DOMAIN_DESCS[d].startsWith(str)||Ability.DOMAIN_DESCS[d].endsWith(str))
            {
                o=new Integer(d<<5);
                break;
            }
        if(o==null)
        for(int d=0;d<Ability.FLAG_DESCS.length;d++)
            if(Ability.FLAG_DESCS[d].startsWith(str))
            {
                o=new Long(1<<d);
                break;
            }
        if(o==null)
        for(short d=0;d<Ability.QUALITY_DESCS.length;d++)
            if(Ability.QUALITY_DESCS[d].startsWith(str)||Ability.QUALITY_DESCS[d].endsWith(str))
            {
                o=new Short(d);
                break;
            }
        return o;
	}
	
	protected boolean evaluateSkillFlagObject(Object o, Ability A)
	{
        if(A!=null) 
        {
            if(o instanceof Object[])
            {
                Object[] set=(Object[])o;
                for(int i=0;i<set.length;i++)
                    if(set[i] instanceof Boolean)
                    {
                        if(evaluateSkillFlagObject(set[i+1],A))
                        	return false;
                        i++;
                    }
                    else
                    if(!evaluateSkillFlagObject(set[i],A))
                    	return false;
                return true;
            }
            else
	        if(o instanceof Integer)
	        {
	            int val=((Integer)o).intValue();
	            if(((A.classificationCode()&Ability.ALL_ACODES)==val)
	            ||((A.classificationCode()&Ability.ALL_DOMAINS)==val))
	                return true;
	        }
	        else
	        if(o instanceof Short)
	        {
	            int val=((Short)o).intValue();
	            if(A.abstractQuality()==val)
	                return true;
	        }
	        else
	        if(o instanceof Long)
	        {
	            long val=((Long)o).longValue();
	            if((A.flags()&val)==val)
	                return true;
	        }
        }
        return false;
	}
	
	
	protected boolean skillFlagCheck(Vector V, char plusMinus, int fromHere, MOB mob)
    {
        Ability A=null;
        Object o=null;
        String str=null;
        for(int v=fromHere;v<V.size();v++)
        {
            str=(String)V.elementAt(v);
            if(str.length()==0) continue;
            if(getMaskCodes().containsKey(str))
                return false;
            o=makeSkillFlagObject(str); 
            if(o==null) continue;
            for(int a=0;a<mob.numAbilities();a++)
            {
                A=mob.fetchAbility(a);
                if(evaluateSkillFlagObject(o,A))
                	return true;
            }
        }
        return false;
    }

	protected Vector levelCompiledHelper(String str, char c, Vector entry)
	{
		if(entry==null) entry=new Vector();
		if(str.startsWith(c+">=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entry.addElement(getMaskCodes().get("+LVLGE"));
			entry.addElement(new Integer(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+"<=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entry.addElement(getMaskCodes().get("+LVLLE"));
			entry.addElement(new Integer(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+">")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entry.addElement(getMaskCodes().get("+LVLGR"));
			entry.addElement(new Integer(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"<")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entry.addElement(getMaskCodes().get("+LVLLT"));
			entry.addElement(new Integer(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"=")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entry.addElement(getMaskCodes().get("+LVLEQ"));
			entry.addElement(new Integer(CMath.s_int(str.substring(2).trim())));
		}
		return entry;
	}
	
	protected StringBuffer levelHelp(String lvl, char c, String append)
	{
		if(lvl.startsWith(c+">=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return new StringBuffer(append+"levels greater than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+"<=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return new StringBuffer(append+"levels less than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+">")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuffer(append+"levels greater than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"<")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuffer(append+"levels less than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"=")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuffer(append+"level "+lvl.substring(2).trim()+" players.  ");
		return new StringBuffer("");
	}
	
	protected int levelMinHelp(String lvl, char c, int minMinLevel, boolean reversed)
    {
        if(lvl.startsWith(c+">=")&&(CMath.isNumber(lvl.substring(3).trim())))
            return reversed?minMinLevel:CMath.s_int(lvl.substring(3).trim());
        else
        if(lvl.startsWith(c+"<=")&&(CMath.isNumber(lvl.substring(3).trim())))
            return reversed?CMath.s_int(lvl.substring(3).trim())+1:minMinLevel;
        else
        if(lvl.startsWith(c+">")&&(CMath.isNumber(lvl.substring(2).trim())))
            return reversed?minMinLevel:CMath.s_int(lvl.substring(2).trim())+1;
        else
        if(lvl.startsWith(c+"<")&&(CMath.isNumber(lvl.substring(2).trim())))
            return reversed?CMath.s_int(lvl.substring(2).trim()):minMinLevel;
        else
        if(lvl.startsWith(c+"=")&&(CMath.isNumber(lvl.substring(2).trim())))
            return reversed?minMinLevel:CMath.s_int(lvl.substring(2).trim());
        return Integer.MIN_VALUE;
    }
	
	protected boolean fromHereEqual(Vector V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.equalsIgnoreCase(plusMinus+find)) return true;
		}
		return false;
	}

	protected boolean fromHereStartsWith(Vector V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.startsWith(plusMinus+find)) return true;
		}
		return false;
	}

	protected boolean fromHereEndsWith(Vector V, char plusMinus, int fromHere, String find)
    {
        for(int v=fromHere;v<V.size();v++)
        {
            String str=(String)V.elementAt(v);
            if(str.length()==0) continue;
            if(getMaskCodes().containsKey(str))
                return false;
            if((str.charAt(0)==plusMinus)&&str.endsWith(find))
                return true;
        }
        return false;
    }
	
	public String maskDesc(String text){return maskDesc(text,false);}
	
	public String maskDesc(String text, boolean skipFirstWord)
	{
		if(text.trim().length()==0) return "Anyone";
		StringBuffer buf=new StringBuffer("");
        Hashtable zapCodes=getMaskCodes();
		Vector V=CMParms.parse(text.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			int val=-1;
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					{
					buf.append(skipFirstWord?"Only ":"Allows only ");
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						if(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
							buf.append(C.name()+", ");
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuffer(buf.substring(0,buf.length()-2));
					buf.append(".  ");
					}
					break;
				case 1: // -baseclass
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if((C.ID().equals(C.baseClass()))
							&&(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim())))
								buf.append(C.name()+" types, ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 2: // -Race
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						Vector cats=new Vector();
						for(Enumeration r=CMClass.races();r.hasMoreElements();)
						{
							Race R=(Race)r.nextElement();
							String cat=R.name().toUpperCase();
							if(cat.length()>6) cat=cat.substring(0,6);
							if((!cats.contains(R.name())
							&&(fromHereStartsWith(V,'+',v+1,cat))))
							   cats.addElement(R.name());
						}
						for(int c=0;c<cats.size();c++)
							buf.append(((String)cats.elementAt(c))+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 12: // -Racecats
					{
						buf.append(skipFirstWord?"Only these racial categories ":"Allows only these racial categories ");
						Vector cats=new Vector();
						for(Enumeration r=CMClass.races();r.hasMoreElements();)
						{
							Race R=(Race)r.nextElement();
							String cat=R.racialCategory().toUpperCase();
							if((!cats.contains(R.racialCategory())
							&&(fromHereStartsWith(V,'+',v+1,cat))))
							   cats.addElement(R.racialCategory());
						}
						for(int c=0;c<cats.size();c++)
							buf.append(((String)cats.elementAt(c))+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 3: // -Alignment
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].substring(0,3)))
							buf.append(Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].toLowerCase()+", ");
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].substring(0,3)))
							buf.append(Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].toLowerCase()+", ");
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].substring(0,3)))
							buf.append(Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].toLowerCase()+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 4: // -Gender
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							buf.append("Male, ");
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							buf.append("Female, ");
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							buf.append("Neuter");
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 5: // -Levels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp((String)V.elementAt(v2),'+',skipFirstWord?"Only ":"Allows only "));
					}
					break;
				case 6: // -ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp((String)V.elementAt(v2),'+',skipFirstWord?"Only class level ":"Allows only class level "));
					}
					break;
                case 103: // -MaxclassLevels
                    {
                        for(int v2=v+1;v2<V.size();v2++)
                            buf.append(levelHelp((String)V.elementAt(v2),'+',skipFirstWord?"Only highest class level ":"Allows only highest class level "));
                    }
                    break;
				case 7: // -Tattoos
					{
						buf.append((skipFirstWord?"The":"Requires")+" the following tattoo(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 8: // +Tattoos
					{
						buf.append("Disallows the following tattoo(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 79: // -Security
					{
						buf.append((skipFirstWord?"The":"Requires")+" following security flag(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 80: // +security
					{
						buf.append("Disallows the following security flag(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 81: // -expertises
					{
						buf.append((skipFirstWord?"The":"Requires")+" following expertises(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("+"))
	                        {
	                        	ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
	                        	if(E!=null) buf.append(E.name+", ");
	                        }
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 82: // +expertises
					{
						buf.append("Disallows the following expertises(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("-"))
	                        {
	                        	ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
	                        	if(E!=null) buf.append(E.name+", ");
	                        }
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 101: // -skillflags
					{
						buf.append((skipFirstWord?"A":"Requires a")+" skill of type: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("+"))
                            {
                                Vector V3=CMParms.parseAny(str2.substring(1),"&",true);
                                String str3=null;
                                for(int v3=0;v3<V3.size();v3++)
                                {
                                    str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower((String)V3.elementAt(v3)),"_"," ");
                                    if(str3.startsWith("!"))
                            			buf.append("not "+str3.substring(1));
                                    else
                                        buf.append(str3);
                                    if(v3<(V3.size()-1)) 
                                        buf.append(" and ");
                                    else
                                        buf.append(", ");
                                }
                            }
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
                case 83: // -skills
                    {
                        buf.append((skipFirstWord?"O":"Requires o")+"ne of the following skill(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int prof=0;
                                str2=str2.substring(1);
                                int x=str2.indexOf("(");
                                if(x>0)
                                {
                                    if(str2.endsWith(")"))
                                        prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
                                    str2=str2.substring(0,x);
                                }
                                Ability A=CMClass.getAbility(str2);
                                if(A!=null)
                                {
                                    if(prof<=0)
                                        buf.append(A.name()+", ");
                                    else
                                        buf.append(A.name()+" at "+prof+"% proficiency, ");
                                }
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                break;
				case 84: // +skills
					{
						buf.append("Disallows the following skill(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("-"))
	                        {
	                        	int prof=0;
	                        	str2=str2.substring(1);
	                        	int x=str2.indexOf("(");
	                        	if(x>0)
	                        	{
	                        		if(str2.endsWith(")"))
		                        		prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
	                        		str2=str2.substring(0,x);
	                        	}
	                        	Ability A=CMClass.getAbility(str2);
	                        	if(A!=null)
	                        	{
	                        		if(prof<=0)
	                        			buf.append(A.name()+", ");
	                        		else
	                        			buf.append(A.name()+" at more than "+prof+"% proficiency, ");
	                        	}
	                        }
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
                case 102: // +skillflag
                    {
                        buf.append("Disallows the skill of type: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                Vector V3=CMParms.parseAny(str2.substring(1),"&",true);
                                String str3=null;
                                for(int v3=0;v3<V3.size();v3++)
                                {
                                    str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower((String)V3.elementAt(v3)),"_"," ");
                                    if(str3.startsWith("!"))
                                        buf.append("not "+str3.substring(1));
                                    else
                                        buf.append(str3);
                                    if(v3<(V3.size()-1)) 
                                        buf.append(" and ");
                                    else
                                        buf.append(", ");
                                }
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
				case 14: // -Clan
					{
						buf.append((skipFirstWord?"M":"Requires m")+"embership in the following clan(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 15: // +Clan
					{
						buf.append("Disallows the following clan(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
                case 50: // -Material
                    {
                        buf.append((skipFirstWord?"C":"Requires c")+"onstruction from the following material(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
                                if(code>=0)
                                    buf.append(CMStrings.capitalizeAndLower(RawMaterial.MATERIAL_DESCS[code>>8])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 49: // +Material
                    {
                        buf.append("Disallows items of the following material(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
                                if(code>=0)
                                    buf.append(CMStrings.capitalizeAndLower(RawMaterial.MATERIAL_DESCS[code>>8])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 58: // -wornon
                    {
                        buf.append((skipFirstWord?"A":"Requires a")+"bility to be worn: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CMLib.utensils().getWornCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Item.WORN_DESCS[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 57: // +wornon
                    {
                        buf.append("Disallows items capable of being worn: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=CMLib.utensils().getWornCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Item.WORN_DESCS[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 72: // -senses
                    {
                        buf.append((skipFirstWord?"The":"Requires")+" following sense(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CMLib.flags().getSensesCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.sensesDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 71: // +senses
                    {
                        buf.append("Disallows the following sense(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=CMLib.flags().getSensesCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.sensesDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 73: // +HOUR
                    {
                        buf.append("Disallowed during the following time(s) of the day: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 74: // -HOUR
                    {
                        buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following time(s) of the day: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 75: // +season
                    {
                        buf.append("Disallowed during the following season(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                if(CMath.isInteger(str2.substring(1).trim()))
                                {
                                    int season=CMath.s_int(str2.substring(1).trim());
                                    if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
                                        buf.append(TimeClock.SEASON_DESCS[season]+", ");
                                }
                                else
                                {
                                    int season=CMClass.globalClock().determineSeason(str2.substring(1).trim());
                                    if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
                                        buf.append(TimeClock.SEASON_DESCS[season]+", ");
                                }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 76: // -season
                    {
                        buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following season(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                if(CMath.isInteger(str2.substring(1).trim()))
                                {
                                    int season=CMath.s_int(str2.substring(1).trim());
                                    if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
                                        buf.append(TimeClock.SEASON_DESCS[season]+", ");
                                }
                                else
                                {
                                    int season=CMClass.globalClock().determineSeason(str2.substring(1).trim());
                                    if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
                                        buf.append(TimeClock.SEASON_DESCS[season]+", ");
                                }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                    
                case 104: // +weather
                {
                    buf.append("Disallowed during the following weather conditions(s): ");
                    for(int v2=v+1;v2<V.size();v2++)
                    {
                        String str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                            break;
                        if(str2.startsWith("-"))
                            if(CMath.isInteger(str2.substring(1).trim()))
                            {
                                int weather=CMath.s_int(str2.substring(1).trim());
                                if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
                                    buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
                            }
                            else
                            {
                                int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
                                if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
                                    buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
                            }
                    }
                    if(buf.toString().endsWith(", "))
                        buf=new StringBuffer(buf.substring(0,buf.length()-2));
                    buf.append(".  ");
                }
                break;
            case 105: // -weather
                {
                    buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following weather conditions(s): ");
                    for(int v2=v+1;v2<V.size();v2++)
                    {
                        String str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                            break;
                        if(str2.startsWith("+"))
                            if(CMath.isInteger(str2.substring(1).trim()))
                            {
                                int weather=CMath.s_int(str2.substring(1).trim());
                                if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
                                    buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
                            }
                            else
                            {
                                int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
                                if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
                                    buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
                            }
                    }
                    if(buf.toString().endsWith(", "))
                        buf=new StringBuffer(buf.substring(0,buf.length()-2));
                    buf.append(".  ");
                }
                break;
                
                case 77: // +month
                    {
                        buf.append("Disallowed during the following month(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 78: // -month
                    {
                        buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following month(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 106: // +day
                    {
                        buf.append("Disallowed during the following day(s) of the month: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 107: // -day
                    {
                        buf.append((skipFirstWord?"Only ":"Allowed only ")+"on the following day(s) of the month: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(CMath.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                        
                case 85: // +quallvl
                    if((v+1)<V.size())
	                {
	                    Ability A=CMClass.getAbility((String)V.elementAt(v+1));
	                    if(A!=null)
	                    {
		                    int adjustment=0;
		                    if(((v+2)<V.size())&&(CMath.isInteger((String)V.elementAt(v+2))))
		                    	adjustment=CMath.s_int((String)V.elementAt(v+2));
		                    buf.append(A.Name());
		                    if(adjustment!=0)
			                    buf.append("Qualifies for "+A.Name());
		                    else
		                    if(adjustment<0)
		                    	buf.append((-adjustment)+" levels before qualifying for "+A.Name());
		                    else
		                    	buf.append(adjustment+" levels after qualifying for "+A.Name());
		                    buf.append(".  ");
	                    }
	                }
	                break;
	            case 86: // -quallvl
                    if((v+1)<V.size())
	                {
	                    Ability A=CMClass.getAbility((String)V.elementAt(v+1));
	                    if(A!=null)
	                    {
		                    int adjustment=0;
		                    if(((v+2)<V.size())&&(CMath.isInteger((String)V.elementAt(v+2))))
		                    	adjustment=CMath.s_int((String)V.elementAt(v+2));
		                    buf.append(A.Name());
		                    if(adjustment!=0)
			                    buf.append("Does not qualify for "+A.Name());
		                    else
		                    if(adjustment<0)
		                    	buf.append("Still prior to "+(-adjustment)+" levels before qualifying for "+A.Name());
		                    else
		                    	buf.append("Still prior to "+adjustment+" levels after qualifying for "+A.Name());
		                    buf.append(".  ");
	                    }
	                }
	                break;
                case 70: // -disposition
                    {
                        buf.append((skipFirstWord?"The":"Requires")+" following disposition(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CMLib.flags().getDispositionCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.dispositionsDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 69: // -disposition
                    {
                        buf.append("Disallows the following disposition(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=CMLib.flags().getDispositionCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.dispositionsDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 52: // -Resource
                    {
                        buf.append((skipFirstWord?"C":"Requires c")+"onstruction from the following materials(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CMLib.materials().getResourceCode(str2.substring(1),false);
                                if(code>=0)
                                    buf.append(CMStrings.capitalizeAndLower(RawMaterial.RESOURCE_DESCS[code&RawMaterial.RESOURCE_MASK])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 51: // +Resource
                    {
                        buf.append("Disallows items of the following material(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=CMLib.materials().getResourceCode(str2.substring(1),false);
                                if(code>=0)
                                    buf.append(CMStrings.capitalizeAndLower(RawMaterial.RESOURCE_DESCS[code&RawMaterial.RESOURCE_MASK])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 53: // -JavaClass
                    {
                        buf.append((skipFirstWord?"B":"Requires b")+"eing of the following type: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(str2.substring(1)+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 54: // +JavaClass
                    {
                        buf.append("Disallows being of the following type: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                buf.append(str2.substring(1)+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
				case 44: // -Deity
					{
						buf.append((skipFirstWord?"W":"Requires w")+"orshipping in the following deity(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 45: // +Deity
					{
						buf.append("Disallows the worshippers of: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 16: // +Names
					{
						buf.append("Disallows the following mob/player name(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 9: // -Names
					{
						buf.append((skipFirstWord?"The":"Requires")+" following name(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 10: // -Player
					buf.append("Disallows players.  ");
					break;
				case 11: // -MOB
					buf.append("Disallows mobs/npcs.  ");
					break;
				case 13: // +racecats
					{
						buf.append("Disallows the following racial cat(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 17: // -anyclass
					{
						buf.append((skipFirstWord?"L":"Requires l")+"evels in one of the following:  ");
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
								buf.append(C.name()+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 18: // +anyclass
					{
						buf.append("Disallows any levels in any of the following:  ");
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'-',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
								buf.append(C.name()+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 19: // +adjstr
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" strength of at least "+val+".  ");
					break;
				case 20: // +adjint
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" intelligence of at least "+val+".  ");
					break;
				case 21: // +adjwis
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" wisdom of at least "+val+".  ");
					break;
				case 22: // +adjdex
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" dexterity of at least "+val+".  ");
					break;
				case 23: // -adjcha
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" constitution of at least "+val+".  ");
					break;
				case 24: // +adjcha
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" charisma of at least "+val+".  ");
					break;
				case 25: // -adjstr
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" strength of at most "+val+".  ");
					break;
				case 26: // -adjint
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" intelligence of at most "+val+".  ");
					break;
				case 27: // -adjwis
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" wisdom of at most "+val+".  ");
					break;
				case 28: // -adjdex
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" dexterity of at most "+val+".  ");
					break;
				case 29: // -adjcon
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" constitution of at most "+val+".  ");
					break;
				case 30: // -adjcha
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" charisma of at most "+val+".  ");
					break;
				case 87: // +str
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base strength of at least "+val+".  ");
					break;
				case 88: // +int
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" base intelligence of at least "+val+".  ");
					break;
				case 89: // +wis
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base wisdom of at least "+val+".  ");
					break;
				case 90: // +dex
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base dexterity of at least "+val+".  ");
					break;
				case 91: // +con
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base constitution of at least "+val+".  ");
					break;
				case 92: // +cha
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base charisma of at least "+val+".  ");
					break;
				case 93: // -str
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base strength of at most "+val+".  ");
					break;
				case 94: // -int
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" base intelligence of at most "+val+".  ");
					break;
				case 95: // -wis
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base wisdom of at most "+val+".  ");
					break;
				case 96: // -dex
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base dexterity of at most "+val+".  ");
					break;
				case 97: // -con
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base constitution of at most "+val+".  ");
					break;
				case 98: // -cha
					val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base charisma of at most "+val+".  ");
					break;
                case 55: // +able
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" magic/ability of at most "+val+".  ");
                    break;
                case 56: // -able
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" magic/ability of at least "+val+".  ");
                    break;
                case 59: // +value
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" value of at most "+val+".  ");
                    break;
                case 60: // -value
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" value of at least "+val+".  ");
                    break;
                case 61: // +weight
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at most "+val+".  ");
                    break;
                case 62: // -weight
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at least "+val+".  ");
                    break;
                case 63: // +armor
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" armor rating of at most "+val+".  ");
                    break;
                case 64: // -armor
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" armor rating of at least "+val+".  ");
                    break;
                case 65: // +damage
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" damage ability of at most "+val+".  ");
                    break;
                case 66: // -damage
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"A":"Requires a")+" damage ability of at least "+val+".  ");
                    break;
                case 67: // +attack
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"An":"Requires an")+" attack bonus of at most "+val+".  ");
                    break;
                case 68: // -attack
                    val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
                    buf.append((skipFirstWord?"An":"Requires an")+" attack bonus of at least "+val+".  ");
                    break;
				case 31: // +Area
					{
						buf.append("Disallows the following area(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 32: // -Area
					{
						buf.append((skipFirstWord?"The":"Requires the")+" following area(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
                case 99: // +Home
                {
                    buf.append("Disallows those whose home is the following area(s): ");
                    for(int v2=v+1;v2<V.size();v2++)
                    {
                        String str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                            break;
                        if(str2.startsWith("-"))
                            buf.append(str2.substring(1)+", ");
                    }
                    if(buf.toString().endsWith(", "))
                        buf=new StringBuffer(buf.substring(0,buf.length()-2));
                    buf.append(".  ");
                }
                break;
                case 100: // -Home
                    {
                        buf.append((skipFirstWord?"From the":"Requires being from the")+" following area(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(str2.substring(1)+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
				case 33: // +Item
					{
						buf.append((skipFirstWord?"The":"Requires the")+" following item(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
                case 48: // -Worn
                {
                    buf.append((skipFirstWord?"W":"Requires w")+"earing the following item(s): ");
                    for(int v2=v+1;v2<V.size();v2++)
                    {
                        String str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                            break;
                        if((str2.startsWith("+"))||(str2.startsWith("-")))
                            buf.append(str2.substring(1)+", ");
                    }
                    if(buf.toString().endsWith(", "))
                        buf=new StringBuffer(buf.substring(0,buf.length()-2));
                    buf.append(".  ");
                }
                break;
				case 42: // +Effects
					{
						buf.append("Disallows the following activities/effects(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 43: // -Effects
					{
						buf.append((skipFirstWord?"P":"Requires p")+"articipation in the following activities/effects(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 46: // -faction
				{
				    buf.append((skipFirstWord?"The":"Requires the")+" following: ");
				    for(int v2=v+1;v2<V.size();v2++)
				    {
				        String str2=(String)V.elementAt(v2);
				        if(zapCodes.containsKey(str2))
				            break;
				        if((str2.startsWith("+"))
				        &&(CMLib.factions().isRangeCodeName(str2.substring(1))))
				        {
				            String desc=CMLib.factions().rangeDescription(str2.substring(1),"or ");
				            if(desc.length()>0) buf.append(desc+"; ");
				        }
				    }
					if(buf.toString().endsWith(", "))
						buf=new StringBuffer(buf.substring(0,buf.length()-2));
					if(buf.toString().endsWith("; "))
						buf=new StringBuffer(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;
				}
			else
			{
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C=(CharClass)c.nextElement();
					if(str.startsWith("-"+CMStrings.padRight(C.name(),4).toUpperCase().trim()))
						buf.append("Disallows "+C.name()+".  ");
				}
				Vector cats=new Vector();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					String cat=R.racialCategory().toUpperCase();
					if(cat.length()>6) cat=cat.substring(0,6);
					if((str.startsWith("-"+cat))&&(!cats.contains(R.racialCategory())))
					{
						cats.addElement(R.racialCategory());
						buf.append("Disallows "+R.racialCategory()+".  ");
					}
				}
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].substring(0,3)))
					buf.append("Disallows "+Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].toLowerCase()+".  ");
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].substring(0,3)))
					buf.append("Disallows "+Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].toLowerCase()+".  ");
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].substring(0,3)))
					buf.append("Disallows "+Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].toLowerCase()+".  ");
				if(str.startsWith("-MALE"))
					buf.append("Disallows Males.  ");
				if(str.startsWith("-FEMALE"))
					buf.append("Disallows Females.  ");
				if(str.startsWith("-NEUTER"))
					buf.append((skipFirstWord?"Only ":"Allows only ")+"Males and Females.  ");
				buf.append(levelHelp(str,'-',"Disallows "));
				if((str.startsWith("-"))
		        &&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
		            String desc=CMLib.factions().rangeDescription(str.substring(1),"and ");
		            if(desc.length()>0) buf.append("Disallows "+desc);
				}
			}
		}

		if(buf.length()==0) buf.append("Anyone.");
		return buf.toString();
	}

	public Vector getAbilityEduReqs(String text)
	{
		Vector preReqs=new Vector();
		if(text.trim().length()==0) 
			return preReqs;
        Hashtable zapCodes=getMaskCodes();
		Vector V=CMParms.parse(text.toUpperCase());
		String str=null;
		String str2=null;
		for(int v=0;v<V.size();v++)
		{
			str=(String)V.elementAt(v);
			if(zapCodes.containsKey(str))
            {
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 81: // -expertises
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("+"))
	                        {
	                        	ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
	                        	if(E!=null) preReqs.addElement(E.ID);
	                        }
						}
					}
					break;
				case 83: // -skills
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=(String)V.elementAt(v2);
	                        if(zapCodes.containsKey(str2))
	                            break;
	                        if(str2.startsWith("+"))
	                        {
	                        	str2=str2.substring(1);
	                        	int x=str2.indexOf("(");
	                        	if(x>0) str2=str2.substring(0,x);
	                        	Ability A=CMClass.getAbility(str2);
	                        	if((A!=null)&&(!preReqs.contains(A.ID())))
	                        		preReqs.addElement(A.ID());
	                        }
						}
					}
					break;
				case 101: // -skillflag
				{
					Vector objs=new Vector();
					Object o=null;
					for(int v2=v+1;v2<V.size();v2++)
					{
						str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                            break;
                        if(str2.startsWith("+"))
                        {
                        	str2=str2.substring(1);
                        	o=this.makeSkillFlagObject(str2);
                        	if(o!=null) 
                        		objs.addElement(o);
                        }
					}
					Ability A=null;
					for(int v2=0;v2<objs.size();v2++)
                    	for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
                    	{
                        	A=(Ability)e.nextElement();
                        	if((evaluateSkillFlagObject(objs.elementAt(v2),A))
                        	&&(!preReqs.contains(A.ID())))
                        	{
                        		preReqs.addElement(A.ID());
                        	}
                    	}
				}
				break;
				}
            }
		}
		return preReqs;
	}

    public int minMaskLevel(String text, int minMinLevel)
    {
        int level=minMinLevel;
        Vector cset=preCompiled(text);
        for(int c=0;c<cset.size();c++)
        {
            Vector V=(Vector)cset.elementAt(c);
            if(V.size()>0)
            switch(((Integer)V.firstElement()).intValue())
            {
            case 5: // -level
            {
                for(int v=1;v<V.size();v+=2)
                    if((v+1)<V.size())
                    switch(((Integer)V.elementAt(v)).intValue())
                    {
                        case 37: // +lvlgr
                            level=((Integer)V.elementAt(v+1)).intValue()+1;
                            break;
                        case 38: // +lvlge
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                        case 39: // +lvlt
                            level=minMinLevel;
                            break;
                        case 40: // +lvlle
                            level=minMinLevel;
                            break;
                        case 41: // +lvleq
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                    }
            }
            break;
            case 6: // -classlevel
                {
                    for(int v=1;v<V.size();v+=2)
                        if((v+1)<V.size())
                        switch(((Integer)V.elementAt(v)).intValue())
                        {
                        case 37: // +lvlgr
                            level=((Integer)V.elementAt(v+1)).intValue()+1;
                            break;
                        case 38: // +lvlge
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                        case 39: // +lvlt
                            level=minMinLevel;
                            break;
                        case 40: // +lvlle
                            level=minMinLevel;
                            break;
                        case 41: // +lvleq
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                        }
                }
                break;
            case 103: // -maxclasslevel
                {
                    for(int v=1;v<V.size();v+=2)
                        if((v+1)<V.size())
                        switch(((Integer)V.elementAt(v)).intValue())
                        {
                        case 37: // +lvlgr
                            level=((Integer)V.elementAt(v+1)).intValue()+1;
                            break;
                        case 38: // +lvlge
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                        case 39: // +lvlt
                            level=minMinLevel;
                            break;
                        case 40: // +lvlle
                            level=minMinLevel;
                            break;
                        case 41: // +lvleq
                            level=((Integer)V.elementAt(v+1)).intValue();
                            break;
                        }
                }
                break;
            case 37: // +lvlgr
                level=minMinLevel;
                break;
            case 38: // +lvlge
                level=minMinLevel;
                break;
            case 39: // +lvlt
                level=((Integer)V.elementAt(1)).intValue();
                break;
            case 40: // +lvlle
                level=((Integer)V.elementAt(1)).intValue()+1;
                break;
            case 41: // +lvleq
                level=minMinLevel;
                break;
            }
        }
        return level;
    }
    
	public Vector maskCompile(String text)
	{
		Vector buf=new Vector();
		if(text.trim().length()==0) return buf;
        Hashtable zapCodes=getMaskCodes();
		Vector V=CMParms.parse(text.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			int val=-1;
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
								entry.addElement(C.name());
						}
					}
					break;
				case 1: // -baseclass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						HashSet seenBase=new HashSet();
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(!seenBase.contains(C.baseClass()))
							{
								seenBase.add(C.baseClass());
								if(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.baseClass(),4).toUpperCase().trim()))
									entry.addElement(C.baseClass());
							}
						}
					}
					break;
				case 2: // -Race
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						Vector cats=new Vector();
						for(Enumeration r=CMClass.races();r.hasMoreElements();)
						{
							Race R=(Race)r.nextElement();
							String cat=R.name().toUpperCase();
							if(cat.length()>6) cat=cat.substring(0,6);
							if((!cats.contains(R.name())
							&&(fromHereStartsWith(V,'+',v+1,cat))))
							   cats.addElement(R.name());
						}
						for(int c=0;c<cats.size();c++)
							entry.addElement(cats.elementAt(c));
					}
					break;
				case 12: // -Racecats
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						Vector cats=new Vector();
						for(Enumeration r=CMClass.races();r.hasMoreElements();)
						{
							Race R=(Race)r.nextElement();
							String cat=R.racialCategory().toUpperCase();
							if((!cats.contains(R.racialCategory())
							&&(fromHereStartsWith(V,'+',v+1,cat))))
							   cats.addElement(R.racialCategory());
						}
						for(int c=0;c<cats.size();c++)
							entry.addElement(cats.elementAt(c));
					}
					break;
				case 13: // +Racecats
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						Vector cats=new Vector();
						for(Enumeration r=CMClass.races();r.hasMoreElements();)
						{
							Race R=(Race)r.nextElement();
							String cat=R.racialCategory().toUpperCase();
							if((!cats.contains(R.racialCategory())
							&&(fromHereStartsWith(V,'-',v+1,cat))))
							   cats.addElement(R.racialCategory());
						}
						for(int c=0;c<cats.size();c++)
							entry.addElement(cats.elementAt(c));
					}
					break;
				case 3: // -Alignment
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].substring(0,3)))
						    entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_EVIL]);
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].substring(0,3)))
						    entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_GOOD]);
						if(fromHereStartsWith(V,'+',v+1,Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].substring(0,3)))
						    entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL]);
					}
					break;
				case 4: // -Gender
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							entry.addElement("M");
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							entry.addElement("F");
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							entry.addElement("N");
					}
					break;
				case 5: // -Levels
				case 6: // -ClassLevels
                case 103: // -MaxclassLevels
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
							levelCompiledHelper(str2,'+',entry);
                            v=V.size();
                        }
					}
					break;
				case 7: // -Tattoos
				case 79: // -security
				case 81: // -expertise
				case 14: // -Clan
				case 44: // -Deity
				case 43: // -Effect
				case 9: // -Names
				case 32: // -Area
                case 100: // -Home
                case 53: // -JavaClass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
							if(str2.startsWith("+"))
								entry.addElement(str2.substring(1));
                            v=V.size();
						}
					}
					break;
				case 46: // -Faction
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get(str));
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=(String)V.elementAt(v2);
                        if(zapCodes.containsKey(str2))
                        {
                            v=v2-1;
                            break;
                        }
                        else
						if((str2.startsWith("+"))
				        &&(CMLib.factions().isRangeCodeName(str2.substring(1))))
							entry.addElement(str2.substring(1).toUpperCase());
                        v=V.size();
					}
					break;
				}
				case 8: // +Tattoos
				case 80: // +security
				case 82: // +expertise
				case 15: // +Clan
				case 45: // +Deity
				case 42: // +Effect
				case 16: // +Names
				case 31: // +Area
                case 99: // +Home
                case 54: // +JavaClass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
							if(str2.startsWith("-"))
								entry.addElement(str2.substring(1));
                            v=V.size();
						}
					}
					break;
                case 83: // +skills
                case 84: // -skills
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                            	str2=str2.substring(1);
                            	int prof=0;
                            	int x=str2.indexOf("(");
                            	if(x>0)
                            	{
                            		if(str2.endsWith(")"))
                            			prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
                            		str2=str2.substring(0,x);
                            	}
                            	Ability A=CMClass.getAbility(str2);
                                if(A!=null)
                                {
                                    entry.addElement(A.ID());
                                    entry.addElement(new Integer(prof));
                                }
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 101: // -skillflag
                case 102: // +skillflag
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                str2=str2.substring(1).toUpperCase();
                                Object o=makeSkillFlagObject(str2);
                                if(o!=null) entry.addElement(o);
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 33: // -Item
                case 48: // -Worn
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("+"))||(str2.startsWith("-")))
                                entry.addElement(str2.substring(1));
                            v=V.size();
                        }
                    }
                    break;
                case 49: // +Material
                case 50: // -Material
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
                                if(code>=0)
                                    entry.addElement(RawMaterial.MATERIAL_DESCS[(code&RawMaterial.MATERIAL_MASK)>>8]);
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 57: // -WornOn
                case 58: // +WornOn
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                int code=CMLib.utensils().getWornCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer((int)CMath.pow(2,code-1)));
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 69: // +Disposition
                case 70: // -Disposition
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                int code=CMLib.flags().getDispositionCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer((int)CMath.pow(2,code)));
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 71: // +Senses
                case 72: // -Senses
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                int code=CMLib.flags().getSensesCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer((int)CMath.pow(2,code)));
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 75: // +Season
                case 76: // -Season
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                if(CMath.isInteger(str2.substring(1).trim()))
                                    entry.addElement(new Integer(CMath.s_int(str2.substring(1).trim())));
                                else
                                if(CMClass.globalClock().determineSeason(str2.substring(1).trim())>=0)
                                    entry.addElement(new Integer(CMClass.globalClock().determineSeason(str2.substring(1).trim())));
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 104: // +weather
                case 105: // -weather
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                if(CMath.isInteger(str2.substring(1).trim()))
                                    entry.addElement(new Integer(CMath.s_int(str2.substring(1).trim())));
                                else
                                if(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())>=0)
                                    entry.addElement(new Integer(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())));
                            }
                            v=V.size();
                        }
                    }
                    break;
                case 73: // +HOUR
                case 74: // -HOUR
                case 77: // +MONTH
                case 78: // -MONTH
                case 106: // +DAY
                case 107: // -DAY
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                                entry.addElement(new Integer(CMath.s_int(str2.substring(1).trim())));
                            v=V.size();
                        }
                    }
                    break;
                case 85: // +quallvl
                case 86: // -quallvl
                    if((v+1)<V.size())
	                {
	                    Ability A=CMClass.getAbility((String)V.elementAt(v+1));
	                    if(A!=null)
	                    {
		                    int adjustment=0;
		                    if(((v+2)<V.size())&&(CMath.isInteger((String)V.elementAt(v+2))))
		                    	adjustment=CMath.s_int((String)V.elementAt(v+2));
	                        Vector entry=new Vector();
	                        buf.addElement(entry);
	                        entry.addElement(zapCodes.get(str));
	                        entry.addElement(A.ID());
	                        entry.addElement(new Integer(adjustment));
	                    }
	                }
                	break;
                case 108: // +sysop
                case 109: // +sysop
                case 110: // +subop
                case 111: // +subop
                {
                    Vector entry=new Vector();
                    buf.addElement(entry);
                    entry.addElement(zapCodes.get(str));
                    break;
                }
                case 51: // +Resource
                case 52: // -Resource
                    {
                        Vector entry=new Vector();
                        buf.addElement(entry);
                        entry.addElement(zapCodes.get(str));
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                            {
                                v=v2-1;
                                break;
                            }
                            else
                            if((str2.startsWith("-"))||(str2.startsWith("+")))
                            {
                                int code=CMLib.materials().getResourceCode(str2.substring(1),false);
                                if(code>=0)
                                    entry.addElement(RawMaterial.RESOURCE_DESCS[(code&RawMaterial.RESOURCE_MASK)]);
                            }
                            v=V.size();
                        }
                    }
                    break;
				case 10: // -Player
				case 11: // -MOB
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						break;
					}
				case 17: // -anyclass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'+',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
								entry.addElement(C.name());
						}
					}
					break;
				case 18: // +anyclass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'-',v+1,CMStrings.padRight(C.name(),4).toUpperCase().trim()))
								entry.addElement(C.name());
						}
					}
					break;
				case 19: // +adjstr
				case 20: // +adjint
				case 21: // +adjwis
				case 22: // +adjdex
				case 23: // -adjcha
				case 24: // +adjcha
				case 25: // -adjstr
				case 26: // -adjint
				case 27: // -adjwis
				case 28: // -adjdex
				case 29: // -adjcon
				case 30: // -adjcha
				case 87: // +str
				case 88: // +int
				case 89: // +wis
				case 90: // +dex
				case 91: // +con
				case 92: // +cha
				case 93: // -str
				case 94: // -int
				case 95: // -wis
				case 96: // -dex
				case 97: // -con
				case 98: // -cha
                case 55: // +able
                case 56: // -able
                case 59: // +value
                case 60: // -value
                case 61: // +weight
                case 62: // -weight
                case 63: // +armor
                case 64: // -armor
                case 65: // +damage
                case 66: // -damage
                case 67: // +attack
                case 68: // -attack
					{
						val=((++v)<V.size())?CMath.s_int((String)V.elementAt(v)):0;
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						entry.addElement(new Integer(val));
						break;
					}
				}
			else
			{
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C=(CharClass)c.nextElement();
					if(str.startsWith("-"+CMStrings.padRight(C.name(),4).toUpperCase().trim()))
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get("+CLASS"));
						entry.addElement(C.name());
					}
				}
				Vector cats=new Vector();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					String cat=R.racialCategory().toUpperCase();
					if(cat.length()>6) cat=cat.substring(0,6);
					if((str.startsWith("-"+cat))&&(!cats.contains(R.racialCategory())))
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get("+RACECAT"));
						entry.addElement(R.racialCategory());
					}
				}
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_EVIL].substring(0,3)))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+ALIGNMENT"));
					entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_EVIL]);
				}
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_GOOD].substring(0,3)))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+ALIGNMENT"));
					entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_GOOD]);
				}
				if(str.startsWith("-"+Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL].substring(0,3)))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+ALIGNMENT"));
					entry.addElement(Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL]);
				}
				if(str.startsWith("-MALE"))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+GENDER"));
					entry.addElement("M");
				}
				if(str.startsWith("-FEMALE"))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+GENDER"));
					entry.addElement("F");
				}
				if(str.startsWith("-NEUTER"))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+GENDER"));
					entry.addElement("N");
				}
				if((str.startsWith("-"))
		        &&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
					Vector entry=new Vector();
					buf.addElement(entry);
					entry.addElement(zapCodes.get("+FACTION"));
					entry.addElement(str.substring(1));
				}
				Vector entry=levelCompiledHelper(str,'-',null);
				if((entry!=null)&&(entry.size()>0))
					buf.addElement(entry);
			}
		}
		buf.trimToSize();
		return buf;
	}
    
	protected Room outdoorRoom(Area A)
    {
        Room R=null;
        for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
        {
            R=(Room)e.nextElement();
            if((R.domainType()&Room.INDOORS)==0) return R;
        }
        return A.getRandomMetroRoom();
    }

	protected CharStats getBaseCharStats(CharStats base, MOB mob)
	{
        if(base==null)
        {
        	base=(CharStats)mob.baseCharStats().copyOf(); 
        	base.getMyRace().affectCharStats(mob,base);
        }
		return base;
	}
	
	public boolean maskCheck(String text, Environmental E, boolean actual){ return maskCheck(preCompiled(text),E,actual);}
	public boolean maskCheck(Vector cset, Environmental E, boolean actual)
	{
		if(E==null) return true;
		if((cset==null)||(cset.size()==0)) return true;
        getMaskCodes();
        CharStats base=null;
        MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
        Item item=(E instanceof Item)?(Item)E:null;
        Room R=(E instanceof Area)?outdoorRoom((Area)E):CMLib.map().roomLocation(E);
        
		for(int c=0;c<cset.size();c++)
		{
			Vector V=(Vector)cset.elementAt(c);
            try
            {
			if(V.size()>0)
			switch(((Integer)V.firstElement()).intValue())
			{
            case 108: // +sysop
                if(CMSecurity.isASysOp(mob))
                    return true;
                break;
            case 109: // -sysop
                if(CMSecurity.isASysOp(mob))
                    return false;
                break;
            case 110: // +subop
                if(CMSecurity.isASysOp(mob)
                ||((R!=null)&&(R.getArea().amISubOp(mob.Name()))))
                    return true;
                break;
            case 111: // -subop
                if(CMSecurity.isASysOp(mob)
                ||((R!=null)&&(R.getArea().amISubOp(mob.Name()))))
                    return false;
                break;
			case 0: // -class
			{
				if(!V.contains(actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
					return false;
				break;
			}
			case 1: // -baseclass
			{
				String baseClass=mob.baseCharStats().getCurrentClass().baseClass();
				if((!actual)&&(!baseClass.equals(mob.charStats().displayClassName())))
				{
					CharClass C=CMClass.getCharClass(mob.charStats().displayClassName());
					if(C!=null) baseClass=C.baseClass();
				}
				if(!V.contains(baseClass))
					return false;
				break;
			}
			case 2: // -race
				if(!V.contains(actual?mob.baseCharStats().getMyRace().name():mob.charStats().raceName()))
					return false;
				break;
			case 3: // -alignment
				if(!V.contains(CMLib.flags().getAlignmentName(mob)))
					return false;
				break;
			case 4: // -gender
			{
		        base=getBaseCharStats(base,mob);
				if(!V.contains(actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+(mob.charStats().genderName().charAt(0)))))
					return false;
				break;
			}
			case 5: // -level
				{
					int level=actual?E.baseEnvStats().level():E.envStats().level();
					boolean found=false;
					for(int v=1;v<V.size();v+=2)
						if((v+1)<V.size())
						switch(((Integer)V.elementAt(v)).intValue())
						{
							case 37: // +lvlgr
								if(level>((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(level>=((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(level<((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(level<=((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(level==((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
			case 6: // -classlevel
				{
					boolean found=false;
					int cl=actual?mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())
								 :mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
					for(int v=1;v<V.size();v+=2)
						if((v+1)<V.size())
						switch(((Integer)V.elementAt(v)).intValue())
						{
							case 37: // +lvlgr
								if((V.size()>1)&&(cl>((Integer)V.elementAt(v+1)).intValue()))
								   found=true;
								break;
							case 38: // +lvlge
								if((V.size()>1)&&(cl>=((Integer)V.elementAt(v+1)).intValue()))
								   found=true;
								break;
							case 39: // +lvlt
								if((V.size()>1)&&(cl<((Integer)V.elementAt(v+1)).intValue()))
								   found=true;
								break;
							case 40: // +lvlle
								if((V.size()>1)&&(cl<=((Integer)V.elementAt(v+1)).intValue()))
								   found=true;
								break;
							case 41: // +lvleq
								if((V.size()>1)&&(cl==((Integer)V.elementAt(v+1)).intValue()))
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
            case 103: // -maxclasslevel
                {
                    boolean found=false;
                    int cl=0;
                    int c2=0;
                    if(actual)
                    {
                    	cl=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(0));
	                    for(int v=1;v<mob.baseCharStats().numClasses();v++)
	                    {
	                        c2=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(v));
	                        if(c2>cl) cl=c2;
	                    }
                    }
                    else
                    {
                    	cl=mob.charStats().getClassLevel(mob.charStats().getMyClass(0));
	                    for(int v=1;v<mob.charStats().numClasses();v++)
	                    {
	                        c2=mob.charStats().getClassLevel(mob.charStats().getMyClass(v));
	                        if(c2>cl) cl=c2;
	                    }
                    }
                    for(int v=1;v<V.size();v+=2)
                        if((v+1)<V.size())
                        switch(((Integer)V.elementAt(v)).intValue())
                        {
                            case 37: // +lvlgr
                                if((V.size()>1)&&(cl>((Integer)V.elementAt(v+1)).intValue()))
                                   found=true;
                                break;
                            case 38: // +lvlge
                                if((V.size()>1)&&(cl>=((Integer)V.elementAt(v+1)).intValue()))
                                   found=true;
                                break;
                            case 39: // +lvlt
                                if((V.size()>1)&&(cl<((Integer)V.elementAt(v+1)).intValue()))
                                   found=true;
                                break;
                            case 40: // +lvlle
                                if((V.size()>1)&&(cl<=((Integer)V.elementAt(v+1)).intValue()))
                                   found=true;
                                break;
                            case 41: // +lvleq
                                if((V.size()>1)&&(cl==((Integer)V.elementAt(v+1)).intValue()))
                                   found=true;
                                break;
                        }
                    if(!found) return false;
                }
                break;
			case 7: // -tattoo
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if((mob.fetchTattoo((String)V.elementAt(v))!=null)
                        ||((R!=null)&&(R.getArea().getBlurbFlag((String)V.elementAt(v))!=null)))
						{ found=true; break;}
                    
					if(!found) return false;
				}
				break;
			case 8: // +tattoo
				{
					for(int v=1;v<V.size();v++)
						if((mob.fetchTattoo((String)V.elementAt(v))!=null)
                        ||((R!=null)&&(R.getArea().getBlurbFlag((String)V.elementAt(v))!=null)))
						{ return false;}
				}
				break;
			case 81: // -expertise
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(mob.fetchExpertise((String)V.elementAt(v))!=null)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 82: // +expertise
				{
					for(int v=1;v<V.size();v++)
						if(mob.fetchExpertise((String)V.elementAt(v))!=null)
						{ return false;}
				}
				break;
			case 83: // -skill
				{
					boolean found=false;
					Ability A=null;
					for(int v=1;v<V.size();v+=2)
					{
						A=mob.fetchAbility((String)V.elementAt(v));
						if((A!=null)&&(A.proficiency()>=((Integer)V.elementAt(v+1)).intValue()))
						{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
            case 101: // -skillflag
                {
                    Ability A=null;
                    Object o=null;
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                    {
                        o=V.elementAt(v);
                        for(int a=0;a<mob.numAbilities();a++)
                        {
                            A=mob.fetchAbility(a);
                            if(evaluateSkillFlagObject(o,A))
                            { found=true; break;}
                        }
                        if(found) break;
                    }
                    if(!found) return false;
                }
                break;
			case 84: // +skill
				{
					Ability A=null;
					for(int v=1;v<V.size();v++)
					{
						A=mob.fetchAbility((String)V.elementAt(v));
						if((A!=null)&&(A.proficiency()>=((Integer)V.elementAt(v+1)).intValue()))
						{ return false;}
					}
				}
				break;
            case 102: // +skillflag
                {
                    Ability A=null;
                    Object o=null;
                    for(int v=1;v<V.size();v++)
                    {
                        o=V.elementAt(v);
                        for(int a=0;a<mob.numAbilities();a++)
                        {
                            A=mob.fetchAbility(a);
                            if(evaluateSkillFlagObject(o,A))
                            	return false;
                        }
                    }
                }
                break;
			case 79: // -security
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(CMSecurity.isAllowed(mob,R,(String)V.elementAt(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 80: // +security
				{
					for(int v=1;v<V.size();v++)
						if(CMSecurity.isAllowed(mob,R,(String)V.elementAt(v)))
						{ return false;}
				}
				break;
			case 9: // -names
				{
					boolean found=false;
					String name=actual?E.Name():E.name();
					for(int v=1;v<V.size();v++)
						if(name.equalsIgnoreCase((String)V.elementAt(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 10: // -player
				if(!mob.isMonster()) return false;
				break;
			case 11: // -npc
				if(mob.isMonster()) return false;
				break;
			case 12: // -racecat
			{
				String raceCat=mob.baseCharStats().getMyRace().racialCategory();
				if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
				{
					Race R2=CMClass.getRace(mob.charStats().raceName());
					if(R2!=null) raceCat=R2.racialCategory();
				}
				if(!V.contains(raceCat))
					return false;
				break;
			}
			case 13: // +racecat
			{
				String raceCat=mob.baseCharStats().getMyRace().racialCategory();
				if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
				{
					Race R2=CMClass.getRace(mob.charStats().raceName());
					if(R2!=null) raceCat=R2.racialCategory();
				}
				if(V.contains(raceCat))
					return false;
				break;
			}
			case 14: // -clan
				{
                    String clanID=(mob!=null)?mob.getClanID():(E instanceof ClanItem)?((ClanItem)E).clanID():"";
					if(clanID.length()==0) 
						return false;
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(clanID.equalsIgnoreCase((String)V.elementAt(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 15: // +clan
                String clanID=(mob!=null)?mob.getClanID():(E instanceof ClanItem)?((ClanItem)E).clanID():"";
				if(clanID.length()>0) 
					for(int v=1;v<V.size();v++)
						if(clanID.equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				break;
            case 49: // +material
                if(V.contains(RawMaterial.MATERIAL_DESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8]))
                    return false;
                break;
            case 50: // -material
                if(!V.contains(RawMaterial.MATERIAL_DESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8]))
                    return false;
                break;
            case 57: // +wornOn
                for(int v=1;v<V.size();v++)
                    if((item.rawProperLocationBitmap()&((Integer)V.elementAt(v)).intValue())>0)
                        return false;
                break;
            case 58: // -wornOn
                {
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                        if((item.rawProperLocationBitmap()&((Integer)V.elementAt(v)).intValue())>0)
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 69: // +disposition
                for(int v=1;v<V.size();v++)
                    if((E.envStats().disposition()&((Integer)V.elementAt(v)).intValue())>0)
                        return false;
                break;
            case 70: // -disposition
                {
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                        if((E.envStats().disposition()&((Integer)V.elementAt(v)).intValue())>0)
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 71: // +senses
                for(int v=1;v<V.size();v++)
                    if((E.envStats().sensesMask()&((Integer)V.elementAt(v)).intValue())>0)
                        return false;
                break;
            case 72: // -senses
                {
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                        if((E.envStats().sensesMask()&((Integer)V.elementAt(v)).intValue())>0)
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 73: // +HOUR
                {
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getTimeOfDay()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 74: // -HOUR
                {
                    boolean found=false;
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getTimeOfDay()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 75: // +season
                {
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getSeasonCode()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 76: // -season
                {
                    boolean found=false;
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getSeasonCode()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 104: // +weather
            {
                if(R!=null)
                for(int v=1;v<V.size();v++)
                    if(R.getArea().getClimateObj().weatherType(R)==((Integer)V.elementAt(v)).intValue())
                        return false;
                break;
            }
            case 105: // -weather
                {
                    boolean found=false;
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getClimateObj().weatherType(R)==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 77: // +month
                {
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getMonth()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 78: // -month
                {
                    boolean found=false;
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getMonth()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 106: // +day
                {
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getDayOfMonth()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 107: // -day
                {
                    boolean found=false;
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getDayOfMonth()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 85: // +quallvl
	            {
	                Ability A=CMClass.getAbility((String)V.elementAt(1));
	                int adjustment=((Integer)V.elementAt(2)).intValue();
	        		int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
	        		int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
	        		if(lvl<clvl) return false;
	            }
                break;
            case 86: // -quallvl
                {
                    Ability A=CMClass.getAbility((String)V.elementAt(1));
                    int adjustment=((Integer)V.elementAt(2)).intValue();
            		int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
            		int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
            		if(lvl>clvl) return false;
                }
                break;
            case 51: // +resource
                if(V.contains(RawMaterial.RESOURCE_DESCS[(item.material()&RawMaterial.RESOURCE_MASK)]))
                    return false;
                break;
            case 52: // -resource
                if(!V.contains(RawMaterial.RESOURCE_DESCS[(item.material()&RawMaterial.RESOURCE_MASK)]))
                    return false;
                break;
            case 53: // -JavaClass
                {
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                        if(E.ID().equalsIgnoreCase((String)V.elementAt(v)))
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 54: // +JavaClass
                    for(int v=1;v<V.size();v++)
                        if(E.ID().equalsIgnoreCase((String)V.elementAt(v)))
                        { return false;}
                break;
			case 44: // -deity
				{
					if(mob.getWorshipCharID().length()==0) 
						return false;
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(mob.getWorshipCharID().equalsIgnoreCase((String)V.elementAt(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 45: // +deity
				if(mob.getWorshipCharID().length()>0) 
					for(int v=1;v<V.size();v++)
						if(mob.getWorshipCharID().equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				break;
			case 43: // -effects
				{
					boolean found=false;
					for(int a=0;a<E.numEffects();a++)
					{
						Ability A=E.fetchEffect(a);
						if(A!=null)
						for(int v=1;v<V.size();v++)
							if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
							{ found=true; break;}
					}
                    if(!found)
                    for(int a=0;a<E.numBehaviors();a++)
                    {
                        Behavior B=E.fetchBehavior(a);
                        if(B!=null)
                        for(int v=1;v<V.size();v++)
                            if(B.name().equalsIgnoreCase((String)V.elementAt(v)))
                            { found=true; break;}
                    }
					if(!found) return false;
				}
				break;
			case 46: // -faction
				{
			    	boolean found=false;
			    	for(int v=1;v<V.size();v++)
			    	    if(CMLib.factions().isFactionedThisWay(mob,(String)V.elementAt(v)))
			    	    { found=true; break;}
					if(!found) return false;
				}
				break;
			case 47: // +faction
				{
			    	for(int v=1;v<V.size();v++)
			    	    if(CMLib.factions().isFactionedThisWay(mob,(String)V.elementAt(v)))
			    	        return false;
				}
				break;
			case 42: // +effects
				for(int a=0;a<E.numEffects();a++)
				{
					Ability A=E.fetchEffect(a);
					if(A!=null)
					for(int v=1;v<V.size();v++)
						if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				}
                for(int a=0;a<E.numBehaviors();a++)
                {
                    Behavior B=E.fetchBehavior(a);
                    if(B!=null)
                    for(int v=1;v<V.size();v++)
                        if(B.name().equalsIgnoreCase((String)V.elementAt(v)))
                            return false;
                }
				break;
			case 16: // +name
				{
					String name=actual?E.Name():E.name();
					for(int v=1;v<V.size();v++)
                        if(name.equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				}
				break;
			case 17: // -anyclass
				{
					boolean found=false;
					if(actual)
					{
						for(int v=1;v<V.size();v++)
							if(mob.baseCharStats().getClassLevel((String)V.elementAt(v))>=0)
							{ found=true; break;}
					}
					else
					{
						for(int v=1;v<V.size();v++)
							if((mob.charStats().getClassLevel((String)V.elementAt(v))>=0)
							||(mob.charStats().displayClassName().equalsIgnoreCase((String)V.elementAt(v))))
							{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 18: // +anyclass
				if(actual)
				{
					for(int v=1;v<V.size();v++)
						if(mob.baseCharStats().getClassLevel((String)V.elementAt(v))>=0)
						{ return false;}
				}
				else
				{
					for(int v=1;v<V.size();v++)
						if((mob.charStats().getClassLevel((String)V.elementAt(v))>=0)
						||(mob.charStats().displayClassName().equalsIgnoreCase((String)V.elementAt(v))))
						{ return false; }
				}
				break;
			case 19: // +adjstr
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 20: // +adjint
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 21: // +adjwis
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 22: // +adjdex
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 23: // -adjcha
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 24: // +adjcha
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 25: // -adjstr
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 26: // -adjint
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 27: // -adjwis
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 28: // -adjdex
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 29: // -adjcon
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 30: // -adjcha
				if((V.size()>1)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 87: // +str
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_STRENGTH)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 88: // +int
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 89: // +wis
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_WISDOM)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 90: // +dex
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_DEXTERITY)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 91: // +con
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_CONSTITUTION)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 92: // +cha
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_CHARISMA)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 93: // -str
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_STRENGTH)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 94: // -int
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 95: // -wis
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_WISDOM)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 96: // -dex
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_DEXTERITY)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 97: // -con
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_CONSTITUTION)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 98: // -cha
		        base=getBaseCharStats(base,mob);
				if((V.size()>1)&&(base.getStat(CharStats.STAT_CHARISMA)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
            case 55: // +able
                if((V.size()>1)&&(E.envStats().ability()>(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 56: // -able
                if((V.size()>1)&&(E.envStats().ability()<(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 61: // +weight
                if((V.size()>1)&&(E.envStats().weight()>(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 62: // -weight
                if((V.size()>1)&&(E.envStats().weight()<(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 63: // +armor
                if((V.size()>1)&&(E.envStats().armor()>(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 64: // -armor
                if((V.size()>1)&&(E.envStats().armor()<(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 65: // +damage
                if((V.size()>1)&&(E.envStats().damage()>(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 66: // -damage
                if((V.size()>1)&&(E.envStats().damage()<(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 67: // +attack
                if((V.size()>1)&&(E.envStats().attackAdjustment()>(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 68: // -attack
                if((V.size()>1)&&(E.envStats().attackAdjustment()<(((Integer)V.elementAt(1)).intValue())))
                   return false;
                break;
            case 59: // +value
                if(mob!=null)
                {
                    if((V.size()>1)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)>(((Integer)V.elementAt(1)).intValue())))
                       return false;
                }
                else
                if(item!=null)
                {
                    if((V.size()>1)&&(item.baseGoldValue()>(((Integer)V.elementAt(1)).intValue())))
                        return false;
                }
                break;
            case 60: // -value
                if(mob!=null)
                {
                    if((V.size()>1)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)<(((Integer)V.elementAt(1)).intValue())))
                       return false;
                }
                else
                if(item!=null)
                {
                    if((V.size()>1)&&(item.baseGoldValue()<(((Integer)V.elementAt(1)).intValue())))
                        return false;
                }
                break;
			case 31: // -area
				{
					boolean found=false;
                    if(R!=null)
						for(int v=1;v<V.size();v++)
							if(R.getArea().Name().equalsIgnoreCase((String)V.elementAt(v)))
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 32: // +area
            {
				if(R!=null)
					for(int v=1;v<V.size();v++)
						if(R.getArea().Name().equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
                break;
            }
            case 100: // -home
            {
                boolean found=false;
                Area A=CMLib.map().getStartArea(E);
                if(A!=null)
                    for(int v=1;v<V.size();v++)
                        if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
                        { found=true; break;}
                if(!found) return false;
            }
            break;
            case 99: // +home
            {
                Area A=CMLib.map().getStartArea(E);
                if(A!=null)
                    for(int v=1;v<V.size();v++)
                        if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
                        { return false;}
                break;
            }
			case 33: // -item
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(mob.fetchInventory((String)V.elementAt(v))!=null)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
            case 48: // -worn
                if(mob!=null)
                {
                    boolean found=false;
                    for(int v=1;v<V.size();v++)
                    {
                        Item I=mob.fetchInventory((String)V.elementAt(v));
                        if((I!=null)&&(!I.amWearingAt(Item.IN_INVENTORY)))
                        { found=true; break;}
                    }
                    if(!found) return false;
                }
                else
                if(E instanceof Item)
                    if(((Item)E).amWearingAt(Item.IN_INVENTORY))
                        return false;
                break;
			case 34: // +class
				if(V.contains(actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
					return false;
				break;
			case 35: // +alignment
				if(V.contains(CMLib.flags().getAlignmentName(mob)))
					return false;
				break;
			case 36: // +gender
		        base=getBaseCharStats(base,mob);
				if(V.contains(actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+mob.charStats().genderName().charAt(0))))
					return false;
				break;
			case 37: // +lvlgr
				if((V.size()>1)&&((actual?E.baseEnvStats().level():E.envStats().level())>((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 38: // +lvlge
				if((V.size()>1)&&((actual?E.baseEnvStats().level():E.envStats().level())>=((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 39: // +lvlt
				if((V.size()>1)&&((actual?E.baseEnvStats().level():E.envStats().level())<((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 40: // +lvlle
				if((V.size()>1)&&((actual?E.baseEnvStats().level():E.envStats().level())<=((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 41: // +lvleq
				if((V.size()>1)&&((actual?E.baseEnvStats().level():E.envStats().level())==((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			}
            }catch(NullPointerException n){}
		}
		return true;
	}
	
}
