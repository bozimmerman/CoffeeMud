package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.File;
import java.util.*;

public class Cooking extends CommonSkill
{
	public String ID() { return "Cooking"; }
	public String name(){ return "Cooking";}
	private static final String[] triggerStrings = {"COOK","COOKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String cookWordShort(){return "cook";};
	public String cookWord(){return "cooking";};
	public boolean honorHerbs(){return true;}

	public static int RCP_FINALFOOD=0;
	public static int RCP_FOODDRINK=1;
	public static int RCP_BONUSSPELL=2;
	public static int RCP_LEVEL=3;
	public static int RCP_MAININGR=4;
	public static int RCP_MAINAMNT=5;

	private Container cooking=null;
	private Item fire=null;
	private Item finalDish=null;
	private String finalDishName=null;
	private int finalAmount=0;
	private Vector finalRecipe=null;
	private boolean burnt=false;
	private Hashtable oldContents=null;
	private static boolean mapped=false;
	public Cooking()
	{
		super();
		displayText="You are "+cookWord()+"...";
		verb=cookWord();
		if(ID().equals("Cooking")&&(!mapped))
		{mapped=true; CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Cooking();}

	public boolean isMineForCooking(MOB mob, Item cooking)
	{
		if(mob.isMine(cooking)) return true;
		if((mob.location()==cooking.owner())
		&&(cooking.container()!=null)
		&&(Sense.isOnFire(cooking.container())))
		   return true;
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((cooking==null)
			||(fire==null)
			||(finalDish==null)
			||(finalRecipe==null)
			||(finalAmount<=0)
			||(!isMineForCooking(mob,cooking))
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
				commonTell(mob,"You start "+cookWord()+" up some "+finalDishName+".");
				displayText="You are "+cookWord()+" "+finalDishName;
				verb=cookWord()+" "+finalDishName;
			}
		}
		return super.tick(ticking,tickID);
	}

	protected static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("COOKING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"recipes.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Cook","Recipes not found!");
			Resources.submitResource("COOKING RECIPES",V);
		}
		return V;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				if((cooking!=null)&&(!aborted)&&(finalRecipe!=null)&&(finalDish!=null))
				{
					Vector V=cooking.getContents();
					for(int v=0;v<V.size();v++)
						((Item)V.elementAt(v)).destroy();
					if((cooking instanceof Drink)&&(finalDish instanceof Drink))
						((Drink)cooking).setLiquidRemaining(0);
					for(int i=0;i<finalAmount*(abilityCode());i++)
					{
						Item food=((Item)finalDish.copyOf());
						food.setMiscText(finalDish.text());
						food.recoverEnvStats();
						if(cooking.owner() instanceof Room)
							((Room)cooking.owner()).addItemRefuse(food,Item.REFUSE_PLAYER_DROP);
						else
						if(cooking.owner() instanceof MOB)
							((MOB)cooking.owner()).addInventory(food);
						food.setContainer(cooking);
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
		if((pot instanceof Drink)&&(((Drink)pot).containsDrink()))
		{
			if(pot instanceof EnvResource)
				h.put(EnvResource.RESOURCE_DESCS[((EnvResource)pot).material()&EnvResource.RESOURCE_MASK]+"/",new Integer(((Drink)pot).liquidRemaining()/10));
			else
				h.put(EnvResource.RESOURCE_DESCS[((Drink)pot).liquidType()&EnvResource.RESOURCE_MASK]+"/",new Integer(((Drink)pot).liquidRemaining()/10));
		}
		if(pot.owner()==null) return h;
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
			Integer INT=(Integer)h.get(ing+"/"+I.Name().toUpperCase());
			if(INT==null) INT=new Integer(0);
			INT=new Integer(INT.intValue()+1);
			h.put(ing+"/"+I.Name().toUpperCase(),INT);
		}
		return h;
	}


	public Vector countIngredients(Vector Vr)
	{
		String[] contents=new String[oldContents.size()];
		int[] amounts=new int[oldContents.size()];
		int numIngredients=0;
		for(Enumeration e=oldContents.keys();e.hasMoreElements();)
		{
			contents[numIngredients]=(String)e.nextElement();
			amounts[numIngredients]=((Integer)oldContents.get(contents[numIngredients])).intValue();
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
					if(vr<Vr.size()-1)amount=Util.s_int((String)Vr.elementAt(vr+1));
					if(amount==0) amount=1;
					if(amount<0) amount=amount*-1;
					if(ingredient.equalsIgnoreCase("water"))
						amount=amount*10;
					boolean found=false;
					for(int i=0;i<contents.length;i++)
					{
						String ingredient2=((String)contents[i]).toUpperCase();
						int amount2=amounts[i];
						if((ingredient2.startsWith(ingredient+"/"))
						||(ingredient2.endsWith("/"+ingredient)))
						{
							found=true;
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
				{
					String content=contents[i];
					if(content.indexOf("/")>=0)
						content=content.substring(0,content.indexOf("/"));
					codedList.addElement(content);
				}
		}
		else
		{
			codedList.addElement(new Integer(amountMade));
			for(int i=0;i<contents.length;i++)
			{
				String ingredient2=(String)contents[i];
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

	public Vector extraIngredientsInOldContents(Vector Vr)
	{
		Vector extra=new Vector();
		for(Enumeration e=oldContents.keys();e.hasMoreElements();)
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
				&&((ingredient.toUpperCase().startsWith(ingredient2+"/"))
				||(ingredient.toUpperCase().endsWith("/"+ingredient2))))
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

	public Vector missingIngredientsFromOldContents(Vector Vr)
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
				if(vr<Vr.size()-1)amount=Util.s_int((String)Vr.elementAt(vr+1));
				if(amount>=0)
				{
					boolean found=false;
					for(Enumeration e=oldContents.keys();e.hasMoreElements();)
					{
						String ingredient2=((String)e.nextElement()).toUpperCase();
						if((ingredient2.startsWith(ingredient.toUpperCase()+"/"))
						||(ingredient2.endsWith("/"+ingredient.toUpperCase())))
						{ found=true; break;}
					}
					if(!found)
						missing.addElement(ingredient);
				}
				else
				if(amount<0){
					foundOptional=true;
					if(oldContents.containsKey(ingredient.toUpperCase()))
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb=cookWord();
		cooking=null;
		fire=null;
		finalRecipe=null;
		finalAmount=0;
		Vector allRecipes=loadRecipes();
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Recipe",16)+" ingredients required\n\r");
			for(int r=0;r<allRecipes.size();r++)
			{
				Vector Vr=(Vector)allRecipes.elementAt(r);
				if(Vr.size()>0)
				{
					String item=(String)Vr.elementAt(RCP_FINALFOOD);
					if(item.length()==0) continue;
					int level=Util.s_int((String)Vr.elementAt(RCP_LEVEL));
					if(level<=mob.envStats().level())
					{
						buf.append(Util.padRight(Util.capitalize(replacePercent(item,"")),16)+" ");
						for(int vr=RCP_MAININGR;vr<Vr.size();vr+=2)
						{
							String ingredient=(String)Vr.elementAt(vr);
							if(ingredient.length()>0)
							{
								int amount=1;
								if(vr<Vr.size()-1)amount=Util.s_int((String)Vr.elementAt(vr+1));
								if(amount==0) amount=1;
								if(amount<0) amount=amount*-1;
								if(ingredient.equalsIgnoreCase("water"))
									amount=amount*10;
								buf.append(ingredient.toLowerCase()+"("+amount+") ");
							}
						}
						buf.append("\n\r");
					}
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		Item possibleContainer=possibleContainer(mob,commands,true,Item.WORN_REQ_UNWORNONLY);
		Item target=getTarget(mob,mob.location(),givenTarget,possibleContainer,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!isMineForCooking(mob,target))
		{
			commonTell(mob,"You probably need to pick that up first.");
			return false;
		}
		if(!(target instanceof Container))
		{
			commonTell(mob,"There's nothing in "+target.name()+" to "+cookWordShort()+"!");
			return false;
		}
		switch(target.material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_GLASS:
		case EnvResource.MATERIAL_METAL:
		case EnvResource.MATERIAL_MITHRIL:
		case EnvResource.MATERIAL_ROCK:
		case EnvResource.MATERIAL_PRECIOUS:
			break;
		default:
			commonTell(mob,target.name()+" is not suitable to "+cookWordShort()+" in.");
			return false;
		}

		fire=getRequiredFire(mob);
		if(fire==null) return false;

		burnt=!profficiencyCheck(0,auto);
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		cooking=(Container)target;
		oldContents=potContents(cooking);

		//***********************************************
		//* figure out recipe
		//***********************************************
		Vector recipes=new Vector();
		Vector closeRecipes=new Vector();
		for(int v=0;v<allRecipes.size();v++)
		{
			Vector Vr=(Vector)allRecipes.elementAt(v);
			boolean found=false;
			for(Enumeration e=oldContents.keys();e.hasMoreElements();)
			{
				String ingredient2=((String)e.nextElement()).toUpperCase();
				if((ingredient2.startsWith(((String)Vr.elementAt(RCP_MAININGR)).toUpperCase()+"/"))
				||(ingredient2.endsWith("/"+((String)Vr.elementAt(RCP_MAININGR)).toUpperCase())))
				{ found=true; break;}
			}
			if(found)
			   closeRecipes.addElement(Vr);
			if((missingIngredientsFromOldContents(Vr).size()==0)
			&&(extraIngredientsInOldContents(Vr).size()==0))
				recipes.addElement(Vr);
		}

		if(recipes.size()==0)
		{
			if(closeRecipes.size()==0)
			{
				commonTell(mob,"You don't know how to make anything out of those ingredients.  Have you tried LIST as a parameter?");
				return false;
			}
			for(int vr=0;vr<closeRecipes.size();vr++)
			{
				Vector Vr=(Vector)closeRecipes.elementAt(vr);
				Vector missing=missingIngredientsFromOldContents(Vr);
				Vector extra=extraIngredientsInOldContents(Vr);
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
		else
		{
			Vector complaints=new Vector();
			for(int vr=0;vr<recipes.size();vr++)
			{
				Vector Vr=(Vector)recipes.elementAt(vr);
				Vector counts=countIngredients(Vr);
				Integer amountMaking=(Integer)counts.elementAt(0);
				String recipeName=replacePercent((String)Vr.elementAt(RCP_FINALFOOD),((String)Vr.elementAt(RCP_MAININGR)).toLowerCase());
				if(counts.size()==1)
				{
					if(Util.s_int((String)Vr.elementAt(RCP_LEVEL))>mob.envStats().level())
						complaints.addElement("If you are trying to make "+recipeName+", you need to wait until you are level "+Util.s_int((String)Vr.elementAt(RCP_LEVEL))+".");
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
		}

		String foodType=(String)finalRecipe.elementAt(RCP_FOODDRINK);
		Vector contents=cooking.getContents();
		String replaceName=((String)finalRecipe.elementAt(RCP_MAININGR));
		for(int v=0;v<contents.size();v++)
		{
			Item I=(Item)contents.elementAt(v);
			if((I instanceof EnvResource)
			&&(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].equalsIgnoreCase((String)finalRecipe.elementAt(RCP_MAININGR))))
			{
				String name=I.Name();
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
		finalDishName=replacePercent((String)finalRecipe.elementAt(RCP_FINALFOOD),Character.toUpperCase(replaceName.charAt(0))+replaceName.substring(1).toLowerCase());
		if(foodType.equalsIgnoreCase("FOOD"))
		{
			finalDish=CMClass.getItem("GenFood");
			Food food=(Food)finalDish;
			finalDish.setName(((burnt)?"burnt ":"")+finalDishName);
			finalDish.setDisplayText("some "+((burnt)?"burnt ":"")+finalDishName+" has been left here");
			finalDish.setDescription("It looks "+((burnt)?"burnt!":"good!"));
			finalDish.setSecretIdentity("This was prepared by "+mob.Name()+".");
			food.setNourishment(0);
			if(!burnt)
			{
				boolean timesTwo=false;
				for(int v=0;v<contents.size();v++)
				{
					Item I=(Item)contents.elementAt(v);
					if((I.material()==EnvResource.RESOURCE_HERBS)&&(honorHerbs()))
						timesTwo=true;
					else
					if(I instanceof Food)
						food.setNourishment(food.nourishment()+(((Food)I).nourishment()+((Food)I).nourishment()));
				}
				if(timesTwo) food.setNourishment(food.nourishment()*2);
			}
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				if((I.material()!=EnvResource.RESOURCE_HERBS)||(!honorHerbs()))
					food.baseEnvStats().setWeight(food.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
			}
			food.setNourishment(food.nourishment()/finalAmount);
			food.baseEnvStats().setWeight(food.baseEnvStats().weight()/finalAmount);
		}
		else
		if(foodType.equalsIgnoreCase("DRINK"))
		{
			finalDish=CMClass.getItem("GenLiquidResource");
			finalDish.setMiscText(cooking.text());
			finalDish.recoverEnvStats();
			finalDish.setName(((burnt)?"spoiled ":"")+finalDishName);
			finalDish.setDisplayText("some "+((burnt)?"spoiled ":"")+finalDishName+" has been left here.");
			finalDish.setDescription("It looks "+((burnt)?"spoiled!":"good!"));
			finalDish.setSecretIdentity("This was prepared by "+mob.Name()+".");
			Drink drink=(Drink)finalDish;
			for(int v=0;v<contents.size();v++)
			{
				Item I=(Item)contents.elementAt(v);
				drink.baseEnvStats().setWeight(drink.baseEnvStats().weight()+((I.baseEnvStats().weight())/finalAmount));
				if(I instanceof Food)
					drink.setLiquidRemaining(drink.liquidRemaining()+((Food)I).nourishment());
			}
			if(drink.liquidRemaining()>0)
			{
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
			if(burnt)drink.setThirstQuenched(1);
		}
		if(finalDish!=null)
		{
			String spell=(String)finalRecipe.elementAt(RCP_BONUSSPELL);
			if((spell!=null)&&(spell.length()>0))
			{
				String parms="";
				int x=spell.indexOf("(");
				if(x>=0)
				{
					int y=spell.indexOf(")");
					if(y>x)
					{
						parms=spell.substring(x+1,y);
						spell=spell.substring(0,x);
					}
				}
				Ability A=CMClass.getAbility(spell);
				if(A!=null)
				{
					finalDish.addNonUninvokableEffect(A);
					A.setMiscText(parms);
				}
			}
			finalDish.recoverEnvStats();
			finalDish.text();
		}
		//***********************************************
		//* done figuring out recipe
		//***********************************************
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		FullMsg msg=new FullMsg(mob,cooking,null,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_OK_ACTION,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) "+cookWord()+" something in <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
