package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MagicMissile extends Spell
{
	public String ID() { return "Spell_MagicMissile"; }
	public String name(){return "Magic Missile";}
	public String displayText(){return "(Magic Missile spell)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){return new Spell_MagicMissile();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			int numMissiles=((int)Math.round(Math.floor(Util.div(adjustedLevel(mob),5)))+1);
			for(int i=0;i<numMissiles;i++)
			{
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(i==0)?((auto?"A magic missle appears hurling full speed at <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, shooting forth a magic missile!^?")+CommonStrings.msp("spelldam2.wav",40)):null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						int damage = 0;
						damage += Dice.roll(1,11,11/numMissiles);
						if(target.location()==mob.location())
							ExternalPlay.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,((i==0)?"^SThe missile ":"^SAnother missile ")+"<DAMAGE> <T-NAME>!^?");
					}
				}
				if(target.amDead())
				{
					target=this.getTarget(mob,commands,givenTarget,true);
					if(target==null)
						break;
					if(target.amDead())
						break;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
