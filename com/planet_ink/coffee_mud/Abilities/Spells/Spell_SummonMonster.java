package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonMonster extends Spell
{
	public String ID() { return "Spell_SummonMonster"; }
	public String name(){return "Monster Summoning";}
	public String displayText(){return "(Monster Summoning)";}
	public int quality(){return BENEFICIAL_SELF;};
	public Environmental newInstance(){	return new Spell_SummonMonster();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked)&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}
	
	public void affect(Affect msg)
	{
		super.affect(msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==Affect.MSG_QUIT))
			unInvoke();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) help from the Java Plain.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(target,mob,true);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for magical help, but chokes on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		String mobID = "Dog";

		if (level >= 5)
		{
			mobID = "Orc";
		}

		if (level >= 10)
		{
			mobID = "Tiger";
		}

		if (level >= 15)
		{
			mobID = "Troll";
		}


		MOB newMOB=(MOB)CMClass.getMOB(mobID);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
