package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class Compare extends StdCommand
{
	public Compare(){}

	private String[] access={getScr("Compare","cmd"),getScr("Compare","cmd1")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("Compare","compwhat"));
			return false;
		}
		commands.removeElementAt(0);
		Item compareThis=mob.fetchInventory(null,(String)commands.elementAt(0));
		if((compareThis==null)||((compareThis!=null)&&(!CMLib.flags().canBeSeenBy(compareThis,mob))))
		{
            Vector V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
            if(V.size()>0) 
			{
                for(int i=0;i<V.size();i++) 
				{
                    Environmental shopkeeper=(Environmental)V.elementAt(i);
                    ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(shopkeeper);
                    Environmental itemToDo=SK.getShop().getStock((String)commands.elementAt(0),mob,SK.whatIsSold(),CMLib.utensils().roomStart(shopkeeper));
                    if((itemToDo==null)||(!(itemToDo instanceof Item))) 
					{
                        continue; // next shopkeeper
                    }
                    compareThis=(Item)itemToDo;
                }
                if((compareThis==null)||((compareThis!=null)&&(!CMLib.flags().canBeSeenBy(compareThis,mob)))) 
				{
                    mob.tell(getScr("Compare","donthave",( (String) commands.elementAt(0))));
                    return false;
                }
            }
            else 
			{
                mob.tell(getScr("Compare","donthave", ( (String) commands.elementAt(0)) ));
                return false;
            }
		}
		long compareThisCode = compareThis.rawProperLocationBitmap();
		if(CMath.bset(compareThisCode,Item.HELD)
		&&(!CMath.bset(compareThisCode,Item.WIELD))
		&&(compareThisCode!=Item.HELD))
			compareThisCode=CMath.unsetb(compareThisCode,Item.HELD);
		Item toThis=null;
		if(commands.size()==1)
		{
			Item possible=null;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)
				&&(I!=compareThis)
				&&(I.rawLogicalAnd()==compareThis.rawLogicalAnd()))
				{
					long compareThatCode = I.rawProperLocationBitmap();
					if(CMath.bset(compareThatCode,Item.HELD)
					&&(!CMath.bset(compareThatCode,Item.WIELD))
					&&(compareThatCode!=Item.HELD))
						compareThatCode=CMath.unsetb(compareThatCode,Item.HELD);
					if(compareThisCode==compareThatCode)
					{
						if(!I.amWearingAt(Item.INVENTORY))
						{ toThis=I; break;}
						if(possible==null) possible=I;
					}
				}
			}
			if(toThis==null) toThis=possible;
			if((toThis==null)||((toThis!=null)&&(!CMLib.flags().canBeSeenBy(toThis,mob))))
			{
				mob.tell(getScr("Compare","compwhath",compareThis.name()));
				return false;
			}
		}
		else
			toThis=mob.fetchInventory(null,CMParms.combine(commands,1));
		if((toThis==null)||((toThis!=null)&&(!CMLib.flags().canBeSeenBy(toThis,mob))))
		{
			mob.tell(getScr("Compare","donthave",((String)commands.elementAt(1))));
			return false;
		}

		if((compareThis instanceof Weapon)&&(toThis instanceof Weapon))
		{
			int cDmg=compareThis.baseEnvStats().damage();
			int tDmg=toThis.baseEnvStats().damage();
			cDmg+=(int)Math.round(CMath.div(compareThis.baseEnvStats().attackAdjustment()-toThis.baseEnvStats().attackAdjustment(),100.0)*cDmg);

			if(cDmg==tDmg)
				mob.tell(getScr("Compare","lokksame",compareThis.name(),toThis.name()));
			else
			if(cDmg>tDmg)
				mob.tell(getScr("Compare","better",compareThis.name(),toThis.name()));
			else
				mob.tell(getScr("Compare","worse",compareThis.name(),toThis.name()));
		}
		else
		if((compareThis instanceof Armor)&&(toThis instanceof Armor))
		{
			if(!compareThis.compareProperLocations(toThis))
			{
				mob.tell(getScr("Compare","notworn",compareThis.name(),toThis.name()));
				return false;
			}
			if(compareThis.baseEnvStats().armor()==toThis.baseEnvStats().armor())
				mob.tell(getScr("Compare","lokksame",compareThis.name(),toThis.name()));
			else
			if(compareThis.baseEnvStats().armor()>toThis.baseEnvStats().armor())
				mob.tell(getScr("Compare","better",compareThis.name(),toThis.name()));
			else
				mob.tell(getScr("Compare","worse",compareThis.name(),toThis.name()));

		}
		else
			mob.tell(getScr("Compare","cannotc",compareThis.name(),toThis.name()));
		return false;
	}
	public double actionsCost(){return 1.0;}
	public boolean canBeOrdered(){return true;}

	
}
