package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_ResistFire extends Spell
{

	public Spell_ResistFire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resist Fire";
		displayText="(Resist Fire)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(6);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ResistFire();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_ABJURATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		mob.tell("Your cool protection warms up.");

		super.unInvoke();

	}


	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_FIRE)
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"The cool coating around <S-NAME> absorbs the hot blast."));
			affect.tagModified(true);
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> invoke(s) a cool field of protection around <T-NAMESELF>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,0);
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke fire protection, but fail(s).");

		return success;
	}
}