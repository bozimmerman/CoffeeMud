package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Thief_Flay extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Flay";
	}

	private final static String localizedName = CMLib.lang().L("Flay");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_LEGAL;
	}

	private static final String[] triggerStrings =I(new String[] {"FLAY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;
		if((affected instanceof MOB)
		&&msg.amISource((MOB)affected)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR)
		&&(msg.target() instanceof Item)
		&&((CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_BACK))
		||(CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_TORSO))))
		{
			msg.source().tell(L("The flayed marks on your back make wearing that too painful."));
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isSitting(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
				return Ability.QUALITY_INDIFFERENT;
			if(target != null)
			{
				if((!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(CMLib.flags().isSitting(mob))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}
		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;
		if((!auto)&&(!CMLib.flags().isBoundOrHeld(target))&&(!CMLib.flags().isSleeping(target)))
		{
			mob.tell(L("@x1 must be prone or bound first.",target.name(mob)));
			return false;
		}
		for(int i=0;i<target.numItems();i++)
		{
			final Item I=target.getItem(i);
			if((I!=null)&&((I.amWearingAt(Wearable.WORN_BACK))||(I.amWearingAt(Wearable.WORN_TORSO))))
			{
				mob.tell(L("@x1 must be remove items worn on the torso or back first.",target.name(mob)));
				return false;
			}
		}

		final Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell(L("You cannot flay without a weapon!"));
				return false;
			}
			ww=(Weapon)w;
			if(ww.weaponClassification()!=Weapon.CLASS_FLAILED)
			{
				mob.tell(L("You cannot flay with a @x1, you need a flailing weapon!",ww.name()));
				return false;
			}
			if(w.material()!=RawMaterial.RESOURCE_LEATHER)
			{
				mob.tell(L("You cannot flay with a @x1, you need a weapon made of leather!",ww.name()));
				return false;
			}
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
			{
				mob.tell(L("You are too far away to try that!"));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,L("<S-NAME> flay(s) the bare back of <T-NAMESELF>!"));
		final boolean makePeace = CMLib.flags().isBound(target) && (mob.getVictim() == null) && (target.getVictim() == null);

		if(success)
		{
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				maliciousAffect(mob,target,asLevel,0,-1);
				if(target.fetchEffect(ID())!=null)
				{
					final Ability A=CMClass.getAbility("Bleeding");
					if(A!=null)
						A.invoke(mob,target,true,asLevel);
					if(makePeace)
					{
						mob.makePeace(true);
						target.makePeace(true);
					}
				}
			}
		}
		else
			maliciousFizzle(mob,target,L("<S-NAME> attempt(s) flay <T-NAMESELF>, but fail(s)."));
		return success;
	}
}

