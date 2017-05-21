package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/**
 * Title: False Realities Flavored CoffeeMUD
 * Description: The False Realities Version of CoffeeMUD
 * Copyright: Copyright (c) 2003 Jeremy Vyska
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Company: http://www.falserealities.com
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Prop_ScrapExplode extends Property {

	@Override
	public String ID()
	{
		return "Prop_ScrapExplode";
	}

	@Override
	public String name()
	{
		return "Scrap Explode";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg affect)
	{
		super.executeMsg(myHost, affect);
		if((affect.target()!=null)&&(affect.target().equals(affected))
		   &&(affect.tool()!=null)&&(affect.tool().ID().equals("Scrapping")))
		{
			final Item item=(Item)affect.target();
			final MOB mob = affect.source();
			if (mob != null)
			{
				final Room room = mob.location();
				final int damage = 3 * item.phyStats().weight();
				CMLib.combat().postDamage(mob, mob, item, damage*2,  CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE, Weapon.TYPE_PIERCING,
						L("Scrapping @x1 causes an explosion which <DAMAGE> <T-NAME>!!!",item.Name()));
				final Set<MOB> theBadGuys=mob.getGroupMembers(new HashSet<MOB>());
				for (final Object element : theBadGuys)
				{
					final MOB inhab=(MOB)element;
					if (mob != inhab)
						CMLib.combat().postDamage(inhab, inhab, item, damage, CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE, Weapon.TYPE_PIERCING,
								L("Fragments from @x1 <DAMAGE> <T-NAME>!",item.Name()));
				}
				room.recoverRoomStats();
			}
			item.destroy();
		}
	}
}
