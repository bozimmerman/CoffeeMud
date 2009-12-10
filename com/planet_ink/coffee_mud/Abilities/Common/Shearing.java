package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Shearing extends CommonSkill
{
	public String ID() { return "Shearing"; }
	public String name(){ return "Shearing";}
	private static final String[] triggerStrings = {"SHEAR","SHEARING"};
    public int classificationCode() {   return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY; }
	public String[] triggerStrings(){return triggerStrings;}

	private MOB sheep=null;
	protected boolean failed=false;
	public Shearing()
	{
		super();
		displayText="You are shearing something...";
		verb="shearing";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((sheep!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&((!((MOB)affected).location().isInhabitant(sheep))))
			unInvoke();
		return super.tick(ticking,tickID);
	}
	
	public Vector getMyWool(MOB M)
	{
		Vector wool=new Vector();
		if((M!=null)
		&&(M.charStats().getMyRace()!=null)
		&&(M.charStats().getMyRace().myResources()!=null)
		&&(M.charStats().getMyRace().myResources().size()>0))
		{
			Vector V=M.charStats().getMyRace().myResources();
			for(int v=0;v<V.size();v++)
				if((V.elementAt(v) instanceof RawMaterial)
				&&(((RawMaterial)V.elementAt(v)).material()==RawMaterial.RESOURCE_WOOL))
					wool.addElement(V.elementAt(v));
		}
		return wool;
	}
	
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((sheep!=null)&&(!aborted))
				{
					if((failed)||(!mob.location().isInhabitant(sheep)))
						commonTell(mob,"You messed up your shearing completely.");
					else
					{
						mob.location().show(mob,null,sheep,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to shear <O-NAME>.");
						spreadImmunity(sheep);
						Vector V=getMyWool(sheep);
						for(int v=0;v<V.size();v++)
						{
							RawMaterial I=(RawMaterial)V.elementAt(v);
							I=(RawMaterial)I.copyOf();
							mob.location().addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=null;
		Room R=mob.location();
		if(R==null) return false;
		sheep=null;
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if((M!=mob)&&(CMLib.flags().canBeSeenBy(M,mob))&&(getMyWool(M).size()>0))
				{
					target=M;
					break;
				}
			}
		}
		else
		if(commands.size()==0)
			mob.tell("Shear what?");
		else
			target=super.getTarget(mob,commands,givenTarget);

		if(target==null) return false;
		if((getMyWool(target).size()<=0)
		||(!target.okMessage(target,CMClass.getMsg(target,target,this,CMMsg.MSG_OK_ACTION,null))))
		{
			commonTell(mob,target,null,"You can't shear <T-NAME>, there's no wool left on <T-HIM-HER>.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) shearing <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			sheep=target;
			verb="shearing "+target.name();
            playSound="scissor.wav";
			int duration=(target.envStats().weight()/(10+getXLEVELLevel(mob)));
			if(duration<10) duration=10;
			if(duration>40) duration=40;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}