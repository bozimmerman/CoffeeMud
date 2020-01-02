package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.ThinAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2020 Bo Zimmerman

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
public class Dyeing extends PaintingSkill
{
	@Override
	public String ID()
	{
		return "Dyeing";
	}

	private final static String localizedName = CMLib.lang().L("Dyeing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"DYE", "DYEING"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ARTISTIC;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Dyeing()
	{
		super();
		displayText=L("You are dyeing...");
		verb=L("dyeing");
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(found!=null)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,L("<S-NAME> mess(es) up the dyeing."));
				else
				{
					removePaintJob(found);
					if(!writing.equalsIgnoreCase("remove"))
						this.addPaintJob(found, writing);
				}
			}
		}
		super.unInvoke();
	}

	@Override
	protected String getRecipeFile()
	{
		return "dyeing.txt";
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		final List<List<String>> recipes = addRecipes(mob,super.loadRecipes(getRecipeFile()));
		writing=CMParms.combine(commands,0).toLowerCase();
		List<String> finalRecipe = null;
		if(writing.equalsIgnoreCase("list"))
		{
			final StringBuilder colors=new StringBuilder(L("^NColors you can choose: "));
			for(final List<String> list : recipes)
			{
				final String name=list.get(RCP_COLOR);
				final int level=CMath.s_int(list.get(RCP_LEVEL));
				final int exp=CMath.s_int(list.get(RCP_EXPERTISE));
				if((level <= adjustedLevel(mob,asLevel))
				&&((exp<=super.getXLEVELLevel(mob))))
					colors.append(name).append("^N, ");
			}
			commonTell(mob,colors.substring(0,colors.length()-2)+"^N.\n\r");
			return false;
		}
		if(commands.size()<2)
		{
			commonTell(mob,L("You must specify what you want to dye, and color to dye it or the word remove, or specify LIST."));
			return false;
		}
		final Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		commands.remove(commands.get(0));
		writing=CMParms.combine(commands,0).toLowerCase();

		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_VEGETATION)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER))
		||(!target.isGeneric()))
		{
			commonTell(mob,L("You can't dye that material."));
			return false;
		}
		for(final List<String> list : recipes)
		{
			final String name=list.get(0);
			final int level=CMath.s_int(list.get(1));
			final int exp=CMath.s_int(list.get(RCP_EXPERTISE));
			if(name.equalsIgnoreCase(writing)
			&&(level<=adjustedLevel(mob,asLevel))
			&&((exp<=super.getXLEVELLevel(mob))))
			{
				finalRecipe=list;
				break;
			}
		}
		if((finalRecipe == null) && (!writing.equalsIgnoreCase("remove")))
		{
			commonTell(mob,L("You can't dye anything '@x1'. Try DYEING LIST for a list or use REMOVE as the color.",writing));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		String startMsg;
		if(writing.equalsIgnoreCase("remove"))
		{
			writing =  "remove";
			verb=L("removing the color from @x1",target.name());
			startMsg=L("<S-NAME> start(s) un-dyeing @x1.",target.name());
		}
		else
		if(finalRecipe != null)
		{
			writing =  finalRecipe.get(RCP_COLOR);
			verb=L("dyeing @x1 @x2",target.name(),writing);
			startMsg=L("<S-NAME> start(s) dyeing @x1.",target.name());
		}
		else
			startMsg=L("<S-NAME> start(s) dyeing @x1.",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		if(!proficiencyCheck(mob,0,auto))
			writing="";
		int duration=30;
		if(finalRecipe != null)
			duration=CMath.s_int(finalRecipe.get(RCP_TICKS));
		if((target.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)
			duration*=2;
		duration=getDuration(duration,mob,1,6);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startMsg);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
