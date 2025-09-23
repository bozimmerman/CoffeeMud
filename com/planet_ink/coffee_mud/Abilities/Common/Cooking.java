package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Consumables;
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
	public String getRecipeFormat()
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
	protected String				defaultFoodSound	= "sizzle.wav";
	protected String				defaultDrinkSound	= "liquid.wav";

	protected PairList<PotIngredient, Integer>	potContents		= null;

	public Cooking()
	{
		super();
		displayText=L("You are @x1...",cookWord());
		verb=cookWord();
	}

	private static class PotIngredient
	{
		public String rscName = null;
		public String rscCat = null;
		public String matName = null;
		public String subType = null;
		public String secretIdentity = null;
		public String itemName = null;

		@Override
		public boolean equals(final Object o)
		{
			if(!(o instanceof PotIngredient))
				return false;
			final PotIngredient oth = (PotIngredient)o;
			return java.util.Objects.equals(rscName, oth.rscName) &&
				java.util.Objects.equals(rscCat, oth.rscCat) &&
				java.util.Objects.equals(matName, oth.matName) &&
				java.util.Objects.equals(subType, oth.subType) &&
				java.util.Objects.equals(secretIdentity, oth.secretIdentity) &&
				java.util.Objects.equals(itemName, oth.itemName);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
		}
	}

	private static class Ingredients
	{
		final int amount;
		final List<String> list;
		public Ingredients(final int amount, final List<String> list)
		{
			this.amount=amount;
			this.list=list;
		}
	}

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(40,mob,level,5);
	}

	@Override
	public Pair<String,Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RCP_FINALNAME ),
				Integer.valueOf(CMath.s_int(recipe.get( RCP_LEVEL ))));
	}

	protected boolean isInnerCookOven(final Container cooking, final boolean mustBeBurning)
	{
		if(cooking.owner() instanceof Room)
		{
			for(final Item I : cooking.getContents())
				if(CMLib.flags().isOnFire(I))
					return true;
			if(mustBeBurning)
				return false;
			for(final Item I : cooking.getContents())
				if(CMLib.materials().getBurnDuration(I)>0)
					return true;
		}
		return false;
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
			||((cooking.container()!=null)&&(CMLib.flags().isOnFire(cooking.container())))
			||(isInnerCookOven(cooking,false))))
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
			||(!contentsSame(getPotIngredients(),potContents))
			||(requireFire()
				&&(!isInnerCookOven(cookingPot,true))
				&&(getRequiredFire(mob,0)==null)
				&&(mob.location()==activityRoom)))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonTelL(mob,"You start @x1 up some @x2.",cookWord(),finalDishName);
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
	public String getRecipeFilename()
	{
		return "recipes.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		final StringBuilder desc = new StringBuilder("");
		for(int vr=RCP_MAININGR;vr<recipe.size();vr+=2)
		{
			final int amt = CMath.s_int((vr+1<recipe.size())?recipe.get(vr+1):"1");
			final String ingredient=recipe.get(vr);
			if(ingredient.length()>0)
			{
				if(amt<0)
					desc.append("("+ingredient).append("), ");
				else
					desc.append(ingredient).append(", ");
			}
		}
		if(desc.length()<3)
			return "Nothing";
		return desc.toString().substring(0,desc.length()-2);
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
					final List<Item> V=getPotContents();
					for(int v=0;v<V.size();v++)
						V.get(v).destroy();
					if(cookingPot instanceof Drink)
					{
						if(buildingI.phyStats().weight()>0)
						{
							if(((Drink)cookingPot).liquidRemaining()>buildingI.phyStats().weight())
								((Drink)cookingPot).setLiquidRemaining(((Drink)cookingPot).liquidRemaining()-buildingI.phyStats().weight());
							else
								((Drink)cookingPot).setLiquidRemaining(0);
						}
						else
							((Drink)cookingPot).setLiquidRemaining(0);
					}
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
									cookingPot.owner().addItem(food,ItemPossessor.Expire.Player_Drop);
								else
								if(cookingPot.owner() instanceof MOB)
									cookingPot.owner().addItem(food);
								CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this, buildingI);
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

	protected boolean contentsSame(final PairList<PotIngredient, Integer> h1, final PairList<PotIngredient, Integer> h2)
	{
		if(h1.size()!=h2.size())
			return false;
		for(final Pair<PotIngredient, Integer> p1 : h1)
		{
			final Pair<PotIngredient, Integer> p2 = findContent(p1.first, h2);
			if(p2 == null)
				return false;
			if(p1.second.intValue()!=p2.second.intValue())
				return false;
		}
		return true;
	}

	protected Pair<PotIngredient,Integer> findContent(final PotIngredient i, final PairList<PotIngredient, Integer> potContents)
	{
		for(final Pair<PotIngredient,Integer> oth : potContents)
			if(oth.first.equals(i))
				return oth;
		return null;
	}

	protected Pair<PotIngredient,Integer> ensureContent(final PotIngredient i, final PairList<PotIngredient ,Integer> potContents)
	{
		Pair<PotIngredient, Integer> p = findContent(i,potContents);
		if(p == null)
		{
			p = new Pair<PotIngredient, Integer>(i,Integer.valueOf(0));
			potContents.add(p);
		}
		return p;
	}

	protected PairList<PotIngredient,Integer> getPotIngredients()
	{
		final PairList<PotIngredient,Integer> potIngredients=new PairArrayList<PotIngredient,Integer>();
		final Container pot = this.cookingPot;
		if(pot != null)
		{
			if((pot instanceof Drink)
			&&(((Drink)pot).liquidRemaining()>0))
			{
				final PotIngredient i = new PotIngredient();
				final int amt = ((Drink)pot).liquidRemaining()/10;
				int material;
				if(pot instanceof RawMaterial)
				{
					material = pot.material();
					i.itemName = pot.Name();
					i.secretIdentity = pot.secretIdentity();
				}
				else
				{
					material = ((Drink)pot).liquidType();
					i.secretIdentity = "";
				}
				i.rscName = RawMaterial.CODES.NAME(material);
				i.matName = RawMaterial.CODES.MAT_NAME(material);
				if(i.itemName == null)
					i.itemName = i.rscName.toLowerCase();
				final Pair<PotIngredient,Integer> p = ensureContent(i, potIngredients);
				p.second = Integer.valueOf(amt);
			}
			if(pot.owner()==null)
				return potIngredients;
			final List<Item> V=getPotContents();
			for(int v=0;v<V.size();v++)
			{
				final Item I=V.get(v);
				final PotIngredient i = new PotIngredient();
				if(I instanceof RawMaterial)
				{
					i.rscName=RawMaterial.CODES.NAME(I.material()).toUpperCase();
					i.matName=RawMaterial.CODES.MAT_NAME(I.material()).toUpperCase();
					if(((RawMaterial)I).getSubType().trim().length()>0)
						i.subType = ((RawMaterial)I).getSubType().trim().toUpperCase();
					if(CMParms.indexOf( RawMaterial.CODES.FISHES(), I.material())>=0)
						i.rscCat = "FISH";
					else
					if(CMParms.indexOf( RawMaterial.CODES.BERRIES(), I.material())>=0)
						i.rscCat = "BERRIES";
					i.itemName = i.rscName;
				}
				else
				if((((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
					||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
					||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH))
				&&(CMParms.parse(I.name()).size()>0))
					i.itemName=CMParms.parse(I.name()).lastElement();
				else
					i.itemName=I.name();
				i.secretIdentity = I.secretIdentity();
				final Pair<PotIngredient,Integer> use = ensureContent(i, potIngredients);
				if(I instanceof RawMaterial)
					use.second = Integer.valueOf(use.second.intValue() + I.phyStats().weight());
				else
					use.second = Integer.valueOf(use.second.intValue() + 1);
			}
		}
		return potIngredients;
	}

	protected Ingredients countIngredients(final List<String> Vr)
	{
		final PairList<PotIngredient,int[]> contents=new PairArrayList<PotIngredient,int[]>(potContents.size());
		for(final Pair<PotIngredient, Integer> ingr  : potContents)
			contents.add(ingr.first,new int[] { ingr.second.intValue() });
		int amountMade=0;
		final Ingredients codedList;
		final List<String> ranOutOfList=new ArrayList<String>();
		final List<String> notEnoughList=new ArrayList<String>();
		while((ranOutOfList.size()==0)&&(notEnoughList.size()==0))
		{
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				final String recipeIngredient=Vr.get(vr).toUpperCase().trim();
				if(recipeIngredient.length()>0)
				{
					int recipeIngredientReqAmt=1;
					if(vr<Vr.size()-1)
						recipeIngredientReqAmt=CMath.s_int(Vr.get(vr+1));
					if(recipeIngredientReqAmt==0)
						recipeIngredientReqAmt=1;
					if(recipeIngredientReqAmt<0)
						recipeIngredientReqAmt=recipeIngredientReqAmt*-1;
					if(recipeIngredient.equalsIgnoreCase("water"))
						recipeIngredientReqAmt=recipeIngredientReqAmt*10;
					for(final Pair<PotIngredient,int[]> i : contents)
					{
						final int amount2=i.second[0];
						final PotIngredient potIngredient=i.first;
						if(this.ingredientMatch(potIngredient, recipeIngredient, false))
						{
							i.second[0]=amount2-recipeIngredientReqAmt;
							if(i.second[0]<0)
								notEnoughList.add(recipeIngredient.toLowerCase());
							if(i.second[0]==0)
								ranOutOfList.add(recipeIngredient.toLowerCase());
						}
					}
				}
			}
			if(notEnoughList.size()==0)
				amountMade++;
		}
		if(notEnoughList.size()>0)
			codedList=new Ingredients(-amountMade, notEnoughList);
		else
		{
			final List<String> list=new ArrayList<String>();
			for(final Pair<PotIngredient,int[]> i : contents)
			{
				final PotIngredient potIngredient=i.first;
				final int amount2=i.second[0];
				if((amount2>0)
				&&((!honorHerbs())||(potIngredient.rscName==null)||(!potIngredient.rscName.equalsIgnoreCase("HERBS")))
				&&((potIngredient.rscName==null)||(!potIngredient.rscName.equalsIgnoreCase("WATER"))))
				{
					if(potIngredient.rscName == null)
						list.add(potIngredient.itemName);
					else
					if((RawMaterial.CODES.FIND_IgnoreCase(potIngredient.rscName)&RawMaterial.MATERIAL_MASK) != RawMaterial.MATERIAL_LIQUID)
						list.add(potIngredient.itemName);
				}
			}
			codedList=new Ingredients(amountMade, list);
		}
		return codedList;
	}

	private boolean ingredientMatch(final PotIngredient potIngr, String recipeIng, final boolean perfectOnly)
	{
		if(recipeIng.length()>0)
		{
			recipeIng = recipeIng.toUpperCase().trim();
			if(potIngr.rscName != null)
			{
				if(potIngr.subType != null)
				{
					if(potIngr.subType.equals(recipeIng))
						return true;
				}
				if(potIngr.rscCat != null)
				{
					if(potIngr.rscCat.equals(recipeIng))
						return true;
				}
				if((potIngr.subType == null)
				&& (potIngr.rscCat == null))
				{
					final int rsc = RawMaterial.CODES.FIND_CaseSensitive(recipeIng);
					if((rsc > 0)&&(recipeIng.equals(potIngr.rscName)))
						return true;
					final RawMaterial.Material mat = RawMaterial.Material.find(recipeIng);
					if((mat != null)&&(recipeIng.equals(potIngr.rscName)))
						return true;
				}
				if(perfectOnly)
					return false;
				if(potIngr.rscName != null)
				{
					if(potIngr.rscName.equals(recipeIng))
						return true;
				}
				if(potIngr.matName != null)
				{
					if(potIngr.matName.equals(recipeIng))
						return true;
				}
			}
			else
			if(!perfectOnly)
			{
				if(potIngr.itemName != null)
				{
					final String s = potIngr.itemName.toUpperCase();
					if(s.endsWith(recipeIng)
					&&((s.length() == recipeIng.length())
						||(!Character.isLetterOrDigit(s.charAt(s.length()-recipeIng.length()-1)))))
						return true;
				}
				if(potIngr.secretIdentity != null)
				{
					final String s = potIngr.secretIdentity.toUpperCase();
					if(s.endsWith(recipeIng)
					&&((s.length() == recipeIng.length())
						||(!Character.isLetterOrDigit(s.charAt(s.length()-recipeIng.length()-1)))))
						return true;
				}
			}
		}
		return false;
	}

	private boolean recipeHasSomeIngredientsInPot(final List<String> recipe)
	{
		boolean found=false;
		final String recipeIngredient = recipe.get(RCP_MAININGR).toUpperCase();
		for(final Pair<PotIngredient,Integer> potIngredient : potContents)
		{
			found = ingredientMatch(potIngredient.first,recipeIngredient,false);
			if(found)
				break;
		}
		return found;
	}

	private boolean isIngredientInRecipe(final PotIngredient potIngredient, final List<String> recipe, final boolean perfectOnly)
	{
		boolean found = false;
		for(int vr=RCP_MAININGR;vr<recipe.size();vr+=2)
		{
			final String ingredient2=recipe.get(vr).toUpperCase();
			found = ingredientMatch(potIngredient, ingredient2, perfectOnly);
			if(found)
				break;
		}
		return found;
	}

	private boolean isIngredientInPot(final String recipeIngredient, final PairList<PotIngredient, Integer> potContents, final boolean perfectOnly)
	{
		boolean found = false;
		for(final Pair<PotIngredient, Integer> potIngredient : potContents)
		{
			found = ingredientMatch(potIngredient.first, recipeIngredient, perfectOnly);
			if(found)
				break;
		}
		return found;
	}

	private List<String> extraIngredientsInPot(final List<String> Vr, final boolean perfectOnly)
	{
		final List<String> extra=new ArrayList<String>();
		for(final Pair<PotIngredient,Integer> potIngredient : potContents)
		{
			boolean found;
			if(honorHerbs()
			&&(potIngredient.first.rscName!=null)
			&&potIngredient.first.rscName.toUpperCase().equals("HERBS")) // herbs exception
				found=true;
			else
				found = isIngredientInRecipe(potIngredient.first, Vr, perfectOnly);
			if(!found)
				extra.add(potIngredient.first.itemName);
		}
		return extra;
	}

	public List<String> missingIngredientsFromPot(final List<String> Vr, final boolean perfectOnly)
	{
		final List<String> missing=new ArrayList<String>();

		for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
		{
			final String recipeIngredient=Vr.get(vr);
			if(recipeIngredient.length()>0)
			{
				int amount=1;
				if(vr<Vr.size()-1)
					amount=CMath.s_int(Vr.get(vr+1));
				final boolean found= isIngredientInPot(recipeIngredient, potContents, perfectOnly);
				if(amount>=0)
				{
					if(!found)
						missing.add(recipeIngredient);
				}
			}
		}
		return missing;
	}

	public int homeCookValue(final MOB mob, final int multiplyer)
	{
		final int hc=getX1Level(mob);
		return hc*hc*multiplyer;
	}

	// this was necessary because ovens contain burnables AND food.
	protected List<Item> getPotContents()
	{
		final Container C = this.cookingPot;
		if(C != null)
		{
			final List<Item> contents = new XVector<Item>(C.getDeepContents());
			int fireMat = -1;
			for(final Iterator<Item> i=contents.iterator();i.hasNext();)
			{
				final Item I = i.next();
				if(CMLib.flags().isOnFire(I))
					fireMat=I.material();
			}
			if(fireMat > 0)
			{
				for(final Iterator<Item> i=contents.iterator();i.hasNext();)
				{
					final Item I = i.next();
					if(I.material()==fireMat)
						i.remove();
				}
			}
			return new ReadOnlyList<Item>(contents);
		}
		return new ArrayList<Item>(0);
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
		double ingredientsWeight = 0.0;
		if(contents!=null)
		{
			for(int v=0;v<contents.size();v++)
			{
				final Item I=contents.get(v);
				if((I.material()!=RawMaterial.RESOURCE_HERBS)||(!honorHerbs()))
					ingredientsWeight += I.basePhyStats().weight();
			}
		}
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
					if((I instanceof Food)
					&&(material<0))
					{
						switch(I.material()&RawMaterial.MATERIAL_MASK)
						{
						case RawMaterial.MATERIAL_VEGETATION:
						case RawMaterial.MATERIAL_FLESH:
							material=I.material();
							break;
						}
					}
				}
				if(ingredientsWeight < finalAmount)
					ingredientsWeight = finalAmount;
				food.basePhyStats().setWeight((int)Math.round(ingredientsWeight));
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
			food.basePhyStats().setWeight((int)Math.round(CMath.div(food.basePhyStats().weight(),finalAmount)));
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
			if(ingredientsWeight >= finalAmount)
				buildingI.basePhyStats().setWeight((int)Math.round(CMath.div(ingredientsWeight,finalAmount)));
			else
			if(ingredientsWeight>0)
				buildingI.basePhyStats().setWeight((int)Math.round(ingredientsWeight));

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
			if(ingredientsWeight >= finalAmount)
				buildingI.basePhyStats().setWeight((int)Math.round(CMath.div(ingredientsWeight,finalAmount)));
			else
			if(ingredientsWeight>0)
				buildingI.basePhyStats().setWeight((int)Math.round(ingredientsWeight));
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
			if(ingredientsWeight >= finalAmount)
				buildingI.basePhyStats().setWeight((int)Math.round(CMath.div(ingredientsWeight,finalAmount)));
			else
			if(ingredientsWeight>0)
				buildingI.basePhyStats().setWeight((int)Math.round(ingredientsWeight));
			else
				buildingI.basePhyStats().setWeight(1);
			playSound=defaultFoodSound;
		}

		if(buildingI!=null)
		{
			if(buildingI instanceof RawMaterial)
				buildingI.setSecretIdentity(buildingI.name());
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
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new ArrayList<CraftedItem>(0));
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
									final int asLevel, final int autoGenerate, final boolean forceLevels, final List<CraftedItem> crafted)
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
		potContents=null;
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
			final int duration=getDuration(mob, 1);
			crafted.add(new CraftedItem(buildingI,null,duration));
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
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? allRecipes : super.matchingRecipes(allRecipes, mask, true);
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
			commonTelL(mob,"@x1\n\rIngredients beginning with the ~ character are optional additives.",buf.toString());
			enhanceList(mob);
			return true;
		}
		final Item possibleContainer=possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);
		final Item target=getTarget(mob,mob.location(),givenTarget,possibleContainer,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
		{
			commonFaiL(mob,commands,"The syntax for this skill is @x1 [CONTAINER]",triggerStrings()[0]);
			return false;
		}

		if(!(target instanceof Container))
		{
			commonFaiL(mob,commands,"There's nothing in @x1 to @x2!",target.name(mob),cookWordShort());
			return false;
		}
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A instanceof Cooking) && (((Cooking)A).cookingPot==target))
			{
				commonFaiL(mob,commands,"That is already in use.");
				return false;
			}
		}
		if(!isMineForCooking(mob,(Container)target))
		{
			commonFaiL(mob,commands,"You probably need to pick that up first.");
			return false;
		}
		if(!meetsLidRequirements(mob,(Container)target))
		{
			commonFaiL(mob,commands,"You need a closeable container to bake that in, and you need to close it to begin.");
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
			commonFaiL(mob,commands,"@x1 is not suitable to @x2 in.",target.name(mob),cookWordShort());
			return false;
		}

		cookingPot=(Container)target;
		if((requireFire())
		&&(!isInnerCookOven(cookingPot,true)))
		{
			final Item fire=getRequiredFire(mob,0);
			if(fire==null)
				return false;
		}

		messedUp=!proficiencyCheck(mob,0,auto);
		int duration=getDuration(mob, 1);
		potContents=getPotIngredients();

		//***********************************************
		//* figure out recipe
		//***********************************************
		List<List<String>> perfectRecipes=new ArrayList<List<String>>();
		final List<List<String>> closerRecipes=new ArrayList<List<String>>();
		final List<List<String>> closeRecipes=new ArrayList<List<String>>();
		for(int v=0;v<allRecipes.size();v++)
		{
			final List<String> recipeV=allRecipes.get(v);
			if(recipeHasSomeIngredientsInPot(recipeV))
				closeRecipes.add(recipeV);
			if((missingIngredientsFromPot(recipeV,true).size()==0)
			&&(extraIngredientsInPot(recipeV,true).size()==0))
				perfectRecipes.add(recipeV);
			if((missingIngredientsFromPot(recipeV,false).size()==0)
			&&(extraIngredientsInPot(recipeV,false).size()==0))
				closerRecipes.add(recipeV);
		}

		if(perfectRecipes.size()==0)
			perfectRecipes=closerRecipes;
		if(perfectRecipes.size()==0)
		{
			if(closeRecipes.size()==0)
			{
				commonFaiL(mob,commands,"You don't know how to make anything out of those ingredients.  Have you tried LIST as a parameter?");
				return false;
			}
			for(int vr=0;vr<closeRecipes.size();vr++)
			{
				final List<String> recipeV=closeRecipes.get(vr);
				final List<String> missing=missingIngredientsFromPot(recipeV,false);
				final List<String> extra=extraIngredientsInPot(recipeV,false);
				final String recipeName=replacePercent(recipeV.get(RCP_FINALFOOD),recipeV.get(RCP_MAININGR).toLowerCase());
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
			final Ingredients ingrs=countIngredients(Vr);
			final Integer amountMaking=Integer.valueOf(ingrs.amount);
			final String recipeName=replacePercent(Vr.get(RCP_FINALFOOD),Vr.get(RCP_MAININGR).toLowerCase());
			if(ingrs.list.size()==0)
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
				for(int i=0;i<ingrs.list.size();i++)
				{
					if(i==0)
						buf.append(ingrs.list.get(i).toLowerCase());
					else
					if(i==ingrs.list.size()-1)
						buf.append(L(", and @x1",ingrs.list.get(i).toLowerCase()));
					else
						buf.append(", "+ingrs.list.get(i).toLowerCase());
				}
				complaints.add(buf.toString());
			}
			else
			if(amountMaking.intValue()>0)
			{
				final StringBuffer buf=new StringBuffer(L("If you are trying to make @x1, you need to remove some of the ",recipeName));
				for(int i=0;i<ingrs.list.size();i++)
				{
					if(i==0)
						buf.append(ingrs.list.get(i).toLowerCase());
					else
					if(i==ingrs.list.size()-1)
						buf.append(L(", and @x1",ingrs.list.get(i).toLowerCase()));
					else
						buf.append(", "+ingrs.list.get(i).toLowerCase());
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

		buildingI=buildItem(mob,finalRecipe,getPotContents());
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
