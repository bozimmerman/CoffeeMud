package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CommonSkill extends StdAbility
{
	public String ID() { return "CommonSkill"; }
	public String name(){ return "Common Skill";}
	private static final String[] triggerStrings = empty;
	public String[] triggerStrings(){return triggerStrings;}

	public int quality(){return Ability.INDIFFERENT;}
	protected String displayText="(Doing something productive)";
	public String displayText(){return displayText;}

	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_COMMONPRACCOST);}
	protected int practicesToPractice(){return 1;}

	protected Room activityRoom=null;
	protected boolean aborted=false;
	protected int tickUp=0;
	protected String verb="working";

	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}

	public Environmental newInstance()	{	return new CommonSkill();	}
	public int classificationCode()	{	return Ability.COMMON_SKILL; }

	private int yield=1;
	public int abilityCode(){return yield;}
	public void setAbilityCode(int newCode){yield=newCode;}

	protected String replacePercent(String thisStr, String withThis)
	{
		if(withThis.length()==0)
		{
			int x=thisStr.indexOf("% ");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf(" %");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		else
		{
			int x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		return thisStr;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())||(mob.location()!=activityRoom)||(!Sense.aliveAwakeMobile(mob,true)))
			{aborted=true; unInvoke(); return false;}
			if(tickDown==4)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb+".");
			else
			if((tickUp%4)==0)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb+".");

			tickUp++;
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(aborted)
					commonEmote(mob,"<S-NAME> stop(s) "+verb);
				else
					commonEmote(mob,"<S-NAME> <S-IS-ARE> done "+verb);

			}
		}
		super.unInvoke();
	}

	protected static Vector loadList(StringBuffer str)
	{
		Vector V=new Vector();
		if(str==null) return V;
		Vector V2=new Vector();
		boolean oneComma=false;
		int start=0;
		int longestList=0;
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
					if(V2.size()>longestList) longestList=V2.size();
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
			if(V2.size()>longestList) longestList=V2.size();
			V.addElement(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=(Vector)V.elementAt(v);
			while(V2.size()<longestList)
				V2.addElement("");
		}
		return V;
	}

	protected void commonTell(MOB mob, Environmental target, Environmental tool, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			if(target!=null) str=Util.replaceAll(str,"<T-NAME>",target.name());
			if(tool!=null)  str=Util.replaceAll(str,"<O-NAME>",tool.name());
			ExternalPlay.quickSay(mob,null,str,false,false);
		}
		else
			mob.tell(mob,target,tool,str);
	}

	protected void commonTell(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
		{
			if(str.startsWith("You")) str="I"+str.substring(3);
			ExternalPlay.quickSay(mob,null,str,false,false);
		}
		else
			mob.tell(str);
	}

	protected void commonEmote(MOB mob, String str)
	{
		if(mob.isMonster()&&(mob.amFollowing()!=null))
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT|Affect.MASK_GENERAL,str);
		else
			mob.tell(mob,null,null,str);
	}

	protected int lookingFor(int material, Room fromHere)
	{
		Vector V=new Vector();
		V.addElement(new Integer(material));
		return lookingFor(V,fromHere);
	}

	protected int lookingFor(Vector materials, Room fromHere)
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=fromHere.getRoomInDir(d);
			Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				int material=room.myResource();
				if(materials.contains(new Integer(material&EnvResource.MATERIAL_MASK)))
				{possibilities.addElement(new Integer(d));}
			}
		}
		if(possibilities.size()==0)
			return -1;
		else
			return ((Integer)(possibilities.elementAt(Dice.roll(1,possibilities.size(),-1)))).intValue();
	}

	protected Item findFirstResource(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(other))
				return findFirstResource(room,EnvResource.RESOURCE_DATA[i][0]);
		return null;
	}
	protected Item findFirstResource(Room room, int resource)
	{
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				return I;
		}
		return null;
	}
	protected Item findMostOfMaterial(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
			if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(other))
				return findMostOfMaterial(room,(i<<8));
		return null;
	}

	protected Item findMostOfMaterial(Room room, int material)
	{
		int most=0;
		int mostMaterial=-1;
		Item mostItem=null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&((I.material()&EnvResource.MATERIAL_MASK)==material)
			&&(I.material()!=mostMaterial)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
			{
				int num=findNumberOfResource(room,I.material());
				if(num>most)
				{
					mostItem=I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}
	
	public Item getRequiredFire(MOB mob)
	{
		Item fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return null;
		}
		return fire;
	}

	protected int findNumberOfResource(Room room, int resource)
	{
		int foundWood=0;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				foundWood++;
		}
		return foundWood;
	}

	protected Environmental makeResource(int myResource)
	{
		if(myResource<0)
			return null;
		else
		{
			int material=(myResource&EnvResource.MATERIAL_MASK);
			Item I=null;
			String name=EnvResource.RESOURCE_DESCS[myResource&EnvResource.RESOURCE_MASK].toLowerCase();
			if((myResource==EnvResource.RESOURCE_WOOL)
			||(myResource==EnvResource.RESOURCE_FEATHERS)
			||(myResource==EnvResource.RESOURCE_SCALES)
			||(myResource==EnvResource.RESOURCE_HIDE)
			||(myResource==EnvResource.RESOURCE_FUR))
			   material=EnvResource.MATERIAL_LEATHER;
			if(myResource==EnvResource.RESOURCE_FISH)
				material=EnvResource.MATERIAL_VEGETATION;
			switch(material)
			{
			case EnvResource.MATERIAL_LEATHER:
			case EnvResource.MATERIAL_FLESH:
				switch(myResource)
				{
				case EnvResource.RESOURCE_MUTTON:
				case EnvResource.RESOURCE_WOOL:
					return CMClass.getMOB("Sheep");
				case EnvResource.RESOURCE_LEATHER:
				case EnvResource.RESOURCE_HIDE:
					switch(Dice.roll(1,10,0))
					{
					case 1:
					case 2:
					case 3: return CMClass.getMOB("Cow");
					case 4: return CMClass.getMOB("Bull");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("Doe");
					case 8:
					case 9:
					case 10: return CMClass.getMOB("Buck");
					}
					break;
				case EnvResource.RESOURCE_PORK:
					return CMClass.getMOB("Pig");
				case EnvResource.RESOURCE_FUR:
				case EnvResource.RESOURCE_MEAT:
					switch(Dice.roll(1,10,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Wolf");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("Buffalo");
					case 8:
					case 9: return CMClass.getMOB("BrownBear");
					case 10: return CMClass.getMOB("BlackBear");
					}
					break;
				case EnvResource.RESOURCE_SCALES:
					switch(Dice.roll(1,10,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Lizard");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("GardenSnake");
					case 8:
					case 9: return CMClass.getMOB("Cobra");
					case 10: return CMClass.getMOB("Python");
					}
					break;
				case EnvResource.RESOURCE_POULTRY:
				case EnvResource.RESOURCE_EGGS:
					return CMClass.getMOB("Chicken");
				case EnvResource.RESOURCE_BEEF:
					switch(Dice.roll(1,5,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Cow");
					case 5: return CMClass.getMOB("Bull");
					}
					break;
				case EnvResource.RESOURCE_FEATHERS:
					switch(Dice.roll(1,4,0))
					{
					case 1: return CMClass.getMOB("WildEagle");
					case 2: return CMClass.getMOB("Falcon");
					case 3: return CMClass.getMOB("Chicken");
					case 4: return CMClass.getMOB("Parakeet");
					}
					break;
				}
				break;
			case EnvResource.MATERIAL_VEGETATION:
			{
				if(myResource==EnvResource.RESOURCE_VINE)
					I=CMClass.getItem("GenResource");
				else
				{
					I=CMClass.getItem("GenFoodResource");
					if(myResource==EnvResource.RESOURCE_HERBS)
						((Food)I).setNourishment(1);
				}
				break;
			}
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_ENERGY:
			{
				I=CMClass.getItem("GenLiquidResource");
				break;
			}
			case EnvResource.MATERIAL_CLOTH:
			case EnvResource.MATERIAL_PAPER:
			case EnvResource.MATERIAL_WOODEN:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_PLASTIC:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
			{
				I=CMClass.getItem("GenResource");
				break;
			}
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			{
				I=CMClass.getItem("GenResource");
				if((myResource!=EnvResource.RESOURCE_ADAMANTITE)
				&&(myResource!=EnvResource.RESOURCE_BRASS)
				&&(myResource!=EnvResource.RESOURCE_BRONZE)
				&&(myResource!=EnvResource.RESOURCE_STEEL))
					name=name+" ore";
				break;
			}
			}
			if(I!=null)
			{
				I.setMaterial(myResource);
				if(I instanceof Drink)
					((Drink)I).setLiquidType(myResource);
				I.setBaseValue(EnvResource.RESOURCE_DATA[myResource&EnvResource.RESOURCE_MASK][1]);
				I.baseEnvStats().setWeight(1);
				if(I instanceof Drink)
					I.setName("some "+name);
				else
					I.setName("a pound of "+name);
				I.setDisplayText("some "+name+" sits here.");
				I.setDescription("");
				I.recoverEnvStats();
				return I;
			}
		}
		return null;
	}

	protected Vector getAllMendable(MOB mob, Environmental from, Item contained)
	{
		Vector V=new Vector();
		if(from==null) return V;
		if(from instanceof Room)
		{
			Room R=(Room)from;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(Sense.canBeSeenBy(I,mob)))
					V.addElement(I);
			}
		}
		else
		if(from instanceof MOB)
		{
			MOB M=(MOB)from;
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(Sense.canBeSeenBy(I,mob))
				&&((mob==from)||(!I.amWearingAt(Item.INVENTORY))))
					V.addElement(I);
			}
		}
		else
		if(from instanceof Item)
		{
			if(from instanceof Container)
				V=getAllMendable(mob,((Item)from).owner(),(Item)from);
			if(canMend(mob,from,true))
				V.addElement(from);
		}
		return V;
	}
	
	public boolean publicScan(MOB mob, Vector commands)
	{
		String rest=Util.combine(commands,1);
		Environmental scanning=null;
		if(rest.length()==0)
			scanning=mob;
		else
		if(rest.equalsIgnoreCase("room"))
			scanning=mob.location();
		else
		{
			scanning=mob.location().fetchInhabitant(rest);
			if((scanning==null)||(!Sense.canBeSeenBy(scanning,mob)))
			{
				commonTell(mob,"You don't see anyone called '"+rest+"' here.");
				return false;
			}
		}
		Vector allStuff=getAllMendable(mob,scanning,null);
		if(allStuff.size()==0)
		{
			if(mob==scanning)
				commonTell(mob,"You don't seem to have anything that needs mending with "+name()+".");
			else
				commonTell(mob,"You don't see anything on "+scanning.name()+" that needs mending with "+name()+".");
			return false;
		}
		StringBuffer buf=new StringBuffer("The following items could use some "+name()+":\n\r");
		for(int i=0;i<allStuff.size();i++)
		{
			Item I=(Item)allStuff.elementAt(i);
			buf.append(Util.padRight(I.usesRemaining()+"%",5)+I.name());
			if(!I.amWearingAt(Item.INVENTORY))
				buf.append(" ("+Sense.wornLocation(I.rawWornCode())+")");
			if(i<(allStuff.size()-1))
				buf.append("\n\r");
		}
		commonTell(mob,buf.toString());
		return true;
	}
	
	
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(E==null) return false;
		if(!(E instanceof Item))
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+E.name()+".");
			return false;
		}
		Item IE=(Item)E;
		if(!IE.subjectToWearAndTear())
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+IE.name()+".");
			return false;
		}
		if(IE.usesRemaining()>=100)
		{
			if(!quiet)
				commonTell(mob,IE.name()+" is in good condition already.");
			return false;
		}
		return true;
	}
	
	
	protected Item makeItemResource(int type)
	{
		Item I=null;
		String name=EnvResource.RESOURCE_DESCS[type&EnvResource.RESOURCE_MASK].toLowerCase();
		if(((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH)
		||((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		if(I instanceof Drink)
			I.setName("some "+name);
		else
			I.setName("a pound of "+name);
		I.setDisplayText("some "+name+" sits here.");
		I.setDescription("");
		I.setMaterial(type);
		I.setBaseValue(EnvResource.RESOURCE_DATA[type&EnvResource.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		return I;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			commonEmote(mob,"<S-NAME> <S-IS-ARE> in combat!");
			return false;
		}
		if(!Sense.canBeSeenBy(mob.location(),mob))
		{
			commonTell(mob,"<S-NAME> can't see to do that!");
			return false;
		}
		if(Sense.isSitting(mob)||Sense.isSleeping(mob))
		{
			commonTell(mob,"You need to stand up!");
			return false;
		}
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
			{
				if(A instanceof CommonSkill)
					((CommonSkill)A).aborted=true;
				A.unInvoke();
			}
		}
		isAnAutoEffect=false;

		// if you can't move, you can't do anything!
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		int manaConsumed=25;
		int diff=mob.envStats().level()-CMAble.qualifyingLevel(mob,this);
		if(diff>0)
		switch(diff)
		{
		case 1: manaConsumed=20; break;
		case 2: manaConsumed=15; break;
		case 3: manaConsumed=10; break;
		default: manaConsumed=5; break;
		}

		if(mob.curState().getMana()<manaConsumed)
		{
			commonTell(mob,"<S-NAME> don't have enough mana to do that.");
			return false;
		}
		activityRoom=mob.location();
		mob.curState().adjMana(-manaConsumed,mob.maxState());
		helpProfficiency(mob);

		return true;
	}
}