package com.planet_ink.coffee_mud.core.collections;

import java.lang.reflect.Array;
import java.util.*;

/*
   Copyright 2015-2025 Bo Zimmerman

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
 * A set of long numbers, which can be added to, removed from, and enumerated.
 * The set is optimized for space, so that contiguous numbers are stored as
 * ranges.
 *
 * @author Bo Zimmerman
 */
public class LongSet implements Set<Long>
{
	/**  Whether this number denotes the beginning of a grouping.*/
	protected static final int 	NEXT_FLAG		= (Integer.MAX_VALUE/2)+1;
	/**  a mask for int number values */
	public static final int 	INT_BITS		= NEXT_FLAG-1;
	/**  Whether this number denotes the beginning of a grouping.*/
	protected static final long NEXT_FLAGL		= (Long.MAX_VALUE/2)+1;
	/**  a mask for int number values */
	public static final long 	LONG_BITS		= NEXT_FLAGL-1;
	/** a secondary mask value for marking numbers */
	public static final long 	OPTION_FLAG_LONG= (LongSet.LONG_BITS+1)/2;

	protected volatile int[]	intArray	= new int[0];
	protected volatile long[]	longArray	= new long[0];

	/**
	 * Returns a copy of this set
	 *
	 * @return a copy of this set
	 */
	public LongSet copyOf()
	{
		final LongSet g=new LongSet();
		synchronized(this)
		{
			g.intArray=intArray.clone();
			g.longArray=longArray.clone();
		}
		return g;
	}

	/**
	 * Returns true if this set contains the given number.
	 * @param x the number to look for
	 * @return true if this set contains the given number.
	 */
	public boolean contains(final long x)
	{
		if(x==-1)
			return true;
		if(x<=INT_BITS)
			return getIntIndex((int)x)>=0;
		return getLongIndex(x)>=0;
	}

	/**
	 * Returns the index of the given int number, or -1 if not found.
	 *
	 * @param x the number to look for
	 * @return the index of the given int number, or -1 if not found.
	 */
	public int getIntIndex(final int x)
	{
		int start=0;
		int end=intArray.length-1;
		int mid=-1;
		while(start<=end)
		{
			mid=(end+start)/2;
			if((mid>0)&&((intArray[mid-1]&NEXT_FLAG)>0))
			{
				mid--;
			}
			if(x<(intArray[mid]&INT_BITS))
				end=mid-1;
			else
			if((intArray[mid]&NEXT_FLAG)>0)
			{
				if(x>intArray[mid+1])
					start=mid+2;
				else
				if(x<=intArray[mid+1])
					return mid;
			}
			else
			if(x>intArray[mid])
				start=mid+1;
			else
				return mid;
		}
		return (-start)-1;
	}

	/**
	 * Returns the index of the given long number, or -1 if not found.
	 *
	 * @param y the number to look for
	 * @return the index of the given long number, or -1 if not found.
	 */
	public int getLongIndex(final long y)
	{
		int start=0;
		int end=longArray.length-1;
		int mid=-1;
		while(start<=end)
		{
			mid=(end+start)/2;
			if((mid>0)&&((longArray[mid-1]&NEXT_FLAGL)>0))
			{
				mid--;
			}
			if(y<(longArray[mid]&LONG_BITS))
				end=mid-1;
			else
			if((longArray[mid]&NEXT_FLAGL)>0)
			{
				if(y>longArray[mid+1])
					start=mid+2;
				else
				if(y<=longArray[mid+1])
					return mid;
			}
			else
			if(y>longArray[mid])
				start=mid+1;
			else
				return mid;
		}
		return (-start)-1;
	}

	/**
	 * Returns all of the int numbers in this set.
	 *
	 * @return all of the int numbers in this set.
	 */
	public int[] getAllIntNumbers()
	{
		int count=0;
		for(int i=0;i<intArray.length;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
			{
				count=count+1+(intArray[i+1]-(intArray[i]&INT_BITS));
				i++;
			}
			else
				count++;
		}
		final int[] nums=new int[count];
		int dex=0;
		for(int i=0;i<intArray.length;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
			{
				for(int x=(intArray[i]&INT_BITS);x<=intArray[i+1];x++)
					nums[dex++]=x;
				i++;
			}
			else
				nums[dex++]=intArray[i];
		}
		return nums;
	}

