package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseLife extends Prayer
{
	public String ID() { return "Prayer_SenseLife"; }
	public String name(){ return "Sense Life";}
	public String displayText(){ return "(Sense Life)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	private Room lastRoom=null;
	public Environmental newInstance(){	return new Prayer_SenseLife();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			lastRoom=null;
			mob.tell("Your life sensations fade.");
		}
	}

	public boolean inhabitated(MOB mob, Room R)
	{
		if(R==null) return false;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(!Sense.isGolem(M))&&(M!=mob))
				return true;
		}
		return false;
	}

	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(inhabitated(mob,R)))
			{
				if(last.length()>0)
					dirs+=", "+last;
				last=Directions.getFromDirectionName(d);
			}
		}
		if(inhabitated(mob,mob.location()))
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

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.TICK_MOB)
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

		Environmental target=mob;
		if((auto)&&(givenTarget!=null)) target=givenTarget;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) life-like senses!":"^S<S-NAME> listen(s) for a message from "+hisHerDiety(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> listen(s) to "+hisHerDiety(mob)+" for a message, but there is no answer.");


		// return whether it worked
		return success;
	}
}
