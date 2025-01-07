package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdPlanarAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.*;
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
   Copyright 2024-2024 Bo Zimmerman

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
public class Skill_PlanarVeil extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_PlanarVeil";
	}

	private final static String	localizedName	= CMLib.lang().L("Planar Veil");

	@Override
	public String name()
	{
		return localizedName;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PLANARVEIL", "PVEIL" });

	protected long lastFail = 0;

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA | USAGE_MOVEMENT;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Room R = CMLib.map().roomLocation(affected);
		if(R == null)
			return false;
		if((R.domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
		&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WOODS))
			unInvoke();
		else
		{
			final Area A =R.getArea();
			final String currPlane = CMLib.flags().getPlaneOfExistence(A);
			if(currPlane == null)
				unInvoke();
		}
		return true;
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> planar veil is lifted."));
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		if(A==null)
			return false;
		Ability effA = mob.fetchEffect(ID());
		if(effA!=null)
		{
			effA.unInvoke();
			return true;
		}
		String currPlane = CMLib.flags().getPlaneOfExistence(A);
		if(currPlane == null)
		{
			currPlane="Prime Material";
			mob.tell(L("\n\rYou are clearly on the Prime Material plane."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final Room room=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> raise(s) a planar veil around <S-HIMHERSELF>."));
		if(room.okMessage(mob,msg))
		{
			room.send(mob,msg);
			effA=super.beneficialAffect(mob, mob,0,-1);
			if(effA != null)
				effA.makeLongLasting();
		}
		else
			mob.location().show(mob,null,this,CMMsg.MSG_THINK,L("<S-NAME> get(s) frustrated over feeling so seen."));
		return success;
	}

}
