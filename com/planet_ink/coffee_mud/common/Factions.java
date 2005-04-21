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
	public static Hashtable hashedFactionRanges=new Hashtable();
	
	public Factions() 
	{
	}
	
	
	public static void clearFactions()
	{
	    factionSet.clear();
	    hashedFactionRanges.clear();
	}
	
	public static Faction getFaction(String factionID) 
	{
		factionID=factionID.toUpperCase();
	    if(Resources.getFileResource(factionID)!=null)
	    {
	        if(!factionSet.containsKey(factionID))
	        {
	            Faction F=new Faction(Resources.getFileResource(factionID),factionID);
	            for(int r=0;r<F.ranges.size();r++)
	            {
	                Faction.FactionRange FR=(Faction.FactionRange)F.ranges.elementAt(r);
	                if(!hashedFactionRanges.containsKey(FR.Name.toUpperCase()))
	                    hashedFactionRanges.put(FR.Name.toUpperCase(),FR);
	            }
	            factionSet.put(factionID,F);
	            return F;
	        }
	        return (Faction)factionSet.get(factionID);
	    }
	    return null;
	}
	
	public static Faction getFactionByRangeName(String rangeID)
	{
	    if(hashedFactionRanges.containsKey(rangeID.toUpperCase()))
	        return (Faction)hashedFactionRanges.get(rangeID.toUpperCase());
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
	        factionSet.remove(F.ID);
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
	public static boolean getExperience(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.experience; return false; }
	public static Faction.FactionRange getRange(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRange(faction); return null; }
	public static Vector getRanges(String factionID) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRanges(); return null; }
	public static Double getRangePercent(String factionID, int faction) { Faction.FactionRange R=Factions.getRange(factionID,faction); if(R==null) return null; return new Double(Util.div((faction - R.low),(R.high - R.low)) * 100.0);}
	public static double getRateModifier(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.rateModifier; return 0; }
	public static int getTotal(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return (f.maximum-f.minimum); return 0; }
	public static int getRandom(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.randomFaction(); return 0; }
	
	public static String AlignID() { return "ALIGNMENT.INI"; }
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
	    try
	    {
		    Session S=null;
		    MOB mob=null;
		    Faction F=null;
		    Faction.FactionChangeEvent CE=null; 
		    for(int s=0;s<Sessions.size();s++)
		    {
		        S=Sessions.elementAt(s);
		        mob=(!S.killFlag())?S.mob():null;
		        if(mob!=null)
		        {
		            for(Enumeration e=factionSet.elements();e.hasMoreElements();)
		            {
		                F=(Faction)e.nextElement();
		                CE=F.findChangeEvent("ADD");
		                if((CE!=null)&&(CE.applies(mob)))
		                    F.executeChange(mob,mob,CE);
		            }
		        }
		    }
	    }catch(Exception e){}
	    return true;
	}
	
	public static int getAlignPurity(int faction, int AlignEq) 
	{
		int bottom=0;
		int top=0;
		Vector ranges = getRanges(AlignID());
		for(int i=0;i<ranges.size();i++) {
			Faction.FactionRange R=(Faction.FactionRange)ranges.elementAt(i);
			if(R.AlignEquiv==AlignEq) {
				if(R.low<bottom) bottom=R.low;
				if(R.high>top) top=R.high;
			}
		}
		switch(AlignEq) {
			case Faction.ALIGN_GOOD:
				return Math.abs(getPercent(AlignID(),faction) - getPercent(AlignID(),top));
			case Faction.ALIGN_EVIL:
				return Math.abs(getPercent(AlignID(),bottom) - getPercent(AlignID(),faction));
			case Faction.ALIGN_NEUTRAL:
				return Math.abs(getPercent(AlignID(),(int)Math.round(Util.div((top+bottom),2))) - getPercent(AlignID(),faction));
			default:
				return 0;
		}
	}
	
	// Please don't mock the name, I couldn't think of a better one.  Sadly.
	public static int getAlignThingie(int AlignEq) 
	{
		int bottom=0;
		int top=0;
		Vector ranges = getRanges(AlignID());
	    if(ranges==null) return 0;
		for(int i=0;i<ranges.size();i++) {
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
