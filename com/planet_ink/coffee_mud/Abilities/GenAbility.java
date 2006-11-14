package com.planet_ink.coffee_mud.Abilities;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2000-2006 Bo Zimmerman

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

public class GenAbility extends StdAbility
{
    // data should be stored in a common instance object .. something common to all genability of same id, 
    // but diff to others.n  I'm thinking like a DVector, and just have 
    private String ID="GenAbility";
    public String ID() { return ID; }
    private static final Hashtable vars=new Hashtable();
    private static final int V_NAME=0;//S
    private static final int V_DISP=1;//S
    private static final int V_TRIG=2;//S[]
    private static final int V_MAXR=3;//I
    private static final int V_MINR=4;//I
    private static final int V_AUTO=5;//B
    private static final int V_FLAG=6;//I
    private static final int V_CLAS=7;//I
    private static final int V_OMAN=8;//I
    private static final int V_USAG=9;//I
    private static final int V_CAFF=10;//I
    private static final int V_CTAR=11;//I
    private static final int V_QUAL=12;//I
    private static final int V_HERE=13;//A
    private static final int V_CMSK=14;//S
    private static final int V_SCRP=15;//S
    private static final int NUM_VS=16;//S
    private static final Object[] makeEmpty()
    {
        Object[] O=new Object[NUM_VS];
        O[V_NAME]="an ability";
        O[V_DISP]="";
        O[V_TRIG]=new String[]{"CAST","CA","C"};
        O[V_MAXR]=new Integer(0);
        O[V_MINR]=new Integer(0);
        O[V_AUTO]=new Boolean(false);
        O[V_FLAG]=new Integer(0);
        O[V_CLAS]=new Integer(Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION);
        O[V_OMAN]=new Integer(-1);
        O[V_USAG]=new Integer(Ability.USAGE_MANA);
        O[V_CAFF]=new Integer(Ability.CAN_MOBS);
        O[V_CTAR]=new Integer(Ability.CAN_MOBS);
        O[V_QUAL]=new Integer(Ability.QUALITY_BENEFICIAL_OTHERS);
        O[V_HERE]=CMClass.getAbility("Prop_HereAdjuster");
        O[V_SCRP]="";
        O[V_CMSK]="";
        return O;
    }
    private static final Object V(String ID, int varNum)
    {
        if(vars.containsKey(ID)) return ((Object[])vars.get(ID))[varNum];
        Object[] O=makeEmpty();
        vars.put(ID,O);
        return O[varNum];
    }
    private Behavior scriptObj=null;
    private long scriptParmHash=0;
    public Behavior getScriptable(){
        if(((String)V(ID,V_SCRP)).hashCode()!=scriptParmHash)
        {
            String parm=(String)V(ID,V_SCRP);
            scriptParmHash=parm.hashCode();
            if(parm.trim().length()==0) 
                scriptObj=null;
            else
            {
                scriptObj=CMClass.getBehavior("Scriptable");
                scriptObj.setParms(parm);
            }
        }
        return scriptObj;
    }
    
    
    public String Name(){return name();}
    public String name(){ return (String)V(ID,V_NAME);}
    public String description(){return "&";}
    public String displayText(){return (String)V(ID,V_DISP);}
    public String[] triggerStrings(){return (String[])V(ID,V_TRIG);}
    public int maxRange(){return adjustedMaxInvokerRange(((Integer)V(ID,V_MAXR)).intValue());}
    public int minRange(){return ((Integer)V(ID,V_MINR)).intValue();}
    public boolean isAutoInvoked(){return ((Boolean)V(ID,V_AUTO)).booleanValue();}
    public long flags(){return ((Integer)V(ID,V_FLAG)).intValue();}
    public int usageType(){return ((Integer)V(ID,V_USAG)).intValue();}
    protected int overrideMana(){return ((Integer)V(ID,V_OMAN)).intValue();} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100
    public int classificationCode(){ return ((Integer)V(ID,V_CLAS)).intValue(); }
    protected int canAffectCode(){return ((Integer)V(ID,V_CAFF)).intValue(); }
    protected int canTargetCode(){return ((Integer)V(ID,V_CTAR)).intValue(); }
    public int abstractQuality(){return ((Integer)V(ID,V_QUAL)).intValue();}
    
    
    public CMObject newInstance()
    {
        try
        {
            GenAbility A=(GenAbility)this.getClass().newInstance();
            A.ID=ID;
            A.scriptParmHash=scriptParmHash;
            if(scriptObj!=null){ 
                A.scriptObj=CMClass.getBehavior("Scriptable"); 
                A.scriptObj.setParms(scriptObj.getParms());
            }
            else
                A.scriptObj=null;
        }
        catch(Exception e)
        {
            Log.errOut(ID(),e);
        }
        return new GenAbility();
    }
    
