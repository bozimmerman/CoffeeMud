package com.planet_ink.coffee_mud.Abilities.Poisons;
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
   Copyright 2022-2024 Bo Zimmerman

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
public class Poison_Anesthesia extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Anesthesia";
	}

	private final static String localizedName = CMLib.lang().L("Anesthesia");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Anesthetized)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	static final char[] PARENS={'(',')'};

	private static final String[] triggerStrings =I(new String[] {"POISONANESTHESIA"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 0;
	} // 0 means no adjustment!

	@Override
	protected int POISON_DELAY()
	{
		return 2;
	}

	@Override
	protected String POISON_DONE()
	{
		return "The numbing feeling runs its course.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> seem(s) numb.^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> anesthetize(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to anesthetize <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return 0;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)+(int)Math.round(rank));
	}

	protected String randomHurt(final char lastC)
	{
		final String word;
		switch(CMLib.dice().roll(1, 10,0))
		{
		default:
			word = "affect(s)";
			break;
		case 2:
			word = "pressure(s)";
			break;
		case 3:
			word = "slightly move(s)";
			break;
		case 4:
			word = "touch(es)";
			break;
		case 5:
			word = "something(s)";
			break;
		case 6:
			word = "damage(s)";
			break;
		case 7:
			word = "compromise(s)";
			break;
		case 8:
			word = "push(es)";
			break;
		case 9:
			word = "do(es)";
			break;
		case 10:
			word = "affect(s)";
			break;
		}
		if(lastC=='s')
			return L(CMStrings.deleteAllofAny(word,PARENS));
		else
		if(lastC=='-')
			return L(CMStrings.deleteAllofAny(word,PARENS));
		else
			return L(word);
	}

	@Override
	protected int POISON_ADDICTION_CHANCE()
	{
		return 2;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.source() == affected)
		{
			if((msg.sourceMinor()==CMMsg.TYP_NOISE)
			&&(msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(), Ability.FLAG_TORTURING)))
				return false;
			if(msg.sourceMinor()==CMMsg.TYP_FLEE)
			{
				msg.source().tell(L("You don't see what you should flee."));
				return false;
			}
			if((msg.sourceMinor()==CMMsg.TYP_OK_VISUAL)
			&&(msg.sourceMessage().length()>0))
			{
				final String poisonEnd = CMLib.protocol().msp("poisoned.wav",10);
				msg.setSourceMessage(CMStrings.replaceFirst(msg.sourceMessage(), poisonEnd, ""));
			}
		}
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.targetMessage()!=null))
		{
			final int x = msg.targetMessage().indexOf("<DAMAGE");
			if(x >= 0)
			{
				final int y=msg.targetMessage().indexOf('>',x);
				final String damnWord = msg.targetMessage().substring(x,y+1);
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), damnWord, randomHurt(damnWord.charAt(damnWord.length()-2))));
			}
		}
		return super.okMessage(myHost, msg);
	}
}
