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
public class Thief_Evesdrop extends ThiefSkill
{
	public String ID() { return "Thief_Evesdrop"; }
	public String name(){ return "Evesdrop";}
		// can NOT have a display text since the ability instance
		// is shared between the invoker and the target
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"EVESDROP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_TELL)
		&&(msg.target()==affected)
		&&(affected instanceof MOB)
		&&(msg.source()!=invoker())
		&&(invoker()!=null)
		&&(invoker().location()==((MOB)msg.target()).location())
		&&(!CMLib.flags().canBeSeenBy(invoker(),(MOB)affected))
		&&(CMLib.flags().isInTheGame(invoker(),true))
		&&(CMLib.flags().canBeHeardBy(affected,invoker())))
		{
			CMMsg msg2=(CMMsg)msg.copyOf();
			String targMsg=msg.targetMessage();
			if(targMsg != null)
			{
				int x=targMsg.toUpperCase().indexOf("TELL(S) YOU");
				if(x>=0) targMsg=targMsg.substring(0,x+8)+affected.Name()+targMsg.substring(x+11);
			}
			msg2.modify(msg.source(),invoker(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),targMsg,CMMsg.NO_EFFECT,null);
			invoker().executeMsg(invoker(),msg2);
		}
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((invoker!=null)&&(affected!=null))
				invoker.tell("You are no longer evesdropping on "+affected.name()+".");
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("Evesdrop on whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You cannot evesdrop on yourself?!");
			return false;
		}
		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			if(A.invoker()==mob)
				A.unInvoke();
			else
			{
				mob.tell(mob,target,null,"It is too crowded to evesdrop on <T-NAME>.");
				return false;
			}
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		if(CMLib.flags().canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));

		boolean success=proficiencyCheck(mob,-(levelDiff*10),auto);

		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_VISUAL,auto?"":"Your attempt to evesdrop on <T-NAMESELF> fails; <T-NAME> spots you!",CMMsg.MSG_OK_VISUAL,auto?"":"You spot <S-NAME> trying to evesdrop on you.",CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_THIEF_ACT,"You are now evesdroping on <T-NAME>.  Enter 'evesdrop <targetname>' again to disengage.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,adjustedLevel(mob,0));
			}
		}
		return success;
	}
}