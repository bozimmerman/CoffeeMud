package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonSapling extends Chant
{
	public String ID() { return "Chant_SummonSapling"; }
	public String name(){ return "Summon Sapling";}
	public String displayText(){return "(Summon Sapling)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonSapling();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(!mob.isInCombat())
				||(mob.location()!=invoker.location())))
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected)))
		{
			if(affect.sourceMinor()==Affect.TYP_DEATH)
			{
				unInvoke();
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> grow(s) still and tree-like.");
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==Affect.TYP_QUIT))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell("Only the anger of combat can summon sapling warriors.");
			return false;
		}
		int material=EnvResource.RESOURCE_OAK;
		if((mob.location().myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			for(int v=0;v<V.size();v++)
			{
				if((((Integer)V.elementAt(v)).intValue()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
					V2.addElement(V.elementAt(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(Dice.roll(1,V2.size(),-1))).intValue();
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) and summon(s) help from the trees.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB target = determineMonster(mob, material);
				beneficialAffect(mob,target,0);
				ExternalPlay.follow(target,mob,true);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int material)
	{
		MOB victim=caster.getVictim();
		MOB newMOB=(MOB)CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster)/4;
		if(level<1) level=1;
		newMOB.baseEnvStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("TreeGolem"));
		String resourceName=EnvResource.RESOURCE_DESCS[material&EnvResource.RESOURCE_MASK].toLowerCase();
		String name=resourceName+" sapling";
		name=Util.startWithAorAn(name).toLowerCase();
		newMOB.setName(name);
		newMOB.setDisplayText(name+" looks enraged!");
		newMOB.setDescription("");
		newMOB.setAlignment(500);
		Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProfficiency(100);
		newMOB.addAbility(A);
		newMOB.setVictim(victim);
		newMOB.recoverEnvStats();
		newMOB.recoverCharStats();
		newMOB.baseEnvStats().setArmor(newMOB.baseCharStats().getCurrentClass().getLevelArmor(newMOB));
		newMOB.baseEnvStats().setAttackAdjustment(newMOB.baseCharStats().getCurrentClass().getLevelAttack(newMOB));
		newMOB.baseEnvStats().setDamage(newMOB.baseCharStats().getCurrentClass().getLevelDamage(newMOB));
		newMOB.baseCharStats().setStat(CharStats.GENDER,(int)'N');
		newMOB.baseEnvStats().setSensesMask(newMOB.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		if(victim.getVictim()!=newMOB) victim.setVictim(newMOB);
		newMOB.location().showOthers(newMOB,null,Affect.MSG_OK_ACTION,"<S-NAME> start(s) attacking "+victim.name()+"!");
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}