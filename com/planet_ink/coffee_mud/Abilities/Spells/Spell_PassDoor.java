package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_PassDoor extends Spell
{
	public String ID() { return "Spell_PassDoor"; }
	public String name(){return "Pass Door";}
	public String displayText(){return "(Pass Door)";}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_TRANSPORTING;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		affectedStats.setDisposition(affectedStats.disposition()|EnvStats.IS_INVISIBLE);
		affectedStats.setHeight(-1);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> no longer translucent.");

		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			int theDir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Exit E=mob.location().getExitInDir(d);
				if((E!=null)
				&&(!E.isOpen()))
				{
					theDir=d;
					break;
				}
			}
			if(theDir>=0)
				commands.addElement(Directions.getDirectionName(theDir));
		}
		String whatToOpen=CMParms.combine(commands,0);
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(!auto)
		{
			if(dirCode<0)
			{
				mob.tell("Pass which direction?!");
				return false;
			}

			Exit exit=mob.location().getExitInDir(dirCode);
			Room room=mob.location().getRoomInDir(dirCode);

			if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob)))
			{
				mob.tell("You can't see anywhere to pass that way.");
				return false;
			}
			//Exit opExit=room.getReverseExit(dirCode);
			if(exit.isOpen())
			{
				mob.tell("But it looks free and clear that way!");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,0,auto);

		if((!success)
		||(mob.fetchEffect(ID())!=null))
			beneficialVisualFizzle(mob,null,"<S-NAME> walk(s) "+Directions.getDirectionName(dirCode)+", but go(es) no further.");
		else
		if(auto)
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,verbalCastCode(mob,null,auto),"<S-NAME> shimmer(s) and turn(s) translucent.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,5);
				mob.recoverEnvStats();
			}
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,verbalCastCode(mob,null,auto),"^S<S-NAME> shimmer(s) and pass(es) "+Directions.getDirectionName(dirCode)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.addEffect(this);
				mob.recoverEnvStats();
				mob.tell("\n\r\n\r");
				CMLib.tracking().move(mob,dirCode,false,false);
				mob.delEffect(this);
				mob.recoverEnvStats();
			}
		}

		return success;
	}
}
