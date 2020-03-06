package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class ConvertingSet<L,K> extends ConvertingCollection<L,K> implements Set<K>
{
	public ConvertingSet(final Collection<L> l, final Converter<L, K> conv)
	{
		super(l,conv);
	}
}