	/**
	 * Returns all of the long numbers in this set.
	 *
	 * @return all of the long numbers in this set.
	 */
	public long[] getAllNumbers()
	{
		final long[] nums=new long[size()];
		int dex=0;
		for(int i=0;i<intArray.length;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
			{
				for(int x=(intArray[i]&INT_BITS);x<=intArray[i+1];x++)
					nums[dex++]=x;
				i++;
			}
			else
				nums[dex++]=intArray[i];
		}
		for(int i=0;i<longArray.length;i++)
		{
			if((longArray[i]&NEXT_FLAGL)>0)
			{
				for(long y=(longArray[i]&LONG_BITS);y<=longArray[i+1];y++)
					nums[dex++]=y;
				i++;
			}
			else
				nums[dex++]=longArray[i];
		}
		return nums;
	}

	/**
	 * Returns whether this set is empty.
	 */
	@Override
	public boolean isEmpty()
	{
		return intArray.length == 0 && longArray.length == 0;
	}

	/**
	 * Returns the number of elements in this set.
	 * @return the number of elements in this set.
	 */
	@Override
	public int size()
	{
		int count=0;
		for(int i=0;i<intArray.length;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
				count=count+1+(intArray[i+1]-(intArray[i++]&INT_BITS));
			else
				count++;
		}
		for(int i=0;i<longArray.length;i++)
		{
			if((longArray[i]&NEXT_FLAGL)>0)
				count=count+1+(int)(longArray[i+1]-(longArray[i++]&LONG_BITS));
			else
				count++;
		}
		return count;
	}

	/**
	 * Grows the int array by the given amount at the given index.
	 *
	 * @param here the index to grow at
	 * @param amount the amount to grow by
	 */
	private void growIntArray(final int here, final int amount)
	{
		final int[] newis=new int[intArray.length+amount];
		for(int i=0;i<here;i++)
			newis[i]=intArray[i];
		for(int i=here;i<intArray.length;i++)
			newis[i+amount]=intArray[i];
		intArray=newis;
	}

	/**
	 * Grows the long array by the given amount at the given index.
	 *
	 * @param here the index to grow at
	 * @param amount the amount to grow by
	 */
	private void growLongArray(final int here, final int amount)
	{
		final long[] newis=new long[longArray.length+amount];
		for(int i=0;i<here;i++)
			newis[i]=longArray[i];
		for(int i=here;i<longArray.length;i++)
			newis[i+amount]=longArray[i];
		longArray=newis;
	}

	/**
	 * Removes the given number from this set.
	 *
	 * @param x the number to remove
	 * @return true if it was found and removed, false otherwise
	 */
	public synchronized boolean remove(final long x)
	{
		if(x==-1)
			return false;
		if(x<=INT_BITS)
			return removeInt((int)x);
		else
			return removeLong(x);
	}

	/**
	 * Removes all of the numbers in the given set from this set.
	 *
	 * @param grp the set of numbers to remove
	 * @return true if all were found and removed, false otherwise
	 */
	public synchronized boolean remove(final LongSet grp)
	{
		final long[] dely=grp.getAllNumbers();
		boolean found=true;
		for (final long element : dely)
		{
			if(!remove(element))
			{
				found=false;
			}
		}
		return found;
	}

	/**
	 * Shrinks the int array by the given amount at the given index.
	 *
	 * @param here the index to shrink at
	 * @param amount the amount to shrink by
	 */
	private void shrinkIntArray(final int here, final int amount)
	{
		final int[] newis=new int[intArray.length-amount];
		for(int i=0;i<here;i++)
			newis[i]=intArray[i];
		for(int i=here;i<newis.length;i++)
			newis[i]=intArray[i+amount];
		intArray=newis;
	}

	/**
	 * Shrinks the long array by the given amount at the given index.
	 *
	 * @param here the index to shrink at
	 * @param amount the amount to shrink by
	 */
	private void shrinkLongArray(final int here, final int amount)
	{
		final long[] newis=new long[longArray.length-amount];
		for(int i=0;i<here;i++)
			newis[i]=longArray[i];
		for(int i=here;i<newis.length;i++)
			newis[i]=longArray[i+amount];
		longArray=newis;
	}

