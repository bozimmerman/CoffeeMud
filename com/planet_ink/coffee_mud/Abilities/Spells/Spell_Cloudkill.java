package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Cloudkill extends Spell
{
	public String ID() { return "Spell_Cloudkill"; }
	public String name(){return "Cloudkill";}
	public String displayText(){return "(Cloudkill)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Cloudkill();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell("You feel less intoxicated.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth clouding.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"A horrendous green cloud appears!":"^S<S-NAME> evoke(s) a horrendous green cloud.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_GAS|(auto?CMMsg.MASK_GENERAL:0),null);
				if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					invoker=mob;

					int damage = target.curState().getHitPoints();

					int midLevel=(int)Math.round(Util.div(adjustedLevel(mob),2.0));
					if(midLevel<target.envStats().level())
						damage-=(int)Math.round(Util.div(damage,2.0));

					if((msg.value()>0)||(msg2.value()>0))
						damage = (int)Math.round(Util.div(damage,2.0));

					if(damage<=0) damage=1;
					if((target.location()==mob.location())
					&&(target.charStats().getBodyPart(Race.BODY_LEG)>0))
					{
						maliciousAffect(mob,target,2,-1);
						MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"The gas <DAMAGE> <T-NAME>. <T-NAME> collapse(s)!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to evoke a green cloud, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}