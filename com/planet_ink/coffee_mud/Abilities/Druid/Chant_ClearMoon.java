package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_ClearMoon extends Chant
{
	public String ID() { return "Chant_ClearMoon"; }
	public String name(){ return "Clear Moon";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}


	public void clearMoons(Environmental E)
	{
		if(E!=null)
		for(int a=E.numEffects()-1;a>=0;a--)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)
			&&(Util.bset(A.flags(),Ability.FLAG_MOONCHANGING)
			   ||Util.bset(A.flags(),Ability.FLAG_MOONSUMMONING)))
				A.unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for a clear moon, but the magic fades.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType(auto),"^S<S-NAME> chant(s) for a clear moon.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				clearMoons(thatRoom);
				for(int i=0;i<thatRoom.numInhabitants();i++)
				{
					MOB M=thatRoom.fetchInhabitant(i);
					clearMoons(M);
				}
				for(int i=0;i<thatRoom.numItems();i++)
				{
					Item I=thatRoom.fetchItem(i);
					clearMoons(I);
				}
			}
		}

		return success;
	}
}