package com.planet_ink.coffee_mud.utils;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class SaucerSupport
{
	private SaucerSupport(){}
	protected final static int TRACK_ATTEMPTS=25;
	protected final static int TRACK_DEPTH=500;
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
			zapCodes.put("-INT",new Integer(26));
			zapCodes.put("-WIS",new Integer(27));
			zapCodes.put("-DEX",new Integer(28));
			zapCodes.put("-CON",new Integer(29));
			zapCodes.put("-CHA",new Integer(30));
			zapCodes.put("-STRENGTH",new Integer(25));
			zapCodes.put("-INTELLIGENCE",new Integer(26));
			zapCodes.put("-WISDOM",new Integer(27));
			zapCodes.put("-DEXTERITY",new Integer(28));
			zapCodes.put("-CONSTITUTION",new Integer(29));
			zapCodes.put("-CHARISMA",new Integer(30));
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
									+"+DEX X (<WORD> those with dexterity greater than X)";

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

	public static boolean tattooCheck(Vector V, char plusMinus, int fromHere, MOB mob)
	{
		Ability A=mob.fetchAffect("Prop_Tattoo");
		if(A==null) return false;
		String txt=A.text().toUpperCase();
		int x=txt.indexOf(";");
		if(x>=0)
		{
			String t=txt.substring(0,x).trim();
			txt=txt.substring(x+1).trim();
			if((t.length()>0)&&(fromHere(V,plusMinus,fromHere,t)))
				return true;
			x=txt.indexOf(";");
		}
		return false;
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
		if(mobRaceCat.length()>6) mobRaceCat=mobRaceCat.substring(0,6);
		String mobRace=mob.charStats().getMyRace().name().toUpperCase();
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

	public static boolean findTheRoom(Room location, 
									  Room destRoom, 
									  int tryCode, 
									  Vector dirVec,
									  Vector theTrail,
									  Hashtable lookedIn,
									  int depth,
									  boolean noWater)
	{
		if(lookedIn==null) return false;
		if(lookedIn.get(location)!=null) return false;
		if(depth>TRACK_DEPTH) return false;
		
		lookedIn.put(location,location);
		for(int x=0;x<dirVec.size();x++)
		{
			int i=((Integer)dirVec.elementAt(x)).intValue();
			Room nextRoom=location.getRoomInDir(i);
			Exit nextExit=location.getExitInDir(i);
			if((nextRoom!=null)
			&&(nextExit!=null)
			&&((!noWater)||(
			  (nextRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(nextRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))))
			{
				if((nextRoom==destRoom)
				||(findTheRoom(nextRoom,destRoom,tryCode,dirVec,theTrail,lookedIn,depth+1,noWater)))
				{
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}
	
	public static Vector findBastardTheBestWay(Room location, 
											   Vector destRooms,
											   boolean noWater)
	{
		
		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		Room trackArray[] = new Room[TRACK_ATTEMPTS];
		
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			Room roomToTry=(Room)destRooms.elementAt(Dice.roll(1,destRooms.size(),-1));
			Hashtable lookedIn=new Hashtable();
			Vector theTrail=new Vector();
			if(findTheRoom(location,roomToTry,2,dirVec,theTrail,lookedIn,0,noWater))
			{
				trailArray[t]=theTrail;
				trackArray[t]=roomToTry;
			}
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			Room which=trackArray[t];
			if((V!=null)&&(which!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}
		
		if(winner<0) 
			return null;
		else
			return trailArray[winner];
	}
	
	public static int trackNextDirectionFromHere(Vector theTrail, 
												 Room location,
												 boolean noWaterOrAir)
	{
		if((theTrail==null)||(location==null))
			return -1;
		if(location==theTrail.elementAt(0))
			return 999;

		Room nextRoom=null;
		int bestDirection=-1;
		int trailLength=Integer.MAX_VALUE;
		for(int dirs=0;dirs<Directions.NUM_DIRECTIONS;dirs++)
		{
			Room thisRoom=location.getRoomInDir(dirs);
			Exit thisExit=location.getExitInDir(dirs);
			if((thisRoom!=null)
			&&(thisExit!=null)
			&&((!noWaterOrAir)||(
			 	  (thisRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_AIR)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_AIR))))
			{
				for(int trail=0;trail<theTrail.size();trail++)
				{
					if((theTrail.elementAt(trail)==thisRoom)
					&&(trail<trailLength))
					{
						bestDirection=dirs;
						trailLength=trail;
						nextRoom=thisRoom;
					}
				}
			}
		}
		return bestDirection;
	}
	
	public static int radiatesFromDir(Room room, Vector rooms)
	{
		for(int i=0;i<rooms.size();i++)
		{
			Room R=(Room)rooms.elementAt(i);
			
			if(R==room) return -1;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(R.getRoomInDir(d)==room)
					return Directions.getOpDirectionCode(d);
		}
		return -1;
	}
	public static void getRadiantRooms(Room room, 
									   Vector rooms, 
									   boolean openOnly,
									   boolean areaOnly,
									   boolean noSkyPlease,
									   Room radiateTo,
									   int maxDepth)
	{
		int depth=0;
		if(room==null) return;
		if(rooms.contains(room)) return;
		HashSet H=new HashSet();
		rooms.addElement(room);
		for(int r=0;r<rooms.size();r++)
			H.add(rooms.elementAt(r));
		int min=0;
		int size=rooms.size();
		boolean radiateToSomewhere=(radiateTo!=null);
		while(depth<maxDepth)
		{
			for(int r=min;r<size;r++)
			{
				Room R1=(Room)rooms.elementAt(r);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					if((noSkyPlease)
					&&(R1.roomID().length()>0)
					&&(R1.rawDoors()[d]!=null)
					&&(R1.rawDoors()[d].roomID().length()==0))
						continue;
					
					Room R=R1.getRoomInDir(d);
					Exit E=R1.getExitInDir(d);
					if((R!=null)
					&&(E!=null)
					&&((!areaOnly)||(R.getArea()==room.getArea()))
					&&((!openOnly)||(E.isOpen()))
					&&(!H.contains(R)))
					{
						rooms.addElement(R);
						H.add(R);
						if((radiateToSomewhere)
						&&(R==radiateTo))
							return;
					}
				}
			}
			min=size;
			size=rooms.size();
			if(min==size) return;
			depth++;
		}
	}
	
	public static void extinguish(MOB source, Environmental target, boolean mundane)
	{
		if(target instanceof Room)
		{
			Room R=(Room)target;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null) SaucerSupport.extinguish(source,M,mundane);
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I!=null) SaucerSupport.extinguish(source,I,mundane);
			}
			return;
		}
		for(int a=target.numAffects()-1;a>=0;a--)
		{
			Ability A=target.fetchAffect(a);
			if((A!=null)&&((!mundane)||(A.classificationCode()==Ability.PROPERTY)))
			{
				if((Util.bset(A.flags(),Ability.FLAG_HEATING)&&(!mundane))
				||(Util.bset(A.flags(),Ability.FLAG_BURNING))
				||((A.ID().equalsIgnoreCase("Spell_SummonElemental")&&A.text().toUpperCase().indexOf("FIRE")>=0)))
					A.unInvoke();
			}
		}
		if((target instanceof MOB)&&(!mundane))
		{
			MOB tmob=(MOB)target;
			if(tmob.charStats().getMyRace().ID().equals("FireElemental"))
				ExternalPlay.postDeath(source,(MOB)target,null);
			for(int i=0;i<tmob.inventorySize();i++)
			{
				Item I=tmob.fetchInventory(i);
				if(I!=null) extinguish(tmob,I,mundane);
			}
		}
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,Host.LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}
	
	public static boolean beMobile(MOB mob, 
								   boolean dooropen, 
								   boolean wander,
								   boolean roomprefer, boolean roomobject, Vector rooms)
	{
		// ridden and following things aren't mobile!
		if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
		||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
			return false;

		Room oldRoom=mob.location();
		
		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB inhab=oldRoom.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.isASysOp(oldRoom)))
				return false;
		}
			
		if(oldRoom instanceof GridLocale)
		{
			Vector V=((GridLocale)oldRoom).getAllRooms();
			Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
			if(R!=null) R.bringMobHere(mob,true);
			oldRoom=mob.location();
		}

		int tries=0;
		int direction=-1;
		while((tries++<10)&&(direction<0))
		{
			direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
			Room nextRoom=oldRoom.getRoomInDir(direction);
			Exit nextExit=oldRoom.getExitInDir(direction);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				Exit opExit=nextRoom.getExitInDir(Directions.getOpDirectionCode(direction));
				for(int a=0;a<nextExit.numAffects();a++)
				{
					Ability aff=nextExit.fetchAffect(a);
					if((aff!=null)&&(aff instanceof Trap))
						direction=-1;
				}

				if(opExit!=null)
				{
					for(int a=0;a<opExit.numAffects();a++)
					{
						Ability aff=opExit.fetchAffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}
				}

				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!Sense.isInFlight(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
				||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
					direction=-1;
				else
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!Sense.isSwimming(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
					direction=-1;
				else
				if((!wander)&&(!oldRoom.getArea().Name().equals(nextRoom.getArea().Name())))
					direction=-1;
				else
				if((roomobject)&&(rooms!=null)&&(rooms.contains(nextRoom)))
					direction=-1;
				else
				if((roomprefer)&&(rooms!=null)&&(!rooms.contains(nextRoom)))
					direction=-1;
				else
					break;
			}
			else
				direction=-1;
		}

		if(direction<0)
			return false;

		Room nextRoom=oldRoom.getRoomInDir(direction);
		Exit nextExit=oldRoom.getExitInDir(direction);
		int opDirection=Directions.getOpDirectionCode(direction);
		if((nextRoom==null)||(nextExit==null))
			return false;
		
		boolean reclose=false;
		boolean relock=false;
		// handle doors!
		if(nextExit.hasADoor()&&(!nextExit.isOpen())&&(dooropen))
		{
			if((nextExit.hasALock())&&(nextExit.isLocked()))
			{
				FullMsg msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
				if(oldRoom.okAffect(mob,msg))
				{
					relock=true;
					msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
					if(oldRoom.okAffect(mob,msg))
						ExternalPlay.roomAffectFully(msg,oldRoom,direction);
				}
			}
			if(!nextExit.isOpen())
			{
				try{ExternalPlay.doCommand(mob,Util.parse("OPEN "+Directions.getDirectionName(direction)));}catch(Exception e){}
				if(nextExit.isOpen())
					reclose=true;
			}
		}
		if(!nextExit.isOpen())
			return false;

		if(((nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		   &&(!Sense.isWaterWorthy(mob))
		   &&(!Sense.isInFlight(mob))
		   &&(mob.fetchAbility("Skill_Swim")!=null))
		{
			Ability A=mob.fetchAbility("Skill_Swim");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)	A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if((nextRoom.ID().indexOf("Surface")>0)
		&&(!Sense.isClimbing(mob))
		&&(!Sense.isInFlight(mob))
		&&(mob.fetchAbility("Skill_Climb")!=null))
		{
			Ability A=mob.fetchAbility("Skill_Climb");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)	A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if(mob.fetchAbility("Thief_Sneak")!=null)
		{
			Ability A=mob.fetchAbility("Thief_Sneak");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)
			{
				A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
				Ability A2=mob.fetchAbility("Thief_Hide");
				if(A2!=null)
					A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			}
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
			ExternalPlay.move(mob,direction,false,false);
		
		if((reclose)&&(mob.location()==nextRoom)&&(dooropen))
		{
			Exit opExit=nextRoom.getExitInDir(opDirection);
			if((opExit!=null)
			&&(opExit.hasADoor())
			&&(opExit.isOpen()))
			{
				try{ExternalPlay.doCommand(mob,Util.parse("CLOSE "+Directions.getDirectionName(opDirection)));}catch(Exception e){}
				if((opExit.hasALock())&&(relock))
				{
					FullMsg msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
					if(nextRoom.okAffect(mob,msg))
					{
						msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_LOCK,Affect.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
						if(nextRoom.okAffect(mob,msg))
							ExternalPlay.roomAffectFully(msg,nextRoom,opDirection);
					}
				}
			}
		}
		return mob.location()!=oldRoom;
	}
	
	
	  public static Hashtable timsItemAdjustments(Item I, 
												int level,
												int material,
												int weight,
												int hands,
												int wclass,
												int reach,
												long worndata)
	{
		Hashtable vals=new Hashtable();
		int materialvalue=EnvResource.RESOURCE_DATA[material&EnvResource.RESOURCE_MASK][1];
		if(I instanceof Weapon)
		{
			int baseattack=0;
			int basereach=0;
			int maxreach=0;
			int basematerial=EnvResource.MATERIAL_WOODEN;
			if(wclass==Weapon.CLASS_FLAILED) baseattack=-5;
			if(wclass==Weapon.CLASS_POLEARM){ basereach=1; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_RANGED){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_THROWN){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_EDGED){ baseattack=10; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_DAGGER){ baseattack=10; basematerial=EnvResource.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_SWORD){ basematerial=EnvResource.MATERIAL_METAL;}
			if(weight==0) weight=10;
			if(basereach>maxreach) maxreach=basereach;
			if(reach<basereach)
			{ 
				reach=basereach;
				vals.put("MINRANGE",""+basereach);
				vals.put("MAXRANGE",""+maxreach);
			}
			else
			if(reach>basereach)
				basereach=reach;
			int damage=((level-1)/((reach/weight)+2) + (weight-baseattack)/5 -reach)*((hands+1)/2);
			int cost=2*((weight*materialvalue)+((5*damage)+baseattack+(reach*10))*damage)/(hands+1);
				
			if(basematerial==EnvResource.MATERIAL_METAL)
			{
				switch(material&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_MITHRIL:
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_ENERGY:
					break;
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_PLASTIC:
					damage-=4;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_PRECIOUS:
					damage-=4;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_LEATHER:
					damage-=6;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_ROCK:
					damage-=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_GLASS:
					damage-=4;
					baseattack-=20;
					break;
				default:
					damage-=8;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case EnvResource.RESOURCE_BALSA:
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case EnvResource.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case EnvResource.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
				case EnvResource.RESOURCE_IRONWOOD:
					baseattack+=10;
					damage+=2;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(basematerial==EnvResource.MATERIAL_WOODEN)
			{
				switch(material&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_WOODEN:
				case EnvResource.MATERIAL_ENERGY:
					break;
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_MITHRIL:
					damage+=2;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_PRECIOUS:
					damage+=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_LEATHER:
				case EnvResource.MATERIAL_PLASTIC:
					damage-=2;
					baseattack-=0;
					break;
				case EnvResource.MATERIAL_ROCK:
					damage+=2;
					baseattack-=10;
					break;
				case EnvResource.MATERIAL_GLASS:
					damage-=2;
					baseattack-=10;
					break;
				default:
					damage-=6;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case EnvResource.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case EnvResource.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
					baseattack+=10;
					damage+=2;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(damage<=0) damage=1;
			
			vals.put("DAMAGE",""+damage);
			vals.put("ATTACK",""+baseattack);
			vals.put("VALUE",""+cost);
		}
		else
		if(I instanceof Armor)
		{
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,50,60,70,80,90};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,47};
			double pts=0.0;
			if(level<0) level=0;
			int materialCode=material&EnvResource.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_LEATHER:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			default:
				useArray=clothPoints;
				break;
			}
			if(level>=useArray[useArray.length-1])
				pts=new Integer(useArray.length-2).doubleValue();
			else
			for(int i=0;i<useArray.length;i++)
			{
				int lvl=useArray[i];
				if(lvl>level)
				{
					pts=new Integer(i-1).doubleValue();
					break;
				}
			}
				   
			double totalpts=0.0;
			double weightpts=0.0;
			for(int i=0;i<Item.wornWeights.length-1;i++)
			{
				if(Util.isSet(worndata,i))
				{
					totalpts+=(pts*Item.wornWeights[i+1]);
					switch(materialCode)
					{
					case EnvResource.MATERIAL_METAL:
					case EnvResource.MATERIAL_MITHRIL:
					case EnvResource.MATERIAL_PRECIOUS:
						weightpts+=Item.wornHeavyPts[i+1][2];
						break;
					case EnvResource.MATERIAL_LEATHER:
					case EnvResource.MATERIAL_GLASS:
					case EnvResource.MATERIAL_PLASTIC:
					case EnvResource.MATERIAL_ROCK:
					case EnvResource.MATERIAL_WOODEN:
						weightpts+=Item.wornHeavyPts[i+1][1];
						break;
					case EnvResource.MATERIAL_ENERGY:
						break;
					default:
						weightpts+=Item.wornHeavyPts[i+1][0];
						break;
					}
					if(hands==1) break;
				}
			}
			int cost=(int)Math.round(((pts*pts) + new Integer(materialvalue).doubleValue()) 
									 * ( weightpts / 2));
			int armor=(int)Math.round(totalpts);
			switch(material)
			{
				case EnvResource.RESOURCE_BALSA:
				case EnvResource.RESOURCE_LIMESTONE:
				case EnvResource.RESOURCE_FLINT:
					armor-=1;
					break;
				case EnvResource.RESOURCE_CLAY:
					armor-=2;
					break;
				case EnvResource.RESOURCE_BONE:
					armor+=2;
					break;
				case EnvResource.RESOURCE_GRANITE:
				case EnvResource.RESOURCE_OBSIDIAN:
				case EnvResource.RESOURCE_IRONWOOD:
					armor+=1;
					break;
				case EnvResource.RESOURCE_SAND:
				case EnvResource.RESOURCE_COAL:
					armor-=4;
					break;
			}
			vals.put("ARMOR",""+armor);
			vals.put("VALUE",""+cost);
			vals.put("WEIGHT",""+(int)Math.round(weightpts));
		}
		return vals;
	}
	
	
	
}
