package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Map extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Map";
	}

	private final static String	localizedName	= CMLib.lang().L("Make Maps");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Mapping)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected final Set<Room>	roomsMappedAlready	= new TreeSet<Room>(new Comparator<Room>(){
		@Override
		public int compare(Room o1, Room o2)
		{
			final String s1=CMLib.map().getExtendedRoomID(o1);
			final String s2=CMLib.map().getExtendedRoomID(o2);
			return s1.compareTo(s2);
		}
		
	});
	protected Item	map				= null;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell(L("You stop mapping."));
		map=null;
	}

	protected boolean isTheMapMsg(final MOB mob, final MOB srcM)
	{
		if((mob!=null)&&(srcM!=null))
			return (srcM == mob);
		return false;
	}
	
	protected Room getCurrentRoom(final MOB mob)
	{
		return mob.location();
	}

	protected String getMapClass()
	{
		return "BardMap";
	}

	protected boolean doExtraChecks(final MOB mob)
	{
		return true;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Item map = this.map;
			if((map != null)&&(mob!=null))
			{
				if((map.owner()==null)
				||(map.owner()!=mob))
					unInvoke();
				else
				if((isTheMapMsg(mob,msg.source()))
				&&(msg.targetMinor()==CMMsg.TYP_ENTER)
				&&(msg.target() instanceof Room)
				&&(CMLib.flags().canBeSeenBy(msg.target(),mob))
				&&(!roomsMappedAlready.contains(msg.target()))
				&&(!CMath.bset(((Room)msg.target()).phyStats().sensesMask(),PhyStats.SENSE_ROOMUNMAPPABLE)))
				{
					roomsMappedAlready.add((Room)msg.target());
					final Room R=mob.location();
					if(R!=null)
						R.send(mob, CMClass.getMsg(mob,map,this,CMMsg.MSG_WROTE, null, CMMsg.MSG_WROTE, CMLib.map().getExtendedRoomID((Room)msg.target()),-1,null));
					map.setReadableText(map.readableText()+";"+CMLib.map().getExtendedRoomID((Room)msg.target()));
					if(map instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
						((com.planet_ink.coffee_mud.Items.interfaces.RoomMap)map).doMapArea();
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Ability A=mob.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			return true;
		}
		if(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell(L("You are too stupid to actually make a map."));
			return false;
		}

		if(!doExtraChecks(mob))
			return false;

		final Item target=getTarget(mob,null,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		Item item=target;
		if(!item.isReadable())
		{
			mob.tell(L("You can't map on that."));
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell(L("You can't map on a scroll."));
			return false;
		}

		if(item instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
		{
			if(!item.ID().equals(getMapClass()))
			{
				mob.tell(L("There's no more room to add to that map."));
				return false;
			}
		}
		else
		if(item.readableText().length()>0)
		{
			mob.tell(L("There's no more room to map on that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_WRITE,L("<S-NAME> start(s) mapping on <T-NAMESELF>."),CMMsg.MSG_WRITE,";",CMMsg.MSG_WRITE,L("<S-NAME> start(s) mapping on <T-NAMESELF>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!item.ID().equals(getMapClass()))
				{
					final Item B=CMClass.getItem(getMapClass());
					B.setContainer(item.container());
					B.setName(item.Name());
					B.setBasePhyStats(item.basePhyStats());
					B.setBaseValue(item.baseGoldValue()*2);
					B.setDescription(item.description());
					B.setDisplayText(item.displayText());
					B.setMaterial(item.material());
					B.setRawLogicalAnd(item.rawLogicalAnd());
					B.setRawProperLocationBitmap(item.rawProperLocationBitmap());
					B.setSecretIdentity(item.secretIdentity());
					CMLib.flags().setRemovable(B,CMLib.flags().isRemovable(item));
					B.setUsesRemaining(item.usesRemaining());
					item.destroy();
					mob.addItem(B);
					item=B;
				}

				roomsMappedAlready.clear();
				final List<String> oldRoomIDs=CMParms.parseSemicolons(item.readableText(), true);
				for(final String roomID : oldRoomIDs)
				{
					if(roomID.length()>0)
					{
						final Room R=CMLib.map().getRoom(roomID);
						if(R!=null)
							roomsMappedAlready.add(R);
					}
				}

				map=item;
				final Room firstRoom=getCurrentRoom(mob);
				if((firstRoom!=null)
				&&(!CMath.bset(firstRoom.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNMAPPABLE)))
				{
					final String firstRoomID=CMLib.map().getExtendedRoomID(firstRoom);
					if((!roomsMappedAlready.contains(firstRoom))&&(firstRoomID.length()>0))
					{
						roomsMappedAlready.add(firstRoom);
						oldRoomIDs.add(firstRoomID);
						map.setReadableText(map.readableText()+";"+firstRoomID);
						if(map instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
							((com.planet_ink.coffee_mud.Items.interfaces.RoomMap)map).doMapArea();
					}
				}
				beneficialAffect(mob,mob,asLevel,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<S-NAME> attempt(s) to start mapping on <T-NAMESELF>, but mess(es) up."));
		return success;
	}

}
