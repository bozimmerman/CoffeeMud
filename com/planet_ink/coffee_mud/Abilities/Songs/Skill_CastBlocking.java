package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Skill_CastBlocking extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_CastBlocking";
	}

	private final static String localizedName = CMLib.lang().L("Cast Blocking");

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
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"CASTBLOCKING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_THEATRE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You must stand up first!"));
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell(L("Only while you are fighting!"));
			return false;
		}
		
		if(mob.location().numInhabitants()==1)
		{
			mob.tell(L("You are the only one here!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> re-arrange(s) the stage blocking of the entire cast."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final List<MOB> oneSide=new Vector<MOB>();
				final List<MOB> twoSide=new Vector<MOB>();
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if(M.isInCombat() && (!twoSide.contains(M))&&(!oneSide.contains(M)))
					{
						MOB vicM=M.getVictim();
						if(oneSide.contains(vicM))
						{
							twoSide.add(M);
							if((vicM!=null)&&(!twoSide.contains(vicM))&&(!oneSide.contains(vicM)))
								oneSide.add(vicM);
						}
						else
						{
							oneSide.add(M);
							if((vicM!=null)&&(!twoSide.contains(vicM))&&(!oneSide.contains(vicM)))
								twoSide.add(vicM);
						}
					}
				}
				if((oneSide.size()>0) && (twoSide.size()>0))
				{
					for(MOB M : oneSide)
					{
						final CMMsg msg2=CMClass.getMsg(mob,M,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),null);
						if(R.okMessage(mob,msg2))
						{
							R.send(mob,msg2);
							if(msg2.value()<=0)
							{
								M.setVictim(twoSide.get(CMLib.dice().roll(1, twoSide.size(), -1)));
								M.setRangeToTarget(CMLib.dice().roll(1, R.maxRange()+1,-1));
							}
						}
					}
					for(MOB M : twoSide)
					{
						final CMMsg msg2=CMClass.getMsg(mob,M,this,CMMsg.MASK_MAGIC|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),null);
						if(R.okMessage(mob,msg2))
						{
							R.send(mob,msg2);
							if(msg2.value()<=0)
							{
								M.setVictim(oneSide.get(CMLib.dice().roll(1, oneSide.size(), -1)));
								M.setRangeToTarget(CMLib.dice().roll(1, R.maxRange()+1,-1));
							}
						}
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> point(s) around, confusing <S-HIM-HERSELF>."));

		return success;
	}

}
