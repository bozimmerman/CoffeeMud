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
   Copyright 2001-2025 Bo Zimmerman

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
public class Prayer_Sacrifice extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Sacrifice";
	}

	private final static String localizedName = CMLib.lang().L("Sacrifice");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_DEATHLORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	public static Item getBody(final Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(!((DeadBody)I).isPlayerCorpse())
			&&(((DeadBody)I).getMobName().length()>0))
				return I;
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof DeadBody))
		   ||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell(L("You may only sacrifice the dead."));
			return false;
		}

		if((((DeadBody)target).isPlayerCorpse())
		&&(!((DeadBody)target).getMobName().equals(mob.Name()))
		&&(((DeadBody)target).hasContent()))
		{
			mob.tell(L("You are not allowed to sacrifice that corpse."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?L("<T-NAME> sacrifice(s) <T-HIM-HERSELF>."):
						L("^S<S-NAME> sacrifice(s) <T-NAMESELF> to @x1.^?",hisHerDiety(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(CMLib.flags().isGood(mob)||CMLib.flags().isLawful(mob))
				{
					double exp=5.0;
					final int levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
					final int levelDiff=mob.phyStats().level()-target.phyStats().level();
					if(levelDiff>levelLimit)
						exp=0.0;
					if(exp>0)
						CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,(int)Math.round(exp)+(super.getXPCOSTLevel(mob)), false);
				}
				target.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":L("<S-NAME> attempt(s) to sacrifice <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
