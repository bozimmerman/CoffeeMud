package com.planet_ink.coffee_mud.core.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;
/*
Copyright 2000-2011 Bo Zimmerman

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
public class ReusableObjectPool 
{
	private final NotifyingCMObjectVector	    	  masterList;
	private final LinkedList<NotifyingCMObjectVector> masterPool = new LinkedList<NotifyingCMObjectVector>();
	
	private class NotifyingCMObjectVector extends Vector<CMObject>
	{
		private static final long serialVersionUID = 1L;
		
		public NotifyingCMObjectVector(List<CMObject> V)
		{
			super(V);
		}
		public NotifyingCMObjectVector(int size)
		{
			super(size);
		}
		protected void finalize() throws Throwable
		{
			NotifyingCMObjectVector V = new NotifyingCMObjectVector(this);
			synchronized(masterPool)
			{
		    	masterPool.addLast(V);
			}
			super.finalize();
		}
	};
	
	public ReusableObjectPool(final List<CMObject> initialEntry)
	{
		this.masterList = new NotifyingCMObjectVector(initialEntry);
	}
	
	public int getMasterPoolSize() { return masterPool.size();}
	
	public final List<CMObject> get()
	{
		if(masterPool.isEmpty())
		{
			System.gc();
			System.runFinalization();
			System.gc();
		}
		synchronized(masterPool)
		{
			if(!masterPool.isEmpty())
			{
				final NotifyingCMObjectVector myList=masterPool.removeFirst();
				if(myList != null)
					return myList;
			}
		}
		final NotifyingCMObjectVector myList=new NotifyingCMObjectVector(masterList.size());
		for(final CMObject o : masterList)
			myList.add(o.copyOf());
		return myList;
	}
}
