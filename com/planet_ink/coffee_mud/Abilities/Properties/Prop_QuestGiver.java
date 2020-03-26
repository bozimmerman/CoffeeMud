package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Prop_QuestGiver extends Property
{
	@Override
	public String ID()
	{
		return "Prop_QuestGiver";
	}

	@Override
	public String name()
	{
		return "Gives a Quest";
	}

	protected int				trigger			= -1;
	protected TreeSet<String>	leaveThemAlone	= new TreeSet<String>();
	protected volatile Quest	quest			= null;

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_EXITS | Ability.CAN_ROOMS | Ability.CAN_AREAS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return (affected instanceof Area);
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		trigger = -1;
	}

	protected int getTrigger()
	{
		if((trigger < 0)&&(affected != null))
		{
			final String triggerStr=CMParms.getParmStr(text(), "TRIGGER", "").toUpperCase().trim();
			if((affected instanceof Area)
			||(affected instanceof Room)
			||(affected instanceof Exit))
				trigger = CMMsg.TYP_ENTER;
			else
			if(affected instanceof MOB)
				trigger = CMMsg.TYP_DEATH;
			else
			if(affected instanceof Item)
				trigger = CMMsg.TYP_GET;

			if(triggerStr.equals("GET"))
				trigger = CMMsg.TYP_GET;
			else
			if(triggerStr.equals("DEATH"))
				trigger = CMMsg.TYP_DEATH;
			else
			if(triggerStr.equals("SPEAK"))
				trigger = CMMsg.TYP_SPEAK;
			else
			if(triggerStr.equals("LEAVE"))
				trigger = CMMsg.TYP_LEAVE;
			else
			if(triggerStr.equals("ENTER"))
				trigger = CMMsg.TYP_ENTER;
			else
			if(triggerStr.equals("PUTIN"))
				trigger = CMMsg.TYP_PUT;
			else
			if(triggerStr.equals("WEAR"))
				trigger = CMMsg.TYP_WEAR;
			else
			if(triggerStr.equals("WIELD"))
				trigger = CMMsg.TYP_WIELD;
			else
			if(triggerStr.equals("READ"))
				trigger = CMMsg.TYP_READ;
			else
			if(triggerStr.equals("LOOK"))
				trigger = CMMsg.TYP_LOOK;
			else
			if(triggerStr.length()>0)
			{
				if(!leaveThemAlone.contains("TheLog2"))
				{
					leaveThemAlone.add("TheLog2");
					Log.errOut("Prop_QuestGiver", "Unknown trigger '"+triggerStr+"' on "+((affected!=null)?affected.name():"unknown"));
				}
			}
		}
		return trigger;
	}

	protected Quest getQuest()
	{
		if((quest == null)
		||(!quest.running()))
		{
			quest=null;
			final String text=CMParms.getParmStr(text(), "QUEST", "");
			String questName="";
			if(text.toUpperCase().startsWith("GENERATE:"))
			{
				final String genParms = text.substring(9);
				final Behavior B=CMClass.getBehavior("RandomQuests");
				if(affected instanceof PhysicalAgent)
					B.startBehavior((PhysicalAgent)affected);
				B.setParms("MINQUESTS=1 MAXQUESTS=1 INLINE=true MIN=1 MAX=1 CHANCE=100 "+genParms);
				for(int i=0;i<10;i++)
				{
					B.tick(affected, Tickable.TICKID_MOB);
					questName = B.getStat("QUEST");
					if(questName.length()>0)
						break;
				}
			}
			else
				questName = text;
			quest=(questName.length()>0)?CMLib.quests().findQuest(questName):null;
			if(quest == null)
			{
				if(!leaveThemAlone.contains("TheLog"))
				{
					leaveThemAlone.add("TheLog");
					Log.errOut("Prop_QuestGiver", "Unknown quest '"+text+"' on "+((affected!=null)?affected.name():"unknown"));
				}
			}
		}
		return quest;
	}

	public void maybeGiveTheQuest(final MOB mob, final CMMsg msg)
	{
		if((mob==null)
		||(mob.isMonster())
		||(leaveThemAlone.contains(mob.Name())))
			return;
		final Quest Q=this.getQuest();
		if((Q == null)||(!Q.canAcceptQuest(mob)))
			return;
		final StringBuilder announcement = new StringBuilder("");
		announcement.append(CMParms.getParmStr(text(),"MESSAGE",""));
		if(announcement.length()>0)
			announcement.append("\n\r");
		if(CMParms.getParmBool(text(), "DETAILS", true))
		{
			if(announcement.length()>0)
				announcement.append("\n\r");
			announcement.append(CMStrings.padRight(L("Quest"), 10)).append(": ").append(Q.displayName()).append("\n\r");
			final MOB questGiverM=Q.getQuestMob(1);
			if(questGiverM!=null)
			{
				final Room questGiverR=questGiverM.location();
				announcement.append(CMStrings.padRight(L("Giver"), 10)).append(": ").append(questGiverM.name())
						.append(L(" at")).append(": ").append(questGiverR.displayText(mob)).append("\n\r");
				if(questGiverR.getArea()!=null)
					announcement.append(CMStrings.padRight(L("   In"), 10)).append(": ").append(questGiverR.getArea().name(mob)).append("\n\r");
			}
			if(Q.instructions().length()>0)
				announcement.append(CMStrings.padRight(L("Descrip."), 10)).append(": ").append(Q.instructions()).append("\n\r");
		}

		final Runnable asker = new Runnable()
		{
			final MOB M=mob;
			final Quest tQ=Q;
			final String msgStr = announcement.toString();
			public void run()
			{
				if(M.session() == null)
					return;
				if(msgStr.length()>0)
					M.tell(msgStr);
				M.session().prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
				{
					final Quest quest=tQ;
					final Session session = mob.session();

					@Override
					public void showPrompt()
					{
						session.promptPrint(L("\n\rAccept this quest (y/N)? "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						if(this.input.equals("Y"))
						{
							quest.acceptQuest(M);
							for(final Enumeration<ScriptingEngine> e = M.scripts();e.hasMoreElements();)
							{
								if(e.nextElement().defaultQuestName().equalsIgnoreCase(tQ.name()))
								{
									M.tell(L("You are now on the quest '@x1'.",tQ.displayName()));
									break;
								}
							}
						}
					}
				});
			}
		};
		if(!CMParms.getParmBool(text(), "OPTIONAL", true))
		{
			final String msgStr = announcement.toString();
			if(msgStr.length()>0)
				mob.tell(msgStr);
			quest.acceptQuest(mob);
		}
		else
		if(msg != null)
			msg.addTrailerRunnable(asker);
		else
			asker.run();
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((!msg.source().isMonster())
		&&(msg.targetMinor()==getTrigger()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			{
				if(affected instanceof Area)
				{
					if((msg.target() instanceof Room)
					&&(CMLib.map().areaLocation(msg.target()) == affected)
					&&(CMLib.map().areaLocation(msg.source()) != affected))
					{
						msg.addTrailerRunnable(new Runnable()
						{
							final MOB mob=msg.source();
							final Room room=(Room)msg.target();
							@Override
							public void run()
							{
								if(room.isInhabitant(mob))
								{
									CMLib.threads().scheduleRunnable(new Runnable()
									{
										@Override
										public void run()
										{
											maybeGiveTheQuest(mob, null);
											leaveThemAlone.add(mob.Name());
										}
									},1000);
								}
							}
						});
					}
				}
				break;
			}
			case CMMsg.TYP_LEAVE:
			{
				if(affected instanceof Area)
				{
					if((msg.target() instanceof Room)
					&&(CMLib.map().areaLocation(msg.source()) == affected)
					&&(CMLib.map().areaLocation(msg.target()) != affected))
					{
						msg.addTrailerRunnable(new Runnable()
						{
							final MOB mob=msg.source();
							final Room room=(Room)msg.target();
							@Override
							public void run()
							{
								if(room.isInhabitant(mob))
								{
									CMLib.threads().scheduleRunnable(new Runnable()
									{
										@Override
										public void run()
										{
											maybeGiveTheQuest(mob,null);
											leaveThemAlone.add(mob.Name());
										}
									},1000);
								}
							}
						});
					}
				}
				break;
			}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((!msg.source().isMonster())
		&&(msg.targetMinor()==getTrigger()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			{
				boolean doIt=false;
				if(affected instanceof Area)
				{
				}
				else
				if(affected instanceof Room)
					doIt=(msg.target() == affected);
				else
				if(affected instanceof Exit)
					doIt=(msg.tool() == affected);
				else
					doIt=(msg.target() == CMLib.map().roomLocation(affected));
				if(doIt)
				{
					msg.addTrailerRunnable(new Runnable()
					{
						final MOB mob=msg.source();
						@Override
						public void run()
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								@Override
								public void run()
								{
									maybeGiveTheQuest(mob, null);
								}
							},1000);
						}
					});
				}
				break;
			}
			case CMMsg.TYP_LEAVE:
			{
				boolean doIt=false;
				if(affected instanceof Room)
					doIt=(msg.target() == affected);
				else
				if(affected instanceof Exit)
					doIt=(msg.tool() == affected);
				else
					doIt=(msg.target() == CMLib.map().roomLocation(affected));
				if(doIt)
				{
					msg.addTrailerRunnable(new Runnable()
					{
						final MOB mob=msg.source();
						@Override
						public void run()
						{
							CMLib.threads().scheduleRunnable(new Runnable()
							{
								@Override
								public void run()
								{
									maybeGiveTheQuest(mob, null);
								}
							},1000);
						}
					});
				}
				break;
			}
			case CMMsg.TYP_LOOK:
			{
				if(affected instanceof Area)
				{
					maybeGiveTheQuest(msg.source(),msg);
					leaveThemAlone.add(msg.source().Name());
				}
				else
				if(affected == msg.target())
				{
					if(msg.target() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				break;
			}
			case CMMsg.TYP_SPEAK:
			case CMMsg.TYP_READ:
			{
				if(affected instanceof Area)
				{
					maybeGiveTheQuest(msg.source(),msg);
					leaveThemAlone.add(msg.source().Name());
				}
				else
				if(affected instanceof Room)
					maybeGiveTheQuest(msg.source(),msg);
				else
				if(affected instanceof Exit)
				{
					if(msg.target() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				else
				{
					if(msg.target() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				break;
			}
			case CMMsg.TYP_DEATH:
			{
				if(affected instanceof MOB)
				{
					if(msg.source() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				else
				{
					maybeGiveTheQuest(msg.source(),msg);
					leaveThemAlone.add(msg.source().Name());
				}
				break;
			}
			case CMMsg.TYP_GET:
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_WEAR:
			{
				if(affected instanceof Item)
				{
					if(msg.target() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				else
				{
					maybeGiveTheQuest(msg.source(),msg);
					leaveThemAlone.add(msg.source().Name());
				}
				break;
			}
			case CMMsg.TYP_PUT:
			{
				if(affected instanceof Container)
				{
					if(msg.target() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				else
				if(affected instanceof Item)
				{
					if(msg.tool() == affected)
						maybeGiveTheQuest(msg.source(),msg);
				}
				else
				{
					maybeGiveTheQuest(msg.source(),msg);
					leaveThemAlone.add(msg.source().Name());
				}
				break;
			}
			default:
				break;
			}
		}
	}
}
