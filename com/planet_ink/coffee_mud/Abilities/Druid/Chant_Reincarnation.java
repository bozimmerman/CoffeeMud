package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_Reincarnation extends Chant
{
	public String ID() { return "Chant_Reincarnation"; }
	public String name(){ return "Reincarnation";}
	public String displayText(){return "(Reincarnation Geis)";}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Chant_Reincarnation();}
	public boolean canBeUninvoked(){return false;}
	protected int overrideMana(){return 200;}

	Race newRace=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(newRace!=null)
		{
			if(affected.displayName().indexOf(" ")>0)
				affectableStats.setName("a "+newRace.name()+" called "+affected.displayName());
			else
				affectableStats.setName(affected.displayName()+" the "+newRace.name());
			newRace.setHeightWeight(affectableStats,'M');
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
			affectableStats.setMyRace(newRace);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(tickDown!=Integer.MAX_VALUE)
		&&((--tickDown)<0))
		{
			tickDown=-1;

			// undo the affects of this spell
			if((affected==null)||(!(affected instanceof MOB)))
				return super.tick(ticking,tickID);
			MOB mob=(MOB)affected;
			mob.tell("Your reincarnation geis is lifted as your form solidifies.");
			if(newRace!=null)
				mob.baseCharStats().setMyRace(newRace);
			mob.delAffect(this);
			if(mob.location()!=null)
				mob.location().recoverRoomStats();
			else
			{
				mob.recoverEnvStats();
				mob.recoverCharStats();
				mob.recoverMaxState();
			}
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg))
			return false;
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;
		if((msg.sourceMinor()==Affect.TYP_DEATH)
		   &&(msg.amISource(mob)))
		{
			newRace=null;
			while((newRace==null)||(newRace.ID().equals("StdRace")))
				newRace=CMClass.randomRace();
			if(newRace!=null)
				mob.tell("You are being reincarnated as a "+newRace.name()+"!!");
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.isMonster())
		{
			mob.tell("Your chant would have no effect on such a creature.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;



		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			int modifier=0;
			if(target!=mob) modifier=Affect.MASK_MALICIOUS;
			FullMsg msg=new FullMsg(mob,target,this,modifier|affectType(auto),(auto?"^S<S-NAME> get(s) put under a reincarnation geis!^?":"^S<S-NAME> chant(s) a reincarnation geis upon <T-NAMESELF>.^?"));
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,1800);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a reincarnation geis, but nothing happens.");

		return success;
	}
}