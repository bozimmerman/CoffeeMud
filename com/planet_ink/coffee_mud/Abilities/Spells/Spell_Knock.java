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
public class Spell_Knock extends Spell
{
	public String ID() { return "Spell_Knock"; }
	public String name(){return "Knock";}
	public String displayText(){return "(Knock Spell)";}
	protected int canTargetCode(){return CAN_ITEMS|CAN_EXITS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Room R=givenTarget==null?mob.location():CMLib.map().roomLocation(givenTarget);
        if(R==null) R=mob.location();
		if((auto||mob.isMonster())&&((commands.size()<1)||(((String)commands.firstElement()).equals(mob.name()))))
		{
			commands.clear();
			int theDir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				Exit E=R.getExitInDir(d);
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
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=R.getExitInDir(dirCode);
		if(openThis==null)
			openThis=getTarget(mob,R,givenTarget,commands,Wearable.FILTER_ANY);
		if(openThis==null) return false;

		if(openThis instanceof Exit)
		{
			if(((Exit)openThis).isOpen())
			{
				mob.tell("That's already open!");
				return false;
			}
		}
		else
		if(openThis instanceof Container)
		{
			if(((Container)openThis).isOpen())
			{
				mob.tell("That's already open!");
				return false;
			}
		}
		else
		{
			mob.tell("You can't cast knock on "+openThis.name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		int levelDiff=openThis.envStats().level()-(mob.envStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		boolean success=proficiencyCheck(mob,-(levelDiff*25),auto);

		if(!success)
			beneficialWordsFizzle(mob,openThis,"<S-NAME> point(s) at "+openThis.name()+" and shout(s) incoherently, but nothing happens.");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,openThis,this,verbalCastCode(mob,openThis,auto),(auto?openThis.name()+" begin(s) to glow!":"^S<S-NAME> point(s) at <T-NAMESELF>.^?")+CMProps.msp("knock.wav",10));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				for(int a=0;a<openThis.numEffects();a++)
				{
					Ability A=openThis.fetchEffect(a);
					if((A!=null)&&(A.ID().equalsIgnoreCase("Spell_WizardLock")))
					{
						String txt=A.text().trim();
						int level=(A.invoker()!=null)?A.invoker().envStats().level():0;
						if(txt.length()>0)
						{
							if(CMath.isInteger(txt))
								level=CMath.s_int(txt);
							else
							{
								int x=txt.indexOf(' ');
								if((x>0)&&(CMath.isInteger(txt.substring(0,x))))
									level=CMath.s_int(txt.substring(0,x));
							}
						}
						if(level<(mob.envStats().level()+3+(2*getXLEVELLevel(mob))))
						{
							A.unInvoke();
							R.show(mob,null,openThis,CMMsg.MSG_OK_VISUAL,"A spell around <O-NAME> seems to fade.");
							break;
						}
					}
				}
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_UNLOCK,null);
				CMLib.utensils().roomAffectFully(msg,R,dirCode);
				msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_OPEN,"<T-NAME> opens.");
				CMLib.utensils().roomAffectFully(msg,R,dirCode);
			}
		}

		return success;
	}
}
