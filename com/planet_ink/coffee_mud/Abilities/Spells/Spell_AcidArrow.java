package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AcidArrow extends Spell
{
	public String ID() { return "Spell_AcidArrow"; }
	public String name(){ return "Acid Arrow";}
	public String displayText(){ return "(Acid Arrow)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int maxRange(){return 2;}
	public Environmental newInstance(){	return new Spell_AcidArrow();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB vic=(MOB)affected;
			if((!vic.amDead())&&(vic.location()!=null))
				ExternalPlay.postDamage(invoker,vic,this,Dice.roll(2,4,0),Affect.TYP_ACID,-1,"<T-NAME> sizzle(s) from the acid arrow residue!");
		}
		return super.tick(ticking,tickID);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"An arrow made of acid appears zooming towards <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, conjuring an acid arrow from the java plain!^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ACID|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))&&((mob.location().okAffect(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				invoker=mob;
                int numDice = adjustedLevel(mob);
				int damage = Dice.roll(numDice, 4, 5);
				if((msg2.wasModified())||(msg.wasModified()))
					damage = (int)Math.round(Util.div(damage,2.0));
				ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.TYP_ACID,Weapon.TYPE_MELTING,"The acidic blast <DAMAGE> <T-NAME>!");
				maliciousAffect(mob,target,3,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and conjur(s) at <T-NAMESELF>, but nothing more happens.");

		return success;
	}
}
