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
import java.lang.reflect.*;
/**
 * Portions Copyright (c) 2003 Jeremy Vyska
 * Portions Copyright (c) 2004-2008 Bo Zimmerman
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

public class DefaultFaction implements Faction, MsgListener
{
    public String ID(){return "DefaultFaction";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultFaction();}}
    public void initializeClass(){}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	protected String ID="";
	protected String name="";
	protected String choiceIntro="";
	protected long[] lastAffectBehaviorChange=new long[1];
	protected int minimum=Integer.MIN_VALUE;
	protected int middle=0;
	protected int difference;
	protected int maximum=Integer.MAX_VALUE;
	protected int highest=Integer.MAX_VALUE;
	protected int lowest=Integer.MIN_VALUE;
	protected String experienceFlag="";
	protected boolean showInScore=false;
	protected boolean showInSpecialReported=false;
	protected boolean showInEditor=false;
	protected boolean showInFactionsCommand=true;
	protected Hashtable ranges=new Hashtable();
	protected Hashtable affBehavs=new Hashtable();
	protected Vector defaults=new Vector();
	protected Vector autoDefaults=new Vector();
	protected double rateModifier=1.0;
    protected Hashtable changes=new Hashtable();
    protected Vector factors=new Vector();
    protected Hashtable relations=new Hashtable();
    protected Vector abilityUsages=new Vector();
    protected Vector choices=new Vector();

    public String factionID(){return ID;}
    public String name(){return name;}
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
    public Enumeration ranges(){ return DVector.s_enum(ranges,false); }
    public Enumeration defaults(){return DVector.s_enum(defaults);}
    public Enumeration autoDefaults(){return DVector.s_enum(autoDefaults);}
    public double rateModifier(){return rateModifier;}
    public Enumeration changeEventKeys(){return  DVector.s_enum(changes,true);}
    public Enumeration factors(){return  DVector.s_enum(factors);}
    public Enumeration relationFactions(){return  DVector.s_enum(relations,true);}
    public Enumeration abilityUsages(){return  DVector.s_enum(abilityUsages);}
    public Enumeration choices(){return  DVector.s_enum(choices);}
    
    public void setFactionID(String newStr){ID=newStr;}
    public void setName(String newStr){name=newStr;}
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
    public boolean delFactor(Object[] o){ return factors.remove(o); }
    public Object[] getFactor(int x){ return ((x>=0)&&(x<factors.size()))?(Object[])factors.elementAt(x):null;}
    public Object[] addFactor(double gain, double loss, String mask){
        Object[] o=new Object[]{new Double(gain),new Double(loss),mask};
        factors.addElement(o);
        return o;
    }
    public boolean delRelation(String factionID) { return relations.remove(factionID)!=null;}
    public boolean addRelation(String factionID, double relation) {
        if(relations.containsKey(factionID))
            return false;
        relations.put(factionID,new Double(relation));
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
        ranges=new Hashtable();
        changes=new Hashtable();
        factors=new Vector();
        relations=new Hashtable();
        abilityUsages=new Vector();
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
                addChangeEvent(words);
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
                    factors.add(new Object[]{new Double(CMath.s_double((String)factor.elementAt(0))),
                                             new Double(CMath.s_double((String)factor.elementAt(1))),
                                             (String)factor.elementAt(2)});
            }
            if(key.startsWith("RELATION"))
            {
                Vector v=CMParms.parse(words);
                if(v.size()>=2)
                {
                    String who=(String)v.elementAt(0);
                    double factor;
                    String amt=((String)v.elementAt(1)).trim();
                    if(amt.endsWith("%"))
                        factor=CMath.s_pct(amt);
                    else
                        factor=1;
                    relations.put(who,new Double(factor));
                }
            }
            if(key.startsWith("ABILITY"))
                addAbilityUsage(words);
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
            if((numCall<0)||(numCall>=changes.size()))
                return ""+changes.size();
            int i=0;
            for(Enumeration e=changes.elements();e.hasMoreElements();)
            {
                FactionChangeEvent FC=(FactionChangeEvent)e.nextElement();
                if(i==numCall)
                    return FC.toString();
                i++;
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
            return CMParms.toSemicolonList((Object[])factors.elementAt(numCall));
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
        FactionData data=new DefaultFactionData(lastAffectBehaviorChange);
        Vector V=new Vector();
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
                    V.addElement(B);
                }
                else
                {
                    Ability A=CMClass.getAbility(ID);
                    A.setMiscText(stuff[0]);
                    A.setAffectedOne(mob);
                    V.addElement(A);
                }
            }
        }
        data.addListenersNTickers(V,V);
        return data;
    }
    
    public Enumeration affectsBehavs(){return  DVector.s_enum(affBehavs,true);}

    public boolean delAffectBehav(String ID) {
        boolean b=affBehavs.remove(ID.toUpperCase().trim())!=null;
        if(b) lastAffectBehaviorChange[0]=System.currentTimeMillis();
        return b;
    }
    
    public boolean addAffectBehav(String ID, String parms, String gainMask) {
        if(affBehavs.containsKey(ID.toUpperCase().trim())) return false;
        if((CMClass.getBehavior(ID)==null)&&(CMClass.getAbility(ID)==null))
            return false;
        affBehavs.put(ID.toUpperCase().trim(),new String[]{parms,gainMask});
        lastAffectBehaviorChange[0]=System.currentTimeMillis();
        return true;
    }
    
    public String[] getAffectBehav(String ID) {
        if(affBehavs.containsKey(ID.toUpperCase().trim()))
            return CMParms.toStringArray(CMParms.makeVector((String[])affBehavs.get(ID.toUpperCase().trim())));
        return null;
    }
    
    public FactionChangeEvent getChangeEvent(String key) 
    {
        if(changes.containsKey(key)) 
            return (FactionChangeEvent)changes.get(key);
        return null;
    }

    public Vector findChoices(MOB mob)
    {
        Vector mine=new Vector();
        String s;
        for(Enumeration e=choices.elements();e.hasMoreElements();) 
        {
            s=(String)e.nextElement();
            if(CMath.isInteger(s))
                mine.addElement(new Integer(CMath.s_int(s)));
            else
            if(CMLib.masking().maskCheck(s, mob,false)) 
            {
                Vector v=CMParms.parse(s);
                for(int j=0;j<v.size();j++) 
                {
                    if(CMath.isInteger((String)v.elementAt(j)))
                        mine.addElement(new Integer(CMath.s_int((String)v.elementAt(j))));
                }
            }
        }
        return mine;
    }
    
    
    public FactionChangeEvent findChangeEvent(Ability key) 
    {
        if(key==null) return null;
        // Direct ability ID's
        if(changes.containsKey(key.ID()))
            return (FactionChangeEvent)changes.get(key.ID().toUpperCase());
        // By TYPE or FLAGS
        FactionChangeEvent C =null;
        for (Enumeration e=changes.elements();e.hasMoreElements();) 
        {
            C= (FactionChangeEvent)e.nextElement();
            if((key.classificationCode()&Ability.ALL_ACODES)==C.IDclassFilter())
                return C;
            if((key.classificationCode()&Ability.ALL_DOMAINS)==C.IDdomainFilter())
                return C;
            if((C.IDflagFilter()>0)&&(CMath.bset(key.flags(),C.IDflagFilter()))) 
                return C;
        }
        return null;
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
                Vector v=CMParms.parse(s);
                for(int j=0;j<v.size();j++) 
                {
                    if(CMath.isNumber((String)v.elementAt(j)))
                        return CMath.s_int((String)v.elementAt(j));
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
                Vector v=CMParms.parse(s);
                for(int j=0;j<v.size();j++) 
                {
                    if(CMath.isNumber((String)v.elementAt(j)))
                        return CMath.s_int((String)v.elementAt(j));
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
        Object[] factor=null;
        for(int i=0;i<factors.size();i++)
        {
            factor=(Object[])factors.elementAt(i);
            if(CMLib.masking().maskCheck(((String)factor[2]),mob,false)) 
            {
                 if(gain)
                     return ((Double)factor[0]).doubleValue();
                 return ((Double)factor[1]).doubleValue();
             }
        }
        return 1.0;
    }

    public void executeMsg(Environmental myHost, CMMsg msg) 
    {
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)    // A death occured
		&&(msg.tool() instanceof MOB)
		&&(myHost instanceof MOB))
        {
            MOB killedM=msg.source();
            final String[] murders={"MURDER","MURDER2","MURDER3","MURDER4","MURDER5"};
            for(int m=0;m<murders.length;m++)
            {
                FactionChangeEvent eventC=getChangeEvent(murders[m]);
                if((eventC!=null)&&eventC.applies(killedM))
                {
                    MOB killingBlowM=(MOB)msg.tool();
                    CharClass combatCharClass=CMLib.combat().getCombatDominantClass(killingBlowM,killedM);
                    HashSet combatBeneficiaries=CMLib.combat().getCombatBeneficiaries(killingBlowM,killedM,combatCharClass);
                    if(combatBeneficiaries.contains(myHost))
                    {
                        MOB killerM=(MOB)myHost;
                        executeChange(killerM,killedM,eventC);
                    }
                    if(myHost==msg.source())
                        for(Iterator i=combatBeneficiaries.iterator();i.hasNext();)
                        {
                            MOB killerM=(MOB)i.next();
                            if(!hasFaction(killerM))
                                executeChange(killerM,killedM,eventC);
                        }
                }
            }
        }

        // Ability Watching
        if((msg.tool() instanceof Ability)
        &&(msg.othersMessage()!=null)
        &&(findChangeEvent((Ability)msg.tool())!=null))
        {
            FactionChangeEvent C=findChangeEvent((Ability)msg.tool());
            if((msg.target() instanceof MOB)&&(C.applies((MOB)msg.target())))
                executeChange(msg.source(),(MOB)msg.target(),C);
            else 
            if (!(msg.target() instanceof MOB))
                executeChange(msg.source(),null,C);
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
	            if(levelFactor>new Integer(levelLimit).doubleValue())
	                levelFactor=new Integer(levelLimit).doubleValue();
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
	            			    		*Math.abs(new Integer(sourceFaction-targetFaction).doubleValue()
	            			    				/Math.abs(new Integer(difference).doubleValue())));
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
        if(source.location()!=null)
        {
            if(source.location().okMessage(source,FacMsg))
            {
                source.location().send(source, FacMsg);
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
	                    if(source.location().okMessage(source,FacMsg))
	                        source.location().send(source, FacMsg);
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
     
     public Faction.FactionChangeEvent addChangeEvent(String key)
     {
         Faction.FactionChangeEvent event;
         if(key==null)
             event=new DefaultFaction.DefaultFactionChangeEvent();
         else
         if(key.indexOf(';')<0)
         {
             event=new DefaultFaction.DefaultFactionChangeEvent();
             if(!event.setEventID(key))
                 return null;
         }
         else
             event=new DefaultFaction.DefaultFactionChangeEvent(key);
         changes.put(event.eventID().toUpperCase().trim(),event);
         return event;
     }
     
     public boolean delChangeEvent(String eventKey)
     {
         return changes.remove(eventKey)!=null;
     }
     
     public static class DefaultFactionChangeEvent implements Faction.FactionChangeEvent
     {
        public String ID="";
        public String flagCache="";
        public int IDclassFilter=-1;
        public int IDflagFilter=-1;
        public int IDdomainFilter=-1;
        public int direction=0;
        public double factor=0.0;
        public String zapper="";
        public boolean outsiderTargetOK=false;
        public boolean selfTargetOK=false;
        public boolean just100=false;
        
        public String eventID(){return ID;}
        public String flagCache(){return flagCache;}
        public int IDclassFilter(){return IDclassFilter;}
        public int IDflagFilter(){return IDflagFilter;}
        public int IDdomainFilter(){return IDdomainFilter;}
        public int direction(){return direction;}
        public double factor(){return factor;}
        public String zapper(){return zapper;}
        public boolean outsiderTargetOK(){return outsiderTargetOK;}
        public boolean selfTargetOK(){return selfTargetOK;}
        public boolean just100(){return just100;}
        public void setDirection(int newVal){direction=newVal;}
        public void setFactor(double newVal){factor=newVal;}
        public void setZapper(String newVal){zapper=newVal;}

        public String toString()
        {
            return ID+";"+CHANGE_DIRECTION_DESCS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+zapper;
        }

        public DefaultFactionChangeEvent(){}
        
        public DefaultFactionChangeEvent(String key) 
        {
            Vector v = CMParms.parseSemicolons(key,false);
            setEventID((String)v.elementAt(0));
            setDirection((String)v.elementAt(1));
            String amt=((String)v.elementAt(2)).trim();
            if(amt.endsWith("%"))
                factor=CMath.s_pct(amt);
            else
                factor=1.0;
            
            if(v.size()>3)
                setFlags((String)v.elementAt(3));
            if(v.size()>4) 
                zapper = (String)v.elementAt(4);
        }
        
        public boolean setEventID(String newID)
        {
            IDclassFilter=-1;
            IDflagFilter=-1;
            IDdomainFilter=-1;
            for(int i=0;i<MISC_TRIGGERS.length;i++) 
                if(MISC_TRIGGERS[i].equalsIgnoreCase(newID))
                { ID=newID;    return true;}
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
        public boolean applies(MOB mob) 
        {
            if(zapper==null) return true;
            return CMLib.masking().maskCheck(zapper,mob,false);
        }
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
    public static class DefaultFactionRange implements Faction.FactionRange
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
    
    public static class DefaultFactionData implements FactionData
    {
        private boolean noListeners=false;
        private boolean noTickers=false;
        private static Enumeration empty=new Vector().elements();
        public DefaultFactionData(long[] factionLastUpdated) {
            this.factionLastUpdated=factionLastUpdated;
        }
        public long lastUpdated=System.currentTimeMillis();
        public long[] factionLastUpdated=new long[0];
        public Vector listeners=new Vector();
        public Vector tickers=listeners;
        public int value=0;
        
        public int value() { return value;}
        public void setValue(int newValue){ this.value=newValue;}
        public Enumeration listeners() { return noListeners?empty:DVector.s_enum(listeners); }
        public Enumeration tickers() { return noTickers?empty:DVector.s_enum(tickers); }
        public void addListenersNTickers(Vector listeners, Vector tickers) {
            this.listeners=listeners;
            this.tickers=tickers;
            noListeners=listeners.size()==0;
            noTickers=tickers.size()==0;
        }
        public boolean requiresUpdating() { return factionLastUpdated[0] > lastUpdated; }
    }
    
    
    public static class DefaultFactionAbilityUsage implements Faction.FactionAbilityUsage
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
