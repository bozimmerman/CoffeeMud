package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_PolymorphObject extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PolymorphObject";
	}

	private final static String localizedName = CMLib.lang().L("Polymorph Object");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}
	
	protected static List<ItemCraftor> craftingSkills=new Vector<ItemCraftor>();
	protected static List<ItemCraftor> getCraftingSkills()
	{
		if(craftingSkills.size()==0)
		{
			final Vector<Ability> V=new Vector<Ability>();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
					V.addElement((ItemCraftor)A.copyOf());
			}
			while(V.size()>0)
			{
				int lowest=Integer.MAX_VALUE;
				ItemCraftor lowestA=null;
				for(int i=0;i<V.size();i++)
				{
					final ItemCraftor A=(ItemCraftor)V.elementAt(i);
					final int ii=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					if(ii<lowest)
					{
						lowest=ii;
						lowestA=A;
					}
				}
				if(lowestA==null)
					lowestA=(ItemCraftor)V.firstElement();
				if(lowestA!=null)
				{
					V.removeElement(lowestA);
					craftingSkills.add(lowestA);
				}
				else
					break;
			}
		}
		return craftingSkills;
	}
	
	protected List<Item> previousItems=null;

	@Override
	public void unInvoke()
	{
		final Physical affected = super.affected;
		super.unInvoke();
		if((previousItems == null) && (text().length()>0))
		{
			previousItems=new XVector<Item>();
			CMLib.coffeeMaker().addItemsFromXML(text(), previousItems, null);
		}
		if(canBeUninvoked() && (affected instanceof Item) && (previousItems!=null) && (previousItems.size()>0))
		{
			final Item item=(Item)affected;
			if(item.owner() instanceof Room)
			{
				((Room)item.owner()).showHappens(CMMsg.MSG_OK_VISUAL, item, L("<S-NAME> reverts to its previous form."));
				for(final Item I : previousItems)
					((Room)item.owner()).addItem(I, Expire.Player_Drop);
				previousItems = null;
			}
			else
			if(item.owner() instanceof MOB)
			{
				((MOB)item.owner()).tell(((MOB)item.owner()),item,null,L("<T-NAME> reverts to its previous form."));
				for(final Item I : previousItems)
					((MOB)item.owner()).addItem(I);
				previousItems = null;
			}
			affected.destroy();
		}
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		// add something to disable traps
		//
		if(commands.size()<2)
		{
			mob.tell(L("Polymorph what object into what?"));
			return false;
		}
		String itemName=commands.get(0);
		final Item targetI=super.getTarget(mob, mob.location(), givenTarget, new XVector<String>(itemName), Wearable.FILTER_UNWORNONLY);
		if(targetI==null)
		{
			mob.tell(L("You don't seem to have a '@x1'.",itemName));
			return false;
		}
		if((targetI instanceof DeadBody)
		||(!CMLib.utensils().canBePlayerDestroyed(mob,targetI,false)))
		{
			mob.tell(L("You can't polymorph that."));
			return false;
		}
		Vector<String> intoWhatV=new XVector<String>(commands);
		intoWhatV.remove(0);
		String intoWhat=CMParms.combineQuoted(commands, 1);
		
		if(targetI.fetchEffect(ID())!=null)
		{
			mob.tell(mob,targetI,null,L("<T-NAME> is already polymorphed!"));
			return false;
		}

		Item intoI = null;
		for(ItemCraftor A : getCraftingSkills())
		{
			List<List<String>> L = A.matchingRecipeNames(intoWhat, false);
			if((L!=null)&&(L.size()>0))
			{
				ItemKeyPair what=A.craftItem(L.get(0).get(0),targetI.material(),true, false);
				if((what!=null)&&(what.item!=null))
				{
					intoI=what.item;
					break;
				}
			}
		}
		if(intoI == null)
		{
			for(ItemCraftor A : getCraftingSkills())
			{
				List<List<String>> L = A.matchingRecipeNames(intoWhat, true);
				if((L!=null)&&(L.size()>0))
				{
					ItemKeyPair what=A.craftItem(L.get(0).get(0),targetI.material(),true, false);
					if((what!=null)&&(what.item!=null))
					{
						intoI=what.item;
						break;
					}
				}
			}
		}
		if(intoI == null)
			intoI = mob.findItem(intoWhat);
		if(intoI == null)
			intoI = CMLib.map().findFirstRoomItem(mob.location().getArea().getCompleteMap(), mob, intoWhat, true, 5);
		
		if(intoI == null)
		{
			mob.tell(L("You have no idea what a '@x1' is.  Perhaps if you saw one again?",intoWhat));
			return false;
		}
		
		if((intoI instanceof ArchonOnly)
		||(!CMLib.flags().isGettable(intoI))
		||(intoI instanceof ClanItem)
		||(intoI.basePhyStats().weight() > mob.maxCarry())
		||(CMath.bset(intoI.phyStats().sensesMask(), PhyStats.SENSE_ITEMNOWISH)))
		{
			mob.tell(L("You can't polymorph anything into @x1?",intoI.Name()));
			return false;
		}
		
		if(intoI.basePhyStats().level()>this.adjustedLevel(mob, asLevel))
		{
			mob.tell(L("You aren't experienced enough to polymorph anything into @x1?",intoI.Name()));
			return false;
		}
		
		double pct= 0.3 + (0.2 * super.getXLEVELLevel(mob));
		int weightDiff = (int)Math.round(CMath.mul(targetI.basePhyStats().weight(),pct));
		if(intoI.basePhyStats().weight() < (targetI.basePhyStats().weight() - weightDiff))
		{
			mob.tell(L("You can only polymorph an item into one no more than @x1 smaller.  @x2 is too small.",CMath.toPct(pct),intoI.Name()));
			return false;
		}
		if(intoI.basePhyStats().weight() > (targetI.basePhyStats().weight() + weightDiff))
		{
			mob.tell(L("You can only polymorph an item into one no more than @x1 large.  @x2 is too big.",CMath.toPct(pct),intoI.Name()));
			return false;
		}
		intoI=(Item)intoI.copyOf();
		if(intoI.material() != targetI.material())
		{
			intoI.setMaterial(targetI.material());
			String oldMaterialName=RawMaterial.CODES.NAME(intoI.material());
			String newMaterialName=RawMaterial.CODES.NAME(targetI.material()).toLowerCase();
			intoI.setName(CMStrings.replaceWord(intoI.Name(), oldMaterialName, newMaterialName));
			intoI.setDisplayText(CMStrings.replaceWord(intoI.displayText(), oldMaterialName, newMaterialName));
			intoI.setDescription(CMStrings.replaceWord(intoI.description(), oldMaterialName, newMaterialName));
		}
		
		CMLib.utensils().disenchantItem(intoI);
		while(intoI.numBehaviors()>0)
		{
			Behavior B=intoI.fetchBehavior(0);
			if(B!=null)
				intoI.delBehavior(B);
		}
		intoI.basePhyStats().setDisposition(intoI.basePhyStats().disposition() & (~PhyStats.IS_BONUS));
		intoI.recoverPhyStats();
		intoI.setBaseValue(0);
		
		// the reason this costs experience is to make it less valuable than Duplicate 
		// but more valuable than Wish.
		final int experienceToLose=getXPCOSTAdjustment(mob,5+intoI.basePhyStats().level());
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
		mob.tell(L("The effort causes you to lose @x1 experience.",""+experienceToLose));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,targetI,this,somanticCastCode(mob,targetI,auto),L("^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAME> polymorphing it into @x1.^?",intoI.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				if(msg.value()>0)
					return false;
				Spell_PolymorphObject A=(Spell_PolymorphObject)super.beneficialAffect(mob, intoI, asLevel, 0);
				if(A!=null)
				{
					final List<Item> items = new XVector<Item>();
					items.add(targetI);
					if(targetI instanceof Container)
						items.addAll(((Container)targetI).getDeepContents());
					A.setMiscText(CMLib.coffeeMaker().getItemsXML(items, new Hashtable<String,List<Item>>(),new HashSet<String>(),null).toString());
					A.previousItems = items;
					ItemPossessor possessor = targetI.owner();
					if(possessor != null)
					{
						for(Item I : items)
							possessor.delItem(I);
						possessor.addItem(intoI);
					}
					else
						mob.addItem(intoI);
				}
				else
					mob.addItem(intoI);
				mob.location().recoverRoomStats();
			}
		}
		else
		{
			
			return beneficialVisualFizzle(mob,targetI,L("<S-NAME> attempt(s) to polymorph <T-NAME> into @x1, but flub(s) it.",intoWhat));
		}

		// return whether it worked
		return success;
	}
}
