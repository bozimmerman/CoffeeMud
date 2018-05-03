package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_SummonDustdevil extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonDustdevil";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Dustdevil");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Summon Dustdevil)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
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
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
					unInvoke();
				else
				{
					Vector<Item> V=new Vector<Item>();
					for(int i=0;i<mob.location().numItems();i++)
					{
						final Item I=mob.location().getItem(i);
						if((I!=null)&&(I.container()==null))
							V.addElement(I);
					}
					boolean giveUp=false;
					for(int i=0;i<V.size();i++)
					{
						final Item I=V.elementAt(i);
						if((mob.maxCarry()>=mob.phyStats().weight()+I.phyStats().weight())
						&&(mob.maxItems()>=(mob.numItems()+I.numberOfItems())))
							CMLib.commands().postGet(mob,null,I,false);
						else
							giveUp=true;
					}
					if(giveUp)
					{
						V=new Vector<Item>();
						for(int i=0;i<mob.numItems();i++)
						{
							final Item I=mob.getItem(i);
							if((I!=null)&&(I.container()==null))
								V.addElement(I);
						}
						for(int i=0;i<V.size();i++)
						{
							final CMMsg msg=CMClass.getMsg(mob,invoker,V.elementAt(i),CMMsg.MSG_GIVE,L("<S-NAME> whirl(s) <O-NAME> to <T-NAMESELF>."));
							if(mob.location().okMessage(mob,msg))
								mob.location().send(mob,msg);
							else
								break;
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				unInvoke();
				return false;
			}
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			{
				msg.source().tell(L("You can't fight!"));
				msg.source().setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		if((canBeUninvoked())&&(mob!=null))
		if(mob.location()!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> dissipate(s)."));
			final Vector<Item> V=new Vector<Item>();
			for(int i=0;i<mob.numItems();i++)
				V.addElement(mob.getItem(i));
			for(int i=0;i<V.size();i++)
			{
				final Item I=V.elementAt(i);
				mob.delItem(I);
				mob.location().addItem(I,ItemPossessor.Expire.Monster_EQ);
			}
		}
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()&Room.INDOORS)>0)
					return Ability.QUALITY_INDIFFERENT;
				if(CMLib.flags().isWateryRoom(R))
					return Ability.QUALITY_INDIFFERENT;
				if(mob.isInCombat())
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if(CMLib.flags().isWateryRoom(mob.location()))
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}

		final int material=RawMaterial.RESOURCE_ASH;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) and summon(s) help from the air.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, material);
				if(target!=null)
				{
					if(target.isInCombat())
						target.makePeace(true);
					beneficialAffect(mob,target,asLevel,0);
					CMLib.commands().postFollow(target,mob,true);
					if(target.amFollowing()!=mob)
						mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s), but nothing happens."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int material)
	{
		final MOB newMOB=CMClass.getMOB("GenMOB");
		final int level=3;
		newMOB.basePhyStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("AirElemental"));
		final String name="a dustdevil";
		newMOB.setName(name);
		newMOB.setDisplayText(L("@x1 whirls around here",name));
		newMOB.setDescription("");
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.basePhyStats().setAbility(25);
		newMOB.basePhyStats().setWeight(caster.phyStats().level()*(caster.phyStats().level()+(2*getXLEVELLevel(caster))));
		newMOB.baseCharStats().setStat(CharStats.STAT_STRENGTH,caster.phyStats().level()+(2*getXLEVELLevel(caster)));
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_INVISIBLE);
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_HIDDEN);
		newMOB.setLocation(caster.location());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.basePhyStats().setDamage(1);
		newMOB.basePhyStats().setAttackAdjustment(0);
		newMOB.basePhyStats().setArmor(100);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appear(s)!"));
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
