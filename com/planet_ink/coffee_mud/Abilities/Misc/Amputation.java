package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Amputation extends StdAbility implements Amputator
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
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"AMPUTATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected Vector missingLimbs=null;
	private int[] amputations=new int[Race.BODY_PARTS];
	private long badWearLocations=0;
	private static final long[] LEFT_LOCS={Wearable.WORN_LEFT_FINGER,Wearable.WORN_LEFT_WRIST};
	private static final long[] RIGHT_LOCS={Wearable.WORN_RIGHT_FINGER,Wearable.WORN_RIGHT_WRIST};
	
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

	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			MOB M = (MOB)affected;
			if((msg.target()==M)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(M,msg.source())))
			{
				String s=CMLib.utensils().niceCommaList(missingLimbNameSet(),true);
				if(s.length()>0)
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
												  CMMsg.MSG_OK_VISUAL,"\n\r"+M.name()+" is missing "+M.charStats().hisher()+" "+s+".\n\r",
												  CMMsg.NO_EFFECT,null,
												  CMMsg.NO_EFFECT,null));
			}
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.amISource(M)))
			{
				M.delEffect(this);
				M.recoverCharStats();
				M.recoverEnvStats();
				M.recoverMaxState();
			}
		}
		super.executeMsg(host,msg);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		missingLimbNameSet();
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((amputations[Race.BODY_LEG]<0)&&(mob.getWearPositions(Wearable.WORN_LEGS)==0))
			{
				if((amputations[Race.BODY_ARM]<0)&&(mob.getWearPositions(Wearable.WORN_ARMS)==0))
					affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_MOVE);
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SITTING);
			}
			if((amputations[Race.BODY_EYE]<0)&&(mob.getWearPositions(Wearable.WORN_EYES)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
			if((amputations[Race.BODY_EAR]<0)&&(mob.getWearPositions(Wearable.WORN_EARS)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		missingLimbNameSet();
		if(amputations[Race.BODY_LEG]<0)
        {
            if(affected.getWearPositions(Wearable.WORN_LEGS)==0)
    			affectableState.setMovement(affectableState.getMovement()/8);
            else
                affectableState.setMovement(affectableState.getMovement()/4);
        }
        else
		if(amputations[Race.BODY_FOOT]<0)
        {
            if(affected.getWearPositions(Wearable.WORN_FEET)==0)
    			affectableState.setMovement(affectableState.getMovement()/4);
            else
                affectableState.setMovement(affectableState.getMovement()/2);
        }
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		missingLimbNameSet();
		for(int i=0;i<amputations.length;i++)
			if(amputations[i]!=0)
				affectableStats.alterBodypart(i,amputations[i]);
		affectableStats.setWearableRestrictionsBitmap(badWearLocations);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

        if((mob.isMonster())&&(mob.amDead())&&(!canBeUninvoked()))
            super.canBeUninvoked=true;
        
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> limbs have been restored.");
	}

	public MOB getTarget(MOB mob, Vector commands, Environmental givenTarget, boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
		if((targetName.length()==0)&&(mob.isInCombat())&&(abstractQuality()==Ability.QUALITY_MALICIOUS)&&(mob.getVictim()!=null))
		   target=mob.getVictim();
		else
		if((targetName.length()==0)&&(abstractQuality()!=Ability.QUALITY_MALICIOUS))
			target=mob;
		else
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Wearable.FILTER_UNWORNONLY);
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

		if((target==null)||((!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardBy(target,mob))||(!target.isInCombat()))))
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
		missingLimbs=CMParms.parseSemicolons(text(),true);
		amputations=new int[Race.BODY_PARTS];
		badWearLocations=0;
		boolean right=false;
		boolean left=false;
		Integer code=null;
		int l1=0;
		for(int v=0;v<missingLimbs.size();v++)
		{
			String s=((String)missingLimbs.elementAt(v)).toUpperCase();
			left=s.startsWith("LEFT ");
			right=s.startsWith("RIGHT ");
			code=(Integer)Race.BODYPARTHASH.get(s.substring(right?6:left?5:0).trim());
			if(code!=null)
			{
				amputations[code.intValue()]--;
				long[] LOCS=left?LEFT_LOCS:right?RIGHT_LOCS:null;
				long GRID=Race.BODY_WEARGRID[code.intValue()][0];
				if(LOCS!=null)
				for(l1=0;l1<LOCS.length;l1++)
					if((GRID&LOCS[l1])>0)
						badWearLocations|=LOCS[l1];
			}
		}
		return missingLimbs;
	}

    public Vector completeLimbNameSet(Environmental E)
    {
        Vector V=new Vector();
        if(!(E instanceof MOB)) return V;
        MOB M=(MOB)E;
        int[] limbs=M.charStats().getMyRace().bodyMask();
        for(int i=0;i<limbs.length;i++)
        {
            if((limbs[i]>0)&&(validamputees[i]))
            {
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
        }
        return V;
    }
    
	public Vector remainingLimbNameSet(Environmental E)
	{
		missingLimbNameSet();
		Vector V=new Vector();
		if(!(E instanceof MOB)) return V;
		MOB M=(MOB)E;
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
// False Realities
// Addition by Tulath, 4/10/04.
// Reason:  Easy single limb amputation removal
// ****************************************************************************
	public void unamputate(Environmental target, Amputator A, String gone)
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

	public Item amputate(Environmental target, Amputator A, String gone)
	{
		if(A==null) return null;
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
		Item limb=null;
		boolean isFakeLimb=false;
		if(target instanceof MOB)
		{
		    MOB tmob=(MOB)target;
		    limb=findFakeLimb(tmob,gone);
		    if(limb!=null)
		    {
	            limb.unWear();
	            limb.removeFromOwnerContainer();
	            isFakeLimb=true;
	            tmob.recoverCharStats();
	            tmob.recoverEnvStats();
	            tmob.recoverMaxState();
		    }
		}
		if(!isFakeLimb)
		{
			limb=CMClass.getItem("GenLimb");
			limb.setName("a "+gone);
			limb.baseEnvStats().setAbility(1);
			limb.setDisplayText("a bloody "+gone+" is sitting here.");
			if(target != null)
				limb.setSecretIdentity(target.name()+"`s bloody "+gone+".");
			int material=RawMaterial.RESOURCE_MEAT;
			if((R!=null)&&(R.myResources()!=null)&&(R.myResources().size()>0))
				for(int r=0;r<R.myResources().size();r++)
				{
					Item I=(Item)R.myResources().elementAt(r);
					int mat=I.material()&RawMaterial.MATERIAL_MASK;
					if(((mat==RawMaterial.MATERIAL_FLESH))
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
		}
		
		if((target instanceof MOB)&&(((MOB)target).location()!=null))
			((MOB)target).location().addItemRefuse(limb,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
		else
		if((target instanceof DeadBody)
		&&(((Item)target).owner()!=null)
		&&(((Item)target).owner() instanceof Room))
			((Room)((Item)target).owner()).addItemRefuse(limb,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
        
        if(!isFakeLimb)
        {
    		Vector theRest=A.affectedLimbNameSet(target,gone,A.missingLimbNameSet());
    		if(!theRest.contains(gone)) theRest.addElement(gone);
            for(int i=0;i<theRest.size();i++)
                A.setMiscText(A.text()+((String)theRest.elementAt(i))+";");
        }
		
		Injury I=(target==null)?null:(Injury)target.fetchEffect("Injury");
		if(I!=null)
		{
		    Vector V=null;
			for(int i=0;i<I.injuries.length;i++)
			{
			    V=I.injuries[i];
			    if(V!=null)
			    for(int v=0;v<V.size();v++)
				    if(((String)((Object[])V.elementAt(v))[0]).equalsIgnoreCase(gone))
				    {
				        V.removeElementAt(v);
				        if(V.size()==0) I.injuries[i]=null;
				        break;
				    }
			}
		}
		if((target instanceof MOB)
		&&(CMLib.dice().roll(1,100,0)<=CMProps.getIntVar(CMProps.SYSTEMI_INJBLEEDPCTCHANCE)))
		{
			Ability A2=CMClass.getAbility("Bleeding");
			if(A2!=null) A2.invoke(((MOB)target),((MOB)target),true,0);
		}
		return limb;
	}

	private Item findFakeLimb(MOB tmob, String named) {
        if(named.length()>0)
        {
            named=named.toUpperCase();
            if(named.startsWith("RIGHT "))
                named=named.substring(6).trim();
            else
            if(named.startsWith("LEFT "))
                named=named.substring(5).trim();
            for(int i=0;i<tmob.inventorySize();i++)
            {
                Item I=tmob.fetchInventory(i);
                if((I!=null)
                &&(!I.amWearingAt(Wearable.IN_INVENTORY))
                &&(I.ID().endsWith("Limb"))
                &&((I.name().toUpperCase().endsWith(named))
                    ||(I.rawSecretIdentity().toUpperCase().endsWith(named))))
                    return I;
            }
        }
        return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&(((String)commands.firstElement()).equals(givenTarget.name())))
				commands.removeElementAt(0);
			choice=CMParms.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=CMParms.combine(commands,1);
			while(commands.size()>1)
			    commands.removeElementAt(1);
		}
		if(choice.toUpperCase().startsWith("RIGHT "))
		    choice=choice.substring(6).trim();
		else
		if(choice.toUpperCase().startsWith("LEFT "))
		    choice=choice.substring(5).trim();
		MOB target=super.getTarget(mob,commands,givenTarget,false,true);
		if(target==null) return false;
		if(!auto)
		{
			LegalBehavior B=null;
			if(mob.location()!=null) B=CMLib.law().getLegalBehavior(mob.location());
			Vector warrants=new Vector();
			if(B!=null)
                warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
			if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),"ABOVELAW")))
			{
			    mob.tell("You are not authorized by law to amputate from "+target.Name()+" at this time.");
			    return false;
			}
			Item w=mob.fetchWieldedItem();
			if(!CMSecurity.isASysOp(mob))
			{
				Weapon ww=null;
				if((w==null)||(!(w instanceof Weapon)))
				{
					mob.tell("You cannot amputate without a weapon!");
					return false;
				}
				ww=(Weapon)w;
				if((ww.weaponType()!=Weapon.TYPE_PIERCING)&&(ww.weaponType()!=Weapon.TYPE_SLASHING))
				{
					mob.tell("You cannot amputate with a "+ww.name()+"!");
					return false;
				}
				if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				{
					mob.tell("You are too far away to try that!");
					return false;
				}
				if((!CMLib.flags().isBoundOrHeld(target))||(!CMLib.flags().isSleeping(target)))
				{
					mob.tell(target.charStats().HeShe()+" must be bound, and asleep on an operating bed before you can amputate.");
					return false;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			Amputation A=(Amputation)target.fetchEffect(ID());
			boolean newOne=false;
			if(A==null){
				A=new Amputation();
				newOne=true;
			}

	        Item fakeLimb=null;
            String gone=null;
	        if((target instanceof MOB)&&(choice.length()>0)) 
	        {
	            fakeLimb=findFakeLimb((MOB)target,choice);
	            if(fakeLimb != null)
	            {
	                Vector VN=completeLimbNameSet(target);
	                for(int i=0;i<VN.size();i++)
	                    if(CMLib.english().containsString((String)VN.elementAt(i),choice))
	                    { gone=(String)VN.elementAt(i); break;}
	                if(gone==null)
	                    fakeLimb=null;
	            }
	        }
	        
			Vector VN=A.remainingLimbNameSet(target);
			if((VN.size()==0)&&(fakeLimb==null))
			{
				if(!auto)
					mob.tell("There is nothing left on "+target.name()+" to amputate!");
				return false;
			}
			if((choice.length()>0)&&(fakeLimb==null)&&(gone==null))
			{
				for(int i=0;i<VN.size();i++)
					if(CMLib.english().containsString((String)VN.elementAt(i),choice))
					{ gone=(String)VN.elementAt(i); break;}
				if(gone==null)
				{
					if(!auto)
						mob.tell("There is nothing left on "+target.name()+" called '"+choice+"'!");
					return false;
				}
			}

			if(gone==null)
			{
                if(target instanceof MOB)
                {
    			    Vector completeSet = completeLimbNameSet(target);
    			    for(int v=0;v<completeSet.size();v++)
    			        if((!VN.contains(completeSet.elementAt(v)))
    			        &&(findFakeLimb((MOB)target,(String)completeSet.elementAt(v))!=null))
    			            VN.addElement(completeSet.elementAt(v));
                }
				gone=(String)VN.elementAt(CMLib.dice().roll(1,VN.size(),-1));
                if(target instanceof MOB)
	                fakeLimb=findFakeLimb((MOB)target,gone);
			}
			String goneName = (fakeLimb!=null)?fakeLimb.name():gone;

			String str=auto?"":"^F^<FIGHT^><S-NAME> amputate(s) <T-YOUPOSS> "+goneName+"!^</FIGHT^>^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),str);
            CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(target,msg))
			{
				MOB vic=target.getVictim();
				MOB vic2=mob.getVictim();
			    target.location().send(target,msg);
				if(msg.value()<=0)
				{
					amputate(target,A,gone);
					if(newOne==true)
						target.addNonUninvokableEffect(A);
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
					target.location().recoverRoomStats();
					CMLib.utensils().confirmWearability(target);
					target.setVictim(vic);
					mob.setVictim(vic2);
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
