package com.planet_ink.coffee_mud.Behaviors.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
/**
 * A ChattyBehavior is a Behavior causes a mob to have a conversation,
 * or even just simply respond to a player or even another mob.
 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
 */
public interface ChattyBehavior extends Behavior
{
	/**
	 * Returns the last thing actually spoken by the wielder of this
	 * behavior, or null if nothing has been said yet.
	 * @return the last thing said.
	 */
	public String getLastThingSaid();

	/**
	 * Returns the last MOB object spoken to.
	 * @return the last MOB object spoken to.
	 */
	public MOB getLastRespondedTo();

	/**
	 * A response object representing something the chatty-one will
	 * definitely be saying soon.
	 * @author Bo Zimmermanimmerman
	 */
	public static class ChattyResponse
	{
		public int			delay;
		public List<String>	parsedCommand;
		
		public ChattyResponse(List<String> cmd, int responseDelay)
		{
			parsedCommand = cmd;
			delay = responseDelay;
		}
	}

	/**
	 * A test response is a possible response to an environmental event, such as
	 * someone speaking or acting.  It is only one possible response to one possible
	 * event, and is weighed against its neighbors for whether it is chosen.
	 * @author Bo Zimmermanimmerman
	 */
	public static class ChattyTestResponse
	{
		public String[] responses;
		public int weight;
		public ChattyTestResponse(String resp)
		{
			weight=CMath.s_int(""+resp.charAt(0));
			responses=CMParms.parseSquiggleDelimited(resp.substring(1),true).toArray(new String[0]);
		}
	}

	/**
	 * A chatty entry embodies a test for a particular environmental event, such as
	 * someone speaking or acting, and all possible responses to that event.
	 * @author Bo Zimmermanimmerman
	 */
	public static class ChattyEntry
	{
		public String				expression;
		public ChattyTestResponse[]	responses;
		public boolean				combatEntry	= false;

		public ChattyEntry(String expression)
		{
			if (expression.startsWith("*"))
			{
				combatEntry = true;
				expression = expression.substring(1);
			}
			this.expression = expression;
		}
	}

	/**
	 * A chatty group is a collection of particular environmental event tests, and
	 * their possible responses.  It completely embodies a particular "chat behavior"
	 * for a particular kind of chatty mob.
	 * @author Bo Zimmermanimmerman
	 */
	public static class ChattyGroup implements Cloneable
	{
		public String[]								groupNames;
		public MaskingLibrary.CompiledZMask[]	groupMasks;
		public ChattyEntry[]						entries	= null;
		public ChattyEntry[]						tickies	= null;

		public ChattyGroup(String[] names, MaskingLibrary.CompiledZMask[] masks)
		{
			groupNames = names;
			groupMasks = masks;
		}

		@Override
		public ChattyGroup clone()
		{
			try
			{
				return (ChattyGroup) super.clone();
			}
			catch (final Exception e)
			{
				return this;
			}
		}
	}
}
