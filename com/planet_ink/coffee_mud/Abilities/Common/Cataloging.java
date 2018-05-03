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
public class Cataloging extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Cataloging";
	}

	private final static String	localizedName	= CMLib.lang().L("Cataloging");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CATAWRITE", "CWRITE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected Physical	found		= null;
	protected Item		catalogI	= null;
	protected boolean	addLore		= false;
	protected boolean	addAppraise	= false;
	
	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Cataloging()
	{
		super();
		displayText=L("You are cataloging...");
		verb=L("cataloging");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(catalogI==null)
					commonTell(mob,L("You mess up your cataloging."));
				else
				if(found instanceof Item)
				{
					final Item item=(Item)found;
					String tag=Labeling.getCurrentTag(item);
					StringBuilder buf=new StringBuilder();
					buf.append(L("\n\rItem: @x1\n\r",item.displayText(mob)));
					final Room R=CMLib.map().roomLocation(item);
					if(R!=null)
					{
						buf.append(L("Location: @x1\n\r",R.displayText(mob)));
						LandTitle T=CMLib.law().getLandTitle(R);
						if(T!=null)
							buf.append(L("Property Title ID: @x1\n\r",T.getTitleID()));
					}
					if(item.container()!=null)
					{
						buf.append(L("Container: @x1\n\r",item.ultimateContainer(null).name()));
					}
					buf.append(L("\n\r"));
					if(item.description().length()==0)
						buf.append(L("You don't see anything special about @x1.  ",item.name()));
					else
						buf.append(item.description(mob)+"  ");
					buf.append(CMLib.commands().getExamineItemString(mob,item)+"\n\r");
					if(item instanceof Container)
					{
						buf.append("\n\r");
						final Container contitem=(Container)item;
						if((contitem.isOpen())
						&&((contitem.capacity()>0)
							||(contitem.hasContent())
							||((contitem instanceof Drink)&&(((Drink)contitem).liquidRemaining()>0))))
						{
							buf.append(item.name()+" contains:\n\r");
							final Vector<Item> newItems=new Vector<Item>();
							if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
							{
								final RawMaterial l=(RawMaterial)CMClass.getItem("GenLiquidResource");
								final int myResource=((Drink)item).liquidType();
								l.setMaterial(myResource);
								((Drink)l).setLiquidType(myResource);
								l.setBaseValue(RawMaterial.CODES.VALUE(myResource));
								l.basePhyStats().setWeight(1);
								final String name=RawMaterial.CODES.NAME(myResource).toLowerCase();
								l.setName(L("some @x1",name));
								l.setDisplayText(L("some @x1 sits here.",name));
								l.setDescription("");
								CMLib.materials().addEffectsToResource(l);
								l.recoverPhyStats();
								newItems.addElement(l);
							}

							if(item.owner() instanceof MOB)
							{
								final MOB M=(MOB)item.owner();
								for(int i=0;i<M.numItems();i++)
								{
									final Item item2=M.getItem(i);
									if((item2!=null)&&(item2.container()==item))
										newItems.addElement(item2);
								}
								buf.append(CMLib.lister().lister(mob,newItems,true,null,null,true,false));
							}
							else
							if(item.owner() instanceof Room)
							{
								final Room room=(Room)item.owner();
								if(room!=null)
								for(int i=0;i<room.numItems();i++)
								{
									final Item item2=room.getItem(i);
									if((item2!=null)&&(item2.container()==item))
										newItems.addElement(item2);
								}
								buf.append(CMLib.lister().lister(mob,newItems,true,null,null,true,false));
							}
						}
						else
						if((contitem.hasADoor())&&((contitem.capacity()>0)||(contitem.hasContent())))
							buf.append(L("@x1 is closed.  ",item.name()));
					}
					final Ability appraiseA=mob.fetchAbility("Thief_Appraise");
					if(appraiseA != null)
					{
						List<String> cmds=new XVector<String>("WORTH");
						if(appraiseA.invoke(mob, cmds, item, true, -1))
							buf.append(cmds.get(0)+"\n\r");
					}
					final Ability loreA=mob.fetchAbility("Thief_Lore");
					if(loreA != null)
					{
						List<String> cmds=new XVector<String>("MSG");
						if(loreA.invoke(mob, cmds, item, true, -1))
							buf.append(cmds.get(0)+"\n\r");
					}
					String raceName = Taxidermy.getStatueRace(item);
					if(raceName.length()>0)
					{
						final Race raceR=CMClass.findRace(raceName);
						if(raceR!=null)
						{
							List<String> bodyParts=new ArrayList<String>();
							for(int i=0;i<Race.BODYPARTSTR.length;i++)
							{
								if(raceR.bodyMask()[i] > 0)
									bodyParts.add(Race.BODYPARTSTR[i].toLowerCase());
							}
							final String parts=CMLib.english().toEnglishStringList(bodyParts);
							buf.append(L("This was a @x1, of the genus @x2.  They weight at least @x3 pounds, stand at least @x4 inches tall, and have the following body parts: @x5.\n\r",
									raceR.name(),raceR.racialCategory(),""+raceR.lightestWeight(),""+raceR.shortestFemale(),parts));
						}
					}
					
					if(tag.length()>0)
						tag=": "+tag;
					
					//item name as the title, and provides the item description, value, level, material, weight, (spell identify) properties and current location of the item.
					final CMMsg msg=CMClass.getMsg(mob,catalogI,this,
							CMMsg.MSG_WRITE,L("<S-NAME> write(s) on <T-NAMESELF>."),
							CMMsg.MSG_WRITE,"::"+item.Name()+tag+"::"+buf.toString(),
							CMMsg.MSG_WRITE,L("<S-NAME> write(s) on <T-NAMESELF>."));
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
					
				}
			}
		}
		super.unInvoke();
	}
	
	public boolean isPossibleCatalog(final MOB mob, final Item I, final Environmental fullyE, final boolean quiet)
	{
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			if(!quiet)
				commonTell(mob,L("You can't write in @x1.",I.name(mob)));
			return false;
		}
		if(!CMLib.flags().isReadable(I))
		{
			if(!quiet)
				commonTell(mob,L("@x1 is not even readable!",CMStrings.capitalizeFirstLetter(I.name(mob))));
			return false;
		}
		if(I instanceof Recipe)
		{
			commonTell(mob,L("@x1 isn't a catalog!",CMStrings.capitalizeAndLower(I.name(mob))));
			return false;
		}
		/*
		String brand = getBrand(I);
		if((brand==null)||(brand.length()==0))
		{
			if(!quiet)
				commonTell(mob,L("You aren't permitted to add catalog entries to @x1.",I.name(mob)));
			return false;
		}
		*/
		
		if(fullyE != null)
		{
			if(!Titling.getCatalogType(I).equals(Titling.getCatalogEntryType(fullyE)))
			{
				if(!quiet)
					commonTell(mob,L("@x1 is not a proper catalog for @x2!",CMStrings.capitalizeFirstLetter(I.name(mob)),fullyE.name()));
			}
		}
		return true;
	}

	public Item findCatalogBook(final MOB mob, final Physical itemI)
	{
		Item catalogI=null;
		for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I.container()==null)
			&&(!I.amWearingAt(Item.IN_INVENTORY))
			&&(isPossibleCatalog(mob, I, itemI, true)))
				catalogI=I;
		}
		if(catalogI==null)
		{
			for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I.container()==null)
				&&(isPossibleCatalog(mob, I, itemI, true)))
					catalogI=I;
			}
		}
		if(catalogI==null)
		{
			for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(isPossibleCatalog(mob, I, itemI, true))
					catalogI=I;
			}
		}
		return catalogI;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
		{
			commonTell(mob,L("You must specify what you want to catalog, and, optionally, where to add the entry.  If you do "
					+ "not specify the catalog, one prepared with the Titling skill will be automatically selected from your inventory."));
			return false;
		}
		Physical physP=null;
		Item catalogI=null;
		String itemName=CMParms.combine(commands,0);
		if(commands.size()==1) // this always means the item is alone
		{
			physP=this.getAnyTarget(mob, mob.location(), true, commands, givenTarget, Wearable.FILTER_UNWORNONLY);
			if((physP==null)||(!CMLib.flags().canBeSeenBy(physP,mob)))
				physP=null;
			if(physP!=null)
			{
				catalogI=this.findCatalogBook(mob, physP);
				if(catalogI==null)
				{
					commonTell(mob,L("You need to specify a proper catalog for '@x1'.",physP.name(mob)));
					return false;
				}
			}
		}
		else
		{
			physP=this.getAnyTarget(mob, mob.location(), true, commands, givenTarget, Wearable.FILTER_UNWORNONLY, true);
			if((physP==null)||(!CMLib.flags().canBeSeenBy(physP,mob)))
				physP=null;
			if(physP!=null)
			{
				catalogI=this.findCatalogBook(mob, physP);
				if(catalogI==null)
				{
					commonTell(mob,L("You need to specify a proper catalog for '@x1'.",physP.name(mob)));
					return false;
				}
			}
			if((physP==null)||(catalogI==null))
			{
				itemName=commands.get(0);
				physP=this.getAnyTarget(mob, mob.location(), true, new XVector<String>(commands.get(0)), givenTarget, Wearable.FILTER_UNWORNONLY);
				if((physP==null)||(!CMLib.flags().canBeSeenBy(physP,mob)))
					physP=null;
				if(physP!=null)
				{
					List<String> cmds2=new ArrayList<String>(commands);
					cmds2.remove(0);
					catalogI=this.getTarget(mob, null, givenTarget, cmds2, Wearable.FILTER_UNWORNONLY);
					if(catalogI!=null)
					{
						if(!isPossibleCatalog(mob, catalogI, null, false))
							return false;
						String cat=Titling.getCatalogType(catalogI);
						if((cat.length()>0)&&(!isPossibleCatalog(mob, catalogI, physP, false)))
							return false;
					}
				}
			}
		}
		if(physP==null)
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",itemName));
			return false;
		}
		if(!(physP instanceof Item))
		{
			commonTell(mob,L("You can't catalog @x1",physP.name()));
			return false;
		}
		if(catalogI==null)
		{
			commonTell(mob,L("You don't seem to have a proper catalog."));
			return false;
		}
		
		final Ability writeA=mob.fetchAbility("Skill_Write");
		if(writeA==null)
		{
			commonTell(mob,L("You must know how to write to entitle."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("cataloging @x1 into @x2",physP.name(),catalogI.name());
		displayText=L("You are @x1",verb);
		this.found=physP;
		this.catalogI=catalogI;
		if((!proficiencyCheck(mob,0,auto))
		||(!writeA.proficiencyCheck(mob,super.getXLEVELLevel(mob)*10,auto)))
			this.catalogI=null;
		final int duration=getDuration(15,mob,1,1);
		final CMMsg msg=CMClass.getMsg(mob,physP,this,getActivityMessageType(),L("<S-NAME> start(s) cataloging <T-NAME> into @x1.",catalogI.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
