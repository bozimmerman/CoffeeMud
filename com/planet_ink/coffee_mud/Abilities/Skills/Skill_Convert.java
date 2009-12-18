package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TimeManager;
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
public class Skill_Convert extends StdSkill
{
	public String ID() { return "Skill_Convert"; }
	public String name(){ return "Convert";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"CONVERT"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_EVANGELISM;}
    protected static DVector convertStack=new DVector(2);
    public int overrideMana(){return 50;}
    public String displayText(){return "";}
    protected String priorFaith="";
    
    public void unInvoke()
    {
        // undo the affects of this spell
        if((affected==null)||(!(affected instanceof MOB)))
            return;
        MOB mob=(MOB)affected;

        super.unInvoke();

        if(canBeUninvoked())
        {
            if(text().length()>0)
                mob.tell("You start to have doubts about "+text()+".");
            mob.setWorshipCharID(priorFaith);
        }
    }
    public boolean tick(Tickable ticking, int tickID)
    {
        if((text().length()>0)&&(affected instanceof MOB)&&(!text().equals(((MOB)affected).getWorshipCharID())))
            ((MOB)affected).setWorshipCharID(text());
        return super.tick(ticking,tickID);
    }
    

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell("You must specify either a deity to convert yourself to, or a player to convert to your religion.");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,null,"I am unable to convert.",false,false);
			return false;
		}

		MOB target=mob;
		Deity D=CMLib.map().getDeity(CMParms.combine(commands,0));
		if(D==null)
		{
			D=mob.getMyDeity();
			target=getTarget(mob,commands,givenTarget,false,true);
			if(target==null)
			{
				mob.tell("You've also never heard of a deity called '"+CMParms.combine(commands,0)+"'.");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"I've never heard of '"+CMParms.combine(commands,0)+"'.",false,false);
				return false;
			}
			if(D==null)
			{
				mob.tell("A faithless one cannot convert "+target.name()+".");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"I am faithless, and can not convert you.",false,false);
				return false;
			}
		}
        if((CMLib.flags().isAnimalIntelligence(target))                
        ||((target.isMonster())&&(target.envStats().level()>mob.envStats().level())))
        {
            mob.tell("You can't convert "+target.name()+".");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,target,"I can not convert you.",false,false);
            return false;
        }
        if(target.getMyDeity()==D)
        {
            mob.tell(target.name()+" already worships "+D.name()+".");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,target,"You already worship "+D.Name()+".",false,false);
            return false;
        }
		if(!auto)
		{
			if(convertStack.contains(target))
			{
				Long L=(Long)convertStack.elementAt(convertStack.indexOf(target),2);
				if((System.currentTimeMillis()-L.longValue())>Tickable.TIME_MILIS_PER_MUDHOUR*5)
					convertStack.removeElement(target);
			}
			if(convertStack.contains(target))
			{
				mob.tell(target.name()+" must wait to be converted again.");
                if(mob.isMonster())
                    CMLib.commands().postSay(mob,target,"You must wait to be converted again.",false,false);
				return false;
			}
		}

        boolean success=proficiencyCheck(mob,0,auto);
        boolean targetMadeSave=CMLib.dice().roll(1,100,0)>(target.charStats().getSave(CharStats.STAT_FAITH));
        if(CMSecurity.isASysOp(mob)) targetMadeSave=false;
        if((!target.isMonster())&&(success)&&(targetMadeSave)&&(target.getMyDeity()!=null))
        {
            mob.tell(target.name()+" is worshipping "+target.getMyDeity().name()+".  "+target.charStats().HeShe()+" must REBUKE "+target.getMyDeity().charStats().himher()+" first.");
            if(mob.isMonster())
                CMLib.commands().postSay(mob,target,"You already worship "+target.getMyDeity().Name()+".",false,false);
            return false;
        }
        if((success)&&(targetMadeSave)&&(!target.isMonster())&&(target!=mob))
        {
            try
            {
                if(!target.session().confirm("\n\r"+mob.displayName(target)+" is trying to convert you to the worship of "+D.name()+".  Is this what you want (N/y)?","N"))
                {
                    mob.location().show(mob,target,CMMsg.MSG_SPEAK,"<S-YOUPOSS> attempt to convert <T-NAME> to the worship of "+D.name()+" is rejected.");
                    return false;
                }
                targetMadeSave=!success;
            }
            catch(Exception e)
            {
                return false;
            }
        }
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((success)&&((!targetMadeSave)||(target==mob)))
		{
			Room dRoom=D.location();
			if(dRoom==mob.location()) dRoom=null;
            if(target.getMyDeity()!=null)
            {
                Ability A=target.fetchEffect(ID());
                if(A!=null){ A.unInvoke(); target.delEffect(A);} 
                CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_REBUKE,null);
                if((mob.location().okMessage(mob,msg2))&&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
                {
                    mob.location().send(target,msg2);
                    if(dRoom!=null) dRoom.send(target,msg2);
                }
            }
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,auto?"<T-NAME> <T-IS-ARE> converted!":"<S-NAME> convert(s) <T-NAMESELF> to the worship of "+D.name()+".");
			CMMsg msg2=CMClass.getMsg(target,D,this,CMMsg.MSG_SERVE,null);
			if((mob.location().okMessage(mob,msg))
			   &&(mob.location().okMessage(mob,msg2))
			   &&((dRoom==null)||(dRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(target,msg2);
				if(dRoom!=null)
					dRoom.send(target,msg2);
				convertStack.addElement(target,Long.valueOf(System.currentTimeMillis()));
				if(mob!=target)
                    if(target.isMonster())
    					CMLib.leveler().postExperience(mob,null,null,1,false);
                    else
                        CMLib.leveler().postExperience(mob,null,null,200,false);
                if(target.isMonster())
                {
                    beneficialAffect(mob,target,asLevel,(int)(TimeManager.MILI_HOUR/Tickable.TIME_TICK));
                    Skill_Convert A=(Skill_Convert)target.fetchEffect(ID());
                    if(A!=null) A.priorFaith=target.getWorshipCharID();
                }
                
			}
		}
		else
        {
            if((target.isMonster())&&(target.fetchEffect("Prayer_ReligiousDoubt")==null))
            {
                Ability A=CMClass.getAbility("Prayer_ReligiousDoubt");
                if(A!=null) A.invoke(mob,target,true,asLevel);
            }
            else
    			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to convert <T-NAMESELF>, but <S-IS-ARE> unconvincing.");
        }

		// return whether it worked
		return success;
	}
    
    public void makeLongLasting(){
        tickDown=(int)(Tickable.TICKS_PER_RLMIN*60*24*7);
    }
}
