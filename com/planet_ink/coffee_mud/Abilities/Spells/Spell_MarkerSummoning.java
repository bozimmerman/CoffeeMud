package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_MarkerSummoning extends Spell
{
	public String ID() { return "Spell_MarkerSummoning"; }
	public String name(){return "Marker Summoning";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room oldRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
			for(int a=0;a<R.numEffects();a++)
			{
				Ability A=R.fetchEffect(a);
				if((A!=null)
				&&(A.invoker()==mob))
				{
					if(A.ID().equals("Spell_SummonMarker"))
					{
						oldRoom=R;
						break;
					}
				}
			}
			if(oldRoom!=null) break;
		}
		if(oldRoom==null)
		{
			mob.tell("You can't seem to focus on your marker.  Are you sure you've already summoned it?");
			return false;
		}
		Room newRoom=mob.location();
		if(oldRoom==newRoom)
		{
			mob.tell("But your marker is HERE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		Vector inhabs=new Vector();
		int profNeg=0;
		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB M=oldRoom.fetchInhabitant(m);
			if(M!=null){
				inhabs.addElement(M);
				int adjustment=M.envStats().level()-mob.envStats().level();
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems();

		boolean success=profficiencyCheck(mob,-(profNeg/2),auto);

		if((success)&&(inhabs.size()>0))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> summon(s) the power of <S-HIS-HER> marker energy!^?");
			if((mob.location().okMessage(mob,msg))&&(oldRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<inhabs.size();i++)
				{
					MOB follower=(MOB)inhabs.elementAt(i);
					FullMsg enterMsg=new FullMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appear(s) in a burst of light.");
					FullMsg leaveMsg=new FullMsg(follower,oldRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,"<S-NAME> disappear(s) in a great summoning swirl.");
					if(oldRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						follower.makePeace();
						oldRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell("\n\r\n\r");
						CommonMsgs.look(follower,true);
					}
				}
				Vector items=new Vector();
				for(int i=oldRoom.numItems()-1;i>=0;i--)
				{
					Item I=oldRoom.fetchItem(i);
					if(I!=null) items.addElement(I);
				}
				for(int i=0;i<items.size();i++)
				{
					Item I=(Item)items.elementAt(i);
					oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" disappears in a summoning swirl!");
					newRoom.bringItemHere(I,-1);
					newRoom.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" appears in a burst of light!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon <S-HIS-HER> marker energy, but fail(s).");


		// return whether it worked
		return success;
	}
}
