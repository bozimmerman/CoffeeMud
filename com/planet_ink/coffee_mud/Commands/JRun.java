package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
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

import org.mozilla.javascript.*;

/*
   Copyright 2005-2020 Bo Zimmerman

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
public class JRun extends StdCommand
{
	public JRun()
	{
	}

	private final String[] access=I(new String[]{"JRUN"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(L("jrun filename1 parm1 parm2 ..."));
			return false;
		}
		commands.remove(0);

		final String fn = commands.get(0);
		final StringBuffer ft = new CMFile(fn,mob,CMFile.FLAG_LOGERRORS).text();
		if((ft==null)||(ft.length()==0))
		{
			mob.tell(L("File '@x1' could not be found.",fn));
			return false;
		}
		commands.remove(0);
		final Context cx = Context.enter();
		try
		{
			final JScriptWindow scope = new JScriptWindow(mob,commands);
			cx.initStandardObjects(scope);
			scope.defineFunctionProperties(JScriptWindow.functions,
										   JScriptWindow.class,
										   ScriptableObject.DONTENUM);
			cx.evaluateString(scope, ft.toString(),"<cmd>", 1, null);
		}
		catch(final Exception e)
		{
			mob.tell(L("JavaScript error: @x1",e.getMessage()));
		}
		Context.exit();
		return false;
	}

	protected static class JScriptWindow extends ScriptableObject
	{
		@Override
		public String getClassName()
		{
			return "JScriptWindow";
		}
		static final long serialVersionUID=45;
		MOB s=null;
		List<String> v=null;

		public MOB mob()
		{
			return s;
		}

		public int numParms()
		{
			return (v == null) ? 0 : v.size();
		}

		public String getParm(final int i)
		{
			if(v==null)
				return "";
			if((i<0)||(i>=v.size()))
				return "";
			return v.get(i);
		}

		public static String[] functions = { "mob", "numParms", "getParm", "getParms", "toJavaString", "getCMType" };

		public String getParms()
		{
			return (v == null) ? "" : CMParms.combineQuoted(v, 0);
		}

		public JScriptWindow(final MOB executor, final List<String> parms)
		{
			s = executor;
			v = parms;
		}

		public String toJavaString(final Object O)
		{
			return Context.toString(O);
		}

		public String getCMType(final Object O)
		{
			if(O == null)
				return "null";
			final CMObjectType typ = CMClass.getObjectType(O);
			if(typ == null)
				return "unknown";
			return typ.name().toLowerCase();
		}
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JSCRIPTS);
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

}
