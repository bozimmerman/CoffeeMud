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
public class Thief_Bribe extends ThiefSkill
{
	public String ID() { return "Thief_Bribe"; }
	public String name(){ return "Bribe";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"BRIBE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	protected MOB lastChecked=null;
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL; }

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

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
		{
			mob.tell("You can't bribe "+target.name()+".");
			return false;
		}

		if(commands.size()<1)
		{
			mob.tell("Bribe "+target.charStats().himher()+" to do what?");
			return false;
		}

		Object O=CMLib.english().findCommand(target,commands);
		if(O instanceof Command)
		{
			if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob)))
			{
				mob.tell("You can't bribe someone into doing that.");
				return false;
			}
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't bribe someone to following you.");
			return false;
		}

		int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		double amountRequired=CMLib.beanCounter().getTotalAbsoluteNativeValue(target)
						+((double)((100l-((mob.charStats().getStat(CharStats.STAT_CHARISMA)+(2l*getXLEVELLevel(mob)))*2)))*target.envStats().level());

		String currency=CMLib.beanCounter().getCurrency(target);
		boolean success=proficiencyCheck(mob,0,auto);

		if((!success)||(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to bribe <T-NAMESELF> to '"+CMParms.combine(commands,0)+"', but no deal is reached.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired)
			{
			    String costWords=CMLib.beanCounter().nameCurrencyShort(currency,amountRequired);
				mob.tell(target.charStats().HeShe()+" requires "+costWords+" to do this.");
			}
			success=false;
		}
		else
		{
		    String costWords=CMLib.beanCounter().nameCurrencyShort(target,amountRequired);
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> bribe(s) <T-NAMESELF> to '"+CMParms.combine(commands,0)+"' for "+costWords+".^?");
			CMLib.beanCounter().subtractMoney(mob,currency,amountRequired);
			mob.recoverEnvStats();
            CMMsg msg2=CMClass.getMsg(mob,target,null,CMMsg.MSG_ORDER,null);
			if((mob.location().okMessage(mob,msg))
            &&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
                mob.location().send(mob,msg2);
				target.doCommand(commands,Command.METAFLAG_FORCED);
			}
			CMLib.beanCounter().addMoney(mob,currency,amountRequired);
			target.recoverEnvStats();
		}
		if(target==lastChecked)
			setProficiency(oldProficiency);
		lastChecked=target;
		return success;
	}

}
