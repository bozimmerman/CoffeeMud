package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;


public class Sense
{
	// sensemask stuff
	public final static int CAN_SEE=1;
	public final static int CAN_SEE_HIDDEN=2;
	public final static int CAN_SEE_INVISIBLE=4;
	public final static int CAN_SEE_EVIL=8;
	public final static int CAN_SEE_GOOD=16;
	public final static int CAN_SEE_SNEAKERS=32;
	public final static int CAN_SEE_BONUS=64;
	public final static int CAN_SEE_DARK=128;
	public final static int CAN_SEE_INFRARED=256;
	public final static int CAN_HEAR=512;
	public final static int CAN_MOVE=1024;
	public final static int CAN_SMELL=2048;
	public final static int CAN_TASTE=4096;
	public final static int CAN_SPEAK=8192;
	public final static int CAN_BREATHE=16384;
	public final static int CAN_SEE_VICTIM=32768;
	public final static int CAN_SEE_METAL=65536;

	public final static long ALLMASK=(int)Math.round((Integer.MAX_VALUE/2)-0.5);

	public static boolean canSee(Environmental E)
	{ return (!isSleeping(E))&&((E.envStats().sensesMask()&CAN_SEE)==0); }
	public static boolean canSeeHidden(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_HIDDEN)==CAN_SEE_HIDDEN); }
	public static boolean canSeeInvisible(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_INVISIBLE)==CAN_SEE_INVISIBLE); }
	public static boolean canSeeEvil(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_EVIL)==CAN_SEE_EVIL); }
	public static boolean canSeeGood(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_GOOD)==CAN_SEE_GOOD); }
	public static boolean canSeeSneakers(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_SNEAKERS)==CAN_SEE_SNEAKERS); }
	public static boolean canSeeBonusItems(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_BONUS)==CAN_SEE_BONUS); }
	public static boolean canSeeInDark(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_DARK)==CAN_SEE_DARK); }
	public static boolean canSeeVictims(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_VICTIM)==CAN_SEE_VICTIM); }
	public static boolean canSeeInfrared(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_INFRARED)==CAN_SEE_INFRARED); }
	public static boolean canHear(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_HEAR)==0); }
	public static boolean canMove(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_MOVE)==0); }
	public static boolean canSmell(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SMELL)==0); }
	public static boolean canTaste(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_TASTE)==0); }
	public static boolean canSpeak(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SPEAK)==0); }
	public static boolean canBreathe(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_BREATHE)==0); }
	public static boolean canSeeMetal(Environmental E)
	{ return ((E.envStats().sensesMask()&CAN_SEE_METAL)==CAN_SEE_METAL); }

	// dispositions
	public final static int IS_SEEN=1;
	public final static int IS_HIDDEN=2;
	public final static int IS_INVISIBLE=4;
	public final static int IS_EVIL=8;
	public final static int IS_GOOD=16;
	public final static int IS_SNEAKING=32;
	public final static int IS_BONUS=64;
	public final static int IS_DARK=128;
	public final static int IS_INFRARED=256;
	public final static int IS_SLEEPING=512;
	public final static int IS_SITTING=1024;
	public final static int IS_FLYING=2048;
	public final static int IS_SWIMMING=4096;
	public final static int IS_LIGHT=8192;
	public final static int IS_CLIMBING=16384;

	public static boolean isSeen(Environmental E)
	{ return ((E.envStats().disposition()&IS_SEEN)==0) || isSleeping(E); }
	public static boolean isHidden(Environmental E)
	{
		boolean isInHide=((E.envStats().disposition()&IS_HIDDEN)==IS_HIDDEN);
		if((isInHide)
		&&(E!=null)
		&&(E instanceof MOB)
		&&(((MOB)E).isInCombat()))
			return false;
		return isInHide; 
	}
	public static boolean isInvisible(Environmental E)
	{ return ((E.envStats().disposition()&IS_INVISIBLE)==IS_INVISIBLE); }
	public static boolean isEvil(Environmental E)
	{
		if ((E.envStats().disposition()&IS_EVIL)==IS_EVIL)
			return true;
		else
		if(E instanceof MOB)
		{
			if(((MOB)E).getAlignment()<350)
				return true;
		}
		return false;
	}
	public static boolean isGood(Environmental E)
	{
		if ((E.envStats().disposition()&IS_GOOD)==IS_GOOD)
			return true;
		else
		if(E instanceof MOB)
			if(((MOB)E).getAlignment()>650)
				return true;
		return false;
	}
	public static boolean isSneaking(Environmental E)
	{ return ((E.envStats().disposition()&IS_SNEAKING)==IS_SNEAKING); }
	public static boolean isABonusItems(Environmental E)
	{ return ((E.envStats().disposition()&IS_BONUS)==IS_BONUS); }
	public static boolean isInDark(Environmental E)
	{ return ((E.envStats().disposition()&IS_DARK)==IS_DARK); }
	public static boolean isLight(Environmental E)
	{ return ((E.envStats().disposition()&IS_LIGHT)==IS_LIGHT); }
	public static boolean isInfrared(Environmental E)
	{ return ((E.envStats().disposition()&IS_INFRARED)==IS_INFRARED); }
	public static boolean isSleeping(Environmental E)
	{ return ((E.envStats().disposition()&IS_SLEEPING)==IS_SLEEPING); }
	public static boolean isSitting(Environmental E)
	{ return ((E.envStats().disposition()&IS_SITTING)==IS_SITTING); }
	public static boolean isFlying(Environmental E)
	{ return ((E.envStats().disposition()&IS_FLYING)==IS_FLYING); }
	public static boolean isClimbing(Environmental E)
	{ return ((E.envStats().disposition()&IS_CLIMBING)==IS_CLIMBING); }
	public static boolean isSwimming(Environmental E)
	{ return ((E.envStats().disposition()&IS_SWIMMING)==IS_SWIMMING); }

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

	public static boolean canBeSeenBy(Environmental seen , Environmental seer)
	{

		if(seer==seen) return true;
		if(seen==null) return true;

		if(!canSee(seer)) return false;
		if(!isSeen(seen)) return false;

		if((isInvisible(seen))&&(!canSeeInvisible(seer)))
		   return false;
		if((isHidden(seen))&&(!canSeeHidden(seer)))
		   return false;
		if((seer instanceof MOB)&&(!(seen instanceof Room)))
		{
			MOB mob=(MOB)seer;
			if(isInDark(mob.location()))
			{
				if((isLight(seen))||(isLight(seer)))
					return true;
				if(canSeeInDark(seer))
					return true;
				if((isInfrared(seen))&&(canSeeInfrared(seer)))
				   return true;
				if((canSeeVictims(seer))&&(mob.getVictim()==seen))
					return true;
				return false;
			}
			return true;
		}
		else
		if(isInDark(seen))
		{
			if(isLight(seer))
				return true;
			if(canSeeInDark(seer))
				return true;
			if((isInfrared(seen))&&(canSeeInfrared(seer)))
			   return true;
			return false;
		}
		return true;
	}
	public static StringBuffer colorCodes(Environmental seen , Environmental seer)
	{
		StringBuffer Say=new StringBuffer("");

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
		if((Sense.isInfrared(seen))
		&&(Sense.canSeeInfrared(seer))
		&&(seer instanceof MOB)
		&&(isInDark(((MOB)seer).location())))
			Say.append(" (^rheat aura^?)");
		if((Sense.isABonusItems(seen))&&(Sense.canSeeBonusItems(seer)))
			Say.append(" (^wmagical aura^?)");
		if((Sense.canSeeMetal(seer))&&(seen instanceof Item))
			if(((Item)seen).material()==Item.METAL)
				Say.append(" (^wmetallic aura^?)");
			else
			if(((Item)seen).material()==Item.MITHRIL)
				Say.append(" (^wmithril aura^?)");
		
		if(isFlying(seen))
			Say.append(" (^pflying^?)");
		if((isLight(seen))&&(seen instanceof Item))
			Say.append(" (^gglowing^?)");
		return Say;
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


	public static String wornLocation(long wornCode)
	{
		switch((int)wornCode)
		{
		case (int)Item.INVENTORY:
			return "inventory";
		case (int)Item.ON_HEAD:
			return "head";
		case (int)Item.ON_NECK:
			return "neck";
		case (int)Item.ON_TORSO:
			return "torso";
		case (int)Item.ON_ARMS:
			return "arms";
		case (int)Item.ON_HANDS:
			return "hands";
		case (int)Item.ON_LEFT_WRIST:
			return "right wrist";
		case (int)Item.ON_RIGHT_WRIST:
			return "left wrist";
		case (int)Item.ON_LEFT_FINGER:
			return "left finger";
		case (int)Item.ON_RIGHT_FINGER:
			return "right finger";
		case (int)Item.ON_FEET:
			return "feet";
		case (int)Item.HELD:
			return "held";
		case (int)Item.WIELD:
			return "wield";
		case (int)Item.FLOATING_NEARBY:
			return "floating nearby";
		case (int)Item.ON_WAIST:
			return "waist";
		case (int)Item.ON_LEGS:
			return "legs";
		default:
			return "body";
		}
	}
}
