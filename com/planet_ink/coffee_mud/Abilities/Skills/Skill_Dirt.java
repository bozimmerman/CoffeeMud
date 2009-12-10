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
public class Skill_Dirt extends StdSkill
{
	boolean doneTicking=false;
	public String ID() { return "Skill_Dirt"; }
	public String name(){ return "Dirt";}
	public String displayText(){ return "(Dirt in your eyes)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"DIRT"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!doneTicking)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((doneTicking)&&(msg.amISource(mob)))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You can see again!");
	}

	public int castingQuality(MOB mob, Environmental target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(!hereOK(mob))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0)
				return Ability.QUALITY_INDIFFERENT;
			if((target instanceof MOB)&&(((MOB)target).charStats().getBodyPart(Race.BODY_EYE)==0))
				return Ability.QUALITY_INDIFFERENT;
            if((target instanceof MOB)&&(!CMLib.flags().canSee((MOB)target)))
                return Ability.QUALITY_INDIFFERENT;
	        if(CMLib.flags().isSleeping(target))
				return Ability.QUALITY_INDIFFERENT;
			if(CMLib.flags().isFlying(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean hereOK(MOB mob)
	{
		Room R=mob.location();
		if(R==null) return false;
		if((R.domainConditions()==Room.CONDITION_WET)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		 ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_AIR)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_MAGIC)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_STONE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_METAL)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_CAVE)
		 ||(R.domainType()==Room.DOMAIN_INDOORS_WOOD))
			return false;
		return true;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;


		if(!hereOK(mob))
		{
            if(!auto)
    			mob.tell("There's no dirt here to kick!");
			return false;
		}

		if((!auto)&&(mob.charStats().getBodyPart(Race.BODY_FOOT)<=0))
		{
			mob.tell("You need feet to kick.");
			return false;
		}

		if((!auto)&&(target.charStats().getBodyPart(Race.BODY_EYE)==0))
		{
			mob.tell(target.name()+" has no eyes, and would not be affected.");
			return false;
		}
		
        if(CMLib.flags().isSleeping(target))
        {
            if(!auto)
                mob.tell(target.name()+" has "+target.charStats().hisher()+" eyes closed.");
            return false;
        }
        
		if((!auto)&&CMLib.flags().isFlying(mob))
		{
			mob.tell("You are too far from the ground to kick dirt.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,-(target.charStats().getStat(CharStats.STAT_DEXTERITY)*3)+(2*getXLEVELLevel(mob)),auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"Dirt flies at <T-NAME>!":"^F^<FIGHT^><S-NAME> kick(s) dirt at <T-NAMESELF>.^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> blinded!");
				maliciousAffect(mob,target,asLevel,3+(getXLEVELLevel(mob)/3),-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to kick dirt at <T-NAMESELF>, but miss(es).");
		return success;
	}
}
