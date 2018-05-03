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

public class Spell_StinkingCloud extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_StinkingCloud";
	}

	private final static String	localizedName	= CMLib.lang().L("Stinking Cloud");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(In the Stinking Cloud)");

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
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	Room	castingRoom	= null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
		{
			final MOB M=(MOB)affected;
			final MOB invoker=(invoker()!=null) ? invoker() : M;
			if((M.location()!=castingRoom)||(M.amDead()))
				unInvoke();
			else
			if((!M.amDead())
			&&(M.location()!=null)
			&&(CMLib.flags().canSmell(M)))
			{
				final int damage= (M.phyStats().level()/10) + super.getXLEVELLevel(invoker);
				if((M.curState().getHunger()<=0))
					CMLib.combat().postDamage(invoker,M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,-1,L("<T-NAME> heave(s) in the stinking cloud."));
				else
				{
					CMLib.combat().postDamage(invoker,M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,-1,L("<T-NAME> heave(s) all over the place!"));
					M.curState().adjHunger(-500,M.maxState().maxHunger(M.baseWeight()));
				}
				CMLib.combat().postRevengeAttack(M, invoker);
			}
			else
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof MOB)
		   &&(msg.amISource((MOB)affected)))
		{
			final MOB mob=(MOB)affected;
			if(CMLib.flags().canSmell(mob))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_ADVANCE:
					if(CMLib.dice().rollPercentage()>(mob.charStats().getSave(CharStats.STAT_SAVE_GAS)))
					{
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> double(s) over from the sickening gas."));
						return false;
					}
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_ADVANCE:
				unInvoke();
				break;
			}
		}
		else
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,L("<T-NAME> smell(s) nauseatingly stinky!"));
		super.executeMsg(myHost,msg);
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
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> manage(s) to escape the stinking cloud!"));
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Set<MOB> h=null;
		if(givenTarget instanceof MOB)
		{
			h=new HashSet<MOB>();
			h.add((MOB)givenTarget);
		}
		else
			h=CMLib.combat().properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell(L("There doesn't appear to be anyone here worth casting this on."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,somanticCastCode(mob,null,auto),auto?"":
				L("^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.  A horrendous cloud of green and orange gas appears!^?")))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),null);
					final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_GAS|(auto?CMMsg.MASK_ALWAYS:0),null);
					if((mob.location().okMessage(mob,msg))
					   &&(mob.location().okMessage(mob,msg2))
					   &&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						mob.location().send(mob,msg2);
						if((msg.value()<=0)&&(msg2.value()<=0)&&(target.location()==mob.location()))
						{
							castingRoom=mob.location();
							success=maliciousAffect(mob,target,asLevel,0,-1)!=null;
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,
									L("<S-NAME> become(s) enveloped in the stinking cloud!"));
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> incant(s), but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
