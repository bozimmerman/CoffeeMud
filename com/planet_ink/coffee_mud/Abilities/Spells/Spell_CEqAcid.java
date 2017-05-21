package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_CEqAcid extends Spell_BaseClanEq {

@Override
public String ID()
{
	return "Spell_CEqAcid";
}

private final static String localizedName = CMLib.lang().L("ClanEnchant Acid");

	@Override
	public String name()
	{
		return localizedName;
	}

@Override
public int abstractQuality()
{
	return Ability.QUALITY_INDIFFERENT;
}

  @Override
public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
  {
	type="Acid";
	// All the work is done by the base model
	if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		return false;
	  return true;
  }
}
