package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Amputation extends StdAbility implements LimbDamage, HealthCondition
{
	@Override
	public String ID()
	{
		return "Amputation";
	}

	private final static String	localizedName	= CMLib.lang().L("Amputation");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		if(affectedLimbNameSet().size()==0)
			return "";
		return "(Missing your "+CMLib.english().toEnglishStringList(affectedLimbNameSet())+")";
	}
	
	@Override
	public String getHealthConditionDesc()
	{
		return "Missing "+CMLib.english().toEnglishStringList(affectedLimbNameSet());
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "AMPUTATE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANATOMY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected List<String>			missingLimbs		= null;
	private int[]					amputations			= new int[Race.BODY_PARTS];
	private long					badWearLocations	= 0;
	
	private static final long[]		LEFT_LOCS			= { Wearable.WORN_LEFT_FINGER, Wearable.WORN_LEFT_WRIST };
	private static final long[]		RIGHT_LOCS			= { Wearable.WORN_RIGHT_FINGER, Wearable.WORN_RIGHT_WRIST };

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

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			final MOB M = (MOB)affected;
			if((msg.target()==M)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(M,msg.source())))
			{
				final String s=CMLib.utensils().niceCommaList(affectedLimbNameSet(),true);
				if(s.length()>0)
				{
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
												  CMMsg.MSG_OK_VISUAL,L("\n\r@x1 is missing @x2 @x3.\n\r",M.name(msg.source()),M.charStats().hisher(),s),
												  CMMsg.NO_EFFECT,null,
												  CMMsg.NO_EFFECT,null));
				}
			}
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.amISource(M)))
			{
				M.delEffect(this);
				M.recoverCharStats();
				M.recoverPhyStats();
				M.recoverMaxState();
			}
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectedLimbNameSet();
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((amputations[Race.BODY_LEG]<0)&&(mob.getWearPositions(Wearable.WORN_LEGS)==0))
			{
				if((amputations[Race.BODY_ARM]<0)&&(mob.getWearPositions(Wearable.WORN_ARMS)==0))
					affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
			}
			if((amputations[Race.BODY_EYE]<0)&&(mob.getWearPositions(Wearable.WORN_EYES)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SEE);
			if((amputations[Race.BODY_EAR]<0)&&(mob.getWearPositions(Wearable.WORN_EARS)==0))
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_HEAR);
		}
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectedLimbNameSet();
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
	
	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectedLimbNameSet();
		for(int i=0;i<amputations.length;i++)
		{
			if(amputations[i]!=0)
				affectableStats.alterBodypart(i,amputations[i]);
		}
		affectableStats.setWearableRestrictionsBitmap(badWearLocations);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		if((mob.isMonster())&&(mob.amDead())&&(!canBeUninvoked()))
			super.canBeUninvoked=true;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> limbs have been restored."));
		}
	}

	public MOB getTarget(MOB mob, List<String> commands, Environmental givenTarget, boolean quiet)
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
				final Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,target,null,L("You can't do that to <T-NAMESELF>."));
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardMovingBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell(L("You don't see them here."));
				else
					mob.tell(L("You don't see '@x1' here.",targetName));
			}
			return null;
		}
		return target;
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		missingLimbs=null;
	}

	@Override
	public List<String> affectedLimbNameSet()
	{
		if(missingLimbs!=null)
			return missingLimbs;
		missingLimbs=new Vector<String>();
		if(affected==null)
			return missingLimbs;
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
			final String s=missingLimbs.get(v).toUpperCase();
			left=s.startsWith("LEFT ");
			right=s.startsWith("RIGHT ");
			code=Race.BODYPARTHASH.get(s.substring(right?6:left?5:0).trim());
			if(code!=null)
			{
				amputations[code.intValue()]--;
				final long[] LOCS=left?LEFT_LOCS:right?RIGHT_LOCS:null;
				final long GRID=Race.BODY_WEARGRID[code.intValue()][0];
				if(LOCS!=null)
				for(l1=0;l1<LOCS.length;l1++)
					if((GRID&LOCS[l1])>0)
						badWearLocations|=LOCS[l1];
			}
		}
		return missingLimbs;
	}

	public List<String> completeLimbNameSet(Environmental E)
	{
		final Vector<String> V=new Vector<String>();
		if(!(E instanceof MOB))
			return V;
		final MOB M=(MOB)E;
		final int[] limbs=M.charStats().getMyRace().bodyMask();
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

	@Override
	public List<String> unaffectedLimbSet()
	{
		affectedLimbNameSet();
		final List<String> V=new Vector<String>();
		if(!(affected instanceof MOB))
			return V;
		final MOB M=(MOB)affected;
		final int[] limbs=new int[Race.BODY_PARTS];
		final List<String> affectedList=affectedLimbNameSet();
		for(int i=0;i<limbs.length;i++)
		{
			limbs[i]=M.charStats().getBodyPart(i);
			if((limbs[i]>0)
			&&(validamputees[i]))
			{
				if(limbs[i]-amputations[i]==1)
				{
					if(!affectedList.contains(Race.BODYPARTSTR[i].toLowerCase()))
						V.add(Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				if(limbs[i]-amputations[i]==2)
				{
					if(!affectedList.contains("left "+Race.BODYPARTSTR[i].toLowerCase()))
						V.add("left "+Race.BODYPARTSTR[i].toLowerCase());
					if(!affectedList.contains("right "+Race.BODYPARTSTR[i].toLowerCase()))
						V.add("right "+Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				{
					int num=0;
					for(final String affectedLimb : affectedList)
					{
						if(Race.BODYPARTSTR[i].equalsIgnoreCase(affectedLimb))
							num++;
					}
					for(int ii=num;ii<limbs[i];ii++)
						V.add(Race.BODYPARTSTR[i].toLowerCase());
				}
			}
		}
		return V;
	}

// ****************************************************************************
// False Realities
// Addition by Tulath, 4/10/04.
// Reason:  Easy single limb amputation removal
// ****************************************************************************
	@Override
	public void restoreLimb(String gone)
	{
		if (affected != null)
		{
			if (affected instanceof MOB)
			{
				((MOB)affected).location().show(((MOB)affected), null, CMMsg.MSG_OK_VISUAL, L("^G<S-YOUPOSS> @x1 miraculously regrows!!^?",gone));
			}
			else
			if ((affected instanceof DeadBody)
			&& (((Item)affected).owner() instanceof Room))
			{
				((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_VISUAL, L("^G@x1's @x2 miraculously regrows!!^?",affected.name(),gone));
			}
		}

		final List<String> theRest = affectedLimbNameSet();
		if (theRest.contains(gone))
			theRest.remove(gone);
		if((theRest.size()==0)&&(affected!=null))
			affected.delEffect(this);
		else
		{
			setMiscText(CMParms.combineWith(theRest,';'));
		}
	}

	public static int getRacialCode(String name)
	{
		final Integer code = Race.BODYPARTHASH_RL_LOWER.get(name.toLowerCase().trim());
		if(code != null)
			return code.intValue();
		return -1;
	}

	@Override
	public Item damageLimb(String gone)
	{
		Race R=null;
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				final MOB M=(MOB)affected;
				if(M.amDead())
					return null;
				R=M.charStats().getMyRace();
				boolean success;
				if(M.location()!=null)
				{
					if(gone.toLowerCase().endsWith("eye"))
						success=M.location().show(M,this,CMMsg.MSG_OK_VISUAL,L("^G<S-YOUPOSS> @x1 is destroyed!^?",gone));
					else
						success=M.location().show(M,this,CMMsg.MSG_OK_VISUAL,L("^G<S-YOUPOSS> @x1 falls off!^?",gone));
				}
				else
					success=false;
				if(!success)
					return null;
			}
			else
			if((affected instanceof DeadBody)
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof Room))
			{
				final DeadBody D=(DeadBody)affected;
				R=D.charStats().getMyRace();
				if(gone.toLowerCase().endsWith("eye"))
					((Room)D.owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("^G@x1's @x2 is destroyed!^?",D.name(),gone));
				else
					((Room)D.owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("^G@x1's @x2 falls off!^?",D.name(),gone));
			}
		}
		Item limb=null;
		boolean isFakeLimb=false;
		if(affected instanceof MOB)
		{
			final MOB tmob=(MOB)affected;
			limb=findFakeLimb(tmob,gone);
			if(limb!=null)
			{
				limb.unWear();
				limb.removeFromOwnerContainer();
				isFakeLimb=true;
				tmob.recoverCharStats();
				tmob.recoverPhyStats();
				tmob.recoverMaxState();
			}
		}
		if(!isFakeLimb)
		{
			limb=CMClass.getItem("GenLimb");
			limb.setName(L("a @x1",gone));
			limb.basePhyStats().setAbility(1);
			limb.setDisplayText(L("a bloody @x1 is sitting here.",gone));
			if(affected != null)
				limb.setSecretIdentity(affected.name()+"`s bloody "+gone+".");
			int material=RawMaterial.RESOURCE_MEAT;
			if((R!=null)&&(R.myResources()!=null)&&(R.myResources().size()>0))
			{
				for(int r=0;r<R.myResources().size();r++)
				{
					final Item I=R.myResources().get(r);
					final int mat=I.material()&RawMaterial.MATERIAL_MASK;
					if(((mat==RawMaterial.MATERIAL_FLESH))
					||(r==R.myResources().size()-1))
					{
						material=I.material();
						break;
					}
				}
			}
			limb.setMaterial(material);
			limb.basePhyStats().setLevel(1);
			limb.basePhyStats().setWeight(5);
			limb.recoverPhyStats();
		}

		if((affected instanceof MOB)&&(((MOB)affected).location()!=null))
			((MOB)affected).location().addItem(limb,ItemPossessor.Expire.Monster_EQ);
		else
		if((affected instanceof DeadBody)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room))
			((Room)((Item)affected).owner()).addItem(limb,ItemPossessor.Expire.Monster_EQ);

		if(!isFakeLimb)
		{
			final List<String> theRest=new Vector<String>();
			final int x=getRacialCode(gone);
			if(x>=0)
			{
				final int[] aff=extraamuputees[x];
				if((aff.length>1)||(aff[0]>=0))
				{
					for (final int element : aff)
					{
						if(((affected instanceof MOB)
							&&(((MOB)affected).charStats().getBodyPart(element)>0))
						||((affected instanceof DeadBody)
							&&(((DeadBody)affected).getSavedMOB() != null)
							&&(((DeadBody)affected).getSavedMOB().charStats().getBodyPart(element)>0)))
						{
							String r=Race.BODYPARTSTR[element].toLowerCase();
							if(gone.startsWith("left "))
								r="left "+r;
							else
							if(gone.startsWith("right "))
								r="right "+r;
							if(missingLimbs == null)
								this.affectedLimbNameSet();
							if(!missingLimbs.contains(r))
								theRest.add(r);
						}
					}
				}
			}
			if(!theRest.contains(gone))
				theRest.add(gone);
			if(missingLimbs != null)
				theRest.addAll(missingLimbs);
			setMiscText(CMParms.combineWith(theRest, ';'));
		}

		if(affected != null)
		{
			for(Enumeration<Ability> a=affected.effects();a.hasMoreElements();)
			{
				final Ability oA=a.nextElement();
				if((oA instanceof LimbDamage)&&(!oA.ID().equals(ID())))
				{
					if(((LimbDamage)oA).affectedLimbNameSet().contains(gone))
						((LimbDamage)oA).restoreLimb(gone);
				}
			}
		}
		
		if((affected instanceof MOB)
		&&(CMLib.dice().roll(1,100,0)<=CMProps.getIntVar(CMProps.Int.INJBLEEDPCTCHANCE)))
		{
			final Ability A2=CMClass.getAbility("Bleeding");
			if(A2!=null)
				A2.invoke(((MOB)affected),((MOB)affected),true,0);
		}
		return limb;
	}

	private Item findFakeLimb(MOB tmob, String named)
	{
		if(named.length()>0)
		{
			named=named.toUpperCase();
			if(named.startsWith("RIGHT "))
				named=named.substring(6).trim();
			else
			if(named.startsWith("LEFT "))
				named=named.substring(5).trim();
			for(int i=0;i<tmob.numItems();i++)
			{
				final Item I=tmob.getItem(i);
				if((I!=null)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(I instanceof FalseLimb)
				&&((I.name().toUpperCase().endsWith(named))
					||(I.rawSecretIdentity().toUpperCase().endsWith(named))))
					return I;
			}
		}
		return null;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&((commands.get(0)).equals(givenTarget.name())))
				commands.remove(0);
			choice=CMParms.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=CMParms.combine(commands,1);
			while(commands.size()>1)
				commands.remove(1);
		}
		//if(choice.toUpperCase().startsWith("RIGHT "))
		//	choice=choice.substring(6).trim();
		//else
		//if(choice.toUpperCase().startsWith("LEFT "))
		//	choice=choice.substring(5).trim();
		final MOB target=super.getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;
		if(!auto)
		{
			LegalBehavior B=null;
			if(mob.location()!=null)
				B=CMLib.law().getLegalBehavior(mob.location());
			List<LegalWarrant> warrants=new Vector<LegalWarrant>();
			if(B!=null)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
			if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
			{
				mob.tell(L("You are not authorized by law to amputate from @x1 at this time.",target.Name()));
				return false;
			}
			final Item w=mob.fetchWieldedItem();
			if(!CMSecurity.isASysOp(mob))
			{
				Weapon ww=null;
				if((w==null)||(!(w instanceof Weapon)))
				{
					mob.tell(L("You cannot amputate without a weapon!"));
					return false;
				}
				ww=(Weapon)w;
				if((ww.weaponDamageType()!=Weapon.TYPE_PIERCING)&&(ww.weaponDamageType()!=Weapon.TYPE_SLASHING))
				{
					mob.tell(L("You cannot amputate with a @x1!",ww.name()));
					return false;
				}
				if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				{
					mob.tell(L("You are too far away to try that!"));
					return false;
				}
				if((!CMLib.flags().isBoundOrHeld(target))||(!CMLib.flags().isSleeping(target)))
				{
					mob.tell(L("@x1 must be bound, and asleep on an operating bed before you can amputate.",target.charStats().HeShe()));
					return false;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			Amputation ampuA=(Amputation)target.fetchEffect(ID());
			boolean newOne=false;
			if(ampuA==null)
			{
				ampuA=new Amputation();
				ampuA.setAffectedOne(target);
				newOne=true;
			}

			Item fakeLimb=null;
			String gone=null;
			if(choice.length()>0)
			{
				fakeLimb=findFakeLimb(target,choice);
				if(fakeLimb != null)
				{
					final List<String> VN=completeLimbNameSet(target);
					for(int i=0;i<VN.size();i++)
					{
						if(CMLib.english().containsString(VN.get(i),choice))
						{
							gone=VN.get(i);
							break;
						}
					}
					if(gone==null)
						fakeLimb=null;
				}
			}

			final List<String> VN=ampuA.unaffectedLimbSet();
			if((VN.size()==0)&&(fakeLimb==null))
			{
				if(!auto)
					mob.tell(L("There is nothing left on @x1 to amputate!",target.name(mob)));
				return false;
			}
			if((choice.length()>0)&&(fakeLimb==null)&&(gone==null))
			{
				for(int i=0;i<VN.size();i++)
				{
					if(CMLib.english().containsString(VN.get(i),choice))
					{
						gone=VN.get(i);
						break;
					}
				}
				if(gone==null)
				{
					if(!auto)
						mob.tell(L("There is nothing left on @x1 called '@x2'!",target.name(mob),choice));
					return false;
				}
			}

			if(gone==null)
			{
				final List<String> completeSet = completeLimbNameSet(target);
				for(int v=0;v<completeSet.size();v++)
				{
					if((!VN.contains(completeSet.get(v)))
					&&(findFakeLimb(target,completeSet.get(v))!=null))
						VN.add(completeSet.get(v));
				}
				gone=VN.get(CMLib.dice().roll(1,VN.size(),-1));
				fakeLimb=findFakeLimb(target,gone);
			}
			final String goneName = (fakeLimb!=null)?fakeLimb.name():gone;

			final String str=auto?"":L("^F^<FIGHT^><S-NAME> amputate(s) <T-YOUPOSS> @x1!^</FIGHT^>^?",goneName);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),str);
			CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(target,msg))
			{
				final MOB vic=target.getVictim();
				final MOB vic2=mob.getVictim();
				target.location().send(target,msg);
				if(msg.value()<=0)
				{
					if(ampuA.damageLimb(gone)!=null)
					{
						if(newOne==true)
							target.addNonUninvokableEffect(ampuA);
						target.recoverCharStats();
						target.recoverPhyStats();
						target.recoverMaxState();
						target.location().recoverRoomStats();
						CMLib.utensils().confirmWearability(target);
						target.setVictim(vic);
						mob.setVictim(vic2);
					}
					else
						success=false;
				}
			}
			else
				success=false;
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to amputate <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
