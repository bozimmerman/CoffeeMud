package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Addictions extends StdAbility
{
    public String ID() { return "Addictions"; }
    public String name(){ return "Addictions";}
    private long lastFix=System.currentTimeMillis();
    public String displayText(){ return craving()?"(Addiction to "+text()+")":"";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
    public int classificationCode(){return Ability.ACODE_PROPERTY;}
    public boolean isAutoInvoked(){return true;}
    public boolean canBeUninvoked(){return false;}
    private Item puffCredit=null;
    private final static long CRAVE_TIME=TimeManager.MILI_HOUR;
    private final static long WITHDRAW_TIME=TimeManager.MILI_DAY;
    
    private boolean craving(){return (System.currentTimeMillis()-lastFix)>CRAVE_TIME;}
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        
        if((craving())
        &&(CMLib.dice().rollPercentage()<=((System.currentTimeMillis()-lastFix)/TimeManager.MILI_HOUR))
        &&(ticking instanceof MOB))
        {
            if((System.currentTimeMillis()-lastFix)>WITHDRAW_TIME)
            {
                ((MOB)ticking).tell("You've managed to kick your addiction.");
                canBeUninvoked=true;
                unInvoke();
                ((MOB)ticking).delEffect(this);
                return false;
            }
            if((puffCredit!=null)
            &&(puffCredit.amDestroyed()
                ||puffCredit.amWearingAt(Wearable.IN_INVENTORY)
                ||puffCredit.owner()!=(MOB)affected))
                puffCredit=null;
            switch(CMLib.dice().roll(1,7,0))
            {
            case 1: ((MOB)ticking).tell("Man, you could sure use some "+text()+"."); break;
            case 2: ((MOB)ticking).tell("Wouldn't some "+text()+" be great right about now?"); break;
            case 3: ((MOB)ticking).tell("You are seriously craving "+text()+"."); break;
            case 4: ((MOB)ticking).tell("There's got to be some "+text()+" around here somewhere."); break;
            case 5: ((MOB)ticking).tell("You REALLY want some "+text()+"."); break;
            case 6: ((MOB)ticking).tell("You NEED some "+text()+", NOW!"); break;
            case 7: ((MOB)ticking).tell("Some "+text()+" would be lovely."); break;
            }
            
        }
        return true;
    }
    
    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if((affected!=null)&&(affected instanceof MOB))
        {
            if((msg.source()==affected)
            &&(msg.targetMinor()==CMMsg.TYP_WEAR)
            &&(msg.target() instanceof Light)
            &&(msg.target() instanceof Container)
            &&(CMath.bset(((Item)msg.target()).rawProperLocationBitmap(),Wearable.WORN_MOUTH))
            &&(((Container)msg.target()).getContents().size()>0)
            &&(CMLib.english().containsString(((Environmental)((Container)msg.target()).getContents().firstElement()).Name(),text())))
                puffCredit=(Item)msg.target();
        }
        return true;
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        if((affected!=null)&&(affected instanceof MOB))
        {
            if(msg.source()==affected)
            {
                if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
                &&((msg.target() instanceof Food)||(msg.target() instanceof Drink))
                &&(msg.target() instanceof Item)
                &&(CMLib.english().containsString(msg.target().Name(),text())))
                    lastFix=System.currentTimeMillis();
                if((msg.amISource((MOB)affected))
                &&(msg.targetMinor()==CMMsg.TYP_HANDS)
                &&(msg.target() instanceof Light)
                &&(msg.tool() instanceof Light)
                &&(msg.target()==msg.tool())
                &&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
                &&(((Light)msg.target()).isLit())
                &&((puffCredit!=null)||CMLib.english().containsString(msg.target().Name(),text())))
                    lastFix=System.currentTimeMillis();
            }
        }
        super.executeMsg(myHost,msg);
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        Environmental target=givenTarget;
        
        if(target==null) return false;
        if(target.fetchEffect(ID())!=null) return false;
        
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;
        boolean success=proficiencyCheck(mob,0,auto);
        if(success)
        {
            String addiction=target.Name().toUpperCase();
            if(addiction.toUpperCase().startsWith("A POUND OF "))
                addiction=addiction.substring(11);
            if(addiction.toUpperCase().startsWith("A "))
                addiction=addiction.substring(2);
            if(addiction.toUpperCase().startsWith("AN "))
                addiction=addiction.substring(3);
            if(addiction.toUpperCase().startsWith("SOME "))
                addiction=addiction.substring(5);
            CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
            if(mob.location()!=null)
            {
                if(mob.location().okMessage(mob,msg))
                {
                    mob.location().send(mob,msg);
                    Ability A=(Ability)copyOf();
                    A.setMiscText(addiction.trim());
                    mob.addNonUninvokableEffect(A);
                }
            }
            else
            {
                Ability A=(Ability)copyOf();
                A.setMiscText(addiction.trim());
                mob.addNonUninvokableEffect(A);
            }
        }
        return success;
    }
}