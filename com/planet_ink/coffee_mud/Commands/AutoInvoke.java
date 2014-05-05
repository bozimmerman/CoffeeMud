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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class AutoInvoke extends StdCommand
{
	public AutoInvoke(){}

	private final String[] access=_i(new String[]{"AUTOINVOKE"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(final MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> abilities=new Vector<String>();
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A.isAutoInvoked())
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY))
				abilities.addElement(A.ID());
		}

		final Vector<String> effects=new Vector<String>();
		for(int a=0;a<mob.numEffects();a++)
		{
			final Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(abilities.contains(A.ID()))
			&&(!A.isSavable()))
				effects.addElement(A.ID());
		}

		final StringBuffer str=new StringBuffer("^xAuto-invoking abilities:^?^.\n\r^N");
		int col=0;
		for(int a=0;a<abilities.size();a++)
		{
			final Ability A=mob.fetchAbility(abilities.elementAt(a));
			if(A!=null)
			{
				if(effects.contains(A.ID()))
					str.append(_("@x1.^xACTIVE^?^.^N ",CMStrings.padRightWith(A.Name(),'.',30)));
				else
					str.append(_("@x1^xINACTIVE^?^.^N",CMStrings.padRightWith(A.Name(),'.',30)));
				if(++col==2)
				{
					col=0;
					str.append("\n\r");
				}
				else
					str.append("  ");
			}
		}
		if(col==1)
			str.append("\n\r");

		mob.tell(str.toString());
		final Session session=mob.session();
		if(session!=null)
		{
			session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
			{
				@Override public void showPrompt() { session.promptPrint(_("Enter one to toggle or RETURN: "));}
				@Override public void timedOut() { }
				@Override public void callBack()
				{
					final String s=this.input;
					Ability foundA=null;
					if(s.length()>0)
					{
						for(int a=0;a<abilities.size();a++)
						{
							final Ability A=mob.fetchAbility(abilities.elementAt(a));
							if((A!=null)&&(A.name().equalsIgnoreCase(s)))
							{ foundA=A; break;}
						}
						if(foundA==null)
						for(int a=0;a<abilities.size();a++)
						{
							final Ability A=mob.fetchAbility(abilities.elementAt(a));
							if((A!=null)&&(CMLib.english().containsString(A.name(),s)))
							{ foundA=A; break;}
						}
						if(foundA==null)
							mob.tell(_("'@x1' is invalid.",s));
						else
						if(effects.contains(foundA.ID()))
						{
							foundA=mob.fetchEffect(foundA.ID());
							if(foundA!=null)
							{
								mob.delEffect(foundA);
								if(mob.fetchEffect(foundA.ID())!=null)
									mob.tell(_("@x1 failed to successfully deactivate.",foundA.name()));
								else
									mob.tell(_("@x1 successfully deactivated.",foundA.name()));
							}
						}
						else
						{
							foundA.autoInvocation(mob);
							if(mob.fetchEffect(foundA.ID())!=null)
								mob.tell(_("@x1 successfully invoked.",foundA.name()));
							else
								mob.tell(_("@x1 failed to successfully invoke.",foundA.name()));
						}
					}
				}
			});
		}
		return false;
	}


	@Override public boolean canBeOrdered(){return true;}

}
