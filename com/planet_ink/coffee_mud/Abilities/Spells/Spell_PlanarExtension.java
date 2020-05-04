package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Spell_PlanarExtension extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PlanarExtension";
	}

	private final static String localizedName = CMLib.lang().L("Planar Extension");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Extension)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();

	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isMonster())
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().getPlaneOfExistence(target) == null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Physical))
			target=givenTarget;
		final Area targetA=CMLib.map().areaLocation(target);
		if(targetA == null)
			return false;
		if(targetA.fetchEffect(ID())!=null)
		{
			mob.tell(mob,targetA,null,L("<T-NAME> has already received a magical extension."));
			return false;
		}

		if(CMLib.flags().getPlaneOfExistence(targetA) == null)
		{
			mob.tell(L("This magic would not work here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,targetA,this,somanticCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) victory over planar entropy."):L("^S<S-NAME> invoke(s) a slowing of planar entry inside <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=beneficialAffect(mob,targetA,asLevel,0);
				if(A!=null)
				{
					if((targetA!=null)
					&&(targetA.numEffects()>0))
					{
						for(final Enumeration<Ability> a=targetA.effects();a.hasMoreElements();)
						{
							final Ability eA=a.nextElement();
							if((eA instanceof PlanarAbility)
							&&(eA.text().length()>0))
							{
								final int planeTickDown = CMath.s_int(eA.getStat("TICKDOWN"));
								eA.setStat("TICKDOWN", ""+(planeTickDown + (((5+super.adjustedLevel(mob, asLevel)/5)*60000)/CMProps.getTickMillis())));
								A.setStat("TICKDOWN", ""+(planeTickDown + (((5+super.adjustedLevel(mob, asLevel)/5)*60000)/CMProps.getTickMillis())));
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to defeat planar entropy, but fail(s)."));

		return success;
	}
}
