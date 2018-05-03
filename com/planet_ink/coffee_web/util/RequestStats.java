package com.planet_ink.coffee_web.util;

/*
   Copyright 2012-2018 Bo Zimmerman

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
 * POJ for tracking some basic statistics about servlets
 * Pretty self-explanatory
 * @author Bo Zimmerman
 *
 */
public class RequestStats
{
	private volatile int  requestsProcessed= 0;
	private volatile long requestTime	   = 0;
	private volatile int  requestsInProcess= 0;
	
	public synchronized void startProcessing()
	{
		requestsInProcess++;
	}
	
	public synchronized void endProcessing(long timeEllapsed)
	{
		requestsInProcess--;
		requestsProcessed++;
		requestTime+=timeEllapsed;
	}
	
	public int getNumberOfRequests()
	{
		return requestsProcessed;
	}
	
	public synchronized long getAverageEllapsedNanos()
	{
		if(requestsProcessed == 0)
			return 0;
		return requestTime / requestsProcessed;
	}
	
	public int getNumberOfRequestsInProcess()
	{
		return requestsInProcess;
	}
}
