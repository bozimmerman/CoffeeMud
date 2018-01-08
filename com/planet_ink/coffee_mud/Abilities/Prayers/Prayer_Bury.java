package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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

public class Prayer_Bury extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Bury";
	}

	private final static String localizedName = CMLib.lang().L("Bury");

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
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=Prayer_Sacrifice.getBody(mob.location());
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof DeadBody))
		   ||(target.rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell(L("You may only bury the dead."));
			return false;
		}
		if((((DeadBody)target).isPlayerCorpse())&&(!((DeadBody)target).getMobName().equals(mob.Name())))
		{
			mob.tell(L("You are not allowed to bury a players corpse."));
			return false;
		}
		Item hole=mob.location().findItem("HoleInTheGround");
		if((hole!=null)&&(!hole.text().equalsIgnoreCase(mob.Name())))
		{
			mob.tell(L("This prayer will not work on this previously used burial ground."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("^S<T-NAME> bur(ys) <T-HIM-HERSELF>.^?"):L("^S<S-NAME> bur(ys) <T-NAMESELF> in the name of @x1.^?",hisHerDiety(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(CMLib.flags().isNeutral(mob))
				{
					double exp=5.0;
					final int levelLimit=CMProps.getIntVar(CMProps.Int.EXPRATE);
					final int levelDiff=mob.phyStats().level()-target.phyStats().level();
					if(levelDiff>levelLimit)
						exp=0.0;
					if(exp>0.0)
						CMLib.leveler().postExperience(mob,null,null,(int)Math.round(exp)+super.getXPCOSTLevel(mob),false);
				}
				if(hole==null)
				{
					final CMMsg holeMsg=CMClass.getMsg(mob, mob.location(),null,CMMsg.MSG_DIG|CMMsg.MASK_ALWAYS, null);
					mob.location().send(mob,holeMsg);
					hole=mob.location().findItem("HoleInTheGround");
				}
				hole.basePhyStats().setDisposition(hole.basePhyStats().disposition()|PhyStats.IS_HIDDEN);
				hole.recoverPhyStats();
				if(!mob.location().isContent(target))
					mob.location().moveItemTo(hole, Expire.Player_Drop);
				else
					target.setContainer((Container)hole);
				CMLib.flags().setGettable(target,false);
				mob.location().recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to bury <T-NAMESELF>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
