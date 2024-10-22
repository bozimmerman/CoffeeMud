package com.planet_ink.coffee_mud.Commands;
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
public class DieRoll extends StdCommand
{
	public DieRoll()
	{
	}

	private final String[]	access	= I(new String[] { "DIEROLL", "DROLL" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private enum DieRollDifficulty
	{
		RUDIMENTARY(-4),
		TRIVIAL(-3),
		SIMPLE(-2),
		EASY(-1),
		NORMAL(0),
		HARD(1),
		DIFFICULT(2),
		STRENUOUS(3),
		ARDUOUS(4),
		MIRACULOUS(5),
		IMPROBABLE(6),
		INCALCULABLE(7),
		IMPOSSIBLE(8);

		public int adj;
		private DieRollDifficulty(final int adj)
		{
			this.adj=adj;
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String errMsg=null;
		if(commands.size()<2)
		{
			errMsg=L("DIEROLL what?  Try 1D20, D6, STRENGTH, SAVE VS COLD or something else.");
			mob.tell(errMsg);
			return false;
		}
		Boolean result = null;
		commands.remove(0);
		final String s = CMStrings.removeAllButLettersAndDigits(CMParms.combine(commands).trim()).toUpperCase();
		if((s.length()>1)
		&&(s.charAt(0)=='D')
		&&(CMath.isInteger(s.substring(1))))
		{
			final int die = CMath.s_int(s.substring(1));
			final int roll = CMLib.dice().roll(1, die, 0);
			mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> make(s) a @x1 on a 1D@x2 roll.",""+roll,""+die));
			result=Boolean.TRUE;
		}
		else
		if((s.length()>2)
		&& Character.isDigit(s.charAt(0)))
		{
			final int x = s.indexOf("D");
			if((x>0)
			&&(CMath.isInteger(s.substring(0,x)))
			&&(CMath.isInteger(s.substring(x+1))))
			{
				final int num = CMath.s_int(s.substring(0,x));
				final int die = CMath.s_int(s.substring(x+1));
				final int roll = CMLib.dice().roll(num, die, 0);
				mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> make(s) a @x1 on a @x2D@x3 roll.",""+roll,""+num,""+die));
				result=Boolean.TRUE;
			}
			else
				mob.tell(L("@x1 is not a valid stat or die roll.",s));
		}
		else
		{
			DieRollDifficulty diff = DieRollDifficulty.NORMAL;
			if(commands.size()>1)
			{
				final DieRollDifficulty d = (DieRollDifficulty)CMath.s_valueOf(DieRollDifficulty.class, commands.get(commands.size()-1).toUpperCase().trim());
				if(d != null)
				{
					diff = d;
					commands.remove(commands.size()-1);
				}
			}
			final String chk = CMParms.combine(commands);
			int cstat = CharStats.CODES.findWhole(chk, true);
			if(cstat < 0)
				cstat = CharStats.CODES.findWhole(chk, false);
			if(cstat >= 0)
			{
				if(CharStats.CODES.isBASE(cstat))
				{
					final int roll = CMLib.dice().roll(1, 25, 0);
					result = Boolean.valueOf(roll < mob.charStats().getStat(cstat)-diff.adj);
				}
				else
				if(CMParms.indexOf(CharStats.CODES.SAVING_THROWS(),cstat)>=0)
				{
					final int roll = CMLib.dice().roll(1, 100, 0);
					result = Boolean.valueOf(roll < mob.charStats().getSave(cstat)-(diff.adj*10));
				}
				if(result == null)
					mob.tell(L("@x1 is a valid stat, but not supported for die rolls.",CharStats.CODES.NAME(cstat).toUpperCase()));
				else
				if(result.booleanValue())
				{
					mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,
							L("<S-NAME> make(s) a(n) @x1 @x2 roll.",diff.name().toLowerCase(),CharStats.CODES.DESC(cstat).toLowerCase()));
				}
				else
				if(!result.booleanValue())
				{
					mob.location().show(mob,null,null,CMMsg.MSG_OK_VISUAL,
							L("<S-NAME> fail(s) a(n) @x1 @x2 roll.",diff.name().toLowerCase(),CharStats.CODES.DESC(cstat).toLowerCase()));
				}
			}
			else
				mob.tell(L("@x1 is not a valid stat to roll against.",chk));
			if(result == null)
			{
				final StringBuilder v = new StringBuilder(L("Valid stats include: "));
				for(final int cd : CharStats.CODES.ALLCODES())
				{
					if((CharStats.CODES.isBASE(cd))
					||(CMParms.indexOf(CharStats.CODES.SAVING_THROWS(),cd)>=0))
						v.append(CharStats.CODES.NAME(cd)).append(", ");
				}
				v.setLength(v.length()-2);
				v.append(".  Difficulties include: ");
				for(final DieRollDifficulty d : DieRollDifficulty.values())
					v.append(d.name()).append(", ");
				v.setLength(v.length()-2);
				v.append(".");
				mob.tell(v.toString());
			}
		}
		return false;
	}
}
