package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_CauseStink extends Spell
{
	public String ID() { return "Spell_CauseStink"; }
	public String name(){return "Cause Stink";}
	public String displayText(){return "(Cause Stink)";}
	public int quality(){return MALICIOUS;};
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_CauseStink();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}
	public int cycle=1;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID==MudHost.TICK_MOB)
		{
			if(Dice.rollPercentage()>25) return true;
			if((affected==null)||(!(affected instanceof MOB))) return false;
			MOB mob=(MOB)affected;
			Room room=mob.location();
			if(room==null) return false;

			String str=null;
			switch(cycle++)
			{
			case 1: str="<S-NAME> emanates an unpleasant odor"; break;
			case 2: str="<S-NAME> smells like <S-HE-SHE> hasn't bathed in a month!"; break;
			case 3: str="<S-NAME> smells bad!"; break;
			case 4: str="<S-NAME> is giving off a horrid odor!"; break;
			case 5: str="<S-NAME> really stinks!"; break;
			case 6: str="Whew! <S-NAME> REALLY stinks!"; break;
			case 7: str="<S-NAME> has an odor resembling that of a skunk!"; break;
			case 8: str="<S-NAME> smells like a dead skunk!"; break;
			case 9: str="<S-NAME> stinks horribly!"; break;
			case 10: str="<S-NAME> seems very very stinky!"; cycle=1; break;
			default: cycle=0; break;
			}
			if(str!=null)
			{
				FullMsg msg=new FullMsg(mob,null,CMMsg.MASK_GENERAL|CMMsg.MASK_SOUND|CMMsg.MASK_EYES|CMMsg.TYP_GENERAL,str);
				if(room.okMessage(mob,msg))
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB M2=room.fetchInhabitant(m);
					if((!M2.isMonster())&&(M2!=mob)&&(Sense.canSmell(M2)))
						M2.executeMsg(M2,msg);
				}
			}
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R=room.getRoomInDir(d);
				if((R!=null)&&(R.numPCInhabitants()>0))
					R.showHappens(CMMsg.MASK_GENERAL|CMMsg.MASK_SOUND|CMMsg.MASK_EYES|CMMsg.TYP_GENERAL,
								  "There is a very bad smell coming from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(d)));
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) and utter(s) a stinky spell at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and utter(s) a spell at <T-NAMESELF>, but it fizzles.");

		// return whether it worked
		return success;
	}
}