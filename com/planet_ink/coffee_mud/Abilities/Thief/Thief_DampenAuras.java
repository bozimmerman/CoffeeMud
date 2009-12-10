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
public class Thief_DampenAuras extends ThiefSkill
{
	public String ID() { return "Thief_DampenAuras"; }
	public String name(){ return "Dampen Auras";}
	public String displayText(){return "(Dampened Auras)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"DAMPENAURAS"};
	public String[] triggerStrings(){return triggerStrings;}
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(unInvoked) return false;
		return super.tick(ticking,tickID);
	}
	
	public void affectEnvStats(Environmental host, EnvStats stats)
	{
		super.affectEnvStats(host,stats);
		if(unInvoked) 
			host.delEffect(this);
		else
			stats.addAmbiance("-MOST");
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(super.canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(msg.amISource((MOB)affected))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
				unInvoke();
			else
			if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				unInvoke();
		}
	}
	
	public void unInvoke()
	{
		Environmental E=affected;
		super.unInvoke();
		if((E instanceof MOB)&&(!((MOB)E).amDead()))
			((MOB)E).tell("You noticed the aura dampening is wearing away.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> can't dampen <S-YOUPOSS> auras again so soon.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":"<T-NAME> dampen(s) <T-HIS-HER> auras.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> attempt(s) to dampen <S-HIS-HER> auras, but fail(s).");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
			Ability A=target.fetchEffect(ID());
			if(A!=null)
			{
				A.tick(target,Tickable.TICKID_MOB);
				Item I=null;
				Environmental affecting=A.affecting();
				StringBuffer items=new StringBuffer("");
				for(int i=0;i<target.inventorySize();i++)
				{
					I=target.fetchInventory(i);
					if(I!=null)
					{
						I.addEffect(A);
						A.setAffectedOne(affecting);
						items.append(", "+I.name());
					}
				}
				if(items.length()>2)
					((MOB)target).tell("You've dampened the auras on the following items: "+items.substring(2));
				target.location().recoverRoomStats();
			}
		}
		return success;
	}
}