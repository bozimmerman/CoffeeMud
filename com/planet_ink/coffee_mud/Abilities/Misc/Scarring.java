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
   Copyright 2020-2023 Bo Zimmerman

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
public class Scarring extends StdAbility implements LimbDamage, HealthCondition
{
	@Override
	public String ID()
	{
		return "Scarring";
	}

	private final static String	localizedName	= CMLib.lang().L("Scarring");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected static final int	tempDuration		= (int)((1000L * 60L * 5) / CMProps.getTickMillis());
	protected Map<String,Tattoo>scarredLimbs		= null;
	private int[]				scarredParts		= new int[Race.BODY_PARTS];
	protected volatile long		nextCheck			= 0;

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Scars on: "+CMLib.english().toEnglishStringList(affectedLimbNameSet());
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

	private static final String[]	triggerStrings	= I(new String[] { "SCAR" });

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

	protected final void confirmScarring(final MOB mob)
	{
		if(mob != null)
		{
			final List<String> keys;
			final Map<String, Tattoo> m;
			keys = affectedLimbNameSet();
			synchronized(this)
			{
				m=this.scarredLimbs;
			}
			if((keys.size()>0)
			&&(m!=null))
			{
				for(final String key : keys)
				{
					final Tattoo T=m.get(key);
					if(T!=null)
					{
						final Tattoo mT = mob.findTattoo(T.name());
						if(mT != null)
							mT.setTickDown(tempDuration);
						else
							mob.addTattoo(T.name() ,tempDuration);
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			//final MOB M = (MOB)affected;
		}
		super.executeMsg(host,msg);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final int num=this.affectedLimbNameSet().size();
		final int oldCha=affectableStats.getStat(CharStats.STAT_CHARISMA);
		if(num>=oldCha)
			affectableStats.setStat(CharStats.STAT_CHARISMA, 1);
		else
			affectableStats.setStat(CharStats.STAT_CHARISMA, oldCha-num);
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if(msg.source() == affected)
		{

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

		// ok, this is tricky
		this.affectedLimbNameSet(); // cause caching
		final Map<String,Tattoo> scarredLimbs=this.scarredLimbs;
		if(canBeUninvoked())
		{
			if(scarredLimbs != null)
			{
				for(final String key : scarredLimbs.keySet())
					mob.delTattoo(scarredLimbs.get(key));
				scarredLimbs.clear();
			}
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> scarred body has been healed."));
		}
		else
		{
			// this MIGHT mean something...so pretend it might
			for(final String key : scarredLimbs.keySet())
			{
				final Tattoo T=scarredLimbs.get(key);
				final Tattoo mT=mob.findTattoo(T.name());
				if((mT != null)&&(mT.getTickDown() > 1))
					mT.setTickDown(1);
			}
			this.nextCheck = System.currentTimeMillis() + 100; // the next check must happen naturally, if it is going to happen
		}
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

		if((target==null)
		||((!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardMovingBy(target,mob))||(!target.isInCombat()))))
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
	public void setMiscText(final String text)
	{
		if(text.toUpperCase().startsWith("+"))
		{
		}
		else
		{
			super.setMiscText(text);
			scarredLimbs=null;
			final Physical affected = this.affected;
			if(affected instanceof MOB)
				confirmScarring((MOB)affected);
		}
	}

	@Override
	public List<String> affectedLimbNameSet()
	{
		if(scarredLimbs!=null)
			return new MapKeyList<String, Tattoo>(scarredLimbs);
		scarredLimbs=new Hashtable<String,Tattoo>();
		if(affected==null)
			return new MapKeyList<String, Tattoo>(scarredLimbs);
		if((!(affected instanceof MOB))&&(!(affected instanceof DeadBody)))
			return new MapKeyList<String, Tattoo>(scarredLimbs);
		synchronized(this)
		{
			final List<String> scarredLimbsList=CMParms.parseSemicolons(text().toLowerCase(),true);
			scarredParts=new int[Race.BODY_PARTS];
			boolean right=false;
			boolean left=false;
			Integer code=null;
			for(int v=0;v<scarredLimbsList.size();v++)
			{
				final String s=scarredLimbsList.get(v);
				left=s.startsWith("left ");
				right=s.startsWith("right ");
				final String subS=s.substring(right?6:left?5:0).trim();
				for(int c=0;c<Race.BODY_PARTS;c++)
				{
					if(getProperPartName(c).equalsIgnoreCase(subS)
					||Race.BODYPARTSTR[c].equalsIgnoreCase(subS))
					{
						code=Integer.valueOf(c);
						scarredParts[c]--;
						final long wearLoc = Race.BODY_WEARVECTOR[code.intValue()];
						final Tattoo T=(Tattoo)CMClass.getCommon("Defaulttattoo");
						T.set(Wearable.CODES.NAME(wearLoc)+": "+L("a scarred "+(left?"left ":(right?"right ":""))+getProperPartName(c).toLowerCase()));
						scarredLimbs.put(s, T); // preserve left/right
					}
				}
			}
		}
		return new MapKeyList<String, Tattoo>(scarredLimbs);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_MOB)
		{
			if(System.currentTimeMillis() >= nextCheck)
			{
				nextCheck = System.currentTimeMillis() + (CMProps.getTickMillis() * tempDuration) - CMProps.getTickMillis();
				if(affected instanceof MOB)
					confirmScarring((MOB)affected);
			}
		}
		return super.tick(ticking, tickID);
	}

	protected String getProperPartName(final int bodyPartCode)
	{
		switch(bodyPartCode)
		{
		case Race.BODY_HEAD:
			return "face";
		case Race.BODY_EYE:
			return "eyebrow";
		default:
			if((bodyPartCode<0)||( bodyPartCode>=Race.BODYPARTSTR.length))
				return null;
			return Race.BODYPARTSTR[ bodyPartCode].toLowerCase();
		}
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
		for(int i=0;i<limbs.length;i++)
		{
			limbs[i]=M.charStats().getBodyPart(i);
			if(limbs[i]>0)
			{
				final String partName=Race.BODYPARTSTR[i].toLowerCase();
				if(scarredParts[i]>0)
				{
					if(limbs[i]-scarredParts[i]==1)
					{
						if(!affected.contains(partName))
							remains.add(partName);
					}
					else
					if(limbs[i]-scarredParts[i]==2)
					{
						if(!affected.contains("left "+partName))
							remains.add("left "+partName);
						if(!affected.contains("right "+partName))
							remains.add("right "+partName);
					}
					else
					for(int ii=0;ii<limbs[i];ii++)
						remains.add(partName);
				}
				else
				for(int ii=0;ii<limbs[i];ii++)
					remains.add(partName);
			}
		}
		final Amputation A=(Amputation)M.fetchEffect("Amputation");
		if(A!=null)
		{
			final List<String> missing=A.affectedLimbNameSet();
			for(final String limb : missing)
				remains.remove(limb);
		}
		return remains;
	}

	@Override
	public void restoreLimb(final String gone)
	{
		final Physical affected = this.affected;
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

		List<String> theRest;
		final Map<String,Tattoo> scarredLimbs;
		theRest = affectedLimbNameSet();
		synchronized(this)
		{
			scarredLimbs=this.scarredLimbs;
		}
		if((scarredLimbs != null)
		&&(scarredLimbs.containsKey(gone)))
		{
			final Tattoo T = scarredLimbs.remove(gone);
			if((T!=null)&&(affected instanceof MOB))
			{
				((MOB)affected).delTattoo(T);
				theRest = affectedLimbNameSet();
			}
		}

		// now make it permanent, whatever just happened
		if((theRest.size()==0)&&(affected!=null))
			affected.delEffect(this);
		else
			setMiscText(CMParms.combineWith(theRest, ';'));
		if(affected instanceof MOB)
			confirmScarring((MOB)affected);
	}

	@Override
	public Item damageLimb(final String scarredLimbName)
	{
		final Physical affected = this.affected;
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				final boolean success=((MOB)affected).location().show(((MOB)affected),this,CMMsg.MSG_OK_VISUAL,L("^G<S-YOUPOSS> @x1 becomes scarred!^?",scarredLimbName));
				if(!success)
					return null;
			}
			else
			if((affected instanceof DeadBody)
			&&(((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof Room))
			{
				((Room)((Item)affected).owner()).showHappens(CMMsg.MSG_OK_VISUAL,L("^G@x1's @x2 becomes scarred!^?",affected.name(),scarredLimbName));
			}
		}
		setMiscText(text()+scarredLimbName.toLowerCase()+";");
		if(affected instanceof MOB)
			confirmScarring((MOB)affected);
		return null;
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
			List<LegalWarrant> warrants=new Vector<LegalWarrant>();
			if(B!=null)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
			if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
			{
				mob.tell(L("You are not authorized by law to scar anything on @x1 at this time.",target.Name()));
				return false;
			}
			final Item w=mob.fetchWieldedItem();
			if(!CMSecurity.isASysOp(mob))
			{
				if((w==null)||(!(w instanceof Weapon)))
				{
					mob.tell(L("You cannot scar anything without a weapon!"));
					return false;
				}
				if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				{
					mob.tell(L("You are too far away to try that!"));
					return false;
				}
				if(!CMLib.flags().isBoundOrHeld(target))
				{
					mob.tell(L("@x1 must be bound before you can scar @x2 limbs.",target.charStats().HeShe(),target.charStats().hisher()));
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

			Scarring scarrednA=(Scarring)target.fetchEffect(ID());
			boolean newOne=false;
			if(scarrednA==null)
			{
				scarrednA=new Scarring();
				scarrednA.setAffectedOne(target);
				newOne=true;
			}

			String scarredStr=null;
			final List<String> healthyLimbSet=scarrednA.unaffectedLimbSet();
			for(final String missingLimb : missingLimbs)
				healthyLimbSet.remove(missingLimb);
			if(healthyLimbSet.size()==0)
			{
				if(!auto)
					mob.tell(L("There is nothing left on @x1 that is unscarredn!",target.name(mob)));
				return false;
			}

			if(choice.length()>0)
			{
				for(int i=0;i<healthyLimbSet.size();i++)
				{
					if(CMLib.english().containsString(healthyLimbSet.get(i),choice))
					{
						scarredStr=healthyLimbSet.get(i);
						break;
					}
				}
				if(scarredStr==null)
				{
					if(!auto)
						mob.tell(L("There is nothing unscarred on @x1 called '@x2'!",target.name(mob),choice));
					return false;
				}
			}

			if(scarredStr==null)
				scarredStr=healthyLimbSet.get(CMLib.dice().roll(1,healthyLimbSet.size(),-1));

			final String scarredName = scarredStr;

			final String str=auto?"":L("^F^<FIGHT^><S-NAME> scar(s) <T-YOUPOSS> @x1!^</FIGHT^>^?",scarredName);
			final int mask=(CMSecurity.isASysOp(mob)?CMMsg.MASK_MOVE:CMMsg.MSK_MALICIOUS_MOVE);
			final CMMsg msg=CMClass.getMsg(mob,target,this,mask|CMMsg.TYP_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),str);
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
						scarrednA.makeLongLasting();
						target.addEffect(scarrednA);
					}
					scarrednA.damageLimb(scarredStr);
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
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to scar one of <T-YOUPOSS> limbs, but fail(s)."));
		return success;
	}
}
