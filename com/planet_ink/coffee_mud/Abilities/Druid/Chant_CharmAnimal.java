package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_CharmAnimal extends Chant
{
	public String ID() { return "Chant_CharmAnimal"; }
	public String name(){ return "Charm Animal";}
	public String displayText(){return "(Charmed)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_CHARMING;}

	private MOB charmer=null;
	private MOB getCharmer()
	{
		if(charmer!=null) return charmer;
		if((invoker!=null)&&(invoker!=affected))
			charmer=invoker;
		else
		if((text().length()>0)&&(affected instanceof MOB))
		{
			Room R=((MOB)affected).location();
			if(R!=null)
				charmer=R.fetchInhabitant(text());
		}
		if(charmer==null) return invoker;
		return charmer;
	}

	public Environmental newInstance(){	return new Chant_CharmAnimal();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.source()==mob.amFollowing()))
				unInvoke();
		if((msg.amISource(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.target()==mob.amFollowing()))
		{
			mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(!mob.isMonster())
		&&(msg.target() instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(mob.amFollowing()!=null)
		&&(((Room)msg.target()).isInhabitant(mob.amFollowing())))
		{
			mob.tell("You don't want to leave your friend.");
			return false;
		}
		else
		if((msg.amISource(mob))
		&&(mob.amFollowing()!=null)
		&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW))
		{
			mob.tell("You like "+mob.amFollowing().name()+" too much.");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		MOB mob=(MOB)affecting();
		if((affected==mob)&&((mob.amFollowing()==null)||(mob.amFollowing()!=getCharmer())))
			ExternalPlay.follow(mob,getCharmer(),true);
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			mob.tell("Your free-will returns.");
			ExternalPlay.follow(mob,null,false);
			ExternalPlay.standIfNecessary(mob);
			if(mob.isMonster())
			{
				if(Dice.rollPercentage()>50)
				{
					if(!Sense.isMobile(mob))
						CoffeeUtensils.wanderAway(mob,true,true);
				}
				else
				if((invoker!=null)&&(invoker!=mob))
					mob.setVictim(invoker);
			}
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!Sense.isAnimalIntelligence(target))
		{
			mob.tell(target.name()+" is not an animal!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?";
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
					if(success)
					{
						if(target.isInCombat()) target.makePeace();
						ExternalPlay.follow(target,mob,false);
						ExternalPlay.makePeaceInGroup(mob);
						if(target.amFollowing()!=mob)
							mob.tell(target.name()+" seems unwilling to follow you.");
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
