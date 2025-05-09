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
   Copyright 2014-2025 Bo Zimmerman

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
public class BrokenLimbs extends StdAbility implements LimbDamage, HealthCondition
{
	@Override
	public String ID()
	{
		return "BrokenLimbs";
	}

	private final static String	localizedName	= CMLib.lang().L("Broken Limbs");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected int						HEALING_TICKS	= (int) ((TimeManager.MILI_MINUTE * 10) / CMProps.getTickMillis());
	protected List<String>				brokenLimbsList	= null;
	protected final static int			Race_BODY_FINGER= Race.BODY_PARTS;
	private int[]						brokenParts		= new int[Race.BODY_PARTS+1];
	private final Map<String, int[]>	setBones		= new STreeMap<String, int[]>();

	@Override
	public String displayText()
	{
		final List<String> affectedLimbs = affectedLimbNameSet();
		if(affectedLimbs.size()==0)
			return "";
		final List<String> limbConditionStrs = new XVector<String>(affectedLimbs);
		for(int i=0;i<limbConditionStrs.size();i++)
		{
			final String limb=limbConditionStrs.get(i);
			final int[] condition = setBones.get(limb);
			if(condition != null)
			{
				final double pct = CMath.div(condition[0], HEALING_TICKS) * 100.0;
				limbConditionStrs.set(i, limb + " (healing "+(100-Math.round(pct))+"%)");
			}
		}
		return "(Broken "+CMLib.english().toEnglishStringList(limbConditionStrs)+")";
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Broken "+CMLib.english().toEnglishStringList(affectedLimbNameSet());
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

	private static final String[]	triggerStrings	= I(new String[] { "BREAKLIMB" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
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

	public final static boolean[] validBrokens ={true,//antenna
												 false,//eye
												 false,//ear
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

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
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
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
									  CMMsg.MSG_OK_VISUAL,L("\n\r@x1 has broken @x2 @x3.\n\r",M.name(msg.source()),M.charStats().hisher(),s),
									  CMMsg.NO_EFFECT,null,
									  CMMsg.NO_EFFECT,null));
			}
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.amISource(M)))
			{
				M.delEffect(this);
				M.recoverCharStats();
				M.recoverPhyStats();
				M.recoverMaxState();
			}
			if(msg.amITarget(M)&&(msg.targetMinor()==CMMsg.TYP_HEALING))
			{
				final int amount=msg.value();
				if((amount>0)
				&&(msg.tool() instanceof Ability)
				&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HEALINGMAGIC))
				&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
				{
					startAllLimbsHealing();
					double healAmount = CMath.div(amount,M.maxState().getHitPoints());
					if(healAmount < 0.05)
						healAmount = 0.05;
					progressLimbHealing(M, (int)Math.round(CMath.mul(HEALING_TICKS, healAmount)));
				}
			}
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectedLimbNameSet();
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if((brokenParts[Race.BODY_LEG]<0)&&(mob.getWearPositions(Wearable.WORN_LEGS)==0))
			{
				if((brokenParts[Race.BODY_ARM]<0)&&(mob.getWearPositions(Wearable.WORN_ARMS)==0))
					affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_MOVE);
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SITTING);
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectedLimbNameSet();
		for(int i=0;i<brokenParts.length;i++)
			if((brokenParts[i]!=0)
			&&((i==Race.BODY_ARM)||(i==Race.BODY_LEG)||(i==Race.BODY_FOOT)||(i==Race.BODY_HAND)))
				affectableStats.alterBodypart(i,brokenParts[i]);
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		affectedLimbNameSet();
		if(brokenParts[Race.BODY_LEG]<0)
		{
			if(affected.getWearPositions(Wearable.WORN_LEGS)==0)
				affectableState.setMovement(affectableState.getMovement()/8);
			else
				affectableState.setMovement(affectableState.getMovement()/4);
		}
		else
		if(brokenParts[Race.BODY_FOOT]<0)
		{
			if(affected.getWearPositions(Wearable.WORN_FEET)==0)
				affectableState.setMovement(affectableState.getMovement()/4);
			else
				affectableState.setMovement(affectableState.getMovement()/2);
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if(msg.source() == affected)
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_STAND:
				{
					final MOB mob=msg.source();
					if((brokenParts[Race.BODY_LEG]<0)&&(mob.getWearPositions(Wearable.WORN_LEGS)==0)&&(!CMLib.flags().isSleeping(mob)))
					{
						mob.tell(L("Your legs are broken!"));
						return false;
					}
					break;
				}
			case CMMsg.TYP_ADVANCE:
			case CMMsg.TYP_RETREAT:
			{
				final MOB mob=msg.source();
				if((brokenParts[Race.BODY_LEG]<0)&&(!CMLib.flags().isSleeping(mob)))
				{
					mob.tell(L("Your broken leg made movement too painful this time.  Try again."));
					return false;
				}
				break;
			}
			case CMMsg.TYP_WIELD:
			{
				final MOB mob=msg.source();
				if((brokenParts[Race.BODY_HAND]<0)&&(mob.getWearPositions(Wearable.WORN_WIELD)==0))
				{
					mob.tell(L("Your weapon hand is broken!"));
					return false;
				}
				break;
			}
			case CMMsg.TYP_WEAPONATTACK:
			{
				final MOB mob=msg.source();
				if(((brokenParts[Race.BODY_HAND]<0)||(brokenParts[Race.BODY_ARM]<0))
				&&(msg.tool() == mob.fetchWieldedItem()))
				{
					mob.tell(L("Your broken limbs make the attack impossible!"));
					return false;
				}
				break;
			}
			case CMMsg.TYP_WEAR:
				if(((brokenParts[Race.BODY_HAND]<0)||(brokenParts[Race_BODY_FINGER]<0))
				&&(msg.target() instanceof Item)
				&&((((Item)msg.target()).rawProperLocationBitmap()&(Wearable.WORN_LEFT_FINGER|Wearable.WORN_RIGHT_FINGER))>0))
				{
					final MOB mob=msg.source();
					mob.tell(L("Your broken fingers make putting that on impossible!"));
					return false;
				}
				break;
			case CMMsg.TYP_REMOVE:
				if(((brokenParts[Race.BODY_HAND]<0)||(brokenParts[Race_BODY_FINGER]<0))
				&&(msg.target() instanceof Item)
				&&((((Item)msg.target()).rawProperLocationBitmap()&(Wearable.WORN_LEFT_FINGER|Wearable.WORN_RIGHT_FINGER))>0))
				{
					final MOB mob=msg.source();
					mob.tell(L("Your broken fingers make removing that on impossible!"));
					return false;
				}
				break;
			default:
				break;
			}
		}
		return true;
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
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> broken limbs have been healed."));
	}

	public MOB getTarget(final MOB mob, final List<String> commands, final Environmental givenTarget, final boolean quiet)
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

	protected void startAllLimbsHealing()
	{
		for(final String limb : affectedLimbNameSet())
			if(!setBones.containsKey(limb))
				setBones.put(limb, new int[]{HEALING_TICKS});
	}

	@Override
	public void setMiscText(final String text)
	{
		if(text.toUpperCase().startsWith("+"))
		{
			if(text.toUpperCase().startsWith("+SETBONES"))
			{
				startAllLimbsHealing();
			}
		}
		else
		{
			super.setMiscText(text);
			brokenLimbsList=null;
		}
	}

	@Override
	public List<String> affectedLimbNameSet()
	{
		if(brokenLimbsList!=null)
			return brokenLimbsList;
		brokenLimbsList=new Vector<String>();
		if(affected==null)
			return brokenLimbsList;
		if((!(affected instanceof MOB))&&(!(affected instanceof DeadBody)))
			return brokenLimbsList;
		brokenLimbsList=CMParms.parseSemicolons(text(),true);
		brokenParts=new int[Race.BODY_PARTS+1];
		boolean right=false;
		boolean left=false;
		Integer code=null;
		for(int v=0;v<brokenLimbsList.size();v++)
		{
			final String s=brokenLimbsList.get(v).toUpperCase();
			left=s.startsWith("LEFT ");
			right=s.startsWith("RIGHT ");
			final String name = s.substring(right?6:left?5:0).trim();
			code=Race.BODYPARTHASH.get(name);
			if(code!=null)
				brokenParts[code.intValue()]--;
			else
			if(name.startsWith("FINGER")||name.endsWith(" FINGER")||(name.endsWith(" FINGERS")))
				brokenParts[Race_BODY_FINGER]--;
		}
		return brokenLimbsList;
	}

	protected List<String> completeBrokenLimbNameSet(final Environmental E)
	{
		final List<String> brokenLimbsV=new Vector<String>();
		if(!(E instanceof MOB))
			return brokenLimbsV;
		final MOB M=(MOB)E;
		final int[] limbs=M.charStats().getMyRace().bodyMask();
		for(int i=0;i<limbs.length;i++)
		{
			if((limbs[i]>0)&&(validBrokens[i]))
			{
				if(limbs[i]==1)
					brokenLimbsV.add(Race.BODYPARTSTR[i].toLowerCase());
				else
				if(limbs[i]==2)
				{
					brokenLimbsV.add("left "+Race.BODYPARTSTR[i].toLowerCase());
					brokenLimbsV.add("right "+Race.BODYPARTSTR[i].toLowerCase());
				}
				else
				for(int ii=0;ii<limbs[i];ii++)
					brokenLimbsV.add(Race.BODYPARTSTR[i].toLowerCase());
			}
		}
		return brokenLimbsV;
	}

	protected void progressLimbHealing(final CMObject obj, final int thisMuch)
	{
		for(final String limb : setBones.keySet())
		{
			final int[] ticksRemaining = setBones.get(limb);
			ticksRemaining[0] -= thisMuch;
			if(ticksRemaining[0] <= 0)
			{
				setBones.remove(limb);
				if(obj instanceof MOB)
					restoreLimb(limb);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_MOB)
		{
			progressLimbHealing(ticking, 1);
		}
		return super.tick(ticking, tickID);
	}

	@Override
	public List<String> unaffectedLimbSet()
	{
		affectedLimbNameSet();
		final List<String> remains=new Vector<String>();
		if(!(affected instanceof MOB))
			return remains;
		final MOB M=(MOB)affected;
		final int[] limbs=new int[Race.BODY_PARTS];
		final List<String> affected=affectedLimbNameSet();
		for(int i=0;i<=limbs.length;i++)
		{
			final int bodyCode = (i<limbs.length)?i:Race.BODY_HAND;
			limbs[bodyCode]=M.charStats().getBodyPart(bodyCode);
			if((limbs[bodyCode]>0)
			&&(validBrokens[bodyCode]))
			{
				final String partName = (i==limbs.length)?"finger":Race.BODYPARTSTR[bodyCode].toLowerCase();
				final int numLimbs = limbs[bodyCode];
				final int numBroken = brokenParts[i];
				if(numLimbs>0)
				{
					if(numLimbs-numBroken==1)
					{
						if(!affected.contains(partName))
							remains.add(partName);
					}
					else
					if(numLimbs-numBroken==2)
					{
						if(!affected.contains("left "+partName))
							remains.add("left "+partName);
						if(!affected.contains("right "+partName))
							remains.add("right "+partName);
					}
					else
					for(int ii=0;ii<numLimbs;ii++)
						remains.add(partName);
				}
				else
				for(int ii=0;ii<numLimbs;ii++)
					remains.add(partName);
			}
		}
		final Amputation A=(Amputation)M.fetchEffect("Amputation");
		if(A!=null)
		{
			final List<String> missing=A.affectedLimbNameSet();
			for(final String limb : missing)
			{
				if(limb.endsWith("hand"))
					remains.remove(CMStrings.replaceAll(limb, "hand", "finger"));
				remains.remove(limb);
			}
		}
		return remains;
	}

	@Override
	public void restoreLimb(final String gone)
	{
		if (affected != null)
		{
			if (affected instanceof MOB)
			{
				((MOB)affected).location().show(((MOB)affected), null, CMMsg.MSG_OK_VISUAL, L("^G<S-YOUPOSS> @x1 is now mended.^?",gone));
			}
			else
			if ((affected instanceof DeadBody)
			&& (((Item)affected).owner() instanceof Room))
			{
				((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_VISUAL, L("^G@x1's @x2 is now mended.^?",affected.name(),gone));
			}
		}

		final List<String> theRest = affectedLimbNameSet();
		if (theRest.contains(gone))
			theRest.remove(gone);
		if((theRest.size()==0)&&(affected!=null))
			affected.delEffect(this);
		else
		{
			setMiscText(CMParms.combineWith(theRest, ';'));
		}
	}

	@Override
	public boolean isDamaged(String limbName)
	{
		limbName = limbName.toLowerCase();
		final List<String> theRest = affectedLimbNameSet();
		if (theRest.contains(limbName))
			return true;
		for(final String s : theRest)
		{
			if(s.endsWith(" "+limbName))
				return true;
		}
		return false;
	}

	@Override
	public Item damageLimb(final String brokenLimbName, final boolean intentional)
	{
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				final boolean success=((MOB)affected).location().show(((MOB)affected),this,CMMsg.MSG_OK_VISUAL,L("^G<S-YOUPOSS> @x1 breaks!^?",brokenLimbName));
				if(!success)
					return null;
			}
			else
			if((affected instanceof DeadBody)
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof Room))
			{
				((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("^G@x1's @x2 breaks!^?",affected.name(),brokenLimbName));
			}
		}
		setMiscText(text()+brokenLimbName.toLowerCase()+";");
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
		if(choice.toUpperCase().startsWith("RIGHT "))
			choice=choice.substring(6).trim();
		else
		if(choice.toUpperCase().startsWith("LEFT "))
			choice=choice.substring(5).trim();
		final MOB target=super.getTarget(mob,commands,givenTarget,false,true);
		if(target==null)
			return false;
		if(!auto)
		{
			LegalBehavior B=null;
			if(mob.location()!=null)
				B=CMLib.law().getLegalBehavior(mob.location());
			List<LegalWarrant> warrants=new ArrayList<LegalWarrant>();
			if(B!=null)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
			if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
			{
				mob.tell(L("You are not authorized by law to break anything on @x1 at this time.",target.Name()));
				return false;
			}
			final Item w=mob.fetchWieldedItem();
			if(!CMSecurity.isASysOp(mob))
			{
				Weapon ww=null;
				if((w==null)||(!(w instanceof Weapon)))
				{
					mob.tell(L("You cannot break anything without a weapon!"));
					return false;
				}
				ww=(Weapon)w;
				if((ww.weaponDamageType()==Weapon.TYPE_PIERCING)
				&&(ww.weaponDamageType()!=Weapon.TYPE_SLASHING)
				&&(ww.weaponDamageType()!=Weapon.TYPE_BASHING))
				{
					mob.tell(L("You cannot break a limb with a @x1!",ww.name()));
					return false;
				}
				if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				{
					mob.tell(L("You are too far away to try that!"));
					return false;
				}
				if(!CMLib.flags().isBoundOrHeld(target))
				{
					mob.tell(L("@x1 must be bound before you can break @x2 limbs.",target.charStats().HeShe(),target.charStats().hisher()));
					return false;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final LimbDamage ampuA=(LimbDamage)target.fetchEffect("Amputation");
			final List<String> missingLimbs;
			if(ampuA != null)
				missingLimbs=ampuA.affectedLimbNameSet();
			else
				missingLimbs=new ArrayList<String>(1);

			BrokenLimbs brokenA=(BrokenLimbs)target.fetchEffect(ID());
			boolean newOne=false;
			if(brokenA==null)
			{
				brokenA=new BrokenLimbs();
				brokenA.setAffectedOne(target);
				newOne=true;
			}

			String brokeStr=null;
			final List<String> healthyLimbSet=brokenA.unaffectedLimbSet();
			for(final String missingLimb : missingLimbs)
				healthyLimbSet.remove(missingLimb);
			if(healthyLimbSet.size()==0)
			{
				if(!auto)
					mob.tell(L("There is nothing left on @x1 that is unbroken!",target.name(mob)));
				return false;
			}

			if(choice.length()>0)
			{
				for(int i=0;i<healthyLimbSet.size();i++)
				{
					if(CMLib.english().containsString(healthyLimbSet.get(i),choice))
					{
						brokeStr=healthyLimbSet.get(i);
						break;
					}
				}
				if(brokeStr==null)
				{
					if(!auto)
						mob.tell(L("There is nothing unbroken on @x1 called '@x2'!",target.name(mob),choice));
					return false;
				}
			}

			if(brokeStr==null)
				brokeStr=healthyLimbSet.get(CMLib.dice().roll(1,healthyLimbSet.size(),-1));

			final String brokeName = brokeStr;

			final String str=auto?"":L("^F^<FIGHT^><S-NAME> break(s) <T-YOUPOSS> @x1!^</FIGHT^>^?",brokeName);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),str);
			CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(target,msg))
			{
				final MOB vic=target.getVictim();
				final MOB vic2=mob.getVictim();
				target.location().send(target,msg);
				if(msg.value()<=0)
				{
					if(newOne)
					{
						brokenA.makeLongLasting();
						target.addEffect(brokenA);
					}
					brokenA.damageLimb(brokeStr, (!auto) && (givenTarget==null));
					target.recoverCharStats();
					target.recoverPhyStats();
					target.recoverMaxState();
					target.location().recoverRoomStats();
					target.setVictim(vic);
					mob.setVictim(vic2);
					success=true;
				}
			}
			else
				success=false;
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to break one of <T-YOUPOSS> limbs, but fail(s)."));
		return success;
	}
}
