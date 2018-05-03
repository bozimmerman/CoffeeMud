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
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_WaterHammer extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_WaterHammer";
	}

	private final static String	localizedName	= CMLib.lang().L("Water Hammer");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
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
	
	public Environmental getWaterSource(MOB caster)
	{
		if(caster == null)
			return null;
		final Room R=caster.location();
		if(R!=null)
		{
			if(CMLib.flags().isWateryRoom(R))
				return R;
			for(Enumeration<Item> i=R.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(I.container() == null)
				{
					if(I instanceof Drink)
					{
						if((((Drink)I).liquidRemaining() > 500)
						&&(((Drink)I).thirstQuenched()>0)
						&&((((Drink)I).liquidType() == RawMaterial.RESOURCE_FRESHWATER)
							||(((Drink)I).liquidType() == RawMaterial.RESOURCE_SALTWATER)))
						{
							return I;
						}
					}
				}
			}
			if((R.getArea() instanceof BoardableShip)
			&&((R.domainType()&Room.INDOORS)==0))
			{
				Item I=((BoardableShip)R.getArea()).getShipItem();
				if(I.owner() instanceof Room)
				{
					Room outerR=((Room)I.owner());
					if(CMLib.flags().isWateryRoom(outerR))
						return outerR;
				}
			}
		}
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(this.getWaterSource(mob)==null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String waterName = L("the water");
		if(!auto)
		{
			Environmental E=this.getWaterSource(mob);
			if(E==null)
			{
				mob.tell(L("There is no large ready source of water here to use as a hammer."));
				return false;
			}
			waterName = E.name();
		}
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L((auto?"A compressed wave from @x1 flies at <T-NAME>!":"^S<S-NAME> chant(s), gather(s) @x1 to <S-HIM-HERSELF> and hurl(s) it towards <T-NAMESELF>!^?"),waterName));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,verbalCastMask(mob,target,auto)|CMMsg.TYP_WATER,null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				final int maxDie =  (adjustedLevel( mob, asLevel )+(3*super.getX1Level(mob))) / 2;
				int damage = CMLib.dice().roll(maxDie,8,5+(maxDie/6));
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));
				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_WATER,Weapon.TYPE_BASHING,L("The compressed wave <DAMAGE> <T-NAME>!"));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades."));

		// return whether it worked
		return success;
	}
}
