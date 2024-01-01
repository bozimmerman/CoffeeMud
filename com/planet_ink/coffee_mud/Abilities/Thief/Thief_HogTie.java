package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Thief_HogTie extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_HogTie";
	}

	private final static String localizedName = CMLib.lang().L("Hog Tie");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_BINDING;
	}

	private static final String[] triggerStrings =I(new String[] {"HOGTIE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected int maxRange=0;

	@Override
	public int maxRange()
	{
		return maxRange;
	}

	@Override
	public int minRange()
	{
		return 0;
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

	protected Item getRope(final MOB mob, final boolean auto)
	{
		Item lasso = mob.fetchHeldItem();
		if(CMLib.flags().isARope(lasso))
			return lasso;
		lasso = mob.fetchWieldedItem();
		if(CMLib.flags().isARope(lasso))
			return lasso;
		if(auto)
		{
			lasso = CMClass.getBasicItem("GenRideable");
			lasso.setMaterial(RawMaterial.RESOURCE_HEMP);
			((Rideable)lasso).setRideBasis(Rideable.Basis.LADDER);
			lasso.setName("a lasso");
			lasso.setDisplayText("a lasso is here");
			return lasso;
		}
		return null;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(target.fetchEffect("Thief_Bind")!=null)
				return Ability.QUALITY_INDIFFERENT;
			if((!CMLib.flags().isSleeping(target))
			&&(!CMLib.flags().isSitting(target))
			&&(CMLib.flags().canMove((MOB)target)))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
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
			final Ability A = CMClass.getAbility("Thief_Bind");
			if(A != null)
			{
				A.setProficiency(100);
				return A.invoke(mob, commands, givenTarget, auto, asLevel);
			}
			mob.tell(L("You can't ever do that.  Stop trying."));
			return false;
		}

		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.fetchEffect("Thief_Bind")!=null)
		{
			mob.tell(L("@x1 is already bound.",target.name(mob)));
			return false;
		}

		final Item rope = getRope(mob,auto);
		if(rope == null)
		{
			mob.tell(L("You need a rope in hand to do that."));
			return false;
		}

		if((!CMLib.flags().isSleeping(target))
		&&(CMLib.flags().canMove(target)
		&&(!CMLib.flags().isSitting(target))
		&&(!auto)))
		{
			mob.tell(L("@x1 doesn't look willing to cooperate.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		success = success & CMLib.commands().postRemove(mob, rope, true);
		success = success & CMLib.commands().postDrop(mob, rope, true, false, false);
		if(success)
		{
			if(auto)
				maxRange=10;
			final String ropeName = rope.Name();
			final String str=auto?L("<T-NAME> become(s) hog tied by @x1.",ropeName):L("<S-NAME> hog tie(s) <T-NAME> with @x1.",ropeName);
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND|CMMsg.MASK_MALICIOUS,auto?"":str,str,str);
			if((target.location().okMessage(mob,msg))
			&&(target.fetchEffect("Thief_Bind")==null))
			{
				target.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final Thief_Bind A=(Thief_Bind)CMClass.getAbility("Thief_Bind");
					A.ropeName = ropeName;
					A.setProficiency(100);
					success = A.invoke(mob, commands, target, true, asLevel);
					if(success)
					{
						final Thief_Bind bA = (Thief_Bind)target.fetchEffect("Thief_Bind");
						if(bA != null)
						{
							bA.amountRemaining *= (3+(super.getXLEVELLevel(mob)/3));
							rope.setRawWornCode(0);
							target.moveItemTo(rope);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to hog tie <T-NAME> and fail(s)."));

		// return whether it worked
		return success;
	}
}
