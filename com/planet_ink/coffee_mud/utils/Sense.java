package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Sense
{
	public static boolean canSee(Environmental E)
	{ return (!isSleeping(E))&&((E.envStats().sensesMask()&EnvStats.CAN_NOT_SEE)==0); }
	public static boolean canSeeHidden(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_HIDDEN)==EnvStats.CAN_SEE_HIDDEN); }
	public static boolean canSeeInvisible(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_INVISIBLE)==EnvStats.CAN_SEE_INVISIBLE); }
	public static boolean canSeeEvil(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_EVIL)==EnvStats.CAN_SEE_EVIL); }
	public static boolean canSeeGood(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_GOOD)==EnvStats.CAN_SEE_GOOD); }
	public static boolean canSeeSneakers(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_SNEAKERS)==EnvStats.CAN_SEE_SNEAKERS); }
	public static boolean canSeeBonusItems(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_BONUS)==EnvStats.CAN_SEE_BONUS); }
	public static boolean canSeeInDark(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_DARK)==EnvStats.CAN_SEE_DARK); }
	public static boolean canSeeVictims(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_VICTIM)==EnvStats.CAN_SEE_VICTIM); }
	public static boolean canSeeInfrared(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_INFRARED)==EnvStats.CAN_SEE_INFRARED); }
	public static boolean canHear(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_HEAR)==0); }
	public static boolean canMove(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_MOVE)==0); }
	public static boolean canSmell(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_SMELL)==0); }
	public static boolean canTaste(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_TASTE)==0); }
	public static boolean canSpeak(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_SPEAK)==0); }
	public static boolean canBreathe(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_NOT_BREATHE)==0); }
	public static boolean canSeeMetal(Environmental E)
	{ return ((E.envStats().sensesMask()&EnvStats.CAN_SEE_METAL)==EnvStats.CAN_SEE_METAL); }
	
	public static boolean isSeen(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_NOT_SEEN)==0) || isSleeping(E); }
	public static boolean isHidden(Environmental E)
	{
		boolean isInHide=((E.envStats().disposition()&EnvStats.IS_HIDDEN)==EnvStats.IS_HIDDEN);
		if((isInHide)
		&&(E!=null)
		&&(E instanceof MOB)
		&&(((MOB)E).isInCombat()))
			return false;
		return isInHide; 
	}
	public static boolean isInvisible(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_INVISIBLE)==EnvStats.IS_INVISIBLE); }
	public static boolean isEvil(Environmental E)
	{
		if ((E.envStats().disposition()&EnvStats.IS_EVIL)==EnvStats.IS_EVIL)
			return true;
		else
		if(E instanceof MOB)
		{
			if(((MOB)E).getAlignment()<350)
				return true;
		}
		return false;
	}
	
	public static boolean isATrackingMonster(Environmental E)
	{
		if(E==null) return false;
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if((A!=null)
			&&(Util.bset(A.flags(),Ability.FLAG_TRACKING))
			&&(E instanceof MOB)
			&&(((MOB)E).isMonster()))
				return true;
		}
		return false;
	}
	
	public static boolean isGood(Environmental E)
	{
		if ((E.envStats().disposition()&EnvStats.IS_GOOD)==EnvStats.IS_GOOD)
			return true;
		else
		if(E instanceof MOB)
			if(((MOB)E).getAlignment()>650)
				return true;
		return false;
	}
	public static boolean isSneaking(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_SNEAKING)==EnvStats.IS_SNEAKING); }
	public static boolean isABonusItems(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS); }
	public static boolean isInDark(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_DARK)==EnvStats.IS_DARK); }
	public static boolean isLightSource(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_LIGHTSOURCE)==EnvStats.IS_LIGHTSOURCE); }
	public static boolean isGlowing(Environmental E)
	{ return (isLightSource(E)||((E.envStats().disposition()&EnvStats.IS_GLOWING)==EnvStats.IS_GLOWING)); }
	public static boolean isGolem(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_GOLEM)==EnvStats.IS_GOLEM); }
	public static boolean isSleeping(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_SLEEPING)==EnvStats.IS_SLEEPING); }
	public static boolean isSitting(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_SITTING)==EnvStats.IS_SITTING); }
	public static boolean isFlying(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_FLYING)==EnvStats.IS_FLYING); }
	public static boolean isClimbing(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_CLIMBING)==EnvStats.IS_CLIMBING); }
	public static boolean isSwimming(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_SWIMMING)==EnvStats.IS_SWIMMING); }
	public static boolean isFalling(Environmental E)
	{ return ((E.envStats().disposition()&EnvStats.IS_FALLING)==EnvStats.IS_FALLING); }

	public static boolean canBeHeardBy(Environmental heard , Environmental hearer)
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
	
	public static boolean canSenseMoving(Environmental sensed, Environmental sensor)
	{
		if(isSneaking(sensed)&&(!canSeeSneakers(sensor)))
		   return false;
		return (canBeHeardBy(sensed,sensor)||canBeSeenBy(sensed,sensor));
	}

	public static boolean aliveAwakeMobile(MOB mob, boolean quiet)
	{
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

	public static boolean isBound(Environmental E)
	{
		if(E==null) return false;
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if((A!=null)&&(Util.bset(A.flags(),Ability.FLAG_BINDING)))
			   return true;
		}
		return false;
	}
	public static boolean isOnFire(Environmental seen)
	{
		if(seen==null) return false;
		if(seen.fetchAffect("Burning")!=null)
			return true;
		if(seen.fetchAffect("Prayer_FlameWeapon")!=null)
			return true;
		if(!(seen instanceof Light))
			return false;
		Light light=(Light)seen;
		if(light.goesOutInTheRain()
		   &&light.isLit())
			return true;
		return false;
	}
	
	public static boolean canBeSeenBy(Environmental seen , Environmental seer)
	{
		if(seer==seen) return true;
		if(seen==null) return true;

		if((seer instanceof MOB)
		&&(Util.bset(((MOB)seer).getBitmap(),MOB.ATT_SYSOPMSGS)))
			return true;
		
		if(!canSee(seer)) return false;
		if(!isSeen(seen)) return false;

		if((isInvisible(seen))&&(!canSeeInvisible(seer)))
		   return false;
		
		if((isHidden(seen))&&(!canSeeHidden(seer)))
		   return false;
		
		if((seer instanceof MOB)&&(!(seen instanceof Room)))
		{
			MOB mob=(MOB)seer;
			if(mob.location()!=null)
			{
				if(isInDark(mob.location()))
				{
					if((isGlowing(seen))||(isLightSource(seer)))
						return true;
					if(canSeeInDark(seer))
						return true;
					if((!isGolem(seen))&&(canSeeInfrared(seer)))
					   return true;
					if((canSeeVictims(seer))&&(mob.getVictim()==seen))
						return true;
					return false;
				}
			}
			return true;
		}
		else
		if(isInDark(seen))
		{
			if(isLightSource(seer))
				return true;
			if(canSeeInDark(seer))
				return true;
			if((isGolem(seen))&&(canSeeInfrared(seer)))
			   return true;
			return false;
		}
		return true;
	}
	public static StringBuffer colorCodes(Environmental seen , Environmental seer)
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
		&&(isInDark(((MOB)seer).location())))
			Say.append(" (^rheat aura^?)");
		if((Sense.isABonusItems(seen))&&(Sense.canSeeBonusItems(seer)))
			Say.append(" (^wmagical aura^?)");
		if((Sense.canSeeMetal(seer))&&(seen instanceof Item))
			if((((Item)seen).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				Say.append(" (^wmetallic aura^?)");
			else
			if((((Item)seen).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL)
				Say.append(" (^wmithril aura^?)");
		
		if(isFlying(seen))
			Say.append(" (^pflying^?)");
		if(isFalling(seen))
			Say.append(" (^pfalling^?)");
		if((isGlowing(seen))&&(seen instanceof Item))
			Say.append(" (^gglowing^?)");
		return Say;
	}

	public static boolean seenTheSameWay(Environmental seer, Environmental seen1, Environmental seen2)
	{
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
			type="flys";
		else
		if(isFalling(seen))
			type="falls";
		else
		if(isClimbing(seen))
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
		else
			return type;

	}

	public static boolean isWaterWorthy(Environmental E)
	{
		if(E==null) return false;
		if(Sense.isSwimming(E)) return true;
		if(E instanceof Rider)
			return isWaterWorthy(((Rider)E).riding());
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
		return (E.charStats().getStat(CharStats.INTELLIGENCE)<2);
	}
	public static boolean isVegetable(MOB E)
	{
		return (E.charStats().getMyRace().racialCategory().equalsIgnoreCase("Vegetation"));
	}
	
	public static boolean isMobile(Environmental E)
	{
		if(E==null) return false;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_MOBILITY)))
				return true;
		}
		return false;
	}
	
	public static boolean canAccess(MOB mob, Area A)
	{
		if(A==null) return false;
		if((!isHidden(A))||(mob.isASysOp(null))||(A.amISubOp(mob.Name())))
			return true;
		return false;
	}
	public static boolean canAccess(MOB mob, Room R)
	{
		if(R==null) return false;
		if((!isHidden(R))||(mob.isASysOp(R)))
			return true;
		return false;
	}
	
	public static String wornLocation(long wornCode)
	{
		for(int wornNum=0;wornNum<20;wornNum++)
		{
			if(wornCode==(1<<wornNum))
				return Item.wornLocation[wornNum+1];
		}
		return "";
	}
}
