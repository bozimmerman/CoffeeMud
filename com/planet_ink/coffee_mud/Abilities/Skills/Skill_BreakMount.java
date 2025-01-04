package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.Properties.Prop_RideResister;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
   Copyright 2023-2025 Bo Zimmerman

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
public class Skill_BreakMount extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_BreakMount";
	}

	private final static String localizedName = CMLib.lang().L("Break Mount");

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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
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
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		synchronized(this)
		{
			mount = null;
		}
	}

	protected synchronized void makeMountPermanent()
	{
		final Triad<MOB,Rideable,int[]> mnt;
		mnt = this.mount;
		if(mnt != null)
		{
			final MOB targM = mnt.first;
			final Rideable mR = mnt.second;
			mR.delBehavior(mR.fetchBehavior("Skill_Buck"));
			mR.delEffect(mR.fetchEffect("Skill_Buck"));
			if((targM.basePhyStats().rejuv() != 0)
			&&(targM.basePhyStats().rejuv() != PhyStats.NO_REJUV))
			{
				Room R = targM.location();
				if(R == null)
					R=CMLib.map().roomLocation(mR);
				if((R == null)&&(affected instanceof MOB))
					R=((MOB)affected).location();
				CMLib.threads().resumeTicking(targM, -1);
				if(R != null)
				{
					R.bringMobHere(targM, false);
					targM.killMeDead(false);
				}
				else
					targM.destroy();
			}
			else
				targM.destroy();
			if(affected instanceof MOB)
				((MOB)affected).tell(L("You have broken @x1.",mR.name(((MOB)affected))));
			this.mount = null;
		}
	}

	protected synchronized void undoMount()
	{
		final Triad<MOB,Rideable,int[]> mnt;
		mnt = this.mount;
		if(mnt != null)
		{
			final MOB targM = mnt.first;
			final Rideable mR = mnt.second;
			Room R = targM.location();
			if(R == null)
				R=CMLib.map().roomLocation(mR);
			if((R == null)&&(affected instanceof MOB))
				R=((MOB)affected).location();
			CMLib.threads().resumeTicking(targM, -1);
			if(R != null)
				R.bringMobHere(targM, false);
			mR.destroy();
			if(affected instanceof MOB)
				((MOB)affected).tell(L("You have failed to break @x1.",targM.name(((MOB)affected))));
			this.mount = null;
		}
	}

	@Override
	public void unInvoke()
	{
		undoMount();
		super.unInvoke();
	}

	protected Triad<MOB,Rideable,int[]> mount = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Triad<MOB,Rideable,int[]> mnt;
		synchronized(this)
		{
			mnt = this.mount;
		}
		if((mnt != null)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			super.helpProficiency(mob, 0);
			if(mnt.second.basePhyStats().isAmbiance("@NOAUTOBUCK"))
				this.makeMountPermanent();
			else
			if((mob.location() != CMLib.map().roomLocation(mnt.second))
			||(!CMLib.flags().isInTheGame(mob, true)))
			{
				mnt.second.delRider(mob);
				if(mob.riding()==mnt.second)
					mob.setRiding(null);
				this.undoMount();
			}
			else
			if(mob.riding() != mnt.second)
			{
				mnt.third[0] += 1;
				if(mnt.third[0] > 10)
					this.undoMount();
			}
			else
			{
				mnt.third[1] += 1;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_MOUNT)
		&&(msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Rideable))
		&&(!msg.source().isInCombat())
		&&(msg.source().riding()==null)
		&&(((MOB)msg.target()).isMonster())
		&&(CMLib.flags().isAnimalIntelligence((MOB)msg.target()))
		&&(CMLib.flags().isInTheGame((MOB)msg.target(), true)))
		{
			final Triad<MOB,Rideable,int[]> mnt;
			synchronized(this)
			{
				mnt = this.mount;
			}
			if(mnt != null)
				return true;
			final MOB targM = (MOB)msg.target();
			final PairList<String, Race> choices = CMLib.utensils().getFavoredMounts(msg.source());
			if((choices.containsSecond(targM.baseCharStats().getMyRace()))
				||(choices.containsFirst(targM.baseCharStats().getMyRace().racialCategory())))
			{
				final Room R = msg.source().location();
				if(proficiencyCheck(msg.source(),0,false)
				&&(R!=null))
				{
					final Rideable mR = (Rideable)CMClass.getMOB("GenRideable");
					for(final String stat : CMLib.coffeeMaker().getAllGenStats(targM))
						CMLib.coffeeMaker().setAnyGenStat(mR, stat, CMLib.coffeeMaker().getAnyGenStat(targM, stat));
					mR.setRideBasis(Basis.LAND_BASED);
					((MOB)mR).bringToLife(R, false);
					Ability A = ((MOB)mR).fetchAbility("Skill_Buck");
					if(A == null)
					{
						A=CMClass.getAbility("Skill_Buck");
						A.setProficiency(100);
						((MOB)mR).addAbility(A);
						A.autoInvocation((MOB)mR, true);
						mR.recoverPhyStats();
					}
					if(CMLib.flags().isInTheGame((MOB)mR, true))
					{
						synchronized(this)
						{
							mount = new Triad<MOB,Rideable,int[]>(targM,mR,new int[] {0,0});
							CMLib.threads().suspendTicking(targM, -1);
							R.delInhabitant(targM);
						}
						final Command mountC = CMClass.getCommand("Mount");
						final String mountNm = R.getContextName(mR);
						final List<String> V = new XVector<String>("MOUNT",mountNm);
						try
						{
							mountC.execute(msg.source(), V, MUDCmdProcessor.METAFLAG_FORCED);
						}
						catch (final IOException e)
						{
							Log.errOut(ID(),e);
						}
						if(msg.source().riding() != mR)
						{
							synchronized(this)
							{
								mount = null;
								CMLib.threads().resumeTicking(targM, -1);
								R.addInhabitant(targM);
							}
							mR.destroy();
							R.show(msg.source(), targM, CMMsg.MSG_NOISYMOVEMENT,
									L("<S-NAME> attempt(s) to mount <T-NAME> and fail(s)."));
						}
						return true;
					}
					else
					{
						mR.destroy();
						R.show(msg.source(), targM, CMMsg.MSG_NOISYMOVEMENT,
								L("<S-NAME> attempt(s) to mount <T-NAME> and fail(s)."));
						return false;
					}
				}
				else
				if(R != null)
				{
					R.show(msg.source(), targM, CMMsg.MSG_NOISYMOVEMENT,
							L("<S-NAME> attempt(s) to mount <T-NAME> and fail(s)."));
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

}
