package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

/*
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Say extends StdCommand
{
	public Say(){}

	private final String[] access={"SAY",
							 "ASK",
							 "`",
							 "SA",
							 "SAYTO"};
	public String[] getAccessWords(){return access;}

	protected static final String[] impossibleTargets={
		"HERE",
		"THERE",
		"IS",
		"JUST",
		"A",
		"AN",
		"TO",
		"THE",
		"SOME",
		"SITS",
		"RESTS",
		"LEFT",
		"HAS",
		"BEEN"
	};

	protected void gmcpSaySend(String sayName, MOB mob, Environmental target, CMMsg msg)
	{
		if((mob.session()!=null)&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)))
		{
			mob.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
					MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, mob, mob, target, null, CMStrings.removeColors(msg.sourceMessage()), false))
					+"\",\"player\":\""+mob.name()+"\"}");
		}
		final Room R=mob.location();
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M!=msg.source())&&(M.session()!=null)&&(M.session().getClientTelnetMode(Session.TELNET_GMCP)))
			{
				M.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
						MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, M, mob, target, null, CMStrings.removeColors(msg.othersMessage()), false))
						+"\",\"player\":\""+mob.name()+"\"}");
			}
		}
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String theWord="Say";
		boolean toFlag=false;
		if(((String)commands.elementAt(0)).equalsIgnoreCase("ASK"))
			theWord="Ask";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("YELL"))
			theWord="Yell";
		else
		if(((String)commands.elementAt(0)).equalsIgnoreCase("SAYTO")
		||((String)commands.elementAt(0)).equalsIgnoreCase("SAYT"))
		{
			theWord="Say";
			toFlag=true;
		}

		final Room R=mob.location();
		if((commands.size()==1)||(R==null))
		{
			mob.tell(theWord+" what?");
			return false;
		}

		Vector<Room> yellRooms=new Vector<Room>();
		if(theWord.toUpperCase().startsWith("YELL"))
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Room R2=R.getRoomInDir(d);
				Exit E2=R.getExitInDir(d);
				if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
					yellRooms.add(R2);
			}
		}
		
		
		String whom="";
		String theWordSuffix="";
		Environmental target=null;
		Physical langTarget=null;
		if(commands.size()>2)
		{
			whom=((String)commands.elementAt(1)).toUpperCase();
			if(!toFlag)
				for(int i=0;i<impossibleTargets.length;i++)
					if(impossibleTargets[i].startsWith(whom))
					{ whom=""; break;}
			if(whom.equalsIgnoreCase("self"))
				target=mob;
			else
			if(whom.length()>0)
			{
				target=R.fetchFromRoomFavorMOBs(null,whom);
				if((toFlag)&&(target==null))
					target=mob.findItem(null,whom);

				if((!toFlag)&&(target!=null))
				{
					if(!(target instanceof MOB))
						target=null;
					else
					if(target.name().toUpperCase().indexOf(whom.toUpperCase())<0)
						target=null;
					else
					if((!target.name().equalsIgnoreCase(whom))&&(whom.length()<4))
						target=null;
				}

				if(target!=null)
				{
					commands.removeElementAt(1);
					if(target instanceof Physical)
						langTarget=(Physical)target;
				}
				else
				{
					if(theWord.toUpperCase().startsWith("YELL"))
					{
						int dir=Directions.getGoodCompassDirectionCode(whom);
						if(dir >=0)
						{
							commands.removeElementAt(1);
							yellRooms=new Vector<Room>();
							if(theWord.toUpperCase().startsWith("YELL"))
							{
								Room R2=R.getRoomInDir(dir);
								Exit E2=R.getExitInDir(dir);
								if(R2!=null)
								{
									theWordSuffix=" "+Directions.getDirectionName(dir);
									yellRooms.add(R2);
									if((E2!=null)&&(E2.isOpen()))
									{
										for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
										{
											Room R3=R2.getRoomInDir(d);
											Exit E3=R2.getExitInDir(d);
											if((R3!=null)&&(E3!=null)&&(E3.isOpen()))
												yellRooms.add(R3);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// if you are the only one in the room to talk to
		// then grab a random mob and assume that's who
		// you are addressing.
		if((langTarget==null)&&(target==null)&&(R.numInhabitants()==2))
		{
			for(int r=0;r<R.numInhabitants();r++)
			{
				MOB M=R.fetchInhabitant(r);
				if(M!=mob)
				{
					langTarget=M;
					target=M;
					break;
				}
			}
		}
	
		// if you are addressing someone speaking a language that you
		// can speak, then speak it.
		Language[] langSwap=null;
		if((langTarget!=null)&&(!mob.isMonster()))
		{
			Language hisL=CMLib.utensils().getLanguageSpoken(langTarget);
			Language myL=CMLib.utensils().getLanguageSpoken(mob);
			if((hisL==null)&&(myL!=null))
				langSwap=new Language[]{null,myL};
			else
			if((hisL!=null)&&((myL==null)||(!hisL.ID().equals(myL.ID()))))
			{
				Language myTargetL = (Language)mob.fetchEffect(hisL.ID());
				if(myTargetL!=null)
					langSwap=new Language[]{myTargetL,myL};
			}
		}
		
		String combinedCommands;
		if(commands.size()==2)
			combinedCommands=(String)commands.get(1);
		else
			combinedCommands=CMParms.combineWithQuotes(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(theWord+"  what?");
			return false;
		}
		if(toFlag&&((target==null)||(!CMLib.flags().canBeSeenBy(target,mob))))
		{
			mob.tell("you don't see "+whom+" here to speak to.");
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.Str.SAYFILTER);

		CMMsg msg=null;
		if((!theWord.equalsIgnoreCase("ASK"))&&(target!=null))
			theWord+="(s) to";
		else
			theWord+="(s)";
		String fromSelf="^T^<SAY \""+CMStrings.removeColors((target!=null)?target.name():mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		String toTarget="^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
		if(target==null)
			msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" '"+combinedCommands+"'^</SAY^>^?");
		else
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,fromSelf,toTarget,fromSelf);
	
		gmcpSaySend("say", mob, target, msg);
		
		if(langSwap!=null)
		{
			if(langSwap[1]!=null)
				langSwap[1].setBeingSpoken(langSwap[1].ID(), false);
			if(langSwap[0]!=null)
				langSwap[0].setBeingSpoken(langSwap[0].ID(), true);
		}
		final boolean useShipDirs=(R instanceof SpaceShip)||(R.getArea() instanceof SpaceShip);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(theWord.toUpperCase().startsWith("YELL"))
			{
				int dirCode=-1;
				Room R3=R;
				for(Room R2 : yellRooms)
				{
					int newDirCode=CMLib.map().getRoomDir(R, R2);
					if(newDirCode<0)
						newDirCode=CMLib.map().getRoomDir(R3, R2);
					else
						R3=R2;
					if(newDirCode>=0)
						dirCode=newDirCode;
					Environmental tool=msg.tool();
					int opDirCode=-1;
					if(dirCode>=0)
						opDirCode=Directions.getOpDirectionCode(dirCode);
					final String inDirName=(dirCode<0)?"":(useShipDirs?Directions.getShipInDirectionName(opDirCode):Directions.getInDirectionName(opDirCode));
					msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^TYou hear someone yell '"+combinedCommands+"' "+inDirName+"^?");
					if((R2.okMessage(mob,msg))
					&&((tool==null)||(tool.okMessage(mob,msg))))
					{
						R2.sendOthers(mob,msg);
					}
				}
			}
		}
		if(langSwap!=null)
		{
			if(langSwap[0]!=null)
				langSwap[0].setBeingSpoken(langSwap[0].ID(), false);
			if(langSwap[1]!=null)
				langSwap[1].setBeingSpoken(langSwap[1].ID(), true);
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return true;}

	
}
