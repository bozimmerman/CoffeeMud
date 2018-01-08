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
   Copyright 2002-2018 Bo Zimmerman

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

public class Chant_Bury extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_Bury";
	}

	private final static String	localizedName	= CMLib.lang().L("Earthfeed");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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

	public static Item getBody(Room R)
	{
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I instanceof DeadBody)
			&&(!((DeadBody)I).isPlayerCorpse())
			&&(((DeadBody)I).getMobName().length()>0))
				return I;
		}
		return null;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(((R.domainType()&Room.INDOORS)>0)
		&&(R.domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((R.getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if((R.domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell(L("This chant does not work here."));
			return false;
		}
		Item hole=R.findItem("HoleInTheGround");
		if((hole!=null)&&(!hole.text().equalsIgnoreCase(mob.Name())))
		{
			mob.tell(L("This chant will not work on this previously used burial ground."));
			return false;
		}
		Item target=null;
		if((commands.size()==0)&&(!auto)&&(givenTarget==null))
			target=getBody(R);
		if(target==null)
			target=getTarget(mob,R,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof DeadBody))
		||(((DeadBody)target).rawSecretIdentity().toUpperCase().indexOf("FAKE")>=0))
		{
			mob.tell(L("You may only feed the dead to the earth."));
			return false;
		}

		if((((DeadBody)target).isPlayerCorpse())
		&&(!((DeadBody)target).getMobName().equals(mob.Name()))
		&&(((DeadBody)target).hasContent()))
		{
			mob.tell(L("You are not allowed to bury that corpse."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> bur(ys) <T-HIM-HERSELF>."):L("^S<S-NAME> chant(s) to <T-NAMESELF>, returning dust to dust.^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(CMLib.flags().isNeutral(mob))
					mob.curState().adjMana(3*target.phyStats().level()+(3*target.phyStats().level()*getXLEVELLevel(mob)),mob.maxState());
				if(hole==null)
				{
					final CMMsg holeMsg=CMClass.getMsg(mob, R,null,CMMsg.MSG_DIG|CMMsg.MASK_ALWAYS, null);
					R.send(mob,holeMsg);
					hole=R.findItem("HoleInTheGround");
				}
				hole.basePhyStats().setDisposition(hole.basePhyStats().disposition()|PhyStats.IS_HIDDEN);
				hole.recoverPhyStats();
				if(!R.isContent(target))
					R.moveItemTo(hole, Expire.Player_Drop);
				else
					target.setContainer((Container)hole);
				CMLib.flags().setGettable(target,false);
				R.recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
