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

public class Chant_RepelVermin extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_RepelVermin";
	}

	private final static String localizedName = CMLib.lang().L("Repel Vermin");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Repel Vermin)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected int pointsRemaining = 8;
	
	@Override
	public void setAffectedOne(Physical affected)
	{
		if(super.affected != affected)
			pointsRemaining = 8 + (adjustedLevel(invoker(),0)/5);
		super.setAffectedOne(affected);
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if((msg.target() == affected) 
		&& msg.isTarget(CMMsg.MASK_MALICIOUS)
		&& CMLib.flags().isVermin(msg.source())
		&&(affected instanceof MOB)
		&&(pointsRemaining >= 0))
		{
			final MOB mob=(MOB)affected;
			final MOB verminM=msg.source();
			final Room R=verminM.location();
			if((R!=null)&&(R==mob.location()))
			{
				if((msg.isSource(CMMsg.TYP_ADVANCE))
				&& (--pointsRemaining >=0))
				{
					R.show(verminM, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> struggle(s) against <T-YOUPOSS> anti-vermin field."));
					return false;
				}
				else
				if(verminM.getVictim() == null)
				{
					verminM.setVictim(mob);
					if(mob.getVictim()==verminM)
					{
						if(mob.rangeToTarget() > 0)
							verminM.setRangeToTarget(mob.rangeToTarget());
						else
						{
							verminM.setRangeToTarget(R.maxRange());
							mob.setRangeToTarget(R.maxRange());
						}
					}
					else
						verminM.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(verminM, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-vermin field."));
					return false;
				}
				else
				if((verminM.getVictim() == affected) && (verminM.rangeToTarget() <= 0))
				{
					verminM.setRangeToTarget(R.maxRange());
					if(mob.getVictim()==verminM)
						mob.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(verminM, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-vermin field."));
					return false;
				}
				else
				if((mob.getVictim()==verminM)&&(mob.rangeToTarget() <= 0))
				{
					mob.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(verminM, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-vermin field."));
					return false;
				}
			}
		}
		else
		if(msg.isSource(CMMsg.TYP_ADVANCE)
		&&(msg.source() == affected)
		&& (msg.source().getVictim()==msg.target())
		&& (CMLib.flags().isVermin((MOB)msg.target()))
		&& (pointsRemaining >=0)
		&& (msg.source().rangeToTarget() == 1))
		{
			final MOB verminM=msg.source().getVictim();
			if(verminM != null)
			{
				final Room R=verminM.location();
				if(R!=null)
				{
					final CMMsg msg2=CMClass.getMsg(verminM,msg.source(),CMMsg.MSG_RETREAT,L("<S-NAME> <S-IS-ARE> pushed back by <T-YOUPOSS> anti-vermin field."));
					if(R.okMessage(verminM,msg2))
						R.send(verminM,msg2);
				}
			}
		}
		else
		if((msg.target() == affected) 
		&& (msg.isTarget(CMMsg.MASK_MALICIOUS)||msg.isTarget(CMMsg.TYP_DAMAGE))
		&&(affected instanceof MOB)
		&&(pointsRemaining >= 0)
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().indexOf("Insect")>=0))
		{
			final Room R=((MOB)affected).location();
			if(R!=null)
			{
				R.show(msg.source(),affected,null,CMMsg.MSG_OK_VISUAL,L("The insect magic from <S-NAME> is repelled by <T-YOUPOSS> anti-vermin field!"));
				return false;
			}
			if((--pointsRemaining) < 0)
			{
				unInvoke();
				return true;
			}
		}
		if(pointsRemaining < 0)
		{
			unInvoke();
			return true;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Physical affected=super.affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
				((MOB)affected).tell(L("Your anti-vermin field fades."));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),auto?L("An anti-vermin field surrounds <T-NAME>!"):L("^S<S-NAME> chant(s) for an anti-vermin field around <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for a field at <T-NAMESELF>, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
