package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Wave extends Prayer
{
	public String ID() { return "Prayer_Wave"; }
	public String name(){ return "Wave";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Waved)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prayer_Wave();}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null) return false;
		int dir=Directions.getGoodDirectionCode(Util.combine(commands,0));
		if(dir<0)
		{
			mob.tell("Wash your opponents which direction?");
			return false;
		}
		Room destRoom=mob.location().getRoomInDir(dir);
		Exit exitRoom=mob.location().getExitInDir(dir);
		if((destRoom==null)||(exitRoom==null)||(dir==Directions.UP))
		{
			mob.tell("You can't wash your opponents that way!");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);
		int numEnemies=h.size();
		for(Iterator e=h.iterator();e.hasNext();)
		{
			MOB target=(MOB)e.next();
			if(target!=mob)
			{
				if(success)
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"<T-NAME> <T-IS-ARE> swept away by a great wave!":"^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+".^?");					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						int harming=Dice.roll(4,adjustedLevel(mob)/numEnemies,numEnemies);
						MUDFight.postDamage(mob,target,this,harming,CMMsg.MASK_GENERAL|CMMsg.TYP_WATER,Weapon.TYPE_BURSTING,"A crashing wave <DAMAGE> <T-NAME>!");
						int chanceToStay=10+(target.charStats().getStat(CharStats.STRENGTH)-mob.envStats().level()*4);
						int roll=Dice.rollPercentage();
						if((roll!=1)&&(roll>chanceToStay))
							MUDTracker.move(target,dir,true,false);
					}
				}
				else
					maliciousFizzle(mob,target,"<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, "+prayingWord(mob)+", but "+hisHerDiety(mob)+" does not heed.");
			}
		}
		return success;
	}
}