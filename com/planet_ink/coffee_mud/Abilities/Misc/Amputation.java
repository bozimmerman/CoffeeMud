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
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	private Vector missingLimbs=null;
	private int[] amputations=new int[Race.BODY_PARTS];

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
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		missingLimbNameSet();
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((amputations[Race.BODY_LEG]<0)&&(mob.getWearPositions(Item.ON_LEGS)==0))
			{
				if((amputations[Race.BODY_ARM]<0)&&(mob.getWearPositions(Item.ON_ARMS)==0))
					affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
				affectableStats.setDisposition(affectableStats.disposition()|affectableStats.IS_SITTING);
			}
			if((amputations[Race.BODY_EYE]<0)&&(mob.getWearPositions(Item.ON_EYES)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
			if((amputations[Race.BODY_EAR]<0)&&(mob.getWearPositions(Item.ON_EARS)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		missingLimbNameSet();
		if((amputations[Race.BODY_LEG]<0)&&(affected.getWearPositions(Item.ON_LEGS)==0))
			affectableState.setMovement(affectableState.getMovement()/8);
		else
		if((amputations[Race.BODY_FOOT]<0)&&(affected.getWearPositions(Item.ON_FEET)==0))
			affectableState.setMovement(affectableState.getMovement()/4);
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		missingLimbNameSet();
		for(int i=0;i<amputations.length;i++)
			if(amputations[i]!=0)
				affectableStats.alterBodypart(i,amputations[i]);
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
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> limbs have been restored.");
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

	public Vector missingLimbNameSet()
	{
		if(missingLimbs!=null) return missingLimbs;
		missingLimbs=new Vector();
		if(affected==null) return missingLimbs;
		if((!(affected instanceof MOB))&&(!(affected instanceof DeadBody)))
		   return missingLimbs;
		missingLimbs=Util.parseSemicolons(text(),true);
		amputations=new int[Race.BODY_PARTS];
		for(int v=0;v<missingLimbs.size();v++)
		{
			String s=(String)missingLimbs.elementAt(v);
			if(s.startsWith("left ")) s=s.substring(5).trim();
			if(s.startsWith("right ")) s=s.substring(6).trim();
			int code=-1;
			for(int r=0;r<Race.BODYPARTSTR.length;r++)
				if(Race.BODYPARTSTR[r].equalsIgnoreCase(s))
				{ code=r; break;}
			if(code>=0)	amputations[code]--;
		}
		return missingLimbs;
	}

	public Vector remainingLimbNameSet(MOB M)
	{
		missingLimbNameSet();
		Vector V=new Vector();
		int[] limbs=new int[Race.BODY_PARTS];
		for(int i=0;i<limbs.length;i++)
		{
			limbs[i]=M.charStats().getBodyPart(i);
			if((limbs[i]>0)
			&&(validamputees[i]))
			{
				if(limbs[i]-amputations[i]==1)
				{
					if(!missingLimbNameSet().contains(Race.BODYPARTSTR[i].toLowerCase()))
						V.addElement(Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				if(limbs[i]-amputations[i]==2)
				{
					if(!missingLimbNameSet().contains("left "+Race.BODYPARTSTR[i].toLowerCase()))
						V.addElement("left "+Race.BODYPARTSTR[i].toLowerCase());
					if(!missingLimbNameSet().contains("right "+Race.BODYPARTSTR[i].toLowerCase()))
						V.addElement("right "+Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				for(int ii=0;ii<limbs[i];ii++)
					V.addElement(Race.BODYPARTSTR[i].toLowerCase());
			}
		}
		return V;
	}

// ****************************************************************************
// False Realities 4.2.4
// Addition by Tulath, 4/10/04.
// Reason:  Easy single limb amputation removal
// ****************************************************************************
	public static void unamputate(Environmental target, Amputation A, String gone) 
	{
		if (target != null) 
		{
			if (target instanceof MOB) {
			    ((MOB)target).location().show(((MOB)target), null, CMMsg.MSG_OK_VISUAL, "^G<S-YOUPOSS> " + gone + " miraculously regrows!!^?");
			}
			else
			if ((target instanceof DeadBody)
			    && (((Item)target).owner() != null)
			    && (((Item)target).owner() instanceof Room)) {
			    ((Room)((Item)target).owner()).showHappens(CMMsg.MSG_OK_VISUAL, "^G" + target.name() + "'s " + gone + " miraculously regrows!!^?");
			}
		}

		if (A == null)return;
		Vector theRest = A.missingLimbNameSet();
		if (theRest.contains(gone))theRest.remove(gone);
		A.setMiscText("");
		for (int i = 0; i < theRest.size(); i++)
		    A.setMiscText(A.text() + ((String)theRest.elementAt(i)) + ";");
	}
	
	public static int getRacialCode(String name)
	{
		name=name.toUpperCase();
		for(int r=0;r<Race.BODYPARTSTR.length;r++)
			if(name.endsWith(Race.BODYPARTSTR[r]))
				return r;
		return -1;
	}

	public Vector affectedLimbNameSet(Object O, String missing, Vector missingLimbs)
	{
		Vector AL=new Vector();
		int x=getRacialCode(missing);
		if(x>=0)
		{
			int[] aff=extraamuputees[x];
			if((aff.length>1)||(aff[0]>=0))
			for(int a=0;a<aff.length;a++)
			if(((O instanceof MOB)&&(((MOB)O).charStats().getBodyPart(aff[a])>0))
			||((O instanceof Race)&&(((Race)O).bodyMask()[aff[a]]>0)))
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
		Race R=null;
		if(target!=null)
		{
			if(target instanceof MOB)
			{
				R=((MOB)target).charStats().getMyRace();
				if(gone.toLowerCase().endsWith("eye"))
					((MOB)target).location().show(((MOB)target),null,CMMsg.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" is destroyed!^?");
				else
					((MOB)target).location().show(((MOB)target),null,CMMsg.MSG_OK_VISUAL,"^G<S-YOUPOSS> "+gone+" falls off!^?");
			}
			else
			if((target instanceof DeadBody)
			&&(((Item)target).owner()!=null)
			&&(((Item)target).owner() instanceof Room))
			{
				R=((DeadBody)target).charStats().getMyRace();
				if(gone.toLowerCase().endsWith("eye"))
					((Room)((Item)target).owner()).showHappens(CMMsg.MSG_OK_VISUAL,"^G"+target.name()+"'s "+gone+" is destroyed!^?");
				else
					((Room)((Item)target).owner()).showHappens(CMMsg.MSG_OK_VISUAL,"^G"+target.name()+"'s "+gone+" falls off!^?");
			}
		}
		Item limb=CMClass.getItem("GenItem");
		limb.setName("a "+gone);
		limb.setDisplayText("a bloody "+gone+" is sitting here.");
		limb.setSecretIdentity(target.name()+"`s bloody "+gone+".");
		int material=EnvResource.RESOURCE_MEAT;
		if((R!=null)&&(R.myResources()!=null)&&(R.myResources().size()>0))
			for(int r=0;r<R.myResources().size();r++)
			{
				Item I=(Item)R.myResources().elementAt(r);
				int mat=I.material()&EnvResource.MATERIAL_MASK;
				if(((mat==EnvResource.MATERIAL_FLESH))
				||(r==R.myResources().size()-1))
				{
					material=I.material();
					break;
				}
			}
		limb.setMaterial(material);
		limb.baseEnvStats().setLevel(1);
		limb.baseEnvStats().setWeight(5);
		limb.recoverEnvStats();
		if(target!=null)
		{
			if(target instanceof MOB)
				((MOB)target).location().addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
			else
			if((target instanceof DeadBody)
			&&(((Item)target).owner()!=null)
			&&(((Item)target).owner() instanceof Room))
				((Room)((Item)target).owner()).addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
		}
		Vector theRest=A.affectedLimbNameSet(target,gone,A.missingLimbNameSet());
		if(!theRest.contains(gone)) theRest.addElement(gone);
		for(int i=0;i<theRest.size();i++)
			A.setMiscText(A.text()+((String)theRest.elementAt(i))+";");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&(((String)commands.firstElement()).equals(givenTarget.name())))
				commands.removeElementAt(0);
			choice=Util.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=(String)commands.lastElement();
			commands.removeElementAt(commands.size()-1);
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			Amputation A=(Amputation)target.fetchEffect(ID());
			boolean newOne=false;
			if(A==null){
				A=new Amputation();
				newOne=true;
			}

			Vector VN=A.remainingLimbNameSet(target);
			if(VN.size()==0)
			{
				if(!auto)
					mob.tell("There is nothing left on "+target.name()+" to amputate!");
				return false;
			}
			String gone=null;
			if(choice.length()>0)
			{
				for(int i=0;i<VN.size();i++)
					if(EnglishParser.containsString((String)VN.elementAt(i),choice))
					{ gone=(String)VN.elementAt(i); break;}
				if(gone==null)
				{
					if(!auto)
						mob.tell("There is nothing left on "+target.name()+" called '"+gone+"'!");
					return false;
				}
			}
				
			if(gone==null)
				gone=(String)VN.elementAt(Dice.roll(1,VN.size(),-1));

			String str=auto?"":"^F<S-NAME> amputate <T-NAMESELF>'s "+gone+"!^?";
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),str);
			if(target.location().okMessage(target,msg))
			{
			    target.location().send(target,msg);
				if(msg.value()<=0)
				{
					amputate(target,A,gone);
					if(newOne==true)
						target.addNonUninvokableEffect(A);
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
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
