package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Spell_PlanarWard extends Spell
{
	@Override
	public String ID()
	{
		return "Spell_PlanarWard";
	}

	private final static String localizedName = CMLib.lang().L("Planar Ward");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Ward)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	protected int quality=Ability.QUALITY_INDIFFERENT;

	@Override
	public int abstractQuality()
	{
		return quality;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your planar ward dissipates."));

		super.unInvoke();

	}

	protected final static PairList<String,String> mixRaces=new PairVector<String,String>();

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if((msg.tool() instanceof Ability)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_COSMOLOGY)
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
			&&(!CMSecurity.isASysOp(msg.source())))
			{
				if((msg.source().location()!=null)&&(msg.source().location()!=R))
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				R.showHappens(CMMsg.MSG_OK_VISUAL,L("Magical energy fizzles and is absorbed into the air!"));
				return false;
			}
			else
			if((msg.target()==R)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDROOMS))
			&&(!CMLib.law().doesHavePriviledgesHere(msg.source(), R)))
			{
				final String startPlane=CMLib.flags().getPlaneOfExistence(msg.source().getStartRoom());
				if(startPlane != null)
				{
					msg.source().tell(L("You don't feel able to move in that direction."));
					return false;
				}
				if(mixRaces.size()==0)
				{
					synchronized(mixRaces)
					{
						if(mixRaces.size()==0)
						{
							final PlanarAbility A=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
							for(final String planeKey : A.getAllPlaneKeys())
							{
								final Map<String,String> planeVars=A.getPlanarVars(planeKey);
								if(planeVars.containsKey(PlanarAbility.PlanarVar.MIXRACE.toString()))
								{
									final String mixRace = planeVars.get(PlanarVar.MIXRACE.toString());
									final Race firstR=CMClass.getRace(mixRace);
									if(firstR!=null)
										mixRaces.add(firstR.ID(),firstR.name());
								}
							}
							if(mixRaces.size()==0)
								mixRaces.add("PlanarRaceDoesntExist","PlanarRaceDoesntExist");
						}
					}
				}
				final Race sR = msg.source().baseCharStats().getMyRace();
				for(final Pair<String,String> r : mixRaces)
				{
					if(((sR.ID().indexOf(r.first)>=0) && (!sR.ID().equals(r.first)))
					||((sR.name().indexOf(r.second)>=0) && (!sR.ID().equals(r.second))))
					{
						msg.source().tell(L("You don't feel able to move in that direction."));
						return false;
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Physical target=null;
		if(commands.size()>0)
		{
			final String s=CMParms.combine(commands,0);
			if(s.equalsIgnoreCase("room"))
				target=mob.location();
			else
			if(s.equalsIgnoreCase("here"))
				target=mob.location();
			else
			if(CMLib.english().containsString(mob.location().ID(),s)
			||CMLib.english().containsString(mob.location().name(),s)
			||CMLib.english().containsString(mob.location().displayText(),s))
				target=mob.location();
		}
		else
			target=mob.location();
		if(target==null)
			return false;
		if((target instanceof Room)&&(target.fetchEffect(ID())!=null))
		{
			mob.tell(L("This place is already under a planar ward."));
			return false;
		}
		if(!(target instanceof Room))
		{
			mob.tell(L("This magic must be used on a place."));
			return false;
		}
		if(!CMLib.law().doesOwnThisLand(mob,((Room)target)))
		{
			mob.tell(L("You are not permitted to use this magic here."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?L("<T-NAME> seem(s) cosmically protected."):L("^S<S-NAME> invoke(s) a planar ward upon <T-NAMESELF>.^?"));
			if(target instanceof Room)
				quality=Ability.QUALITY_MALICIOUS;
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,(int)((10+super.adjustedLevel(mob, asLevel))*CMProps.getTicksPerDay()));
				if(target instanceof Room)
				{
					final Spell_PlanarWard A=(Spell_PlanarWard)target.fetchEffect(ID());
					if(A!=null)
						A.quality=Ability.QUALITY_MALICIOUS;
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to invoke a planar ward, but fail(s)."));
		quality=Ability.QUALITY_INDIFFERENT;

		return success;
	}
}
