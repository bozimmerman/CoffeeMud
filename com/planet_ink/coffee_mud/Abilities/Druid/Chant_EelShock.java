package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_EelShock extends Chant
{
	public String ID() { return "Chant_EelShock"; }
	public String name(){return "Eel Shock";}
	public String displayText(){return "(Stunned)";}
	public int quality(){return MALICIOUS;}
	public int maxRange() {return 3;}
	public int minRange() {return 0;}
	protected int canAffectCode(){return CAN_MOBS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell("<S-YOUPOSS> are no longer stunned.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(EnvStats.IS_SITTING);
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
		&&(msg.sourceMajor()>0))
		{
			mob.tell("You are stunned.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=MUDFight.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth shocking.");
			return false;
		}

		Room location = mob.location();
		boolean roomWet = false;

		if(location.domainType() == Room.DOMAIN_INDOORS_UNDERWATER ||
		   location.domainType() == Room.DOMAIN_INDOORS_WATERSURFACE ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_UNDERWATER ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_WATERSURFACE ||
		   location.domainType() == Room.DOMAIN_OUTDOORS_SWAMP)
		{
		   roomWet = true;
		}

		Area currentArea = location.getArea();
		if(currentArea.getClimateObj().weatherType(location) == Climate.WEATHER_RAIN ||
		   currentArea.getClimateObj().weatherType(location) == Climate.WEATHER_THUNDERSTORM)
		{
		   roomWet = true;
		}

		if(!roomWet)
		{
				mob.tell("It's too dry to invoke this chant.");
				return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),"^S<S-NAME> chant(s) and electrical sparks dance across <S-HIS-HER> skin.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_GENERAL:0),"<T-NAME> is stunned.");
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
						maliciousAffect(mob,target,3,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> sees tiny sparks dance across <S-HIS-HER> skin, but nothing more happens.");
		// return whether it worked
		return success;
	}
}
