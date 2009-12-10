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
public class Thief_Caltrops extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_Caltrops"; }
	public String name(){ return "Caltrops";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"CALTROPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public String caltropTypeName(){return "";}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
    public Vector getTrapComponents() { return new Vector(); }
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{maliciousAffect(mob,E,qualifyingClassLevel+trapBonus,0,-1); return (Trap)E.fetchEffect(ID());}

	public boolean sprung(){return false;}
	public void spring(MOB mob)
	{
		if((!invoker().mayIFight(mob))
		||(invoker().getGroupMembers(new HashSet()).contains(mob))
		||(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) some "+caltropTypeName()+"caltrops on the floor.");
		else
			CMLib.combat().postDamage(invoker(),mob,null,CMLib.dice().roll(1,6,adjustedLevel(invoker(),0)),
					CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"The "+caltropTypeName()+"caltrops on the ground <DAMAGE> <T-NAME>.");
		// does not set sprung flag -- as this trap never goes out of use
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((target!=null)&&(!(target instanceof Room)))
                return Ability.QUALITY_INDIFFERENT;
            target=(target!=null)?target:mob.location();
            if(target.fetchEffect(ID())!=null)
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return super.okMessage(myHost,msg);
		if(!(affected instanceof Room)) return super.okMessage(myHost,msg);
		if(invoker()==null) return super.okMessage(myHost,msg);
		Room room=(Room)affected;
		if((msg.amITarget(room)||room.isInhabitant(msg.source()))
		&&(!msg.amISource(invoker()))
		&&((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE)
			||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
				spring(msg.source());
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Environmental target=(givenTarget!=null)?givenTarget:mob.location();
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(CMStrings.capitalizeFirstLetter(caltropTypeName())+"Caltrops have already been tossed down here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(mob.location().show(mob,target,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT,"<S-NAME> throw(s) down "+caltropTypeName()+"caltrops!"))
				maliciousAffect(mob,target,asLevel,0,-1);
			else
				success=false;
		}
		else
			maliciousFizzle(mob,target,"<S-NAME> fail(s) to throw down <S-HIS-HER> "+caltropTypeName()+"caltrops properly.");
		return success;
	}
}
