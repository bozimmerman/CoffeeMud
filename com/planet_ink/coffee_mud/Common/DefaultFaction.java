package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionData;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionRange;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.*;
/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2010 Bo Zimmerman
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@SuppressWarnings("unchecked")
public class DefaultFaction implements Faction, MsgListener
{
    public String ID(){return "DefaultFaction";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultFaction();}}
    public void initializeClass(){}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	protected String ID="";
	protected String name="";
	protected String choiceIntro="";
	protected long[] lastFactionDataChange=new long[1];
	protected int minimum=Integer.MIN_VALUE;
	protected int middle=0;
	protected int difference;
	protected int maximum=Integer.MAX_VALUE;
	protected int highest=Integer.MAX_VALUE;
	protected int lowest=Integer.MIN_VALUE;
	protected long internalFlagBitmap=0;
	protected String experienceFlag="";
	protected boolean useLightReactions=false;
	protected boolean showInScore=false;
	protected boolean showInSpecialReported=false;
	protected boolean showInEditor=false;
	protected boolean showInFactionsCommand=true;
	protected Hashtable<String,FactionRange> ranges=new Hashtable<String,FactionRange>();
	protected Hashtable<String,String[]> affBehavs=new Hashtable<String,String[]>();
	protected Vector<String> defaults=new Vector<String>();
	protected Vector<String> autoDefaults=new Vector<String>();
	protected double rateModifier=1.0;
    protected Hashtable<String,FactionChangeEvent[]> changes=new Hashtable();
    protected Hashtable<String,FactionChangeEvent[]> abilityChangesCache=new Hashtable();
    protected Vector<Faction.FactionZapFactor> factors=new Vector<Faction.FactionZapFactor>();
    protected Hashtable<String,Double> relations=new Hashtable<String,Double>();
    protected Vector<Faction.FactionAbilityUsage> abilityUsages=new Vector<Faction.FactionAbilityUsage>();
    protected Vector<String> choices=new Vector<String>();
    protected Vector<Faction.FactionReactionItem> reactions=new Vector();
    protected Hashtable<String,Vector<Faction.FactionReactionItem>> reactionHash=new Hashtable<String,Vector<Faction.FactionReactionItem>>();
    protected Ability presenceReactionPrototype=null;


    public String factionID(){return ID;}
    public String name(){return name;}
    public long getInternalFlags() { return internalFlagBitmap;}
    public String choiceIntro(){return choiceIntro;}
    public int minimum(){return minimum;}
    public int middle(){return middle;}
    public int difference(){return difference;}
    public int maximum(){return maximum;}
    public int highest(){return highest;}
    public int lowest(){return lowest;}
    public String experienceFlag(){return experienceFlag;}
    public boolean showInScore(){return showInScore;}
    public boolean showInSpecialReported(){return showInSpecialReported;}
    public boolean showInEditor(){return showInEditor;}
    public boolean showInFactionsCommand(){return showInFactionsCommand;}
    public Enumeration<Faction.FactionRange> ranges(){ return DVector.s_enum(ranges,false); }
    public Enumeration<String> defaults(){return DVector.s_enum(defaults);}
    public Enumeration<String> autoDefaults(){return DVector.s_enum(autoDefaults);}
    public double rateModifier(){return rateModifier;}
    public Enumeration<String> changeEventKeys(){return  DVector.s_enum(changes,true);}
    public Enumeration<Faction.FactionZapFactor> factors(){return  DVector.s_enum(factors);}
    public Enumeration<String> relationFactions(){return  DVector.s_enum(relations,true);}
    public Enumeration<Faction.FactionAbilityUsage> abilityUsages(){return  DVector.s_enum(abilityUsages);}
    public Enumeration<String> choices(){return  DVector.s_enum(choices);}
    public void setLightReactions(boolean truefalse){useLightReactions=truefalse;}
    public boolean useLightReactions(){return useLightReactions;}

    public void setFactionID(String newStr){ID=newStr;}
    public void setName(String newStr){name=newStr;}
    public void setInternalFlags(long bitmap) { internalFlagBitmap=bitmap;}
    public void setChoiceIntro(String newStr){choiceIntro=newStr;}
    public void setExperienceFlag(String newStr){experienceFlag=newStr;}
    public void setShowInScore(boolean truefalse){showInScore=truefalse;}
    public void setShowInSpecialReported(boolean truefalse){showInSpecialReported=truefalse;}
    public void setShowInEditor(boolean truefalse){showInEditor=truefalse;}
    public void setShowInFactionsCommand(boolean truefalse){showInFactionsCommand=truefalse;}
    public void setChoices(Vector v){choices=(v==null)?new Vector():v;}
    public void setAutoDefaults(Vector v){autoDefaults=(v==null)?new Vector():v;}
    public void setDefaults(Vector v){defaults=(v==null)?new Vector():v;}
    public void setRateModifier(double d){rateModifier=d;}
    public Faction.FactionAbilityUsage getAbilityUsage(int x){
        return ((x>=0)&&(x<abilityUsages.size()))
                ?(Faction.FactionAbilityUsage)abilityUsages.elementAt(x)
                :null;
    }
    public boolean delFactor(Faction.FactionZapFactor f){ return factors.remove(f); }
    public Faction.FactionZapFactor getFactor(int x){ return ((x>=0)&&(x<factors.size()))?factors.elementAt(x):null;}
    public Faction.FactionZapFactor addFactor(double gain, double loss, String mask){
    	Faction.FactionZapFactor o=new DefaultFactionZapFactor(gain,loss,mask);
        factors.addElement(o);
        return o;
    }
    public boolean delRelation(String factionID) { return relations.remove(factionID)!=null;}
    public boolean addRelation(String factionID, double relation) {
        if(relations.containsKey(factionID))
            return false;
        relations.put(factionID,Double.valueOf(relation));
        return true;
    }
    public double getRelation(String factionID) {
        if(relations.containsKey(factionID))
            return ((Double)relations.get(factionID)).doubleValue();
        return 0.0;
    }

    public DefaultFaction(){super();}
    public void initializeFaction(String aname)
    {
        ID=aname;
        name=aname;
        minimum=0;
        middle=50;
        maximum=100;
        highest=100;
        lowest=0;
        difference=CMath.abs(maximum-minimum);
        experienceFlag="EXTREME";
        addRange("0;100;Sample Range;SAMPLE;");
        defaults.addElement("0");
    }

    public void initializeFaction(StringBuffer file, String fID)
    {
        boolean debug = false;

        ID = fID;
        CMProps alignProp = new CMProps(new ByteArrayInputStream(CMStrings.strToBytes(file.toString())));
        if(alignProp.isEmpty()) return;
        name=alignProp.getStr("NAME");
        choiceIntro=alignProp.getStr("CHOICEINTRO");
        minimum=alignProp.getInt("MINIMUM");
        maximum=alignProp.getInt("MAXIMUM");
        if(maximum<minimum)
        {
            minimum=maximum;
            maximum=alignProp.getInt("MINIMUM");
        }
        recalc();
        experienceFlag=alignProp.getStr("EXPERIENCE").toUpperCase().trim();
        if(experienceFlag.length()==0) experienceFlag="NONE";
        rateModifier=alignProp.getDouble("RATEMODIFIER");
        showInScore=alignProp.getBoolean("SCOREDISPLAY");
        showInFactionsCommand=alignProp.getBoolean("SHOWINFACTIONSCMD");
        showInSpecialReported=alignProp.getBoolean("SPECIALREPORTED");
        showInEditor=alignProp.getBoolean("EDITALONE");
        defaults =CMParms.parseSemicolons(alignProp.getStr("DEFAULT"),true);
        autoDefaults =CMParms.parseSemicolons(alignProp.getStr("AUTODEFAULTS"),true);
        choices =CMParms.parseSemicolons(alignProp.getStr("AUTOCHOICES"),true);
        useLightReactions=alignProp.getBoolean("USELIGHTREACTIONS");
        ranges=new Hashtable<String,FactionRange>();
        changes=new Hashtable<String,FactionChangeEvent[]>();
        factors=new Vector<FactionZapFactor>();
        relations=new Hashtable<String,Double>();
        abilityUsages=new Vector<FactionAbilityUsage>();
        reactions=new Vector<FactionReactionItem>();
        reactionHash=new Hashtable<String,Vector<FactionReactionItem>>();
        for(Enumeration e=alignProp.keys();e.hasMoreElements();)
        {
            if(debug) Log.sysOut("FACTIONS","Starting Key Loop");
            String key = (String) e.nextElement();
            if(debug) Log.sysOut("FACTIONS","  Key Found     :"+key);
            String words = (String) alignProp.get(key);
            if(debug) Log.sysOut("FACTIONS","  Words Found   :"+words);
            if(key.startsWith("RANGE"))
                addRange(words);
            if(key.startsWith("CHANGE"))
                createChangeEvent(words);
            if(key.startsWith("AFFBEHAV"))
            {
                Object[] O=CMParms.parseSafeSemicolonList(words,false).toArray();
                if(O.length==3)
                    addAffectBehav((String)O[0],(String)O[1],(String)O[2]);
            }
            if(key.startsWith("FACTOR"))
            {
                Vector factor=CMParms.parseSemicolons(words,false);
                if(factor.size()>2)
                    factors.add(new DefaultFactionZapFactor(CMath.s_double((String)factor.elementAt(0)),
                                             CMath.s_double((String)factor.elementAt(1)),
                                             (String)factor.elementAt(2)));
            }
            if(key.startsWith("RELATION"))
            {
                Vector V=CMParms.parse(words);
                if(V.size()>=2)
                {
                    String who=(String)V.elementAt(0);
                    double factor;
                    String amt=((String)V.elementAt(1)).trim();
                    if(amt.endsWith("%"))
                        factor=CMath.s_pct(amt);
                    else
                        factor=1;
                    relations.put(who,Double.valueOf(factor));
                }
            }
            if(key.startsWith("ABILITY"))
                addAbilityUsage(words);
            if(key.startsWith("REACTION"))
            {
            	DefaultFactionReactionItem item = new DefaultFactionReactionItem(words);
                addReaction(item.rangeName(), item.presentMOBMask(), item.reactionObjectID(), item.parameters());
            }
        }
    }

    private void recalc() {
        minimum=Integer.MAX_VALUE;
        maximum=Integer.MIN_VALUE;
        for(Enumeration e=ranges();e.hasMoreElements();)
        {
            Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
            if(FR.high()>maximum) maximum=FR.high();
            if(FR.low()<minimum) minimum=FR.low();
        }
        if(minimum==Integer.MAX_VALUE) minimum=Integer.MIN_VALUE;
        if(maximum==Integer.MIN_VALUE) maximum=Integer.MAX_VALUE;
        if(maximum<minimum)
        {
            int oldMin=minimum;
            minimum=maximum;
            maximum=oldMin;
        }
        middle=minimum+(int)Math.round(CMath.div(maximum-minimum,2.0));
        difference=CMath.abs(maximum-minimum);
        lastFactionDataChange[0]=System.currentTimeMillis();
    }

    public String getTagValue(String tag)
    {
        int tagRef=CMLib.factions().isFactionTag(tag);
        if(tagRef<0) return "";
        int numCall=-1;
        if((tagRef<TAG_NAMES.length)&&(TAG_NAMES[tagRef].endsWith("*")))
            if(CMath.isInteger(tag.substring(TAG_NAMES[tagRef].length()-1)))
                numCall=CMath.s_int(tag.substring(TAG_NAMES[tagRef].length()-1));
        switch(tagRef)
        {
        case TAG_NAME: return name;
        case TAG_MINIMUM: return ""+minimum;
        case TAG_MAXIMUM: return ""+maximum;
        case TAG_SCOREDISPLAY: return Boolean.toString(showInScore).toUpperCase();
        case TAG_SHOWINFACTIONSCMD: return Boolean.toString(showInFactionsCommand).toUpperCase();
        case TAG_SPECIALREPORTED: return Boolean.toString(showInSpecialReported).toUpperCase();
        case TAG_EDITALONE: return Boolean.toString(showInEditor).toUpperCase();
        case TAG_DEFAULT: return CMParms.toSemicolonList(defaults);
        case TAG_AUTODEFAULTS: return CMParms.toSemicolonList(autoDefaults);
        case TAG_CHOICEINTRO: return choiceIntro;
        case TAG_AUTOCHOICES: return CMParms.toSemicolonList(choices);
        case TAG_RATEMODIFIER: return ""+rateModifier;
        case TAG_EXPERIENCE: return ""+experienceFlag;
        case TAG_RANGE_:
        {
            if((numCall<0)||(numCall>=ranges.size()))
                return ""+ranges.size();
            int x=0;
            for(Enumeration e=ranges();e.hasMoreElements();)
            {
                Faction.FactionRange FR=(Faction.FactionRange)e.nextElement();
                if(x==numCall) return FR.toString();
                x++;
            }
            return "";
        }
        case TAG_CHANGE_:
        {
        	int sz=0;
        	for(Enumeration<Faction.FactionChangeEvent[]> es=changes.elements();es.hasMoreElements();)
        		sz+=es.nextElement().length;
            if((numCall<0)||(numCall>=sz))
                return ""+sz;
            int i=0;
            for(Enumeration<Faction.FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
            {
                Faction.FactionChangeEvent[] FCs=e.nextElement();
                for(int ii=0;ii<FCs.length;ii++)
                {
	                if(i==numCall)
	                    return FCs[ii].toString();
	                i++;
                }
            }
            return "";
        }
        case TAG_ABILITY_:
        {
            if((numCall<0)||(numCall>=abilityUsages.size()))
                return ""+abilityUsages.size();
            return ((FactionAbilityUsage)abilityUsages.elementAt(numCall)).toString();
        }
        case TAG_FACTOR_:
        {
            if((numCall<0)||(numCall>=factors.size()))
                return ""+factors.size();
            return factors.elementAt(numCall).toString();
        }
        case TAG_RELATION_:
        {
            if((numCall<0)||(numCall>=relations.size()))
                return ""+relations.size();
            int i=0;
            for(Enumeration e=relations.keys();e.hasMoreElements();)
            {
                String factionName=(String)e.nextElement();
                Double D=(Double)relations.get(factionName);
                if(i==numCall)
                    return factionName+" "+CMath.toPct(D.doubleValue());
                i++;
            }
            return "";
        }
        case TAG_AFFBEHAV_:
        {
            if((numCall<0)||(numCall>=affBehavs.size()))
                return ""+affBehavs.size();
            int i=0;
            for(Enumeration e=affBehavs.keys();e.hasMoreElements();)
            {
                String ID=(String)e.nextElement();
                String[] data=(String[])affBehavs.get(ID);
                if(i==numCall)
                    return ID+";"+CMParms.toSafeSemicolonList(data);
                i++;
            }
            return "";
        }
        case TAG_REACTION_:
        {
            if((numCall<0)||(numCall>=reactions.size()))
                return ""+reactions.size();
            Faction.FactionReactionItem item = (Faction.FactionReactionItem)reactions.elementAt(numCall);
            return item.toString();
        }
        case TAG_USELIGHTREACTIONS: return ""+useLightReactions;
        }
        return "";
    }

    public String getINIDef(String tag, String delimeter)
    {
        int tagRef=CMLib.factions().isFactionTag(tag);
        if(tagRef<0)
            return "";
        String rawTagName=TAG_NAMES[tagRef];
        if(TAG_NAMES[tagRef].endsWith("*"))
        {
            int number=CMath.s_int(getTagValue(rawTagName));
            StringBuffer str=new StringBuffer("");
            for(int i=0;i<number;i++)
            {
                String value=getTagValue(rawTagName.substring(0,rawTagName.length()-1)+i);
                str.append(rawTagName.substring(0,rawTagName.length()-1)+(i+1)+"="+value+delimeter);
            }
            return str.toString();
        }
        return rawTagName+"="+getTagValue(tag)+delimeter;
    }
    
    public FactionData makeFactionData(MOB mob)
    {
        FactionData data=new DefaultFactionData(this);
        Vector<Ability> aV=new Vector<Ability>();
        Vector<Behavior> bV=new Vector<Behavior>();
        String ID=null;
        String[] stuff=null;
        if(mob.isMonster())
        for(Enumeration e=affectsBehavs();e.hasMoreElements();)
        {
            ID=(String)e.nextElement();
            stuff=getAffectBehav(ID);
            if(CMLib.masking().maskCheck(stuff[1],mob,true))
            {
                Behavior B=CMClass.getBehavior(ID);
                if(B!=null)
                {
                    B.setParms(stuff[0]);
                    bV.addElement(B);
                }
                else
                {
                    Ability A=CMClass.getAbility(ID);
                    A.setMiscText(stuff[0]);
                    A.setAffectedOne(mob);
                    aV.addElement(A);
                }
            }
        }
        data.addHandlers(aV,bV);
        return data;
    }
    
    public Enumeration<String> affectsBehavs(){return  DVector.s_enum(affBehavs,true);}

    public boolean delAffectBehav(String ID) {
        boolean b=affBehavs.remove(ID.toUpperCase().trim())!=null;
        if(b) lastFactionDataChange[0]=System.currentTimeMillis();
        return b;
    }
    
    public boolean addAffectBehav(String ID, String parms, String gainMask) {
        if(affBehavs.containsKey(ID.toUpperCase().trim())) return false;
        if((CMClass.getBehavior(ID)==null)&&(CMClass.getAbility(ID)==null))
            return false;
        affBehavs.put(ID.toUpperCase().trim(),new String[]{parms,gainMask});
        lastFactionDataChange[0]=System.currentTimeMillis();
        return true;
    }
    
    public String[] getAffectBehav(String ID) {
        if(affBehavs.containsKey(ID.toUpperCase().trim()))
            return CMParms.toStringArray(CMParms.makeVector((String[])affBehavs.get(ID.toUpperCase().trim())));
        return null;
    }
    
    public Enumeration<Faction.FactionReactionItem> reactions(){return  DVector.s_enum(reactions);}

    public Enumeration<Faction.FactionReactionItem> reactions(String rangeName){return  DVector.s_enum((Vector)reactionHash.get(rangeName.toUpperCase().trim()));}

    public boolean delReaction(Faction.FactionReactionItem item)
    {
    	Vector V=(Vector)reactionHash.get(item.rangeName().toUpperCase().trim());
    	boolean res = reactions.remove(item);
    	V.remove(item);
    	if(reactions.size()==0) reactionHash.clear();
        lastFactionDataChange[0]=System.currentTimeMillis();
    	return res;
    }
    
    public boolean addReaction(String range, String mask, String abilityID, String parms)
    {
    	Vector V=(Vector)reactionHash.get(range.toUpperCase().trim());
    	if(V==null) {
    		V=new Vector();
    		reactionHash.put(range.toUpperCase().trim(), V);
    	}
    	DefaultFactionReactionItem item = new DefaultFactionReactionItem();
    	item.setRangeName(range);
    	item.setPresentMOBMask(mask);
    	item.setReactionObjectID(abilityID);
    	item.setParameters(parms);
    	reactions.add(item);
    	V.add(item);
        lastFactionDataChange[0]=System.currentTimeMillis();
    	return true;
    }
    
    public FactionChangeEvent[] getChangeEvents(String key) 
    {
        return (FactionChangeEvent[])changes.get(key);
    }

    public Vector findChoices(MOB mob)
    {
        Vector mine=new Vector();
        String s;
        for(Enumeration e=choices.elements();e.hasMoreElements();)
        {
            s=(String)e.nextElement();
            if(CMath.isInteger(s))
                mine.addElement(Integer.valueOf(CMath.s_int(s)));
            else
            if(CMLib.masking().maskCheck(s, mob,false))
            {
                Vector V=CMParms.parse(s);
                for(int j=0;j<V.size();j++)
                {
                    if(CMath.isInteger((String)V.elementAt(j)))
                        mine.addElement(Integer.valueOf(CMath.s_int((String)V.elementAt(j))));
                }
            }
        }
        return mine;
    }


    public FactionChangeEvent[] findAbilityChangeEvents(Ability key)
    {
        if(key==null) return null;
        // Direct ability ID's
        if(abilityChangesCache.containsKey(key.ID().toUpperCase()))
            return abilityChangesCache.get(key.ID().toUpperCase());
        if(changes.containsKey(key.ID().toUpperCase()))
        {
        	abilityChangesCache.put(key.ID().toUpperCase(), abilityChangesCache.get(key.ID().toUpperCase()));
        	return abilityChangesCache.get(key.ID().toUpperCase());
        }
        // By TYPE or FLAGS
        FactionChangeEvent[] Cs =null;
        Vector<FactionChangeEvent> events=new Vector<FactionChangeEvent>();
        for (Enumeration<FactionChangeEvent[]> e=changes.elements();e.hasMoreElements();)
        {
            Cs=e.nextElement();
            for(FactionChangeEvent C : Cs)
            {
	            if((key.classificationCode()&Ability.ALL_ACODES)==C.IDclassFilter())
	                events.addElement(C);
	            else
	            if((key.classificationCode()&Ability.ALL_DOMAINS)==C.IDdomainFilter())
	                events.addElement(C);
	            else
	            if((C.IDflagFilter()>0)&&(CMath.bset(key.flags(),C.IDflagFilter())))
	                events.addElement(C);
            }
        }
        FactionChangeEvent[] evs = events.toArray(new FactionChangeEvent[0]);
        abilityChangesCache.put(key.ID().toUpperCase(), evs);
        return evs;
    }

    public Faction.FactionRange fetchRange(String codeName)
    {
        return (FactionRange)ranges.get(codeName.toUpperCase().trim());
    }

    public FactionRange fetchRange(int faction)
    {
        for (Enumeration e=ranges.elements();e.hasMoreElements();)
        {
            FactionRange R = (FactionRange)e.nextElement();
            if ( (faction >= R.low()) && (faction <= R.high()))
                return R;
        }
        return null;
    }
    public String fetchRangeName(int faction)
    {
        for (Enumeration e=ranges.elements();e.hasMoreElements();)
        {
            FactionRange R = (FactionRange)e.nextElement();
            if ( (faction >= R.low()) && (faction <= R.high()))
                return R.name();
        }
        return "";
    }

    public int asPercent(int faction)
    {
        return (int)Math.round(CMath.mul(CMath.div(faction-minimum,(maximum-minimum)),100));
    }

    public int asPercentFromAvg(int faction)
    {
        // =(( (B2+A2) / 2 ) - C2) / (B2-A2) * 100
        // C = current, A = min, B = Max
        return (int)Math.round(CMath.mul(CMath.div(((maximum+minimum)/2)-faction,maximum-minimum),100));
    }

    public int randomFaction()
    {
        Random gen = new Random();
        return maximum - gen.nextInt(maximum-minimum);
    }

    public int findDefault(MOB mob)
    {
        String s;
        for(Enumeration e=defaults.elements();e.hasMoreElements();)
        {
            s=(String)e.nextElement();
            if(CMath.isNumber(s))
                return CMath.s_int(s);
            else
            if(CMLib.masking().maskCheck(s, mob,false))
            {
                Vector V=CMParms.parse(s);
                for(int j=0;j<V.size();j++)
                {
                    if(CMath.isNumber((String)V.elementAt(j)))
                        return CMath.s_int((String)V.elementAt(j));
                }
            }
        }
        return 0;
    }

    public int findAutoDefault(MOB mob)
    {
        String s;
        for(Enumeration e=autoDefaults.elements();e.hasMoreElements();)
        {
            s=(String)e.nextElement();
            if(CMath.isNumber(s))
                return CMath.s_int(s);
            else
            if(CMLib.masking().maskCheck(s, mob,false))
            {
                Vector V=CMParms.parse(s);
                for(int j=0;j<V.size();j++)
                {
                    if(CMath.isNumber((String)V.elementAt(j)))
                        return CMath.s_int((String)V.elementAt(j));
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public boolean hasFaction(MOB mob)
    {
        return (mob.fetchFaction(ID)!=Integer.MAX_VALUE);
    }

    public boolean hasUsage(Ability A)
    {
        FactionAbilityUsage usage=null;
        for(int i=0;i<abilityUsages.size();i++)
        {
            usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
            if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
            ||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
                &&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
                &&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
                &&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
                return true;
        }
        return false;
    }

    public boolean canUse(MOB mob, Ability A)
    {
        FactionAbilityUsage usage=null;
        for(int i=0;i<abilityUsages.size();i++)
        {
            usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
            if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
            ||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
                &&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
                &&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
                &&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
            {
                int faction=mob.fetchFaction(ID);
                if((faction < usage.low()) || (faction > usage.high()))
                    return false;
            }
        }
        return true;
    }

    public double findFactor(MOB mob, boolean gain)
    {
        for(Faction.FactionZapFactor factor : factors)
            if(CMLib.masking().maskCheck(factor.compiledMOBMask(),mob,false))
            	return gain?factor.gainFactor():factor.lossFactor();
        return 1.0;
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
    	FactionChangeEvent[] events;
        if((msg.sourceMinor()==CMMsg.TYP_DEATH)    // A death occured
        &&((msg.source()==myHost)||(msg.tool()==myHost))
        &&(msg.tool() instanceof MOB))
        {
            MOB killedM=msg.source();
            MOB killingBlowM=(MOB)msg.tool();
            events=getChangeEvents((msg.source()==myHost)?"MURDER":"KILL");
            FactionChangeEvent eventC;
            if(events!=null)
            for(int e=0;e<events.length;e++)
            {
                eventC=events[e];
                if(eventC.applies(killingBlowM,killedM))
                {
                    CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
                    HashSet combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
                    for(Iterator i=combatBeneficiaries.iterator();i.hasNext();)
                        executeChange((MOB)i.next(),killedM,eventC);
                }
            }
        }
        
        if((msg.tool() instanceof Ability)
        &&(msg.target()==myHost)    // Arrested watching
        &&(msg.tool().ID().equals("Skill_Handcuff"))
        &&(msg.source().isMonster()))
        {
        	Room R=msg.source().location();
        	if((R!=null)&&(R.getArea()!=null))
        	{
                FactionChangeEvent eventC;
                events=getChangeEvents("ARRESTED");
                if(events!=null)
                {
    	        	LegalBehavior B=CMLib.law().getLegalBehavior(R);
    	        	if((B!=null)&&(B.isAnyOfficer(R.getArea(), msg.source())))
    	        	{
		                for(int e=0;e<events.length;e++)
		                {
		                    eventC=events[e];
		    	            if(eventC.applies(msg.source(),(MOB)msg.target()))
				                executeChange(msg.source(),(MOB)msg.target(),eventC);
		                }
    	        	}
                }
        	}
        }
        
        if((msg.sourceMinor()==CMMsg.TYP_GIVE)    // Bribe watching
        &&(msg.source()==myHost)
        &&(msg.tool() instanceof Coins)
        &&(msg.target() instanceof MOB))
        {
            FactionChangeEvent eventC;
            events=getChangeEvents("BRIBE");
            if(events!=null)
            for(int e=0;e<events.length;e++)
            {
                eventC=events[e];
	            if(eventC.applies(msg.source(),(MOB)msg.target()))
	            {
	                double amount=CMath.s_double(eventC.getTriggerParm("AMOUNT"));
	                double pctAmount = CMath.s_pct(eventC.getTriggerParm("PCT"))
	                				 * CMLib.beanCounter().getTotalAbsoluteNativeValue((MOB)msg.target());
	                if(pctAmount>amount) amount=pctAmount;
	                if(amount==0) amount=1.0;
	                if(((Coins)msg.tool()).getTotalValue()>=amount)
		                executeChange(msg.source(),(MOB)msg.target(),eventC);
	            }
            }
        }

        if((msg.sourceMinor()==CMMsg.TYP_SPEAK)    // Talk watching
        &&(msg.othersMessage()!=null)
        &&(msg.source()==myHost))
        {
            FactionChangeEvent eventC;
            events=getChangeEvents("TALK");
            if((events!=null)&&(events.length>0))
            {
            	Room R=msg.source().location();
            	Vector<MOB> targets=new Vector<MOB>();
            	if(msg.target() instanceof MOB)
            		targets.add((MOB)msg.target());
            	else
            	for(int m=0;m<R.numInhabitants();m++)
            	{
            		MOB M=R.fetchInhabitant(m);
            		if((M!=null)&&(M.isMonster())
            		&&(CMLib.flags().canBeHeardBy(msg.source(),M))
            		&&(M.amFollowing()!=msg.source()))
            			targets.add(M);
            	}
	            String sayMsg=CMStrings.getSayFromMessage(msg.othersMessage().toLowerCase());
	            Matcher M=null;
                if((sayMsg!=null)&&(sayMsg.length()>0)&&(R!=null))
	            for(int e=0;e<events.length;e++)
	            {
	                eventC=events[e];
	            	Long time=(Long)eventC.stateVariable(1);
	            	if(time==null)
	            		time=Long.valueOf(System.currentTimeMillis());
	            	if(System.currentTimeMillis()<time.longValue())
	            		continue;
	            	Pattern P=(Pattern)eventC.stateVariable(0);
	            	if(P==null)
	            	{
	            		String mask=eventC.getTriggerParm("REGEX");
	            		if((mask==null)||(mask.trim().length()==0))
	            			mask=".*";
	            		P=Pattern.compile(mask.toLowerCase());
	            		eventC.setStateVariable(0,P);
	            	}
	            	M=P.matcher(sayMsg);
	                if(M.matches())
	                {
		            	Long addTime=(Long)eventC.stateVariable(2);
		            	if(addTime==null)
		            	{
		            		addTime=Long.valueOf(CMath.s_long(eventC.getTriggerParm("WAIT"))*Tickable.TIME_TICK);
		            		eventC.setStateVariable(2,addTime);
		            	}
		            	eventC.setStateVariable(1,Long.valueOf(System.currentTimeMillis()+addTime.longValue()));
		            	for(MOB target : targets)
				            if(eventC.applies(msg.source(),target))
				            {
				                executeChange(msg.source(),target,eventC);
				                break;
				            }
	                }
	            }
            }
            events=getChangeEvents("MUDCHAT");
            if((events!=null)&&(events.length>0))
            {
            	Room R=msg.source().location();
            	Vector<MOB> targets=new Vector<MOB>();
            	if(msg.target() instanceof MOB)
            		targets.add((MOB)msg.target());
            	else
            	for(int m=0;m<R.numInhabitants();m++)
            	{
            		MOB M=R.fetchInhabitant(m);
            		if((M!=null)&&(M.isMonster())
            		&&(CMLib.flags().canBeHeardBy(msg.source(),M))
            		&&(M.amFollowing()!=msg.source()))
            			targets.add(M);
            	}
            	boolean foundOne=false;
            	for(MOB target : targets)
            	{
	            	ChattyBehavior mudChatB=null;
	            	Behavior B=null;
	            	for(int b=0;b<target.numBehaviors();b++)
	            	{
	            		B=target.fetchBehavior(b);
	            		if(B instanceof ChattyBehavior)
	            			mudChatB=(ChattyBehavior)B;
	            	}
	            	if(mudChatB!=null)
	            	{
			            String sayMsg=CMStrings.getSayFromMessage(msg.othersMessage().toLowerCase());
		                if((sayMsg!=null)&&(sayMsg.length()>0))
			            for(int e=0;e<events.length;e++)
			            {
			                eventC=events[e];
			            	Long time=(Long)eventC.stateVariable(0);
			            	if(time==null)
			            		time=Long.valueOf(System.currentTimeMillis());
			            	if(System.currentTimeMillis()<time.longValue())
			            		continue;
			            	Long addTime=(Long)eventC.stateVariable(1);
			            	if(addTime==null)
			            	{
			            		addTime=Long.valueOf(CMath.s_long(eventC.getTriggerParm("WAIT"))*Tickable.TIME_TICK);
			            		if(addTime.longValue()<Tickable.TIME_TICK) addTime=Long.valueOf(Tickable.TIME_TICK);
			            		eventC.setStateVariable(1,addTime);
			            	}
			            	eventC.setStateVariable(0,Long.valueOf(System.currentTimeMillis()+addTime.longValue()));
				            if(eventC.applies(msg.source(),target))
				            {
				            	if((mudChatB.getLastRespondedTo()==msg.source())
				            	&&(mudChatB.getLastThingSaid()!=null)
				            	&&(!mudChatB.getLastThingSaid().equalsIgnoreCase(sayMsg)))
				            	{
				            		executeChange(msg.source(),target,eventC);
				            		foundOne=true;
				            	}
				            }
			            }
		                if(foundOne)
		                	break;
	            	}
            	}
            }
        }
        // Ability Watching
        if((msg.tool() instanceof Ability)
        &&(msg.othersMessage()!=null)
        &&((events=findAbilityChangeEvents((Ability)msg.tool()))!=null))
        {
        	for(int e=0;e<events.length;e++)
        	{
	            FactionChangeEvent C=events[e];
	            if((msg.target() instanceof MOB)&&(C.applies(msg.source(),(MOB)msg.target())))
	                executeChange(msg.source(),(MOB)msg.target(),C);
	            else
	            if (!(msg.target() instanceof MOB))
	                executeChange(msg.source(),null,C);
        	}
        }
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)  // Experience is being altered
        &&(msg.target() instanceof MOB)           // because a mob died
        &&(myHost==msg.source())      // this Faction is on the mob that killed them
        &&(!experienceFlag.equals("NONE"))
        &&(msg.value()>0))
        {
            MOB killer=msg.source();
            MOB vic=(MOB)msg.target();

            if(experienceFlag.equals("HIGHER"))
                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * CMath.div(Math.abs(killer.fetchFaction(ID)-minimum),(maximum - minimum)))));
            else
            if(experienceFlag.equals("LOWER"))
                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * CMath.div(Math.abs(maximum-killer.fetchFaction(ID)),(maximum - minimum)))));
            else
            if(vic.fetchFaction(ID)!=Integer.MAX_VALUE)
            {
                if(experienceFlag.equals("EXTREME"))
                    msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * CMath.div(Math.abs(vic.fetchFaction(ID) - killer.fetchFaction(ID)),(maximum - minimum)))));
                else
                if(experienceFlag.equals("FOLLOWHIGHER"))
                    msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * CMath.div(Math.abs(vic.fetchFaction(ID)-minimum),(maximum - minimum)))));
                else
                if(experienceFlag.equals("FOLLOWLOWER"))
                    msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * CMath.div(Math.abs(maximum-vic.fetchFaction(ID)),(maximum - minimum)))));
                if(msg.value()<=0)
                    msg.setValue(0);
            }
        }
        return true;
    }

    public void executeChange(MOB source, MOB target, FactionChangeEvent event)
    {
        int sourceFaction= source.fetchFaction(ID);
        int targetFaction = sourceFaction * -1;
        if((source==target)&&(!event.selfTargetOK())&&(!event.eventID().equalsIgnoreCase("TIME")))
            return;

        if(target!=null)
        {
            if(hasFaction(target))
                targetFaction=target.fetchFaction(ID);
            else
            if(!event.outsiderTargetOK())
                return;
        }
        else
            target = source;

        double baseChangeAmount=100.0;
        if((source!=target)&&(!event.just100()))
        {
            int levelLimit=CMProps.getIntVar(CMProps.SYSTEMI_EXPRATE);
            int levelDiff=target.envStats().level()-source.envStats().level();

            if(levelDiff<(-levelLimit) )
                baseChangeAmount=0.0;
            else
            if(levelLimit>0)
            {
                double levelFactor=CMath.div(levelDiff,levelLimit);
                if(levelFactor> ((double)levelLimit))
                    levelFactor=((double)levelLimit);
                baseChangeAmount=baseChangeAmount+CMath.mul(levelFactor,100);
            }
        }

        int factionAdj=1;
        int changeDir=0;
        switch(event.direction())
        {
        case FactionChangeEvent.CHANGE_DIRECTION_MAXIMUM:
            factionAdj=maximum-sourceFaction;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_MINIMUM:
            factionAdj=minimum-sourceFaction;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_UP:
            changeDir=1;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_DOWN:
            changeDir=-1;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_OPPOSITE:
            if(source!=target)
            {
                if(targetFaction==middle)
                    changeDir=(sourceFaction>middle)?1:-1;
                else
                    changeDir=(targetFaction>middle)?-1:1;
                if((sourceFaction>middle)&&(targetFaction>middle)) changeDir=-1;
                baseChangeAmount=CMath.div(baseChangeAmount,2.0)
                                +(int)Math.round(CMath.div(baseChangeAmount,2.0)
                                        *Math.abs((sourceFaction-targetFaction)
                                                /Math.abs(difference)));
            }
            else
                factionAdj=0;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_AWAY:
            if(source!=target)
                changeDir=targetFaction>=sourceFaction?-1:1;
            else
                factionAdj=0;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_TOWARD:
            if(source!=target)
                changeDir=targetFaction>=sourceFaction?1:-1;
            else
                factionAdj=0;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_REMOVE:
            factionAdj=Integer.MAX_VALUE;
            break;
        case FactionChangeEvent.CHANGE_DIRECTION_ADD:
            factionAdj=findDefault(source);
            if(!hasFaction(source))
                source.addFaction(ID,0);
            else
                factionAdj=0;
            break;
        }
        if(changeDir!=0)
        {
            //int baseExp=(int)Math.round(theAmount);

            // Pardon the completely random seeming 1.42 and 150.
            // They're the result of making graphs of scenarios and massaging the formula, nothing more or less.
            if((hasFaction(target))||(event.outsiderTargetOK()))
                factionAdj=changeDir*(int)Math.round(rateModifier*baseChangeAmount);
            else
                factionAdj=0;
            factionAdj*=event.factor();
            factionAdj=(int)Math.round(CMath.mul(factionAdj,findFactor(source,(factionAdj>=0))));
        }

        if(factionAdj==0) return;

        CMMsg FacMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,ID);
        FacMsg.setValue(factionAdj);
        Room R=source.location();
        if(R!=null)
        {
            if(R.okMessage(source,FacMsg))
            {
                R.send(source, FacMsg);
                factionAdj=FacMsg.value();
                if((factionAdj!=Integer.MAX_VALUE)&&(factionAdj!=Integer.MIN_VALUE))
                {
                    // Now execute the changes on the relation.  We do this AFTER the execution of the first so
                    // that any changes from okMessage are incorporated
                    for(Enumeration e=relations.keys();e.hasMoreElements();)
                    {
                        String relID=((String)e.nextElement());
                        FacMsg=CMClass.getMsg(source,target,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,relID);
                        FacMsg.setValue((int)Math.round(CMath.mul(factionAdj, ((Double)relations.get(relID)).doubleValue())));
                        if(R.okMessage(source,FacMsg))
                            R.send(source, FacMsg);
                    }
                }
            }
        }
        else
        if((factionAdj==Integer.MAX_VALUE)||(factionAdj==Integer.MIN_VALUE))
            source.removeFaction(ID);
        else
            source.adjustFaction(ID,factionAdj);
    }

     public String usageFactorRangeDescription(Ability A)
     {
         StringBuffer rangeStr=new StringBuffer();
         FactionAbilityUsage usage=null;
         HashSet namesAdded=new HashSet();
         for(int i=0;i<abilityUsages.size();i++)
         {
             usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
             if((usage.possibleAbilityID()&&usage.abilityFlags().equalsIgnoreCase(A.ID()))
             ||(((usage.type()<0)||((A.classificationCode()&Ability.ALL_ACODES)==usage.type()))
                 &&((usage.flag()<0)||(CMath.bset(A.flags(),usage.flag())))
                 &&((usage.notflag()<0)||(!CMath.bset(A.flags(),usage.notflag())))
                 &&((usage.domain()<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain()))))
             {
                for(Enumeration e=ranges();e.hasMoreElements();)
                {
                     FactionRange R=(FactionRange)e.nextElement();
                     if((((R.high()<=usage.high())&&(R.high()>=usage.low()))
                         ||((R.low()>=usage.low()))&&(R.low()<=usage.high()))
                     &&(!namesAdded.contains(R.name())))
                     {
                         namesAdded.add(R.name());
                         if(rangeStr.length()>0) rangeStr.append(", ");
                         rangeStr.append(R.name());
                     }
                }
             }
         }
         return rangeStr.toString();
    }

     private static String _ALL_TYPES=null;
     public String ALL_CHANGE_EVENT_TYPES()
     {
         StringBuffer ALL_TYPES=new StringBuffer("");
         if(_ALL_TYPES!=null) return _ALL_TYPES;
         for(int i=0;i<Faction.FactionChangeEvent.MISC_TRIGGERS.length;i++)
             ALL_TYPES.append(Faction.FactionChangeEvent.MISC_TRIGGERS[i]+", ");
         for(int i=0;i<Ability.ACODE_DESCS.length;i++)
             ALL_TYPES.append(Ability.ACODE_DESCS[i]+", ");
         for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
             ALL_TYPES.append(Ability.DOMAIN_DESCS[i]+", ");
         for(int i=0;i<Ability.FLAG_DESCS.length;i++)
             ALL_TYPES.append(Ability.FLAG_DESCS[i]+", ");
         _ALL_TYPES=ALL_TYPES.toString()+" a valid Skill, Spell, Chant, etc. ID.";
         return _ALL_TYPES;
     }

     public Faction.FactionChangeEvent createChangeEvent(String key)
     {
         Faction.FactionChangeEvent event;
         if(key==null) return null;
         if(key.indexOf(';')<0)
         {
             event=new DefaultFaction.DefaultFactionChangeEvent(this);
             if(!event.setEventID(key))
                 return null;
         }
         else
             event=new DefaultFaction.DefaultFactionChangeEvent(this,key);
         abilityChangesCache.clear();
         Faction.FactionChangeEvent[] events=changes.get(event.eventID().toUpperCase().trim());
         if(events==null)
        	 events=new Faction.FactionChangeEvent[0];
         events=Arrays.copyOf(events, events.length+1);
         events[events.length-1]=event;
    	 changes.put(event.eventID().toUpperCase().trim(), events);
         return event;
     }

     private boolean replaceEvents(String key, Faction.FactionChangeEvent event, boolean strict)
     {
    	 Faction.FactionChangeEvent[] events=changes.get(key);
    	 if(events==null) return false;
    	 Faction.FactionChangeEvent[] nevents=new Faction.FactionChangeEvent[events.length-1];
		 int ne1=0;
		 boolean done=false;
    	 for(int x=0;x<events.length;x++)
    	 {
    		 if((strict&&(events[x] == event))||((!strict)&&(events[x].toString().equals(event.toString()))))
    		 {
    	         abilityChangesCache.clear();
        		 if(nevents.length==0)
        			 changes.remove(key);
        		 else
	        		 changes.put(key,nevents);
        		 done=true;
    		 }
    		 else
    	     if(ne1<nevents.length)
    			 nevents[ne1++]=events[x];
    	 }
    	 return done;
     }
     public void clearChangeEvents()
     {
         abilityChangesCache.clear();
         changes.clear();
     }
     
     public boolean delChangeEvent(Faction.FactionChangeEvent event)
     {
         abilityChangesCache.clear();
         for(Enumeration<String> e=changes.keys();e.hasMoreElements();)
        	 if(replaceEvents(e.nextElement(),event,true))
        		 return true;
         for(Enumeration<String> e=changes.keys();e.hasMoreElements();)
        	 if(replaceEvents(e.nextElement(),event,false))
        		 return true;
         return false;
     }

     public class DefaultFactionChangeEvent implements Faction.FactionChangeEvent
     {
    	private String ID="";
        private String flagCache="";
        private int IDclassFilter=-1;
        private int IDflagFilter=-1;
        private int IDdomainFilter=-1;
        private int direction=0;
        private double factor=0.0;
        private String targetZapperStr="";
        private boolean outsiderTargetOK=false;
        private boolean selfTargetOK=false;
        private boolean just100=false;
        private Object[] stateVariables=new Object[0];
        private Hashtable<String,String> savedTriggerParms=new Hashtable<String,String>();
        private String triggerParms="";
        private Faction myFaction;
        private Vector compiledTargetZapper=null;
        private Vector compiledSourceZapper=null;

        public String eventID(){return ID;}
        public String flagCache(){return flagCache;}
        public int IDclassFilter(){return IDclassFilter;}
        public int IDflagFilter(){return IDflagFilter;}
        public int IDdomainFilter(){return IDdomainFilter;}
        public int direction(){return direction;}
        public double factor(){return factor;}
        public String targetZapper(){return targetZapperStr;}
        public boolean outsiderTargetOK(){return outsiderTargetOK;}
        public boolean selfTargetOK(){return selfTargetOK;}
        public boolean just100(){return just100;}
        public void setDirection(int newVal){direction=newVal;}
        public void setFactor(double newVal){factor=newVal;}
        public void setTargetZapper(String newVal)
        {
        	targetZapperStr=newVal;
        	compiledTargetZapper=null;
        	if(newVal.trim().length()>0)
        		compiledTargetZapper=CMLib.masking().maskCompile(newVal);
        }
        public Vector compiledTargetZapper(){return compiledTargetZapper;}
        public Vector compiledSourceZapper(){return compiledSourceZapper;}
		public String getTriggerParm(String parmName) {
			if((triggerParms==null)||(triggerParms.length()==0))
				return "";
			String S=savedTriggerParms.get(parmName);
			if(S!=null) return S;
			return "";
		}

        public String toString()
        {
        	if(triggerParms.trim().length()>0)
	            return ID+"("+triggerParms.replace(';',',')+");"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
        	else
	            return ID+";"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+targetZapperStr;
        }

        public DefaultFactionChangeEvent(Faction F){myFaction=F;}

        public DefaultFactionChangeEvent(Faction F, String key)
        {
        	myFaction=F;
            Vector v = CMParms.parseSemicolons(key,false);
            
            String trigger =(String)v.elementAt(0);
            triggerParms="";
            int x=trigger.indexOf('(');
            if((x>0)&&(trigger.endsWith(")")))
            {
            	setTriggerParameters(trigger.substring(x+1,trigger.length()-1));
            	trigger=trigger.substring(0,x);
            }
            
            setEventID(trigger);
            setDirection((String)v.elementAt(1));
            String amt=((String)v.elementAt(2)).trim();
            if(amt.endsWith("%"))
                setFactor(CMath.s_pct(amt));
            else
                setFactor(1.0);

            if(v.size()>3)
                setFlags((String)v.elementAt(3));
            if(v.size()>4)
                setTargetZapper((String)v.elementAt(4));
        }

        public boolean setEventID(String newID)
        {
            IDclassFilter=-1;
            IDflagFilter=-1;
            IDdomainFilter=-1;
            for(int i=0;i<MISC_TRIGGERS.length;i++)
                if(MISC_TRIGGERS[i].equalsIgnoreCase(newID))
                { ID=MISC_TRIGGERS[i];    return true;}
            for(int i=0;i<Ability.ACODE_DESCS.length;i++)
                if(Ability.ACODE_DESCS[i].equalsIgnoreCase(newID))
                {    IDclassFilter=i; ID=newID; return true;}
            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
                if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(newID))
                {    IDdomainFilter=i<<5;  ID=newID;return true;}
            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                if(Ability.FLAG_DESCS[i].equalsIgnoreCase(newID))
                { IDflagFilter=(int)CMath.pow(2,i);  ID=newID; return true;}
            if(CMClass.getAbility(newID)!=null)
            { ID=newID; return true;}
            return false;
        }
        public boolean setDirection(String d)
        {
        	if(CMath.isInteger(d))
        		direction=CMath.s_int(d);
        	else
            if(d.startsWith("U")) {
                direction = CHANGE_DIRECTION_UP;
            }
            else
            if(d.startsWith("D")) {
                direction = CHANGE_DIRECTION_DOWN;
            }
            else
            if(d.startsWith("OPP")) {
                direction = CHANGE_DIRECTION_OPPOSITE;
            }
            else
            if(d.startsWith("REM")) {
                direction = CHANGE_DIRECTION_REMOVE;
            }
            else
            if(d.startsWith("MIN")) {
                direction = CHANGE_DIRECTION_MINIMUM;
            }
            else
            if(d.startsWith("MAX")) {
                direction = CHANGE_DIRECTION_MAXIMUM;
            }
            else
            if(d.startsWith("ADD")) {
                direction = CHANGE_DIRECTION_ADD;
            }
            else
            if(d.startsWith("TOW")) {
                direction = CHANGE_DIRECTION_TOWARD;
            }
            else
            if(d.startsWith("AWA")) {
                direction = CHANGE_DIRECTION_AWAY;
            }
            else
                return false;
            return true;
        }
        public void setFlags(String newFlagCache)
        {
            flagCache=newFlagCache.toUpperCase().trim();
            Vector flags=CMParms.parse(flagCache);
            if(flags.contains("OUTSIDER")) outsiderTargetOK=true;
            if(flags.contains("SELFOK")) selfTargetOK=true;
            if(flags.contains("JUST100")) just100=true;
        }
        public boolean applies(MOB source, MOB target)
        {
        	if((compiledTargetZapper!=null)
        	&&(!CMLib.masking().maskCheck(compiledTargetZapper,target,false)))
        		return false;
        	if((compiledSourceZapper!=null)
        	&&(!CMLib.masking().maskCheck(compiledSourceZapper,target,false)))
        		return false;
        	return true;
        }
        
        public String triggerParameters() { return triggerParms;}
        public void setTriggerParameters(String newVal)
        {
        	triggerParms=newVal;
        	savedTriggerParms=CMParms.parseEQParms(newVal);
        	compiledSourceZapper=null;
        	String S=savedTriggerParms.get("MASK");
        	if((S!=null)&&(S.length()>0))
        		compiledSourceZapper=CMLib.masking().maskCompile(S);
        }
        public Object stateVariable(int x){ return ((x>=0)&&(x<stateVariables.length))?stateVariables[x]:null;}
        public void setStateVariable(int x, Object newVal)
        {
        	if(x<0) return;
        	if(x>=stateVariables.length) stateVariables=Arrays.copyOf(stateVariables,x+1);
        	stateVariables[x]=newVal;
        }
        public Faction getFaction(){return myFaction;}
    }

    public Faction.FactionRange addRange(String key){
        Faction.FactionRange FR=new DefaultFaction.DefaultFactionRange(this,key);
        ranges.put(FR.codeName().toUpperCase().trim(),FR);
        recalc();
        return FR;
    }
    public boolean delRange(FactionRange FR)
    {
        if(!ranges.containsKey(FR.codeName().toUpperCase().trim())) return false;
        ranges.remove(FR.codeName().toUpperCase().trim());
        recalc();
        return true;
    }
    public class DefaultFactionRange implements Faction.FactionRange
    {
        public int low;
        public int high;
        public String Name="";
        public String CodeName="";
        public int AlignEquiv;
        public Faction myFaction=null;

        public int low(){return low;}
        public int high(){return high;}
        public String name(){return Name;}
        public String codeName(){return CodeName;}
        public int alignEquiv(){return AlignEquiv;}
        public Faction myFaction(){return myFaction;}

        public void setLow(int newVal){low=newVal;}
        public void setHigh(int newVal){high=newVal;}
        public void setName(String newVal){Name=newVal;}
        public void setAlignEquiv(int newVal){AlignEquiv=newVal;}

        public DefaultFactionRange(Faction F, String key)
        {
            myFaction=F;
            Vector v = CMParms.parseSemicolons(key,false);
            Name = (String) v.elementAt(2);
            low = CMath.s_int( (String) v.elementAt(0));
            high = CMath.s_int( (String) v.elementAt(1));
            if(v.size()>3)
                CodeName=(String)v.elementAt(3);
            if(v.size()>4)
                AlignEquiv = CMLib.factions().getAlignEquiv((String)v.elementAt(4));
            else
                AlignEquiv = Faction.ALIGN_INDIFF;
        }

        public String toString()
        {
            return low +";"+high+";"+Name+";"+CodeName+";"+ALIGN_NAMES[AlignEquiv];
        }
        public int random()
        {
            Random gen = new Random();
            return high - gen.nextInt(high-low);
        }
    }

    public Faction.FactionAbilityUsage addAbilityUsage(String key){
        Faction.FactionAbilityUsage usage=
            (key==null)?new DefaultFaction.DefaultFactionAbilityUsage()
                       : new DefaultFaction.DefaultFactionAbilityUsage(key);
        abilityUsages.addElement(usage);
        return usage;
    }
    public boolean delAbilityUsage(Faction.FactionAbilityUsage usage){return abilityUsages.remove(usage);}

    public class DefaultFactionData implements FactionData
    {
        private int value=0;
        private boolean noListeners=false;
        private boolean noTickers=false;
        private boolean noStatAffectors=false;
        private long lastUpdated=System.currentTimeMillis();
        private Ability[] myEffects=new Ability[0];
        private Behavior[] myBehaviors=new Behavior[0];
        private DVector currentReactionSets = new DVector(2);
        private Ability lightPresenceAbilities[] = new Ability[0];
        private Faction.FactionRange currentRange = null;
        private boolean erroredOut=false;
        private Faction myFaction;
        public DefaultFactionData(Faction F){myFaction=F;}
        public int value() { return value;}
        public Faction getFaction() { return myFaction;}
        public void setValue(int newValue)
        {
        	this.value=newValue;
        	if((currentRange==null)||(this.value<currentRange.low())||(this.value>currentRange.high()))
        	{
        		synchronized(this)
        		{
                	if((currentRange!=null)&&(this.value>=currentRange.low())&&(this.value<=currentRange.high()))
                		return;
	        		currentRange = fetchRange(value);
	        		if(currentRange==null)
	        		{
	        			if(!erroredOut)
		        			Log.errOut("DefaultFactionData","Faction "+factionID()+" does not define a range for "+this.value);
	        			erroredOut=true;
	        		}
	        		else
	        		{
	        			erroredOut=false;
	        			currentReactionSets=new DVector(2);
	        			for(Enumeration e=reactions();e.hasMoreElements();)
	        			{
	        				Faction.FactionReactionItem react = (Faction.FactionReactionItem)e.nextElement();
	        				if(!react.rangeName().equalsIgnoreCase(currentRange.codeName()))
	        					continue;
	        				Faction.FactionReactionItem sampleReact = null;
	        				Vector reactSet=null;
	        				for(int r=0;r<currentReactionSets.size();r++)
	        				{
	        					reactSet=(Vector)currentReactionSets.elementAt(r,2);
	        					sampleReact=(Faction.FactionReactionItem)reactSet.firstElement();
	        					if(react.presentMOBMask().trim().equalsIgnoreCase(sampleReact.presentMOBMask().trim()))
	        					{
	        						reactSet.addElement(react);
	        						react=null; break;
	        					}
	        				}
	        				if(react!=null)
	        					currentReactionSets.addElement(react.compiledPresentMOBMask(),CMParms.makeVector(react));
	        			}
	        			//noReactions=currentReactionSets.size()==0;
	        		}
	                noListeners=(myEffects.length==0) && (currentReactionSets.size()==0);
	                noTickers=(myBehaviors.length==0) && ((currentReactionSets.size()==0)||(!useLightReactions()));
        		}
        	}
        }
        
    	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
    	{
    		if(!noStatAffectors)
	    		for(Ability A : myEffects) A.affectEnvStats(affected, affectableStats);
    	}
    	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
    	{
    		if(!noStatAffectors)
    			for(Ability A : myEffects) A.affectCharStats(affectedMob, affectableStats);
    	}
    	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
    	{
    		if(!noStatAffectors)
	    		for(Ability A : myEffects) A.affectCharState(affectedMob, affectableMaxState);
    	}
        public void addHandlers(Vector<Ability> listeners, Vector<Behavior> tickers) 
        {
            this.myEffects=listeners.toArray(new Ability[0]);
            this.myBehaviors=tickers.toArray(new Behavior[0]);
            noListeners=(listeners.size()==0) && (currentReactionSets.size()==0);
            noTickers=(tickers.size()==0) && ((currentReactionSets.size()==0)||(!useLightReactions()));
            noStatAffectors=(listeners.size()==0);
        }
        public boolean requiresUpdating() { return lastFactionDataChange[0] > lastUpdated; }
        
        private Ability setPresenceReaction(MOB M, Environmental myHost)
        {
	    	Vector myReactions=null;
	    	Vector tempReactSet=null;
	    	Faction.FactionReactionItem reactionItem = null;
	    	for(int d=0;d<currentReactionSets.size();d++)
	    		if(CMLib.masking().maskCheck((Vector)currentReactionSets.elementAt(d,1),M,true))
	    		{
	    			if(myReactions==null) myReactions=new Vector();
	    			tempReactSet=(Vector)currentReactionSets.elementAt(d,2);
	    			for(Enumeration e=tempReactSet.elements();e.hasMoreElements();)
	    			{
		    			reactionItem=(Faction.FactionReactionItem)e.nextElement();
		    			myReactions.add(reactionItem.reactionObjectID()+"="+reactionItem.parameters());
	    			}
	    		}
	    	if(myReactions!=null)
	    		if(useLightReactions())
	    		{
	    			presenceReactionPrototype.invoke(M,myReactions,myHost,false,0);
	    			if(myReactions.size()==1)
	    			{
	    				Ability A=(Ability)myReactions.firstElement();
		    			A.setInvoker(M);
		    			return A;
	    			}
	    		}
	    		else
			    	presenceReactionPrototype.invoke(M,myReactions,myHost,true,0);
	    	return null;
        }
        
    	public void executeMsg(Environmental myHost, CMMsg msg)
    	{
    		if(noListeners) return;
			synchronized(lightPresenceAbilities)
			{
				if((currentReactionSets.size()>0)
				&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
				&&(msg.target() instanceof Room))
				{
			    	if(presenceReactionPrototype==null)
			    		if((presenceReactionPrototype=CMClass.getAbility("PresenceReaction"))==null) return;
					if((msg.source()==myHost)
					&&(!msg.source().isMonster()))
					{
						MOB M=null;
				    	Room R=(Room)msg.target();
				    	Vector<Ability> lightPresenceReactions=new Vector<Ability>();
				    	Ability A=null;
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)&&(M!=myHost)&&(M.isMonster()))
							{
								A=setPresenceReaction(M,myHost);
								if(A!=null) // means yes, we are using light, and yes, heres a reaction to add
									lightPresenceReactions.add(A);
							}
						}
						lightPresenceAbilities = lightPresenceReactions.toArray(new Ability[0]);
					}
					else
					if((msg.source().isMonster())
					&&(msg.target()==CMLib.map().roomLocation(myHost)))
					{
						Ability A=setPresenceReaction(msg.source(),myHost);
						if(A!=null){ // means yes, we are using light, and yes, heres a reaction to add
							lightPresenceAbilities = Arrays.copyOf(lightPresenceAbilities, lightPresenceAbilities.length+1);
							lightPresenceAbilities[lightPresenceAbilities.length-1]=A;
						}
					}
				}
				else
				if((lightPresenceAbilities.length>0)
				&&(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				&&(msg.target() instanceof Room))
				{
					if((msg.source()==myHost)
					&&(!msg.source().isMonster()))
					{
						Room R=(Room)msg.target();
						MOB M=null;
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)&&(M!=myHost)&&(M.isMonster()))
								presenceReactionPrototype.invoke(M,new Vector(),null,true,0);
						}
		    			lightPresenceAbilities=new Ability[0];
					}
					else
					{
						presenceReactionPrototype.invoke(msg.source(),new Vector(),null,true,0);
						Ability[] newAbilities = new Ability[lightPresenceAbilities.length];
		    			int l=0;
		    			for(int a=0;a<lightPresenceAbilities.length;a++)
		    				if(lightPresenceAbilities[a].affecting()==null)
		    				{}
		    				else
		    				if(lightPresenceAbilities[a].affecting()==msg.source())
		    					lightPresenceAbilities[a].invoke(msg.source(),new Vector(),null,true,0);
		    				else
		    					newAbilities[l++]=lightPresenceAbilities[a];
		    			if(l==0)
			    			lightPresenceAbilities=new Ability[0];
		    			else
		    			if(l<lightPresenceAbilities.length)
							lightPresenceAbilities = Arrays.copyOf(newAbilities, l);
					}
				}
			}
            for(Ability A : lightPresenceAbilities)
                A.executeMsg(A.invoker(), msg);
            for(Ability A : myEffects)
                A.executeMsg(myHost, msg);
            for(Behavior B : myBehaviors)
                B.executeMsg(myHost, msg);
    	}
    	public boolean okMessage(Environmental myHost, CMMsg msg)
    	{
    		if(noListeners) return true;
            for(Ability A : myEffects)
                if(!A.okMessage(myHost, msg))
                	return false;
            for(Behavior B : myBehaviors)
                if(!B.okMessage(myHost, msg))
                	return false;
            for(Ability A : lightPresenceAbilities)
                if(!A.okMessage(A.invoker(), msg))
                	return false;
    		return true;
    	}
        public boolean tick(Tickable ticking, int tickID)
        {
    		if(noTickers) return true;
            for(Ability A : myEffects)
                if(!A.tick(ticking, tickID))
                	return false;
            for(Behavior B : myBehaviors)
                if(!B.tick(ticking, tickID))
                	return false;
            for(Ability A : lightPresenceAbilities)
                if(!A.tick(A.invoker(), tickID))
                	return false;
        	return true;
        }
    }
    
    public class DefaultFactionZapFactor implements Faction.FactionZapFactor
    {
    	private double gainF=1.0;
    	private double lossF=1.0;
    	private String mask="";
    	private Vector compiledMask=null;
    	public DefaultFactionZapFactor(double gain, double loss, String mask)
    	{
    		setGainFactor(gain);
    		setLossFactor(loss);
    		setMOBMask(mask);
    	}
        public double gainFactor() { return gainF;}
        public void setGainFactor(double val){gainF=val;}
        public double lossFactor(){return lossF;}
        public void setLossFactor(double val){lossF=val;}
        public String MOBMask(){return mask;}
        public Vector compiledMOBMask(){return compiledMask;}
        public void setMOBMask(String str){
        	mask=str;
        	compiledMask=CMLib.masking().maskCompile(str);
        }
        public String toString(){ return gainF+";"+lossF+";"+mask; }
    }
    
    public class DefaultFactionReactionItem implements Faction.FactionReactionItem
    {
    	private String reactionObjectID="";
    	private String mobMask="";
    	private String rangeName="";
    	private String parms="";
    	private Vector compiledMobMask=null;
        public String reactionObjectID(){return reactionObjectID;}
        public void setReactionObjectID(String str){reactionObjectID=str;}
        public String presentMOBMask(){return mobMask;}
        public void setPresentMOBMask(String str){
        	mobMask=str;
        	if((str==null)||(str.trim().length()==0))
        		compiledMobMask=null;
        	else
	        	compiledMobMask=CMLib.masking().maskCompile(str);
        }
        public Vector compiledPresentMOBMask(){ return compiledMobMask;}
        public String rangeName(){return rangeName;}
        public void setRangeName(String str){rangeName=str.toUpperCase().trim();}
        public String parameters(){return parms;}
        public String parameters(String name){ return CMStrings.replaceAll(parms,"<TARGET>", name);}
        public void setParameters(String str){parms=str;}
        public String toString(){ return rangeName+";"+mobMask+";"+reactionObjectID+";"+parms;}
        public DefaultFactionReactionItem(){}

        public DefaultFactionReactionItem(String key)
        {
        	int x=key.indexOf(';');
        	String str = key.substring(0,x).toUpperCase().trim();
        	String rest = key.substring(x+1);
        	setRangeName(str);
        	
        	x=rest.indexOf(';');
        	str = rest.substring(0,x).trim();
        	rest = rest.substring(x+1);
        	setPresentMOBMask(str);
        	
        	x=rest.indexOf(';');
        	str = rest.substring(0,x).trim();
        	rest = rest.substring(x+1);
        	setReactionObjectID(str);
        	setParameters(rest);
        }
        
    }
    
    public class DefaultFactionAbilityUsage implements Faction.FactionAbilityUsage
    {
        public String ID="";
        public boolean possibleAbilityID=false;
        public int type=-1;
        public int domain=-1;
        public int flag=-1;
        public int low=0;
        public int high=0;
        public int notflag=-1;

        public String abilityFlags(){return ID;}
        public boolean possibleAbilityID(){return possibleAbilityID;}
        public int type(){return type;}
        public int domain(){return domain;}
        public int flag(){return flag;}
        public int low(){return low;}
        public int high(){return high;}
        public int notflag(){return notflag;}
        public void setLow(int newVal){low=newVal;}
        public void setHigh(int newVal){high=newVal;}

        public DefaultFactionAbilityUsage(){}
        public DefaultFactionAbilityUsage(String key)
        {
            Vector v = CMParms.parseSemicolons(key,false);
            setAbilityFlag((String)v.firstElement());
            low = CMath.s_int( (String) v.elementAt(1));
            high = CMath.s_int( (String) v.elementAt(2));
        }
        public String toString()
        {
            return ID+";"+low+";"+high;
        }

        public Vector setAbilityFlag(String str)
        {
            ID=str;
            Vector flags=CMParms.parse(ID);
            Vector unknowns=new Vector();
            possibleAbilityID=false;
            for(int f=0;f<flags.size();f++)
            {
                String strflag=(String)flags.elementAt(f);
                boolean not=strflag.startsWith("!");
                if(not) strflag=strflag.substring(1);
                switch(CMLib.factions().getAbilityFlagType(strflag))
                {
                case 1:
                    type=CMParms.indexOfIgnoreCase(Ability.ACODE_DESCS, strflag);
                    break;
                case 2:
                    domain=CMParms.indexOfIgnoreCase(Ability.DOMAIN_DESCS, strflag);
                    break;
                case 3:
                    int val=CMParms.indexOfIgnoreCase(Ability.FLAG_DESCS, strflag);
                    if(not)
                    {
                        if(notflag<0) notflag=0;
                        notflag=notflag|(int)CMath.pow(2,val);
                    }
                    else
                    {
                        if(flag<0) flag=0;
                        flag=flag|(int)CMath.pow(2,val);
                    }
                    break;
                default:
                    unknowns.addElement(strflag);
                    break;
                }
            }
            if((type<0)&&(domain<0)&&(flag<0))
                possibleAbilityID=true;
            return unknowns;
        }
    }
}
