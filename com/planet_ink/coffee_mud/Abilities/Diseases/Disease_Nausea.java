package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_Nausea extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Nausea";
	}

	private final static String localizedName = CMLib.lang().L("Nausea");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Nausea)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int spreadBitmap()
	{
		return 0; // handles it's own special kind of spreading
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 0;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return (int)CMProps.getTicksPerMudHour()*2;
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return CMLib.dice().roll(3, 4, 1);
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel much better.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> do(es) not feel right.^?");
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source()==affected)
		&&(msg.sourceMinor()==CMMsg.TYP_EAT))
		{
			final MOB mob=msg.source();
			final boolean hungry=mob.curState().getHunger()<=0;
			if((!hungry)
			&&(mob.curState().getHunger()>=mob.maxState().maxHunger(mob.baseWeight()))
			&&(CMLib.dice().roll(1,100,0)<3)
			&&(!CMLib.flags().isGolem(msg.source()))
			&&(msg.source().fetchEffect("Disease_Obesity")==null))
			{
				final Ability A=CMClass.getAbility("Disease_Obesity");
				if ((A != null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
					A.invoke(mob, mob, true, 0);
			}
		}
	}

	protected void beNautious()
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if((R!=null)
			&&(R.isInhabitant(mob))
			&&(R.show(mob, null, this, CMMsg.MASK_HANDS|CMMsg.MASK_SOUND,L("<S-NAME> throw(s) up."))))
			{
				final Item I=CMClass.getItem("GenLiquidResource");
				CMLib.flags().setGettable(I,true);
				((Drink)I).setLiquidHeld(100);
				((Drink)I).setLiquidRemaining(100);
				((Drink)I).setLiquidType(RawMaterial.RESOURCE_SLIME);
				I.setMaterial(RawMaterial.RESOURCE_SLIME);
				I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_UNSAVABLE);
				CMLib.materials().addEffectsToResource(I);
				I.recoverPhyStats();
				I.setName(L("a puddle of vomit"));
				I.setDisplayText(L("a stinky puddle lies here."));
				I.setDescription(L("It`s really gross."));
				R.addItem(I, Expire.Monster_EQ);
				final Ability sA=CMClass.getAbility("Prop_Smell");
				if(sA!=null)
				{
					sA.setMiscText("+It smells awful.");
					I.addNonUninvokableEffect(sA);
				}
				for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if((M!=null)
					&&(M!=mob)
					&&(!CMLib.flags().isGolem(M))
					&&(!CMLib.flags().isUndead(M))
					&&(CMLib.flags().canBeSeenBy(mob, M))
					&&(!CMLib.flags().isSleeping(M))
					&&(CMLib.dice().rollPercentage()<20))
					{
						Ability A=(Ability)this.copyOf();
						A.invoke(M, M, true, 0);
						A=M.fetchEffect(ID());
						if(A!=null)
							A.setStat("TICKDOWN", "15"); // just enough for one puke
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;
		final MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			beNautious();
		}
		return true;
	}

}

