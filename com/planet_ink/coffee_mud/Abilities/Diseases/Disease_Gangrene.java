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

public class Disease_Gangrene extends Disease
{
	public String ID() { return "Disease_Gangrene"; }
	public String name(){ return "Gangrene";}
	public String displayText(){ return "(Gangrene)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 4;}

	protected int DISEASE_TICKS(){return 100*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);}
	protected int DISEASE_DELAY(){return 5;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your gangrous wounds feel better.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> gangrous wounds.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> wince(s) in pain.";}
	public int abilityCode(){return 0;}
	protected int tickUpToDay=0;
	protected int daysSick=0;
    private boolean norecurse=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;
		tickUpToDay++;
		if(tickUpToDay==CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY))
		{
			daysSick++;
			tickUpToDay=0;
		}
		MOB mob=(MOB)affected;
		if(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
		{ unInvoke(); return false;}
		if(lastHP<mob.curState().getHitPoints())
			mob.curState().setHitPoints(mob.curState().getHitPoints()
							-((mob.curState().getHitPoints()-lastHP)/2));
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,DISEASE_AFFECT());
			int damage=1;
			CMLib.combat().postDamage(diseaser,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_DISEASE,-1,null);
			if(CMLib.dice().rollPercentage()==1)
			{
				Ability A=CMClass.getAbility("Disease_Fever");
				if(A!=null) A.invoke(diseaser,mob,true,0);
			}
			return true;
		}
		lastHP=mob.curState().getHitPoints();
		return true;
	}
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null) return;
		if(daysSick>0)
		{
			affectableState.setHitPoints(affectableState.getHitPoints()-(daysSick*(affectableState.getHitPoints()/10)));
			if((affectableState.getHitPoints()<=0)&&(!norecurse))
			{
				MOB diseaser=invoker;
				if(diseaser==null) diseaser=affected;
                norecurse=true;
                CMLib.combat().postDeath(diseaser,affected,null);
                norecurse=false;
			}
		}
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-4);
		if(affectableStats.getStat(CharStats.STAT_CHARISMA)<0)
		affectableStats.setStat(CharStats.STAT_CHARISMA,0);
	}
}
