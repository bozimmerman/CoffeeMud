package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MagicMissile extends Spell
{
	public Spell_MagicMissile()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Magic Missile";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Magic Missile spell)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;
		maxRange=1;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MagicMissile();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.SPELL_EVOCATION;
	}

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
			int numMissiles=((int)Math.round(Math.floor(Util.div(mob.envStats().level(),5)))+1);
			for(int i=0;i<numMissiles;i++)
			{
				FullMsg msg=new FullMsg(mob,target,this,affectType,(i==0)?(auto?"A magic missle appears hurling full speed at <T-NAME>!":"<S-NAME> point(s) at <T-NAMESELF>, shooting forth a magic missile!"):null);
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						int damage = 0;
						damage += Dice.roll(1,10,1);
						if(target.location()==mob.location())
						{
							target.location().show(target,null,Affect.MSG_OK_ACTION,((i==0)?"The missile ":"Another missile ")+ExternalPlay.standardHitWord(-1,damage)+" <S-NAME>!");
							ExternalPlay.postDamage(mob,target,this,damage);
						}
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
