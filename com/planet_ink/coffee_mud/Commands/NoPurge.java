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
public class NoPurge extends StdCommand
{
	public NoPurge()
	{
	}

	private final String[] access=I(new String[]{"NOPURGE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private int checkExisting(final String protectMe)
	{
		final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if((protectedOnes!=null)&&(protectedOnes.size()>0))
		{
			for(int b=0;b<protectedOnes.size();b++)
			{
				final String B=protectedOnes.get(b);
				if(B.equalsIgnoreCase(protectMe))
					return b;
			}
		}
		return -1;
	}

	private void reallyProtect(final String protectMe)
	{
		final StringBuffer str=Resources.getFileResource("protectedplayers.ini",false);
		if(protectMe.trim().length()>0)
			str.append(protectMe+"\n");
		Resources.updateFileResource("::protectedplayers.ini",str);
	}

	private void unProtect(final String protectMe)
	{
		final StringBuffer str=Resources.getFileResource("protectedplayers.ini",false);
		if(str.toString().startsWith(protectMe+"\n"))
			str.delete(0, protectMe.length()+1);
		else
		{
			final int x=str.indexOf("\n"+protectMe+"\n");
			if(x>0)
				str.delete(x, protectMe.length()+1);
		}
		Resources.updateFileResource("::protectedplayers.ini",str);
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		final String protectMe=CMParms.combine(commands,0);
		if(protectMe.length()==0)
		{
			mob.tell(L("Protect whom?  Enter a player name to protect from autopurge."));
			return false;
		}
		if((!CMLib.players().playerExists(protectMe))
		&&(!CMLib.players().accountExists(protectMe))
		&&(CMLib.clans().getClan(protectMe)==null))
		{
			mob.tell(L("Protect whom?  '@x1' is not a known player.",protectMe));
			return false;
		}
		final int existingIndex = checkExisting(protectMe);
		if(existingIndex >= 0)
		{
			if(mob.Name().equalsIgnoreCase(protectMe))
				mob.tell(L("You are already protected.  Do LIST NOPURGE and check out #@x1.",""+(existingIndex+1)));
			else
				mob.tell(L("That player already protected.  Do LIST NOPURGE and check out #@x1.",""+(existingIndex+1)));
			return false;
		}
		this.reallyProtect(protectMe);
		if(mob.Name().equalsIgnoreCase(protectMe))
			mob.tell(L("You are now protected from autopurge."));
		else
			mob.tell(L("The player '@x1' is now protected from autopurge.",protectMe));
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if((args.length==0)||(!(args[0] instanceof MOB)))
			return Boolean.FALSE;
		if((metaFlags & MUDCmdProcessor.METAFLAG_REVERSED) == MUDCmdProcessor.METAFLAG_REVERSED)
		{
			if(this.checkExisting(((MOB)args[0]).Name())<0)
				return Boolean.FALSE;
			this.unProtect(((MOB)args[0]).Name());
		}
		else
		{
			if(this.checkExisting(((MOB)args[0]).Name())>=0)
				return Boolean.FALSE;
			this.reallyProtect(((MOB)args[0]).Name());
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE);
	}

}
