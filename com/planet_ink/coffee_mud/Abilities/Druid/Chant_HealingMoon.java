package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_HealingMoon extends Chant
{
	public String ID() { return "Chant_HealingMoon"; }
	public String name(){ return "Healing Moon";}
	public String displayText(){return "(Healing Moon)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_HealingMoon();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("You are no longer under the healing moon.");

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
				mob.curState().adjHitPoints(mob.charStats().getStat(CharStats.CONSTITUTION),mob.maxState());
		}
		else
		if(affected instanceof Room)
		{
			Room room=(Room)affected;
			if((room.getArea().weatherType(room)==Area.WEATHER_BLIZZARD)
			||(room.getArea().weatherType(room)==Area.WEATHER_CLOUDY)
			||(room.getArea().weatherType(room)==Area.WEATHER_DUSTSTORM)
			||(room.getArea().weatherType(room)==Area.WEATHER_HAIL)
			||(room.getArea().weatherType(room)==Area.WEATHER_RAIN)
			||(room.getArea().weatherType(room)==Area.WEATHER_SLEET)
			||(room.getArea().weatherType(room)==Area.WEATHER_SNOW)
			||(room.getArea().weatherType(room)==Area.WEATHER_THUNDERSTORM)
			||((room.getArea().getTODCode()!=Area.TIME_DUSK)
			   &&(room.getArea().getTODCode()!=Area.TIME_NIGHT)))
				unInvoke();
			else
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M.fetchAffect(ID())==null))
				{
					Ability A=(Ability)copyOf();
					M.addAffect(A);
				}
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((target.domainType()&Room.INDOORS)>0)
		{
			mob.tell("You cannot summon the healing moon here.");
			return false;
		}
		if((target.getArea().weatherType(target)==Area.WEATHER_BLIZZARD)
		||(target.getArea().weatherType(target)==Area.WEATHER_CLOUDY)
		||(target.getArea().weatherType(target)==Area.WEATHER_DUSTSTORM)
		||(target.getArea().weatherType(target)==Area.WEATHER_HAIL)
		||(target.getArea().weatherType(target)==Area.WEATHER_RAIN)
		||(target.getArea().weatherType(target)==Area.WEATHER_SLEET)
		||(target.getArea().weatherType(target)==Area.WEATHER_SNOW)
		||(target.getArea().weatherType(target)==Area.WEATHER_THUNDERSTORM)
		||((target.getArea().getTODCode()!=Area.TIME_DUSK)
		   &&(target.getArea().getTODCode()!=Area.TIME_NIGHT)))
		{
			mob.tell("You cannot see the moon right now.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the moon.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The Healing Moon Rises!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the moon, but the magic fades.");
		// return whether it worked
		return success;
	}
}