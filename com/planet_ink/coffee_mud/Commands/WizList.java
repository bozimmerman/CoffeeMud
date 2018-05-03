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

public class WizList extends StdCommand
{
	public WizList(){}

	private final String[] access=I(new String[]{"WIZLIST"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer head=new StringBuffer("");
		final boolean isArchonLooker=CMSecurity.isASysOp(mob);
		head.append("^x[");
		head.append(CMStrings.padRight(L("Class"),16)+" ");
		head.append(CMStrings.padRight(L("Race"),8)+" ");
		head.append(CMStrings.padRight(L("Lvl"),4)+" ");
		if(isArchonLooker)
			head.append(CMStrings.padRight(L("Last"),18)+" ");
		head.append("] Character Name^.^?\n\r");
		mob.tell("^x["+CMStrings.centerPreserve(L("The Administrators of @x1",CMProps.getVar(CMProps.Str.MUDNAME)),head.length()-10)+"]^.^?");
		final java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		String mask=CMProps.getVar(CMProps.Str.WIZLISTMASK);
		if(mask.length()==0)
			mask="-ANYCLASS +Archon";
		final MaskingLibrary.CompiledZMask compiledMask=CMLib.masking().maskCompile(mask);
		for(final PlayerLibrary.ThinPlayer U : allUsers)
		{
			CharClass C;
			final MOB player = CMLib.players().getPlayer(U.name());
			if(player != null)
				C=player.charStats().getCurrentClass();
			else
				C=CMClass.getCharClass(U.charClass());
			if(C==null)
				C=CMClass.findCharClass(U.charClass());
			if(((player!=null)&&(CMLib.masking().maskCheck(compiledMask, player, true)))
			||(CMLib.masking().maskCheck(compiledMask, U)))
			{
				head.append("[");
				if(C!=null)
					head.append(CMStrings.padRight(C.name(),16)+" ");
				else
					head.append(CMStrings.padRight(L("Unknown"),16)+" ");
				head.append(CMStrings.padRight(U.race(),8)+" ");
				if((C==null)||(!C.leveless()))
					head.append(CMStrings.padRight(""+U.level(),4)+" ");
				else
					head.append(CMStrings.padRight("    ",4)+" ");
				if(isArchonLooker)
					head.append(CMStrings.padRight(CMLib.time().date2String(U.last()),18)+" ");
				head.append("] "+U.name());
				head.append("\n\r");
			}
		}
		mob.tell(head.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
