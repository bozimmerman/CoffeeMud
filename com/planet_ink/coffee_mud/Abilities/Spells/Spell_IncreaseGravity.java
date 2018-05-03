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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_IncreaseGravity extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_IncreaseGravity";
	}

	private final static String	localizedName	= CMLib.lang().L("Increase Gravity");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Gravity is Increased)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS | CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected Room	theGravityRoom	= null;

	protected Room gravityRoom()
	{
		if (theGravityRoom != null)
			return theGravityRoom;
		if (affected instanceof Room)
			theGravityRoom = (Room) affected;
		return theGravityRoom;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		else
		if((affected!=null)&&(affected instanceof Room)&&(invoker!=null))
		{
			final Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB inhab=room.fetchInhabitant(i);
				if(inhab.fetchEffect(ID())==null)
				{
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					A.startTickDown(invoker,inhab,tickDown);
				}
				if(inhab.isInCombat())
					inhab.curState().adjMovement(-1,inhab.maxState());
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				final Room room=(Room)affected;
				room.showHappens(CMMsg.MSG_OK_VISUAL, L("Gravity returns to normal..."));
			}
			else
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((mob.location()!=null)&&(mob.location()!=gravityRoom()))
					mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, L("Your weight returns to normal.."));
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			if((((MOB)affected).location()!=gravityRoom())
			||((gravityRoom()!=null)&&(gravityRoom().fetchEffect(ID())==null)))
			{
				unInvoke();
				return true;
			}
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_ADVANCE:
			{
				if(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)<18)
				{
					msg.source().tell(L("You feel too heavy to advance."));
					return false;
				}
				break;
			}
		case CMMsg.TYP_RETREAT:
			{
				if(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)<18)
				{
					msg.source().tell(L("You feel too heavy to retreat."));
					return false;
				}
				break;
			}
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			{
				if(msg.source().charStats().getStat(CharStats.STAT_STRENGTH)<18)
				{
					msg.source().tell(L("You feel too heavy to leave."));
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!(affected instanceof MOB))
			return;
		if(((MOB)affected).location()!=gravityRoom())
			unInvoke();
		else
		{
			if((affectableStats.disposition()&PhyStats.IS_FLYING)>0)
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_FLYING);
			affectableStats.setWeight(affectableStats.weight()*2);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,L("Gravity has already been increased here!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final CMMsg msg = CMClass.getMsg(mob, target, this, somanticCastCode(mob,target,auto), L((auto?"G":"^S<S-NAME> speak(s) and wave(s) and g")+"ravity begins to increase!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				theGravityRoom=mob.location();
				beneficialAffect(mob,mob.location(),asLevel,adjustedLevel(mob,asLevel));
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> speak(s) heavily, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
