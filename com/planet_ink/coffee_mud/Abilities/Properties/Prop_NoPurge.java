package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoPurge extends Property
{
	public String ID() { return "Prop_NoPurge"; }
	public String name(){ return "Prevents automatic purging";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Prop_NoPurge();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I!=null) I.setDispossessionTime(0);
			}
		}
	}
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if((msg.targetMinor()==Affect.TYP_DROP)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item))
			((Item)msg.target()).setDispossessionTime(0);
	}
}