package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Dragonfire extends Spell
{
	public String ID() { return "Spell_Dragonfire"; }
	public String name(){return "Dragonfire";}
	public String displayText(){return "(Dragonfire)";}
	public int maxRange(){return 3;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_Dragonfire();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth burning.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{

			if(mob.location().show(mob,null,this,affectType(auto),(auto?"A blast of flames erupt!":"^S<S-NAME> blast(s) flames from <S-HIS-HER> mouth!^?")+CommonStrings.msp("fireball.wav",40)))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_GENERAL:0),null);
				if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int maxDie =  adjustedLevel(mob);
					int damage = Dice.roll(maxDie,6,maxDie);
					if((msg.value()>0)||(msg2.value()>0))
						damage = (int)Math.round(Util.div(damage,2.0));

					ExternalPlay.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The dragonfire <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.");


		// return whether it worked
		return success;
	}
}
