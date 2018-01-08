package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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

public class Prayer_ReligiousDoubt extends Prayer
{
	public static final long DOUBT_TIME=TimeManager.MILI_HOUR;

	@Override
	public String ID()
	{
		return "Prayer_ReligiousDoubt";
	}

	private final static String localizedName = CMLib.lang().L("Religious Doubt");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public String displayText()
	{
		if(otherSide)
			return "";
		return "(Religious Doubt)";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected int tickUp=0;
	protected boolean otherSide=false;

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(super.canBeUninvoked())
		{
			if(!otherSide)
				affectableStats.setStat(CharStats.STAT_FAITH,affectableStats.getStat(CharStats.STAT_FAITH)-100);
			else
				affectableStats.setStat(CharStats.STAT_FAITH,affectableStats.getStat(CharStats.STAT_FAITH)+100);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(super.canBeUninvoked()))
		{
			final boolean oldOther=otherSide;
			otherSide=(++tickUp)>tickDown;
			if((oldOther!=otherSide)&&(affected instanceof MOB))
				((MOB)affected).recoverCharStats();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(otherSide)
			return true;
		if(msg.target()==affected)
		{
			if(!(affected instanceof MOB))
				return true;
			if((msg.source()!=msg.target())
			&&(msg.tool() instanceof Ability)
			&&(msg.tool().ID().equalsIgnoreCase("Skill_Convert")))
			{
				msg.source().tell((MOB)msg.target(),null,null,L("<S-NAME> is not interested in hearing your religious beliefs."));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 for <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> questioning <S-HIS-HER> faith, but does not seem convinced yet."));
				beneficialAffect(mob,target,asLevel,(int)(DOUBT_TIME/CMProps.getTickMillis()));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for <T-NAMESELF>, but the magic fades.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
