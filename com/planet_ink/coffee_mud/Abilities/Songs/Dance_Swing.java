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
		if(tickID==Host.TICK_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(msg.amITarget(mob)
		   &&(Sense.aliveAwakeMobile(mob,true))
		   &&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		   &&(!doneThisRound)
		   &&(mob.rangeToTarget()==0))
		{
			if((msg.tool()!=null)&&(msg.tool() instanceof Item))
			{
				Item attackerWeapon=(Item)msg.tool();
				if((attackerWeapon!=null)
				&&(attackerWeapon instanceof Weapon)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_FLAILED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_NATURAL)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_RANGED)
				&&(((Weapon)attackerWeapon).weaponClassification()!=Weapon.CLASS_THROWN))
				{
					FullMsg msg2=new FullMsg(mob,msg.source(),null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> parr(ys) "+attackerWeapon.name()+" attack from <T-NAME>!");
					if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-70,false))
					&&(mob.location().okMessage(mob,msg2)))
					{
						doneThisRound=true;
						mob.location().send(mob,msg2);
						return false;
					}
				}
			}
		}
		return true;
	}

}