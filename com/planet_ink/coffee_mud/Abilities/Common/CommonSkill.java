package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CommonSkill extends StdAbility
{
	protected Room activityRoom=null;
	protected boolean aborted=false;
	protected int tickUp=0;
	protected String verb="working";

	public CommonSkill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Common Skill";
		displayText="(Doing something productive)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;
		trainsRequired=0;
		practicesRequired=1;
		practicesToPractice=1;

		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new CommonSkill();
	}

	public int classificationCode()
	{
		return Ability.COMMON_SKILL;
	}

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
	
	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())||(mob.location()!=activityRoom))
			{aborted=true; unInvoke(); return false;}
			if(tickDown==4)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb+".");
			else
			if((tickUp%4)==0)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb+".");
			
			tickUp++;	
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(aborted)
				mob.tell("You stop "+verb);
			else
				mob.tell("You are done "+verb);
			
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
		return V;
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
				I=CMClass.getItem("GenFoodResource");
				break;
			}
			case EnvResource.MATERIAL_CLOTH:
			case EnvResource.MATERIAL_PAPER:
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_WOODEN:
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
				name=name+" ore";
				break;
			}
			}
			if(I!=null)
			{
				I.setMaterial(myResource);
				I.setBaseValue(EnvResource.RESOURCE_DATA[myResource&EnvResource.RESOURCE_MASK][1]);
				I.baseEnvStats().setWeight(1);
				I.setName("a pound of "+name);
				I.setDisplayText("some "+name+" sits here.");
				I.setDescription("Looks like "+I.name()+".");
				I.recoverEnvStats();
				return I;
			}
		}
		return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You are in combat!");
			return false;
		}
		if(!Sense.canBeSeenBy(mob.location(),mob))
		{
			mob.tell("You can't see to do that!");
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
		int diff=mob.envStats().level()-envStats().level();
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
			mob.tell("You don't have enough mana to do that.");
			return false;
		}
		activityRoom=mob.location();
		mob.curState().adjMana(-manaConsumed,mob.maxState());
		helpProfficiency(mob);
		
		return true;
	}
}