	/**
	 * Checks the integrity of the int array.
	 *
	 * @return true if the int array is valid, false otherwise
	 */
	protected boolean checkIntArray()
	{
		for(int i=1;i<intArray.length;i++)
		{
			if((intArray[i]&INT_BITS) <= (intArray[i-1]&INT_BITS))
			{
				return false;
			}
			if(((intArray[i] & NEXT_FLAG)>0) && ((intArray[i-1] & NEXT_FLAG)>0))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks the integrity of the long array.
	 *
	 * @return true if the long array is valid, false otherwise
	 */
	protected boolean checkLongArray()
	{
		for(int i=1;i<longArray.length;i++)
		{
			if((longArray[i]&LONG_BITS) <= (longArray[i-1]&LONG_BITS))
			{
				return false;
			}
			if(((longArray[i] & NEXT_FLAGL)>0) && ((longArray[i-1] & NEXT_FLAGL)>0))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Consolodates the int array, combining any ranges that can be combined.
	 */
	private void consolodateInts()
	{
		for(int i=0;i<intArray.length-1;i++)
		{
			if(((intArray[i]&NEXT_FLAG)==0)
			&&(intArray[i]+1==(intArray[i+1]&INT_BITS)))
			{
				if((intArray[i+1]&NEXT_FLAG)>0)
				{
					if((i>0)&&((intArray[i-1]&NEXT_FLAG)>0))
					{
						shrinkIntArray(i,2);
						return;
					}
					shrinkIntArray(i,1);
					intArray[i]=((intArray[i]&INT_BITS)-1)|NEXT_FLAG;
					return;
				}
				if((i>0)&&((intArray[i-1]&NEXT_FLAG)>0))
				{
					shrinkIntArray(i+1,1);
					intArray[i]++;
					return;
				}
				intArray[i]=intArray[i]|NEXT_FLAG;
				return;
			}
		}
	}

	/**
	 * Consolodates the long array, combining any ranges that can be combined.
	 */
	private void consolodateLongs()
	{
		for(int i=0;i<longArray.length-1;i++)
		{
			if(((longArray[i]&NEXT_FLAGL)==0)
			&&(longArray[i]+1==(longArray[i+1]&LONG_BITS)))
			{
				if((longArray[i+1]&NEXT_FLAGL)>0)
				{
					if((i>0)&&((longArray[i-1]&NEXT_FLAGL)>0))
					{
						shrinkLongArray(i,2);
						return;
					}
					shrinkLongArray(i,1);
					longArray[i]=((longArray[i]&LONG_BITS)-1)|NEXT_FLAGL;
					return;
				}
				if((i>0)&&((longArray[i-1]&NEXT_FLAGL)>0))
				{
					shrinkLongArray(i+1,1);
					longArray[i]++;
					return;
				}
				longArray[i]=longArray[i]|NEXT_FLAGL;
				return;
			}
		}
	}

	/**
	 * Adds all of the numbers in the given set to this set.
	 *
	 * @param grp the set of numbers to add
	 * @return this set
	 */
	public LongSet add(final LongSet grp)
	{
		if(grp==null)
			return this;
		final long[] all=grp.getAllNumbers();
		for (final long element : all)
			add(element);
		return this;
	}

	/**
	 * Adds the given number to this set.
	 *
	 * @param x the number to add
	 * @return this set
	 */
	public synchronized LongSet add(final long x)
	{
		if(x==-1)
			return null;
		if(x<=INT_BITS)
			addInt((int)x);
		else
			addLong(x);
		return this;
	}

	/**
	 * Adds all of the numbers in the given range to this set, inclusive.
	 *
	 * @param from the number to start at
	 * @param to the number to end at
	 * @return this set
	 */
	public synchronized LongSet add(final long from, final long to)
	{
		if((from==-1)||(to<from))
			return null;
		if(to<=INT_BITS)
			addIntRange((int)from,(int)to);
		else
			addLongRange(from, to);
		return this;
	}

	/**
	 * Adds all of the numbers in the given range to this set, inclusive.
	 *
	 * @param x1 the number to start at
	 * @param x2 the number to end at
	 */
	private void addLongRange(final long x1, final long x2)
	{
		if(x1 == x2)
		{
			addLong(x1);
			return;
		}

		if(x2 < x1)
		{
			throw new IllegalArgumentException("x2 < x1");
		}

		int index1=getLongIndex(x1);
		int index2=getLongIndex(x2);
		if((index2>=0)&&(index2==index1))
			return;
		if(index1>=0)
		{
			if((longArray[index1]&NEXT_FLAGL)==0)
			{
				growLongArray(index1+1,1);
				longArray[index1]=longArray[index1]|NEXT_FLAGL;
			}
		}
		else
		if(index1<0)
		{
			index1=(index1+1)*-1;
			int need=2;
			if(index1<longArray.length)
			{
				if(index1>=0)
				{
					if(x2<(longArray[index1]&LONG_BITS))
					{
						growLongArray(index1,2);
						longArray[index1]=x1|NEXT_FLAGL;
						longArray[index1+1]=x2;
						return;
					}
					else
					if(((longArray[index1]&NEXT_FLAGL)>0)&&((longArray[index1]&LONG_BITS)>=x1))
					{
						need=0;
						longArray[index1]=x1|NEXT_FLAGL;
					}
					else
					if((x2>=(longArray[index1]&LONG_BITS))&&((longArray[index1]&NEXT_FLAGL)==0))
					{
						need=1;
					}
				}
			}
			if(index1>=longArray.length)
			{
				index1=longArray.length;
				growLongArray(longArray.length,need);
				longArray[index1]=x1|NEXT_FLAGL;
				longArray[index1+1]=x2;
				return;
			}
			else
			if(need>0)
			{
				growLongArray(index1,need);
				longArray[index1]=x1|NEXT_FLAGL;
			}
		}

		index2=index1+1;
		while((index2<longArray.length-1)&&((longArray[index2+1]&LONG_BITS)<=x2))
			index2++;
		if(index2>index1+1)
		{
			if((longArray[index2]&NEXT_FLAGL)>0)
				shrinkLongArray(index1+1,index2-index1);
			else
				shrinkLongArray(index1+1,index2-index1-1);
		}
		if(x2>longArray[index1+1])
			longArray[index1+1]=x2;
		consolodateLongs();
	}

	/**
	 * Adds the given number to this set.
	 *
	 * @param x the number to add
	 */
	private void addLong(final long x)
	{
		int index=getLongIndex(x);
		if(index>=0)
			return;
		index=(index+1)*-1;
		if((index>0)&&((longArray[index-1]&NEXT_FLAGL)>0))
			index--;
		int end=index+2;
		if(end>longArray.length)
			end=longArray.length;
		for(int i=index;i<end;i++)
		{
			if((longArray[i]&NEXT_FLAGL)>0)
			{
				if((x>=(longArray[i]&LONG_BITS))&&(x<=longArray[i+1]))
					return;
				if(x==((longArray[i]&LONG_BITS)-1))
				{
					longArray[i]=x|NEXT_FLAGL;
					consolodateLongs();
					return;
				}
				if(x==(longArray[i+1]+1))
				{
					longArray[i+1]=x;
					consolodateLongs();
					return;
				}
				if(x<(longArray[i]&LONG_BITS))
				{
					growLongArray(i,1);
					longArray[i]=x;
					consolodateLongs();
					return;
				}
				i++;
			}
			else
			if(x==longArray[i])
				return;
			else
			if(x==longArray[i]-1)
			{
				growLongArray(i,1);
				longArray[i]=x|NEXT_FLAGL;
				consolodateLongs();
				return;
			}
			else
			if(x==longArray[i]+1)
			{
				growLongArray(i+1,1);
				longArray[i]=longArray[i]|NEXT_FLAGL;
				longArray[i+1]=x;
				consolodateLongs();
				return;
			}
			else
			if(x<longArray[i])
			{
				growLongArray(i,1);
				longArray[i]=x;
				consolodateLongs();
				return;
			}
		}
		growLongArray(longArray.length,1);
		longArray[longArray.length-1]=x;
		consolodateLongs();
		return;
	}

	/**
	 * Adds the given number to this set.
	 *
	 * @param x the number to add
	 */
	private void addInt(final int x)
	{
		int index=getIntIndex(x);
		if(index>=0)
			return;
		index=(index+1)*-1;
		if((index>0)&&((intArray[index-1]&NEXT_FLAG)>0))
			index--;
		int end=index+2;
		if(end>intArray.length)
			end=intArray.length;
		for(int i=index;i<end;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
			{
				if((x>=(intArray[i]&INT_BITS))&&(x<=intArray[i+1]))
					return;
				if(x==((intArray[i]&INT_BITS)-1))
				{
					intArray[i]=x|NEXT_FLAG;
					consolodateInts();
					return;
				}
				if(x==(intArray[i+1]+1))
				{
					intArray[i+1]=x;
					consolodateInts();
					return;
				}
				if(x<(intArray[i]&INT_BITS))
				{
					growIntArray(i,1);
					intArray[i]=x;
					consolodateInts();
					return;
				}
				i++;
			}
			else
			if(x==intArray[i])
				return;
			else
			if(x==intArray[i]-1)
			{
				growIntArray(i,1);
				intArray[i]=x|NEXT_FLAG;
				consolodateInts();
				return;
			}
			else
			if(x==intArray[i]+1)
			{
				growIntArray(i+1,1);
				intArray[i]=intArray[i]|NEXT_FLAG;
				intArray[i+1]=x;
				consolodateInts();
				return;
			}
			else
			if(x<intArray[i])
			{
				growIntArray(i,1);
				intArray[i]=x;
				consolodateInts();
				return;
			}
		}
		growIntArray(intArray.length,1);
		intArray[intArray.length-1]=x;
		consolodateInts();
		return;
	}

	/**
	 * Removes the given long number from this set.
	 *
	 * @param x the number to remove
	 * @return true if it was found and removed, false otherwise
	 */
	private boolean removeLong(final long x)
	{
		int index=getLongIndex(x);
		if(index<0)
			return false;
		if((index>0)&&((longArray[index-1]&NEXT_FLAGL)>0))
			index--;
		int end=index+2;
		if(end>longArray.length)
			end=longArray.length;
		for(int i=index;i<end;i++)
		{
			if((longArray[i]&NEXT_FLAGL)>0)
			{
				if(x<(longArray[i]&LONG_BITS))
					return false;
				if(x==(longArray[i]&LONG_BITS))
				{
					longArray[i]++;
					if((x+1)==longArray[i+1])
						shrinkLongArray(i,1);
					return true;
				}
				if(x==longArray[i+1])
				{
					longArray[i+1]--;
					if((x-1)==(longArray[i]&LONG_BITS))
						shrinkLongArray(i,1);
					return true;
				}
				if(x<longArray[i+1])
				{
					if(x==((longArray[i]&LONG_BITS)+1))
					{
						growLongArray(i+1,1);
						longArray[i]=(longArray[i]&LONG_BITS);
						longArray[i+1]=(x+1|NEXT_FLAGL);
					}
					else
					if(x==longArray[i+1]-1)
					{
						growLongArray(i+1,1);
						longArray[i+1]=x-1;
					}
					else
					{
						growLongArray(i+1,2);
						longArray[i+1]=x-1;
						longArray[i+2]=(x+1)|NEXT_FLAGL;
					}
					return true;
				}
				i++;
			}
			else
			if(x<longArray[i])
				return false;
			else
			if(x==longArray[i])
			{
				shrinkLongArray(i,1);
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the given int number from this set.
	 *
	 * @param x the number to remove
	 * @return true if it was found and removed, false otherwise
	 */
	private boolean removeInt(final int x)
	{
		int index=getIntIndex(x);
		if(index<0)
			return false;
		if((index>0)&&((intArray[index-1]&NEXT_FLAG)>0))
			index--;
		int end=index+2;
		if(end>intArray.length)
			end=intArray.length;
		for(int i=index;i<end;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
			{
				if(x<(intArray[i]&INT_BITS))
					return false;
				if(x==(intArray[i]&INT_BITS))
				{
					intArray[i]++;
					if((x+1)==intArray[i+1])
						shrinkIntArray(i,1);
					return true;
				}
				if(x==intArray[i+1])
				{
					intArray[i+1]--;
					if((x-1)==(intArray[i]&INT_BITS))
						shrinkIntArray(i,1);
					return true;
				}
				if(x<intArray[i+1])
				{
					if(x==((intArray[i]&INT_BITS)+1))
					{
						growIntArray(i+1,1);
						intArray[i]=(intArray[i]&INT_BITS);
						intArray[i+1]=(x+1|NEXT_FLAG);
					}
					else
					if(x==intArray[i+1]-1)
					{
						growIntArray(i+1,1);
						intArray[i+1]=x-1;
					}
					else
					{
						growIntArray(i+1,2);
						intArray[i+1]=x-1;
						intArray[i+2]=(x+1)|NEXT_FLAG;
					}
					return true;
				}
				i++;
			}
			else
			if(x<intArray[i])
				return false;
			else
			if(x==intArray[i])
			{
				shrinkIntArray(i,1);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds all of the numbers in the given range to this set, inclusive.
	 *
	 * @param x1 the number to start at
	 * @param x2 the number to end at
	 */
	private void addIntRange(final int x1, final int x2)
	{
		if(x1 == x2)
		{
			addInt(x1);
			return;
		}

		if(x2 < x1)
		{
			throw new IllegalArgumentException("x2 < x1");
		}

		int index1=getIntIndex(x1);
		int index2=getIntIndex(x2);
		if((index2>=0)&&(index2==index1))
			return;
		if(index1>=0)
		{
			if((intArray[index1]&NEXT_FLAG)==0)
			{
				growIntArray(index1+1,1);
				intArray[index1]=intArray[index1]|NEXT_FLAG;
			}
		}
		else
		if(index1<0)
		{
			index1=(index1+1)*-1;
			int need=2;
			if(index1<intArray.length)
			{
				if(index1>=0)
				{
					if(x2<(intArray[index1]&INT_BITS))
					{
						growIntArray(index1,2);
						intArray[index1]=x1|NEXT_FLAG;
						intArray[index1+1]=x2;
						return;
					}
					else
					if(((intArray[index1]&NEXT_FLAG)>0)&&((intArray[index1]&INT_BITS)>=x1))
					{
						need=0;
						intArray[index1]=x1|NEXT_FLAG;
					}
					else
					if((x2>=(intArray[index1]&INT_BITS))&&((intArray[index1]&NEXT_FLAG)==0))
					{
						need=1;
					}
				}
			}
			if(index1>=intArray.length)
			{
				index1=intArray.length;
				growIntArray(intArray.length,need);
				intArray[index1]=x1|NEXT_FLAG;
				intArray[index1+1]=x2;
				return;
			}
			else
			if(need>0)
			{
				growIntArray(index1,need);
				intArray[index1]=x1|NEXT_FLAG;
			}
		}

		index2=index1+1;
		while((index2<intArray.length-1)&&((intArray[index2+1]&INT_BITS)<=x2))
			index2++;
		if(index2>index1+1)
		{
			if((intArray[index2]&NEXT_FLAG)>0)
				shrinkIntArray(index1+1,index2-index1);
			else
				shrinkIntArray(index1+1,index2-index1-1);
		}
		if(x2>intArray[index1+1])
			intArray[index1+1]=x2;
		consolodateInts();
	}

	/**
	 * A test method for this class.
	 *
	 * @param g the group to test
	 * @param used the set of numbers that should be in the group
	 * @param span the maximum number that should be in the group
	 * @return true if the group contains exactly the numbers in used, false
	 *         otherwise
	 */
	private static boolean checkList(final LongSet g, final Set<Integer> used, final int span)
	{
		if(!g.checkIntArray())
		{
			return false;
		}
		for(final Integer l1 : used)
		{
			if(!g.contains(l1.intValue()))
			{
				return false;
			}
		}
		for(int i=1;i<=span;i++)
		{
			final Integer I=Integer.valueOf(i);
			if((!used.contains(I))&&(g.contains(I.intValue())))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * A test method for this class.
	 *
	 * @param args none
	 */
	protected static void test(final String[] args)
	{
		final Random r=new Random(System.currentTimeMillis());
		final int span=200;
		int bigDiff=0;
		for(int it=0;it<50000;it++)
		{
			final Set<Integer> l=new TreeSet<Integer>();
			final LongSet g=new LongSet();
			for(int i=0;i<200;i++)
			{
				if(r.nextBoolean())
				{
					final int n=r.nextInt(3)+1;
					final int x1=r.nextInt(span)+1;
					final LongSet gc=g.copyOf();
					g.addIntRange(x1, x1+n);
					for(int x=x1;x<=x1+n;x++)
						l.add(Integer.valueOf(x));
					if(!checkList(g,l,span))
					{
						System.err.println("Fail !!");
						gc.addIntRange(x1,x1+n);
					}
				}
				else
				{
					final int x1=r.nextInt(span)+1;
					final LongSet gc=g.copyOf();
					g.addInt(x1);
					l.add(Integer.valueOf(x1));
					if(!checkList(g,l,span))
					{
						System.err.println("Fail !!");
						gc.addInt(x1);
					}
				}
			}
			if((g.size()-g.intArray.length)>bigDiff)
			{
				bigDiff=g.size()-g.intArray.length;
			}
		}
	}

	/**
	 * Adds the given number to this set.
	 *
	 * @param e the number to add
	 * @return true if the number was added, false if it was already present
	 */
	@Override
	public boolean add(final Long e)
	{
		return (e != null) && add(e.longValue()) != null;
	}

	/**
	 * Adds all of the numbers in the given collection to this set.
	 *
	 * @param c the collection of Longs to add
	 * @return true if all the numbers were added, false if any were already
	 *         present
	 */
	@Override
	public boolean addAll(final Collection<? extends Long> c)
	{
		if(c != null)
		{
			for(final Long L : c)
			{
				if(!add(L))
					return false;
			}
		}
		return true;
	}

	/**
	 * Removes all of the numbers from this set.
	 */
	@Override
	public void clear()
	{
		synchronized(this)
		{
			intArray=new int[0];
			longArray=new long[0];
		}
	}

	/**
	 * Converts the given object to a long value if it is a Number.
	 *
	 * @param o the object to convert
	 * @return the long value of the object
	 * @throws ClassCastException if the object is not a Number
	 */
	private long valueOf(final Object o)
	{
		if(o instanceof Long)
			return ((Long)o).longValue();
		else
		if(o instanceof Integer)
			return ((Integer)o).longValue();
		else
		if(o instanceof Double)
			return ((Double)o).longValue();
		else
		if(o instanceof Float)
			return ((Float)o).longValue();
		else
		if(o instanceof Short)
			return ((Short)o).longValue();
		else
		if(o instanceof Byte)
			return ((Byte)o).longValue();
		throw new ClassCastException("Not a number: "+o.getClass().getCanonicalName());
	}

	/**
	 * Returns whether this set contains the given number.
	 *
	 * @param o the number to check for
	 * @return true if the number is in this set, false otherwise
	 */
	@Override
	public boolean contains(final Object o)
	{
		return this.contains(valueOf(o));
	}

	/**
	 * Returns whether this set contains all of the numbers in the given
	 * collection.
	 *
	 * @param c the collection of Longs to check for
	 * @return true if all the numbers are in this set, false otherwise
	 */
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		if(c!=null)
		{
			for(final Object o : c)
			{
				if(!contains(o))
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns an iterator over the numbers in this set.
	 *
	 * @return an iterator over the numbers in this set
	 */
	@Override
	public Iterator<Long> iterator()
	{
		final long[] all=this.getAllNumbers();
		final LongSet me=this;
		return new Iterator<Long>()
		{
			private int index=0;

			@Override
			public boolean hasNext()
			{
				return index < all.length;
			}

			@Override
			public Long next()
			{
				if(hasNext())
				{
					return Long.valueOf(all[index++]);
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove()
			{
				if(index<all.length)
				{
					me.remove(all[index]);
				}
			}
		};
	}

	/**
	 * Removes the given number from this set.
	 *
	 * @param o the number to remove
	 * @return true if it was found and removed, false otherwise
	 */
	@Override
	public boolean remove(final Object o)
	{
		return this.remove(valueOf(o));
	}

	/**
	 * Removes all of the numbers in the given collection from this set.
	 *
	 * @param c the collection of Longs to remove
	 * @return true if all were found and removed, false otherwise
	 */
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean found = true;
		if(c!=null)
		{
			for(final Object o : c)
			{
				if(!remove(o))
					found=false;
			}
		}
		return found;
	}

	/**
	 * Retains only the numbers in this set that are also in the given
	 * collection.
	 *
	 * @param c the collection of Longs to retain
	 * @return true if any numbers were removed, false otherwise
	 */
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		final Object[] os=toArray();
		boolean foundAny=false;
		for(final Object o : os)
		{
			if(!c.contains(o))
			{
				foundAny = remove(o) || foundAny;
			}
		}
		return foundAny;
	}

	/**
	 * Constructs a Long array containing all of the numbers in this set.
	 *
	 * @return a Long array containing all of the numbers in this set
	 */
	@Override
	public Object[] toArray()
	{
		final Long[] all = new Long[size()];
		int index=0;
		for(final Iterator<Long> i=iterator();i.hasNext();)
		{
			all[index++] = i.next();
		}
		return all;
	}

	/**
	 * A helper method for toArray(T[]).
	 *
	 * @param r the array to start with
	 * @param it the iterator to get more elements from
	 * @return an array containing all of the elements
	 */
	// from abstractcollection.java
	@SuppressWarnings("unchecked")
	private static <T> T[] finishToArray(T[] r, final Iterator<?> it)
	{
		int i = r.length;
		while (it.hasNext())
		{
			final int cap = r.length;
			if (i == cap)
			{
				final int newCap = cap + (cap >> 1) + 1;
				r = Arrays.copyOf(r, newCap);
			}
			r[i++] = (T)it.next();
		}
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}

	/**
	 * Constructs an array containing all of the numbers in this set.
	 *
	 * @param a the array to start with
	 * @return an array containing all of the numbers in this set
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a)
	{
		final int size = size();
		final T[] r = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		final Iterator<Long> it = iterator();

		for (int i = 0; i < r.length; i++)
		{
			if (!it.hasNext())
			{
				if (a != r)
					return Arrays.copyOf(r, i);
				r[i] = null; // null-terminate
				return r;
			}
			r[i] = (T) it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}

	/**
	 * Convenience method to return a string representation of this set.
	 *
	 * @return a string representation of this set
	 */
	@Override
	public String toString()
	{
		final StringBuilder str=new StringBuilder("{");
		for(final int x : intArray)
		{
			str.append(x).append(",");
		}
		if(str.charAt(str.length()-1)==',')
		{
			str.setCharAt(str.length()-1, '}');
		}
		else
		{
			str.append('}');
		}
		str.append(",{");
		for(final long x : longArray)
		{
			str.append(x).append(",");
		}
		if(str.charAt(str.length()-1)==',')
		{
			str.setCharAt(str.length()-1, '}');
		}
		else
		{
			str.append('}');
		}
		return str.toString();
	}

	/**
	 * Parses a string representation of a LongSet. The string must be in the
	 * format produced by toString().
	 *
	 * @param txt the string to parse
	 * @return this set, or null if the string could not be parsed
	 */
	public LongSet parseString(String txt)
	{
		intArray=new int[0];
		longArray=new long[0];
		txt=txt.trim();
		if(txt.length()==0)
			return null;
		if((!txt.startsWith("{"))&&(!txt.endsWith("}")))
			return null;
		final int x=txt.indexOf("},{");
		if(x<0)
			return null;
		if(x>1)
		{
			final String[] strs=txt.substring(1,x).split(",");
			intArray=new int[strs.length];
			for(int v=0;v<strs.length;v++)
			{
				intArray[v]=Integer.parseInt(strs[v].trim());
			}
		}
		if(x+3<txt.length()-1)
		{
			final String[] strs=txt.substring(x+3,txt.length()-1).split(",");
			longArray=new long[strs.length];
			for(int v=0;v<strs.length;v++)
			{
				longArray[v]=Long.parseLong(strs[v].trim());
			}
		}
		return this;
	}

	/**
	 * Returns a random number from this set.
	 *
	 * @return a random number from this set, or -1 if the set is empty
	 */
	public long getRandom()
	{
		final Random r=new Random();
		final int roomCount=size();
		if(roomCount<=0)
			return -1;
		final int which=r.nextInt(roomCount);
		long count=0;
		for(int i=0;i<intArray.length;i++)
		{
			if((intArray[i]&NEXT_FLAG)>0)
				count=count+1+(intArray[i+1]-(intArray[i]&INT_BITS));
			else
				count++;
			if(which<count)
			{
				if((intArray[i]&NEXT_FLAG)>0)
					return (intArray[i+1]-(count-which))+1;
				return intArray[i]&INT_BITS;
			}
		}
		for(int i=0;i<longArray.length;i++)
		{
			if((longArray[i]&NEXT_FLAGL)>0)
				count=count+1+(int)(longArray[i+1]-(longArray[i]&LONG_BITS));
			else
				count++;
			if(which<count)
			{
				if((longArray[i]&NEXT_FLAGL)>0)
					return (longArray[i+1]-(count-which))+1;
				return longArray[i]&LONG_BITS;
			}
		}
		return -1;
	}
}
