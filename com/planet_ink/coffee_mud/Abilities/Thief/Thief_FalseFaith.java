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
   Copyright 2020-2022 Bo Zimmerman

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
public class Thief_FalseFaith extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_FalseFaith";
	}

	private final static String localizedName = CMLib.lang().L("False Faith");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay1 = CMLib.lang().L("(False Atheism)");
	private final static String localizedStaticDisplay2 = "(False @x1-ism)";

	@Override
	public String displayText()
	{
		if(text().length()>0)
		{
			if(text().equals("NONE"))
				return localizedStaticDisplay1;
			else
				return L(localizedStaticDisplay2,text());
		}
		else
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

	private static final String[] triggerStrings =I(new String[] {"FALSEFAITH","FFAITH"});
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
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(text().length()>0)
		{
			if(text().equals("NONE"))
				affectableStats.setWorshipCharID("");
			else
				affectableStats.setWorshipCharID(text());
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_HOLYEVENT)
		&&(msg.source() instanceof Deity)
		&&(msg.othersMessage()!=null)
		&&(msg.target()==affected)
		&&((msg.othersMessage().equalsIgnoreCase(Deity.HolyEvent.REBUKE.toString()))
			||(msg.othersMessage().equalsIgnoreCase(Deity.HolyEvent.DISAPPOINTED.toString()))))
		{
			msg.source().tell(L("That minion is beyond your disappointment."));
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		if(commands.size()==0)
		{
			mob.tell(L("You must specify either a deity to falsely believef in, STOP to remove your falsity, or NONE to pretend to be an atheist."));
			return false;
		}

		String deityName = CMParms.combine(commands,0);
		final Deity deityToConvertToM=CMLib.map().getDeity(deityName);
		if(deityToConvertToM==null)
		{
			if(deityName.equalsIgnoreCase("none"))
				deityName="NONE";
			else
			if(deityName.equalsIgnoreCase("stop"))
				deityName="";
			else
			{
				mob.tell(L("'@x1' is not a known deity.  Try DEITIES, NONE, or STOP.",deityName));
				return false;
			}
		}
		else
			deityName=deityToConvertToM.Name();
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> turn(s) <S-YOUPOSS> beliefs on their head."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability mA=target.fetchEffect(ID());
				if((mA!=null)
					&&(deityName.equalsIgnoreCase(target.baseCharStats().getWorshipCharID())
						||((deityName.equals("NONE"))&&(target.baseCharStats().getWorshipCharID().length()==0))
						||(deityName.length()==0)))
				{
					mA.unInvoke();
					target.delEffect(mA);
				}
				else
				{
					if(mA==null)
						mA=beneficialAffect(mob,target,asLevel,0);
					mA.setMiscText(deityName);
					mA.makeLongLasting();
					mob.recoverCharStats();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> turn(s) away and then back, but look(s) the same."));

		return success;
	}
}
