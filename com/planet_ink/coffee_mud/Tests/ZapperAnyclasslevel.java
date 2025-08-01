package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class ZapperAnyclasslevel extends PropTest
{
	@Override
	public String ID()
	{
		return "ZapperAnyclasslevel";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "all_masks"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		resetTest();
		final String mask1="-ANYCLASSLEVEL +Gaian +>=30 +Druid +<10";
		final String mask2="+ANYCLASSLEVEL -Gaian ->=30 -Druid -<10";
		final MaskingLibrary.CompiledZMask cmask1 = CMLib.masking().maskCompile(mask1);
		//mob.tell(("Test:@x2-1: @x1",CMLib.masking().maskDesc(mask1),what));
		if (!CMLib.masking().maskCheck(mask1, mobs[0], true))
		{
			return (("Error#1"));
		}
		if (!CMLib.masking().maskCheck(cmask1, mobs[0], true))
		{
			return (("Error#2"));
		}
		if (CMLib.masking().maskCheck(mask1, mobs[1], true))
		{
			return (("Error#3"));
		}
		if (CMLib.masking().maskCheck(cmask1, mobs[1], true))
		{
			return (("Error#4"));
		}
		final MaskingLibrary.CompiledZMask cmask2 = CMLib.masking().maskCompile(mask2);
		//mob.tell(("Test:@x2-2: @x1", CMLib.masking().maskDesc(mask2),what));
		if (CMLib.masking().maskCheck(mask2, mobs[0], true))
		{
			return (("Error#5"));
		}
		if (CMLib.masking().maskCheck(cmask2, mobs[0], true))
		{
			return (("Error#6"));
		}
		if (!CMLib.masking().maskCheck(mask2, mobs[1], true))
		{
			return (("Error#7"));
		}
		if (!CMLib.masking().maskCheck(cmask2, mobs[1], true))
		{
			return (("Error#8"));
		}
		return null;
	}
}
