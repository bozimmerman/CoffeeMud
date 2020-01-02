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
public class Prop_MOBEmoter extends Property
{
	@Override
	public String ID()
	{
		return "Prop_MOBEmoter";
	}

	protected Behavior emoter=null;
	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		final int x=newText.indexOf(' ');
		if(x>0)
		{
			final String id=newText.substring(0, x).trim();
			emoter = CMClass.getBehavior(id);
			if(emoter!=null)
			{
				final String behaviorArgs = newText.substring(x+1).trim();
				emoter.setParms(behaviorArgs);
				return;
			}
		}
		emoter = CMClass.getBehavior("Emoter");
		if(emoter!=null)
			emoter.setParms(newText);
	}

	@Override
	public String accountForYourself()
	{
		if(emoter != null)
			return emoter.accountForYourself();
		return "";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(emoter!=null)
			emoter.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(emoter!=null)
			return emoter.okMessage(myHost,msg);
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((ticking instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			if(emoter!=null)
			{
				if(!emoter.tick(ticking,tickID))
				{
					if(CMParms.getParmInt(emoter.getParms(),"expires",0)>0)
						((MOB)ticking).delEffect(this);
				}
			}
		}
		return true;
	}
}
