package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Sense extends StdLibrary implements CMFlagLibrary
{
	@Override
	public String ID()
	{
		return "Sense";
	}

	@Override
	public boolean canSee(MOB M)
	{
		return (M != null) && (!isSleeping(M)) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_SEE) == 0);
	}

	@Override
	public boolean canBeLocated(Physical P)
	{
		if(P instanceof MOB)
			return true;
		else
		if ((P != null) 
		&& (!isSleeping(P)) 
		&& ((P.phyStats().sensesMask() & PhyStats.SENSE_UNLOCATABLE) == 0))
		{
			if ((P instanceof Item) && (((Item) P).container() != null))
				return canBeLocated(((Item) P).container());
			return true;
		}
		return false;
	}

	@Override
	public boolean canSeeHidden(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_HIDDEN) == PhyStats.CAN_SEE_HIDDEN);
	}

	@Override
	public boolean canSeeHiddenItems(MOB M)
	{
		return (M != null) 
			&& (((M.phyStats().sensesMask() & PhyStats.CAN_SEE_HIDDEN_ITEMS) == PhyStats.CAN_SEE_HIDDEN_ITEMS)
				||((M.phyStats().sensesMask() & PhyStats.CAN_SEE_HIDDEN) == PhyStats.CAN_SEE_HIDDEN));
	}
	
	@Override
	public boolean canSeeInvisible(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_INVISIBLE) == PhyStats.CAN_SEE_INVISIBLE);
	}

	@Override
	public boolean canSeeEvil(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_EVIL) == PhyStats.CAN_SEE_EVIL);
	}

	@Override
	public boolean canSeeGood(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_GOOD) == PhyStats.CAN_SEE_GOOD);
	}

	@Override
	public boolean canSeeSneakers(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_SNEAKERS) == PhyStats.CAN_SEE_SNEAKERS);
	}

	@Override
	public boolean canSeeBonusItems(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_BONUS) == PhyStats.CAN_SEE_BONUS);
	}

	@Override
	public boolean canSeeInDark(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_DARK) == PhyStats.CAN_SEE_DARK);
	}

	@Override
	public boolean canSeeVictims(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_VICTIM) == PhyStats.CAN_SEE_VICTIM);
	}

	@Override
	public boolean canSeeInfrared(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_INFRARED) == PhyStats.CAN_SEE_INFRARED);
	}

	@Override
	public boolean canHear(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_HEAR) == 0);
	}

	@Override
	public boolean canTrack(Physical P)
	{
		return (P != null) && ((P.phyStats().sensesMask() & PhyStats.CAN_NOT_TRACK) == 0);
	}

	@Override
	public boolean canAutoAttack(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_AUTO_ATTACK) == 0);
	}

	@Override
	public boolean canConcentrate(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_THINK) == 0);
	}

	@Override
	public boolean canMove(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_MOVE) == 0);
	}

	@Override
	public boolean allowsMovement(Room R)
	{
		return (R != null) && ((R.phyStats().sensesMask() & PhyStats.SENSE_ROOMNOMOVEMENT) == 0);
	}

	@Override
	public boolean allowsMovement(Area A)
	{
		return (A != null) && ((A.phyStats().sensesMask() & PhyStats.SENSE_ROOMNOMOVEMENT) == 0);
	}

	@Override
	public boolean canSmell(MOB M)
	{
		return canBreatheHere(M, M.location()) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_SMELL) == 0);
	}

	@Override
	public boolean canTaste(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_TASTE) == 0);
	}

	@Override
	public boolean canSpeak(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_SPEAK) == 0);
	}

	@Override
	public boolean canBreathe(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_NOT_BREATHE) == 0);
	}

	@Override
	public boolean canNotBeCamped(Physical P)
	{
		return (P != null) && ((P.phyStats().sensesMask() & PhyStats.CAN_NOT_BE_CAMPED) != 0);
	}

	@Override
	public boolean canBreatheThis(MOB M, int atmoResource)
	{
		return (canBreathe(M)
				&&((atmoResource<0)
					||(M.charStats().getBreathables().length==0)
					||(Arrays.binarySearch(M.charStats().getBreathables(), atmoResource)>=0)));
	}

	@Override
	public boolean canBreatheHere(MOB M, Room R)
	{
		return (M != null) && (canBreatheThis(M, (R == null) ? -1 : R.getAtmosphere()));
	}

	@Override
	public boolean canSeeMetal(MOB M)
	{
		return (M != null) && ((M.phyStats().sensesMask() & PhyStats.CAN_SEE_METAL) == PhyStats.CAN_SEE_METAL);
	}

	@Override
	public boolean isReadable(Item I)
	{
		return (I != null) && ((I.phyStats().sensesMask() & PhyStats.SENSE_ITEMREADABLE) == PhyStats.SENSE_ITEMREADABLE);
	}

	@Override
	public boolean isGettable(Item I)
	{
		return (I != null) && ((I.phyStats().sensesMask() & PhyStats.SENSE_ITEMNOTGET) == 0);
	}

	@Override
	public boolean isDroppable(Item I)
	{
		return (I != null) && ((I.phyStats().sensesMask() & PhyStats.SENSE_ITEMNODROP) == 0);
	}

	@Override
	public boolean isRemovable(Item I)
	{
		return (I != null) && ((I.phyStats().sensesMask() & PhyStats.SENSE_ITEMNOREMOVE) == 0);
	}

	@Override
	public boolean isCataloged(Environmental E)
	{
		return (E instanceof Physical) 
				&& ((((Physical) E).basePhyStats().disposition() & PhyStats.IS_CATALOGED) == PhyStats.IS_CATALOGED);
	}

	private Room roomLocation(Environmental E)
	{
		if(E==null)
			return null;
		if((E instanceof Area)&&(!((Area)E).isProperlyEmpty()))
			return ((Area)E).getRandomProperRoom();
		else
		if(E instanceof Room)
			return (Room)E;
		else
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
			return ((MOB)((Item)E).owner()).location();
		else
		if(E instanceof Ability)
			return roomLocation(((Ability)E).affecting());
		else
		if(E instanceof Exit)
			return roomLocation(((Exit)E).lastRoomUsedFrom(null));
		return null;
	}
	
	@Override
	public boolean isWithSeenContents(Physical P)
	{
		return (P != null)
			&&((P instanceof MOB)
				||((P.phyStats().sensesMask() & PhyStats.SENSE_CONTENTSUNSEEN) == 0));
	}

	@Override
	public boolean isSavable(Physical P)
	{
		if((P==null)||((P.phyStats().disposition()&PhyStats.IS_UNSAVABLE)==0))
		{
			if((P instanceof Item)
			&&(((Item)P).container()!=null)
			&&(((Item)P).container()!=P))
				return isSavable(((Item)P).container());
			return true;
		}
		return false;
	}

	@Override
	public void setSavable(Physical P, boolean truefalse)
	{
		if(P==null)
			return;
		if(CMath.bset(P.basePhyStats().disposition(),PhyStats.IS_UNSAVABLE))
		{
			if(truefalse)
			{
				P.basePhyStats().setDisposition(CMath.unsetb(P.basePhyStats().disposition(),PhyStats.IS_UNSAVABLE));
				P.phyStats().setDisposition(CMath.unsetb(P.phyStats().disposition(),PhyStats.IS_UNSAVABLE));
			}
		}
		else
		if(!truefalse)
		{
			P.basePhyStats().setDisposition(CMath.setb(P.basePhyStats().disposition(),PhyStats.IS_UNSAVABLE));
			P.phyStats().setDisposition(CMath.setb(P.phyStats().disposition(),PhyStats.IS_UNSAVABLE));
		}
	}

	@Override
	public void setReadable(Item I, boolean truefalse)
	{
		if(I==null)
			return;
		if(CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE))
		{
			if(!truefalse)
			{
				I.basePhyStats().setSensesMask(CMath.unsetb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE));
				I.phyStats().setSensesMask(CMath.unsetb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE));
			}
		}
		else
		if(truefalse)
		{
			I.basePhyStats().setSensesMask(CMath.setb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE));
			I.phyStats().setSensesMask(CMath.setb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMREADABLE));
		}
	}

	@Override
	public boolean isEnspelled(Physical F)
	{
		for(int a=0;a<F.numEffects();a++) // personal affects
		{
			final Ability A=F.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())&&(!A.isSavable())
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
				return true;
		}
		return false;
	}

	@Override
	public void setGettable(Item I, boolean truefalse)
	{
		if(I==null)
			return;
		if(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET))
		{
			if(!truefalse)
			{
				I.basePhyStats().setSensesMask(CMath.setb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
				I.phyStats().setSensesMask(CMath.setb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
			}
		}
		else
		if(truefalse)
		{
			I.basePhyStats().setSensesMask(CMath.unsetb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
			I.phyStats().setSensesMask(CMath.unsetb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNOTGET));
		}
	}

	@Override
	public void setDroppable(Item I, boolean truefalse)
	{
		if(I==null)
			return;
		if(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP))
		{
			if(!truefalse)
			{
				I.basePhyStats().setSensesMask(CMath.setb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP));
				I.phyStats().setSensesMask(CMath.setb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP));
			}
		}
		else
		if(truefalse)
		{
			I.basePhyStats().setSensesMask(CMath.unsetb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP));
			I.phyStats().setSensesMask(CMath.unsetb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNODROP));
		}
	}

	@Override
	public void setRemovable(Item I, boolean truefalse)
	{
		if(I==null)
			return;
		if(!CMath.bset(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE))
		{
			if(!truefalse)
			{
				I.basePhyStats().setSensesMask(CMath.setb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE));
				I.phyStats().setSensesMask(CMath.setb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE));
			}
		}
		else
		if(truefalse)
		{
			I.basePhyStats().setSensesMask(CMath.unsetb(I.basePhyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE));
			I.phyStats().setSensesMask(CMath.unsetb(I.phyStats().sensesMask(),PhyStats.SENSE_ITEMNOREMOVE));
		}
	}

	@Override
	public boolean isSeeable(Physical P)
	{ 
		return (P!=null)&&(((P.phyStats().disposition()&PhyStats.IS_NOT_SEEN)==0) || isSleeping(P)); 
	}
	
	@Override
	public boolean isCloaked(Physical P)
	{ 
		return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_CLOAKED)==PhyStats.IS_CLOAKED);
	}
	
	@Override
	public boolean isHidden(Physical P)
	{
		if(P==null)
			return false;
		final boolean isInHide=((P.phyStats().disposition()&PhyStats.IS_HIDDEN)==PhyStats.IS_HIDDEN);
		if((P instanceof MOB)
		&&(isInHide)
		&&(((MOB)P).isInCombat()))
			return false;
		return isInHide;
	}

	@Override
	public boolean isUndead(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_UNDEAD).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isEggLayer(final Race race)
	{
		final String[] eggLayers = new String[] 
		{
			"Amphibian",
			"Avian",
			"Dragon",
			"Reptile",
			"Ophidian",
			"Fish"
		};
		return (race == null) ? false : CMStrings.containsIgnoreCase(eggLayers, race.racialCategory());
	}

	@Override
	public boolean isFish(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_FISH).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isMarine(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_MARINE).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isOutsider(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_OUTSIDER).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isInsect(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_INSECT).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isVermin(MOB mob)
	{
		return (mob != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_VERMIN).contains(mob.charStats().getMyRace().racialCategory());
	}
	
	@Override
	public boolean isAPlant(Item I)
	{
		return (I!=null) && ((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION);
	}
	
	@Override
	public boolean isAPlant(MOB M)
	{
		if(isGolem(M) && (M!=null) && (M.charStats()!=null) && (M.charStats().getMyRace()!=null))
		{
			final List<RawMaterial> mats = M.charStats().getMyRace().myResources();
			for(final Item I : mats)
			{
				if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
					return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public boolean isUnattackable(Physical P)
	{ 
		return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_UNATTACKABLE)==PhyStats.IS_UNATTACKABLE); 
	}
	
	@Override
	public boolean isInvisible(Physical P)
	{ 
		return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_INVISIBLE)==PhyStats.IS_INVISIBLE); 
	}

	@Override
	public boolean isRejuvingItem(Item I)
	{
		if(I==null)
			return false;
		for(int i=0;i<I.numEffects();i++)
		{
			if(I.fetchEffect(i) instanceof ItemTicker)
				return true;
		}
		return false;
	}

	@Override
	public boolean isReallyEvil(FactionMember M)
	{
		if(M != null)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(final Enumeration<String> e=M.factions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),M.fetchFaction(F.factionID()));
					if((FR!=null)&&(FR.alignEquiv()==Faction.Align.EVIL))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isEvil(Physical P)
	{
		if(P==null)
			return false;
		if ((P.phyStats().disposition()&PhyStats.IS_EVIL)==PhyStats.IS_EVIL)
			return true;
		else
		if(P instanceof FactionMember)
			return isReallyEvil((FactionMember)P);
		return false;
	}
	
	@Override
	public boolean isOpenAccessibleContainer(Item I)
	{
		return (I instanceof Container)
			 &&(CMath.bset(I.phyStats().sensesMask(), PhyStats.SENSE_INSIDEACCESSIBLE))
			 &&((Container)I).isOpen();
	}
	
	@Override
	public boolean isTracking(MOB M)
	{
		if(M!=null)
			return flaggedAffects(M,Ability.FLAG_TRACKING).size()>0;
		return false;
	}

	@Override
	public boolean isATrackingMonster(MOB M)
	{
		if(M==null)
			return false;
		if(M.isMonster())
			return flaggedAffects(M,Ability.FLAG_TRACKING).size()>0;
		return false;
	}

	@Override
	public boolean isReallyGood(FactionMember M)
	{
		if(M != null)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(final Enumeration<String> e=M.factions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),M.fetchFaction(F.factionID()));
					if((FR!=null)&&(FR.alignEquiv()==Faction.Align.GOOD))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getAge(MOB M)
	{
		final Ability A=M.fetchEffect("Age");
		if((A==null)||(A.displayText().length()==0))
		{
			if(M.baseCharStats().getStat(CharStats.STAT_AGE)==0)
				return "unknown";
			return L("@x1 year(s) old",""+M.baseCharStats().getStat(CharStats.STAT_AGE));
		}
		else
		{
			String s=A.displayText();
			if(s.startsWith("(")) 
				s=s.substring(1);
			if(s.endsWith(")")) 
				s=s.substring(0,s.length()-1);
			return s;
		}
	}

	@Override
	public boolean isGood(Physical P)
	{
		if(P==null)
			return false;
		if ((P.phyStats().disposition()&PhyStats.IS_GOOD)==PhyStats.IS_GOOD)
			return true;
		else
		if(P instanceof FactionMember)
			return isReallyGood((FactionMember)P);
		return false;
	}

	@Override
	public boolean isTrapped(Physical P)
	{
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A instanceof Trap))
				return true;
		}
		return false;
	}

	@Override
	public boolean isDeadlyOrMaliciousEffect(final PhysicalAgent P)
	{
		if(P==null)
			return false;
		if(flaggedBehaviors(P, Behavior.FLAG_POTENTIALLYAUTODEATHING).size()>0)
			return true;
		if(isTrapped(P))
			return true;
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A instanceof AbilityContainer)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY))
			{
				final AbilityContainer U=(AbilityContainer)A;
				for(final Enumeration<Ability> e=U.allAbilities();e.hasMoreElements();)
				{
					final Ability uA=e.nextElement();
					if((uA!=null)&&(uA.abstractQuality()==Ability.QUALITY_MALICIOUS))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isPossiblyAggressive(MOB M)
	{
		if(M==null)
			return false;
		final List<Behavior> V=flaggedBehaviors(M,Behavior.FLAG_POTENTIALLYAGGRESSIVE);
		return ((V==null)||(V.size()==0))? false:true;
	}

	@Override
	public boolean isAggressiveTo(MOB M, MOB toM)
	{
		if((M==null)||(toM==null))
			return false;
		final List<Behavior> V=flaggedBehaviors(M,Behavior.FLAG_POTENTIALLYAGGRESSIVE);
		if((V==null)||(V.size()==0))
			return false;
		for(final Behavior B : V)
		{
			if(B.grantsAggressivenessTo(toM))
				return true;
		}
		return false;
	}

	@Override
	public String getAlignmentName(Environmental E)
	{
		if(E instanceof Physical)
		{
			if((((Physical)E).phyStats().disposition()&PhyStats.IS_GOOD)==PhyStats.IS_GOOD)
				return Faction.Align.GOOD.toString();
			if((((Physical)E).phyStats().disposition()&PhyStats.IS_EVIL)==PhyStats.IS_EVIL)
				return Faction.Align.EVIL.toString();
		}
		if(E instanceof MOB)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(final Enumeration<String> e=((MOB)E).factions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),((MOB)E).fetchFaction(F.factionID()));
					if((FR!=null)&&((FR.alignEquiv()==Align.GOOD)||(FR.alignEquiv()==Align.EVIL)))
						return FR.alignEquiv().toString();
				}
			}
		}
		return Faction.Align.NEUTRAL.toString();
	}

	@Override
	public boolean isReallyNeutral(FactionMember M)
	{
		if(M != null)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(final Enumeration<String> e=M.factions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),M.fetchFaction(F.factionID()));
					if(FR!=null)
					switch(FR.alignEquiv())
					{
						case NEUTRAL:
							return true;
						case EVIL:
							return false;
						case GOOD:
							return false;
						default:
							continue;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean isNeutral(Physical P)
	{
		if(P==null)
			return false;
		if(((P.phyStats().disposition()&PhyStats.IS_GOOD)==PhyStats.IS_GOOD)
		|| ((P.phyStats().disposition()&PhyStats.IS_EVIL)==PhyStats.IS_EVIL))
			return false;
		else
		if(P instanceof FactionMember)
			return isReallyNeutral((FactionMember)P);
		return true;
	}

	@Override
	public boolean isSneaking(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_SNEAKING) == PhyStats.IS_SNEAKING);
	}

	@Override
	public boolean isABonusItems(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_BONUS) == PhyStats.IS_BONUS);
	}

	@Override
	public boolean isInDark(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_DARK) == PhyStats.IS_DARK);
	}

	@Override
	public boolean isLightSource(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_LIGHTSOURCE) == PhyStats.IS_LIGHTSOURCE);
	}

	@Override
	public boolean isGlowing(Physical P)
	{
		return (P != null) && ((isLightSource(P) || ((P.phyStats().disposition() & PhyStats.IS_GLOWING) == PhyStats.IS_GLOWING)));
	}

	@Override
	public boolean isGolem(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_GOLEM) == PhyStats.IS_GOLEM);
	}

	@Override
	public boolean isSleeping(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_SLEEPING) == PhyStats.IS_SLEEPING);
	}

	@Override
	public boolean isSitting(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_SITTING) == PhyStats.IS_SITTING);
	}

	@Override
	public boolean isFlying(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_FLYING) == PhyStats.IS_FLYING);
	}

	@Override
	public boolean isClimbing(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_CLIMBING) == PhyStats.IS_CLIMBING);
	}

	@Override
	public boolean isCrawlable(Physical P)
	{
		if((P instanceof Room)||(P instanceof Area)||(P instanceof Exit))
			return (P.phyStats().sensesMask() & PhyStats.SENSE_ROOMCRUNCHEDIN) == PhyStats.SENSE_ROOMCRUNCHEDIN;
		return false;
	}

	@Override
	public boolean isSwimming(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_SWIMMING) == PhyStats.IS_SWIMMING);
	}

	@Override
	public boolean isFalling(Physical P)
	{
		return (P != null) && ((P.phyStats().disposition() & PhyStats.IS_FALLING) == PhyStats.IS_FALLING);
	}

	@Override
	public int getFallingDirection(Physical P)
	{
		if(!isFalling(P))
			return -1;
		Ability A=P.fetchEffect("Falling");
		if((A!=null) && (CMath.s_bool(A.getStat("REVERSED"))))
			return Directions.UP;
		return Directions.DOWN;
	}
	
	@Override
	public boolean isRunningLongCommand(MOB M)
	{
		return (M != null) 
			&& (M.session() != null) 
			&& ((System.currentTimeMillis() - M.session().getInputLoopTime()) > 30000);
	}

	@Override
	public boolean isSwimmingInWater(Physical P)
	{
		if(!isSwimming(P))
			return false;
		return isWateryRoom(roomLocation(P));
	}
	
	@Override
	public boolean isInWilderness(Physical P)
	{
		if(P instanceof MOB)
			return isInWilderness(((MOB)P).location());
		else
		if(P instanceof Item)
			return isInWilderness(((Item)P).owner());
		else
		if(P instanceof Room)
		{
			return (((Room)P).domainType()!=Room.DOMAIN_OUTDOORS_CITY)
					&&(((Room)P).domainType()!=Room.DOMAIN_OUTDOORS_SPACEPORT)
					&&(((Room)P).domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
					&&(((Room)P).domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&((((Room)P).domainType()&Room.INDOORS)==0);
		}
		else
			return false;
	}

	@Override
	public boolean canBeHeardMovingBy(Physical heard , MOB hearer)
	{
		if(hearer==heard)
			return true;
		if(hearer==null)
			return false;
		if(heard==null)
			return false;
		if((!isSeeable(heard))&&(isCloaked(heard)))
		{
			if((!(heard instanceof MOB))
			||(heard.phyStats().level()>hearer.phyStats().level())
			||(!CMSecurity.isASysOp(hearer)))
				return false;
		}
		if(!canHear(hearer))
			return false;
		if(isSneaking(heard)&&(!canSeeSneakers(hearer)))
			return false;
		return true;
	}

	@Override
	public boolean canBeHeardSpeakingBy(Physical heard , MOB hearer)
	{
		if(hearer==heard)
			return true;
		if(hearer==null)
			return false;
		if(heard==null)
			return false;
		if(!canHear(hearer))
			return false;
		return true;
	}

	@Override
	public boolean canSenseMoving(Physical sensed, MOB sensor)
	{
		return (canBeHeardMovingBy(sensed,sensor)||canBeSeenBy(sensed,sensor));
	}

	@Override
	public boolean canSenseEnteringLeaving(Physical sensed, MOB sensor)
	{
		return canBeHeardMovingBy(sensed,sensor);
	}

	@Override
	public boolean isAliveAwakeMobileUnbound(MOB mob, boolean quiet)
	{
		if(!isAliveAwakeMobile(mob,quiet))
			return false;
		if(isBound(mob))
		{
			if(!quiet)
				mob.tell(L("You are bound!"));
			return false;
		}
		if(isBoundOrHeld(mob))
		{
			if(!quiet)
				mob.tell(L("You are paralyzed!"));
			return false;
		}
		return true;
	}

	@Override
	public boolean isAliveAwakeMobile(final MOB mob, final boolean quiet)
	{
		if(mob==null)
			return false;
		if(quiet)
		{
			if(mob.amDead()
			||(mob.curState().getHitPoints()<0)
			||((mob.phyStats().disposition()&PhyStats.IS_SLEEPING)!=0)
			||((mob.phyStats().sensesMask()&PhyStats.CAN_NOT_MOVE)!=0))
				return false;
			return true;
		}
		if(mob.amDead()||(mob.curState().getHitPoints()<0))
		{
			mob.tell(L("You are DEAD!"));
			return false;
		}
		if(isSleeping(mob))
		{
			mob.tell(L("You are sleeping!"));
			return false;
		}
		if(!canMove(mob))
		{
			mob.tell(L("You can't move!"));
			return false;
		}
		return true;
	}

	@Override
	public boolean isStanding(MOB mob)
	{
		return (!isSitting(mob))&&(!isSleeping(mob));
	}

	@Override
	public boolean isBound(Physical P)
	{
		if((P!=null)&&((P.phyStats().disposition()&PhyStats.IS_BOUND)==PhyStats.IS_BOUND))
			return true;
		return false;
	}

	@Override
	public boolean isBoundOrHeld(Physical P)
	{
		if(P==null)
			return false;
		if((P.phyStats().disposition()&PhyStats.IS_BOUND)==PhyStats.IS_BOUND)
			return true;
		return flaggedAnyAffects(P,Ability.FLAG_BINDING|Ability.FLAG_PARALYZING).size()>0;
	}

	@Override
	public boolean isOnFire(Physical seen)
	{
		if(seen==null)
			return false;
		if(seen.fetchEffect("Burning")!=null)
			return true;
		if(seen.fetchEffect("Prayer_FlameWeapon")!=null)
			return true;
		if(!(seen instanceof Light))
			return false;
		final Light light=(Light)seen;
		if(light.goesOutInTheRain() && light.isLit())
			return true;
		return false;
	}

	@Override
	public boolean isFloatingFreely(Physical P)
	{
		return ((P.fetchEffect("GravityFloat")!=null)&&(P.phyStats().isAmbiance(L("Floating"))));
	}
	
	@Override
	public int getHideScore(Physical seen)
	{
		if(seen!=null)
		{
			int hideFactor=seen.phyStats().level();
			if(seen instanceof MOB)
				hideFactor+=(((MOB)seen).charStats().getStat(CharStats.STAT_DEXTERITY))/2;
			if(CMath.bset(seen.basePhyStats().disposition(),PhyStats.IS_HIDDEN))
				hideFactor+=100;
			else
			if(seen instanceof MOB)
			{
				hideFactor+=((MOB)seen).charStats().getSave(CharStats.STAT_SAVE_DETECTION);
				if(seen.phyStats().height()>=0)
					hideFactor-=(int)Math.round(Math.sqrt(seen.phyStats().height()));
			}
			else
				hideFactor+=100;
			return hideFactor;
		}
		return 0;
	}

	@Override
	public int getDetectScore(MOB seer)
	{
		if(seer!=null)
		{
			int detectFactor=seer.charStats().getStat(CharStats.STAT_WISDOM)/2;
			if(CMath.bset(seer.basePhyStats().sensesMask(),PhyStats.CAN_SEE_HIDDEN))
				detectFactor+=100;
			else // the 100 represents proff, and level represents time searching.
				detectFactor+=seer.charStats().getSave(CharStats.STAT_SAVE_OVERLOOKING);
			if(seer.phyStats().height()>=0)
				detectFactor-=(int)Math.round(Math.sqrt(seer.phyStats().height()));
			return detectFactor;
		}
		return 0;
	}

	@Override
	public boolean canBeSeenBy(Environmental seen , MOB seer)
	{
		if(seer==seen)
			return true;
		if(seen==null)
			return true;

		if((seer!=null)
		&&(seer.isAttributeSet(MOB.Attrib.SYSOPMSGS)))
			return true;

		if(!canSee(seer))
			return false;

		if(!(seen instanceof Physical))
			return true;

		final Physical seenP=(Physical)seen;

		if((!isSeeable(seenP))&&(seer!=null))
		{
			if((!(seenP instanceof MOB))
			||(seenP.phyStats().level()>seer.phyStats().level())
			||(!CMSecurity.isASysOp(seer)))
				return false;
		}

		if((isInvisible(seenP))&&(!canSeeInvisible(seer)))
			return false;

		if((isHidden(seenP))&&(!(seenP instanceof Room)))
		{
			if(seer == null)
				return false;
			if(seenP instanceof Item)
			{
				if(!canSeeHiddenItems(seer))
					return false;
			}
			else
			if(!canSeeHidden(seer))
			{
				if((!(seen instanceof MOB))
				||(getDetectScore(seer)<getHideScore((MOB)seen)))
					return false;
			}
		}

		if((seer!=null)&&(!(seenP instanceof Room)))
		{
			final Room R=seer.location();
			if((R!=null)&&(isInDark(R)))
			{
				if((isGlowing(seenP))||(isLightSource(seer)))
					return true;
				if(canSeeInDark(seer))
					return true;
				if((!isGolem(seenP))&&(canSeeInfrared(seer))&&(seenP instanceof MOB))
					return true;
				if((canSeeVictims(seer))&&(seer.getVictim()==seenP))
					return true;
				final Area area=R.getArea();
				if(area != null)
				{
					if((area.getClimateObj().canSeeTheMoon(R,null))
					&&(area.getTimeObj().getMoonPhase(R)==TimeClock.MoonPhase.FULL))
						return true;
				}
				return false;
			}
			return true;
		}
		else
		if(isInDark(seenP))
		{
			if((seenP instanceof Room)
			&&(((Room)seenP).getArea().getClimateObj().canSeeTheMoon(((Room)seenP),null)))
			{
				switch(((Room)seenP).getArea().getTimeObj().getMoonPhase((Room)seenP))
				{
				case FULL:
				case WAXGIBBOUS:
				case WANEGIBBOUS:
					return true;
				default:
					break;
				}
			}

			if(isLightSource(seer))
				return true;
			if(canSeeInDark(seer))
				return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean canBarelyBeSeenBy(Environmental seen , MOB seer)
	{
		if(!canBeSeenBy(seen,seer))
		{
			if((seer!=null)&&(!(seen instanceof Room)))
			{
				final Room R=seer.location();
				if((R!=null)&&(isInDark(R)))
				{
					if(R.getArea().getClimateObj().canSeeTheMoon(R,null))
					{
						return R.getArea().getTimeObj().getMoonPhase(R) != TimeClock.MoonPhase.NEW;
					}
				}
			}
			else
			if((seen instanceof Physical) && (isInDark((Physical)seen)))
			{
				if((seen instanceof Room)
				&&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen),null)))
				{
					switch(((Room)seen).getArea().getTimeObj().getMoonPhase((Room)seen))
					{
						case FULL:
						case WAXGIBBOUS:
						case WANEGIBBOUS:
							return false;
						case NEW:
							return false;
						default:
							return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canActAtAll(final Tickable affecting)
	{
		if(affecting instanceof MOB)
		{
			final MOB monster=(MOB)affecting;
			if((!isAliveAwakeMobile(monster,true))
			||(monster.location()==null)
			||(!isInTheGame(monster,false)))
				return false;
			return true;
		}
		return false;
	}

	@Override
	public boolean canFreelyBehaveNormal(final Tickable affecting)
	{
		if(affecting instanceof MOB)
		{
			final MOB monster=(MOB)affecting;
			if((!canActAtAll(monster))
			||(monster.isInCombat())
			||(monster.amFollowing()!=null)
			||(monster.curState().getHitPoints()<(monster.maxState().getHitPoints()/2)))
				return false;
			return true;
		}
		return false;
	}

	@Override
	public boolean isSeenTheSameWay(MOB seer, Physical seen1, Physical seen2)
	{
		if(canBeSeenBy(seen1,seer)!=canBeSeenBy(seen2,seer))
			return false;
		if((isEvil(seen1)!=isEvil(seen2))&&(canSeeEvil(seer)))
			return false;
		if((isGood(seen1)!=isGood(seen2))&&(canSeeGood(seer)))
			return false;
		if((isABonusItems(seen1)!=isABonusItems(seen2))&&(canSeeBonusItems(seer)))
			return false;
		if(isInvisible(seen1)!=isInvisible(seen2))
			return false;
		if(isSneaking(seen1)!=isSneaking(seen2))
			return false;
		if(isHidden(seen1)!=isHidden(seen2))
			return false;
		if(isFlying(seen1)!=isFlying(seen2))
			return false;
		if(isBound(seen1)!=isBound(seen2))
			return false;
		if(isFalling(seen1)!=isFalling(seen2))
			return false;
		if(isGlowing(seen1)!=isGlowing(seen2))
			return false;
		if(isGolem(seen1)!=isGolem(seen2))
			return false;
		if(canSeeMetal(seer)&&(seen1 instanceof Item)&&(seen2 instanceof Item)
			&&((((Item)seen1).material()&RawMaterial.MATERIAL_MASK)!=(((Item)seen2).material()&RawMaterial.MATERIAL_MASK)))
		   return false;
		if(!CMStrings.compareStringArraysIgnoreCase(seen1.phyStats().ambiances(),seen2.phyStats().ambiances()))
			return false;
		return true;
	}

	@Override
	public boolean isWaterWorthy(Physical P)
	{
		if(P==null)
			return false;
		if(isSwimming(P)
		&&((!(P instanceof Item))||(!((Item)P).subjectToWearAndTear())||(((Item)P).usesRemaining()>0)))
			return true;
		if((P instanceof Rider)&&(((Rider)P).riding()!=null))
			return isWaterWorthy(((Rider)P).riding());
		if((P instanceof Rideable)
		&&(((Rideable)P).rideBasis()==Rideable.RIDEABLE_WATER)
		&&(P instanceof MOB))
			return true;
		if(P instanceof Item)
		{
			final List<Item> V=new Vector<Item>();
			if(P instanceof Container)
				V.addAll(((Container)P).getDeepContents());
			if(!V.contains(P)) 
				V.add((Item)P);
			long totalWeight=0;
			long totalFloatilla=0;
			final RawMaterial.CODES codes = RawMaterial.CODES.instance();
			for(int v=0;v<V.size();v++)
			{
				final Item I=V.get(v);
				totalWeight+=I.basePhyStats().weight();
				totalFloatilla+=totalWeight*codes.bouancy(I.material());
			}
			if(P instanceof Container)
			{
				final long cap=((Container)P).capacity();
				if(totalWeight<cap)
				{
					totalFloatilla+=(cap-totalWeight);
					totalWeight+=cap-totalWeight;
				}
			}
			if(totalWeight<=0)
				return true;

			if(isFalling(P))
				return false;
			return (totalFloatilla/totalWeight)<=1000;
		}
		return false;
	}

	@Override
	public boolean isInFlight(Physical P)
	{
		if(P==null)
			return false;
		if(isFlying(P))
			return true;
		if(P instanceof Rider)
			return isInFlight(((Rider)P).riding());
		return false;
	}

	@Override
	public boolean isAnimalIntelligence(MOB M)
	{
		return (M!=null)&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2);
	}

	@Override
	public boolean isVegetable(MOB M)
	{
		return (M != null) 
				&& CMProps.getListFileVarSet(CMProps.ListFile.RACIAL_CATEGORY_IS_VEGETATION).contains(M.charStats().getMyRace().racialCategory());
	}

	@Override
	public boolean isMobile(PhysicalAgent P)
	{
		if(P!=null)
		{
			if((P instanceof BoardableShip)&&(!((BoardableShip)P).amDestroyed()))
				return true;
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if((B!=null)&&(CMath.bset(B.flags(),Behavior.FLAG_MOBILITY)))
					return true;
			}
		}
		return false;
	}

	@Override
	public List<Behavior> flaggedBehaviors(final PhysicalAgent P, final long flag)
	{
		final Vector<Behavior> V=new Vector<Behavior>(1);
		if(P!=null)
		{
			for(final Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				if((B!=null)&&(CMath.bset(B.flags(),flag)))
				{
					V.addElement(B);
				}
			}
		}
		return V;
	}

	@Override
	public List<Ability> domainAnyAffects(final Physical P, final int domain)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(P!=null)
		{
			if(domain>Ability.ALL_ACODES)
			{
				P.eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public void apply(Ability A)
					{
						if((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
							V.addElement(A);
					}
				});
			}
			else
			{
				P.eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public void apply(Ability A)
					{
						if((A.classificationCode()&Ability.ALL_ACODES)==domain)
							V.addElement(A);
					}
				});
			}
		}
		return V;
	}

	@Override
	public List<Ability> domainAffects(final Physical P, final int domain)
	{
		return domainAnyAffects(P,domain);
	}
	
	@Override
	public List<Ability> domainAbilities(final MOB M, final int domain)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(M!=null)
		{
			if(domain>Ability.ALL_ACODES)
			{
				for(final Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)==domain))
					{ 
						V.addElement(A);
					}
				}
			}
			else
			for(final Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==domain))
				{ 
					V.addElement(A);
				}
			}
		}
		return V;
	}
	
	@Override
	public List<Ability> flaggedAnyAffects(final Physical P, final long flag)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(P!=null)
		{
			P.eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public void apply(Ability A)
				{
					if((A.flags()&flag)>0)
						V.addElement(A);
				}
			});
		}
		return V;
	}
	
	@Override
	public List<Ability> matchedAffects(final MOB invoker, final Physical P, final long flag, final int abilityCode, final int domain)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(P!=null)
		{
			P.eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public void apply(Ability A)
				{
					if(((invoker==null)||(A.invoker()==invoker))
					&&(((flag<0)||(A.flags()&flag)>0))
					&&(((abilityCode<0)||((A.classificationCode()&Ability.ALL_ACODES)==abilityCode)))
					&&(((domain<0)||((A.classificationCode()&Ability.ALL_DOMAINS)==domain))))
						V.addElement(A);
				}
			});
		}
		return V;
	}
	
	@Override
	public List<Ability> flaggedAffects(final Physical P, final long flag)
	{
		return flaggedAnyAffects(P,flag);
	}

	@Override
	public List<Ability> flaggedAbilities(MOB M, long flag)
	{
		final Vector<Ability> V=new Vector<Ability>();
		if(M!=null)
		{
			for(final Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(CMath.bset(A.flags(),flag)))
				{
					V.addElement(A);
				}
			}
		}
		return V;
	}

	@Override
	public boolean canAccess(MOB mob, Area A)
	{
		if(A==null)
			return false;
		if((isHidden(A)) && (mob==null))
			return false;
		if(((!isHidden(A))
			&&(mob.location()!=null)&&(mob.location().getArea().getTimeObj()==A.getTimeObj()))
		||(CMSecurity.isASysOp(mob))
		||(A.amISubOp(mob.Name())))
			return true;
		return false;
	}

	@Override
	public boolean canAccess(MOB mob, Room R)
	{
		if(R==null)
			return false;
		if((isHidden(R)) && (mob==null))
			return false;
		if(((!isHidden(R))
			&&(mob.location()!=null)&&(mob.location().getArea().getTimeObj()==R.getArea().getTimeObj()))
		||(CMSecurity.isASysOp(mob))
		||(R.getArea().amISubOp(mob.Name())))
			return true;
		return false;
	}

	@Override
	public boolean isMetal(Environmental E)
	{
		if(E instanceof Item)
			return((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				||((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL);
		if(E instanceof MOB)
		{
			final MOB M=(MOB)E;
			if(isGolem(M) && (M.charStats()!=null) && (M.charStats().getMyRace()!=null))
			{
				final List<RawMaterial> mats = M.charStats().getMyRace().myResources();
				for(final Item I : mats)
				{
					if(CMath.bset(I.material(), RawMaterial.MATERIAL_METAL)
					||CMath.bset(I.material(), RawMaterial.MATERIAL_MITHRIL))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isAiryRoom(Room R)
	{
		if(R==null)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_INDOORS_AIR:
		case Room.DOMAIN_OUTDOORS_AIR:
			return true;
		}
		return false;
	}

	@Override
	public boolean isWateryRoom(Room R)
	{
		if(R==null)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return true;
		}
		return ((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				||isSwimming(R);
	}

	/**
	 * Returns whether the given room, whatever is 
	 * watery, such as a water surface, etc.
	 * @param R the room to check
	 * @return true if it is water surfacy, false otherwise
	 */
	@Override
	public boolean isWaterySurfaceRoom(Room R)
	{
		if(R==null)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return true;
		}
		return ((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
				&&isSwimming(R);
	}

	/**
	 * Returns whether the given room, whatever is 
	 * the surface of deep water, such as a water surface, etc.
	 * with an underwater room
	 * @param R the room to check
	 * @return true if it is water surfacy, false otherwise
	 */
	@Override
	public boolean isDeepWaterySurfaceRoom(Room R)
	{
		
		if((R==null)||(!isWaterySurfaceRoom(R)))
			return false;
		R.giveASky(0);
		return isUnderWateryRoom(R.getRoomInDir(Directions.DOWN));
	}

	/**
	 * Returns whether the given room, whatever is 
	 * watery, such as an underwater, etc.
	 * @param R the room to check
	 * @return true if it is underwatery, false otherwise
	 */
	@Override
	public boolean isUnderWateryRoom(Room R)
	{
		if(R==null)
			return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
			return true;
		}
		return ((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				&&isSwimming(R);
	}
	
	@Override
	public boolean isInTheGame(final Environmental E, final boolean reqInhabitation)
	{
		if(E instanceof MOB)
			return isInTheGame((MOB)E,reqInhabitation);
		if(E instanceof Item)
			return isInTheGame((Item)E,reqInhabitation);
		if(E instanceof Room)
			return isInTheGame((Room)E,reqInhabitation);
		if(E instanceof Area)
			return isInTheGame((Area)E,reqInhabitation);
		return true;
	}
	
	@Override
	public boolean isInTheGame(final MOB E, final boolean reqInhabitation)
	{
		return (E.location()!=null)
				&& E.amActive()
				&&((!reqInhabitation)||E.location().isInhabitant(E));
	}

	@Override
	public boolean isInTheGame(final Item E, final boolean reqInhabitation)
	{
		if(E.owner() instanceof MOB)
		{
			return isInTheGame((MOB)E.owner(),reqInhabitation);
		}
		else
		if(E.owner() instanceof Room)
		{
			return ((!E.amDestroyed())
					&&((!reqInhabitation)||(((Room)E.owner()).isContent(E))));
		}
		return false;
	}

	@Override
	public boolean isInTheGame(final Room E, final boolean reqInhabitation)
	{
		return CMLib.map().getRoom(CMLib.map().getExtendedRoomID(E))==E;
	}

	@Override
	public boolean isInTheGame(final Area E, final boolean reqInhabitation)
	{
		return CMLib.map().getArea(E.Name())==E;
	}

	@Override
	public boolean isEnchanted(Item I)
	{
		// poison is not an enchantment.
		// neither is disease, or standard properties.
		for(int i=0;i<I.numEffects();i++)
		{
			final Ability A=I.fetchEffect(i);
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_DISEASE)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_POISON))
				return true;
		}
		return false;
	}

	public boolean isAgingThing(Physical P)
	{
		if(P==null)
			return false;
		final Ability A=P.fetchEffect("Age");
		if((A!=null)&&(CMath.isInteger(A.text())&&(CMath.s_long(A.text())>Short.MAX_VALUE)))
			return true;
		return false;
	}

	@Override 
	public boolean isChild(Environmental E)
	{ 
		return isBaby(E)||((E instanceof MOB)&&(((MOB)E).isMonster())&&(isAgingThing((MOB)E)));
	}
	
	@Override 
	public boolean isBaby(Environmental E)
	{ 
		return ((E instanceof CagedAnimal)&&(isAgingThing((CagedAnimal)E)));
	}

	@Override
	public boolean isStillAffectedBy(Physical obj, List<Ability> oneOf, boolean anyTallF)
	{
		for(int a=oneOf.size()-1;a>=0;a--)
		{
			if(obj.fetchEffect(oneOf.get(a).ID())==null)
			{
				if(!anyTallF)
					return false;
			}
			else
			{
				if(anyTallF)
					return true;
			}
		}
		return !anyTallF;
	}

	@Override
	public String getDispositionDescList(Physical obj, boolean useVerbs)
	{
		if(obj == null)
			return "";
		final int disposition = obj.phyStats().disposition();
		final StringBuffer buf=new StringBuffer("");
		if(useVerbs)
		{
			for(int i=0;i<PhyStats.IS_VERBS.length;i++)
				if(CMath.isSet(disposition,i))
					buf.append(PhyStats.IS_VERBS[i]+", ");
		}
		else
		for(int i=0;i<PhyStats.IS_CODES.length;i++)
		{
			if(CMath.isSet(disposition,i))
				buf.append(PhyStats.IS_CODES[i]+", ");
		}
		String buff=buf.toString();
		if(buff.endsWith(", "))
			buff=buff.substring(0,buff.length()-2).trim();
		return buff;
	}

	@Override
	public String getSensesDescList(Physical obj, boolean useVerbs)
	{
		if(obj == null)
			return "";
		final int senses = obj.phyStats().sensesMask();
		final StringBuffer buf=new StringBuffer("");
		if(useVerbs)
		{
			for(int i=0;i<PhyStats.CAN_SEE_VERBS.length;i++)
				if(CMath.isSet(senses,i))
					buf.append(PhyStats.CAN_SEE_VERBS[i]+", ");
		}
		else
		for(int i=0;i<PhyStats.CAN_SEE_CODES.length;i++)
		{
			if(CMath.isSet(senses,i))
				buf.append(PhyStats.CAN_SEE_CODES[i]+", ");
		}
		String buff=buf.toString();
		if(buff.endsWith(", "))
			buff=buff.substring(0,buff.length()-2).trim();
		return buff;
	}

	@Override
	public int getDispositionIndex(String name)
	{
		name=name.toUpperCase().trim();
		for(int code=0;code<PhyStats.IS_CODES.length-1;code++)
		{
			if(PhyStats.IS_CODES[code].endsWith(name))
			{
				return code;
			}
		}
		return -1;
	}

	@Override
	public int getSensesIndex(String name)
	{
		name=name.toUpperCase().trim();
		for(int code=0;code<PhyStats.CAN_SEE_CODES.length-1;code++)
		{
			if(PhyStats.CAN_SEE_CODES[code].endsWith(name))
			{
				return code;
			}
		}
		return -1;
	}

	@Override
	public String getAbilityType(Ability A)
	{
		if(A==null)
			return "";
		return Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES];
	}
	
	@Override
	public String getAbilityType_(Ability A)
	{
		if(A==null)
			return "";
		return Ability.ACODE_DESCS_[A.classificationCode()&Ability.ALL_ACODES];
	}
	
	@Override
	public String getAbilityDomain(Ability A)
	{
		if(A==null)
			return "";
		return Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
	}
	
	@Override
	public int getAbilityType(String name)
	{
		for(int i=0;i<Ability.ACODE_DESCS.length;i++)
		{
			if(name.equalsIgnoreCase(Ability.ACODE_DESCS[i]))
				return i;
		}
		return -1;
	}
	
	@Override
	public int getAbilityType_(String name)
	{
		for(int i=0;i<Ability.ACODE_DESCS_.length;i++)
		{
			if(name.equalsIgnoreCase(Ability.ACODE_DESCS_[i]))
				return i;
		}
		return -1;
	}
	
	@Override
	public int getAbilityDomain(String name)
	{
		for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
		{
			if(name.equalsIgnoreCase(Ability.DOMAIN_DESCS[i]))
				return i<<5;
		}
		return -1;
	}

	@Override
	public boolean isAControlledFollower(MOB invoker, MOB mob, Ability A)
	{
		if((mob==null)||(mob==invoker)||(!mob.isMonster()))
			return false;
		if(A==null)
			return mob.getStartRoom()==null;
		A = mob.fetchEffect(A.ID());
		if(A==null)
			return false;
		if((A.invoker() == invoker)||(A.invoker()==null))
			return true;
		return false;
	}

	@Override
	public boolean hasAControlledFollower(MOB invoker, Ability A)
	{
		if(invoker==null)
			return false;
		final Room R = invoker.location();
		if(R==null)
			return false;
		for(int r=0;r<R.numInhabitants();r++)
		{
			if(isAControlledFollower(invoker, R.fetchInhabitant(r), A))
				return true;
		}
		final Set<MOB> H = invoker.getGroupMembers(new HashSet<MOB>());
		for (final MOB mob : H)
		{
			if(isAControlledFollower(invoker, mob, A))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDispositionBlurbs(Physical seen , MOB seer)
	{
		final PhyStats pStats=seen.phyStats();
		final String[] ambiances=pStats.ambiances();
		if(!pStats.isAmbiance("-ALL"))
		{
			final StringBuilder say=new StringBuilder("^N");
			if(!pStats.isAmbiance("-MOST"))
			{
				if((isEvil(seen))&&(canSeeEvil(seer))&&(!pStats.isAmbiance("-EVIL")))
					say.append(" (glowing ^rred^?)");
				if((isGood(seen))&&(canSeeGood(seer))&&(!pStats.isAmbiance("-GOOD")))
					say.append(" (glowing ^bblue^?)");
				if((isInvisible(seen))&&(canSeeInvisible(seer))&&(!pStats.isAmbiance("-INVISIBLE")))
					say.append(" (^yinvisible^?)");
				if((isSneaking(seen))&&(canSeeSneakers(seer))&&(!pStats.isAmbiance("-SNEAKING")))
					say.append(" (^ysneaking^?)");
				if((isHidden(seen))
				&&(canSeeHidden(seer)||((seen instanceof Item)&&(canSeeHiddenItems(seer))))
				&&(!pStats.isAmbiance("-HIDDEN")))
					say.append(" (^yhidden^?)");
				if((!isGolem(seen))&&(canSeeInfrared(seer))&&(seen instanceof MOB)&&(isInDark(seer.location()))&&(!pStats.isAmbiance("-HEAT")))
					say.append(" (^rheat aura^?)");
				if((isABonusItems(seen))&&(canSeeBonusItems(seer))&&(!pStats.isAmbiance("-MAGIC")))
					say.append(" (^wmagical aura^?)");
				if((canSeeMetal(seer))&&(seen instanceof Item)&&(!pStats.isAmbiance("-METAL")))
				{
					if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
						say.append(" (^wmetallic aura^?)");
					else
					if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
						say.append(" (^wmithril aura^?)");
				}
				if((isGlowing(seen))&&(!(seen instanceof Room))&&(!pStats.isAmbiance("-GLOWING")))
					say.append(" (^gglowing^?)");
				if((seen instanceof MOB) && isRunningLongCommand((MOB)seen)&&(!pStats.isAmbiance("-BUSY")))
					say.append(" (^gbusy^?)");
				for(int i=0;i<ambiances.length;i++)
				{
					if(!ambiances[i].startsWith("-"))
					{
						if(ambiances[i].startsWith("(?)"))
						{
							final int x=ambiances[i].indexOf(':');
							if(canBeHeardSpeakingBy(seen, seer))
								say.append(" ("+ambiances[i].substring(3,(x>3)?x:ambiances[i].length())+")");
							else
							if(x>3)
								say.append(" ("+ambiances[i].substring(x+1)+")");
						}
						else
							say.append(" ("+ambiances[i]+")");
					}
				}
			}
			if(isBound(seen)&&(!pStats.isAmbiance("-BOUND")))
				say.append(" (^Wbound^?)");
			if(isFlying(seen)&&(!(seen instanceof Exit))&&(!pStats.isAmbiance("-FLYING")))
				say.append(" (^pflying^?)");
			if((isFalling(seen))&&(!pStats.isAmbiance("-FALLING")))
			{
				final Room R=roomLocation(seen);
				switch(R.domainType())
				{
				case Room.DOMAIN_INDOORS_AIR:
				case Room.DOMAIN_OUTDOORS_AIR:
					say.append(" (^pfalling^?)");
					break;
				case Room.DOMAIN_INDOORS_WATERSURFACE:
				case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				case Room.DOMAIN_INDOORS_UNDERWATER:
				case Room.DOMAIN_OUTDOORS_UNDERWATER:
				case Room.DOMAIN_OUTDOORS_SEAPORT:
					say.append(" (^psinking^?)");
					break;
				default:
					if(!(seen instanceof MOB))
						say.append(" (^pfalling^?)");
					break;
				}
			}
			if(say.length()>1)
			{
				say.append(" ");
				return say.toString();
			}
		}
		return "";
	}

	protected boolean isAlcoholEffect(Ability A)
	{
		if(A!=null)
		{
			if(CMath.bset(A.flags(),Ability.FLAG_INTOXICATING))
				return true;
			if(A instanceof AbilityContainer)
			{
				for(Enumeration<Ability> a2=((AbilityContainer)A).allAbilities();a2.hasMoreElements();)
				{
					final Ability A2=a2.nextElement();
					if((A2!=null)&&(CMath.bset(A2.flags(),Ability.FLAG_INTOXICATING)))
						return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isAlcoholic(Physical thang)
	{
		if(thang==null)
			return false;
		if(thang instanceof Item)
		{
			if(((Item)thang).material()==RawMaterial.RESOURCE_LIQUOR)
				return true;
		}
		if(thang instanceof Drink)
		{
			if(((Drink)thang).liquidType()==RawMaterial.RESOURCE_LIQUOR)
				return true;
			if(thang instanceof Container)
			{
				for(Item I : ((Container)thang).getContents())
				{
					if((I!=thang) && isAlcoholic(I))
						return true;
				}
			}
			if(thang instanceof SpellHolder)
			{
				for(Ability A : ((SpellHolder)thang).getSpells())
				{
					if(isAlcoholEffect(A))
						return true;
				}
			}
		}
		for(int a=0;a<thang.numEffects();a++) // personal
		{
			final Ability A=thang.fetchEffect(a);
			if(this.isAlcoholEffect(A))
				return true;
		}
		return false;
	}
	
	@Override
	public String getPresentDispositionVerb(Physical seen, ComingOrGoing flag_msgType)
	{
		String type=null;
		if(isFalling(seen))
		{
			if((seen instanceof BoardableShip)
			&&(seen instanceof Item)
			&&(((Item)seen).owner() instanceof Room)
			&&(this.isWateryRoom((Room)((Item)seen).owner())))
				type="sinks";
			else
				type="falls";
		}
		else
		if(isSleeping(seen))
		{
			if(flag_msgType!=ComingOrGoing.IS)
				type=(seen instanceof MOB) ? "sleepwalks" : "floats";
			else
				type="sleeps";
		}
		else
		if(isSneaking(seen))
			type="sneaks";
		else
		if(isHidden(seen))
			type="prowls";
		else
		if(isSitting(seen))
		{
			if(flag_msgType!=ComingOrGoing.IS)
				type="crawls";
			else
			if(seen instanceof MOB)
				type="sits";
			else
				type="sits";
		}
		else
		if(isFlying(seen))
			type="flies";
		else
		if((isClimbing(seen))&&(flag_msgType!=ComingOrGoing.IS))
			type="climbs";
		else
		if(isSwimmingInWater(seen))
			type="swims";
		else
		if(flag_msgType != null)
		{
			switch(flag_msgType)
			{
			case ARRIVES:
				if(seen instanceof MOB)
					return ((MOB)seen).charStats().getArriveStr();
				else
					return "arrives";
			case LEAVES:
				if(seen instanceof MOB)
					return ((MOB)seen).charStats().getLeaveStr();
				else
					return "leaves";
			default:
				return "is";
			}
		}

		if(flag_msgType==ComingOrGoing.ARRIVES)
			return type+" in";
		return type;

	}

	@Override
	public String getDispositionStateList(MOB mob)
	{
		final StringBuilder str=new StringBuilder("");
		if(isClimbing(mob))
			str.append("climbing, ");
		if((mob.phyStats().disposition()&PhyStats.IS_EVIL)>0)
			str.append("evil, ");
		if(isFalling(mob))
			str.append("falling, ");
		if(isBound(mob))
			str.append("bound, ");
		if(isFlying(mob))
			str.append("flies, ");
		if((mob.phyStats().disposition()&PhyStats.IS_GOOD)>0)
			str.append("good, ");
		if(isHidden(mob))
			str.append("hidden, ");
		if(isInDark(mob))
			str.append("darkness, ");
		if(isInvisible(mob))
			str.append("invisible, ");
		if(isGlowing(mob))
			str.append("glowing, ");
		if(isCloaked(mob))
			str.append("cloaked, ");
		if(!isSeeable(mob))
			str.append("unseeable, ");
		if(isSitting(mob))
			str.append("crawls, ");
		if(isSleeping(mob))
			str.append("sleepy, ");
		if(isSneaking(mob))
			str.append("sneaks, ");
		if(isSwimming(mob))
			str.append("swims, ");
		if(str.toString().endsWith(", "))
			return str.toString().substring(0,str.length()-2);
		return str.toString();
	}

	@Override
	public String getSensesStateList(MOB mob)
	{
		final StringBuilder str=new StringBuilder("");
		if(!canHear(mob))
			str.append("deaf, ");
		if(!canSee(mob))
			str.append("blind, ");
		if(!canMove(mob))
			str.append("can't move, ");
		if(canSeeBonusItems(mob))
			str.append(L("detect magic, "));
		if(canSeeEvil(mob))
			str.append(L("detect evil, "));
		if(canSeeGood(mob))
			str.append(L("detect good, "));
		if(canSeeHidden(mob))
			str.append("see hidden, ");
		else
		if(canSeeHiddenItems(mob))
			str.append("see hidden items, ");
		if(canSeeInDark(mob))
			str.append(L("darkvision, "));
		if(canSeeInfrared(mob))
			str.append(L("infravision, "));
		if(canSeeInvisible(mob))
			str.append(L("see invisible, "));
		if(canSeeMetal(mob))
			str.append(L("metalvision, "));
		if(canSeeSneakers(mob))
			str.append(L("see sneaking, "));
		if(!canSmell(mob))
			str.append("can't smell, ");
		if(!canSpeak(mob))
			str.append("can't speak, ");
		if(!canTaste(mob))
			str.append("can't eat, ");
		if(str.toString().endsWith(", "))
			return str.toString().substring(0,str.length()-2);
		return str.toString();
	}
}
