package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Prop_NoPurge extends Property
{
	public String ID() { return "Prop_NoPurge"; }
	public String name(){ return "Prevents automatic purging";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_ITEMS;}

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
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected!=null)
		{
			if(affected instanceof Room)
			{
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(msg.target()!=null)
				&&(msg.target() instanceof Item))
					((Item)msg.target()).setDispossessionTime(0);
			}
			else
			if(affected instanceof Container)
			{
				if((msg.targetMinor()==CMMsg.TYP_PUT)
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
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(msg.target()!=null)
				&&(msg.target() instanceof Item)
				&&(msg.target()==affected))
					((Item)msg.target()).setDispossessionTime(0);
			}
		}
	}
}
