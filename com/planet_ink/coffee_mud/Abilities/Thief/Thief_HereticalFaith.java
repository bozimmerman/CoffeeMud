package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Songs.Skill_Disguise;
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
   Copyright 2022-2025 Bo Zimmerman

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
public class Thief_HereticalFaith extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_HereticalFaith";
	}

	private final static String localizedName = CMLib.lang().L("Heretical Faith");

	@Override
	public String name()
	{
		return localizedName;
	}

	//private final static String localizedDisplay = CMLib.lang().L("(Heretical Faith)");

	@Override
	public String displayText()
	{
		return "";
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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"HERETICALFAITH", "HFAITH"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DECEPTIVE;
	}

	@Override
	public int abilityCode()
	{
		if(invoker()==null)
			return 0;
		return super.getXLEVELLevel(invoker());
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setDeityName(text());
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		String deityName = CMParms.combine(commands,0);
		if(commands.size()==0)
		{
			mob.tell(L("You must specify either a deity to mask yourself in, STOP to remove your mask, or NONE to mask as an atheist."));
			return false;
		}

		if(deityName.equalsIgnoreCase("none"))
			deityName="NONE";
		else
		if(deityName.equalsIgnoreCase("stop"))
			deityName="";
		else
			deityName=CMStrings.capitalizeAllFirstLettersAndLower(deityName);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			Ability mA=target.fetchEffect(ID());
			final boolean unmask=
				((mA!=null)
				&&(deityName.equalsIgnoreCase(target.baseCharStats().getWorshipCharID())
						||((deityName.equals("NONE"))&&(target.baseCharStats().getWorshipCharID().length()==0))
						||(deityName.length()==0)));

			final String mmsg;
			if(unmask)
				mmsg = L("<S-NAME> renounce(s) <S-YOUPOSS> new religeous beliefs.");
			else
				mmsg = L("<S-NAME> <S-HAS-HAVE> a conversion experience, altering <S-HIS-HER> religeous beliefs.");
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),mmsg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(unmask)
				{
					if(mA!=null)
					{
						mA.unInvoke();
						target.delEffect(mA);
					}
				}
				else
				{
					if(mA==null)
						mA=beneficialAffect(mob,target,asLevel,0);
					if(mA!=null)
					{
						if(deityName.equals("NONE"))
							deityName="";
						mA.setMiscText(deityName);
						mA.makeLongLasting();
					}
					mob.recoverCharStats();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> turn(s) away, <S-HAS-HAVE> a conversion experience, but feel(s) the same."));

		return success;
	}
}
