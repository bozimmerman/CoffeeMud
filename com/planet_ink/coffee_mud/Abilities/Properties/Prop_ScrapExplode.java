package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Prop_ScrapExplode extends Property {

	public String ID() { return "Prop_ScrapExplode"; }
	public String name() { return "Scrap Explode"; }
	protected int canAffectCode() { return Ability.CAN_ITEMS; }

	public void executeMsg(Environmental myHost, CMMsg affect)
	{
	    super.executeMsg(myHost, affect);
	    if((affect.target()!=null)&&(affect.target().equals(affected))
	       &&(affect.tool()!=null)&&(affect.tool().ID().equals("Scrapping")))
		{
			Item item=(Item)affect.target();
			MOB mob = (MOB)affect.source();
			Room room = mob.location();
			int damage = 3 * item.envStats().weight();
			if (mob != null)
			{
				MUDFight.postDamage(mob, mob, item, damage*2,  CMMsg.MSG_OK_VISUAL, Weapon.TYPE_PIERCING,
				        "Scrapping " + item.Name() + " causes an explosion which <DAMAGE> <T-NAME>!!!");
				HashSet theBadGuys=mob.getGroupMembers(new HashSet());
				for(Iterator e=theBadGuys.iterator();e.hasNext();)
				{
					MOB inhab=(MOB)e.next();
					if (mob != inhab)
						MUDFight.postDamage(inhab, inhab, item, damage, CMMsg.MSG_OK_VISUAL, Weapon.TYPE_PIERCING,
						        "Fragments from " + item.Name() + " <DAMAGE> <T-NAME>!");
				}
			}
			item.destroy();
			room.recoverRoomStats();
	    }
	}
}
