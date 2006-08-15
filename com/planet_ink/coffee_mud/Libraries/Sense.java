package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
	public boolean canSee(MOB E)
	{ return (E!=null)&&(!isSleeping(E))&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SEE)==0); }
	public boolean canBeLocated(Environmental E)
	{ return (E!=null)&&(!isSleeping(E))&&((E.envStats().sensesMask()&EnvStats.SENSE_UNLOCATABLE)==0); }
    public boolean canBeSaved(Item E)
    { return (E==null)||((E.envStats().sensesMask()&EnvStats.SENSE_ITEMNEVERSAVED)==0); }
	public boolean canSeeHidden(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_HIDDEN)==EnvStats.CAN_SEE_HIDDEN); }
	public boolean canSeeInvisible(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_INVISIBLE)==EnvStats.CAN_SEE_INVISIBLE); }
	public boolean canSeeEvil(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_EVIL)==EnvStats.CAN_SEE_EVIL); }
	public boolean canSeeGood(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_GOOD)==EnvStats.CAN_SEE_GOOD); }
	public boolean canSeeSneakers(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_SNEAKERS)==EnvStats.CAN_SEE_SNEAKERS); }
	public boolean canSeeBonusItems(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_BONUS)==EnvStats.CAN_SEE_BONUS); }
	public boolean canSeeInDark(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_DARK)==EnvStats.CAN_SEE_DARK); }
	public boolean canSeeVictims(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_VICTIM)==EnvStats.CAN_SEE_VICTIM); }
	public boolean canSeeInfrared(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_INFRARED)==EnvStats.CAN_SEE_INFRARED); }
	public boolean canHear(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_HEAR)==0); }
	public boolean canMove(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_MOVE)==0); }
	public boolean allowsMovement(Room R)
	{ return (R!=null)&&((R.envStats().sensesMask()&EnvStats.SENSE_ROOMNOMOVEMENT)==0); }
	public boolean allowsMovement(Area A)
	{ return (A!=null)&&((A.envStats().sensesMask()&EnvStats.SENSE_ROOMNOMOVEMENT)==0); }
	public boolean canSmell(MOB E)
	{ return canBreathe(E)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SMELL)==0); }
	public boolean canTaste(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_TASTE)==0); }
	public boolean canSpeak(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SPEAK)==0); }
	public boolean canBreathe(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_BREATHE)==0); }
	public boolean canSeeMetal(MOB E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.CAN_SEE_METAL)==EnvStats.CAN_SEE_METAL); }
	public boolean isReadable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMREADABLE)==EnvStats.SENSE_ITEMREADABLE); }
	public boolean isGettable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNOTGET)==0); }
	public boolean isDroppable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNODROP)==0); }
	public boolean isRemovable(Item I)
	{ return (I!=null)&&((I.envStats().sensesMask()&EnvStats.SENSE_ITEMNOREMOVE)==0); }
	public boolean hasSeenContents(Environmental E)
	{ return (E!=null)&&((E.envStats().sensesMask()&EnvStats.SENSE_CONTENTSUNSEEN)==0); }
	public void setReadable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(CMath.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
				I.envStats().setSensesMask(CMath.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(CMath.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
			I.envStats().setSensesMask(CMath.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
		}
	}
	public void setGettable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(CMath.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
				I.envStats().setSensesMask(CMath.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(CMath.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
			I.envStats().setSensesMask(CMath.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET));
		}
	}
	public void setDroppable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(CMath.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
				I.envStats().setSensesMask(CMath.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(CMath.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
			I.envStats().setSensesMask(CMath.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
		}
	}
	public void setRemovable(Item I, boolean truefalse)
	{
		if(I==null) return;
		if(!CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))
		{
			if(!truefalse)
			{
				I.baseEnvStats().setSensesMask(CMath.setb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
				I.envStats().setSensesMask(CMath.setb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
			}
		}
		else
		if(truefalse)
		{
			I.baseEnvStats().setSensesMask(CMath.unsetb(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
			I.envStats().setSensesMask(CMath.unsetb(I.envStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
		}
	}
	public boolean isSeen(Environmental E)
	{ return (E!=null)&&(((E.envStats().disposition()&EnvStats.IS_NOT_SEEN)==0) || isSleeping(E)); }
	public boolean isCloaked(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_CLOAKED)==EnvStats.IS_CLOAKED);}
	public boolean isHidden(Environmental E)
	{
		if(E==null) return false;
		boolean isInHide=((E.envStats().disposition()&EnvStats.IS_HIDDEN)==EnvStats.IS_HIDDEN);
		if((isInHide)
		&&(E instanceof MOB)
		&&(((MOB)E).isInCombat()))
			return false;
		return isInHide;
	}
	public boolean isInvisible(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_INVISIBLE)==EnvStats.IS_INVISIBLE); }
	public boolean isEvil(Environmental E)
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
		        F=CMLib.factions().getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=CMLib.factions().getRange(F.factionID(),((MOB)E).fetchFaction(F.factionID()));
			        if((FR!=null)&&(FR.alignEquiv()==Faction.ALIGN_EVIL))
			            return true;
		        }
		    }
		}
		return false;
	}

	public boolean isATrackingMonster(Environmental E)
	{
		if(E==null) return false;
		if((E instanceof MOB)&&(((MOB)E).isMonster()))
			return flaggedAffects(E,Ability.FLAG_TRACKING).size()>0;
		return false;
	}

	public boolean isGood(Environmental E)
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
		        F=CMLib.factions().getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=CMLib.factions().getRange(F.factionID(),((MOB)E).fetchFaction(F.factionID()));
			        if((FR!=null)&&(FR.alignEquiv()==Faction.ALIGN_GOOD))
			            return true;
		        }
		    }
        }
		return false;
	}

	public String getAlignmentName(Environmental E)
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
		        F=CMLib.factions().getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=CMLib.factions().getRange(F.factionID(),((MOB)E).fetchFaction(F.factionID()));
			        if(FR!=null)
			        switch(FR.alignEquiv())
			        {
			        case Faction.ALIGN_EVIL: return Faction.ALIGN_NAMES[Faction.ALIGN_EVIL];
			        case Faction.ALIGN_GOOD: return Faction.ALIGN_NAMES[Faction.ALIGN_GOOD];
			        }
		        }
		    }
        }
        return Faction.ALIGN_NAMES[Faction.ALIGN_NEUTRAL];
	}
	
    public boolean isNeutral(Environmental E)
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
		        F=CMLib.factions().getFaction((String)e.nextElement());
		        if(F!=null)
		        {
			        FR=CMLib.factions().getRange(F.factionID(),((MOB)E).fetchFaction(F.factionID()));
			        if(FR!=null)
			        switch(FR.alignEquiv())
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

	public boolean isSneaking(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SNEAKING)==EnvStats.IS_SNEAKING); }
	public boolean isABonusItems(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS); }
	public boolean isInDark(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_DARK)==EnvStats.IS_DARK); }
	public boolean isLightSource(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_LIGHTSOURCE)==EnvStats.IS_LIGHTSOURCE); }
	public boolean isGlowing(Environmental E)
	{ return (E!=null)&&((isLightSource(E)||((E.envStats().disposition()&EnvStats.IS_GLOWING)==EnvStats.IS_GLOWING))); }
	public boolean isGolem(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_GOLEM)==EnvStats.IS_GOLEM); }
	public boolean isSleeping(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SLEEPING)==EnvStats.IS_SLEEPING); }
	public boolean isSitting(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SITTING)==EnvStats.IS_SITTING); }
	public boolean isFlying(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_FLYING)==EnvStats.IS_FLYING); }
	public boolean isClimbing(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_CLIMBING)==EnvStats.IS_CLIMBING); }
	public boolean isSwimming(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_SWIMMING)==EnvStats.IS_SWIMMING); }
	public boolean isFalling(Environmental E)
	{ return (E!=null)&&((E.envStats().disposition()&EnvStats.IS_FALLING)==EnvStats.IS_FALLING); }
	public boolean isBusy(Environmental E)
	{ return (E instanceof MOB)&&(((MOB)E).session()!=null)&&((System.currentTimeMillis()-((MOB)E).session().lastLoopTime())>30000);}

	public boolean isSwimmingInWater(Environmental E)
	{ 
		if(!isSwimming(E)) return false;
		Room R=CMLib.map().roomLocation(E);
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
	public boolean canBeHeardBy(Environmental heard , MOB hearer)
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

	public boolean canSenseMoving(Environmental sensed, MOB sensor)
	{
		if(isSneaking(sensed)&&(!canSeeSneakers(sensor)))
		   return false;
		return (canBeHeardBy(sensed,sensor)||canBeSeenBy(sensed,sensor));
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
    
	public boolean aliveAwakeMobile(MOB mob, boolean quiet)
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

	public boolean isStanding(MOB mob)
	{
	    return (!isSitting(mob))&&(!isSleeping(mob));
	}
	public boolean isBound(Environmental E)
	{
		if((E!=null)&&((E.envStats().disposition()&EnvStats.IS_BOUND)==EnvStats.IS_BOUND))
			return true;
		return false;
	}
	public boolean isBoundOrHeld(Environmental E)
	{
		if(E==null) return false;
		if((E.envStats().disposition()&EnvStats.IS_BOUND)==EnvStats.IS_BOUND)
			return true;
		return flaggedAnyAffects(E,Ability.FLAG_BINDING|Ability.FLAG_PARALYZING).size()>0;
	}
	public boolean isOnFire(Environmental seen)
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

	public int getHideScore(Environmental seen)
	{
		if((seen!=null)&&(isHidden(seen)))
        {
            int hideFactor=seen.envStats().level();
            if(seen instanceof MOB)
                hideFactor+=(((MOB)seen).charStats().getStat(CharStats.STAT_DEXTERITY))/2;
            if(CMath.bset(seen.baseEnvStats().disposition(),EnvStats.IS_HIDDEN))
                hideFactor+=100;
            else
            if(seen instanceof MOB)
            {
                hideFactor+=((MOB)seen).charStats().getSave(CharStats.STAT_SAVE_DETECTION);
                if(seen.envStats().height()>=0)
	            	hideFactor-=(int)Math.round(Math.sqrt(new Integer(seen.envStats().height()).doubleValue()));
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
            if(CMath.bset(seer.baseEnvStats().sensesMask(),EnvStats.CAN_SEE_HIDDEN))
                detectFactor+=100;
            else // the 100 represents proff, and level represents time searching.
                detectFactor+=seer.charStats().getSave(CharStats.STAT_SAVE_OVERLOOKING);
            if(seer.envStats().height()>=0)
	            detectFactor-=(int)Math.round(Math.sqrt(new Integer(seer.envStats().height()).doubleValue()));
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
		if(!isSeen(seen))
		{
			if((!(seen instanceof MOB))
			||(seen.envStats().level()>seer.envStats().level())
			||(!CMSecurity.isASysOp(seer)))
				return false;
		}

		if((isInvisible(seen))&&(!canSeeInvisible(seer)))
		   return false;

		if((isHidden(seen))&&(!(seen instanceof Room)))
        {
            if((!canSeeHidden(seer))||(seer==null))
    		   return false;
            //if(this.getHideScore(seen)>getDetectScore(seer))
            //    return false;
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
                if(R.getArea().getClimateObj().canSeeTheMoon(R,null))
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
            &&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen),null)))
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
            &&(((Room)seen).getArea().getClimateObj().canSeeTheMoon(((Room)seen),null)))
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
    
	public StringBuffer colorCodes(Environmental seen , MOB seer)
	{
		StringBuffer Say=new StringBuffer("^N");

		if((isEvil(seen))&&(canSeeEvil(seer)))
			Say.append(" (glowing ^rred^?)");
		if((isGood(seen))&&(canSeeGood(seer)))
			Say.append(" (glowing ^bblue^?)");
		if((isInvisible(seen))&&(canSeeInvisible(seer)))
			Say.append(" (^yinvisible^?)");
		if((isSneaking(seen))&&(canSeeSneakers(seer)))
			Say.append(" (^ysneaking^?)");
		if((isHidden(seen))&&(canSeeHidden(seer)))
			Say.append(" (^yhidden^?)");
		if((!isGolem(seen))
		&&(canSeeInfrared(seer))
		&&(seen instanceof MOB)
		&&(isInDark(seer.location())))
			Say.append(" (^rheat aura^?)");
		if((isABonusItems(seen))&&(canSeeBonusItems(seer)))
			Say.append(" (^wmagical aura^?)");
		if((canSeeMetal(seer))&&(seen instanceof Item))
			if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
				Say.append(" (^wmetallic aura^?)");
			else
			if((((Item)seen).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
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
		if(isBusy(seen))
			Say.append(" (^gbusy^?)");
        if(Say.length()>1)
        {
            Say.append(" ");
    		return Say;
        }
        return new StringBuffer("");
	}

	public boolean seenTheSameWay(MOB seer, Environmental seen1, Environmental seen2)
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
		return true;
	}

	public final static int flag_arrives=0;
	public final static int flag_leaves=1;
	public final static int flag_is=2;
	public String dispositionString(Environmental seen, int flag_msgType)
	{
		String type=null;
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

	public boolean isWaterWorthy(Environmental E)
	{
		if(E==null) return false;
		if(isSwimming(E)) return true;
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
				totalFloatilla+=totalWeight*RawMaterial.RESOURCE_DATA[I.material()&RawMaterial.RESOURCE_MASK][4];
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


	public boolean isInFlight(Environmental E)
	{
		if(E==null) return false;
		if(isFlying(E)) return true;
		if(E instanceof Rider)
			return isInFlight(((Rider)E).riding());
		return false;
	}

	public boolean isAnimalIntelligence(MOB E)
	{
		return (E!=null)&&(E.charStats().getStat(CharStats.STAT_INTELLIGENCE)<2);
	}
	public boolean isVegetable(MOB E)
	{
		return (E!=null)&&(E.charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation"));
	}


	public boolean isMobile(Environmental E)
	{
		if(E!=null)
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(CMath.bset(B.flags(),Behavior.FLAG_MOBILITY)))
					return true;
			}
		return false;
	}

	public Vector flaggedBehaviors(Environmental E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int b=0;b<E.numBehaviors();b++)
			{
				Behavior B=E.fetchBehavior(b);
				if((B!=null)&&(CMath.bset(B.flags(),flag)))
				{ V.addElement(B);}
			}
		return V;
	}

	
	public Vector flaggedAnyAffects(Environmental E, long flag)
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
	public Vector flaggedAffects(Environmental E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int a=0;a<E.numEffects();a++)
			{
				Ability A=E.fetchEffect(a);
				if((A!=null)&&(CMath.bset(A.flags(),flag)))
				{ V.addElement(A);}
			}
		return V;
	}

	public Vector flaggedAbilities(MOB E, long flag)
	{
		Vector V=new Vector();
		if(E!=null)
			for(int a=0;a<E.numAbilities();a++)
			{
				Ability A=E.fetchAbility(a);
				if((A!=null)&&(CMath.bset(A.flags(),flag)))
				{ V.addElement(A);}
			}
		return V;
	}

	public boolean canAccess(MOB mob, Area A)
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
	public boolean canAccess(MOB mob, Room R)
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
			switch(lighting.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LEATHER:
				return 20+lighting.envStats().weight();
			case RawMaterial.MATERIAL_CLOTH:
			case RawMaterial.MATERIAL_PAPER:
			case RawMaterial.MATERIAL_PLASTIC:
				return 5+lighting.envStats().weight();
			case RawMaterial.MATERIAL_WOODEN:
				return 150+(lighting.envStats().weight()*5);
			case RawMaterial.MATERIAL_VEGETATION:
			case RawMaterial.MATERIAL_FLESH:
				return -1;
			case RawMaterial.MATERIAL_UNKNOWN:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_ENERGY:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_PRECIOUS:
				return 0;
			}
		}
		return 0;
	}
	
	public boolean isInTheGame(Environmental E, boolean reqInhabitation)
	{
		if(E instanceof Room)
			return CMLib.map().getRoom(CMLib.map().getExtendedRoomID((Room)E))==E;
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
			return CMLib.map().getArea(E.Name())==E;
		else
			return true;
		
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
	
	
	public String wornLocation(long wornCode)
	{
		for(int wornNum=0;wornNum<Item.WORN_DESCS.length-1;wornNum++)
		{
			if(wornCode==(1<<wornNum))
				return Item.WORN_DESCS[wornNum+1];
		}
		return "";
	}
	
	public boolean stillAffectedBy(Environmental obj, Vector oneOf, boolean anyTallF)
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
    
    public String dispositionList(int disposition, boolean useVerbs)
    {
        StringBuffer buf=new StringBuffer("");
        if(useVerbs)
        {
            for(int i=0;i<EnvStats.dispositionsVerb.length;i++)
                if(CMath.isSet(disposition,i))
                    buf.append(EnvStats.dispositionsVerb[i]+", ");
        }
        else
        for(int i=0;i<EnvStats.dispositionsNames.length;i++)
            if(CMath.isSet(disposition,i))
                buf.append(EnvStats.dispositionsNames[i]+", ");
        String buff=buf.toString();
        if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
        return buff;
    }
    
    public String sensesList(int disposition, boolean useVerbs)
    {
        StringBuffer buf=new StringBuffer("");
        if(useVerbs)
        {
            for(int i=0;i<EnvStats.sensesVerb.length;i++)
                if(CMath.isSet(disposition,i))
                    buf.append(EnvStats.sensesVerb[i]+", ");
        }
        else
        for(int i=0;i<EnvStats.sensesNames.length;i++)
            if(CMath.isSet(disposition,i))
                buf.append(EnvStats.sensesNames[i]+", ");
        String buff=buf.toString();
        if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
        return buff;
    }
    
    public int getDispositionCode(String name)
    {
        name=name.toUpperCase().trim();
        for(int code=0;code<EnvStats.dispositionsNames.length-1;code++)
            if(EnvStats.dispositionsNames[code].endsWith(name))
                return code;
        return -1;
    }
    
    public int getSensesCode(String name)
    {
        name=name.toUpperCase().trim();
        for(int code=0;code<EnvStats.sensesNames.length-1;code++)
            if(EnvStats.sensesNames[code].endsWith(name))
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
    
}
