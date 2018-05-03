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
   Copyright 2004-2018 Bo Zimmerman

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

public class Fighter_SideKick extends MonkSkill
{
	@Override
	public String ID()
	{
		return "Fighter_SideKick";
	}

	private final static String localizedName = CMLib.lang().L("Side Kick");

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
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_KICKING;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected Weapon naturalWeapon=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if((mob.isInCombat())
			&&(CMLib.flags().isAliveAwakeMobile(mob,true))
			&&(mob.rangeToTarget()==0)
			&&(mob.charStats().getBodyPart(Race.BODY_LEG)>1)
			&&(mob.location()!=null)
			&&(!anyWeapons(mob)))
			{
				MOB elligibleTarget=null;
				for(int m=0;m<mob.location().numInhabitants();m++)
				{
					final MOB M=mob.location().fetchInhabitant(m);
					if((M!=null)
					&&(M.getVictim()==mob)
					&&(mob.getVictim()!=M)
					&&(M.rangeToTarget()==0))
					{
						elligibleTarget=M;
						break;
					}
				}
				if(elligibleTarget!=null)
				{
					if(CMLib.dice().rollPercentage()>95)
						helpProficiency(mob, 0);
					if((naturalWeapon==null)
					||(naturalWeapon.amDestroyed()))
					{
						naturalWeapon=CMClass.getWeapon("GenWeapon");
						naturalWeapon.setName(L("a side kick"));
						naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
						naturalWeapon.setUsesRemaining(1000);
						naturalWeapon.setWeaponDamageType(Weapon.TYPE_BASHING);
						naturalWeapon.basePhyStats().setDamage(5);
						naturalWeapon.recoverPhyStats();
					}
					naturalWeapon.setUsesRemaining(100);
					final MOB prevVictim=mob.getVictim();
					CMLib.combat().postAttack(mob,elligibleTarget,naturalWeapon);
					if((prevVictim!=mob.getVictim())&&(elligibleTarget==mob.getVictim())&&((prevVictim==null)||(!prevVictim.amDead())))
						mob.setVictim(prevVictim);
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool() instanceof Weapon)
		&&(msg.tool()==naturalWeapon))
			msg.setValue(msg.value()+naturalWeapon.basePhyStats().damage()+super.getXLEVELLevel(mob));
		return true;
	}

}
