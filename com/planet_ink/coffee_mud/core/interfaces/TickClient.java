package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

/*
   Copyright 2013-2018 Bo Zimmerman

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
 * Encapsulates, manages, and collects statistics about a single ticking
 * object in the game.  These clients are grouped together in TickableGroup
 * objects, which are in turn called by the ServiceEngine to get thread time.
 * @author Bo Zimmerman
 *
 */
public interface TickClient extends Comparable<TickClient>
{
	/**
	 * Returns the ticking object that this client encapsulates
	 * @see Tickable
	 * @return the ticking object that this client encapsulates
	 */
	public Tickable getClientObject();

	/**
	 * Returns the TICKID_ constant assigned to this object
	 * @see Tickable#TICKID_AREA
	 * @return the TICKID_ constant assigned to this object
	 */
	public int getTickID();

	/**
	 * Returns the status of this ticking client as a
	 * displayable String, usually just a translation
	 * of the {@link Tickable#getTickStatus()}
	 * @see Tickable#getTickStatus()
	 * @return the TICKID_ constant assigned to this object
	 */
	public String getStatus();

	/**
	 * Returns the name of this ticking client as a
	 * displayable String, usually just a translation
	 * of the {@link Tickable#name()}
	 * @return the TICKID_ constant assigned to this object
	 */
	public String getName();

	/**
	 * Sets the status of this ticking client as a
	 * displayable String
	 * @param status the new status to set this client to
	 * @see TickClient#getStatus()
	 */
	public void setStatus(String status);

	/**
	 * Potentially allows the internal object to be ticked. This
	 * will cause the current tick down, which starts at
	 * totaltickdown to be decremented by one.  When it reaches 0,
	 * the internal object will be "ticked".
	 * @see Tickable#tick(Tickable, int)
	 * @param forceTickDown true to override the currentTickDown
	 * @return true if the object is done ticking forever, false to keep going
	 */
	public boolean tickTicker(boolean forceTickDown);

	/**
	 * Returns the time, in ms, that this object last ticked.
	 * @return the time, in ms, that this object last ticked.
	 */
	public long getLastStartTime();

	/**
	 * Returns the time, in ms, that this object last stopped ticking.
	 * @return the time, in ms, that this object last stopped ticking
	 */
	public long getLastStopTime();

	/**
	 * Returns the number of ms that this object has ticked in total
	 * @return the number of ms that this object has ticked in total
	 */
	public long getMilliTotal();

	/**
	 * Returns the number of times that this object has ticked in total
	 * @return the number of times that this object has ticked in total
	 */
	public long getTickTotal();

	/**
	 * Returns true if this is currently getting thread time
	 * @return true if this is currently getting thread time
	 */
	public boolean isAwake();

	/**
	 * Returns true if this object is currently suspended
	 * @return  true if this object is currently suspended
	 */
	public boolean isSuspended();

	/**
	 * Sets whether this object is suspended from getting thread time
	 * @param trueFalse true to suspend it, false to unsuspend it
	 */
	public void setSuspended(boolean trueFalse);

	/**
	 * Returns the number of ticks total before the internal object is
	 * allowed to get thread time.
	 * @return the number of ticks total
	 */
	public int getTotalTickDown();

	/**
	 * Returns the number of ticks remaining before the internal object is
	 * allowed to get thread time.
	 * @return the number of ticks remaining
	 */
	public int getCurrentTickDown();

	/**
	 * Sets the number of ticks remaining before the internal object is
	 * allowed to get thread time to 1.
	 */
	public void setCurrentTickDownPending();
	
	/**
	 * Gets the number of total milliseconds before another tick 
	 * will occur., or -1.
	 * @return the number of ms to go
	 */
	public long getTimeMSToNextTick();
}
