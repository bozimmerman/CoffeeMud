package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Disease_Lepresy extends Disease
{
	public String ID() { return "Disease_Lepresy"; }
	public String name(){ return "Leprosy";}
	public String displayText(){ return "(Leprosy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 10;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your leprosy is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) pale!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	public int difficultyLevel(){return 4;}

	private static String replaceDamageTag(String str, int damage, int damageType)
	{
		if(str==null) return null;
		int replace=str.indexOf("<DAMAGE>");
		if(replace>=0)
			return str.substring(0,replace)+CommonStrings.standardHitWord(damageType,damage)+str.substring(replace+8);
		return str;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.targetMessage()!=null))
		{
			if(msg.targetMessage().indexOf("<DAMAGE>")>=0)
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),replaceDamageTag(msg.targetMessage(),1,0),
						  msg.othersCode(),msg.othersMessage());
			else
			if((msg.tool()!=null)&&(msg.tool() instanceof Weapon))
			msg.modify(msg.source(),
						  msg.target(),
						  msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),"^F^<FIGHT^>"+((Weapon)msg.tool()).hitString(1)+"^</FIGHT^>^?",
						  msg.othersCode(),msg.othersMessage());
		}
		return super.okMessage(myHost,msg);
	}

}
