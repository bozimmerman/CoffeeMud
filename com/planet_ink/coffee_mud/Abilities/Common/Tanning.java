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
   Copyright 2017-2018 Bo Zimmerman

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

public class Tanning extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Tanning";
	}

	private final static String	localizedName	= CMLib.lang().L("Tanning");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TAN", "TANNING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_NATURELORE;
	}

	protected Item		found			= null;
	boolean				fireRequired	= false;
	protected int		amount			= 0;
	protected String	oldItemName		= "";
	protected String	foundShortName	= "";
	protected boolean	messedUp		= false;

	public Tanning()
	{
		super();
		displayText = L("You are tanning...");
		verb = L("tanning");
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((found==null)||(fireRequired&&(getRequiredFire(mob,0)==null)))
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
				final Room R=mob.location();
				if((found!=null)&&(!aborted)&&(R!=null))
				{
					if(messedUp)
						commonTell(mob,L("You've messed up tanning @x1!",oldItemName));
					else
					{
						amount=amount*(baseYield()+abilityCode());
						final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
						msg.setValue(amount);
						if(R.okMessage(mob, msg))
						{
							String s="s";
							if(msg.value()==1)
								s="";
							msg.modify(L("<S-NAME> manage(s) to tan @x1 pound@x2 of @x3.",""+msg.value(),s,foundShortName));
							R.send(mob, msg);
							if(found.material() != RawMaterial.RESOURCE_NOTHING)
							{
								for(int i=0;i<msg.value();i++)
								{
									final Item newFound=(Item)found.copyOf();
									if(!dropAWinner(mob,newFound))
										break;
									CMLib.commands().postGet(mob,null,newFound,true);
								}
							}
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(super.checkStop(mob, commands)||(R==null))
			return true;
		verb=L("tanning");
		final String str=CMParms.combine(commands,0);
		final Item I=R.findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,L("You don't see anything called '@x1' here.",str));
			return false;
		}
		boolean okMaterial=I.material()==RawMaterial.RESOURCE_HIDE;
		oldItemName=I.Name();
		if(!okMaterial)
		{
			commonTell(mob,L("You don't know how to tan @x1.",I.name(mob)));
			return false;
		}

		if(CMLib.flags().isEnchanted(I))
		{
			commonTell(mob,L("@x1 is enchanted, and can't be tanned.",I.name(mob)));
			return false;
		}

		final ArrayList<Item> V=new ArrayList<Item>();
		int totalWeight = 0;
		for(int i=0;i<R.numItems();i++)
		{
			final Item I2=R.getItem(i);
			if((I2!=null)&&(I2.sameAs(I)))
			{
				totalWeight += I2.phyStats().weight();
				V.add(I2);
			}
		}

		final LandTitle t=CMLib.law().getLandTitle(R);
		if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,R)))
		{
			mob.tell(L("You are not allowed to tan anything here."));
			return false;
		}

		for(int i=0;i<R.numItems();i++)
		{
			final Item I2=R.getItem(i);
			if((I2.container()!=null)&&(V.contains(I2.container())))
			{
				commonTell(mob,L("You need to remove the contents of @x1 first.",I2.name(mob)));
				return false;
			}
		}
		final Item fire=getRequiredFire(mob,0);
		fireRequired=true;
		if(fire==null)
			return false;

		found=null;
		amount=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int duration=getDuration(45,mob,1,10);
		messedUp=!proficiencyCheck(mob,0,auto);
		found=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_LEATHER);
		foundShortName="nothing";
		playSound="ripping.wav";
		if(found!=null)
			foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),L("<S-NAME> start(s) tanning @x1.",I.name()));
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			for(int v=0;v<V.size();v++)
			{
				duration+=V.get(v).phyStats().weight()/2;
				V.get(v).destroy();
			}
			amount=totalWeight;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
