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
   Copyright 2001-2018 Bo Zimmerman

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
public class FlamingSword extends Longsword
{
	@Override
	public String ID()
	{
		return "FlamingSword";
	}

	public FlamingSword()
	{
		super();

		setName("a fancy longsword");
		setDisplayText("a fancy longsword has been dropped on the ground.");
		setDescription("A one-handed sword with a very slight red tinge on the blade.");
		secretIdentity="A Flaming Sword (Additional fire damage when you strike)";
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(1);
		basePhyStats().setWeight(4);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(8);
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_LIGHTSOURCE | PhyStats.IS_BONUS);
		baseGoldValue=2500;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_STEEL;
		weaponDamageType=TYPE_SLASHING;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead())
		&&(msg.source()==owner()))
		{
			final Room room=msg.source().location();
			final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),this,
					CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
			if((room!=null) && (room.okMessage(msg.source(),msg2)))
			{
				room.send(msg.source(), msg2);
				if(msg2.value()<=0)
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= basePhyStats().level();
					CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),null,flameDamage,
							CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,name()+" <DAMAGE> <T-NAME>!");
				}
			}
		}
	}

}
