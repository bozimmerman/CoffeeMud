package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
public class Skill_Stability extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Stability";
	}

	private final static String localizedName = CMLib.lang().L("Stability");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if((msg.tool() instanceof Ability)
		&&(msg.amITarget(affected))
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&&(((Ability)msg.tool()).abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_MOVING))
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,-40+(2*getXLEVELLevel(mob)),false)))
		{
			Room roomS=null;
			Room roomD=null;
			if(msg.target() instanceof MOB)
				roomD=((MOB)msg.target()).location();
			if(msg.source().location()!=null)
				roomS=msg.source().location();
			if(msg.target() instanceof Room)
				roomD=(Room)msg.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.show((MOB)affected,null,msg.tool(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> remain(s) stable despite the <O-NAME>."));
			if(roomD!=null)
				roomD.show((MOB)affected,null,msg.tool(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> remain(s) stable despite the <O-NAME>."));
			helpProficiency((MOB)affected, 0);
			return false;
		}
		return true;
	}

}
