package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoPurge extends Property
{
	public String ID() { return "Prop_NoPurge"; }
	public String name(){ return "Prevents automatic purging";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prop_NoPurge();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected!=null)
		{
			if(affected instanceof Room)
			{
				Room R=(Room)affected;
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if(I!=null) I.setDispossessionTime(0);
				}
			}
			else
			if(affected instanceof Container)
			{
				if(((Container)affected).owner() instanceof Room)
				{
					((Container)affected).setDispossessionTime(0);
					Vector V=((Container)affected).getContents();
					for(int v=0;v<V.size();v++)
						((Item)V.elementAt(v)).setDispossessionTime(0);
				}
			}
			else
			if(affected instanceof Item)
				((Item)affected).setDispossessionTime(0);
		}
	}
	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if(affected!=null)
		{
			if(affected instanceof Room)
			{
				if((msg.targetMinor()==Affect.TYP_DROP)
				&&(msg.target()!=null)
				&&(msg.target() instanceof Item))
					((Item)msg.target()).setDispossessionTime(0);
			}
			else
			if(affected instanceof Container)
			{
				if((msg.targetMinor()==Affect.TYP_PUT)
				&&(msg.target()!=null)
				&&(msg.target()==affected)
				&&(msg.target() instanceof Item)
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					((Item)msg.target()).setDispossessionTime(0);
					((Item)msg.tool()).setDispossessionTime(0);
				}
			}
			else
			if(affected instanceof Item)
			{
				if((msg.targetMinor()==Affect.TYP_DROP)
				&&(msg.target()!=null)
				&&(msg.target() instanceof Item)
				&&(msg.target()==affected))
					((Item)msg.target()).setDispossessionTime(0);
			}
		}
	}
}