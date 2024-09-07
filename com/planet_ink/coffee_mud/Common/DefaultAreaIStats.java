package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class DefaultAreaIStats implements AreaIStats
{
	private volatile AISBuildState	state		= AISBuildState.NOT_STARTED;
	private final int[]				statData	= new int[Area.Stats.values().length];
	private Race					commonRace	= null;

	private enum AISBuildState
	{
		NOT_STARTED,
		RUNNING,
		FINISHED
	}

	private class AISBuild
	{
		private Faction				theFaction		= null;
		private final List<Integer>	levelRanges		= new Vector<Integer>();
		private final List<Integer>	alignRanges		= new Vector<Integer>();
		private volatile long		totalAlignments	= 0;
		private RoomnumberSet		done			= null;
		private final Map<Race,Pair<int[], Boolean>> races = new Hashtable<Race,Pair<int[], Boolean>>();
	}

	@Override
	public String ID()
	{
		return "DefaultAreaIStats";
	}

	@Override
	public String name()
	{
		return "DefaultAreaIStats";
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultAreaIStats();
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return new DefaultAreaIStats();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public boolean isFinished()
	{
		return state == AISBuildState.FINISHED;
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public int getStat(final Area.Stats stat)
	{
		return this.statData[stat.ordinal()];
	}

	@Override
	public void setStat(final Area.Stats stat, final int val)
	{
		this.statData[stat.ordinal()] = val;
	}

	protected void buildAreaIMobStats(final AISBuild workData, final MOB mob)
	{
		if ((mob != null) && mob.isMonster())
		{
			if (!CMLib.flags().isUnattackable(mob))
			{
				AISBuild wk;
				synchronized(workData)
				{
					wk = workData;
				}
				final Race R = mob.baseCharStats().getMyRace();
				final int lvl = mob.basePhyStats().level();
				wk.levelRanges.add(Integer.valueOf(lvl));
				if ((wk.theFaction != null)
				&& (mob.fetchFaction(wk.theFaction.factionID()) != Integer.MAX_VALUE))
				{
					wk.alignRanges.add(Integer.valueOf(mob.fetchFaction(wk.theFaction.factionID())));
					wk.totalAlignments += mob.fetchFaction(wk.theFaction.factionID());
				}
				statData[Area.Stats.POPULATION.ordinal()]++;
				statData[Area.Stats.TOTAL_LEVELS.ordinal()] += lvl;
				boolean animalFlag = false;
				if (!CMLib.flags().isAnimalIntelligence(mob))
				{
					statData[Area.Stats.TOTAL_INTELLIGENT_LEVELS.ordinal()] += lvl;
					statData[Area.Stats.INTELLIGENT_MOBS.ordinal()]++;
				}
				else
				{
					statData[Area.Stats.ANIMALS.ordinal()]++;
					animalFlag = true;
				}
				if(!wk.races.containsKey(R))
					wk.races.put(R, new Pair<int[], Boolean>(new int[] {0}, Boolean.valueOf(animalFlag)));
				wk.races.get(R).first[0]++;
				if (lvl < statData[Area.Stats.MIN_LEVEL.ordinal()])
					statData[Area.Stats.MIN_LEVEL.ordinal()] = lvl;
				if (lvl >= statData[Area.Stats.MAX_LEVEL.ordinal()])
				{
					if (lvl > statData[Area.Stats.MAX_LEVEL.ordinal()])
					{
						statData[Area.Stats.MAX_LEVEL.ordinal()] = lvl;
						statData[Area.Stats.MAX_LEVEL_MOBS.ordinal()] = 0;
					}
					statData[Area.Stats.MAX_LEVEL_MOBS.ordinal()]++;
				}
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
				{
					if(CMLib.flags().isGood(mob))
						statData[Area.Stats.GOOD_MOBS.ordinal()]++;
					else
					if(CMLib.flags().isEvil(mob))
						statData[Area.Stats.EVIL_MOBS.ordinal()]++;
				}
				if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
				{
					if(CMLib.flags().isLawful(mob))
						statData[Area.Stats.LAWFUL_MOBS.ordinal()]++; else
					if(CMLib.flags().isChaotic(mob))
						statData[Area.Stats.CHAOTIC_MOBS.ordinal()]++;
				}
				if(mob.fetchEffect("Prop_ShortEffects")!=null)
					statData[Area.Stats.BOSS_MOBS.ordinal()]++;
				if(" Humanoid Elf Dwarf Halfling HalfElf ".indexOf(" "+R.racialCategory()+" ")>=0)
					statData[Area.Stats.HUMANOIDS.ordinal()]++;
			}
		}
	}

	protected void buildAreaIRoomMobsStats(final AISBuild workData, final Room R)
	{
		for (int i = 0; i < R.numInhabitants(); i++)
			buildAreaIMobStats(workData, R.fetchInhabitant(i));
	}

	protected void buildAreaIItemStats(final AISBuild workData, final Item I)
	{
		if((I instanceof Boardable)
		&&((!(I instanceof PrivateProperty))
			||(!((PrivateProperty)I).isProperlyOwned())))
		{
			final Area A = ((Boardable) I).getArea();
			if (A == null)
				return;
			for (final Enumeration<Room> r2 = A.getProperMap(); r2.hasMoreElements();)
			{
				final Room R2 = r2.nextElement();
				for (int i2 = 0; i2 < R2.numInhabitants(); i2++)
					buildAreaIMobStats(workData, R2.fetchInhabitant(i2));
			}
		}
	}

	@Override
	public Race getCommonRace()
	{
		return this.commonRace;
	}

	protected void buildAreaIRoomItemsStats(final AISBuild workData, final Room R)
	{
		for (int i = 0; i < R.numItems(); i++)
		{
			final Item I = R.getItem(i);
			buildAreaIItemStats(workData, I);
		}
	}

	protected void buildAreaIRoomStats(final AISBuild workData, final Room R)
	{
		final int countable;
		if (R instanceof GridLocale)
		{
			statData[Area.Stats.VISITABLE_ROOMS.ordinal()]--;
			countable = ((GridLocale) R).getGridSize();
		}
		else
			countable = 1;
		statData[Area.Stats.COUNTABLE_ROOMS.ordinal()] += countable;
		if ((R.domainType() & Room.INDOORS) > 0)
		{
			statData[Area.Stats.INDOOR_ROOMS.ordinal()] += countable;
			switch (R.domainType())
			{
			case Room.DOMAIN_INDOORS_CAVE:
				statData[Area.Stats.CAVE_ROOMS.ordinal()] += countable;
				break;
			case Room.DOMAIN_INDOORS_METAL:
			case Room.DOMAIN_INDOORS_STONE:
			case Room.DOMAIN_INDOORS_WOOD:
				statData[Area.Stats.CITY_ROOMS.ordinal()] += countable;
				break;
			case Room.DOMAIN_INDOORS_UNDERWATER:
			case Room.DOMAIN_INDOORS_WATERSURFACE:
				statData[Area.Stats.WATER_ROOMS.ordinal()] += countable;
				break;
			}
		}
		else
		{
			switch (R.domainType())
			{
			case Room.DOMAIN_OUTDOORS_CITY:
				statData[Area.Stats.CITY_ROOMS.ordinal()] += countable;
				break;
			case Room.DOMAIN_OUTDOORS_DESERT:
				statData[Area.Stats.DESERT_ROOMS.ordinal()] += countable;
				break;
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
			case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				statData[Area.Stats.WATER_ROOMS.ordinal()] += countable;
				break;
			}
		}
		this.buildAreaIRoomMobsStats(workData, R);
		this.buildAreaIRoomItemsStats(workData, R);
		if(R.roomID().length()>0)
		{
			synchronized(workData.done)
			{
				workData.done.add(R.roomID());
			}
		}
	}

	protected void finishBuildingStats(final AISBuild workData, final Area A)
	{
		AISBuild wk;
		synchronized(workData)
		{
			wk = workData;
		}
		if ((statData[Area.Stats.POPULATION.ordinal()] == 0)
		|| (wk.levelRanges.size() == 0))
		{
			statData[Area.Stats.MIN_LEVEL.ordinal()] = 0;
			statData[Area.Stats.MAX_LEVEL.ordinal()] = 0;
		}
		else
		{
			Collections.sort(wk.levelRanges);
			Collections.sort(wk.alignRanges);
			statData[Area.Stats.MED_LEVEL.ordinal()] = wk.levelRanges.get((int) Math.round(Math.floor(CMath.div(wk.levelRanges.size(), 2.0)))).intValue();
			if (wk.alignRanges.size() > 0)
			{
				statData[Area.Stats.MED_ALIGNMENT.ordinal()] = wk.alignRanges.get((int) Math.round(Math.floor(CMath.div(wk.alignRanges.size(), 2.0)))).intValue();
				statData[Area.Stats.MIN_ALIGNMENT.ordinal()] = wk.alignRanges.get(0).intValue();
				statData[Area.Stats.MAX_ALIGNMENT.ordinal()] = wk.alignRanges.get(wk.alignRanges.size() - 1).intValue();
			}
			statData[Area.Stats.AVG_LEVEL.ordinal()] = (int) Math.round(CMath.div(statData[Area.Stats.TOTAL_LEVELS.ordinal()], statData[Area.Stats.POPULATION.ordinal()]));
			statData[Area.Stats.AVG_ALIGNMENT.ordinal()] = (int) Math.round(((double) wk.totalAlignments) / (statData[Area.Stats.POPULATION.ordinal()]));
			if(wk.levelRanges.size()>0)
			{
				Integer modeLevel=null;
				int modeCt = -1;
				Integer lastI=wk.levelRanges.get(0);
				int curCt = 0;
				for(int i=1;i<wk.levelRanges.size();i++)
				{
					final Integer I=wk.levelRanges.get(i);
					if(I.intValue() != lastI.intValue())
					{
						if(curCt > modeCt)
						{
							modeLevel = lastI;
							modeCt=curCt;
						}
						curCt=0;
					}
					lastI=I;
					curCt++;
				}
				if(curCt > modeCt)
				{
					modeLevel = lastI;
					modeCt=curCt;
				}
				if(modeLevel != null)
					statData[Area.Stats.MODE_LEVEL.ordinal()] = modeLevel.intValue();
			}
		}
		wk.levelRanges.clear();
		if(wk.alignRanges.size()>0)
		{
			Integer modeAlign=null;
			int modeCt = -1;
			Integer lastI=wk.alignRanges.get(0);
			int curCt = 0;
			for(int i=1;i<wk.alignRanges.size();i++)
			{
				final Integer I=wk.alignRanges.get(i);
				if(I.intValue() != lastI.intValue())
				{
					if(curCt > modeCt)
					{
						modeAlign = lastI;
						modeCt=curCt;
					}
					curCt=0;
				}
				lastI=I;
				curCt++;
			}
			if(curCt > modeCt)
			{
				modeAlign = lastI;
				modeCt=curCt;
			}
			if(modeAlign != null)
				statData[Area.Stats.MODE_ALIGNMENT.ordinal()] = modeAlign.intValue();
		}
		wk.alignRanges.clear();
		int maxRace = 0;
		Race winRace = null;
		for(final Race R : wk.races.keySet())
		{
			if(wk.races.get(R).first[0] > maxRace)
			{
				maxRace = wk.races.get(R).first[0];
				winRace = R;
			}
		}
		commonRace = winRace;
		A.basePhyStats().setLevel(statData[Area.Stats.MED_LEVEL.ordinal()]);
		A.phyStats().setLevel(statData[Area.Stats.MED_LEVEL.ordinal()]);
		Resources.removeResource("HELP_" + A.Name().toUpperCase());
		state = AISBuildState.FINISHED;
		wk = null;
	}

	@Override
	public AreaIStats build(final Area A)
	{
		if((state != AISBuildState.NOT_STARTED) || (A==null))
			return this;
		state = AISBuildState.RUNNING;
		Faction theF = null;
		for (final Enumeration<Faction> e = CMLib.factions().factions(); e.hasMoreElements();)
		{
			final Faction F = e.nextElement();
			if (F.showInSpecialReported())
				theF = F;
		}
		final AISBuild workData = new AISBuild();
		workData.theFaction = theF;
		workData.done = (RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		statData[Area.Stats.POPULATION.ordinal()] = 0;
		statData[Area.Stats.MIN_LEVEL.ordinal()] = Integer.MAX_VALUE;
		statData[Area.Stats.MAX_LEVEL.ordinal()] = Integer.MIN_VALUE;
		statData[Area.Stats.AVG_LEVEL.ordinal()] = 0;
		statData[Area.Stats.MED_LEVEL.ordinal()] = 0;
		statData[Area.Stats.MODE_LEVEL.ordinal()] = 0;
		statData[Area.Stats.MODE_ALIGNMENT.ordinal()] = 0;
		statData[Area.Stats.AVG_ALIGNMENT.ordinal()] = 0;
		statData[Area.Stats.TOTAL_LEVELS.ordinal()] = 0;
		statData[Area.Stats.TOTAL_INTELLIGENT_LEVELS.ordinal()] = 0;
		statData[Area.Stats.VISITABLE_ROOMS.ordinal()] = A.getProperRoomnumbers().roomCountAllAreas();
		Resources.removeResource("PIETY_"+A.Name().toUpperCase());
		for (final Enumeration<Room> r = A.getProperMap(); r.hasMoreElements();)
		{
			final Room R = r.nextElement();
			this.buildAreaIRoomStats(workData,R);
		}
		final double totalRooms=A.getProperRoomnumbers().roomCountAllAreas();
		final double currentRooms=A.getCachedRoomnumbers().roomCountAllAreas();
		if((CMath.bset(A.flags(), Area.FLAG_THIN)) &&(totalRooms > currentRooms))
		{
			final Runnable roomRun = new Runnable()
			{
				final Enumeration<String> rid = A.getProperRoomnumbers().getRoomIDs();
				final AISBuild wkDat = workData;
				final Area areaA = A;

				@Override
				public void run()
				{
					if(CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN))
						return;
					if((areaA==null)||(areaA.amDestroyed()))
						return;
					if(!rid.hasMoreElements())
						finishBuildingStats(wkDat,areaA);
					else
					{
						String roomID = rid.nextElement();
						final int x = roomID.lastIndexOf("#(");
						if(x > 0)
							roomID=roomID.substring(0,x);
						if(workData.done.contains(roomID))
							CMLib.threads().scheduleRunnable(this, 1);
						else
						{
							final long startTime = System.currentTimeMillis();
							final Room R = CMLib.database().DBReadRoomObject(roomID, false, false);
							if(R == null)
								Log.debugOut("Unknown roomID building '"+areaA.Name()+"' istats: "+roomID);
							else
							{
								try
								{
									CMLib.database().DBReadContent(roomID,R,false);
									CMLib.threads().unTickAll(R);
									buildAreaIRoomStats(wkDat,R);
									if(R instanceof GridLocale)
									{
										for(final Room gR : ((GridLocale)R).getAllRooms())
											buildAreaIRoomStats(wkDat,gR);
									}
								}
								catch(final Exception e)
								{
									Log.errOut(e);
								}
								finally
								{
									R.destroy();
								}
							}
							CMLib.threads().scheduleRunnable(this, 10+(System.currentTimeMillis()-startTime));
						}
					}
				}
			};
			// you are probably Not Done
			CMLib.threads().scheduleRunnable(roomRun, 1);
		}
		else
			finishBuildingStats(workData,A);
		return this;
	}
}
