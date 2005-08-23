package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Addictions extends StdAbility
{
    public String ID() { return "Addictions"; }
    public String name(){ return "Addictions";}
    private long lastFix=System.currentTimeMillis();
    public String displayText(){ return craving()?"(Addiction to "+text()+")":"";}
    protected int canAffectCode(){return CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    public int quality(){return Ability.OK_SELF;}
    public int classificationCode(){return Ability.PROPERTY;}
    public boolean isAutoInvoked(){return true;}
    public boolean canBeUninvoked(){return false;}
    
    private boolean craving(){return (System.currentTimeMillis()-lastFix)>IQCalendar.MILI_HOUR;}
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((craving())&&(Dice.rollPercentage()<10)&&(ticking instanceof MOB))
        {
            switch(Dice.roll(1,7,0))
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
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        if((affected!=null)&&(affected instanceof MOB))
        {
            if(msg.source()==affected)
            {
                if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
                &&((msg.target() instanceof Food)||(msg.target() instanceof Drink))
                &&(msg.target() instanceof Item)
                &&(EnglishParser.containsString(msg.target().Name(),text())))
                    lastFix=System.currentTimeMillis();
                if((msg.amISource((MOB)affected))
                &&(msg.targetMinor()==CMMsg.TYP_HANDS)
                &&(msg.target() instanceof Light)
                &&(msg.tool() instanceof Light)
                &&(msg.target()==msg.tool())
                &&(((Light)msg.target()).amWearingAt(Item.ON_MOUTH))
                &&(((Light)msg.target()).isLit())
                &&(EnglishParser.containsString(msg.target().Name(),text())))
                    lastFix=System.currentTimeMillis();
            }
        }
        super.executeMsg(myHost,msg);
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        MOB target=getTarget(mob,commands,givenTarget);
        
        if(target==null) return false;
        if(target.fetchEffect(ID())!=null) return false;
        
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;
        boolean success=profficiencyCheck(mob,0,auto);
        if(success)
        {
            String addiction=target.Name();
            if(addiction.toUpperCase().startsWith("A POUND OF "))
                addiction=addiction.substring(11);
            if(addiction.toUpperCase().startsWith("A "))
                addiction=addiction.substring(2);
            if(addiction.toUpperCase().startsWith("AN "))
                addiction=addiction.substring(3);
            if(addiction.toUpperCase().startsWith("SOME "))
                addiction=addiction.substring(5);
            FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
            if(target.location()!=null)
            {
                if(target.location().okMessage(target,msg))
                {
                    target.location().send(target,msg);
                    Ability A=(Ability)copyOf();
                    A.setMiscText(addiction.trim());
                    target.addNonUninvokableEffect(A);
                }
            }
            else
            {
                Ability A=(Ability)copyOf();
                A.setMiscText(addiction.trim());
                target.addNonUninvokableEffect(A);
            }
        }
        return success;
    }
}