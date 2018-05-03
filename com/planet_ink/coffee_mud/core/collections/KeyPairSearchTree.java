package com.planet_ink.coffee_mud.core.collections;

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
 * A quick prefix lookup tree that takes a lot of memory,
 * but is worth it in speed.  It stores key/pairs, and
 * can be asked if a string starts with one of the keys.
 * If it does, it returns the value of the pair.
 * @author Bo Zimmerman
 *
 */
public class KeyPairSearchTree<V>
{
	protected KeyPairNode<String,V> root=new KeyPairNode<String,V>();

	protected class KeyPairNode<K,T>
	{
		@SuppressWarnings("unchecked")
		public KeyPairNode<K,T>[] limbs=new KeyPairNode[127];
		public Pair<K,T> value=null;
	}
	
	/**
	 * Store a new key/value pair
	 * @param key the key to add
	 * @param value the value of the key
	 */
	public void addEntry(String key, V value)
	{
		KeyPairNode<String,V> curr=root;
		for(int i=0;i<key.length();i++)
		{
			int c=key.charAt(i) % 127;
			if(curr.limbs[c]==null)
				curr.limbs[c]=new KeyPairNode<String,V>();
			curr=curr.limbs[c];
		}
		curr.value=new Pair<String,V>(key,value);
	}
	
	/**
	 * Retrieve teh value for the longest key that
	 * the given string starts with
	 * @param fullStr the string that might start with a key
	 * @return the value
	 */
	public Pair<String,V> findLongestValue(String fullStr)
	{
		Pair<String,V> lastValue=null;
		KeyPairNode<String,V> curr=root;
		for(int i=0;i<fullStr.length();i++)
		{
			if(curr.value != null)
				lastValue=curr.value;
			int c=fullStr.charAt(i) % 127;
			if(curr.limbs[c]==null)
				return lastValue;
			else
				curr=curr.limbs[c];
		}
		if((curr!=null)&&(curr.value != null))
			lastValue=curr.value;
		return lastValue;
	}
}
