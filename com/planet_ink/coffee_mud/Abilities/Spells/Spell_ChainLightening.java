package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ChainLightening extends Spell
{
	public String ID() { return "Spell_ChainLightening"; }
	public String name(){return "Chain Lightning";}
	public int maxRange(){return 2;}
	public int quality(){return MALICIOUS;};
	public Environmental newInstance(){	return new Spell_ChainLightening();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null) h=new Hashtable();

		Hashtable myGroup=mob.getGroupMembers(new Hashtable());
		Vector targets=new Vector();
		for(Enumeration e=h.elements();e.hasMoreElements();)
			targets.addElement(e.nextElement());
		for(Enumeration e=myGroup.elements();e.hasMoreElements();)
		{
			MOB M=(MOB)e.nextElement();
			if((M!=mob)&&(!targets.contains(M))) targets.addElement(M);
		}
		targets.addElement(mob);

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int maxDie=adjustedLevel(mob);
		int damage = Dice.roll(maxDie,8,1);

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			mob.location().show(mob,null,affectType(auto),auto?"A thunderous crack of lightning erupts!":"^S<S-NAME> invoke(s) a thunderous crack of lightning.^?");
			while(damage>0)
			for(int i=0;i<targets.size();i++)
			{
				MOB target=(MOB)targets.elementAt(i);
				if(target.amDead()||(target.location()!=mob.location()))
				{
					int count=0;
					for(int i2=0;i2<targets.size();i2++)
					{
						MOB M2=(MOB)targets.elementAt(i2);
						if((!M2.amDead())
						   &&(mob.location()!=null)
						   &&(mob.location().isInhabitant(M2))
						   &&(M2.location()==mob.location()))
							 count++;
					}
					if(count<2)
						return true;
					continue;
				}

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				boolean oldAuto=auto;
				if((target==mob)||(myGroup.contains(target)))
				   auto=true;
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ELECTRIC|(auto?Affect.MASK_GENERAL:0),null);
				auto=oldAuto;
				if((mob.location().okAffect(mob,msg))&&((mob.location().okAffect(mob,msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int dmg=damage;
					if((!msg.wasModified())&&(!msg2.wasModified()))
						dmg = (int)Math.round(Util.div(dmg,2.0));
					if(target.location()==mob.location())
					{
						ExternalPlay.postDamage(mob,target,this,dmg,Affect.MASK_GENERAL|Affect.TYP_ELECTRIC,Weapon.TYPE_STRIKING,"The bolt <DAMAGE> <T-NAME>!");
						damage = (int)Math.round(Util.div(damage,2.0));
						if(damage<5){ damage=0; break;}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}