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
			String say="Penis wrinkle!";
			switch(CMLib.dice().roll(1,30,0))
			{
			case 1:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = "You are a very bad " + target.charStats().displayClassName() + "!";
				else
					say = "ARGH!";
				break;
			case 2:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = "I think all " + target.charStats().raceName() + "s are stupid!";
				else
					say = "FLARK!";
				break;
			case 3:
				say = "Damn flark!";
				break;
			case 4:
				say = "Squeegee!";
				break;
			case 5:
				say = "Ding dong!";
				break;
			case 6:
				say = "Goober!";
				break;
			case 7:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = "Noodle" + ((target.charStats().getStat(CharStats.STAT_GENDER) == 'M') ? "boy" : "girl") + "!";
				else
					say = "NOODLE!!!!!";
				break;
			case 8:
				say = "Groin scratcher!";
				break;
			case 9:
				say = "Geek!";
				break;
			case 10:
				say = "Dork!";
				break;
			case 11:
				say = "Orc kisser!";
				break;
			case 12:
				say = "Jerk!";
				break;
			case 13:
				say = "Tuddleworm!";
				break;
			case 14:
				say = "Poopie diaper!";
				break;
			case 15:
				say = "Panty stain!";
				break;
			case 16:
				say = "Blah blah blah blah blah!";
				break;
			case 17:
				say = "Hairpit sniffer!";
				break;
			case 18:
				say = "Gluteous maximus cavity!";
				break;
			case 19:
				say = "Uncle copulator!";
				break;
			case 20:
				say = "Toe jam eater!";
				break;
			case 21:
				say = "Partial excrement!";
				break;
			case 22:
				say = "Female dog!";
				break;
			case 23:
				say = "Illegitimate offspring!";
				break;
			case 24:
				say = "You are overweight!";
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
					say = "You.. you.. ah nevermind.";
				else
					say = "Whatever";
				break;
			case 28:
				say = "Yokle!";
				break;
			case 29:
				say = "Ugly head!";
				break;
			case 30:
				if((target!=null)
				&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
				&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
				&&(CMLib.flags().canBeSeenBy(target,mob)))
					say = "Goop" + ((target.charStats().getStat(CharStats.STAT_GENDER) == 'M') ? "boy" : "girl") + "!";
				else
					say = "Goop!";
				break;
			}
			return L(say);
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
