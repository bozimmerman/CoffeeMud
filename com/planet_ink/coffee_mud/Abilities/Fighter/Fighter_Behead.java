package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Fighter_Behead extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_Behead";
	}

	private final static String localizedName = CMLib.lang().L("Behead");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"BEHEAD"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(0);
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null)&&(target instanceof MOB))
		{
			final Race R=((MOB)target).charStats().getMyRace();
			if(R.bodyMask()[Race.BODY_HEAD]<=0)
				return Ability.QUALITY_INDIFFERENT;
			LegalBehavior B=null;
			if(mob.location()!=null)
				B=CMLib.law().getLegalBehavior(mob.location());
			List<LegalWarrant> warrants=new Vector<LegalWarrant>();
			if(B!=null)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),(MOB)target);
			if(warrants.size()==0)
				return Ability.QUALITY_INDIFFERENT;
			final Item w=mob.fetchWieldedItem();
			Weapon ww=null;
			if((w==null)||(!(w instanceof Weapon)))
				return Ability.QUALITY_INDIFFERENT;
			ww=(Weapon)w;
			if(ww.weaponDamageType()!=Weapon.TYPE_SLASHING)
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
				return Ability.QUALITY_INDIFFERENT;
			if(!CMLib.flags().isBoundOrHeld(target))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final Race R=target.charStats().getMyRace();
		if(R.bodyMask()[Race.BODY_HEAD]<=0)
		{
			mob.tell(L("@x1 has no head!",target.name(mob)));
			return false;
		}

		LegalBehavior B=null;
		if(mob.location()!=null)
			B=CMLib.law().getLegalBehavior(mob.location());
		List<LegalWarrant> warrants=new Vector<LegalWarrant>();
		if(B!=null)
			warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
		if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
		{
			mob.tell(L("You are not allowed to behead @x1 at this time.",target.Name()));
			return false;
		}

		final Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell(L("You cannot behead without a weapon!"));
			return false;
		}
		ww=(Weapon)w;
		if((!auto)&&(!CMSecurity.isASysOp(mob)))
		{
			if(ww.weaponDamageType()!=Weapon.TYPE_SLASHING)
			{
				mob.tell(L("You cannot behead with a @x1!",ww.name()));
				return false;
			}
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
			{
				mob.tell(L("You are too far away to try that!"));
				return false;
			}
			if(!CMLib.flags().isBoundOrHeld(target))
			{
				mob.tell(L("@x1 is not bound and would resist.",target.charStats().HeShe()));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		final boolean hit=(auto)||CMLib.combat().rollToHit(mob,target);
		boolean success=proficiencyCheck(mob,0,auto)&&(hit);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(1);
				final Ability A2=target.fetchEffect("Injury");
				if(A2!=null)
					A2.setMiscText(mob.Name()+"/head");
				CMLib.combat().postDamage(mob,target,ww,Integer.MAX_VALUE/2,CMMsg.MSG_WEAPONATTACK,ww.weaponClassification(),auto?"":L("^F^<FIGHT^><S-NAME> rear(s) back and behead(s) <T-NAME>!^</FIGHT^>^?@x1",CMLib.protocol().msp("decap.wav",30)));
				mob.location().recoverRoomStats();
				final Item limb=CMClass.getItem("GenLimb");
				limb.setName(L("@x1`s head",target.Name()));
				limb.basePhyStats().setAbility(1);
				limb.setDisplayText(L("the bloody head of @x1 is sitting here.",target.Name()));
				limb.setSecretIdentity(target.name()+"`s bloody head.");
				int material=RawMaterial.RESOURCE_MEAT;
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
				limb.setMaterial(material);
				limb.basePhyStats().setLevel(1);
				limb.basePhyStats().setWeight(5);
				limb.recoverPhyStats();
				mob.location().addItem(limb,ItemPossessor.Expire.Player_Drop);
				for(int i=0;i<warrants.size();i++)
				{
					final LegalWarrant W=warrants.get(i);
					W.setCrime("pardoned");
					W.setOffenses(0);
				}
			}
			else
				success=false;
			if(mob.getVictim()==target)
				mob.makePeace(true);
			if(target.getVictim()==mob)
				target.makePeace(true);
		}
		else
			maliciousFizzle(mob,target,L("<S-NAME> attempt(s) a beheading and fail(s)!"));
		return success;
	}
}
