package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_PassDoor extends Spell
{
	public Spell_PassDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Pass Door";
		displayText="(Translucent)";
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=0;

		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_PassDoor();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_CONJURATION;
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		affectedStats.setDisposition(affectedStats.disposition()|EnvStats.IS_SEEN);
		affectedStats.setHeight(-1);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("You are no longer translucent.");

		super.unInvoke();

	}

	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		String whatToOpen=Util.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(!auto)
		{
			if(dirCode<0)
			{
				mob.tell("Pass which direction?!");
				return false;
			}

			Exit exit=mob.location().getExitInDir(dirCode);
			Room room=mob.location().getRoomInDir(dirCode);

			if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob))))
			{
				mob.tell("You can't see anywhere to pass that way.");
				return false;
			}
			//Exit opExit=room.getPairedExit(dirCode);
			if(exit.isOpen())
			{
				mob.tell("But it looks free and clear that way!");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if((!success)
		||(mob.fetchAffect(ID())!=null))
			beneficialVisualFizzle(mob,null,"<S-NAME> walk(s) "+Directions.getDirectionName(dirCode)+", but go(es) no further.");
		else
		if(auto)
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType,"<S-NAME> shimmer(s) and turn(s) translucent.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,5);
				mob.recoverEnvStats();
			}
		}
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType,"^S<S-NAME> shimmer(s) and pass(es) "+Directions.getDirectionName(dirCode)+".^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.addAffect(this);
				mob.recoverEnvStats();
				mob.tell("\n\r\n\r");
				ExternalPlay.move(mob,dirCode,false);
				mob.delAffect(this);
				mob.recoverEnvStats();
			}
		}

		return success;
	}
}