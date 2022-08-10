package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2022-2022 Bo Zimmerman

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
public class Fighter_Earbox extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Earbox";
	}

	private final static String	localizedName	= CMLib.lang().L("Earbox");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "EARBOX" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_GRAPPLING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target instanceof MOB))
		{
			final FighterGrappleSkill A=getGrappleA((MOB)target);
			if((A==null)||(A.invoker()!=mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		affectableStats.setStat(CharStats.STAT_CRIT_DAMAGE_PCT_WEAPON,
				affectableStats.getStat(CharStats.STAT_CRIT_DAMAGE_PCT_WEAPON)
				+5+(2*super.getXLEVELLevel(affectedMob)));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.riding()!=mob.riding())
		{
			if(target.riding()!=null)
				mob.tell(L("You can't do that to someone @x1 @x2!",target.riding().stateString(target),target.riding().name()));
			else
				mob.tell(L("You can't do that to someone while @x1 @x2!",mob.riding().stateString(mob),mob.riding().name()));
			return false;
		}

		Ability A=getGrappleA(target);
		if((A==null)||(A.invoker()!=mob))
		{
			mob.tell(L("You nee(s) to have @x1 in a close grapple for this to work!",target.name(mob)));
			return false;
		}

		if(mob.charStats().getBodyPart(Race.BODY_ARM)<2)
		{
			mob.tell(L("You lack the arms to do this."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
					auto?L("<T-NAME> groan(s)!"):L("^F^<FIGHT^><S-NAME> earbox(es) <T-NAME>!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()>0)
					return maliciousFizzle(mob,target,L("<T-NAME> dodge(s) <S-YOUPOSS> earbox maneuver."));

				final Item I=CMClass.getWeapon("StdWeapon");
				I.setName(L("a strike about the head"));
				((Weapon)I).setWeaponDamageType(Weapon.TYPE_BASHING);
				((Weapon)I).setWeaponClassification(Weapon.CLASS_NATURAL);
				I.setRawWornCode(Wearable.WORN_WIELD);
				if (I.subjectToWearAndTear())
					I.setUsesRemaining(100);
				try
				{
					mob.addEffect(this);
					mob.recoverCharStats();
					CMLib.combat().postWeaponAttackResult(mob, target, I, super.getXLEVELLevel(mob), true);
				}
				finally
				{
					mob.delEffect(this);
					mob.recoverCharStats();
					invoker=null;
				}
				I.destroy();
				A=CMClass.getAbility("Spell_Deafness");
				if(A != null)
					A.startTickDown(mob, target, 2);
				if(CMLib.dice().roll(1, 1000, 0)==1)
				{
					A=CMClass.getAbility("Disease_Migraines");
					if((A!=null)&&(target.fetchEffect(A.ID())==null))
						A.invoke(target,target,true,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to earbox <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
