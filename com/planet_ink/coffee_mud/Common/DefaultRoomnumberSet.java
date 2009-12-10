package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
Copyright 2007-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class DefaultRoomnumberSet implements RoomnumberSet
{
    public DVector root=new DVector(2);
    public String ID(){return "DefaultRoomnumberSet";}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultRoomnumberSet();}}
    public void initializeClass(){}
    public CMObject copyOf()
    {
        DefaultRoomnumberSet R=new DefaultRoomnumberSet();
        R.root=new DVector(2);
        CMIntegerGrouper CI=null;
        for(int r=0;r<root.size();r++)
        {
        	CI=((CMIntegerGrouper)root.elementAt(r,2));
            R.root.addElement(root.elementAt(r,1),CI==null?null:CI.copyOf());
        }
        return R;
    }
    public void add(RoomnumberSet set)
    {
    	CMIntegerGrouper his=null;
    	CMIntegerGrouper mine=null;
    	String arName=null;
    	for(Enumeration v=set.getAreaNames();v.hasMoreElements();)
    	{
    		arName=(String)v.nextElement();
    		his=set.getGrouper(arName);
    		mine=set.getGrouper(arName);
    		if(mine==null)
    		{
    			if(his!=null)
	    			mine=(CMIntegerGrouper)his.copyOf();
    			root.addElement(arName.toUpperCase(),mine);
    		}
    		else
    			mine.add(his);
    	}
    }
    
    public void remove(String str)
    {
        String areaName=str.toUpperCase().trim();
        if(areaName.length()==0) return;
        
        String theRest=null;
        long roomNum=-1;
        int x=areaName.indexOf("#");
        CMIntegerGrouper CI=null;
        if(x<=0)
        	CI=getGrouper(areaName);
        else
        if(x>0)
        {
            theRest=areaName.substring(x+1).trim();
            areaName=areaName.substring(0,x);
        	CI=getGrouper(areaName);
        	if(CI==null) return;
            x=theRest.indexOf("#(");
            if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
            {
                int comma=theRest.indexOf(",",x);
                if(comma>0)
                {
                    roomNum=(Long.parseLong(theRest.substring(0,x))<<30);
                    roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
                    roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
                    if(roomNum<CMIntegerGrouper.NEXT_BITS) roomNum|=CMIntegerGrouper.GRID_FLAGL;
                }
            }
            else
            if(CMath.isInteger(theRest))
                roomNum=Integer.parseInt(theRest.substring(x+1).trim());
        }
        if(CI==null) return;
        CI.remove(roomNum);
        if(CI.roomCount()==0)
        	root.removeElement(areaName.toUpperCase());
    }
    
    public int roomCountAllAreas()
    {
    	int total=0;
    	CMIntegerGrouper CMI=null;
    	for(int i=0;i<root.size();i++)
    	{
            CMI=(CMIntegerGrouper)root.elementAt(i,2);
            if(CMI==null) 
            	total++;
            else
	            total+=CMI.roomCount();
    	}
    	return total;
    }
    
    public int roomCount(String areaName)
    {
        int x=areaName.indexOf("#");
        if(x>0)
            areaName=areaName.substring(0,x).toUpperCase();
        else
            areaName=areaName.toUpperCase();
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
            {
                if(root.elementAt(mid,2)!=null)
                {
                	CMIntegerGrouper CMI=(CMIntegerGrouper)root.elementAt(mid,2);
                	if(CMI==null) return 1;
                    return CMI.roomCount();
                }
                return 0;
            }
            else
            if(comp<0)
                end=mid-1;
            else
                start=mid+1;
        }
        return 0;
    }
    
    public String random()
    {
    	int total=0;
    	CMIntegerGrouper CMI=null;
    	for(int i=0;i<root.size();i++)
    	{
            CMI=(CMIntegerGrouper)root.elementAt(i,2);
            if(CMI==null)
            	total++;
            else
	            total+=CMI.roomCount();
    	}
    	if(total<=0) return null;
    	int which=CMLib.dice().roll(1,total,-1);
    	total=0;
        String roomID=null;
    	for(int i=0;i<root.size();i++)
    	{
            CMI=(CMIntegerGrouper)root.elementAt(i,2);
            if(CMI==null)
            	total++;
            else
	    		total+=CMI.roomCount();
    		if(which<total)
    		{
    			roomID=(String)root.elementAt(i,1);
    			break;
    		}
    	}
    	if(roomID==null) return null;
    	if(CMI==null)
    	{
    		//Log.errOut("RNUMS","Unable to even select an integer group! Picked "+which+"/"+grandTotal);
    		return roomID;
    	}
		long selection=CMI.random();
		return convertRoomID(roomID,selection);
    }
    
    public int[] convertRoomID(long coded)
    {
		if(coded==-1) return null;
		int[] ids=new int[3];
		ids[1]=-1;
		ids[2]=-1;
		if(coded<=CMIntegerGrouper.NEXT_BITS)
		{
			ids[0]=(int)coded;
			return ids;
		}
		long mask=0;
		for(int i=0;i<15;i++) mask=(mask<<1)+1;
		ids[2]=(int)(coded&mask);
		long mask2=mask<<15;
		ids[1]=(int)((coded&mask2)>>15);
		mask|=mask2;
		mask=mask<<30;
		ids[0]=(int)(((coded&mask)>>30)&(CMIntegerGrouper.NEXT_BITSL-CMIntegerGrouper.GRID_FLAGL));
		return ids;
    }
    public String convertRoomID(String prefix, long coded)
    {
		if(coded==-1) return prefix;
		if(coded<CMIntegerGrouper.NEXT_BITS)
			return prefix+"#"+coded;
		long mask=0;
		for(int i=0;i<15;i++) mask=(mask<<1)+1;
		long thirdID=coded&mask;
		long mask2=mask<<15;
		long secondID=(coded&mask2)>>15;
		mask|=mask2;
		mask=mask<<30;
		long firstID=(((coded&mask)>>30)&(CMIntegerGrouper.NEXT_BITSL-CMIntegerGrouper.GRID_FLAGL));
		return prefix+"#"+firstID+"#("+secondID+","+thirdID+")";
    }
    
    public Enumeration getAreaNames(){ return ((Vector)root.getDimensionVector(1).clone()).elements();}
    
    private boolean isGrouper(String areaName)
    {
    	areaName=areaName.toUpperCase();
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0) return true;
            else
            if(comp<0)
                end=mid-1;
            else
                start=mid+1;
        }
        return false;
    }
    
    public CMIntegerGrouper getGrouper(String areaName)
    {
    	areaName=areaName.toUpperCase();
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
            {
                if(root.elementAt(mid,2)!=null)
                    return ((CMIntegerGrouper)root.elementAt(mid,2));
                return null;
            }
            else
            if(comp<0)
                end=mid-1;
            else
                start=mid+1;
        }
        return null;
    }
    
    public boolean contains(String str)
    {
    	if(str==null) return false;
        String theRest=null;
        long roomNum=-1;
        int origX=str.indexOf("#");
        int x=origX;
        if(x>0)
        {
            theRest=str.substring(x+1).trim();
            str=str.substring(0,x);
            x=theRest.indexOf("#(");
            if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
            {
                int comma=theRest.indexOf(",",x);
                if(comma>0)
                {
                    roomNum=Long.parseLong(theRest.substring(0,x))<<30;
                    roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
                    roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
                    if(roomNum<CMIntegerGrouper.NEXT_BITS) roomNum|=CMIntegerGrouper.GRID_FLAGL;
                }
            }
            else
            if(CMath.isInteger(theRest))
                roomNum=Integer.parseInt(theRest.substring(x+1).trim());
        }
        
        CMIntegerGrouper myGrouper=getGrouper(str);
        if((origX<0)&&(myGrouper==null)&&(isGrouper(str)))
        	return true;
        if(myGrouper==null) return false;
        return myGrouper.contains(roomNum);
    }
    
    public String xml()
    {
        StringBuffer str=new StringBuffer("<AREAS>");
        for(int i=0;i<root.size();i++)
        {
            str.append("<AREA><ID>"+(String)root.elementAt(i,1)+"</ID>");
            if(root.elementAt(i,2)!=null)
                str.append("<NUMS>"+((CMIntegerGrouper)root.elementAt(i,2)).text()+"</NUMS>");
            str.append("</AREA>");
        }
        return str.toString()+"</AREAS>";
    }

    public void parseXML(String xml)
    {
        Vector V=CMLib.xml().parseAllXML(xml);
        if((V==null)||(V.size()==0)) return;
        Vector xV=CMLib.xml().getRealContentsFromPieces(V,"AREAS");
        root.clear();
        String ID=null;
        String NUMS=null;
        if((xV!=null)&&(xV.size()>0))
            for(int x=0;x<xV.size();x++)
            {
                XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                if((ablk.tag.equalsIgnoreCase("AREA"))&&(ablk.contents!=null))
                {
                    ID=CMLib.xml().getValFromPieces(ablk.contents,"ID");
                    NUMS=CMLib.xml().getValFromPieces(ablk.contents,"NUMS");
                    if((NUMS!=null)&&(NUMS.length()>0))
                        root.addElement(ID,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).parseText(NUMS));
                    else
                        root.addElement(ID,null);
                }
            }
    }
    
    public void add(String str)
    {
        String areaName=str.toUpperCase().trim();
        if(areaName.length()==0) return;
        
        String theRest=null;
        long roomNum=-1;
        int x=areaName.indexOf("#");
        if(x>0)
        {
            theRest=areaName.substring(x+1).trim();
            areaName=areaName.substring(0,x);
            x=theRest.indexOf("#(");
            if((x>=0)&&(theRest.endsWith(")"))&&(CMath.isInteger(theRest.substring(0,x))))
            {
                int comma=theRest.indexOf(",",x);
                if(comma>0)
                {
                    roomNum=(Long.parseLong(theRest.substring(0,x))<<30);
                    roomNum+=(Long.parseLong(theRest.substring(x+2,comma))<<15);
                    roomNum+=Long.parseLong(theRest.substring(comma+1,theRest.length()-1));
                    if(roomNum<CMIntegerGrouper.NEXT_BITS) roomNum|=CMIntegerGrouper.GRID_FLAGL;
                }
            }
            else
            if(CMath.isInteger(theRest))
                roomNum=Integer.parseInt(theRest.substring(x+1).trim());
        }
        int start=0;
        int end=root.size()-1;
        int comp=-1;
        int mid=-1;
        int lastStart=0;
        int lastEnd=root.size()-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=areaName.compareTo((String)root.elementAt(mid,1));
            if(comp==0)
                break;
            else
            if(comp<0)
            {
                lastEnd=end;
                end=mid-1;
            }
            else
            {
                lastStart=start;
                start=mid+1;
            }
        }
        if(comp==0)
        {
            if(root.elementAt(mid,2)!=null)
                ((CMIntegerGrouper)root.elementAt(mid,2)).add(roomNum);
        }
        else
        {
            if(mid<0)
                root.addElement(areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
            else
            {
                for(comp=lastStart;comp<=lastEnd;comp++)
                    if(areaName.compareTo((String)root.elementAt(comp,1))<0)
                    {
                        root.insertElementAt(comp,areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
                        return;
                    }
                root.addElement(areaName,((CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper")).add(roomNum));
            }
        }
    }
    
    public Enumeration getRoomIDs(){return new RoomnumberSetEnumeration();}
    
    private class RoomnumberSetEnumeration implements Enumeration
    {
    	Enumeration areaNames=null;
    	String areaName=null;
    	long[] nums=null;
    	String nextID=null;
    	int n=0;
    	public RoomnumberSetEnumeration(){ areaNames=getAreaNames();}
    	public boolean hasMoreElements(){
    		if(nextID==null) getNextID();
    		return nextID!=null;
    	}
    	public Object nextElement(){
    		if(nextID==null) getNextID();
    		String next=nextID;
    		nextID=null;
    		return next;
    	}
    	private void getNextID()
    	{
    		if(nums==null)
    		{
    			nextID=null;
    			if((areaNames==null)||(!areaNames.hasMoreElements()))
    				return;
    			areaName=(String)areaNames.nextElement();
    			CMIntegerGrouper grp=getGrouper(areaName);
    			if(grp==null){ nextID=areaName; return;}
    			nums=grp.allRoomNums();
    			n=0;
    		}
    		if((nums==null)||(n>=nums.length))
    		{
    			nums=null;
    			getNextID();
    			return;
    		}
    		long num=nums[n++];
    		nextID=convertRoomID(areaName,num);
    	}
    }
}
