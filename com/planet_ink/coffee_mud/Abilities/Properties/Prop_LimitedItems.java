package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_LimitedItems extends Property
{
	public String ID() { return "Prop_LimitedItems"; }
	public String name(){ return "Limited Item";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	public static Hashtable instances=new Hashtable();
	public static boolean[] playersLoaded=new boolean[1];
	private boolean norecurse=false;
	private boolean destroy=false;

	public String accountForYourself()
	{
		if(Util.s_int(text())<=0)
			return "Only 1 may exist";
		else
			return "Only "+Util.s_int(text())+" may exist.";
	}

	public void affectEnvStats(Environmental E, EnvStats affectableStats)
	{
		super.affectEnvStats(E,affectableStats);
		
		if((!(E instanceof Item))||(((Item)E).owner()==null))
			return;
		
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return;
		
		if(norecurse) return;
		norecurse=true;
		
		if(destroy) ((Item)affected).destroy();
		
		synchronized(playersLoaded)
		{
			if(!playersLoaded[0])
			{
				playersLoaded[0]=true;
				Log.sysOut("Prop_LimitedItems","Checking player inventories");
				Vector V=CMClass.DBEngine().getUserList();
				for(int v=0;v<V.size();v++)
				{
					MOB M=CMMap.getLoadPlayer((String)((Vector)V.elementAt(v)).firstElement());
					if((M.location()!=null)&&(M.location().isInhabitant(M)))
						Log.sysOut("Prop_LimitedItems",M.name()+" is in the Game!!!");
				}
				Log.sysOut("Prop_LimitedItems","Done checking player inventories");
			}
		}
		
		Item I=(Item)E;
		if(Sense.isInTheGame(I))
		{
			int max=Util.s_int(text());
			Vector myInstances=null;
			synchronized(instances)
			{
				if(!instances.containsKey(I.Name()))
				{
					myInstances=new Vector();
					instances.put(I.Name(),myInstances);
					myInstances.addElement(I);
				}
			}
			myInstances=(Vector)instances.get(I.Name());
			if(myInstances!=null)
			{
				synchronized(myInstances)
				{
					if(!myInstances.contains(I))
					{
						if(max==0) max++;
						int num=0;
						for(int i=myInstances.size()-1;i>=0;i--)
							if(!((Item)myInstances.elementAt(i)).amDestroyed())
								num++;
							else
								myInstances.removeElementAt(i);
						if(num>=max)
						{
							I.destroy();
							destroy=true;
						}
						else
							myInstances.addElement(I);
					}
				}
			}
		}
		norecurse=false;
	}
}
