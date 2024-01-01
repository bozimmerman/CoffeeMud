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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2024 Bo Zimmerman

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
public class Prayer_EmpowerUnholyArmor extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_EmpowerUnholyArmor";
	}

	private final static String localizedName = CMLib.lang().L("Empower Unholy Armor");

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
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_BLESSING;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
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

		final long okLocs = Wearable.WORN_FEET|Wearable.WORN_LEGS|Wearable.WORN_WAIST|Wearable.WORN_TORSO|Wearable.WORN_ARMS
				|Wearable.WORN_LEFT_WRIST|Wearable.WORN_RIGHT_WRIST|Wearable.WORN_HANDS|Wearable.WORN_HEAD;
		if(!(target instanceof Armor))
		{
			mob.tell(mob,target,null,L("You can't empower <T-NAME> with this prayer!"));
			return false;
		}
		final long rawProp =((Armor)target).rawProperLocationBitmap();
		if((target instanceof Shield)
		||((rawProp&okLocs)==0)
		||((rawProp&(okLocs|Wearable.WORN_HELD))!=((Armor)target).rawProperLocationBitmap()))
		{
			mob.tell(mob,target,null,L("You can't empower something worn like <T-NAME> with this prayer!"));
			return false;
		}

		if(target.phyStats().ability()>4)
		{
			mob.tell(L("@x1 cannot be empowered further.",target.name(mob)));
			return false;
		}

		final String deityName=mob.charStats().getWorshipCharID();
		if(deityName.length()==0)
		{
			mob.tell(L("You must worship a deity to begin the empowering."));
			return false;
		}

		if(!Prayer.checkInfusionMismatch(mob, target))
		{
			mob.tell(L("You can not empower that repulsive weapon."));
			return false;
		}
		if(CMLib.flags().isGood(target))
		{
			mob.tell(L("You can not empower that repulsive weapon."));
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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> hold(s) <T-NAMESELF> above <S-HIS-HER> head and @x1.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glows!"));
				target.basePhyStats().setAbility(target.basePhyStats().ability()+1);
				target.basePhyStats().setLevel(target.basePhyStats().level()+3);
				target.basePhyStats().setDisposition(target.basePhyStats().disposition() & ~PhyStats.IS_GOOD);
				target.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS|PhyStats.IS_EVIL);
				final Ability zappA=target.fetchEffect("Prop_WearZapper");
				if(zappA==null)
				{
					final Ability A=CMClass.getAbility("Prop_WearZapper");
					A.setMiscText("+FACTION -GOOD -NEUTRAL -DEITY \"+"+deityName.toUpperCase().trim()+"\"");
					target.addNonUninvokableEffect(A);
				}
				else
				{
					if((zappA.text().indexOf("-NEUTRAL")<0)||(zappA.text().indexOf("-GOOD")<0))
						zappA.setMiscText(zappA.text()+" +FACTION -GOOD -NEUTRAL");
					if((zappA.text().indexOf("-DEITY")<0)||(zappA.text().indexOf("\"+"+deityName.toUpperCase().trim()+"\"")<0))
						zappA.setMiscText(zappA.text()+" -DEITY \"+"+deityName.toUpperCase().trim()+"\"");
				}
				target.recoverPhyStats();
				target.text();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> above <S-HIS-HER> head and @x1, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
