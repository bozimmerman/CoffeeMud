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
	public String ID;
    public String name;
    public int minimum;
    public int maximum;
    public boolean experience;
    public Vector ranges;
    public Vector defaults;
    public double rateModifier;
    public Hashtable Changes;
    public Hashtable ChangeMap;
    public Vector factors;
    public Hashtable relations;
    public Vector abilityUsages;

    public final static int ALIGN_INDIFF=0;
    public final static int ALIGN_EVIL=1;
    public final static int ALIGN_NEUTRAL=2;
    public final static int ALIGN_GOOD=3;

    public Faction(StringBuffer file, String fID) 
    {
        boolean debug = false;

        try 
        {
            ID = fID;
            Properties alignProp = new Properties();
            alignProp.load(new ByteArrayInputStream(file.toString().getBytes()));
			if(alignProp.isEmpty()) return;
            name=alignProp.getProperty("NAME");
            minimum=(new Integer(alignProp.getProperty("MINIMUM")).intValue());
            maximum=(new Integer(alignProp.getProperty("MAXIMUM")).intValue());
            experience=(new Boolean(alignProp.getProperty("EXPERIENCE")).booleanValue());
            rateModifier=new Double(alignProp.getProperty("RATEMODIFIER")).doubleValue();
            setupDefaults(alignProp.getProperty("DEFAULT"));
            ranges=new Vector();
            Changes=new Hashtable();
            ChangeMap=new Hashtable();
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
                {
                    FactionRange R=new FactionRange(words);
                    ranges.add(R);
                }
                if(key.startsWith("CHANGE"))
                {
                    FactionChangeEvent C=new FactionChangeEvent(words);
                    Changes.put(key.toUpperCase(),C);
                    ChangeMap.put(C.ID.toUpperCase(),C);
                }
                if(key.startsWith("FACTOR"))
                {
                    Vector factor=Util.parseSemicolons(words,true);
                    factors.add(factor);
                }
                if(key.startsWith("RELATION"))
                {
                    Vector v=Util.parseSemicolons(words,true);
                    String who=(String)v.elementAt(0);
                    double factor;
                    String amt=((String)v.elementAt(1)).trim();
                    if(amt.endsWith("%"))
                        factor=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
                    else
                        factor=1;
                    relations.put(who,new Double(factor));
                }
                if(key.startsWith("ABILITY"))
                {
                    FactionAbilityUsage A=new FactionAbilityUsage(words);
                    abilityUsages.add(A);
                }
            }
        }
        catch (IOException ex) {
        }
    }

    public FactionChangeEvent findChangeEvent(String key) 
    {
        if(ChangeMap.containsKey(key.toUpperCase())) 
        {
            String k=(String)ChangeMap.get(key.toUpperCase());
            return (FactionChangeEvent)Changes.get(k);
        }
        if(Changes.containsKey(key)) 
        {
            return (FactionChangeEvent)Changes.get(key.toUpperCase());
        }
        return null;
    }

    public FactionChangeEvent findChangeEvent(Ability key) 
    {
        // Direct ability ID's
        if(ChangeMap.containsKey(key.ID().toUpperCase()))
            return (FactionChangeEvent)ChangeMap.get(key.ID().toUpperCase());
        if(Changes.containsKey(key.ID()))
            return (FactionChangeEvent)Changes.get(key.ID().toUpperCase());
        // By TYPE or FLAGS
        for (Enumeration e=ChangeMap.keys();e.hasMoreElements();) 
        {
            FactionChangeEvent C = (FactionChangeEvent)ChangeMap.get(e.nextElement());
            String types[]=Ability.TYPE_DESCS;
            for(int i=0;i< types.length;i++) 
                if(types[i].equalsIgnoreCase(C.ID))
                    if((key.classificationCode()&Ability.ALL_CODES)==i) 
                        return C;
            String flags[]=Ability.FLAG_DESCS;
            for(int i=0;i< flags.length;i++)
                if(flags[i].equalsIgnoreCase(C.ID))
                    if(Util.bset(key.flags(),i) ) 
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

    public int asPercent(int faction) 
    {
        return Util.mul((int)Math.round(Util.div(faction+(0-minimum),(maximum-minimum))),100);
    }

    public int asPercentFromAvg(int faction) 
    {
        // =(( (B2+A2) / 2 ) - C2) / (B2-A2) * 100
        // C = current, A = min, B = Max
        return Util.mul((int)Math.round(Util.div(((maximum+minimum)/2)-faction,maximum-minimum)),100);
    }

    public int randomFaction() 
    {
        Random gen = new Random();
        return maximum - gen.nextInt(maximum-minimum);
    }

    public void setupDefaults(String s) 
    {
        if(defaults==null)
        {
            defaults =Util.parseSemicolons(s,true);
        }
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
                            return Integer.parseInt((String)v.elementAt(j));
                    }
                }
                else
                {
                    if(Util.isNumber((String)defaults.elementAt(i)))
                         return Integer.parseInt((String)defaults.elementAt(i));
                }
            }
        }
        return 0;
    }

    public boolean hasFaction(MOB mob, String ID) 
    {
        return (mob.fetchFaction(ID)!=Integer.MAX_VALUE);
    }

    public boolean hasUsage(Ability A) 
    {
        for(int i=0;i<abilityUsages.size();i++) 
        {
            FactionAbilityUsage usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
            if(A.classificationCode()!=usage.type) continue;
            if(!Util.bset(A.flags(),usage.flag)) continue;
            return true;
        }
        return false;
    }

    public boolean canUse(MOB mob, Ability A) 
    {
        for(int i=0;i<abilityUsages.size();i++) 
        {
            FactionAbilityUsage usage=(FactionAbilityUsage)abilityUsages.elementAt(i);
            if(A.classificationCode()!=usage.type) continue;
            if(!Util.bset(A.flags(),usage.flag)) continue;
            int faction=mob.fetchFaction(ID);
            if((faction >= usage.low) && (faction <= usage.high)) return true;
        }
        return false;
    }

    public double findFactor(MOB mob, boolean gain) 
    {
        for(int i=0;i<factors.size();i++)
        {
            Vector factor=(Vector)factors.elementAt(i);
            if((((String)factor.elementAt(2)).length()>0)
            &&(MUDZapper.zapperCheck(((String)factor.elementAt(2)),mob))) 
            {
                 if(gain)
                     return new Double(((String)factor.elementAt(2))).doubleValue();
                 else
                     return new Double(((String)factor.elementAt(2))).doubleValue();
             }
        }
        return 1.0;
    }

    public void executeMsg(Environmental myHost, CMMsg msg) 
    {
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)    // A death occured
        &&(msg.tool() instanceof MOB)           // a mob was responsible
        &&(myHost==msg.source())    // The mob that died is this one
        &&(msg.tool()!=msg.source()))
        {
            FactionChangeEvent C=findChangeEvent("MURDER");
            if(C!=null) executeChange(msg.source(),(MOB)msg.tool(),C);
        }

        // Ability Watching
        if((msg.tool()!=null)
        &&(msg.tool() instanceof Ability)
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
        &&experience)
        {
            MOB killer=msg.source();
            MOB vic=(MOB)msg.target();

            if(!(vic.fetchFaction(ID)==Integer.MAX_VALUE))
                msg.setValue( (int)Math.round((msg.value()/2.0) +( (msg.value()/2.0) * Util.div(Math.abs(vic.fetchFaction(ID) - killer.fetchFaction(ID)),(maximum - minimum)))));
        }
        return true;
    }

    public void executeChange(MOB source, MOB target, FactionChangeEvent event) 
    {
        int targetFaction = 0;
        if(target!=null)
            targetFaction=target.fetchFaction(ID);
        else 
        {
            target = source;
            targetFaction = source.fetchFaction(ID) * -1;
        }

            double theAmount=new Integer(100).doubleValue();
            int levelLimit=CommonStrings.getIntVar(CommonStrings.SYSTEMI_EXPRATE);
            int levelDiff=target.envStats().level()-source.envStats().level();

            if(levelDiff<(-levelLimit) )
                theAmount=0.0;
            else
            if(levelLimit>0)
            {
                double levelFactor=Util.div(levelDiff,levelLimit);
                if(levelFactor>new Integer(levelLimit).doubleValue())
                    levelFactor=new Integer(levelLimit).doubleValue();
                theAmount=theAmount+Util.mul(levelFactor,100);
            }

            int baseExp=(int)Math.round(theAmount);
            int factionAdj=1;
            int changeDir=0;
            switch(event.direction) 
            {
            case FactionChangeEvent.FACTION_UP:
                changeDir=1;  break;
            case FactionChangeEvent.FACTION_DOWN:
                changeDir=-1; break;
            case FactionChangeEvent.FACTION_OPPOSITE:
                changeDir= -1 * targetFaction / Math.abs(target.fetchFaction(ID)); 
            	break;
            }
            // Pardon the completely random seeming 1.42 and 150.
            // They're the result of making graphs of scenarios and massaging the formula, nothing more or less.
            if(hasFaction(target,ID))
              factionAdj=(int)Math.round((Util.mul(changeDir,(Util.mul(((( maximum + minimum )/ 2) - Math.abs(targetFaction)),( rateModifier + ( Util.mul(Util.div(Math.pow(baseExp,1.42),150),rateModifier))))))));
            else
                if(event.targetIndifference)
                    factionAdj=(int)Math.round((Util.mul(changeDir,(Util.mul(((( maximum + minimum )/ 2) - Math.abs(findDefault(target))),( rateModifier + ( Util.mul(Util.div(Math.pow(baseExp,1.42),150),rateModifier))))))));
            factionAdj*=event.factor;
            if(event.direction==FactionChangeEvent.FACTION_MAXIMUM)
                factionAdj=maximum-source.fetchFaction(ID);
            if(event.direction==FactionChangeEvent.FACTION_MINIMUM)
                factionAdj=minimum-source.fetchFaction(ID);

            factionAdj=(int)Math.round(Util.mul(factionAdj,findFactor(source,(factionAdj>=0))));

            FullMsg FacMsg=new FullMsg(source,target,null,CMMsg.MASK_GENERAL|CMMsg.TYP_FACTIONCHANGE,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,ID);
            FacMsg.setValue(factionAdj);
            if(source.location()!=null)
            {
                if(source.location().okMessage(source,FacMsg))
                {
                    source.location().send(source, FacMsg);
                    factionAdj=FacMsg.value();
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
            else
                source.adjustFaction(ID,factionAdj);
    }

	 public String usageFactors(Ability A) 
	 { 
         StringBuffer ranges=new StringBuffer(); 
         for(int i=0;i<abilityUsages.size();i++) 
         { 
              FactionAbilityUsage usage=(FactionAbilityUsage)abilityUsages.elementAt(i); 
              if(A.classificationCode()!=usage.type) continue; 
              if(!Util.bset(A.flags(),usage.flag)) continue; 
              for(int k=usage.low;k<=usage.high;k++) 
              { 
                   FactionRange R=fetchRange(k); 
                   if(ranges.indexOf(R.Name)>0) continue; 
                   if(ranges.length()>0) ranges.append(", "); 
                   ranges.append(R.Name); 
              } 
         } 
         return ranges.toString(); 
    }
	 
    public static class FactionChangeEvent 
    {
        public String ID;
        public int direction;
        public double factor;
        public String zapper;
        public boolean targetIndifference;

        public static final int FACTION_UP = 0;
        public static final int FACTION_DOWN = 1;
        public static final int FACTION_OPPOSITE = 2;
        public static final int FACTION_MINIMUM = 3;
        public static final int FACTION_MAXIMUM = 4;

        public String toString() {
            return "FactionChangeEvent Event '"+ID+"': ["+direction+"] ["+factor+"] ["+zapper+"] ["+targetIndifference+"]";
        }

        public FactionChangeEvent(String key) 
        {
            Vector v = Util.parseSemicolons(key,true);
            ID=(String)v.elementAt(0);
            String d=(String)v.elementAt(1);
            if(d.startsWith("U")) {
                direction = FACTION_UP;
            }
            if(d.startsWith("D")) {
                direction = FACTION_DOWN;
            }
            if(d.startsWith("U")) {
                direction = FACTION_OPPOSITE;
            }
            if(d.startsWith("MIN")) {
                direction = FACTION_MINIMUM;
            }
            if(d.startsWith("MAX")) {
                direction = FACTION_MAXIMUM;
            }
            String amt=((String)v.elementAt(2)).trim();
            if(amt.endsWith("%"))
              factor=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
            else
              factor=1;
            if((v.size()>3)&&(((String)v.elementAt(3)).equalsIgnoreCase("ANY"))) targetIndifference = true;
            if(v.size()>4)
                zapper = (String)v.elementAt(4);
        }

        public boolean applies(MOB mob) 
        {
            if(zapper==null) return true;
            return MUDZapper.zapperCheck(zapper,mob);
        }
    }

    public static class FactionRange 
    {
        public String ID;
        public int low;
        public int high;
        public String Name;
        public String zap;
        public int AlignEquiv;

        public FactionRange(String key) 
        {
            Vector v = Util.parseSemicolons(key,true);
            Name = (String) v.elementAt(2);
            low = new Integer( (String) v.elementAt(0)).intValue();
            high = new Integer( (String) v.elementAt(1)).intValue();
            zap = (String) v.elementAt(3);
            if(v.size()>4) {
                if(((String)v.elementAt(4)).equalsIgnoreCase("GOOD")) 
                    AlignEquiv = Faction.ALIGN_GOOD;
                else 
                if(((String)v.elementAt(4)).equalsIgnoreCase("NEUTRAL")) 
                    AlignEquiv = Faction.ALIGN_NEUTRAL;
                else 
                if(((String)v.elementAt(4)).equalsIgnoreCase("EVIL")) 
                    AlignEquiv = Faction.ALIGN_EVIL;
                else 
                    AlignEquiv = Faction.ALIGN_INDIFF;
            }
            else
                AlignEquiv = Faction.ALIGN_INDIFF;
        }

        public int random() 
        {
            Random gen = new Random();
            return high - gen.nextInt(high-low);
        }
    }

    public static class FactionAbilityUsage 
    {
        public String ID;
        public int type;
        public int flag;
        public int low;
        public int high;

        public FactionAbilityUsage(String key) 
        {
            Vector v = Util.parseSemicolons(key,false);
            ID=(String)v.elementAt(0);
            for(int i=0;i<Ability.TYPE_DESCS.length;i++) 
            {
                if((String)v.elementAt(1)==Ability.TYPE_DESCS[i]) 
                {
                    type=i;
                    break;
                }
            }
            for(int i=0;i<Ability.FLAG_DESCS.length;i++) 
            {
                if((String)v.elementAt(1)==Ability.FLAG_DESCS[i]) 
                {
                    flag=i;
                    break;
                }
            }
            low = new Integer( (String) v.elementAt(2)).intValue();
            high = new Integer( (String) v.elementAt(3)).intValue();
        }
    }
}
