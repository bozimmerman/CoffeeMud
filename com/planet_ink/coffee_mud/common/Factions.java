package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.system.DBConnections;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */

public class Factions implements Tickable
{
	public static Hashtable factionSet = new Hashtable();
	private static Hashtable hashedFactionRanges=new Hashtable();
	
	public Factions() 
	{
	}
	
	
	public static void clearFactions()
	{
	    factionSet.clear();
	    hashedFactionRanges.clear();
	}
	
	private static Hashtable rangeCodeNames(){ return hashedFactionRanges; }
	public static boolean isRangeCodeName(String key){ return rangeCodeNames().containsKey(key.toUpperCase());}
	public static boolean isFactionedThisWay(MOB mob, String rangeCodeName)
	{
	    Faction.FactionRange FR=(Faction.FactionRange)rangeCodeNames().get(rangeCodeName.toUpperCase());
	    if(FR==null) return false;
	    Faction.FactionRange FR2=FR.myFaction.fetchRange(mob.fetchFaction(FR.myFaction.ID));
	    if(FR2==null) return false;
	    return FR2.CodeName.equalsIgnoreCase(FR.CodeName);
	}
	public static String rangeDescription(String rangeCodeName, String andOr)
	{
	    Faction.FactionRange FR=(Faction.FactionRange)rangeCodeNames().get(rangeCodeName.toUpperCase());
	    if((FR==null)||(FR.myFaction==null)||(FR.myFaction.ranges.size()<=0))
	        return "";
	    Vector relevantFactions=new Vector();
	    for(int r=0;r<FR.myFaction.ranges.size();r++)
	    {
	        if((((Faction.FactionRange)FR.myFaction.ranges.elementAt(r)).CodeName.equalsIgnoreCase(FR.CodeName)))
	            relevantFactions.addElement(FR.myFaction.ranges.elementAt(r));
	    }
	    if(relevantFactions.size()==0) return "";
	    if(relevantFactions.size()==1)
	        return FR.myFaction.name+" of "+((Faction.FactionRange)relevantFactions.firstElement()).Name;
	    StringBuffer buf=new StringBuffer(FR.myFaction.name+" of ");
	    for(int i=0;i<relevantFactions.size()-1;i++)
	        buf.append(((Faction.FactionRange)relevantFactions.elementAt(i)).Name+", ");
        buf.append(andOr+((Faction.FactionRange)relevantFactions.lastElement()).Name);
        return buf.toString();
	}
	    
	
	
	public static Faction getFaction(String factionID) 
	{
	    if(factionID==null) return null;
		Faction F=(Faction)factionSet.get(factionID.toUpperCase());
		if(F!=null) return F;
        StringBuffer buf=Resources.getFileResource(factionID);
	    if((buf!=null)&&(buf.length()>0))
	    {
            F=new Faction(buf,factionID);
            for(int r=0;r<F.ranges.size();r++)
            {
                Faction.FactionRange FR=(Faction.FactionRange)F.ranges.elementAt(r);
                String CodeName=(FR.CodeName.length()>0)?FR.CodeName.toUpperCase():FR.Name.toUpperCase();
                if(!hashedFactionRanges.containsKey(CodeName))
                    hashedFactionRanges.put(CodeName,FR);
            }
            factionSet.put(factionID.toUpperCase(),F);
            return F;
	    }
        return null;
	}
	
	public static Faction getFactionByRangeCodeName(String rangeCodeName)
	{
	    if(hashedFactionRanges.containsKey(rangeCodeName.toUpperCase()))
	        return (Faction)hashedFactionRanges.get(rangeCodeName.toUpperCase());
	    return null;
	}
	
	public static Faction getFactionByName(String factionNamed) 
	{
	    for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	    {
	        Faction f=(Faction)factionSet.get(e.nextElement());
	        if(f.name.equalsIgnoreCase(factionNamed)) return f;
	    }
	    return null;
	}
	
	public static boolean removeFaction(String factionID) 
	{
	    if(factionID==null) 
	    {
	        for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	        {
	            Faction f=(Faction)factionSet.get(e.nextElement());
	            removeFaction(f.ID);
	        }
	        return true;
	    }
	    else
	    {
	        Faction F=getFactionByName(factionID);
	        if(F==null) F=getFaction(factionID);
	        if(F==null) return false;
	        Resources.removeResource(F.ID);
	        factionSet.remove(F.ID.toUpperCase());
	        return true;
	    }
	}
	
	public static String listFactions() 
	{
	    StringBuffer msg=new StringBuffer();
	    msg.append("\n\r^.^N");
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    msg.append("| ^HFaction Name^N                   | ^HFaction INI Source File (Faction ID)^N    |\n\r");
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	    {
	        Faction f=(Faction)factionSet.get(e.nextElement());
	        msg.append("| ");
	        msg.append(Util.padRight(f.name,30));
	        msg.append(" | ");
	        msg.append(Util.padRight(f.ID,39));
	        msg.append(" |\n\r");
	    }
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    msg.append("\n\r");
	    return msg.toString();
	}
	
