package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Enthrall extends Spell
{
	public String ID() { return "Spell_Enthrall"; }
	public String name(){return "Enthrall";}
	public String displayText(){return "(Enthralled)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Enthrall();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public long flags(){return Ability.FLAG_CHARMING;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(mob.amFollowing()==null) return super.okAffect(myHost,affect);

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.source()==mob.amFollowing()))
				unInvoke();
		if((affect.amISource(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.target()==mob.amFollowing()))
		{
			mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
			return false;
		}
		else
		if((affect.amISource(mob))
		&&(!mob.isMonster())
		&&(affect.target() instanceof Room)
		&&(affect.targetMinor()==affect.TYP_LEAVE)
		&&(mob.amFollowing()!=null)
		&&(((Room)affect.target()).isInhabitant(mob.amFollowing())))
		{
			mob.tell("You don't want to leave your friend.");
			return false;
		}
		else
		if((affect.amISource(mob))
		&&(mob.amFollowing()!=null)
		&&(affect.sourceMinor()==Affect.TYP_NOFOLLOW))
		{
			mob.tell("You like "+mob.amFollowing().name()+" too much.");
			return false;
		}
		   
		

		return super.okAffect(myHost,affect);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		MOB mob=(MOB)affecting();
		if((affected==mob)&&((mob.amFollowing()==null)||(mob.amFollowing()!=invoker)))
			ExternalPlay.follow(mob,invoker,true);
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
			if((mob.isMonster())&&(!Sense.isMobile(mob)))
				CoffeeUtensils.wanderAway(mob,true,true);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.mayIFight(mob))||(levelDiff>=10))
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(-10-((target.charStats().getStat(CharStats.INTELLIGENCE))+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"^S<S-NAME> smile(s) powerfully at <T-NAMESELF>.^?";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_VERBAL_SPELL,str);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,Affect.MSK_CAST_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0));
					if(success)
					{
						ExternalPlay.follow(target,mob,false);
						ExternalPlay.makePeaceInGroup(mob);
						if(target.amFollowing()!=mob)
							mob.tell(target.name()+" seems unwilling to follow you.");
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> smile(s) powerfully at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
