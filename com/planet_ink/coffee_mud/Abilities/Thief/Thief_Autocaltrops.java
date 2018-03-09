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
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_Autocaltrops extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Autocaltrops";
	}

	@Override
	public String displayText()
	{
		return L("(Autocaltropping)");
	}

	private final static String localizedName = CMLib.lang().L("AutoCaltrops");

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
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"AUTOCALTROPS"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected boolean noRepeat=false;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(!noRepeat)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(((MOB)affected).location()!=null))
			dropem(msg.source(),(Room)msg.target());
	}

	public void dropem(MOB mob, Room R)
	{
		Ability A=mob.fetchAbility("Thief_Caltrops");
		if(A==null)
		{
			A=CMClass.getAbility("Thief_Caltrops");
			A.setProficiency(100);
		}
		final int mana=mob.curState().getMana();
		final int movement=mob.curState().getMovement();
		A.invoke(mob,R,false,0);
		mob.curState().setMana(mana);
		mob.curState().setMana(movement);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(!((MOB)affected).amDead())&&(super.canBeUninvoked()))
			((MOB)affected).tell(L("You stop throwing down caltrops."));
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		if(target.fetchEffect(ID())!=null)
		{
			target.tell(L("You are no longer automatically dropping caltrops."));
			target.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if((!auto)&&(target.fetchAbility("Thief_Caltrops")==null))
		{
			target.tell(L("You don't know how to make and drop caltrops yet!"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			target.tell(L("You will now automatically drop caltrops around when you enter a room."));
			beneficialAffect(mob,target,asLevel,5+(3*getXLEVELLevel(mob)));
			dropem(target,target.location());
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to prepare some caltrops for quick dropping, but mess(es) up."));
		return success;
	}
}
