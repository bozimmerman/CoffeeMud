package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

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
public class MUDHelp
{
	private MUDHelp(){};

	public static StringBuffer getHelpText(String helpStr, MOB forMOB)
	{
		if(getHelpFile().size()==0)
			return null;
		if(helpStr.length()==0) return null;
		StringBuffer thisTag=getHelpText(helpStr,getHelpFile(),forMOB);
		if(thisTag!=null) return thisTag;
		if(getArcHelpFile().size()==0) return null;
		thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB);
		return thisTag;
	}

	public static Vector getTopics(boolean archonHelp, boolean standardHelp)
	{
		Properties rHelpFile=null;
		Vector reverseList=new Vector();
		if(archonHelp)
			rHelpFile=getArcHelpFile();
		if(standardHelp)
		{
			if(rHelpFile==null)
				rHelpFile=getHelpFile();
			else
			{
				for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
				{
					String ptop = (String)e.nextElement();
					String thisTag=rHelpFile.getProperty(ptop);
					if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
						|| (rHelpFile.getProperty(thisTag)== null) )
							reverseList.addElement(ptop);
				}
				rHelpFile=getHelpFile();
			}
		}
		if(rHelpFile!=null)
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String ptop = (String)e.nextElement();
			String thisTag=rHelpFile.getProperty(ptop);
			if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
				|| (rHelpFile.getProperty(thisTag)== null) )
					reverseList.addElement(ptop);
		}
		Collections.sort((List)reverseList);
		return reverseList;
	}
	
	private static String getActualUsage(Ability A, int which, MOB forMOB)
	{
		if(forMOB==null){
			forMOB=CMClass.getMOB("StdMOB");
			forMOB.maxState().setMana(Integer.MAX_VALUE/2);
			forMOB.maxState().setMovement(Integer.MAX_VALUE/2);
			forMOB.maxState().setHitPoints(Integer.MAX_VALUE/2);
		}

		int[] consumption=A.usageCost(forMOB);
		int whichConsumed=consumption[0];
		switch(which)
		{
		case Ability.USAGE_MOVEMENT: whichConsumed=consumption[1]; break;
		case Ability.USAGE_MANA: whichConsumed=consumption[0]; break;
		case Ability.USAGE_HITPOINTS: whichConsumed=consumption[2]; break;
		}
		if(whichConsumed==Integer.MAX_VALUE/2) return "all";
		return ""+whichConsumed;
	}

	public static String fixHelp(String tag, String str, MOB forMOB)
	{
		if(str.startsWith("<ABILITY>"))
		{
			str=str.substring(9);
			String name=tag;
			int type=-1;
			if(name.startsWith("SPELL_"))
			{
				type=Ability.SPELL;
				name=name.substring(6);
			}
			else
			if(name.startsWith("PRAYER_"))
			{
				type=Ability.PRAYER;
				name=name.substring(7);
			}
			else
			if(name.startsWith("DANCE_"))
			{
				type=Ability.SONG;
				name=name.substring(6);
			}
			else
			if((name.startsWith("SONG_"))
			||(name.startsWith("PLAY_")))
			{
				type=Ability.SONG;
				name=name.substring(5);
			}
			else
			if(name.startsWith("CHANT_"))
			{
				type=Ability.CHANT;
				name=name.substring(6);
			}
			name=name.replace('_',' ');
			Vector helpedPreviously=new Vector();
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((A.ID().equalsIgnoreCase(tag)
						&&((type<0)||(type==(A.classificationCode()&Ability.ALL_CODES)))
					||(A.name().equalsIgnoreCase(name)))
				&&(!helpedPreviously.contains(A)))
				{
					helpedPreviously.addElement(A);
					StringBuffer prepend=new StringBuffer("");
					type=(A.classificationCode()&Ability.ALL_CODES);
					prepend.append("\n\r");
					switch(type)
					{
					case Ability.SPELL:
						prepend.append(Util.padRight("Spell",9));
						break;
					case Ability.PRAYER:
						prepend.append(Util.padRight("Prayer",9));
						break;
					case Ability.CHANT:
						prepend.append(Util.padRight("Chant",9));
						break;
					case Ability.SONG:
						prepend.append(Util.padRight("Song",9));
						break;
					default:
						prepend.append(Util.padRight("Skill",9));
						break;
					}
					prepend.append(": "+A.name());
					if(type==Ability.SPELL)
					{
						prepend.append("\n\rSchool   : ");
						int school=(A.classificationCode()&Ability.ALL_DOMAINS)>>5;
						prepend.append(Util.capitalize(Ability.DOMAIN_DESCS[school]));
					}
					Vector avail=new Vector();
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						int lvl=CMAble.getQualifyingLevel(C.ID(),true,A.ID());
						if((!C.ID().equalsIgnoreCase("Archon"))
						&&(lvl>=0)
						&&(C.playerSelectable())
						&&(!CMAble.getSecretSkill(C.ID(),true,A.ID())))
							avail.addElement(C.name()+"("+lvl+")");
					}
					for(int c=0;c<avail.size();c++)
					{
						if((c%4)==0)
							prepend.append("\n\rAvailable: ");
						prepend.append(((String)avail.elementAt(c))+" ");
					}
					if(type==Ability.PRAYER)
					{
						prepend.append("\n\rAlignment: ");
						boolean notgood=(!A.appropriateToMyAlignment(1000));
						boolean notneutral=(!A.appropriateToMyAlignment(500));
						boolean notevil=(!A.appropriateToMyAlignment(0));
						boolean good=!notgood;
						boolean neutral=!notneutral;
						boolean evil=!notevil;

						if(good&&neutral&&evil)
							prepend.append("Unaligned/Doesn't matter");
						else
						if(neutral&&notgood&&notevil)
							prepend.append("Neutral");
						else
						if(good&&notevil)
							prepend.append("Good");
						else
						if(evil&&notgood)
							prepend.append("Evil");
					}
					if(!A.isAutoInvoked())
					{
						prepend.append("\n\rUse Cost : ");
						if(A.usageType()==Ability.USAGE_NADA)
							prepend.append("None");
						if(Util.bset(A.usageType(),Ability.USAGE_MANA))
							prepend.append("Mana ("+getActualUsage(A,Ability.USAGE_MANA,forMOB)+") ");
						if(Util.bset(A.usageType(),Ability.USAGE_MOVEMENT))
							prepend.append("Movement ("+getActualUsage(A,Ability.USAGE_MOVEMENT,forMOB)+") ");
						if(Util.bset(A.usageType(),Ability.USAGE_HITPOINTS))
							prepend.append("Hit Points ("+getActualUsage(A,Ability.USAGE_HITPOINTS,forMOB)+") ");
						prepend.append("\n\rQuality  : ");
						switch(A.quality())
						{
						case Ability.MALICIOUS:
							prepend.append("Malicious");
							break;
						case Ability.BENEFICIAL_OTHERS:
						case Ability.BENEFICIAL_SELF:
							prepend.append("Always Beneficial");
							break;
						case Ability.OK_OTHERS:
						case Ability.OK_SELF:
							prepend.append("Sometimes Beneficial");
							break;
						case Ability.INDIFFERENT:
							prepend.append("Circumstantial");
							break;
						}
						prepend.append("\n\rTargets  : ");
						if((A.quality()==Ability.BENEFICIAL_SELF)
						||(A.quality()==Ability.OK_SELF))
							prepend.append("Caster only");
						else
						if((CMClass.items().hasMoreElements())
						&&(CMClass.mobTypes().hasMoreElements())
						&&(CMClass.exits().hasMoreElements())
						&&(CMClass.locales().hasMoreElements()))
						{
							Item I=(Item)CMClass.items().nextElement();
							MOB M=(MOB)CMClass.mobTypes().nextElement();
							Exit E=(Exit)CMClass.exits().nextElement();
							Room R=(Room)CMClass.locales().nextElement();
							if(A.canAffect(I)||A.canTarget(I))
								prepend.append("Items ");
							if(A.canAffect(M)||A.canTarget(M))
								prepend.append("Creatures ");
							if(A.canAffect(E)||A.canTarget(E))
								prepend.append("Exits ");
							if(A.canAffect(R)||A.canTarget(R))
								prepend.append("Rooms ");
						}
						else
						if(A.quality()==Ability.INDIFFERENT)
							prepend.append("Items or Rooms");
						else
						if(A.quality()==Ability.MALICIOUS)
							prepend.append("Others");
						else
						if((A.quality()==Ability.BENEFICIAL_OTHERS)
						||(A.quality()==Ability.OK_SELF))
							prepend.append("Caster, or others");
						prepend.append("\n\rRange    : ");
						int min=A.minRange();
						int max=A.maxRange();
						if(min+max==0)
							prepend.append("Touch, or not applicable");
						else
						{
							if(min==0)
								prepend.append("Touch");
							else
								prepend.append("Range "+min);
							if(max>0)
								prepend.append(" - Range "+max);
						}
						if((A.triggerStrings()!=null)
						   &&(A.triggerStrings().length>0))
						{
							prepend.append("\n\rCommands : ");
							for(int i=0;i<A.triggerStrings().length;i++)
							{
								prepend.append(A.triggerStrings()[i]);
								if(i<(A.triggerStrings().length-1))
								   prepend.append(", ");
							}
						}
					}
					else
						prepend.append("\n\rInvoked  : Automatic");
					str=prepend.toString()+"\n\r"+str;
				}
			}
		}
		return str;
	}

	public static StringBuffer getHelpText(String helpStr, Properties rHelpFile, MOB forMOB)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=rHelpFile.getProperty(helpStr);
		if(thisTag==null){thisTag=rHelpFile.getProperty("SPELL_"+helpStr); if(thisTag!=null) helpStr="SPELL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PRAYER_"+helpStr); if(thisTag!=null) helpStr="PRAYER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SONG_"+helpStr); if(thisTag!=null) helpStr="SONG_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("DANCE_"+helpStr); if(thisTag!=null) helpStr="DANCE_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PLAY_"+helpStr); if(thisTag!=null) helpStr="PLAY_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("CHANT_"+helpStr); if(thisTag!=null) helpStr="CHANT_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PROP_"+helpStr); if(thisTag!=null) helpStr="PROP_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("BEHAVIOR_"+helpStr); if(thisTag!=null) helpStr="BEHAVIOR_"+helpStr;}

		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String key=((String)e.nextElement()).toUpperCase();
			if(key.startsWith(helpStr))
			{
				thisTag=rHelpFile.getProperty(key);
				helpStr=key;
				break;
			}
		}
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String key=((String)e.nextElement()).toUpperCase();
			if(key.indexOf(helpStr)>=0)
			{
				thisTag=rHelpFile.getProperty(key);
				helpStr=key;
				break;
			}
		}
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		{
			String ahelpStr=helpStr.replaceAll("_"," ").trim();
			boolean found=false;
			for(Enumeration e=CMMap.areas();e.hasMoreElements();)
			{
				Area A=(Area)e.nextElement();
				if(A.name().equalsIgnoreCase(ahelpStr))
				{
					helpStr=A.name();
					found=true;
					break;
				}
			}
			if(!found)
				for(Enumeration e=CMMap.areas();e.hasMoreElements();)
				{
					Area A=(Area)e.nextElement();
					if(EnglishParser.containsString(A.name(),ahelpStr))
					{
						helpStr=A.name();
						break;
					}
				}
		}
		while((thisTag!=null)&&(thisTag.length()>0)&&(thisTag.length()<31))
		{
			String thisOtherTag=rHelpFile.getProperty(thisTag);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(thisTag)))
				thisTag=null;
			else
			{
				helpStr=thisTag;
				thisTag=thisOtherTag;
			}
		}
		// the area exception
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
			if(CMMap.getArea(helpStr.trim())!=null)
				return CMMap.getArea(helpStr.trim()).getAreaStats();
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
			return null;
		return new StringBuffer(fixHelp(helpStr,thisTag,forMOB));
	}

	public static Properties getArcHelpFile()
	{
		Properties arcHelpFile=(Properties)Resources.getResource("ARCHON HELP FILE");
		if(arcHelpFile==null)
		{
			arcHelpFile=new Properties();
			File directory=new File("resources"+File.separatorChar+"help"+File.separatorChar);
			if((directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
				{
					String item=list[l];
					if((item!=null)&&(item.length()>0))
					{
						if(item.toUpperCase().endsWith(".INI")&&(item.toUpperCase().startsWith("ARC_")))
							try{arcHelpFile.load(new FileInputStream("resources"+File.separatorChar+"help"+File.separatorChar+item));}catch(IOException e){Log.errOut("CommandProcessor",e);}
					}
				}
			}
			if(arcHelpFile!=null)
			{
				for(Enumeration e=arcHelpFile.keys();e.hasMoreElements();)
				{
					String key=(String)e.nextElement();
					String entry=(String)arcHelpFile.get(key);
					int x=entry.indexOf("<ZAP=");
					if(x>=0)
					{
						int y=entry.indexOf(">",x);
						if(y>(x+5))
						{
							String word=entry.substring(x+5,y).trim();
							entry=entry.substring(0,x)+MUDZapper.zapperInstructions("\n\r",word)+entry.substring(y+1);
							arcHelpFile.remove(key);
							arcHelpFile.put(key,entry);
						}
					}
				}
				Resources.submitResource("ARCHON HELP FILE",arcHelpFile);
			}
		}
		return arcHelpFile;
	}

	public static Properties getHelpFile()
	{
		Properties helpFile=(Properties)Resources.getResource("MAIN HELP FILE");
		if(helpFile==null)
		{
			helpFile=new Properties();
			File directory=new File("resources"+File.separatorChar+"help"+File.separatorChar);
			if((directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
				{
					String item=list[l];
					if((item!=null)&&(item.length()>0))
					{
						if(item.toUpperCase().endsWith(".INI")&&(!item.toUpperCase().startsWith("ARC_")))
							try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"help"+File.separatorChar+item));}catch(IOException e){Log.errOut("CommandProcessor",e);}
					}
				}
			}
			if(helpFile!=null)
				Resources.submitResource("MAIN HELP FILE",helpFile);
		}
		return helpFile;
	}
	
	public static void unloadHelpFile(MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help"+File.separatorChar+"help.txt")!=null)
			Resources.removeResource("help"+File.separatorChar+"help.txt");
		if(Resources.getResource("text"+File.separatorChar+"races.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"races.txt");
		if(Resources.getResource("text"+File.separatorChar+"newchar.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"newchar.txt");
		if(Resources.getResource("text"+File.separatorChar+"stats.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"stats.txt");
		if(Resources.getResource("text"+File.separatorChar+"classes.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"classes.txt");
		if(Resources.getResource("text"+File.separatorChar+"alignment.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"alignment.txt");
		if(Resources.getResource("help"+File.separatorChar+"arc_help.txt")!=null)
			Resources.removeResource("help"+File.separatorChar+"arc_help.txt");
		if(Resources.getResource("MAIN HELP FILE")!=null)
			Resources.removeResource("MAIN HELP FILE");
		if(Resources.getResource("ARCHON HELP FILE")!=null)
			Resources.removeResource("ARCHON HELP FILE");

		// also the intro page
		if(Resources.getResource("text"+File.separatorChar+"intro.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"intro.txt");

		if(Resources.getResource("text"+File.separatorChar+"offline.txt")!=null)
			Resources.removeResource("text"+File.separatorChar+"offline.txt");

		if(mob!=null)
			mob.tell("Help files unloaded. Next HELP, AHELP, new char will reload.");
	}
}
