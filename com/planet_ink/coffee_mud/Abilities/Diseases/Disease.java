package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Disease extends StdAbility implements DiseaseAffect
{
	@Override
	public String ID()
	{
		return "Disease";
	}

	private final static String	localizedName	= CMLib.lang().L("Disease");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(a disease)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS | CAN_ITEMS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DISEASE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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
	public int classificationCode()
	{
		return Ability.ACODE_DISEASE;
	}

	@Override
	public boolean isMalicious()
	{
		return true;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Suffering the effects of " + name();
	}

	protected int DISEASE_TICKS()
	{
		return 48;
	}

	protected int DISEASE_DELAY()
	{
		return 5;
	}

	protected String DISEASE_DONE()
	{
		return L("Your disease has run its course.");
	}

	protected String DISEASE_START()
	{
		return L("^G<S-NAME> come(s) down with a disease.^?");
	}

	protected String DISEASE_AFFECT()
	{
		return L("<S-NAME> ache(s) and groan(s).");
	}

	protected boolean DISEASE_REQSEE()
	{
		return false;
	}

	@Override
	public int spreadBitmap()
	{
		return 0;
	}

	@Override
	public int abilityCode()
	{
		return spreadBitmap();
	}

	@Override
	public int difficultyLevel()
	{
		return 0;
	}

	protected boolean	processing	= false;

	protected int		diseaseTick	= DISEASE_DELAY();

	protected boolean catchIt(MOB mob, Physical target)
	{
		MOB diseased=invoker;
		if(invoker==target)
			return true;
		if(diseased==null)
			diseased=mob;
		if((diseased==null)&&(target instanceof MOB))
			diseased=(MOB)target;

		if((target!=null)
		&&(diseased!=null)
		&&(target.fetchEffect(ID())==null)
		&&((!DISEASE_REQSEE())||((target instanceof MOB)&&(CMLib.flags().canBeSeenBy(diseased,(MOB)target)))))
		{
			if(target instanceof MOB)
			{
				final MOB targetMOB=(MOB)target;
				if((CMLib.dice().rollPercentage()>targetMOB.charStats().getSave(CharStats.STAT_SAVE_DISEASE))
				&&(targetMOB.location()!=null))
				{
					final MOB following=targetMOB.amFollowing();
					final boolean doMe=invoke(diseased,targetMOB,true,0);
					if(targetMOB.amFollowing()!=following)
						targetMOB.setFollowing(following);
					return doMe;
				}
				else
					spreadImmunity(targetMOB);
			}
			else
			{
				maliciousAffect(diseased,target,0,DISEASE_TICKS(),-1);
				return true;
			}
		}
		return false;
	}

	protected boolean catchIt(MOB mob)
	{
		if(mob==null)
			return false;
		if(mob.location()==null)
			return false;
		final MOB target=mob.location().fetchRandomInhabitant();
		return catchIt(mob,target);
	}

	@Override
	public void unInvoke()
	{
		if(affected==null)
			return;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;

			super.unInvoke();
			if(canBeUninvoked())
			{
				if(!mob.amDead())
					spreadImmunity(mob);
				mob.tell(mob,null,this,DISEASE_DONE());
			}
		}
		else
			super.unInvoke();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_DAMAGE))
			&&(msg.amISource(mob))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool() instanceof Weapon)
			&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
			&&(msg.source().fetchWieldedItem()==null)
			&&(msg.target() instanceof MOB)
			&&(msg.target()!=msg.source())
			&&(CMLib.dice().rollPercentage()>(((MOB)msg.target()).charStats().getSave(CharStats.STAT_SAVE_DISEASE)+70)))
				catchIt(mob,(MOB)msg.target());
			else
			if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONTACT))
			&&(msg.amISource(mob)||msg.amITarget(mob))
			&&(msg.target() instanceof MOB)
			&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE)||CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS))
			&&((msg.tool()==null)
				||((msg.tool() instanceof Weapon)
					&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL))))
				catchIt(mob,msg.amITarget(mob)?msg.source():(MOB)msg.target());
			else
			if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_STD))
			&&((msg.amITarget(mob))||(msg.amISource(mob)))
			&&(msg.tool() instanceof Social)
			&&(msg.target() instanceof MOB)
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>")))
				catchIt(mob,msg.amITarget(mob)?msg.source():(MOB)msg.target());
		}
		else
		if(affected instanceof Item)
		{
			try
			{
				if(!processing)
				{
					final Item myItem=(Item)affected;
					if(myItem.owner()==null)
						return;
					processing=true;
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_DRINK:
						if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONSUMPTION))
						||(CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONTACT)))
						{
							if((myItem instanceof Drink)
							&&(msg.amITarget(myItem)))
								catchIt(msg.source(),msg.source());
						}
						break;
					case CMMsg.TYP_EAT:
						if((CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONSUMPTION))
						||(CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONTACT)))
						{
	
							if((myItem instanceof Food)
							&&(msg.amITarget(myItem)))
								catchIt(msg.source(),msg.source());
						}
						break;
					case CMMsg.TYP_GET:
					case CMMsg.TYP_PUSH:
					case CMMsg.TYP_PULL:
						if(CMath.bset(spreadBitmap(),DiseaseAffect.SPREAD_CONTACT))
						{
							if((!(myItem instanceof Drink))
							  &&(!(myItem instanceof Food))
							  &&(msg.amITarget(myItem)))
								catchIt(msg.source(),msg.source());
						}
						break;
					}
				}
			}
			finally
			{
				processing=false;
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final MOB mvictim;
			synchronized(mob)
			{
				mvictim=mob.getVictim();
			}
			final MOB tvictim;
			synchronized(target)
			{
				tvictim=target.getVictim();
			}
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_HANDS|(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.TYP_DISEASE,"");
			final Room R=target.location();
			if((R!=null)&&(R.okMessage(target,msg)))
			{
				R.send(target,msg);
				if(msg.value()<=0)
				{
					R.show(target,null,CMMsg.MSG_OK_VISUAL,DISEASE_START());
					success=maliciousAffect(mob,target,asLevel,DISEASE_TICKS(),-1)!=null;
				}
				else
					spreadImmunity(target);
			}
			if(!isMalicious())
			{
				final MOB newmvictim;
				synchronized(mob)
				{
					newmvictim=mob.getVictim();
				}
				final MOB newtvictim;
				synchronized(target)
				{
					newtvictim=target.getVictim();
				}
				if((mvictim==null)&&(newmvictim==target))
				{
					mob.setVictim(null);
					mob.makePeace(true);
				}
				if((tvictim==null)&&(newtvictim==mob))
				{
					target.setVictim(null);
					target.makePeace(true);
				}
			}
			else
			if(auto)
			{
				if(mob.getVictim()!=mvictim)
					mob.setVictim(mvictim);
				if(target.getVictim()!=tvictim)
					target.setVictim(tvictim);
			}
		}
		return success;
	}
}
