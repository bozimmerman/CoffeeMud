package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
/*
	Copyright 2026-2026 Bo Zimmerman
	
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
public class ConcurrentConnectState extends StdTest
{
	private final String[]	addresses	= buildAddresses();
	private boolean			spamBlockWasDisabled	= false;

	private static String[] buildAddresses()
	{
		final String[] addrs=new String[16];
		for(int i=0;i<addrs.length;i++)
			addrs[i]="10.245."+i+".7";
		return addrs;
	}

	@Override
	public String ID()
	{
		return "ConcurrentConnectState";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] { "all", "core" };
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		spamBlockWasDisabled=CMSecurity.isDisabled(CMSecurity.DisFlag.CONNSPAMBLOCK);
		if(spamBlockWasDisabled)
			CMSecurity.removeDisableVar(CMSecurity.DisFlag.CONNSPAMBLOCK);
		for(final String addr : addresses)
		{
			if(CMSecurity.isBanned(addr)||CMSecurity.isIPBlocked(addr))
				return "Error#1: "+addr+" is already banned or blocked";
			if(CMProps.isOnWhiteList(CMProps.WhiteList.IPSCONN, addr))
				return "Error#2: "+addr+" is already whitelisted";
		}
		final int numThreads=24;
		final int numIters=10000;
		final AtomicReference<String> errorMsg=new AtomicReference<String>(null);
		final List<Thread> threads=new ArrayList<Thread>();
		for(int t=0;t<numThreads;t++)
		{
			final int threadIdx=t;
			final boolean spamCheckPath=((t&1)==0);
			final Thread thread=new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final int[] numAtAddress=new int[1];
						for(int i=0;i<numIters;i++)
						{
							final String address=addresses[(threadIdx*7+i)%addresses.length];
							for(int b=0;b<6;b++)
							{
								if(spamCheckPath)
									CMLib.login().performSpamConnectionCheck(address);
								else
								{
									final CMSecurity.ConnectState state=CMSecurity.getConnectState(address,numAtAddress);
									if((state!=CMSecurity.ConnectState.NORMAL)
									&&(state!=CMSecurity.ConnectState.BLOCKED))
									{
										errorMsg.compareAndSet(null,"Error#3: unexpected state "+state+" for "+address);
										return;
									}
								}
							}
							CMSecurity.clearConnectState(address);
							if(spamCheckPath)
								CMLib.login().performSpamConnectionCheck(address);
							else
							{
								final CMSecurity.ConnectState state=CMSecurity.getConnectState(address,numAtAddress);
								if((state!=CMSecurity.ConnectState.NORMAL)
								&&(state!=CMSecurity.ConnectState.BLOCKED))
								{
									errorMsg.compareAndSet(null,"Error#3: unexpected state "+state+" for "+address);
									return;
								}
							}
						}
					}
					catch(final Throwable t)
					{
						errorMsg.compareAndSet(null,"Error#4: "+t.getClass().getSimpleName()+": "+t.getMessage());
					}
				}
			});
			threads.add(thread);
		}
		for(final Thread thread : threads)
			thread.start();
		final long deadline=System.currentTimeMillis()+60000;
		for(final Thread thread : threads)
		{
			final long remaining=deadline-System.currentTimeMillis();
			if(remaining>0)
			{
				try
				{
					thread.join(remaining);
				}
				catch(final InterruptedException e)
				{
				}
			}
			if(thread.isAlive())
				return "Error#5: thread "+thread.getName()+" never finished the concurrent connection-state loop";
		}
		if(errorMsg.get()!=null)
			return errorMsg.get();
		return null;
	}

	@Override
	public void cleanupTest()
	{
		if(spamBlockWasDisabled)
			CMSecurity.setDisableVar(CMSecurity.DisFlag.CONNSPAMBLOCK);
		else
			CMSecurity.removeDisableVar(CMSecurity.DisFlag.CONNSPAMBLOCK);
		for(final String addr : addresses)
		{
			try
			{
				CMSecurity.clearConnectState(addr);
				CMSecurity.getConnectState(addr,null);
				CMSecurity.clearConnectState(addr);
			}
			catch(final Throwable t)
			{
			}
		}
	}
}
