package com.planet_ink.coffee_mud.commands;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
public class CommandSet extends Hashtable
{

	private String commandList="";
	private Vector extraCMDs=new Vector();
	private Vector commandsToLeaveOut=new Vector();
	
	public static final int AFFECT=0;
	public static final int PUSH=1;
	public static final int PULL=2;
	public static final int PREVIOUS_CMD=3;
	public static final int COMPARE=4;
	public static final int BUY=5;
	public static final int CONSIDER=6;
	public static final int CHANNEL=7;
	public static final int CLOSE=8;
	public static final int COMMANDS=9;
	public static final int CREATE=10;
	public static final int DESTROY=11;
	public static final int DESCRIPTION=12;
	
	public static final int DOWN=14;
	public static final int DRINK=15;
	public static final int DROP=16;
	public static final int EAST=17;
	public static final int EAT=18;
	public static final int EMOTE=19;
	public static final int EVOKE=20;
	public static final int EQUIPMENT=21;
	public static final int EXAMINE=22;
	public static final int EXITS=23;
	public static final int FILL=24;
	public static final int FLEE=25;
	public static final int FOLLOW=26;
	public static final int GET=27;
	public static final int GIVE=28;
	public static final int GROUP=29;
	public static final int GTELL=30;
	public static final int BUG=31;
	public static final int HELP=32;
	public static final int GO=33;
	public static final int HOLD=34;
	public static final int INVENTORY=35;
	public static final int KILL=36;
	public static final int LIST=37;
	public static final int LOCK=38;
	public static final int LOOK=39;
	public static final int NOCHANNEL=40;
	public static final int MODIFY=41;
	public static final int NORTH=42;
	public static final int NOFOLLOW=43;
	public static final int OPEN=44;
	public static final int ORDER=45;
	public static final int PASSWORD=46;
	
	public static final int PRACTICE=48;
	public static final int PUT=49;
	public static final int QUIET=50;
	public static final int QUIT=51;
	public static final int READ=52;
	public static final int PRAYERS=53;
	public static final int CREDITS=54;
	public static final int REMOVE=55;
	public static final int REPLY=56;
	public static final int REPORT=57;
	public static final int SYSMSGS=58;
	public static final int SAY=59;
	public static final int SAVE=60;
	public static final int SONGS=61;
	public static final int SELL=62;
	public static final int SIT=63;
	public static final int SCORE=64;
	public static final int SKILLS=65;
	public static final int SLEEP=66;
	public static final int SOCIALS=67;
	public static final int SOUTH=68;
	public static final int AREAS=69;
	public static final int SPELLS=70;
	public static final int SPLIT=71;
	public static final int STAND=72;
	
	public static final int TAKE=74;
	public static final int TELL=75;
	public static final int TRAIN=76;
	public static final int TEACH=77;
	
	public static final int UNLOCK=79;
	public static final int UP=80;
	public static final int WHO=81;
	public static final int WAKE=82;
	public static final int WEAR=83;
	public static final int WEST=84;
	public static final int WHOIS=85;
	public static final int WIELD=86;
	public static final int WIMPY=87;
	
