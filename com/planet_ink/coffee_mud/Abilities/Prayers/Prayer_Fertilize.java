package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_Fertilize extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Fertilize";
	}

	private final static String localizedName = CMLib.lang().L("Fertilize");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private volatile int oldResource = -1;
	
	@Override
	public void unInvoke()
	{
		if(this.canBeUninvoked() && (affected instanceof Room))
		{
			final Room R=(Room)affected;
			if((R!=null)&&(oldResource>0))
				R.setResource(oldResource);
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			if((R.myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			{
				oldResource=R.myResource();
				for(int m=0;m<R.numInhabitants();m++)
				{
					final MOB M=R.fetchInhabitant(m);
					if(M!=null)
					{
						Ability A=M.fetchEffect("Farming");
						if(A==null)
							A=M.fetchEffect("Foraging");
						if(A==null)
							A=M.fetchEffect("MasterFarming");
						if(A==null)
							A=M.fetchEffect("MasterForaging");
						if(A==null)
							A=M.fetchEffect("MasterGardening");
						if(A==null)
							A=M.fetchEffect("Gardening");
						if(A!=null)
							A.setAbilityCode(1);
					}
				}
			}
		}
		return super.tick(ticking,tickID);

	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		final int type=mob.location().domainType();
		if(((type&Room.INDOORS)>0)
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
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"":L("^S<S-NAME> @x1 to make the land fruitful.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				this.oldResource=mob.location().myResource();
				if(beneficialAffect( mob,
								  mob.location(),
								  asLevel,
								  CMLib.ableMapper().qualifyingClassLevel( mob, this ) *
									  (int)( ( CMProps.getMillisPerMudHour() *
											  (mob.location().getArea().getTimeObj().getHoursInDay()) ) /
											  CMProps.getTickMillis() ) )!=null)
				{
					// the chant should be better than the prayer, so leave this part out -- 
					// but keep the functionality around just in case we want it.
					//mob.location().setResource(RawMaterial.RESOURCE_DIRT);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 to make the land fruitful, but nothing happens.",prayForWord(mob)));

		// return whether it worked
		return success;
	}
}
