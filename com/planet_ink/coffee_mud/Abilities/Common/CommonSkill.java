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
			if(tickDown<4)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> <S-IS-ARE> almost done "+verb);
			else
			if(tickUp==0)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> begin(s) "+verb);
			else
			if((tickUp%4)==0)
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> continue(s) "+verb);
			
			tickUp++;	
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(aborted)
				mob.tell("You stop "+verb);
			else
				mob.tell("You are done "+verb);
			
		}
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You are in combat!");
			return false;
		}

		isAnAutoEffect=false;

		// if you can't move, you can't cast! Not even verbal!
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
		mob.curState().adjMana(-manaConsumed,mob.maxState());
		helpProfficiency(mob);
		
		return true;
	}
}