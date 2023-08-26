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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_SetPolearm extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_SetPolearm";
	}

	private final static String	localizedName	= CMLib.lang().L("Set Polearm");

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
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_WEAPON_USE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SETPOLEARM" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected Item polearmI = null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if(msg.source()==affected)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ADVANCE:
			case CMMsg.TYP_RETREAT:
			{
				if(getPolearm(msg.source())!=null)
				{
					final Room R = msg.source().location();
					R.show(msg.source(), null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> stand(s) firm."));
					return false;
				}
				break;
			}
			case CMMsg.TYP_DAMAGE:
				if((msg.target() instanceof MOB)
				&&(((MOB)msg.target()).riding() instanceof MOB)
				&&(msg.target()!=msg.source())
				&&(msg.value()>0))
				{
					if(getPolearm(msg.source())!=null)
						msg.setValue((int)Math.round(CMath.mul(msg.value(),1.5)));
				}
				break;
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_ENTER:
				unInvoke();
				break;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source()!=affected)
			&&(msg.source().riding() !=null)
			&&(msg.targetMinor()==CMMsg.TYP_ADVANCE)
			&&(msg.source().getVictim()==affected))
			{
				final Item I = this.getPolearm((MOB)affected);
				if(I == null)
					unInvoke();
				else
				{
					if((msg.source().rangeToTarget()>0)
					&&(msg.source().rangeToTarget() == I.maxRange()))
					{
						final Ability A = CMClass.getAbility("Fighter_DismountingBlow");
						if(A!=null)
						{
							A.setProficiency(proficiency());
							A.invoke((MOB)affected, msg.source(), false, 0);
						}
					}
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked()
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			final Room R = (mob==null)?null:mob.location();
			if(R != null)
			{
				if(polearmI != null)
					R.show(mob, polearmI, CMMsg.MSG_OK_VISUAL, L("<S-NAME> un-set(s) <T-NAME>."));
				else
					R.show(mob, polearmI, CMMsg.MSG_OK_VISUAL, L("<S-NAME> un-set(s) <S-HIM-HERSELF>."));
			}
		}
		super.unInvoke();

	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB)
		&&(super.canBeUninvoked()))
		{
			final MOB mob=(MOB)affected;
			if((getPolearm(mob)==null)
			||(mob.riding()!=null))
			{
				unInvoke();
				return false;
			}
		}
		return super.tick(ticking, tickID);
	}

	protected Weapon getPolearm(final MOB mob)
	{
		final Item I = mob.fetchWieldedItem();
		if((I instanceof Weapon)
		&&(((Weapon)I).weaponClassification()==Weapon.CLASS_POLEARM)
		&&(((Weapon)I).maxRange()>0))
			return (Weapon)I;
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		final Weapon polearmI = getPolearm(mob);
		if((polearmI == null)&&(!auto))
		{
			mob.tell(L("You aren't wielding a polearm!"));
			return false;
		}
		final Rideable ride = mob.riding();
		if((ride != null)&&(!auto))
		{
			mob.tell(L("Not while you are "+ride.rideString(mob)+"@x1",ride.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,polearmI,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("^F<S-NAME> set(s) <T-NAME>!^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Fighter_SetPolearm A = (Fighter_SetPolearm)beneficialAffect(mob,mob,asLevel,0);
				if(A != null)
					A.polearmI = polearmI;
			}
		}
		else
			beneficialVisualFizzle(mob,polearmI,L("<S-NAME> attempt(s) to set <S-HIS-HER> @x1, but goof(s) it up."));
		return success;
	}
}
