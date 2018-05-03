package com.planet_ink.coffee_mud.core.collections;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public interface DoubleFilterer<K>
{
	public static enum Result
	{
		ALLOWED,
		REJECTED,
		NOTAPPLICABLE
	}
	
	public Result getFilterResult(K obj);

	@SuppressWarnings("rawtypes")
	public static final DoubleFilterer ANYTHING=new DoubleFilterer()
	{
		@Override
		public Result getFilterResult(Object obj)
		{
			return Result.NOTAPPLICABLE;
		}
	};
}
