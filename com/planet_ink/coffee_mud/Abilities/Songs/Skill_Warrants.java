package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_Warrants extends BardSkill
{
	public String ID() { return "Skill_Warrants"; }
	public String name(){ return "Warrants";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"WARRANTS"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}

	public Behavior getArrest(Area A)
	{
		if(A==null) return null;
		Vector V=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
		if(V.size()==0) return null;
		return (Behavior)V.firstElement();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Behavior B=null;
		if(mob.location()!=null)
			B=getArrest(mob.location().getArea());
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,(-25+mob.charStats().getStat(CharStats.CHARISMA)),auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector V=new Vector();
				if(B!=null)
				{
					V.addElement(new Integer(2));
					B.modifyBehavior(mob.location().getArea(),mob,V);
				}
				if(V.size()==0)
				{
					mob.tell("No one is wanted for anything here.");
					return false;
				}
				StringBuffer buf=new StringBuffer("");
				buf.append(Util.padRight("Name",14)+" "+Util.padRight("Victim",14)+" "+Util.padRight("Witness",14)+" Crime\n\r");
				for(int v=0;v<V.size();v++)
				{
					Vector V2=(Vector)V.elementAt(v);
					buf.append(Util.padRight((String)V2.elementAt(0),14)+" ");
					buf.append(Util.padRight((String)V2.elementAt(1),14)+" ");
					buf.append(Util.padRight((String)V2.elementAt(2),14)+" ");
					buf.append(((String)V2.elementAt(3))+"\n\r");
				}
				if(!mob.isMonster()) mob.session().rawPrintln(buf.toString());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to gather warrant information, but fail(s).");

		return success;
	}

}
