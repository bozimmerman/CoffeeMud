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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
		&&(msg.tool()!=null)
		&&(msg.source().getVictim()==msg.target())
		&&(msg.source().rangeToTarget()>0)
		&&(msg.tool() instanceof Weapon)
		&&(((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)&&(((Weapon)msg.tool()).requiresAmmunition())
			||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN)))
		&&(msg.source().location()!=null)
		&&(msg.source().location()==affected)
		&&(!msg.source().amDead()))
		{
			if(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN)
				msg.source().location().show(msg.source(),null,msg.tool(),CMMsg.MSG_OK_VISUAL,"<O-NAME> flies slowly by.");
			else
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,"The shot from "+msg.tool().name()+" flies slowly by.");
			int damage=(msg.targetCode()-CMMsg.MASK_HURT)/2;
			SaucerSupport.adjustDamageMessage(msg,damage*-1);
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;

		if(target.fetchEffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"Projectiles are already slow here!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) slowly.^?");
			if(mob.location().okMessage(mob,msg))
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
