package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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
public class Cooking extends EnhancedCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Cooking";
	}

	private final static String	localizedName	= CMLib.lang().L("Cooking");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "COOK", "COOKING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public String cookWordShort()
	{
		return "cook";
	}

	public String cookWord()
	{
		return "cooking";
	}

	public boolean honorHerbs()
	{
		return true;
	}

	public boolean requireFire()
	{
		return true;
	}

	public boolean requireLid()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "MISC";
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EPICUREAN;
	}

	@Override
	public String parametersFormat()
	{
		return
		"ITEM_NAME\tFOOD_DRINK||RESOURCE_NAME\tSMELL_LIST||CODED_SPELL_LIST\tITEM_LEVEL\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
		+"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED";
	}

	public static int	RCP_FINALFOOD	= 0;
	public static int	RCP_FOODDRINK	= 1;
	public static int	RCP_BONUSSPELL	= 2;
	public static int	RCP_LEVEL		= 3;
	public static int	RCP_MAININGR	= 4;
	public static int	RCP_MAINAMNT	= 5;

	protected Container				cookingPot			= null;
	protected String				finalDishName		= null;
	protected int					finalAmount			= 0;
	protected List<String>			finalRecipe			= null;
	protected Map<String, Integer>	oldPotContents		= null;
	protected String				defaultFoodSound	= "sizzle.wav";
	protected String				defaultDrinkSound	= "liquid.wav";

	public Cooking()
	{
		super();
		displayText=L("You are @x1...",cookWord());
		verb=cookWord();
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(40,mob,level,5);
	}

	protected boolean isMineForCooking(final MOB mob, final Container cooking)
	{
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A instanceof Cooking) && (((Cooking)A).cookingPot==cooking) && (A!=this))
				return false;
		}
		if(mob.isMine(cooking))
			return true;
		if((mob.location()==cooking.owner())
		&&((CMLib.flags().isOnFire(cooking))
			||(!requireFire())
			||((cooking.container()!=null)&&(CMLib.flags().isOnFire(cooking.container())))))
		   return true;
		return false;
	}

	protected boolean meetsLidRequirements(final MOB mob, final Container cooking)
	{
		if(!requireLid())
			return true;
		if((cooking.hasADoor())&&(!cooking.isOpen()))
			return true;
		if((cooking.container()!=null)
		&&(cooking.container().hasADoor())
		&&(!cooking.container().isOpen()))
		   return true;
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.target()==cookingPot)||(msg.tool()==cookingPot))
		&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_HANDS))
		&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_ALWAYS))
		&&(requireFire())
		&&(affected instanceof MOB))
		{
			msg.source().tell(L("Ouch! That's HOT!"));
			return false;
		}
		return super.okMessage(myHost, msg);
	}

	protected void stirThePot(final MOB mob)
	{
		if(buildingI!=null)
		{
			if((tickUp % 5)==1)
			{
				final Room R=mob.location();
				if(R==activityRoom)
				{
					R.show(mob,cookingPot,buildingI,CMMsg.MASK_ALWAYS|getActivityMessageType(),
							L("<S-NAME> taste-test(s) the <O-NAME> in <T-NAME>."));
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((cookingPot==null)
			||(buildingI==null)
			||(finalRecipe==null)
			||(finalAmount<=0)
			||(!isMineForCooking(mob,cookingPot))
			||(!meetsLidRequirements(mob,cookingPot))
			||(!contentsSame(potContents(cookingPot),oldPotContents))
			||(requireFire()&&(getRequiredFire(mob,0)==null)))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonTell(mob,L("You start @x1 up some @x2.",cookWord(),finalDishName));
				displayText=L("You are @x1 @x2",cookWord(),finalDishName);
				verb=cookWord()+" "+finalDishName;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	@Override
	public String parametersFile()
	{
		return "recipes.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Container cookingPot=this.cookingPot;
				final List<String> finalRecipe = this.finalRecipe;
				final Item buildingI=this.buildingI;
				if((cookingPot!=null)
				&&(finalRecipe!=null)
				&&(buildingI!=null)
				&&(mob!=null))
				{
					final List<Item> V=cookingPot.getDeepContents();
					for(int v=0;v<V.size();v++)
						V.get(v).destroy();
					if((cookingPot instanceof Drink)&&(buildingI instanceof Drink))
						((Drink)cookingPot).setLiquidRemaining(0);
					if(!aborted)
					{
						final CMMsg msg=CMClass.getMsg(mob,buildingI,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
						setMsgXPValue(mob,msg);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							buildingI.basePhyStats().setLevel(1); // the newbie exception
							buildingI.phyStats().setLevel(1); // the newbie exception
							for(int i=0;i<finalAmount*(baseYield()+abilityCode());i++)
							{
								final Item food=((Item)buildingI.copyOf());
								food.setMiscText(buildingI.text());
								food.recoverPhyStats();
								if(cookingPot.owner() instanceof Room)
									((Room)cookingPot.owner()).addItem(food,ItemPossessor.Expire.Player_Drop);
								else
								if(cookingPot.owner() instanceof MOB)
									((MOB)cookingPot.owner()).addItem(food);
								food.setContainer(cookingPot);
								if(((food.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
								&&(cookingPot instanceof Drink))
									((Drink)cookingPot).setLiquidRemaining(0);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	protected boolean contentsSame(final Map<String,Integer> h1, final Map<String,Integer> h2)
	{
		if(h1.size()!=h2.size())
			return false;
		for(final String key : h1.keySet())
		{
			final Integer INT1=h1.get(key);
			final Integer INT2=h2.get(key);
			if((INT1==null)||(INT2==null))
				return false;
			if(INT1.intValue()!=INT2.intValue())
				return false;
		}
		return true;
	}

	protected Map<String,Integer> potContents(final Container pot)
	{
		final Map<String,Integer> h=new Hashtable<String,Integer>();
		if((pot instanceof Drink)&&(((Drink)pot).liquidRemaining()>0))
		{
			if(pot instanceof RawMaterial)
				h.put(RawMaterial.CODES.NAME(((RawMaterial)pot).material())+"/",Integer.valueOf(((Drink)pot).liquidRemaining()/10));
			else
				h.put(RawMaterial.CODES.NAME(((Drink)pot).liquidType())+"/",Integer.valueOf(((Drink)pot).liquidRemaining()/10));
		}
		if(pot.owner()==null)
			return h;
		final List<Item> V=pot.getDeepContents();
		for(int v=0;v<V.size();v++)
		{
			final Item I=V.get(v);
			String ing="Unknown";
			if(I instanceof RawMaterial)
			{
				ing=RawMaterial.CODES.NAME(I.material());
				if(CMParms.indexOf( RawMaterial.CODES.FISHES(), I.material())>=0)
					ing+="/FISH";
				else
				if(CMParms.indexOf( RawMaterial.CODES.BERRIES(), I.material())>=0)
					ing+="/BERRIES";
			}
			else
			if((((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH))
			&&(CMParms.parse(I.name()).size()>0))
				ing=CMParms.parse(I.name()).lastElement().toUpperCase();
			else
				ing=I.name();
			Integer INT=h.get(ing+"/"+I.rawSecretIdentity().toUpperCase()+"/"+I.Name().toUpperCase()+"/");
			if(INT==null)
				INT=Integer.valueOf(0);
			if(I instanceof RawMaterial)
				INT=Integer.valueOf(INT.intValue()+((RawMaterial)I).phyStats().weight());
			else
				INT=Integer.valueOf(INT.intValue()+1);
			h.put(ing+"/"+I.rawSecretIdentity().toUpperCase()+"/"+I.Name().toUpperCase()+"/",INT);
		}
		return h;
	}

	protected List<Object> countIngredients(final List<String> Vr)
	{
		final String[] contents=new String[oldPotContents.size()];
		final int[] amounts=new int[oldPotContents.size()];
		int numIngredients=0;
		for(final String ingr  : oldPotContents.keySet())
		{
			contents[numIngredients]=ingr;
			amounts[numIngredients]=oldPotContents.get(contents[numIngredients]).intValue();
			numIngredients++;
		}

		int amountMade=0;

		final List<Object> codedList=new ArrayList<Object>();
		boolean RanOutOfSomething=false;
		boolean NotEnoughForThisRun=false;
		while((!RanOutOfSomething)&&(!NotEnoughForThisRun))
		{
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				final String ingredient=Vr.get(vr).toUpperCase();
				if(ingredient.length()>0)
				{
					int amount=1;
					if(vr<Vr.size()-1)
						amount=CMath.s_int(Vr.get(vr+1));
					if(amount==0)
						amount=1;
					if(amount<0)
						amount=amount*-1;
					if(ingredient.equalsIgnoreCase("water"))
						amount=amount*10;
					for(int i=0;i<contents.length;i++)
					{
						final String ingredient2=contents[i].toUpperCase();
						final int amount2=amounts[i];
						final int index =ingredient2.indexOf(ingredient+"/");
						if((index==0)||((index>0)&&(!Character.isLetter(ingredient2.charAt(index-1)))))
						{
							amounts[i]=amount2-amount;
							if(amounts[i]<0)
								NotEnoughForThisRun=true;
							if(amounts[i]==0)
								RanOutOfSomething=true;
						}
					}
				}
			}
			if(!NotEnoughForThisRun)
				amountMade++;
		}
		if(NotEnoughForThisRun)
		{
			codedList.add(Integer.valueOf(-amountMade));
			for(int i=0;i<contents.length;i++)
			{
				if(amounts[i]<0)
				{
					String content=contents[i];
					if(content.indexOf('/')>=0)
						content=content.substring(0,content.indexOf('/'));
					codedList.add(content);
				}
			}
		}
		else
		{
			codedList.add(Integer.valueOf(amountMade));
			for(int i=0;i<contents.length;i++)
			{
				final String ingredient2=contents[i];
				final int amount2=amounts[i];
				if((amount2>0)
				&&((!honorHerbs())||(!ingredient2.toUpperCase().startsWith("HERBS/")))
				&&(!ingredient2.toUpperCase().startsWith("WATER/")))
				{
					String content=contents[i];
					if(content.indexOf('/')>=0)
						content=content.substring(0,content.indexOf('/'));
					codedList.add(content);
				}
			}
		}

		return codedList;
	}

	private List<String> extraIngredientsInOldContents(final List<String> Vr, final boolean perfectOnly)
	{
		final List<String> extra=new ArrayList<String>();
		for(final String ingredient : oldPotContents.keySet())
		{
			boolean found=false;

			if(honorHerbs()&&ingredient.toUpperCase().startsWith("HERBS/")) // herbs exception
				found=true;
			else
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				final String ingredient2=Vr.get(vr).toUpperCase();
				final int index=ingredient.toUpperCase().indexOf(ingredient2+"/");
				if((ingredient2.length()>0)
				&&(((!perfectOnly)&&((index==0)||((index>0)&&(!Character.isLetter(ingredient.charAt(index-1))))))
				||((perfectOnly)&&ingredient.toUpperCase().equalsIgnoreCase(ingredient2))))
					found=true;
			}
			if(!found)
			{
				String content=ingredient;
				if(content.indexOf('/')>=0)
					content=content.substring(0,content.indexOf('/'));
				extra.add(content);
			}
		}
		return extra;
	}

	public List<String> missingIngredientsFromOldContents(final List<String> Vr, final boolean perfectOnly)
	{
		final List<String> missing=new ArrayList<String>();

		String possiblyMissing=null;
		boolean foundOptional=false;
		boolean hasOptional=false;
		for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
		{
			final String ingredient=Vr.get(vr);
			if(ingredient.length()>0)
			{
				int amount=1;
				if(vr<Vr.size()-1)
					amount=CMath.s_int(Vr.get(vr+1));
				boolean found=false;
				for(final String ingredient2 : oldPotContents.keySet())
				{
					final int index=ingredient2.toUpperCase().indexOf(ingredient.toUpperCase()+"/");
					if((((index==0)||((index>0)&&(!Character.isLetter(ingredient2.charAt(index-1))))))
					||((!perfectOnly)&&ingredient2.equalsIgnoreCase(ingredient.toUpperCase())))
					{
						found = true;
						break;
					}
				}
				if(amount>=0)
				{
					if(!found)
						missing.add(ingredient);
				}
				else
				if(amount<0)
				{
					foundOptional=true;
					if(found)
						hasOptional=true;
					else
						possiblyMissing=ingredient;
				}
			}
		}
		if((foundOptional)&&(!hasOptional))
			missing.add(possiblyMissing);
		return missing;
	}

	public int homeCookValue(final MOB mob, final int multiplyer)
	{
		final int hc=getX1Level(mob);
		return hc*hc*multiplyer;
	}

	public Item buildItem(final MOB mob, final List<String> finalRecipe, final List<Item> contents)
	{
		String replaceName=(finalRecipe.get(RCP_MAININGR));
		boolean rotten = false;
		final Item buildingI;
		if(contents!=null)
		{
			for(int v=0;v<contents.size();v++)
			{
				final Item I=contents.get(v);
				if((I instanceof RawMaterial)
				&&(I instanceof Decayable)
				&&(I.fetchEffect("Poison_Rotten")!=null))
				{
					rotten=true;
					break;
				}
			}
			for(int v=0;v<contents.size();v++)
			{
				final Item I=contents.get(v);
				if(I instanceof RawMaterial)
				{
					if(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(finalRecipe.get(RCP_MAININGR)))
					{
						if((((RawMaterial)I).domainSource()!=null)
						&&(!CMath.isNumber(((RawMaterial)I).domainSource()))
						&&(CMClass.getRace(((RawMaterial)I).domainSource()))!=null)
							replaceName=CMClass.getRace(((RawMaterial)I).domainSource()).name().toLowerCase();
						else
						{
							String name=I.Name();
							if(name.endsWith(" meat"))
								name=name.substring(0,name.length()-5);
							if(name.endsWith(" flesh"))
								name=name.substring(0,name.length()-6);
							if(name.toLowerCase().endsWith(L(" bundle")))
								name = name.substring(0,name.length()-L(" bundle").length());
							name=name.trim();
							final int x=name.lastIndexOf(' ');
							if((x>0)&&(!name.substring(x+1).trim().equalsIgnoreCase("of")))
								replaceName=name.substring(x+1);
							else
								replaceName=name;
						}
						break;
					}
				}
			}
		}
		finalDishName=replacePercent(finalRecipe.get(RCP_FINALFOOD),
									CMStrings.capitalizeAndLower(replaceName));
		final String foodType=finalRecipe.get(RCP_FOODDRINK);
		if(foodType.equalsIgnoreCase("FOOD"))
		{
			buildingI=CMClass.getItem("GenFood");
			this.buildingI=buildingI;
			final Food food=(Food)buildingI;
			if(requireFire())
			{
				buildingI.setName(((messedUp)?"burnt ":"")+finalDishName);
				buildingI.setDisplayText(L("some @x1@x2 is here",((messedUp)?"burnt ":""),finalDishName));
				buildingI.setDescription(L("It looks @x1",((messedUp)?"burnt!":rotten?"rotten!":"good!")));
			}
			else
			{
				buildingI.setName(((messedUp)?"ruined ":"")+finalDishName);
				buildingI.setDisplayText(L("some @x1@x2 is here",((messedUp)?"ruined ":""),finalDishName));
				buildingI.setDescription(L("It looks @x1",((messedUp)?"ruined!":rotten?"rotten!":"good!")));
			}
			buildingI.basePhyStats().setLevel(CMath.s_int(finalRecipe.get(RCP_LEVEL)));
			buildingI.phyStats().setLevel(buildingI.basePhyStats().level());
			food.setNourishment(0);
			if(!messedUp)
			{
				boolean timesTwo=false;
				if((contents!=null)&&(contents.size()>0))
				{
					for(int v=0;v<contents.size();v++)
					{
						final Item I=contents.get(v);
						if((I.material()==RawMaterial.RESOURCE_HERBS)&&(honorHerbs()))
							timesTwo=true;
						else
						if(I instanceof Food)
							food.setNourishment(food.nourishment()+(((Food)I).nourishment()+((Food)I).nourishment())+25);
						else
							food.setNourishment(food.nourishment()+25);
					}
				}
				else
				{
					for(int vr=RCP_MAININGR;vr<finalRecipe.size();vr+=2)
					{
						final String ingredient=finalRecipe.get(vr).toUpperCase();
						if(ingredient.length()>0)
						{
							int amount=1;
							if(vr<finalRecipe.size()-1)
								amount=CMath.s_int(finalRecipe.get(vr+1));
							if(amount==0)
								amount=1;
							if(amount<0)
								amount=amount*-1;
							if(ingredient.equalsIgnoreCase("water")||ingredient.equalsIgnoreCase("milk"))
								continue;
							if(ingredient.equalsIgnoreCase("herbs"))
							{
								timesTwo=true;
								continue;
							}
							final int resourceCode=RawMaterial.CODES.FIND_IgnoreCase(ingredient);
							if((resourceCode >0)&&((resourceCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
								continue;
							food.setNourishment(food.nourishment()+(100*amount));
						}
					}
				}
				if(timesTwo)
					food.setNourishment(food.nourishment()*2);
			}
			int material=-1;
			for(int vr=RCP_MAININGR;vr<finalRecipe.size();vr+=2)
			{
				final String ingredient=finalRecipe.get(vr).toUpperCase();
				if(ingredient.length()>0)
				{
					final int resourceCode=RawMaterial.CODES.FIND_IgnoreCase(ingredient);
					if(resourceCode >0)
					{
						if(((resourceCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
						||((resourceCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
						{
							material=resourceCode;
							break;
						}
					}
				}
			}
			if(contents!=null)
			{
				for(int v=0;v<contents.size();v++)
				{
					final Item I=contents.get(v);
					if((I.material()!=RawMaterial.RESOURCE_HERBS)||(!honorHerbs()))
						food.basePhyStats().setWeight(food.basePhyStats().weight()+((I.basePhyStats().weight())/finalAmount));
					if((I instanceof Food)
					&&(material<0))
					{
						switch(((Food)I).material()&RawMaterial.MATERIAL_MASK)
						{
						case RawMaterial.MATERIAL_VEGETATION:
						case RawMaterial.MATERIAL_FLESH:
							material=((Food)I).material();
							break;
						}
					}
				}
				if(material<0)
				{
					for(int v=0;v<contents.size();v++)
					{
						final Item I=contents.get(v);
						if((I.material()!=RawMaterial.RESOURCE_HERBS)||(!honorHerbs()))
							food.basePhyStats().setWeight(food.basePhyStats().weight()+((I.basePhyStats().weight())/finalAmount));
						if(I instanceof Food)
						{
							switch(((Food)I).material()&RawMaterial.MATERIAL_MASK)
							{
							case RawMaterial.MATERIAL_VEGETATION:
							case RawMaterial.MATERIAL_FLESH:
								material=((Food)I).material();
								break;
							}
						}
					}
				}
			}
			if(material<0)
			{
				final String materialFoodName=replacePercent(finalRecipe.get(RCP_FINALFOOD),"").trim().toUpperCase();
				for(int i=0;i<RawMaterial.CODES.TOTAL();i++)
				{
					if(materialFoodName.equals(RawMaterial.CODES.NAME(i)))
					{
						material=i;
						break;
					}
				}
				if((contents!=null)&&(material<0))
				{
					for(int v=0;v<contents.size();v++)
					{
						final Item I=contents.get(v);
						if(I instanceof Drink)
						{
							material=((Drink)I).liquidType();
							break;
						}
					}
				}
			}
			food.setMaterial(material<0?RawMaterial.RESOURCE_BEEF:material);
			if((rotten)&&(food.nourishment()>1))
				food.setNourishment(food.nourishment()/2/(finalAmount>0?finalAmount:1));
			else
			if(mob!=null)
				food.setNourishment((food.nourishment()+homeCookValue(mob,10))/finalAmount);
			if(rotten)
			{
				final Ability A=CMClass.getAbility("Poison_Rotten");
				if(A!=null)
					food.addNonUninvokableEffect(A);
			}
			else
			if(!messedUp)
				CMLib.materials().addEffectsToResource(food);
			food.basePhyStats().setWeight(food.basePhyStats().weight()/finalAmount);
			if(food.basePhyStats().weight()>0)
				food.setBite(food.nourishment() / (food.basePhyStats().weight()*2));
			else
				food.setBite(food.nourishment());
			playSound=defaultFoodSound;
		}
		else
		if(foodType.equalsIgnoreCase("DRINK"))
		{
			buildingI=CMClass.getItem("GenLiquidResource");
			this.buildingI=buildingI;
			//building.setMiscText(cooking.text());
			//building.recoverPhyStats();
			buildingI.setName((messedUp?"spoiled ":"")+finalDishName);
			buildingI.setDisplayText(L("some @x1@x2 is here.",((messedUp)?"spoiled ":""),finalDishName));
			buildingI.setDescription(L("It looks @x1",((messedUp)?"spoiled!":rotten?"rotten!":"good!")));
			buildingI.basePhyStats().setLevel(CMath.s_int(finalRecipe.get(RCP_LEVEL)));
			buildingI.phyStats().setLevel(buildingI.basePhyStats().level());
			final Drink drink=(Drink)buildingI;
			int liquidType=RawMaterial.RESOURCE_FRESHWATER;
			for(int vr=RCP_MAININGR;vr<finalRecipe.size();vr+=2)
			{
				final String ingredient=finalRecipe.get(vr).toUpperCase();
				if(ingredient.length()>0)
				{
					final int resourceCode=RawMaterial.CODES.FIND_IgnoreCase(ingredient);
					if(resourceCode >0)
					{
						if((resourceCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
						{
							liquidType=resourceCode;
							break;
						}
					}
				}
			}
			if(contents!=null)
			{
				for(int v=0;v<contents.size();v++)
				{
					final Item I=contents.get(v);
					buildingI.basePhyStats().setWeight(buildingI.basePhyStats().weight()+((I.basePhyStats().weight())/finalAmount));
					if(I instanceof Food)
						drink.setLiquidRemaining(drink.liquidRemaining()+((Food)I).nourishment()+25);
					if((I instanceof Drink)
					&&(liquidType < 0)
					&&((((Drink)I).liquidType()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
						liquidType=((Drink)I).liquidType();
				}
			}
			buildingI.basePhyStats().setWeight(buildingI.basePhyStats().weight()/finalAmount);
			if(drink.liquidRemaining()>0)
			{
				drink.setLiquidRemaining(drink.liquidRemaining()+homeCookValue(mob,10));
				drink.setLiquidHeld(drink.liquidRemaining()+homeCookValue(mob,10));
				if(buildingI.basePhyStats().weight()>0)
					drink.setThirstQuenched(drink.liquidRemaining()/(buildingI.basePhyStats().weight()*2));
				else
					drink.setThirstQuenched(drink.liquidRemaining());
			}
			else
			{
				drink.setLiquidHeld(1);
				drink.setLiquidRemaining(1);
				drink.setThirstQuenched(1);
			}
			if(messedUp)
				drink.setThirstQuenched(1);
			else
			if(rotten && (drink.thirstQuenched() > 1))
				drink.setThirstQuenched(drink.thirstQuenched()/2);
			playSound=defaultDrinkSound;
			buildingI.setMaterial(liquidType);
			drink.setLiquidType(liquidType);
			if(rotten)
			{
				final Ability A=CMClass.getAbility("Poison_Rotten");
				if(A!=null)
					buildingI.addNonUninvokableEffect(A);
			}
			else
			if(!messedUp)
				CMLib.materials().addEffectsToResource((Item)drink);
		}
		else
		if(CMClass.getItem(foodType)!=null)
		{
			buildingI=CMClass.getItem(foodType);
			this.buildingI=buildingI;
			final String ruinWord=(buildingI instanceof Drink)?"spoiled ":(requireFire()?"burnt ":"ruined ");
			buildingI.setName(((messedUp)?ruinWord:"")+finalDishName);
			buildingI.setDisplayText(L("some @x1@x2 is here",((messedUp)?ruinWord:""),finalDishName));
			if(buildingI instanceof Drink)
			{
				final Drink drink=(Drink)buildingI;
				final int rem=drink.liquidHeld();
				drink.setLiquidRemaining(0);
				int liquidType=RawMaterial.RESOURCE_FRESHWATER;
				if(contents!=null)
				for(int v=0;v<contents.size();v++)
				{
					final Item I=contents.get(v);
					buildingI.basePhyStats().setWeight(buildingI.basePhyStats().weight()+((I.basePhyStats().weight())/finalAmount));
					drink.setLiquidRemaining(drink.liquidRemaining()+rem);
					if((I instanceof Drink)&&((((Drink)I).liquidType()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
						liquidType=((Drink)I).liquidType();
				}
				if((drink.liquidRemaining()>0)&&(!messedUp))
					drink.setLiquidHeld(drink.liquidRemaining());
				else
				{
					drink.setLiquidHeld(1);
					drink.setLiquidRemaining(1);
					drink.setThirstQuenched(1);
				}
				buildingI.setMaterial(liquidType);
				drink.setLiquidType(liquidType);
			}
			buildingI.basePhyStats().setWeight(buildingI.basePhyStats().weight()/finalAmount);
			if(!messedUp)
				CMLib.materials().addEffectsToResource(buildingI);
			playSound=defaultFoodSound;
		}
		else
		{
			buildingI=CMClass.getItem("GenResource");
			this.buildingI=buildingI;
			if(messedUp)
				buildingI.setMaterial(RawMaterial.RESOURCE_DUST);
			else
			{
				final int code = RawMaterial.CODES.FIND_IgnoreCase(foodType);
				if(code>=0)
					buildingI.setMaterial(code);
			}
			final String ruinWord=(buildingI instanceof Drink)?"spoiled ":(requireFire()?"burnt ":"ruined ");
			buildingI.setName(((messedUp)?ruinWord:"")+finalDishName);
			buildingI.setDisplayText(L("some @x1@x2 is here",((messedUp)?ruinWord:""),finalDishName));
			buildingI.basePhyStats().setWeight(buildingI.basePhyStats().weight()/finalAmount);
			playSound=defaultFoodSound;
		}

		if(buildingI!=null)
		{
			if(buildingI instanceof RawMaterial)
				((RawMaterial)buildingI).setSecretIdentity(buildingI.name());
			else
			if(mob!=null)
				buildingI.setSecretIdentity(L("This was prepared by @x1.",mob.Name()));
			final String spell=finalRecipe.get(RCP_BONUSSPELL);
			if((spell!=null)&&(spell.length()>0))
			{
				if(buildingI instanceof Perfume)
					((Perfume)buildingI).setSmellList(spell);
				else
					addSpellsOrBehaviors(buildingI,spell,null,null);
			}
			buildingI.recoverPhyStats();
			buildingI.text();
		}
		return buildingI;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}

	@Override
	protected void applyName(final Item item, final String word, final boolean hide)
	{
		super.applyName(item, word, hide);
		if(!buildingI.description().contains(L("rotten")))
		{
			buildingI.setDescription(L("It looks @x1",((messedUp)?(requireFire()?"burnt!":"ruined!"):(word+"!"))));
		}
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
									final int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=cookWord();
		cookingPot=null;
		finalRecipe=null;
		finalAmount=0;
		buildingI=null;
		finalDishName=null;
		messedUp=false;
		oldPotContents=null;
		activity = CraftingActivity.CRAFTING;
		final List<List<String>> allRecipes=addRecipes(mob,loadRecipes());
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		int recipeLevel = 1;
		if(autoGenerate>0)
		{
			finalAmount=1;
			List<String> finalRecipe=null;
			String recipeName=null;
			if((commands.size()>0))
				recipeName=CMParms.combine(commands,0);
			else
			{
				final List<List<String>> recipes=loadRecipes();
				final List<String> V = recipes.get(CMLib.dice().roll(1,recipes.size(),-1));
				recipeName=replacePercent(V.get(0),"");
			}
			for(int r=0;r<allRecipes.size();r++)
			{
				final List<String> Vr=allRecipes.get(r);
				if(Vr.size()>0)
				{
					final String item=Vr.get(RCP_FINALFOOD);
					if(replacePercent(item,"").equalsIgnoreCase(recipeName))
					{
						finalRecipe = Vr;
						break;
					}
				}
			}
			if(finalRecipe==null)
			{
				for(int r=0;r<allRecipes.size();r++)
				{
					final List<String> Vr=allRecipes.get(r);
					if(Vr.size()>0)
					{
						final String item=Vr.get(RCP_FINALFOOD);
						if(replacePercent(item,"").toLowerCase().indexOf(recipeName.toLowerCase())>=0)
						{
							finalRecipe = Vr;
							break;
						}
					}
				}
			}
			if(finalRecipe==null)
				return false;
			buildingI=buildItem(mob,finalRecipe,null);
			if(forceLevels)
			{
				buildingI.basePhyStats().setLevel(CMath.s_int(finalRecipe.get(RCP_LEVEL)));
				buildingI.phyStats().setLevel(buildingI.basePhyStats().level());
			}
			crafted.add(buildingI);
			return true;
		}
		randomRecipeFix(mob,allRecipes,commands,-1);
		final int colWidth1=CMLib.lister().fixColWidth(20,mob.session());
		final int colWidth2=CMLib.lister().fixColWidth(4,mob.session());
		final int lineWidth=CMLib.lister().fixColWidth(78,mob.session());
		if((commands.size()>0)
		&&(commands.get(0)).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(
				L("@x1 @x2^.^? ^B^~wIngredients required^N\n\r"
				,CMStrings.padRight(L("^xRecipe"),colWidth1)
				,CMStrings.padRight(L("^xLvl"),colWidth2))
			);
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? allRecipes : super.matchingRecipeNames(allRecipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> Vr=listRecipes.get(r);
				if(Vr.size()>0)
				{
					final String item=Vr.get(RCP_FINALFOOD);
					if(item.length()==0)
						continue;
					final int level=CMath.s_int(Vr.get(RCP_LEVEL));
					if(((level<=xlevel(mob))||allFlag))
					{
						StringBuilder line=new StringBuilder("");
						line.append("^c"+CMStrings.padRight(CMStrings.capitalizeAndLower(replacePercent(item,"")),colWidth1)+"^w ")
							.append(CMStrings.padRight(""+level,colWidth2)+"^w ");
						for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
						{
							String ingredient=Vr.get(vr);
							if(ingredient.length()>0)
							{
								int amount=1;
								if(vr<Vr.size()-1)
									amount=CMath.s_int(Vr.get(vr+1));
								if(amount==0)
									amount=1;
								if(amount<0)
								{
									ingredient="~"+ingredient;
									amount=amount*-1;
								}
								if(ingredient.equalsIgnoreCase("water"))
									amount=amount*10;
								final String next=ingredient.toLowerCase()+"("+amount+") ";
								if(line.length()+next.length()-2>=lineWidth)
								{
									buf.append(line).append("\n\r");
									line=new StringBuilder("^w ")
											.append(CMStrings.padRight(" ", colWidth1)).append(" ")
											.append(CMStrings.padRight(" ", colWidth2));
								}
								line.append(next);
							}
						}
						buf.append(line).append("\n\r");
					}
				}
			}
			commonTell(mob,L("@x1\n\rIngredients beginning with the ~ character are optional additives.",buf.toString()));
			enhanceList(mob);
			return true;
		}
		final Item possibleContainer=possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);
		final Item target=getTarget(mob,mob.location(),givenTarget,possibleContainer,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
		{
			commonTell(mob,L("The syntax for this skill is @x1 [CONTAINER]",triggerStrings()[0]));
			return false;
		}

		if(!(target instanceof Container))
		{
			commonTell(mob,L("There's nothing in @x1 to @x2!",target.name(mob),cookWordShort()));
			return false;
		}
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A instanceof Cooking) && (((Cooking)A).cookingPot==target))
			{
				commonTell(mob,L("That is already in use."));
				return false;
			}
		}
		if(!isMineForCooking(mob,(Container)target))
		{
			commonTell(mob,L("You probably need to pick that up first."));
			return false;
		}
		if(!meetsLidRequirements(mob,(Container)target))
		{
			commonTell(mob,L("You need a closeable container to bake that in, and you need to close it to begin."));
			return false;
		}
		switch(target.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_PRECIOUS:
			break;
		default:
			commonTell(mob,L("@x1 is not suitable to @x2 in.",target.name(mob),cookWordShort()));
			return false;
		}

		if(requireFire())
		{
			final Item fire=getRequiredFire(mob,0);
			if(fire==null)
				return false;
		}

		messedUp=!proficiencyCheck(mob,0,auto);
		int duration=getDuration(mob, 1);
		cookingPot=(Container)target;
		oldPotContents=potContents(cookingPot);

		//***********************************************
		//* figure out recipe
		//***********************************************
		List<List<String>> perfectRecipes=new ArrayList<List<String>>();
		final List<List<String>> closerRecipes=new ArrayList<List<String>>();
		final List<List<String>> closeRecipes=new ArrayList<List<String>>();
		for(int v=0;v<allRecipes.size();v++)
		{
			final List<String> Vr=allRecipes.get(v);
			boolean found=false;
			for(final String ingredient2 : oldPotContents.keySet())
			{
				final int index =ingredient2.indexOf(Vr.get(RCP_MAININGR).toUpperCase()+"/");
				if((index==0)||((index>0)&&(!Character.isLetter(ingredient2.charAt(index-1)))))
				{
					found = true;
					break;
				}
			}
			if(Vr.get(0).indexOf("ruit brand")>=0)
				System.out.println("hi");
			if(found)
				closeRecipes.add(Vr);
			if((missingIngredientsFromOldContents(Vr,true).size()==0)
			&&(extraIngredientsInOldContents(Vr,true).size()==0))
				perfectRecipes.add(Vr);
			if((missingIngredientsFromOldContents(Vr,false).size()==0)
			&&(extraIngredientsInOldContents(Vr,false).size()==0))
				closerRecipes.add(Vr);
		}

		if(perfectRecipes.size()==0)
			perfectRecipes=closerRecipes;
		if(perfectRecipes.size()==0)
		{
			if(closeRecipes.size()==0)
			{
				commonTell(mob,L("You don't know how to make anything out of those ingredients.  Have you tried LIST as a parameter?"));
				return false;
			}
			for(int vr=0;vr<closeRecipes.size();vr++)
			{
				final List<String> Vr=closeRecipes.get(vr);
				final List<String> missing=missingIngredientsFromOldContents(Vr,false);
				final List<String> extra=extraIngredientsInOldContents(Vr,false);
				final String recipeName=replacePercent(Vr.get(RCP_FINALFOOD),Vr.get(RCP_MAININGR).toLowerCase());
				if(extra.size()>0)
				{
					final StringBuffer buf=new StringBuffer(L("If you are trying to make @x1, you need to remove ",recipeName));
					for(int i=0;i<extra.size();i++)
					{
						if(i==0)
							buf.append(extra.get(i).toLowerCase());
						else
						if(i==extra.size()-1)
							buf.append(L(", and @x1",extra.get(i).toLowerCase()));
						else
							buf.append(", " + extra.get(i).toLowerCase());
					}
					commonTell(mob,buf.toString()+".");
				}
				else
				if(missing.size()>0)
				{
					final StringBuffer buf=new StringBuffer(L("If you are trying to make @x1, you need to add ",recipeName));
					for(int i=0;i<missing.size();i++)
					{
						if(i==0)
							buf.append(missing.get(i).toLowerCase());
						else
						if(i==missing.size()-1)
							buf.append(L(", and @x1",missing.get(i).toLowerCase()));
						else
							buf.append(", "+missing.get(i).toLowerCase());
					}
					commonTell(mob,buf.toString()+".");
				}
			}
			return false;
		}
		final List<String> complaints=new ArrayList<String>();
		for(int vr=0;vr<perfectRecipes.size();vr++)
		{
			final List<String> Vr=perfectRecipes.get(vr);
			final List<Object> counts=countIngredients(Vr);
			final Integer amountMaking=(Integer)counts.get(0);
			final String recipeName=replacePercent(Vr.get(RCP_FINALFOOD),Vr.get(RCP_MAININGR).toLowerCase());
			if(counts.size()==1)
			{
				if(CMath.s_int(Vr.get(RCP_LEVEL))>xlevel(mob))
					complaints.add("If you are trying to make "+recipeName+", you need to wait until you are level "+CMath.s_int(Vr.get(RCP_LEVEL))+".");
				else
				{
					finalRecipe=Vr;
					finalAmount=amountMaking.intValue();
					break;
				}
			}
			else
			if(amountMaking.intValue()<=0)
			{
				final StringBuffer buf=new StringBuffer(L("If you are trying to make @x1, you need to add a little more ",recipeName));
				for(int i=1;i<counts.size();i++)
				{
					if(i==1)
						buf.append(((String)counts.get(i)).toLowerCase());
					else
					if(i==counts.size()-1)
						buf.append(L(", and @x1",((String)counts.get(i)).toLowerCase()));
					else
						buf.append(", "+((String)counts.get(i)).toLowerCase());
				}
				complaints.add(buf.toString());
			}
			else
			if(amountMaking.intValue()>0)
			{
				final StringBuffer buf=new StringBuffer(L("If you are trying to make @x1, you need to remove some of the ",recipeName));
				for(int i=1;i<counts.size();i++)
				{
					if(i==1)
						buf.append(((String)counts.get(i)).toLowerCase());
					else
					if(i==counts.size()-1)
						buf.append(L(", and @x1",((String)counts.get(i)).toLowerCase()));
					else
						buf.append(", "+((String)counts.get(i)).toLowerCase());
				}
				complaints.add(buf.toString());
			}
		}
		if(finalRecipe==null)
		{
			for(int c=0;c<complaints.size();c++)
				commonTell(mob,(complaints.get(c)));
			return false;
		}

		buildingI=buildItem(mob,finalRecipe,cookingPot.getDeepContents());
		duration=getDuration(mob, CMath.isInteger(finalRecipe.get(RCP_LEVEL))?CMath.s_int(finalRecipe.get(RCP_LEVEL)):1);
		//***********************************************
		//* done figuring out recipe
		//***********************************************
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		recipeLevel=CMath.s_int(finalRecipe.get(RCP_LEVEL));
		final CMMsg msg=CMClass.getMsg(mob,cookingPot,this,getActivityMessageType(),getActivityMessageType(),getActivityMessageType(),L("<S-NAME> start(s) @x1 something in <T-NAME>.",cookWord()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			cookingPot=(Container)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,buildingI,recipeLevel,enhancedTypes);
		}
		return true;
	}
}
