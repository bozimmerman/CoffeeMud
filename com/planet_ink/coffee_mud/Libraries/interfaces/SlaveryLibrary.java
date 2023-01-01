package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.CommonMsgs;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2023 Bo Zimmerman

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
 * Process handling for slaves and the victims of a Geas.
 * It accepts a plain-english command, generates the steps
 * for the slave to carry out the command, and then, when
 * ticked, handles that execution for the slave.
 *
 * @author Bo Zimmerman
 *
 */
public interface SlaveryLibrary extends CMLibrary
{
	/**
	 * Given the master, and the slave mobs, and the plain english
	 * order, this will do its best to generate the steps that the
	 * slave will perform to accomplish the order.
	 *
	 * @param masterM the master, geas-giver
	 * @param slaveM the slave, geas-receiver
	 * @param req the order/command
	 * @return the steps to tick through
	 */
	public GeasSteps processRequest(MOB masterM, MOB slaveM, String req);

	/**
	 * The main stepping object for the slave/geas victim,
	 * which tracks multiple high-level steps, even of which
	 * must be completed for the task itself to be completed.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface GeasSteps extends List<GeasStep>
	{
		/**
		 * The main ticking method, which will cause the slave/mob
		 * to take an action towards the completion of the geas
		 * task.
		 */
		public void step();

		/**
		 * For internal use, mostly, this will cause the mob to
		 * change rooms is possible.
		 *
		 * @param wander true to allow leaving an area
		 */
		public void move(boolean wander);

		/**
		 * When the slave hears someone speak in the same room as
		 * them, it might be to answer a question.  This method is
		 * therefore called to process any perceived speech, whether
		 * currently needed or not.
		 *
		 * @param speakerM the speaker mob
		 * @param targetM the target mob, if any
		 * @param response the words spoken
		 * @return true if the response was processed, false if ignored
		 */
		public boolean sayResponse(MOB speakerM, MOB targetM, String response);

		/**
		 * Returns this geas steps slave mob.
		 * @return this geas steps slave mob
		 */
		public MOB stepperM();

		/**
		 * Returns the set of mobs that these steps have bothered
		 * with questions, in order to prevent repeats.
		 * @return the set of bothered mobs
		 */
		public Set<MOB> getBotheredMobs();

		/**
		 * Returns true only when the slave has completed every step
		 * in their process and these steps can be deleted.
		 *
		 * @return true when done
		 */
		public boolean isDone();
	}

	/**
	 * A process handler for an individual requires step in a
	 * geas or command.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface GeasStep
	{
		/**
		 * The main ticking method, which will cause the slave/mob
		 * to take an action towards the completion of the geas
		 * step.  It returns the next string action to take.
		 *
		 * @return the next action to take
		 */
		public String step();

		/**
		 * Sometimes an individual action inside a step requires multiple
		 * ticks to accomplish.  This simple ordinal counter is used to
		 * keep track of those. It is 0 based.
		 * @param subStepNum the sub step ordinal
		 */
		public void setSubStep(int subStepNum);

		/**
		 * When an individual step action 'bothers' a mob with questions
		 * or other actions, this method allows that chosen mob to be
		 * tracked over to to judge the response.
		 *
		 * @return null, or the mob last bothered
		 */
		public MOB getBotheredMob();

		/**
		 * Given a message or question, this will cause the slave to
		 * speak it to someone in the room, thus bothering them.
		 *
		 * @param msgOrQ the message to speak.
		 * @return true if someone was bothered, false otherwise
		 */
		public boolean botherIfAble(String msgOrQ);

		/**
		 * When the slave hears someone speak in the same room as
		 * them, it might be to answer a question.  This method is
		 * therefore called to process any perceived speech, whether
		 * currently needed or not.
		 *
		 * @param speakerM the speaker mob
		 * @param targetM the target mob, if any
		 * @param response the words spoken
		 * @return true if the response was processed, false if ignored
		 */
		public boolean sayResponse(MOB speakerM, MOB targetM, String response);
	}
}
