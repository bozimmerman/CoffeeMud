package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2005-2025 Bo Zimmerman

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
public class Unbinding extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Unbinding";
	}

	private final static String localizedName = CMLib.lang().L("Unbinding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"UNBIND","UNTIE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_BINDING;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	protected Physical	found		= null;
	protected Ability	removing	= null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==3)
			{
				List<Ability> affects=null;
				if(found!=null)
					affects=CMLib.flags().flaggedAffects(found,Ability.FLAG_BINDING);
				if((affects!=null)
				&&(affects.size()>0))
				{
					removing=affects.get(0);
					displayText=L("You are removing @x1 from @x2",removing.name(),found.name());
					verb=L("removing @x1 from @x2",removing.name(),found.name());
				}
				else
				{
					//final StringBuffer str=new StringBuffer(L("You can't seem to remove any of the bindings.\n\r"));
					//commonTell(mob,str.toString());
					unInvoke();
				}
			}
			else
			if((found!=null)&&(mob!=null))
			{
				final Room foundR=CMLib.map().roomLocation(found);
				if(foundR!=mob.location())
				{
					aborted=true;
					unInvoke();
				}
				if(!CMLib.flags().canBeSeenBy(found,mob))
				{
					aborted=true;
					unInvoke();
				}
				if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
				{
					aborted=true;
					unInvoke();
				}
				if((removing!=null)&&(found.fetchEffect(removing.ID())!=removing))
				{
					aborted=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((found!=null)
				&&(removing!=null)
				&&(!aborted))
				{
					removing.unInvoke();
					if(found.fetchEffect(removing.ID())==null)
						mob.location().show(mob,this,getActivityMessageType(),L("<S-NAME> manage(s) to remove @x1 from @x2.",removing.name(),found.name()));
					else
						mob.location().show(mob,this,getActivityMessageType(),L("<S-NAME> fail(s) to remove @x1 from @x2.",removing.name(),found.name()));
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		final Physical target=super.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if((!auto)&&(target==mob))
		{
			commonFaiL(mob,commands,"You can't unbind yourself!");
			return false;
		}
		if((!auto)&&mob.isInCombat())
		{
			commonFaiL(mob,commands,"Not while you are fighting!");
			return false;
		}
		final List<Ability> affects=CMLib.flags().flaggedAffects(target,Ability.FLAG_BINDING);
		if(affects.size()==0)
		{
			commonFaiL(mob,commands,"@x1 does not have any bindings you can remove.",target.name(mob));
			return false;
		}
		final Ability A=affects.get(0);

		verb=L("unbinding");
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int duration=CMLib.ableMapper().lowestQualifyingLevel(A.ID())-(CMLib.ableMapper().qualifyingLevel(mob,A)+(2*getXLEVELLevel(mob)));
		if(duration<5)
			duration=4;
		final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,L("<S-NAME> begin(s) to unbind <T-NAMESELF>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=target;
			verb=L("unbinding @x1",found.name());
			displayText=L("You are @x1",verb);
			final int difficulty = (A.invoker()==mob) ? 0 : -A.abilityCode();
			found=proficiencyCheck(mob,difficulty,auto)?found:null;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;

	}
}
