package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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
		return 0;
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

	protected int getTicksBetweenCasts()
	{
		return (int)CMProps.getTicksPerDay();
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
		for(final Enumeration<Places> p=CMLib.map().holyPlaces(D.Name());p.hasMoreElements();)
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
				final int piety = A.getPiety(D.Name());
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

	protected Quest quest1 = null;
	protected Quest quest2 = null;
	protected MiniJSON.JSONObject data=null;

	@Override
	public void unInvoke()
	{
		if((quest1 != null)
		&&(quest1.running()
			||(CMLib.quests().fetchQuest(quest1.name())==quest1)))
		{
			quest1.stopQuest();
			quest1.enterDormantState();
			CMLib.quests().delQuest(quest1);
			if(affected instanceof MOB)
				((MOB)affected).tell("You have failed to start a divine feud.");
		}
		else
		if((quest2 != null)
		&&(quest2.running()
			||(CMLib.quests().fetchQuest(quest2.name())==quest2)))
		{
			if((affected instanceof MOB)
			&&(quest2.wasWinner(affected.Name())))
				((MOB)affected).tell("You have successfully started a divine feud.");
			else
				((MOB)affected).tell("You have failed to start a divine feud.");
			quest2.stopQuest();
			quest2.enterDormantState();
			CMLib.quests().delQuest(quest2);
		}
		else
		if(affected instanceof MOB)
			((MOB)affected).tell("You have failed to start a divine feud.");
		super.unInvoke();
	}

	protected boolean tickUninvoke()
	{
		unInvoke();
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return tickUninvoke();
		final MOB mob=(MOB)affected;
		if(quest1 !=  null)
		{
			if(!quest1.wasWinner(mob.Name()))
			{
				if(!quest1.running())
					return tickUninvoke();
				// not won, but still running, so keep going...
				return true;
			}
			quest1.stopQuest();
			quest1.enterDormantState();
			CMLib.quests().delQuest(quest1);
			quest1=null;
			try
			{
				final Map<String,Object> definedIDs = new Hashtable<String,Object>();
				final Area deity1Area=CMLib.map().getArea(data.getCheckedString("area1"));
				if(deity1Area==null)
					return tickUninvoke();
				definedIDs.put("TARGETAREA_NAME", data.getCheckedString("area1"));
				definedIDs.put("target_level".toUpperCase(), ""+mob.phyStats().level());
				final Deity deity1M=CMLib.map().getDeity(data.getCheckedString("deity1"));
				if(deity1M==null)
					return tickUninvoke();
				final Deity deity2M=CMLib.map().getDeity(data.getCheckedString("deity2"));
				if(deity2M==null)
					return tickUninvoke();
				final String name1Code = deity1M.Name().toUpperCase().trim().replace(' ', '_');
				final Faction deity1F=CMLib.factions().getFaction("DEITY_"+name1Code);
				if(deity1F!=null)
				{
					definedIDs.put("target_faction".toUpperCase(), deity1F.factionID());
					definedIDs.put("target_faction_amt".toUpperCase(), "-1000");
				}
				else
				{
					definedIDs.put("target_faction".toUpperCase(), "");
					definedIDs.put("target_faction_amt".toUpperCase(), "0");
				}
				definedIDs.put("AGGRESSION", "YES");
				definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
				definedIDs.put("target_int".toUpperCase(), "10");
				final String template1=data.getCheckedString("template1");
				if(deity1Area.getPiety(deity1M.Name())>5)
				{
					if(template1.indexOf("capture")>0)
					{
						definedIDs.put("TEMPLATE", "auto_escort11");
						definedIDs.put("targetname".toUpperCase(), data.getCheckedString("target1name"));
						definedIDs.put("attackername".toUpperCase(), L("a highwayman"));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("To frame @x1 for the kidnappings, you must escort your prisoner to a @x1 stronghold in @x2. ${reason_short}.",deity1M.Name(),deity1Area.Name()));
					}
					else
					if(template1.indexOf("collect")>0)
					{
						definedIDs.put("TEMPLATE", "auto_delivery1");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity1M.Name()));
						definedIDs.put("DELIVEREE_MASK","-HOME \"+"+deity1Area.Name()+"\" -DEITY \"+"+deity1M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("To frame @x1 for the thefts, you must now deliver the incriminating items to followers of @x1 in @x2. ${reason_short}.",deity1M.Name(),deity1Area.Name()));

						definedIDs.put("itemname".toUpperCase(), data.getCheckedString("item1name"));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "GOLD");
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
					}
					else
					if(template1.indexOf("delivery")>0)
					{
						definedIDs.put("TEMPLATE", "auto_protect1");
						definedIDs.put("targetname".toUpperCase(), L("a maker of @x1 holy symbols",deity1M.Name()));
						definedIDs.put("attackername".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Now defend your act by defending the maker of such symbols from outraged followers of @x1 in @x2. ${reason_short}.",deity2M.Name(),deity1Area.Name()));
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
					}
					else
					if(template1.indexOf("killer")>0)
					{
						definedIDs.put("TEMPLATE", "auto_delivery4");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity1M.Name()));
						definedIDs.put("DELIVEREE_MASK","-HOME \"+"+deity1Area.Name()+"\" -DEITY \"+"+deity1M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Now complete your framing of @x1 by seeming to delivery your previous victim`s money to  @x1`s followers in @x2. ${reason_short}.",deity1M.Name(),deity1Area.Name()));
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
					}
					else
						return tickUninvoke();
					definedIDs.put("NUM_TARGETS", ""+(int)Math.round(CMath.mul((double)deity1Area.getPiety(deity1M.Name()),0.25)));
				}
				else
				{
					if(template1.indexOf("capture")>0)
					{
						definedIDs.put("TEMPLATE", "auto_escort11");
						definedIDs.put("targetname".toUpperCase(), data.getCheckedString("target1name"));
						definedIDs.put("attackername".toUpperCase(), L("a highwayman"));
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("To frame @x1 for the kidnappings, you must escort your prisoner to a @x1 stronghold. ${reason_short}.",deity1M.Name()));
					}
					else
					if(template1.indexOf("collect")>0)
					{
						definedIDs.put("TEMPLATE", "auto_delivery3");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity1M.Name()));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your final step to creating the feud is to deliver the stolen items to @x1`s followers. ${reason_short}.",deity1M.Name()));
						definedIDs.put("itemname".toUpperCase(), data.getCheckedString("item1name"));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "GOLD");
						definedIDs.put("MULTI_TARGET", "YES");
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
					}
					else
					if(template1.indexOf("delivery")>0)
					{
						definedIDs.put("TEMPLATE", "auto_delivery3");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity1M.Name()));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your final step to creating the feud is to stage the same incriminating items with @x1`s followers. ${reason_short}.",deity1M.Name()));
						definedIDs.put("itemname".toUpperCase(), L("a symbol of @x1",deity1M.Name()));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "GOLD");
						definedIDs.put("MULTI_TARGET", "YES");
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
					}
					else
					if(template1.indexOf("killer")>0)
					{
						definedIDs.put("TEMPLATE", "auto_delivery3");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity1M.Name()));
						definedIDs.put("itemname".toUpperCase(), L("the head of a follower of @x1",deity2M.Name()));
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your final step to creating the feud is to scatter the heads of @x1`s followers around followers of @x2. ${reason_short}.",deity2M.Name(),deity1M.Name()));
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
					}
					else
						return tickUninvoke();
				}
				final Quest q2=deviseAndStartQuest(mob, definedIDs);
				if(q2 == null)
					return tickUninvoke();
				this.quest2=q2;
			}
			catch(final MiniJSON.MJSONException x)
			{
				Log.errOut(x);
				return tickUninvoke();
			}
		}
		else
		if(quest2 != null)
		{
			try
			{
				if(!quest2.wasWinner(mob.Name()))
				{
					if(!quest2.running())
						return tickUninvoke();
					// not won, but still running, so keep going...
					return true;
				}
				quest2.stopQuest();
				quest2.enterDormantState();
				CMLib.quests().delQuest(quest2);
				final int playerXP=(int)Math.round(CMath.mul((double)mob.getExpNextLevel(),0.20+(0.03 * super.getXLEVELLevel(mob))));
				CMLib.leveler().postExperience(mob, null, "", playerXP, false);
				final int deityFactionChange=100+(10*super.getXLEVELLevel(mob));
				final Deity deity1M=CMLib.map().getDeity(data.getCheckedString("deity1"));
				if(deity1M==null)
					return tickUninvoke();
				final Deity deity2M=CMLib.map().getDeity(data.getCheckedString("deity2"));
				if(deity2M==null)
					return tickUninvoke();
				final String name1Code = deity1M.Name().toUpperCase().trim().replace(' ', '_');
				final Faction deity1F=CMLib.factions().getFaction("DEITY_"+name1Code);
				if(deity1F!=null)
				{
					CMLib.factions().postFactionChange(deity2M, this, deity1F.factionID(), -deityFactionChange);
					if(deity2M.isSavable()
					&& (deity2M.getStartRoom()!=null)
					&& (deity2M.getStartRoom().roomID().length()>0)
					&& (deity2M.databaseID().length()>0)
					&& (!CMath.bset(deity2M.getStartRoom().getArea().flags(), Area.FLAG_INSTANCE_CHILD)))
						CMLib.database().DBUpdateMOB(deity2M.getStartRoom().roomID(), deity2M);
				}
				mob.tell("You have successfully started a divine feud.");
				final MOB invokerM=invoker();
				if(invokerM!=null)
				{
					final Thief_InciteDivineFeud realA=(Thief_InciteDivineFeud)invokerM.fetchAbility(ID());
					if(realA!=null)
						realA.setTimeOfNextCast(invokerM);
				}
				mob.delEffect(this);
				this.setAffectedOne(null);
				return tickUninvoke();
			}
			catch(final MiniJSON.MJSONException x)
			{
				Log.errOut(x);
				return tickUninvoke();
			}
		}
		else
			return tickUninvoke();
		return true;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText.length()>0)
		&&(newMiscText.startsWith("{")))
		{
			try
			{
				data=new MiniJSON().parseObject(newMiscText);
				quest2=null;
				quest1=CMLib.quests().fetchQuest(data.getCheckedString("quest1"));
			}
			catch (final MJSONException e)
			{
				Log.errOut(e);
			}
		}
	}

	public Quest deviseAndStartQuest(final MOB affected, final Map<String,Object> definedIDs)
	{
		final Map<String,Object> origDefined=new XHashtable<String,Object>(definedIDs);
		int maxAttempts=5;
		while((--maxAttempts)>=0)
		{
			try
			{
				definedIDs.clear();
				definedIDs.putAll(origDefined);
				final StringBuffer xml = Resources.getFileResource("randareas/example.xml", true);
				if((xml==null)||(xml.length()==0))
				{
					Log.errOut("Unable to generate a quest for "+affected.name()+" because file not found: randareas/example.xml");
					return null;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				if(!definedIDs.containsKey("QUEST_CRITERIA"))
					definedIDs.put("QUEST_CRITERIA", "-NAME \"+"+affected.Name()+"\" -NPC");
				definedIDs.put("DURATION", ""+CMProps.getTicksPerHour());
				definedIDs.put("EXPIRATION", ""+CMProps.getTicksPerHour());
				final Map<String,Object> preDefined=new XHashtable<String,Object>(definedIDs);
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
				final Quest badQ=CMLib.quests().fetchQuest(Q.name());
				if(badQ!=null)
					throw new CMException("Unable to create your quest.  One of that name already exists!");
				Log.sysOut("Generate",affected.Name()+" created quest '"+Q.name()+"' via "+ID());
				CMLib.quests().addQuest(Q);
				if(!Q.running())
				{
					if(!Q.startQuest())
					{
						CMLib.quests().delQuest(Q);
						throw new CMException("Unable to start the quest.  Something went wrong.  Perhaps the problem was logged?");
					}
				}
				Q.setCopy(true);
				return Q;
			}
			catch(final CMException cme)
			{
				if(Log.debugChannelOn() && CMSecurity.isDebugging(DbgFlag.MUDPERCOLATOR))
					Log.debugOut(cme);
				else
					Log.errOut(cme.getMessage());
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

		final String deityName = CMParms.combine(commands,0);
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
		if(deity2Areas.size()==0)
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
			public int compare(final Area o1, final Area o2)
			{
				final int diff1 = Math.abs(M.phyStats().level() - o1.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]);
				final int diff2 = Math.abs(M.phyStats().level() - o2.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]);
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
				final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
				obj.putString("deity1", deity1M.Name());
				obj.putString("deity2", deity2M.Name());
				obj.putString("area1", deity1Area.Name());
				obj.putString("area2", deity2Area.Name());
				final Map<String,Object> definedIDs = new Hashtable<String,Object>();
				definedIDs.put("TARGETAREA_NAME", deity2Area.Name());
				definedIDs.put("target_level".toUpperCase(), ""+mob.phyStats().level());
				final String name2Code = deity2M.Name().toUpperCase().trim().replace(' ', '_');
				final Faction deity2F=CMLib.factions().getFaction("DEITY_"+name2Code);
				if(deity2F!=null)
				{
					definedIDs.put("target_faction".toUpperCase(), deity2F.factionID());
					definedIDs.put("target_faction_amt".toUpperCase(), "1000");
				}
				else
				{
					definedIDs.put("target_faction".toUpperCase(), "");
					definedIDs.put("target_faction_amt".toUpperCase(), "0");
				}
				definedIDs.put("AGGRESSION", "YES");
				definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
				definedIDs.put("target_int".toUpperCase(), "10");
				if(deity2Area.getPiety(deity2M.Name())>5)
				{
					switch(CMLib.dice().roll(1, 4, 0))
					{
					case 1:
						definedIDs.put("TEMPLATE", "auto_capture1");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("CAPTURABLES_MASK","-HOME \"+"+deity2Area.Name()+"\" -DEITY \"+"+deity2M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to kidnap a few of @x1`s followers in @x2. ${reason_short}.",deity2M.Name(),deity2Area.Name()));
						break;
					case 2:
						definedIDs.put("TEMPLATE", "auto_collect1");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("HOLDERS_MASK","-HOME \"+"+deity2Area.Name()+"\" -DEITY \"+"+deity2M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to steal some special gifts from @x1 to @x2 followers in @x3. ${reason_short}.",deity2M.Name(),deity2M.charStats().hisher(),deity2Area.Name()));
						break;
					case 3:
						definedIDs.put("TEMPLATE", "auto_delivery1");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("DELIVEREE_MASK","-HOME \"+"+deity2Area.Name()+"\" -DEITY \"+"+deity2M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to deliver incriminating items of @x1 to @x2`s followers in @x3. ${reason_short}.",deity1M.Name(),deity2M.Name(),deity2Area.Name()));

						definedIDs.put("itemname".toUpperCase(), L("a symbol of @x1",deity1M.Name()));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "GOLD");
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
						break;
					case 4:
						definedIDs.put("TEMPLATE", "auto_killer1");
						definedIDs.put("target_name".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("KILLABLES_MASK","-HOME \"+"+deity2Area.Name()+"\" -DEITY \"+"+deity2M.Name()+"\"");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to kill off a few of @x1`s followers in @x2. ${reason_short}.",deity2M.Name(),deity2Area.Name()));
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
						break;
					}
					definedIDs.put("NUM_TARGETS", ""+(int)Math.round(CMath.mul((double)deity2Area.getPiety(deity2M.Name()),0.25)));
				}
				else
				{
					switch(CMLib.dice().roll(1, 4, 0))
					{
					case 1:
						definedIDs.put("TEMPLATE", "auto_capture2");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to kidnap a few of @x1`s followers in @x2. ${reason_short}.",deity2M.Name(),deity2Area.Name()));
						break;
					case 2:
						definedIDs.put("TEMPLATE", "auto_collect3");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to steal some special gifts from @x1 to @x2 followers in @x3. ${reason_short}.",deity2M.Name(),deity2M.charStats().hisher(),deity2Area.Name()));
						break;
					case 3:
						definedIDs.put("TEMPLATE", "auto_delivery3");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to deliver incriminating items of @x1 to @x2`s followers in @x3. ${reason_short}.",deity1M.Name(),deity2M.Name(),deity2Area.Name()));
						definedIDs.put("itemname".toUpperCase(), L("a symbol of @x1",deity1M.Name()));
						definedIDs.put("item_level".toUpperCase(), "1");
						definedIDs.put("item_material".toUpperCase(), "GOLD");
						if(definedIDs.containsKey("target_faction_amt".toUpperCase())
						&&(!definedIDs.get("target_faction_amt".toUpperCase()).equals("0")))
							definedIDs.put("target_faction_amt".toUpperCase(),""+(-CMath.s_int(definedIDs.get("target_faction_amt".toUpperCase()).toString())));
						break;
					case 4:
						definedIDs.put("TEMPLATE", "auto_killer2");
						definedIDs.put("targetname".toUpperCase(), L("a follower of @x1",deity2M.Name()));
						definedIDs.put("MULTI_TARGET", "YES");
						definedIDs.put("quest_instructionstring".toUpperCase(),
								L("Your first step to creating the feud is to kill off a few of @x1`s followers in @x2. ${reason_short}.",deity2M.Name(),deity2Area.Name()));
						definedIDs.put("AGGRESSION", "NO");
						definedIDs.put("target_is_aggressive".toUpperCase(), "NO");
						break;
					}
				}
				obj.putString("template1", definedIDs.get("TEMPLATE").toString());
				final Quest q1=deviseAndStartQuest(mob, definedIDs);
				if(q1 == null)
				{
					mob.tell(L("<S-NAME> attempt(s) to plot a feud between @x1 and @x2, but become(s) exhausted before figuring out a plot.",deity1M.Name(),deity2M.Name()));
					return false;
				}
				obj.putString("quest1", q1.name());
				if(definedIDs.containsKey("target_item_name".toUpperCase()))
					obj.putString("item1name", definedIDs.get("target_item_name".toUpperCase()).toString());
				obj.putString("target1name", definedIDs.get("target_name".toUpperCase()).toString());
				final Thief_InciteDivineFeud dA=(Thief_InciteDivineFeud)beneficialAffect(mob,mob,asLevel,(int)(CMProps.getTicksPerHour()*2));
				if(dA!=null)
					dA.setMiscText(obj.toString());
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to plot a feud, but is drawing a blank."));

		return success;
	}
}
