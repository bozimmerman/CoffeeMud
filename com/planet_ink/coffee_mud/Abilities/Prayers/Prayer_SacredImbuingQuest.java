package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
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
public class Prayer_SacredImbuingQuest extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_SacredImbuingQuest";
	}

	private final static String	localizedName	= CMLib.lang().L("Sacred Imbuing Quest");

	@Override
	public String name()
	{
		return localizedName;
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
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Sacred Imbuing Quest)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING|Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

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
				((MOB)affected).tell("You have failed the sacred imbuing quest.");
		}
		else
		if(affected instanceof MOB)
			((MOB)affected).tell("You have failed the sacred imbuing quest.");
		super.unInvoke();
	}

	protected final static LimitedTreeSet<String> lastUsed = new LimitedTreeSet<String>(TimeManager.MILI_DAY, 1000, true);


	protected Quest quest1 = null;
	protected MiniJSON.JSONObject data=null;
	protected Item targetItem = null;
	protected Ability targetAbility = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(!(affected instanceof MOB))
			return tickUninvoke();
		final MOB mob=(MOB)affected;
		try
		{
			if(targetItem ==null)
			{
				final String itemID=data.getCheckedString("itemid");
				for(final Enumeration<Item> i=mob.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if((""+I).equals(itemID))
					{
						targetItem=I;
						break;
					}
				}
			}
			if((targetItem == null)||(!targetItem.amBeingWornProperly())||(targetItem.owner()!=mob))
			{
				if(targetItem == null)
					mob.tell(L("You're no longer wearing @x1!",data.getCheckedString("itemname")));
				else
					mob.tell(L("You're no longer wearing @x1!",targetItem.Name()));
				return tickUninvoke();
			}
			if(targetAbility==null)
				targetAbility=CMClass.getAbility(data.getCheckedString("ability"));
			if(targetAbility==null)
				return tickUninvoke();

			if(quest1 != null)
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
				mob.tell("You have successfully completed the sacred imbuing quest.");
				final MOB invokerM=invoker();
				if(invokerM!=null)
				{
					final Prayer_SacredImbuingQuest realA=(Prayer_SacredImbuingQuest)invokerM.fetchAbility(ID());
					if(realA!=null)
					{
						int experienceToLose=1000;
						experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(targetAbility.ID()));
						experienceToLose=getXPCOSTAdjustment(invokerM,experienceToLose);
						experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
						mob.tell(L("You lose @x1 experience points for the success of the sacred imbuing quest.",""+experienceToLose));
						lastUsed.add(mob.Name());
					}
				}
				this.completeImbuing(mob,targetItem,targetAbility);
				mob.delEffect(this);
				this.setAffectedOne(null);
				return tickUninvoke();
			}
			else
				return tickUninvoke();
		}
		catch(final MiniJSON.MJSONException x)
		{
			Log.errOut(x);
			return tickUninvoke();
		}
	}

	public void completeImbuing(final MOB mob, final Item targetI, final Ability imbuePrayerA)
	{
		mob.location().show(mob,targetI,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) with a sacred light!"));
		targetI.basePhyStats().setDisposition(targetI.basePhyStats().disposition()|PhyStats.IS_BONUS);
		targetI.basePhyStats().setLevel(targetI.basePhyStats().level()+(CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())/2));
		//Vector<String> V=CMParms.parseCommas(CMLib.utensils().wornList(wand.rawProperLocationBitmap()),true);
		if(targetI instanceof Armor)
		{
			final Ability A=CMClass.getAbility("Prop_WearSpellCast");
			A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
			targetI.addNonUninvokableEffect(A);
		}
		else
		if(targetI instanceof Weapon)
		{
			final Ability A=CMClass.getAbility("Prop_FightSpellCast");
			A.setMiscText("25%;MAXTICKS=12;"+imbuePrayerA.ID()+";");
			targetI.addNonUninvokableEffect(A);
		}
		else
		if((targetI instanceof Food)
		||(targetI instanceof Drink))
		{
			final Ability A=CMClass.getAbility("Prop_UseSpellCast2");
			A.setMiscText(imbuePrayerA.ID()+";");
			targetI.addNonUninvokableEffect(A);
		}
		else
		if(targetI.fitsOn(Wearable.WORN_HELD)||targetI.fitsOn(Wearable.WORN_WIELD))
		{
			final Ability A=CMClass.getAbility("Prop_WearSpellCast");
			A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
			targetI.addNonUninvokableEffect(A);
		}
		else
		{
			final Ability A=CMClass.getAbility("Prop_WearSpellCast");
			A.setMiscText("LAYERED;"+imbuePrayerA.ID()+";");
			targetI.addNonUninvokableEffect(A);
		}
		targetI.recoverPhyStats();
	}

	protected boolean tickUninvoke()
	{
		unInvoke();
		return false;
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
				quest1=CMLib.quests().fetchQuest(data.getCheckedString("quest1"));
			}
			catch (final MJSONException e)
			{
				Log.errOut(e);
			}
		}
	}

	public Quest deviseAndStartQuest(final MOB mob, final MOB targetM, final Map<String,Object> definedIDs)
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
					Log.errOut("Unable to generate a quest for "+targetM.name()+" because file not found: randareas/example.xml");
					return null;
				}
				final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
				if(!definedIDs.containsKey("QUEST_CRITERIA"))
					definedIDs.put("QUEST_CRITERIA", "-NAME \"+"+targetM.Name()+"\" -NPC");
				definedIDs.put("DURATION", ""+CMProps.getTicksPerHour());
				definedIDs.put("EXPIRATION", ""+CMProps.getTicksPerHour());
				definedIDs.put("MULTIAREA", "YES");
				final Map<String,Object> preDefined=new XHashtable<String,Object>(definedIDs);
				CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, preDefined.keySet());
				final String idName = "ALL_QUESTS";
				if((!(definedIDs.get(idName) instanceof XMLTag))
				||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("quest")))
				{
					Log.errOut(L("The quest id '@x1' has not been defined in the data file for @x2.",idName,targetM.name()));
					return null;
				}
				final XMLTag piece=(XMLTag)definedIDs.get(idName);
				try
				{
					CMLib.percolator().checkRequirements(piece, definedIDs);
				}
				catch(final CMException cme)
				{
					Log.errOut(L("Required ids for @x1 were missing: @x2: for @x3",idName,cme.getMessage(),targetM.name()));
					return null;
				}
				String s=CMLib.percolator().buildQuestScript(piece, definedIDs, mob);
				if(s.length()==0)
					throw new CMException("Failed to create any sort of quest at all! WTF!!");
				CMLib.percolator().postProcess(definedIDs);
				if((!definedIDs.containsKey("QUEST_ID"))
				||(!(definedIDs.get("QUEST_ID") instanceof String)))
					throw new CMException("Unable to create your quest because a quest_id was not generated");
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				s=CMStrings.replaceFirst(s, "set mob reselect", "set pcmob reselect");
				s=CMStrings.replaceFirst(s, "give script LOAD=", "give script PLAYEROK LOAD=");
				Q.setScript(s,true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
					throw new CMException("Unable to create your quest.  Please consult the log.");
				//mob.tell("Generated quest '"+Q.name()+"'");
				final Quest badQ=CMLib.quests().fetchQuest(Q.name());
				if(badQ!=null)
					throw new CMException("Unable to create your quest.  One of that name already exists!");
				Log.sysOut("Generate",targetM.Name()+" created quest '"+Q.name()+"' via "+ID());
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
				final CMMsg msg=CMClass.getMsg(targetM, mob.location(),null, CMMsg.MSG_ENTER, null);
				mob.location().send(targetM, msg);
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
		Log.errOut(L("Failed to finish creating a quest for @x1",targetM.name()));
		return null;
	}

	protected boolean isOkPrayer(final Ability imbuePrayerA)
	{
		if((imbuePrayerA.ID().equals("Spell_Stoneskin"))
		||(imbuePrayerA.ID().equals("Spell_MirrorImage"))
		||(CMath.bset(imbuePrayerA.flags(), FLAG_SUMMONING))
		||(imbuePrayerA.canAffect(CAN_ROOMS))
		||(imbuePrayerA.abstractQuality()==Ability.QUALITY_MALICIOUS)
		||((!imbuePrayerA.canAffect(CAN_MOBS))&&(!imbuePrayerA.canTarget(CAN_MOBS))))
			return false;
		return true;
	}

	protected int getXPCost(final Ability imbuePrayerA)
	{
		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		return experienceToLose;
	}

	protected boolean isAppropriateItem(final Physical target)
	{
		return (target instanceof Item) && (((Item)target).amBeingWornProperly());
	}

	protected boolean checkAlignment(final MOB mob, final Physical target, final boolean quiet)
	{
		return true;
	}

	protected int maxPrayerLevel()
	{
		return 15;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<3)
		{
			mob.tell(L("Imbue which prayer onto what on whom?"));
			return false;
		}

		final List<String> mobCommands=new XVector<String>(commands.get(commands.size()-1));
		final List<String> skillName=new XVector<String>(commands.get(0));
		final List<String> itemName=new XVector<String>(commands);
		itemName.remove(0);
		itemName.remove(itemName.size()-1);
		final MOB targetM=this.getTarget(mob, mobCommands, null);
		if(targetM==null)
			return false;
		if(targetM==mob)
		{
			mob.tell(L("Your deity will not allow you to send yourself on such a quest."));
			return false;
		}
		if(!targetM.isPlayer())
		{
			mob.tell(L("@x1 does not look ready for such a task.",targetM.Name()));
			return false;
		}
		if(lastUsed.contains(targetM.Name()))
		{
			mob.tell(L("@x1 must wait a bit before being ready again for such a task.",targetM.Name()));
			return false;
		}
		final Item targetI=this.getTarget(targetM, null, givenTarget, itemName, Wearable.FILTER_WORNONLY);
		if(targetI==null)
		{
			mob.tell(L("Unable to find worn item @x1 on @x2.",CMParms.combine(itemName, 0),targetM.Name()));
			return false;
		}

		final String deityName=mob.charStats().getWorshipCharID();
		if(deityName.length()==0)
		{
			mob.tell(L("You must worship a deity to begin the imbuing.",targetM.Name()));
			return false;
		}
		if(!targetM.charStats().getWorshipCharID().equals(deityName))
		{
			mob.tell(L("@x1 must worship also worship @x2 to begin the imbuing.",targetM.Name(),deityName));
			return false;
		}

		if(!this.isAppropriateItem(targetI))
		{
			mob.tell(mob,targetI,null,L("<T-NAME> can't be imbued with this prayer!"));
			return false;
		}
		if(!Prayer.checkInfusionMismatch(mob, targetI))
		{
			mob.tell(L("That repulsive thing can not be imbued."));
			return false;
		}

		final Deity.DeityWorshipper zappA=CMLib.law().getClericInfusion(targetI);
		if(zappA instanceof Ability)
		{
			if((CMath.bset(((Ability)zappA).flags(),Ability.FLAG_ZAPPER))
			&&(!CMLib.masking().maskCheck(((Ability) zappA).text(), mob, true)))
			{
				mob.tell(L("You can't seem to focus on @x1.",targetI.name(mob)));
				return false;
			}
		}

		if(!checkAlignment(mob,targetI,false))
			return false;

		final String prayerName=CMParms.combine(skillName,0).trim();
		Ability imbuePrayerA=null;
		final List<Ability> ables=new ArrayList<Ability>();
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&((!A.isSavable())||(CMLib.ableMapper().qualifiesByLevel(mob,A)))
			&&(!A.ID().equals(this.ID())))
				ables.add(A);
		}
		imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,true);
		if(imbuePrayerA==null)
			imbuePrayerA = (Ability)CMLib.english().fetchEnvironmental(ables,prayerName,false);
		if(imbuePrayerA==null)
		{
			mob.tell(L("You don't know how to imbue anything with '@x1'.",prayerName));
			return false;
		}

		if(!isOkPrayer(imbuePrayerA))
		{
			mob.tell(L("That prayer cannot be used to imbue anything."));
			return false;
		}

		if((CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID())>maxPrayerLevel())
		||(((StdAbility)imbuePrayerA).usageCost(null,true)[0]>45)
		||(CMath.bset(imbuePrayerA.flags(), Ability.FLAG_CLANMAGIC)))
		{
			mob.tell(L("That prayer is too powerful to imbue into anything."));
			return false;
		}
		if(!targetI.isGeneric())
		{
			mob.tell(L("@x1 can't be imbued at all.",targetI.name()));
			return false;
		}
		if(CMLib.flags().isEnchanted(targetI))
		{
			mob.tell(L("@x1 already appears to be magical.",targetI.name(mob)));
			return false;
		}

		int experienceToLose=1000;
		experienceToLose+=(100*CMLib.ableMapper().lowestQualifyingLevel(imbuePrayerA.ID()));
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to use this prayer."));
			return false;
		}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			setMiscText(imbuePrayerA.ID()); // important for reliquist discharge
			final CMMsg msg=CMClass.getMsg(mob,targetI,this,verbalCastCode(mob,targetI,auto),L("^S<S-NAME> @x1 while pointing at <T-NAMESELF> on @x2.^?",super.prayWord(mob),targetM.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
				final Map<String,Object> definedIDs = new Hashtable<String,Object>();
				definedIDs.put("AREA_NAME", mob.location().getArea().Name());
				definedIDs.put("target_level".toUpperCase(), ""+targetM.phyStats().level());
				definedIDs.put("AGGRESSION", "YES");
				definedIDs.put("target_is_aggressive".toUpperCase(), "YES");
				definedIDs.put("TEMPLATE", "normal");
				definedIDs.put("DEITYNAME", deityName);
				final Quest q1=deviseAndStartQuest(mob, targetM, definedIDs);
				if(q1 == null)
				{
					experienceToLose=getXPCOSTAdjustment(mob,50);
					experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
					mob.tell(L("You lose @x1 experience points for the failure.",""+experienceToLose));
					beneficialWordsFizzle(mob,targetI,L("<S-NAME> fail(s) entirely.",super.prayWord(mob),targetM.Name()));
					return false;
				}
				obj.putString("ability", imbuePrayerA.ID());
				obj.putString("template1", definedIDs.get("TEMPLATE").toString());
				obj.putString("quest1", q1.name());
				obj.putString("itemname", targetI.Name());
				obj.putString("itemid", ""+targetI);
				final Prayer_SacredImbuingQuest dA=(Prayer_SacredImbuingQuest)beneficialAffect(mob,targetM,asLevel,(int)(CMProps.getTicksPerHour()*2));
				if(dA!=null)
					dA.setMiscText(obj.toString());
			}
		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,50);
			experienceToLose=-CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			mob.tell(L("You lose @x1 experience points for the failure.",""+experienceToLose));
			beneficialWordsFizzle(mob,targetI,L("<S-NAME> @x1 while pointing at <T-NAMESELF> on @x2, and looking very frustrated.",super.prayWord(mob),targetM.Name()));
		}
		// return whether it worked
		return success;
	}
}
