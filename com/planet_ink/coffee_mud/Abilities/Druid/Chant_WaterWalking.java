package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_WaterWalking extends Chant
{
	public String ID() { return "Chant_WaterWalking"; }
	public String name(){ return "Water Walking";}
	public String displayText(){return "(Water Walking)";}
	private boolean triggerNow=false;
	public Environmental newInstance(){	return new Chant_WaterWalking();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(triggerNow||((mob.location()!=null)&&(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
		}
	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		if(affected==null) return true;
		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(mob.location()!=null)
		&&(affect.target()!=null)
		&&(affect.target() instanceof Room))
		{
			if((affect.sourceMinor()==Affect.TYP_ENTER)
			&&(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(affect.target()==mob.location().getRoomInDir(Directions.UP)))
			{
				affect.source().tell("Your water walking magic prevents you from ascending from the water surface.");
				return false;
			}
			else
			if((affect.sourceMinor()==Affect.TYP_LEAVE)
			&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Exit))
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().getRoomInDir(d);
					if((R!=null)
					&&(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					{
						for(int dx=0;dx<Directions.NUM_DIRECTIONS;dx++)
						{
							Exit E=R.getExitInDir(dx);
							if((E!=null)&&(E==affect.tool()))
							{
								triggerNow=true;
								affect.source().recoverEnvStats();
								return true;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(triggerNow)triggerNow=false;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You have a sinking feeling that your water walking ability is gone.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already a water walker.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> feel(s) a little lighter!");
					success=beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fizzles.");

		// return whether it worked
		return success;
	}
}