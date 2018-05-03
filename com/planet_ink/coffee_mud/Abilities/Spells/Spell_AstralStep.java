package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Spell_AstralStep extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_AstralStep";
	}

	private final static String	localizedName	= CMLib.lang().L("Astral Step");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Astral Step)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectedStats)
	{
		super.affectPhyStats(affected,affectedStats);
		affectedStats.setDisposition(affectedStats.disposition()|PhyStats.IS_INVISIBLE);
		affectedStats.setDisposition(affectedStats.disposition()|PhyStats.IS_SNEAKING);
		affectedStats.setDisposition(affectedStats.disposition()|PhyStats.IS_NOT_SEEN);
		affectedStats.setHeight(-1);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> reappear(s) from the Astral Plane."));
		}

		super.unInvoke();
	}

	protected int highestLevelHere(final MOB mob, final Room R, int highestLevel)
	{
		for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&& M.isMonster()
			&&(M.amFollowing()==null)
			&&(M.phyStats().level()>highestLevel))
				highestLevel=M.phyStats().level();
		}
		for(Enumeration<Item> i=R.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I!=null)
			&&(I.phyStats().level()>highestLevel))
				highestLevel = I.phyStats().level();
		}
		final LandTitle T=CMLib.law().getLandTitle(R);
		if((T!=null)
		&&(!CMLib.law().doesHavePriviledgesHere(mob, R)))
		{
			final MOB M = CMLib.law().getPropertyOwner(T);
			if((M!=null)
			&&(M.phyStats().level()>highestLevel))
				highestLevel = M.phyStats().level();
		}
		return highestLevel;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if((auto||mob.isMonster())&&((commands.size()<1)||((commands.get(0)).equals(mob.name()))))
		{
			commands.clear();
			int theDir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Exit E=mob.location().getExitInDir(d);
				if((E!=null)
				&&(!E.isOpen()))
				{
					theDir=d;
					break;
				}
			}
			if(theDir>=0)
				commands.add(CMLib.directions().getDirectionName(theDir));
		}
		if(commands.size()==0)
		{
			mob.tell(L("Pass which direction?!"));
			return false;
		}
		List<Integer> dirs = new LinkedList<Integer>();
		for(String dirName : commands)
		{
			final int dirCode=CMLib.directions().getGoodDirectionCode(dirName);
			if(dirCode<0)
			{
				mob.tell(L("@x1 is not a valid direction?!",dirName));
				return false;
			}
			dirs.add(Integer.valueOf(dirCode));
		}
		
		final int maxDirs = 1 + super.getXLEVELLevel(mob);
		if(dirs.size() > maxDirs)
		{
			mob.tell(L("You can only step in @x1 direction(s).",""+maxDirs));
			return false;
		}

		if(dirs.size()==0)
		{
			mob.tell(L("Pass which direction?!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		boolean success=false;
		for(int dirDex = 0; dirDex < dirs.size(); dirDex++)
		{
			final Room R=mob.location();
			int dirCode = dirs.get(dirDex).intValue();
			final Exit exit=R.getExitInDir(dirCode);
			final Room room=R.getRoomInDir(dirCode);

			if((exit==null)||(room==null)||(!CMLib.flags().canBeSeenBy(exit,mob)))
			{
				mob.tell(L("You can't see anywhere to pass that way."));
				return false;
			}
			int adjustment = 0;
			if(!auto)
			{
				int highestLevel = exit.phyStats().level();
				highestLevel = highestLevelHere(mob,R,highestLevel);
				highestLevel = highestLevelHere(mob,room,highestLevel);
				adjustment = (mob.phyStats().level() - highestLevel) * 10;
				if(adjustment > 0)
					adjustment = 0;
			}

			success=proficiencyCheck(mob,adjustment,auto);
	
			if((!success)
			||(mob.fetchEffect(ID())!=null))
			{
				beneficialVisualFizzle(mob,null,L("<S-NAME> step(s) @x1, but go(es) no further.",CMLib.directions().getDirectionName(dirCode)));
				return false;
			}
			else
			if(auto)
			{
				final CMMsg msg=CMClass.getMsg(mob,null,null,verbalCastCode(mob,null,auto),L("^S<S-NAME> shimmer(s) and disappear(s) through the astral plane.^?"));
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					beneficialAffect(mob,mob,asLevel,5);
					mob.recoverPhyStats();
				}
				else
					return false;
			}
			else
			{
				final CMMsg msg=CMClass.getMsg(mob,null,null,verbalCastCode(mob,null,auto),L("^S<S-NAME> shimmer(s) and step(s) @x1 through the astral plane.^?",CMLib.directions().getDirectionName(dirCode)));
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					mob.tell(L("\n\r\n\r"));
					if(mob.fetchEffect(ID())==null)
					{
						final Ability A=(Ability)this.copyOf();
						A.setSavable(false);
						try
						{
							mob.addEffect(A);
							mob.recoverPhyStats();
							CMLib.tracking().walk(mob,dirCode,false,false);
						}
						finally
						{
							mob.delEffect(A);
						}
					}
					else
						CMLib.tracking().walk(mob,dirCode,false,false);
					mob.recoverPhyStats();
				}
				else
					return false;
			}
		}
		return success;
	}
}
