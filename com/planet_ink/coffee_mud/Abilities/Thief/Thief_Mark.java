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
public class Thief_Mark extends ThiefSkill
{
	public String ID() { return "Thief_Mark"; }
	public String name(){ return "Mark";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"MARK"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public int code=0;
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_COMBATLORE;}

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}
	public MOB mark=null;
	public int ticks=0;

	public String displayText(){
		if(mark!=null)
			return "(Marked: "+mark.name()+", "+ticks+" ticks)";
		return "";
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amISource(mark)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			mark=null;
			ticks=0;
			setMiscText("");
		}
		super.executeMsg(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected!=null)&&(affected instanceof MOB)&&(((MOB)affected).getVictim()==mark))
		{
			int xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setDamage(affectableStats.damage()+((ticks+xlvl)/20));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+((ticks+xlvl)/2));
		}
	}

	public boolean tick(Tickable me, int tickID)
	{
		if((text().length()==0)
		||((affected==null)||(!(affected instanceof MOB))))
		   return super.tick(me,tickID);
		MOB mob=(MOB)affected;
		if(mob.location()!=null)
		{
			if(mark==null)
			{
				int x=text().indexOf("/");
				if(x<0) return super.tick(me,tickID);
				MOB M=mob.location().fetchInhabitant(text().substring(0,x));
				if(M!=null)
				{
					mark=M;
					ticks=CMath.s_int(text().substring(x+1));
				}
				else
				{
					mark=null;
					ticks=0;
					setMiscText("");
				}
			}
			else
			if(mob.location().isInhabitant(mark)
		    &&(CMLib.flags().canBeSeenBy(mark,mob))
		    &&(!CMLib.flags().canBeSeenBy(mob,mark)))
			{
				ticks++;
				setMiscText(mark.Name()+"/"+ticks);
			}
			else
			if(mark.amDestroyed())
			{
				mark=null;
				ticks=0;
				setMiscText("");
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("Who would you like to mark?");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You cannot mark yourself!");
			return false;
		}
		Ability A=mob.fetchEffect(ID());
		if((A!=null)&&(((Thief_Mark)A).mark==target))
		{
			target.delEffect(A);
			mob.tell("You remove your mark from "+target.displayName(mob));
			return true;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
        if(levelDiff<0) levelDiff=0;
        levelDiff*=5;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);

		if(!success)
			return beneficialVisualFizzle(mob,target,"<S-NAME> lose(s) <S-HIS-HER> concentration on <T-NAMESELF>.");
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> mark(s) <T-NAMESELF>.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			A=mob.fetchEffect(ID());
			if(A==null)
			{
				A=(Ability)copyOf();
				mob.addEffect(A);
				A.makeNonUninvokable();
			}
			((Thief_Mark)A).mark=target;
			((Thief_Mark)A).ticks=0;
			A.setMiscText(target.Name()+"/0");
			mob.tell("You may use the mark skill again to unmark them.");
		}
		return success;
	}

}
