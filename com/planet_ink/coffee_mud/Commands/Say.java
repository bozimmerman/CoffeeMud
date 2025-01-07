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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Say extends StdCommand
{
	public Say()
	{
	}

	private final String[] access=I(new String[]{"SAY",
												 "ASK",
												 "`",
												 "SA",
												 "SAYTO"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

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

	protected void gmcpSaySend(final String sayName, final MOB mob, final Environmental target, final CMMsg msg)
	{
		final String player=CMStrings.removeAllButLettersAndDigits(CMStrings.removeColors(mob.name()));
		if((mob.session()!=null)
		&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)))
		{
			mob.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
					MiniJSON.toJSONString(CMStrings.unWWrap(CMLib.coffeeFilter().fullOutFilter(null, mob, mob, target, null,
							CMStrings.removeColors(msg.sourceMessage()), false)).trim())
					+"\",\"player\":\""+player+"\"}");
		}
		final Room R=mob.location();
		if((R!=null)
		&&(msg.othersMessage()!=null))
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)
				&&(M!=msg.source())
				&&(M.session()!=null)
				&&(M.session().getClientTelnetMode(Session.TELNET_GMCP)))
				{
					if((M == target)
					&&(msg.targetMessage()!=null))
					{
						M.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
								MiniJSON.toJSONString(CMStrings.unWWrap(CMLib.coffeeFilter().fullOutFilter(null, M, mob, target, null,
										CMStrings.removeColors(msg.targetMessage()), false)).trim())
								+"\",\"player\":\""+player+"\"}");
					}
					else
					if(msg.othersMessage()!=null)
					{
						M.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
								MiniJSON.toJSONString(CMStrings.unWWrap(CMLib.coffeeFilter().fullOutFilter(null, M, mob, target, null,
										CMStrings.removeColors(msg.othersMessage()), false)).trim())
								+"\",\"player\":\""+player+"\"}");
					}
				}
			}
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		String theWord="Say";
		boolean toFlag=false;
		final String theCommand=commands.get(0).toUpperCase();
		if(theCommand.equals("ASK"))
			theWord="Ask";
		else
		if(theCommand.equals("YELL"))
			theWord="Yell";
		else
		if(theCommand.equals("YELLTO"))
		{
			theWord="Yell";
			toFlag=true;
		}
		else
		if(theCommand.equals("YELLAT"))
		{
			theWord="Yell";
			toFlag=true;
		}
		else
		if(theCommand.equals("SAYTO")
		||theCommand.equals("SAYT"))
		{
			theWord="Say";
			toFlag=true;
		}

		final Room R=mob.location();
		if((commands.size()==1)||(R==null))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1 what?",theWord));
			return false;
		}

		List<Room> yellRooms=new ArrayList<Room>();
		if(theCommand.equals("YELL"))
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room R2=R.getRoomInDir(d);
				final Exit E2=R.getExitInDir(d);
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
			whom=commands.get(1).toUpperCase();
			if(!toFlag)
			{
				final Item I=mob.fetchHeldItem();
				if((I instanceof Wand)
				&&(((Wand)I).magicWord().equalsIgnoreCase(whom)))
				{
					whom = "";
				}
				else
				{
					for (final String impossibleTarget : impossibleTargets)
					{
						if(impossibleTarget.startsWith(whom))
						{
							whom = "";
							break;
						}
					}
				}
			}
			if(whom.equalsIgnoreCase("self"))
			{
				target=mob;
				commands.remove(1);
			}
			else
			if(toFlag && (whom.equalsIgnoreCase("noone")||whom.equalsIgnoreCase("nobody")))
			{
				target=null;
				commands.remove(1);
			}
			else
			if(whom.length()>0)
			{
				target=R.fetchFromRoomFavorMOBs(null,whom);
				if((toFlag)
				&&(target==null))
					target=mob.findItem(null,whom);

				if((!toFlag)
				&&(target!=null))
				{
					if(!(target instanceof MOB))
						target=null;
					else
					{
						final String tname = target.name().toUpperCase();
						int x=whom.lastIndexOf('.'); // clean out any numeric references
						final String srchWhom;
						if(x<0)
							srchWhom=whom.toUpperCase();
						else
						if(CMath.isInteger(whom.substring(x+1)))
							srchWhom=whom.toUpperCase().substring(0,x);
						else
						{
							x=whom.indexOf('.');
							if(CMath.isInteger(whom.substring(0,x)))
								srchWhom=whom.toUpperCase().substring(x+1);
							else
								srchWhom=whom.toUpperCase();
						}
						if(whom.length()<4)
						{
							if(!srchWhom.equals(tname))
								target=null;
						}
						else
						if(tname.indexOf(srchWhom)<0)
							target=null;
					}
				}

				if(target!=null)
				{
					commands.remove(1);
					if(target instanceof Physical)
						langTarget=(Physical)target;
				}
				else
				{
					if(theCommand.equals("YELL"))
					{
						final int dir=CMLib.directions().getGoodCompassDirectionCode(whom);
						if(dir >=0)
						{
							commands.remove(1);
							yellRooms=new ArrayList<Room>();
							if(theCommand.equals("YELL"))
							{
								final Room R2=R.getRoomInDir(dir);
								final Exit E2=R.getExitInDir(dir);
								if(R2!=null)
								{
									theWordSuffix=" "+CMLib.directions().getDirectionName(dir);
									yellRooms.add(R2);
									if((E2!=null)
									&&(E2.isOpen()))
									{
										for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
										{
											final Room R3=R2.getRoomInDir(d);
											final Exit E3=R2.getExitInDir(d);
											if((R3!=null)
											&&(E3!=null)
											&&(E3.isOpen()))
												yellRooms.add(R3);
										}
									}
								}
							}
						}
					}
				}
				if(toFlag
				&&((target==null)||(!CMLib.flags().canBeSeenBy(target,mob))))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("you don't see @x1 here to speak to.",whom));
					return false;
				}
			}
		}

		Language[] langSwap=null;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTOLANGUAGE))
		{
			// if you are the only one in the room to talk to
			// then grab a random mob and assume that's who
			// you are addressing.
			if((!toFlag)
			&&(langTarget==null)
			&&(target==null)
			&&(R.numInhabitants()==2))
			{
				for(int r=0;r<R.numInhabitants();r++)
				{
					final MOB M=R.fetchInhabitant(r);
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
			if((langTarget!=null)
			&&(!mob.isMonster())
			&&(!CMLib.flags().isAnimalIntelligence(mob)))
			{
				final Language hisL=CMLib.utensils().getLanguageSpoken(langTarget);
				final Language myL=CMLib.utensils().getLanguageSpoken(mob);
				if((hisL==null)
				&&(myL!=null)
				&&(mob.fetchAbility("Common")!=null)
				&&((!(langTarget instanceof MOB))
					||(langTarget.fetchEffect(myL.ID())==null)
					||(!(((MOB)langTarget).isPlayer()))))
				{
					langSwap=new Language[]{null,myL};
				}
				else
				if((hisL!=null)
				&&((myL==null)||(!hisL.ID().equals(myL.ID()))))
				{
					final Language myTargetL = (Language)mob.fetchEffect(hisL.ID());
					if(myTargetL!=null)
						langSwap=new Language[]{myTargetL,myL};
				}
			}
		}

		String combinedCommands;
		if(commands.size()==2)
			combinedCommands=commands.get(1);
		else
			combinedCommands=CMParms.combineQuoted(commands,1);
		if(combinedCommands.equals(""))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1  what?",theWord));
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.Str.SAYFILTER);
		if(mob.isPlayer()
		|| CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_FORCED)
		|| CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_ORDER))
			combinedCommands=CMLib.coffeeFilter().secondaryUserInputFilter(combinedCommands);
		CMMsg msg=null;
		if((!theCommand.equals("ASK"))&&(target!=null))
			theWord=L(theWord+"(s) to");
		else
			theWord=L(theWord+"(s)");
		if(target==null)
			msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" '"+combinedCommands+"'^</SAY^>^?");
		else
		{
			final String fromSelf="^T^<SAY \""+CMStrings.removeColors(target.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
			final String toTarget="^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> "+theWord.toLowerCase()+theWordSuffix+" <T-NAMESELF> '"+combinedCommands+"'^</SAY^>^?";
			msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,fromSelf,toTarget,fromSelf);
		}

		gmcpSaySend("say", mob, target, msg);

		if(langSwap!=null)
		{
			if(langSwap[1]!=null)
				langSwap[1].setBeingSpoken(langSwap[1].ID(), false);
			if(langSwap[0]!=null)
				langSwap[0].setBeingSpoken(langSwap[0].ID(), true);
		}
		final Directions.DirType dirType=CMLib.flags().getDirType(R);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if(theCommand.equals("YELL"))
			{
				int dirCode=-1;
				Room R3=R;
				for(final Room R2 : yellRooms)
				{
					int newDirCode=CMLib.map().getRoomDir(R, R2);
					if(newDirCode<0)
						newDirCode=CMLib.map().getRoomDir(R3, R2);
					else
						R3=R2;
					if(newDirCode>=0)
						dirCode=newDirCode;
					final Environmental tool=msg.tool();
					int opDirCode=-1;
					if(dirCode>=0)
					{
						opDirCode=R.getReverseDir(dirCode);
						if(opDirCode<0)
							opDirCode=Directions.getOpDirectionCode(dirCode);
					}
					final String inDirName=(dirCode<0)?"":(CMLib.directions().getInDirectionName(opDirCode, dirType));
					msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,L("^TYou hear someone yell ")+"'"+combinedCommands+"' "+inDirName+"^?");
					if((R2.okMessage(mob,msg))
					&&((tool==null)||(tool.okMessage(mob,msg))))
					{
						R2.sendOthers(mob,msg);
					}
				}
			}
		}
		else
			CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
		if(langSwap!=null)
		{
			if(langSwap[0]!=null)
				langSwap[0].setBeingSpoken(langSwap[0].ID(), false);
			if(langSwap[1]!=null)
				langSwap[1].setBeingSpoken(langSwap[1].ID(), true);
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