	public CommandSet()
	{
		put("AFFECT",new Integer(AFFECT));
			put("AFF",new Integer(AFFECT));
		put("AREAS",new Integer(AREAS));
		put("PUSH",new Integer(PUSH));
		put("PULL",new Integer(PULL));
		put("BUG",new Integer(BUG));
		put("BUY",new Integer(BUY));
		put("CLOSE",new Integer(CLOSE));
			put("CL",new Integer(CLOSE));
			put("CLO",new Integer(CLOSE));
		put("COMMANDS",new Integer(COMMANDS));
		put("COMPARE",new Integer(COMPARE));
			put("COMP",new Integer(COMPARE));
		put("CONSIDER",new Integer(CONSIDER));
			put("CON",new Integer(CONSIDER));
		put("CREATE",new Integer(CREATE));
		put("CREDITS",new Integer(CREDITS));
		put("DESTROY",new Integer(DESTROY));
		put("DESCRIPTION",new Integer(DESCRIPTION));
		put("DOWN",new Integer(DOWN));
			put("D",new Integer(DOWN));
		put("DRINK",new Integer(DRINK));
		put("DROP",new Integer(DROP));
			put("DRO",new Integer(DROP));
		put("EAST",new Integer(EAST));
			put("E",new Integer(EAST));
		put("EAT",new Integer(EAT));
		put("EMOTE",new Integer(EMOTE));
		put("EQUIPMENT",new Integer(EQUIPMENT));
			put("EQ",new Integer(EQUIPMENT));
		put("EXAMINE",new Integer(EXAMINE));
			put("EXA",new Integer(EXAMINE));
			put("EXAM",new Integer(EXAMINE));
		put("EXITS",new Integer(EXITS));
			put("EX",new Integer(EXITS));
		put("FILL",new Integer(FILL));
		put("FLEE",new Integer(FLEE));
		put("FOLLOW",new Integer(FOLLOW));
			put("FOL",new Integer(FOLLOW));
		put("GET",new Integer(GET));
		put("GIVE",new Integer(GIVE));
		put("GO",new Integer(GO));
		put("GROUP",new Integer(GROUP));
			put("GR",new Integer(GROUP));
		put("GTELL",new Integer(GTELL));
		put("HELP",new Integer(HELP));
		put("HOLD",new Integer(HOLD));
		put("INVENTORY",new Integer(INVENTORY));
			put("INV",new Integer(INVENTORY));
		put("KILL",new Integer(KILL));
		put("LIST",new Integer(LIST));
			put("LI",new Integer(LIST));
		put("LOCK",new Integer(LOCK));
		put("LOOK",new Integer(LOOK));
			put("LOO",new Integer(LOOK));
		put("MODIFY",new Integer(MODIFY));
		put("NORTH",new Integer(NORTH));
			put("N",new Integer(NORTH));
		put("NOFOLLOW",new Integer(NOFOLLOW));
			put("NOFOL",new Integer(NOFOLLOW));
		put("OPEN",new Integer(OPEN));
			put("OP",new Integer(OPEN));
		put("ORDER",new Integer(ORDER));
		put("PASSWORD",new Integer(PASSWORD));
		put("PRACTICE",new Integer(PRACTICE));
			put("PRAC",new Integer(PRACTICE));
		put("PRAYERS",new Integer(PRAYERS));
		put("!",new Integer(PREVIOUS_CMD));
		put("PUT",new Integer(PUT));
		put("QUIET",new Integer(QUIET));
		put("QUIT",new Integer(QUIT));
		put("READ",new Integer(READ));
		put("REMOVE",new Integer(REMOVE));
			put("REM",new Integer(REMOVE));
		put("REPLY",new Integer(REPLY));
			put("REP",new Integer(REPLY));
		put("REPORT",new Integer(REPORT));
		put("REST",new Integer(SIT));
		put("SAY",new Integer(SAY));
			put("'",new Integer(SAY));
		put("SAVE",new Integer(SAVE));
		put("SELL",new Integer(SELL));
		put("SIT",new Integer(SIT));
		put("SCORE",new Integer(SCORE));
			put("SC",new Integer(SCORE));
		put("SKILLS",new Integer(SKILLS));
			put("SK",new Integer(SKILLS));
		put("SLEEP",new Integer(SLEEP));
		put("SOCIALS",new Integer(SOCIALS));
		put("SOUTH",new Integer(SOUTH));
			put("S",new Integer(SOUTH));
		put("SONGS",new Integer(SONGS));
		put("SPELLS",new Integer(SPELLS));
			put("SP",new Integer(SPELLS));
		put("SPLIT",new Integer(SPLIT));
		put("STAND",new Integer(STAND));
			put("ST",new Integer(STAND));
		put("SYSMSGS",new Integer(SYSMSGS));
		put("TAKE",new Integer(TAKE));
		put("TEACH",new Integer(TEACH));
			put("TEA",new Integer(TEACH));
		put("TELL",new Integer(TELL));
		put("TRAIN",new Integer(TRAIN));
			put("TR",new Integer(TRAIN));
		put("UNLOCK",new Integer(UNLOCK));
		put("UP",new Integer(UP));
			put("U",new Integer(UP));
		put("WAKE",new Integer(WAKE));
		put("WEAR",new Integer(WEAR));
		put("WEST",new Integer(WEST));
			put("W",new Integer(WEST));
		put("WHOIS",new Integer(WHOIS));
		put("WHO",new Integer(WHO));
		put("WIELD",new Integer(WIELD));
		put("WIMPY",new Integer(WIMPY));
			put("WIMP",new Integer(WIMPY));
	}
	
	
	public void loadAbilities(Vector abilities)
	{
		for(int e=0;e<abilities.size();e++)
		{
			Ability thisAbility=(Ability)abilities.elementAt(e);
			if((thisAbility.triggerStrings()!=null)
			&&(thisAbility.triggerStrings().size()>0))
			{
				for(int a=0;a<thisAbility.triggerStrings().size();a++)
				{
					String ts=(String)thisAbility.triggerStrings().elementAt(a);
					if(a==0)
					{
						extraCMDs.addElement(ts);
						if(!thisAbility.putInCommandlist())
							commandsToLeaveOut.addElement(ts);
					}
					if(this.get(ts)==null)
						this.put(ts,new Integer(EVOKE));
				}
			}
		}
	}
	
