package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
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

public class Faction implements MsgListener
{
	public String ID="";
    public String name="";
    public String choiceIntro="";
    public int minimum=Integer.MIN_VALUE;
    public int middle=0;
    public int difference;
    public int maximum=Integer.MAX_VALUE;
    public int highest=Integer.MAX_VALUE;
    public int lowest=Integer.MIN_VALUE;
    public String experienceFlag="";
    public boolean showinscore=false;
    public boolean showinspecialreported=false;
    public boolean showineditor=false;
    public boolean showinfactionscommand=true;
    public Vector ranges=new Vector();
    public Vector defaults=new Vector();
    public Vector autoDefaults=new Vector();
    public double rateModifier=1.0;
    public Hashtable Changes=new Hashtable();
    public Vector factors=new Vector();
    public Hashtable relations=new Hashtable();
    public Vector abilityUsages=new Vector();
    public Vector choices=new Vector();

    public final static int ALIGN_INDIFF=0;
    public final static int ALIGN_EVIL=1;
    public final static int ALIGN_NEUTRAL=2;
    public final static int ALIGN_GOOD=3;
    public final static String[] ALIGN_NAMES={"","EVIL","NEUTRAL","GOOD"};
    public final static String[] EXPAFFECT_NAMES={"NONE","EXTREME","HIGHER","LOWER","FOLLOWHIGHER","FOLLOWLOWER"};
    public final static int TAG_NAME=0;
    public final static int TAG_MINIMUM=1;
    public final static int TAG_MAXIMUM=2;
    public final static int TAG_SCOREDISPLAY=3;
    public final static int TAG_SPECIALREPORTED=4;
    public final static int TAG_EDITALONE=5;
    public final static int TAG_DEFAULT=6;
    public final static int TAG_AUTODEFAULTS=7;
    public final static int TAG_AUTOCHOICES=8;
    public final static int TAG_CHOICEINTRO=9;
    public final static int TAG_RATEMODIFIER=10;
    public final static int TAG_EXPERIENCE=11;
    public final static int TAG_RANGE_=12;
    public final static int TAG_CHANGE_=13;
    public final static int TAG_ABILITY_=14;
    public final static int TAG_FACTOR_=15;
    public final static int TAG_RELATION_=16;
    public final static int TAG_SHOWINFACTIONSCMD=17;
    public final static String[] ALL_TAGS={"NAME","MINIMUM","MAXIMUM","SCOREDISPLAY","SPECIALREPORTED","EDITALONE","DEFAULT",
        "AUTODEFAULTS","AUTOCHOICES","CHOICEINTRO","RATEMODIFIER","EXPERIENCE","RANGE*","CHANGE*","ABILITY*","FACTOR*","RELATION*",
        "SHOWINFACTIONSCMD"};

    public Faction(String aname)
    {
        ID=aname;
        name=aname;
        minimum=0;
        middle=50;
        maximum=100;
        highest=100;
        lowest=0;
        difference=Util.abs(maximum-minimum);
        experienceFlag="EXTREME";
        ranges.addElement(new Faction.FactionRange(this,"0;100;Sample Range;SAMPLE;"));
        defaults.addElement("0");
    }

