package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_LoveMoon extends Chant
{
	public String ID() { return "Chant_LoveMoon"; }
	public String name(){ return "Love Moon";} 
	public String displayText(){return "(Love Moon)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_LoveMoon();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			if(affected instanceof Room)
				((Room)affected).showHappens(Affect.MSG_OK_VISUAL,"The love moon sets.");
			super.unInvoke();
			return;
		}
		
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("You are no longer under the love moon.");

		super.unInvoke();

	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob.location().fetchAffect(ID())==null)
				unInvoke();
			else
			{
				Vector choices=new Vector();
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if((M!=null)
					&&(M!=mob)
					&&(Sense.canBeSeenBy(M,mob))
					&&(M.charStats().getStat(CharStats.GENDER)!=mob.charStats().getStat(CharStats.GENDER))
					&&(M.charStats().getStat(CharStats.GENDER)!=(int)'N')
					&&(M.charStats().getSave(CharStats.CHARISMA)>14))
						choices.addElement(M);
				}
				if(choices.size()>0)
				{
					MOB M=(MOB)choices.elementAt(Dice.roll(1,choices.size(),-1));
					try{
					if(Dice.rollPercentage()==1)
						ExternalPlay.doCommand(mob,Util.parse("MATE "+M.name()));
					else
					if(Dice.rollPercentage()>10)
						switch(Dice.roll(1,5,0))
						{
						case 1:
							mob.tell("You feel strange urgings towards "+M.name()+".");
							break;
						case 2:
							mob.tell("You have strong happy feelings towards "+M.name()+".");
							break;
						case 3:
							mob.tell("You feel very appreciative of "+M.name()+".");
							break;
						case 4:
							mob.tell("You feel very close to "+M.name()+".");
							break;
						case 5:
							mob.tell("You feel lovingly towards "+M.name()+".");
							break;
						}
					}catch(Exception e){}
				}
			}
		}
		else
		if(affected instanceof Room)
		{
			Room room=(Room)affected;
			if(!Chant_BlueMoon.moonInSky(room,this))
				unInvoke();
			else
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M.fetchAffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					M.addAffect(A);
					M.recoverCharStats();
				}
			}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+6);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(!Chant_BlueMoon.moonInSky(mob.location(),null))
		{
			mob.tell("You must be able to see the moon for this magic to work.");
			return false;
		}
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("This place is already under the love moon.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The Love Moon Rises!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}
