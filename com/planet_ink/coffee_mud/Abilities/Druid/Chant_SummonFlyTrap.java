package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonFlyTrap extends Chant
{
	public String ID() { return "Chant_SummonFlyTrap"; }
	public String name(){ return "Summon FlyTrap";}
	public String displayText(){return "(Summon FlyTrap)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonFlyTrap();}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(((MOB)affected).location()!=null))
			{
				MOB mob=(MOB)affected;
				Room R=mob.location();
				for(int r=0;r<R.numItems();r++)
				{
					Item I=R.fetchItem(r);
					if((I!=null)
					&&(I instanceof DeadBody)
					&&(((DeadBody)I).charStats()!=null)
					&&(((DeadBody)I).charStats().getMyRace()!=null))
					{
						String raceCat=((DeadBody)I).charStats().getMyRace().racialCategory();
						if(raceCat.equals("Insect")||raceCat.equals("Arachnid"))
						{
							if(R.show(mob,I,Affect.MSG_HANDS|Affect.MASK_SOUND,"<S-NAME> devour(s) <T-NAMESELF>."))
							{
								I.destroy();
								break;
							}
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> shrivels up and dies.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the fertile ground.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob);
				beneficialAffect(mob,target,0);
				ExternalPlay.follow(target,mob,true);
				if(target.amFollowing()!=mob)
					mob.tell(target.name()+" seems unwilling to follow you.");
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster)
	{
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		newMOB.setName("a large flytrap");
		newMOB.setDisplayText(newMOB.Name()+" is planted here");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		Behavior B=CMClass.getBehavior("Aggressive");
		B.setParms("mobkiller -RACE +Insect +Arachnid");
		newMOB.addBehavior(B);
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseEnvStats().setSpeed(newMOB.baseCharStats().getCurrentClass().getLevelSpeed(newMOB));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setBitmap(MOB.ATT_AUTOASSIST);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.location().show(newMOB,null,Affect.MSG_OK_ACTION,"<S-NAME> grow(s) from the ground.");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}