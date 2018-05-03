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

public class Scrapping extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Scrapping";
	}

	private final static String	localizedName	= CMLib.lang().L("Scrapping");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SCRAP", "SCRAPPING" });

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

	public Scrapping()
	{
		super();
		displayText = L("You are scrapping...");
		verb = L("scrapping");
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
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					if(messedUp)
						commonTell(mob,L("You've messed up scrapping @x1!",oldItemName));
					else
					{
						amount=amount*(baseYield()+abilityCode());
						final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
						msg.setValue(amount);
						if(mob.location().okMessage(mob, msg))
						{
							String s="s";
							if(msg.value()==1)
								s="";
							msg.modify(L("<S-NAME> manage(s) to scrap @x1 pound@x2 of @x3.",""+msg.value(),s,foundShortName));
							mob.location().send(mob, msg);
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
		if(super.checkStop(mob, commands))
			return true;
		verb=L("scrapping");
		final String str=CMParms.combine(commands,0);
		final Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,L("You don't see anything called '@x1' here.",str));
			return false;
		}
		boolean okMaterial=true;
		oldItemName=I.Name();
		switch(I.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_FLESH:
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_ENERGY:
		case RawMaterial.MATERIAL_GAS:
		case RawMaterial.MATERIAL_VEGETATION:
			{
				okMaterial=false;
				break;
			}
		}
		if(!okMaterial)
		{
			commonTell(mob,L("You don't know how to scrap @x1.",I.name(mob)));
			return false;
		}

		if(I instanceof RawMaterial)
		{
			commonTell(mob,L("@x1 already looks like scrap.",I.name(mob)));
			return false;
		}

		if(CMLib.flags().isEnchanted(I))
		{
			commonTell(mob,L("@x1 is enchanted, and can't be scrapped.",I.name(mob)));
			return false;
		}

		final Vector<Item> V=new Vector<Item>();
		int totalWeight=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if((I2!=null)&&(I2.sameAs(I)))
			{
				totalWeight+=I2.phyStats().weight();
				V.addElement(I2);
			}
		}

		final LandTitle t=CMLib.law().getLandTitle(mob.location());
		if((t!=null)&&(!CMLib.law().doesHavePriviledgesHere(mob,mob.location())))
		{
			mob.tell(L("You are not allowed to scrap anything here."));
			return false;
		}

		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if((I2.container()!=null)&&(V.contains(I2.container())))
			{
				commonTell(mob,L("You need to remove the contents of @x1 first.",I2.name(mob)));
				return false;
			}
		}
		amount=totalWeight/5;
		if(amount<1)
		{
			commonTell(mob,L("You don't have enough here to get anything from."));
			return false;
		}
		fireRequired=false;
		if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_SYNTHETIC)
		||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
		{
			final Item fire=getRequiredFire(mob,0);
			fireRequired=true;
			if(fire==null)
				return false;
		}

		final PrivateProperty prop=CMLib.law().getPropertyRecord(I);
		if(((prop != null)&&(!CMLib.law().doesHavePrivilegesWith(mob, prop)))
		||(I instanceof SailingShip))
		{
			commonTell(mob,L("@x1 can't be scrapped.",I.name(mob)));
			return false;
		}

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int duration=getDuration(45,mob,1,10);
		messedUp=!proficiencyCheck(mob,0,auto);
		found=CMLib.materials().makeItemResource(I.material());
		foundShortName="nothing";
		playSound="ripping.wav";
		if(found!=null)
			foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),L("<S-NAME> start(s) scrapping @x1.",I.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int v=0;v<V.size();v++)
			{
				if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
					duration+=V.elementAt(v).phyStats().weight();
				else
					duration+=V.elementAt(v).phyStats().weight()/2;
				V.elementAt(v).destroy();
			}
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
