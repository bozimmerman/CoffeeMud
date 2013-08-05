package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class GenMutingField extends GenPersonalShield
{
	public String ID(){	return "GenMutingField";}

	public GenMutingField()
	{
		super();
		setName("a muting field generator");
		setDisplayText("a muting field generator sits here.");
	}
	
	protected String fieldOnStr(MOB viewerM) { return "A thick field of energy surrounds "+name(viewerM)+"."; }
	
	protected String fieldDeadStr(MOB viewerM) { return "The thick field around <S-NAME> flickers and dies out."; }
	
	@Override public TechType getTechType() { return TechType.PERSONAL_SHIELD; }

	@Override 
	protected boolean doShield(MOB mob, CMMsg msg, double successFactor)
	{
		mob.phyStats().setSensesMask(mob.phyStats().sensesMask()|PhyStats.CAN_NOT_HEAR);
		if(mob.location()!=null)
		{
			if(msg.tool() instanceof Weapon)
			{
				String s="^F"+((Weapon)msg.tool()).hitString(0)+"^N";
				if(s.indexOf("<DAMAGE> <T-HIM-HER>")>0)
					mob.location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(s, "<DAMAGE>", "it`s absorbed by the shield around"));
				else
				if(s.indexOf("<DAMAGES> <T-HIM-HER>")>0)
					mob.location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(s, "<DAMAGES>", "is absorbed by the shield around"));
				else
					mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The field around <S-NAME> absorbs the "+msg.tool().name()+" damage.");
			}
			else
				mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The field around <S-NAME> absorbs the "+msg.tool().name()+" damage.");
		}
		return false;
	}
	
	@Override 
	protected boolean doesShield(MOB mob, CMMsg msg, double successFactor)
	{
		if(!activated())
			return false;
		if((msg.tool() instanceof Electronics) 
		&& (msg.tool() instanceof Weapon) 
		&& (Math.random() >= successFactor)
		&& (((Weapon)msg.tool()).weaponType()==Weapon.TYPE_SONICING))
		{
			return true;
		}
		return false;
	}
}
