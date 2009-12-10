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
public class Thief_Alertness extends ThiefSkill
{
	public String ID() { return "Thief_Alertness"; }
	public String name(){ return "Alertness";}
	public String displayText(){return "(Alertness)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"ALERTNESS"};
    public int classificationCode(){    return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public String[] triggerStrings(){return triggerStrings;}
	Room room=null;


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{

			MOB mob=(MOB)affected;
			if(!CMLib.flags().aliveAwakeMobile(mob,true))
			{ unInvoke(); return false;}
			if(mob.location()!=room)
			{
				room=mob.location();
				Vector choices=null;
				for(int i=0;i<room.numItems();i++)
				{
					Item I=room.fetchItem(i);
					if((I!=null)
					&&(CMLib.flags().canBeSeenBy(I,mob))
					&&(I.displayText().length()==0))
					{
						if(choices==null) choices=new Vector();
						choices.addElement(I);
					}
				}
				if(choices!=null)
				{
					int alert=getXLEVELLevel(mob);
					if(alert<=0)alert=1;
					while((alert>0)&&(choices.size()>0))
					{
						Item I=(Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
						choices.removeElement(I);
						mob.tell(I.name()+": "+I.description());
						alert--;
					}
				}
			}
		}
		return true;
	}

	public void unInvoke()
	{
		MOB M=(MOB)affected;
		super.unInvoke();
		if((M!=null)&&(!M.amDead()))
			M.tell("You don't feel quite so alert any more.");
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already alert.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_EYES),auto?"<T-NAME> become(s) alert.":"<S-NAME> become(s) suddenly alert.");
		if(!success)
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to look alert, but become(s) distracted.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
