package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_ImprovedShieldDefence extends FighterSkill
{
	public String ID() { return "Fighter_ImprovedShieldDefence"; }
	public String name(){ return "Improved Shield Defence";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_SHIELDUSE;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    protected boolean gettingBonus=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		gettingBonus=false;
		if((affected==null)||(!(affected instanceof MOB))) return;
		Item w=((MOB)affected).fetchFirstWornItem(Wearable.WORN_HELD);
		if((w==null)||(!(w instanceof Shield))) return;
		gettingBonus=true;
		affectableStats.setArmor(affectableStats.armor()-((int)Math.round(CMath.mul(w.envStats().armor(),(CMath.div(proficiency(),100.0+(5.0*getXLEVELLevel(invoker()))))))));
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((msg.amITarget(mob))
		&&(gettingBonus)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob.isInCombat())
		&&(CMLib.dice().rollPercentage()==1)
		&&(!mob.amDead()))
			helpProficiency(mob);
	}

}