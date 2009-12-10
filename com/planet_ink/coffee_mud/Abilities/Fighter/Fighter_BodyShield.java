package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_BodyShield extends FighterSkill
{
	public String ID() { return "Fighter_BodyShield"; }
	public String name(){ return "Body Shield";}
	public String displayText(){ return "";}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_GRAPPLING;}
	public boolean doneThisRound=false;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(!mob.amDead())
		&&(!CMLib.flags().isSleeping(mob))
		&&(msg.source()!=mob.getVictim())
		&&(msg.source()!=mob)
		&&((msg.value())>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&(mob.getVictim()!=null)
		&&(mob.getVictim().fetchEffect("Fighter_Pin")!=null)
		&&(!doneThisRound)
		&&(mob.getVictim().baseWeight()>=(mob.baseWeight()/2)))
		{
			Ability A=mob.fetchEffect("Fighter_Pin");
			if((A!=null)&&(A.invoker()==mob))
			{
				doneThisRound=true;
				int regain=(int)Math.round(CMath.mul((msg.value()),CMath.div(proficiency(),100.0)));
				msg.setValue(msg.value()-regain);
				CMMsg msg2=CMClass.getMsg(mob,mob.getVictim(),this,CMMsg.MSG_DAMAGE,"<S-NAME> use(s) <T-NAMESELF> as a body shield!");
				msg2.setValue(regain);
				msg.addTrailerMsg(msg2);
				helpProficiency(mob);
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}
}