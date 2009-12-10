package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Thief_Safehouse extends ThiefSkill
{
    public String ID() { return "Thief_Safehouse"; }
    public String name(){ return "Safehouse";}
    public String displayText(){return "(Safehouse)";}
    protected int canAffectCode(){return CAN_ROOMS;}
    protected int canTargetCode(){return 0;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
    private static final String[] triggerStrings = {"SAFEHOUSE"};
    public String[] triggerStrings(){return triggerStrings;}
    public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}

    public boolean okMessage(Environmental host, CMMsg msg)
    {
        if(!super.okMessage(host,msg))
            return false;

        if((msg.target()==affected)
        &&(msg.targetMinor()==CMMsg.TYP_ENTER)
        &&(affected instanceof Room)
        &&(isLaw(msg.source())))
        {
            msg.source().tell("You don't think there's anything going on in there.");
            return false;
        }
        return true;
    }

    public boolean isLawHere(Room R)
    {
        if(R!=null)
        {
            LegalBehavior law=CMLib.law().getLegalBehavior(R);
            if(law!=null)
            {
                Area A=CMLib.law().getLegalObject(R);
                MOB M=null;
                for(int r=0;r<R.numInhabitants();r++)
                {
                    M=R.fetchInhabitant(r);
                    if((M!=null)&&(law.isAnyOfficer(A,M)||law.isJudge(A,M)))
                        return true;
                }
            }
        }
        return false;
    }
    
    public boolean isLaw(MOB mob)
    {
        if(mob==null) return false;
        if(affected instanceof Room)
        {
            LegalBehavior law=CMLib.law().getLegalBehavior((Room)affected);
            if(law!=null)
            {
                Area A=CMLib.law().getLegalObject((Room)affected);
                if(law.isAnyOfficer(A,mob)||law.isJudge(A,mob))
                    return true;
            }
        }
        return false;
    }
    
    public void executeMsg(Environmental host, CMMsg msg)
    {
        super.executeMsg(host,msg);
        if((canBeUninvoked())&&(invoker()!=null)&&(invoker().location()!=affected))
            unInvoke();
    }

    public boolean isGoodSafehouse(Room target)
    {
        if(target==null) return false;
        if((target.domainType()==Room.DOMAIN_INDOORS_WOOD)||(target.domainType()==Room.DOMAIN_INDOORS_STONE))
            for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
            {
                Room R=target.getRoomInDir(d);
                if((R!=null)&&(R.domainType()==Room.DOMAIN_OUTDOORS_CITY))
                    return true;
            }
        return false;
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        Room target=mob.location();
        if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
            target=(Room)givenTarget;
        Ability A=target.fetchEffect(ID());
        if(A!=null)
        {
            mob.tell("This place is already a safehouse.");
            return false;
        }
        if((!auto)&&(CMLib.law().getLegalBehavior(target)==null))
        {
            mob.tell("There is no law here!");
            return false;
        }
        if(!isGoodSafehouse(target))
        {
    		TrackingLibrary.TrackingFlags flags;
    		flags = new TrackingLibrary.TrackingFlags()
    				.add(TrackingLibrary.TrackingFlag.OPENONLY)
    				.add(TrackingLibrary.TrackingFlag.AREAONLY)
    				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
    				.add(TrackingLibrary.TrackingFlag.NOAIR)
    				.add(TrackingLibrary.TrackingFlag.NOWATER);
            Vector V=CMLib.tracking().getRadiantRooms(target,flags,50+(2*getXLEVELLevel(mob)));
            Room R=null;
            int v=0;
            for(;v<V.size();v++)
            {
                R=(Room)V.elementAt(v);
                if((isGoodSafehouse(R))&&(!isLawHere(R)))
                    break;
            }
            mob.tell("A place like this can't be a safehouse.");
            if((isGoodSafehouse(R))&&(!isLawHere(R)))
            {
                V=CMLib.tracking().findBastardTheBestWay(target,CMParms.makeVector(R),flags,50+(2*getXLEVELLevel(mob)));
                StringBuffer trail=new StringBuffer("");
                int dir=CMLib.tracking().trackNextDirectionFromHere(V,target,true);
                while(target!=R)
                {
                    if((dir<0)||(dir>=Directions.NUM_DIRECTIONS())||(target==null)) break;
                    trail.append(Directions.getDirectionName(dir));
                    if(target.getRoomInDir(dir)!=R) trail.append(", ");
                    target=target.getRoomInDir(dir);
                    dir=CMLib.tracking().trackNextDirectionFromHere(V,target,true);
                }
                if(target==R)
                    mob.tell("You happen to know of one nearby though.  Go: "+trail.toString());
            }
            return false;
        }

        boolean success=proficiencyCheck(mob,0,auto);

        CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> hide(s) out from the law here.");
        if(!success)
            return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> attempt(s) hide out from the law here, but things are just too hot.");
        else
        if(mob.location().okMessage(mob,msg))
        {
            mob.location().send(mob,msg);
            beneficialAffect(mob,target,asLevel,(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)));
        }
        return success;
    }
}