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
public class Thief_Surrender extends ThiefSkill
{
	public String ID() { return "Thief_Surrender"; }
	public String name(){ return "Surrender";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"SURRENDER"};
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_INFLUENTIAL;}
	public String[] triggerStrings(){return triggerStrings;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Vector theList=new Vector();
		int gold=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB vic=mob.location().fetchInhabitant(i);
			if((vic!=null)&&(vic!=mob)&&(vic.isInCombat())&&(vic.getVictim()==mob))
			{
				gold+=(vic.envStats().level()*100)-(2*getXLEVELLevel(mob));
				theList.addElement(vic);
			}
		}
		double goldRequired=(double)gold;
		if((!mob.isInCombat())||(theList.size()==0))
		{
			mob.tell("There's no one to surrender to!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String localCurrency=CMLib.beanCounter().getCurrency(mob.getVictim());
	    String costWords=CMLib.beanCounter().nameCurrencyShort(localCurrency,goldRequired);
		if(success&&CMLib.beanCounter().getTotalAbsoluteValue(mob,localCurrency)>=goldRequired)
		{
			StringBuffer enemiesList=new StringBuffer("");
			for(int v=0;v<theList.size();v++)
			{
				MOB vic=(MOB)theList.elementAt(v);
				if(v==0)
					enemiesList.append(vic.name());
				else
				if(v==theList.size()-1)
					enemiesList.append(", and "+vic.name());
				else
					enemiesList.append(", "+vic.name());
			}
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> surrender(s) to "+enemiesList.toString()+", paying "+costWords+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.beanCounter().subtractMoney(mob,localCurrency,goldRequired);
				mob.recoverEnvStats();
				mob.makePeace();
				for(int v=0;v<theList.size();v++)
				{
					MOB vic=(MOB)theList.elementAt(v);
					CMLib.beanCounter().addMoney(vic,localCurrency,CMath.div(goldRequired,theList.size()));
					vic.recoverEnvStats();
					vic.makePeace();
				}
			}
			else
				success=false;
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to surrender and fail(s).");
		return success;
	}
}
