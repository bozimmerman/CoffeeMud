package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Spell_ShoddyAura extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ShoddyAura";
	}

	private final static String localizedName = CMLib.lang().L("Shoddy Aura");

	@Override
	public String name()
	{
		return localizedName;
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
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		//affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
		affectableStats.addAmbiance("(shoddy)");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		final Physical affected=this.affected;
		if(((msg.targetMinor() == CMMsg.TYP_SELL)
			||(msg.targetMinor() == CMMsg.TYP_VALUE))
		&&((msg.tool()==affected)
			||((affected instanceof Container)
				&&(msg.tool() instanceof Item)
				&&(((Item)msg.tool()).ultimateContainer(affected)==affected))))
		{
			if(msg.target() instanceof MOB)
				CMLib.commands().postSay((MOB)msg.target(),msg.source(),L("I'm not interested."),true,false);
			else
				msg.source().tell(L("The buyer does not look interested."));
			return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.target()==affected)
		&&((msg.sourceMinor()==CMMsg.TYP_LOOK)||(msg.sourceMinor()==CMMsg.TYP_EXAMINE)))
		{
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
					  CMMsg.MSG_OK_VISUAL,L("\n\rIt appears to be shoddily made and in crummy condition.\n\r",affected.name(msg.source())),
					  CMMsg.NO_EFFECT,null,
					  CMMsg.NO_EFFECT,null));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Physical target=super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		if(!(target instanceof Item))
		{
			mob.tell(mob,target,null,L("You can only alter sling bullets with this spell, which <T-NAME> is not."));
			return false;
		}

		if(target.fetchEffect("Spell_ShoddyAura")!=null)
		{
			mob.tell(mob,target,null,L("<T-NAME> already has a shoddy aura."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L("^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> appear(s) to become shoddy and bad."));
				final TimeClock C=CMLib.time().localClock(mob);
				final long ticksPerMudday=C.getHoursInDay() * CMProps.getTicksPerHour();
				final Ability A=super.beneficialAffect(mob, target, asLevel, (int)ticksPerMudday*2);
				if(A!=null)
				{
					A.setMiscText(""+((Item)target).baseGoldValue());
					target.addNonUninvokableEffect(A);
				}
				target.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, looking very frustrated."));
		// return whether it worked
		return success;
	}
}
