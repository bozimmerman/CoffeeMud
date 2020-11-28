package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.Songs.Skill_Disguise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
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
public class Thief_InciteDivineFeud extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_InciteDivineFeud";
	}

	private final static String localizedName = CMLib.lang().L("Incite Divine Feud");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Incite Divine Feud)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"INCITEDIVINEFEUD"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}
	
	protected volatile long timeToNextCast=0;
	
	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast=absoluteTime;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}
	
	protected List<Area> findDeityAreas(final Deity D)
	{
		final List<Area> deityAreas = new ArrayList<Area>();
		for(final Enumeration<Places> p=D.holyPlaces();p.hasMoreElements();)
		{
			final Places P=p.nextElement();
			final String deityName=CMLib.law().getClericInfused(P);
			if((deityName!=null)
			&&(D.Name().equalsIgnoreCase(deityName)))
			{
				if((P instanceof Area)
				&&(!deityAreas.contains(P)))
					deityAreas.add((Area)P);
				else
				if((P instanceof Room)
				&&(!deityAreas.contains(((Room)P).getArea())))
					deityAreas.add(((Room)P).getArea());
			}
		}
		if(deityAreas.size()==0)
		{
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				int piety = D.getAreaPiety(A.Name());
				if(piety > 5)
					deityAreas.add(A);
			}
		}
		for(final Iterator<Area> a=deityAreas.iterator();a.hasNext();)
		{
			final Area A=a.next();
			if(CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
				a.remove();
			else
			if((A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]<10)
			||(A.getAreaIStats()[Area.Stats.COUNTABLE_ROOMS.ordinal()]<10))
				a.remove();
		}
		return deityAreas;
	}

	public Quest deviseAndStartQuest(final MOB affected, final Map<String,Object> definedIDs)
	{
		int maxAttempts=5;
		while((--maxAttempts)>=0)
		{
			try
			{
				final StringBuffer xml = Resources.getFileResource("randareas/example.xml", true);
				if((xml==null)||(xml.length()==0))
				{
					Log.errOut("Unable to generate a quest for "+affected.name()+" because file not found: randareas/example.xml");
					return null;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				if(!definedIDs.containsKey("QUEST_CRITERIA"))
					definedIDs.put("QUEST_CRITERIA", "-NAME \""+affected.Name()+"\" -NPC");
				Map<String,Object> preDefined=new XHashtable<String,Object>(definedIDs);
				CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, preDefined.keySet());
				final String idName = "ALL_QUESTS";
				if((!(definedIDs.get(idName) instanceof XMLTag))
				||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("quest")))
				{
					Log.errOut(L("The quest id '@x1' has not been defined in the data file for @x2.",idName,affected.name()));
					return null;
				}
				final XMLTag piece=(XMLTag)definedIDs.get(idName);
				try
				{
					CMLib.percolator().checkRequirements(piece, definedIDs);
				}
				catch(final CMException cme)
				{
					Log.errOut(L("Required ids for @x1 were missing: @x2: for @x3",idName,cme.getMessage(),affected.name()));
					return null;
				}
				final Modifiable obj = null;
				final String s=CMLib.percolator().buildQuestScript(piece, definedIDs, obj);
				if(s.length()==0)
					throw new CMException("Failed to create any sort of quest at all! WTF!!");
				CMLib.percolator().postProcess(definedIDs);
				if((!definedIDs.containsKey("QUEST_ID"))
				||(!(definedIDs.get("QUEST_ID") instanceof String)))
					throw new CMException("Unable to create your quest because a quest_id was not generated");
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setScript(s,true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
					throw new CMException("Unable to create your quest.  Please consult the log.");
				//mob.tell("Generated quest '"+Q.name()+"'");
				//Log.sysOut("Generate",mob.Name()+" created quest '"+Q.name()+"'");
				Q.autostartup();
				if(!Q.running())
				{
					if(!Q.startQuest())
						throw new CMException("Unable to start the quest.  Something went wrong.  Perhaps the problem was logged?");
				}
				Q.setCopy(true);
				return Q;
			}
			catch(final CMException cme)
			{
				Log.debugOut(cme); //TODO:BZ:DELME
			}
		}
		Log.errOut(L("Failed to finish creating a quest for @x1",affected.name()));
		return null;
	}
	
	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Deity deity1M=mob.charStats().getMyDeity();
		if((deity1M==null)
		||(deity1M==mob.baseCharStats().getMyDeity()))
		{
			mob.tell(L("You must have false faith in a deity other than your own to do this."));
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell(L("You must specify a deity to start a feud with @x1 with, or STOP to stop the feud.",deity1M.Name()));
			return false;
		}

		String deityName = CMParms.combine(commands,0);
		if(deityName.equalsIgnoreCase("stop"))
		{
			final Ability dA=mob.fetchEffect(ID());
			if(dA!=null)
				dA.unInvoke();
			return false;
		}
		final Deity deity2M=CMLib.map().getDeity(deityName);
		if(deity2M==null)
		{
			mob.tell(L("You don't know of a deity called @x1.",deityName));
			return false;
		}
		
		if(deity2M==deity1M)
		{
			mob.tell(L("You can't get a deity to feud against itself."));
			return false;
		}
		
		final List<Area> deity1Areas = findDeityAreas(deity1M);
		if(deity1Areas.size()==0)
		{
			mob.tell(L("There are no appropriate places sacred to @x1 to blame.",deity1M.Name()));
			return false;
		}
		final List<Area> deity2Areas = findDeityAreas(deity2M);
		if(deity1Areas.size()==0)
		{
			mob.tell(L("There are no appropriate places sacred to @x1 to sabotage.",deity2M.Name()));
			return false;
		}
		deity1Areas.removeAll(deity2Areas);
		if(deity1Areas.size()==0)
		{
			mob.tell(L("There are no appropriate places sacred to @x1 alone in which to cause a feud.",deity1M.Name()));
			return false;
		}
		// sort the areas by close-ness to the players level
		final Comparator<Area> levelComparator = new Comparator<Area>()
		{
			final MOB M=mob;
			@Override
			public int compare(Area o1, Area o2)
			{
				int diff1 = Math.abs(M.phyStats().level() - o1.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]); 
				int diff2 = Math.abs(M.phyStats().level() - o2.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]); 
				return Integer.valueOf(diff1).compareTo(Integer.valueOf(diff2));
			}
		};
		
		Collections.sort(deity1Areas, levelComparator);
		Collections.sort(deity2Areas, levelComparator);
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// generate quest of damage to deity2 by deity1 (me-fake).
			
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> begin(s) plotting a feud between @x1 and @x2.",deity1M.Name(),deity2M.Name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Area deity1Area=deity1Areas.get(0);
				final Area deity2Area=deity2Areas.get(0);
				MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
				obj.putString("deity1", deity1M.Name());
				obj.putString("deity2", deity2M.Name());
				obj.putString("area1", deity1Area.Name());
				obj.putString("area2", deity2Area.Name());
				final String[] templateChoices;
				final Map<String,Object> definedIDs = new Hashtable<String,Object>();
				definedIDs.put("TARGETAREA_NAME", deity2Area.Name());
				if(deity2M.getAreaPiety(deity2Area.Name())>5)
				{
					templateChoices=new String[] {"auto_capture1", "auto_collect2", "auto_delivery1", "auto_killer1"};
					definedIDs.put("NUM_TARGETS", ""+(int)Math.round(CMath.mul((double)deity2M.getAreaPiety(deity2Area.Name()),0.25)));
					
					// choose from quest set 1 related to doing 25% of the pious
				}
				else
				{
					// choose from the quest sets related just to random mobs in the area
				}
				Thief_InciteDivineFeud dA=(Thief_InciteDivineFeud)beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE/10);
				if(dA!=null)
				{
					dA.makeLongLasting();
					dA.setMiscText(obj.toString());
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to plot a feud, but is drawing a blank."));

		return success;
	}
}
