package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2016-2016 Bo Zimmerman

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


public class Chant_Capsize extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Capsize";
	}

	private final static String	localizedName	= CMLib.lang().L("Capsize");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 50;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	public List<Item> getAllTheStuff(Item I)
	{
		final List<Item> items = new ArrayList<Item>();
		if(I instanceof Container)
			items.addAll(((Container)I).getContents());
		if(I instanceof BoardableShip)
		{
			Area A=((BoardableShip)I).getShipArea();
			if(A!=null)
			{
				for(Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null && (R.numItems() >0) && (R.domainType()&Room.INDOORS)==0)
					{
						for(Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							Item I2=i.nextElement();
							if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isGettable(I2)))
							{
								items.add(I2);
							}
						}
					}
				}
			}
		}
		return items;
	}
	
	protected MOB getHighestLevel(MOB casterM, Item I)
	{
		int highestLevelPC=0;
		int highestLevelNPC=0;
		MOB highestLevelPCM=null;
		MOB highestLevelNPCM=null;
		Set<MOB> grp = casterM.getGroupMembers(new HashSet<MOB>());
		if(I instanceof Rideable)
		{
			for(Enumeration<Rider> r=((Rideable)I).riders();r.hasMoreElements();)
			{
				Rider R=r.nextElement();
				if((R instanceof MOB)&&(!grp.contains(R)))
				{
					MOB M=(MOB)R;
					if(M.isMonster() && (M.phyStats().level() > highestLevelNPC))
					{
						highestLevelNPC = M.phyStats().level();
						highestLevelNPCM = M;
					}
					
					if(M.isPlayer() && (M.phyStats().level() > highestLevelPC))
					{
						highestLevelPC = M.phyStats().level();
						highestLevelPCM = M;
					}
						
				}
			}
		}
		if(I instanceof BoardableShip)
		{
			Area A=((BoardableShip)I).getShipArea();
			if(A!=null)
			{
				for(Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null && (R.numInhabitants() >0))
					{
						for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
						{
							MOB M=m.nextElement();
							if(!grp.contains(M))
							{
								if(M.isMonster() && (M.phyStats().level() > highestLevelNPC))
								{
									highestLevelNPC = M.phyStats().level();
									highestLevelNPCM = M;
								}
								
								if(M.isPlayer() && (M.phyStats().level() > highestLevelPC))
								{
									highestLevelPC = M.phyStats().level();
									highestLevelPCM = M;
								}
							}
						}
					}
				}
			}
		}
		if(highestLevelPC > 0)
			return highestLevelPCM;
		return highestLevelNPCM;
	}
	
	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof BoardableShip))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((R.domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be on the water for this chant to work."));
			return false;
		}
		if((R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
		{
			mob.tell(L("This chant does not work here."));
			return false;
		}
		Item target=getTarget(mob,R,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;
		
		if(target instanceof BoardableShip)
		{ //ok
		}
		else
		if((target instanceof Rideable) && (((Rideable)target).rideBasis() == Rideable.RIDEABLE_WATER))
		{ //ok
		}
		else
		{
			mob.tell(L("That's not a boat!"));
			return false;
		}
		
		if(CMLib.flags().isFlying(target))
		{
			mob.tell(L("This chant would have no effect on @x1!",target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			MOB M=this.getHighestLevel(mob, target);
			if(M!=null)
			{
				int chanceToFail = M.charStats().getSave(CharStats.STAT_SAVE_JUSTICE);
				if (chanceToFail > Integer.MIN_VALUE)
				{
					final int diff = (M.phyStats().level() - mob.phyStats().level());
					final int diffSign = diff < 0 ? -1 : 1;
					chanceToFail += (diffSign * (diff * diff));
					if (chanceToFail < 5)
						chanceToFail = 5;
					else
					if (chanceToFail > 95)
						chanceToFail = 95;
		
					if (CMLib.dice().rollPercentage() < chanceToFail)
					{
						success = false;
					}
				}
			}
		}
		
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L(""):L("^S<S-NAME> chant(s) to <T-NAMESELF>, pummeling it with a massive wave.^?"));
			if(R.okMessage(mob,msg))
			{
				List<Item> items = this.getAllTheStuff(target);
				R.send(mob,msg);
				for(Item I : items)
				{
					I.setContainer(null);
					if(I.owner() != R)
						R.moveItemTo(I);
				}
				R.recoverRoomStats();
				R.show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> momentarily capsizes!");
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
