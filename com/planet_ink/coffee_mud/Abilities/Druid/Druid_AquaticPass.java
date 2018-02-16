package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Druid_AquaticPass extends StdAbility
{
	@Override
	public String ID()
	{
		return "Druid_AquaticPass";
	}

	private final static String	localizedName	= CMLib.lang().L("Aquatic Pass");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(aquatic passage)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "AQUAPASS","APASS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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
		return Ability.ACODE_SKILL | Ability.DOMAIN_STEALTHY;
	}

	public boolean canPassHere(Physical affected)
	{
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			final Room R=M.location();
			if(R!=null)
			{
				if(CMLib.flags().isWateryRoom(R))
					return true;
				return false;
			}
		}
		return false;
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if(!canPassHere(affected))
		{
			mob.tell(L("You must be in the water to perform the Aquatic Pass."));
			return false;
		}
		final String whatToOpen=CMParms.combine(commands,0);
		final int dirCode=CMLib.directions().getGoodDirectionCode(whatToOpen);
		if(dirCode<0)
		{
			mob.tell(L("Pass which direction?!"));
			return false;
		}

		final Exit exit=mob.location().getExitInDir(dirCode);
		final Room room=mob.location().getRoomInDir(dirCode);

		if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob)))
		{
			mob.tell(L("You can't see anywhere to pass that way."));
			return false;
		}
		final Exit opExit=room.getReverseExit(dirCode);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
		{
			if(exit.isOpen())
				CMLib.tracking().walk(mob,dirCode,false,false);
			else
				beneficialVisualFizzle(mob,null,L("<S-NAME> go(es) @x1, but go(es) no further.",CMLib.directions().getDirectionName(dirCode)));
		}
		else
		if(exit.isOpen())
		{
			if(mob.fetchEffect(ID())==null)
			{
				final Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				try
				{
					mob.addEffect(A);
					mob.recoverPhyStats();
					CMLib.tracking().walk(mob,dirCode,false,false);
				}
				finally
				{
					mob.delEffect(A);
				}
			}
			else
				CMLib.tracking().walk(mob,dirCode,false,false);
			mob.recoverPhyStats();
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_QUIETMOVEMENT|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final boolean open=exit.isOpen();
				final boolean locked=exit.isLocked();
				exit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),true,exit.defaultsClosed(),exit.hasALock(),false,exit.defaultsLocked());
				mob.tell(L("\n\r\n\r"));
				if(mob.fetchEffect(ID())==null)
				{
					final Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					try
					{
						mob.addEffect(A);
						mob.recoverPhyStats();
						CMLib.tracking().walk(mob,dirCode,false,false);
					}
					finally
					{
						mob.delEffect(A);
					}
				}
				else
					CMLib.tracking().walk(mob,dirCode,false,false);
				mob.recoverPhyStats();
				exit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
				if(opExit!=null)
					opExit.setDoorsNLocks(exit.hasADoor(),open,exit.defaultsClosed(),exit.hasALock(),locked,exit.defaultsLocked());
			}
		}

		return success;
	}
}
