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
   Copyright 2003-2020 Bo Zimmerman

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
public class Chant_MuddyGrounds extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_MuddyGrounds";
	}

	private final static String localizedName = CMLib.lang().L("Muddy Grounds");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public void unInvoke()
	{
		if((canBeUninvoked())&&(affected instanceof Room))
			((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,L("The mud in '@x1' dries up.",((Room)affected).displayText()));
		super.unInvoke();
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if((affected!=null)&&(affected instanceof Room))
			affectableStats.setWeight((affectableStats.weight()*2)+1);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(!canBeUninvoked()
		&&(!hasTicked))
		{
			if((msg.source() != null)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() == affected)
			&&(affected instanceof Room))
			{
				final Room R=(Room)affected;
				if((R!=null)
				&&(!hasTicked))
				{
					if((!CMLib.threads().isTicking(this, -1))
					&&(!CMLib.threads().isTicking(R, -1)))
						CMLib.threads().startTickDown(this, Tickable.TICKID_SPELL_AFFECT, 3);
				}
			}
		}
	}

	protected boolean hasTicked = false;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof Room))
		{
			hasTicked=true;
			final Room R=(Room)affected;
			for(int m=0;m<R.numInhabitants();m++)
			{
				final MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.isInCombat()))
					M.curState().adjMovement(-1,M.maxState());
			}
		}
		return super.tick(ticking,tickID);

	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				final int type=R.domainType();
				if(((type&Room.INDOORS)>0)
				||(type==Room.DOMAIN_OUTDOORS_AIR)
				||(type==Room.DOMAIN_OUTDOORS_CITY)
				||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
				||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		final int type=mob.location().domainType();
		if(((type&Room.INDOORS)>0)
			||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
			||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell(L("That magic won't work here."));
			return false;
		}

		if(mob.location().fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already muddy."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"":L("^S<S-NAME> chant(s) to the ground.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The ground here turns to MUD!"));
				if(CMLib.law().doesOwnThisLand(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMLib.database().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) to the ground, but nothing happens."));

		// return whether it worked
		return success;
	}
}
