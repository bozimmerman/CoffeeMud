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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Spell_MinorImage extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_MinorImage";
	}

	private final static String	localizedName	= CMLib.lang().L("Minor Image");

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
		return Ability.ACODE_SPELL | Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected int getDuration(final MOB caster, final int asLevel)
	{
		return 10 + (super.adjustedLevel(caster, asLevel) /6);
	}
	
	protected boolean canSeeAppearance()
	{
		return false;
	}
	
	protected boolean canTargetOthers()
	{
		return false;
	}
	
	protected volatile MOB parentM = null;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			if(CMLib.players().playerExists(newMiscText))
				parentM=CMLib.players().getLoadPlayer(newMiscText);
		}
	}
	
	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((mob!=null)&&(mob.playerStats()==null))
			{
				final Room R=mob.location();
				if(R!=null)
					R.show(mob,null,null,CMMsg.MSG_OK_VISUAL, L("<S-NAME> vanish(es)!"));
				if(mob.amDead())
					mob.setLocation(null);
				mob.destroy();
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected==null)||(parentM==null)||(affected==parentM))
			return;
		affectableStats.setName(parentM.Name());
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOLEM);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if((affected==null)||(parentM==null)||(affected==parentM))
			return;
		affectableStats.setRaceName(parentM.charStats().raceName());
		affectableStats.setDisplayClassName(parentM.charStats().displayClassName());
		affectableStats.setGenderName(parentM.charStats().genderName());
		affectableStats.setStat(CharStats.STAT_WISDOM, 1);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE, 1);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected instanceof MOB)&&(affected != parentM))
		{
			final MOB simulacruM=(MOB)affected;
			if(msg.amISource(simulacruM) && (!CMath.bset(msg.sourceMajor(), CMMsg.MASK_ALWAYS)))
			{
				msg.source().tell("You can't do anything, you're just a stationary illusion!");
				return false;
			}
			else
			if(msg.amITarget(simulacruM))
			{
				if(CMath.bset(msg.targetMajor(), CMMsg.MASK_MALICIOUS))
					unInvoke();
				else
				if((msg.tool() instanceof Ability)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_DIVINATION))
					return true;
				else
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_DAMAGE:
				case CMMsg.TYP_WEAPONATTACK:
				case CMMsg.TYP_CAST_SPELL:
				case CMMsg.TYP_JUSTICE:
				case CMMsg.TYP_FIRE:
				case CMMsg.TYP_COLD:
				case CMMsg.TYP_WATER:
				case CMMsg.TYP_GAS:
				case CMMsg.TYP_MIND:
				case CMMsg.TYP_GENERAL:
				case CMMsg.TYP_ACID:
				case CMMsg.TYP_ELECTRIC:
				case CMMsg.TYP_POISON:
				case CMMsg.TYP_UNDEAD:
					unInvoke();
					break;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target;
		if(canTargetOthers())
		{
			target=super.getTarget(mob, commands, givenTarget);
			if(target==null)
				return false;
		}
		else
		if(givenTarget instanceof MOB)
			target=(MOB)givenTarget;
		else
			target=mob;
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				MOB M=determineMonster(target,target.location(),target.phyStats().level());
				Spell_MinorImage A = (Spell_MinorImage)beneficialAffect(mob,M,asLevel,getDuration(mob,asLevel));
				if(A!=null)
				{
					A.setMiscText(target.Name());
					A.parentM=target;
					mob.location().show(target,M,CMMsg.MSG_OK_VISUAL,L("An image of <S-NAME> appears!"));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> arms around <T-NAMESELF>, but nothing happens."));
		// return whether it worked
		return success;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		
		final Physical affected=this.affected;
		if(affected instanceof MOB)
			((MOB)affected).makePeace(false);
		
		return true;
	}
	
	public List<Tattoo> getSeenTattoos(MOB mob)
	{
		long wornCode=0;
		final List<Tattoo> seenTatts = new ArrayList<Tattoo>();
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			wornCode=codes.all_ordered()[l];
			final List<Item> wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
			int numLocations=mob.getWearPositions(wornCode);
			if(numLocations==0)
				numLocations=1;
			int emptySlots=numLocations;
			if(wornHere.size()>0)
			{
				final List<List<Item>> sets=new Vector<List<Item>>(numLocations);
				for(int i=0;i<numLocations;i++)
					sets.add(new Vector<Item>());
				Item I=null;
				Item I2=null;
				short layer=Short.MAX_VALUE;
				short layerAtt=0;
				short layer2=Short.MAX_VALUE;
				short layerAtt2=0;
				List<Item> set=null;
				for(int i=0;i<wornHere.size();i++)
				{
					I=wornHere.get(i);
					if(I.container()!=null)
						continue;
					if(I instanceof Armor)
					{
						layer=((Armor)I).getClothingLayer();
						layerAtt=((Armor)I).getLayerAttributes();
					}
					else
					{
						layer=0;
						layerAtt=0;
					}
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						if(set.size()==0)
						{
							set.add(I);
							break;
						}
						for(int s2=0;s2<set.size();s2++)
						{
							I2=set.get(s2);
							if(I2 instanceof Armor)
							{
								layer2=((Armor)I2).getClothingLayer();
								layerAtt2=((Armor)I2).getLayerAttributes();
							}
							else
							{
								layer2=0;
								layerAtt2=0;
							}
							if(layer2==layer)
							{
								if(((layerAtt&Armor.LAYERMASK_MULTIWEAR)>0)
								&&((layerAtt2&Armor.LAYERMASK_MULTIWEAR)>0))
									set.add(s2,I);
								break;
							}
							if(layer2>layer)
							{
								set.add(s2,I);
								break;
							}
						}
						if(set.contains(I))
							break;
						if(layer2<layer)
						{
							set.add(I);
							break;
						}
					}
					wornHere.clear();
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						int s2=set.size()-1;
						for(;s2>=0;s2--)
						{
							I2=set.get(s2);
							wornHere.add(I2);
							if((!(I2 instanceof Armor))
							||(!CMath.bset(((Armor)I2).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)))
							{
								emptySlots--;
								break;
							}
						}
					}
				}
			}
			if(emptySlots>0)
			{
				double numTattoosTotal=0;
				String wornName=codes.name(wornCode).toUpperCase();
				for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
				{
					final Tattoo T = e.nextElement();
					if(T.getTattooName().startsWith(wornName+":"))
						numTattoosTotal+=1.0;
				}
				int numTattoosToShow=(int)Math.round(Math.ceil(CMath.mul(numTattoosTotal,CMath.div(emptySlots,numLocations))));
				for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
				{
					final Tattoo T = e.nextElement();
					if((T.getTattooName().startsWith(wornName+":"))
					&&((--numTattoosToShow)>=0))
					{
						
						seenTatts.add(T);
					}
				}
			}
		}
		return seenTatts;
	}
	
	public PairList<Item, Long> getSeenEquipment(MOB mob)
	{
		long wornCode=0;
		Item thisItem=null;
		final PairList<Item, Long> seenEQ = new PairVector<Item, Long>();
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int l=0;l<codes.all_ordered().length;l++)
		{
			wornCode=codes.all_ordered()[l];
			final List<Item> wornHere=mob.fetchWornItems(wornCode,(short)(Short.MIN_VALUE+1),(short)0);
			int numLocations=mob.getWearPositions(wornCode);
			if(numLocations==0)
				numLocations=1;
			if(wornHere.size()>0)
			{
				final List<List<Item>> sets=new Vector<List<Item>>(numLocations);
				for(int i=0;i<numLocations;i++)
					sets.add(new Vector<Item>());
				Item I=null;
				Item I2=null;
				short layer=Short.MAX_VALUE;
				short layerAtt=0;
				short layer2=Short.MAX_VALUE;
				short layerAtt2=0;
				List<Item> set=null;
				for(int i=0;i<wornHere.size();i++)
				{
					I=wornHere.get(i);
					if(I.container()!=null)
						continue;
					if(I instanceof Armor)
					{
						layer=((Armor)I).getClothingLayer();
						layerAtt=((Armor)I).getLayerAttributes();
					}
					else
					{
						layer=0;
						layerAtt=0;
					}
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						if(set.size()==0)
						{
							set.add(I);
							break;
						}
						for(int s2=0;s2<set.size();s2++)
						{
							I2=set.get(s2);
							if(I2 instanceof Armor)
							{
								layer2=((Armor)I2).getClothingLayer();
								layerAtt2=((Armor)I2).getLayerAttributes();
							}
							else
							{
								layer2=0;
								layerAtt2=0;
							}
							if(layer2==layer)
							{
								if(((layerAtt&Armor.LAYERMASK_MULTIWEAR)>0)
								&&((layerAtt2&Armor.LAYERMASK_MULTIWEAR)>0))
									set.add(s2,I);
								break;
							}
							if(layer2>layer)
							{
								set.add(s2,I);
								break;
							}
						}
						if(set.contains(I))
							break;
						if(layer2<layer)
						{
							set.add(I);
							break;
						}
					}
					wornHere.clear();
					for(int s=0;s<sets.size();s++)
					{
						set=sets.get(s);
						int s2=set.size()-1;
						for(;s2>=0;s2--)
						{
							I2=set.get(s2);
							wornHere.add(I2);
							if((!(I2 instanceof Armor))
							||(!CMath.bset(((Armor)I2).getLayerAttributes(),Armor.LAYERMASK_SEETHROUGH)))
							{
								break;
							}
						}
					}
				}
				for(int i=0;i<wornHere.size();i++)
				{
					thisItem=wornHere.get(i);
					if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
					{
						if(CMLib.flags().isSeeable(thisItem))
							seenEQ.add(thisItem, Long.valueOf(thisItem.rawWornCode()));
					}
				}
			}
		}
		return seenEQ;
	}

	public MOB determineMonster(MOB target, Room R, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
		newMOB.basePhyStats().setLevel(target.basePhyStats().level());
		newMOB.basePhyStats().setWeight(target.basePhyStats().weight());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.setSavable(false);
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setSpeed(target.basePhyStats().speed());
		newMOB.setName(L("an image of @x1",target.Name()));
		newMOB.setDisplayText(L("@x1 is here.",target.Name()));
		newMOB.setDescription(target.description());
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(target.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		if(canSeeAppearance())
		{
			PairList<Item, Long> eq = this.getSeenEquipment(target);
			for(Pair<Item, Long> e : eq)
			{
				Item eqI = CMClass.getItem("GenItem");
				eqI.setName(e.first.Name());
				eqI.setRawProperLocationBitmap(e.first.rawProperLocationBitmap());
				eqI.setRawLogicalAnd(e.first.rawLogicalAnd());
				eqI.basePhyStats().setDisposition(e.first.phyStats().disposition());
				eqI.basePhyStats().setSensesMask(e.first.phyStats().sensesMask());
				eqI.setDescription("You can see through it!");
				eqI.setMaterial(RawMaterial.RESOURCE_NOTHING);
				eqI.basePhyStats().setWeight(0);
				eqI.recoverPhyStats();
				CMLib.flags().setRemovable(eqI, false);
				CMLib.flags().setGettable(eqI, false);
				eqI.recoverPhyStats();
				newMOB.addItem(eqI);
				eqI.wearAt(e.second.longValue());
			}
			List<Tattoo> tatts = this.getSeenTattoos(target);
			for(Tattoo T : tatts)
			{
				T=(Tattoo)T.copyOf();
				newMOB.addTattoo(T);
			}
			newMOB.recoverCharStats();
			newMOB.recoverPhyStats();
			newMOB.recoverMaxState();
		}
		R.recoverRoomStats();
		newMOB.setStartRoom(null);
		//CMLib.threads().deleteAllTicks(newMOB); // really bad idea! you need the tick to end the effect!
		return(newMOB);
	}

}
