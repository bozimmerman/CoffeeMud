package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SummonEarthElemental extends Prayer
{
	public String ID() { return "Prayer_SummonEarthElemental"; }
	public String name(){return "Summon Earth Elemental";}
	public String displayText(){return "(Demon Gate)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	public Environmental newInstance(){	return new Prayer_SummonEarthElemental();}

	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(mob.amFollowing()!=invoker())
					unInvoke();
			}
		}
		return super.tick(tickID);
	}

	public void affect(Affect msg)
	{
		super.affect(msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==Affect.TYP_QUIT))
			unInvoke();
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for elemental assistance.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB myMonster = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(myMonster,mob,true);
				invoker=mob;
				beneficialAffect(mob,myMonster,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for elemental assistance, but is not answered.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(13);
		newMOB.baseEnvStats().setLevel(level/2);
		newMOB.setAlignment(500);
		newMOB.baseEnvStats().setWeight(850);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(caster.envStats().damage()/2);
		newMOB.baseEnvStats().setAttackAdjustment(caster.envStats().attackAdjustment()/2);
		newMOB.baseEnvStats().setArmor(caster.envStats().armor()/2);
		newMOB.baseEnvStats().setSpeed(1);
		newMOB.baseCharStats().setStat(CharStats.STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.CONSTITUTION,25);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("EarthElemental"));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.setName("a hideous rock beast");
		newMOB.setDisplayText("a hideous rock beast is stomping around here");
		newMOB.setDescription("This enormous hunk of rock is roughly the shape of a humanoid.");
		ride.setMobCapacity(2);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);


	}
}
