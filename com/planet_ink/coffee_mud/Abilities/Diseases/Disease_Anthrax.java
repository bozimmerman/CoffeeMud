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

public class Disease_Anthrax extends Disease
{
	public String ID() { return "Disease_Anthrax"; }
	public String name(){ return "Anthrax";}
	public String displayText(){ return "(Anthrax)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 2;}

	protected int DISEASE_TICKS(){return CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*10;}
	protected int DISEASE_DELAY(){return 15;}
	protected int lastHP=Integer.MAX_VALUE;
	protected String DISEASE_DONE(){return "Your anthrax wounds clear up.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) ill.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> watch(s) black necrotic wounds appear on <S-HIS-HER> flesh.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_CONTACT;}
    private boolean norecurse=false;
	protected int conDown=0;
	protected int conTickDown=60;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,DISEASE_AFFECT());
			int damage=CMLib.dice().roll(1,6,0);
			if(damage>1)
            {
				CMLib.combat().postDamage(diseaser,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_DISEASE,-1,null);
            }
			if((--conTickDown)<=0)
			{
				conTickDown=60;
				conDown++;
			}
			return true;
		}
		lastHP=mob.curState().getHitPoints();
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<=0) return;
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-conDown);
		if((affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)&&(!norecurse))
		{
			conDown=-1;
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=affected;
            norecurse=true;
            CMLib.combat().postDeath(diseaser,affected,null);
            norecurse=false;
		}
	}
}
