package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Amputation extends StdAbility
{
	public String ID() { return "Amputation"; }
	public String name(){ return "Amputation";}
	public String displayText(){
		long missingLimbList=missingLimbList();
		StringBuffer buf=new StringBuffer("");
		for(int i=0;i<AMPUTATE_BITS;i++)
			if(Util.isSet((int)missingLimbList,i))
				buf.append(", "+AMPUTATE_DESCS[i]);
		if(buf.length()==0) return "";
		return "(Missing your"+buf.substring(1)+")";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Amputation();}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"AMPUTATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.SKILL;}

	public static final long AMPUTATE_LEFTHAND=1;
	public static final long AMPUTATE_RIGHTHAND=2;
	public static final long AMPUTATE_LEFTARM=4|AMPUTATE_LEFTHAND;
	public static final long AMPUTATE_RIGHTARM=8|AMPUTATE_RIGHTHAND;
	public static final long AMPUTATE_LEFTFOOT=16;
	public static final long AMPUTATE_RIGHTFOOT=32;
	public static final long AMPUTATE_LEFTLEG=64|AMPUTATE_LEFTFOOT;
	public static final long AMPUTATE_RIGHTLEG=128|AMPUTATE_RIGHTFOOT;
	public static final long AMPUTATE_LEFTEAR=256;
	public static final long AMPUTATE_RIGHTEAR=512;
	public static final long AMPUTATE_LEFTEYE=1024;
	public static final long AMPUTATE_RIGHTEYE=2048;
	public static final long AMPUTATE_BOTHARMS=AMPUTATE_LEFTARM|AMPUTATE_RIGHTARM;
	public static final long AMPUTATE_BOTHLEGS=AMPUTATE_LEFTLEG|AMPUTATE_RIGHTLEG;
	public static final long AMPUTATE_BOTHFEET=AMPUTATE_LEFTFOOT|AMPUTATE_RIGHTFOOT;
	public static final long AMPUTATE_BOTHHANDS=AMPUTATE_LEFTHAND|AMPUTATE_RIGHTHAND;
	public static final long AMPUTATE_BOTHEARS=AMPUTATE_LEFTEAR|AMPUTATE_RIGHTEAR;
	public static final long AMPUTATE_BOTHEYES=AMPUTATE_LEFTEYE|AMPUTATE_RIGHTEYE;
	public static final long AMPUTATE_ARMSNLEGS=AMPUTATE_BOTHARMS|AMPUTATE_BOTHLEGS;
	public static final long AMPUTATE_BITS=12;
	public static final String[] AMPUTATE_DESCS={"left hand","right hand",
												 "left arm","right arm",
												 "left foot","right foot",
												 "left leg","right leg",
												 "left ear","right ear",
												 "left eye","right eye"};
	public static final long[] AMPUTATE_CODES=	 {AMPUTATE_LEFTHAND,AMPUTATE_LEFTHAND,
												 AMPUTATE_LEFTARM,AMPUTATE_RIGHTARM,
												 AMPUTATE_LEFTFOOT,AMPUTATE_RIGHTFOOT,
												 AMPUTATE_LEFTLEG,AMPUTATE_RIGHTLEG,
												 AMPUTATE_LEFTEAR,AMPUTATE_RIGHTEAR,
												 AMPUTATE_LEFTEYE,AMPUTATE_RIGHTEYE};
	public static final long[] AMPUTATE_WEARS=	 {Item.ON_LEFT_FINGER,Item.ON_RIGHT_FINGER,
												 Item.ON_LEFT_WRIST,Item.ON_RIGHT_WRIST,
												 0,0,
												 0,0,
												 AMPUTATE_LEFTEAR,AMPUTATE_RIGHTEAR,
												 AMPUTATE_LEFTEYE,AMPUTATE_RIGHTEYE};

	protected long missingLimbList(){ return Util.s_long(text());}
	public boolean canWear(long missingLimbs, Item item)
	{
		long forbiddenWornBits=0;
		if((missingLimbs&AMPUTATE_BOTHARMS)==AMPUTATE_BOTHARMS)
			forbiddenWornBits=forbiddenWornBits|Item.ON_ARMS;
		if((missingLimbs&AMPUTATE_BOTHLEGS)==AMPUTATE_BOTHLEGS)
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEGS;
		if((missingLimbs&AMPUTATE_BOTHFEET)==AMPUTATE_BOTHFEET)
			forbiddenWornBits=forbiddenWornBits|Item.ON_FEET;
		if((missingLimbs&AMPUTATE_BOTHEYES)==AMPUTATE_BOTHEYES)
			forbiddenWornBits=forbiddenWornBits|Item.ON_EYES;
		if((missingLimbs&AMPUTATE_BOTHEARS)>0)
			forbiddenWornBits=forbiddenWornBits|Item.ON_EARS;
		if((missingLimbs&AMPUTATE_LEFTHAND)>0)
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEFT_FINGER;
		if((missingLimbs&AMPUTATE_RIGHTHAND)>0)
			forbiddenWornBits=forbiddenWornBits|Item.ON_RIGHT_FINGER;
		if((missingLimbs&AMPUTATE_LEFTARM)>0)
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEFT_WRIST;
		if((missingLimbs&AMPUTATE_RIGHTARM)>0)
			forbiddenWornBits=forbiddenWornBits|Item.ON_RIGHT_WRIST;
		if((missingLimbs&AMPUTATE_BOTHHANDS)==AMPUTATE_BOTHHANDS)
			forbiddenWornBits=forbiddenWornBits|Item.ON_HANDS;

		if((item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&forbiddenWornBits)>0))
			return false;
		else
		if((!item.rawLogicalAnd())&&((item.rawProperLocationBitmap()&(Integer.MAX_VALUE-forbiddenWornBits))==0))
			return false;
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		long missingLimbList=missingLimbList();
		if((missingLimbList&AMPUTATE_BOTHLEGS)==AMPUTATE_BOTHLEGS)
		{
			if((missingLimbList&AMPUTATE_ARMSNLEGS)==AMPUTATE_ARMSNLEGS)
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
			affectableStats.setDisposition(affectableStats.disposition()|affectableStats.IS_SITTING);
		}
		if((missingLimbList&AMPUTATE_BOTHEYES)==AMPUTATE_BOTHEYES)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
		if((missingLimbList&AMPUTATE_BOTHEARS)==AMPUTATE_BOTHEARS)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if((missingLimbList()&AMPUTATE_BOTHFEET)>0)
		{
			if((missingLimbList()&AMPUTATE_BOTHFEET)==AMPUTATE_BOTHFEET)
				affectableState.setMovement(affectableState.getMovement()/8);
			else
				affectableState.setMovement(affectableState.getMovement()/4);
		}
	}
	public boolean okAffect(Affect affect)
	{
		MOB myChar=(MOB)affected;
		if(myChar==null) return false;
		if((affect.amISource(myChar))
			&&(!Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&((affect.sourceCode()&Affect.MASK_GENERAL)==0))
		{
			long missingLimbList=missingLimbList();
			switch(affect.targetMinor())
			{
			case Affect.TYP_PULL:
			case Affect.TYP_PUSH:
			case Affect.TYP_GET:
			case Affect.TYP_CLOSE:
			case Affect.TYP_DROP:
			case Affect.TYP_OPEN:
				if((missingLimbList&AMPUTATE_BOTHARMS)==AMPUTATE_BOTHARMS)
				{
					myChar.tell("Your condition prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_DELICATE_HANDS_ACT:
			case Affect.TYP_FILL:
			case Affect.TYP_GIVE:
			case Affect.TYP_HANDS:
			case Affect.TYP_LOCK:
			case Affect.TYP_PUT:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_WRITE:
				if((missingLimbList&AMPUTATE_BOTHHANDS)==AMPUTATE_BOTHHANDS)
				{
					myChar.tell("Your condition prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_HOLD:
			case Affect.TYP_WIELD:
				if((affect.target()!=null)
				&&((missingLimbList&AMPUTATE_BOTHHANDS)>0)
				&&(affect.target() instanceof Item))
				{
					if(((missingLimbList&AMPUTATE_BOTHHANDS)!=AMPUTATE_BOTHHANDS)
					&&(!((Item)affect.target()).rawLogicalAnd())
					&&((!myChar.amWearingSomethingHere(Item.HELD))&&(!myChar.amWearingSomethingHere(Item.WIELD))))
						break;
					switch(affect.targetMinor())
					{
						case Affect.TYP_HOLD:
							myChar.tell("Your lack of limbs prevents you from holding "+affect.target().name()+".");
							break;
						case Affect.TYP_WIELD:
							myChar.tell("Your lack of limbs prevents you from wielding "+affect.target().name()+".");
							break;
					}
					return false;
				}
				break;
			case Affect.TYP_WEAR:
				if((affect.target()!=null)
				&&(affect.target() instanceof Item)
				&&(!canWear(missingLimbList,(Item)affect.target())))
				{
					myChar.tell("Your lack of limbs prevents you from putting on "+affect.target().name()+".");
					return false;
				}
				break;
			case Affect.TYP_DRINK:
				if((missingLimbList&AMPUTATE_BOTHHANDS)==AMPUTATE_BOTHHANDS)
				{
					if(affect.target()==null) return true;
					if(!myChar.isMine(affect.target())) return true;
					myChar.tell("Your lack the limbs prevents you from drinking from that.");
					return false;
				}
				break;
			}
		}
		else
		if((affect.amITarget(myChar))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&((missingLimbList()&AMPUTATE_BOTHARMS)==AMPUTATE_BOTHARMS))
		{
			affect.source().tell("You cannot give anything to the "+name()+".");
			return false;
		}
		return super.okAffect(affect);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your limbs have been restored.");
	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(quality()==Ability.MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(quality()!=Ability.MALICIOUS))
			target=mob;
		else
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell("You can't do that to '"+targetName+"'.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}
		return target;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			Amputation A=(Amputation)target.fetchAffect(ID());
			boolean newOne=false;
			if(A==null){
				A=new Amputation();
				newOne=true;
			}

			Vector V=new Vector();
			long bit=0;
			long missingLimbList=A.missingLimbList();
			for(int i=0;i<AMPUTATE_BITS;i++)
			{
				if(!Util.isSet((int)missingLimbList,i))
					V.addElement(new Integer(i));
			}
			if(V.size()==0)
			{
				if(!auto)
					mob.tell("There is nothing left on "+target.name()+" to amputate!");
				return false;
			}
			bit=((Integer)V.elementAt(Dice.roll(1,V.size(),-1))).longValue();
			String gone=AMPUTATE_DESCS[(int)bit];
			long code=AMPUTATE_CODES[(int)bit];

			String str=auto?"":"^F<S-NAME> amputate <T-NAMESELF>'s "+gone+"!^?";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_DELICATE_HANDS_ACT|(auto?Affect.MASK_GENERAL:0),str);
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				if(!msg.wasModified())
				{
					if((AMPUTATE_BOTHEYES&bit)>0)
						target.location().show(target,null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" is destroyed!^?");
					else
					{
						target.location().show(target,null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" falls off!^?");
						Item limb=CMClass.getItem("GenItem");
						limb.setName("a "+gone);
						limb.setDisplayText("a bloody "+gone+" is sitting here.");
						limb.setSecretIdentity(target.name()+"`s bloody "+gone+".");
						limb.baseEnvStats().setLevel(1);
						limb.baseEnvStats().setWeight(5);
						limb.recoverEnvStats();
						target.location().addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
					}
					if(newOne==true)
						target.addNonUninvokableAffect(A);
					A.setMiscText(""+(A.missingLimbList()|code));
					mob.confirmWearability();
				}
			}
			else
				success=false;
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to amputate <T-NAMESELF>, but fail(s).");
        return success;
	}
}