    public Faction(StringBuffer file, String fID) 
    {
        boolean debug = false;
        
        ID = fID;
        INI alignProp = new INI(new ByteArrayInputStream(file.toString().getBytes()));
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
        middle=minimum+(int)Math.round(Util.div(maximum-minimum,2.0));
        difference=Util.abs(maximum-minimum);
        experienceFlag=alignProp.getStr("EXPERIENCE").toUpperCase().trim();
        if(experienceFlag.length()==0) experienceFlag="NONE";
        rateModifier=alignProp.getDouble("RATEMODIFIER");
        showinscore=alignProp.getBoolean("SCOREDISPLAY");
        showinfactionscommand=alignProp.getBoolean("SHOWINFACTIONSCMD");
        showinspecialreported=alignProp.getBoolean("SPECIALREPORTED");
        showineditor=alignProp.getBoolean("EDITALONE");
        defaults =Util.parseSemicolons(alignProp.getStr("DEFAULT"),true);
        autoDefaults =Util.parseSemicolons(alignProp.getStr("AUTODEFAULTS"),true);
        choices =Util.parseSemicolons(alignProp.getStr("AUTOCHOICES"),true);
        ranges=new Vector();
        Changes=new Hashtable();
        factors=new Vector();
        relations=new Hashtable();
        abilityUsages=new Vector();
        highest=Integer.MIN_VALUE;
        lowest=Integer.MAX_VALUE;
        for(Enumeration e=alignProp.keys();e.hasMoreElements();)
        {
            if(debug) Log.sysOut("FACTIONS","Starting Key Loop");
            String key = (String) e.nextElement();
            if(debug) Log.sysOut("FACTIONS","  Key Found     :"+key);
            String words = (String) alignProp.get(key);
            if(debug) Log.sysOut("FACTIONS","  Words Found   :"+words);
            if(key.startsWith("RANGE"))
            {
                FactionRange R=new FactionRange(this,words);
                ranges.add(R);
                if(R.low<lowest) lowest=R.low;
                if(R.high>highest) highest=R.high;
            }
            if(key.startsWith("CHANGE"))
            {
                FactionChangeEvent C=new FactionChangeEvent(words);
                Changes.put(C.ID.toUpperCase(),C);
            }
            if(key.startsWith("FACTOR"))
            {
                Vector factor=Util.parseSemicolons(words,false);
                factors.add(factor);
            }
            if(key.startsWith("RELATION"))
            {
                Vector v=Util.parse(words);
                if(v.size()>=2)
                {
                    String who=(String)v.elementAt(0);
                    double factor;
                    String amt=((String)v.elementAt(1)).trim();
                    if(amt.endsWith("%"))
                        factor=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
                    else
                        factor=1;
                    relations.put(who,new Double(factor));
                }
            }
            if(key.startsWith("ABILITY"))
            {
                FactionAbilityUsage A=new FactionAbilityUsage(words);
                abilityUsages.add(A);
            }
        }
    }

    public static int isTag(String tag)
    {
        for(int i=0;i<ALL_TAGS.length;i++)
            if(tag.equalsIgnoreCase(ALL_TAGS[i]))
                return i;
            else
            if(ALL_TAGS[i].endsWith("*")&&tag.startsWith(ALL_TAGS[i].substring(0,ALL_TAGS[i].length()-1)))
                return i;
        return -1;
    }
    
