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
import com.planet_ink.coffee_mud.Libraries.StdLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class DefaultCMIntegerGrouper implements CMIntegerGrouper
{
    public String ID(){return "DefaultCMIntegerGrouper";}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public void initializeClass(){}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCMIntegerGrouper();}}
    
    public int[] xs=new int[0];
    public long[] ys=new long[0];
    
    public String text()
    {
        return "{"+CMParms.toStringList(xs)+"},{"+CMParms.toStringList(ys)+"}";
    }
    public CMObject copyOf()
    {
        DefaultCMIntegerGrouper R=new DefaultCMIntegerGrouper();
        R.xs=(int[])xs.clone();
        R.ys=(long[])ys.clone();
        return R;
    }
    
    
    public CMIntegerGrouper parseText(String txt)
    {
        xs=new int[0];
        ys=new long[0];
        txt=txt.trim();
        if(txt.length()==0) return null;
        if((!txt.startsWith("{"))&&(!txt.endsWith("}"))) 
            return null;
        int x=txt.indexOf("},{");
        if(x<0) return null;
        String Xstr=txt.substring(1,x);
        String Ystr=txt.substring(x+3,txt.length()-1);
        Vector XV=CMParms.parseCommas(Xstr,true);
        Vector YV=CMParms.parseCommas(Ystr,true);
        xs=new int[XV.size()];
        for(int v=0;v<XV.size();v++)
            xs[v]=CMath.s_int((String)XV.elementAt(v));
        ys=new long[YV.size()];
        for(int v=0;v<YV.size();v++)
            ys[v]=CMath.s_long((String)YV.elementAt(v));
        return this;
    }
    
    public long[] packedGridRoomNums(){return ys;}
    public int[] packedRoomNums(){return xs;}
    
    public boolean contains(long x)
    {
    	if(x==-1) return true;
        if(x<=NEXT_BITS)
        	return getXindex((int)x)>=0;
    	return getYindex(x)>=0;
    }

    public int getXindex(int x)
    {
        int start=0;
        int end=xs.length-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            if((mid>0)&&((xs[mid-1]&NEXT_FLAG)>0)){mid--;}
            if(x<(xs[mid]&NEXT_BITS))
                end=mid-1;
            else
            if((xs[mid]&NEXT_FLAG)>0)
            {
            	if(x>xs[mid+1])
	            	start=mid+2;
            	else
            	if(x<=xs[mid+1])
            		return mid;
            }
            else
            if(x>xs[mid])
            	start=mid+1;
            else
            	return mid;
        }
        return (-start)-1;
    }
    
    public int getYindex(long y)
    {
        int start=0;
        int end=ys.length-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            if((mid>0)&&((ys[mid-1]&NEXT_FLAGL)>0)){mid--;}
            if(y<(ys[mid]&NEXT_BITSL))
                end=mid-1;
            else
            if((ys[mid]&NEXT_FLAGL)>0)
            {
            	if(y>ys[mid+1])
	            	start=mid+2;
            	else
            	if(y<=ys[mid+1])
            		return mid;
            }
            else
            if(y>ys[mid])
            	start=mid+1;
            else
            	return mid;
        }
        return (-start)-1;
    }
    
    public int[] allPrimaryRoomNums()
    {
        int count=0;
        for(int i=0;i<xs.length;i++)
            if((xs[i]&NEXT_FLAG)>0)
            {
                count=count+1+(xs[i+1]-(xs[i]&NEXT_BITS));
                i++;
            }
            else
                count++;
    	int[] nums=new int[count];
    	int dex=0;
        for(int i=0;i<xs.length;i++)
        {
            if((xs[i]&NEXT_FLAG)>0)
            {
            	for(int x=(xs[i]&NEXT_BITS);x<=xs[i+1];x++)
                	nums[dex++]=x;
	            i++;
	        }
            else
            	nums[dex++]=xs[i];
        }
    	return nums;
    }

    public long[] allRoomNums()
    {
    	long[] nums=new long[roomCount()];
    	int dex=0;
        for(int i=0;i<xs.length;i++)
        {
            if((xs[i]&NEXT_FLAG)>0)
            {
            	for(int x=(xs[i]&NEXT_BITS);x<=xs[i+1];x++)
                	nums[dex++]=x;
            	i++;
            }
            else
            	nums[dex++]=xs[i];
        }
        for(int i=0;i<ys.length;i++)
            if((ys[i]&NEXT_FLAGL)>0)
            {
            	for(long y=(ys[i]&NEXT_BITSL);y<=ys[i+1];y++)
                	nums[dex++]=y;
		    	i++;
		    }
            else
            	nums[dex++]=ys[i];
        return nums;
    }


    public int roomCount()
    {
        int count=0;
        for(int i=0;i<xs.length;i++)
            if((xs[i]&NEXT_FLAG)>0)
            	count=count+1+(xs[i+1]-(xs[i++]&NEXT_BITS)); 
            else
                count++;
        for(int i=0;i<ys.length;i++)
            if((ys[i]&NEXT_FLAGL)>0)
                count=count+1+(int)(ys[i+1]-(ys[i++]&NEXT_BITSL));
            else
                count++;
        return count;
    }

    public long random()
    {
    	int roomCount=roomCount();
    	if(roomCount<=0) return -1;
    	int which=CMLib.dice().roll(1,roomCount,-1);
    	long count=0;
        for(int i=0;i<xs.length;i++)
        {
            if((xs[i]&NEXT_FLAG)>0)
                count=count+1+(xs[i+1]-(xs[i]&NEXT_BITS));
            else
                count++;
            if(which<count)
            {
                if((xs[i]&NEXT_FLAG)>0)
                	return (xs[i+1]-(count-which))+1;
            	return xs[i]&NEXT_BITS;
            }
        }
        for(int i=0;i<ys.length;i++)
        {
            if((ys[i]&NEXT_FLAGL)>0)
                count=count+1+(int)(ys[i+1]-(ys[i]&NEXT_BITSL));
            else
                count++;
            if(which<count)
            {
                if((ys[i]&NEXT_FLAGL)>0)
                	return (ys[i+1]-(count-which))+1;
            	return ys[i]&NEXT_BITSL;
            }
        }
        Log.errOut("CMINTS","Unable to select a random room int. Picked "+which+"/"+roomCount);
        return -1;
    }

    public void growarrayx(int here, int amount)
    {
        int[] newis=new int[xs.length+amount];
        for(int i=0;i<here;i++)
            newis[i]=xs[i];
        for(int i=here;i<xs.length;i++)
            newis[i+amount]=xs[i];
        xs=newis;
    }

    public void growarrayy(int here, int amount)
    {
        long[] newis=new long[ys.length+amount];
        for(int i=0;i<here;i++)
            newis[i]=ys[i];
        for(int i=here;i<ys.length;i++)
            newis[i+amount]=ys[i];
        ys=newis;
    }

    public synchronized CMIntegerGrouper remove(long x)
    {
    	if(x==-1) return null;
        if(x<=NEXT_BITS)
            removex((int)x);
        else
        	removey(x);
        return this;
    }
    public synchronized CMIntegerGrouper remove(CMIntegerGrouper grp)
    {
    	long[] dely=grp.allRoomNums();
    	for(int i=0;i<dely.length;i++)
    		remove(dely[i]);
    	return this;
    }
    public void shrinkarrayx(int here, int amount)
    {
        int[] newis=new int[xs.length-amount];
        for(int i=0;i<here;i++)
            newis[i]=xs[i];
        for(int i=here;i<newis.length;i++)
            newis[i]=xs[i+amount];
        xs=newis;
    }

    public void shrinkarrayy(int here, int amount)
    {
        long[] newis=new long[ys.length-amount];
        for(int i=0;i<here;i++)
            newis[i]=ys[i];
        for(int i=here;i<newis.length;i++)
            newis[i]=ys[i+amount];
        ys=newis;
    }

    public void consolodatex()
    {
        for(int i=0;i<xs.length-1;i++)
            if(((xs[i]&NEXT_FLAG)==0)
            &&(xs[i]+1==(xs[i+1]&NEXT_BITS)))
            {
                if((xs[i+1]&NEXT_FLAG)>0)
                {
                    if((i>0)&&((xs[i-1]&NEXT_FLAG)>0))
                    {
                        shrinkarrayx(i,2);
                        return;
                    }
                    shrinkarrayx(i,1);
                    xs[i]=((xs[i]&NEXT_BITS)-1)|NEXT_FLAG;
                    return;
                }
                if((i>0)&&((xs[i-1]&NEXT_FLAG)>0))
                {
                    shrinkarrayx(i+1,1);
                    xs[i]++;
                    return;
                }
                xs[i]=xs[i]|NEXT_FLAG;
                return;
            }
    }

    public void consolodatey()
    {
        for(int i=0;i<ys.length-1;i++)
            if(((ys[i]&NEXT_FLAGL)==0)
            &&(ys[i]+1==(ys[i+1]&NEXT_BITSL)))
            {
                if((ys[i+1]&NEXT_FLAGL)>0)
                {
                    if((i>0)&&((ys[i-1]&NEXT_FLAGL)>0))
                    {
                        shrinkarrayy(i,2);
                        return;
                    }
                    shrinkarrayy(i,1);
                    ys[i]=((ys[i]&NEXT_BITSL)-1)|NEXT_FLAGL;
                    return;
                }
                if((i>0)&&((ys[i-1]&NEXT_FLAGL)>0))
                {
                    shrinkarrayy(i+1,1);
                    ys[i]++;
                    return;
                }
                ys[i]=ys[i]|NEXT_FLAGL;
                return;
            }
    }

    public CMIntegerGrouper add(CMIntegerGrouper grp)
    {
    	if(grp==null) return this;
    	long[] all=grp.allRoomNums();
    	for(int l=0;l<all.length;l++)
    		add(all[l]);
    	return this;
    }

    public synchronized CMIntegerGrouper add(long x)
    {
    	if(x==-1) return null;
        if(x<=NEXT_BITS)
            addx((int)x);
        else
            addy(x);
        return this;
    }

    public void addy(long x)
    {
    	int index=getYindex(x);
    	if(index>=0) return;
    	index=(index+1)*-1;
    	if((index>0)&&((ys[index-1]&NEXT_FLAGL)>0)) index--;
    	int end=index+2;
    	if(end>ys.length) end=ys.length;
        for(int i=index;i<end;i++)
            if((ys[i]&NEXT_FLAGL)>0)
            {
                if((x>=(ys[i]&NEXT_BITSL))&&(x<=ys[i+1]))
                    return;
                if(x==((ys[i]&NEXT_BITSL)-1))
                {
                    ys[i]=x|NEXT_FLAGL;
                    consolodatey();
                    return;
                }
                if(x==(ys[i+1]+1))
                {
                    ys[i+1]=x;
                    consolodatey();
                    return;
                }
                if(x<(ys[i]&NEXT_BITSL))
                {
                    growarrayy(i,1);
                    ys[i]=x;
                    consolodatey();
                    return;
                }
                i++;
            }
            else
            if(x==ys[i])
                return;
            else
            if(x==ys[i]-1)
            {
                growarrayy(i,1);
                ys[i]=x|NEXT_FLAGL;
                consolodatey();
                return;
            }
            else
            if(x==ys[i]+1)
            {
                growarrayy(i+1,1);
                ys[i]=ys[i]|NEXT_FLAGL;
                ys[i+1]=x;
                consolodatey();
                return;
            }
            else
            if(x<ys[i])
            {
                growarrayy(i,1);
                ys[i]=x;
                consolodatey();
                return;
            }
        growarrayy(ys.length,1);
        ys[ys.length-1]=x;
        consolodatey();
        return;
    }

    public void addx(int x)
    {
    	int index=getXindex(x);
    	if(index>=0) return;
    	index=(index+1)*-1;
    	if((index>0)&&((xs[index-1]&NEXT_FLAG)>0)) index--;
    	int end=index+2;
    	if(end>xs.length) end=xs.length;
        for(int i=index;i<end;i++)
            if((xs[i]&NEXT_FLAG)>0)
            {
                if((x>=(xs[i]&NEXT_BITS))&&(x<=xs[i+1]))
                    return;
                if(x==((xs[i]&NEXT_BITS)-1))
                {
                    xs[i]=x|NEXT_FLAG;
                    consolodatex();
                    return;
                }
                if(x==(xs[i+1]+1))
                {
                    xs[i+1]=x;
                    consolodatex();
                    return;
                }
                if(x<(xs[i]&NEXT_BITS))
                {
                    growarrayx(i,1);
                    xs[i]=x;
                    consolodatex();
                    return;
                }
                i++;
            }
            else
            if(x==xs[i])
                return;
            else
            if(x==xs[i]-1)
            {
                growarrayx(i,1);
                xs[i]=x|NEXT_FLAG;
                consolodatex();
                return;
            }
            else
            if(x==xs[i]+1)
            {
                growarrayx(i+1,1);
                xs[i]=xs[i]|NEXT_FLAG;
                xs[i+1]=x;
                consolodatex();
                return;
            }
            else
            if(x<xs[i])
            {
                growarrayx(i,1);
                xs[i]=x;
                consolodatex();
                return;
            }
        growarrayx(xs.length,1);
        xs[xs.length-1]=x;
        consolodatex();
        return;
    }

    public void removey(long x)
    {
    	int index=getYindex(x);
    	if(index<0) return;
    	if((index>0)&&((ys[index-1]&NEXT_FLAGL)>0)) index--;
    	int end=index+2;
    	if(end>ys.length) end=ys.length;
        for(int i=index;i<end;i++)
            if((ys[i]&NEXT_FLAGL)>0)
            {
                if(x<(ys[i]&NEXT_BITSL)) return;
                if(x==(ys[i]&NEXT_BITSL))
                {
                    ys[i]++;
                    if((x+1)==ys[i+1])
                    	shrinkarrayy(i,1);
                    return;
                }
                if(x==ys[i+1])
                {
                    ys[i+1]--;
                    if((x-1)==(ys[i]&NEXT_BITSL))
                    	shrinkarrayy(i,1);
                    return;
                }
                if(x<ys[i+1])
                {
                	if(x==((ys[i]&NEXT_BITSL)+1))
                	{
	                    growarrayy(i+1,1);
                		ys[i]=(ys[i]&NEXT_BITSL);
                		ys[i+1]=(x+1|NEXT_FLAGL);
                	}
                	else
                	if(x==ys[i+1]-1)
                	{
	                    growarrayy(i+1,1);
                		ys[i+1]=x-1;
                	}
                	else
                	{
	                    growarrayy(i+1,2);
	                    ys[i+1]=x-1;
	                    ys[i+2]=(x+1)|NEXT_FLAGL;
                	}
                    return;
                }
                i++;
            }
            else
            if(x<ys[i])
            	return;
            else
            if(x==ys[i])
            {
                shrinkarrayy(i,1);
                return;
            }
    }

    public void removex(int x)
    {
    	int index=getXindex(x);
    	if(index<0) return;
    	if((index>0)&&((xs[index-1]&NEXT_FLAG)>0)) index--;
    	int end=index+2;
    	if(end>xs.length) end=xs.length;
        for(int i=index;i<end;i++)
            if((xs[i]&NEXT_FLAG)>0)
            {
                if(x<(xs[i]&NEXT_BITS)) return;
                if(x==(xs[i]&NEXT_BITS))
                {
                    xs[i]++;
                    if((x+1)==xs[i+1])
                    	shrinkarrayx(i,1);
                    return;
                }
                if(x==xs[i+1])
                {
                    xs[i+1]--;
                    if((x-1)==(xs[i]&NEXT_BITS))
                    	shrinkarrayx(i,1);
                    return;
                }
                if(x<xs[i+1])
                {
                	if(x==((xs[i]&NEXT_BITS)+1))
                	{
	                    growarrayx(i+1,1);
                		xs[i]=(xs[i]&NEXT_BITS);
                		xs[i+1]=(x+1|NEXT_FLAG);
                	}
                	else
                	if(x==xs[i+1]-1)
                	{
	                    growarrayx(i+1,1);
                		xs[i+1]=x-1;
                	}
                	else
                	{
	                    growarrayx(i+1,2);
	                    xs[i+1]=x-1;
	                    xs[i+2]=(x+1)|NEXT_FLAG;
                	}
                    return;
                }
                i++;
            }
            else
            if(x<xs[i])
            	return;
            else
            if(x==xs[i])
            {
                shrinkarrayx(i,1);
                return;
            }
    }
}
