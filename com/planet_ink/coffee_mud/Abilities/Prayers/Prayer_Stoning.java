package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_Stoning extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Stoning";
	}

	private final static String localizedName = CMLib.lang().L("Stoning");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	protected Vector<MOB> cits=new Vector<MOB>();

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		cits=new Vector<MOB>();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);
		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if(R!=null)
		{
			for(int i=0;i<cits.size();i++)
			{
				final MOB M=cits.elementAt(i);
				if((M.location()!=mob.location())||(mob.amDead()))
				{
					CMLib.tracking().wanderAway(M,true,false);
					M.destroy();
					M.setLocation(null);
				}
				else
				{
					if(CMLib.dice().rollPercentage()>=50)
					{
						int dmg=mob.maxState().getHitPoints()/20;
						if(dmg<1)
							dmg=1;
						final Item W=M.fetchWieldedItem();
						if(W!=null)
						{
							W.basePhyStats().setDamage(dmg);
							W.phyStats().setDamage(dmg);
						}
						CMLib.combat().postDamage(M,mob,W,dmg,CMMsg.MSG_WEAPONATTACK|CMMsg.MASK_ALWAYS,Weapon.TYPE_BASHING,L("<S-NAME> stone(s) <T-NAMESELF>!"));
					}
					else
						R.show(M,mob,null,CMMsg.MSG_NOISE,L("<S-NAME> shout(s) obscenities at <T-NAMESELF>."));
				}
			}
			while(cits.size()<10)
			{
				final MOB M=CMClass.getMOB("AngryCitizen");
				if(M==null)
				{
					unInvoke();
					break;
				}
				final Room R2=CMClass.getLocale("StdRoom");
				cits.addElement(M);
				M.bringToLife(R2,true);
				CMLib.tracking().wanderIn(M,R);
				M.setStartRoom(null);
				R2.destroy();
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		LegalBehavior B=null;
		if(mob.location()!=null)
			B=CMLib.law().getLegalBehavior(mob.location());
		List<LegalWarrant> warrants=new Vector<LegalWarrant>();
		if(B!=null)
			warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),target);
		if((warrants.size()==0)&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ABOVELAW)))
		{
			mob.tell(L("You are not allowed to stone @x1 at this time.",target.Name()));
			return false;
		}

		if((!auto)&&(!CMLib.flags().isBoundOrHeld(target))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("@x1 must be bound first.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> call(s) for the stoning of <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE)!=null;
					for(int i=0;i<warrants.size();i++)
					{
						final LegalWarrant W=warrants.get(i);
						W.setCrime("pardoned");
						W.setOffenses(0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> call(s) for the stoning of <T-NAMESELF>."));

		// return whether it worked
		return success;
	}
}
