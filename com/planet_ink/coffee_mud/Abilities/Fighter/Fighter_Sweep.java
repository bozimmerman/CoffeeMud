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

public class Fighter_Sweep extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Sweep";
	}

	private final static String localizedName = CMLib.lang().L("Sweep");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"SWEEP"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{

		final float f=(float)CMath.mul(0.1,(float)getXLEVELLevel(invoker()));
		affectableStats.setAttackAdjustment((int)Math.round(CMath.div(affectableStats.attackAdjustment(),2.0-f)));
		affectableStats.setDamage((int)Math.round(CMath.div(affectableStats.damage(),3.0-f)));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			final Set<MOB> h=properTargets(mob,target,false);
			if(h.size()<2)
				return Ability.QUALITY_INDIFFERENT;
			for(final MOB M : h)
			{
				if((M.rangeToTarget()<0)||(M.rangeToTarget()>0))
					h.remove(M);
			}
			if(h.size()<2)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell(L("You are too far away to sweep!"));
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to sweep!"));
			return false;
		}
		final Set<MOB> h=properTargets(mob,givenTarget,false);
		for(final MOB M : h)
		{
			if((M.rangeToTarget()<0)||(M.rangeToTarget()>0))
				h.remove(M);
		}

		if(h.size()==0)
		{
			mob.tell(L("There aren't enough enough targets in range!"));
			return false;
		}

		final Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell(L("You need a weapon to sweep!"));
			return false;
		}
		final Weapon wp=(Weapon)w;
		if(wp.weaponDamageType()!=Weapon.TYPE_SLASHING)
		{
			mob.tell(L("You cannot sweep with @x1!",wp.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,L("^F^<FIGHT^><S-NAME> sweep(s)!^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				final Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				try
				{
					mob.addEffect(A);
					mob.recoverPhyStats();
					for (final Object element : h)
					{
						final MOB target=(MOB)element;
						msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_OK_ACTION|(auto?CMMsg.MASK_ALWAYS:0),null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							CMLib.combat().postAttack(mob,target,w);
						}
					}
				}
				finally
				{
					mob.delEffect(A);
				}
				mob.recoverPhyStats();
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> fail(s) to sweep."));

		// return whether it worked
		return success;
	}
}
