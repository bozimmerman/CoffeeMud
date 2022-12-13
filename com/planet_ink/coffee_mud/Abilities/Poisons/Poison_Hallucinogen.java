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
   Copyright 2022-2022 Bo Zimmerman

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
public class Poison_Hallucinogen extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Hallucinogen";
	}

	private final static String localizedName = CMLib.lang().L("Hallucinogen");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Drugged)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	static final char[] PARENS={'(',')'};

	private static final String[] triggerStrings =I(new String[] {"POISONHALLUCINOGEN"});
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
		return "The hallucinations subside.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> start(s) hallucinating.^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return 0;
	}

	protected Map<Environmental, String> hallus = new Hashtable<Environmental, String>();

	protected String getHalluName(final Environmental E)
	{
		if(E == null)
			return "";
		final MOB mob = (affected instanceof MOB)?(MOB)affected:null;
		if(hallus.containsKey(E))
			return hallus.get(E);
		final int rand = CMLib.dice().rollPercentage();
		final String adj;
		if(CMLib.dice().rollPercentage()<50)
		{
			final RecipeDriven P = ((RecipeDriven)CMClass.getCommon("Lacquerring"));
			final int x = CMLib.dice().roll(1, P.fetchRecipes().size(),-1);
			final List<String> pv = P.fetchRecipes().get(x);
			adj = pv.get(3);
		}
		else
			adj="";
		if(E instanceof Item)
		{
			if(rand < 50)
			{
				final int resourceCode=RawMaterial.CODES.ALL()[CMLib.dice().roll(1, RawMaterial.CODES.ALL().length, -1)];
				final String name=RawMaterial.CODES.NAME(resourceCode).toLowerCase();
				hallus.put(E, L("some @x1",(adj + " " + name).trim()));
			}
			else
			if(rand < 90)
			{
				String iname = CMClass.randomBasicItemPrototype().name().toLowerCase();
				iname = CMLib.english().removeArticleLead(CMStrings.replaceAll(iname, "generic ", ""));
				hallus.put(E, CMLib.english().startWithAorAn((adj + " " + iname).trim()));
			}
			else
			{
				final String mname = CMClass.randomRace().name().toLowerCase();
				hallus.put(E, CMLib.english().startWithAorAn((adj + " " + mname).trim()));
			}
		}
		else
		if(E instanceof MOB)
		{
			if(rand < 10)
			{
				final int resourceCode=RawMaterial.CODES.ALL()[CMLib.dice().roll(1, RawMaterial.CODES.ALL().length, -1)];
				final String name=RawMaterial.CODES.NAME(resourceCode).toLowerCase();
				hallus.put(E, L("some @x1",(adj + " " + name).trim()));
			}
			else
			if(rand < 20)
			{
				String iname = CMClass.randomBasicItemPrototype().name().toLowerCase();
				iname = CMLib.english().removeArticleLead(CMStrings.replaceAll(iname, "generic ", ""));
				hallus.put(E, CMLib.english().startWithAorAn((adj + " " + iname).trim()));
			}
			else
			{
				final String mname = CMClass.randomRace().name().toLowerCase();
				hallus.put(E, CMLib.english().startWithAorAn((adj + " " + mname).trim()));
			}
		}
		else
		if(E instanceof Exit)
		{
			if(rand < 90)
			{
				final int resourceCode=RawMaterial.CODES.ALL()[CMLib.dice().roll(1, RawMaterial.CODES.ALL().length, -1)];
				final String name=RawMaterial.CODES.NAME(resourceCode).toLowerCase();
				return L("some @x1",(adj + " " + name).trim());
			}
			else
			if(rand < 95)
			{
				String iname = CMClass.randomBasicItemPrototype().name().toLowerCase();
				iname = CMLib.english().removeArticleLead(CMStrings.replaceAll(iname, "generic ", ""));
				return CMLib.english().startWithAorAn((adj + " " + iname).trim());
			}
			else
			{
				final String mname = CMClass.randomRace().name().toLowerCase();
				return CMLib.english().startWithAorAn((adj + " " + mname).trim());
			}
		}
		else
		if(E instanceof Physical)
			return ((Physical)E).name(mob);
		else
			return E.name();
		return hallus.get(E);
	}

	public String halluString(String str, final String prefix, final Environmental E)
	{
		if(str==null)
			return null;
		if(str.trim().length()==0)
			return str;
		final String nname = getHalluName(E);
		str = CMStrings.replaceAll(str, prefix+"NOART>", nname);
		str = CMStrings.replaceAll(str, prefix+">", nname);
		str = CMStrings.replaceAll(str, prefix+"SELF>", nname);
		return str;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			if((msg.source() == affected)
			&&(msg.sourceMessage()!=null))
			{
				if((msg.target()!=null)&&(msg.target()!=affected))
					msg.setSourceMessage(halluString(msg.sourceMessage(), "<T-NAME", msg.target()));
				if(msg.tool() instanceof Physical)
					msg.setSourceMessage(halluString(msg.sourceMessage(), "<O-NAME", msg.tool()));
			}
			if((msg.target()==affected)
			&&(msg.targetMessage()!=null))
			{
				if((msg.source()!=null)&&(msg.source()!=affected))
					msg.setTargetMessage(halluString(msg.targetMessage(), "<S-NAME", msg.source()));
				if(msg.tool() instanceof Physical)
					msg.setTargetMessage(halluString(msg.targetMessage(), "<O-NAME", msg.tool()));
			}
			if((msg.source()!=affected)
			&&(msg.target()!=affected)
			&&(msg.othersMessage()!=null)
			&&(msg.source().location().numPCInhabitants()<2))
			{
				if((msg.source()!=null)&&(msg.source()!=affected))
					msg.setOthersMessage(halluString(msg.othersMessage(), "<S-NAME", msg.source()));
				if((msg.target()!=null)&&(msg.target()!=affected))
					msg.setOthersMessage(halluString(msg.othersMessage(), "<T-NAME", msg.target()));
				if(msg.tool() instanceof Physical)
					msg.setOthersMessage(halluString(msg.othersMessage(), "<O-NAME", msg.tool()));
			}
		}
		return super.okMessage(myHost, msg);
	}
}