	public void loadChannels(String list)
	{
		while(list.length()>0)
		{
			int x=list.indexOf(",");
			
			String item=null;
			if(x<0)
			{
				item=list;
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			Channels.numChannelsLoaded++;
			Channels.channelNames.addElement(item.toUpperCase().trim());
			//extraCMDs.addElement(item.toUpperCase().trim());
			put(item.toUpperCase().trim(),new Integer(CommandSet.CHANNEL));
			//extraCMDs.addElement("NO"+item.toUpperCase().trim());
			put("NO"+item.toUpperCase().trim(),new Integer(CommandSet.NOCHANNEL));
		}
	}
	
	public String commandList()
	{
		if(commandList.length()!=0) 
			return commandList;
		
		Hashtable reverseHash=new Hashtable();
		for(Enumeration e=keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			Integer code=(Integer)get(key);
			if((code!=null)
			&&(code.intValue()!=CHANNEL)
			&&(code.intValue()!=NOCHANNEL))
			{
				String displayable=(String)reverseHash.get(code);
				if(displayable==null)
				{
					reverseHash.put(code,key);
				}
				else
				if(displayable.length()<key.length())
				{
					reverseHash.remove(code);
					reverseHash.put(code,key);
				}
			}
		}
		
		StringBuffer msg=new StringBuffer("");
		Vector reverseList=new Vector();
		for(Enumeration e=reverseHash.elements();e.hasMoreElements();)
			reverseList.addElement((String)e.nextElement());

		for(int i=0;i<extraCMDs.size();i++)
		{
			String ts=(String)extraCMDs.elementAt(i);
			boolean found=false;
			for(int t=0;t<reverseList.size();t++)
			{
				String to=(String)reverseList.elementAt(t);
				if(to.equalsIgnoreCase(ts))
				{
					found=true;
					break;
				}
			}
			for(int e=0;e<commandsToLeaveOut.size();e++)
				if(((String)commandsToLeaveOut.elementAt(e)).equalsIgnoreCase(ts))
				{
					found=true;
					break;
				}
			if(!found)
				reverseList.addElement(ts);
		}
		
		Collections.sort((List)reverseList);
		int col=0;
		for(int i=0;i<reverseList.size();i++)
		{
			if((++col)>6) 
			{
				msg.append("\n\r");
				col=1;
			}
			
			msg.append(Util.padRight((String)reverseList.elementAt(i),13));
		}
		msg.append("\n\r\n\rEnter HELP 'COMMAND' for more information on these commands.\n\r");
		commandList=msg.toString();
		return commandList;
	}
}
