package com.planet_ink.coffee_mud.Abilities;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Thief_Trap extends ThiefSkill
{

	public Thief_Trap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lay Traps";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("TRAP");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(23);

		addQualifyingClass(new Thief().ID(),23);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Trap();
	}

	public static final Trap getATrap(Environmental unlockThis)
	{
		Trap theTrap=null;
		int roll=Dice.rollPercentage();
		if(unlockThis instanceof Exit)
		{
			if(((Exit)unlockThis).hasADoor())
			{
				if(((Exit)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=new Trap_Open();
					else
					if(roll<80)
						theTrap=new Trap_Unlock();
					else
						theTrap=new Trap_Enter();
				}
				else
				{
					if(roll<50)
						theTrap=new Trap_Open();
					else
						theTrap=new Trap_Enter();
				}
			}
			else
				theTrap=new Trap_Enter();
		}
		else
		if(unlockThis instanceof Container)
		{
			if(((Container)unlockThis).hasALid)
			{
				if(((Container)unlockThis).hasALock)
				{
					if(roll<20)
						theTrap=new Trap_Open();
					else
					if(roll<80)
						theTrap=new Trap_Unlock();
					else
						theTrap=new Trap_Get();
				}
				else
				{
					if(roll<50)
						theTrap=new Trap_Open();
					else
						theTrap=new Trap_Get();
				}
			}
			else
				theTrap=new Trap_Get();
		}
		else
		if(unlockThis instanceof Item)
			theTrap=new Trap_Get();
		return theTrap;
	}

	public static void setTrapped(Environmental myThang, boolean isTrapped)
	{
		Trap t=Thief_Trap.getATrap(myThang);
		t.baseEnvStats().setRejuv(50);
		t.recoverEnvStats();
		setTrapped(myThang,t,isTrapped);
	}
	public static void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numAffects();a++)
		{
			Ability A=myThang.fetchAffect(a);
			if(A instanceof Trap)
				A.unInvoke();
		}

		if(isTrapped)
			myThang.addAffect(theTrap);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		String whatTounlock=CommandProcessor.combine(commands,0);
		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatTounlock.toUpperCase());
		Environmental unlockThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatTounlock);
				unlockThis=mob.location().getExit(dirCode);
			}
		}
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoom(mob,null,whatTounlock);

		if(unlockThis==null)
		{
			mob.tell("You don't see that here.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()-unlockThis.envStats().level()-envStats().level())*3));
		Trap theTrap=null;
		for(int a=0;a<unlockThis.numAffects();a++)
		{
			Ability A=unlockThis.fetchAffect(a);
			if(A instanceof Trap)
				theTrap=(Trap)A;
		}
		if(theTrap!=null)
		{
			mob.tell(unlockThis.name()+" is already trapped!");
			return false;
		}

		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to lay a trap on "+unlockThis.name()+".");

		theTrap=this.getATrap(unlockThis);
		int rejuv=((30-mob.envStats().level())*30);
		theTrap.baseEnvStats().setRejuv(rejuv);
		theTrap.recoverEnvStats();


		if(theTrap==null)
		{
			mob.tell("You don't know how to lay a trap on "+unlockThis.name()+".");
			return false;
		}

		theTrap.sprung=false;
		if(success)
		{
			mob.tell("You have completed your task.");
			unlockThis.addAffect(theTrap);
			ServiceEngine.startTickDown(this,ServiceEngine.TRAP_DESTRUCTION,mob.envStats().level()*30);
		}
		else
		{
			if(Dice.rollPercentage()>50)
			{
				unlockThis.addAffect(theTrap);
				ServiceEngine.startTickDown(this,ServiceEngine.TRAP_DESTRUCTION,mob.envStats().level()*30);
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> trigger(s) the trap on accident!");
				theTrap.spring(mob);
			}
			else
			{
				mob.tell("You fail in your attempt.");
			}
		}


		return success;
	}
}