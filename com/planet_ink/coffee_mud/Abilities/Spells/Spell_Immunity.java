package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Immunity extends Spell
{
	private int immunityType=-1;
	private String immunityName="";
	public Spell_Immunity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Immunity";
		displayText="(Immunity)";
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
		return new Spell_Immunity();
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
		mob.tell("Your immunity has passed.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==immunityType)
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> is immune to "+immunityName+"."));
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> attain(s) an immunity barrier.":"<S-NAME> invoke(s) an immunity barrier around <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				switch(Dice.roll(1,5,0))
				{
				case 1: 
					immunityType=Affect.TYP_ACID;
					immunityName="acid";
					break;
				case 2: 
					immunityType=Affect.TYP_FIRE;
					immunityName="fire";
					break;
				case 3: 
					immunityType=Affect.TYP_GAS;
					immunityName="gas";
					break;
				case 4: 
					immunityType=Affect.TYP_COLD;
					immunityName="acid";
					displayText="(immunity to cold)";
					break;
				case 5: 
					immunityType=Affect.TYP_ELECTRIC;
					immunityName="electricity";
					displayText="(immunity to electricity)";
					break;
				}
				displayText="(immunity to "+immunityName+")";
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an immunity barrie, but fail(s).");

		return success;
	}
}
