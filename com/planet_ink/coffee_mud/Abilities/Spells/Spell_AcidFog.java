package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_AcidFog extends Spell
{
	public String ID() { return "Spell_AcidFog"; }
	public String name(){ return "Acid Fog";}
	public String displayText(){ return "(Acid Fog)";}
	public int quality(){ return MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int minRange(){return 2;}
	public int maxRange(){return 5;}
	Room castingLocation=null;
	public Environmental newInstance(){	return new Spell_AcidFog();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean tick(int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB vic=(MOB)affected;
			if(vic.location()!=castingLocation)
				unInvoke();
			else
			if((!vic.amDead())&&(vic.location()!=null))
			{
				int damage=vic.envStats().level();
				ExternalPlay.postDamage(invoker,vic,this,Dice.roll(1,damage,0),Affect.TYP_ACID,-1,"<T-NAME> sizzle(s) in the acid fog!");
			}
		}
		return super.tick(tickID);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
		{
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the acid fog!");
		}
	}
		

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth melting.");
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
			mob.location().show(mob,null,affectType(auto),auto?"A horrendous cloud of acid appears!":"^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_ACID|(auto?Affect.ACT_GENERAL:0),null);
				if((mob.location().okAffect(msg))
				   &&(mob.location().okAffect(msg2))
				   &&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((!msg.wasModified())&&(!msg2.wasModified())&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,(mob.envStats().level()*10),-1);
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) enveloped in the acid fog!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}