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

public class Chant_SacredEarth extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SacredEarth";
	}

	private final static String	localizedName	= CMLib.lang().L("Sacred Earth");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Sacred Earth)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ENDURING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Room)))
			return;
		final Room R=(Room)affected;
		if(canBeUninvoked())
			R.showHappens(CMMsg.MSG_OK_VISUAL,L("The sacred earth charm is ended."));

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_GATHERINGSKILL))
		{
			msg.source().tell(L("The sacred earth will not allow you to violate it."));
			return false;
		}
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() instanceof MOB)
		&&((((MOB)msg.target()).charStats().getMyRace().racialCategory().equals("Vegetation"))
		||(((MOB)msg.target()).charStats().getMyRace().racialCategory().equals("Earth Elemental"))))
		{
			final int recovery=(int)Math.round(CMath.div((msg.value()),2.0));
			msg.setValue(msg.value()-recovery);
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(((R.domainType()&Room.INDOORS)>0)
				||CMLib.flags().isWateryRoom(R)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
					return Ability.QUALITY_INDIFFERENT;
			}

			if(mob.isInCombat())
			{
				final MOB victim=mob.getVictim();
				if(victim!=null)
				{
					if(((victim.charStats().getMyRace().racialCategory().equals("Vegetation"))
					||(victim.charStats().getMyRace().racialCategory().equals("Earth Elemental"))))
						return Ability.QUALITY_INDIFFERENT;
				}
				if(((!mob.charStats().getMyRace().racialCategory().equals("Vegetation"))
				&&(!mob.charStats().getMyRace().racialCategory().equals("Earth Elemental"))))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("This earth is already sacred."));
			return false;
		}
		if((((mob.location().domainType()&Room.INDOORS)>0)
		   ||(CMLib.flags().isWateryRoom(mob.location()))
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR))
		&&(!auto))
		{
			mob.tell(L("This chant will not work here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to the ground.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The charm of the sacred earth begins here!"));
					beneficialAffect(mob,target,asLevel,0);
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R=mob.location().getRoomInDir(d);
						if((R!=null)
						&&(R.fetchEffect(ID())==null)
						&&((R.domainType()&Room.INDOORS)==0)
						&&(!CMLib.flags().isWateryRoom(R))
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
							beneficialAffect(mob,R,asLevel,0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) to the ground, but the magic fades."));
		// return whether it worked
		return success;
	}
}
