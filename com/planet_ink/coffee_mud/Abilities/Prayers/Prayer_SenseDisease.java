package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SenseDisease extends Prayer
{
	public String ID() { return "Prayer_SenseDisease"; }
	public String name(){ return "Sense Disease";}
	public String displayText(){ return "(Sense Disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return OK_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	private Room lastRoom=null;

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
			mob.tell("Your disease sensations fade.");
		}
	}

	public Ability getDisease(Environmental mob)
	{
		for(int m=0;m<mob.numEffects();m++)
		{
			Ability A=mob.fetchEffect(m);
			if((A.classificationCode()&A.ALL_CODES)==A.DISEASE)
				return A;
		}
		return null;
	}
	private static final Vector empty=new Vector();
	public Vector diseased(MOB mob, Room R)
	{
		if(R==null) return empty;
		Vector V=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M!=mob)&&(getDisease(M)!=null))
			{
				if(V==null) V=new Vector();
				V.addElement(M);
			}
		}
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I!=null)
			&&(I.container()==null)
			&&(getDisease(I)!=null))
			{
				if(V==null) V=new Vector();
				V.addElement(I);
			}
		}
		if(V!=null)
			return V;
		else
			return empty;
	}

	public void messageTo(MOB mob)
	{
		String last="";
		String dirs="";
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=mob.location().getRoomInDir(d);
			Exit E=mob.location().getExitInDir(d);
			if((R!=null)&&(E!=null)&&(diseased(mob,R).size()>0))
			{
				if(last.length()>0)
					dirs+=", "+last;
				last=Directions.getFromDirectionName(d);
			}
		}
		Vector V=diseased(mob,mob.location());
		if(V.size()>0)
		{
			boolean didSomething=false;
			for(int v=0;v<V.size();v++)
			{
				Environmental E=(Environmental)V.elementAt(v);
				if(Sense.canBeSeenBy(E,mob))
				{
					didSomething=true;
					if(last.length()>0)
						dirs+=", "+last;
					last=E.name();
				}
			}
			if(!didSomething)
			{
				if(last.length()>0)
					dirs+=", "+last;
				last="here";
			}
		}

		if((dirs.length()==0)&&(last.length()==0))
			mob.tell("You do not sense any disease.");
		else
		if(dirs.length()==0)
			mob.tell("You sense disease coming from "+last+".");
		else
			mob.tell("You sense disease coming from "+dirs.substring(2)+", and "+last+".");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
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

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) disease senses!":"^S<S-NAME> listen(s) for a message from "+hisHerDiety(mob)+".^?");
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
