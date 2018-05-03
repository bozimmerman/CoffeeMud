package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_PhantomHound extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PhantomHound";
	}

	private final static String localizedName = CMLib.lang().L("Phantom Hound");

	@Override
	public String name()
	{
		return localizedName;
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
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected MOB	victim		= null;
	protected int	pointsLeft	= 0;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(((affected==null)
			||(unInvoked)
			||(!(affected instanceof MOB)))
				&&(canBeUninvoked()))
				unInvoke();
			else
			{
				final MOB beast=(MOB)affected;
				int a=0;
				while(a<beast.numEffects()) // personal
				{
					final Ability A=beast.fetchEffect(a);
					if(A!=null)
					{
						final int n=beast.numEffects();
						if(A.ID().equals(ID()))
							a++;
						else
						{
							A.unInvoke();
							if(beast.numEffects()==n)
								a++;
						}
					}
					else
						a++;
				}
				if((!beast.isInCombat())||(beast.getVictim()!=victim))
				{
					final Room R=beast.location();
					if(R!=null)
						R.show(beast, null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> vanish(es)!"));
					if(beast.amDead())
						beast.setLocation(null);
					beast.destroy();
				}
				else
				{
					pointsLeft-=(victim.charStats().getStat(CharStats.STAT_INTELLIGENCE));
					pointsLeft-=victim.phyStats().level();
					final int pointsLost=beast.baseState().getHitPoints()-beast.curState().getHitPoints();
					if(pointsLost>0)
						pointsLeft-=pointsLost/4;
					if(pointsLeft<0)
					{
						final Room R=beast.location();
						if(R!=null)
							R.show(victim, beast,CMMsg.MSG_OK_VISUAL, L("<S-NAME> disbelieve(s) <T-NAME>, who vanishes!"));
						if(beast.amDead())
							beast.setLocation(null);
						beast.destroy();
					}
				}
			}

		}
		return super.tick(ticking,tickID);
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
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			final int damageType=Weapon.TYPE_NATURAL;
			if(msg.sourceMessage()!=null)
				msg.setSourceMessage(CMLib.combat().replaceDamageTag(msg.sourceMessage(), msg.value(), damageType, CMMsg.View.SOURCE));
			if(msg.targetMessage()!=null)
				msg.setTargetMessage(CMLib.combat().replaceDamageTag(msg.targetMessage(), msg.value(), damageType, CMMsg.View.TARGET));
			if(msg.othersMessage()!=null)
				msg.setOthersMessage(CMLib.combat().replaceDamageTag(msg.othersMessage(), msg.value(), damageType, CMMsg.View.OTHERS));
			msg.setValue(0);
		}
		return super.okMessage(myHost,msg);

	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to cast this spell!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> invoke(s) a ferocious phantom assistant.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB newMOB=CMClass.getMOB("GenMOB");
				newMOB.setName(L("the phantom hound"));
				newMOB.setDisplayText(L("the phantom hound is here"));
				newMOB.setStartRoom(null);
				newMOB.setDescription(L("This is the most ferocious beast you have ever seen."));
				newMOB.basePhyStats().setAttackAdjustment(mob.phyStats().attackAdjustment()+100);
				newMOB.basePhyStats().setArmor(mob.basePhyStats().armor()-20);
				newMOB.basePhyStats().setDamage(75);
				newMOB.basePhyStats().setLevel(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				newMOB.basePhyStats().setSensesMask(PhyStats.CAN_SEE_DARK|PhyStats.CAN_SEE_HIDDEN|PhyStats.CAN_SEE_INVISIBLE|PhyStats.CAN_SEE_SNEAKERS);
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
				newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
				for(final int i : CharStats.CODES.SAVING_THROWS())
					newMOB.baseCharStats().setStat(i,200);
				newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
				newMOB.basePhyStats().setAbility(100);
				newMOB.baseState().setMana(100);
				newMOB.baseState().setMovement(1000);
				newMOB.recoverPhyStats();
				newMOB.recoverCharStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.text();
				newMOB.bringToLife(mob.location(),true);
				CMLib.beanCounter().clearZeroMoney(newMOB,null);
				newMOB.setMoneyVariation(0);
				newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
				newMOB.setStartRoom(null);
				victim=mob.getVictim();
				if(victim!=null)
				{
					victim.setVictim(newMOB);
					newMOB.setVictim(victim);
				}
				pointsLeft=100+(5*this.adjustedLevel(mob, asLevel));
				beneficialAffect(mob,newMOB,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to invoke a spell, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
