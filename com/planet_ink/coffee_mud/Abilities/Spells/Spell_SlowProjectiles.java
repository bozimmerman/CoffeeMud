package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SlowProjectiles extends Spell
{
	public String ID() { return "Spell_SlowProjectiles"; }
	public String name(){return "Slow Projectiles";}
	public String displayText(){return "(Slow Projectiles)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Spell_SlowProjectiles();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.source().getVictim()==affect.target())
		&&(affect.source().rangeToTarget()>0)
		&&(affect.tool() instanceof Weapon)
		&&(((((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)&&(((Weapon)affect.tool()).requiresAmmunition())
			||(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN)))
		&&(affect.source().location()!=null)
		&&(affect.source().location()==affected)
		&&(!affect.source().amDead()))
		{
			if(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_THROWN)
				affect.source().location().show(affect.source(),null,affect.tool(),Affect.MSG_OK_VISUAL,"<O-NAME> flys slowly by.");
			else
				affect.source().location().show(affect.source(),null,Affect.MSG_OK_VISUAL,"The shot from "+affect.tool().name()+" flys slowly by.");
			int damage=(affect.targetCode()-Affect.MASK_HURT)/2;
			affect.modify(affect.source(),
						  affect.target(),
						  affect.tool(),
						  affect.sourceCode(),
						  affect.sourceMessage(),
						  Affect.MASK_HURT+damage,
						  affect.targetMessage(),
						  affect.othersCode(),
						  affect.othersMessage());
		}
		return super.okAffect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"Projectiles are already slow here!");
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> encant(s) slowly.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a field of slowness, but fail(s).");

		return success;
	}
}
