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
   Copyright 2003-2025 Bo Zimmerman

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
public class AnimalTaming extends CommonSkill
{
	@Override
	public String ID()
	{
		return "AnimalTaming";
	}

	private final static String localizedName = CMLib.lang().L("Animal Taming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"TAME","TAMING","ANIMALTAMING"});
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

	protected Physical taming=null;
	protected boolean messedUp=false;
	public AnimalTaming()
	{
		super();
		displayText=L("You are taming...");
		verb=L("taming");
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((taming==null)||(mob.location()==null))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof MOB)&&(!mob.location().isInhabitant((MOB)taming)))
			{
				messedUp=true;
				unInvoke();
			}
			if((taming instanceof Item)&&(!mob.location().isContent((Item)taming)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((taming!=null)&&(!aborted))
				{
					MOB animal=null;
					if(taming instanceof MOB)
						animal=(MOB)taming;
					else
					if((taming!=null)&&(taming instanceof CagedAnimal))
						animal=((CagedAnimal)taming).unCageMe();
					if((messedUp)||(animal==null))
						commonTelL(mob,"You've failed to tame @x1!",taming.name());
					else
					{
						if(animal.numBehaviors()==0)
							commonTelL(mob,"@x1 is already tame.",taming.name());
						else
						{
							int amount=1;
							amount=amount*(baseYield()+abilityCode());
							if(amount>animal.numBehaviors())
								amount=animal.numBehaviors();
							String s="";
							if(amount>1)
								s="of "+amount+" ";
							s+="of "+animal.charStats().hisher()+" behaviors";
							animal.basePhyStats().addAmbiance("@TAMED");
							mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to tame @x1 @x2.",animal.name(),s));
							for(int i=0;i<amount;i++)
							{
								if(animal.numBehaviors()==0)
									break;
								final Behavior B=animal.fetchBehavior(CMLib.dice().roll(1,animal.numBehaviors(),-1));
								if(B!=null)
								{
									animal.delBehavior(B);
								}
								animal.recoverCharStats();
								animal.recoverPhyStats();
								animal.recoverMaxState();
							}
							animal.resetToMaxState();
							if(taming instanceof CagedAnimal)
							{
								animal.text();
								((CagedAnimal)taming).cageMe(animal);
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	protected boolean mayITame(final MOB mob, final MOB M, final String str)
	{
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("taming");
		taming=null;
		Item cage=null;
		final String str=CMParms.combine(commands,0);
		MOB M;
		if(givenTarget instanceof MOB)
			M=(MOB)givenTarget;
		else
			M=getVisibleRoomTarget(mob,str);
		taming=null;
		if(M!=null)
		{
			if(!CMLib.flags().canBeSeenBy(M,mob))
			{
				commonTelL(mob,"You don't see anyone called '@x1' here.",str);
				return false;
			}
			if((!M.isMonster())
			   ||(!CMLib.flags().isAnAnimal(M)))
			{
				commonTelL(mob,"You can't tame @x1.",M.name(mob));
				return false;
			}
			if((CMLib.flags().canMove(M))&&(!CMLib.flags().isBoundOrHeld(M)))
			{
				commonTelL(mob,"@x1 doesn't seem willing to cooperate.",M.name(mob));
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
					cage=I;
					break;
				}
			}
			if(commands.size()>0)
			{
				final String last=commands.get(commands.size()-1);
				final Item I=mob.location().findItem(null,last);
				if((I!=null)
				&&(I instanceof Container)
				&&((((Container)I).containTypes()&Container.CONTAIN_CAGED)==Container.CONTAIN_CAGED))
				{
					cage=I;
					commands.remove(last);
				}
			}
			if(cage==null)
			{
				commonTelL(mob,"You don't see anyone called '@x1' here.",str);
				return false;
			}
			taming=mob.location().findItem(cage,CMParms.combine(commands,0));
			if((taming==null)||(!CMLib.flags().canBeSeenBy(taming,mob))||(!(taming instanceof CagedAnimal)))
			{
				commonTelL(mob,"You don't see any creatures in @x1 called '@x2'.",cage.name(),CMParms.combine(commands,0));
				return false;
			}
			M=((CagedAnimal)taming).unCageMe();
		}
		else
			return false;

		if(!mayITame(mob,M,str))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-taming.phyStats().level()+(2*getXLEVELLevel(mob)),auto);
		final int duration=getDuration(35,mob,taming.phyStats().level(),10);
		verb=L("taming @x1",M.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) taming @x1.",M.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
