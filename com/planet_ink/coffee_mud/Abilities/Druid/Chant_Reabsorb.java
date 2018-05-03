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

public class Chant_Reabsorb extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Reabsorb";
	}

	private final static String localizedName = CMLib.lang().L("Reabsorb");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof Item))
				return Ability.QUALITY_INDIFFERENT;
			final Room R=mob.location();
			if(R!=null)
			{
				final int type=R.domainType();
				if((type==Room.DOMAIN_INDOORS_STONE)
				||(type==Room.DOMAIN_INDOORS_WOOD)
				||(type==Room.DOMAIN_INDOORS_MAGIC)
				||(type==Room.DOMAIN_INDOORS_UNDERWATER)
				||(type==Room.DOMAIN_INDOORS_WATERSURFACE)
				||(type==Room.DOMAIN_OUTDOORS_AIR)
				||(type==Room.DOMAIN_OUTDOORS_CITY)
				||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
				||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=this.getTarget(mob,mob.location(),givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		final List<DeadBody> V=CMLib.utensils().getDeadBodies(target);
		for(int v=0;v<V.size();v++)
		{
			final DeadBody D=V.get(v);
			if((D!=null)
			&&(D.isPlayerCorpse())
			&&(!D.getMobName().equals(mob.Name())))
			{
				mob.tell(L("You are not allowed to reabsorb a player corpse."));
				return false;
			}
		}
		if(!(target.owner() instanceof Room))
		{
			mob.tell(L("You need to put @x1 on the ground first.",target.name(mob)));
			return false;
		}
		if(!CMLib.utensils().canBePlayerDestroyed(mob,target,true))
		{
			mob.tell(L("@x1 can not be reabsorbed.",target.name(mob)));
			return false;
		}
		
		final int type=mob.location().domainType();
		if((type==Room.DOMAIN_INDOORS_STONE)
			||(type==Room.DOMAIN_INDOORS_WOOD)
			||(type==Room.DOMAIN_INDOORS_MAGIC)
			||(type==Room.DOMAIN_INDOORS_UNDERWATER)
			||(type==Room.DOMAIN_INDOORS_WATERSURFACE)
			||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
			||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell(L("That magic won't work here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> starts vibrating!"):L("^S<S-NAME> chant(s), causing <T-NAMESELF> to decay!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The ground reabsorbs @x1.",target.name()));
					target.destroy();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAME>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
