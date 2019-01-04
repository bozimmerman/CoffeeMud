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
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2019 Bo Zimmerman

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
	public Config()
	{
	}

	private final String[] access=I(new String[]{"CONFIG","AUTO"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String postStr="";
		if((commands!=null)&&(commands.size()>1))
		{
			final String name=commands.get(1);
			MOB.Attrib finalA=null;
			for(final MOB.Attrib a : MOB.Attrib.values())
			{
				if(name.equalsIgnoreCase(a.getName()))
					finalA=a;
			}
			if(finalA==null)
			{
				if(name.equalsIgnoreCase("TELNETGA"))
					finalA=MOB.Attrib.TELNET_GA;
				else
				if(name.equalsIgnoreCase("TELNET_GA"))
					finalA=MOB.Attrib.TELNET_GA;
				else
				if(name.equalsIgnoreCase("LIST"))
				{
					final StringBuilder str=new StringBuilder("");
					for(final MOB.Attrib A : MOB.Attrib.values())
						str.append(A.getName()).append(", ");
					postStr=L("Options include: @x1.",str.substring(0,str.length()-2));
					mob.tell(postStr);
					return true;
				}
				else
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
				final boolean newSet = !mob.isAttributeSet(finalA);
				switch(finalA)
				{
				case ANSI:
					if(mob.session() != null)
					{
						mob.session().setClientTelnetMode(Session.TELNET_ANSI,newSet);
						mob.session().setServerTelnetMode(Session.TELNET_ANSI,newSet);
					}
					break;
				case AUTOASSIST:
					break;
				case AUTODRAW:
					break;
				case AUTOEXITS:
					break;
				case AUTOFORWARD:
				{
					final PlayerStats pStats = mob.playerStats();
					if((pStats != null)
					&&(pStats.getAccount() != null))
					{
						pStats.getAccount().setFlag(AccountFlag.NOAUTOFORWARD, newSet);
						CMLib.database().DBUpdateAccount(pStats.getAccount());
					}
					break;
				}
				case AUTOGOLD:
					break;
				case AUTOGUARD:
					break;
				case AUTOIMPROVE:
					break;
				case AUTOLOOT:
					break;
				case AUTOMAP:
					break;
				case AUTOMELEE:
					break;
				case AUTONOTIFY:
					break;
				case AUTORUN:
					break;
				case AUTOWEATHER:
					break;
				case BRIEF:
					break;
				case COMPRESS:
					break;
				case DAILYMESSAGE:
					break;
				case MXP:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_MXP,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_MXP,newSet);
					}
					break;
				case NOBATTLESPAM:
					break;
				case NOFOLLOW:
					break;
				case NOTEACH:
					break;
				case PLAYERKILL:
					break;
				case QUIET:
					break;
				case SOUND:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_MSP,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_MSP,newSet);
					}
					break;
				case SYSOPMSGS:
					break;
				case TELNET_GA:
					if(mob.session() != null)
					{
						mob.session().changeTelnetMode(Session.TELNET_GA,newSet);
						mob.session().setClientTelnetMode(Session.TELNET_GA,newSet);
					}
					break;
				default:
					break;
				}
				mob.setAttribute(finalA, newSet);
			}
			mob.tell(postStr);
		}

		final StringBuffer msg=new StringBuffer(L("^HYour configuration flags:^?\n\r"));
		final List<MOB.Attrib> sorted = new XVector<MOB.Attrib>(MOB.Attrib.values());
		Collections.sort(sorted,new Comparator<MOB.Attrib>()
		{
			@Override
			public int compare(final Attrib o1, final Attrib o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});
		for(final MOB.Attrib a : sorted)
		{
			if((a==MOB.Attrib.SYSOPMSGS)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SYSMSGS))))
				continue;
			if((a==MOB.Attrib.AUTOMAP)&&(CMProps.getIntVar(CMProps.Int.AWARERANGE)<=0))
				continue;

			msg.append("^W"+CMStrings.padRight(a.getName(),15)+"^N: ");
			boolean set=mob.isAttributeSet(a);
			if(a.isAutoReversed())
				set=!set;
			msg.append(set?L("^gON"):L("^rOFF"));
			msg.append("\n\r");
		}
		msg.append("^N");
		if(mob.playerStats()!=null)
		{
			final String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
			msg.append("^W"+CMStrings.padRight(L("LINEWRAP"),15)+"^N: ^w"+wrap);
			if((mob.session()!=null)&&(mob.playerStats().getWrap() != mob.session().getWrap()))
				msg.append(" ("+mob.session().getWrap()+")");
			msg.append("^N^.\n\r");
			final String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"^rDisabled";
			msg.append("^w"+CMStrings.padRight(L("PAGEBREAK"),15)+"^N: ^w"+pageBreak);
			msg.append("^N^.\n\r");
		}
		msg.append("^N");
		mob.tell(msg.toString());
		mob.tell(postStr);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
