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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color256;
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
public class MasterLacquerring extends MasterPaintingSkill
{
	@Override
	public String ID()
	{
		return "MasterLacquerring";
	}

	private final static String localizedName = CMLib.lang().L("Master Lacquering");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"MASTERLACQUERING", "MASTERLACQUER", "MLACQUER", "MLACQUERING"});
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

	public MasterLacquerring()
	{
		super();
		displayText=L("You are lacquering...");
		verb=L("lacquering");
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
					commonEmote(mob,L("<S-NAME> mess(es) up the lacquering."));
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
		return "masterlacquering.txt";
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		final List<List<String>> recipes = addRecipes(mob,super.loadRecipes(getRecipeFile()));
		if(CMSecurity.isASysOp(mob) && (CMParms.combine(commands).equalsIgnoreCase("test")))
		{
			doPaintingTest(mob, recipes);
			return false;
		}

		writing=CMParms.combine(commands,0).toLowerCase();
		List<String> finalRecipe = null;
		if(writing.equalsIgnoreCase("list"))
		{
			final StringBuilder colors=new StringBuilder(L("^NDesigns you can choose: "));
			final TreeSet<String> namesUsed = new TreeSet<String>();
			for(final List<String> list : recipes)
			{
				final String name=list.get(RCP_FINALNAME);
				final int level=CMath.s_int(list.get(RCP_LEVEL));
				if((level <= adjustedLevel(mob,asLevel))
				&&(!namesUsed.contains(name)))
				{
					namesUsed.add(name);
					colors.append(name).append("^N, ");
				}
			}
			commonTell(mob,colors.substring(0,colors.length()-2)+"^N.  "+L("Use MASTERLACQUER COLORS to see what colors you can choose.\n\r"));
			return false;
		}
		else
		if(writing.toLowerCase().startsWith("color"))
		{
			final StringBuilder colors=new StringBuilder(L("^NColors you can choose: "));
			final TreeSet<String> done=new TreeSet<String>();
			for(final Enumeration<Color256> c=CMLib.color().getColors256();c.hasMoreElements();)
			{
				final Color256 C=c.nextElement();
				final int exp=C.getExpertiseNum();
				if(exp<=super.getXLEVELLevel(mob)
				&&(!C.getCmChars().equals("^K"))
				&&(!C.getCmChars().equals("^#000")))
				{
					if(!done.contains(C.getName1()))
					{
						colors.append(C.getCmChars()).append(C.getName1()).append("^N, ");
						done.add(C.getName1());
					}
					if(!done.contains(C.getName2()))
					{
						colors.append(C.getCmChars()).append(C.getName2()).append("^N, ");
						done.add(C.getName2());
					}
				}
			}
			commonTell(mob,colors.substring(0,colors.length()-2)+"^N.\n\r");
			return false;
		}
		if((commands.size()>1)&&(commands.get(1).equalsIgnoreCase("remove")))
			writing="remove";
		else
		if(commands.size()<4)
		{
			commonTell(mob,L("You must specify what you want to lacqer, the design to use, and two or three colors to use, or the word REMOVE, or specify LIST."));
			return false;
		}
		else
			writing=commands.get(1);
		final Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target!=null)&&(CMLib.flags().canBeSeenBy(target,mob)))
		{
			final Set<MOB> followers=mob.getGroupMembers(new TreeSet<MOB>());
			boolean ok=false;
			for(final MOB M : followers)
			{
				if(target.secretIdentity().indexOf(getBrand(M))>=0)
					ok=true;
			}
			if(!ok)
			{
				commonTell(mob,L("You aren't allowed to work on '@x1'.",(commands.get(0))));
				return false;
			}
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		commands.remove(commands.get(0)); // remove item
		commands.remove(commands.get(0)); // remove design
		final List<Color256> colorsFound = new ArrayList<Color256>();
		final List<String> colorNamesFound = new ArrayList<String>();
		if(!writing.equalsIgnoreCase("remove"))
		{
			final String allColors = CMParms.combine(commands,0);
			String workColors = allColors.toLowerCase();
			while(workColors.length()>0)
			{
				final int numFound=colorsFound.size();
				for(final String cStr : getAllColors256NamesLowercased())
				{
					if(workColors.startsWith(cStr))
					{
						final Color256 C=getAllColors256NamesMap().get(cStr);
						if(C!=null)
						{
							if(C.getExpertiseNum()<=super.getXLEVELLevel(mob))
							{
								colorNamesFound.add(cStr);
								colorsFound.add(C);
								workColors=workColors.substring(cStr.length()).trim();
							}
						}
						break;
					}
				}
				if(colorsFound.size()==numFound)
				{
					commonTell(mob,L("The first color in '@x1' is unrecognized. Try MASTERLACQUER COLORS",workColors));
					return false;
				}
			}
			if(colorsFound.size()<2)
			{
				commonTell(mob,L("At least two colors is required."));
				return false;
			}
			if(colorsFound.size()>3)
			{
				commonTell(mob,L("You may not list more than 3 colors."));
				return false;
			}
		}
		if((((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_SYNTHETIC)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
			&&((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN))
		||(!target.isGeneric()))
		{
			commonTell(mob,L("You can't lacquer that material."));
			return false;
		}

		for(final List<String> list : recipes)
		{
			final String name=list.get(0);
			final int level=CMath.s_int(list.get(1));
			final int numColors=requiredColorsInRecipe(list.get(RCP_MASK));
			if(name.equalsIgnoreCase(writing)
			&&(level<=adjustedLevel(mob,asLevel))
			&&(numColors==colorsFound.size()))
			{
				finalRecipe=list;
				break;
			}
		}
		if((finalRecipe == null) && (!writing.equalsIgnoreCase("remove")))
		{
			for(final List<String> list : recipes)
			{
				final String name=list.get(0);
				final int level=CMath.s_int(list.get(1));
				if(name.equalsIgnoreCase(writing)
				&&(level<=adjustedLevel(mob,asLevel)))
				{
					commonTell(mob,L("I'm afraid that recipe does not support @x1 colors.",""+colorsFound.size()));
					return false;
				}
			}
		}

		if((finalRecipe == null) && (!writing.equalsIgnoreCase("remove")))
		{
			commonTell(mob,L("You can't lacquer anything '@x1'. Try MASTERLACQUERING LIST for a list or use REMOVE as the color.",writing));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		String startMsg;
		if(writing.equalsIgnoreCase("remove"))
		{
			writing =  "remove";
			verb=L("removing the color from @x1",target.name());
			startMsg=L("<S-NAME> start(s) un-lacquering @x1.",target.name());
		}
		else
		if(finalRecipe != null)
		{
			writing =  finalRecipe.get(RCP_COLOR);
			for(int i=0;i<colorNamesFound.size();i++)
				writing=CMStrings.replaceAll(writing, "@x"+(i+1), colorNamesFound.get(i));
			verb=L("lacquering @x1 @x2",target.name(),writing);
			startMsg=L("<S-NAME> start(s) lacquering @x1.",target.name());
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
			final MasterLacquerring M=(MasterLacquerring)beneficialAffect(mob,mob,asLevel,duration);
			if(M!=null)
			{
				M.colors=colorsFound;
				M.recipe=finalRecipe;
				M.colorsNames=colorNamesFound;
			}
		}
		return true;
	}
}
