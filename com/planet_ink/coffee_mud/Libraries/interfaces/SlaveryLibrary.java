package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.CommonMsgs;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2018 Bo Zimmerman

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
public interface SlaveryLibrary extends CMLibrary
{
	public enum Step
	{
		EVAL,
		INT1,
		INT2,
		INT3,
		INT4,
		INT5,
		ALLDONE;//==-99
		
		public Step nextStep()
		{
			switch(this)
			{
			case EVAL:
				return INT1;
			case INT1:
				return INT2;
			case INT2:
				return INT3;
			case INT3:
				return INT4;
			case INT4:
				return INT5;
			case INT5:
				return ALLDONE;
			default:
				return this;
			}
		}
	}

	public List<Map<String,String>> findMatch(MOB mob, List<String> prereq);
	public String cleanWord(String s);
	public GeasSteps processRequest(MOB you, MOB me, String req);

	public static interface GeasStep
	{
		public boolean botherIfAble(String msgOrQ);
		
		public boolean sayResponse(MOB speaker, MOB target, String response);
		
		public String step();
		
		public void setStep(Step step);
		
		public MOB getBotheredMob();
	}
	
	public static interface GeasSteps extends List<GeasStep>
	{
		public void step();
		
		public void move(int moveCode);
		
		public boolean sayResponse(MOB speaker, MOB target, String response);
		
		public MOB stepperM();
		
		public Set<MOB> getBotheredMobs();
		
		public boolean isDone();
	}
}
