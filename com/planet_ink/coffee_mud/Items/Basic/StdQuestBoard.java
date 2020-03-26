package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

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

public class StdQuestBoard extends StdItem
{
	@Override
	public String ID()
	{
		return "StdQuestBoard";
	}

	public StdQuestBoard()
	{
		super();
		setName("a quest board");
		setDisplayText("a quest board is posted here.");
		setDescription("Use the READ command to read it. ");
		material=RawMaterial.RESOURCE_OAK;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	protected volatile int		questHash	= 0;
	protected volatile long		nextCheck	= 0;
	protected final List<Quest>	myQuests	= new SVector<Quest>();
	protected CompiledZMask		sareaMask	= null;
	protected CompiledZMask		tareaMask	= null;
	protected CompiledZMask		aareaMask	= null;
	protected CompiledZMask		giverMask	= null;
	protected String			typFilter	= "";
	protected String			catFilter	= "";

	protected final static String[]	MYCODES	= { "SAREAMASK", "TAREAMASK", "AAREAMASK", "GIVERMASK", "TYPFILTER", "CATFILTER" };

	@Override
	public void setReadableText(final String text)
	{
		super.setReadableText(text);
		for(final String stat : MYCODES)
			setStat(stat,CMParms.getParmStr(text, stat, ""));
	}

	protected int calculateQuestHash()
	{
		int hash = 0;
		for(final Enumeration<Quest> q=CMLib.quests().enumQuests();q.hasMoreElements();)
		{
			final Quest Q=q.nextElement();
			hash ^= Q.hashCode() + (Q.running()?1:0);
		}
		return hash;
	}

	protected List<Quest> getQuestList(final MOB mob)
	{
		final List<Quest> list=new XArrayList<Quest>(getQuestList());
		for(final Iterator<Quest> q=list.iterator();q.hasNext();)
		{
			final Quest Q=q.next();
			if(!Q.canAcceptQuest(mob))
				q.remove();
		}
		return list;
	}

	protected boolean isWellFormedQuest(final Quest Q)
	{
		if((Q.displayName()!=null)
		&&(Q.instructions().length()>0)
		&&(Q.displayName()!=null)
		&&(Q.instructions().length()>0)
		&&(Q.canAcceptQuest(null)))
			return true;
		return false;
	}

	protected List<Quest> getQuestList()
	{
		final long now=System.currentTimeMillis();
		if(now > nextCheck)
		{
			synchronized(myQuests)
			{
				if(now > nextCheck)
				{
					nextCheck=now + 5000;
					if(calculateQuestHash() != questHash)
					{
						questHash=calculateQuestHash();
						final MaskingLibrary masker=CMLib.masking();
						this.myQuests.clear();
						for(final Enumeration<Quest> q=CMLib.quests().enumQuests();q.hasMoreElements();)
						{
							final Quest Q=q.nextElement();
							if(Q.running() && (!Q.stopping()))
							{
								if((typFilter.length()>0)
								&&(!CMLib.english().containsString(Q.questTypeDesc(), typFilter)))
									continue;
								if((catFilter.length()>0)
								&&(!CMLib.english().containsString(Q.questCategory(), catFilter)))
									continue;
								Room questGiverR=null;
								final MOB questGiverM=Q.getQuestMob(1);
								if(questGiverM!=null)
								{
									questGiverR=questGiverM.location();
									Area questGiverA=null;
									questGiverA=(questGiverR!=null)?questGiverR.getArea():null;
									if((giverMask != null)
									&&(!masker.maskCheck(giverMask, questGiverM, true)))
										continue;
									if(questGiverA!=null)
									{
										if((sareaMask != null)
										&&(!masker.maskCheck(sareaMask, questGiverA, true)))
											continue;
										if((aareaMask != null)
										&&(!masker.maskCheck(aareaMask, questGiverA, true)))
											continue;
									}
								}
								if((tareaMask != null)||(aareaMask!=null))
								{
									final Set<Area> areasToDo=new TreeSet<Area>();
									for(int i=1;i<Integer.MAX_VALUE;i++)
									{
										final Room R = Q.getQuestRoom(i);
										if(R==null)
											break;
										if(R==questGiverR)
											continue;
										final Area A=R.getArea();
										if(A==null)
											continue;
										if(areasToDo.contains(A))
											continue;
										areasToDo.add(A);
										for(final Area A2 : A.getParentsRecurse())
										{
											if(!areasToDo.contains(A2))
												areasToDo.add(A2);
										}
									}
									boolean areaCheck = false;
									for(final Area A : areasToDo)
									{
										if((sareaMask != null)
										&&(masker.maskCheck(sareaMask, A, true)))
										{
											areaCheck=true;
											break;
										}
										if((aareaMask != null)
										&&(!masker.maskCheck(aareaMask, A, true)))
										{
											areaCheck=true;
											break;
										}
									}
									if(!areaCheck)
										continue;
								}
								if(isWellFormedQuest(Q))
									myQuests.add(Q);
							}
						}
					}
				}
			}
		}
		return myQuests;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
			{
				if(CMLib.flags().canBeSeenBy(this,mob)
				&&(!mob.isMonster())
				&&(mob.playerStats()!=null))
				{
					int which=0;
					final List<String> parse=CMParms.parse(msg.targetMessage());
					for(int v=0;v<parse.size();v++)
					{
						final String s=parse.get(v);
						if(CMath.s_long(s)>0)
							which=CMath.s_int(msg.targetMessage());
					}
					final StringBuilder entry = new StringBuilder("\n\r");
					Quest Q=null;
					if(which == 0)
					{
						int num=1;
						for(final Quest tQ : getQuestList(mob))
						{
							if(tQ.displayName().length()>0)
								entry.append(CMStrings.padLeft(""+num++, 3)+") "+tQ.displayName()).append("\n\r");
						}
					}
					else
					{
						int num=1;
						for(final Quest Q1 : getQuestList(mob))
						{
							if(Q1.displayName().length()>0)
							{
								if(which == num++)
								{
									Q=Q1;
									break;
								}
							}
						}
						if(Q!=null)
						{
							entry.append(CMStrings.padRight(L("Quest"), 10)).append(": ").append(Q.displayName()).append("\n\r");
							final MOB questGiverM=Q.getQuestMob(1);
							if(questGiverM!=null)
							{
								final Room questGiverR=questGiverM.location();
								entry.append(CMStrings.padRight(L("Giver"), 10)).append(": ").append(questGiverM.name())
										.append(L(" at")).append(": ").append(questGiverR.displayText(mob)).append("\n\r");
								if(questGiverR.getArea()!=null)
									entry.append(CMStrings.padRight(L("   In"), 10)).append(": ").append(questGiverR.getArea().name(mob)).append("\n\r");
							}
							if(Q.instructions().length()>0)
								entry.append(CMStrings.padRight(L("Descrip."), 10)).append(": ").append(Q.instructions()).append("\n\r");
						}
					}
					if(entry.length()>0)
					{
						final CMMsg readMsg=CMClass.getMsg(msg.source(), msg.target(), msg.tool(),
								 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, L("It says '@x1'.\n\r",entry.toString()),
								 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, entry.toString(),
								 CMMsg.NO_EFFECT, null);
						//mob.tell(entry.toString()+"\n\r");
						readMsg.setSourceMessage(readMsg.sourceMessage()+description());
						msg.addTrailerMsg(readMsg);
						if((Q != null)
						&&(mob.session() != null))
						{
							final MOB mob1=mob;
							final Quest tQ=Q;
							msg.addTrailerRunnable(new Runnable()
							{
								final MOB M=mob1;
								public void run()
								{
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
							});
						}
					}
					return; // don't let the readable text be shown!
				}
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}


}
