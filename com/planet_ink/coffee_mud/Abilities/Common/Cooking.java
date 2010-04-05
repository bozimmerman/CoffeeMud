package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Cooking extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "Cooking"; }
	public String name(){ return "Cooking";}
	private static final String[] triggerStrings = {"COOK","COOKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "cook";}
	public String cookWord(){return "cooking";}
	public boolean honorHerbs(){return true;}
	public boolean requireFire(){return true;}
	public boolean requireLid(){return false;}
    public String supportedResourceString(){return "MISC";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tFOOD_DRINK||RESOURCE_NAME\tSMELL_LIST||CODED_SPELL_LIST\tITEM_LEVEL\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED\t"
        +"RESOURCE_OR_KEYWORD\tOPTIONAL_AMOUNT_REQUIRED";}

	public static int RCP_FINALFOOD=0;
	public static int RCP_FOODDRINK=1;
	public static int RCP_BONUSSPELL=2;
	public static int RCP_LEVEL=3;
	public static int RCP_MAININGR=4;
	public static int RCP_MAINAMNT=5;

	protected Container cookingPot=null;
    protected String finalDishName=null;
    protected int finalAmount=0;
    protected Vector finalRecipe=null;
    protected Hashtable oldPotContents=null;
    protected String defaultFoodSound="sizzle.wav";
    protected String defaultDrinkSound="liquid.wav";
     
	public Cooking()
	{
		super();
		displayText="You are "+cookWord()+"...";
		verb=cookWord();
	}

	public boolean isMineForCooking(MOB mob, Container cooking)
	{
		if(mob.isMine(cooking)) 
		    return true;
		if((mob.location()==cooking.owner())
		&&((CMLib.flags().isOnFire(cooking))
		    ||(!requireFire())
			||((cooking.container()!=null)&&(CMLib.flags().isOnFire(cooking.container())))))
		   return true;
		return false;
	}

	public boolean meetsLidRequirements(MOB mob, Container cooking)
	{
	    if(!requireLid()) return true;
	    if((cooking.hasALid())&&(!cooking.isOpen())) 
	        return true;
		if((cooking.container()!=null)
		&&(cooking.container() instanceof Container)
		&&(((Container)cooking.container()).hasALid())
		&&(!((Container)cooking.container()).isOpen()))
		   return true;
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((cookingPot==null)
			||(building==null)
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
				commonTell(mob,"You start "+cookWord()+" up some "+finalDishName+".");
				displayText="You are "+cookWord()+" "+finalDishName;
				verb=cookWord()+" "+finalDishName;
			}
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "recipes.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				if((cookingPot!=null)&&(finalRecipe!=null)&&(building!=null))
				{
					Vector V=cookingPot.getContents();
					for(int v=0;v<V.size();v++)
						((Item)V.elementAt(v)).destroy();
					if((cookingPot instanceof Drink)&&(building instanceof Drink))
						((Drink)cookingPot).setLiquidRemaining(0);
					if(!aborted)
					for(int i=0;i<finalAmount*(abilityCode());i++)
					{
						Item food=((Item)building.copyOf());
						food.setMiscText(building.text());
						food.recoverEnvStats();
						if(cookingPot.owner() instanceof Room)
							((Room)cookingPot.owner()).addItemRefuse(food,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
						else
						if(cookingPot.owner() instanceof MOB)
							((MOB)cookingPot.owner()).addInventory(food);
						food.setContainer(cookingPot);
						if(((food.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
						&&(cookingPot instanceof Drink))
							((Drink)cookingPot).setLiquidRemaining(0);
							
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean contentsSame(Hashtable h1, Hashtable h2)
	{
		if(h1.size()!=h2.size()) return false;
		for(Enumeration e=h1.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			Integer INT1=(Integer)h1.get(key);
			Integer INT2=(Integer)h2.get(key);
			if((INT1==null)||(INT2==null)) return false;
			if(INT1.intValue()!=INT2.intValue()) return false;
		}
		return true;
	}

	public Hashtable potContents(Container pot)
	{
		Hashtable h=new Hashtable();
		if((pot instanceof Drink)&&(((Drink)pot).liquidRemaining()>0))
		{
			if(pot instanceof RawMaterial)
				h.put(RawMaterial.CODES.NAME(((RawMaterial)pot).material())+"/",Integer.valueOf(((Drink)pot).liquidRemaining()/10));
			else
				h.put(RawMaterial.CODES.NAME(((Drink)pot).liquidType())+"/",Integer.valueOf(((Drink)pot).liquidRemaining()/10));
		}
		if(pot.owner()==null) return h;
		Vector V=pot.getContents();
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.elementAt(v);
			String ing="Unknown";
			if(I instanceof RawMaterial)
				ing=RawMaterial.CODES.NAME(I.material());
			else
			if((((I.material()&RawMaterial.MATERIAL_VEGETATION)>0)
				||((I.material()&RawMaterial.MATERIAL_LIQUID)>0)
				||((I.material()&RawMaterial.MATERIAL_FLESH)>0))
				&&(CMParms.parse(I.name()).size()>0))
					ing=((String)CMParms.parse(I.name()).lastElement()).toUpperCase();
			else
				ing=I.name();
			Integer INT=(Integer)h.get(ing+"/"+I.secretIdentity().toUpperCase()+"/"+I.Name().toUpperCase()+"/");
			if(INT==null) INT=Integer.valueOf(0);
			INT=Integer.valueOf(INT.intValue()+1);
			h.put(ing+"/"+I.secretIdentity().toUpperCase()+"/"+I.Name().toUpperCase()+"/",INT);
		}
		return h;
	}


	public Vector countIngredients(Vector Vr)
	{
		String[] contents=new String[oldPotContents.size()];
		int[] amounts=new int[oldPotContents.size()];
		int numIngredients=0;
		for(Enumeration e=oldPotContents.keys();e.hasMoreElements();)
		{
			contents[numIngredients]=(String)e.nextElement();
			amounts[numIngredients]=((Integer)oldPotContents.get(contents[numIngredients])).intValue();
			numIngredients++;
		}

		int amountMade=0;

		Vector codedList=new Vector();
		boolean RanOutOfSomething=false;
		boolean NotEnoughForThisRun=false;
		while((!RanOutOfSomething)&&(!NotEnoughForThisRun))
		{
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				String ingredient=((String)Vr.elementAt(vr)).toUpperCase();
				if(ingredient.length()>0)
				{
					int amount=1;
					if(vr<Vr.size()-1)amount=CMath.s_int((String)Vr.elementAt(vr+1));
					if(amount==0) amount=1;
					if(amount<0) amount=amount*-1;
					if(ingredient.equalsIgnoreCase("water"))
						amount=amount*10;
					for(int i=0;i<contents.length;i++)
					{
						String ingredient2=contents[i].toUpperCase();
						int amount2=amounts[i];
						if(ingredient2.indexOf(ingredient+"/")>=0)
						{
							amounts[i]=amount2-amount;
							if(amounts[i]<0) NotEnoughForThisRun=true;
							if(amounts[i]==0) RanOutOfSomething=true;
						}
					}
				}
			}
			if(!NotEnoughForThisRun) amountMade++;
		}
		if(NotEnoughForThisRun)
		{
			codedList.addElement(Integer.valueOf(-amountMade));
			for(int i=0;i<contents.length;i++)
				if(amounts[i]<0)
				{
					String content=contents[i];
					if(content.indexOf("/")>=0)
						content=content.substring(0,content.indexOf("/"));
					codedList.addElement(content);
				}
		}
		else
		{
			codedList.addElement(Integer.valueOf(amountMade));
			for(int i=0;i<contents.length;i++)
			{
				String ingredient2=contents[i];
				int amount2=amounts[i];
				if((amount2>0)
				&&((!honorHerbs())||(!ingredient2.toUpperCase().startsWith("HERBS/")))
				&&(!ingredient2.toUpperCase().startsWith("WATER/")))
				{
					String content=contents[i];
					if(content.indexOf("/")>=0)
						content=content.substring(0,content.indexOf("/"));
					codedList.addElement(content);
				}
			}
		}

		return codedList;
	}

	public Vector extraIngredientsInOldContents(Vector Vr, boolean perfectOnly)
	{
		Vector extra=new Vector();
		for(Enumeration e=oldPotContents.keys();e.hasMoreElements();)
		{
			boolean found=false;
			String ingredient=(String)e.nextElement();

			if(honorHerbs()&&ingredient.toUpperCase().startsWith("HERBS/")) // herbs exception
				found=true;
			else
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				String ingredient2=((String)Vr.elementAt(vr)).toUpperCase();
				if((ingredient2.length()>0)
				&&(((!perfectOnly)&&(ingredient.toUpperCase().indexOf(ingredient2+"/"))>=0)
				||((perfectOnly)&&ingredient.toUpperCase().equalsIgnoreCase(ingredient2))))
					found=true;
			}
			if(!found)
			{
				String content=ingredient;
				if(content.indexOf("/")>=0)
					content=content.substring(0,content.indexOf("/"));
				extra.addElement(content);
			}
		}
		return extra;
	}

	public Vector missingIngredientsFromOldContents(Vector Vr, boolean perfectOnly)
	{
		Vector missing=new Vector();

		String possiblyMissing=null;
		boolean foundOptional=false;
		boolean hasOptional=false;
		for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
		{
			String ingredient=(String)Vr.elementAt(vr);
			if(ingredient.length()>0)
			{
				int amount=1;
				if(vr<Vr.size()-1)amount=CMath.s_int((String)Vr.elementAt(vr+1));
				boolean found=false;
				for(Enumeration e=oldPotContents.keys();e.hasMoreElements();)
				{
					String ingredient2=((String)e.nextElement()).toUpperCase();
					if((ingredient2.indexOf(ingredient.toUpperCase()+"/")>=0)
					||((!perfectOnly)&&ingredient2.equalsIgnoreCase(ingredient.toUpperCase())))
					{ found=true; break;}
				}
				if(amount>=0)
				{
					if(!found)
						missing.addElement(ingredient);
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
			missing.addElement(possiblyMissing);
		return missing;
	}

	public int homeCookValue(MOB mob, int multiplyer)
	{
		int hc=getX1Level(mob);
		return hc*hc*multiplyer;
	}

	public Item buildItem(MOB mob, Vector finalRecipe, Vector contents)
	{
		String replaceName=((String)finalRecipe.elementAt(RCP_MAININGR));
		if(contents!=null)
		for(int v=0;v<contents.size();v++)
		{
			Item I=(Item)contents.elementAt(v);
			if((I instanceof RawMaterial)
			&&(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase((String)finalRecipe.elementAt(RCP_MAININGR))))
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
					name=name.trim();
					int x=name.lastIndexOf(" ");
					if((x>0)&&(!name.substring(x+1).trim().equalsIgnoreCase("of")))
						replaceName=name.substring(x+1);
					else
						replaceName=name;
				}
				break;
			}
		}
		finalDishName=replacePercent((String)finalRecipe.elementAt(RCP_FINALFOOD),
									CMStrings.capitalizeAndLower(replaceName));
		String foodType=(String)finalRecipe.elementAt(RCP_FOODDRINK);
		if(foodType.equalsIgnoreCase("FOOD"))
		{
			building=CMClass.getItem("GenFood");
			Food food=(Food)building;
			building.setName(((messedUp)?"burnt ":"")+finalDishName);
			building.setDisplayText("some "+((messedUp)?"burnt ":"")+finalDishName+" has been left here");
			building.setDescription("It looks "+((messedUp)?"burnt!":"good!"));
			food.setNourishment(0);
			if(!messedUp)
			{
				boolean timesTwo=false;
				if(contents!=null)
				for(int v=0;v<contents.size();v++)
				{
					Item I=(Item)contents.elementAt(v);
					if((I.material()==RawMaterial.RESOURCE_HERBS)&&(honorHerbs()))
						timesTwo=true;
					else
					if(I instanceof Food)
						food.setNourishment(food.nourishment()+(((Food)I).nourishment()+((Food)I).nourishment()));
					else
						food.setNourishment(food.nourishment()+10);
				}
				if(timesTwo) food.setNourishment(food.nourishment()*2);
                if(food.nourishment()>300)
                    food.setBite((int)Math.round(Math.ceil(CMath.div(food.nourishment(),2))));
			}
            ((Food)building).setMaterial(RawMaterial.RESOURCE_BEEF);
            if(contents!=null)
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				if((I.material()!=RawMaterial.RESOURCE_HERBS)||(!honorHerbs()))
					food.baseEnvStats().setWeight(food.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
                if(I instanceof Food)
                    switch(((Food)I).material()&RawMaterial.MATERIAL_MASK)
                    {
                    case RawMaterial.MATERIAL_VEGETATION:
                        food.setMaterial(((Food)I).material());
                        break;
                    case RawMaterial.MATERIAL_FLESH:
                        food.setMaterial(((Food)I).material());
                        break;
                    }
			}
            if(mob!=null)
				food.setNourishment((food.nourishment()+homeCookValue(mob,10))/finalAmount);
			food.baseEnvStats().setWeight(food.baseEnvStats().weight()/finalAmount);
            playSound=defaultFoodSound;
		}
		else
		if(foodType.equalsIgnoreCase("DRINK"))
		{
			building=CMClass.getItem("GenLiquidResource");
			//building.setMiscText(cooking.text());
			//building.recoverEnvStats();
			building.setName(((messedUp)?"spoiled ":"")+finalDishName);
			building.setDisplayText("some "+((messedUp)?"spoiled ":"")+finalDishName+" has been left here.");
			building.setDescription("It looks "+((messedUp)?"spoiled!":"good!"));
			Drink drink=(Drink)building;
            int liquidType=RawMaterial.RESOURCE_FRESHWATER;
            if(contents!=null)
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
				if(I instanceof Food)
					drink.setLiquidRemaining(drink.liquidRemaining()+((Food)I).nourishment());
                if((I instanceof Drink)&&((((Drink)I).liquidType()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
                    liquidType=((Drink)I).liquidType();
			}
			if(drink.liquidRemaining()>0)
			{
				drink.setLiquidRemaining(drink.liquidRemaining()+homeCookValue(mob,10));
				drink.setLiquidHeld(drink.liquidRemaining());
				drink.setThirstQuenched(drink.liquidRemaining());
			}
			else
			{
				drink.setLiquidHeld(1);
				drink.setLiquidRemaining(1);
				drink.setThirstQuenched(1);
			}
			drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()/finalAmount);
			if(messedUp)drink.setThirstQuenched(1);
            playSound=defaultDrinkSound;
            building.setMaterial(liquidType);
            drink.setLiquidType(liquidType);
    		CMLib.materials().addEffectsToResource((Item)drink);
		}
		else
		if(CMClass.getItem(foodType)!=null)
		{
			building=CMClass.getItem(foodType);
			building.setName(((messedUp)?"ruined ":"")+finalDishName);
			building.setDisplayText("some "+((messedUp)?"ruined ":"")+finalDishName+" has been left here");
			if(building instanceof Drink)
			{
				Drink drink=(Drink)building;
				int rem=drink.liquidHeld();
				drink.setLiquidRemaining(0);
                int liquidType=RawMaterial.RESOURCE_FRESHWATER;
                if(contents!=null)
				for(int v=0;v<contents.size();v++)
				{
					Item I=(Item)contents.elementAt(v);
					drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
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
				building.setMaterial(liquidType);
                drink.setLiquidType(liquidType);
			}
			building.baseEnvStats().setWeight(building.baseEnvStats().weight()/finalAmount);
            playSound=defaultFoodSound;
		}
		else
		{
			building=CMClass.getItem("GenResource");
			if(messedUp)
				building.setMaterial(RawMaterial.RESOURCE_DUST);
			else
			{
				int code = RawMaterial.CODES.FIND_IgnoreCase(foodType);
				if(code>=0)
					building.setMaterial(code);
			}
			building.setName(((messedUp)?"ruined ":"")+finalDishName);
			building.setDisplayText("some "+((messedUp)?"ruined ":"")+finalDishName+" has been left here");
			building.baseEnvStats().setWeight(building.baseEnvStats().weight()/finalAmount);
            playSound=defaultFoodSound;
		}
		
		if(building!=null)
		{
			if(mob!=null)
				building.setSecretIdentity("This was prepared by "+mob.Name()+".");
			String spell=(String)finalRecipe.elementAt(RCP_BONUSSPELL);
			if((spell!=null)&&(spell.length()>0))
			{
				if(building instanceof Perfume)
					((Perfume)building).setSmellList(spell);
				else
					addSpells(building,spell);
			}
			building.recoverEnvStats();
			building.text();
		}
		return building;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb=cookWord();
		cookingPot=null;
		finalRecipe=null;
		finalAmount=0;
		building=null;
		finalDishName=null;
		messedUp=false;
		oldPotContents=null;
		Vector allRecipes=addRecipes(mob,loadRecipes());
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			finalAmount=1;
			commands.removeElementAt(0);
			Vector finalRecipe=null;
			String recipeName=null;
			if((commands.size()>0)&&(commands.firstElement() instanceof String))
				recipeName=CMParms.combine(commands,0);
			else
			{
				Vector recipes=loadRecipes();
				Vector V=(Vector)recipes.elementAt(CMLib.dice().roll(1,recipes.size(),-1));
				recipeName=replacePercent((String)V.firstElement(),"");
			}
			for(int r=0;r<allRecipes.size();r++)
			{
				Vector Vr=(Vector)allRecipes.elementAt(r);
				if(Vr.size()>0)
				{
					String item=(String)Vr.elementAt(RCP_FINALFOOD);
					if(replacePercent(item,"").equalsIgnoreCase(recipeName))
					{ finalRecipe=Vr; break;}
				}
			}
			if(finalRecipe==null)
			for(int r=0;r<allRecipes.size();r++)
			{
				Vector Vr=(Vector)allRecipes.elementAt(r);
				if(Vr.size()>0)
				{
					String item=(String)Vr.elementAt(RCP_FINALFOOD);
					if(replacePercent(item,"").toLowerCase().indexOf(recipeName.toLowerCase())>=0)
					{ finalRecipe=Vr; break;}
				}
			}
			if(finalRecipe==null) return false;
			building=buildItem(mob,finalRecipe,null);
			commands.addElement(building);
			return true;
		}
		randomRecipeFix(mob,allRecipes,commands,-1);
		if((commands.size()>0)
		&&(commands.firstElement() instanceof String)
		&&((String)commands.firstElement()).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer(CMStrings.padRight("^xRecipe",20)+"^.^? ^wIngredients required^N\n\r");
			for(int r=0;r<allRecipes.size();r++)
			{
				Vector Vr=(Vector)allRecipes.elementAt(r);
				if(Vr.size()>0)
				{
					String item=(String)Vr.elementAt(RCP_FINALFOOD);
					if(item.length()==0) continue;
					int level=CMath.s_int((String)Vr.elementAt(RCP_LEVEL));
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						buf.append("^c"+CMStrings.padRight(CMStrings.capitalizeAndLower(replacePercent(item,"")),20)+"^w ");
						for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
						{
							String ingredient=(String)Vr.elementAt(vr);
							if(ingredient.length()>0)
							{
								int amount=1;
								if(vr<Vr.size()-1)amount=CMath.s_int((String)Vr.elementAt(vr+1));
								if(amount==0) amount=1;
								if(amount<0)
							    {
							        ingredient="~"+ingredient;
							        amount=amount*-1;
							    }
								if(ingredient.equalsIgnoreCase("water"))
									amount=amount*10;
								buf.append(ingredient.toLowerCase()+"("+amount+") ");
							}
						}
						buf.append("\n\r");
					}
				}
			}
			commonTell(mob,buf.toString()+"\n\rIngredients beginning with the ~ character are optional additives.");
			return true;
		}
		Item possibleContainer=possibleContainer(mob,commands,true,Wearable.FILTER_UNWORNONLY);
		Item target=getTarget(mob,mob.location(),givenTarget,possibleContainer,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
        {
            commonTell(mob,"The syntax for this skill is "+triggerStrings()[0]+" [CONTAINER]");
            return false;
        }

		if(!(target instanceof Container))
		{
			commonTell(mob,"There's nothing in "+target.name()+" to "+cookWordShort()+"!");
			return false;
		}
		if(!isMineForCooking(mob,(Container)target))
		{
			commonTell(mob,"You probably need to pick that up first.");
			return false;
		}
		if(!meetsLidRequirements(mob,(Container)target))
		{
			commonTell(mob,"You need a closeable container to bake that in, and you need to close it to begin.");
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
			commonTell(mob,target.name()+" is not suitable to "+cookWordShort()+" in.");
			return false;
		}

		if(requireFire())
		{
			Item fire=getRequiredFire(mob,0);
			if(fire==null) return false;
		}

		messedUp=!proficiencyCheck(mob,0,auto);
		int duration=getDuration(40,mob,1,5);
		cookingPot=(Container)target;
		oldPotContents=potContents(cookingPot);

		//***********************************************
		//* figure out recipe
		//***********************************************
		Vector perfectRecipes=new Vector();
		Vector closerRecipes=new Vector();
		Vector closeRecipes=new Vector();
		for(int v=0;v<allRecipes.size();v++)
		{
			Vector Vr=(Vector)allRecipes.elementAt(v);
			boolean found=false;
			for(Enumeration e=oldPotContents.keys();e.hasMoreElements();)
			{
				String ingredient2=((String)e.nextElement()).toUpperCase();
				if(ingredient2.indexOf(((String)Vr.elementAt(RCP_MAININGR)).toUpperCase()+"/")>=0)
				{ found=true; break;}
			}
			if(found)
			   closeRecipes.addElement(Vr);
			if((missingIngredientsFromOldContents(Vr,true).size()==0)
			&&(extraIngredientsInOldContents(Vr,true).size()==0))
				perfectRecipes.addElement(Vr);
			if((missingIngredientsFromOldContents(Vr,false).size()==0)
			&&(extraIngredientsInOldContents(Vr,false).size()==0))
				closerRecipes.addElement(Vr);
		}

		if(perfectRecipes.size()==0)
			perfectRecipes=closerRecipes;
		if(perfectRecipes.size()==0)
		{
			if(closeRecipes.size()==0)
			{
				commonTell(mob,"You don't know how to make anything out of those ingredients.  Have you tried LIST as a parameter?");
				return false;
			}
			for(int vr=0;vr<closeRecipes.size();vr++)
			{
				Vector Vr=(Vector)closeRecipes.elementAt(vr);
				Vector missing=missingIngredientsFromOldContents(Vr,false);
				Vector extra=extraIngredientsInOldContents(Vr,false);
				String recipeName=replacePercent((String)Vr.elementAt(RCP_FINALFOOD),((String)Vr.elementAt(RCP_MAININGR)).toLowerCase());
				if(extra.size()>0)
				{
					StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to remove ");
					for(int i=0;i<extra.size();i++)
						if(i==0) buf.append(((String)extra.elementAt(i)).toLowerCase());
						else
						if(i==extra.size()-1) buf.append(", and "+((String)extra.elementAt(i)).toLowerCase());
						else buf.append(", "+((String)extra.elementAt(i)).toLowerCase());
					commonTell(mob,buf.toString()+".");
				}
				else
				if(missing.size()>0)
				{
					StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to add ");
					for(int i=0;i<missing.size();i++)
						if(i==0) buf.append(((String)missing.elementAt(i)).toLowerCase());
						else
						if(i==missing.size()-1) buf.append(", and "+((String)missing.elementAt(i)).toLowerCase());
						else buf.append(", "+((String)missing.elementAt(i)).toLowerCase());
					commonTell(mob,buf.toString()+".");
				}
			}
			return false;
		}
		Vector complaints=new Vector();
		for(int vr=0;vr<perfectRecipes.size();vr++)
		{
			Vector Vr=(Vector)perfectRecipes.elementAt(vr);
			Vector counts=countIngredients(Vr);
			Integer amountMaking=(Integer)counts.elementAt(0);
			String recipeName=replacePercent((String)Vr.elementAt(RCP_FINALFOOD),((String)Vr.elementAt(RCP_MAININGR)).toLowerCase());
			if(counts.size()==1)
			{
				if(CMath.s_int((String)Vr.elementAt(RCP_LEVEL))>xlevel(mob))
					complaints.addElement("If you are trying to make "+recipeName+", you need to wait until you are level "+CMath.s_int((String)Vr.elementAt(RCP_LEVEL))+".");
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
				StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to add a little more ");
				for(int i=1;i<counts.size();i++)
					if(i==1)
						buf.append(((String)counts.elementAt(i)).toLowerCase());
					else
					if(i==counts.size()-1)
						buf.append(", and "+((String)counts.elementAt(i)).toLowerCase());
					else
						buf.append(", "+((String)counts.elementAt(i)).toLowerCase());
				complaints.addElement(buf.toString());
			}
			else
			if(amountMaking.intValue()>0)
			{
				StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to remove some of the ");
				for(int i=1;i<counts.size();i++)
					if(i==1)
						buf.append(((String)counts.elementAt(i)).toLowerCase());
					else
					if(i==counts.size()-1)
						buf.append(", and "+((String)counts.elementAt(i)).toLowerCase());
					else
						buf.append(", "+((String)counts.elementAt(i)).toLowerCase());
				complaints.addElement(buf.toString());
			}
		}
		if(finalRecipe==null)
		{
			for(int c=0;c<complaints.size();c++)
				commonTell(mob,((String)complaints.elementAt(c)));
			return false;
		}

		building=buildItem(mob,finalRecipe,cookingPot.getContents());
		//***********************************************
		//* done figuring out recipe
		//***********************************************
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		CMMsg msg=CMClass.getMsg(mob,cookingPot,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) "+cookWord()+" something in <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			cookingPot=(Container)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
