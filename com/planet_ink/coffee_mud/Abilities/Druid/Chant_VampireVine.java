package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_VampireVine extends Chant_SummonVine
{
	public String ID() { return "Chant_VampireVine"; }
	public String name(){ return "Vampire Vine";}
	public String displayText(){return "(Vampire Vine)";}
	public Environmental newInstance(){	return new Chant_VampireVine();}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)&&(Util.bset(msg.targetCode(),Affect.MASK_HURT)))
			{
				int amount=msg.targetCode()-Affect.MASK_HURT;
				if(amount>3)
				{
					amount=amount/4;
					((MOB)affected).curState().adjHitPoints(amount,((MOB)affected).maxState());
					if(invoker!=null)
						invoker.curState().adjHitPoints(amount,invoker.maxState());
				}
			}
		}
		
		return true;
	}
	
	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster);
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseEnvStats().setAbility(19);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Vine"));
		String name="a vampire vine";
		newMOB.setName(name);
		newMOB.setDisplayText(name+" looks enraged!");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProfficiency(100);
		newMOB.addAbility(A);
		newMOB.setVictim(victim);
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.baseEnvStats().setDamage(30+(9*(level/5)));
		newMOB.baseEnvStats().setAttackAdjustment(10+(level));
		newMOB.baseEnvStats().setArmor(100-(30+(level/2)));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		//if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
		newMOB.location().showOthers(newMOB,null,Affect.MSG_OK_ACTION,"<S-NAME> start(s) attacking "+victim.displayName()+"!");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
