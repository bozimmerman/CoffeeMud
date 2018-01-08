package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class DefaultRoomnumberSet implements RoomnumberSet
{
	@Override
	public String ID()
	{
		return "DefaultRoomnumberSet";
	}

	@Override
	public String name()
	{
		return ID();
	}

	public STreeMap<String,LongSet> root=new STreeMap<String,LongSet>();

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(final Exception e)
		{
			return new DefaultRoomnumberSet();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		final DefaultRoomnumberSet R=new DefaultRoomnumberSet();
		R.root=new STreeMap<String,LongSet>();
		LongSet CI=null;
		for(final String area : root.keySet())
		{
			CI=root.get(area);
			if(CI == null)
				R.root.put(area,null);
			else
				R.root.put(area,CI.copyOf());
		}
		return R;
	}

	@Override
	public synchronized void add(RoomnumberSet set)
	{
		LongSet his=null;
		LongSet mine=null;
		String arName=null;
		for(final Iterator<String> v=set.getAreaNames();v.hasNext();)
		{
			arName=v.next();
			his=set.getGrouper(arName);
			mine=set.getGrouper(arName);
			if(mine==null)
			{
				if(his!=null)
					mine=his.copyOf();
				root.put(arName.toUpperCase(),mine);
			}
			else
				mine.add(his);
		}
	}

	@Override
	public synchronized void remove(String str)
	{
		String areaName=str.toUpperCase().trim();
		if(areaName.length()==0)
			return;

		String theRest=null;
		long roomNum=-1;
		int x=areaName.indexOf('#');
		LongSet CI=null;
		if(x<=0)
		{
			CI=getGrouper(areaName);
			if(CI!=null)
			{
				root.remove(areaName);
				return;
			}
		}
		else
		if(x>0)
		{
			theRest=areaName.substring(x+1).trim();
			areaName=areaName.substring(0,x);
			CI=getGrouper(areaName);
			if(CI==null)
				return;
			x=theRest.indexOf("#(");
			if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
			{
				final int comma=theRest.indexOf(",",x);
				if(comma>0)
				{
					roomNum=(Long.parseLong(theRest.substring(0,x))<<30);
					roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
					roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
					if(roomNum<LongSet.INT_BITS)
						roomNum|=LongSet.OPTION_FLAG_LONG;
				}
			}
			else
			if(CMath.isInteger(theRest))
				roomNum=Integer.parseInt(theRest.substring(x+1).trim());
		}
		if(CI==null)
			return;
		CI.remove(Long.valueOf(roomNum));
		if(CI.size()==0)
			root.remove(areaName.toUpperCase());
	}

	@Override
	public int roomCountAllAreas()
	{
		int total=0;
		for(final LongSet CMI : root.values())
		{
			if(CMI==null)
				total++;
			else
				total+=CMI.size();
		}
		return total;
	}

	@Override
	public boolean isEmpty()
	{
		if(!root.isEmpty())
		{
			for(final LongSet CMI : root.values())
			{
				if((CMI!=null)&&(!CMI.isEmpty()))
					return false;
			}
		}
		return true;
	}

	@Override
	public int roomCount(String areaName)
	{
		final int x=areaName.indexOf('#');
		if(x>0)
			areaName=areaName.substring(0,x).toUpperCase();
		else
			areaName=areaName.toUpperCase();
		final LongSet CMI=root.get(areaName);
		if(CMI!=null)
			return CMI.size();
		return 0;
	}

	@Override
	public String random()
	{
		int total=roomCountAllAreas();
		if(total<=0)
			return null;
		final int which=CMLib.dice().roll(1,total,-1);
		total=0;
		String roomID=null;
		LongSet CMI = null;
		for(final Entry<String,LongSet> set : root.entrySet())
		{
			CMI=set.getValue();
			if(CMI==null)
				total++;
			else
				total+=CMI.size();
			if(which<total)
			{
				roomID=set.getKey();
				break;
			}
		}
		if(roomID==null)
			return null;
		if(CMI==null)
		{
			//Log.errOut("RNUMS","Unable to even select an integer group! Picked "+which+"/"+grandTotal);
			return roomID;
		}
		final long selection=CMI.getRandom();
		return convertRoomID(roomID,selection);
	}

	public int[] convertRoomID(long coded)
	{
		if(coded==-1)
			return null;
		final int[] ids=new int[3];
		ids[1]=-1;
		ids[2]=-1;
		if(coded<=LongSet.INT_BITS)
		{
			ids[0]=(int)coded;
			return ids;
		}
		long mask=0;
		for(int i=0;i<15;i++) mask=(mask<<1)+1;
		ids[2]=(int)(coded&mask);
		final long mask2=mask<<15;
		ids[1]=(int)((coded&mask2)>>15);
		mask|=mask2;
		mask=mask<<30;
		ids[0]=(int)(((coded&mask)>>30)&(LongSet.LONG_BITS-LongSet.OPTION_FLAG_LONG));
		return ids;
	}

	public String convertRoomID(String prefix, long coded)
	{
		if(coded==-1)
			return prefix;
		if(coded<LongSet.INT_BITS)
			return prefix+"#"+coded;
		long mask=0;
		for(int i=0;i<15;i++) mask=(mask<<1)+1;
		final long thirdID=coded&mask;
		final long mask2=mask<<15;
		final long secondID=(coded&mask2)>>15;
		mask|=mask2;
		mask=mask<<30;
		final long firstID=(((coded&mask)>>30)&(LongSet.LONG_BITS-LongSet.OPTION_FLAG_LONG));
		return prefix+"#"+firstID+"#("+secondID+","+thirdID+")";
	}

	@Override
	public Iterator<String> getAreaNames()
	{
		return root.keySet().iterator();
	}

	private boolean isGrouper(String areaName)
	{
		return root.containsKey(areaName.toUpperCase());
	}

	@Override
	public LongSet getGrouper(String areaName)
	{
		return root.get(areaName.toUpperCase());
	}

	@Override
	public boolean contains(String str)
	{
		if(str==null)
			return false;
		String theRest=null;
		long roomNum=0;
		final int origX=str.indexOf('#');
		int x=origX;
		if(x>0)
		{
			theRest=str.substring(x+1).trim();
			str=str.substring(0,x);
			x=theRest.indexOf("#(");
			if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
			{
				final int comma=theRest.indexOf(",",x);
				if(comma>0)
				{
					roomNum=Long.parseLong(theRest.substring(0,x))<<30;
					roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
					roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
					if(roomNum<LongSet.INT_BITS)
						roomNum|=LongSet.OPTION_FLAG_LONG;
				}
			}
			else
			if(CMath.isInteger(theRest))
				roomNum=Integer.parseInt(theRest.substring(x+1).trim());
		}

		final LongSet myGrouper=getGrouper(str);
		if((origX<0)&&(myGrouper==null)&&(isGrouper(str)))
			return true;
		if(myGrouper==null)
			return false;
		return myGrouper.contains(roomNum);
	}

	@Override
	public String xml()
	{
		final StringBuffer str=new StringBuffer("<AREAS>");
		for(final Entry<String,LongSet> set : root.entrySet())
		{
			str.append("<AREA><ID>"+set.getKey()+"</ID>");
			if(set.getValue()!=null)
				str.append("<NUMS>"+set.getValue().toString()+"</NUMS>");
			str.append("</AREA>");
		}
		return str.toString()+"</AREAS>";
	}

	@Override
	public void parseXML(String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0))
			return;
		final List<XMLLibrary.XMLTag> xV=CMLib.xml().getContentsFromPieces(V,"AREAS");
		root.clear();
		String ID=null;
		String NUMS=null;
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				final XMLTag ablk=xV.get(x);
				if((ablk.tag().equalsIgnoreCase("AREA"))&&(ablk.contents()!=null))
				{
					ID=ablk.getValFromPieces("ID").toUpperCase();
					NUMS=ablk.getValFromPieces("NUMS");
					if((NUMS!=null)&&(NUMS.length()>0))
						root.put(ID,new LongSet().parseString(NUMS));
					else
						root.put(ID,null);
				}
			}
		}
	}

	@Override
	public synchronized void add(String str)
	{
		String areaName=str.toUpperCase().trim();
		if(areaName.length()==0)
			return;

		String theRest=null;
		long roomNum=-1;
		int x=areaName.indexOf('#');
		if(x>0)
		{
			theRest=areaName.substring(x+1).trim();
			areaName=areaName.substring(0,x);
			x=theRest.indexOf("#(");
			if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
			{
				final int comma=theRest.indexOf(",",x);
				if(comma>0)
				{
					roomNum=(Long.parseLong(theRest.substring(0,x))<<30);
					roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
					roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
					if(roomNum<LongSet.INT_BITS)
						roomNum|=LongSet.OPTION_FLAG_LONG;
				}
			}
			else
			if(CMath.isInteger(theRest))
				roomNum=Integer.parseInt(theRest.substring(x+1).trim());
		}
		LongSet CI = root.get(areaName);
		if(CI==null)
		{
			if(roomNum>=0)
				CI=new LongSet();
			root.put(areaName,CI);
		}
		if((CI!=null)&&(roomNum>=0))
		{
			CI.add(Long.valueOf(roomNum));
		}
	}

	@Override 
	public Enumeration<String> getRoomIDs()
	{
		return new RoomnumberSetEnumeration();
	}

	private class RoomnumberSetEnumeration implements Enumeration<String>
	{
		Iterator<String> areaNames=null;
		String areaName=null;
		long[] nums=null;
		String nextID=null;
		int n=0;
		public RoomnumberSetEnumeration(){ areaNames=getAreaNames();}
		@Override
		public boolean hasMoreElements()
		{
			if(nextID==null)
				getNextID();
			return nextID!=null;
		}

		@Override
		public String nextElement()
		{
			if(nextID==null)
				getNextID();
			final String next=nextID;
			nextID=null;
			return next;
		}

		private void getNextID()
		{
			if(nums==null)
			{
				nextID=null;
				if((areaNames==null)||(!areaNames.hasNext()))
					return;
				areaName=areaNames.next();
				final LongSet grp=getGrouper(areaName);
				if(grp==null)
				{
					nextID=areaName;
					return;
				}
				nums=grp.getAllNumbers();
				n=0;
			}
			if((nums==null)||(n>=nums.length))
			{
				nums=null;
				getNextID();
				return;
			}
			final long num=nums[n++];
			nextID=convertRoomID(areaName,num);
		}
	}
}
