package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Doppleganger extends Property
{
	public String ID() { return "Prop_Doppleganger"; }
	public String name(){ return "Doppleganger";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_Doppleganger();}
	private boolean lastLevelChangers=true;

	public String accountForYourself()
	{ return "Level Changer";	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected instanceof MOB)
		&&(lastLevelChangers))
		{
			lastLevelChangers=false;
			MOB mob=(MOB)affected;

			if((mob.location()!=null)
			&&(Sense.aliveAwakeMobile(mob,true))
			&&(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
			&&(mob.location().numInhabitants()>1))
			{
				int total=0;
				int num=0;
				MOB victim=mob.getVictim();
				if(victim!=null)
				{
					total+=victim.envStats().level();
					num++;
				}
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if((M!=mob)
					&&((M.getVictim()==mob)||(victim==null))
					&&(M.fetchAffect(ID())==null))
					{
						total+=M.envStats().level();
						num++;
					}
				}
				if(num>0)
				{
					int level=(total/num)+Util.s_int(text());
					if(level<1) level=1;
					if(level!=mob.baseEnvStats().level())
					{
						CharClass C=mob.charStats().getCurrentClass();
						mob.baseEnvStats().setLevel(level);
						mob.baseEnvStats().setArmor(C.getLevelArmor(mob));
						mob.baseEnvStats().setAttackAdjustment(C.getLevelAttack(mob));
						mob.baseEnvStats().setDamage(C.getLevelDamage(mob));
						mob.baseEnvStats().setSpeed(1.0+(Util.div(level,100)*4.0));
						mob.baseState().setHitPoints(Dice.rollHP(level,11));
						mob.baseState().setMana(C.getLevelMana(mob));
						mob.baseState().setMovement(C.getLevelMove(mob));
						mob.recoverEnvStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
					}
				}
			}
		}
		return super.okAffect(myHost,affect);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking==affected)
		&&(tickID==Host.MOB_TICK)
		&&(affected instanceof MOB))
			lastLevelChangers=true;
		return super.tick(ticking,tickID);
	}
}
