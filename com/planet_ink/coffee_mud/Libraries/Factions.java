package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;

/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2010 Bo Zimmerman</p>
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

@SuppressWarnings("unchecked")
public class Factions extends StdLibrary implements FactionManager
{
    public String ID(){return "Factions";}
	public Hashtable<String,Faction> factionSet = new Hashtable<String,Faction>();
	public Hashtable<String,Faction> hashedFactionRanges=new Hashtable<String,Faction>();
	
    public Enumeration<Faction> factions(){return DVector.s_enum(factionSet,false);}
    public int numFactions(){return factionSet.size();}
	public void clearFactions()
	{
	    factionSet.clear();
	    hashedFactionRanges.clear();
	}
    public void reloadFactions(String factionList)
    {
        Vector preLoadFactions=CMParms.parseSemicolons(factionList,true);
        clearFactions();
        for(int i=0;i<preLoadFactions.size();i++)
            getFaction((String)preLoadFactions.elementAt(i));
    }
	
	public Hashtable<String,Faction> rangeCodeNames(){ return hashedFactionRanges; }
	public boolean isRangeCodeName(String key){ return rangeCodeNames().containsKey(key.toUpperCase());}
	public boolean isFactionedThisWay(MOB mob, String rangeCodeName)
	{
	    Faction F=(Faction)rangeCodeNames().get(rangeCodeName.toUpperCase());
	    if(F==null) return false;
	    Faction.FactionRange FR=(Faction.FactionRange)F.fetchRange(rangeCodeName.toUpperCase().trim());
	    if(FR==null) return false;
	    Faction.FactionRange FR2=F.fetchRange(mob.fetchFaction(F.factionID()));
	    if(FR2==null) return false;
	    return FR2.codeName().equalsIgnoreCase(FR.codeName());
	}
	public String rangeDescription(String rangeCodeName, String andOr)
	{
        Faction F=(Faction)rangeCodeNames().get(rangeCodeName.toUpperCase());
        if(F==null) return "";
        Faction.FactionRange FR=(Faction.FactionRange)F.fetchRange(rangeCodeName.toUpperCase().trim());
        if(FR==null) return "";
	    Vector relevantFactions=new Vector();
        for(Enumeration e=F.ranges();e.hasMoreElements();)
	    {
            Faction.FactionRange FR2=(Faction.FactionRange)e.nextElement();
	        if(FR2.codeName().equalsIgnoreCase(FR.codeName()))
	            relevantFactions.addElement(FR2);
	    }
	    if(relevantFactions.size()==0) return "";
	    if(relevantFactions.size()==1)
	        return F.name()+" of "+((Faction.FactionRange)relevantFactions.firstElement()).name();
	    StringBuffer buf=new StringBuffer(F.name()+" of ");
	    for(int i=0;i<relevantFactions.size()-1;i++)
	        buf.append(((Faction.FactionRange)relevantFactions.elementAt(i)).name()+", ");
        buf.append(andOr+((Faction.FactionRange)relevantFactions.lastElement()).name());
        return buf.toString();
	}
	    
	private Faction buildFactionFromXML(StringBuffer buf, String factionID)
	{
        Faction F=(Faction)CMClass.getCommon("DefaultFaction");
        F.initializeFaction(buf,factionID);
        for(Enumeration e=F.ranges();e.hasMoreElements();)
        {
            Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
            String CodeName=(FR.codeName().length()>0)?FR.codeName().toUpperCase():FR.name().toUpperCase();
            if(!hashedFactionRanges.containsKey(CodeName))
                hashedFactionRanges.put(CodeName,F);
            String SimpleUniqueCodeName = F.name().toUpperCase()+"."+CodeName;
            if(!hashedFactionRanges.containsKey(SimpleUniqueCodeName))
                hashedFactionRanges.put(SimpleUniqueCodeName,F);
            String UniqueCodeName = SimpleUniqueCodeName.replace(' ','_');
            if(!hashedFactionRanges.containsKey(UniqueCodeName))
                hashedFactionRanges.put(UniqueCodeName,F);
        }
        addFaction(factionID,F);
        return F;
	}

	public void addFaction(String factionID, Faction F)
	{
        factionSet.put(factionID.toUpperCase().trim(),F);
	}
	
	public Faction getFaction(String factionID) 
	{
	    if(factionID==null) return null;
		Faction F=(Faction)factionSet.get(factionID.toUpperCase());
		if(F!=null) return F;
		CMFile FILE=new CMFile(Resources.makeFileResourceName(factionID),null,true);
		if(!FILE.exists()) return null;
        StringBuffer buf=FILE.text();
	    if((buf!=null)&&(buf.length()>0))
	    	return buildFactionFromXML(buf, factionID);
        return null;
	}
	
	public Faction getFactionByRangeCodeName(String rangeCodeName)
	{
	    if(hashedFactionRanges.containsKey(rangeCodeName.toUpperCase()))
	        return (Faction)hashedFactionRanges.get(rangeCodeName.toUpperCase());
	    return null;
	}
	
	public Faction getFactionByName(String factionNamed) 
	{
		Faction F;
	    for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	    {
	        F=(Faction)factionSet.get(e.nextElement());
	        if(F.name().equalsIgnoreCase(factionNamed)) 
	        	return F;
	    }
	    return null;
	}
	
	public boolean removeFaction(String factionID) 
	{
		Faction F;
	    if(factionID==null) 
	    {
	        for(Enumeration e=factionSet.keys();e.hasMoreElements();) 
	        {
	            F=(Faction)factionSet.get(e.nextElement());
	            removeFaction(F.factionID());
	        }
	        return true;
	    }
        F=getFactionByName(factionID);
        if(F==null) F=getFaction(factionID);
        if(F==null) return false;
        Resources.removeResource(F.factionID());
        factionSet.remove(F.factionID().toUpperCase());
        return true;
	}
	
