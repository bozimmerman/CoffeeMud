package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;


public class Spell_DetectMagic extends Spell
	implements DivinationDevotion
{

	public boolean successfulObservation=false;

	public Spell_DetectMagic()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Magic";
		displayText="(Detect Magic)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(5);

		addQualifyingClass("Mage",5);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DetectMagic();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"Your eyes cease to sparkle.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(successfulObservation)
			affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_BONUS);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting magic.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> gain(s) sparkling eyes!":"<S-NAME> chant(s) for sparkling eyes!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			successfulObservation=success;
			beneficialAffect(mob,mob,0);
		}

		return success;
	}
}
