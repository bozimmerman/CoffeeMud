package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_DivinePerspective extends Prayer
{
	public String ID() { return "Prayer_DivinePerspective"; }
	public String name(){return "Divine Perspective";}
	public String displayText(){return "(Perspective)";}
	public long flags(){return Ability.FLAG_HOLY;}
	public int quality(){return Ability.OK_SELF;}
	public String mobName="";
	public boolean noRecurse=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if(invoker!=null)
				invoker.tell("The perspective of '"+mob.name()+"' fades from your mind.");
		super.unInvoke();

	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(noRecurse)return;

		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_LOOK)
		&&(invoker!=null)
		&&(msg.target()!=null)
		&&((invoker.location()!=((MOB)affected).location())||(!(msg.target() instanceof Room))))
		{
			noRecurse=true;
			FullMsg newAffect=new FullMsg(invoker,msg.target(),CMMsg.TYP_LOOK,null);
			msg.target().executeMsg(msg.target(),newAffect);
		}
		else
		if((affected instanceof MOB)
		&&(invoker!=null)
		&&(msg.source() != invoker)
		&&(invoker.location()!=((MOB)affected).location())
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null))
		{
			noRecurse=true;
			invoker.executeMsg(invoker,msg);
		}
		noRecurse=false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.getWorshipCharID().length()==0)
		||(CMMap.getDeity(mob.getWorshipCharID())==null))
		{
			mob.tell("You must worship a god to use this prayer.");
			return false;
		}
		Deity target=CMMap.getDeity(mob.getWorshipCharID());
		Room newRoom=target.location();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			mobName=target.Name();
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) the holy perspective of '"+mobName+"'.^?");
			FullMsg msg2=new FullMsg(mob,target,this,affectType(auto),null);
			if((mob.location().okMessage(mob,msg))&&((newRoom==mob.location())||(newRoom.okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(newRoom!=mob.location()) newRoom.send(target,msg2);
				beneficialAffect(mob,target,asLevel,10);
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke the holy perspective of "+target.Name()+", but fail(s).");


		// return whether it worked
		return success;
	}
}
