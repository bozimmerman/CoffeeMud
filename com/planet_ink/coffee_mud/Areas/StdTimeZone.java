package com.planet_ink.coffee_mud.Areas;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class StdTimeZone extends StdArea
{
	public String ID(){	return "StdTimeZone";}

	public StdTimeZone()
	{
	    super();

	    myClock = (TimeClock)CMClass.getCommon("DefaultTimeClock");
	}

	public CMObject copyOf()
	{
        CMObject O=super.copyOf();
        if(O instanceof Area) ((Area)O).setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
        return O;
	}

	public TimeClock getTimeObj(){return myClock;}
	public void setName(String newName)
	{
		super.setName(newName);
		myClock.setLoadName(newName);
	}

	public void addChild(Area Adopted) {
		super.addChild(Adopted);
		Adopted.setTimeObj(getTimeObj());
	}
	public void initChildren() {
		super.initChildren();
		if(children!=null)
			for(int i=0;i<children.size();i++)
				((Area)children.elementAt(i)).setTimeObj(getTimeObj());
	}
}
