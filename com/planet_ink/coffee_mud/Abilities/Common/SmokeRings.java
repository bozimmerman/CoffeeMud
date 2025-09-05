package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class SmokeRings extends CommonSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "SmokeRings";
	}

	private final static String	localizedName	= CMLib.lang().L("Smoke Rings");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SMOKERINGS"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	public SmokeRings()
	{
		super();
		displayText="";
		canBeUninvoked=false;
	}


	protected static final int	RCP_XLEVEL		= 2;
	protected static final int	RCP_DISPLAYSTRING	= 3;

	@Override
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes(getRecipeFilename());
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tXLEVEL\tDISPLAY_MASK";
	}

	@Override
	public String getRecipeFilename()
	{
		return "smokerings.txt";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			String name;
			for(int i=RecipeDriven.RCP_LEVEL+1;i<list.size();i++)
			{
				name=list.get(i);
				if(name.equalsIgnoreCase(recipeName)
				||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
					matches.add(name);
			}
		}
		return matches;
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RecipeDriven.RCP_LEVEL + 1 ),
				Integer.valueOf(CMath.s_int(recipe.get( RecipeDriven.RCP_LEVEL ))));
	}

	public String getSmokeStr(final MOB mob)
	{
		if (text().length() > 0)
			return text();
		final int level = super.adjustedLevel(mob, 0);
		final List<List<String>> recipes = CMLib.utensils().addExtRecipes(mob,ID(),fetchRecipes());
		final List<String> choices = new ArrayList<String>();
		for (final List<String> recipe : recipes)
		{
			if(CMath.s_int(recipe.get(RecipeDriven.RCP_LEVEL)) <= level)
			{
				final int xlevel = CMath.s_int(recipe.get(RCP_XLEVEL));
				if((xlevel == 0) || (xlevel <= super.getXLEVELLevel(mob)))
					choices.add(recipe.get(RCP_DISPLAYSTRING));
			}
		}
		if (choices.size() == 0)
			return L("<S-NAME> blow(s) out a perfect smoke ring.");
		final String choice = choices.get(CMLib.dice().roll(1, choices.size(), -1));
		if(choice.startsWith("*"))
			return L("<S-NAME> blow(s) out @x1.",choice.substring(1).trim());
		return choice;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(((host instanceof MOB)
		&&(msg.amISource((MOB)host)))
		&&(msg.targetMinor()==CMMsg.TYP_PUFF)
		&&(msg.target() instanceof Light)
		&&(msg.tool() instanceof Light)
		&&(!text().equals("stop"))
		&&(msg.target()==msg.tool())
		&&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
		&&(((Light)msg.target()).isLit())
		&&(proficiencyCheck(null,(10*getXLEVELLevel((MOB)host)),false)))
		{
			if(CMLib.dice().rollPercentage()==1)
				helpProficiency((MOB)host,0);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,msg.tool(),CMMsg.MSG_OK_VISUAL,getSmokeStr(msg.source())));
		}
		super.executeMsg(host,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return true;
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String word = CMParms.combine(commands).toLowerCase();
		final List<List<String>> recipes = CMLib.utensils().addExtRecipes(mob,ID(),fetchRecipes());
		if(word.equals("list"))
		{
			final StringBuilder words=new StringBuilder(L("^NSmoke Ring Styles: RANDOM, STOP, or "));
			for(final List<String> list : recipes)
			{
				final String name=list.get(RCP_FINALNAME);
				final int level=CMath.s_int(list.get(RCP_LEVEL));
				final int xlevel=CMath.s_int(list.get(RCP_XLEVEL));
				if((level <= adjustedLevel(mob,asLevel))
				&&(super.getXLEVELLevel(mob) >= xlevel))
					words.append(name).append(", ");
			}
			commonTell(mob,words.substring(0,words.length()-2)+".\n\r");
			return false;
		}
		String match = null;
		if((word.length()==0)
		||"random".startsWith(word))
			match = "";
		else
		if("stop".startsWith(word))
			match = "stop";
		else
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			final int level=CMath.s_int(list.get(RCP_LEVEL));
			final int xlevel=CMath.s_int(list.get(RCP_XLEVEL));
			if((level <= adjustedLevel(mob,asLevel))
			&&(super.getXLEVELLevel(mob) >= xlevel)
			&&(name.equalsIgnoreCase(word)))
				match = list.get(RCP_DISPLAYSTRING);
		}
		if(match == null)
		{
			commonTelL(mob,"Smoke how? '@x1' is unknown. Try @x2 LIST.",word,triggerStrings[0]);
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if (match.length() == 0)
			mob.tell(L("You will now select your smoke rings randomly."));
		else
			mob.tell(L("New smoke ring style selected."));
		final Ability effA = mob.fetchEffect(ID());
		if(effA != null)
			effA.setMiscText(match);
		return true;
	}
}
