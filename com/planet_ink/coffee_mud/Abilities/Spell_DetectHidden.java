package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_DetectHidden extends Spell
	implements DivinationDevotion
{

	public boolean successfulObservation=false;

	public Spell_DetectHidden()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Detect Hidden";
		displayText="(Detect Hidden)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		addQualifyingClass(new Mage().ID(),7);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DetectHidden();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell(mob,null,"Your vision is no longer as keen.");
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(successfulObservation)
			affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_HIDDEN);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already detecting hidden things.");
			return false;
		}

		String str="<S-NAME> attain(s) keen vision!";

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			successfulObservation=success;
			beneficialAffect(mob,mob,0);
		}

		return success;
	}
}
