package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_SummonSteed extends Spell
{
	public String ID() { return "Spell_SummonSteed"; }
	public String name(){return "Summon Steed";}
	public String displayText(){return "(Summon Steed)";}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_SummonSteed();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked)&&(mob!=null))
			mob.destroy();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.location()==null)
				||(mob.amDead())
				||(invoker==null)
				||(invoker.location()==null)
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delAffect(this);
					mob.destroy();
				}
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
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> call(s) for a loyal steed.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, mob.envStats().level());
				ExternalPlay.follow(target,mob,true);
				invoker=mob;
				target.addNonUninvokableAffect((Ability)copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for a loyal steed, but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		
		MOB newMOB=(MOB)CMClass.getMOB("GenRideable");
		Rideable ride=(Rideable)newMOB;
		newMOB.baseEnvStats().setAbility(11);
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setWeight(500);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		if(level<4)
		{
			newMOB.setName("a pony");
			newMOB.setDisplayText("a very pretty pony stands here");
			newMOB.setDescription("She looks loyal, and oh so pretty.");
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'F');
			ride.setMobCapacity(1);
		}
		else
		if(level<10)
		{
			newMOB.setName("a pack horse");
			newMOB.setDisplayText("a sturdy pack horse stands here");
			newMOB.setDescription("A strong and loyal beast, who looks like he`s seen his share of work.");
			ride.setMobCapacity(2);
		}
		else
		if(level<18)
		{
			newMOB.setName("a riding horse");
			newMOB.setDisplayText("a loyal riding horse stands here");
			newMOB.setDescription("A proud and noble companion; brown hair with a long black mane.");
			ride.setMobCapacity(2);
		}
		else
		{
			newMOB.setName("a warhorse");
			newMOB.setDisplayText("a mighty warhorse stands here");
			newMOB.setDescription("Ferocious, fleet of foot, and strong, a best of breed!");
			ride.setMobCapacity(3);
		}
		newMOB.setAlignment(500);
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
