package com.planet_ink.coffee_mud.core.collections;

/*
   Copyright 2015-2018 Bo Zimmerman

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
 * If it does, it returns the value of the pair.  This
 * version supports wildcard characters ? and *.
 * @author Bo Zimmerman
 *
 */
public class KeyPairWildSearchTree<V> extends KeyPairSearchTree<V>
{
	/**
	 * Retrieve the value for the longest key that
	 * the given string starts with, with wildcard support
	 * @param fullStr the string that might start with a key
	 * @param startNode the search node to start with
	 * @param index the index into the string to start with, and ends with
	 * @return the value
	 */
	protected Pair<String,V> findLongestValueWithWildcards(final String fullStr, KeyPairNode<String,V> startNode, int[] index)
	{
		Pair<String,V> lastValue=null;
		KeyPairNode<String,V> curr=startNode;
		for(int i=index[0];i<fullStr.length();i++)
		{
			if(curr.value != null)
			{
				lastValue=curr.value;
				index[0] = i;
			}
			int c=fullStr.charAt(i) % 127;
			if(curr.limbs[c]==null)
			{
				if(i<fullStr.length()-1)
				{
					if(curr.limbs['?'] != null)
					{
						curr=curr.limbs['?'];
						continue;
					}
					if(curr.limbs['*'] != null)
					{
						curr=curr.limbs['*'];
						while(i<fullStr.length())
						{
							if(curr.limbs[fullStr.charAt(i) % 127] != null)
							{
								final int[] startIndex=new int[]{i};
								final Pair<String,V> newValue = findLongestValueWithWildcards(fullStr,curr,startIndex);
								if(newValue != null)
								{
									index[0] = startIndex[0];
									return new Pair<String,V>(fullStr.substring(0,index[0]),newValue.second);
								}
							}
							i++;
						}
					}
				}
				return lastValue;
			}
			else
				curr=curr.limbs[c];
		}
		if((curr!=null)&&(curr.value != null))
		{
			index[0] = fullStr.length();
			lastValue=curr.value;
		}
		return lastValue;
	}
	
	/**
	 * Retrieve the value for the longest key that
	 * the given string starts with
	 * @param fullStr the string that might start with a key
	 * @return the value
	 */
	@Override
    public Pair<String,V> findLongestValue(final String fullStr)
	{
		return findLongestValueWithWildcards(fullStr,root,new int[]{0});
	}
}
