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

public class Chant_SummonFire extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonFire";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Fire");

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
		return 0;
	}

	protected Room	FireLocation	= null;
	protected Item	littleFire		= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HEATING | Ability.FLAG_FIREBASED;
	}

	@Override
	public void unInvoke()
	{
		if(FireLocation==null)
			return;
		if(littleFire==null)
			return;
		if(canBeUninvoked())
			FireLocation.showHappens(CMMsg.MSG_OK_VISUAL,L("The little magical fire goes out."));
		super.unInvoke();
		if(canBeUninvoked())
		{
			final Item fire=littleFire; // protects against uninvoke loops!
			littleFire=null;
			fire.destroy();
			FireLocation.recoverRoomStats();
			FireLocation=null;
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((((mob.location().domainType()&Room.INDOORS)>0))&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||CMLib.flags().isWateryRoom(mob.location()))
		   &&(!auto))
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s) for fire.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Item I=CMClass.getItem("GenItem");
				I.basePhyStats().setWeight(50);
				I.setName(L("a magical campfire"));
				I.setDisplayText(L("A roaring magical campfire has been built here."));
				I.setDescription(L("It consists of magically burning flames, consuming no fuel."));
				I.recoverPhyStats();
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				I.setMiscText(I.text());
				final Ability B=CMClass.getAbility("Burning");
				B.setAbilityCode(100 | 512); // can't be put out by weather, and item destroyed on burn end
				I.addNonUninvokableEffect(B);

				mob.location().addItem(I);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, a little magical campfire begins burning here."));
				FireLocation=mob.location();
				littleFire=I;
				beneficialAffect(mob,I,asLevel,0);
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) for fire, but nothing happens."));

		// return whether it worked
		return success;
	}
}
