package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2019-2025 Bo Zimmerman

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
public class Disease_Tourettes extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Tourettes";
	}

	private final static String localizedName = CMLib.lang().L("Tourettes");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Tourettes)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 9;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return 999999;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 8;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel more at ease.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> contract(s) Tourettes Syndrome.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		if(!(affected instanceof MOB))
			return "";
		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if(R==null)
			return "";
		final MOB target=R.fetchRandomInhabitant();
		if((!mob.amDead())
		&&(CMLib.flags().canSpeak(mob)))
		{
			String say=L("Penis wrinkle!");
			switch(CMLib.dice().roll(1,30,0))
			{
			case 1:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = L("You are a very bad @x1!",target.charStats().displayClassName());
				else
					say = L("ARGH!");
				break;
			case 2:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = L("I think all @x1s are stupid!",target.charStats().raceName());
				else
					say = L("FLARK!");
				break;
			case 3:
				say = L("Damn flark!");
				break;
			case 4:
				say = L("Squeegee!");
				break;
			case 5:
				say = L("Ding dong!");
				break;
			case 6:
				say = L("Goober!");
				break;
			case 7:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = L("Noodle @x1!",target.charStats().boygirl());
				else
					say = L("NOODLE!!!!!");
				break;
			case 8:
				say = L("Groin scratcher!");
				break;
			case 9:
				say = L("Geek!");
				break;
			case 10:
				say = L("Dork!");
				break;
			case 11:
				say = L("Orc kisser!");
				break;
			case 12:
				say = L("Jerk!");
				break;
			case 13:
				say = L("Tuddleworm!");
				break;
			case 14:
				say = L("Poopie diaper!");
				break;
			case 15:
				say = L("Panty stain!");
				break;
			case 16:
				say = L("Blah blah blah blah blah!");
				break;
			case 17:
				say = L("Hairpit sniffer!");
				break;
			case 18:
				say = L("Gluteus maximus cavity!");
				break;
			case 19:
				say = L("Uncle copulator!");
				break;
			case 20:
				say = L("Toe jam eater!");
				break;
			case 21:
				say = L("Partial excrement!");
				break;
			case 22:
				say = L("Female dog!");
				break;
			case 23:
				say = L("Illegitimate offspring!");
				break;
			case 24:
				say = L("You are overweight!");
				break;
			case 25:
				say = "You smell funny!";
				break;
			case 26:
				say = "You aren't very smart!";
				break;
			case 27:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = L("You.. you.. ah nevermind.");
				else
					say = L("Whatever");
				break;
			case 28:
				say = L("Yokle!");
				break;
			case 29:
				say = L("Ugly head!");
				break;
			case 30:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = L("Goop@x1!",target.charStats().boygirl());
				else
					say = L("Goop!");
				break;
			}
			return say;
		}
		return "";
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Mental compulsion disorder: Tourettes Syndrome.";
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected==null)
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			final String aff=this.DISEASE_AFFECT();
			if((aff != null)&&(aff.length()>0))
				CMLib.commands().forceStandardCommand(mob, "Yell", new XVector<String>("YELL",aff));
			return true;
		}
		return true;
	}
}
