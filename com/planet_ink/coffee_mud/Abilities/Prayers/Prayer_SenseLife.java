package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseLife extends Prayer
{
	private Room lastRoom=null;
	public Prayer_SenseLife()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sense Life";
		baseEnvStats().setLevel(1);

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_SenseLife();
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		
		super.unInvoke();
		lastRoom=null;
		mob.tell("Your life sensations fade.");
	}
	
	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null))
			{
				if(R.numInhabitants()>0)
				{
					if(last.length()>0)
						dirs+=", "+last;
					last=Directions.getFromDirectionName(d);
				}
			}
		}
		if(mob.location().numInhabitants()>1)
		{
			if(last.length()>0)
				dirs+=", "+last;
			last="here";
		}

		if((dirs.length()==0)&&(last.length()==0))
			mob.tell("You do not sense any life beyond your own.");
		else
		if(dirs.length()==0)
			mob.tell("You sense a life force coming from "+last+".");
		else
			mob.tell("You sense a life force coming from "+dirs.substring(2)+", and "+last+".");
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		   &&(affected!=null)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> listen(s) for a message from <S-HIS-HER> god.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> listen(s) to <S-HIS-HER> god for a message, but there is no answer.");


		// return whether it worked
		return success;
	}
}
