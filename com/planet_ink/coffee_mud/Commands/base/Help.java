package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.CreateEdit;
import com.planet_ink.coffee_mud.Commands.base.sysop.SysopItemUsage;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Help
{
	private Help(){}
	
	private static Properties helpFile=null;
	private static Properties arcHelpFile=null;
	
	public static boolean getArcHelpFile()
	{
		if(arcHelpFile==null)
		{
			arcHelpFile=new Properties();
			try{arcHelpFile.load(new FileInputStream("resources"+File.separatorChar+"arc_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
		}
		if(arcHelpFile==null)
			return false;
		return true;
	}

	public static boolean getHelpFile()
	{
		if(helpFile==null)
		{
			helpFile=new Properties();
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"misc_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"skill_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"common_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"spell_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"songs_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"prayer_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"chant_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
		}
		if(helpFile==null) return false;
		return true;
	}

	public static void topics(MOB mob)
	{
		if(!getHelpFile())
		{
			mob.tell("No help is available.");
			return;
		}

		doTopics(mob,helpFile,"HELP", "PLAYER TOPICS");
	}

	public static void arcTopics(MOB mob)
	{
		if(!getArcHelpFile())
		{
			mob.tell("No archon help is available.");
			return;
		}

		doTopics(mob,arcHelpFile,"AHELP", "ARCHON TOPICS");
	}

	public static StringBuffer fourColumns(Vector reverseList)
	{
		StringBuffer topicBuffer=new StringBuffer("");
		int col=0;
		for(int i=0;i<reverseList.size();i++)
		{
			if((++col)>4)
			{
				topicBuffer.append("\n\r");
				col=1;
			}
			if(((String)reverseList.elementAt(i)).length()>18)
			{
				topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),(18*2)+1)+" ");
				++col;
			}
			else
				topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),18)+" ");
		}
		return topicBuffer;
	}
	
	public static void doTopics(MOB mob, Properties rHelpFile, String helpName, String resName)
	{
		StringBuffer topicBuffer=(StringBuffer)Resources.getResource(resName);
		if(topicBuffer==null)
		{
			topicBuffer=new StringBuffer();

			Vector reverseList=new Vector();
			for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = (String)e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
			}

			Collections.sort((List)reverseList);
			topicBuffer=new StringBuffer("Help topics: \n\r\n\r");
			topicBuffer.append(fourColumns(reverseList));
			topicBuffer=new StringBuffer(topicBuffer.toString().replace('_',' '));
			Resources.submitResource(resName,topicBuffer);
		}
		if((topicBuffer!=null)&&(!mob.isMonster()))
			mob.session().rawPrintln(topicBuffer.toString()+"\n\r\n\rEnter "+helpName+" (TOPIC NAME) for more information.");
	}

	
	public static StringBuffer getHelpText(String helpStr)
	{
		if(!getHelpFile())
			return null;
		if(helpStr.length()==0) return null;
		StringBuffer thisTag=getHelpText(helpStr,helpFile);
		if(thisTag!=null) return thisTag;
		if(!getArcHelpFile()) return null;
		thisTag=getHelpText(helpStr,arcHelpFile);
		return thisTag;
	}
	
					
	public static String fixHelp(String tag, String str)
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
			if(name.startsWith("SONG_"))
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
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability A=(Ability)CMClass.abilities.elementAt(a);
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
					for(int c=0;c<CMClass.charClasses.size();c++)
					{
						CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
						int lvl=CMAble.getQualifyingLevel(C.ID(),A.ID());
						if((!C.ID().equalsIgnoreCase("Archon"))&&(lvl>=0))
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
						if((CMClass.items.size()>0)
						&&(CMClass.MOBs.size()>0)
						&&(CMClass.exits.size()>0)
						&&(CMClass.locales.size()>0))
						{
							Item I=(Item)CMClass.items.elementAt(0);
							MOB M=(MOB)CMClass.MOBs.elementAt(0);
							Exit E=(Exit)CMClass.exits.elementAt(0);
							Room R=(Room)CMClass.locales.elementAt(0);
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
	
	public static StringBuffer getHelpText(String helpStr,Properties rHelpFile)
	{
		// the area exception
		if(CMMap.getArea(helpStr.trim())!=null)
		{
			StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+helpStr.trim().toUpperCase());
			if(s==null)
			{
				s=CMMap.getArea(helpStr.trim()).getAreaStats();
				Resources.submitResource("HELP_"+helpStr.trim().toUpperCase(),s);
			}
			return s;
		}
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=rHelpFile.getProperty(helpStr);
		if(thisTag==null){thisTag=rHelpFile.getProperty("SPELL_"+helpStr); if(thisTag!=null) helpStr="SPELL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PRAYER_"+helpStr); if(thisTag!=null) helpStr="PRAYER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SONG_"+helpStr); if(thisTag!=null) helpStr="SONG_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("CHANT_"+helpStr); if(thisTag!=null) helpStr="CHANT_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PROP_"+helpStr); if(thisTag!=null) helpStr="PROP_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("BEHAVIOR_"+helpStr); if(thisTag!=null) helpStr="BEHAVIOR_"+helpStr;}
		
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
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
			return null;
		return new StringBuffer(fixHelp(helpStr,thisTag));
	}
	
	public static void help(MOB mob, String helpStr)
	{
		if(!getHelpFile())
		{
			mob.tell("No help is available.");
			return;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
			thisTag=Resources.getFileResource("help.txt");
		else
			thisTag=getHelpText(helpStr,helpFile);
		if(thisTag==null)
			mob.tell("No help is available on '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(thisTag.toString());
	}

	public static void arcHelp(MOB mob, String helpStr)
	{
		if(!getArcHelpFile())
		{
			mob.tell("No archon help is available.");
			return;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
		{
			thisTag=Resources.getFileResource("arc_help.txt");
			if((thisTag!=null)&&(helpStr.equalsIgnoreCase("more")))
			{
				StringBuffer theRest=(StringBuffer)Resources.getResource("arc_help.therest");
				if(theRest==null)
				{
					Vector V=new Vector();
					theRest=new StringBuffer("\n\rProperties:\n\r");
					for(int a=0;a<CMClass.abilities.size();a++)
					{
						Ability A=(Ability)CMClass.abilities.elementAt(a);
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.PROPERTY))
							V.addElement(A.ID());
					}
					theRest.append(fourColumns(V));
					theRest.append("\n\r\n\rBehaviors:\n\r");
					V=new Vector();
					for(int b=0;b<CMClass.behaviors.size();b++)
					{
						Behavior B=(Behavior)CMClass.behaviors.elementAt(b);
						if(B!=null) V.addElement(B.ID());
					}
					theRest.append(fourColumns(V)+"\n\r");
					Resources.submitResource("arc_help.therest",theRest);
				}
				thisTag=new StringBuffer(thisTag.toString());
				thisTag.append(theRest);
			}
		}
		else
			thisTag=getHelpText(helpStr,arcHelpFile);
		if(thisTag==null)
			mob.tell("No archon help is available on '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(thisTag.toString());
	}

	public static void unloadHelpFile(MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help.txt")!=null)
			Resources.removeResource("help.txt");
		if(Resources.getResource("races.txt")!=null)
			Resources.removeResource("races.txt");
		if(Resources.getResource("newchar.txt")!=null)
			Resources.removeResource("newchar.txt");
		if(Resources.getResource("stats.txt")!=null)
			Resources.removeResource("stats.txt");
		if(Resources.getResource("classes.txt")!=null)
			Resources.removeResource("classes.txt");
		if(Resources.getResource("alignment.txt")!=null)
			Resources.removeResource("alignment.txt");
		if(Resources.getResource("arc_help.txt")!=null)
			Resources.removeResource("arc_help.txt");
		helpFile=null;
		arcHelpFile=null;

		// also the intro page
		if(Resources.getResource("intro.txt")!=null)
			Resources.removeResource("intro.txt");

		if(Resources.getResource("offline.txt")!=null)
			Resources.removeResource("offline.txt");

		if(mob!=null)
			mob.tell("Help files unloaded. Next HELP, AHELP, new char will reload.");
	}

}
