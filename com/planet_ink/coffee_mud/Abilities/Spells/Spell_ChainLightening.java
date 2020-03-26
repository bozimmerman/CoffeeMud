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
   Copyright 2001-2020 Bo Zimmerman

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
public class Spell_ChainLightening extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ChainLightening";
	}

	private final static String localizedName = CMLib.lang().L("Chain Lightning");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(2);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ELECTRICBASED;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, boolean auto, final int asLevel)
	{
		Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
			h=new HashSet<MOB>();

		final Set<MOB> myGroup=mob.getGroupMembers(new HashSet<MOB>());
		final List<MOB> targets=new ArrayList<MOB>(h);
		for (final MOB element : h)
			targets.add(element);
		for (final MOB element : myGroup)
		{
			final MOB M=element;
			if((M!=mob)
			&&(!targets.contains(M)))
				targets.add(M);
		}
		if(!targets.contains(mob))
			targets.add(mob);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int maxDie =  (int)Math.round(CMath.div(adjustedLevel(mob,asLevel)+(2.0*super.getX1Level(mob)),2.0));
		int damage = CMLib.dice().roll(maxDie,8,maxDie/4); // it gets split in half at post-time, for each person split in half again

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final Room R=mob.location();
			if(R.show(mob,null,this,verbalCastCode(mob,null,auto),L(auto?"A thunderous crack of lightning erupts!":"^S<S-NAME> invoke(s) a thunderous crack of lightning.^?")+CMLib.protocol().msp("lightning.wav",40)))
			{
				while(damage>0)
				{
					final int oldDamage=damage;
					for(int i=0;i<targets.size();i++)
					{
						final MOB target=targets.get(i);
						if(target.amDead()||(target.location()!=R))
						{
							int count=0;
							for(int i2=0;i2<targets.size();i2++)
							{
								final MOB M2=targets.get(i2);
								if((!M2.amDead())
								&&(R!=null)
								&&(R.isInhabitant(M2))
								&&(M2.location()==R))
									count++;
							}
							if(count<2)
								return true;
							continue;
						}

						final boolean oldAuto=auto;
						if((target==mob)||(myGroup.contains(target)))
							auto=true;
						final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
						final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_ALWAYS:0),null);
						auto=oldAuto;
						if((R.okMessage(mob,msg))
						&&(R.okMessage(mob,msg2)))
						{
							R.send(mob,msg);
							R.send(mob,msg2);
							invoker=mob;

							int dmg=damage;
							if((msg.value()>0)||(msg2.value()>0)||myGroup.contains(target)||(mob==target))
								dmg = (int)Math.round(CMath.div(dmg,2.0));
							if(target.location()==R)
							{
								CMLib.combat().postDamage(mob,target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,L("The bolt <DAMAGE> <T-NAME>!"));
								damage = (int)Math.round(CMath.div(damage,2.0));
								if(damage<5)
								{
									damage=0;
									break;
								}
							}
						}
					}
					if(oldDamage==damage)
						break;
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
