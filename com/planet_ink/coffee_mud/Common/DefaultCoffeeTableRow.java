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
public class DefaultCoffeeTableRow implements CoffeeTableRow
{
    public String ID(){return "DefaultCoffeeTableRow";}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    
    public Hashtable stats=new Hashtable();
    public long highestOnline=0;
    public long numberOnlineTotal=0;
    public long numberOnlineCounter=0;
    public long startTime=0;
    public long endTime=0;
    
    
    public void setStartTime(long time){startTime=time;}
    public void setEndTime(long time){endTime=time;}
    public long startTime(){return startTime;}
    public long endTime(){return endTime;}
    public long highestOnline(){return highestOnline;}
    public long numberOnlineTotal(){return numberOnlineTotal;}
    public long numberOnlineCounter(){return numberOnlineCounter;}
    public String data()
    {
        StringBuffer data=new StringBuffer("");
        data.append(CMLib.xml().convertXMLtoTag("HIGH",highestOnline));
        data.append(CMLib.xml().convertXMLtoTag("NUMONLINE",numberOnlineTotal));
        data.append(CMLib.xml().convertXMLtoTag("NUMCOUNT",numberOnlineCounter));
        data.append("<STATS>");
        for(Enumeration e=stats.keys();e.hasMoreElements();)
        {
            String s=(String)e.nextElement();
            long[] l=(long[])stats.get(s);
            data.append(CMLib.xml().convertXMLtoTag(s,CMParms.toStringList(l)));
        }
        data.append("</STATS>");
        return data.toString();
    }
    
    public void bumpVal(String s, int type)
    {
        long[] stat=null;
        synchronized(stats)
        {
            if(stats.containsKey(s))
                stat=(long[])stats.get(s);
            else
            {
                stat=new long[STAT_TOTAL];
                stats.put(s,stat);
            }
        }
        stat[type]++;
    }
    
    public void totalUp(String code, long[] tot)
    {
        code=tagFix(code);
        for(Enumeration e=stats.keys();e.hasMoreElements();)
        {
            String s=(String)e.nextElement();
            if(s.startsWith(code)
            ||(s.startsWith("C")&&code.startsWith("*")))
            {
                long[] theseStats=(long[])stats.get(s);
                for(int t=0;t<theseStats.length;t++)
                    tot[t]+=theseStats[t];
            }
        }
    }

    public String tagFix(String s)
    {
        return s.trim().replaceAll(" ","_").toUpperCase();
    }
    
    public void bumpVal(CMObject E, int type)
    {
        if((E instanceof MOB)&&(((MOB)E).isMonster())) return;
        
        if(type==STAT_SPECIAL_NUMONLINE)
        {
            int ct=0;
            for(int s=0;s<CMLib.sessions().size();s++)
            {
                Session S=CMLib.sessions().elementAt(s);
                if((S!=null)&&(S.mob()!=null)
                &&(S.mob().location()!=null)
                &&(S.mob().location().isInhabitant(S.mob())))
                   ct++;
            }
            numberOnlineCounter++;
            numberOnlineTotal+=ct;
            if(ct>highestOnline)
                highestOnline=ct;
            return;
        }
        // classes, races, levels, genders, faiths, clanned, grouped
        if(E instanceof MOB)
        {
            MOB mob=(MOB)E;
            bumpVal("B"+tagFix(mob.baseCharStats().getCurrentClass().baseClass()),type);
            bumpVal("C"+tagFix(mob.baseCharStats().getCurrentClass().ID()),type);
            bumpVal("R"+tagFix(mob.baseCharStats().getMyRace().ID()),type);
            bumpVal("L"+mob.baseEnvStats().level(),type);
            bumpVal("G"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER)),type);
            bumpVal("F"+tagFix(mob.getWorshipCharID()),type);
            bumpVal("Q"+tagFix(mob.getClanID()),type);
            HashSet H=mob.getGroupMembers(new HashSet());
            bumpVal("J"+H.size(),type);
            int pct=0;
            for(Iterator e=H.iterator();e.hasNext();)
                if(!((MOB)e.next()).isMonster()) pct++;
            if(pct==0)pct=1;
            bumpVal("P"+pct,type);
        }
        else
        if(E instanceof Ability)
            bumpVal("A"+tagFix(E.ID()),type);
        else
        if(E instanceof Quest)
            bumpVal("U"+tagFix(((Quest)E).name()),type);
    }
    
    public void populate(long start, long end, String data)
    {
        synchronized(stats)
        {
            startTime=start;
            endTime=end;
            Vector all=CMLib.xml().parseAllXML(data);
            if((all==null)||(all.size()==0)) return;
            highestOnline=CMLib.xml().getIntFromPieces(all,"HIGH");
            numberOnlineTotal=CMLib.xml().getIntFromPieces(all,"NUMONLINE");
            numberOnlineCounter=CMLib.xml().getIntFromPieces(all,"NUMCOUNT");
            XMLLibrary.XMLpiece X=CMLib.xml().getPieceFromPieces(all,"STATS");
            if((X==null)||(X.contents==null)||(X.contents.size()==0)||(!X.tag.equals("STATS")))
                return;
            stats.clear();
            for(int s=0;s<X.contents.size();s++)
            {
                XMLLibrary.XMLpiece S=(XMLLibrary.XMLpiece)X.contents.elementAt(s);
                long[] l=CMParms.toLongArray(CMParms.parseCommas(S.value,true));
                if(l.length<STAT_TOTAL)
                {
                    long[] l2=new long[STAT_TOTAL];
                    for(int i=0;i<l.length;i++)
                        l2[i]=l[i];
                    l=l2;
                }
                long[] l2=(long[])stats.get(S.tag);
                if(l2!=null)
                {
                    for(int i=0;i<l2.length;i++)
                        l[i]+=l2[i];
                    stats.remove(S.tag);
                }
                stats.put(S.tag,l);
            }
        }
    }
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultCoffeeTableRow();}}
    public void initializeClass(){}
    
    public CMObject copyOf()
    {
        try{
            DefaultCoffeeTableRow CR=(DefaultCoffeeTableRow)this.clone();
            CR.stats=(Hashtable)stats.clone();
            return CR;
        }
        catch(Exception e){return newInstance();}
    }
}
