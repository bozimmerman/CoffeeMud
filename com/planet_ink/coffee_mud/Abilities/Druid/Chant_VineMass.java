package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_VineMass extends Chant_SummonVine
{
	public String ID() { return "Chant_VineMass"; }
	public String name(){ return "Vine Mass";}
	public String displayText(){return "(Vine Mass)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_VineMass();}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=null;
		int limit=(caster.envStats().level()/4);
		for(int i=0;i<limit;i++)
		{
			newMOB=(MOB)CMClass.getMOB("GenMOB");
			int level=adjustedLevel(caster);
			if(level<1) level=1;
			newMOB.baseEnvStats().setLevel(level);
			newMOB.baseEnvStats().setAbility(13);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
			String name="a vine";
			newMOB.setName(name);
			newMOB.setDisplayText(name+" looks enraged!");
			newMOB.setDescription("");
			newMOB.setAlignment(500);
			Ability A=CMClass.getAbility("Fighter_Rescue");
			A.setProfficiency(100);
			newMOB.addAbility(A);
			newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
			newMOB.setLocation(caster.location());
			newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
			newMOB.baseEnvStats().setDamage(6+(5*(level/5)));
			newMOB.baseEnvStats().setAttackAdjustment(10);
			newMOB.baseEnvStats().setArmor(100-(30+(level/2)));
			newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
			newMOB.setMiscText(newMOB.text());
			newMOB.recoverCharStats();
			newMOB.recoverEnvStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.bringToLife(caster.location(),true);
			newMOB.setMoney(0);
			if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
			newMOB.setVictim(victim);
			newMOB.setStartRoom(null);
			if((i+1)<limit)
			{
				beneficialAffect(caster,newMOB,0);
				CommonMsgs.follow(newMOB,caster,true);
				if(newMOB.amFollowing()!=caster)
				{
					A=newMOB.fetchEffect(ID());
					if(A!=null) A.unInvoke();
					return null;
				}
				MUDFight.postAttack(newMOB,victim,newMOB.fetchWieldedItem());
			}
		}
		return(newMOB);
	}
}
