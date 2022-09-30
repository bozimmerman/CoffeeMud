package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2022-2022 Bo Zimmerman

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
 * A Triggerer is a manager for one or more series of actions
 * which, when done properly, results in a completed trigger,
 * which can then, well, trigger something.
 *
 * @author Bo Zimmerman
 *
 */
public interface Triggerer extends CMCommon
{
	/**
	 * The definition of the key words in the definitions.
	 * Most of these require a parameter of one sort or another,
	 * depending on the code.  The command phrases
	 * are separated by &amp; (for and) or | for or.
	 * While all arguments are stored as strings (mostly),
	 * the enum does give clues about the number and
	 * ultimate type of each argument.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum TriggerCode
	{
		SAY(String.class),
		TIME(Integer.class),
		PUTTHING(String.class, String.class),
		BURNMATERIAL(RawMaterial.Material.class),
		BURNTHING(String.class),
		EAT(String.class),
		DRINK(String.class),
		INROOM(String.class),
		RIDING(String.class),
		CAST(Ability.class),
		EMOTE(String.class),
		PUTVALUE(Integer.class, String.class),
		PUTMATERIAL(RawMaterial.Material.class, String.class),
		BURNVALUE(Integer.class),
		SITTING(),
		STANDING(),
		SLEEPING(),
		READING(String.class),
		RANDOM(String.class),
		CHECK(String.class),
		WAIT(Integer.class),
		YOUSAY(String.class),
		OTHERSAY(String.class),
		ALLSAY(String.class),
		SOCIAL(Social.class, String.class),
		;
		public Class<?>[] parmTypes;
		private TriggerCode(final Class<?>... parms)
		{
			parmTypes = parms;
		}
	}

	/**
	 * Some triggers benefit from unique names, and in those cases,
	 * this allows that name to be set for proper matching.
	 *
	 * @param name the Name
	 * @return this is this
	 */
	public Triggerer setName(final String name);

	/**
	 * Parses and adds a new series of steps, which together form
	 * a complete trigger.  The key is an arbitrary object that
	 * is sent back to the caller when a trigger occurs, and so
	 * should be unique to each series of steps
	 *
	 * @param key the arbitrary but unique key object
	 * @param encodedTrigger the encoded trigger steps
	 * @param socials null, or list of custom socials that might apply to this trigger
	 * @param errors null, or a list to put errors into
	 */
	public void addTrigger(final Object key, final String encodedTrigger,
						   final Map<String, List<Social>> socials, final List<String> errors);

	/**
	 * Returns whether a trigger with the given key is being tracked here.
	 *
	 * @param key the trigger key
	 * @return true if it was found, false otherwise
	 */
	public boolean hasTrigger(final Object key);

	/**
	 * Returns a description of the trigger steps
	 * of the given trigger key
	 *
	 * @param key the arbitrary but unique key object
	 * @return a readable description
	 */
	public String getTriggerDesc(final Object key);

	/**
	 * Generates a message for the next step in the
	 * tracked trigger denoted by the given trigger
	 * key.
	 *
	 * @param mob the mob to check
	 * @param key the arbitrary but unique key object
	 * @param force true to force even an unstarted trigger
	 * @return null, or a message for the given mob to do
	 */
	public CMMsg genNextAbleTrigger(final MOB mob, final Object key, boolean force);

	/**
	 * Sets the given mob as being ignored for the purpose of
	 * tracking, or re-enables tracking.
	 *
	 * @param mob the mob to check
	 * @param truefalse true to turn on ignoring, false otherwise
	 */
	public void setIgnoreTracking(final MOB mob, boolean truefalse);

	/**
	 * If the given trigger key is in progress by the given mob,
	 * this cancels it entirely.
	 *
	 * @param mob the mob to check
	 * @param key the arbitrary but unique key object
	 */
	public void deleteTracking(final MOB mob, final Object key);

	/**
	 * Returns whether the given trigger key is in progress
	 * by the given mob
	 *
	 * @param mob the mob to check
	 * @param key the arbitrary but unique key object
	 * @return true if tracking is happening, false otherwise
	 */
	public boolean isTracking(final MOB mob, final Object key);

	/**
	 * Given a message and a trigger key, this will see if
	 * the message might begin, progress, or complete the
	 * given trigger steps.  If it might (just might) do that,
	 * it will return true, or false otherwise.
	 *
	 * @see Triggerer#isCompleted(Object, CMMsg)
	 * @see Triggerer#whichTracking(CMMsg)
	 * @see Triggerer#whichCompleted(Object[], CMMsg)
	 *
	 * @param key the arbitrary but unique key object
	 * @param msg the message that might be relevant.
	 * @return true if it is relevant, false otherwise
	 */
	public boolean isTracking(final Object key, final CMMsg msg);

