package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_PoisonousVine extends Chant
{
	public String ID() { return "Chant_PoisonousVine"; }
	public String name(){ return "Poisonous Vine";}
	public String displayText(){return "(Poisonous Vine)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_PoisonousVine();}

	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setAbility(13);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		String name="a poisonous vine";
		newMOB.setName(name);
		newMOB.setDisplayText(name+" looks enraged!");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProfficiency(100);
		newMOB.addAbility(A);
		A=null;
		int classlevel=CMAble.qualifyingClassLevel(caster,this)-CMAble.qualifyingLevel(caster,this);
		switch(classlevel/5)
		{
		case 0:	A=CMClass.getAbility("Poison_BeeSting"); break;
		case 1:	A=CMClass.getAbility("Poison_Bloodboil"); break;
		case 2:	A=CMClass.getAbility("Poison_Venom"); break;
		default: 	A=CMClass.getAbility("Poison_Decreptifier"); break;
		}
		if(A!=null)
		{
			A.setProfficiency(100);
			newMOB.addAbility(A);
		}
		newMOB.addBehavior(CMClass.getBehavior("CombatAbilities"));
		newMOB.setVictim(victim);
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
		//if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
		newMOB.location().showOthers(newMOB,null,Affect.MSG_OK_ACTION,"<S-NAME> start(s) attacking "+victim.name()+"!");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
