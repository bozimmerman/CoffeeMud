package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Skill_Joke extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Joke";
	}

	private final static String localizedName = CMLib.lang().L("Joke");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"JOKE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!CMLib.flags().canBeHeardSpeakingBy(mob,target)))
		{
			mob.tell(L("@x1 can't hear your words.",target.charStats().HeShe()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			if(auto)
				str=L("<T-NAME> remember(s) a joke!");
			else
			{
				final List<String> insultd=Resources.getFileLineVector(Resources.getFileResource("skills/insultd.txt",true));
				final List<String> insulto=Resources.getFileLineVector(Resources.getFileResource("skills/insulto.txt",true));
				final String[] ob=new String[5];
				final String[] de=new String[5];
				for (int cnt=1; cnt<4; cnt++)
				{
					ob[cnt] = insulto.get(CMLib.dice().roll(1,insulto.size(),-1));
					de[cnt] = insultd.get(CMLib.dice().roll(1,insultd.size(),-1));
				}
				String joke=null;
				switch(CMLib.dice().roll(1,7,0))
				{
				case 1:
					joke=  "Q: What do you get if you cross a "+ob[1]+" with a "+ob[2]+"?\n\r"
						  +"A: "+de[1]+" "+ob[3]+"!";
					break;
				case 2:
					joke=  "What did the "+ob[1]+" say to the "+ob[2]+"?\n\r"
						  +"'You are "+de[1]+" "+ob[3]+"!'";
					break;
				case 3:
				{
					final String targetName=target.name();
					String jokerName=mob.name();
					if(mob==target)
						jokerName="Someone";
					joke= jokerName+": 'Knock, knock!'\n\r"
						 +targetName+": 'Who's there?'\n\r"
						 +jokerName+": 'A "+ob[1]+".'\n\r"
						 +targetName+": 'A "+ob[1]+" who?'\n\r"
						 +jokerName+": '"+de[1]+" "+ob[2]+"!'";
					break;
				}
				case 4:
					joke= "Q: What's the difference between a "+ob[1]+" and a "+ob[2]+"?\n\r"
						 +"A: A "+ob[1]+" is "+de[1]+" "+ob[3]+"!";
					break;
				case 5:
					joke= "Q: What did the big "+ob[1]+" say to the little "+ob[1]+"?\n\r"
						 +"A: 'You are "+de[1]+" "+ob[2]+"!'";
					break;
				case 6:
					joke= "Q: What do you call "+de[1]+" "+ob[1]+" without "+de[2]+" "+ob[2]+"?\n\r"
						 +"A: "+CMStrings.capitalizeAndLower(de[3])+" "+ob[3]+"!";
					break;
				case 7:
					joke= "Q: When is "+de[1]+" "+ob[1]+" not "+de[1]+" "+ob[1]+"?\n\r"
						 +"A: When it's "+de[2]+" "+ob[2]+"!'";
					break;
				}
				str=L("<S-NAME> joke(s) to <T-NAMESELF>:\n\r@x1",joke);
			}
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK|(auto?CMMsg.MASK_ALWAYS:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(CMLib.dice().rollPercentage()<25)
				{
					final Ability A=CMClass.getAbility("Spell_Laughter");
					A.invoke(mob,target,true,asLevel);
				}
				else
				{
					final Ability A=CMClass.getAbility("Disease_Giggles");
					A.invoke(mob,target,true,asLevel);
				}

			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to think up a joke for <T-NAMESELF>, but fail(s)."));

		return success;
	}

}
