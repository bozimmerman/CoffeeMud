package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID(){	return "StdLasso";}
	public StdLasso()
	{
		super();
		setName("a lasso");
		setDisplayText("a lasso has been left here.");
		setDescription("Its a rope with a big stiff loop on one end!");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(1);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(0);
		baseGoldValue=10;
		recoverEnvStats();
		minRange=1;
		maxRange=1;
		weaponType=Weapon.TYPE_NATURAL;
		material=EnvResource.RESOURCE_HEMP;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(true);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			return;
			//msg.addTrailerMsg(new FullMsg(msg.source(),this,CMMsg.MSG_DROP,null));
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			unWear();
			msg.addTrailerMsg(new FullMsg(msg.source(),this,CMMsg.MASK_GENERAL|CMMsg.MSG_DROP,null));
			msg.addTrailerMsg(new FullMsg((MOB)msg.target(),this,CMMsg.MASK_GENERAL|CMMsg.MSG_GET,null));
			msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),this,CMMsg.MASK_GENERAL|CMMsg.TYP_GENERAL,null));
		}
		else
		if((msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_GENERAL)
		&&(((MOB)msg.target()).isMine(this))
		&&(msg.sourceMessage()==null))
		{
			Ability A=CMClass.getAbility("Thief_Bind");
			if(A!=null)
			{
				A.setAffectedOne(this);
				A.invoke(msg.source(),msg.target(),true,envStats().level());
			}
		}
		else
			super.executeMsg(myHost,msg);
	}


}
