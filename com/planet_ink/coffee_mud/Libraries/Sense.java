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
   Copyright 2000-2014 Bo Zimmerman

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
	public String ID(){return "Sense";}
	public boolean canSee(MOB M)
	{ return (M!=null)&&(!isSleeping(M))&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_SEE)==0); }
	public boolean canBeLocated(Physical P)
	{ 
		if ( (P!=null)&&(!isSleeping(P))&&((P.phyStats().sensesMask()&PhyStats.SENSE_UNLOCATABLE)==0) )
		{
			if((P instanceof Item)&&(((Item)P).container()!=null))
				return canBeLocated(((Item)P).container());
			return true;
		}
		return false;
	}
	public boolean canSeeHidden(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_HIDDEN)==PhyStats.CAN_SEE_HIDDEN); }
	public boolean canSeeInvisible(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_INVISIBLE)==PhyStats.CAN_SEE_INVISIBLE); }
	public boolean canSeeEvil(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_EVIL)==PhyStats.CAN_SEE_EVIL); }
	public boolean canSeeGood(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_GOOD)==PhyStats.CAN_SEE_GOOD); }
	public boolean canSeeSneakers(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_SNEAKERS)==PhyStats.CAN_SEE_SNEAKERS); }
	public boolean canSeeBonusItems(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_BONUS)==PhyStats.CAN_SEE_BONUS); }
	public boolean canSeeInDark(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_DARK)==PhyStats.CAN_SEE_DARK); }
	public boolean canSeeVictims(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_VICTIM)==PhyStats.CAN_SEE_VICTIM); }
	public boolean canSeeInfrared(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_INFRARED)==PhyStats.CAN_SEE_INFRARED); }
	public boolean canHear(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_HEAR)==0); }
	public boolean canWorkOnSomething(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_WORK)==0); }
	public boolean canAutoAttack(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_AUTO_ATTACK)==0); }
	public boolean canConcentrate(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_THINK)==0); }
	public boolean canMove(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_MOVE)==0); }
	public boolean allowsMovement(Room R)
	{ return (R!=null)&&((R.phyStats().sensesMask()&PhyStats.SENSE_ROOMNOMOVEMENT)==0); }
	public boolean allowsMovement(Area A)
	{ return (A!=null)&&((A.phyStats().sensesMask()&PhyStats.SENSE_ROOMNOMOVEMENT)==0); }
	public boolean canSmell(MOB M)
	{ return canBreatheHere(M,M.location())&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_SMELL)==0); }
	public boolean canTaste(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_TASTE)==0); }
	public boolean canSpeak(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_SPEAK)==0); }
	public boolean canBreathe(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_BREATHE)==0); }
	public boolean canBreatheThis(MOB M, int atmoResource)
	{ 
		return (canBreathe(M)
				&&((M.phyStats().sensesMask()&PhyStats.CAN_NOT_BREATHE)==0)
				&&((atmoResource<0)
					||(M.charStats().getBreathables().length==0)
					||(Arrays.binarySearch(M.charStats().getBreathables(), atmoResource)>=0)));
	}
	public boolean canBreatheHere(MOB M, Room R)
	{ return (M!=null)&&(canBreatheThis(M,(R==null)?-1:R.getAtmosphere())); }
	public boolean canSeeMetal(MOB M)
	{ return (M!=null)&&((M.phyStats().sensesMask()&PhyStats.CAN_SEE_METAL)==PhyStats.CAN_SEE_METAL); }
	public boolean isReadable(Item I)
	{ return (I!=null)&&((I.phyStats().sensesMask()&PhyStats.SENSE_ITEMREADABLE)==PhyStats.SENSE_ITEMREADABLE); }
	public boolean isGettable(Item I)
	{ return (I!=null)&&((I.phyStats().sensesMask()&PhyStats.SENSE_ITEMNOTGET)==0); }
	public boolean isDroppable(Item I)
	{ return (I!=null)&&((I.phyStats().sensesMask()&PhyStats.SENSE_ITEMNODROP)==0); }
	public boolean isRemovable(Item I)
	{ return (I!=null)&&((I.phyStats().sensesMask()&PhyStats.SENSE_ITEMNOREMOVE)==0); }
	public boolean isCataloged(Environmental E)
	{ return (E instanceof Physical)&&((((Physical)E).basePhyStats().disposition()&PhyStats.IS_CATALOGED)==PhyStats.IS_CATALOGED); }
	public boolean hasSeenContents(Physical P)
	{ return (P!=null)&&((P.phyStats().sensesMask()&PhyStats.SENSE_CONTENTSUNSEEN)==0); }
	public boolean isSavable(Physical P)
	{ 
		if((P==null)||((P.phyStats().disposition()&PhyStats.IS_UNSAVABLE)==0))
		{
			if((P instanceof Item)&&(((Item)P).container()!=null)&&(((Item)P).container()!=P))
				return isSavable(((Item)P).container());
			return true;
		}
		return false;
	}
	public void setSavable(Physical P, boolean truefalse)
	{
		if(P==null) return;
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
	public void setReadable(Item I, boolean truefalse)
	{
		if(I==null) return;
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

	public boolean isEnspelled(Physical F)
	{
		for(int a=0;a<F.numEffects();a++) // personal affects
		{
			Ability A=F.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked())&&(!A.isAutoInvoked())&&(!A.isSavable())
			&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			   ||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
				return true;
		}
		return false;
	}
	
	public void setGettable(Item I, boolean truefalse)
	{
		if(I==null) return;
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
	public void setDroppable(Item I, boolean truefalse)
	{
		if(I==null) return;
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
	public void setRemovable(Item I, boolean truefalse)
	{
		if(I==null) return;
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
	public boolean isSeen(Physical P)
	{ return (P!=null)&&(((P.phyStats().disposition()&PhyStats.IS_NOT_SEEN)==0) || isSleeping(P)); }
	public boolean isCloaked(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_CLOAKED)==PhyStats.IS_CLOAKED);}
	public boolean isHidden(Physical P)
	{
		if(P==null) return false;
		boolean isInHide=((P.phyStats().disposition()&PhyStats.IS_HIDDEN)==PhyStats.IS_HIDDEN);
		if((P instanceof MOB)
		&&(isInHide)
		&&(((MOB)P).isInCombat()))
			return false;
		return isInHide;
	}
	public boolean isUnattackable(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_UNATTACKABLE)==PhyStats.IS_UNATTACKABLE); }
	public boolean isInvisible(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_INVISIBLE)==PhyStats.IS_INVISIBLE); }

	public boolean isRejuvingItem(Item I)
	{
		if(I==null) return false;
		for(int i=0;i<I.numEffects();i++)
			if(I.fetchEffect(i) instanceof ItemTicker)
				return true;
		return false;
	}

	public boolean isReallyEvil(Physical P)
	{
		if(P instanceof MOB)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(Enumeration<String> e=((MOB)P).fetchFactions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),((MOB)P).fetchFaction(F.factionID()));
					if((FR!=null)&&(FR.alignEquiv()==Faction.Align.EVIL))
						return true;
				}
			}
		}
		return false;
	}
	public boolean isEvil(Physical P)
	{
		if(P==null) return false;
		if ((P.phyStats().disposition()&PhyStats.IS_EVIL)==PhyStats.IS_EVIL)
			return true;
		else
			return isReallyEvil(P);
	}

	public boolean isTracking(MOB M)
	{
		if(M!=null)
			return flaggedAffects(M,Ability.FLAG_TRACKING).size()>0;
		return false;
	}
	
	public boolean isATrackingMonster(MOB M)
	{
		if(M==null) return false;
		if(M.isMonster())
			return flaggedAffects(M,Ability.FLAG_TRACKING).size()>0;
		return false;
	}

	public boolean isReallyGood(Physical P)
	{
		if(P instanceof MOB)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(Enumeration<String> e=((MOB)P).fetchFactions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),((MOB)P).fetchFaction(F.factionID()));
					if((FR!=null)&&(FR.alignEquiv()==Faction.Align.GOOD))
						return true;
				}
			}
		}
		return false;
	}

	public String getAge(MOB M)
	{
		Ability A=M.fetchEffect("Age");
		if((A==null)||(A.displayText().length()==0))
		{
			if(M.baseCharStats().getStat(CharStats.STAT_AGE)==0)
				return "unknown";
			return M.baseCharStats().getStat(CharStats.STAT_AGE)+" year(s) old";
		}
		else
		{
			String s=A.displayText();
			if(s.startsWith("(")) s=s.substring(1);
			if(s.endsWith(")")) s=s.substring(0,s.length()-1);
			return s;
		}
	}
	
	public boolean isGood(Physical P)
	{
		if(P==null) return false;
		if ((P.phyStats().disposition()&PhyStats.IS_GOOD)==PhyStats.IS_GOOD)
			return true;
		else
			return isReallyGood(P);
	}

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

	public boolean isDeadlyOrMaliciousEffect(final PhysicalAgent P)
	{
		if(P==null) return false;
		if(CMLib.flags().flaggedBehaviors(P, Behavior.FLAG_POTENTIALLYAUTODEATHING).size()>0)
			return true;
		if(isTrapped(P))
			return true;
		for(final Enumeration<Ability> a=P.effects();a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if((A instanceof AbilityContainer)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY))
			{
				AbilityContainer U=(AbilityContainer)A;
				for(final Enumeration<Ability> e=U.allAbilities();e.hasMoreElements();)
				{
					Ability uA=e.nextElement();
					if((uA!=null)&&(uA.abstractQuality()==Ability.QUALITY_MALICIOUS))
						return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPossiblyAggressive(MOB M)
	{
		if(M==null) return false;
		List<Behavior> V=CMLib.flags().flaggedBehaviors(M,Behavior.FLAG_POTENTIALLYAGGRESSIVE);
		return ((V==null)||(V.size()==0))? false:true;
	}

	public boolean isAggressiveTo(MOB M, MOB toM)
	{
		if((M==null)||(toM==null)) return false;
		List<Behavior> V=CMLib.flags().flaggedBehaviors(M,Behavior.FLAG_POTENTIALLYAGGRESSIVE);
		if((V==null)||(V.size()==0)) return false;
		for(Behavior B : V)
			if(B.grantsAggressivenessTo(toM))
				return true;
		return false;
	}


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
			for(Enumeration<String> e=((MOB)E).fetchFactions();e.hasMoreElements();)
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

	public boolean isNeutral(Physical P)
	{
		if(P==null) return false;
		if(((P.phyStats().disposition()&PhyStats.IS_GOOD)==PhyStats.IS_GOOD)
		|| ((P.phyStats().disposition()&PhyStats.IS_EVIL)==PhyStats.IS_EVIL))
			return false;
		if(P instanceof MOB)
		{
			Faction F=null;
			Faction.FRange FR=null;
			for(Enumeration<String> e=((MOB)P).fetchFactions();e.hasMoreElements();)
			{
				F=CMLib.factions().getFaction(e.nextElement());
				if(F!=null)
				{
					FR=CMLib.factions().getRange(F.factionID(),((MOB)P).fetchFaction(F.factionID()));
					if(FR!=null)
					switch(FR.alignEquiv())
					{
					case NEUTRAL: return true;
					case EVIL: return false;
					case GOOD: return false;
					default: continue;
					}
				}
			}
		}
		return true;
	}

	public boolean isSneaking(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_SNEAKING)==PhyStats.IS_SNEAKING); }
	public boolean isABonusItems(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_BONUS)==PhyStats.IS_BONUS); }
	public boolean isInDark(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_DARK)==PhyStats.IS_DARK); }
	public boolean isLightSource(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_LIGHTSOURCE)==PhyStats.IS_LIGHTSOURCE); }
	public boolean isGlowing(Physical P)
	{ return (P!=null)&&((isLightSource(P)||((P.phyStats().disposition()&PhyStats.IS_GLOWING)==PhyStats.IS_GLOWING))); }
	public boolean isGolem(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_GOLEM)==PhyStats.IS_GOLEM); }
	public boolean isSleeping(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_SLEEPING)==PhyStats.IS_SLEEPING); }
	public boolean isSitting(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_SITTING)==PhyStats.IS_SITTING); }
	public boolean isFlying(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_FLYING)==PhyStats.IS_FLYING); }
	public boolean isClimbing(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_CLIMBING)==PhyStats.IS_CLIMBING); }
	public boolean isSwimming(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_SWIMMING)==PhyStats.IS_SWIMMING); }
	public boolean isFalling(Physical P)
	{ return (P!=null)&&((P.phyStats().disposition()&PhyStats.IS_FALLING)==PhyStats.IS_FALLING); }
	public boolean isBusy(Physical P)
	{ return (P instanceof MOB)&&(((MOB)P).session()!=null)&&((System.currentTimeMillis()-((MOB)P).session().getInputLoopTime())>30000);}

	public boolean isSwimmingInWater(Physical P)
	{
		if(!isSwimming(P)) return false;
		Room R=CMLib.map().roomLocation(P);
		if(R==null) return false;
		switch(R.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			return true;
		}
		return false;
	}
	
	public boolean canBeHeardMovingBy(Physical heard , MOB hearer)
	{
		if(hearer==heard) return true;
		if(hearer==null)
			return false;
		if(heard==null)
			return false;
		if((!isSeen(heard))&&(isCloaked(heard)))
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

	public boolean canBeHeardSpeakingBy(Physical heard , MOB hearer)
	{
		if(hearer==heard) return true;
		if(hearer==null)
			return false;
		if(heard==null)
			return false;
		if(!canHear(hearer))
			return false;
		return true;
	}

	public boolean canSenseMoving(Physical sensed, MOB sensor)
	{
		return (canBeHeardMovingBy(sensed,sensor)||canBeSeenBy(sensed,sensor));
	}
	
	public boolean canSenseEnteringLeaving(Physical sensed, MOB sensor)
	{
		return canBeHeardMovingBy(sensed,sensor);
	}

	public boolean aliveAwakeMobileUnbound(MOB mob, boolean quiet)
	{
		if(!aliveAwakeMobile(mob,quiet))
			return false;
		if(isBound(mob))
		{
			if(!quiet)
				mob.tell("You are bound!");
			return false;
		}
		if(isBoundOrHeld(mob))
		{
			if(!quiet)
				mob.tell("You are paralyzed!");
			return false;
		}
		return true;
	}

	public boolean aliveAwakeMobile(final MOB mob, final boolean quiet)
	{
		if(mob==null) return false;
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
			mob.tell("You are DEAD!");
			return false;
		}
		if(isSleeping(mob))
		{
			mob.tell("You are sleeping!");
			return false;
		}
		if(!canMove(mob))
		{
			mob.tell("You can't move!");
			return false;
		}
		return true;
	}

	public boolean isStanding(MOB mob)
	{
		return (!isSitting(mob))&&(!isSleeping(mob));
	}
	public boolean isBound(Physical P)
	{
		if((P!=null)&&((P.phyStats().disposition()&PhyStats.IS_BOUND)==PhyStats.IS_BOUND))
			return true;
		return false;
	}
	public boolean isBoundOrHeld(Physical P)
	{
		if(P==null) return false;
		if((P.phyStats().disposition()&PhyStats.IS_BOUND)==PhyStats.IS_BOUND)
			return true;
		return flaggedAnyAffects(P,Ability.FLAG_BINDING|Ability.FLAG_PARALYZING).size()>0;
	}
	public boolean isOnFire(Physical seen)
	{
		if(seen==null) return false;
		if(seen.fetchEffect("Burning")!=null)
			return true;
		if(seen.fetchEffect("Prayer_FlameWeapon")!=null)
			return true;
		if(!(seen instanceof Light))
			return false;
		Light light=(Light)seen;
		if(light.goesOutInTheRain()
		   &&light.isLit())
			return true;
		return false;
	}

	public int getHideScore(Physical seen)
	{
		if((seen!=null)&&(isHidden(seen)))
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

	public int getDetectScore(MOB seer)
	{
		if((seer!=null)&&(canSeeHidden(seer)))
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

	public boolean canBeSeenBy(Environmental seen , MOB seer)
	{
		if(seer==seen) return true;
		if(seen==null) return true;

		if((seer!=null)
		&&(CMath.bset(seer.getBitmap(),MOB.ATT_SYSOPMSGS)))
			return true;

		if(!canSee(seer)) return false;
		if(!(seen instanceof Physical))
			return true;
		Physical seenP=(Physical)seen;
		
		if((!isSeen(seenP))&&(seer!=null))
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
			if((!canSeeHidden(seer))||(seer==null))
			   return false;
			//if(this.getHideScore(seenP)>getDetectScore(seer))
			//    return false;
		}

		if((seer!=null)&&(!(seenP instanceof Room)))
		{
			Room R=seer.location();
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
				if((R.getArea().getClimateObj().canSeeTheMoon(R,null))
				&&(R.getArea().getTimeObj().getMoonPhase()==TimeClock.MoonPhase.FULL))
						return true;
				return false;
			}
			return true;
		}
		else
		if(isInDark(seenP))
		{
			if((seenP instanceof Room)
			&&(((Room)seenP).getArea().getClimateObj().canSeeTheMoon(((Room)seenP),null)))
				switch(((Room)seenP).getArea().getTimeObj().getMoonPhase())
				{
				case FULL:
				case WAXGIBBOUS:
				case WANEGIBBOUS:
					return true;
				default:
					break;
				}

			if(isLightSource(seer))
				return true;
			if(canSeeInDark(seer))
				return true;
			return false;
		}
		return true;
	}

	public boolean canBarelyBeSeenBy(Environmental seen , MOB seer)
	{
		if(!canBeSeenBy(seen,seer))
		if((seer!=null)&&(!(seen instanceof Room)))
		{
			Room R=seer.location();
			if((R!=null)&&(isInDark(R)))
			{
				if(R.getArea().getClimateObj().canSeeTheMoon(R,null))
				{
					return R.getArea().getTimeObj().getMoonPhase() != TimeClock.MoonPhase.NEW;
				}
			}
		}
		else
		if((seen instanceof Physical) && (isInDark((Physical)seen)))
		{
			if((seen instanceof Room)
			&&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen),null)))
				switch(((Room)seen).getArea().getTimeObj().getMoonPhase())
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
		return false;
	}

	public boolean canActAtAll(final Tickable affecting)
	{
		if(affecting instanceof MOB)
		{
			final MOB monster=(MOB)affecting;
			if((!aliveAwakeMobile(monster,true))
			||(monster.location()==null)
			||(!isInTheGame(monster,false)))
				return false;
			return true;
		}
		return false;
	}

	public boolean canFreelyBehaveNormal(final Tickable affecting)
	{
		if(affecting instanceof MOB)
		{
			final MOB monster=(MOB)affecting;
			if((!canActAtAll(monster))
			||(monster.isInCombat())
			||(monster.amFollowing()!=null)
			||(monster.curState().getHitPoints()<monster.maxState().getHitPoints()))
				return false;
			return true;
		}
		return false;
	}

	public StringBuffer colorCodes(Physical seen , MOB seer)
	{
		final PhyStats pStats=seen.phyStats();
		final String[] ambiances=pStats.ambiances();
		if(!pStats.isAmbiance("-ALL"))
		{
			final StringBuffer say=new StringBuffer("^N");
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
				if((isHidden(seen))&&(canSeeHidden(seer))&&(!pStats.isAmbiance("-HIDDEN")))
					say.append(" (^yhidden^?)");
				if((!isGolem(seen))&&(canSeeInfrared(seer))&&(seen instanceof MOB)&&(isInDark(seer.location()))&&(!pStats.isAmbiance("-HEAT")))
					say.append(" (^rheat aura^?)");
				if((isABonusItems(seen))&&(canSeeBonusItems(seer))&&(!pStats.isAmbiance("-MAGIC")))
					say.append(" (^wmagical aura^?)");
				if((canSeeMetal(seer))&&(seen instanceof Item)&&(!pStats.isAmbiance("-METAL")))
					if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
						say.append(" (^wmetallic aura^?)");
					else
					if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
						say.append(" (^wmithril aura^?)");
				if((isGlowing(seen))&&(!(seen instanceof Room))&&(!pStats.isAmbiance("-GLOWING")))
					say.append(" (^gglowing^?)");
				if(isBusy(seen)&&(!pStats.isAmbiance("-BUSY")))
					say.append(" (^gbusy^?)");
				for(int i=0;i<ambiances.length;i++)
					if(!ambiances[i].startsWith("-"))
					{
						if(ambiances[i].startsWith("(?)"))
						{
							int x=ambiances[i].indexOf(':');
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
			if(isBound(seen)&&(!pStats.isAmbiance("-BOUND")))
				say.append(" (^Wbound^?)");
			if(isFlying(seen)&&(!(seen instanceof Exit))&&(!pStats.isAmbiance("-FLYING")))
				say.append(" (^pflying^?)");
			if((isFalling(seen))&&(!pStats.isAmbiance("-FALLING"))
			&&((!(seen instanceof MOB))
				||(((MOB)seen).location()==null)
				||((((MOB)seen).location().domainType()!=Room.DOMAIN_OUTDOORS_AIR)
					&&(((MOB)seen).location().domainType()!=Room.DOMAIN_INDOORS_AIR))))
				say.append(" (^pfalling^?)");
			if(say.length()>1)
			{
				say.append(" ");
				return say;
			}
		}
		return new StringBuffer("");
	}

	public boolean seenTheSameWay(MOB seer, Physical seen1, Physical seen2)
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
		if(!CMStrings.compareStringArrays(seen1.phyStats().ambiances(),seen2.phyStats().ambiances()))
			return false;
		return true;
	}

	public final static int flag_arrives=0;
	public final static int flag_leaves=1;
	public final static int flag_is=2;
	public String dispositionString(Physical seen, int flag_msgType)
	{
		String type=null;
		if(isFalling(seen))
			type="falls";
		else
		if(isSleeping(seen))
		{
			if(flag_msgType!=flag_is)
				type="floats";
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
			if(flag_msgType!=flag_is)
				type="crawls";
			else
				type="sits";
		}
		else
		if(isFlying(seen))
			type="flies";
		else
		if((isClimbing(seen))&&(flag_msgType!=flag_is))
			type="climbs";
		else
		if(isSwimmingInWater(seen))
			type="swims";
		else
		if((flag_msgType==flag_arrives)||(flag_msgType==flag_leaves))
		{
			if(seen instanceof MOB)
			{
				if(flag_msgType==flag_arrives)
					return ((MOB)seen).charStats().getMyRace().arriveStr();
				else
				if(flag_msgType==flag_leaves)
					return ((MOB)seen).charStats().getMyRace().leaveStr();
			}
			else
			if(flag_msgType==flag_arrives)
				return "arrives";
			else
			if(flag_msgType==flag_leaves)
				return "leaves";
		}
		else
			return "is";

		if(flag_msgType==flag_arrives)
			return type+" in";
		return type;

	}

	public boolean isWaterWorthy(Physical P)
	{
		if(P==null) return false;
		if(isSwimming(P)) return true;
		if((P instanceof Rider)&&(((Rider)P).riding()!=null))
			return isWaterWorthy(((Rider)P).riding());
		if((P instanceof Rideable)&&(((Rideable)P).rideBasis()==Rideable.RIDEABLE_WATER))
			return true;
		if(P instanceof Item)
		{
			List<Item> V=new Vector<Item>();
			if(P instanceof Container)
				V.addAll(((Container)P).getContents());
			if(!V.contains(P)) V.add((Item)P);
			long totalWeight=0;
			long totalFloatilla=0;
			RawMaterial.CODES codes = RawMaterial.CODES.instance();
			for(int v=0;v<V.size();v++)
			{
				Item I=V.get(v);
				totalWeight+=I.basePhyStats().weight();
				totalFloatilla+=totalWeight*codes.bouancy(I.material());
			}
			if(P instanceof Container)
			{
				long cap=((Container)P).capacity();
				if(totalWeight<cap)
				{
					totalFloatilla+=(cap-totalWeight);
					totalWeight+=cap-totalWeight;
				}
			}
			if(totalWeight<=0) return true;

			return (totalFloatilla/totalWeight)<=1000;
		}
		return false;
	}


	public boolean isInFlight(Physical P)
	{
		if(P==null) return false;
		if(isFlying(P)) return true;
		if(P instanceof Rider)
			return isInFlight(((Rider)P).riding());
		return false;
	}

	public boolean isAnimalIntelligence(MOB M)
	{
		return (M!=null)&&(M.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2);
	}
	public boolean isVegetable(MOB M)
	{
		return (M!=null)&&(M.charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation"));
	}


	public boolean isMobile(PhysicalAgent P)
	{
		if(P!=null)
			for(Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				Behavior B=e.nextElement();
				if((B!=null)&&(CMath.bset(B.flags(),Behavior.FLAG_MOBILITY)))
					return true;
			}
		return false;
	}

	public List<Behavior> flaggedBehaviors(final PhysicalAgent P, final long flag)
	{
		Vector<Behavior> V=new Vector<Behavior>();
		if(P!=null)
			for(Enumeration<Behavior> e=P.behaviors();e.hasMoreElements();)
			{
				Behavior B=e.nextElement();
				if((B!=null)&&(CMath.bset(B.flags(),flag)))
				{ V.addElement(B);}
			}
		return V;
	}


	public List<Ability> domainAnyAffects(final Physical P, final int domain)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(P!=null)
			if(domain>Ability.ALL_ACODES)
			{
				P.eachEffect(new EachApplicable<Ability>(){
	                public void apply(Ability A) {
	    				if((A.classificationCode()&Ability.ALL_DOMAINS)==domain)
	    					V.addElement(A);
	                }
				});
			}
			else
				P.eachEffect(new EachApplicable<Ability>(){
	                public void apply(Ability A) {
	    				if((A.classificationCode()&Ability.ALL_ACODES)==domain)
	    					V.addElement(A);
	                }
				});
		return V;
	}
	
	public List<Ability> domainAffects(final Physical P, final int domain)
	{
		return domainAnyAffects(P,domain);
	}
	public List<Ability> domainAbilities(final MOB M, final int domain)
	{
		Vector<Ability> V=new Vector<Ability>(1);
		if(M!=null)
			if(domain>Ability.ALL_ACODES)
			{
				for(Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&((A.classificationCode()&Ability.ALL_DOMAINS)==domain))
					{ V.addElement(A);}
				}
			}
			else
			for(Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==domain))
				{ V.addElement(A);}
			}
		return V;
	}
	public List<Ability> flaggedAnyAffects(final Physical P, final long flag)
	{
		final Vector<Ability> V=new Vector<Ability>(1);
		if(P!=null)
			P.eachEffect(new EachApplicable<Ability>(){
                public void apply(Ability A) {
					if((A.flags()&flag)>0)
						V.addElement(A);
                }
			});
		return V;
	}
	public List<Ability> flaggedAffects(final Physical P, final long flag)
	{
		return flaggedAnyAffects(P,flag);
	}

	public List<Ability> flaggedAbilities(MOB M, long flag)
	{
		Vector<Ability> V=new Vector<Ability>();
		if(M!=null)
			for(Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
			{
				Ability A=a.nextElement();
				if((A!=null)&&(CMath.bset(A.flags(),flag)))
				{ V.addElement(A);}
			}
		return V;
	}

	public boolean canAccess(MOB mob, Area A)
	{
		if(A==null) return false;
		if((isHidden(A)) && (mob==null))
			return false;
		if(((!isHidden(A))
			&&(mob.location()!=null)&&(mob.location().getArea().getTimeObj()==A.getTimeObj()))
		||(CMSecurity.isASysOp(mob))
		||(A.amISubOp(mob.Name())))
			return true;
		return false;
	}
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

	public boolean isMetal(Environmental E)
	{
		if(E instanceof Item)
			return((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			   ||((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL);
		return false;
	}


	public int burnStatus(Environmental E)
	{
		if(E instanceof Item)
		{
			Item lighting=(Item)E;
			switch(lighting.material())
			{
			case RawMaterial.RESOURCE_COAL:
				if(E instanceof RawMaterial)
					return 40;
				return 20*(1+lighting.phyStats().weight());
			case RawMaterial.RESOURCE_LAMPOIL:
				return 5+lighting.phyStats().weight();
			default:
				break;
			}
			switch(lighting.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LEATHER:
				return 20+lighting.phyStats().weight();
			case RawMaterial.MATERIAL_CLOTH:
			case RawMaterial.MATERIAL_PAPER:
			case RawMaterial.MATERIAL_SYNTHETIC:
				return 5+lighting.phyStats().weight();
			case RawMaterial.MATERIAL_WOODEN:
				if(E instanceof RawMaterial)
					return 20;
				return 20*(1+lighting.phyStats().weight());
			case RawMaterial.MATERIAL_VEGETATION:
			case RawMaterial.MATERIAL_FLESH:
				return -1;
			case RawMaterial.MATERIAL_UNKNOWN:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_ENERGY:
			case RawMaterial.MATERIAL_GAS:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_PRECIOUS:
				return 0;
			}
		}
		return 0;
	}

	public boolean isInTheGame(final Environmental E, final boolean reqInhabitation)
	{
		if(E instanceof MOB) return isInTheGame((MOB)E,reqInhabitation);
		if(E instanceof Item) return isInTheGame((Item)E,reqInhabitation);
		if(E instanceof Room) return isInTheGame((Room)E,reqInhabitation);
		if(E instanceof Area) return isInTheGame((Area)E,reqInhabitation);
		return true;
	}
	public boolean isInTheGame(final MOB E, final boolean reqInhabitation)
	{
		return (E.location()!=null) 
				&& E.amActive()
				&&((!reqInhabitation)||E.location().isInhabitant(E));
	}
	
	public boolean isInTheGame(final Item E, final boolean reqInhabitation)
	{
		if(E.owner() instanceof MOB)
			return isInTheGame((MOB)E.owner(),reqInhabitation);
		else
		if(E.owner() instanceof Room)
			return ((!E.amDestroyed())
					&&((!reqInhabitation)||(((Room)E.owner()).isContent(E))));
		return false;
	}
	
	public boolean isInTheGame(final Room E, final boolean reqInhabitation)
	{
		return CMLib.map().getRoom(CMLib.map().getExtendedRoomID(E))==E;
	}
	
	public boolean isInTheGame(final Area E, final boolean reqInhabitation)
	{
		return CMLib.map().getArea(E.Name())==E;
	}
	
	public boolean enchanted(Item I)
	{
		// poison is not an enchantment.
		// neither is disease, or standard properties.
		for(int i=0;i<I.numEffects();i++)
		{
			Ability A=I.fetchEffect(i);
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
		if(P==null) return false;
		Ability A=P.fetchEffect("Age");
		if((A!=null)&&(CMath.isInteger(A.text())&&(CMath.s_long(A.text())>Short.MAX_VALUE)))
			return true;
		return false;
	}

	public boolean isChild(Environmental E){ return isBaby(E)||((E instanceof MOB)&&(((MOB)E).isMonster())&&(isAgingThing((MOB)E)));}
	public boolean isBaby(Environmental E){ return ((E instanceof CagedAnimal)&&(isAgingThing((CagedAnimal)E)));}

	public boolean stillAffectedBy(Physical obj, List<Ability> oneOf, boolean anyTallF)
	{
		for(int a=oneOf.size()-1;a>=0;a--)
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
		return !anyTallF;
	}

	public String dispositionList(int disposition, boolean useVerbs)
	{
		StringBuffer buf=new StringBuffer("");
		if(useVerbs)
		{
			for(int i=0;i<PhyStats.IS_VERBS.length;i++)
				if(CMath.isSet(disposition,i))
					buf.append(PhyStats.IS_VERBS[i]+", ");
		}
		else
		for(int i=0;i<PhyStats.IS_CODES.length;i++)
			if(CMath.isSet(disposition,i))
				buf.append(PhyStats.IS_CODES[i]+", ");
		String buff=buf.toString();
		if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
		return buff;
	}

	public String sensesList(int disposition, boolean useVerbs)
	{
		StringBuffer buf=new StringBuffer("");
		if(useVerbs)
		{
			for(int i=0;i<PhyStats.CAN_SEE_VERBS.length;i++)
				if(CMath.isSet(disposition,i))
					buf.append(PhyStats.CAN_SEE_VERBS[i]+", ");
		}
		else
		for(int i=0;i<PhyStats.CAN_SEE_CODES.length;i++)
			if(CMath.isSet(disposition,i))
				buf.append(PhyStats.CAN_SEE_CODES[i]+", ");
		String buff=buf.toString();
		if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
		return buff;
	}

	public int getDispositionCode(String name)
	{
		name=name.toUpperCase().trim();
		for(int code=0;code<PhyStats.IS_CODES.length-1;code++)
			if(PhyStats.IS_CODES[code].endsWith(name))
				return code;
		return -1;
	}

	public int getSensesCode(String name)
	{
		name=name.toUpperCase().trim();
		for(int code=0;code<PhyStats.CAN_SEE_CODES.length-1;code++)
			if(PhyStats.CAN_SEE_CODES[code].endsWith(name))
				return code;
		return -1;
	}

	public String getAbilityType(Ability A)
	{
		if(A==null) return "";
		return Ability.ACODE_DESCS[A.classificationCode()&Ability.ALL_ACODES];
	}
	public String getAbilityDomain(Ability A)
	{
		if(A==null) return "";
		return Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
	}
	public int getAbilityType(String name)
	{
		for(int i=0;i<Ability.ACODE_DESCS.length;i++)
			if(name.equalsIgnoreCase(Ability.ACODE_DESCS[i]))
				return i;
		return -1;
	}
	public int getAbilityDomain(String name)
	{
		for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
			if(name.equalsIgnoreCase(Ability.DOMAIN_DESCS[i]))
				return i<<5;
		return -1;
	}

	public boolean isAControlledFollower(MOB invoker, MOB mob, Ability A)
	{
		if((mob==null)||(mob==invoker)||(!mob.isMonster())) return false;
		if(A==null) 
			return mob.getStartRoom()==null;
		A = mob.fetchEffect(A.ID());
		if(A==null) 
			return false;
		if((A.invoker() == invoker)||(A.invoker()==null)) 
			return true;
		return false;
	}
	
	public boolean hasAControlledFollower(MOB invoker, Ability A)
	{
		if(invoker==null) return false;
		Room R = invoker.location();
		if(R==null) return false;
		for(int r=0;r<R.numInhabitants();r++)
			if(isAControlledFollower(invoker, R.fetchInhabitant(r), A))
				return true;
		Set<MOB> H = invoker.getGroupMembers(new HashSet<MOB>());
		for(Iterator<MOB> i=H.iterator();i.hasNext();)
			if(isAControlledFollower(invoker, i.next(), A))
				return true;
		return false;
	}
	
	public String describeDisposition(MOB mob)
	{
		StringBuilder str=new StringBuilder("");
		if(CMLib.flags().isClimbing(mob))
			str.append("climbing, ");
		if((mob.phyStats().disposition()&PhyStats.IS_EVIL)>0)
			str.append("evil, ");
		if(CMLib.flags().isFalling(mob))
			str.append("falling, ");
		if(CMLib.flags().isBound(mob))
			str.append("bound, ");
		if(CMLib.flags().isFlying(mob))
			str.append("flies, ");
		if((mob.phyStats().disposition()&PhyStats.IS_GOOD)>0)
			str.append("good, ");
		if(CMLib.flags().isHidden(mob))
			str.append("hidden, ");
		if(CMLib.flags().isInDark(mob))
			str.append("darkness, ");
		if(CMLib.flags().isInvisible(mob))
			str.append("invisible, ");
		if(CMLib.flags().isGlowing(mob))
			str.append("glowing, ");
		if(CMLib.flags().isCloaked(mob))
			str.append("cloaked, ");
		if(!CMLib.flags().isSeen(mob))
			str.append("unseeable, ");
		if(CMLib.flags().isSitting(mob))
			str.append("crawls, ");
		if(CMLib.flags().isSleeping(mob))
			str.append("sleepy, ");
		if(CMLib.flags().isSneaking(mob))
			str.append("sneaks, ");
		if(CMLib.flags().isSwimming(mob))
			str.append("swims, ");
		if(str.toString().endsWith(", "))
			return str.toString().substring(0,str.length()-2);
		return str.toString();
	}
	
	public String describeSenses(MOB mob)
	{
		StringBuilder str=new StringBuilder("");
		if(!CMLib.flags().canHear(mob))
			str.append("deaf, ");
		if(!CMLib.flags().canSee(mob))
			str.append("blind, ");
		if(!CMLib.flags().canMove(mob))
			str.append("can't move, ");
		if(CMLib.flags().canSeeBonusItems(mob))
			str.append("detect magic, ");
		if(CMLib.flags().canSeeEvil(mob))
			str.append("detect evil, ");
		if(CMLib.flags().canSeeGood(mob))
			str.append("detect good, ");
		if(CMLib.flags().canSeeHidden(mob))
			str.append("see hidden, ");
		if(CMLib.flags().canSeeInDark(mob))
			str.append("darkvision, ");
		if(CMLib.flags().canSeeInfrared(mob))
			str.append("infravision, ");
		if(CMLib.flags().canSeeInvisible(mob))
			str.append("see invisible, ");
		if(CMLib.flags().canSeeMetal(mob))
			str.append("metalvision, ");
		if(CMLib.flags().canSeeSneakers(mob))
			str.append("see sneaking, ");
		if(!CMLib.flags().canSmell(mob))
			str.append("can't smell, ");
		if(!CMLib.flags().canSpeak(mob))
			str.append("can't speak, ");
		if(!CMLib.flags().canTaste(mob))
			str.append("can't eat, ");
		if(str.toString().endsWith(", "))
			return str.toString().substring(0,str.length()-2);
		return str.toString();
	}
}
