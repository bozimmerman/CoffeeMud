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
   Copyright 2022-2023 Bo Zimmerman

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
public class Fighter_Bulldog extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Bulldog";
	}

	private final static String	localizedName	= CMLib.lang().L("Bulldog");

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

	private static final String[]	triggerStrings	= I(new String[] { "BULLDOG" });

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
			if(getGrappleA((MOB)target)==null)
				return Ability.QUALITY_INDIFFERENT;
			if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
				return Ability.QUALITY_INDIFFERENT;
			if((!CMLib.flags().isStanding(mob))&&(mob!=target))
				return Ability.QUALITY_INDIFFERENT;
			if((target.fetchEffect("Fighter_ChokeHold")==null)
			&&(target.fetchEffect("Fighter_Headlock")==null))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((CMLib.flags().isSitting(target)||CMLib.flags().isSleeping(target)))
		{
			mob.tell(L("@x1 is already on the floor!",target.name(mob)));
			return false;
		}

		if((!auto)&&(!CMLib.flags().isStanding(mob))&&(mob!=target))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		if(target.riding()!=mob.riding())
		{
			if(target.riding()!=null)
				mob.tell(L("You can't do that to someone @x1 @x2!",target.riding().stateString(target),target.riding().name()));
			else
				mob.tell(L("You can't do that to someone while @x1 @x2!",mob.riding().stateString(mob),mob.riding().name()));
			return false;
		}

		Ability suppA=target.fetchEffect("Fighter_ChokeHold");
		if(suppA == null)
			suppA=target.fetchEffect("Fighter_Headlock");
		if((suppA==null)||(suppA.invoker()==target))
		{
			mob.tell(L("@x1 need(s) to be in a head hold grapple for this to work!",target.name(mob)));
			return false;
		}
		final Ability suppiA=(suppA.invoker()==null)?null:suppA.invoker().fetchEffect(suppA.ID());

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final boolean oldSav1=suppA.isSavable();
			final boolean oldUnv1=suppA.canBeUninvoked();
			final boolean oldSav2=(suppiA==null)?true:suppiA.isSavable();
			final boolean oldUnv2=(suppiA==null)?true:suppiA.canBeUninvoked();
			try
			{
				suppA.makeNonUninvokable();
				if(suppiA!=null)
					suppiA.makeNonUninvokable();
				final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),
						auto?L("<T-NAME> flops(s) down!"):L("^F^<FIGHT^><S-NAME> pull(s) <T-NAME> down!^</FIGHT^>^?"));
				CMLib.color().fixSourceFightColor(msg);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()>0)
						return maliciousFizzle(mob,target,L("<T-NAME> dodge(s) <S-YOUPOSS> bulldogging move."));
					final Item I=CMClass.getWeapon("StdWeapon");
					I.setName(L("a bulldog slam"));
					((Weapon)I).setWeaponDamageType(Weapon.TYPE_BASHING);
					((Weapon)I).setWeaponClassification(Weapon.CLASS_NATURAL);
					I.setRawWornCode(Wearable.WORN_WIELD);
					if (I.subjectToWearAndTear())
						I.setUsesRemaining(100);
					CMLib.combat().postWeaponAttackResult(mob, target, I, super.getXLEVELLevel(mob), true);
					I.destroy();
					mob.location().show(mob, target, CMMsg.MSG_OK_ACTION,L("<T-NAME> hit(s) the ground!"));
					target.phyStats().setDisposition(target.phyStats().disposition()|PhyStats.IS_SITTING);
					target.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_SITTING);
					mob.phyStats().setDisposition(target.phyStats().disposition()|PhyStats.IS_SITTING);
					mob.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_SITTING);
				}
			}
			finally
			{
				suppA.setStat("CANUNINVOKE", ""+oldUnv1);
				suppA.setSavable(oldSav1);
				if(suppiA!=null)
				{
					suppiA.setStat("CANUNINVOKE", ""+oldUnv2);
					suppiA.setSavable(oldSav2);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to leap on <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
