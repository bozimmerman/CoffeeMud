package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CommonSkill extends StdAbility
{
	public Room activityRoom=null;
	public boolean aborted=false;
	public int tickUp=0;
	public String verb="working";

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
			if(tickUp==0)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> begin(s) "+verb+".");
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
	
	public int lookingFor(int material, Room fromHere)
	{
		Vector V=new Vector();
		V.addElement(new Integer(material));
		return lookingFor(V,fromHere);
	}
	
	public int lookingFor(Vector materials, Room fromHere)
	{
		Vector possibilities=new Vector();
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room room=fromHere.getRoomInDir(d);
			Exit exit=fromHere.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(exit.isOpen()))
			{
				Environmental E=room.myResource();
				if(E!=null)
				{
					if(E instanceof Item)
					{
						if(materials.contains(new Integer(((Item)E).material()&EnvResource.MATERIAL_MASK)))
						{possibilities.addElement(new Integer(d));}
					}
					else
					if((E instanceof MOB)&&(materials.contains(new Integer(EnvResource.MATERIAL_FLESH))))
					{possibilities.addElement(new Integer(d));}
				}
			}
		}
		if(possibilities.size()==0) 
			return -1;
		else 
			return ((Integer)(possibilities.elementAt(Dice.roll(1,possibilities.size(),-1)))).intValue();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You are in combat!");
			return false;
		}
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
			{
				if(A.ID().equals(ID()))
					A.unInvoke();
				else
				{
					mob.tell("You are too busy to do any "+verb+" right now.");
					return false;
				}
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