package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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
public class Song_SingleMindedness extends Song
{
	@Override
	public String ID()
	{
		return "Song_SingleMindedness";
	}

	private final static String localizedName = CMLib.lang().L("Single Mindedness");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	protected CMMsg themsg=null;

	@Override
	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return false;
	}

	@Override
	public void executeMsg(Environmental ticking, CMMsg msg)
	{
		super.executeMsg(ticking,msg);
		if((themsg==null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS)))
			themsg=msg;
	}

	@Override
	public boolean okMessage(Environmental ticking, CMMsg msg)
	{
		if((themsg!=null)
		&&(msg.source()!=invoker())
		&&(msg.source()==affected)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(themsg.sourceMinor()!=msg.sourceMinor()))
		{
			msg.source().tell(msg.source(),null,null,L("The only thing you have a mind to do is '@x1'.",themsg.sourceMessage()));
			return false;
		}
		return super.okMessage(ticking,msg);
	}
}
