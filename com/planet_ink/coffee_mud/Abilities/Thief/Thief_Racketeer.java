package com.planet_ink.coffee_mud.Abilities.Thief;

import java.util.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Items.interfaces.Coins;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
 * Copyright 2000-2010 Bo Zimmerman, Lee Fox Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
@SuppressWarnings("unchecked")
public class Thief_Racketeer extends ThiefSkill
{
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;}
    public String ID()
    {
        return "Thief_Racketeer";
    }

    public String name()
    {
        return "Racketeer";
    }

    public String displayText()
    {
        return "";
    }

    protected int canAffectCode()
    {
        return CAN_MOBS;
    }

    protected int canTargetCode()
    {
        return CAN_MOBS;
    }

    public int abstractQuality()
    {
        return Ability.QUALITY_MALICIOUS;
    }
    private static final String[] triggerStrings={"RACKETEER"};

    public String[] triggerStrings()
    {
        return triggerStrings;
    }

    protected boolean disregardsArmorCheck(MOB mob)
    {
        return true;
    }
    public Vector mobs=new Vector();

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if(!super.okMessage(myHost,msg))
        {
            return false;
        }
        MOB source=msg.source();
        if((!msg.source().Name().equals(text()))&&((msg.source().getClanID().length()==0)||(!msg.source().getClanID().equals(text())))
                &&(msg.tool() instanceof Ability)&&(msg.target()==affected)
                &&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_THIEF_SKILL))
        {
            if(invoker()==source)
            {
                source.tell(msg.target().name()+" is currently under your protection.");
            }else
            {
                source.tell(msg.target().name()+" is under "+invoker().name()+"'s protection.");
                invoker().tell("Word on the street is that "+source.name()+" is hassling "+msg.target().name()+" who is under your protection.");
            }
            return false;
        }
        return true;
    }

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(target != null)
            {
                if((CMLib.coffeeShops().getShopKeeper(target)==null)&&(target.fetchBehavior("MoneyChanger")==null)
                &&(target.fetchBehavior("ItemMender")==null)&&(target.fetchBehavior("ItemIdentifier")==null)
                &&(target.fetchBehavior("ItemRefitter")==null))
                    return Ability.QUALITY_INDIFFERENT;
                if(target.fetchEffect(ID())!=null)
                    return Ability.QUALITY_INDIFFERENT;
                if((target instanceof MOB)&&(!((MOB)target).mayIFight(mob)))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if((commands.size()<1)&&(givenTarget==null))
        {
            mob.tell("Get protection money from whom?");
            return false;
        }
        MOB target=null;
        if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
            target=(MOB)givenTarget;
        else
            target=mob.location().fetchInhabitant(CMParms.combine(commands,0));
        if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
        {
            mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
            return false;
        }
        if(mob.isInCombat())
        {
            mob.tell("You are too busy to racketeer right now.");
            return false;
        }
        if((CMLib.coffeeShops().getShopKeeper(target)==null)&&(target.fetchBehavior("MoneyChanger")==null)
                &&(target.fetchBehavior("ItemMender")==null)&&(target.fetchBehavior("ItemIdentifier")==null)
                &&(target.fetchBehavior("ItemRefitter")==null))
        {
            mob.tell("You can't get protection money from "+target.name()+".");
            return false;
        }
        Ability A=target.fetchEffect(ID());
        if(A!=null)
        {
            if(A.invoker()==mob)
                mob.tell(target.name()+" has already been extracted from today.");
            else
            {
                mob.tell(target.name()+" is already under "+A.invoker().name()+"'s protection.");
                A.invoker().tell("Word on the street is that "+mob.name()+" is trying to push into your business with "
                                    +target.name()+".");
            }
            return false;
        }
        int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
        if(!target.mayIFight(mob))
        {
            mob.tell("You cannot racketeer "+target.charStats().himher()+".");
            return false;
        }
        if(!super.invoke(mob,commands,givenTarget,auto,asLevel)) return false;
        double amount=(double)CMLib.dice().roll(proficiency(),target.envStats().level(),0);
        boolean success=proficiencyCheck(mob,-(levelDiff),auto);
        if(success)
        {
            CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT,"<S-NAME> extract(s) "
                    +CMLib.beanCounter().nameCurrencyShort(target,amount)+" of protection money from <T-NAME>.");
            if(mob.location().okMessage(mob,msg))
            {
                mob.location().send(mob,msg);
                beneficialAffect( mob,
                                  target,
                                  asLevel,
                                  (int)((((long)Tickable.TIME_MILIS_PER_MUDHOUR) *
                                         ((long)mob.location().getArea().getTimeObj().getHoursInDay()) *
                                         ((long)mob.location().getArea().getTimeObj().getDaysInMonth()) ) /
                                             ((long)Tickable.TIME_TICK) ));
                Coins C=CMLib.beanCounter().makeBestCurrency(mob,amount);
                if(C!=null)
                {
                    mob.location().addItemRefuse(C,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                    CMLib.commands().postGet(mob,null,C,true);
                }
            }
        }else
            maliciousFizzle(mob,target,"<T-NAME> seem(s) unintimidated by <S-NAME>.");
        return success;
    }
}
