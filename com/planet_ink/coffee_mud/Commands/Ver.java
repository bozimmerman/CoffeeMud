package com.planet_ink.coffee_mud.Commands;
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
import java.util.concurrent.TimeUnit;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Ver extends StdCommand
{
	public Ver()
	{
	}

	private final String[] access=I(new String[]{"VERSION","VER"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		mob.tell(L("CoffeeMud v@x1",CMProps.getVar(CMProps.Str.MUDVER)));
		mob.tell(L("(C) 2000-2020 Bo Zimmerman"));
		mob.tell(L("^<A HREF=\"mailto:bo@zimmers.net\"^>bo@zimmers.net^</A^>"));
		mob.tell(L("^<A HREF=\"http://www.coffeemud.org\"^>http://www.coffeemud.org^</A^>"));
		mob.tell(L("System up time: @x1",CMLib.time().date2EllapsedTime(CMLib.host().getUptimeSecs()*1000, TimeUnit.SECONDS, false)));
		final Command C=CMClass.getCommand("Shutdown");
		try
		{
			final Object o = C.executeInternal(mob, 0, new Object[0]);
			if((o instanceof String)
			&&(((String)o).length()>0))
				mob.tell((String)o);
		}
		catch(final Exception e)
		{
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
