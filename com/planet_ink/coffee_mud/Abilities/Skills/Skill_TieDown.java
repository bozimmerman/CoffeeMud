package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Skill_TieDown extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_TieDown";
	}

	private final static String localizedName = CMLib.lang().L("Tie Down");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Tied down)");
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_BINDING;
	}

	private static final String[] triggerStrings =I(new String[] {"TIEDOWN"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_BINDING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BOUND);
		affectableStats.addAmbiance("Tied Down");
	}

	@Override
	public int abilityCode()
	{
		return 20 + (8 * super.getXLEVELLevel(invoker()));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.source().riding()==affected))
		{
			final Physical affected=this.affected;
			msg.source().tell(L("@x1 is tied down and not going anywhere.",affected.Name()));
			final Room R=CMLib.map().roomLocation(affected);
			if(affected instanceof BoardableShip)
				R.show(msg.source(), null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> struggle(s) against the ropes."));
			else
				R.show(msg.source(), affected, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> fail(s) to sail off in <T-NAME>."));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if((commands.size()>0)&&(commands.get(0)).equalsIgnoreCase("UNTIE"))
		{
			commands.remove(0);
			final Item target=super.getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ROOMONLY);
			if(target==null)
				return false;
			final Skill_TieDown A=(Skill_TieDown)target.fetchEffect(ID());
			if(A!=null)
			{
				if(mob.location().show(mob,target,null,CMMsg.MSG_HANDS,L("<S-NAME> attempt(s) to untie <T-NAMESELF>.")))
				{
					if((A.invoker()==null)
					||(A.invoker()==mob)
					|| (mob.phyStats().level() > (A.invoker().phyStats().level() + (6 * A.getXLEVELLevel(A.invoker())))))
					{
						A.unInvoke();
						return true;
					}
					else
					{
						mob.location().show(mob,target,null,CMMsg.MSG_HANDS,L("<S-NAME> fail(s)."));
						return false;
					}
				}
				return false;
			}
			mob.tell(L("@x1 doesn't appear to be tied down.",target.name(mob)));
			return false;
		}

		final Item target=super.getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ROOMONLY);
		if(target==null)
			return false;

		if((target instanceof Rideable)
		&&((((Rideable)target).rideBasis()==Rideable.RIDEABLE_WATER)
			||(((Rideable)target).rideBasis()==Rideable.RIDEABLE_AIR))
		&&(((Rideable)target).mobileRideBasis()))
		{
			// OK
		}
		else
		if(target instanceof SailingShip)
		{
			final PrivateProperty rec=CMLib.law().getPropertyRecord(target);
			if((rec == null)
			||CMLib.law().doesHavePrivilegesWith(mob, rec))
			{
				// OK
			}
			else
			{
				mob.tell(L("@x1 may not be tied down by you.",target.Name()));
				return false;
			}
		}
		else
		{
			mob.tell(L("@x1 can not be tied down.",target.Name()));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;
		if((R.domainType()!=Room.DOMAIN_OUTDOORS_SEAPORT)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_SEAPORT)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_CAVE_SEAPORT))
		{
			mob.tell(L("@x1 can not be tied down anywhere except at a sea port.",R.Name()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String str=auto?L("<T-NAME> become(s) tied down."):L("<S-NAME> tie(s) down <T-NAME> with intricate knots.");
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_NOISYMOVEMENT,auto?"":str,str,str);
			if((mob.location().okMessage(mob,msg))&&(mob.fetchEffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				success=beneficialAffect(mob,target,asLevel,Ability.TICKS_FOREVER)!=null;
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to tie down <T-NAME> and fail(s)."));

		// return whether it worked
		return success;
	}
}
