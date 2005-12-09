package com.planet_ink.coffee_mud.utils;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class MUDZapper
{
	private MUDZapper(){}
	
	private static Hashtable zapCodes=new Hashtable();

	private static Hashtable getZapCodes()
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
			zapCodes.put("+STR",new Integer(19));
			zapCodes.put("+INT",new Integer(20));
			zapCodes.put("+WIS",new Integer(21));
			zapCodes.put("+DEX",new Integer(22));
			zapCodes.put("+CON",new Integer(23));
			zapCodes.put("+CHA",new Integer(24));
			zapCodes.put("+STRENGTH",new Integer(19));
			zapCodes.put("+INTELLIGENCE",new Integer(20));
			zapCodes.put("+WISDOM",new Integer(21));
			zapCodes.put("+DEXTERITY",new Integer(22));
			zapCodes.put("+CONSTITUTION",new Integer(23));
			zapCodes.put("+CHARISMA",new Integer(24));
			zapCodes.put("-STR",new Integer(25));
			zapCodes.put("-STRENGTH",new Integer(25));
			zapCodes.put("-INT",new Integer(26));
			zapCodes.put("-INTELLIGENCE",new Integer(26));
			zapCodes.put("-WIS",new Integer(27));
			zapCodes.put("-WISDOM",new Integer(27));
			zapCodes.put("-DEX",new Integer(28));
			zapCodes.put("-DEXTERITY",new Integer(28));
			zapCodes.put("-CON",new Integer(29));
			zapCodes.put("-CONSTITUTION",new Integer(29));
			zapCodes.put("-CHA",new Integer(30));
			zapCodes.put("-CHARISMA",new Integer(30));
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
            zapCodes.put("+JAVACLASS",new Integer(53));
            zapCodes.put("-JAVACLASS",new Integer(54));
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
		}
		return zapCodes;
	}

	private static final String ZAP =
        "+SYSOP (allow archons or area staff to bypass the rules)  <BR>"
		+"-SYSOP (always <WORD> archons and area staff)  <BR>"
		+"-PLAYER (<WORD> all players) <BR>"
		+"-MOB (<WORD> all mobs/npcs)  <BR>"
		+"-CLASS  (<WORD> all classes)  <BR>"
		+"-BASECLASS  (<WORD> all base classes)  <BR>"
		+"+thief +mage +ranger (create exceptions to -class and -baseclass) <BR>"
		+"-thief -mage  -ranger (<WORD> only listed classes)<BR>"
		+"-RACE (<WORD> all races)  <BR>"
		+"+elf +dwarf +human +half +gnome (create exceptions to -race)  <BR>"
		+"-elf -dwarf -human -half -gnome (<WORD> only listed races)  <BR>"
		+"-RACECAT (<WORD> all racial categories)  <BR>"
		+"+RACECAT (do not <WORD> all racial categories)  <BR>"
		+"+elf +insect +humanoid +canine +gnome (create exceptions to -racecat)  <BR>"
		+"-elf -insect -humanoid -canine -gnome (create exceptions to +racecat)  <BR>"
		+"-ALIGNMENT (<WORD> all alignments)  <BR>"
		+"+evil +good +neutral (create exceptions to -alignment)  <BR>"
		+"-evil -good -neutral (<WORD> only listed alignments)  <BR>"
		+"-GENDER (<WORD> all genders)  <BR>"
		+"+male +female +neuter (create exceptions to -gender)  <BR>"
		+"-male -female -neuter (<WORD> only listed genders)  <BR>"
		+"-FACTION (<WORD> all faction and values, even a lack of faction) <BR>"
		+"+myfactionrange +myotherfactionrange (create exceptions to -faction) <BR>"
		+"-myfactionrange -myotherfactionrange (<WORD> only named faction range)<BR>"
		+"-TATTOOS (<WORD> all tattoos, even a lack of a tatoo) <BR>"
		+"+mytatto +thistattoo +anytattoo etc..  (create exceptions to -tattoos) <BR>"
		+"+TATTOOS (do not <WORD> any or no tattoos) <BR>"
		+"-mytattoo -anytatto, etc.. (create exceptions to +tattoos) <BR>"
		+"-LEVEL (<WORD> all levels)  <BR>"
		+"+=1 +>5 +>=7 +<13 +<=20 (create exceptions to -level using level ranges)  <BR>"
		+"-=1 ->5 ->=7 -<13 -<=20 (<WORD> only listed levels range) <BR>"
		+"-NAMES (<WORD> everyone) <BR>"
		+"+bob \"+my name\" etc.. (create name exceptions to -names) <BR>"
		+"+NAMES (do not <WORD> anyone who has a name) <BR>"
		+"-bob \"-my name\" etc.. (create name exceptions to +names) <BR>"
		+"-CLAN (<WORD> anyone, even no clan) <BR>"
		+"+Killers \"+Holy Avengers\" etc.. (create clan exceptions to -clan) <BR>"
		+"+CLAN (do not <WORD> anyone, even non clan people) <BR>"
		+"-Killers \"-Holy Avengers\" etc.. (create clan exceptions to +clan) <BR>"
		+"-DEITY (<WORD> anyone, even no deity) <BR>"
		+"+Apollo \"+Grothon The Great\" etc.. (create deity exceptions to -deity) <BR>"
		+"+DEITY (do not <WORD> anyone, even non deity worshipping people) <BR>"
		+"-Apollo \"-rothon The Great\" etc.. (create deity exceptions to +deity) <BR>"
		+"-ANYCLASS (<WORD> all multi-class combinations)  <BR>"
		+"+thief +mage +ranger (exceptions -anyclass, allow any levels) <BR>"
		+"+ANYCLASS (do not <WORD> all multi-class combinations)  <BR>"
		+"-thief -mage -ranger (exceptions to +anyclass, disallow any levels) <BR>"
		+"-STR X (<WORD> those with strength greater than X)  <BR>"
		+"+STR X (<WORD> those with strength less than X)  <BR>"
		+"-INT X (<WORD> those with intelligence greater than X)  <BR>"
		+"+INT X (<WORD> those with intelligence less than X)  <BR>"
		+"-WIS X (<WORD> those with wisdom greater than X)  <BR>"
		+"+WIS X (<WORD> those with wisdom less than X)  <BR>"
		+"-CON X (<WORD> those with constitution greater than X)  <BR>"
		+"+CON X (<WORD> those with constitution less than X)  <BR>"
		+"-CHA X (<WORD> those with charisma greater than X)  <BR>"
		+"+CHA X (<WORD> those with charisma less than X)  <BR>"
		+"-DEX X (<WORD> those with dexterity greater than X)  <BR>"
		+"+DEX X (<WORD> those with dexterity less than X) <BR>"
		+"-AREA (<WORD> in all areas) <BR>"
		+"\"+my areaname\" etc.. (create exceptions to +area) <BR>"
		+"+AREA (do not <WORD> any areas) <BR>"
		+"\"-my areaname\" etc.. (create exceptions to -area) <BR>"
		+"-ITEM \"+item name\" etc... (<WORD> only those with an item name) <BR>"
        +"-WORN \"+item name\" etc... (<WORD> only those wearing item name) <BR>"
		+"-EFFECTS (<WORD> anyone, even no effects) <BR>"
		+"+Sleep \"+Wood Chopping\" etc.. (create name exceptions to -effects) <BR>"
		+"+EFFECTS (do not <WORD> anyone, even non effected people) <BR>"
		+"-Sleep \"-Wood Chopping\" etc.. (create name exceptions to +effects) <BR>"
        +"-MATERIAL \"+WOODEN\" etc.. (<WORN> only items of added materials) <BR>"
        +"+MATERIAL \"-WOODEN\" etc.. (Do not <WORN> items of subtracted materials) <BR>"
        +"-RESOURCES \"+OAK\" etc.. (<WORN> only items of added resources) <BR>"
        +"+RESOURCES \"-OAK\" etc.. (Do not <WORN> items of subtracted resources) <BR>"
        +"-JAVACLASS \"+GENMOB\" etc.. (<WORN> only objects of added java class) <BR>"
        +"+JAVACLASS \"-GENITEM\" etc.. (Do not <WORN> objs of subtracted classes) <BR>"
        +"-RESOURCES \"+OAK\" etc.. (<WORN> only items of added resources) <BR>"
        +"+RESOURCES \"-OAK\" etc.. (Do not <WORN> items of subtracted resources) <BR>"
        +"-ABILITY X (<WORD> those with magical ability less than X)  <BR>"
        +"+ABILITY X (<WORD> those with magical ability greater than X) <BR>"
        +"-VALUE X (<WORD> those with value or money less than X)  <BR>"
        +"+VALUE X (<WORD> those with value or money greater than X) <BR>"
        +"-WEIGHT X (<WORD> those weighing less than X)  <BR>"
        +"+WEIGHT X (<WORD> those weighing more than X) <BR>"
        +"-ARMOR X (<WORD> those with armor bonus less than X)  <BR>"
        +"+ARMOR X (<WORD> those with armor bonus more than X) <BR>"
        +"-DAMAGE X (<WORD> those with damage bonus less than X)  <BR>"
        +"+DAMAGE X (<WORD> those with damage bonus more than X) <BR>"
        +"-ATTACK X (<WORD> those with attack bonus less than X)  <BR>"
        +"+ATTACK X (<WORD> those with attack bonus more than X) <BR>"
        +"-WORNON \"+TORSO\" etc.. (<WORN> only items wearable on added locs) <BR>"
        +"+WORNON \"-NECK\" etc.. (Do not <WORN> items wearable on subtracted locs) <BR>"
        +"-DISPOSITION \"+ISHIDDEN\" etc.. (<WORN> only with added dispositions) <BR>"
        +"+DISPOSITION \"-ISHIDDEN\" etc.. (Do not <WORN> only with sub disp) <BR>"
        +"-SENSES \"+CANSEEDARK\" etc.. (<WORN> only those with added sens.) <BR>"
        +"+SENSES \"-CANSEEDARK\" etc.. (Do not <WORN> those with subtracted sens.) <BR>"
        +"-HOUR X (<WORD> always, unless the hour is X)  <BR>"
        +"+HOUR X (<WORD> those only when the hour is X) <BR>"
        +"-SEASON FALL (<WORD> those only when season is FALL)  <BR>"
        +"+SEASON SPRING (<WORD> those whenever the season is SPRING) <BR>"
        +"-MONTH X (<WORD> those only when month number is X)  <BR>"
        +"+MONTH X (<WORD> those whenever the month number is X)";

	public static String zapperInstructions(String CR, String word)
	{
		String copy=new String(ZAP);
		if((CR!=null)&&(!CR.equalsIgnoreCase("<BR>")))
			copy=Util.replaceAll(copy,"<BR>",CR);
		if((word==null)||(word.length()==0))
			copy=Util.replaceAll(copy,"<WORD>","disallow");
		else
			copy=Util.replaceAll(copy,"<WORD>",word);
		return copy;
	}


	public static boolean tattooCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		for(int v=0;v<mob.numTattoos();v++)
		{
			String tattoo=mob.fetchTattoo(v);
			if((tattoo!=null)
			&&(tattoo.length()>0)
			&&(Character.isDigit(tattoo.charAt(0)))
			&&(tattoo.indexOf(" ")>0)
			&&(Util.isNumber(tattoo.substring(0,tattoo.indexOf(" ")))))
			   tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
			if(fromHereStartsWith(V,plusMinus,fromHere,tattoo))
				return true;
		}
		return false;
	}

	private static boolean levelCheck(String text, char prevChar, int lastPlace, int lvl)
	{
		int x=0;
		while(x>=0)
		{
			x=text.indexOf(">",lastPlace);
			if(x<0)	x=text.indexOf("<",lastPlace);
			if(x<0)	x=text.indexOf("=",lastPlace);
			if(x>=0)
			{
				char prev='+';
				if(x>0) prev=text.charAt(x-1);

				char primaryChar=text.charAt(x);
				x++;
				boolean andEqual=false;
				if(text.charAt(x)=='=')
				{
					andEqual=true;
					x++;
				}
				lastPlace=x;

				if(prev==prevChar)
				{
					boolean found=false;
					String cmpString="";
					while((x<text.length())&&
						  (((text.charAt(x)==' ')&&(cmpString.length()==0))
						   ||(Character.isDigit(text.charAt(x)))))
					{
						if(Character.isDigit(text.charAt(x)))
							cmpString+=text.charAt(x);
						x++;
					}
					if(cmpString.length()>0)
					{
						int cmpLevel=Util.s_int(cmpString);
						if((cmpLevel==lvl)&&(andEqual))
							found=true;
						else
						switch(primaryChar)
						{
						case '>': found=(lvl>cmpLevel); break;
						case '<': found=(lvl<cmpLevel); break;
						case '=': found=(lvl==cmpLevel); break;
						}
					}
					if(found) return true;
				}
			}
		}
		return false;
	}

	public static Vector levelCompiledHelper(String str, char c, Vector entry)
	{
		if(entry==null) entry=new Vector();
		if(str.startsWith(c+">="))
		{
			entry.addElement(zapCodes.get("+LVLGE"));
			entry.addElement(new Integer(Util.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+"<="))
		{
			entry.addElement(zapCodes.get("+LVLLE"));
			entry.addElement(new Integer(Util.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+">"))
		{
			entry.addElement(zapCodes.get("+LVLGR"));
			entry.addElement(new Integer(Util.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"<"))
		{
			entry.addElement(zapCodes.get("+LVLLT"));
			entry.addElement(new Integer(Util.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"="))
		{
			entry.addElement(zapCodes.get("+LVLEQ"));
			entry.addElement(new Integer(Util.s_int(str.substring(2).trim())));
		}
		return entry;
	}
	
	public static StringBuffer levelHelp(String str, char c, String append)
	{
		if(str.startsWith(c+">="))
			return new StringBuffer(append+"levels greater than or equal to "+str.substring(3).trim()+".  ");
		else
		if(str.startsWith(c+"<="))
			return new StringBuffer(append+"levels less than or equal to "+str.substring(3).trim()+".  ");
		else
		if(str.startsWith(c+">"))
			return new StringBuffer(append+"levels greater than "+str.substring(2).trim()+".  ");
		else
		if(str.startsWith(c+"<"))
			return new StringBuffer(append+"levels less than "+str.substring(2).trim()+".  ");
		else
		if(str.startsWith(c+"="))
			return new StringBuffer(append+"level "+str.substring(2).trim()+" players.  ");
		return new StringBuffer("");
	}
	
	
	public static boolean fromHereEqual(Vector V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.length()==0) continue;
			if(zapCodes.containsKey(str))
				return false;
			if(str.equalsIgnoreCase(plusMinus+find)) return true;
		}
		return false;
	}

	public static boolean factionCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=((String)V.elementAt(v)).toUpperCase();
			if(str.length()>0)
			{
				if(zapCodes.containsKey(str))
					return false;
				if((str.charAt(0)==plusMinus)
				&&(Factions.isFactionedThisWay(mob,str.substring(1))))
				    return true;
			}
		}
		return false;
	}

	public static boolean nameCheck(Vector V, char plusMinus, int fromHere, Environmental E)
	{
        if(fromHereEqual(V,plusMinus,fromHere,E.name()))
            return true;
		Vector names=Util.parse(E.name().toUpperCase());
		for(int v=0;v<names.size();v++)
			if(fromHereEqual(V,plusMinus,fromHere,(String)names.elementAt(v)))
				return true;
        if(fromHereEqual(V,plusMinus,fromHere,E.displayText()))
            return true;
		names=Util.parse(E.displayText().toUpperCase());
		for(int v=0;v<names.size();v++)
			if(fromHereEqual(V,plusMinus,fromHere,(String)names.elementAt(v)))
				return true;
		return false;
	}

	public static boolean areaCheck(Vector V, char plusMinus, int fromHere, Environmental E)
	{
        Room R=CoffeeUtensils.roomLocation(E);
        if(R==null) return false;
		Area A=R.getArea();
		if(A==null) return false;
		return fromHereStartsWith(V,plusMinus,fromHere,A.name());
	}

	public static boolean itemCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		if((mob==null)||(mob.location()==null)) return false;
		for(int v=fromHere;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.length()==0) continue;
			if(zapCodes.containsKey(str))
				return false;
			if(mob.fetchInventory(str)!=null)
				return true;
		}
		return false;
	}
	
    public static boolean wornCheck(Vector V, char plusMinus, int fromHere, MOB mob)
    {
        if((mob==null)||(mob.location()==null)) return false;
        Item I=null;
        for(int v=fromHere;v<V.size();v++)
        {
            String str=(String)V.elementAt(v);
            if(str.length()==0) continue;
            if(zapCodes.containsKey(str))
                return false;
            I=mob.fetchInventory(str);
            if((I!=null)&&(!I.amWearingAt(Item.INVENTORY)))
                return true;
        }
        return false;
    }
	public static boolean fromHereStartsWith(Vector V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.length()==0) continue;
			if(zapCodes.containsKey(str))
				return false;
			if(str.startsWith(plusMinus+find)) return true;
		}
		return false;
	}

    public static boolean fromHereEndsWith(Vector V, char plusMinus, int fromHere, String find)
    {
        for(int v=fromHere;v<V.size();v++)
        {
            String str=(String)V.elementAt(v);
            if(str.length()==0) continue;
            if(zapCodes.containsKey(str))
                return false;
            if((str.charAt(0)==plusMinus)&&str.endsWith(find))
                return true;
        }
        return false;
    }
	public static String zapperDesc(String text)
	{
		if(text.trim().length()==0) return "Anyone";
		StringBuffer buf=new StringBuffer("");
		getZapCodes();
		Vector V=Util.parse(text.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			int val=-1;
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					{
					buf.append("Allows only ");
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						if(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
							buf.append(C.name()+", ");
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuffer(buf.substring(0,buf.length()-2));
					buf.append(".  ");
					}
					break;
				case 1: // -baseclass
					{
						buf.append("Allows only ");
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if((C.ID().equals(C.baseClass()))
							&&(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim())))
								buf.append(C.name()+" types, ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 2: // -Race
					{
						buf.append("Allows only ");
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
						buf.append("Allows only these racial categories ");
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
						buf.append("Allows only ");
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
						buf.append("Allows only ");
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
							buf.append(levelHelp((String)V.elementAt(v2),'+',"Allows only "));
					}
					break;
				case 6: // -ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp((String)V.elementAt(v2),'+',"Allows only class "));
					}
					break;
				case 7: // -Tattoos
					{
						buf.append("Requires the following tattoo(s): ");
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
				case 14: // -Clan
					{
						buf.append("Requires membership in the following clan(s): ");
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
                        buf.append("Requires construction from the following material(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CoffeeUtensils.getMaterialCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Util.capitalizeAndLower(EnvResource.MATERIAL_DESCS[code>>8])+", ");
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
                                int code=CoffeeUtensils.getMaterialCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Util.capitalizeAndLower(EnvResource.MATERIAL_DESCS[code>>8])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 58: // -wornon
                    {
                        buf.append("Requires ability to be worn: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CoffeeUtensils.getWornCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Item.wornLocation[code]+", ");
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
                                int code=CoffeeUtensils.getWornCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Item.wornLocation[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 71: // -senses
                    {
                        buf.append("Requires the following sense(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=Sense.getSensesCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.sensesDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 72: // +senses
                    {
                        buf.append("Disallows the following sense(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=Sense.getSensesCode(str2.substring(1));
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
                                buf.append(Util.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 74: // -HOUR
                    {
                        buf.append("Allowed only during the following time(s) of the day: ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(Util.s_int(str2.substring(1).trim())+", ");
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
                                if(Util.isInteger(str2.substring(1).trim()))
                                {
                                    int season=Util.s_int(str2.substring(1).trim());
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
                        buf.append("Allowed only during the following season(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                if(Util.isInteger(str2.substring(1).trim()))
                                {
                                    int season=Util.s_int(str2.substring(1).trim());
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
                case 77: // +month
                    {
                        buf.append("Disallowed during the following month(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                                buf.append(Util.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 78: // -month
                    {
                        buf.append("Allowed only during the following month(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                                buf.append(Util.s_int(str2.substring(1).trim())+", ");
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 69: // -disposition
                    {
                        buf.append("Requires the following disposition(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=Sense.getDispositionCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(EnvStats.dispositionsDesc[code]+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 70: // +disposition
                    {
                        buf.append("Disallows the following disposition(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("-"))
                            {
                                int code=Sense.getDispositionCode(str2.substring(1));
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
                        buf.append("Requires construction from the following materials(s): ");
                        for(int v2=v+1;v2<V.size();v2++)
                        {
                            String str2=(String)V.elementAt(v2);
                            if(zapCodes.containsKey(str2))
                                break;
                            if(str2.startsWith("+"))
                            {
                                int code=CoffeeUtensils.getResourceCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Util.capitalizeAndLower(EnvResource.RESOURCE_DESCS[code&EnvResource.RESOURCE_MASK])+", ");
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
                                int code=CoffeeUtensils.getResourceCode(str2.substring(1));
                                if(code>=0)
                                    buf.append(Util.capitalizeAndLower(EnvResource.RESOURCE_DESCS[code&EnvResource.RESOURCE_MASK])+", ");
                            }
                        }
                        if(buf.toString().endsWith(", "))
                            buf=new StringBuffer(buf.substring(0,buf.length()-2));
                        buf.append(".  ");
                    }
                    break;
                case 53: // -JavaClass
                    {
                        buf.append("Requires being of the following type: ");
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
						buf.append("Requires worshipping in the following deity(s): ");
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
						buf.append("Requires the following name(s): ");
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
						buf.append("Requires levels in one of the following:  ");
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
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
							if(fromHereStartsWith(V,'-',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
								buf.append(C.name()+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 19: // +str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a strength of at least "+val+".");
					break;
				case 20: // +int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a intelligence of at least "+val+".");
					break;
				case 21: // +wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a wisdom of at least "+val+".");
					break;
				case 22: // +dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a dexterity of at least "+val+".");
					break;
				case 23: // +con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a constitution of at least "+val+".");
					break;
				case 24: // +cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a charisma of at least "+val+".");
					break;
				case 25: // -str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a strength of at most "+val+".");
					break;
				case 26: // -int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a intelligence of at most "+val+".");
					break;
				case 27: // -wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a wisdom of at most "+val+".");
					break;
				case 28: // -dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a dexterity of at most "+val+".");
					break;
				case 29: // -con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a constitution of at most "+val+".");
					break;
				case 30: // -cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a charisma of at most "+val+".");
					break;
                case 55: // +able
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a magic/ability of at most "+val+".");
                    break;
                case 56: // -able
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a magic/ability of at least "+val+".");
                    break;
                case 59: // +value
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a value of at most "+val+".");
                    break;
                case 60: // -value
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a value of at least "+val+".");
                    break;
                case 61: // +weight
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a weight/encumbrance of at most "+val+".");
                    break;
                case 62: // -weight
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a weight/encumbrance of at least "+val+".");
                    break;
                case 63: // +armor
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a armor rating of at most "+val+".");
                    break;
                case 64: // -armor
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a armor rating of at least "+val+".");
                    break;
                case 65: // +damage
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a damage ability of at most "+val+".");
                    break;
                case 66: // -damage
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires a damage ability of at least "+val+".");
                    break;
                case 67: // +attack
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires an attack bonus of at most "+val+".");
                    break;
                case 68: // -attack
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    buf.append("Requires an attack bonus of at least "+val+".");
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
						buf.append("Requires the following area(s): ");
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
						buf.append("Requires the following item(s): ");
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
                    buf.append("Requires wearing the following item(s): ");
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
						buf.append("Requires participation in the following activities/effects(s): ");
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
				    buf.append("Requires the following: ");
				    for(int v2=v+1;v2<V.size();v2++)
				    {
				        String str2=(String)V.elementAt(v2);
				        if(zapCodes.containsKey(str2))
				            break;
				        if((str2.startsWith("+"))
				        &&(Factions.isRangeCodeName(str2.substring(1))))
				        {
				            String desc=Factions.rangeDescription(str2.substring(1),"or ");
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
					if(str.startsWith("-"+Util.padRight(C.name(),4).toUpperCase().trim()))
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
					buf.append("Allows only Males and Females.  ");
				buf.append(levelHelp(str,'-',"Disallows "));
				if((str.startsWith("-"))
		        &&(Factions.isRangeCodeName(str.substring(1))))
				{
		            String desc=Factions.rangeDescription(str.substring(1),"and ");
		            if(desc.length()>0) buf.append("Disallows "+desc);
				}
			}
		}

		if(buf.length()==0) buf.append("Anyone.");
		return buf.toString();
	}

	public static Vector zapperCompile(String text)
	{
		Vector buf=new Vector();
		if(text.trim().length()==0) return buf;
		getZapCodes();
		Vector V=Util.parse(text.toUpperCase());
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
							if(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
								entry.addElement(C.name());
						}
					}
					break;
				case 1: // -baseclass
					{
						Vector entry=new Vector();
						buf.addElement(entry);
						entry.addElement(zapCodes.get(str));
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if((C.ID().equals(C.baseClass())
							&&(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))))
								entry.addElement(C.baseClass());
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
                        }
					}
					break;
				case 7: // -Tattoos
				case 14: // -Clan
				case 44: // -Deity
				case 43: // -Effect
				case 9: // -Names
				case 32: // -Area
                case 54: // -JavaClass
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
				        &&(Factions.isRangeCodeName(str2.substring(1))))
							entry.addElement(str2.substring(1).toUpperCase());
					}
					break;
				}
				case 8: // +Tattoos
				case 15: // +Clan
				case 45: // +Deity
				case 42: // +Effect
				case 16: // +Names
				case 31: // +Area
                case 53: // +JavaClass
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
                                int code=CoffeeUtensils.getMaterialCode(str2.substring(1));
                                if(code>=0)
                                    entry.addElement(EnvResource.MATERIAL_DESCS[(code&EnvResource.MATERIAL_MASK)>>8]);
                            }
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
                                int code=CoffeeUtensils.getWornCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer(Util.pow(2,code-1)));
                            }
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
                                int code=Sense.getDispositionCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer(Util.pow(2,code)));
                            }
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
                                int code=Sense.getSensesCode(str2.substring(1));
                                if(code>=0) entry.addElement(new Integer(Util.pow(2,code)));
                            }
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
                                if(Util.isInteger(str2.substring(1).trim()))
                                    entry.addElement(new Integer(Util.s_int(str2.substring(1).trim())));
                                else
                                if(CMClass.globalClock().determineSeason(str2.substring(1).trim())>=0)
                                    entry.addElement(new Integer(CMClass.globalClock().determineSeason(str2.substring(1).trim())));
                            }
                        }
                    }
                    break;
                case 73: // +HOUR
                case 74: // -HOUR
                case 77: // +MONTH
                case 78: // -MONTH
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
                                entry.addElement(new Integer(Util.s_int(str2.substring(1).trim())));
                        }
                    }
                    break;
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
                                int code=CoffeeUtensils.getResourceCode(str2.substring(1));
                                if(code>=0)
                                    entry.addElement(EnvResource.RESOURCE_DESCS[(code&EnvResource.RESOURCE_MASK)]);
                            }
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
							if(fromHereStartsWith(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
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
							if(fromHereStartsWith(V,'-',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
								entry.addElement(C.name());
						}
					}
					break;
				case 19: // +str
				case 20: // +int
				case 21: // +wis
				case 22: // +dex
				case 23: // +con
				case 24: // +cha
				case 25: // -str
				case 26: // -int
				case 27: // -wis
				case 28: // -dex
				case 29: // -con
				case 30: // -cha
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
						val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
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
					if(str.startsWith("-"+Util.padRight(C.name(),4).toUpperCase().trim()))
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
		        &&(Factions.isRangeCodeName(str.substring(1))))
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
		return buf;
	}

	public static boolean zapperCheckReal(Vector cset, Environmental E)
	{
		if(E==null) return true;
		if((cset==null)||(cset.size()==0)) return true;
		getZapCodes();
        MOB mob=(E instanceof MOB)?(MOB)E:null;
        Item item=(E instanceof Item)?(Item)E:null;
		for(int c=0;c<cset.size();c++)
		{
			Vector V=(Vector)cset.elementAt(c);
            try
            {
			if(V.size()>0)
			switch(((Integer)V.firstElement()).intValue())
			{
			case 0: // -class
				if(!V.contains(mob.baseCharStats().getCurrentClass().name()))
					return false;
				break;
			case 1: // -baseclass
				if(!V.contains(mob.baseCharStats().getCurrentClass().baseClass()))
					return false;
				break;
			case 2: // -race
				if(!V.contains(mob.baseCharStats().getMyRace().name()))
					return false;
				break;
			case 3: // -alignment
				if(!V.contains(Sense.getAlignmentName(mob)))
					return false;
				break;
			case 4: // -gender
				if(!V.contains(""+((char)mob.baseCharStats().getStat(CharStats.GENDER))))
					return false;
				break;
			case 5: // -level
				{
					boolean found=false;
					for(int v=1;v<V.size();v+=2)
						if((v+1)<V.size())
						switch(((Integer)V.elementAt(v)).intValue())
						{
							case 37: // +lvlgr
								if(E.baseEnvStats().level()>((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(E.baseEnvStats().level()>=((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(E.baseEnvStats().level()<((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(E.baseEnvStats().level()<=((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(E.baseEnvStats().level()==((Integer)V.elementAt(v+1)).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
			case 6: // -classlevel
				{
					boolean found=false;
					int cl=mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass());
					for(int v=1;v<V.size();v+=2)
						if((v+1)<V.size())
						switch(((Integer)V.elementAt(v)).intValue())
						{
							case 37: // +lvlgr
								if((V.size()>1)&&(cl>((Integer)V.elementAt(1)).intValue()))
								   found=true;
								break;
							case 38: // +lvlge
								if((V.size()>1)&&(cl>=((Integer)V.elementAt(1)).intValue()))
								   found=true;
								break;
							case 39: // +lvlt
								if((V.size()>1)&&(cl<((Integer)V.elementAt(1)).intValue()))
								   found=true;
								break;
							case 40: // +lvlle
								if((V.size()>1)&&(cl<=((Integer)V.elementAt(1)).intValue()))
								   found=true;
								break;
							case 41: // +lvleq
								if((V.size()>1)&&(cl==((Integer)V.elementAt(1)).intValue()))
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
						if(mob.fetchTattoo((String)V.elementAt(v))!=null)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 8: // +tattoo
				{
					for(int v=1;v<V.size();v++)
						if(mob.fetchTattoo((String)V.elementAt(v))!=null)
						{ return false;}
				}
				break;
			case 9: // -names
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(E.Name().equalsIgnoreCase((String)V.elementAt(v)))
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
				if(!V.contains(mob.baseCharStats().getMyRace().racialCategory()))
					return false;
				break;
			case 13: // +racecat
				if(V.contains(mob.baseCharStats().getMyRace().racialCategory()))
					return false;
				break;
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
                if(V.contains(EnvResource.MATERIAL_DESCS[(item.material()&EnvResource.MATERIAL_MASK)>>8]))
                    return false;
                break;
            case 50: // -material
                if(!V.contains(EnvResource.MATERIAL_DESCS[(item.material()&EnvResource.MATERIAL_MASK)>>8]))
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
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getTimeOfDay()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 74: // -HOUR
                {
                    boolean found=false;
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getTimeOfDay()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 75: // +season
                {
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getSeasonCode()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 76: // -season
                {
                    boolean found=false;
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getSeasonCode()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 77: // +month
                {
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getMonth()==((Integer)V.elementAt(v)).intValue())
                            return false;
                    break;
                }
            case 78: // -month
                {
                    boolean found=false;
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
                    for(int v=1;v<V.size();v++)
                        if(R.getArea().getTimeObj().getMonth()==((Integer)V.elementAt(v)).intValue())
                        { found=true; break;}
                    if(!found) return false;
                }
                break;
            case 51: // +resource
                if(V.contains(EnvResource.RESOURCE_DESCS[(item.material()&EnvResource.RESOURCE_MASK)]))
                    return false;
                break;
            case 52: // -resource
                if(!V.contains(EnvResource.RESOURCE_DESCS[(item.material()&EnvResource.RESOURCE_MASK)]))
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
					if(E.numEffects()==0) 
						return false;
					boolean found=false;
					for(int a=0;a<E.numEffects();a++)
					{
						Ability A=E.fetchEffect(a);
						if(A!=null)
						for(int v=1;v<V.size();v++)
							if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
							{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 46: // -faction
				{
			    	boolean found=false;
			    	for(int v=1;v<V.size();v++)
			    	    if(Factions.isFactionedThisWay(mob,(String)V.elementAt(v)))
			    	    { found=true; break;}
					if(!found) return false;
				}
				break;
			case 47: // +faction
				{
			    	for(int v=1;v<V.size();v++)
			    	    if(Factions.isFactionedThisWay(mob,(String)V.elementAt(v)))
			    	        return false;
				}
				break;
			case 42: // +effects
				if(E.numEffects()>0) 
				for(int a=0;a<E.numEffects();a++)
				{
					Ability A=E.fetchEffect(a);
					if(A!=null)
					for(int v=1;v<V.size();v++)
						if(A.Name().equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				}
				break;
			case 16: // +name
				{
					for(int v=1;v<V.size();v++)
                        if(E.Name().equalsIgnoreCase((String)V.elementAt(v)))
						{ return false;}
				}
				break;
			case 17: // -anyclass
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(mob.baseCharStats().getClassLevel((String)V.elementAt(v))>=0)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 18: // +anyclass
				for(int v=1;v<V.size();v++)
					if(mob.baseCharStats().getClassLevel((String)V.elementAt(v))>=0)
					{ return false;}
				break;
			case 19: // +str
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.STRENGTH)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 20: // +int
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 21: // +wis
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.WISDOM)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 22: // +dex
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.DEXTERITY)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 23: // +con
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 24: // +cha
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.CHARISMA)<(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 25: // -str
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.STRENGTH)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 26: // -int
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 27: // -wis
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.WISDOM)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 28: // -dex
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.DEXTERITY)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 29: // -con
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.CONSTITUTION)>(((Integer)V.elementAt(1)).intValue())))
				   return false;
				break;
			case 30: // -cha
				if((V.size()>1)&&(mob.baseCharStats().getStat(CharStats.CHARISMA)>(((Integer)V.elementAt(1)).intValue())))
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
                    if((V.size()>1)&&(BeanCounter.getTotalAbsoluteValueAllCurrencies(mob)>(((Integer)V.elementAt(1)).intValue())))
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
                    if((V.size()>1)&&(BeanCounter.getTotalAbsoluteValueAllCurrencies(mob)<(((Integer)V.elementAt(1)).intValue())))
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
                    Room R=CoffeeUtensils.roomLocation(E);
                    if(R!=null)
						for(int v=1;v<V.size();v++)
							if(R.getArea().Name().equalsIgnoreCase((String)V.elementAt(v)))
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 32: // +area
            {
                Room R=CoffeeUtensils.roomLocation(E);
				if(R!=null)
					for(int v=1;v<V.size();v++)
						if(R.getArea().Name().equalsIgnoreCase((String)V.elementAt(v)))
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
                        if((I!=null)&&(!I.amWearingAt(Item.INVENTORY)))
                        { found=true; break;}
                    }
                    if(!found) return false;
                }
                else
                if(E instanceof Item)
                    if(((Item)E).amWearingAt(Item.INVENTORY))
                        return false;
                break;
			case 34: // +class
				if(V.contains(mob.baseCharStats().getCurrentClass().name()))
					return false;
				break;
			case 35: // +alignment
				if(V.contains(Sense.getAlignmentName(mob)))
					return false;
				break;
			case 36: // +gender
				if(V.contains(""+((char)mob.baseCharStats().getStat(CharStats.GENDER))))
					return false;
				break;
			case 37: // +lvlgr
				if((V.size()>1)&&(E.baseEnvStats().level()>((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 38: // +lvlge
				if((V.size()>1)&&(E.baseEnvStats().level()>=((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 39: // +lvlt
				if((V.size()>1)&&(E.baseEnvStats().level()<((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 40: // +lvlle
				if((V.size()>1)&&(E.baseEnvStats().level()<=((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			case 41: // +lvleq
				if((V.size()>1)&&(E.baseEnvStats().level()==((Integer)V.elementAt(1)).intValue()))
				   return false;
				break;
			}
            }catch(NullPointerException n){}
		}
		return true;
	}
	
	public static boolean zapperCheck(String text, Environmental E)
	{
		if(E==null) return true;
		if(text.trim().length()==0) return true;
		getZapCodes();

        String mobClass=null;
        String mobRaceCat=null;
        String mobRace=null;
        String mobAlign=null;
        String mobGender=null;
        int classLevel=-1;
        Vector V=Util.parse(text.toUpperCase());
        MOB mob=(E instanceof MOB)?(MOB)E:null;
        Item item=(E instanceof Item)?(Item)E:null;
        String clanID=null;
        
        if(mob!=null)
        {
            if(mob.charStats()==null) return true;
            mobClass=Util.padRight(mob.charStats().displayClassName(),4).toUpperCase().trim();
            mobRaceCat=mob.charStats().getMyRace().racialCategory().toUpperCase();
    		if(!mob.charStats().getMyRace().name().equals(mob.charStats().raceName()))
    		{
    			Race R=CMClass.getRace(mob.charStats().raceName());
    			if(R!=null) mobRaceCat=R.racialCategory().toUpperCase();
    			else mobRaceCat=mob.charStats().raceName().toUpperCase();
    		}
    		if(mobRaceCat.length()>6) mobRaceCat=mobRaceCat.substring(0,6);
    		mobRace=mob.charStats().raceName().toUpperCase();
    		if(mobRace.length()>6) mobRace=mobRace.substring(0,6);
    		mobAlign=Sense.getAlignmentName(mob).substring(0,3);
    		mobGender=mob.charStats().genderName().toUpperCase();
            classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
            if(CMSecurity.isASysOp(mob)
            ||((mob.location()!=null)&&(mob.location().getArea().amISubOp(mob.Name()))))
                for(int v=0;v<V.size();v++)
                {
                    String str=(String)V.elementAt(v);
                    if(str.equals("+SYSOP")) return true;
                    else
                    if(str.equals("-SYSOP")) return false;
                }
        }
		int level=E.envStats().level();
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			int val=-1;
            try
            {
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					if(!fromHereStartsWith(V,'+',v+1,mobClass)) return false;
					break;
				case 1: // -baseclass
					if((!fromHereStartsWith(V,'+',v+1,Util.padRight(mob.charStats().getCurrentClass().baseClass(),4).toUpperCase().trim()))
					&&(!fromHereStartsWith(V,'+',v+1,mobClass))) return false;
					break;
				case 2: // -Race
					if(!fromHereStartsWith(V,'+',v+1,mobRace))
						return false;
					break;
				case 3: // -Alignment
					if(!fromHereStartsWith(V,'+',v+1,mobAlign)) return false;
					break;
				case 4: // -Gender
					if(!fromHereStartsWith(V,'+',v+1,mobGender)) return false;
					break;
				case 5: // -Levels
					if(!levelCheck(Util.combine(V,v+1),'+',0,level)) return false;
					break;
				case 6: // -ClassLevels
					if(!levelCheck(Util.combine(V,v+1),'+',0,classLevel)) return false;
					break;
				case 7: // -tattoos
					if(!tattooCheck(V,'+',v+1,mob)) return false;
					break;
				case 8: // +tattoos
					if(tattooCheck(V,'-',v+1,mob)) return false;
					break;
				case 9: // -names
					if(!nameCheck(V,'+',v+1,E)) return false;
					break;
				case 10: // -Player
					if(!mob.isMonster()) return false;
					break;
				case 11: // -MOB
					if(mob.isMonster()) return false;
					break;
				case 12: // -Racecat
					if(!fromHereStartsWith(V,'+',v+1,mobRaceCat))
						return false;
					break;
				case 13: // +Racecat
					if(fromHereStartsWith(V,'-',v+1,mobRaceCat))
						return false;
					break;
				case 14: // -Clan
                {
                    if(clanID==null) clanID=(mob!=null)?mob.getClanID():(E instanceof ClanItem)?((ClanItem)E).clanID():"";
					if((clanID.length()==0)
					||(!fromHereStartsWith(V,'+',v+1,clanID.toUpperCase())))
						return false;
					break;
                }
				case 15: // +Clan
                {
                    if(clanID==null) clanID=(mob!=null)?mob.getClanID():(E instanceof ClanItem)?((ClanItem)E).clanID():"";
					if((clanID.length()>0)
					&&(fromHereStartsWith(V,'-',v+1,clanID.toUpperCase())))
						return false;
					break;
                }
				case 44: // -Deity
					if((mob.getWorshipCharID().length()==0)
					||(!fromHereStartsWith(V,'+',v+1,mob.getWorshipCharID().toUpperCase())))
						return false;
					break;
				case 45: // +Deity
					if((mob.getWorshipCharID().length()>0)
					&&(fromHereStartsWith(V,'-',v+1,mob.getWorshipCharID().toUpperCase())))
						return false;
					break;
                case 49: // +material
                    if(fromHereStartsWith(V,'-',v+1,EnvResource.MATERIAL_DESCS[(item.material()&EnvResource.MATERIAL_MASK)>>8]))
                        return false;
                    break;
                case 50: // -material
                    if(!fromHereStartsWith(V,'+',v+1,EnvResource.MATERIAL_DESCS[(item.material()&EnvResource.MATERIAL_MASK)>>8]))
                        return false;
                    break;
                case 51: // +resource
                    if(fromHereStartsWith(V,'-',v+1,EnvResource.RESOURCE_DESCS[(item.material()&EnvResource.RESOURCE_MASK)]))
                        return false;
                    break;
                case 52: // -resource
                    if(!fromHereStartsWith(V,'+',v+1,EnvResource.RESOURCE_DESCS[(item.material()&EnvResource.RESOURCE_MASK)]))
                        return false;
                    break;
                case 53: // -JavaClass
                    if(!fromHereStartsWith(V,'+',v+1,E.ID().toUpperCase()))
                        return false;
                    break;
                case 54: // +JavaClass
                    if(fromHereStartsWith(V,'-',v+1,E.ID().toUpperCase()))
                        return false;
                    break;
				case 16: // +names
					if(nameCheck(V,'-',v+1,E))
						return false;
					break;
				case 17: // -anyclass
					{
						boolean found=false;
						for(int c=0;c<mob.charStats().numClasses();c++)
							if((fromHereStartsWith(V,'+',v+1,Util.padRight(mob.charStats().getMyClass(c).name(),4).toUpperCase().trim()))
                            ||(fromHereStartsWith(V,'+',v+1,Util.padRight(mob.charStats().getMyClass(c).name(mob.charStats().getClassLevel(mob.charStats().getMyClass(c))),4).toUpperCase().trim())))
								found=true;
						if(!found) return false;
					}
					break;
				case 18: // +anyclass
					for(int c=0;c<mob.charStats().numClasses();c++)
						if((fromHereStartsWith(V,'-',v+1,Util.padRight(mob.charStats().getMyClass(c).name(),4).toUpperCase().trim()))
                        ||(fromHereStartsWith(V,'-',v+1,Util.padRight(mob.charStats().getMyClass(c).name(mob.charStats().getClassLevel(mob.charStats().getMyClass(c))),4).toUpperCase().trim())))
							return false;
					break;
				case 19: // +str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.STRENGTH)<val)
						return false;
					break;
				case 20: // +int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.INTELLIGENCE)<val)
						return false;
					break;
				case 21: // +wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.WISDOM)<val)
						return false;
					break;
				case 22: // +dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.DEXTERITY)<val)
						return false;
					break;
				case 23: // +con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CONSTITUTION)<val)
						return false;
					break;
				case 24: // +cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CHARISMA)<val)
						return false;
					break;
				case 25: // -str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.STRENGTH)>val)
						return false;
					break;
				case 26: // -int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.INTELLIGENCE)>val)
						return false;
					break;
				case 27: // -wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.WISDOM)>val)
						return false;
					break;
				case 28: // -dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.DEXTERITY)>val)
						return false;
					break;
				case 29: // -con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CONSTITUTION)>val)
						return false;
					break;
				case 30: // -cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CHARISMA)>val)
						return false;
					break;
                case 55: // +able
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().ability()>val)
                        return false;
                    break;
                case 56: // -able
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().ability()<val)
                        return false;
                    break;
                case 59: // +value
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if((mob!=null)&&(BeanCounter.getTotalAbsoluteValueAllCurrencies(mob)>val))
                        return false;
                    else
                    if((item!=null)&&(item.baseGoldValue()>val))
                        return false;
                    break;
                case 60: // -value
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if((mob!=null)&&(BeanCounter.getTotalAbsoluteValueAllCurrencies(mob)<val))
                        return false;
                    else
                    if((item!=null)&&(item.baseGoldValue()<val))
                        return false;
                    break;
                case 61: // +weight
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().weight()>val)
                        return false;
                    break;
                case 62: // -weight
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().weight()<val)
                        return false;
                    break;
                case 63: // +armor
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().armor()>val)
                        return false;
                    break;
                case 64: // -armor
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().armor()<val)
                        return false;
                    break;
                case 65: // +damage
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().damage()>val)
                        return false;
                    break;
                case 66: // -damage
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().damage()<val)
                        return false;
                    break;
                case 67: // +attack
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().attackAdjustment()>val)
                        return false;
                    break;
                case 68: // -attack
                    val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
                    if(E.envStats().attackAdjustment()<val)
                        return false;
                    break;
				case 31: // +area
					if(areaCheck(V,'-',v+1,E))
						return false;
					break;
				case 32: // -area
					if(!areaCheck(V,'+',v+1,E)) return false;
					break;
				case 33: // +item
					if(!itemCheck(V,'-',v+1,mob)) return false;
                    if(!itemCheck(V,'+',v+1,mob)) return false;
					break;
                case 48: // +worn
                    if(mob!=null)
                    {
                        if(!wornCheck(V,'+',v+1,mob)) 
                            return false;
                        if(!wornCheck(V,'-',v+1,mob)) 
                            return false;
                    }
                    else
                    if(E instanceof Item)
                        if(((Item)E).amWearingAt(Item.INVENTORY))
                            return false;
                    break;
				case 43: // -Effects
				{
					if(E.numEffects()==0) return false;
					boolean found=false;
					for(int a=0;a<E.numEffects();a++)
					{
						Ability A=E.fetchEffect(a);
						if((A!=null)&&(fromHereStartsWith(V,'+',v+1,A.Name().toUpperCase())))
						{ found=true; break;}
					}
					if(!found) return false;
					break;
				}
				case 42: // +Effects
					for(int a=0;a<E.numEffects();a++)
					{
						Ability A=E.fetchEffect(a);
						if((A!=null)&&(fromHereStartsWith(V,'-',v+1,A.Name().toUpperCase())))
							return false;
					}
					break;
				case 46: // -Faction
					if(!factionCheck(V,'+',v+1,mob)) 
					    return false;
					break;
                case 57: // +wornOn
                    for(int i=0;i<Item.wornCodes.length;i++)
                        if(((item.rawProperLocationBitmap()&Item.wornCodes[i])>0)
                        &&(fromHereEndsWith(V,'-',v+1,Item.wornLocation[i].toUpperCase())))
                            return false;
                    break;
                case 58: // -wornOn
                    {
                        boolean found=false;
                        for(int i=0;i<Item.wornCodes.length;i++)
                            if(((item.rawProperLocationBitmap()&Item.wornCodes[i])>0)
                            &&(fromHereEndsWith(V,'+',v+1,Item.wornLocation[i].toUpperCase())))
                            { found=true; break;}
                        if(!found) return false;
                    }
                    break;
                case 69: // +disposition
                    for(int i=0;i<EnvStats.dispositionsNames.length;i++)
                        if((Util.isSet(E.envStats().disposition(),i))
                        &&(fromHereEndsWith(V,'-',v+1,EnvStats.dispositionsNames[i])))
                            return false;
                    break;
                case 70: // -disposition
                    {
                        boolean found=false;
                        for(int i=0;i<EnvStats.dispositionsNames.length;i++)
                            if((Util.isSet(E.envStats().disposition(),i))
                            &&(fromHereEndsWith(V,'+',v+1,EnvStats.dispositionsNames[i])))
                            { found=true; break;}
                        if(!found) return false;
                    }
                    break;
                case 71: // +senses
                    for(int i=0;i<EnvStats.sensesNames.length;i++)
                        if((Util.isSet(E.envStats().sensesMask(),i))
                        &&(fromHereEndsWith(V,'-',v+1,EnvStats.sensesNames[i])))
                            return false;
                    break;
                case 72: // -senses
                    {
                        boolean found=false;
                        for(int i=0;i<EnvStats.sensesNames.length;i++)
                            if((Util.isSet(E.envStats().sensesMask(),i))
                            &&(fromHereEndsWith(V,'+',v+1,EnvStats.sensesNames[i])))
                            { found=true; break;}
                        if(!found) return false;
                    }
                    break;
                case 73: // +hour
                    {
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&(fromHereEndsWith(V,'-',v+1,""+R.getArea().getTimeObj().getTimeOfDay())))
                            return false;
                        break;
                    }
                case 74: // -hour
                    {
                        boolean found=false;
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&(fromHereEndsWith(V,'+',v+1,""+R.getArea().getTimeObj().getTimeOfDay())))
                        { found=true;}
                        if(!found) return false;
                    }
                    break;
                case 75: // +season
                    {
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&((fromHereEndsWith(V,'-',v+1,""+R.getArea().getTimeObj().getSeasonCode()))
                            ||(fromHereEndsWith(V,'-',v+1,TimeClock.SEASON_DESCS[R.getArea().getTimeObj().getSeasonCode()]))))
                            return false;
                        break;
                    }
                case 76: // -season
                    {
                        boolean found=false;
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&((fromHereEndsWith(V,'+',v+1,""+R.getArea().getTimeObj().getSeasonCode()))
                            ||(fromHereEndsWith(V,'+',v+1,TimeClock.SEASON_DESCS[R.getArea().getTimeObj().getSeasonCode()]))))
                            { found=true;}
                        if(!found) return false;
                    }
                    break;
                case 77: // +month
                    {
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&(fromHereEndsWith(V,'-',v+1,""+R.getArea().getTimeObj().getMonth())))
                            return false;
                        break;
                    }
                case 78: // -month
                    {
                        boolean found=false;
                        Room R=CoffeeUtensils.roomLocation(E);
                        if((R!=null)
                        &&(fromHereEndsWith(V,'+',v+1,""+R.getArea().getTimeObj().getMonth())))
                        { found=true;}
                        if(!found) return false;
                    }
                    break;
				}
			else
			if(str.startsWith("-"+mobClass)) return false;
			else
			if(str.startsWith("-"+mobRace)) return false;
			else
			if(str.startsWith("-"+mobAlign)) return false;
			else
			if(str.startsWith("-"+mobGender)) return false;
			else
			if(levelCheck(str,'-',0,level)) return false;
			else
			if(str.startsWith("-")
            &&(mob!=null)
			&&(Factions.isFactionedThisWay(mob,str.substring(1))))
				return false;
            }
            catch(NullPointerException n){}
		}
		return true;
	}

}
