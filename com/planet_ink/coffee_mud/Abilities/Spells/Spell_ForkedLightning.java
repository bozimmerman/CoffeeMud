package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ForkedLightning extends Spell
{
	public String ID() { return "Spell_ForkedLightning"; }
	public String name(){return "Forked Lightning";}
	public int maxRange(){return 2;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_ForkedLightning();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth electrocuting.");
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

			if(mob.location().show(mob,null,this,affectType(auto),(auto?"A thunderous crack of lightning erupts!":"^S<S-NAME> invoke(s) a thunderous crack of forked lightning.^?")+CommonStrings.msp("lightning.wav",40)))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_ELECTRIC|(auto?CMMsg.MASK_GENERAL:0),null);
				if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int maxDie=(int)Math.round(Util.div(adjustedLevel(mob),2.0));
					int damage = Dice.roll(maxDie,8,1);
					if((msg.wasModified())||(msg2.wasModified()))
						damage = (int)Math.round(Util.div(damage,2.0));
					if(target.location()==mob.location())
						ExternalPlay.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"A bolt <DAMAGE> <T-NAME>!");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}