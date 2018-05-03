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

public class Prayer_AntiUndeadField extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_AntiUndeadField";
	}

	private final static String localizedName = CMLib.lang().L("Anti Undead Field");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Anti Undead Field)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
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
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	protected int pointsRemaining = 5;
	
	@Override
	public void setAffectedOne(Physical affected)
	{
		if(super.affected != affected)
			pointsRemaining = 3 + (adjustedLevel(invoker(),0)/5);
		super.setAffectedOne(affected);
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if((msg.target() == affected) 
		&& msg.isTarget(CMMsg.MASK_MALICIOUS)
		&&(msg.source().charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
		&&(affected instanceof MOB)
		&&(pointsRemaining >= 0))
		{
			final MOB mob=(MOB)affected;
			final MOB undeadMOB=msg.source();
			final Room R=undeadMOB.location();
			if((R!=null)&&(R==mob.location()))
			{
				if((msg.isSource(CMMsg.TYP_ADVANCE))
				&& (--pointsRemaining >=0))
				{
					R.show(undeadMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> struggle(s) against <T-YOUPOSS> anti-undead field."));
					return false;
				}
				else
				if(undeadMOB.getVictim() == null)
				{
					undeadMOB.setVictim(mob);
					if(mob.getVictim()==undeadMOB)
					{
						if(mob.rangeToTarget() > 0)
							undeadMOB.setRangeToTarget(mob.rangeToTarget());
						else
						{
							undeadMOB.setRangeToTarget(R.maxRange());
							mob.setRangeToTarget(R.maxRange());
						}
					}
					else
						undeadMOB.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(undeadMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-undead field."));
					return false;
				}
				else
				if((undeadMOB.getVictim() == affected) && (undeadMOB.rangeToTarget() <= 0))
				{
					undeadMOB.setRangeToTarget(R.maxRange());
					if(mob.getVictim()==undeadMOB)
						mob.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(undeadMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-undead field."));
					return false;
				}
				else
				if((mob.getVictim()==undeadMOB)&&(mob.rangeToTarget() <= 0))
				{
					mob.setRangeToTarget(R.maxRange());
					if((--pointsRemaining) < 0)
					{
						unInvoke();
						return true;
					}
					R.show(undeadMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> repelled by <T-YOUPOSS> anti-undead field."));
					return false;
				}
			}
		}
		else
		if(msg.isSource(CMMsg.TYP_ADVANCE)
		&&(msg.source() == affected)
		&& (msg.source().getVictim()==msg.target())
		&& (((MOB)msg.target()).charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
		&& (pointsRemaining >=0)
		&& (msg.source().rangeToTarget() == 1))
		{
			final MOB undeadM=msg.source().getVictim();
			if(undeadM != null)
			{
				final Room R=undeadM.location();
				if(R!=null)
				{
					final CMMsg msg2=CMClass.getMsg(undeadM,msg.source(),CMMsg.MSG_RETREAT,L("<S-NAME> <S-IS-ARE> pushed back by <T-YOUPOSS> anti-undead field."));
					if(R.okMessage(undeadM,msg2))
						R.send(undeadM,msg2);
				}
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
				((MOB)affected).tell(L("Your anti-undead field fades."));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target==null)
			return false;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("An anti-undead field surrounds <T-NAME>!"):L("^S<S-NAME> @x1 for an anti-undead field!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for an anti-undead field, but <S-HIS-HER> plea is not answered.",prayWord(mob)));
 
		// return whether it worked
		return success;
	}
}
