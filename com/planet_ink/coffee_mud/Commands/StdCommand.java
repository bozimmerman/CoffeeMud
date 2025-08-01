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
   Copyright 2002-2025 Bo Zimmerman

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
public class StdCommand implements Command
{
	protected final String	ID;
	private final String[]	access	= null;

	public StdCommand()
	{
		final String id=this.getClass().getName();
		final int x=id.lastIndexOf('.');
		if(x>=0)
			ID=id.substring(x+1);
		else
			ID=id;
	}

	@Override
	public String ID()
	{
		return ID;
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public void initializeClass()
	{
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	public static String[] I(final String[] str)
	{
		for(int i=0;i<str.length;i++)
			str[i]=CMLib.lang().commandWordTranslation(str[i]);
		return str;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		// accepts the mob executing, and a list of Strings as a parm.
		// the return value is arbitrary, though false is conventional.
		return false;
	}

	@Override
	public boolean preExecute(final MOB mob, final List<String> commands, final int metaFlags, final int secondsElapsed, final double actionsRemaining)
		throws java.io.IOException
	{
		return true;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		// fake it!
		final Vector<String> commands = new Vector<String>();
		if(getAccessWords().length>0)
			commands.add(getAccessWords()[0]);
		else
			commands.add(ID());
		for(final Object o : args)
		{
			if(o != null)
				commands.add(o.toString());
		}
		return Boolean.valueOf(execute(mob,commands,metaFlags));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int getArgumentSetIndex(final Class[][] fmt, final Object... args)
	{
		for (int index=0;index<fmt.length;index++)
		{
			final Class[] ff=fmt[index];
			if(ff.length==args.length)
			{
				boolean check=true;
				for(int i=0;i<ff.length;i++)
				{
					if((args[i]!=null)
					&&(ff[i]!=null)
					&&(!ff[i].isAssignableFrom(args[i].getClass())))
					{
						check=false;
						break;
					}
				}
				if(check)
					return index;
			}
		}
		return -1;
	}

	@SuppressWarnings({ "rawtypes"})
	public boolean checkArguments(final Class[][] fmt, final Object... args)
	{
		final int index = getArgumentSetIndex(fmt,args);
		if(index >=0)
			return true;

		final StringBuilder str=new StringBuilder("");
		str.append(L("Illegal arguments. Sent: "));
		for(final Object o : args)
		{
			if(o==null)
				str.append(L("null "));
			else
				str.append(o.getClass().getSimpleName()).append(" ");
		}
		str.append(L(". Correct: "));
		for (final Class[] element : fmt)
		{
			for(final Class c : element)
				str.append(c.getSimpleName()).append(" ");
		}
		Log.errOut(ID(),str.toString());
		return false;
	}

	protected MOB getVisibleRoomTarget(final MOB mob, final String whom)
	{
		if(mob == null)
			return null;
		final Room R=mob.location();
		if(R==null)
			return null;
		MOB target = R.fetchInhabitant(whom);
		int ctr=1;
		while ((target != null)
		&& (!CMLib.flags().canBeSeenBy(target, mob))
		&&(whom.indexOf('.')<0))
			target = R.fetchInhabitant(whom+"."+(++ctr));
		return target;
	}

	protected boolean isOccupiedWithOtherWork(final MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(!A.isAutoInvoked())
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				return true;
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID(), 0.0);
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID(), 0.0);
	}

	@Override
	public boolean canBeCancelled()
	{
		return false;
	}

	@Override
	public double checkedActionsCost(final MOB mob, final List<String> cmds)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return combatActionsCost(mob,cmds);
			final Room R=mob.location();
			if(R!=null)
			{
				final Area A=R.getArea();
				if(A instanceof Boardable)
				{
					final Boardable ship = (Boardable)A;
					if((ship.getBoardableItem() instanceof Combatant)
					&&(((Combatant)ship.getBoardableItem()).isInCombat()))
						return combatActionsCost(mob,cmds);
				}
			}
		}
		return actionsCost(mob,cmds);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return true;
	}

	@Override
	public CMObject newInstance()
	{
		return this;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final Object O=this.clone();
			return (CMObject)O;
		}
		catch(final CloneNotSupportedException e)
		{
			return this;
		}
	}

	protected final static Filterer<Environmental> noCoinFilter=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(final Environmental obj)
		{
			return !(obj instanceof Coins);
		}
	};

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public boolean putInCommandlist()
	{
		return true;
	}
}