	/**
	 * Given a message, this will check all triggers to see if
	 * the message might begin, progress, or complete one of the
	 * triggers.  If it might (just might) do any of those, this
	 * will return the trigger keys that should be checked further
	 * by calling whichCompleted.
	 *
	 * @see Triggerer#isCompleted(Object, CMMsg)
	 * @see Triggerer#isTracking(Object, CMMsg)
	 * @see Triggerer#whichCompleted(Object[], CMMsg)
	 *
	 * @param msg the message that might be relevant
	 * @return the unique keys being tracked related to the msg
	 */
	public Object[] whichTracking(final CMMsg msg);

	/**
	 * Given a trigger key, and a message which may be progressed
	 * completed by the given message, this will progress it, and
	 * then return true if it was completed.
	 *
	 * @see Triggerer#whichTracking(CMMsg)
	 * @see Triggerer#isTracking(Object, CMMsg)
	 * @see Triggerer#whichCompleted(Object[], CMMsg)
	 *
	 * @param key the arbitrary but unique key object
	 * @param msg the message which may cause a trigger to complete
	 * @return true if it is completed, false otherwise
	 */
	public boolean isCompleted(final Object key, final CMMsg msg);

	/**
	 * Given a set of trigger keys that may be progressed or
	 * completed by the given message, this will progress them, and
	 * then return the set of keys which, because of the message, are
	 * now in a completed state.
	 *
	 * @see Triggerer#isCompleted(Object, CMMsg)
	 * @see Triggerer#whichTracking(CMMsg)
	 * @see Triggerer#isTracking(Object, CMMsg)
	 *
	 * @param keys the arbitrary but unique keys to apply the message to
	 * @param msg the message which may cause triggers to complete
	 * @return the keys for completed triggers
	 */
	public Object[] whichCompleted(final Object[] keys, final CMMsg msg);

	/**
	 * Given a set of trigger keys that may be progressed or
	 * completed by the given message, this will progress them, and
	 * then return the first one which , because of the message, is
	 * now in a completed state, along with any args accumulated.
	 *
	 * @see Triggerer#isCompleted(Object, CMMsg)
	 * @see Triggerer#whichTracking(CMMsg)
	 * @see Triggerer#isTracking(Object, CMMsg)
	 *
	 * @param keys the arbitrary but unique keys to apply the message to
	 * @param msg the message which may cause triggers to complete
	 * @return null, or the key and arguments pair
	 */
	public Pair<Object,List<String>> getCompleted(final Object[] keys, final CMMsg msg);

	/**
	 * If any triggers are currently in a wait state, this will return
	 * which ones, as of the moment this method was called, are now
	 * done waiting.  Calling this will reset those wait times.  It
	 * returns the set of players who have triggers in wait state.
	 * The caller should 'ping' those players with a message that
	 * called one of the completed methods here.
	 *
	 * @return the set of players whose triggers need pinging
	 */
	public MOB[] whosDoneWaiting();

	/**
	 * Given a parsed user command line entry, this will search the trigger internal
	 * socials for a likely social object match, and return it.
	 *
	 * @param commands the parsed user command line
	 * @param exactOnly true for exact base name match only
	 * @param checkItemTargets true to consider I-NAME target socials
	 * @return null, or the found social.
	 */
	public Social fetchSocial(List<String> commands, boolean exactOnly, boolean checkItemTargets);

	/**
	 * Returns the trigger keys for any triggers that the given
	 * mob currently has in progress.
	 *
	 * @param mob the mob who started something
	 * @return all of the trigger keys in progress
	 */
	public Object[] getInProgress(final MOB mob);

	/**
	 * Returns whether the given mob recently completed the trigger
	 * with the given key, typically within 4 seconds.
	 *
	 * @param mob the mob to check
	 * @param key the object key to check
	 * @return true if completed recently, false
	 */
	public boolean wasCompletedRecently(final MOB mob, final Object key);

	/**
	 * If something has changed that requires a triggerer or tracker to
	 * be reset, then every instance is notified, and this method can
	 * be called to see if this specific object needs resetting.
	 *
	 * @see Triggerer#setObsolete()
	 *
	 * @return true if this instance is obsolete, false otherwise
	 */
	public boolean isObsolete();

	/**
	 * If this triggering engine needs disabling due to changes in
	 * local abilities, this method can be called.
	 *
	 * @see Triggerer#isObsolete()
	 */
	public void setObsolete();

	/**
	 * If this triggering engine is disabled, this will return true
	 *
	 * @return true if this instance is disabled
	 */
	public boolean isDisabled();

	/**
	 * In order to have a Triggerer signaled that the rituals
	 * may need refreshing, this method exists to be called globally.
	 * Every Triggerer impl should implement this.
	 *
	 * @see Triggerer#isObsolete()
	 *
	 * @author Bo Zimmerman
	 */
	public static class TrigSignal
	{
		public static int sig = 0;
	}
}
