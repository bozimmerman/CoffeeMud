package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.File;
import java.util.*;

public class Cook extends CommonSkill
{
	public static int RCP_FINALFOOD=0;
	public static int RCP_FOODDRINK=1;
	public static int RCP_MAININGR=2;
	public static int RCP_MAINAMNT=3;
	
	private Container cooking=null;
	private Item fire=null;
	private Item finalDish=null;
	private String finalDishName=null;
	private int finalAmount=0;
	private Vector finalRecipe=null;
	private boolean burnt=false;
	private Hashtable oldContents=null;
	public Cook()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cooking";

		displayText="You are cooking...";
		verb="cooking";
		miscText="";
		triggerStrings.addElement("COOK");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Cook();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if((cooking==null)
			||(fire==null)
			||(finalDish==null)
			||(finalRecipe==null)
			||(finalAmount<=0)
			||(!mob.isMine(cooking))
			||(!contentsSame(potContents(cooking),oldContents))
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				mob.tell("You start cooking up some "+finalDishName+".");
				displayText="You are cooking "+finalDishName;
				verb="cooking "+finalDishName;
			}
		}
		return super.tick(tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("COOKING RECIPES");
		if(V==null)
		{
			V=new Vector();
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"recipes.txt");
			if(str!=null)
			{
				Vector V2=new Vector();
				boolean oneComma=false;
				int start=0;
				for(int i=0;i<str.length();i++)
				{
					if(str.charAt(i)=='\t')
					{
						V2.addElement(str.substring(start,i));
						start=i+1;
						oneComma=true;
					}
					else
					if((str.charAt(i)=='\n')||(str.charAt(i)=='\r'))
					{
						if(oneComma)
						{
							V2.addElement(str.substring(start,i));
							V.addElement(V2);
							V2=new Vector();
						}
						start=i+1;
						oneComma=false;
					}
				}
				if(V2.size()>1)
				{
					if(oneComma)
						V2.addElement(str.substring(start,str.length()));
					V.addElement(V2);
				}
			}
			else
				Log.errOut("Cook","Recipes not found!");
			Resources.submitResource("COOKING RECIPES",V);
		}
		return V;
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((cooking!=null)&&(!aborted)&&(finalRecipe!=null)&&(finalDish!=null))
			{
				Vector V=cooking.getContents();
				for(int v=0;v<V.size();v++)
					((Item)V.elementAt(v)).destroyThis();
				if((cooking instanceof Drink)&&(finalDish instanceof Drink))
					((Drink)cooking).setLiquidRemaining(0);
				for(int i=0;i<finalAmount;i++)
				{
					Item food=((Item)finalDish.copyOf());
					food.setMiscText(finalDish.text());
					food.recoverEnvStats();
					((MOB)cooking.myOwner()).addInventory(food);
					food.setLocation(cooking);
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
		if((pot instanceof Drink)&&(((Drink)pot).containsDrink()))
		{
			if(pot instanceof EnvResource)
				h.put(EnvResource.RESOURCE_DESCS[((EnvResource)pot).material()&EnvResource.RESOURCE_MASK],new Integer(((Drink)pot).liquidRemaining()/10));
			else
				h.put(EnvResource.RESOURCE_DESCS[((Drink)pot).liquidType()&EnvResource.RESOURCE_MASK],new Integer(((Drink)pot).liquidRemaining()/10));
		}
		if(pot.myOwner()==null) return h;
		if(!(pot.myOwner() instanceof MOB)) return h;
		MOB mob=(MOB)pot.myOwner();
		Vector V=pot.getContents();
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.elementAt(v);
			String ing="Unknown";
			if(I instanceof EnvResource)
				ing=EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK];
			else
			if((((I.material()&EnvResource.MATERIAL_VEGETATION)>0)
				||((I.material()&EnvResource.MATERIAL_LIQUID)>0)
				||((I.material()&EnvResource.MATERIAL_FLESH)>0))
				&&(Util.parse(I.name()).size()>0))
					ing=((String)Util.parse(I.name()).lastElement()).toUpperCase();
			else
				ing=I.name();
			Integer INT=(Integer)h.get(ing);
			if(INT==null) INT=new Integer(0);
			INT=new Integer(INT.intValue()+1);
			h.put(ing,INT);
		}
		return h;
	}

	
	public Vector countIngrediants(Vector Vr)
	{
		String[] contents=new String[oldContents.size()];
		int[] amounts=new int[oldContents.size()];
		int numIngrediants=0;
		for(Enumeration e=oldContents.keys();e.hasMoreElements();)
		{
			contents[numIngrediants]=(String)e.nextElement();
			amounts[numIngrediants]=((Integer)oldContents.get(contents[numIngrediants])).intValue();
			numIngrediants++;
		}
		
		int amountMade=0;
		
		Vector codedList=new Vector();
		boolean RanOutOfSomething=false;
		boolean NotEnoughForThisRun=false;
		while((!RanOutOfSomething)&&(!NotEnoughForThisRun))
		{
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				String ingrediant=(String)Vr.elementAt(vr);
				if(ingrediant.length()>0)
				{
					int amount=1;
					if(vr<Vr.size()-1)amount=Util.s_int((String)Vr.elementAt(vr+1));
					if(amount<0) amount=amount*-1;
					if(ingrediant.equalsIgnoreCase("water"))
						amount=amount*10;
					for(int i=0;i<contents.length;i++)
					{
						String ingrediant2=(String)contents[i];
						int amount2=amounts[i];
						if(ingrediant.equalsIgnoreCase(ingrediant2))
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
			codedList.addElement(new Integer(-amountMade));
			for(int i=0;i<contents.length;i++)
				if(amounts[i]<0) 
					codedList.addElement(contents[i]);
		}
		else
		{
			codedList.addElement(new Integer(amountMade));
			for(int i=0;i<contents.length;i++)
			{
				String ingrediant2=(String)contents[i];
				int amount2=amounts[i];
				if((amount2>0)
				&&(!ingrediant2.equalsIgnoreCase("water")))
					codedList.addElement(contents[i]);
			}
		}
		
		return codedList;
	}
	
	public Vector extraIngrediantsInOldContents(Vector Vr)
	{
		Vector extra=new Vector();
		for(Enumeration e=oldContents.keys();e.hasMoreElements();)
		{
			boolean found=false;
			String ingrediant=(String)e.nextElement();
			for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
			{
				String ingrediant2=(String)Vr.elementAt(vr);
				if((ingrediant2.length()>0)&&(ingrediant2.equalsIgnoreCase(ingrediant)))
					found=true;
			}
			if(!found) extra.addElement(ingrediant);
		}
		return extra;
	}
	
	public Vector missingIngrediantsFromOldContents(Vector Vr)
	{
		Vector missing=new Vector();
		
		String possiblyMissing=null;
		boolean foundOptional=false;
		boolean hasOptional=false;
		for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
		{
			String ingrediant=(String)Vr.elementAt(vr);
			if(ingrediant.length()>0)
			{
				int amount=1;
				if(vr<Vr.size()-1)amount=Util.s_int((String)Vr.elementAt(vr+1));
				if((amount>=0)&&(!oldContents.containsKey(ingrediant.toUpperCase())))
					missing.addElement(ingrediant);
				else
				if(amount<0){
					foundOptional=true;
					if(oldContents.containsKey(ingrediant.toUpperCase()))
						hasOptional=true;
					else
						possiblyMissing=ingrediant;
				}
			}
		}
		if((foundOptional)&&(!hasOptional))
			missing.addElement(possiblyMissing);
		return missing;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="cooking";
		cooking=null;
		fire=null;
		finalRecipe=null;
		finalAmount=0;
		Item target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(!mob.isMine(target))
		{
			mob.tell("You'll need to pick that up first.");
			return false;
		}
		if(!(target instanceof Container))
		{
			mob.tell("There's nothing in "+target.name()+" to cook!");
			return false;
		}
		switch(target.material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_GLASS:
		case EnvResource.MATERIAL_METAL:
		case EnvResource.MATERIAL_MITHRIL:
		case EnvResource.MATERIAL_ROCK:
			break;
		default:
			mob.tell(target.name()+" is not suitable to cook in.");
			return false;
		}
		
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.location()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			mob.tell("You'll need to build a fire first.");
			return false;
		}
		burnt=!profficiencyCheck(0,auto);
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		cooking=(Container)target;
		oldContents=potContents(cooking);
		
		//***********************************************
		//* figure out recipe
		//***********************************************
		Vector allRecipes=loadRecipes();
		Vector recipes=new Vector();
		Vector closeRecipes=new Vector();
		for(int v=0;v<allRecipes.size();v++)
		{
			Vector Vr=(Vector)allRecipes.elementAt(v);
			String FoodDrink=(String)Vr.elementAt(RCP_FOODDRINK);
			String recName=(String)Vr.elementAt(this.RCP_FINALFOOD);
			if(oldContents.containsKey(((String)Vr.elementAt(RCP_MAININGR)).toUpperCase()))
			   closeRecipes.addElement(Vr);
			if((missingIngrediantsFromOldContents(Vr).size()==0)
			&&(extraIngrediantsInOldContents(Vr).size()==0))
				recipes.addElement(Vr);
		}

		if(recipes.size()==0)
		{
			if(closeRecipes.size()==0)
			{
				mob.tell("You don't know how to make anything out of those ingrediants.");
				return false;
			}
			for(int vr=0;vr<closeRecipes.size();vr++)
			{
				Vector Vr=(Vector)closeRecipes.elementAt(vr);
				Vector missing=missingIngrediantsFromOldContents(Vr);
				Vector extra=extraIngrediantsInOldContents(Vr);
				String recipeName=(String)Vr.elementAt(RCP_FINALFOOD);
				int x=recipeName.indexOf("%");
				if(x>=0) recipeName=new StringBuffer(recipeName).replace(x,x+1,((String)Vr.elementAt(RCP_MAININGR)).toLowerCase()).toString();
				if(extra.size()>0)
				{
					StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to remove ");
					for(int i=0;i<extra.size();i++)
						if(i==0) buf.append(((String)extra.elementAt(i)).toLowerCase());
						else
						if(i==extra.size()-1) buf.append(", and "+((String)extra.elementAt(i)).toLowerCase());
						else buf.append(", "+((String)extra.elementAt(i)).toLowerCase());
					mob.tell(buf.toString()+".");
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
					mob.tell(buf.toString()+".");
				}
			}
			return false;
		}
		else
		{
			Vector complaints=new Vector();
			for(int vr=0;vr<recipes.size();vr++)
			{
				Vector Vr=(Vector)recipes.elementAt(vr);
				String recipeName=(String)Vr.elementAt(RCP_FINALFOOD);
				Vector counts=countIngrediants(Vr);
				Integer amountMaking=(Integer)counts.elementAt(0);
				int x=recipeName.indexOf("%");
				if(x>=0) recipeName=new StringBuffer(recipeName).replace(x,x+1,((String)Vr.elementAt(RCP_MAININGR)).toLowerCase()).toString();
				if(counts.size()==1)
				{
					finalRecipe=Vr;
					finalAmount=amountMaking.intValue();
					break;
				}
				else
				if(amountMaking.intValue()<=0)
				{
					StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to add a little more ");
					for(int i=1;i<counts.size();i++)
						if(i==1) buf.append(((String)counts.elementAt(i)).toLowerCase());
						else
						if(i==counts.size()-1) buf.append(", and "+((String)counts.elementAt(i)).toLowerCase());
						else buf.append(", "+((String)counts.elementAt(i)).toLowerCase());
					complaints.addElement(buf.toString());
				}
				else
				if(amountMaking.intValue()>0)
				{
					StringBuffer buf=new StringBuffer("If you are trying to make "+recipeName+", you need to add a little more ");
					for(int i=1;i<counts.size();i++)
						if(i==1) buf.append(((String)counts.elementAt(i)).toLowerCase());
						else
						if(i==counts.size()-1) buf.append(", and "+((String)counts.elementAt(i)).toLowerCase());
						else buf.append(", "+((String)counts.elementAt(i)).toLowerCase());
					complaints.addElement(buf.toString());
				}
			}
			if(finalRecipe==null)
			{
				for(int c=0;c<complaints.size();c++)
					mob.tell(((String)complaints.elementAt(c)));
				return false;
			}
		}
		
		String recipeName=(String)finalRecipe.elementAt(RCP_FINALFOOD);
		String foodType=(String)finalRecipe.elementAt(RCP_FOODDRINK);
		Vector contents=cooking.getContents();
		if(recipeName.indexOf("%")>=0)
		{
			int r=recipeName.indexOf("%");
			String replaceName=((String)finalRecipe.elementAt(RCP_MAININGR));
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				if((I instanceof EnvResource)
				&&(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].equalsIgnoreCase((String)finalRecipe.elementAt(RCP_MAININGR))))
				{
					String name=I.name();
					if(name.endsWith(" meat"))
						name=name.substring(0,name.length()-5);
					if(name.endsWith(" flesh"))
						name=name.substring(0,name.length()-6);
					name=name.trim();
					int x=name.lastIndexOf(" ");
					if(x>0)
						replaceName=name.substring(x+1);
					else
						replaceName=name;
					break;
				}
			}
			replaceName=Character.toUpperCase(replaceName.charAt(0))+replaceName.substring(1).toLowerCase();
			recipeName=new StringBuffer(recipeName).replace(r,r+1,replaceName).toString();
		}
		finalDishName=recipeName;
		if(foodType.equalsIgnoreCase("FOOD"))
		{
			finalDish=CMClass.getItem("GenFood");
			Food food=(Food)finalDish;
			finalDish.setName(((burnt)?"burnt ":"")+recipeName);
			finalDish.setDisplayText("some "+((burnt)?"burnt ":"")+recipeName+" has been left here");
			finalDish.setDescription("It looks "+((burnt)?"burnt!":"good!"));
			food.setNourishment(0);
			if(!burnt)
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				if(I instanceof Food)
					food.setNourishment(food.nourishment()+(((Food)I).nourishment()+((Food)I).nourishment()));
			}
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				food.baseEnvStats().setWeight(food.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
			}
			food.setNourishment(food.nourishment()/finalAmount);
			food.baseEnvStats().setWeight(food.baseEnvStats().weight()/finalAmount);
			food.recoverEnvStats();
			food.text();
		}
		else
		if((foodType.equalsIgnoreCase("DRINK"))&&(cooking instanceof Drink))
		{
			finalDish=CMClass.getItem("GenLiquidResource");
			finalDish.setMiscText(cooking.text());
			finalDish.recoverEnvStats();
			finalDish.setName(((burnt)?"spoiled ":"")+recipeName);
			finalDish.setDisplayText("some "+((burnt)?"spoiled ":"")+recipeName+" has been left here.");
			finalDish.setDescription("It looks "+((burnt)?"spoiled!":"good!"));
			Drink drink=(Drink)finalDish;
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
				if(I instanceof Food)
					drink.setLiquidRemaining(drink.liquidRemaining()+((Food)I).nourishment());
			}
			drink.setLiquidHeld(drink.liquidRemaining());
			drink.setThirstQuenched(drink.liquidRemaining());
			drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()/finalAmount);
			if(burnt)drink.setThirstQuenched(1);
			drink.text();
		}
		
		//***********************************************
		//* done figuring out recipe
		//***********************************************
		
		FullMsg msg=new FullMsg(mob,cooking,null,Affect.MSG_NOISYMOVEMENT,Affect.MSG_OK_ACTION,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) cooking something in <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
