package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_Absorption extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Absorption";
	}

	private final static String localizedName = CMLib.lang().L("Absorption");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Absorption)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_VEXING;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	protected Ability absorbed=null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB M=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(absorbed!=null)&&(M!=null))
		{
			M.delAbility(absorbed);
			M.tell(L("You forget all about @x1.",absorbed.name()));
			absorbed=null;
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		if(target==mob)
		{
			mob.tell(L("Umm.. ok. Done."));
			return false;
		}
		final Prayer_Absorption old=(Prayer_Absorption)mob.fetchEffect(ID());
		if(old!=null)
		{
			if(old.absorbed!=null)
				mob.tell(L("You have already absorbed @x1 from someone.",old.absorbed.name()));
			else
				mob.tell(L("You have already absorbed a skill from someone."));
			return false;
		}

		absorbed=null;
		int tries=0;
		while((absorbed==null)&&((++tries)<100))
		{
			absorbed=target.fetchRandomAbility();
			if(absorbed==null)
				break;
			if(mob.fetchAbility(absorbed.ID())!=null)
				absorbed=null;
			else
			if(absorbed.isAutoInvoked())
				absorbed=null;
			else
			if(CMLib.ableMapper().qualifyingLevel(mob,absorbed)>0)
				absorbed=null;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(absorbed!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 for some of <T-YOUPOSS> knowledge!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(absorbed.ID());
				absorbed=(Ability)absorbed.copyOf();
				absorbed.setSavable(false);
				mob.addAbility(absorbed);
				mob.tell(L("You have absorbed @x1!",absorbed.name()));
				beneficialAffect(mob,mob,asLevel,15);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for some of <T-YOUPOSS> knowledge, but <S-HIS-HER> plea is not answered.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
