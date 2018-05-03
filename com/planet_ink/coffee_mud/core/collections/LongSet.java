package com.planet_ink.coffee_mud.core.collections;

import java.lang.reflect.Array;
import java.util.*;

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

	protected volatile int[] intArray=new int[0];
	protected volatile long[] longArray=new long[0];

	public LongSet copyOf()
	{
		LongSet g=new LongSet();
		synchronized(this)
		{
			g.intArray=intArray.clone();
			g.longArray=longArray.clone();
		}
		return g;
	}
	
	public boolean contains(long x)
	{
		if(x==-1)
			return true;
		if(x<=INT_BITS)
			return getIntIndex((int)x)>=0;
		return getLongIndex(x)>=0;
	}

	public int getIntIndex(int x)
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

	public int getLongIndex(long y)
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

	@Override
	public boolean isEmpty()
	{
		return intArray.length == 0 && longArray.length == 0;
	}

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

	private void growIntArray(int here, int amount)
	{
		final int[] newis=new int[intArray.length+amount];
		for(int i=0;i<here;i++)
			newis[i]=intArray[i];
		for(int i=here;i<intArray.length;i++)
			newis[i+amount]=intArray[i];
		intArray=newis;
	}

	private void growLongArray(int here, int amount)
	{
		final long[] newis=new long[longArray.length+amount];
		for(int i=0;i<here;i++)
			newis[i]=longArray[i];
		for(int i=here;i<longArray.length;i++)
			newis[i+amount]=longArray[i];
		longArray=newis;
	}

	public synchronized boolean remove(long x)
	{
		if(x==-1)
			return false;
		if(x<=INT_BITS)
			return removeInt((int)x);
		else
			return removeLong(x);
	}
	
	public synchronized boolean remove(LongSet grp)
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
	
	private void shrinkIntArray(int here, int amount)
	{
		final int[] newis=new int[intArray.length-amount];
		for(int i=0;i<here;i++)
			newis[i]=intArray[i];
		for(int i=here;i<newis.length;i++)
			newis[i]=intArray[i+amount];
		intArray=newis;
	}

	private void shrinkLongArray(int here, int amount)
	{
		final long[] newis=new long[longArray.length-amount];
		for(int i=0;i<here;i++)
			newis[i]=longArray[i];
		for(int i=here;i<newis.length;i++)
			newis[i]=longArray[i+amount];
		longArray=newis;
	}

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

	public LongSet add(LongSet grp)
	{
		if(grp==null)
			return this;
		final long[] all=grp.getAllNumbers();
		for (final long element : all)
			add(element);
		return this;
	}

	public synchronized LongSet add(long x)
	{
		if(x==-1)
			return null;
		if(x<=INT_BITS)
			addInt((int)x);
		else
			addLong(x);
		return this;
	}

	public synchronized LongSet add(long from, long to)
	{
		if((from==-1)||(to<from))
			return null;
		if(to<=INT_BITS)
			addIntRange((int)from,(int)to);
		else
			addLongRange(from, to);
		return this;
	}

	private void addLongRange(long x1, long x2)
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
	
	private void addLong(long x)
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
	
	private void addInt(int x)
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

	private boolean removeLong(long x)
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

	private boolean removeInt(int x)
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
	
	private void addIntRange(int x1, int x2)
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
	
	private static boolean checkList(LongSet g, Set<Integer> used, int span)
	{
		if(!g.checkIntArray())
		{
			return false;
		}
		for(Integer l1 : used)
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

	protected static void test(String[] args)
	{
		Random r=new Random(System.currentTimeMillis());
		int span=200;
		int bigDiff=0;
		for(int it=0;it<50000;it++)
		{
			Set<Integer> l=new TreeSet<Integer>();
			LongSet g=new LongSet();
			for(int i=0;i<200;i++)
			{
				if(r.nextBoolean())
				{
					int n=r.nextInt(3)+1;
					int x1=r.nextInt(span)+1;
					LongSet gc=g.copyOf();
					g.addIntRange(x1, x1+n);
					for(int x=x1;x<=x1+n;x++)
						l.add(new Integer(x));
					if(!checkList(g,l,span))
					{
						System.err.println("Fail !!");
						gc.addIntRange(x1,x1+n);
					}
				}
				else
				{
					int x1=r.nextInt(span)+1;
					LongSet gc=g.copyOf();
					g.addInt(x1);
					l.add(new Integer(x1));
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

	@Override
	public boolean add(Long e) 
	{
		return (e != null) && add(e.longValue()) != null;
	}

	@Override
	public boolean addAll(Collection<? extends Long> c) 
	{
		if(c != null)
		{
			for(Long L : c)
			{
				if(!add(L))
					return false;
			}
		}
		return true;
	}

	@Override
	public void clear() 
	{
		synchronized(this)
		{
			intArray=new int[0];
			longArray=new long[0];
		}
	}

	private long valueOf(Object o)
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
	
	@Override
	public boolean contains(Object o) 
	{
		return this.contains(valueOf(o));
	}

	@Override
	public boolean containsAll(Collection<?> c) 
	{
		if(c!=null)
		{
			for(Object o : c)
			{
				if(!contains(o))
					return false;
			}
		}
		return true;
	}

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

	@Override
	public boolean remove(Object o) 
	{
		return this.remove(valueOf(o));
	}

	@Override
	public boolean removeAll(Collection<?> c) 
	{
		boolean found = true;
		if(c!=null)
		{
			for(Object o : c)
			{
				if(!remove(o))
					found=false;
			}
		}
		return found;
	}

	@Override
	public boolean retainAll(Collection<?> c) 
	{
		Object[] os=toArray();
		boolean foundAny=false;
		for(Object o : os)
		{
			if(!c.contains(o))
			{
				foundAny = remove(o) || foundAny;
			}
		}
		return foundAny;
	}

	@Override
	public Object[] toArray() 
	{
		final Long[] all = new Long[size()];
		int index=0;
		for(Iterator<Long> i=iterator();i.hasNext();)
		{
			all[index++] = i.next();
		}
		return all;
	}

	// from abstractcollection.java
	@SuppressWarnings("unchecked")
	private static <T> T[] finishToArray(T[] r, Iterator<?> it) 
	{
		int i = r.length;
		while (it.hasNext()) 
		{
			int cap = r.length;
			if (i == cap) 
			{
				int newCap = cap + (cap >> 1) + 1;
				r = Arrays.copyOf(r, newCap);
			}
			r[i++] = (T)it.next();
		}
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}

	@SuppressWarnings("unchecked")

	@Override
	public <T> T[] toArray(T[] a) 
	{
		int size = size();
		T[] r = a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		Iterator<Long> it = iterator();
		
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
	
	@Override
	public String toString()
	{
		StringBuilder str=new StringBuilder("{");
		for(int x : intArray)
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
		for(long x : longArray)
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
