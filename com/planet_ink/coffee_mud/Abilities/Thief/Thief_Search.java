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
public class Thief_Search extends ThiefSkill
{
	public String ID() { return "Thief_Search"; }
	public String name(){ return "Search";}
	public String displayText(){return "(Searching)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;}
	private static final String[] triggerStrings = {"SEARCH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    private Room lastRoom=null;
    private int bonusThisRoom=0;
    
    public void affectCharStats(MOB affected, CharStats affectableStats)
    {
        super.affectCharStats(affected,affectableStats);
        affectableStats.setStat(CharStats.STAT_SAVE_OVERLOOKING,bonusThisRoom+proficiency()+affectableStats.getStat(CharStats.STAT_SAVE_OVERLOOKING));
    }
    
    public boolean tick(Tickable ticking, int tickID)
    {
        if((affected!=null)&&(affected instanceof MOB))
        {
            if(!CMLib.flags().aliveAwakeMobile((MOB)affected,true))
            { unInvoke(); return false;}
            if(((MOB)affected).location()!=lastRoom)
            {
                lastRoom=((MOB)affected).location();
                bonusThisRoom=getXLEVELLevel((MOB)affected)*2;
                ((MOB)affected).recoverCharStats();
            }
            else
            if(bonusThisRoom<affected.envStats().level())
            {
                bonusThisRoom+=5;
	            ((MOB)affected).recoverCharStats();
	        }
        }
        return true;
    }

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.fetchEffect(this.ID())!=null)
                return Ability.QUALITY_INDIFFERENT;
            
            Room R=mob.location();
            if(R!=null)
                for(int r=0;r<R.numInhabitants();r++)
                {
                    MOB M=R.fetchInhabitant(r);
                    if((M!=null)&&(M!=mob)&&(CMLib.flags().isHidden(M)))
                        return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
                }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already aware of hidden things.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"<T-NAME> become(s) very observant.":"<S-NAME> examine(s) <S-HIS-HER> surroundings carefully.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> look(s) around carefully, but become(s) distracted.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			target.envStats().setSensesMask(mob.envStats().sensesMask()|EnvStats.CAN_SEE_HIDDEN);
			target.envStats().setSensesMask(mob.envStats().sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
			CMLib.commands().postLook(target,false);
			target.recoverEnvStats();
		}
		return success;
	}
}
