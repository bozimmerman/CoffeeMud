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
public class Thief_Listen extends ThiefSkill
{
	public String ID() { return "Thief_Listen"; }
	public String name(){ return "Listen";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;}
	private static final String[] triggerStrings = {"LISTEN"};
	public String[] triggerStrings(){return triggerStrings;}

	protected Room sourceRoom=null;
	protected Room room=null;
	protected String lastSaid="";

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location()!=null)
		&&(sourceRoom!=null)
		&&(!invoker().isInCombat())
		&&(invoker().location()==sourceRoom))
		{
			if(invoker().location()==room)
			{
				if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				&&(msg.othersCode()==CMMsg.NO_EFFECT)
				&&(msg.othersMessage()==null)
				&&(msg.sourceMessage()!=null)
				&&(!msg.amISource(invoker()))
				&&(!msg.amITarget(invoker()))
				&&(!lastSaid.equals(msg.sourceMessage())))
				{
					lastSaid=msg.sourceMessage();
					if((invoker().envStats().level()+(getXLEVELLevel(invoker())*10))>msg.source().envStats().level())
						invoker().tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
					else
						invoker().tell(msg.source(),null,null,"<S-NAME> said something, but you couldn't quite make it out.");
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.othersMinor()==CMMsg.TYP_SPEAK)
			&&(msg.othersMessage()!=null)
			&&(msg.sourceMessage()!=null)
			&&(!lastSaid.equals(msg.sourceMessage())))
			{
				lastSaid=msg.sourceMessage();
				if((invoker().envStats().level()+(getXLEVELLevel(invoker())*10))>msg.source().envStats().level())
					invoker().tell(msg.source(),msg.target(),msg.tool(),msg.sourceMessage());
				else
					invoker().tell(msg.source(),null,null,"<S-NAME> said something, but you couldn't quite make it out.");
			}

		}
		else
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whom=CMParms.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whom);
		if(!CMLib.flags().canHear(mob))
		{
			mob.tell("You don't hear anything.");
			return false;
		}

		if(room!=null)
		for(int a=room.numEffects()-1;a>=0;a--)
		{
			Ability A=room.fetchEffect(a);
			if((A.ID().equals(ID()))&&(invoker()==mob))
				A.unInvoke();
		}
		room=null;
		if(dirCode<0)
			room=mob.location();
		else
		{
			if((mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
			{
				mob.tell("Listen which direction?");
				return false;
			}
			room=mob.location().getRoomInDir(dirCode);
			if((room.domainType()&Room.INDOORS)==0)
			{
				mob.tell("You can only listen indoors.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_SMALL_HANDS_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,"<S-NAME> listen(s)"+((dirCode<0)?"":" "+Directions.getDirectionName(dirCode))+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			success=proficiencyCheck(mob,0,auto);
			int numberHeard=0;
			int levelsHeard=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)&&(!CMLib.flags().isSneaking(inhab))&&(!CMLib.flags().isHidden(inhab))&&(inhab!=mob))
				{
					numberHeard++;
					if(inhab.envStats().level()>(mob.envStats().level()+(2*super.getXLEVELLevel(mob))))
						levelsHeard+=(inhab.envStats().level()-(mob.envStats().level()+(2*super.getXLEVELLevel(mob))));
				}
			}
			if((success)&&(numberHeard>0))
			{
				if(((proficiency()+(getXLEVELLevel(mob)*10))>(50+levelsHeard))||(room==mob.location()))
				{
					mob.tell("You definitely hear "+numberHeard+" creature(s).");
					if(proficiency()>((room==mob.location())?50:75))
					{
						sourceRoom=mob.location();
						beneficialAffect(mob,room,asLevel,((room==mob.location())?0:10));
					}
				}
				else
					mob.tell("You definitely hear something.");
			}
			else
				mob.tell("You don't hear anything.");
		}
		return success;
	}

}
