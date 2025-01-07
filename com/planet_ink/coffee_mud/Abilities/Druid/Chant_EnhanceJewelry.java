package com.planet_ink.coffee_mud.Abilities.Druid;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_EnhanceJewelry extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_EnhanceJewelry";
	}

	private final static String localizedName = CMLib.lang().L("Enhance Jewelry");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Armor))
		{
			mob.tell(mob,target,null,L("You can't enhance <T-NAME> with this magic!"));
			return false;
		}
		final long goodCheck = ((Armor)target).rawProperLocationBitmap()
				& ( Wearable.WORN_EARS | Wearable.WORN_RIGHT_FINGER | Wearable.WORN_LEFT_FINGER | Wearable.WORN_NECK | Wearable.WORN_LEFT_WRIST | Wearable.WORN_RIGHT_WRIST);
		if(goodCheck == 0)
		{
			mob.tell(L("@x1 can not be enhanced with this magic, as it is not worn on the ears, fingers, neck, or wrist."));
			return false;
		}
		final long badCheck = ((Armor)target).rawProperLocationBitmap()
				& ( Wearable.WORN_TORSO | Wearable.WORN_ARMS | Wearable.WORN_FEET | Wearable.WORN_ABOUT_BODY | Wearable.WORN_HANDS | Wearable.WORN_HEAD);
		if(badCheck != 0)
		{
			mob.tell(L("@x1 can not be enhanced with this magic, as it is not worn exclusively on the ears, fingers, neck, or wrist."));
			return false;
		}

		if(target.phyStats().ability()>=5+(super.getXLEVELLevel(mob)/2))
		{
			mob.tell(L("@x1 cannot be enhanced further.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int experienceToLose=getXPCOSTAdjustment(mob,50);
		experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
		mob.tell(L("The effort causes you to lose @x1 experience.",""+experienceToLose));

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":L("^S<S-NAME> hold(s) <T-NAMESELF> and chant(s) to it.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glows!"));
				target.basePhyStats().setAbility(target.basePhyStats().ability()+1);
				if(target instanceof Armor)
					((Armor)target).setLayerAttributes((short)(((Armor)target).getLayerAttributes()&(~Armor.LAYERMASK_MULTIWEAR)));

				target.basePhyStats().setLevel(target.basePhyStats().level()+3);
				target.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> tightly and chant(s), but fail(s)."));

		// return whether it worked
		return success;
	}
}
