package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SpellWard extends Chant
{
	public String ID() { return "Chant_SpellWard"; }
	public String name(){return "Spell Ward";}
	public String displayText(){return "(Spell Ward)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_SpellWard();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your ward against spells fades.");

		super.unInvoke();

	}


	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okAffect(myHost,affect);

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&((((Ability)affect.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
		&&(invoker!=null)
		&&(!mob.amDead())
		&&(Dice.rollPercentage()<35))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"The ward around <S-NAME> inhibits "+affect.tool().name()+"!");
			return false;
		}
		return super.okAffect(myHost,affect);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchAffect(ID())!=null)
		{
			target.tell("You are already affected by "+name()+".");
			return false;
		}
		

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> <T-IS-ARE> protected from spells.":"^S<S-NAME> chant(s) for a ward against spells around <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a ward, but nothing happens.");

		return success;
	}
}