package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Anchor extends Spell
{
	public Spell_Anchor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Anchor";
		displayText="(Anchor)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Anchor();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ABJURATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		mob.tell("Your anchor has been lifted.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&("spell_summon;spell_gate".toUpperCase().indexOf(affect.tool().ID().toUpperCase())>=0))
		{
			Room roomS=null;
			Room roomD=null;
			if((affect.target()!=null)&&(affect.target() instanceof MOB)&&(((MOB)affect.target()).location()!=null))
				roomD=((MOB)affect.target()).location();
			else
			if((affect.source()!=null)&&(affect.source().location()!=null))
				roomS=affect.source().location();
			else
			if((affect.target()!=null)&&(affect.target() instanceof Room))
				roomD=(Room)affect.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.show(affect.source(),null,Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			if(roomD!=null)
				roomD.show(affect.source(),null,Affect.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"An magical anchoring field envelopes <T-NAME>!":"<S-NAME> invoke(s) an anchoring field of protection around <T-NAMESELF>."));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an anchoring field, but fail(s).");

		return success;
	}
}
