package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Doppleganger extends StdMOB
{
	public String ID(){return "Doppleganger";}
	private MOB mimicing=null;
	private long ticksSinceMimicing=0;

	public Doppleganger()
	{
		super();
		revert();
	}

	private void revert()
	{
		Random randomizer = new Random(System.currentTimeMillis());
		Username="a doppleganger";
		setDescription("A formless biped creature, with wicked black eyes.");
		setDisplayText("A formless biped stands here.");
		setBaseEnvStats(new DefaultEnvStats());
		setBaseCharStats(new DefaultCharStats());
		setBaseState(new DefaultCharState());
		setAlignment(0);
		setMoney(250);
		baseEnvStats.setWeight(100 + Math.abs(randomizer.nextInt() % 101));

		baseCharStats().setStat(CharStats.INTELLIGENCE,10 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.DEXTERITY,9 + Math.abs(randomizer.nextInt() % 6));

		baseEnvStats().setDamage(7);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(20);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Mobile"));
		addBehavior(CMClass.getBehavior("MudChat"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}



	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==MudHost.TICK_MOB))
		{
			if(mimicing!=null)
			{
				ticksSinceMimicing++;
				if(ticksSinceMimicing>500)
				{
					revert();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public DeadBody killMeDead(boolean createBody)
	{
		revert();
		return super.killMeDead(createBody);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.amITarget(this))&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)))
		{
			if(mimicing!=null)
			{
				if((mimicing.getVictim()!=null)&&(mimicing.getVictim()!=this))
					mimicing=null;
				if((mimicing.location()!=null)&&(mimicing.location()!=location()))
					mimicing=null;
			}
			if((mimicing==null)&&(location()!=null)&&(msg.source()!=null))
			{
				location().show(this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> take(s) on a new form!");
				mimicing=msg.source();
				Username=mimicing.Name();
				setDisplayText(mimicing.displayText());
				setDescription(mimicing.description());
				setBaseEnvStats(mimicing.baseEnvStats().cloneStats());
				setBaseCharStats(mimicing.baseCharStats().cloneCharStats());
				setBaseState(mimicing.baseState().cloneCharState());
				recoverEnvStats();
				recoverCharStats();
				recoverMaxState();
				resetToMaxState();
				ticksSinceMimicing=0;
			}
		}
		return true;
	}
}
