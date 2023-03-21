package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2023 Bo Zimmerman

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
public class Painting extends CommonSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "Painting";
	}

	private final static String	localizedName	= CMLib.lang().L("Painting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PAINT", "PAINTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	protected Item		building	= null;
	protected boolean	messedUp	= false;

	@Override
	public String supportedResourceString()
	{
		return "LEATHER";
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tITEM_BASE_VALUE\tACTIVE_VERB\t"
		+"ITEM_CLASS_ID\tEXPERTISENUM\tZAPPERMASK\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_VALUE		= 3;
	protected static final int	RCP_VERB		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_XNUM		= 6;
	protected static final int	RCP_ZAPPERMASK	= 7;
	protected static final int	RCP_SPELL		= 8;

	@Override
	public List<List<String>> fetchRecipes()
	{
		@SuppressWarnings("unchecked")
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+getRecipeFilename());
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+getRecipeFilename(),null,CMFile.FLAG_LOGERRORS).text();
			V=new ReadOnlyList<List<String>>(CMLib.utensils().loadRecipeList(str.toString()));
			if(V.size()==0)
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+getRecipeFilename(),V);
		}
		return V;
	}

	public List<List<String>> matchingRecipes(final List<List<String>> recipes, final String recipeName, final boolean beLoose)
	{
		return new CraftingSkill().matchingRecipes(recipes, recipeName, beLoose);
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			if(name.equalsIgnoreCase(recipeName)
			||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
				matches.add(name);
		}
		return matches;
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RecipeDriven.RCP_FINALNAME ),
				Integer.valueOf(CMath.s_int(recipe.get( RecipeDriven.RCP_LEVEL ))));
	}

	@Override
	public String getRecipeFilename()
	{
		return "painting.txt";
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTelL(mob,"<S-NAME> mess(es) up painting @x1.",building.name());
					else
						mob.location().addItem(building,ItemPossessor.Expire.Player_Drop);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> originalCommands = new XVector<String>(commands);
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()==0)
		{
			commonTelL(mob,"Paint what or on what? Enter \"paint list\", \"paint [type] [canvas name]\", or paint \"wall\".");
			return false;
		}
		String paintingKeyWords=null;
		String paintingDesc=null;
		while(commands.size()>1)
		{
			final String last=(commands.get(commands.size()-1));
			if(last.startsWith("PAINTINGKEYWORDS="))
			{
				paintingKeyWords=last.substring(17).trim();
				if(paintingKeyWords.length()>0)
					commands.remove(commands.size()-1);
				else
					paintingKeyWords=null;
			}
			else
			if(last.startsWith("PAINTINGDESC="))
			{
				paintingDesc=last.substring(13).trim();
				if(paintingDesc.length()>0)
					commands.remove(commands.size()-1);
				else
					paintingDesc=null;
			}
			else
				break;
		}

		final String str=CMParms.combine(commands,0).toLowerCase().trim();
		final String word = commands.get(0).toLowerCase().trim();

		building=null;
		messedUp=false;
		Session S=mob.session();
		if((S==null)&&(mob.amFollowing()!=null))
			S=mob.amFollowing().session();
		if(S==null)
		{
			commonTelL(mob,"I can't work! I need a player to follow!");
			return false;
		}

		List<String> foundRecipe = null;
		Item canvasI=null;
		if("wall".startsWith(str.toLowerCase()))
		{
			if(!CMLib.law().doesOwnThisProperty(mob,mob.location()))
			{
				commonTelL(mob,"You need the owners permission to paint the walls here.");
				return false;
			}
		}
		else
		if("list".startsWith(word))
		{
			final List<List<String>> recipes = new XVector<List<String>>(fetchRecipes());
			CMLib.utensils().addExtRecipes(mob, ID(), recipes);
			String mask=(commands.size()==1)?"":CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer("");
			final int[] cols={
				CMLib.lister().fixColWidth(29,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session())
			};
			int toggler=1;
			final int toggleTop=2;
			for(int i=0;i<toggleTop;i++)
				buf.append(L("^H@x1 @x2 ",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Lvl"),cols[1])));
			buf.append("^N\n\r");
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : matchingRecipes(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=V.get(RCP_FINALNAME);
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String zmask=V.get(RCP_ZAPPERMASK);
					final int exp = CMath.s_int(V.get(RCP_XNUM));
					if((item.trim().length()>0)
					&&((exp<=super.getXLEVELLevel(mob))||allFlag)
					&&((level<=xlevel(mob))||allFlag)
					&&((zmask.trim().length()==0)||CMLib.masking().maskCheck(zmask, mob, true)))
					{
						buf.append("^w"+CMStrings.padRight(item,cols[0])+"^N "+CMStrings.padRight(""+level,cols[1])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if(commands.size()<2)
		{
			commonTelL(mob,"Paint what or on what? Enter \"paint list\", \"paint [type] [canvas name]\", or paint \"wall\".");
			return false;
		}
		else
		{
			final String what=CMParms.combine(commands,1).toLowerCase().trim();
			final String recipeName = word;
			final List<List<String>> recipes = new XVector<List<String>>(fetchRecipes());
			CMLib.utensils().addExtRecipes(mob, ID(), recipes);

			canvasI=mob.location().findItem(null,what);
			if((canvasI==null)||(!CMLib.flags().canBeSeenBy(canvasI,mob)))
			{
				commonTelL(mob,"You don't see any canvases called '@x1' sitting here.",what);
				return false;
			}
			if((canvasI.material()!=RawMaterial.RESOURCE_COTTON)
			&&(canvasI.material()!=RawMaterial.RESOURCE_SILK)
			&&(!canvasI.Name().toUpperCase().endsWith("CANVAS"))
			&&(!canvasI.Name().toUpperCase().endsWith("SILKSCREEN")))
			{
				commonTelL(mob,"You cannot paint on '@x1'.",str);
				return false;
			}

			final List<List<String>> matches=matchingRecipes(recipes,recipeName,false);
			for(int r=0;r<matches.size();r++)
			{
				final List<String> V=matches.get(r);
				if(V.size()>0)
				{
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String zmask=V.get(RCP_ZAPPERMASK);
					final int exp = CMath.s_int(V.get(RCP_XNUM));
					if(((exp<=super.getXLEVELLevel(mob)))
					&&((level<=xlevel(mob)))
					&&((zmask.trim().length()==0)||CMLib.masking().maskCheck(zmask, mob, true)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTelL(mob,"You don't know how to paint a '@x1'.  Try \"paint list\" for a list.",recipeName);
				return false;
			}
		}

		int duration=25;
		final Session session=mob.session();
		final Ability me=this;
		final Physical target=givenTarget;
		if(str.equalsIgnoreCase("wall"))
		{
			if((paintingKeyWords!=null)&&(paintingDesc!=null))
			{
				building=CMClass.getItem("GenWallpaper");
				building.setName(paintingKeyWords);
				building.setDescription(paintingDesc);
				building.setSecretIdentity(getBrand(mob));
			}
			else
			if(session != null)
			{
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("Enter the key words (not the description) for this work.\n\r: "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String name=input.trim();
						if(name.length()==0)
							return;
						final Vector<String> V=CMParms.parse(name.toUpperCase());
						for(int v=0;v<V.size();v++)
						{
							final String vstr=" "+(V.elementAt(v))+" ";
							for(int i=0;i<mob.location().numItems();i++)
							{
								final Item I=mob.location().getItem(i);
								if((I!=null)
								&&(I.displayText().length()==0)
								&&(!CMLib.flags().isGettable(I))
								&&((" "+I.name().toUpperCase()+" ").indexOf(vstr)>=0))
								{
									final Item dupI=I;
									final String dupWord=vstr.trim().toLowerCase();
									session.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
									{
										@Override
										public void showPrompt()
										{
											session.promptPrint(L("\n\r'@x1' already shares one of these key words ('@x2').  Would you like to destroy it (y/N)? ", dupI.name(), dupWord));
										}

										@Override
										public void timedOut()
										{
										}

										@Override
										public void callBack()
										{
											if(this.input.equals("Y"))
											{
												dupI.destroy();
											}
										}
									});
									return;
								}
							}
						}
						session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("\n\rEnter a description for this.\n\r:"));
							}

							@Override
							public void timedOut()
							{
							}

							@Override
							public void callBack()
							{
								final String desc=this.input.trim();
								if(desc.length()==0)
									return;
								session.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
								{
									@Override
									public void showPrompt()
									{
										session.promptPrint(L("Wall art key words: '@x1', description: '@x2'.  Correct (Y/n)? ", name, desc));
									}

									@Override
									public void timedOut()
									{
									}

									@Override
									public void callBack()
									{
										if(this.input.equals("Y"))
										{
											final Vector<String> newCommands=new XVector<String>(originalCommands);
											newCommands.add("PAINTINGKEYWORDS="+name);
											newCommands.add("PAINTINGDESC="+desc);
											me.invoke(mob, newCommands, target, auto, asLevel);
										}
									}
								});
							}
						});
					}
				});
				return true;
			}
		}
		else
		if(canvasI!=null)
		{
			if((paintingKeyWords!=null)&&(paintingDesc!=null)&&(foundRecipe!=null))
			{
				building=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
				final String name = CMLib.english().startWithAorAn(
					foundRecipe.get(RCP_FINALNAME)
				) + L(" of ") + paintingKeyWords;
				building.setName(name);
				building.setDisplayText(L("@x1 is here.",name));
				building.setDescription(paintingDesc);
				building.basePhyStats().setWeight(canvasI.basePhyStats().weight());
				building.setBaseValue(canvasI.baseGoldValue()+(CMLib.dice().roll(1,CMath.s_int(foundRecipe.get(RCP_VALUE))+super.getXLEVELLevel(mob),0)));
				building.setMaterial(canvasI.material());
				building.basePhyStats().setLevel(canvasI.basePhyStats().level());
				building.setSecretIdentity(getBrand(mob));
				final String spell=foundRecipe.get(RCP_SPELL);
				new CraftingSkill().addSpellsOrBehaviors(building,spell,new ArrayList<CMObject>(),new ArrayList<CMObject>());
				canvasI.destroy();
			}
			else
			if((session != null)&&(foundRecipe!=null))
			{
				final String paintingName = foundRecipe.get(RCP_FINALNAME).toLowerCase();
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("\n\rIn brief, what is this a @x1 of?\n\r: ",paintingName));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String name=this.input.trim();
						if(name.length()==0)
							return;
						session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("\n\rPlease describe this @x1.\n\r: ",paintingName));
							}

							@Override
							public void timedOut()
							{
							}

							@Override
							public void callBack()
							{
								final String desc=this.input.trim();
								if(desc.length()==0)
									return;
								final Vector<String> newCommands=new XVector<String>(originalCommands);
								newCommands.add("PAINTINGKEYWORDS="+name);
								newCommands.add("PAINTINGDESC="+desc);
								me.invoke(mob, newCommands, target, auto, asLevel);
							}
						});
					}
				});
				return true;
			}
		}

		if(building == null)
		{
			this.commonTelL(mob,"I have no idea what I'm doing.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			building.destroy();
			building=null;
			return false;
		}

		String paintingName = L("painting");
		if(foundRecipe != null)
			paintingName = foundRecipe.get(RCP_VERB).toLowerCase();
		final String startStr=L("<S-NAME> start(s) @x2 @x1.",building.name(),paintingName);
		displayText=L("You are @x2 @x1",building.name(),paintingName);
		verb=L("@x1 @x2",paintingName,building.name());
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		duration=getDuration(25,mob,1,2);

		final CMMsg msg=CMClass.getMsg(mob,building,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
