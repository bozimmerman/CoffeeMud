package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.*;

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
public class StdTickClient implements TickClient
{
	public final Tickable	clientObject;
	public final int		tickID;
	public final int		reTickDown;
	public volatile int		tickDown	= 0;
	public boolean			suspended	= false;
	public volatile long	lastStart	= 0;
	public volatile long	lastStop	= 0;
	public volatile long	milliTotal	= 0;
	public volatile int		tickTotal	= 0;
	public volatile String	status		= null;

	public StdTickClient(Tickable newClientObject, int newTickDown, int newTickID)
	{
		reTickDown=newTickDown;
		tickDown=newTickDown;
		clientObject=newClientObject;
		tickID=newTickID;
	}

	@Override
	public String getName()
	{
		if(clientObject!=null)
		{
			return clientObject.name();
		}
		return "?";
	}

	@Override
	public final Tickable getClientObject()
	{
		return clientObject;
	}

	@Override
	public final int getTickID()
	{
		return tickID;
	}

	@Override
	public int getTotalTickDown()
	{
		return reTickDown;
	}

	@Override
	public int getCurrentTickDown()
	{
		return tickDown;
	}

	@Override
	public String getStatus()
	{
		if(!isAwake())
			return "Sleeping";
		if(status != null)
			return status;
		final Tickable T=getClientObject();
		if(T==null)
			return "Awake";
		return "Awake ("+T.getTickStatus()+")";
	}

	@Override
	public void setStatus(String status)
	{
		this.status = status;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof StdTickClient)
			return compareTo((StdTickClient)obj)==0;
		return false;
	}

	@Override
	public int hashCode()
	{
		return clientObject == null ?  0 : clientObject.hashCode();
	}

	@Override
	public int compareTo(TickClient arg0)
	{
		if(clientObject != arg0.getClientObject())
			return (clientObject.hashCode() > arg0.getClientObject().hashCode())?1:-1;
		if(tickID>arg0.getTickID())
			return 1;
		if(tickID<arg0.getTickID())
			return -1;
		return 0;
	}
	
	@Override
	public void setCurrentTickDownPending()
	{
		tickDown = 1;
	}

	@Override
	public long getTimeMSToNextTick()
	{
		if(isSuspended() || isAwake())
			return -1;
		long msToGo = CMProps.getTickMillis() - (System.currentTimeMillis() - lastStop);
		if(tickDown > 0)
			msToGo += (CMProps.getTickMillis() + tickDown);
		return msToGo;
	}
	
	@Override
	public boolean tickTicker(boolean forceTickDown)
	{
		try
		{
			lastStart=System.currentTimeMillis();
			if(forceTickDown)
				tickDown = 0;
			if((!suspended) && ((--tickDown)<1))
			{
				tickDown=reTickDown;
				try
				{
					if(!clientObject.tick(clientObject,tickID))
					{
						return true;
					}
				}
				catch(final Throwable t)
				{
					Log.errOut(t);
					return true;
				}
			}
		}
		finally
		{
			lastStop=System.currentTimeMillis();
			milliTotal+=(lastStop-lastStart);
			tickTotal++;
		}
		return false;
	}

	@Override
	public long getLastStartTime()
	{
		return lastStart;
	}

	@Override
	public long getLastStopTime()
	{
		return lastStop;
	}

	@Override
	public long getMilliTotal()
	{
		return milliTotal;
	}

	@Override
	public long getTickTotal()
	{
		return tickTotal;
	}

	@Override
	public boolean isAwake()
	{
		return lastStop < lastStart;
	}

	@Override
	public boolean isSuspended()
	{
		return suspended;
	}

	@Override
	public void setSuspended(boolean trueFalse)
	{
		suspended = trueFalse;
	}
}
