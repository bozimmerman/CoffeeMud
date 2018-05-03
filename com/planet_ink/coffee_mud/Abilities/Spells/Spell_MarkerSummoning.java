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

public class Spell_MarkerSummoning extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MarkerSummoning";
	}

	private final static String localizedName = CMLib.lang().L("Marker Summoning");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room oldRoom=null;
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
				{
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)
						&&(A.invoker()==mob))
						{
							if(A.ID().equals("Spell_SummonMarker"))
							{
								oldRoom=R;
								break;
							}
						}
					}
				}
				if(oldRoom!=null)
					break;
			}
		}
		catch(final NoSuchElementException nse)
		{
		}
		if(oldRoom==null)
		{
			mob.tell(L("You can't seem to focus on your marker.  Are you sure you've already summoned it?"));
			return false;
		}
		final Room newRoom=mob.location();
		if(oldRoom==newRoom)
		{
			mob.tell(L("But your marker is HERE!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Vector<MOB> inhabs=new Vector<MOB>();
		int profNeg=0;
		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			final MOB M=oldRoom.fetchInhabitant(m);
			if(M!=null)
			{
				inhabs.addElement(M);
				final int adjustment=M.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				profNeg+=adjustment;
			}
		}
		profNeg+=newRoom.numItems();

		final boolean success=proficiencyCheck(mob,-(profNeg/2),auto);

		if((success)&&(inhabs.size()>0))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> summon(s) the power of <S-HIS-HER> marker energy!^?"));
			if((mob.location().okMessage(mob,msg))&&(oldRoom.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<inhabs.size();i++)
				{
					final MOB follower=inhabs.elementAt(i);
					final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,L("<S-NAME> appear(s) in a burst of light."));
					final CMMsg leaveMsg=CMClass.getMsg(follower,oldRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC,L("<S-NAME> disappear(s) in a great summoning swirl."));
					if(oldRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
					{
						follower.makePeace(true);
						oldRoom.send(follower,leaveMsg);
						newRoom.bringMobHere(follower,false);
						newRoom.send(follower,enterMsg);
						follower.tell(L("\n\r\n\r"));
						CMLib.commands().postLook(follower,true);
					}
				}
				final Vector<Item> items=new Vector<Item>();
				for(int i=oldRoom.numItems()-1;i>=0;i--)
				{
					final Item I=oldRoom.getItem(i);
					if(I!=null)
						items.addElement(I);
				}
				for(int i=0;i<items.size();i++)
				{
					final Item I=items.elementAt(i);
					oldRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 disappears in a summoning swirl!",I.name()));
					newRoom.moveItemTo(I);
					newRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 appears in a burst of light!",I.name()));
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to summon <S-HIS-HER> marker energy, but fail(s)."));

		// return whether it worked
		return success;
	}
}
