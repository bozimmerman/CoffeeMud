package com.planet_ink.coffee_mud.Items.MiscTech;
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
public class StdAbsorbantShield extends StdReflectiveShield
{
	public String ID(){	return "StdAbsorbantShield";}

	public StdAbsorbantShield()
	{
		super();
		setName("an absorption shield generator");
		basePhyStats.setWeight(2);
		setDisplayText("an absorption shield generator sits here.");
		setDescription("");
		baseGoldValue=500;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		super.setRawProperLocationBitmap(Wearable.WORN_ABOUT_BODY);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
	}
	
	@Override protected String fieldOnStr(MOB viewerM) { return "A sparkling field of energy surrounds "+name(viewerM)+"."; }
	
	@Override protected String fieldDeadStr(MOB viewerM) { return "The sparkling field around <S-NAME> flickers and dies out."; }
	
	@Override protected boolean doShield(MOB mob, CMMsg msg, double successFactor)
	{
		if(msg.value()<=0) 
			return true;
		if(successFactor>=1.0)
		{
			mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The sparkling field around <S-NAME> completely absorbs the "+msg.tool().name()+" attack from <T-NAME>.");
			msg.setValue(0);
		}
		else
		if(successFactor>=0.0)
		{
			msg.setValue((int)Math.round(successFactor*msg.value()));
			final String showDamage = CMProps.getVar(CMProps.Str.SHOWDAMAGE).equalsIgnoreCase("YES")?" ("+Math.round(successFactor*100.0)+")":"";
			if(successFactor>=0.75)
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,"The sparkling field around <S-NAME> absorbs most"+showDamage+" of the <O-NAMENOART> damage."));
			else
			if(successFactor>=0.50)
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,"The sparkling field around <S-NAME> absorbs much"+showDamage+" of the <O-NAMENOART> damage."));
			else
			if(successFactor>=0.25)
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,"The sparkling field around <S-NAME> absorbs some"+showDamage+" of the <O-NAMENOART> damage."));
			else
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,"The sparkling field around <S-NAME> absorbs a little"+showDamage+" of the <O-NAMENOART> damage."));
		}
		return true;
	}
	
	@Override protected boolean doesShield(MOB mob, CMMsg msg, double successFactor)
	{
		return activated();
	}
	
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAbsorbantShield)) return false;
		return super.sameAs(E);
	}
}