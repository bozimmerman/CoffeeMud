package com.planet_ink.coffee_mud.Items.Weapons;
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



/*
   Copyright 2003-2016 Bo Zimmerman

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
public class StdLasso extends StdWeapon
{
	@Override
	public String ID()
	{
		return "StdLasso";
	}

	public StdLasso()
	{
		super();
		setName("a lasso");
		setDisplayText("a lasso has been left here.");
		setDescription("Its a rope with a big stiff loop on one end!");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(1);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		baseGoldValue=10;
		recoverPhyStats();
		minRange=1;
		maxRange=1;
		weaponDamageType=Weapon.TYPE_NATURAL;
		material=RawMaterial.RESOURCE_HEMP;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(true);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			msg.setValue(0);
		}
		return true;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			return;
			//msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			unWear();
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
			msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.TYP_GENERAL,null));
		}
		else
		if((msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_GENERAL)
		&&(((MOB)msg.target()).isMine(this))
		&&(msg.sourceMessage()==null))
		{
			final Ability A=CMClass.getAbility("Thief_Bind");
			if(A!=null)
			{
				A.setAffectedOne(this);
				A.invoke(msg.source(),(MOB)msg.target(),true,phyStats().level());
			}
		}
		else
			super.executeMsg(myHost,msg);
	}


}
