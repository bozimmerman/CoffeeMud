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
public class Statcreationspeed extends StdTest
{
	@Override
	public String ID()
	{
		return "Statcreationspeed";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		int times=CMath.s_int(CMParms.combine(commands,2));
		if(times<=0)
			times=9999999;
		mob.tell(L("times=@x1",""+times));
		Object newStats=null;
		long time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			newStats=mob.basePhyStats().copyOf();
		mob.tell(L("PhyStats CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
		time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			mob.basePhyStats().copyInto((PhyStats)newStats);
		mob.tell(L("PhyStats CopyInto took :@x1",""+(System.currentTimeMillis()-time)));

		time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			newStats=mob.baseCharStats().copyOf();
		mob.tell(L("CharStats CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
		time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			mob.baseCharStats().copyInto((CharStats)newStats);
		mob.tell(L("CharStats CopyInto took :@x1",""+(System.currentTimeMillis()-time)));

		time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			newStats=mob.maxState().copyOf();
		mob.tell(L("CharState CopyOf took :@x1",""+(System.currentTimeMillis()-time)));
		time=System.currentTimeMillis();
		for(int i=0;i<times;i++)
			mob.maxState().copyInto((CharState)newStats);
		mob.tell(L("CharState CopyInto took :@x1",""+(System.currentTimeMillis()-time)));
		return null;
	}
}
