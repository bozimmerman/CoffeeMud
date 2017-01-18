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
   Copyright 2017-2017 Bo Zimmerman

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
public class Composting extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Composting";
	}

	private final static String localizedName = CMLib.lang().L("Composting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "COMPOST", "COMPOSTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "VEGETATION|FLESH";
	}

	protected Item		found			= null;
	protected Item		compost			= null;
	protected Room		room			= null;
	protected String	foundShortName	= "";

	
	public Composting()
	{
		super();
		displayText=L("You are composting...");
		verb=L("composting");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(45,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			final MOB mob=invoker();
			if(tickUp==6)
			{
				if((compost==null)
				||(mob==null)
				||(mob.location()==null))
				{
					commonTell(mob,L("Your @x1 composting has failed.\n\r",foundShortName));
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		final boolean isaborted=aborted;
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected==invoker))
			{
				if((compost!=null)&&(!isaborted))
				{
					int amount = compost.phyStats().weight();
					if(amount == 1)
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("a pounds of @x2 compost is ready here.",""+amount,foundShortName));
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 pound(s) of @x2 compost are ready here.",""+amount,foundShortName));
					final Item newFound=(Item)compost.copyOf();
					dropAWinner(null,room,newFound);
				}
			}
		}
		super.unInvoke();
	}

	public boolean isCompostable(final MOB mob, final Item I)
	{
		if((I instanceof RawMaterial)
		&&((I instanceof Food)||(I instanceof Drink))
		&&(CMLib.flags().isGettable(I))
		&&(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)))
			return true;
		return false;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		verb=L("composting");
		if(mob.isMonster()
		&&(!auto)
		&&(!CMLib.flags().isAnimalIntelligence(mob))
		&&(commands.size()==0))
		{
			Item mine=null;
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I2=mob.location().getItem(i);
				if(isCompostable(mob,I2))
				{
					mine=I2;
					commands.add(RawMaterial.CODES.NAME(I2.material()));
					break;
				}
			}
			if(mine==null)
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I2=mob.getItem(i);
				if(isCompostable(mob,I2))
				{
					commands.add(RawMaterial.CODES.NAME(I2.material()));
					mine=(Item)I2.copyOf();
					if(mob.location().findItem(null,mob.location().getContextName(I2))==null)
						mob.location().addItem(mine,ItemPossessor.Expire.Resource);
					break;
				}
			}
			if(mine==null)
			{
				commonTell(mob,L("You don't have anything you can compost."));
				return false;
			}
		}
		else
		if(commands.size()==0)
		{
			commonTell(mob,L("Compost what?"));
			return false;
		}
		Item mine=super.getTarget(null, mob.location(), givenTarget, commands, null);
		if(mine==null)
		{
			commonTell(mob,L("You'll need to have some @x1 to seed from on the ground first.",foundShortName));
			return false;
		}
		if(!isCompostable(mob,mine))
		{
			commonTell(mob,L("'@x1' is not suitable for composting.",mine.Name()));
			return false;
		}

		found=mine;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		this.compost=null;
		if(proficiencyCheck(mob,0,auto))
		{
			Item compost = CMClass.getItem("GenItem");
			compost.setName("a pound of compost");
			compost.setDisplayText("a pound of compost is lying here");
			compost.setSecretIdentity("compost");
			compost.addNonUninvokableEffect(CMClass.getAbility("Prop_Rotten"));
			compost.basePhyStats().setWeight(1);
			compost.recoverPhyStats();
			if(mine.phyStats().weight()==1)
				this.compost=compost;
			else
			{
				this.compost=CMClass.getItem("GenPackagedStack");
				((PackagedItems)this.compost).packageMe(compost,mine.phyStats().weight());
			}
		}

		mine.destroy();
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) composting @x1.",foundShortName));
		verb=L("composting @x1",foundShortName);
		displayText=L("You are composting @x1",foundShortName);
		room=mob.location();
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
