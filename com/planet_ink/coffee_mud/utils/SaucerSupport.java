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
		}
		return zapCodes;
	}

	private static final String ZAP ="+sysop (allow archons or area subops to bypass the rules)  <BR> "
									+"-sysop (always <WORD> archons and area subops)  <BR>"
									+"-player (<WORD> all players) <BR>"
									+"-mob (<WORD> all mobs/npcs)  <BR>"
									+"-class  (<WORD> all classes)  <BR>"
									+"-baseclass  (<WORD> all base classes)  <BR>"
									+"+thief +mage +ranger (create exceptions to -class and -baseclass) <BR>"
									+"-thief -mage  -ranger (<WORD> only listed classes)<BR>"
									+"-race (<WORD> all races)  <BR>"
									+"+elf +dwarf +human +half +gnome (create exceptions to -race)  <BR>"
									+"-elf -dwarf -human -half -gnome (<WORD> only listed races)  <BR>"
									+"-racecat (<WORD> all racial categories)  <BR>"
									+"+racecat (do not <WORD> all racial categories)  <BR>"
									+"+elf +insect +humanoid +canine +gnome (create exceptions to -racecat)  <BR>"
									+"-elf -insect -humanoid -canine -gnome (create exceptions to +racecat)  <BR>"
									+"-alignment (<WORD> all alignments)  <BR>"
									+"+evil +good +neutral (create exceptions to -alignment)  <BR>"
									+"-evil -good -neutral (<WORD> only listed alignments)  <BR>"
									+"-gender (<WORD> all genders)  <BR>"
									+"+male +female +neuter (create exceptions to -gender)  <BR>"
									+"-male -female -neuter (<WORD> only listed genders)  <BR>"
									+"-tattoos (<WORD> all tattoos, even a lack of a tatoo) <BR>"
									+"+mytatto +thistattoo +anytattoo etc..  (create exceptions to -tattoos) <BR>"
									+"+tattoos (do not <WORD> any or no tattoos) <BR>"
									+"-mytattoo -anytatto, etc.. (create exceptions to +tattoos) <BR>"
									+"-level (<WORD> all levels)  <BR>"
									+"+=1 +>5 +>=7 +<13 +<=20 (create exceptions to -level using level ranges)  <BR>"
									+"-=1 ->5 ->=7 -<13 -<=20 (<WORD> only listed levels range) <BR>"
									+"-names (<WORD> everyone) <BR>"
									+"+bob \"+my name\" etc.. (create exceptions to -names) <BR>"
									+"-bob \"-my name\" etc.. (<WORD> those with the given name)";

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
		Vector names=Util.parse(mob.name());
		for(int v=0;v<names.size();v++)
			if(fromHere(V,plusMinus,fromHere,(String)names.elementAt(v)))
				return true;
		names=Util.parse(mob.displayText());
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
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					{
					buf.append("Allows only ");
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						if(fromHere(V,'+',v+1,C.name().toUpperCase().substring(0,3)))
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
							&&(fromHere(V,'+',v+1,C.name().toUpperCase().substring(0,3)))))
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
				}
			else
			{
				for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				{
					CharClass C=(CharClass)c.nextElement();
					if(str.startsWith("-"+C.name().toUpperCase().substring(0,3)))
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
		
		String mobClass=mob.charStats().displayClassName().toUpperCase().substring(0,3);
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
			if(zapCodes.containsKey(str))
				switch(((Integer)zapCodes.get(str)).intValue())
				{
				case 0: // -class
					if(!fromHere(V,'+',v+1,mobClass)) return false;
					break;
				case 1: // -baseclass
					if((!fromHere(V,'+',v+1,mob.charStats().getCurrentClass().baseClass().toUpperCase().substring(0,3)))
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
			if(str.startsWith("-"+mob.name().toUpperCase())) return false;
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
									   int maxDepth)
	{
		int depth=0;
		if(room==null) return;
		if(rooms.contains(room)) return;
		rooms.addElement(room);
		int min=0;
		int size=rooms.size();
		while(depth<maxDepth)
		{
			for(int r=min;r<size;r++)
			{
				Room R1=(Room)rooms.elementAt(r);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=R1.getRoomInDir(d);
					Exit E=R1.getExitInDir(d);
					if((R!=null)
					&&(E!=null)
					&&(R.getArea()==room.getArea())
					&&((!openOnly)||(E.isOpen()))
					&&(!rooms.contains(R)))
						rooms.addElement(R);
				}
			}
			min=size;
			size=rooms.size();
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
		// ridden things dont wander!
		if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
		||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
			return false;

		Room oldRoom=mob.location();
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

				if((oldRoom.domainConditions()!=nextRoom.domainConditions())
				&&(!Sense.isInFlight(mob))
				&&((nextRoom.domainConditions()==Room.DOMAIN_INDOORS_AIR)
				||(nextRoom.domainConditions()==Room.DOMAIN_OUTDOORS_AIR)))
					direction=-1;
				else
				if((oldRoom.domainConditions()!=nextRoom.domainConditions())
				&&(!Sense.isSwimming(mob))
				&&((nextRoom.domainConditions()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(nextRoom.domainConditions()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
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

		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB inhab=oldRoom.fetchInhabitant(m);
			if((inhab!=null)&&(inhab.isASysOp(oldRoom)))
				return false;
		}
			
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

		int dir=direction;
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
			int oldMana=mob.curState().getMana();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldMana);
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
			int oldMana=mob.curState().getMana();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldMana);
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
			int oldMana=mob.curState().getMana();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldMana);
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
}
