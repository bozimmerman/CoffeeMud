package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonInsects extends Chant
{
	public String ID() { return "Chant_SummonInsects"; }
	public String name(){ return "Summon Insects";}
	public String displayText(){return "(In a swarm of insects)";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 5;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	Room castingLocation=null;
	public Environmental newInstance(){	return new Chant_SummonInsects();}

	public boolean tick(int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB vic=(MOB)affected;
			if(vic.location()!=castingLocation)
				unInvoke();
			else
			if((!vic.amDead())&&(vic.location()!=null))
				ExternalPlay.postDamage(invoker,vic,this,Dice.roll(1,3,0),Affect.TYP_OK_VISUAL,-1,"<T-NAME> <T-IS-ARE> stung by the swarm!");
		}
		return super.tick(tickID);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the insect swarm!");
	}
		

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(h==null)
			{
				mob.location().show(mob,null,affectType(auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appear.  Finding noone to sting, they flutter away.^?");
				return false;
			}
			mob.location().show(mob,null,affectType(auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appears and attacks!^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ACID|(auto?Affect.MASK_GENERAL:0),null);
				if((mob.location().okAffect(msg))
				   &&(mob.location().okAffect(msg2))
				   &&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((!msg.wasModified())&&(!msg2.wasModified())&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,(mob.envStats().level()*10),-1);
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) enveloped by the swarm of stinging insects!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fizzles.");


		// return whether it worked
		return success;
	}
}