package com.planet_ink.coffee_web.util;

import java.util.LinkedList;

import com.planet_ink.coffee_mud.core.collections.Pair;

/*
   Copyright 2014-2018 Bo Zimmerman

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
 * A wrapper for a per second throttling object
 * @author Bo Zimmerman
 *
 */
public class ThrottleSpec
{
	private final	 Long	maxBytesEachSecond;
	private volatile long	currentTotal	= 0;
	private volatile long	bytesRemaining;

	private final LinkedList<Pair<Long,Long>> rollingBucket = new LinkedList<Pair<Long,Long>>();

	/**
	 * Create a ThrottleSpec object for a particular path/domain. 
	 * @param bytesAllowedEachSecond the maximum bytes per second at the domain/path
	 */
	public ThrottleSpec(long bytesAllowedEachSecond)
	{
		this.maxBytesEachSecond = new Long(bytesAllowedEachSecond);
		this.bytesRemaining	= maxBytesEachSecond.longValue() - currentTotal;
	}
	
	/**
	 * Request permission to output up to the number of bytes requested.
	 * This method responds with the number of bytes permitted.
	 * @param bytesRequested the max bytes to request transmitting
	 * @return the permitted number of bytes to transmit
	 */
	public synchronized long request(final long bytesRequested)
	{
		trimBucket(System.currentTimeMillis());
		if(bytesRemaining > bytesRequested)
			return bytesRequested;
		if(bytesRemaining > 0)
			return bytesRemaining;
		if((rollingBucket.size()>0) && (bytesRemaining <= 0))
		{
			try 
			{ 
				long remainingTime = (rollingBucket.getFirst().first.longValue() - System.currentTimeMillis());
				Thread.sleep( remainingTime < 2 ? 1 : remainingTime ); 
			} 
			catch(Exception e){ }
			return request(bytesRequested);
		}
		return 1;
	}
	
	private void trimBucket(long now)
	{
		while(rollingBucket.size()>0)
		{
			final Pair<Long,Long> p = rollingBucket.peekFirst();
			if(now < p.first.longValue())
				break;
			this.currentTotal -= rollingBucket.removeFirst().second.longValue();
			this.bytesRemaining	= maxBytesEachSecond.longValue() - currentTotal;
		}
	}
	
	/**
	 * Called to report to the throttle object how many bytes were
	 * actually transmitted in a given instance.
	 * @param bytesWritten bytes written
	 */
	public synchronized void registerWritten(final long bytesWritten)
	{
		final long now=System.currentTimeMillis();
		trimBucket(now);
		rollingBucket.addLast(new Pair<Long,Long>(Long.valueOf(now + 1000),Long.valueOf(bytesWritten)));
		this.currentTotal += bytesWritten;
		this.bytesRemaining	= maxBytesEachSecond.longValue() - currentTotal;
	}
}
