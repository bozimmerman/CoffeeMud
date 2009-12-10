package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_Narcolepsy extends Disease
{
	public String ID() { return "Disease_Narcolepsy"; }
	public String name(){ return "Narcolepsy";}
	public String displayText(){ return "(Narcolepsy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return (int)(Tickable.TIME_MILIS_PER_MUDHOUR/Tickable.TIME_TICK);}
	protected String DISEASE_DONE(){return "Your narcolepsy is cured!";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) sleepy.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> <S-IS-ARE> getting sleepy...";}
	public int abilityCode(){return 0;}
	public int difficultyLevel(){return 6;}
	protected int attDown=1;

	public boolean tick(Tickable ticking, int tickID)
	{

		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.curState().adjFatigue(mob.curState().getFatigue()+CharState.FATIGUED_MILLIS,mob.maxState());
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if(!CMLib.flags().isSleeping(mob))
			{
				Command C=CMClass.getCommand("Sleep");
				try{if(C!=null) C.execute(mob,CMParms.parse("Sleep"),Command.METAFLAG_FORCED);}catch(Exception e){}
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)/2);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<1)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
		super.affectCharStats(affected,affectableStats);
	}

}
