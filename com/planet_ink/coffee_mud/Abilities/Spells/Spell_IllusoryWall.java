package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IllusoryWall extends Spell
{
	public Spell_IllusoryWall()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Illusory Wall";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=Ability.CAN_EXITS;
		canTargetCode=Ability.CAN_EXITS;
		
		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_IllusoryWall();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		String whatToOpen=Util.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode<0)
		{
			mob.tell("Cast which direction?!");
			return false;
		}

		Exit exit=mob.location().getExitInDir(dirCode);
		Room room=mob.location().getRoomInDir(dirCode);

		if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob))))
		{
			mob.tell("That way is already closed.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> whisper(s) "+Directions.getDirectionName(dirCode)+", but nothing happens.");
		else
		{
			FullMsg msg=new FullMsg(mob,exit,null,affectType,"<S-NAME> whisper(s) "+Directions.getDirectionName(dirCode)+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,exit,0);
			}
		}

		return success;
	}
}