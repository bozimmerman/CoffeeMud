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

public class Spell_Repulsion extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Repulsion";
	}

	private final static String localizedName = CMLib.lang().L("Repulsion");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Repulsion)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(3);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING;
	}

	public int amountRemaining=0;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			{
				if(mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> struggle(s) against the repulsion field.")))
				{
					amountRemaining-=mob.charStats().getStat(CharStats.STAT_STRENGTH);
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID == Tickable.TICKID_MOB)
		{
			final Room R=CMLib.map().roomLocation(affected);
			if((R!=null)
			&&(invoker!=null)
			&&((!R.isInhabitant(invoker))||(!invoker.isInCombat())))
			{
				unInvoke();
				return false;
			}
		}
		return super.tick(ticking, tickID);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> manage(s) to break <S-HIS-HER> way free of the repulsion field."));
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if((h==null)||(h.size()==0))
		{
			mob.tell(L("There doesn't appear to be anyone here worth repelling."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms and cast(s) a spell.^?")))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),null);
					if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							amountRemaining=130;
							if(target.location()==mob.location())
							{
								success=maliciousAffect(mob,target,asLevel,((mob.phyStats().level()+(2*getXLEVELLevel(mob)))*10),-1)!=null;
								int level=2;
								if((CMLib.ableMapper().qualifyingClassLevel(mob,this)>0)&&((adjustedLevel(mob,asLevel)-CMLib.ableMapper().qualifyingClassLevel(mob,this))>10))
									level+=((adjustedLevel(mob,asLevel)-CMLib.ableMapper().qualifyingClassLevel(mob,this))-10)/10;
								if(level<2)
									level=2;
								target.location().show(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> become(s) repelled!"));
								if((target.getVictim()!=null)&&(target.rangeToTarget()>0))
									target.setRangeToTarget(target.rangeToTarget());
								else
								if(target.location().maxRange()<level)
									target.setRangeToTarget(target.location().maxRange());
								else
									target.setRangeToTarget(level);
								if(target.getVictim()!=null)
									target.getVictim().setRangeToTarget(target.rangeToTarget());
								if(mob.getVictim()==null) 
									mob.setVictim(null); // correct range
								if(target.getVictim()==null) 
									target.setVictim(null); // correct range
							}
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> wave(s) <S-HIS-HER> arms, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