	public String ID(){return "Factions";}
	public String name(){return "Factions";}
	public long getTickStatus(){ return Tickable.STATUS_NOT;}
	public static String getName(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.name; return ""; }
	public static int getMinimum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.minimum; return 0; }
	public static int getMaximum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.maximum; return 0; }
	public static int getPercent(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercent(faction); return 0; }
	public static int getPercentFromAvg(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercentFromAvg(faction); return 0; }
	public static Faction.FactionRange getRange(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRange(faction); return null; }
	public static Vector getRanges(String factionID) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRanges(); return null; }
	public static double getRangePercent(String factionID, int faction) { Faction.FactionRange R=Factions.getRange(factionID,faction); if(R==null) return 0.0; return (Util.div((faction - R.low),(R.high - R.low)) * 100.0);}
	public static double getRateModifier(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.rateModifier; return 0; }
	public static int getTotal(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return (f.maximum-f.minimum); return 0; }
	public static int getRandom(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.randomFaction(); return 0; }
	
	public static String AlignID() { return "alignment.ini"; }
	public static void setAlignment(MOB mob, int newAlignment)
	{
	    if(getFaction(AlignID())!=null) 
	        mob.addFaction(AlignID(),getAlignThingie(newAlignment));
	}
	
	public static void setAlignmentOldRange(MOB mob, int oldRange)
	{
		if(getFaction(AlignID())!=null)
		{
			if(oldRange>=650)
				Factions.setAlignment(mob,Faction.ALIGN_GOOD);
			else
			if(oldRange>=350) 
			    Factions.setAlignment(mob,Faction.ALIGN_NEUTRAL);
			else
			if(oldRange>=0) 
			    Factions.setAlignment(mob,Faction.ALIGN_EVIL);
			else{ /* a -1 value is the new norm */}
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
        if(Sessions.size()==0) 
            return true;
	    try
	    {
		    Session S=null;
		    MOB mob=null;
		    Faction F=null;
		    Faction.FactionChangeEvent CE=null; 
            DVector outSiders=new DVector(2);
            DVector timers=new DVector(2);
            for(Enumeration e=factionSet.elements();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                CE=F.findChangeEvent("ADDOUTSIDER");
                if(CE!=null) outSiders.addElement(CE,F);
                CE=F.findChangeEvent("TIME");
                if(CE!=null) timers.addElement(CE,F);
            }
            if((outSiders.size()==0)&&(timers.size()==0)) 
                return true;
		    for(int s=0;s<Sessions.size();s++)
		    {
		        S=Sessions.elementAt(s);
		        mob=(!S.killFlag())?S.mob():null;
		        if(mob!=null)
		        {
                    for(int o=0;o<outSiders.size();o++)
		            {
		                CE=(Faction.FactionChangeEvent)outSiders.elementAt(o,1);
                        F=(Faction)outSiders.elementAt(o,2);
		                if((CE.applies(mob))&&(!F.hasFaction(mob)))
		                    F.executeChange(mob,mob,CE);
		            }
                    for(int o=0;o<timers.size();o++)
                    {
                        CE=(Faction.FactionChangeEvent)timers.elementAt(o,1);
                        F=(Faction)timers.elementAt(o,2);
		                if((CE.applies(mob))&&(F.hasFaction(mob)))
		                    F.executeChange(mob,mob,CE);
		            }
		        }
		    }
	    }catch(Exception e){}
	    return true;
	}
	
	public static int getAlignPurity(int faction, int AlignEq) 
	{
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
        int pct=getPercent(AlignID(),faction);
		Vector ranges = getRanges(AlignID());
		for(int i=0;i<ranges.size();i++) 
        {
			Faction.FactionRange R=(Faction.FactionRange)ranges.elementAt(i);
			if(R.AlignEquiv==AlignEq) 
            {
				if(R.low<bottom) bottom=R.low;
				if(R.high>top) top=R.high;
			}
		}
		switch(AlignEq) 
        {
			case Faction.ALIGN_GOOD:
				return Math.abs(pct - getPercent(AlignID(),top));
			case Faction.ALIGN_EVIL:
				return Math.abs(getPercent(AlignID(),bottom) - pct);
			case Faction.ALIGN_NEUTRAL:
				return Math.abs(getPercent(AlignID(),(int)Math.round(Util.div((top+bottom),2))) - pct);
			default:
				return 0;
		}
	}
	
	// Please don't mock the name, I couldn't think of a better one.  Sadly.
	public static int getAlignThingie(int AlignEq) 
	{
        int bottom=Integer.MAX_VALUE;
        int top=Integer.MIN_VALUE;
		Vector ranges = getRanges(AlignID());
	    if(ranges==null) return 0;
		for(int i=0;i<ranges.size();i++) 
        {
			Faction.FactionRange R=(Faction.FactionRange)ranges.elementAt(i);
			if(R.AlignEquiv==AlignEq) {
				if(R.low<bottom) bottom=R.low;
				if(R.high>top) top=R.high;
			}
		}
		switch(AlignEq) 
		{
			case Faction.ALIGN_GOOD:
				return top;
			case Faction.ALIGN_EVIL:
				return bottom;
			case Faction.ALIGN_NEUTRAL:
				return (int)Math.round(Util.div((top+bottom),2));
			default:
				return 0;
		}
	}
}
