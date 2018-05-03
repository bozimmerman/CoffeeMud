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

public class Spell_ForcefulHand extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ForcefulHand";
	}

	private final static String localizedName = CMLib.lang().L("Forceful Hand");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Forceful Hand)");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}
	
	protected Item theHand=null;
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if((theHand == null) || (theHand.amDestroyed()))
			unInvoke();
		
		if((msg.target() == affected) 
		&& msg.isTarget(CMMsg.MASK_MALICIOUS)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			final MOB combatMOB=msg.source();
			final Room R=combatMOB.location();
			if((R!=null)&&(R==mob.location()))
			{
				if(!R.isHere(theHand))
					unInvoke();
				else
				if(msg.isSource(CMMsg.TYP_ADVANCE))
				{
					R.show(combatMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> struggle(s) against a forceful hand."));
					return false;
				}
				else
				if(combatMOB.getVictim() == null)
				{
					combatMOB.setVictim(mob);
					if(mob.getVictim()==combatMOB)
					{
						if(mob.rangeToTarget() > 0)
							combatMOB.setRangeToTarget(mob.rangeToTarget());
						else
						{
							combatMOB.setRangeToTarget(1);
							mob.setRangeToTarget(1);
						}
					}
					else
						combatMOB.setRangeToTarget(1);
					R.show(combatMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE> pushed back by a forceful hand."));
					return false;
				}
				else
				if((combatMOB.getVictim() == affected) && (combatMOB.rangeToTarget() <= 0))
				{
					combatMOB.setRangeToTarget(1);
					if(mob.getVictim()==combatMOB)
						mob.setRangeToTarget(1);
					R.show(combatMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE>  pushed back by a forceful hand."));
					return false;
				}
				else
				if((mob.getVictim()==combatMOB) && (mob.rangeToTarget() <= 0))
				{
					mob.setRangeToTarget(1);
					if(mob==combatMOB)
						mob.setRangeToTarget(1);
					R.show(combatMOB, affected, CMMsg.MSG_OK_ACTION, L("<S-NAME> <S-IS-ARE>  pushed back by a forceful hand."));
					return false;
				}
			}
		}
		else
		if(msg.isSource(CMMsg.TYP_ADVANCE)
		&&(msg.source() == affected)
		&& (msg.source().getVictim()==msg.target())
		&& (msg.source().rangeToTarget() == 1))
		{
			final MOB plantM=msg.source().getVictim();
			if(plantM != null)
			{
				final Room R=plantM.location();
				if(R!=null)
				{
					final CMMsg msg2=CMClass.getMsg(plantM,msg.source(),CMMsg.MSG_RETREAT,L("<S-NAME> <S-IS-ARE> held back by a forceful hand."));
					if(R.okMessage(plantM,msg2))
					{
						R.send(plantM,msg2);
						return false;
					}
				}
			}
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
				((MOB)affected).tell(L("The forceful hand vanish(es)."));
			if(theHand != null)
			{
				theHand.destroy();
				theHand=null;
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> a forceful hand."));
			return false;
		}
		
		if(!target.isInCombat())
		{
			mob.tell(L("This only works while in combat."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),auto?L("An forceful hand protects <T-NAME>!"):L("^S<S-NAME> evoke(s) a forceful hand in front of <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Spell_ForcefulHand A = (Spell_ForcefulHand)beneficialAffect(mob,target,asLevel,3 + (int)Math.round(Math.sqrt(adjustedLevel(mob,asLevel))));
				if(A!=null)
				{
					final Item theHand=CMClass.getBasicItem("GenItem");
					theHand.setName(L("a forceful hand"));
					theHand.setDisplayText(L("a forceful hand is protecting @x1",target.name()));
					theHand.basePhyStats().setDisposition(theHand.basePhyStats().disposition()|PhyStats.IS_FLYING);
					CMLib.flags().setGettable(theHand, false);
					theHand.recoverPhyStats();
					final Room R=target.location();
					if(R!=null)
						R.addItem(theHand);
					A.theHand = theHand;
					if((target.getVictim() != null) && (target.rangeToTarget() <= 0))
					{
						target.setRangeToTarget(1);
						if(R!=null)
							R.show(target, target.getVictim(), CMMsg.MSG_OK_ACTION, L("<T-NAME> <T-IS-ARE> pushed back by a forceful hand."));
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> evoke(s), but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
