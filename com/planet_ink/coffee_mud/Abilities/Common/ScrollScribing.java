package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2018 Bo Zimmerman

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

public class ScrollScribing extends SpellCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "ScrollScribing";
	}

	private final static String	localizedName	= CMLib.lang().L("Scroll Scribing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ENSCRIBE", "SCROLLSCRIBE", "SCROLLSCRIBING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	public String supportedResourceString()
	{
		return "MISC";
	}

	@Override
	public String parametersFormat()
	{
		return "SPELL_ID\tRESOURCE_NAME";
	}

	protected Ability	theSpell		= null;
	protected Scroll	fromTheScroll	= null;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if((buildingI==null)
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "scribing.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						commonTell(mob,L("Something went wrong! @x1 explodes!",buildingI.name()));
						buildingI.destroy();
					}
					else
					{
						int theSpellLevel=spellLevel(mob,theSpell);
						if(fromTheScroll != null)
							eraseFromScrollItem(fromTheScroll,theSpell,theSpellLevel);
						buildingI=buildScrollItem((Scroll)buildingI, theSpell, theSpellLevel);
						if(buildingI.secretIdentity().length()==0)
							setBrand(mob, buildingI);

					}
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	protected int spellLevel(MOB mob, Ability A)
	{
		int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(lvl<0)
			lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
		switch(lvl)
		{
		case 0:
			return lvl;
		case 1:
			return lvl;
		case 2:
			return lvl + 1;
		case 3:
			return lvl + 1;
		case 4:
			return lvl + 2;
		case 5:
			return lvl + 2;
		case 6:
			return lvl + 3;
		case 7:
			return lvl + 3;
		case 8:
			return lvl + 4;
		case 9:
			return lvl + 4;
		default:
			return lvl + 5;
		}
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	@Override
	public ItemKeyPair craftItem(String recipe)
	{
		return craftItem(recipe, 0, false, false);
	}

	protected void eraseFromScrollItem(Scroll buildingI, Ability theSpell, int level)
	{
		if(buildingI == null)
			return;
		StringBuilder newList=new StringBuilder();
		for(Ability A : buildingI.getSpells())
		{
			if(!A.ID().equalsIgnoreCase(theSpell.ID()))
				newList.append(A.ID()).append(";");
			if(buildingI.isGeneric())
			{
				final String testName=L(" OF @x1",A.Name().toUpperCase());
				if(buildingI.Name().toUpperCase().endsWith(testName))
					buildingI.setName(buildingI.Name().substring(0,buildingI.Name().length()-testName.length()));
			}
		}
		buildingI.setSpellList(newList.toString());
		if(buildingI.isGeneric())
		{
			if(buildingI.getSpells().size()==1)
			{
				if(buildingI.name().toUpperCase().endsWith(L(" SCROLL")))
					buildingI.setName(L("@x1 of @x2",buildingI.name(),buildingI.getSpells().get(0).name().toLowerCase()));
				buildingI.setDisplayText(L("@x1 sits here.",buildingI.Name()));
			}
		}
		if(buildingI.usesRemaining()>0)
			buildingI.setUsesRemaining(buildingI.usesRemaining()-1);
		buildingI.text();
	}
	
	protected Scroll buildScrollItem(Scroll buildingI, Ability theSpell, int level)
	{
		if(buildingI == null)
			buildingI=(Scroll)CMClass.getItem("GenScroll");
		StringBuilder newList=new StringBuilder();
		if(buildingI.usesRemaining()==0)
		{
			for(Ability A : buildingI.getSpells())
				this.eraseFromScrollItem(buildingI, A, -1);
		}
		else
		{
			for(Ability A : buildingI.getSpells())
			{
				newList.append(A.ID()).append(";");
				final String testName=L(" OF @x1",A.Name().toUpperCase());
				if(buildingI.Name().toUpperCase().endsWith(testName))
					buildingI.setName(buildingI.Name().substring(0,buildingI.Name().length()-testName.length()));
			}
		}
		newList.append(theSpell.ID());
		buildingI.setSpellList(newList.toString());
		if(buildingI.getSpells().size()==1)
		{
			if(buildingI.name().toUpperCase().endsWith(L(" SCROLL")))
				buildingI.setName(L("@x1 of @x2",buildingI.name(),theSpell.name().toLowerCase()));
			buildingI.setDisplayText(L("@x1 sits here.",buildingI.Name()));
			buildingI.basePhyStats().setLevel(level);
			buildingI.phyStats().setLevel(level);
			buildingI.recoverPhyStats();
		}
		else
		if(buildingI.basePhyStats().level() < level)
		{
			buildingI.basePhyStats().setLevel(level);
			buildingI.phyStats().setLevel(level);
			buildingI.recoverPhyStats();
		}
		buildingI.setDescription("");
		buildingI.setUsesRemaining(buildingI.usesRemaining()+1);
		buildingI.text();
		return buildingI;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}
	
	@Override
	protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, 
								 final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(autoGenerate>0)
		{
			final Ability theSpell=super.getCraftableSpellRecipeSpell(commands);
			if(theSpell==null) 
				return false;
			final int level=spellLevel(mob,theSpell);
			buildingI=buildScrollItem(null, theSpell, level);
			crafted.add(buildingI);
			return true;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()<1)
		{
			commonTell(mob,L("Enscribe what? Enter \"enscribe list\" for a list, or \"enscribe stop\" to cancel."));
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String pos=commands.get(commands.size()-1);
		if(((commands.get(0))).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Scrolls you know how to enscribe:\n\r"));
			final int colWidth=CMLib.lister().fixColWidth(22,mob.session());
			final int col2Width=CMLib.lister().fixColWidth(12,mob.session());
			final String headerPart="^H"+CMStrings.padRight(L("Spell"),colWidth)+" "+CMStrings.padRight(L("Materials"),col2Width);
			buf.append(headerPart).append("^w| ^H").append(headerPart).append("^.^N\n\r");
			int toggler=1;
			final int toggleTop=2;
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(0);
					final String matts=CMStrings.capitalizeAndLower(V.get(1));
					final Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&((spellLevel(mob,A)>=0)||(allFlag))
					&&((xlevel(mob)>=spellLevel(mob,A))||(allFlag))
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(spell,mask)))
					{
						buf.append(CMStrings.padRight(A.name(),colWidth)).append(" ");
						buf.append(CMStrings.padRight(matts,col2Width)+((toggler!=toggleTop)?"^w| ^N":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(toggler!=1)
				buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((!auto)&&(commands.size()<2))
		{
			commonEmote(mob,L("You must specify what magic you wish to enscribe, and the paper to enscribe it in.  You can specify the word \"from\" and another scroll name to move a spell from one scroll to another."));
			return false;
		}
		else
		{
			buildingI=getTarget(mob,null,givenTarget,CMParms.parse(pos),Wearable.FILTER_UNWORNONLY);
			commands.remove(pos);
			if(buildingI==null)
				return false;
			if(!mob.isMine(buildingI))
			{
				commonTell(mob,L("You'll need to pick that up first."));
				return false;
			}
			if((((buildingI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER))
			&&(buildingI.material() != RawMaterial.RESOURCE_SILK))
			{
				commonTell(mob,L("@x1 isn't even made of paper or silk!",buildingI.name(mob)));
				return false;
			}
			if((!(buildingI instanceof Scroll))
			||(!buildingI.isGeneric())
			||(!(buildingI instanceof MiscMagic))
			||(buildingI instanceof Recipe))
			{
				commonTell(mob,L("There's can't enscribe magic on @x1!",buildingI.name(mob)));
				return false;
			}
			if(((Scroll)buildingI).getSpells().size()>0)
			{
				int level=25;
				for(final Ability A : ((Scroll)buildingI).getSpells())
				{
					int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
					if(lvl<0)
						lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					level -= lvl;
				}
				if(level <= 0)
				{
					commonTell(mob,L("You can only scribe on blank scrolls, or scroll with enough free space on it."));
					return false;
				}
			}
			String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			fromTheScroll=null;
			String ingredient="";
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(0);
					final Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(xlevel(mob)>=spellLevel(mob,A))
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
						ingredient=V.get(1);
					}
				}
			}
			int manaToLose=10;
			int experienceToLose=0;
			if(theSpell==null)
			{
				int x=CMParms.indexOfIgnoreCase(commands, "from");
				if((x>0)&&(x<commands.size()-1))
				{
					recipeName=CMParms.combine(commands,0,x);
					String otherScrollName=CMParms.combine(commands,x+1,commands.size());
					Item scrollFromI=getTarget(mob,null,givenTarget,CMParms.parse(otherScrollName),Wearable.FILTER_UNWORNONLY);
					if(scrollFromI==null)
						return false;
					if(!mob.isMine(scrollFromI))
					{
						commonTell(mob,L("You'll need to pick that up first."));
						return false;
					}
					if(!(scrollFromI instanceof Scroll))
					{
						commonTell(mob,L("@x1 is not a scroll!",scrollFromI.name(mob)));
						return false;
					}
					if((!(scrollFromI instanceof Scroll))||(((Scroll)scrollFromI).getSpells().size()==0))
					{
						commonTell(mob,L("@x1 has no spells on it!",scrollFromI.name(mob)));
						return false;
					}
					if(scrollFromI.usesRemaining() <=0)
					{
						commonTell(mob,L("@x1 has no magical charge left in it.",scrollFromI.name(mob)));
						return false;
					}
					ingredient="";
					for(Ability A : ((Scroll)scrollFromI).getSpells())
					{
						if((A!=null)
						&&(xlevel(mob)>=spellLevel(mob,A))
						&&(A.name().equalsIgnoreCase(recipeName)))
							theSpell=A;
					}
					if(theSpell==null)
					{
						commonTell(mob,L("You can't enscribe the spell '@x1' from the scroll @x2!",recipeName,scrollFromI.name(mob)));
						return false;
					}
					fromTheScroll=(Scroll)scrollFromI;
				}
				else
				if(theSpell==null)
				{
					commonTell(mob,L("You don't know how to enscribe '@x1'.  Try \"enscribe list\" for a list.",recipeName));
					return false;
				}
				manaToLose+=spellLevel(mob,theSpell)*10;
			}
			else
			{
				manaToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				manaToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
				experienceToLose+=10+CMLib.ableMapper().qualifyingLevel(mob,theSpell);
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell);
				if(experienceToLose < CMLib.ableMapper().qualifyingLevel(mob,theSpell))
					experienceToLose = CMLib.ableMapper().qualifyingLevel(mob,theSpell);
			}
			
			if(buildingI!=null)
			{
				if(((Scroll)buildingI).usesRemaining()>0)
				{
					for(final Ability spell: ((Scroll)buildingI).getSpells())
					{
						if(spell.ID().equals(theSpell.ID()))
						{
							mob.tell(L("That spell is already scribed onto @x1.",buildingI.name()));
							return false;
						}
					}
				}
			}
			
			final int resourceType=(ingredient.length()==0) ? -1 : RawMaterial.CODES.FIND_IgnoreCase(ingredient);

			int[][] data = null;
			if(resourceType>0)
			{
				final int[] pm={resourceType};
				data=fetchFoundResourceData(mob,
											1,ingredient,pm,
											0,null,null,
											bundling,
											-1,
											null);
				if(data==null) 
					return false;
			}
			if(manaToLose<10) 
				manaToLose=10;

			if(mob.curState().getMana()<manaToLose)
			{
				commonTell(mob,L("You need at least @x1 mana to accomplish that.",""+manaToLose));
			}
			
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			
			mob.curState().adjMana(-manaToLose, mob.maxState());

			if((resourceType>0)&&(data != null))
				CMLib.materials().destroyResourcesValue(mob.location(),data[0][FOUND_AMT],data[0][FOUND_CODE],0,null);

			playSound=null;
			if(experienceToLose > 0)
			{
				experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
				CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
				commonTell(mob,L("You lose @x1 experience points for the effort.",""+experienceToLose));
			}
			
			int duration=getDuration(CMLib.ableMapper().qualifyingLevel(mob,theSpell)*5,mob,CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID()),10);
			if(duration<10)
				duration=10;
			messedUp=!proficiencyCheck(mob,0,auto);

			String msgStr;
			if(fromTheScroll != null)
			{
				msgStr=L("<S-NAME> start(s) transscribing @x1 from @x2 to @x3.",theSpell.name(),fromTheScroll.name(),buildingI.name());
				displayText=L("You are transscribing @x1 from @x2 to @x3",theSpell.name(),fromTheScroll.name(),buildingI.name());
				verb=L("transscribing @x1 from @x2 to @x3",theSpell.name(),fromTheScroll.name(),buildingI.name());
			}
			else
			{
				msgStr=L("<S-NAME> start(s) scribing @x1 onto @x2.",theSpell.name(),buildingI.name());
				displayText=L("You are scribing @x1 onto @x2",theSpell.name(),buildingI.name());
				verb=L("scribing @x1 onto @x2",theSpell.name(),buildingI.name());
			}
			final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),msgStr);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				buildingI=(Item)msg.target();
				beneficialAffect(mob,mob,asLevel,duration);
			}
		}
		return true;
	}
}
