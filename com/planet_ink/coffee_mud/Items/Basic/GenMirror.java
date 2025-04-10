package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class GenMirror extends GenItem
{
	@Override
	public String ID()
	{
		return "GenMirror";
	}

	protected boolean oncePerRound=false;

	public GenMirror()
	{
		super();
		setName("a generic mirror");
		basePhyStats.setWeight(2);
		setDisplayText("a generic mirror sits here.");
		setDescription("You see yourself in it!");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_GLASS);
	}

	private volatile MOB lastLooker = null;
	private volatile long timeout = 0;

	@Override
	public String description()
	{
		if((System.currentTimeMillis()-timeout)>1000)
			this.lastLooker=null;
		final MOB lastLooker = this.lastLooker;
		if(lastLooker != null)
		{
			final MOB looker = CMClass.getFactoryMOB("someone", 1, CMLib.map().roomLocation(lastLooker));
			looker.basePhyStats().setSensesMask(lastLooker.phyStats().sensesMask());
			final CMMsg msg = CMClass.getMsg(looker, lastLooker, null, CMMsg.TYP_LOOK, null);
			final Session fakeS=(Session)CMClass.getCommon("FakeSession");
			fakeS.initializeSession(null,Thread.currentThread().getThreadGroup().getName(),"MEMORY");
			fakeS.setMob(looker);
			looker.setSession(fakeS);
			CMLib.commands().handleBeingLookedAt(msg);
			fakeS.setMob(null);
			looker.setSession(null);
			looker.destroy();
			//this.lastLooker=null;
			return L("In @x1, you see:\n\r^H@x2.",name(),fakeS.getAfkMessage());
		}
		return "You see yourself in it!";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.target()==this)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			timeout = System.currentTimeMillis();
			lastLooker = msg.source();
		}
		if((owner==null)
		||(!(owner instanceof MOB))
		||(!amBeingWornProperly()))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)owner;
		if((msg.amITarget(mob))
		&&(!oncePerRound)
		&&(msg.tool() instanceof Ability)
		&&((msg.tool().ID().equals("Spell_FleshStone"))
			||(msg.tool().ID().equals("Prayer_FleshRock")))
		&&(!mob.amDead())
		&&(mob!=msg.source())
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
		{
			oncePerRound=true;
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 reflects the vicious magic!",name()));
			final Ability A=(Ability)msg.tool();
			A.invoke(mob,msg.source(),true,phyStats().level());
			return false;
		}
		oncePerRound=false;
		return super.okMessage(myHost,msg);
	}

}
