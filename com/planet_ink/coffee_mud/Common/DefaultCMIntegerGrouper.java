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
   Copyright 2000-2005 Bo Zimmerman

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
public class DefaultCMIntegerGrouper implements CMIntegerGrouper
{
    public String ID(){return "DefaultCMIntegerGrouper";}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCMIntegerGrouper();}}
    
    public int[] xs=new int[0];
    public long[] ys=new long[0];
    
    public CMObject copyOf()
    {
        DefaultCMIntegerGrouper R=new DefaultCMIntegerGrouper();
        R.xs=new int[xs.length];
        for(int i=0;i<xs.length;i++)
            R.xs[i]=xs[i];
        R.ys=new long[ys.length];
        for(int i=0;i<ys.length;i++)
            R.ys[i]=ys[i];
        return R;
    }
    
    
    public String text()
    {
        return "{"+CMParms.toStringList(xs)+"},{"+CMParms.toStringList(ys)+"}";
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
    
    public boolean contains(long x)
    {
        if(x<0) return true;
        if(x<=Integer.MAX_VALUE)
        {
            for(int i=0;i<xs.length;i++)
                if((xs[i]&NEXT_FLAG)==NEXT_FLAG)
                {
                    if((x>=(xs[i]&NEXT_BITS))&&(x<=xs[i+1]))
                        return true;
                    if(x<=xs[i+1])
                        return false;
                    i++;
                }
                else
                if(x==xs[i])
                    return true;
                else
                if(x<xs[i])
                    return false;
        }
        else
        {
            for(int i=0;i<ys.length;i++)
                if((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
                {
                    if((x>=(ys[i]&NEXT_BITSL))&&(x<=ys[i+1]))
                        return true;
                    if(x<=ys[i+1])
                        return false;
                    i++;
                }
                else
                if(x==ys[i])
                    return true;
                else
                if(x<ys[i])
                    return false;
        }
        return false;
    }
    
    public int roomCount()
    {
        int count=0;
        for(int i=0;i<xs.length;i++)
            if(((xs[i]&NEXT_FLAG)==NEXT_FLAG)
            &&(i<(xs.length-1)))
                count=count+1+(xs[i+1]-(xs[i]&NEXT_BITS));
            else
                count++;
        for(int i=0;i<ys.length;i++)
            if(((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
            &&(i<(ys.length-1)))
                count=count+1+(int)(ys[i+1]-(ys[i]&NEXT_BITSL));
            else
                count++;
        return count;
    }
    
    public void growarrayx(int here)
    {
        int[] newis=new int[xs.length+1];
        for(int i=0;i<here;i++)
            newis[i]=xs[i];
        for(int i=here;i<xs.length;i++)
            newis[i+1]=xs[i];
        xs=newis;
    }
   
    public void growarrayy(int here)
    {
        long[] newis=new long[ys.length+1];
        for(int i=0;i<here;i++)
            newis[i]=ys[i];
        for(int i=here;i<ys.length;i++)
            newis[i+1]=ys[i];
        ys=newis;
    }
    
    public void shrinkarrayx(int here)
    {
        int[] newis=new int[xs.length-1];
        for(int i=0;i<here;i++)
            newis[i]=xs[i];
        for(int i=here;i<xs.length;i++)
            newis[i-1]=xs[i];
        xs=newis;
    }
    
    public void shrinkarrayy(int here)
    {
        long[] newis=new long[ys.length-1];
        for(int i=0;i<here;i++)
            newis[i]=ys[i];
        for(int i=here;i<ys.length;i++)
            newis[i-1]=ys[i];
        ys=newis;
    }
    
    public void consolodatex()
    {
        for(int i=0;i<xs.length-1;i++)
            if(((xs[i]&NEXT_FLAG)==0)
            &&((xs[i]&NEXT_BITS)==((xs[i+1]&NEXT_BITS)+1)))
            {
                if((xs[i+1]&NEXT_FLAG)==NEXT_FLAG)
                {
                    if((i>0)&&((xs[i-1]&NEXT_FLAG)==NEXT_FLAG))
                    {
                        shrinkarrayx(i+1);
                        shrinkarrayx(i);
                        return;
                    }
                    shrinkarrayx(i);
                    xs[i]=((xs[i]&NEXT_BITS)-1)|NEXT_FLAG;
                    return;
                }
                if((i>0)&&((xs[i-1]&NEXT_FLAG)==NEXT_FLAG))
                {
                    shrinkarrayx(i+1);
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
            &&((ys[i]&NEXT_BITSL)==((ys[i+1]&NEXT_BITSL)+1)))
            {
                if((ys[i+1]&NEXT_FLAGL)==NEXT_FLAGL)
                {
                    if((i>0)&&((ys[i-1]&NEXT_FLAGL)==NEXT_FLAGL))
                    {
                        shrinkarrayy(i+1);
                        shrinkarrayy(i);
                        return;
                    }
                    shrinkarrayy(i);
                    ys[i]=((ys[i]&NEXT_BITSL)-1)|NEXT_FLAGL;
                    return;
                }
                if((i>0)&&((ys[i-1]&NEXT_FLAG)==NEXT_FLAG))
                {
                    shrinkarrayy(i+1);
                    ys[i]++;
                    return;
                }
                ys[i]=ys[i]|NEXT_FLAGL;
                return;
            }
    }
    
    public synchronized CMIntegerGrouper add(long x)
    {
        if(x<0) return null;
        if(x<NEXT_FLAG)
            addx((int)x);
        else
            addy(x);
        return this;
    }
    
    public void addy(long x)
    {
        for(int i=0;i<ys.length;i++)
            if((ys[i]&NEXT_FLAGL)==NEXT_FLAGL)
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
                    growarrayy(i);
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
                growarrayy(i);
                ys[i]=x|NEXT_FLAGL;
                consolodatey();
                return;
            }
            else
            if(x==ys[i]+1)
            {
                growarrayy(i+1);
                ys[i]=ys[i]|NEXT_FLAGL;
                ys[i+1]=x;
                consolodatey();
                return;
            }
            else
            if(x<ys[i])
            {
                growarrayy(i);
                ys[i]=x;
                consolodatey();
                return;
            }
        growarrayy(ys.length);
        ys[ys.length-1]=x;
        consolodatey();
        return;
    }
    
    public void addx(int x)
    {
        for(int i=0;i<xs.length;i++)
            if((xs[i]&NEXT_FLAG)==NEXT_FLAG)
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
                    growarrayx(i);
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
                growarrayx(i);
                xs[i]=x|NEXT_FLAG;
                consolodatex();
                return;
            }
            else
            if(x==xs[i]+1)
            {
                growarrayx(i+1);
                xs[i]=xs[i]|NEXT_FLAG;
                xs[i+1]=x;
                consolodatex();
                return;
            }
            else
            if(x<xs[i])
            {
                growarrayx(i);
                xs[i]=x;
                consolodatex();
                return;
            }
        growarrayx(xs.length);
        xs[xs.length-1]=x;
        consolodatex();
        return;
    }

}
