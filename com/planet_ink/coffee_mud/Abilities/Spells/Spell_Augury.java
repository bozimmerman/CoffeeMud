package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Augury extends Spell
{
	public String ID() { return "Spell_Augury"; }
	public String name(){return "Augury";}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_Augury();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean isTrapped(Environmental E)
	{
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)&&(A instanceof Trap))
				return true;
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Divine the fate of which direction?");
			return false;
		}
		String targetName=Util.combine(commands,0);

		Exit exit=null;
		Exit opExit=null;
		Room room=null;
		int dirCode=Directions.getGoodDirectionCode(targetName);
		if(dirCode>=0)
		{
			exit=mob.location().getExitInDir(dirCode);
			room=mob.location().getRoomInDir(dirCode);
			if(room!=null)
				opExit=mob.location().getReverseExit(dirCode);
		}
		else
		{
			mob.tell("Divine the fate of which direction?");
			return false;
		}
		if((exit==null)||(room==null))
		{
			mob.tell("You couldn't go that way if you wanted to!");
			return false;
		}

		if(!super.invoke(mob,commands,null,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger "+Directions.getDirectionName(dirCode)+", incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				boolean aggressiveMonster=false;
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mon=room.fetchInhabitant(m);
					if(mon!=null)
						for(int b=0;b<mon.numBehaviors();b++)
						{
							Behavior B=mon.fetchBehavior(b);
							if((B!=null)&&(B.grantsAggressivenessTo(mob)))
							{
								aggressiveMonster=true;
								break;
							}
						}
				}
				mob.location().send(mob,msg);
				if((isTrapped(exit))
				||(isTrapped(room))
				||(aggressiveMonster)
				||((opExit!=null)&&(isTrapped(opExit))))
					mob.tell("You feel going that way would be bad.");
				else
					mob.tell("You feel going that way would be ok.");
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> point(s) <S-HIS-HER> finger "+Directions.getDirectionName(dirCode)+", incanting, but then loses concentration.");


		// return whether it worked
		return success;
	}
}