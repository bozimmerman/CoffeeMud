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

import java.util.List;

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

public class Chant_ControlFire extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_ControlFire";
	}

	private final static String localizedName = CMLib.lang().L("Control Fire");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
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
	public int maxRange()
	{
		return adjustedMaxInvokerRange(5);
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_FIREBASED;
	}

	private Item getFireSource(MOB target)
	{
		for(int i=0;i<target.numItems();i++)
		{
			final Item I=target.getItem(i);
			if((CMLib.flags().isOnFire(I))&&(I.container()==null))
				return I;
		}

		for(int i=0;i<target.location().numItems();i++)
		{
			final Item I=target.location().getItem(i);
			if((CMLib.flags().isOnFire(I))&&(I.container()==null))
				return I;
		}
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(getFireSource((MOB)target)==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Item fireSource=getFireSource(target);

		if((success)&&(fireSource!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"Suddenly "+fireSource.name()+" flares up and attacks <T-HIM-HER>!^?":"^S<S-NAME> chant(s) to <T-NAMESELF>.  Suddenly "+fireSource.name()+" flares up and attacks <T-HIM-HER>!^?")+CMLib.protocol().msp("fireball.wav",40));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_FIRE,null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				final int numDice = (int)Math.round(CMath.div(adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)),2.0))+1;
				int damage = CMLib.dice().roll(numDice, 6, 20);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The flames <DAMAGE> <T-NAME>!"));
				CMLib.utensils().extinguish(mob,fireSource,false);
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
