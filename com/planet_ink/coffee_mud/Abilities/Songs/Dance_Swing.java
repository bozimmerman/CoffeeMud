package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Swing extends Dance
{
	public String ID() { return "Dance_Swing"; }
	public String name(){ return "Swing";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Swing();}
	private boolean doneThisRound=false;
	protected String danceOf(){return name()+" Dancing";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect)) return false;
		
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		   &&(!doneThisRound)
		   &&(mob.rangeToTarget()==0))
		{
			if((affect.tool()!=null)&&(affect.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)affect.tool();
				if((attackerWeapon!=null)
				&&(attackerWeapon instanceof Weapon)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> parr(ys) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-70,false))
					&&(mob.location().okAffect(mob,msg)))
					{
						doneThisRound=true;
						mob.location().send(mob,msg);
						return false;
					}
				}
			}
		}
		return true;
	}

}