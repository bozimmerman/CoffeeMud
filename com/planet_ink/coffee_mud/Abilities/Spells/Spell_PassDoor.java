package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_PassDoor extends Spell
{
	public String ID() { return "Spell_PassDoor"; }
	public String name(){return "Pass Door";}
	public String displayText(){return "(Translucent)";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_PassDoor();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		affectedStats.setDisposition(affectedStats.disposition()|EnvStats.IS_NOT_SEEN);
		affectedStats.setHeight(-1);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer translucent.");

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
			FullMsg msg=new FullMsg(mob,null,null,affectType(auto),"<S-NAME> shimmer(s) and turn(s) translucent.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,5);
				mob.recoverEnvStats();
			}
		}
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType(auto),"^S<S-NAME> shimmer(s) and pass(es) "+Directions.getDirectionName(dirCode)+".^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.addAffect(this);
				mob.recoverEnvStats();
				mob.tell("\n\r\n\r");
				ExternalPlay.move(mob,dirCode,false,false);
				mob.delAffect(this);
				mob.recoverEnvStats();
			}
		}

		return success;
	}
}