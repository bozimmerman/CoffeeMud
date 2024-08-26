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
public class Skill_Trample extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Trample";
	}

	private final static String localizedName = CMLib.lang().L("Trample");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings =I(new String[] {"TRAMPLE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FITNESS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected boolean weightCheck(final MOB mob, final MOB target)
	{
		if(mob.phyStats().weight()/2 > target.phyStats().weight())
			return true;
		return false;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	protected int		enhancement	= 0;
	protected boolean	doneTicking	= false;

	@Override
	public int abilityCode()
	{
		return enhancement;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		enhancement = newCode;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		else
		if(msg.amISource(mob)&&(msg.sourceMinor()==CMMsg.TYP_STAND))
			return false;
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			doneTicking=true;
		super.unInvoke();
		if(canBeUninvoked() && (mob!=null))
		{
			final Room R=mob.location();
			if((R!=null)&&(!mob.amDead()))
			{
				final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> regain(s) <S-HIS-HER> feet."));
				if(R.okMessage(mob,msg)&&(!mob.amDead()))
				{
					R.send(mob,msg);
					CMLib.commands().postStand(mob,true, false);
				}
			}
			else
				mob.tell(L("You regain your feet."));
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)
		&&(target instanceof MOB))
		{
			if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
			if(!weightCheck(mob,(MOB)target))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat() && mob.rangeToTarget()==0)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!auto)
		&&(mob.isInCombat())
		&&(mob.rangeToTarget()<=0))
		{
			mob.tell(L("You are too close to do that."));
			return false;
		}

		if((!auto)
		&&(!weightCheck(mob,target)))
		{
			mob.tell(L("@x1 is too large for you to trample.",target.name(mob)));
			return false;
		}

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			mob.tell(L("@x1 must stand up first!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			final int oldRange = mob.isInCombat()?mob.rangeToTarget():-1;
			str=auto?L("<T-NAME> is trampled!"):L("^F^<FIGHT^><S-NAME> trample(s) <T-NAMESELF>!^</FIGHT^>^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),str);
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((oldRange>=0)&&(mob.rangeToTarget() > oldRange))
					mob.setRangeToTarget(oldRange);
				if(msg.value()>0)
					return maliciousFizzle(mob,target,L("<T-NAME> dodge(s) <S-YOUPOSS> trample attempt."));
				int damage = (int)Math.round(CMath.mul(CMLib.dice().roll(1, 6, abilityCode()),CMath.div(mob.phyStats().weight(),target.phyStats().weight())));
				if(!CMLib.flags().isStanding(target))
					damage *= 2;
				CMLib.combat().postDamage(mob,target,this,damage,
						CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,
						Weapon.TYPE_BASHING,L("^F^<FIGHT^><S-YOUPOSS> trample <DAMAGES> <T-NAME>!^</FIGHT^>^?@x1",CMLib.protocol().msp("bashed1.wav",30)));
				if(CMLib.flags().isStanding(target))
				{
					maliciousAffect(mob,target,asLevel,2,-1);
					target.tell(L("You go down!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to  trample <T-NAMESELF>, but miss(es)."));

		return success;
	}

}
