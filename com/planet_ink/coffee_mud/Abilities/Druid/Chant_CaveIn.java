package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CaveIn extends Chant
{
	public String ID() { return "Chant_CaveIn"; }
	public String name(){ return "Cave-In";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS|CAN_EXITS;}
	public Environmental newInstance(){	return new Chant_CaveIn();}
	public int amountRemaining=0;
	public long flags(){return Ability.FLAG_BINDING;}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected instanceof Exit)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_LEAVE)||(msg.targetMinor()==CMMsg.TYP_OPEN))
		&&(msg.source().envStats().height()>=0)
		&&((msg.tool()==affected)||(msg.target()==affected)))
		{
			msg.source().tell("This exit is blocked by rubble, and can not be moved through.");
			return false;
		}
		else
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			MOB mob=(MOB)affected;
			if(msg.sourceMinor()==CMMsg.TYP_STAND)
				return false;
			if((!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&((Util.bset(msg.sourceMajor(),CMMsg.MASK_HANDS))
			||(Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE))))
			{
				mob.location().show(mob,null,null,CMMsg.MSG_OK_ACTION,"<S-NAME> struggle(s) to get out from under the rocks.");
				amountRemaining-=(mob.charStats().getStat(CharStats.STRENGTH)*4);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okMessage(host,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BOUND);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=null;
		if((commands.size()>0)&&(givenTarget==null))
		{
			int dir=Directions.getGoodDirectionCode(Util.combine(commands,0));
			if((dir>=0)&&(dir!=Directions.UP)&&(mob.location().getExitInDir(dir)!=null))
				target=mob.location().getExitInDir(dir);
		}
		if(target==null)
			target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if((target instanceof Item)||(target instanceof Room))
		{
			mob.tell("This chant can only target exits or creatures.");
			return false;
		}
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE))
		{
			mob.tell("This chant only works in caves.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=200;
				if(target instanceof Exit)
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"A cave-in causes rubble to fall, blocking <T-NAME>!");
				else
				if(target instanceof MOB)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"A cave-in drops rocks on <T-NAME>!");
					int maxDie =  (int)Math.round(new Integer(adjustedLevel(mob)).doubleValue()/3.0);
					int damage = Dice.roll(maxDie,3,maxDie);
					if(msg.value()>0)
						damage = (int)Math.round(Util.div(damage,1.5));
					if(((MOB)target).location()==mob.location())
						MUDFight.postDamage(mob,(MOB)target,this,damage,CMMsg.MSG_OK_VISUAL,Weapon.TYPE_BASHING,"The falling rubble <DAMAGE> <T-NAME>!");
				}
				if(msg.value()<=0)
					success=maliciousAffect(mob,target,(target instanceof Exit)?0:10,0);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}