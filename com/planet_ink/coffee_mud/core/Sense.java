package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Sense
{
	public static boolean canSee(MOB E)
	{ return (E!=null)&&(!isSleeping(E))&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SEE)==0); }
	public static boolean canBeLocated(Item E)
	{ return (E!=null)&&(!isSleeping(E))&&((E.envStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)==0); }
    public static boolean canBeSaved(Item E)
    { return (E==null)||((E.envStats().sensesMask()&EnvStats.SENSE_ITEMNEVERSAVED)==0); }
	public static boolean canSeeHidden(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_HIDDEN)==EnvStats.CAN_SEE_HIDDEN); }
	public static boolean canSeeInvisible(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_INVISIBLE)==EnvStats.CAN_SEE_INVISIBLE); }
	public static boolean canSeeEvil(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_EVIL)==EnvStats.CAN_SEE_EVIL); }
	public static boolean canSeeGood(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_GOOD)==EnvStats.CAN_SEE_GOOD); }
	public static boolean canSeeSneakers(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_SNEAKERS)==EnvStats.CAN_SEE_SNEAKERS); }
	public static boolean canSeeBonusItems(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_BONUS)==EnvStats.CAN_SEE_BONUS); }
	public static boolean canSeeInDark(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_DARK)==EnvStats.CAN_SEE_DARK); }
	public static boolean canSeeVictims(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_VICTIM)==EnvStats.CAN_SEE_VICTIM); }
	public static boolean canSeeInfrared(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_INFRARED)==EnvStats.CAN_SEE_INFRARED); }
	public static boolean canHear(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_HEAR)==0); }
	public static boolean canMove(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_MOVE)==0); }
	public static boolean allowsMovement(Room R)
	{ return (R!=null)&&((R.envStats().sensesMask()&EnvStats.SENSE_ROOMNOMOVEMENT)==0); }
	public static boolean allowsMovement(Area A)
	{ return (A!=null)&&((A.envStats().sensesMask()&EnvStats.SENSE_ROOMNOMOVEMENT)==0); }
	public static boolean canSmell(MOB E)
	{ return canBreathe(E)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SMELL)==0); }
	public static boolean canTaste(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_TASTE)==0); }
	public static boolean canSpeak(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SPEAK)==0); }
	public static boolean canBreathe(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_BREATHE)==0); }
	public static boolean canSeeMetal(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_METAL)==EnvStats.CAN_SEE_METAL); }
	public static boolean isReadable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMREADABLE)==EnvStats.SENSE_ITEMREADABLE); }
	public static boolean isGettable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNOTGET)==0); }
	public static boolean isDroppable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNODROP)==0); }
	public static boolean isRemovable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNOREMOVE)==0); }
	public static boolean hasSeenContents(Environmental E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.SENSE_CONTENTSUNSEEN)==0); }
	public static void setReadable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(Util.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
				I.envStats().setSensesMask(Util.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(Util.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
			I.envStats().setSensesMask(Util.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
		}
	}
	public static void setGettable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(Util.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
				I.envStats().setSensesMask(Util.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(Util.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
			I.envStats().setSensesMask(Util.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
		}
	}
	public static void setDroppable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(Util.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
				I.envStats().setSensesMask(Util.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(Util.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
			I.envStats().setSensesMask(Util.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
		}
	}
	public static void setRemovable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!Util.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(Util.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
				I.envStats().setSensesMask(Util.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(Util.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
			I.envStats().setSensesMask(Util.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
		}
	}
	public static boolean isSeen(Environmental E)
	{ return (E!=null)&&(((E.envStats().disposition()&EnvStats.IS_NOT_SEEN)==0) || isSleeping(E)); }
	public static boolean isCloaked(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_CLOAKED)==EnvStats.IS_CLOAKED);}
	public static boolean isHidden(Environmental E)
	{
		if(E==null) return false;
		boolean isInHide=((E.envStats().disposition()&EnvStats.IS_HIDDEN)==EnvStats.IS_HIDDEN);
		if((isInHide)
		&&(E instanceof MOB)
		&&(((MOB)E).isInCombat()))
			return false;
		return isInHide;
	}
	public static boolean isInvisible(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_INVISIBLE)==EnvStats.IS_INVISIBLE); }
	public static boolean isEvil(Environmental E)
	{
		if(E==null) return false;
		if ((E.envStats().disposition()&EnvStats.IS_EVIL)==EnvStats.IS_EVIL)
			return true;
		else
		if(E instanceof MOB)
		{
		    Faction F=null;
		    Faction.FactionRange FR=null;
		    for(Enumeration e=((MOB)E).fetchFactions();e.hasMoreElements();)
		    {
		        F=Factions.getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=Factions.getRange(F.ID,((MOB)E).fetchFaction(F.ID));
			        if((FR!=null)&&(FR.AlignEquiv==Faction.ALIGN_EVIL))
			            return true;
		        }
		    }
		}
		return false;
	}

	public static boolean isATrackingMonster(Environmental E)
	{
		if(E==null) return false;
		if((E instanceof MOB)&&(((MOB)E).isMonster()))
			return flaggedAffects(E,Ability.FLAG_TRACKING).size()>0;
		return false;
	}

	public static boolean isGood(Environmental E)
	{
		if(E==null) return false;
		if ((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_GOOD)
			return true;
		else
		if(E instanceof MOB)
        {
		    Faction F=null;
		    Faction.FactionRange FR=null;
		    for(Enumeration e=((MOB)E).fetchFactions();e.hasMoreElements();)
		    {
		        F=Factions.getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=Factions.getRange(F.ID,((MOB)E).fetchFaction(F.ID));
			        if((FR!=null)&&(FR.AlignEquiv==Faction.ALIGN_GOOD))
			            return true;
		        }
		    }
        }
		return false;
	}

	public static String getAlignmentName(Environmental E)
	{
		if((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_GOOD)
		    return Faction.ALIGN_NAMES[Faction.ALIGN_GOOD];
		if((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_EVIL)
		    return Faction.ALIGN_NAMES[Faction.ALIGN_EVIL];
        if(E instanceof MOB)
        {
		    Faction F=null;
		    Faction.FactionRange FR=null;
		    for(Enumeration e=((MOB)E).fetchFactions();e.hasMoreElements();)
		    {
		        F=Factions.getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=Factions.getRange(F.ID,((MOB)E).fetchFaction(F.ID));
			        if(FR!=null)
			        switch(FR.AlignEquiv)
			        {
			        case Faction.ALIGN_EVIL: return Faction.ALIGN_NAMES[Faction.ALIGN_EVIL];
			        case Faction.ALIGN_GOOD: return Faction.ALIGN_NAMES[Faction.ALIGN_GOOD];
			        }
		        }
		    }
        }
        return Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL];
	}
	
    public static boolean isNeutral(Environmental E)
    {
        if(E==null) return false;
		if(((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_GOOD)
		|| ((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_EVIL))
			return false;
        if(E instanceof MOB)
        {
		    Faction F=null;
		    Faction.FactionRange FR=null;
		    for(Enumeration e=((MOB)E).fetchFactions();e.hasMoreElements();)
		    {
		        F=Factions.getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=Factions.getRange(F.ID,((MOB)E).fetchFaction(F.ID));
			        if(FR!=null)
			        switch(FR.AlignEquiv)
			        {
			        case Faction.ALIGN_NEUTRAL: return true;
			        case Faction.ALIGN_EVIL: return false;
			        case Faction.ALIGN_GOOD: return false;
			        }
		        }
		    }
        }
        return true;
    }

	public static boolean isSneaking(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SNEAKING)==EnvStats.IS_SNEAKING); }
	public static boolean isABonusItems(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS); }
	public static boolean isInDark(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_DARK)==EnvStats.IS_DARK); }
	public static boolean isLightSource(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_LIGHTSOURCE)==EnvStats.IS_LIGHTSOURCE); }
	public static boolean isGlowing(Environmental E)
	{ return (E!=null)&&((isLightSource(E)||((E.envStats().disposition()&EnvStats.IS_GLOWING)==EnvStats.IS_GLOWING))); }
	public static boolean isGolem(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_GOLEM)==EnvStats.IS_GOLEM); }
	public static boolean isSleeping(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SLEEPING)==EnvStats.IS_SLEEPING); }
	public static boolean isSitting(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SITTING)==EnvStats.IS_SITTING); }
	public static boolean isFlying(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_FLYING)==EnvStats.IS_FLYING); }
	public static boolean isClimbing(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_CLIMBING)==EnvStats.IS_CLIMBING); }
	public static boolean isSwimming(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SWIMMING)==EnvStats.IS_SWIMMING); }
	public static boolean isFalling(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_FALLING)==EnvStats.IS_FALLING); }

	public static boolean canBeHeardBy(Environmental heard , MOB hearer)
	{
		if(hearer==heard) return true;
		if(hearer==null)
			return false;
		if(heard==null)
			return false;
		if(!canHear(hearer))
			return false;
		if(isSneaking(heard)&&(!canSeeSneakers(hearer)))
		   return false;
		return true;
	}

	public static boolean canSenseMoving(Environmental sensed, MOB sensor)
	{
		if(isSneaking(sensed)&&(!canSeeSneakers(sensor)))
		   return false;
		return (canBeHeardBy(sensed,sensor)||canBeSeenBy(sensed,sensor));
	}
	
    public static boolean aliveAwakeMobileUnbound(MOB mob, boolean quiet)
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
    
	public static boolean aliveAwakeMobile(MOB mob, boolean quiet)
	{
		if(mob==null) return false;
		if(mob.amDead()||(mob.curState()==null)||(mob.curState().getHitPoints()<0))
		{
			if(!quiet)
				mob.tell("You are DEAD!");
			return false;
		}
		if(isSleeping(mob))
		{
			if(!quiet)
				mob.tell("You are sleeping!");
			return false;
		}
		if(!canMove(mob))
		{
			if(!quiet)
				mob.tell("You can't move!");
			return false;
		}
		return true;
	}

	public static boolean isStanding(MOB mob)
	{
	    return (!isSitting(mob))&&(!isSleeping(mob));
	}
	public static boolean isBound(Environmental E)
	{
		if((E!=null)&&((E.envStats().disposition()&EnvStats.IS_BOUND)==EnvStats.IS_BOUND))
			return true;
		return false;
	}
	public static boolean isBoundOrHeld(Environmental E)
	{
		if(E==null) return false;
		if((E.envStats().disposition()&EnvStats.IS_BOUND)==EnvStats.IS_BOUND)
			return true;
		return flaggedAnyAffects(E,Ability.FLAG_BINDING|Ability.FLAG_PARALYZING).size()>0;
	}
	public static boolean isOnFire(Environmental seen)
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

	public static boolean canBeSeenBy(Environmental seen , MOB seer)
	{
		if(seer==seen) return true;
		if(seen==null) return true;

		if((seer!=null)
		&&(Util.bset(seer.getBitmap(),MOB.ATT_SYSOPMSGS)))
			return true;

		if(!canSee(seer)) return false;
		if(!isSeen(seen))
		{
			if((!(seen instanceof MOB))
			||(seen.envStats().level()>seer.envStats().level())
			||(!CMSecurity.isASysOp(seer)))
				return false;
		}

		if((isInvisible(seen))&&(!canSeeInvisible(seer)))
		   return false;

		if(isHidden(seen))
        {
            if((!canSeeHidden(seer))||(seer==null))
    		   return false;
            int hideFactor=seen.envStats().level();
            if(seen instanceof MOB)
                hideFactor+=(((MOB)seen).charStats().getStat(CharStats.DEXTERITY));
            if(Util.bset(seen.baseEnvStats().disposition(),EnvStats.IS_HIDDEN))
                hideFactor+=100;
            else
            if(seen instanceof MOB)
                hideFactor+=((MOB)seen).charStats().getStat(CharStats.SAVE_DETECTION);
            else
                hideFactor+=100;
            int detectFactor=seer.charStats().getStat(CharStats.WISDOM);
            if(Util.bset(seer.baseEnvStats().sensesMask(),EnvStats.CAN_SEE_HIDDEN))
                detectFactor+=(100+seer.envStats().level());
            else // the 100 represents proff, and level represents time searching.
                detectFactor+=seer.charStats().getStat(CharStats.SAVE_OVERLOOKING);
            if(seen instanceof MOB)
                detectFactor+=(seen.envStats().height()-seer.envStats().height());
            if(hideFactor>detectFactor)
                return false;
        }
        

		if((seer!=null)&&(!(seen instanceof Room)))
		{
            Room R=seer.location();
			if((R!=null)&&(isInDark(R)))
			{
				if((isGlowing(seen))||(isLightSource(seer)))
					return true;
				if(canSeeInDark(seer))
					return true;
				if((!isGolem(seen))&&(canSeeInfrared(seer))&&(seen instanceof MOB))
				   return true;
				if((canSeeVictims(seer))&&(seer.getVictim()==seen))
					return true;
                if(R.getArea().getClimateObj().canSeeTheMoon(R))
                    switch(R.getArea().getTimeObj().getMoonPhase())
                    {
                    case TimeClock.PHASE_FULL:
                        return true;
                    }
				return false;
			}
			return true;
		}
		else
		if(isInDark(seen)) 
		{
            if((seen instanceof Room)
            &&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen))))
                switch(((Room)seen).getArea().getTimeObj().getMoonPhase())
                {
                case TimeClock.PHASE_FULL:
                case TimeClock.PHASE_WAXGIBBOUS:
                case TimeClock.PHASE_WANEGIBBOUS:
                    return true;
                }
            
			if(isLightSource(seer))
				return true;
			if(canSeeInDark(seer))
				return true;
			return false;
		}
		return true;
	}
    
    public static boolean canBarelyBeSeenBy(Environmental seen , MOB seer)
    {
        if(!canBeSeenBy(seen,seer))
        if((seer!=null)&&(!(seen instanceof Room)))
        {
            Room R=seer.location();
            if((R!=null)&&(isInDark(R)))
            {
                if(R.getArea().getClimateObj().canSeeTheMoon(R))
                {
                    switch(R.getArea().getTimeObj().getMoonPhase())
                    {
                    case TimeClock.PHASE_NEW:
                        return false;
                    default:
                        return true;
                    }
                }
            }
        }
        else
        if(isInDark(seen)) 
        {
            if((seen instanceof Room)
            &&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen))))
                switch(((Room)seen).getArea().getTimeObj().getMoonPhase())
                {
                case TimeClock.PHASE_FULL:
                case TimeClock.PHASE_WAXGIBBOUS:
                case TimeClock.PHASE_WANEGIBBOUS:
                    return false;
                case TimeClock.PHASE_NEW:
                    return false;
                default: 
                    return true;
                }
        }
        return false;
    }
    
	public static StringBuffer colorCodes(Environmental seen , MOB seer)
	{
		StringBuffer Say=new StringBuffer("^N");

		if((Sense.isEvil(seen))&&(Sense.canSeeEvil(seer)))
			Say.append(" (glowing ^rred^?)");
		if((Sense.isGood(seen))&&(Sense.canSeeGood(seer)))
			Say.append(" (glowing ^bblue^?)");
		if((Sense.isInvisible(seen))&&(Sense.canSeeInvisible(seer)))
			Say.append(" (^yinvisible^?)");
		if((Sense.isSneaking(seen))&&(Sense.canSeeSneakers(seer)))
			Say.append(" (^ysneaking^?)");
		if((Sense.isHidden(seen))&&(Sense.canSeeHidden(seer)))
			Say.append(" (^yhidden^?)");
		if((!Sense.isGolem(seen))
		&&(Sense.canSeeInfrared(seer))
		&&(seen instanceof MOB)
		&&(isInDark(seer.location())))
			Say.append(" (^rheat aura^?)");
		if((Sense.isABonusItems(seen))&&(Sense.canSeeBonusItems(seer)))
			Say.append(" (^wmagical aura^?)");
		if((Sense.canSeeMetal(seer))&&(seen instanceof Item))
			if((((Item)seen).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				Say.append(" (^wmetallic aura^?)");
			else
			if((((Item)seen).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)
				Say.append(" (^wmithril aura^?)");

		if(isBound(seen))
			Say.append(" (^Wbound^?)");
		if(isFlying(seen)&&(!(seen instanceof Exit)))
			Say.append(" (^pflying^?)");
		if((isFalling(seen))
		&&((!(seen instanceof MOB))
		   ||(((MOB)seen).location()==null)
		   ||((((MOB)seen).location().domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			  &&(((MOB)seen).location().domainType()!=Room.DOMAIN_INDOORS_AIR))))
			Say.append(" (^pfalling^?)");
		if((isGlowing(seen))&&(!(seen instanceof Room)))
			Say.append(" (^gglowing^?)");
        if(Say.length()>1)
        {
            Say.append(" ");
    		return Say;
        }
        return new StringBuffer("");
	}

	public static boolean seenTheSameWay(MOB seer, Environmental seen1, Environmental seen2)
	{
		if(Sense.canBeSeenBy(seen1,seer)!=Sense.canBeSeenBy(seen2,seer))
		   return false;
		if((Sense.isEvil(seen1)!=Sense.isEvil(seen2))&&(Sense.canSeeEvil(seer)))
			return false;
		if((Sense.isGood(seen1)!=Sense.isGood(seen2))&&(Sense.canSeeGood(seer)))
			return false;
		if((Sense.isABonusItems(seen1)!=Sense.isABonusItems(seen2))&&(Sense.canSeeBonusItems(seer)))
			return false;
		if(Sense.isInvisible(seen1)!=Sense.isInvisible(seen2))
			return false;
		if(Sense.isSneaking(seen1)!=Sense.isSneaking(seen2))
			return false;
		if(Sense.isHidden(seen1)!=Sense.isHidden(seen2))
			return false;
		if(Sense.isFlying(seen1)!=Sense.isFlying(seen2))
			return false;
		if(Sense.isBound(seen1)!=Sense.isBound(seen2))
			return false;
		if(Sense.isFalling(seen1)!=Sense.isFalling(seen2))
			return false;
		if(Sense.isGlowing(seen1)!=Sense.isGlowing(seen2))
			return false;
		if(Sense.isGolem(seen1)!=Sense.isGolem(seen2))
			return false;
		if(Sense.canSeeMetal(seer)&&(seen1 instanceof Item)&&(seen2 instanceof Item)
			&&((((Item)seen1).material()&EnvResource.MATERIAL_MASK)!=(((Item)seen2).material()&EnvResource.MATERIAL_MASK)))
		   return false;
		return true;
	}

	public final static int flag_arrives=0;
	public final static int flag_leaves=1;
	public final static int flag_is=2;
	public static String dispositionString(Environmental seen, int flag_msgType)
	{
		String type=null;
		if(Sense.isSneaking(seen))
			type="sneaks";
		else
		if(Sense.isHidden(seen))
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
		if(isSleeping(seen))
		{
			if(flag_msgType!=flag_is)
				type="floats";
			else
				type="sleeps";
		}
		else
		if(isFlying(seen))
			type="flies";
		else
		if(isFalling(seen))
			type="falls";
		else
		if((isClimbing(seen))&&(flag_msgType!=flag_is))
			type="climbs";
		else
		if(isSwimming(seen))
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

	public static boolean isWaterWorthy(Environmental E)
	{
		if(E==null) return false;
		if(Sense.isSwimming(E)) return true;
		if((E instanceof Rider)&&(((Rider)E).riding()!=null))
			return isWaterWorthy(((Rider)E).riding());
		if((E instanceof Rideable)&&(((Rideable)E).rideBasis()==Rideable.RIDEABLE_WATER))
		    return true;
		if(E instanceof Item)
		{
			Vector V=(E instanceof Container)?((Container)E).getContents():new Vector();
			if(!V.contains(E)) V.addElement(E);
			long totalWeight=0;
			long totalFloatilla=0;
			for(int v=0;v<V.size();v++)
			{
				Item I=(Item)V.elementAt(v);
				totalWeight+=I.baseEnvStats().weight();
				totalFloatilla+=totalWeight*EnvResource.RESOURCE_DATA[I.material()&EnvResource.RESOURCE_MASK][4];
			}
			if(E instanceof Container)
			{
				long cap=((Container)E).capacity();
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


	public static boolean isInFlight(Environmental E)
	{
		if(E==null) return false;
		if(Sense.isFlying(E)) return true;
		if(E instanceof Rider)
			return isInFlight(((Rider)E).riding());
		return false;
	}

	public static boolean isAnimalIntelligence(MOB E)
	{
		return (E!=null)&&(E.charStats().getStat(CharStats.INTELLIGENCE)<2);
	}
	public static boolean isVegetable(MOB E)
	{
		return (E!=null)&&(E.charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation"));
	}


	public static boolean isMobile(Environmental E)
	{
		if(E!=null)
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_MOBILITY)))
					return true;
			}
		return false;
	}

	public static Vector flaggedBehaviors(Environmental E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(Util.bset(B.flags(),flag)))
				{ V.addElement(B);}
			}
		return V;
	}

	
	public static Vector flaggedAnyAffects(Environmental E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int a=0;a<E.numEffects();a++)
			{
				Ability A=E.fetchEffect(a);
				if((A!=null)&&((A.flags()&flag)>0))
				{ V.addElement(A);}
			}
		return V;
	}
	public static Vector flaggedAffects(Environmental E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int a=0;a<E.numEffects();a++)
			{
				Ability A=E.fetchEffect(a);
				if((A!=null)&&(Util.bset(A.flags(),flag)))
				{ V.addElement(A);}
			}
		return V;
	}

	public static Vector flaggedAbilities(MOB E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int a=0;a<E.numAbilities();a++)
			{
				Ability A=E.fetchAbility(a);
				if((A!=null)&&(Util.bset(A.flags(),flag)))
				{ V.addElement(A);}
			}
		return V;
	}

	public static boolean canAccess(MOB mob, Area A)
	{
		if(A==null) return false;
		if(((mob==null)&&(!isHidden(A)))
		||((!isHidden(A))
			&&(mob.location()!=null)
			&&(mob.location().getArea().getTimeObj()==A.getTimeObj()))
		||(CMSecurity.isASysOp(mob))
		||(A.amISubOp(mob.Name())))
			return true;
		return false;
	}
	public static boolean canAccess(MOB mob, Room R)
	{
		if(R==null) return false;
		if(((mob==null)&&(!isHidden(R)))
		||((!isHidden(R))
			&&(mob.location()!=null)
			&&(mob.location().getArea().getTimeObj()==R.getArea().getTimeObj()))
		||(CMSecurity.isASysOp(mob))
		||(R.getArea().amISubOp(mob.Name())))
			return true;
		return false;
	}
	
	public static boolean isMetal(Environmental E)
	{
		if(E instanceof Item)
			return((((Item)E).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
			   ||((((Item)E).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL);
		return false;
	}

	
	public static int burnStatus(Environmental E)
	{
		if(E instanceof Item)
		{
			Item lighting=(Item)E;
			switch(lighting.material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_LEATHER:
				return 20+lighting.envStats().weight();
			case EnvResource.MATERIAL_CLOTH:
			case EnvResource.MATERIAL_PAPER:
			case EnvResource.MATERIAL_PLASTIC:
				return 5+lighting.envStats().weight();
			case EnvResource.MATERIAL_WOODEN:
				return 150+(lighting.envStats().weight()*5);
			case EnvResource.MATERIAL_VEGETATION:
			case EnvResource.MATERIAL_FLESH:
				return -1;
			case EnvResource.MATERIAL_UNKNOWN:
			case EnvResource.MATERIAL_GLASS:
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_ENERGY:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_PRECIOUS:
				return 0;
			}
		}
		return 0;
	}
	
	public static boolean isInTheGame(Environmental E, boolean reqInhabitation)
	{
		if(E instanceof Room)
			return CMMap.getRoom(CMMap.getExtendedRoomID((Room)E))==E;
		else
		if(E instanceof MOB)
			return (((MOB)E).location()!=null)
				   &&((MOB)E).amActive()
				   &&((!reqInhabitation)||(((MOB)E).location().isInhabitant((MOB)E)));
		else
		if(E instanceof Item)
		{
			if(((Item)E).owner() instanceof MOB)
				return isInTheGame(((Item)E).owner(),reqInhabitation);
			else
			if(((Item)E).owner() instanceof Room)
				return ((!((Item)E).amDestroyed())
						&&((!reqInhabitation)||(((Room)((Item)E).owner()).isContent((Item)E))));
			else
				return false;
		}
		else
		if(E instanceof Area)
			return CMMap.getArea(E.Name())==E;
		else
			return true;
		
	}
	
	public static boolean enchanted(Item I)
	{
		for(int i=0;i<I.numEffects();i++)
		{
			Ability A=I.fetchEffect(i);
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_CODES)!=Ability.PROPERTY))
				return true;
		}
		return false;
	}
	
	
	public static String wornLocation(long wornCode)
	{
		for(int wornNum=0;wornNum<Item.wornLocation.length-1;wornNum++)
		{
			if(wornCode==(1<<wornNum))
				return Item.wornLocation[wornNum+1];
		}
		return "";
	}
	
	public static boolean stillAffectedBy(Environmental obj, Vector oneOf, boolean anyTallF)
	{
		for(int a=oneOf.size()-1;a>=0;a--)
			if(obj.fetchEffect(((Ability)oneOf.elementAt(a)).ID())==null)
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
    
    public static String dispositionList(int disposition, boolean useVerbs)
    {
        StringBuffer buf=new StringBuffer("");
        if(useVerbs)
        {
            for(int i=0;i<EnvStats.dispositionsVerb.length;i++)
                if(Util.isSet(disposition,i))
                    buf.append(EnvStats.dispositionsVerb[i]+", ");
        }
        else
        for(int i=0;i<EnvStats.dispositionsNames.length;i++)
            if(Util.isSet(disposition,i))
                buf.append(EnvStats.dispositionsNames[i]+", ");
        String buff=buf.toString();
        if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
        return buff;
    }
    
    public static String sensesList(int disposition, boolean useVerbs)
    {
        StringBuffer buf=new StringBuffer("");
        if(useVerbs)
        {
            for(int i=0;i<EnvStats.sensesVerb.length;i++)
                if(Util.isSet(disposition,i))
                    buf.append(EnvStats.sensesVerb[i]+", ");
        }
        else
        for(int i=0;i<EnvStats.sensesNames.length;i++)
            if(Util.isSet(disposition,i))
                buf.append(EnvStats.sensesNames[i]+", ");
        String buff=buf.toString();
        if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
        return buff;
    }
    
    public static int getDispositionCode(String name)
    {
        name=name.toUpperCase().trim();
        for(int code=0;code<EnvStats.dispositionsNames.length-1;code++)
            if(EnvStats.dispositionsNames[code].endsWith(name))
                return code;
        return -1;
    }
    
    public static int getSensesCode(String name)
    {
        name=name.toUpperCase().trim();
        for(int code=0;code<EnvStats.sensesNames.length-1;code++)
            if(EnvStats.sensesNames[code].endsWith(name))
                return code;
        return -1;
    }
    
    public static String getAbilityType(Ability A)
    {
        if(A==null) return "";
        return Ability.TYPE_DESCS[A.classificationCode()&Ability.ALL_CODES];
    }
    public static String getAbilityDomain(Ability A)
    {
        if(A==null) return "";
        return Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5];
    }
    public static int getAbilityType(String name)
    {
        for(int i=0;i<Ability.TYPE_DESCS.length;i++)
            if(name.equalsIgnoreCase(Ability.TYPE_DESCS[i]))
                return i;
        return -1;
    }
    public static int getAbilityDomain(String name)
    {
        for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
            if(name.equalsIgnoreCase(Ability.DOMAIN_DESCS[i]))
                return i<<5;
        return -1;
    }
    
}
