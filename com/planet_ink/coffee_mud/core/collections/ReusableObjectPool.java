package com.planet_ink.coffee_mud.core.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2011-2018 Bo Zimmerman

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
public class ReusableObjectPool<T extends CMObject>
{
	private final NotifyingCMObjectVector<T>		masterList;
	private final Stack<NotifyingCMObjectVector<T>>	masterPool	= new Stack<NotifyingCMObjectVector<T>>();
	private volatile int							created		= 0;
	private volatile int							requests	= 0;
	private volatile int							distributed	= 0;
	private volatile int							returned	= 0;
	private final int								minEntries;
	private final Object							sync		= new Object();
	private final PoolFixer							fixer		= new PoolFixer();

	private class PoolFixer implements Runnable
	{
		private boolean	isRunning	= false;

		public void startFixing()
		{
			if (isRunning)
			{
				fix();
				return;
			}
			synchronized (this)
			{
				if (!isRunning)
				{
					isRunning = true;
					new Thread(Thread.currentThread().getThreadGroup(), this, "PoolFixer" + Thread.currentThread().getThreadGroup().getName().charAt(0)).start();
					return;
				}
			}
			fix();
		}

		public void fix()
		{
			Runtime.getRuntime().runFinalization();
			System.gc();
			try
			{
				Thread.sleep(100);
			}
			catch (final Exception e)
			{
			}
			Runtime.getRuntime().runFinalization();
			try
			{
				Thread.sleep(100);
			}
			catch (final Exception e)
			{
			}
			System.gc();
		}

		@Override
		public void run()
		{
			try
			{
				int iters = 0;
				while (masterPool.size() < minEntries)
				{
					fix();
					if ((masterPool.size() < minEntries) && (iters > 5))
					{
						iters = 0;
						synchronized (sync)
						{
							masterPool.add(makeNewEntry());
						}
					}
					iters++;
				}
			}
			finally
			{
				synchronized (this)
				{
					isRunning = false;
				}
			}
		}
	}

	private class NotifyingCMObjectVector<K extends CMObject> extends Vector<T>
	{
		private static final long	serialVersionUID	= 1L;

		public NotifyingCMObjectVector(final List<T> V)
		{
			super(V);
		}

		public NotifyingCMObjectVector(int size)
		{
			super(size);
		}

		@Override
		protected void finalize() throws Throwable
		{
			final NotifyingCMObjectVector<T> V = new NotifyingCMObjectVector<T>(this);
			synchronized (sync)
			{
				returned++;
				masterPool.push(V);
			}
			super.finalize();
		}
	}

	public ReusableObjectPool(final List<T> initialEntry, final int minEntries)
	{
		if (initialEntry.size() == 0)
			this.masterList = new NotifyingCMObjectVector<T>(new ReadOnlyList<T>(initialEntry));
		else
			this.masterList = new NotifyingCMObjectVector<T>(initialEntry);
		this.minEntries = (minEntries < 2) ? 2 : minEntries;
		for (int i = 0; i < minEntries; i++)
			masterPool.add(makeNewEntry());
	}

	public int getMasterPoolSize()
	{
		return masterPool.size();
	}

	public int getListSize()
	{
		return masterList.size();
	}

	@SuppressWarnings("unchecked")
	private final NotifyingCMObjectVector<T> makeNewEntry()
	{
		final NotifyingCMObjectVector<T> myList = new NotifyingCMObjectVector<T>(masterList.size());
		for (final T o : masterList)
			myList.add((T) o.copyOf());
		created++;
		if (created == minEntries * 1000)
			Log.errOut("ReuseOP", "Reusable Object Pool pass all reason: " + CMParms.toListString(masterList));
		return myList;
	}

	public final List<T> get()
	{
		if (masterList.size() == 0)
			return masterList;
		requests++;
		if (masterPool.size() < minEntries / 2)
			fixer.startFixing();
		synchronized (sync)
		{
			if (!masterPool.isEmpty())
			{
				final NotifyingCMObjectVector<T> myList = masterPool.pop();
				if (myList != null)
				{
					distributed++;
					return myList;
				}
			}
		}
		fixer.fix();
		return makeNewEntry();
		// final List<T> myList=new Vector<T>(masterList.size());
		// for(final T o : masterList)
		// myList.add((T)o.copyOf());
		// return myList;
	}
}
