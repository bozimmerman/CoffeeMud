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
   Copyright 2024-2025 Bo Zimmerman

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
public class Thief_NameUrchin extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_NameUrchin";
	}

	private final static String localizedName = CMLib.lang().L("Name Urchin");

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
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;
	}

	private static final String[] triggerStrings =I(new String[] {"NAMEURCHIN"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectedStats)
	{
		super.affectPhyStats(affected,affectedStats);
		if(text().length()>0)
			affectedStats.setName(text());
	}

	public volatile int nameCheckCtr = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(++nameCheckCtr > 10)
		{
			nameCheckCtr = 0;
			final Physical P = affected;
			if((P instanceof MOB)
			&&(((MOB)P).amFollowing()==null)
			&&(CMLib.flags().isInTheGame((MOB)P,true))
			&&(((MOB)P).getLiegeID()==null)||(((MOB)P).getLiegeID().trim().length()==0))
			{
				P.delEffect(P.fetchEffect(ID()));
				P.recoverPhyStats();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		if(commands.size()<2)
		{
			mob.tell(L("You must specify the urchin, and a name to give him or her (or the name 'random' for a random one)."));
			return false;
		}
		String myName=CMStrings.capitalizeAndLower(commands.remove(commands.size()-1)).trim();
		if(myName.length()==0)
		{
			mob.tell(L("You must specify a name."));
			return false;
		}
		if(myName.indexOf(' ')>=0)
		{
			mob.tell(L("Your name may not contain a space."));
			return false;
		}
		if(CMStrings.containsAny(myName, "!@#$%^&*()+<>.,'\";:) {}[]|\\/?~`".toCharArray()))
		{
			mob.tell(L("Your name contains illegal characters."));
			return false;
		}
		myName = CMLib.coffeeFilter().secondaryUserInputFilter(myName);
		if(myName.equalsIgnoreCase("random"))
		{
			final List<String> names = Resources.getFileLineVector(Resources.getFileResource("skills/urchins.txt", true));
			if(names.size()==0)
			{
				mob.tell(L("You need to specify a name."));
				return false;
			}
			myName=names.get(CMLib.dice().roll(1,names.size(),-1));
		}

		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!target.getLiegeID().equals(mob.Name())
		||(target.fetchBehavior("Thiefness")==null)
		||(target.fetchBehavior("Scavenger")==null)
		||(!Thief_MyUrchins.isMyUrchin(target, mob))))
		{
			mob.tell(L("@x1 is not one of your urchins.",target.name(mob)));
			return false;
		}
		if(target.name().indexOf(myName)>0)
		{
			mob.tell(L("That's already @x1's name.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int experienceToLose=getXPCOSTAdjustment(mob,100);
		final int amt = -CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
		if(amt > 0)
			mob.tell(L("You lose @x1 xp in the attempt.",""+amt));
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?"":L("<S-NAME> name(s) <T-NAME> `@x1`.",myName);
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND,auto?"":str,str,str);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				final Ability A=(Ability)this.copyOf();
				A.setMiscText(myName);
				target.addNonUninvokableEffect(A);
				try
				{
					target.setLiegeID(""); // prevent back-xp giving
					CMLib.leveler().postExperience(target,"ABILITY:"+ID(),null,null,1000, false);
				}
				finally
				{
					target.setLiegeID(mob.Name());
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to give <T-NAME> a new name, but <T-NAME> <T-IS-ARE> not having it."));

		// return whether it worked
		return success;
	}
}
