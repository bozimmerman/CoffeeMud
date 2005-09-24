package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Prop_SparringRoom extends Property
{
	public String ID() { return "Prop_SparringRoom"; }
	public String name(){ return "Player Death Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(!msg.source().isMonster()))
		{
			MOB source=null;
			if((msg.tool()!=null)&&(msg.tool() instanceof MOB))
				source=(MOB)msg.tool();
			MOB target=msg.source();
			Room deathRoom=target.location();
			deathRoom.show(source,source,CMMsg.MSG_OK_VISUAL,msg.sourceMessage());
			if(source!=null)
			{
                CharClass combatCharClass=MUDFight.getCombatDominantClass(source,target);
                HashSet beneficiaries=MUDFight.getCombatBeneficiaries(source,target,combatCharClass);
                combatCharClass.dispenseExperience(beneficiaries,target);
			}
			target.makePeace();
			target.setRiding(null);
			for(int a=target.numEffects()-1;a>=0;a--)
			{
				Ability A=target.fetchEffect(a);
				if(A!=null) A.unInvoke();
			}
			target.setLocation(null);
			while(target.numFollowers()>0)
			{
				MOB follower=target.fetchFollower(0);
				if(follower!=null)
				{
					follower.setFollowing(null);
					target.delFollower(follower);
				}
			}
			target.setFollowing(null);
			Room R=null;
			if(text().trim().length()>0)
				R=CMMap.getRoom(text().trim());
			if(R==null) R=target.getStartRoom();
			R.bringMobHere(target,false);
			target.bringToLife(R,true);
			target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
			deathRoom.recoverRoomStats();
			return false;
		}
		return true;
	}
}
