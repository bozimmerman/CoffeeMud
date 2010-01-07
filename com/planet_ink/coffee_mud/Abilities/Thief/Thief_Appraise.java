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
public class Thief_Appraise extends ThiefSkill
{
	public String ID() { return "Thief_Appraise"; }
	public String name(){ return "Appraise";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"APPRAISE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public int code=0;
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STREETSMARTS;}

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell("What would you like to appraise?");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.elementAt(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
        levelDiff*=5;
		boolean success=proficiencyCheck(mob,-levelDiff,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> appraise(s) <T-NAMESELF>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			double realValue=0.0;
			if(target instanceof Coins)
				realValue = ((Coins)target).getTotalValue();
			else
				realValue=(double)target.value();
			int materialCode=target.material();
			int weight=target.baseEnvStats().weight();
			int height=target.baseEnvStats().height();
			int allWeight=target.envStats().weight();
			if(!success)
			{
				double deviance=CMath.div(CMLib.dice().roll(1,100,0)+50,100);
				realValue=CMath.mul(realValue,deviance);
				materialCode=CMLib.dice().roll(1,RawMaterial.CODES.TOTAL(),-1);
				weight=(int)Math.round(CMath.mul(weight,deviance));
				height=(int)Math.round(CMath.mul(height,deviance));
				allWeight=(int)Math.round(CMath.mul(allWeight,deviance));
			}
			StringBuffer str=new StringBuffer("");
			str.append(target.name()+" is made of "+RawMaterial.CODES.NAME(materialCode));
			str.append(" is worth about "+CMLib.beanCounter().nameCurrencyShort(mob,realValue)+".");
			if(target instanceof Armor)
				str.append("\n\r"+target.name()+" is a size "+height+".");
			if(weight!=allWeight)
				str.append("\n\rIt weighs "+weight+" pounds empty and "+allWeight+" pounds right now.");
			else
				str.append("\n\rIt weighs "+weight+" pounds.");
			mob.tell(str.toString());
		}
		return success;
	}

}