    protected void cloneFix(Ability E)
    {
        if(E instanceof GenAbility)
        {
            GenAbility A=(GenAbility)E;
            A.scriptParmHash=scriptParmHash;
            if(A.scriptObj!=null){ 
                scriptObj=CMClass.getBehavior("Scriptable"); 
                scriptObj.setParms(A.scriptObj.getParms());
            }
            else
                scriptObj=null;
        }
    }
    
    public boolean isGeneric(){return true;}
    
    public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
    {
        // dont forget to allow super. calls to Spell.invoke, Chant.invoke, etc.. based on classification?
        return super.invoke(mob,commands,target,auto,asLevel);
    }
    
    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        return true;
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        Behavior B=getScriptable();
        if(B!=null)
            B.executeMsg(myHost,msg);
        return;
    }

    public void affectEnvStats(Environmental affectedEnv, EnvStats affectableStats)
    {
        if((Ability)V(ID,V_HERE)!=null)
            ((Ability)V(ID,V_HERE)).affectEnvStats(affectedEnv,affectableStats);
    }
    public void affectCharStats(MOB affectedMob, CharStats affectableStats)
    {
        if(((Ability)V(ID,V_HERE))!=null)
            ((Ability)V(ID,V_HERE)).affectCharStats(affectedMob,affectableStats);
        
    }
    public void affectCharState(MOB affectedMob, CharState affectableMaxState)
    {
        if(((Ability)V(ID,V_HERE))!=null)
            ((Ability)V(ID,V_HERE)).affectCharState(affectedMob,affectableMaxState);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        Behavior B=getScriptable();
        if(B!=null)
            if(!B.okMessage(myHost,msg))
                return false;
        return true;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if((unInvoked)&&(canBeUninvoked()))
            return false;
        if(!super.tick(ticking,tickID))
            return false;
        Behavior B=getScriptable();
        if(B!=null)
            if(!B.tick(ticking,tickID))
                return false;
        return true;
    }
    
    // lots of work to be done here
    public int getSaveStatIndex(){return getStatCodes().length;}
    private static final String[] CODES={"CLASS",//0
                                         "TEXT",//1
                                         "NAME",//2
                                         "DISPLAY",//3
                                         "TRIGSTR",//4
                                         "MAXRANGE",//5
                                         "MINRANGE",//6
                                         "AUTOINVOKE",//7
                                         "FLAGS",//8
                                         "CLASSIFICATION",//9
                                         "OVERRIDEMANA",//10
                                         "USAGEMASK",//11
                                         "CANAFFECTMASK",//12
                                         "CANTARGETMASK",//13
                                         "QUALITY",//14
                                         "HERESTATS",//15
                                         "CASTMASK",//16
                                         "SCRIPT",//17
                                        };
    public String[] getStatCodes(){return CODES;}
    protected int getCodeNum(String code){
        for(int i=0;i<CODES.length;i++)
            if(code.equalsIgnoreCase(CODES[i])) return i;
        return -1;
    }
    public String getStat(String code){
        switch(getCodeNum(code))
        {
        case 0: return ID();
        case 1: return text();
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        switch(getCodeNum(code))
        {
        case 0: return;
        case 1: setMiscText(val); break;
        }
    }
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof GenAbility)) return false;
        if(!((GenAbility)E).ID().equals(ID)) return false;
        if(!((GenAbility)E).text().equals(text())) return false;
        return true;
    }
}
