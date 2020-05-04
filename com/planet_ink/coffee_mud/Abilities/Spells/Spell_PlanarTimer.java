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
public class Spell_PlanarTimer extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PlanarTimer";
	}

	private final static String localizedName = CMLib.lang().L("Planar Timer");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Timer)");

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
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
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
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical affected = this.affected;
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final Area A=CMLib.map().areaLocation(affected);
			if((A!=null)
			&&(A.numEffects()>0))
			{
				for(final Enumeration<Ability> a=A.effects();a.hasMoreElements();)
				{
					final Ability eA=a.nextElement();
					if((eA instanceof PlanarAbility)
					&&(eA.text().length()>0))
					{
						final int planeTickDown = CMath.s_int(eA.getStat("TICKDOWN"));
						this.tickDown=planeTickDown;
						final long secsRemain = (CMProps.getTickMillis() & this.tickDown) / 1000L;
						if(secsRemain == (5 * 60))
							((MOB)affected).tell(L("You feel that this place has but 5 minutes remaining."));
						else
						if(secsRemain == (1 * 60))
							((MOB)affected).tell(L("You feel that this place has but 1 minute remaining."));
						else
						if(secsRemain > (28)&&(secsRemain <=32))
							((MOB)affected).tell(L("You feel that this place has but "+secsRemain+" seconds remaining."));
						return super.tick(ticking, tickID);
					}
				}
			}
		}
		unInvoke();
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> the insight."));
			return false;
		}

		if(CMLib.flags().getPlaneOfExistence(target) == null)
		{
			mob.tell(L("This magic would not work here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) insight into planar entropy."):L("^S<S-NAME> invoke(s) an insight into planar entry inside <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
					A.tick(target, Tickable.TICKID_MOB);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to divine planar entropy, but fail(s)."));

		return success;
	}
}
