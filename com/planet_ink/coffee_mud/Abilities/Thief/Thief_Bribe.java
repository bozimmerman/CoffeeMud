package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Thief_Bribe extends ThiefSkill
{
	public String ID() { return "Thief_Bribe"; }
	public String name(){ return "Bribe";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"BRIBE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	private MOB lastChecked=null;

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("Bribe whom?");
			return false;
		}
		Vector V=new Vector();
		V.addElement(commands.elementAt(0));
		MOB target=this.getTarget(mob,V,givenTarget);
		if(target==null) return false;

		commands.removeElementAt(0);

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.INTELLIGENCE)<3))
		{
			mob.tell("You can't bribe "+target.name()+".");
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("Bribe "+target.charStats().himher()+" to do what?");
			return false;
		}

		Object O=EnglishParser.findCommand(target,commands);
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob)))
			{
				mob.tell("You can't bribe someone into doing that.");
				return false;
			}
		}
		
		if((((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		&&(!target.isMonster())
		&&(!mob.isMonster()))
		{
			mob.tell("You can't bribe someone to following you.");
			return false;
		}
		
		int oldProfficiency=profficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int amountRequired=target.getMoney()+(Math.round((100-(mob.charStats().getStat(CharStats.CHARISMA)*2)))*target.envStats().level());

		boolean success=profficiencyCheck(mob,0,auto);

		if((!success)||(mob.getMoney()<amountRequired))
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to bribe <T-NAMESELF> to '"+Util.combine(commands,0)+"', but no deal is reached.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if(mob.getMoney()<amountRequired)
				mob.tell(target.charStats().HeShe()+" requires "+amountRequired+" coins to do this.");
			success=false;
		}
		else
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> bribe(s) <T-NAMESELF> to '"+Util.combine(commands,0)+"' for "+amountRequired+" coins.^?");
			mob.setMoney(mob.getMoney()-amountRequired);
			mob.recoverEnvStats();
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.doCommand(commands);
			}
			target.setMoney(target.getMoney()+amountRequired);
			target.recoverEnvStats();
		}
		if(target==lastChecked)
			setProfficiency(oldProfficiency);
		lastChecked=target;
		return success;
	}

}