	public String listFactions() 
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
	        msg.append(CMStrings.padRight(f.name(),30));
	        msg.append(" | ");
	        msg.append(CMStrings.padRight(f.factionID(),39));
	        msg.append(" |\n\r");
	    }
	    msg.append("+--------------------------------+-----------------------------------------+\n\r");
	    msg.append("\n\r");
	    return msg.toString();
	}
	
	public String name(){return "Factions";}
	public long getTickStatus(){ return Tickable.STATUS_NOT;}
	public String getName(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.name(); return ""; }
	public int getMinimum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.minimum(); return 0; }
	public int getMaximum(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.maximum(); return 0; }
	public int getPercent(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercent(faction); return 0; }
	public int getPercentFromAvg(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.asPercentFromAvg(faction); return 0; }
	public Faction.FactionRange getRange(String factionID, int faction) { Faction f=getFaction(factionID); if(f!=null) return f.fetchRange(faction); return null; }
	public Enumeration<Faction.FactionRange> getRanges(String factionID) { 
        Faction f=getFaction(factionID); 
        if(f!=null) return f.ranges(); 
        return null; 
    }
	public double getRangePercent(String factionID, int faction) 
    { 
        Faction F=getFaction(factionID); 
        if(F==null) return 0.0;
        return CMath.div((int)Math.round(CMath.div((faction - F.minimum()),(F.maximum() - F.minimum())) * 10000.0),100.0);
    }
	public int getTotal(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return (f.maximum()-f.minimum()); return 0; }
	public int getRandom(String factionID) {  Faction f=getFaction(factionID); if(f!=null) return f.randomFaction(); return 0; }
	
	public String AlignID() { return "alignment.ini"; }
	public void setAlignment(MOB mob, int newAlignment)
	{
	    if(getFaction(AlignID())!=null) 
	        mob.addFaction(AlignID(),getAlignThingie(newAlignment));
	}
	
	public void setAlignmentOldRange(MOB mob, int oldRange)
	{
		if(getFaction(AlignID())!=null)
		{
			if(oldRange>=650)
				setAlignment(mob,Faction.ALIGN_GOOD);
			else
			if(oldRange>=350) 
			    setAlignment(mob,Faction.ALIGN_NEUTRAL);
			else
			if(oldRange>=0) 
			    setAlignment(mob,Faction.ALIGN_EVIL);
			else{ /* a -1 value is the new norm */}
		}
	}
	
    public boolean postChangeAllFactions(MOB mob, MOB victim, int amount, boolean quiet)
    {
        if((mob==null))
            return false;
        CMMsg msg=CMClass.getMsg(mob,victim,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,""+quiet);
        msg.setValue(amount);
        if(mob.location()!=null)
        {
            if(mob.location().okMessage(mob,msg))
                mob.location().send(mob,msg);
            else
                return false;
        }
        return true;
    }
    
    public boolean postFactionChange(MOB mob,Environmental tool, String factionID, int amount)
	{
		if((mob==null))
			return false;
		CMMsg msg=CMClass.getMsg(mob,null,tool,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,factionID);
		msg.setValue(amount);
		if(mob.location()!=null)
		{
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		return true;
	}

    protected Faction makeReactionFaction(String prefix, String classID, String Name, String baseTemplateFilename)
    {
    	String codedName=Name.toUpperCase().trim().replace(' ','_');
    	String factionID=prefix+codedName;
    	Faction templateF=getFaction("factions/"+codedName.toLowerCase()+".ini");
    	if(templateF==null)
    		templateF=getFaction(baseTemplateFilename);
    	if(templateF==null)
    	{
    		Log.errOut("Factions","Could not find base template '"+baseTemplateFilename+"'");
    		return null;
    	}
    	StringBuffer buf = rebuildFactionProperties(templateF);
		factionSet.remove(templateF.factionID().toUpperCase().trim());
    	String bufStr = buf.toString();
    	bufStr = CMStrings.replaceAll(bufStr,"<NAME>",Name);
    	bufStr = CMStrings.replaceAll(bufStr,"<FACTIONID>",factionID);
    	bufStr = CMStrings.replaceAll(bufStr,"<CLASSID>",classID);
    	buf=new StringBuffer(bufStr);
    	Faction F=buildFactionFromXML(buf, factionID);
    	F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_IGNOREAUTO);
    	F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_NEVERSAVE);
    	F.setInternalFlags(F.getInternalFlags() | Faction.IFLAG_CUSTOMTICK);
        addFaction(factionID,F);
    	return F;
    }
    
    public Faction[] getSpecialFactions(MOB mob, Room R)
    {
        if((mob==null)||(R==null))
        	return null;
        Faction F=null;
        String SPECIALTYPE=CMProps.getVar(CMProps.SYSTEM_AUTOREACTION).toUpperCase().trim();
        if((SPECIALTYPE==null)||(SPECIALTYPE.length()==0))
        	return null;
        if(SPECIALTYPE.equals("AREA"))
        {
        	Area A=R.getArea();
        	if(A!=null)
        	{
	        	String areaCode = A.Name().toUpperCase().trim().replace(' ','_');
	        	F=getFaction("AREA_"+areaCode);
	        	if(F==null)
	        		F=makeReactionFaction("AREA_",A.ID(),A.Name(),"examples/areareaction.ini");
	        	if(F==null) return null;
	        	return new Faction[]{F};
        	}
        }
        else
        if(SPECIALTYPE.equals("NAME"))
        {
        	Vector<Faction> Fs=new Vector<Faction>();
        	for(int i=0;i<R.numInhabitants();i++)
        	{
        		MOB M=R.fetchInhabitant(i);
        		if((M!=null)&&(M!=mob)&&(M.isMonster()))
        		{
    	        	String nameCode = M.Name().toUpperCase().trim().replace(' ','_');
    	        	F=getFaction("NAME_"+nameCode);
    	        	if(F==null)
    	        		F=makeReactionFaction("NAME_",M.ID(),M.Name(),"examples/namereaction.ini");
    	        	if(F!=null)
    	        		Fs.add(F);
        		}
        	}
        	if(Fs.size()==0) return null;
        	return Fs.toArray(new Faction[0]);
        }
        else
        if(SPECIALTYPE.equals("RACE"))
        {
        	Vector<Faction> Fs=new Vector<Faction>();
        	HashSet<Race> done=new HashSet<Race>(2);
        	for(int i=0;i<R.numInhabitants();i++)
        	{
        		MOB M=R.fetchInhabitant(i);
        		if((M!=null)&&(M!=mob)&&(M.isMonster())&&(!done.contains(M.charStats().getMyRace())))
        		{
        			Race rR=M.charStats().getMyRace();
        			done.add(rR);
    	        	String nameCode = rR.name().toUpperCase().trim().replace(' ','_');
    	        	F=getFaction("RACE_"+nameCode);
    	        	if(F==null)
    	        		F=makeReactionFaction("RACE_",rR.ID(),rR.name(),"examples/racereaction.ini");
    	        	if(F!=null)
    	        		Fs.add(F);
        		}
        	}
        	if(Fs.size()==0) return null;
        	return Fs.toArray(new Faction[0]);
        }
        return null;
    }
    
    
    public void updatePlayerFactions(MOB mob, Room R)
    {
        if((mob==null)||(R==null))
        	return;
        else
    	{
	    	Faction F=null;
	        for(Enumeration e=factions();e.hasMoreElements();)
	        {
	            F=(Faction)e.nextElement();
	            if(((F.getInternalFlags()&Faction.IFLAG_IGNOREAUTO)==0)
	            &&(!F.hasFaction(mob))
	            &&(F.findAutoDefault(mob)!=Integer.MAX_VALUE))
	                mob.addFaction(F.factionID(),F.findAutoDefault(mob));
	        }
    	}
        Faction[] Fs=getSpecialFactions(mob,R);
        if(Fs!=null)
	        for(Faction F : Fs)
	            if((F!=null)&&(!F.hasFaction(mob))&&(F.findAutoDefault(mob)!=Integer.MAX_VALUE))
	                mob.addFaction(F.factionID(),F.findAutoDefault(mob));
    }

    protected void addOutsidersAndTimers(Faction F, Vector<Faction.FactionChangeEvent> outSiders, Vector<Faction.FactionChangeEvent> timers)
    {
	    Faction.FactionChangeEvent[] CEs=null;
        CEs=F.getChangeEvents("ADDOUTSIDER");
        if(CEs!=null)
	        for(int i=0;i<CEs.length;i++)
	        	outSiders.addElement(CEs[i]);
        CEs=F.getChangeEvents("TIME");
        if(CEs!=null)
        for(int i=0;i<CEs.length;i++)
        {
        	if(CEs[i].triggerParameters().length()==0)
            	timers.addElement(CEs[i]);
        	else
        	{
            	int[] ctr=(int[])CEs[i].stateVariable(0);
            	if(ctr==null)
            	{
            		ctr=new int[]{CMath.s_int(CEs[i].getTriggerParm("ROUNDS"))};
            		CEs[i].setStateVariable(0,ctr);
            	}
            	if((--ctr[0])<=0)
            	{
                	ctr[0]=CMath.s_int(CEs[i].getTriggerParm("ROUNDS"));
                	timers.addElement(CEs[i]);
            	}
        	}
        }
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
        if(CMLib.sessions().size()==0) 
            return true;
	    try
	    {
		    Session S=null;
		    MOB mob=null;
		    Faction F=null;
		    Faction.FactionChangeEvent CE=null;
            Vector<Faction.FactionChangeEvent> outSiders=new Vector<Faction.FactionChangeEvent>();
            Vector<Faction.FactionChangeEvent> timers=new Vector<Faction.FactionChangeEvent>();
            HashSet<Faction> factionsDone=new HashSet<Faction>();
            for(Enumeration e=factionSet.elements();e.hasMoreElements();)
            {
                F=(Faction)e.nextElement();
                if((F.getInternalFlags()&Faction.IFLAG_CUSTOMTICK)==0)
                {
                	addOutsidersAndTimers(F, outSiders, timers);
                	factionsDone.add(F);
                }
            }
            Room R;
            Faction[] Fs;
		    for(int s=0;s<CMLib.sessions().size();s++)
		    {
		        S=CMLib.sessions().elementAt(s);
		        mob=(!S.killFlag())?S.mob():null;
		        R=(mob==null)?null:mob.location();
		        if(R!=null)
		        {
		        	Fs=getSpecialFactions(mob, R);
		        	if(Fs!=null)
		        		for(Faction sF : Fs)
		        			if(!factionsDone.contains(sF))
			        		{
		                    	addOutsidersAndTimers(sF, outSiders, timers);
		                    	factionsDone.add(sF);
			        		}
                    for(int o=0;o<outSiders.size();o++)
		            {
		                CE=(Faction.FactionChangeEvent)outSiders.elementAt(o);
		                if((CE.applies(mob,mob))&&(!CE.getFaction().hasFaction(mob)))
		                	CE.getFaction().executeChange(mob,mob,CE);
		            }
                    for(int o=0;o<timers.size();o++)
                    {
                        CE=(Faction.FactionChangeEvent)timers.elementAt(o);
		                if((CE.applies(mob,mob))&&(CE.getFaction().hasFaction(mob)))
		                	CE.getFaction().executeChange(mob,mob,CE);
		            }
		        }
		    }
	    }catch(Exception e){ Log.errOut("Factions",e);}
	    return true;
	}
	
	public int getAlignPurity(int faction, int AlignEq) 
	{
		int bottom=Integer.MAX_VALUE;
		int top=Integer.MIN_VALUE;
        int pct=getPercent(AlignID(),faction);
		Enumeration e = getRanges(AlignID());
        if(e!=null)
		for(;e.hasMoreElements();) 
        {
			Faction.FactionRange R=(Faction.FactionRange)e.nextElement();
			if(R.alignEquiv()==AlignEq) 
            {
				if(R.low()<bottom) bottom=R.low();
				if(R.high()>top) top=R.high();
			}
		}
		switch(AlignEq) 
        {
			case Faction.ALIGN_GOOD:
				return Math.abs(pct - getPercent(AlignID(),top));
			case Faction.ALIGN_EVIL:
				return Math.abs(getPercent(AlignID(),bottom) - pct);
			case Faction.ALIGN_NEUTRAL:
				return Math.abs(getPercent(AlignID(),(int)Math.round(CMath.div((top+bottom),2))) - pct);
			default:
				return 0;
		}
	}
	
	// Please don't mock the name, I couldn't think of a better one.  Sadly.
	public int getAlignThingie(int AlignEq) 
	{
        int bottom=Integer.MAX_VALUE;
        int top=Integer.MIN_VALUE;
		Enumeration e = getRanges(AlignID());
	    if(e==null) return 0;
		for(;e.hasMoreElements();) 
        {
			Faction.FactionRange R=(Faction.FactionRange)e.nextElement();
			if(R.alignEquiv()==AlignEq) {
				if(R.low()<bottom) bottom=R.low();
				if(R.high()>top) top=R.high();
			}
		}
		switch(AlignEq) 
		{
			case Faction.ALIGN_GOOD:
				return top;
			case Faction.ALIGN_EVIL:
				return bottom;
			case Faction.ALIGN_NEUTRAL:
				return (int)Math.round(CMath.div((top+bottom),2));
			default:
				return 0;
		}
	}
    public int isFactionTag(String tag)
    {
        for(int i=0;i<Faction.TAG_NAMES.length;i++)
            if(tag.equalsIgnoreCase(Faction.TAG_NAMES[i]))
                return i;
            else
            if(Faction.TAG_NAMES[i].endsWith("*")&&tag.startsWith(Faction.TAG_NAMES[i].substring(0,Faction.TAG_NAMES[i].length()-1)))
                return i;
        return -1;
    }
    public int getAlignEquiv(String str)
    {
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_GOOD])) 
            return Faction.ALIGN_GOOD;
        else 
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL])) 
            return Faction.ALIGN_NEUTRAL;
        else 
        if(str.equalsIgnoreCase(Faction.ALIGN_NAMES[Faction.ALIGN_EVIL])) 
            return  Faction.ALIGN_EVIL;
        else 
            return  Faction.ALIGN_INDIFF;
    }
    
    private String getWordAffOrBehav(String ID)
    {
        if(CMClass.getBehavior(ID)!=null)
            return "behavior";
        if(CMClass.getAbility(ID)!=null)
            return "ability";
        if(CMClass.getCommand(ID)!=null)
            return "command";
        return null;
    }
    
    
    public void modifyFaction(MOB mob, Faction me) throws IOException
    {
        if(mob.isMonster())
            return;
        boolean ok=false;
        int showFlag=-1;
        if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
            showFlag=-999;
        while((mob.session()!=null)&&(!mob.session().killFlag())&&(!ok))
        {
            int showNumber=0;
            // name
            me.setName(CMLib.genEd().prompt(mob,me.name(),++showNumber,showFlag,"Name"));

            // ranges
            ++showNumber;
            if(!me.ranges().hasMoreElements())
                me.addRange("0;100;Sample Range;SAMPLE;");
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction Division/Ranges List:\n\r");
                list.append(CMStrings.padRight("   Code",16)+CMStrings.padRight("Name",21)+CMStrings.padRight("Min",11)+CMStrings.padRight("Max",11)+CMStrings.padRight("Align",6)+"\n\r");
                for(Enumeration e=me.ranges();e.hasMoreElements();)
                {
                    Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
                    list.append(CMStrings.padRight("   "+FR.codeName(),15)+" ");
                    list.append(CMStrings.padRight(FR.name(),20)+" ");
                    list.append(CMStrings.padRight(""+FR.low(),10)+" ");
                    list.append(CMStrings.padRight(""+FR.high(),10)+" ");
                    list.append(CMStrings.padRight(Faction.ALIGN_NAMES[FR.alignEquiv()],5)+"\n\r");
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a CODE to add, remove, or modify:","");
                if(which.length()==0)
                    break;
                which=which.trim().toUpperCase();
                if(which.indexOf(' ')>=0)
                {
                    mob.tell("Faction Range code names may not contain spaces.");
                    break;
                }
                Faction.FactionRange FR=me.fetchRange(which);
                if(FR==null)
                {
                    if(mob.session().confirm("Create a new range code named '"+which+"' (y/N): ","N"))
                    {
                        FR=me.addRange("0;100;Change My Name;"+which+";");
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.delRange(FR);
                    mob.tell("Range deleted.");
                    FR=null;
                }
                if(FR!=null)
                {
                    String newName=mob.session().prompt("Enter a new name ("+FR.name()+")\n\r: ",FR.name());
                    boolean error99=false;
                    if(newName.length()==0)
                        error99=true;
                    else
                    for(Enumeration e=me.ranges();e.hasMoreElements();)
                    {
                        Faction.FactionRange FR3=(Faction.FactionRange)e.nextElement();
                        if(FR3.name().equalsIgnoreCase(FR.name())&&(FR3!=FR))
                        { mob.tell("A range already exists with that name!"); error99=true; break;}
                    }
                    if(error99)
                        mob.tell("(no change)");
                    else
                        FR.setName(newName);
                    newName=mob.session().prompt("Enter the low end of the range ("+FR.low()+")\n\r: ",""+FR.low());
                    if(!CMath.isInteger(newName))
                        mob.tell("(no change)");
                    else
                        FR.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt("Enter the high end of the range ("+FR.high()+")\n\r: ",""+FR.high());
                    if((!CMath.isInteger(newName))||(CMath.s_int(newName)<FR.low()))
                        mob.tell("(no change)");
                    else
                        FR.setHigh(CMath.s_int(newName));
                    StringBuffer prompt=new StringBuffer("Select the 'virtue' (if any) of this range:\n\r");
                    StringBuffer choices=new StringBuffer("");
                    for(int r=0;r<Faction.ALIGN_NAMES.length;r++)
                    {
                        choices.append(""+r);
                        if(r==Faction.ALIGN_INDIFF)
                            prompt.append(r+") Not applicable\n\r");
                        else
                            prompt.append(r+") "+Faction.ALIGN_NAMES[r].toLowerCase()+"\n\r");
                    }
                    FR.setAlignEquiv(CMath.s_int(mob.session().choose(prompt.toString()+"Enter alignment equivalency or 0: ",choices.toString(),""+FR.alignEquiv())));
                }
            }


            // show in score
            me.setShowInScore(CMLib.genEd().prompt(mob,me.showInScore(),++showNumber,showFlag,"Show in 'Score'"));

            // show in factions
            me.setShowInFactionsCommand(CMLib.genEd().prompt(mob,me.showInFactionsCommand(),++showNumber,showFlag,"Show in 'Factions' command"));

            // show in special reports
            boolean alreadyReporter=false;
            for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
            {
                Faction F2=(Faction)e.nextElement();
                if(F2.showInSpecialReported()) alreadyReporter=true;
            }
            if(!alreadyReporter)
                me.setShowInSpecialReported(CMLib.genEd().prompt(mob,me.showInSpecialReported(),++showNumber,showFlag,"Show in Reports"));

            // show in editor
            me.setShowInEditor(CMLib.genEd().prompt(mob,me.showInEditor(),++showNumber,showFlag,"Show in MOB Editor"));

            // auto defaults
            boolean error=true;
            me.setAutoDefaults(CMParms.parseSemicolons(CMLib.genEd().prompt(mob,CMParms.toSemicolonList(me.autoDefaults()),++showNumber,showFlag,"Optional automatic assigned values with zapper masks (semicolon delimited).\n\r    "),true));

            // non-auto defaults
            error=true;
            if(!me.defaults().hasMoreElements())
                me.setDefaults(CMParms.makeVector("0"));
            ++showNumber;
            while(error&&(mob.session()!=null)&&(!mob.session().killFlag()))
            {
                error=false;
                String newDefaults=CMLib.genEd().prompt(mob,CMParms.toSemicolonList(me.defaults()),showNumber,showFlag,"Other default values with zapper masks (semicolon delimited).\n\r    ");
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                Vector V=CMParms.parseSemicolons(newDefaults,true);
                if(V.size()==0)
                {
                    mob.tell("This field may not be empty.");
                    error=true;
                }
                me.setDefaults(CMParms.parseSemicolons(newDefaults,true));
            }

            // choices and choice intro
            me.setChoices(CMParms.parseSemicolons(CMLib.genEd().prompt(mob,CMParms.toSemicolonList(me.choices()),++showNumber,showFlag,"Optional new player value choices (semicolon-delimited).\n\r    "),true));
            if(me.choices().hasMoreElements())
                me.setChoiceIntro(CMLib.genEd().prompt(mob,me.choiceIntro(),++showNumber,showFlag,"Optional choices introduction text. Filename"));

            // rate modifier
            String newModifier=CMLib.genEd().prompt(mob,CMath.toPct(me.rateModifier()),++showNumber,showFlag,"Rate modifier");
            if((CMath.isNumber(newModifier))||(CMath.isPct(newModifier)))
                me.setRateModifier(CMath.s_pct(newModifier));

            // experience flag
            boolean error2=true;
            ++showNumber;
            while(error2&&(mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                error2=false;
                StringBuffer nextPrompt=new StringBuffer("\n\r");
                int myval=-1;
                for(int i=0;i<Faction.EXPAFFECT_NAMES.length;i++)
                {
                    if(me.experienceFlag().equalsIgnoreCase(Faction.EXPAFFECT_NAMES[i]))
                        myval=i;
                    nextPrompt.append("  "+(i+1)+") "+CMStrings.capitalizeAndLower(Faction.EXPAFFECT_NAMES[i].toLowerCase())+"\n\r");
                }
                if(myval<0){ me.setExperienceFlag("NONE"); myval=0;}
                if((showFlag!=showNumber)&&(showFlag>-999))
                {
                    mob.tell(showNumber+". Affect on experience: "+Faction.EXPAFFECT_NAMES[myval]);
                    break;
                }
                String prompt="Affect on experience:  "+Faction.EXPAFFECT_NAMES[myval]+nextPrompt.toString()+"\n\rSelect a value: ";
                int mynewval=CMLib.genEd().prompt(mob,myval+1,showNumber,showFlag,prompt);
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                if((mynewval<=0)||(mynewval>Faction.EXPAFFECT_NAMES.length))
                {
                    mob.tell("That value is not valid.");
                    error2=true;
                }
                else
                    me.setExperienceFlag(Faction.EXPAFFECT_NAMES[mynewval-1]);
            }

            // factors by mask
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction change adjustment Factors with Zapper Masks:\n\r");
                list.append("    #) "+CMStrings.padRight("Zapper Mask",31)+CMStrings.padRight("Gain",6)+CMStrings.padRight("Loss",6)+"\n\r");
                StringBuffer choices=new StringBuffer("");
                int numFactors=0;
                for(Enumeration<Faction.FactionZapFactor> e=me.factors();e.hasMoreElements();)
                {
                    Faction.FactionZapFactor factor=e.nextElement();
                    choices.append(((char)('A'+numFactors)));
                    list.append("    "+(((char)('A'+numFactors))+") "));
                    list.append(CMStrings.padRight(factor.MOBMask(),30)+" ");
                    list.append(CMStrings.padRight(""+CMath.toPct(factor.gainFactor()),5)+" ");
                    list.append(CMStrings.padRight(""+CMath.toPct(factor.lossFactor()),5)+"\n\r");
                    numFactors++;
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Enter a # to remove, or modify, or enter 0 to Add:","0"+choices.toString(),"").trim().toUpperCase();
                int factorNum=choices.toString().indexOf(which);
                if((which.length()!=1)
                ||((!which.equalsIgnoreCase("0"))
                    &&((factorNum<0)||(factorNum>=numFactors))))
                    break;
                Faction.FactionZapFactor factor=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    factor=me.getFactor(factorNum);
                    if(factor!=null)
                        if(mob.session().choose("Would you like to M)odify or D)elete this range (M/d): ","MD","M").toUpperCase().startsWith("D"))
                        {
                            me.delFactor(factor);
                            mob.tell("Factor deleted.");
                            factor=null;
                        }
                }
                else
                    factor=me.addFactor(1.0,1.0,"");
                if(factor!=null)
                {
                    String mask=mob.session().prompt("Enter a new zapper mask ("+factor.MOBMask()+")\n\r: ",factor.MOBMask());
                    double newHigh=factor.gainFactor();
                    String newName=mob.session().prompt("Enter gain adjustment ("+CMath.toPct(newHigh)+"): ",CMath.toPct(newHigh));
                    if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
                        mob.tell("(no change)");
                    else
                        newHigh=CMath.s_pct(newName);

                    double newLow=factor.lossFactor();
                    newName=mob.session().prompt("Enter loss adjustment ("+CMath.toPct(newLow)+"): ",CMath.toPct(newLow));
                    if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
                        mob.tell("(no change)");
                    else
                        newLow=CMath.s_pct(newName);
                    me.delFactor(factor);
                    factor=me.addFactor(newHigh,newLow,mask);
                }
            }

            // relations between factions
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Cross-Faction Relations:\n\r");
                list.append("    Faction"+CMStrings.padRight("",25)+"Percentage change\n\r");
                for(Enumeration e=me.relationFactions();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    double value=me.getRelation(key);
                    Faction F=CMLib.factions().getFaction(key);
                    if(F!=null)
                    {
                        list.append("    "+CMStrings.padRight(F.name(),31)+" ");
                        list.append(CMath.toPct(value));
                        list.append("\n\r");
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Enter a faction to add, remove, or modify relations:","");
                if(which.length()==0)
                    break;
                Faction theF=null;
                for(Enumeration e=me.relationFactions();e.hasMoreElements();)
                {
                    String key=(String)e.nextElement();
                    Faction F=CMLib.factions().getFaction(key);
                    if((F!=null)&&(F.name().equalsIgnoreCase(which)))
                        theF=F;
                }
                if(theF==null)
                {
                    Faction possibleF=CMLib.factions().getFaction(which);
                    if(possibleF==null) possibleF=CMLib.factions().getFactionByName(which);
                    if(possibleF==null)
                        mob.tell("'"+which+"' is not a valid faction.");
                    else
                    if(mob.session().confirm("Create a new relation for faction  '"+possibleF.name()+"' (y/N):","N"))
                    {
                        theF=possibleF;
                        me.addRelation(theF.factionID(),1.0);
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this relation (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.delRelation(theF.factionID());
                    mob.tell("Relation deleted.");
                    theF=null;
                }
                if(theF!=null)
                {
                    String amount=CMath.toPct(me.getRelation(theF.factionID()));
                    String newName=mob.session().prompt("Enter a relation amount ("+amount+"): ",""+amount);
                    if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
                        mob.tell("(no change)");
                    me.delRelation(theF.factionID());
                    me.addRelation(theF.factionID(),CMath.s_pct(newName));
                }
            }

            // faction change triggers
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                StringBuffer list=new StringBuffer(showNumber+". Faction Change Triggers:\n\r");
                list.append("    "+CMStrings.padRight("Type",15)
                        +" "+CMStrings.padRight("Direction",10)
                        +" "+CMStrings.padRight("Factor",10)
                        +" "+CMStrings.padRight("Flags",20)
                        +" Mask\n\r");
                int numChanges=0;
                StringBuffer choices=new StringBuffer("");
                Hashtable<Character,Faction.FactionChangeEvent> choicesHashed=new Hashtable<Character,Faction.FactionChangeEvent>(); 
                for(Enumeration e=me.changeEventKeys();e.hasMoreElements();)
                {
                    Faction.FactionChangeEvent[] CEs=me.getChangeEvents((String)e.nextElement());
                    if(CEs!=null)
                    {
                    	for(int e1=0;e1<CEs.length;e1++)
                    	{
                    		Faction.FactionChangeEvent CE=CEs[e1];
                            choices.append(((char)('A'+numChanges)));
                            list.append(" "+(((char)('A'+numChanges))+") "));
                            choicesHashed.put(Character.valueOf((char)('A'+numChanges)), CE);
	                        if(CE.triggerParameters().trim().length()==0)
		                        list.append(CMStrings.padRight(CE.eventID(),15)+" ");
	                        else
		                        list.append(CMStrings.padRight(CE.eventID()+":"+CE.triggerParameters(),15)+" ");
	                        list.append(CMStrings.padRight(Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CE.direction()],10)+" ");
	                        list.append(CMStrings.padRight(CMath.toPct(CE.factor()),10)+" ");
	                        list.append(CMStrings.padRight(CE.flagCache(),20)+" ");
	                        list.append(CE.targetZapper()+"\n\r");
	                        numChanges++;
                    	}
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().prompt("Select a ID to add, remove, or modify:","");
                which=which.toUpperCase().trim();
                if(which.length()==0) continue;
                Faction.FactionChangeEvent CE=(which.length()>0)?choicesHashed.get(Character.valueOf(which.charAt(0))):null;
                if(CE==null)
                {
                    String newID=mob.session().prompt("Enter a new change ID (?): ").toUpperCase().trim();
                    if(newID.length()==0) break;
                    if(newID.equalsIgnoreCase("?"))
                    {
                        mob.tell("Valid triggers: \n\r"+me.ALL_CHANGE_EVENT_TYPES());
                        continue;
                    }
                    CE=me.createChangeEvent(newID);
                    if(CE==null)
                    {
                        mob.tell("That ID is invalid.  Try '?'.");
                        continue;
                    }
                    else
                    if(!mob.session().confirm("Create a new trigger using ID '"+newID+"' (y/N): ","N"))
                    {
                        me.delChangeEvent(CE);
                        CE=null;
                        break;
                    }
                }
                else
                if(mob.session().choose("Would you like to M)odify or D)elete this trigger (M/d): ","MD","M").toUpperCase().startsWith("D"))
                {
                    me.delChangeEvent(CE);
                    mob.tell("Trigger deleted.");
                    CE=null;
                }

                if(CE!=null)
                {
                    String newFlags=mob.session().prompt("Trigger parms ("+CE.triggerParameters()+"): ",CE.triggerParameters());
                    if((newFlags.length()==0)||(newFlags.equals(CE.triggerParameters())))
                        mob.tell("(no change)");
                    else
                        CE.setTriggerParameters(newFlags.trim());
                }
                if(CE!=null)
                {
                    StringBuffer directions=new StringBuffer("Valid directions:\n\r");
                    StringBuffer cmds=new StringBuffer("");
                    for(int i=0;i<Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS.length;i++)
                    {
                        directions.append(((char)('A'+i))+") "+Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[i]+"\n\r");
                        cmds.append((char)('A'+i));
                    }
                    String str=mob.session().choose(directions+"\n\rSelect a new direction ("+Faction.FactionChangeEvent.CHANGE_DIRECTION_DESCS[CE.direction()]+"): ",cmds.toString()+"\n\r","");
                    if((str.length()==0)||str.equals("\n")||str.equals("\r")||(cmds.toString().indexOf(str.charAt(0))<0))
                        mob.tell("(no change)");
                    else
                        CE.setDirection((cmds.toString().indexOf(str.charAt(0))));
                }
                if(CE!=null)
                {
                    if(CE.factor()==0.0) CE.setFactor(1.0);
                    String amount=CMath.toPct(CE.factor());
                    String newName=mob.session().prompt("Enter the amount factor ("+amount+"): ",""+amount);
                    if((!CMath.isNumber(newName))&&(!CMath.isPct(newName)))
                        mob.tell("(no change)");
                    else
                        CE.setFactor(CMath.s_pct(newName));
                }
                if(CE!=null)
                {
                    mob.tell("Valid flags include: "+CMParms.toStringList(Faction.FactionChangeEvent.FLAG_DESCS)+"\n\r");
                    String newFlags=mob.session().prompt("Enter new flag(s) ("+CE.flagCache()+"): "+CE.flagCache(),CE.flagCache());
                    if((newFlags.length()==0)||(newFlags.equals(CE.flagCache())))
                        mob.tell("(no change)");
                    else
                        CE.setFlags(newFlags);
                }
                if(CE!=null)
                {
                    String newFlags=mob.session().prompt("Zapper mask ("+CE.targetZapper()+"): "+CE.targetZapper(),CE.targetZapper());
                    if((newFlags.length()==0)||(newFlags.equals(CE.targetZapper())))
                        mob.tell("(no change)");
                    else
                        CE.setTargetZapper(newFlags);
                }
            }

            // Ability allowances
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(showNumber+". Ability allowances:\n\r");
                list.append("    #) "
                        +CMStrings.padRight("Ability masks",40)
                        +" "+CMStrings.padRight("Low value",10)
                        +" "+CMStrings.padRight("High value",10)
                        +"\n\r");
                int numUsages=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                for(Enumeration e=me.abilityUsages();e.hasMoreElements();)
                {
                    Faction.FactionAbilityUsage CA=(Faction.FactionAbilityUsage)e.nextElement();
                    if(CA!=null)
                    {
                        list.append("    "+((char)('A'+numUsages)+") "));
                        list.append(CMStrings.padRight(CA.abilityFlags(),40)+" ");
                        list.append(CMStrings.padRight(CA.low()+"",10)+" ");
                        list.append(CMStrings.padRight(CA.high()+"",10)+" ");
                        list.append("\n\r");
                        choices.append((char)('A'+numUsages));
                        numUsages++;
                    }
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Select an allowance to remove or modify, or enter 0 to Add:",choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                Faction.FactionAbilityUsage CA=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    int num=(which.charAt(0)-'A');
                    if((num<0)||(num>=numUsages))
                        break;
                    CA=(Faction.FactionAbilityUsage)me.getAbilityUsage(num);
                    if(CA==null)
                    {
                        mob.tell("That allowance is invalid..");
                        continue;
                    }
                    if(mob.session().choose("Would you like to M)odify or D)elete this allowance (M/d): ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.delAbilityUsage(CA);
                        mob.tell("Allowance deleted.");
                        CA=null;
                    }
                }
                else
                if(!mob.session().confirm("Create a new allowance (y/N): ","N"))
                {
                    continue;
                }
                else
                    CA=me.addAbilityUsage(null);
                if(CA!=null)
                {
                    boolean cont=false;
                    while((!cont)&&(!mob.session().killFlag()))
                    {
                        String newFlags=mob.session().prompt("Ability determinate masks or ? ("+CA.abilityFlags()+"): "+CA.abilityFlags(),CA.abilityFlags());
                        if(newFlags.equalsIgnoreCase("?"))
                        {
                            StringBuffer vals=new StringBuffer("Valid masks: \n\r");
                            for(int i=0;i<Ability.ACODE_DESCS.length;i++)
                                vals.append(Ability.ACODE_DESCS[i]+", ");
                            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
                                vals.append(Ability.DOMAIN_DESCS[i]+", ");
                            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                                vals.append(Ability.FLAG_DESCS[i]+", ");
                            vals.append(" * Any ABILITY ID (skill/prayer/spell/etc)");
                            mob.tell(vals.toString());
                            cont=false;
                        }
                        else
                        {
                            cont=true;
                            if((newFlags.length()==0)||(newFlags.equals(CA.abilityFlags())))
                                mob.tell("(no change)");
                            else
                            {
                                Vector unknowns=CA.setAbilityFlag(newFlags);
                                if(unknowns.size()>0)
                                    for(int i=unknowns.size()-1;i>=0;i--)
                                        if(CMClass.getAbility((String)unknowns.elementAt(i))!=null)
                                            unknowns.removeElementAt(i);
                                if(unknowns.size()>0)
                                {
                                    mob.tell("The following are unknown masks: '"+CMParms.toStringList(unknowns)+"'.  Please correct them.");
                                    cont=false;
                                }
                            }
                        }
                    }
                    String newName=mob.session().prompt("Enter the minimum value to use the ability ("+CA.low()+"): ",""+CA.low());
                    if((!CMath.isInteger(newName))||(CA.low()==CMath.s_int(newName)))
                        mob.tell("(no change)");
                    else
                        CA.setLow(CMath.s_int(newName));
                    newName=mob.session().prompt("Enter the maximum value to use the ability ("+CA.high()+"): ",""+CA.high());
                    if((!CMath.isInteger(newName))||(CA.high()==CMath.s_int(newName)))
                        mob.tell("(no change)");
                    else
                        CA.setHigh(CMath.s_int(newName));
                    if(CA.high()<CA.low()) CA.setHigh(CA.low());
                }
            }


            // Affects/Behaviors
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(showNumber+". Effects/Behaviors:\n\r");
                list.append("    #) "
                        +CMStrings.padRight("Ability/Behavior ID",25)
                        +" "+CMStrings.padRight("MOB Mask",20)
                        +" "+CMStrings.padRight("Parameters",20)
                        +"\n\r");
                int numAffBehavs=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                Vector IDs=new Vector();
                String ID=null;
                for(Enumeration e=me.affectsBehavs();e.hasMoreElements();)
                {
                    ID=(String)e.nextElement();
                    String[] parms=me.getAffectBehav(ID);
                    list.append("    "+((char)('A'+numAffBehavs)+") "));
                    list.append(CMStrings.padRight(ID,25)+" ");
                    list.append(CMStrings.padRight(parms[1]+"",20)+" ");
                    list.append(CMStrings.padRight(parms[0]+"",20)+" ");
                    list.append("\n\r");
                    choices.append((char)('A'+numAffBehavs));
                    IDs.addElement(ID);
                    numAffBehavs++;
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Select an ability/behavior ID to remove or modify, or enter 0 to Add:",choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                if(!which.equalsIgnoreCase("0"))
                {
                    int num=(which.charAt(0)-'A');
                    if((num<0)||(num>=IDs.size()))
                        break;
                    ID=(String)IDs.elementAt(num);
                    String type=getWordAffOrBehav(ID);
                    if(mob.session().choose("Would you like to M)odify or D)elete this "+type+" (M/d): ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.delAffectBehav(ID);
                        mob.tell(CMStrings.capitalizeAndLower(type)+" deleted.");
                        ID=null;
                    }
                }
                else
                {
                    boolean cont=true;
                    while((cont)&&(!mob.session().killFlag()))
                    {
                        cont=false;
                        ID=mob.session().prompt("Enter a new Ability or Behavior ID or ?: ");
                        if(ID.equalsIgnoreCase("?"))
                        {
                            StringBuffer vals=new StringBuffer("Valid IDs: \n\r");
                            vals.append(CMParms.toCMObjectStringList(CMClass.abilities()));
                            vals.append(CMParms.toCMObjectStringList(CMClass.behaviors()));
                            mob.tell(vals.toString());
                            cont=true;
                        }
                        else
                        {
                            if((ID.length()==0)||(me.getAffectBehav(ID)!=null))
                            {
                                mob.tell("(nothing done)");
                                ID=null;
                                break;
                            }
                            String type=getWordAffOrBehav(ID);
                            if(type==null)
                            {
                                mob.tell("'"+ID+" is neither a valid behavior ID or ability ID.  Use ? for a list.");
                                cont=true;
                            }
                            else
                            if(!mob.session().confirm("Create a new "+type+" (y/N): ","N"))
                            {
                                ID=null;
                                break;
                            }
                            else
                                me.addAffectBehav(ID,"","");
                        }
                    }
                }
                if(ID!=null)
                {
                    String type=getWordAffOrBehav(ID);
                    String[] oldData=me.getAffectBehav(ID);
                    String[] newData=new String[2];
                    boolean cont=true;
                    while((cont)&&(!mob.session().killFlag()))
                    {
                        cont=false;
                        String mask=mob.session().prompt("Enter a new Zapper Mask or ? ("+oldData[1]+")\n\r: ",oldData[1]);
                        if(mask.equalsIgnoreCase("?"))
                        {
                            mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
                            cont=true;
                        }
                        else
                        if(!mask.equals(oldData[1]))
                            newData[1]=mask;
                    }
                    
                    cont=true;
                    while((cont)&&(!mob.session().killFlag()))
                    {
                        cont=false;
                        String parms=mob.session().prompt("Enter new "+type+" parameters for "+ID+" or ? ("+oldData[0]+")\n\r: ",oldData[0]);
                        if(parms.equalsIgnoreCase("?"))
                        {
                            mob.tell(CMLib.help().getHelpText(ID,mob,true).toString());
                            cont=true;
                        }
                        else
                            if(!parms.equals(oldData[0]))
                                newData[0]=parms;
                    }
                    if((newData[0]==null)&&(newData[1]!=null))
                        newData[0]=oldData[0];
                    else
                    if((newData[0]!=null)&&(newData[1]==null))
                        newData[1]=oldData[1];
                    if((newData[0]!=null)&&(newData[1]!=null))
                    {
                        me.delAffectBehav(ID);
                        me.addAffectBehav(ID,newData[0],newData[1]);
                    }
                }
            }

            // Reaction Command/Affects/Behaviors
            ++showNumber;
            while((mob.session()!=null)&&(!mob.session().killFlag())&&(!((showFlag>0)&&(showFlag!=showNumber))))
            {
                if((showFlag>0)&&(showFlag!=showNumber)) break;
                StringBuffer list=new StringBuffer(showNumber+". Reaction Commands/Effects/Behaviors:\n\r");
                list.append("    #) "
                        +CMStrings.padRight("Range",15)
                        +" "+CMStrings.padRight("MOB Mask",18)
                        +" "+CMStrings.padRight("Able/Beh/Cmd",15)
                        +" "+CMStrings.padRight("Parameters",18)
                        +"\n\r");
                int numReactions=0;
                StringBuffer choices=new StringBuffer("0\n\r");
                Vector reactions=new Vector();
                Faction.FactionReactionItem item=null;
                for(Enumeration e=me.reactions();e.hasMoreElements();)
                {
                    item=(Faction.FactionReactionItem)e.nextElement();
                    list.append("    "+((char)('A'+numReactions)+") "));
                    list.append(CMStrings.padRight(item.rangeName(),15)+" ");
                    list.append(CMStrings.padRight(item.presentMOBMask()+"",18)+" ");
                    list.append(CMStrings.padRight(item.reactionObjectID()+"",15)+" ");
                    list.append(CMStrings.padRight(item.parameters()+"",18)+" ");
                    list.append("\n\r");
                    choices.append((char)('A'+numReactions));
                    reactions.addElement(item);
                    numReactions++;
                }
                mob.tell(list.toString());
                if((showFlag!=showNumber)&&(showFlag>-999)) break;
                String which=mob.session().choose("Select one to remove or modify, or enter 0 to Add:",choices.toString(),"");
                if(which.length()!=1)
                    break;
                which=which.toUpperCase().trim();
                item=null;
                if(!which.equalsIgnoreCase("0"))
                {
                    int num=(which.charAt(0)-'A');
                    if((num<0)||(num>=reactions.size()))
                        break;
                    item=(Faction.FactionReactionItem)reactions.elementAt(num);
                    String type=getWordAffOrBehav(item.reactionObjectID());
                    if(mob.session().choose("Would you like to M)odify or D)elete this "+type+" (M/d): ","MD","M").toUpperCase().startsWith("D"))
                    {
                        me.delReaction(item);
                        mob.tell(CMStrings.capitalizeAndLower(type)+" deleted.");
                        item=null;
                    }
                }
                
                String type="";
                String[] oldData=new String[]{"","","",""};
                if(item != null)
                {
                    type=getWordAffOrBehav(item.reactionObjectID());
                    oldData=new String[]{item.rangeName(),item.presentMOBMask(),item.reactionObjectID(),item.parameters()};
                }
                
                String[] newData=new String[4];
                
                boolean cont=true;
                
                cont=true;
                while((cont)&&(!mob.session().killFlag()))
                {
                    cont=false;
                    
                    String rangeCode=mob.session().prompt("Enter a new range code or ? ("+oldData[0]+")\n\r: ",oldData[0]).toUpperCase().trim();
                    if(rangeCode.equalsIgnoreCase("?"))
                    {
                    	StringBuffer str=new StringBuffer("");
                        for(Enumeration e=me.ranges();e.hasMoreElements();)
                        {
                            Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
                            str.append(FR.codeName()+" ");
                        }
                        mob.tell(str.toString().trim()+"\n\r");
                        cont=true;
                    }
                    else
                    if(!rangeCode.equals(oldData[0]))
                    {
                    	cont=true;
                        for(Enumeration e=me.ranges();e.hasMoreElements();)
                            if(((Faction.FactionRange)e.nextElement()).codeName().equalsIgnoreCase(rangeCode))
                            {
                                newData[0]=rangeCode;
                                cont=false;
                            }
                        if(cont)
                        	mob.tell("'"+rangeCode+"' is not a valid range code.  Use ?");
                    }
                }
                
                cont = true;
                while((cont)&&(!mob.session().killFlag()))
                {
                    cont=false;
                    
                    String mask=mob.session().prompt("Enter a new Zapper Mask or ? ("+oldData[1]+")\n\r: ",oldData[1]);
                    if(mask.equalsIgnoreCase("?"))
                    {
                        mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
                        cont=true;
                    }
                    else
                    if(!mask.equals(oldData[1]))
                        newData[1]=mask;
                }
                
                cont=true;
                while((cont)&&(!mob.session().killFlag()))
                {
                    cont=false;
	                String ID=mob.session().prompt("Enter a new Ability, Behavior, or Command ID ("+oldData[2]+")\n\r: ",oldData[2]);
	                if(ID.equalsIgnoreCase("?"))
	                {
	                    StringBuffer vals=new StringBuffer("Valid IDs: \n\r");
	                    vals.append(CMParms.toCMObjectStringList(CMClass.abilities()));
	                    vals.append(CMParms.toCMObjectStringList(CMClass.behaviors()));
	                    vals.append(CMParms.toCMObjectStringList(CMClass.commands()));
	                    mob.tell(vals.toString());
	                    cont=true;
	                }
	                else
	                {
	                    type=getWordAffOrBehav(ID);
	                    if(type==null)
	                    {
	                        mob.tell("'"+ID+" is neither a valid behavior, command, ability ID. Use ? for a list.");
	                        cont=true;
	                    }
	                    else
	                        newData[2]=ID;
	                }
                }
                
                cont=true;
                while((cont)&&(!mob.session().killFlag()))
                {
                    cont=false;
                    String parms=mob.session().prompt("Enter new "+type+" parameters for "+newData[2]+" or ? ("+oldData[3]+")\n\r: ",oldData[3]);
                    if(parms.equalsIgnoreCase("?"))
                    {
                        mob.tell(CMLib.help().getHelpText(newData[3],mob,true).toString());
                        cont=true;
                    }
                    else
                    if(!parms.equals(oldData[3]))
                        newData[3]=parms;
                }
                
                for(int n=0;n<oldData.length;n++)
                	if(newData[n]==null)
                		newData[n]=oldData[n];
                if(item==null)
                	me.addReaction(newData[0], newData[1], newData[2], newData[3]);
                else
                {
                	item.setRangeName(newData[0]);
                	item.setPresentMOBMask(newData[1]);
                	item.setReactionObjectID(newData[2]);
                	item.setParameters(newData[3]);
                }
            }
            if(me.reactions().hasMoreElements())
	            me.setLightReactions(CMLib.genEd().prompt(mob,me.useLightReactions(),++showNumber,showFlag,"Use 'Light' Reactions"));
            else
            	me.setLightReactions(false);
            
            if(showFlag<-900){ ok=true; break;}
            if(showFlag>0){ showFlag=-1; continue;}
            showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
            if(showFlag<=0)
            {
                showFlag=-1;
                ok=true;
            }
        }
        
        String errMsg=resaveFaction(me);
        if(errMsg.length()>0)
            mob.tell(errMsg);
    }

    private StringBuffer rebuildFactionProperties(Faction F)
    {
        Vector oldV=Resources.getFileLineVector(Resources.getFileResource(F.factionID(),true));
        if(oldV.size()<10)
        {

        }
        boolean[] defined=new boolean[Faction.TAG_NAMES.length];
        for(int i=0;i<defined.length;i++) defined[i]=false;
        for(int v=0;v<oldV.size();v++)
        {
            String s=(String)oldV.elementAt(v);
            if(!(s.trim().startsWith("#")||s.trim().length()==0||(s.indexOf("=")<0)))
            {
                String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                int tagRef=CMLib.factions().isFactionTag(tag);
                if(tagRef>=0) defined[tagRef]=true;
            }
        }
        boolean[] done=new boolean[Faction.TAG_NAMES.length];
        for(int i=0;i<done.length;i++) done[i]=false;
        int lastCommented=-1;
        String CR="\r\n";
        StringBuffer buf=new StringBuffer("");
        for(int v=0;v<oldV.size();v++)
        {
            String s=(String)oldV.elementAt(v);
            if(s.trim().length()==0)
            {
                if((lastCommented>=0)&&(!done[lastCommented]))
                {
                    done[lastCommented]=true;
                    buf.append(F.getINIDef(Faction.TAG_NAMES[lastCommented],CR)+CR);
                    lastCommented=-1;
                }
            }
            else
            if(s.trim().startsWith("#")||(s.indexOf("=")<0))
            {
                buf.append(s+CR);
                int x=s.indexOf("=");
                if(x>=0)
                {
                    s=s.substring(0,x).trim();
                    int first=s.length()-1;
                    for(;first>=0;first--)
                        if(!Character.isLetterOrDigit(s.charAt(first)))
                            break;
                    first=CMLib.factions().isFactionTag(s.substring(first).trim().toUpperCase());
                    if(first>=0) lastCommented=first;
                }
            }
            else
            {
                String tag=s.substring(0,s.indexOf("=")).trim().toUpperCase();
                int tagRef=CMLib.factions().isFactionTag(tag);
                if(tagRef<0)
                    buf.append(s+CR);
                else
                if(!done[tagRef])
                {
                    done[tagRef]=true;
                    buf.append(F.getINIDef(tag,CR)+CR);
                }
            }
        }
        if((lastCommented>=0)&&(!done[lastCommented]))
            buf.append(F.getINIDef(Faction.TAG_NAMES[lastCommented],CR)+CR);
        return buf;
    }
    
    public String resaveFaction(Faction F)
    {
        if((F.factionID().length()>0)
        &&(CMLib.factions().getFaction(F.factionID())!=null))
        {
        	if(!CMath.bset(F.getInternalFlags(), Faction.IFLAG_NEVERSAVE))
        	{
	        	StringBuffer buf = rebuildFactionProperties(F);
	            if(!Resources.updateFileResource(F.factionID(),buf))
	                return "Faction File '"+F.factionID()+"' could not be modified.  Make sure it is not READ-ONLY.";
        	}
        }
        else
            return "Can not save a blank faction";
        return "";
    }
    public int getAbilityFlagType(String strflag)
    {
        for(int i=0;i<Ability.ACODE_DESCS.length;i++) 
            if(Ability.ACODE_DESCS[i].equalsIgnoreCase(strflag))
                return 1;
        for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
            if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(strflag))
                return 2;
        if(strflag.startsWith("!")) strflag=strflag.substring(1);
        for(int i=0;i< Ability.FLAG_DESCS.length;i++)
            if(Ability.FLAG_DESCS[i].equalsIgnoreCase(strflag))
                return 3;
        if(CMClass.getAbility(strflag)!=null)
            return 0;
        return -1;
    }
    
}
