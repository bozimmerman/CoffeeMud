package com.planet_ink.coffee_mud.Abilities.Fighter;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class Fighter_Jousting extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Jousting";
	}

	private final static String localizedName = CMLib.lang().L("Jousting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"JOUST","JOUSTING"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int minRange()
	{
		return 1;
	}

	@Override
	public int maxRange()
	{
		return 99;
	}

	public volatile boolean done=false;
	public volatile int hits=0;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==msg.source().fetchWieldedItem()))
		{
			if(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
				done=true;
			else
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.value()>0))
			{
				final MOB targetM = (MOB)msg.target();
				if((targetM.riding()!=null)
				&&(hits==0)
				&&(targetM.riding().isMobileRideBasis())
				&&(CMLib.dice().rollPercentage()<25+(5*this.getXLEVELLevel(msg.source()))))
				{
					msg.addTrailerRunnable(new Runnable() {
						final MOB joustM = msg.source();
						final MOB disM = targetM;
						@Override
						public void run()
						{
							if(disM.riding() != null)
							{
								final Ability disA = CMClass.getAbility("Fighter_DismountingBlow");
								disA.setProficiency(100);
								disA.invoke(joustM, disM, true, 0);
							}
						}
					});
				}
				done=true;
				if(hits < 2)
					msg.setValue(msg.value()*(3-hits));
				hits++;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(done)
			{
				final Physical affected = this.affected;
				unInvoke();
				final Ability A = CMClass.getAbility("Fighter_Ridethrough");
				if((A!=null)
				&&(affected instanceof MOB)
				&&(((MOB)affected).getVictim()!=null))
					A.invoke((MOB)affected, new XVector<String>(), ((MOB)affected).getVictim(), true, 0);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			final Item I = mob.fetchWieldedItem();
			if((!(I instanceof Weapon))
			||(((Weapon)I).maxRange()<=0))
				return Ability.QUALITY_INDIFFERENT;
			if((mob.isInCombat())&&(mob.rangeToTarget()<=((Weapon)I).maxRange()))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isMobileMounted(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final boolean inCombat=mob.isInCombat();
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell(L("You are already in the middle of a joust!"));
			return false;
		}

		Item polearmI = null;
		final Rideable mount=mob.riding();
		if(!auto)
		{
			if(inCombat
			&&(mob.rangeToTarget()<=0)
			&&(!auto))
			{
				mob.tell(L("You can not joust while in melee!"));
				return false;
			}

			if(!CMLib.flags().isMobileMounted(mob))
			{
				mob.tell(L("You must be riding a mount to use this skill."));
				return false;
			}

			polearmI = Fighter_DismountingBlow.getPolearm(mob);
			if(polearmI == null)
			{
				mob.tell(L("You must have an appropriate melee weapon with range to do that."));
				return false;
			}
			if(inCombat
			&&(target==mob.getVictim())
			&&(mob.rangeToTarget()<=((Weapon)polearmI).maxRange()))
			{
				mob.tell(L("You must be further from your enemy to do that."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String msgStr;
			if(mount != null)
				msgStr = L("^F^<FIGHT^><S-NAME> "+mount.rideString(mob)+" hard at <T-NAMESELF> in a Joust!^?^</FIGHT^>");
			else
				msgStr = L("^F^<FIGHT^><S-NAME> joust(s) <T-NAMESELF>!^?^</FIGHT^>");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE, msgStr);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((mob.getVictim()==target)
				||(target.getVictim()==mob))
				{
					if(!inCombat)
					{
						mob.setVictim(target);
						mob.setRangeToTarget(mob.location().maxRange());
					}
					msg.setSourceMessage(null);
					msg.setTargetMessage(null);
					msg.setOthersMessage(null);
					msg.setTool(null);
					final int maxRange = polearmI != null ? ((Weapon)polearmI).maxRange() : 0;
					for(int i=mob.rangeToTarget()-1;i>=maxRange;i--)
					{
						if(mob.location().okMessage(mob, msg))
							mob.location().send(mob, msg);
					}
					if(mob.rangeToTarget()<=maxRange)
					{
						done=false;
						hits=0;
						beneficialAffect(mob,mob,asLevel,2);
						mob.recoverPhyStats();
						if(!inCombat)
							CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
						if(mob.getVictim()==null)
							mob.setVictim(null); // correct range
						if(target.getVictim()==null)
							target.setVictim(null); // correct range
					}
				}
			}
		}
		else
		if(mount != null)
			return maliciousFizzle(mob,target,L("<S-NAME> "+mount.rideString(mob)+" at <T-NAMESELF> in a Joust, but miss(es)."));
		else
			return maliciousFizzle(mob,target,L("<S-NAME> joust(s) at <T-NAMESELF> in a Joust, but miss(es)."));

		// return whether it worked
		return success;
	}
}
