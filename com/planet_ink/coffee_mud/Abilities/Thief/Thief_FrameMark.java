package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_FrameMark extends ThiefSkill
{
	public String ID() { return "Thief_FrameMark"; }
	public String name(){ return "Frame Mark";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"FRAME"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 50;}

	public MOB getMark(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}
	public int getMarkTicks(MOB mob)
	{
		Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getMark(mob);
		if(target==null)
		{
			mob.tell("You need to have marked someone before you can frame him or her.");
			return false;
		}

		Behavior B=null;
		if(mob.location()!=null) B=CoffeeUtensils.getLegalBehavior(mob.location());
		if((B==null)
		||(!B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),mob,new Integer(6))))
		{
			mob.tell("You aren't wanted for anything here.");
			return false;
		}
		if(mob.getMoney()<(target.envStats().level()*1000))
		{
			mob.tell("You'll need at least "+(target.envStats().level()*1000)+" gold on hand to frame "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=(target.envStats().level()-mob.envStats().level()*15);
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(mob,levelDiff,auto);

		mob.setMoney(mob.getMoney()-(target.envStats().level()*1000));

		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> frame(s) <T-NAMESELF>.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Vector V=new Vector();
			V.addElement(new Integer(0));
			V.addElement(target);
			B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),mob,V);
		}
		return success;
	}

}
