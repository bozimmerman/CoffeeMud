package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_MountedRetreat extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_MountedRetreat";
	}

	private final static String localizedName = CMLib.lang().L("Mounted Retreat");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"MFLEE","MRETREAT","MOUNTEDRETREAT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_EVASIVE;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You can only retreat from combat!"));
			return false;
		}

		final Room R = mob.location();
		if(R==null)
			return false;

		if(!CMLib.flags().isMobileMounted(mob))
		{
			mob.tell(L("You must be riding a mount to use this skill."));
			return false;
		}

		String where=CMParms.combine(commands,0);
		int directionCode=-1;
		if(where.length()==0)
		{
			final Vector<Integer> directions=new Vector<Integer>();
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Exit thisExit=mob.location().getExitInDir(d);
				final Room thisRoom=mob.location().getRoomInDir(d);
				if((thisRoom!=null)
				&&(thisExit!=null)
				&&(thisExit.isOpen())
				&&(!CMath.bset(thisRoom.domainType(),Room.INDOORS)))
					directions.addElement(Integer.valueOf(d));
			}
			// up is last resort
			if(directions.size()>1)
				directions.removeElement(Integer.valueOf(Directions.UP));
			if(directions.size()>0)
			{
				directionCode=directions.elementAt(CMLib.dice().roll(1,directions.size(),-1)).intValue();
				where=CMLib.directions().getDirectionName(directionCode);
			}
		}
		else
			directionCode=CMLib.directions().getGoodDirectionCode(where);
		if(directionCode<0)
		{
			mob.tell(L("Retreat where?!"));
			return false;
		}

		final Room nR = R.getRoomInDir(directionCode);
		final Exit nE = R.getExitInDir(directionCode);
		if(((nR == null) || (nE == null))
		||(!nE.isOpen())
		||(CMath.bset(nR.domainType(),Room.INDOORS)))
		{
			mob.tell(L("You won't be able to retreat that way."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Set<Rider> grp = new HashSet<Rider>();
		mob.getGroupMembersAndRideables(grp);
		int successDelta = 0;
		for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
		{
			final MOB M = m.nextElement();
			if((M!=null)
			&&(M!=mob)
			&&(M.getVictim()!=null)
			&&(!grp.contains(M))
			&&(grp.contains(M.getVictim())))
			{
				if(CMLib.flags().isMobileMounted(M))
					successDelta -= 25;
				final Item I = M.fetchWieldedItem();
				if((I instanceof Weapon)
				&&(((Weapon)I).maxRange()>0))
					successDelta -= 5;
			}
		}

		final boolean success=proficiencyCheck(mob,successDelta,auto);

		if(!success)
		{
			mob.tell(L("Your attempt at a dignified mounted retreat is thwarted!"));
			CMLib.commands().postFlee(mob,where);
		}
		else
		{
			if(!where.equals("NOWHERE"))
			{
				mob.makePeace(true);
				CMLib.tracking().walk(mob,directionCode,true,false);
			}
		}
		return success;
	}
}
