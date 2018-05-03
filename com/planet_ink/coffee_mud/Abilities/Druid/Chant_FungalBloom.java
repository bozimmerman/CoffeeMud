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
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_FungalBloom extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_FungalBloom";
	}

	private final static String	localizedName	= CMLib.lang().L("Fungal Bloom");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	public Item getShroomHere(Room R)
	{
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I!=null)
			&&(I.container()==null)
			&&((I.material()==RawMaterial.RESOURCE_MUSHROOMS)
				||(I.material()==RawMaterial.RESOURCE_FUNGUS))
			&&(I.fetchEffect("Bomb_Poison")==null))
				return I;
		}
		return null;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
				||(R.domainType()==Room.DOMAIN_INDOORS_AIR)
				||(CMLib.flags().isWateryRoom(R)))
					return Ability.QUALITY_INDIFFERENT;
				if(getShroomHere(mob.location())==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_AIR)
		||(CMLib.flags().isWateryRoom(mob.location())))
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}

		Item target=null;
		if(commands.size()==0)
			target=getShroomHere(mob.location());
		else
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
		{
			if(mob.isMonster())
				target=getShroomHere(mob.location());
			if(target==null)
				return false;
		}
		if((target.material()!=RawMaterial.RESOURCE_MUSHROOMS)
		&&(target.material()!=RawMaterial.RESOURCE_FUNGUS))
		{
			mob.tell(L("@x1 is not a fungus!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.setDescription(L("It seems to be getting puffier and puffier!"));
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 seems to be puffing up!",target.name()));
				Ability A=CMClass.getAbility("Bomb_Poison");
				A.setMiscText("Poison_Bloodboil");
				A.setInvoker(mob);
				A.setSavable(false);
				((Trap)A).setReset(3);
				target.addEffect(A);
				A=target.fetchEffect(A.ID());
				if(A!=null)
					((Trap)A).activateBomb();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to the <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
