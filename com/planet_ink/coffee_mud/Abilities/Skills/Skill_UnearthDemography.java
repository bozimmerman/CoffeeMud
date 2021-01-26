package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2020-2021 Bo Zimmerman

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
public class Skill_UnearthDemography extends StdAbility
{
	@Override
	public String ID()
	{
		return "Skill_UnearthDemography";
	}

	private final static String	localizedName	= CMLib.lang().L("Unearth Demography");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= L("(Unearthing Local Demography)");

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "UNEARTHDEMOGRAPHY", "DEMOGRAPHY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean putInCommandlist()
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_STREETSMARTS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected enum DemogField
	{
		COUNT,
		ALIGNMENT,
		INCLINATION,
		AVG_LEVEL,
		MIN_LEVEL,
		MAX_LEVEL,
		BEHAVIOR,
		PCT_COUNT
	}

	protected static Map<String,Map<String,Map<DemogField,String>>> allAreaInfo = new TreeMap<String,Map<String,Map<DemogField,String>>>();

	protected final Map<String,Map<String,Map<DemogField,String>>> knownAreaInfo = new TreeMap<String,Map<String,Map<DemogField,String>>>();

	@Override
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		if(text.length()>0)
		{
			knownAreaInfo.clear();
			final XMLLibrary xml=CMLib.xml();
			final List<XMLLibrary.XMLTag> areaStrsV = xml.parseAllXML(text);
			for(final XMLLibrary.XMLTag atag : areaStrsV)
			{
				if(atag.tag().equalsIgnoreCase("AREA"))
				{
					final Map<String,Map<DemogField,String>> races = new TreeMap<String,Map<DemogField,String>>();
					final String name=xml.restoreAngleBrackets(atag.getParmValue("NAME"));
					if((name != null)
					&&(name.length()>0)
					&&(atag.contents()!=null)
					&&(atag.contents().size()>0))
					{
						knownAreaInfo.put(name, races);
						for(final XMLLibrary.XMLTag rtag : atag.contents())
						{
							if(rtag.tag().equalsIgnoreCase("RACE"))
							{
								final Map<DemogField,String> fields = new TreeMap<DemogField,String>();
								final String id=xml.restoreAngleBrackets(rtag.getParmValue("ID"));
								if((id != null)&&(id.length()>0))
								{
									races.put(id, fields);
									if((rtag.contents()!=null)&&(rtag.contents().size()>0))
									{
										for(final XMLLibrary.XMLTag ftag : rtag.contents())
										{
											if(ftag.tag().equalsIgnoreCase("FIELD"))
											{
												final DemogField f=(DemogField)CMath.s_valueOf(DemogField.class, ftag.getParmValue("ID"));
												if(f!=null)
													fields.put(f, xml.restoreAngleBrackets(ftag.value()));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}


	protected void updateText()
	{
		final StringBuilder str=new StringBuilder("");
		final XMLLibrary xml=CMLib.xml();
		for(final String areaName : knownAreaInfo.keySet())
		{
			final int x=areaName.indexOf('_');
			if((x<0)||(!CMath.isInteger(areaName.substring(0,x))))
			{
				final Map<String,Map<DemogField,String>> races = knownAreaInfo.get(areaName);
				str.append("<AREA NAME=\""+xml.parseOutAngleBracketsAndQuotes(areaName)+"\">");
				for(final String raceName : races.keySet())
				{
					final Map<DemogField,String> fields = races.get(raceName);
					str.append("<RACE ID=\""+xml.parseOutAngleBracketsAndQuotes(raceName)+"\">");
					for(final DemogField field : fields.keySet())
					{
						str.append("<FIELD ID=\""+xml.parseOutAngleBracketsAndQuotes(field.toString())+"\">");
						str.append(xml.parseOutAngleBrackets(fields.get(field)));
						str.append("</FIELD>");
					}
					str.append("</RACE>");
				}
				str.append("</AREA>");
			}
		}
		super.miscText = str.toString();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.target() instanceof Room))
		{
			if(msg.targetMinor()==CMMsg.TYP_ENTER)
			{
				slowDown=true;
				slowExpire=System.currentTimeMillis()+CMProps.getTickMillis()+1;
				mob.recoverPhyStats();
				if((System.currentTimeMillis()>nextTidbit)||(nextTidbit==0)||(nextTidbit==Long.MAX_VALUE))
					nextTidbit=System.currentTimeMillis()+CMProps.getTickMillis();
				final Room R=(Room)msg.target();
				if(R.getArea()!=workingArea.get())
				{
					final Ability me=this;
					CMLib.threads().scheduleRunnable(new Runnable()
					{
						final Ability meA=me;
						@Override
						public void run()
						{
							meA.unInvoke();
						}
					}, 1000);
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if(P instanceof MOB)
			((MOB)P).tell(L("You stop unearthing demographic clues."));
	}

	protected volatile boolean slowDown = false;
	protected volatile long slowExpire = 0;
	protected volatile long nextTidbit = 0;

	protected WeakReference<Area> workingArea = null;
	protected volatile Map<String,Map<DemogField,String>> unearthedableKnowledge = null;

	protected volatile Enumeration<String> workingRoomIDs = null;
	protected final Map<String,Pair<long[],Map<String,int[]>>> workCounters = new TreeMap<String,Pair<long[],Map<String,int[]>>>();
	protected volatile int roomCounter = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Tickable.TICKID_MOB)
			return true;
		final long now=System.currentTimeMillis();
		final Physical affected=this.affected;
		if((slowDown) && (now > slowExpire))
		{
			slowDown=false;
			if(affected != null)
				affected.recoverPhyStats();
		}
		if(workingArea != null)
		{
			final Area workA = workingArea.get();
			if(workA != null)
			{
				if((affected instanceof MOB)
				&&(((MOB)affected).location()!=null)
				&&(((MOB)affected).location().getArea()!=workA))
				{
					unInvoke();
					return false;
				}
				unearthedableKnowledge = Skill_UnearthDemography.allAreaInfo.get(workA.Name());
				if(unearthedableKnowledge == null)
				{
					if(workingRoomIDs == null)
					{
						workCounters.clear();
						workingRoomIDs = workA.getProperRoomnumbers().getRoomIDs();
						final int numRoomsPerTick = workA.numberOfProperIDedRooms() / 10;
						roomCounter = 1;
						if(numRoomsPerTick > 1)
							roomCounter = numRoomsPerTick;
					}
					if(!workingRoomIDs.hasMoreElements())
					{
						final Map<String,Map<DemogField,String>> unearthedableKnowledge = new TreeMap<String,Map<DemogField,String>>();
						int totalMobs = 0;
						for(final String rname : workCounters.keySet())
						{
							final Pair<long[], Map<String,int[]>> data=workCounters.get(rname);
							totalMobs += data.first[DemogField.COUNT.ordinal()];
						}
						final String factionID1=CMLib.factions().getAlignmentID();
						final String factionID2=CMLib.factions().getInclinationID();
						final Faction F1=CMLib.factions().getFaction(factionID1);
						final Faction F2=CMLib.factions().getFaction(factionID2);
						for(final String rname : workCounters.keySet())
						{
							final Pair<long[], Map<String,int[]>> data=workCounters.get(rname);
							final Map<DemogField,String> rmap=new TreeMap<DemogField,String>();
							rmap.put(DemogField.COUNT, ""+data.first[DemogField.COUNT.ordinal()]);
							unearthedableKnowledge.put(rname, rmap);
							if(data.first[DemogField.COUNT.ordinal()]>0)
							{
								final int avg1=(int)(data.first[DemogField.ALIGNMENT.ordinal()] / data.first[DemogField.COUNT.ordinal()]);
								rmap.put(DemogField.ALIGNMENT, (F1==null)?(""+avg1):F1.fetchRangeName(avg1));
								final int avg2=(int)(data.first[DemogField.INCLINATION.ordinal()] / data.first[DemogField.COUNT.ordinal()]);
								rmap.put(DemogField.INCLINATION, (F2==null)?(""+avg1):F2.fetchRangeName(avg2));
								final int avg3=(int)(data.first[DemogField.AVG_LEVEL.ordinal()] / data.first[DemogField.COUNT.ordinal()]);
								rmap.put(DemogField.AVG_LEVEL, ""+avg3);
								rmap.put(DemogField.MIN_LEVEL, ""+data.first[DemogField.MIN_LEVEL.ordinal()]);
								rmap.put(DemogField.MAX_LEVEL, ""+data.first[DemogField.MAX_LEVEL.ordinal()]);
								String bestBehav = "";
								int highest = 0;
								for(final String str : data.second.keySet())
								{
									if(data.second.get(str)[0] > highest)
									{
										highest=data.second.get(str)[0];
										bestBehav=str;
									}
								}
								rmap.put(DemogField.BEHAVIOR, bestBehav);
								final double pct=CMath.div(data.first[DemogField.COUNT.ordinal()], (double)totalMobs);
								rmap.put(DemogField.PCT_COUNT, CMath.toWholePct(pct));
							}
						}
						allAreaInfo.put(workA.Name(), unearthedableKnowledge);
						this.unearthedableKnowledge = unearthedableKnowledge;
						workingRoomIDs=null;
						workCounters.clear();
					}
					else
					{
						final long startTime=System.currentTimeMillis();
						final String factionID1=CMLib.factions().getAlignmentID();
						final String factionID2=CMLib.factions().getInclinationID();
						final Faction F1=CMLib.factions().getFaction(factionID1);
						final Faction F2=CMLib.factions().getFaction(factionID2);
						for(int i=0; (i<roomCounter) && (workingRoomIDs.hasMoreElements());i++)
						{
							final String nextRoomID=workingRoomIDs.nextElement();
							final Room baseR=workA.getRoom(nextRoomID);
							if(baseR!=null)
							{
								final Room R=CMClass.getLocale("StdRoom");
								R.setRoomID(baseR.roomID());
								CMLib.database().DBReadMobContent(R.roomID(),R);
								for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
								{
									final MOB M=m.nextElement();
									final Race raceR=(M!=null)?M.baseCharStats().getMyRace():null;
									if((M!=null)&&(raceR!=null))
									{
										if(!workCounters.containsKey(raceR.name()))
										{
											workCounters.put(raceR.name(), new Pair<long[],Map<String,int[]>>(
													new long[DemogField.values().length],
													new TreeMap<String,int[]>()));
										}
										final Pair<long[],Map<String,int[]>> p=workCounters.get(raceR.name());
										synchronized(p)
										{
											p.first[DemogField.COUNT.ordinal()]++;
											int fint1=M.fetchFaction(factionID1);
											if(fint1==Integer.MAX_VALUE)
												fint1=(F1==null)?0:F1.middle();
											p.first[DemogField.ALIGNMENT.ordinal()] += fint1;
											int fint2=M.fetchFaction(factionID2);
											if(fint2==Integer.MAX_VALUE)
												fint2=(F2==null)?0:F2.middle();
											p.first[DemogField.INCLINATION.ordinal()] += fint2;
										}
										p.first[DemogField.AVG_LEVEL.ordinal()] += M.basePhyStats().level();
										if((p.first[DemogField.MIN_LEVEL.ordinal()]==0)||(M.basePhyStats().level()<p.first[DemogField.MIN_LEVEL.ordinal()]))
											p.first[DemogField.MIN_LEVEL.ordinal()]=M.basePhyStats().level();
										if(M.basePhyStats().level()>p.first[DemogField.MAX_LEVEL.ordinal()])
											p.first[DemogField.MAX_LEVEL.ordinal()]=M.basePhyStats().level();
										for(final Enumeration<Behavior> b=M.behaviors();b.hasMoreElements();)
										{
											final Behavior B=b.nextElement();
											if(!(B.ID().equals("Scriptable")))
											{
												final String accounting=B.accountForYourself();
												if((accounting!=null)&&(accounting.length()>0))
												{
													if(!p.second.containsKey(accounting))
														p.second.put(accounting, new int[] {0});
													final int[] ct = p.second.get(accounting);
													synchronized(ct)
													{
														ct[0]++;
													}
												}
											}
										}
									}
								}
								R.destroy();
							}
						}
						final long ellapsed = System.currentTimeMillis() - startTime;
						if(ellapsed > 100)
						{
							if(roomCounter > 1) // if it took too long, adjust it
								roomCounter = (int)Math.round(CMath.div(roomCounter,(ellapsed/100.0)));
						}
					}
				}
				else
				if((now > nextTidbit) && (nextTidbit > 0))
				{
					nextTidbit = Long.MAX_VALUE;
					if(affected instanceof MOB)
					{
						final String discovery=this.getNewTidbit();
						final MOB mob=(MOB)affected;
						if((discovery!=null)&&(discovery.length()>0))
							mob.tell(discovery);
						else
							mob.tell(L("You have failed to unearth any new clues about this area."));
					}
				}
			}
		}

		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB) && slowDown && (!((MOB)affected).isInCombat()))
		{
			affectableStats.setSpeed(affectableStats.speed()/2.0);
		}
	}

	protected String translateField(final String raceName, final DemogField field, final String value)
	{
		return L("The @x1 in this area are @x2.",CMLib.english().makePlural(raceName),translateField(field,value).toLowerCase());
	}

	protected String translateField(final DemogField field, final String value)
	{
		if((value==null)||(value.length()==0))
			return "";
		switch(field)
		{
		case ALIGNMENT:
			return L("Mostly @x1",value);
		case AVG_LEVEL:
			return L("Of avg level @x1",value);
		case BEHAVIOR:
			return L("Known to engage in @x1",value);
		case COUNT:
			return L("Numbering @x1",value);
		case INCLINATION:
			return L("Mostly @x1",value);
		case MAX_LEVEL:
			return L("Can be as high as level @x1",value);
		case MIN_LEVEL:
			return L("Can be as weak as level @x1",value);
		case PCT_COUNT:
			return L("@x1 of all creatures",value);
		default:
			return "";
		}
	}

	protected String getNewTidbit()
	{
		if((this.unearthedableKnowledge != null)
		&&(this.workingArea!=null))
		{
			final Area workA=this.workingArea.get();
			if(workA!=null)
			{
				if(!this.knownAreaInfo.containsKey(workA.Name()))
					this.knownAreaInfo.put(workA.Name(), new TreeMap<String,Map<DemogField,String>>());
				final Map<String,Map<DemogField,String>> myInfo = this.knownAreaInfo.get(workA.Name());
				List<String> raceNames = new XArrayList<String>(this.unearthedableKnowledge.keySet());
				if(raceNames.size()>0)
				{
					final double points = (1 + (adjustedLevel(invoker(),0) / 10)) + super.getXLEVELLevel(invoker());
					final double maxPoints = 10 + CMath.div(CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL),10.0);
					String raceName;
					if(points >= myInfo.size())
					{
						Collections.sort(raceNames, new Comparator<String>()
						{
							@Override
							public int compare(final String o1, final String o2)
							{
								final int l1=CMath.s_int(unearthedableKnowledge.get(o1).get(DemogField.COUNT));
								final int l2=CMath.s_int(unearthedableKnowledge.get(o1).get(DemogField.COUNT));
								if(l1>l2)
									return -1;
								else
								if(l1<l2)
									return 1;
								return 0;
							}
						});
						raceName=null;
						for(final String r : raceNames)
						{
							if(!myInfo.containsKey(r) || (myInfo.get(r).size()==0))
							{
								raceName=r;
								break;
							}
						}
						if(raceName == null)
							raceName = raceNames.get(CMLib.dice().roll(1, raceNames.size(), -1));
					}
					else
					{
						raceNames = new XArrayList<String>(myInfo.keySet());
						raceName = raceNames.get(CMLib.dice().roll(1, raceNames.size(), -1));
					}

					if(!unearthedableKnowledge.containsKey(raceName))
						unearthedableKnowledge.put(raceName, new TreeMap<DemogField,String>());
					final Map<DemogField,String> theFields = unearthedableKnowledge.get(raceName);
					if(!myInfo.containsKey(raceName))
						myInfo.put(raceName, new TreeMap<DemogField,String>());
					final Map<DemogField,String> myFields = myInfo.get(raceName);
					final int fieldOrdinal = CMLib.dice().roll(1, DemogField.values().length, -1);
					final DemogField field = DemogField.values()[fieldOrdinal];
					if(myFields.containsKey(field))
						return "";
					final int max=(int)Math.round(CMath.mul(DemogField.values().length, CMath.div(points,maxPoints)))+1;
					if((myFields.size()>0)&&(myFields.size()>=max))
						return "";
					if(theFields.containsKey(field))
					{
						myFields.put(field, theFields.get(field));
						final Physical affected=this.affected;
						if(affected instanceof MOB)
						{
							// I believe, since the effect is copying the base, the existing fields are auto-updated in both.
							final Skill_UnearthDemography A=(Skill_UnearthDemography)((MOB)affected).fetchAbility(ID());
							if(A!=null)
								A.updateText();
						}
						return translateField(raceName,field,theFields.get(field));
					}
				}
			}
		}
		return "";
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!CMLib.flags().isAliveAwakeMobile(mob,false))
			return false;

		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;

		if((commands.size()>0)
		&&(commands.get(0).equalsIgnoreCase("stop")))
		{
			final Ability oldA=mob.fetchEffect(ID());
			if(oldA!=null)
			{
				oldA.unInvoke();
				return true;
			}
			else
				mob.tell(L("You weren't unearthing anything."));
			return false;
		}
		else
		if((commands.size()==0)
		||(!commands.get(0).equalsIgnoreCase("start")))
		{
			final Area theA;
			if(commands.size()==0)
				theA=A;
			else
			{
				final String areaNameStr=CMParms.combine(commands);
				Area A1=CMLib.map().getArea(areaNameStr);
				if(A1==null)
					A1=CMLib.map().findArea(areaNameStr);
				if(A1==null)
				{
					mob.tell(L("You don't know any area called '@x1'.",areaNameStr));
					return false;
				}
				theA=A1;
			}
			if(!knownAreaInfo.containsKey(theA.Name()))
			{
				final Ability oldA=mob.fetchEffect(ID());
				if(oldA!=null)
				{
					mob.tell(L("You do not yet have any information about @x1.  "
							+ "Use UNEARTH START to start investigating.",theA.Name()));
				}
				else
				{
					mob.tell(L("You do not yet have any information about @x1.  Keep looking!",theA.Name()));
				}
				return false;
			}
			else
			{
				final StringBuilder str=new StringBuilder(L("\n\rInformation about @x1\n\r",theA.Name()));
				final Map<String,Map<DemogField,String>> races = knownAreaInfo.get(theA.Name());
				for(final String raceName : races.keySet())
				{
					final Map<DemogField,String> fields = races.get(raceName);
					str.append(L("You found @x1 signs",raceName));
					if(fields.size()==0)
						str.append(".\n\r");
					else
					{
						str.append(L(", and they are: \n\r"));
						for(final DemogField field : fields.keySet())
						{
							final String info=this.translateField(field, fields.get(field));
							if((info!=null)&&(info.length()>0))
								str.append(L("    @x1.\n\r",info));
						}
					}
				}
				mob.tell(str.toString());
			}
			return true;
		}

		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			mob.tell(L("You can't see anything to unearth!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=super.proficiencyCheck(mob, 0, auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> begin(s) unearthing clues about the local fauna and demography."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				this.workingArea = new WeakReference<Area>(A);
				final Skill_UnearthDemography demoA = (Skill_UnearthDemography)super.beneficialAffect(mob, mob, asLevel, Integer.MAX_VALUE/5);
				if(demoA != null)
				{
					demoA.workingArea = new WeakReference<Area>(A);
					demoA.unearthedableKnowledge=null;
				}

				mob.recoverPhyStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to unearth some clues, but can't seem to concentrate."));
		// return whether it worked
		return success;
	}
}
