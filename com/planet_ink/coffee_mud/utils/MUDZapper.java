package com.planet_ink.coffee_mud.utils;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
		}
		return zapCodes;
	}

	private static final String ZAP ="+SYSOP (allow archons or area subops to bypass the rules)  <BR>"
									+"-SYSOP (always <WORD> archons and area subops)  <BR>"
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
									+"-ANYCLASS (<WORD> all multi-class combinations)  <BR>"
									+"+thief +mage +ranger (exceptions -anyclass, allow any levels) <BR>"
									+"+ANYCLASS (do not <WORD> all multi-class combinations)  <BR>"
									+"-thief -mage -ranger (exceptions to +anyclass, disallow any levels) <BR>"
									+"-STR X (<WORD> those with strength less than X)  <BR>"
									+"+STR X (<WORD> those with strength greater than X)  <BR>"
									+"-INT X (<WORD> those with intelligence less than X)  <BR>"
									+"+INT X (<WORD> those with intelligence greater than X)  <BR>"
									+"-WIS X (<WORD> those with wisdom less than X)  <BR>"
									+"+WIS X (<WORD> those with wisdom greater than X)  <BR>"
									+"-CON X (<WORD> those with constitution less than X)  <BR>"
									+"+CON X (<WORD> those with constitution greater than X)  <BR>"
									+"-CHA X (<WORD> those with charisma less than X)  <BR>"
									+"+CHA X (<WORD> those with charisma greater than X)  <BR>"
									+"-DEX X (<WORD> those with dexterity less than X)  <BR>"
									+"+DEX X (<WORD> those with dexterity greater than X) <BR>"
									+"-AREA (<WORD> in all areas) <BR>"
									+"\"+my areaname\" etc.. (create exceptions to +area) <BR>"
									+"+AREA (do not <WORD> any areas) <BR>"
									+"\"-my areaname\" etc.. (create exceptions to -area) <BR>"
									+"+ITEM \"+item name\" etc... (<WORD> only those with an item name)";

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
			if(fromHere(V,plusMinus,fromHere,(String)mob.fetchTattoo(v)))
				return true;
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
	
	
	public static boolean nameCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		Vector names=Util.parse(mob.name().toUpperCase());
		for(int v=0;v<names.size();v++)
			if(fromHere(V,plusMinus,fromHere,(String)names.elementAt(v)))
				return true;
		names=Util.parse(mob.displayText().toUpperCase());
		for(int v=0;v<names.size();v++)
			if(fromHere(V,plusMinus,fromHere,(String)names.elementAt(v)))
				return true;
		return false;
	}

	public static boolean areaCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		if((mob==null)||(mob.location()==null)) return false;
		Area A=mob.location().getArea();
		if(A==null) return false;
		return fromHere(V,plusMinus,fromHere,A.name());
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
	
	public static boolean fromHere(Vector V, char plusMinus, int fromHere, String find)
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
						if(fromHere(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))
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
							if((C.ID().equals(C.baseClass())
							&&(fromHere(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))))
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
							&&(fromHere(V,'+',v+1,cat))))
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
							&&(fromHere(V,'+',v+1,cat))))
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
						for(int c=0;c<=1000;c+=500)
						{
							String C=CommonStrings.shortAlignmentStr(c);
							if(fromHere(V,'+',v+1,C.toUpperCase().substring(0,3)))
								buf.append(C+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 4: // -Gender
					{
						buf.append("Allows only ");
						if(fromHere(V,'+',v+1,"MALE"))
							buf.append("Male, ");
						if(fromHere(V,'+',v+1,"FEMALE"))
							buf.append("Female, ");
						if(fromHere(V,'+',v+1,"FEMALE"))
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str.startsWith("+")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str.startsWith("+")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
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
							if((C.ID().equals(C.baseClass())
							&&(fromHere(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))))
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
							if((C.ID().equals(C.baseClass())
							&&(fromHere(V,'+',v+1,Util.padRight(C.name(),4).toUpperCase().trim()))))
								buf.append(C.name()+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuffer(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 19: // -str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a strength of at least "+val+".");
					break;
				case 20: // -int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a intelligence of at least "+val+".");
					break;
				case 21: // -wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a wisdom of at least "+val+".");
					break;
				case 22: // -dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a dexterity of at least "+val+".");
					break;
				case 23: // -con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a constitution of at least "+val+".");
					break;
				case 24: // -cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a charisma of at least "+val+".");
					break;
				case 25: // +str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a strength of at most "+val+".");
					break;
				case 26: // +int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a intelligence of at most "+val+".");
					break;
				case 27: // +wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a wisdom of at most "+val+".");
					break;
				case 28: // +dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a dexterity of at most "+val+".");
					break;
				case 29: // +con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a constitution of at most "+val+".");
					break;
				case 30: // +cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					buf.append("Requires a charisma of at most "+val+".");
					break;
				case 31: // +Area
					{
						buf.append("Disallows the following area(s): ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str.startsWith("-")))
								buf.append(str2+", ");
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
							String str2=(String)V.elementAt(v);
							if((!zapCodes.containsKey(str2))&&(str2.startsWith("-")))
								buf.append(str2+", ");
						}
						if(buf.toString().endsWith(", "))
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
				for(int c=0;c<=1000;c+=500)
				{
					String C=CommonStrings.shortAlignmentStr(c);
					if(str.startsWith("-"+C.toUpperCase().substring(0,3)))
					   buf.append("Disallows "+C+".  ");
				}
				if(str.startsWith("-MALE"))
					buf.append("Disallows Males.  ");
				if(str.startsWith("-FEMALE"))
					buf.append("Disallows Females.  ");
				if(str.startsWith("-NEUTER"))
					buf.append("Allows only Males and Females.  ");
				buf.append(levelHelp(str,'-',"Disallows "));
			}
		}

		if(buf.length()==0) buf.append("Anyone.");
		return buf.toString();
	}

	public static boolean zapperCheck(String text, MOB mob)
	{
		if(mob==null) return true;
		if(mob.charStats()==null) return true;
		if(text.trim().length()==0) return true;
		getZapCodes();

		String mobClass=Util.padRight(mob.charStats().displayClassName(),4).toUpperCase().trim();
		String mobRaceCat=mob.charStats().getMyRace().racialCategory().toUpperCase();
		if(!mob.charStats().getMyRace().name().equals(mob.charStats().raceName()))
		{
			Race R=CMClass.getRace(mob.charStats().raceName());
			if(R!=null) mobRaceCat=R.racialCategory().toUpperCase();
			else mobRaceCat=mob.charStats().raceName().toUpperCase();
		}
		if(mobRaceCat.length()>6) mobRaceCat=mobRaceCat.substring(0,6);
		String mobRace=mob.charStats().raceName().toUpperCase();
		if(mobRace.length()>6) mobRace=mobRace.substring(0,6);
		String mobAlign=CommonStrings.shortAlignmentStr(mob.getAlignment()).toUpperCase().substring(0,3);
		String mobGender=mob.charStats().genderName().toUpperCase();
		int level=mob.envStats().level();
		int classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());

		Vector V=Util.parse(text.toUpperCase());
		if(mob.isASysOp(mob.location()))
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if(str.equals("+SYSOP")) return true;
			else
			if(str.equals("-SYSOP")) return false;
		}
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			int val=-1;
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					if(!fromHere(V,'+',v+1,mobClass)) return false;
					break;
				case 1: // -baseclass
					if((!fromHere(V,'+',v+1,Util.padRight(mob.charStats().getCurrentClass().baseClass(),4).toUpperCase().trim()))
					&&(!fromHere(V,'+',v+1,mobClass))) return false;
					break;
				case 2: // -Race
					if(!fromHere(V,'+',v+1,mobRace))
						return false;
					break;
				case 3: // -Alignment
					if(!fromHere(V,'+',v+1,mobAlign)) return false;
					break;
				case 4: // -Gender
					if(!fromHere(V,'+',v+1,mobGender)) return false;
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
					if(!nameCheck(V,'+',v+1,mob)) return false;
					break;
				case 10: // -Player
					if(!mob.isMonster()) return false;
					break;
				case 11: // -MOB
					if(mob.isMonster()) return false;
					break;
				case 12: // -Racecat
					if(!fromHere(V,'+',v+1,mobRaceCat))
						return false;
					break;
				case 13: // +Racecat
					if(fromHere(V,'-',v+1,mobRaceCat))
						return false;
					break;
				case 14: // -Clan
					if((mob.getClanID().length()==0)
					||(!fromHere(V,'+',v+1,mob.getClanID().toUpperCase())))
						return false;
					break;
				case 15: // +Clan
					if((mob.getClanID().length()>0)
					&&(fromHere(V,'-',v+1,mob.getClanID().toUpperCase())))
						return false;
					break;
				case 16: // +names
					if(nameCheck(V,'-',v+1,mob))
						return false;
					break;
				case 17: // -anyclass
					{
						boolean found=false;
						for(int c=0;c<mob.charStats().numClasses();c++)
							if(fromHere(V,'+',v+1,Util.padRight(mob.charStats().getMyClass(c).name(),4).toUpperCase().trim()))
								found=true;
						if(!found) return false;
					}
					break;
				case 18: // +anyclass
					for(int c=0;c<mob.charStats().numClasses();c++)
						if(fromHere(V,'-',v+1,Util.padRight(mob.charStats().getMyClass(c).name(),4).toUpperCase().trim()))
							return false;
					break;
				case 19: // -str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.STRENGTH)<val)
						return false;
					break;
				case 20: // -int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.INTELLIGENCE)<val)
						return false;
					break;
				case 21: // -wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.WISDOM)<val)
						return false;
					break;
				case 22: // -dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.DEXTERITY)<val)
						return false;
					break;
				case 23: // -con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CONSTITUTION)<val)
						return false;
					break;
				case 24: // -cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CHARISMA)<val)
						return false;
					break;
				case 25: // +str
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.STRENGTH)>val)
						return false;
					break;
				case 26: // +int
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.INTELLIGENCE)>val)
						return false;
					break;
				case 27: // +wis
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.WISDOM)>val)
						return false;
					break;
				case 28: // +dex
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.DEXTERITY)>val)
						return false;
					break;
				case 29: // +con
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CONSTITUTION)>val)
						return false;
					break;
				case 30: // +cha
					val=((++v)<V.size())?Util.s_int((String)V.elementAt(v)):0;
					if(mob.charStats().getStat(CharStats.CHARISMA)>val)
						return false;
					break;
				case 31: // +area
					if(areaCheck(V,'-',v+1,mob))
						return false;
					break;
				case 32: // -area
					if(!areaCheck(V,'+',v+1,mob)) return false;
					break;
				case 33: // +item
					if(!itemCheck(V,'+',v+1,mob)) return false;
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
		}
		return true;
	}

}