    public String getTagValue(String tag)
    {
        int tagRef=isTag(tag);
        if(tagRef<0) return "";
        int numCall=-1;
        if((tagRef<ALL_TAGS.length)&&(ALL_TAGS[tagRef].endsWith("*")))
            if(Util.isInteger(tag.substring(ALL_TAGS[tagRef].length()-1)))
                numCall=Util.s_int(tag.substring(ALL_TAGS[tagRef].length()-1));
        switch(tagRef)
        {
        case TAG_NAME: return name;
        case TAG_MINIMUM: return ""+minimum;
        case TAG_MAXIMUM: return ""+maximum;
        case TAG_SCOREDISPLAY: return Boolean.toString(showinscore).toUpperCase();
        case TAG_SHOWINFACTIONSCMD: return Boolean.toString(showinfactionscommand).toUpperCase();
        case TAG_SPECIALREPORTED: return Boolean.toString(showinspecialreported).toUpperCase();
        case TAG_EDITALONE: return Boolean.toString(showineditor).toUpperCase();
        case TAG_DEFAULT: return Util.toSemicolonList(defaults);
        case TAG_AUTODEFAULTS: return Util.toSemicolonList(autoDefaults);
        case TAG_CHOICEINTRO: return choiceIntro;
        case TAG_AUTOCHOICES: return Util.toSemicolonList(choices);
        case TAG_RATEMODIFIER: return ""+rateModifier;
        case TAG_EXPERIENCE: return ""+experienceFlag;
        case TAG_RANGE_:
        {
            if((numCall<0)||(numCall>=ranges.size()))
                return ""+ranges.size();
            return ((FactionRange)ranges.elementAt(numCall)).toString();
        }
        case TAG_CHANGE_:
        {
            if((numCall<0)||(numCall>=Changes.size()))
                return ""+Changes.size();
            int i=0;
            for(Enumeration e=Changes.elements();e.hasMoreElements();)
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
            return Util.toSemicolonList((Vector)factors.elementAt(numCall));
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
                    return factionName+" "+((int)Math.round(D.doubleValue()*100.0))+"%";
                i++;
            }
            return "";
        }
        }
        return "";
    }
    
    public String getINIDef(String tag, String delimeter)
    {
        int tagRef=isTag(tag);
        if(tagRef<0)
            return "";
        String rawTagName=ALL_TAGS[tagRef];
        if(ALL_TAGS[tagRef].endsWith("*"))
        {
            int number=Util.s_int(getTagValue(rawTagName));
            StringBuffer str=new StringBuffer("");
            for(int i=0;i<number;i++)
            {
                String value=getTagValue(rawTagName.substring(0,rawTagName.length()-1)+i);
                str.append(rawTagName.substring(0,rawTagName.length()-1)+(i+1)+"="+value+delimeter);
            }
            return str.toString();
        }
        else
            return rawTagName+"="+getTagValue(tag)+delimeter;
    }
    
    public FactionChangeEvent findChangeEvent(String key) 
    {
        if(Changes.containsKey(key)) 
            return (FactionChangeEvent)Changes.get(key.toUpperCase());
        return null;
    }

    public Vector findChoices(MOB mob)
    {
        Vector mine=new Vector();
        if(choices!=null) 
        {
            for(int i=0;i<choices.size();i++) 
            {
                if(Util.isInteger((String)choices.elementAt(i)))
                    mine.addElement(new Integer(Util.s_int((String)choices.elementAt(i))));
                else
                if(MUDZapper.zapperCheck((String)choices.elementAt(i), mob)) 
                {
                    Vector v=Util.parse((String)choices.elementAt(i));
                    for(int j=0;j<v.size();j++) 
                    {
                        if(Util.isInteger((String)v.elementAt(j)))
                            mine.addElement(new Integer(Util.s_int((String)v.elementAt(j))));
                    }
                }
            }
        }
        return mine;
    }
    
    
    public FactionChangeEvent findChangeEvent(Ability key) 
    {
        if(key==null) return null;
        // Direct ability ID's
        if(Changes.containsKey(key.ID()))
            return (FactionChangeEvent)Changes.get(key.ID().toUpperCase());
        // By TYPE or FLAGS
        FactionChangeEvent C =null;
        for (Enumeration e=Changes.elements();e.hasMoreElements();) 
        {
            C= (FactionChangeEvent)e.nextElement();
            if((key.classificationCode()&Ability.ALL_CODES)==C.IDclassFilter)
                return C;
            if((key.classificationCode()&Ability.ALL_DOMAINS)==C.IDdomainFilter)
                return C;
            if((C.IDflagFilter>0)&&(Util.bset(key.flags(),C.IDflagFilter))) 
                return C;
        }
        return null;
    }

    public Vector fetchRanges() 
    {
        return ranges;
    }

    public FactionRange fetchRange(int faction) 
    {
        if(ranges!=null) 
        {
            for (int i = 0; i < ranges.size(); i++) 
            {
                FactionRange R = (FactionRange) ranges.elementAt(i);
                if ( (faction >= R.low) && (faction <= R.high))
                    return R;
            }
        }
        return null;
    }
    public String fetchRangeName(int faction) 
    {
        if(ranges!=null) 
        {
            for (int i = 0; i < ranges.size(); i++) 
            {
                FactionRange R = (FactionRange) ranges.elementAt(i);
                if ( (faction >= R.low) && (faction <= R.high))
                    return R.Name;
            }
        }
        return "";
    }

    public int asPercent(int faction) 
    {
        return (int)Math.round(Util.mul(Util.div(faction-minimum,(maximum-minimum)),100));
    }

    public int asPercentFromAvg(int faction) 
    {
        // =(( (B2+A2) / 2 ) - C2) / (B2-A2) * 100
        // C = current, A = min, B = Max
        return (int)Math.round(Util.mul(Util.div(((maximum+minimum)/2)-faction,maximum-minimum),100));
    }

    public int randomFaction() 
    {
        Random gen = new Random();
        return maximum - gen.nextInt(maximum-minimum);
    }

    public int findDefault(MOB mob) 
    {
        if(defaults!=null) 
        {
            for(int i=0;i<defaults.size();i++) 
            {
                if(MUDZapper.zapperCheck((String)defaults.elementAt(i), mob)) 
                {
                    Vector v=Util.parse((String)defaults.elementAt(i));
                    for(int j=0;j<v.size();j++) 
                    {
                        if(Util.isNumber((String)v.elementAt(j)))
                            return Util.s_int((String)v.elementAt(j));
                    }
                }
                else
                {
                    if(Util.isNumber((String)defaults.elementAt(i)))
                         return Util.s_int((String)defaults.elementAt(i));
                }
            }
        }
        return 0;
    }

    public int findAutoDefault(MOB mob) 
    {
        if(autoDefaults!=null) 
        {
            for(int i=0;i<autoDefaults.size();i++) 
            {
                if(MUDZapper.zapperCheck((String)autoDefaults.elementAt(i), mob)) 
                {
                    Vector v=Util.parse((String)autoDefaults.elementAt(i));
                    for(int j=0;j<v.size();j++) 
                    {
                        if(Util.isNumber((String)v.elementAt(j)))
                            return Util.s_int((String)v.elementAt(j));
                    }
                }
                else
                {
                    if(Util.isNumber((String)autoDefaults.elementAt(i)))
                         return Util.s_int((String)autoDefaults.elementAt(i));
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
            if((usage.possibleAbilityID&&usage.ID.equalsIgnoreCase(A.ID()))
            ||(((usage.type<0)||((A.classificationCode()&Ability.ALL_CODES)==usage.type))
                &&((usage.flag<0)||(Util.bset(A.flags(),usage.flag)))
                &&((usage.notflag<0)||(!Util.bset(A.flags(),usage.notflag)))
                &&((usage.domain<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain))))
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
            if((usage.possibleAbilityID&&usage.ID.equalsIgnoreCase(A.ID()))
            ||(((usage.type<0)||((A.classificationCode()&Ability.ALL_CODES)==usage.type))
                &&((usage.flag<0)||(Util.bset(A.flags(),usage.flag)))
                &&((usage.notflag<0)||(!Util.bset(A.flags(),usage.notflag)))
                &&((usage.domain<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain))))
            {
                int faction=mob.fetchFaction(ID);
                if((faction < usage.low) || (faction > usage.high)) 
                    return false;
            }
        }
        return true;
    }

    public double findFactor(MOB mob, boolean gain) 
    {
        for(int i=0;i<factors.size();i++)
        {
            Vector factor=(Vector)factors.elementAt(i);
            if((factor.size()>2)
            &&(MUDZapper.zapperCheck(((String)factor.elementAt(2)),mob))) 
            {
                 if(gain)
                     return Util.s_double(((String)factor.elementAt(1)));
                 else
                     return Util.s_double(((String)factor.elementAt(0)));
             }
        }
        return 1.0;
    }

    public void executeMsg(Environmental myHost, CMMsg msg) 
    {
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)    // A death occured
		&&(msg.source()==myHost)
		&&(msg.tool() instanceof MOB))
        {
            FactionChangeEvent C=findChangeEvent("MURDER");
            if((C!=null)&&(C.applies(msg.source())))
                executeChange((MOB)msg.tool(),msg.source(),C);
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
                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(killer.fetchFaction(ID)-minimum),(maximum - minimum)))));
            else
            if(experienceFlag.equals("LOWER"))
                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(maximum-killer.fetchFaction(ID)),(maximum - minimum)))));
            else
            if(vic.fetchFaction(ID)!=Integer.MAX_VALUE)
            {
                if(experienceFlag.equals("EXTREME"))
	                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(vic.fetchFaction(ID) - killer.fetchFaction(ID)),(maximum - minimum)))));
                else
                if(experienceFlag.equals("FOLLOWHIGHER"))
	                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(vic.fetchFaction(ID)-minimum),(maximum - minimum)))));
                else
                if(experienceFlag.equals("FOLLOWLOWER"))
	                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(maximum-vic.fetchFaction(ID)),(maximum - minimum)))));
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
        if((source==target)&&(!event.selfTargetOK)&&(!event.ID.equalsIgnoreCase("TIME")))
            return;
        
        if(target!=null)
        {
            if(hasFaction(target))
	            targetFaction=target.fetchFaction(ID);
            else
            if(!event.outsiderTargetOK)
                return;
        }
        else 
            target = source;
        
        double baseChangeAmount=100.0;
        if((source!=target)&&(target!=null)&&(!event.just100))
        {
	        int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
	        int levelDiff=target.envStats().level()-source.envStats().level();
	
	        if(levelDiff<(-levelLimit) )
	            baseChangeAmount=0.0;
	        else
	        if(levelLimit>0)
	        {
	            double levelFactor=Util.div(levelDiff,levelLimit);
	            if(levelFactor>new Integer(levelLimit).doubleValue())
	                levelFactor=new Integer(levelLimit).doubleValue();
	            baseChangeAmount=baseChangeAmount+Util.mul(levelFactor,100);
	        }
        }

        int factionAdj=1;
        int changeDir=0;
        switch(event.direction) 
        {
        case FactionChangeEvent.FACTION_MAXIMUM:
            factionAdj=maximum-sourceFaction;
        	break;
        case FactionChangeEvent.FACTION_MINIMUM:
            factionAdj=minimum-sourceFaction;
        	break;
        case FactionChangeEvent.FACTION_UP:
            changeDir=1;  
        	break;
        case FactionChangeEvent.FACTION_DOWN:
            changeDir=-1; 
        	break;
        case FactionChangeEvent.FACTION_OPPOSITE:
	        if(source!=target)
	        {
	            changeDir=targetFaction>=0?-1:1;
	            baseChangeAmount=Util.div(baseChangeAmount,2.0)+(int)Math.round(Util.div(baseChangeAmount,2.0)*Math.abs(new Integer(sourceFaction-targetFaction).doubleValue()/Math.abs(new Integer(difference).doubleValue())));
	        }
	        else
	            factionAdj=0;
        	break;
        case FactionChangeEvent.FACTION_AWAY:
	        if(source!=target)
	            changeDir=targetFaction>=sourceFaction?-1:1;
	        else
	            factionAdj=0;
        	break;
        case FactionChangeEvent.FACTION_TOWARD:
	        if(source!=target)
	            changeDir=targetFaction>=sourceFaction?1:-1;
	        else
	            factionAdj=0;
        	break;
        case FactionChangeEvent.FACTION_REMOVE:
            factionAdj=Integer.MAX_VALUE;
        	break;
        case FactionChangeEvent.FACTION_ADD:
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
	        if((hasFaction(target))||(event.outsiderTargetOK))
	            factionAdj=changeDir*(int)Math.round(rateModifier*baseChangeAmount);
	        else
	            factionAdj=0;
	        factionAdj*=event.factor;
	        factionAdj=(int)Math.round(Util.mul(factionAdj,findFactor(source,(factionAdj>=0))));
        }

		if(factionAdj==0) return;
		
        FullMsg FacMsg=new FullMsg(source,target,null,CMMsg.MASK_GENERAL|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,ID);
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
	                    FacMsg=new FullMsg(source,target,null,CMMsg.MASK_GENERAL|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,relID);
	                    FacMsg.setValue((int)Math.round(Util.mul(factionAdj, ((Double)relations.get(relID)).doubleValue())));
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

	 public String usageFactors(Ability A) 
	 { 
         StringBuffer rangeStr=new StringBuffer(); 
         FactionAbilityUsage usage=null;
         HashSet namesAdded=new HashSet();
         for(int i=0;i<abilityUsages.size();i++) 
         {
             usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
             if((usage.possibleAbilityID&&usage.ID.equalsIgnoreCase(A.ID()))
             ||(((usage.type<0)||((A.classificationCode()&Ability.ALL_CODES)==usage.type))
                 &&((usage.flag<0)||(Util.bset(A.flags(),usage.flag)))
                 &&((usage.notflag<0)||(!Util.bset(A.flags(),usage.notflag)))
                 &&((usage.domain<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==usage.domain))))
             {
                for(int r=0;r<ranges.size();r++)
                { 
                     FactionRange R=(FactionRange)ranges.elementAt(r);
                     if((((R.high<=usage.high)&&(R.high>=usage.low))
                         ||((R.low>=usage.low))&&(R.low<=usage.high))
                     &&(!namesAdded.contains(R.Name)))
                     {
                         namesAdded.add(R.Name);
                         if(rangeStr.length()>0) rangeStr.append(", "); 
                         rangeStr.append(R.Name); 
                     }
                }
             }
         }
         return rangeStr.toString(); 
    }
	 
    public static class FactionChangeEvent 
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

        public static final int FACTION_UP = 0;
        public static final int FACTION_DOWN = 1;
        public static final int FACTION_OPPOSITE = 2;
        public static final int FACTION_MINIMUM = 3;
        public static final int FACTION_MAXIMUM = 4;
        public static final int FACTION_REMOVE = 5;
        public static final int FACTION_ADD = 6;
        public static final int FACTION_AWAY = 7;
        public static final int FACTION_TOWARD= 8;
        public static final String[] FACTION_DIRECTIONS={
            "UP",
            "DOWN",
            "OPPOSITE",
            "MINIMUM",
            "MAXIMUM",
            "REMOVE",
            "ADD",
            "AWAY",
            "TOWARD"
        };
        public static final String[] VALID_FLAGS={
            "OUTSIDER","SELFOK","JUST100"
        };
        public static final String[] MISC_TRIGGERS={
            "MURDER","TIME","ADDOUTSIDER"
        };
        
        public String toString()
        {
            return ID+";"+FACTION_DIRECTIONS[direction]+";"+((int)Math.round(factor*100.0))+"%;"+flagCache+";"+zapper;
        }

        public FactionChangeEvent(){}
        
        public FactionChangeEvent(String key) 
        {
            Vector v = Util.parseSemicolons(key,false);
            setFilterID((String)v.elementAt(0));
            setDirection((String)v.elementAt(1));
            String amt=((String)v.elementAt(2)).trim();
            if(amt.endsWith("%"))
                factor=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
            else
                factor=1.0;
            
            if(v.size()>3)
                setFlags((String)v.elementAt(3));
            if(v.size()>4) 
                zapper = (String)v.elementAt(4);
        }
        
        private static String _ALL_TYPES=null;
        public static String ALL_TYPES()
        {
            StringBuffer ALL_TYPES=new StringBuffer("");
            if(_ALL_TYPES!=null) return _ALL_TYPES;
            for(int i=0;i<MISC_TRIGGERS.length;i++) 
                ALL_TYPES.append(MISC_TRIGGERS[i]+", ");
            for(int i=0;i<Ability.TYPE_DESCS.length;i++) 
                ALL_TYPES.append(Ability.TYPE_DESCS[i]+", ");
            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
                ALL_TYPES.append(Ability.DOMAIN_DESCS[i]+", ");
            for(int i=0;i<Ability.FLAG_DESCS.length;i++) 
                ALL_TYPES.append(Ability.FLAG_DESCS[i]+", ");
            _ALL_TYPES=ALL_TYPES.toString()+" a valid Skill, Spell, Chant, etc. ID.";
            return _ALL_TYPES;
        }
        public boolean setFilterID(String newID)
        {
            for(int i=0;i<MISC_TRIGGERS.length;i++) 
                if(MISC_TRIGGERS[i].equalsIgnoreCase(newID))
                { ID=newID;    return true;}
            for(int i=0;i<Ability.TYPE_DESCS.length;i++) 
                if(Ability.TYPE_DESCS[i].equalsIgnoreCase(newID))
                {    IDclassFilter=i; ID=newID; return true;}
            for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
                if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(newID))
                {    IDdomainFilter=i<<5;  ID=newID;return true;}
            for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                if(Ability.FLAG_DESCS[i].equalsIgnoreCase(newID))
                { IDflagFilter=Util.pow(2,i);  ID=newID; return true;}
            if(CMClass.getAbility(newID)!=null)
            { ID=newID; return true;}
            return false;
        }
        public boolean setDirection(String d)
        {
            if(d.startsWith("U")) {
                direction = FACTION_UP;
            }
            else
            if(d.startsWith("D")) {
                direction = FACTION_DOWN;
            }
            else
            if(d.startsWith("OPP")) {
                direction = FACTION_OPPOSITE;
            }
            else
            if(d.startsWith("REM")) {
                direction = FACTION_REMOVE;
            }
            else
            if(d.startsWith("MIN")) {
                direction = FACTION_MINIMUM;
            }
            else
            if(d.startsWith("MAX")) {
                direction = FACTION_MAXIMUM;
            }
            else
            if(d.startsWith("ADD")) {
                direction = FACTION_ADD;
            }
            else
            if(d.startsWith("TOW")) {
                direction = FACTION_TOWARD;
            }
            else
            if(d.startsWith("AWA")) {
                direction = FACTION_AWAY;
            }
            else
                return false;
            return true;
        }
        public void setFlags(String newFlagCache)
        {
            flagCache=newFlagCache.toUpperCase().trim();
            Vector flags=Util.parse(flagCache);
            if(flags.contains("OUTSIDER")) outsiderTargetOK=true;
            if(flags.contains("SELFOK")) selfTargetOK=true;
            if(flags.contains("JUST100")) just100=true;
        }
        public boolean applies(MOB mob) 
        {
            if(zapper==null) return true;
            return MUDZapper.zapperCheck(zapper,mob);
        }
    }

    public static int getAlignEquiv(String str)
    {
        if(str.equalsIgnoreCase(ALIGN_NAMES[Faction.ALIGN_GOOD])) 
            return Faction.ALIGN_GOOD;
        else 
        if(str.equalsIgnoreCase(ALIGN_NAMES[Faction.ALIGN_NEUTRAL])) 
            return Faction.ALIGN_NEUTRAL;
        else 
        if(str.equalsIgnoreCase(ALIGN_NAMES[Faction.ALIGN_EVIL])) 
            return  Faction.ALIGN_EVIL;
        else 
            return  Faction.ALIGN_INDIFF;
    }
    
    public static class FactionRange 
    {
        public String ID="";
        public int low;
        public int high;
        public String Name="";
        public String CodeName="";
        public int AlignEquiv;
        public Faction myFaction=null;

        public FactionRange(Faction F, String key) 
        {
            myFaction=F;
            Vector v = Util.parseSemicolons(key,false);
            Name = (String) v.elementAt(2);
            low = Util.s_int( (String) v.elementAt(0));
            high = Util.s_int( (String) v.elementAt(1));
            if(v.size()>3)
                CodeName=(String)v.elementAt(3);
            if(v.size()>4) 
                AlignEquiv = getAlignEquiv((String)v.elementAt(4));
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

    public static class FactionAbilityUsage 
    {
        public String ID="";
        public boolean possibleAbilityID=false;
        public int type=-1;
        public int domain=-1;
        public int flag=-1;
        public int low=0;
        public int high=0;
        public int notflag=-1;

        public FactionAbilityUsage(){} 
        public FactionAbilityUsage(String key) 
        {
            Vector v = Util.parseSemicolons(key,false);
            setAbilityFlag((String)v.firstElement());
            low = Util.s_int( (String) v.elementAt(1));
            high = Util.s_int( (String) v.elementAt(2));
        }
        public String toString()
        {
            return ID+";"+low+";"+high;
        }
        public Vector setAbilityFlag(String str)
        {
            ID=str;
            Vector flags=Util.parse(ID);
            Vector unknowns=new Vector();
            for(int f=0;f<flags.size();f++)
            {
                String strflag=(String)flags.elementAt(f);
                boolean not=strflag.startsWith("!");
                if(not) strflag=strflag.substring(1);
                boolean known=false;
                for(int i=0;i<Ability.TYPE_DESCS.length;i++) 
                    if(Ability.TYPE_DESCS[i].equalsIgnoreCase(strflag))
                    {
                        type=i;
                        known=true;
                    }
                if(!known)
                for(int i=0;i<Ability.DOMAIN_DESCS.length;i++) 
                    if(Ability.DOMAIN_DESCS[i].equalsIgnoreCase(strflag))
                    {
                        domain=i<<5;
                        known=true;
                    }
                if(!known)
                for(int i=0;i< Ability.FLAG_DESCS.length;i++)
                    if(Ability.FLAG_DESCS[i].equalsIgnoreCase(strflag))
                    {
                        known=true;
                        if(not)
                        {
                            if(notflag<0) notflag=0;
                            notflag=notflag|Util.pow(2,i);
                        }
                        else
                        {
                            if(flag<0) flag=0;
                            flag=flag|Util.pow(2,i);
                        }
                    }
                if(!known)
                    unknowns.addElement(strflag);
            }
            if((type<0)&&(domain<0)&&(flag<0))
                possibleAbilityID=true;
            return unknowns;
        }
    }
}
