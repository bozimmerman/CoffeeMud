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
public class AnimalTrading extends CommonSkill
{
	@Override
	public String ID()
	{
		return "AnimalTrading";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Trading");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ANIMALTRADING", "ANIMALTRADE", "ANIMALSELL", "ASELL" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected Vector<String>	recentlyTraded	= new Vector<String>();

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;

		Environmental taming=null;
		Item cage=null;

		commands.add(0,"SELL");
		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Sell what to whom?");
		if(shopkeeper==null)
			return false;
		if(commands.size()==0)
		{
			commonTell(mob,L("Sell what?"));
			return false;
		}

		final String str=CMParms.combine(commands,0);
		MOB M=mob.location().fetchInhabitant(str);
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",str));
				return false;
			}
			if((!M.isMonster())||(!CMLib.flags().isAnimalIntelligence(M)))
			{
				commonTell(mob,L("You can't sell @x1.",M.name(mob)));
				return false;
			}
			if((CMLib.flags().canMove(M))&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTell(mob,L("@x1 doesn't seem willing to cooperate.  You need to bind the animal before you can sell it.",M.name(mob)));
				return false;
			}
			taming=M;
		}
		else
		if(mob.location()!=null)
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I=mob.location().getItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage = I;
					break;
				}
			}
			if(cage==null)
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage = I;
					break;
				}
			}
			if(commands.size()>0)
			{
				final String last=commands.get(commands.size()-1);
				final Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,last,Wearable.FILTER_ANY);
				if((E!=null)
				&&(E instanceof Item)
				&&(E instanceof Container)
				&&((((Container)E).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=(Item)E;
					commands.remove(last);
				}
			}
			if(cage==null)
			{
				commonTell(mob,L("You don't see anyone called '@x1' here.",str));
				return false;
			}
			taming=mob.location().fetchFromMOBRoomFavorsItems(mob,cage,CMParms.combine(commands,0),Wearable.FILTER_ANY);
			if((taming==null)||(!CMLib.flags().canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTell(mob,L("You don't see any creatures in @x1 called '@x2'.",cage.name(),CMParms.combine(commands,0)));
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}
		else
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
		{
			final CMMsg msg=CMClass.getMsg(mob,shopkeeper,M,CMMsg.MSG_SELL,L("<S-NAME> sell(s) <O-NAME> to <T-NAME>."));
			final CMMsg msg2=CMClass.getMsg(mob,M,this,getActivityMessageType(),null);
			if(!recentlyTraded.contains(mob.Name()))
			{
				while(recentlyTraded.size()>30)
					recentlyTraded.removeElementAt(0);
				recentlyTraded.addElement(M.Name());
				msg2.setValue(-1);
			}
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if(taming instanceof Item)
					((Item)taming).destroy();
			}
		}
		else
			beneficialWordsFizzle(mob,shopkeeper,L("<S-NAME> <S-IS-ARE>n't able to strike a deal with <T-NAME>."));
		return true;
	}
}
