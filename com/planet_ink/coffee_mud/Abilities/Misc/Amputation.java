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
		StringBuffer buf=new StringBuffer("");
		for(int i=0;i<missingLimbNameSet().size();i++)
			buf.append(", "+missingLimbNameSet().elementAt(i));
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
	public boolean isAutoInvoked(){return true;}
	public int classificationCode(){return Ability.PROPERTY;}
	private Vector missingLimbs=null;
	private long forbiddenWornBits=-1;

	public final static boolean[] validamputees={true,//antenea
												 true,//eye
												 true,//ear
												 false,//head
												 false,//neck
												 true,//arm
												 true,//hand
												 false,//torso
												 true,//leg
												 true,//foot
												 true,//nose
												 false,//gills
												 false,//mouth
												 false,//waist
												 true,//tail
												 true//wing
												 };
	
	public final static int[][] extraamuputees={{-1},//antenea
											    {-1},//eye
											    {-1},//ear
											    {Race.BODY_EAR,Race.BODY_EYE,Race.BODY_MOUTH,Race.BODY_NOSE,Race.BODY_ANTENEA,Race.BODY_GILL},//head
												{Race.BODY_EAR,Race.BODY_EYE,Race.BODY_MOUTH,Race.BODY_NOSE,Race.BODY_ANTENEA,Race.BODY_GILL,Race.BODY_HEAD},//nect
												{Race.BODY_HAND},//arm
												{-1},//hand
												{-1},//torso
												{Race.BODY_FOOT},//leg
												{-1},//foot
												{-1},//nose
												{-1},//gills
												{-1},//mouth
												{-1},//waist
												{-1},//tail
												{-1}//wing
												};
	public boolean canWear(Item item)
	{
		missingLimbNameSet();
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
		missingLimbNameSet();
		if(Util.bset(forbiddenWornBits,Item.ON_LEGS))
		{
			if(Util.bset(forbiddenWornBits,Item.ON_ARMS))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
			affectableStats.setDisposition(affectableStats.disposition()|affectableStats.IS_SITTING);
		}
		if(Util.bset(forbiddenWornBits,Item.ON_EYES))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
		if(Util.bset(forbiddenWornBits,Item.ON_EARS))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		missingLimbNameSet();
		if(Util.bset(forbiddenWornBits,Item.ON_LEGS))
			affectableState.setMovement(affectableState.getMovement()/8);
		else
		if(Util.bset(forbiddenWornBits,Item.ON_FEET))
			affectableState.setMovement(affectableState.getMovement()/4);
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		MOB myChar=(MOB)affected;
		if(myChar==null) return false;
		if((affect.amISource(myChar))
			&&(!Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&((affect.sourceCode()&Affect.MASK_GENERAL)==0))
		{
			missingLimbNameSet();
			switch(affect.targetMinor())
			{
			case Affect.TYP_PULL:
			case Affect.TYP_PUSH:
			case Affect.TYP_GET:
			case Affect.TYP_REMOVE:
			case Affect.TYP_CLOSE:
			case Affect.TYP_DROP:
			case Affect.TYP_THROW:
			case Affect.TYP_OPEN:
				if(Util.bset(forbiddenWornBits,Item.ON_ARMS))
				{
					myChar.tell("Your condition prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_DELICATE_HANDS_ACT:
			case Affect.TYP_JUSTICE:
			case Affect.TYP_FILL:
			case Affect.TYP_GIVE:
			case Affect.TYP_HANDS:
			case Affect.TYP_LOCK:
			case Affect.TYP_PUT:
			case Affect.TYP_UNLOCK:
			case Affect.TYP_WRITE:
				if(Util.bset(forbiddenWornBits,Item.ON_HANDS))
				{
					myChar.tell("Your condition prevents you from doing that.");
					return false;
				}
				break;
			case Affect.TYP_HOLD:
			case Affect.TYP_WIELD:
				if((affect.target()!=null)
				&&(Util.bset(forbiddenWornBits,Item.ON_LEFT_FINGER)||Util.bset(forbiddenWornBits,Item.ON_RIGHT_FINGER))
				&&(affect.target() instanceof Item))
				{
					if((Util.bset(forbiddenWornBits,Item.ON_LEFT_FINGER)!=Util.bset(forbiddenWornBits,Item.ON_RIGHT_FINGER))
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
				&&(!canWear((Item)affect.target())))
				{
					myChar.tell("Your lack of limbs prevents you from putting on "+affect.target().name()+".");
					return false;
				}
				break;
			case Affect.TYP_DRINK:
				if(Util.bset(forbiddenWornBits,Item.ON_HANDS))
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
		&&(Util.bset(forbiddenWornBits,Item.ON_ARMS)))
		{
			affect.source().tell("You cannot give anything to the "+myChar.name()+".");
			return false;
		}
		return super.okAffect(myHost,affect);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-YOUPOSS> limbs have been restored.");
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
						mob.tell(mob,target,null,"You can't do that to <T-NAMESELF>.");
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

	public void setMiscText(String text)
	{
		super.setMiscText(text);
		missingLimbs=null;
	}
	
	public static Vector racialLimbNameSet(Race R)
	{
		Vector V=new Vector();
		int[] limbs=R.bodyMask();
		for(int i=0;i<limbs.length;i++)
		{
			if(validamputees[i])
				if(limbs[i]==1)
					V.addElement(Race.BODYPARTSTR[i].toLowerCase());
				else
				if(limbs[i]==2)
				{
					V.addElement("left "+Race.BODYPARTSTR[i].toLowerCase());
					V.addElement("right "+Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				for(int ii=0;ii<limbs[i];ii++)
					V.addElement(Race.BODYPARTSTR[i].toLowerCase());
		}
		return V;
	}
	
	public Vector missingLimbNameSet()
	{
		if(missingLimbs!=null) return missingLimbs;
		forbiddenWornBits=-1;
		missingLimbs=new Vector();
		if(affected==null) return missingLimbs;
		if((!(affected instanceof MOB))&&(!(affected instanceof DeadBody)))
		   return missingLimbs;
		missingLimbs=Util.parseSemicolons(text());
		forbiddenWornBits=0;
		if(missingLimbs.contains("left eye")&&missingLimbs.contains("right eye"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_EYES;
		if(missingLimbs.contains("left ear")&&missingLimbs.contains("right ear"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_EARS;
		if(missingLimbs.contains("left arm")&&missingLimbs.contains("right arm"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_ARMS;
		if(missingLimbs.contains("left arm"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEFT_WRIST;
		if(missingLimbs.contains("right arm"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_RIGHT_WRIST;
		if(missingLimbs.contains("left hand")&&missingLimbs.contains("right hand"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_HANDS;
		if(missingLimbs.contains("left hand"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEFT_FINGER;
		if(missingLimbs.contains("right hand"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_RIGHT_FINGER;
		if(missingLimbs.contains("left leg")&&missingLimbs.contains("right leg"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_LEGS;
		if(missingLimbs.contains("left foot")&&missingLimbs.contains("right foot"))
			forbiddenWornBits=forbiddenWornBits|Item.ON_FEET;
		return missingLimbs;
	}

	public Vector remainingLimbNameSet(Race R)
	{
		Vector RV=racialLimbNameSet(R);
		Vector MV=missingLimbNameSet();
		for(int m=0;m<MV.size();m++)
		{
			String S=(String)MV.elementAt(m);
			if(RV.contains(S)) RV.removeElement(S);
		}
		return RV;
	}
	
	public static int getRacialCode(String name)
	{
		name=name.toUpperCase();
		for(int r=0;r<Race.BODYPARTSTR.length;r++)
			if(name.endsWith(Race.BODYPARTSTR[r]))
				return r;
		return -1;
	}
	
	public Vector affectedLimbNameSet(Race R, String missing, Vector missingLimbs)
	{
		Vector AL=new Vector();
		int x=getRacialCode(missing);
		if(x>=0)
		{
			int[] aff=extraamuputees[x];
			if((aff.length>1)||(aff[0]>=0))
			for(int a=0;a<aff.length;a++)
			if(R.bodyMask()[aff[a]]>0)
			{
				String r=Race.BODYPARTSTR[aff[a]].toLowerCase();
				if(missing.startsWith("left "))
				   r="left "+r;
				else
				if(missing.startsWith("right "))
				   r="right "+r;
				if(!missingLimbs.contains(r))
					AL.addElement(r);
			}
		}
		return AL;
	}
	
	public static void amputate(Environmental target, Amputation A, String gone)
	{
		if(A==null) return;
		if(target!=null)
		{
			if(target instanceof MOB)
			{
				if(gone.toLowerCase().endsWith("eye"))
					((MOB)target).location().show(((MOB)target),null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" is destroyed!^?");
				else
					((MOB)target).location().show(((MOB)target),null,Affect.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" falls off!^?");
			}
			else
			if((target instanceof DeadBody)
			&&(((Item)target).owner()!=null)
			&&(((Item)target).owner() instanceof Room))
			{
				if(gone.toLowerCase().endsWith("eye"))
					((Room)((Item)target).owner()).showHappens(Affect.MSG_OK_VISUAL,"^G"+target.name()+"'s "+gone+" is destroyed!^?");
				else
					((Room)((Item)target).owner()).showHappens(Affect.MSG_OK_VISUAL,"^G"+target.name()+"'s "+gone+" falls off!^?");
			}
		}
		Item limb=CMClass.getItem("GenItem");
		limb.setName("a "+gone);
		limb.setDisplayText("a bloody "+gone+" is sitting here.");
		limb.setSecretIdentity(target.name()+"`s bloody "+gone+".");
		limb.setMaterial(EnvResource.RESOURCE_MEAT);
		limb.baseEnvStats().setLevel(1);
		limb.baseEnvStats().setWeight(5);
		limb.recoverEnvStats();
		Race R=null;
		if(target!=null)
		{
			if(target instanceof MOB)
			{
				((MOB)target).location().addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
				R=((MOB)target).charStats().getMyRace();
			}
			else
			if((target instanceof DeadBody)
			&&(((Item)target).owner()!=null)
			&&(((Item)target).owner() instanceof Room))
			{
				((Room)((Item)target).owner()).addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
				R=((DeadBody)target).charStats().getMyRace();
			}
		}
		Vector theRest=A.affectedLimbNameSet(R,gone,A.missingLimbNameSet());
		if(!theRest.contains(gone)) theRest.addElement(gone);
		for(int i=0;i<theRest.size();i++)
			A.setMiscText(A.text()+((String)theRest.elementAt(i))+";");
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

			Vector VN=A.remainingLimbNameSet(target.charStats().getMyRace());
			if(VN.size()==0)
			{
				if(!auto)
					mob.tell("There is nothing left on "+target.name()+" to amputate!");
				return false;
			}
			String gone=(String)VN.elementAt(Dice.roll(1,VN.size(),-1));

			String str=auto?"":"^F<S-NAME> amputate <T-NAMESELF>'s "+gone+"!^?";
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_DELICATE_HANDS_ACT|(auto?Affect.MASK_GENERAL:0),str);
			if(target.location().okAffect(target,msg))
			{
			    target.location().send(target,msg);
				if(!msg.wasModified())
				{
					amputate(target,A,gone);
					if(newOne==true)
					{
						target.addAbility(A);
						A.autoInvocation(target);
					}
					else
					{
						Ability A2=target.fetchAbility(A.ID());
						if(A2!=null) A2.setMiscText(A.text());
					}
					target.confirmWearability();
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
