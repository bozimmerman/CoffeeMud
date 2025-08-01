package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
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
   Copyright 2017-2025 Bo Zimmerman

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

	protected Item		compost			= null;
	protected Room		room			= null;
	protected String	foundShortName	= "";

	public Composting()
	{
		super();
		displayText=L("You are composting...");
		verb=L("composting");
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof Room))
		{
			final MOB mob=invoker();
			if(tickUp==6)
			{
				if((compost==null)
				||(mob==null)
				||(mob.location()==null))
				{
					commonTelL(mob,"Your @x1 composting has failed.\n\r",foundShortName);
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
					final int amount = compost.phyStats().weight();
					if(amount == 1)
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("A pound of compost is ready here.",""+amount));
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 pound(s) of compost are ready here.",""+amount));
					dropAWinner(invoker,room,compost);
					compost = null;
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

	protected boolean isCompost(final Item I)
	{
		return ((I!=null) &&(I.rawSecretIdentity().equals("compost")));
	}

	@Override
	public boolean bundle(final MOB mob, final List<String> what)
	{
		if((what.size()<3)
		||((!CMath.isNumber(what.get(1)))&&(!what.get(1).equalsIgnoreCase("ALL"))))
		{
			commonTelL(mob,"You must specify an amount to bundle, followed by what to bundle.");
			return false;
		}
		int amount=CMath.s_int(what.get(1));
		if(what.get(1).equalsIgnoreCase("ALL"))
			amount=Integer.MAX_VALUE;
		if(amount<=0)
		{
			commonTelL(mob,"@x1 is not an appropriate amount.",""+amount);
			return false;
		}
		int numHere=0;
		final Room R=mob.location();
		if(R==null)
			return false;
		final String name=CMParms.combine(what,2);
		int foundResource=-1;
		Item foundAnyway=null;
		final Hashtable<String,Ability> foundAblesH=new Hashtable<String,Ability>();
		Ability A=null;
		final List<Item> foundOnes = new ArrayList<Item>();
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if(CMLib.english().containsString(I.Name(),name))
			{
				if(foundAnyway==null)
					foundAnyway=I;
				if(this.isCompost(I))
				{
					for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
					{
						A=a.nextElement();
						if((A!=null)
						&&(!A.canBeUninvoked())
						&&(!foundAblesH.containsKey(A.ID())))
							foundAblesH.put(A.ID(),A);
					}
					foundResource=I.material();
					foundOnes.add(I);
					numHere+=I.phyStats().weight();
				}
			}
		}
		if((numHere==0)||(foundResource<0)||(foundOnes.size()<0))
		{
			return super.bundle(mob, what);
		}
		if(amount==Integer.MAX_VALUE)
			amount=numHere;
		if(numHere<amount)
		{
			commonTelL(mob,"You only see @x1 pounds of @x2 on the ground here.",""+numHere,name);
			return false;
		}
		if(amount == 1)
			return true;
		final Item I=CMClass.getItem("GenPackagedStack");
		I.setMaterial(RawMaterial.RESOURCE_DIRT);
		Item compostItem = null;
		for(int i=0;i<foundOnes.size();i++)
		{
			final Item I2=foundOnes.get(i);
			if(I2!=null)
			{
				if(I2 instanceof PackagedItems)
					compostItem=((PackagedItems)I2).peekFirstItem();
				else
					compostItem=(Item)I2.copyOf();
				I2.destroy();
			}
		}
		((PackagedItems)I).packageMe(compostItem,amount);
		if(R.show(mob,null,I,getActivityMessageType(),L("<S-NAME> create(s) <O-NAME>.")))
		{
			if((!I.amDestroyed())&&(!R.isContent(I)))
				R.addItem(I,ItemPossessor.Expire.Player_Drop);
		}
		if(compostItem != null)
			compostItem.destroy();
		for(final Enumeration<String> e=foundAblesH.keys();e.hasMoreElements();)
			I.addNonUninvokableEffect((Ability)((Environmental)foundAblesH.get(e.nextElement())).copyOf());
		R.recoverRoomStats();
		return true;
	}

	protected int[][] fetchFoundResourceData(final MOB mob, int req1Required, String req1Desc, final Item first)
	{
		final int[][] data=new int[2][3];
		if((req1Desc!=null)&&(req1Desc.length()==0))
			req1Desc=null;

		final Item firstWood=first;

		String subType = "";
		data[0][CraftingSkill.FOUND_AMT]=0;
		if(firstWood!=null)
		{
			if(firstWood instanceof RawMaterial)
			{
				subType = ((RawMaterial)first).getSubType();
				data[0][CraftingSkill.FOUND_AMT]=CMLib.materials().findNumberOfResourceLike(mob.location(),(RawMaterial)firstWood);
			}
			else
				data[0][CraftingSkill.FOUND_AMT]=1;
			data[0][CraftingSkill.FOUND_CODE]=firstWood.material();
			if(firstWood instanceof RawMaterial)
				data[0][CraftingSkill.FOUND_SUB]=(((RawMaterial)firstWood).getSubType()).hashCode();
			else
				data[0][CraftingSkill.FOUND_SUB]="".hashCode();
		}

		if(req1Required>0)
		{
			if(data[0][CraftingSkill.FOUND_AMT]==0)
			{
				if(req1Desc!=null)
					commonTelL(mob,"There is no @x1 here to make anything from!  It might need to be put down first.",req1Desc.toLowerCase());
				return null;
			}
			req1Required=fixResourceRequirement(data[0][CraftingSkill.FOUND_CODE],req1Required);
		}

		if((req1Required == Integer.MAX_VALUE) && (data[0][CraftingSkill.FOUND_AMT] > 0))
			req1Required = data[0][CraftingSkill.FOUND_AMT];
		if(req1Required>data[0][CraftingSkill.FOUND_AMT])
		{
			commonTelL(mob,"You need @x1 pounds of @x2 to do that.  There is not enough here.  Are you sure you set it all on the ground first?",
					""+req1Required,CMLib.materials().makeResourceSimpleName(first.material(), subType).toLowerCase());
			return null;
		}
		data[0][CraftingSkill.FOUND_AMT]=req1Required;
		return data;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
				return bundle(mob,commands);
			return false;
		}

		if(commands.size()==0)
		{
			if(auto)
			{
				// ?
				return false;
			}
			else
			{
				commonTelL(mob,"Compost how much of what?");
				return false;
			}
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
				commonTelL(mob,"You don't have anything you can compost.");
				return false;
			}
		}
		else
		if(commands.size()==0)
		{
			commonTelL(mob,"Compost how much of what?");
			return false;
		}
		int amount=CMath.s_int(commands.get(0));
		if(commands.get(0).equalsIgnoreCase("ALL"))
			amount=Integer.MAX_VALUE;
		if(amount<=0)
			amount=1;
		else
			commands.remove(0);

		foundShortName = CMParms.combine(commands);
		final Item mine=super.getTarget(mob, mob.location(), givenTarget, commands, new Filterer<Environmental>()
		{
			@Override
			public boolean passesFilter(final Environmental obj)
			{
				return (obj instanceof Item) && (((Item)obj).owner() instanceof Room);
			}

		});
		if(mine==null)
		{
			commonTelL(mob,"You'll need to have some @x1 on the ground first.",foundShortName);
			return false;
		}
		if(!isCompostable(mob,mine))
		{
			commonTelL(mob,"'@x1' is not suitable for composting.",mine.Name());
			return false;
		}
		foundShortName = mine.name();
		Item found=mine;
		final int[][] data=fetchFoundResourceData(mob,amount,"material",mine);
		if(data==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(amount == Integer.MAX_VALUE)
			amount = data[0][CraftingSkill.FOUND_AMT];
		CMLib.materials().destroyResourcesAmt(mob.location(),amount,data[0][CraftingSkill.FOUND_CODE],
				((RawMaterial)found).getSubType(), null);
		this.compost=null;
		if(proficiencyCheck(mob,0,auto))
		{
			final Item compost = CMClass.getItem("GenItem");
			compost.setName("a pound of compost");
			compost.setDisplayText("a pound of compost is lying here");
			compost.setSecretIdentity("compost");
			compost.addNonUninvokableEffect(CMClass.getAbility("Prop_Rotten"));
			compost.basePhyStats().setWeight(1);
			compost.setMaterial(data[0][CraftingSkill.FOUND_CODE]);
			compost.recoverPhyStats();
			if(amount==1)
				this.compost=compost;
			else
			{
				this.compost=CMClass.getItem("GenPackagedStack");
				compost.setName("a pound(s) of compost");
				compost.setDisplayText("a pound(s) of compost is lying here");
				((PackagedItems)this.compost).packageMe(compost,amount);
			}
		}

		final int duration=compost == null ? getDuration(3+1,mob,1,1) : getDuration(3+compost.phyStats().weight(),mob,1,1);

		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) composting @x1.",foundShortName));
		verb=L("composting @x1",foundShortName);
		displayText=L("You are composting @x1",foundShortName);
		room=mob.location();
		final String oldFoundName = (found==null)?"":found.Name();
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			if((found!=null)&&(!found.Name().equals(oldFoundName)))
				foundShortName=CMLib.english().removeArticleLead(found.Name());
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
