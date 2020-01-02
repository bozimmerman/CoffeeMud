package com.planet_ink.coffee_mud.Abilities.Skills;
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_Groin extends StdSkill implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Skill_Groin";
	}

	private final static String localizedName = CMLib.lang().L("Groin");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(dazed and in pain)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"GROIN"});
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Dazed and in pain";
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_TORTURING;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS)))
		{
			if((msg.sourceMajor(CMMsg.MASK_EYES))
			||(msg.sourceMajor(CMMsg.MASK_HANDS))
			||(msg.sourceMajor(CMMsg.MASK_MOUTH))
			||(msg.sourceMajor(CMMsg.MASK_MOVE)))
			{
				if(msg.sourceMessage()!=null)
					mob.tell(L("You are way too dazed."));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition((affectableStats.disposition()|PhyStats.IS_SITTING) & ~PhyStats.IS_SLEEPING );
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> seem(s) less dazed."));
				else
					mob.tell(L("You feel less dazed."));
				CMLib.commands().postStand(mob,true, false);
			}
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect(ID())!=null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	protected volatile Reference<MOB> lastHit = new WeakReference<MOB>(null);

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away from your target to kick them!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*10;
		else
			levelDiff=0;

		// now see if it worked
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,(-levelDiff)+(-((target.charStats().getStat(CharStats.STAT_DEXTERITY)-mob.charStats().getStat(CharStats.STAT_STRENGTH)))),auto)&&(hit);

		if(lastHit.get() == target)
		{
			final Room R=mob.location();
			if(R!=null)
				R.show(target, mob, CMMsg.MSG_QUIETMOVEMENT, L("<S-NAME> push(es) <S-HIS-HER> legs together and cover(s) <S-HIS-HER> groin!"));
			success=false;
		}

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					L(auto?"<T-NAME> clutch(es) <T-HIS-HER> groin area in obvious pain!":
						"^F<S-NAME> kick(s) <T-NAMESELF> in the groin!^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int topDamage=(adjustedLevel(mob,asLevel)/3)+2;
				int damage=CMLib.dice().roll(1,topDamage,0);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				if(damage >= target.curState().getHitPoints())
					damage=target.curState().getHitPoints()-1;
				CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,
						L("^F^<FIGHT^>The crotch blow <DAMAGES> <T-NAME>!^</FIGHT^>^?@x1",CMLib.protocol().msp("bashed1.wav",30)));
				if(target.charStats().getStat(CharStats.STAT_GENDER) == 'M')
					success=maliciousAffect(mob,target,asLevel,2+super.getXLEVELLevel(mob),-1)!=null;
				this.lastHit=new WeakReference<MOB>(target);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to kick <T-NAMESELF> in the groin, but fail(s)."));

		// return whether it worked
		return success;
	}
}
