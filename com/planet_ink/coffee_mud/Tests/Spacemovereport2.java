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
Copyright 2024-2024 Bo Zimmerman

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
public class Spacemovereport2 extends StdTest
{
	@Override
	public String ID()
	{
		return "Spacemovereport2";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		for(double dir0 = 0; dir0 <=Math.PI*2; dir0 += (Math.PI/12.0))
		{
			if(dir0 == 0)
				dir0 = 0.0001;
			if(dir0 == (2*Math.PI))
				dir0 = (2*Math.PI)-0.0001;
			for(double dir1 = 0; dir1 <(Math.PI+(Math.PI/12)); dir1 += (Math.PI/12.0))
			{
				if(dir1 == 0)
					dir1 = 0.0001;
				if(dir1 == (Math.PI))
					dir1 = (Math.PI)-0.0001;
				for(double adir0 = 0; adir0 <=Math.PI*2; adir0 += (Math.PI/12.0))
				{
					if(adir0 == 0)
						adir0 = 0.0001;
					if(adir0 == (2*Math.PI))
						adir0 = (2*Math.PI)-0.0001;
					for(double adir1 = 0; adir1 <(Math.PI+(Math.PI/12)); adir1 += (Math.PI/12.0))
					{
						if(adir1 == 0)
							adir1 = 0.0001;
						if(adir1 == (Math.PI))
							adir1 = (Math.PI)-0.0001;
						if(dir1 > Math.PI)
							dir1=Math.PI;
						if(adir1 > Math.PI)
							adir1=Math.PI;
						final double[] angle1 = new double[] {dir0, dir1};
						final double[] angle2 = new double[] {adir0, adir1};
						final double[] mid = CMLib.space().getMiddleAngle(angle1, angle2);
						mob.tell("Middle angle between "+Math.round(Math.toDegrees(angle1[0]))+"mk"+Math.round(Math.toDegrees(angle1[1]))
						+"   and   "+Math.round(Math.toDegrees(angle2[0]))+"mk"+Math.round(Math.toDegrees(angle2[1]))
						+"       is: "+Math.round(Math.toDegrees(mid[0]))+"mk"+Math.round(Math.toDegrees(mid[1])));
					}
				}
			}
		}
		return null;
	}
}
