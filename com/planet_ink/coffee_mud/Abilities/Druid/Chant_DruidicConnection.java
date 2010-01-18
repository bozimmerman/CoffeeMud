package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_DruidicConnection extends Chant
{
    public String ID() { return "Chant_DruidicConnection"; }
    public String name(){ return "Druidic Connection";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
    protected int canAffectCode(){return CAN_AREAS;}
    protected int canTargetCode(){return 0;}
    protected long lastTime=System.currentTimeMillis();
    public boolean bubbleAffect(){return (affected instanceof Area);}
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((invoker==null)
        ||(!CMLib.flags().isInTheGame(invoker(),true))
        ||(invoker().location()==null)
        ||((affected instanceof Area)&&(!((Area)affected).inMyMetroArea(invoker().location().getArea()))))
        {
            unInvoke();
            return false;
        }
        long ellapsed=System.currentTimeMillis()-lastTime;
        if(affected instanceof Area)
        {
            int hoursPerDay=((Area)affected).getTimeObj().getHoursInDay();
            long millisPerHoursPerDay=hoursPerDay*Tickable.TIME_MILIS_PER_MUDHOUR;
            if(ellapsed>=millisPerHoursPerDay)
            {
                lastTime=System.currentTimeMillis();
                Vector V=Druid_MyPlants.myAreaPlantRooms(invoker(),(Area)affected);
                int pct=0;
                if(((Area)affected).getAreaIStats()[Area.AREASTAT_VISITABLEROOMS]>10)
                    pct=(int)Math.round(100.0*CMath.div(V.size(),((Area)affected).getAreaIStats()[Area.AREASTAT_VISITABLEROOMS]));
                if(pct<50)
                {
                    unInvoke();
                    return false;
                }
                invoker.tell("Your prolonged connection to this place fills you with harmony!");
                int xp=(int)Math.round(5.0*CMath.mul(CMath.div(V.size(),((Area)affected).getAreaIStats()[Area.AREASTAT_VISITABLEROOMS])
                                            ,((Area)affected).getAreaIStats()[Area.AREASTAT_AVGLEVEL]));
                CMLib.leveler().postExperience(invoker(),null,null,xp,false);
            }
        }
        return true;
    }
    
    public void affectEnvStats(Environmental affected, EnvStats stats)
    {
        super.affectEnvStats(affected,stats);
        if(affected instanceof MOB)
        {
            MOB mob=(MOB)affected;
            if((mob==invoker())
            ||CMLib.flags().isAnimalIntelligence(mob)
            ||(mob.charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation")))
            {
                stats.setAttackAdjustment((int)Math.round(CMath.mul(1.1+(0.05*super.getXLEVELLevel(invoker())),stats.attackAdjustment())));
                stats.setArmor(stats.armor()-(2*stats.level())-(2*getXLEVELLevel(invoker())));
            }
        }
    }
    
    public void unInvoke()
    {
        if((canBeUninvoked())&&(invoker!=null)&&(affected instanceof Area))
        {
            Vector V=Druid_MyPlants.myAreaPlantRooms(invoker,(Area)affected);
            if(V.size()>1)
                V.removeElementAt(0);
            for(int v=0;v<V.size();v++)
            {
                Item I=Druid_MyPlants.myPlant((Room)V.elementAt(v),invoker,0);
                int num=0;
                while(I!=null) 
                	I=Druid_MyPlants.myPlant((Room)V.elementAt(v),invoker,++num);
                for(int x=num-1;x>=0;x--)
                {
                    I=Druid_MyPlants.myPlant((Room)V.elementAt(v),invoker,x);
                    if(I!=null)
	                    I.destroy();
                }
            }
            invoker.tell("You have destroyed your connection with "+affected.name()+"!");
            for(Enumeration e=((Area)affected).getMetroMap();e.hasMoreElements();)
                ((Room)e.nextElement()).recoverRoomStats();
        }
        super.unInvoke();
    }
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if((mob.location()==null)) return false;
        Area target=mob.location().getArea();
        if((auto)&&(givenTarget instanceof Area)) target=(Area)givenTarget;
        if(target==null) return false;
        boolean quietly=((commands!=null)&&(commands.size()>0)&&(commands.contains("QUIETLY")));
        if(target.fetchEffect(ID())!=null)
        {
            if(!quietly)
                mob.tell("This place is already connected to a druid.");
            return false;
        }
        Vector V=Druid_MyPlants.myAreaPlantRooms(mob,target);
        int pct=0;
        if(target.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS]>10)
            pct=(int)Math.round(100.0*CMath.div(V.size(),target.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS]));
        if(pct<50)
        {
            if(!quietly)
                mob.tell("You'll need to summon more of your special plant-life here to develop the connection.");
            return false;
        }
        if((!auto)&&(!mob.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Druid")))
        {
            if(!quietly)
                mob.tell("Only druids can make this connection.");
            return false;
        }

        if(CMLib.law().isACity(target)
        &&(!auto))
        {
            if(!quietly)
                mob.tell("This chant does not work here.");
            return false;
        }
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
            return false;

        boolean success=proficiencyCheck(mob,0,auto);

        if(success)
        {
            // it worked, so build a copy of this ability,
            // and add it to the affects list of the
            // affected MOB.  Then tell everyone else
            // what happened.
            CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"This area seems connected to <S-NAME>.":"^S<S-NAME> chant(s), establishing a natural connection with this area.^?");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                beneficialAffect(mob,target,asLevel,0);
                Chant_DruidicConnection A=(Chant_DruidicConnection)target.fetchEffect(ID());
                if(A!=null)
                {
                    A.setSavable(false);
                    A.makeLongLasting();
                    A.lastTime=System.currentTimeMillis();
                    for(Enumeration e=target.getMetroMap();e.hasMoreElements();)
                        ((Room)e.nextElement()).recoverRoomStats();
                }
            }
            else
                success=false;
        }
        else
            beneficialWordsFizzle(mob,target,"<S-NAME> chant(s), but the magic fades.");

        // return whether it worked
        return success;
    }
}