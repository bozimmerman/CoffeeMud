package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Herding extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Herding";
	}

	public Herding()
	{
		super();
		displayText=L("You are herding...");
		verb=L("herding");
	}

	private final static String localizedName = CMLib.lang().L("Herding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HERD","HERDING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected List<MOB>	herd			= null;
	protected int		herdDir			= -1;
	protected boolean	failed			= false;
	protected String	foundShortName	= "";

	protected void playerRides(final Rider M, final Set<Rider> rides)
	{
		if((M!=null)
		&&(!rides.contains(M)))
		{
			rides.add(M);
			if(M.riding() != null)
				playerRides(M.riding(),rides);
			if(M instanceof Rideable)
			{
				final Rideable R = (Rideable)M;
				for(final Enumeration<Rider> r = R.riders();r.hasMoreElements();)
					playerRides(r.nextElement(), rides);
			}
		}
	}

	protected int playerGroupWeight(final Rider M)
	{
		final Set<Rider> set = new HashSet<Rider>();
		playerRides(M,set);
		int weight = 0;
		for(final Rider R : set)
			weight += R.phyStats().weight();
		return weight;
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(activityRoom == null)
				activityRoom=mob.location();
			if((mob.riding()==null)
			||(mob.location()!=activityRoom))
			{
				unInvoke();
				return false;
			}
			if((tickUp==0)
			&&(herdDir >= 0))
			{
				for(final MOB M : herd)
				{
					if(M.location()==activityRoom)
						CMLib.tracking().walk(M, herdDir, false, false, false, false);
					CMLib.tracking().markToWanderHomeLater(M, (int)CMProps.getTicksPerHour());
				}
				if(mob.location()==activityRoom)
					CMLib.tracking().walk(mob, herdDir, false, false, false, false);
				unInvoke();
				return false;
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING|Ability.FLAG_TRANSPORTING;
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<2)
		{
			super.commonTelL(mob,"Herd which race in what direction?");
			return false;
		}

		herd=new Vector<MOB>();
		verb=L("herding");
		activityRoom=null;
		final Room R=mob.location();
		if(R==null)
			return false;

		if(mob.riding() == null)
		{
			super.commonTelL(mob,"You need to be mounted to do this.");
			return false;
		}

		final String raceName = CMParms.combine(commands,0,commands.size()-1);
		MOB exampleM = null;
		// first look for simple match
		for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
		{
			final MOB M = m.nextElement();
			if((M.isMonster())
			&&(CMLib.flags().isAnAnimal(M))
			&&(CMLib.flags().canBeSeenBy(M, mob))
			&&(mob.riding()!=M)
			&&((raceName.toLowerCase().startsWith(M.charStats().getMyRace().name().toLowerCase()))
				||CMLib.english().containsString(M.charStats().getMyRace().name(),raceName)))
			{
				exampleM = M;
				break;
			}
		}
		if(exampleM == null) // if simple match fails, do name match
		{
			exampleM = R.fetchInhabitant(raceName);
			if((exampleM == null)
			||(!exampleM.isMonster())
			||(!CMLib.flags().canBeSeenBy(exampleM, mob))
			||(mob.riding()==exampleM)
			||(!CMLib.flags().isAnAnimal(exampleM)))
				exampleM = null;
		}
		if(exampleM == null)
		{
			super.commonTelL(mob,"You don't see any '@x1' you can herd here.",raceName);
			return false;
		}

		for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
		{
			final MOB M = m.nextElement();
			if((M.isMonster())
			&&(CMLib.flags().isAnAnimal(M))
			&&(CMLib.flags().canBeSeenBy(M, mob))
			&&(mob.riding()!=M)
			&&(M.charStats().getMyRace()==exampleM.charStats().getMyRace()))
				herd.add(M);
		}

		final String dirName = commands.get(commands.size()-1);
		final int direction = CMLib.directions().getDirectionCode(dirName);
		if(direction < 0)
		{
			super.commonTelL(mob,"'@x1' is not a proper direction.",dirName);
			return false;
		}

		final Exit nE = R.getExitInDir(direction);
		final Room nR = R.getRoomInDir(direction);
		if((nE==null)
		||(nR==null)
		||(!nE.isOpen()))
		{
			super.commonTell(mob, "You can't herd them that way.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		this.failed = proficiencyCheck(mob,0,auto);
		final Race herdRace = exampleM.charStats().getMyRace();
		final double avgWeight = herdRace.lightestWeight()+(herdRace.weightVariance()/2);
		final double totalWeight = this.playerGroupWeight(mob);
		final int total = Math.max(1, (int)Math.round(totalWeight / avgWeight * (1.0+super.getXLEVELLevel(mob))));

		final String pluralRaces = CMLib.english().makePlural(exampleM.charStats().getMyRace().name());
		final String finalDirName = CMLib.directions().getDirectionName(direction);
		final CMMsg msg=CMClass.getMsg(mob,exampleM,this,getActivityMessageType(),
				L("<S-NAME> start(s) herding @x1 @x2.",pluralRaces,finalDirName));
		int ct = 0;
		for(final Iterator<MOB> m = herd.iterator();m.hasNext();)
		{
			final MOB M = m.next();
			msg.setTarget(M);
			if((++ct<=total)
			&&R.okMessage(mob,msg))
				R.send(mob,msg);
			else
				m.remove();
			msg.setSourceMessage(null);
			msg.setTargetMessage(null);
			msg.setOthersMessage(null);
		}
		if(herd.size()==0)
		{
			commonTelL(mob,"No one seems to be paying attention to you.");
			return false;
		}
		verb=L("herding @x1 @x2",pluralRaces,finalDirName);
		activityRoom=R;
		herdDir = direction;
		final int duration=herd.size();
		beneficialAffect(mob,mob,asLevel,duration);
		return true;
	}
}
