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

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Config extends StdCommand
{
	public Config(){}

	private final String[] access=I(new String[]{"CONFIG","AUTO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String postStr="";
		if((commands!=null)&&(commands.size()>1))
		{
			final String name=commands.get(1);
			MOB.Attrib finalA=null;
			for(MOB.Attrib a : MOB.Attrib.values())
			{
				if(name.equalsIgnoreCase(a.getName()))
					finalA=a;
			}
			if(finalA==null)
			{
				if(name.equalsIgnoreCase("LINEWRAP"))
				{
					final String newWrap=(commands.size()>2)?CMParms.combine(commands,2):"";
					int newVal=mob.playerStats().getWrap();
					if((CMath.isInteger(newWrap))&&(CMath.s_int(newWrap)>10))
						newVal=CMath.s_int(newWrap);
					else
					if("DISABLED".startsWith(newWrap.toUpperCase())&&(newWrap.length()>0))
						newVal=0;
					else
					{
						mob.tell(L("'@x1' is not a valid linewrap setting. Enter a number larger than 10 or 'disable'.",newWrap));
						return false;
					}
					mob.playerStats().setWrap(newVal);
					postStr=L("Configuration option change: LINEWRAP");
				}
				else
				if(name.equalsIgnoreCase("PAGEBREAK"))
				{
					final String newBreak=(commands.size()>2)?CMParms.combine(commands,2):"";
					int newVal=mob.playerStats().getWrap();
					if((CMath.isInteger(newBreak))&&(CMath.s_int(newBreak)>0))
						newVal=CMath.s_int(newBreak);
					else
					if("DISABLED".startsWith(newBreak.toUpperCase())&&(newBreak.length()>0))
						newVal=0;
					else
					{
						mob.tell(L("'@x1' is not a valid pagebreak setting. Enter a number larger than 0 or 'disable'.",newBreak));
						return false;
					}
					mob.playerStats().setPageBreak(newVal);
					postStr=L("Configuration option change: PAGEBREAK");
				}
				else
					postStr=L("Unknown configuration flag '@x1'.",name);
			}
			else
			{
				postStr=L("Configuration flag toggled: "+finalA.getName());
				mob.setAttribute(finalA, !mob.isAttributeSet(finalA));
			}
			mob.tell(postStr);
		}
		
		final StringBuffer msg=new StringBuffer(L("^HYour configuration flags:^?\n\r"));
		for(MOB.Attrib a : MOB.Attrib.values())
		{
			if((a==MOB.Attrib.SYSOPMSGS)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SYSMSGS))))
				continue;
			if((a==MOB.Attrib.AUTOMAP)&&(CMProps.getIntVar(CMProps.Int.AWARERANGE)<=0))
				continue;

			msg.append(CMStrings.padRight(a.getName(),15)+": ");
			boolean set=mob.isAttributeSet(a);
			if(a.isAutoReversed()) 
				set=!set;
			msg.append(set?L("ON"):L("OFF"));
			msg.append("\n\r");
		}
		if(mob.playerStats()!=null)
		{
			final String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
			msg.append(CMStrings.padRight(L("LINEWRAP"),15)+": "+wrap);
			msg.append("\n\r");
			final String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"Disabled";
			msg.append(CMStrings.padRight(L("PAGEBREAK"),15)+": "+pageBreak);
			msg.append("\n\r");
		}
		mob.tell(msg.toString());
		mob.tell(postStr);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
