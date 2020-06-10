package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.StdThinInstance;
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
public class Chant_PlanarLink extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_PlanarLink";
	}

	private final static String localizedName = CMLib.lang().L("Planar Link");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = "(Planar Link @x1)";

	protected PlanarAbility	boundPlane		= null;
	protected String		localAreaName	= null;

	@Override
	public String displayText()
	{
		if((boundPlane != null)&&(localAreaName!=null))
			return CMLib.lang().L(localizedStaticDisplay,boundPlane.getPlanarName()+" "+localAreaName);
		else
			return CMLib.lang().L(localizedStaticDisplay,"?");
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_COSMOLOGY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		final Room room=((MOB)affected).location();
		if(canBeUninvoked())
		{
			if((boundPlane != null)&&(localAreaName!=null))
				mob.tell(L("Your link to the plane of @x1 dims.",boundPlane.getPlanarName()+" "+localAreaName));
			else
				mob.tell(L("Your link to the planes dims."));
		}
		super.unInvoke();
		room.recoverRoomStats();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(this.boundPlane != null)
			this.boundPlane.setStat("TICKDOWN", ""+this.tickDown);
		return true;
	}

	protected String getStrippedRoomID(final String roomID)
	{
		final int x=roomID.indexOf('#');
		if(x<0)
			return null;
		return roomID.substring(x);
	}

	protected String convertToMyArea(final String areaName, final String roomID)
	{
		final String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null)
			return null;
		return areaName+strippedID;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.tool() instanceof PlanarAbility)
		&&(msg.target() instanceof Room)
		&&(affected instanceof MOB)
		&&(((MOB)affected).getGroupMembers(new HashSet<MOB>()).contains(msg.source()))
		&&(this.boundPlane != null)
		&&(this.boundPlane.affecting() instanceof Area))
		{
			final String boundPlane = this.boundPlane.getPlanarName();
			final PlanarAbility prospectivePlaneA=(PlanarAbility)msg.tool();
			final Room prospectivePlaneR=(Room)msg.target();
			final Area prospectiveArea=prospectivePlaneR.getArea();
			if((boundPlane.equalsIgnoreCase(prospectivePlaneA.getPlanarName()))
			||((""+prospectiveArea.getBlurbFlag("PLANEOFEXISTENCE")).toUpperCase().indexOf(boundPlane.toUpperCase())>=0))
			{
				final Room oldRoom = prospectivePlaneA.getOldRoom();
				final Area mA=(oldRoom!=null)?oldRoom.getArea():null;
				final Area redirectA=(Area)this.boundPlane.affecting();
				if((mA!=null)
				&&(mA.Name().equals(localAreaName)))
				{
					final Room R=redirectA.getRoom(convertToMyArea(redirectA.Name(), CMLib.map().getExtendedRoomID((Room)msg.target())));
					if(R!=null)
						msg.setTarget(R);
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			target.tell(L("You already have a planar link."));
			return false;
		}

		final Room R=target.location();
		if(R==null)
			return false;
		final Area A=R.getArea();
		PlanarAbility planeA=null;
		for(final Enumeration<Ability> a=A.effects();a.hasMoreElements();)
		{
			final Ability aA=a.nextElement();
			if(aA instanceof PlanarAbility)
				planeA=(PlanarAbility)aA;
		}

		if((CMLib.flags().getPlaneOfExistence(target.location())==null)
		||(planeA==null))
		{
			mob.tell(L("This chant requires being on another plane of existence."));
			return false;
		}

		if(CMLib.map().getExtendedRoomID(R).toLowerCase().indexOf(A.Name().toLowerCase())!=0)
		{
			mob.tell(L("For some reason, this magic won't seem to work in this exact place.  Try somewhere nearby."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			return beneficialWordsFizzle(mob,mob.location(),L("<S-NAME> chant(s) for a planar link, but fail(s)."));
		}

		final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) a planar link!"):L("^S<S-NAME> chant(s), creating a link between <S-HIM-HERSELF> and this plane!^?"));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final Chant_PlanarLink linkA = (Chant_PlanarLink)beneficialAffect(mob,target,asLevel,(int)
															(CMProps.getTicksPerHour()
															+(CMProps.getTicksPerMinute()*adjustedLevel(mob,0))
															+(30*CMProps.getTicksPerMinute()*super.getXLEVELLevel(mob))
															));
			if(linkA!=null)
			{
				linkA.boundPlane = planeA;
				final Room oldRoom = planeA.getOldRoom();
				if(oldRoom != null)
					linkA.localAreaName = oldRoom.getArea().Name();
				else
					linkA.localAreaName = "Unknown";
			}
			target.location().recoverRoomStats(); // attempt to handle followers
		}

		return success;
	}
}
