package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

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
* A utility interface for applying "each" code to iterable objects
* @author Bo Zimmerman
*
*/
public interface EachApplicable<T>
{
	/**
	 * Implement the code that will apply to each object
	 * @param a the object to work on
	 */
	public void apply(final T a);
	
	/**
	 * Example class that affect phyStats
	 */
	public static class ApplyAffectPhyStats<T extends StatsAffecting> implements EachApplicable<T>
	{
		protected final Physical me;
		
		public ApplyAffectPhyStats(Physical me)
		{
			this.me=me;
		}

		@Override
		public void apply(T a) 
		{
			a.affectPhyStats(me, me.phyStats());
		}
	}
	
	/**
	 * Example class that recovers phyStats
	 */
	public static class ApplyRecoverPhyStats<T extends Affectable> implements EachApplicable<T>
	{
		@Override
		public void apply(T a) 
		{
			a.recoverPhyStats();
		}
	}
	
}
