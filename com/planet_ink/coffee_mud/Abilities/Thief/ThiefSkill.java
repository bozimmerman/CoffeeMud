package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class ThiefSkill extends StdAbility
{
	public String ID() { return "ThiefSkill"; }
	public String name(){ return "a Thief Skill";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){	return Ability.ACODE_THIEF_SKILL;}
    protected int canAffectCode(){return 0;}
    protected int canTargetCode(){return CAN_MOBS;}
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fumble(s) "+name()+" due to <S-HIS-HER> clumsy armor!");
			return false;
		}
		return true;
	}
	
	public int getMOBLevel(MOB meMOB)
	{
		if(meMOB==null) return 0;
		return meMOB.envStats().level();
	}
	public MOB getHighestLevelMOB(MOB meMOB, Vector not)
	{
		if(meMOB==null) return null;
		Room R=meMOB.location();
		if(R==null) return null;
		int highestLevel=0;
		MOB highestMOB=null;
		HashSet H=meMOB.getGroupMembers(new HashSet());
		if(not!=null) H.addAll(not);
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=meMOB)
			&&(!CMLib.flags().isSleeping(M))
			&&(!H.contains(M))
			&&(highestLevel<M.envStats().level()))
			{
				highestLevel=M.envStats().level();
				highestMOB=M;
			}
		}
		return highestMOB;
	}
}
