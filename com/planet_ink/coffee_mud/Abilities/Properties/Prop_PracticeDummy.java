package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_PracticeDummy extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_PracticeDummy"; }
	public String name(){ return "Practice Dummy";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_PracticeDummy();}

	public String accountForYourself()
	{ return "Undefeatable";	}

	public void affectCharState(MOB mob, CharState affectableMaxState)
	{
		super.affectCharState(mob,affectableMaxState);
		if(text().toUpperCase().indexOf("KILL")<0)
		{
			affectableMaxState.setHitPoints(99999);
			mob.curState().setHitPoints(99999);
		}
	}

	public void affectEnvStats(Environmental E, EnvStats affectableStats)
	{
		super.affectEnvStats(E,affectableStats);
		if(text().toUpperCase().indexOf("KILL")<0)
			affectableStats.setArmor(100);
	}
	

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect)) return false;
		if((Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)
			&&(affected!=null)
			&&(affected instanceof MOB)
			&&(affect.amISource((MOB)affected))))
		{
			((MOB)affected).makePeace();
			Room room=((MOB)affected).location();
			if(room!=null)
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB mob=room.fetchInhabitant(i);
				if((mob.getVictim()!=null)&&(mob.getVictim()==affected))
					mob.makePeace();
			}
			return false;
		}
		return true;
	}
}
