package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class Thief_MakeBomb extends ThiefSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "Thief_MakeBomb";
	}

	private final static String localizedName = CMLib.lang().L("Make Bombs");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected final static int	RCP_TRAPID		= 3;
	protected final static int	RCP_ABILITYID	= 4;
	protected final static int	RCP_TRIGGERMSG	= 5;
	protected final static int	RCP_DAMAGEMSG	= 6;
	protected final static int	RCP_AVOIDMSG	= 7;

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"BOMB"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String obvious;
		Trap theTrap=null;
		List<String> theRecipe = null;
		final PairList<List<String>,Trap> traps=new PairVector<List<String>,Trap>();
		final int qualifyingClassLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(getXLEVELLevel(mob));
		final List<List<String>> recipes=CMLib.utensils().addExtRecipes(mob,ID(),this.fetchRecipes());
		for(final List<String> V : recipes)
		{
			final Ability A=CMClass.getAbility(V.get(RCP_TRAPID));
			final int level = CMath.s_int(V.get(RCP_LEVEL));
			A.setMiscText(":"+level+":");
			if((A instanceof Trap)
			&&(((Trap)A).maySetTrap(mob,qualifyingClassLevel)))
				traps.add(V,(Trap)A);
		}
		Collections.sort(traps, new Comparator<Pair<List<String>,Trap>>()
		{
			@Override
			public int compare(final Pair<List<String>, Trap> o1, final Pair<List<String>, Trap> o2)
			{
				final List<String> l1=o1.first;
				final List<String> l2=o2.first;
				final int v1=CMath.s_int(l1.get(RCP_LEVEL));
				final int v2=CMath.s_int(l2.get(RCP_LEVEL));
				return (v1==v2)?0:((v1<v2)?-1:1);
			}
		});
		Physical trapThis=givenTarget;
		if(trapThis!=null)
		{

			theRecipe=traps.get(CMLib.dice().roll(1,traps.size(),-1)).first;
			theTrap=(Trap)CMClass.getAbility(theRecipe.get(RCP_TRAPID));
			obvious=null;
		}
		else
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 Requires\n\r",
					CMStrings.padRight(L("Bomb Name"),25),
					CMStrings.padRight(L("Lvl"),4)));
			final int restLen = CMLib.lister().fixColWidth(78 - 26 - 5, mob);
			for(int r=0;r<traps.size();r++)
			{
				final List<String> V=traps.getFirst(r);
				final Trap T=traps.getSecond(r);
				buf.append(CMStrings.padRight(V.get(RCP_FINALNAME),25)+" ");
				buf.append(CMStrings.padRight(V.get(RCP_LEVEL),4)+" ");
				buf.append(CMStrings.limit(T.requiresToSet(),restLen)+"\n\r");
			}
			if(mob.session()!=null)
				mob.session().safeRawPrintln(buf.toString());
			return true;
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell(L("Make a bomb from what, with what kind of bomb? Use bomb list for a list."));
				return false;
			}
			final String name=commands.get(commands.size()-1);
			commands.remove(commands.size()-1);
			if((commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("OBVIOUS")))
				obvious=commands.remove(commands.size()-1);
			else
				obvious=null;
			for(int r=0;r<traps.size();r++)
			{
				final List<String> V=traps.getFirst(r);
				final Trap T=traps.getSecond(r);
				if(V.get(RCP_FINALNAME).equalsIgnoreCase(name))
				{
					theTrap=T;
					theRecipe=V;
				}
			}
			if(theTrap==null)
			{
				for(int r=0;r<traps.size();r++)
				{
					final List<String> V=traps.getFirst(r);
					final Trap T=traps.getSecond(r);
					if(CMLib.english().containsString(V.get(RCP_FINALNAME),name))
					{
						theTrap=T;
						theRecipe=V;
						break;
					}
				}
			}
			if((theTrap==null)||(theRecipe==null))
			{
				mob.tell(L("'@x1' is not a valid bomb name.  Try BOMB LIST.",name));
				return false;
			}

			trapThis=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
			if(trapThis==null)
				return false;
			if((!auto)
			&&(!theTrap.canSetTrapOn(mob,trapThis)))
				return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+((mob.phyStats().level()+(getXLEVELLevel(mob)*3)
											 -trapThis.phyStats().level())*3),auto);
		final Trap theOldTrap=CMLib.utensils().fetchMyTrap(trapThis);
		if(theOldTrap!=null)
		{
			if(theOldTrap.disabled())
				success=false;
			else
			{
				theOldTrap.spring(mob);
				return false;
			}
		}

		final CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?L("@x1 begins to glow!",trapThis.name()):L("<S-NAME> attempt(s) to make a bomb out of <T-NAMESELF>.")));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell(L("You have completed your task."));
				theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),false);
				if((obvious!=null)&&CMLib.flags().isTrapped(trapThis))
					trapThis.basePhyStats().addAmbiance("("+theRecipe.get(RCP_FINALNAME)+")");
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50)
				{
					final Trap T=theTrap.setTrap(mob,trapThis,getXLEVELLevel(mob),adjustedLevel(mob,asLevel),false);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> set(s) the bomb off on accident!"));
					T.spring(mob);
				}
				else
				{
					mob.tell(L("You fail in your attempt."));
				}
			}
		}
		return success;
	}

	@Override
	public String getRecipeFilename()
	{
		return "bombs.txt";
	}

	@Override
	public List<List<String>> fetchRecipes()
	{
		@SuppressWarnings("unchecked")
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+getRecipeFilename());
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+getRecipeFilename(),null,CMFile.FLAG_LOGERRORS).text();
			V=new ReadOnlyList<List<String>>(CMLib.utensils().loadRecipeList(str.toString(), true));
			if(V.size()==0)
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+getRecipeFilename(),V);
		}
		return V;
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tN_A\t"
		+ "TRAP_ID\tABILITYID\tTRIGGER_MSG\tDAMAGE_MSG\tAVOID_MSG";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RCP_FINALNAME);
			if(name.equalsIgnoreCase(recipeName)
			||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
				matches.add(name);
		}
		return matches;
	}

	@Override
	public Pair<String,Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RCP_FINALNAME ),
				Integer.valueOf(CMath.s_int(recipe.get( RCP_LEVEL ))));
	}
}
