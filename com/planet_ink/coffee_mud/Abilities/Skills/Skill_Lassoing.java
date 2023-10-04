package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2023-2023 Bo Zimmerman

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
public class Skill_Lassoing extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Lassoing";
	}

	private final static String	localizedName	= CMLib.lang().L("Lassoing");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
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

	private static final String[]	triggerStrings	= I(new String[] { "LASSO" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ROPEUSE;
	}

	protected final static int[] LASSO_MATERIALS = new int[] {
		RawMaterial.RESOURCE_HEMP
	};

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof Item))
			return;
		final Item I = (Item)affected;
		final MOB binderM = invoker();

		super.unInvoke();

		if(canBeUninvoked() && (I.owner() instanceof MOB) && (binderM != null))
		{
			final MOB boundM = (MOB)I.owner();
			final Ability A=boundM.fetchEffect("Thief_Bind");
			if((A!=null)&&(A.invoker()==binderM))
			{
				A.unInvoke();
				if((boundM.location()!=null)&&(!boundM.amDead()))
					boundM.location().show(boundM,I,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-HAS-HAVE> been freed from <T-NAME>."));
			}
			if(boundM.isMine(I))
				CMLib.commands().postDrop(boundM, I, true, false, false);
		}
	}

	protected void untieIfPoss(final MOB binder, final MOB boundM)
	{
		final Physical affected = this.affected;
		final Room R=boundM.location();
		if((R==null)||(!(affected instanceof Item)))
			return;
		final Item lasso = (Item)affected;
		if((CMLib.flags().canBeSeenBy(boundM, binder))
		&&(CMLib.flags().isAliveAwakeMobileUnbound(binder, true)))
		{
			final Ability A=boundM.fetchEffect("Thief_Bind");
			if((A==null)||(A.invoker()!=binder))
			{
				binder.tell(L("You can't figure out the knots."));
			}
			else
			{
				final CMMsg msg2=CMClass.getMsg(binder,boundM,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> untie(s) <T-NAME>."));
				if(R.okMessage(binder, msg2))
				{
					if((CMLib.flags().isAnimalIntelligence(boundM))&&(boundM.getVictim()==binder))
						boundM.makePeace(true);
					A.unInvoke();
					unInvoke();
					if((CMLib.flags().isAnimalIntelligence(boundM))&&(boundM.getVictim()==binder))
						boundM.makePeace(true);
					if(!CMLib.flags().isBound(boundM))
					{
						if(boundM.isMine(lasso))
							msg2.addTrailerMsg(CMClass.getMsg(boundM,lasso,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
						if(!binder.isMine(lasso))
							msg2.addTrailerMsg(CMClass.getMsg(binder,lasso,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
					}
					R.send(binder, msg2);
				}
			}
		}
		else
			binder.tell(L("You don't see that here."));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(((msg.sourceMinor()==CMMsg.TYP_HUH)||(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL))
		&&(msg.source()==invoker())
		&&(affected instanceof Item)
		&&(((Item)affected).owner() instanceof MOB)
		&&(((Item)affected).owner() != msg.source())
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0)
		&&(CMLib.flags().isBound(((Item)affected).owner())))
		{
			String tmsg=msg.targetMessage().toLowerCase().trim();
			final int wdx=tmsg.indexOf(' ');
			final String wd = (wdx<0)?"":tmsg.substring(0,wdx);
			if("get".startsWith(wd)||"take".startsWith(wd))
			{
				final MOB boundM=(MOB)((Item)affected).owner();
				final Room R=boundM.location();
				if(R!=null)
				{
					tmsg=tmsg.substring(wdx+1).trim();
					final List<String> rest=CMParms.parse(tmsg);
					String itemName=tmsg;
					final int x=rest.indexOf("from");
					if((x>0)&&(x<rest.size()-1))
					{
						itemName=CMParms.combine(rest, 0, x);
						final String mobName=CMParms.combine(rest,x+1);
						final MOB M=R.fetchInhabitant(mobName);
						if((M!=null)
						&&(M==boundM)
						&&(CMLib.english().containsString(affected.name(), itemName)))
						{
							untieIfPoss(msg.source(), boundM);
							return false;
						}
						return true;
					}
					if(CMLib.english().containsString(affected.name(), itemName))
					{
						untieIfPoss(msg.source(), boundM);
						return false;
					}
					return true;
				}
			}
			else
			if("untie".startsWith(wd))
			{
				final MOB boundM=(MOB)((Item)affected).owner();
				tmsg=tmsg.substring(wdx+1).trim();
				final Room R=boundM.location();
				if(R!=null)
				{
					final MOB M=R.fetchInhabitant(tmsg);
					if((M!=null)
					&&(M==boundM))
					{
						untieIfPoss(msg.source(), boundM);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(affected instanceof Item)
		&&(msg.target()==((Item)affected).owner())
		&&(msg.value()>0)
		&&(((Item)affected).owner() instanceof MOB)
		&&(msg.tool()!=affected)
		&&(CMLib.flags().isBound(((Item)affected).owner()))
		&&(invoker()!=null))
		{
			final MOB M = (MOB)((Item)affected).owner();
			final Ability A=M.fetchEffect("Thief_Bind");
			if((A!=null)&&(A.invoker()==invoker()))
			{
				A.unInvoke();
				unInvoke();
				if(M.isMine(affected))
					msg.addTrailerMsg(CMClass.getMsg(M,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
				msg.addTrailerMsg(CMClass.getMsg(invoker(),affected,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB targetM = super.getTarget(mob, commands, givenTarget, false, true);
		if(targetM == null)
			return false;

		final Room R = targetM.location();
		if(R == null)
			return false;

		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob, false))
			return false;

		Item lasso = mob.fetchHeldItem();
		if(!CMLib.flags().isARope(lasso))
			lasso=null;
		if(lasso == null)
		{
			for(final Enumeration<Item> i = mob.items();i.hasMoreElements();)
			{
				final Item I = i.nextElement();
				if(CMLib.flags().isARope(I)
				&&(I.container()==null))
				{
					lasso = I;
					break;
				}
			}
		}
		if((lasso == null) && (auto))
		{
			lasso = CMClass.getBasicItem("GenRideable");
			lasso.setMaterial(RawMaterial.RESOURCE_HEMP);
			((Rideable)lasso).setRideBasis(Rideable.Basis.LADDER);
			lasso.setName("a lasso");
			lasso.setDisplayText("a lasso is here");
		}

		if(lasso == null)
		{
			mob.tell(L("You don't seem to have a suitable rope handy."));
			return false;
		}

		final Set<Item> items = new HashSet<Item>();
		items.addAll(mob.fetchWornItems(Wearable.WORN_HELD, (short)-2048,(short)0));
		items.addAll(mob.fetchWornItems(Wearable.WORN_WIELD, (short)-2048,(short)0));
		final int numHands = mob.charStats().getBodyPart(Race.BODY_HAND);
		if(items.size() >= numHands)
		{
			mob.tell(L("You need a free hand to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
			lasso.unWear();
		final CMMsg dropMsg = CMClass.getMsg(mob,lasso,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null);
		success = success && R.okMessage(mob, dropMsg);
		success = success && CMLib.combat().rollToHit(mob,targetM);
		if(success)
			R.send(mob, dropMsg);
		success = success && (lasso.owner() instanceof Room);
		final CMMsg getMsg = CMClass.getMsg(targetM,lasso,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null);
		success = success && R.okMessage(targetM, getMsg);
		boolean anyCombatants=false;
		final Set<MOB> allMyFam=mob.getGroupMembers(new HashSet<MOB>());
		if(success) // now, if lassoing an animal, make sure combat is NOT started
		{
			if(CMLib.flags().isAnimalIntelligence(targetM))
			{
				for(final MOB M : allMyFam)
				{
					if(M.isInCombat())
						anyCombatants=true;
				}
			}
		}
		success = success && R.show(mob, targetM, this, CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT, L("^F<S-NAME> lasso(s) <T-NAME>!^N"));
		if(success)
			R.send(targetM, getMsg);
		success = success && (lasso.owner() == targetM);
		if(success) // now, if lassoing an animal, make sure combat is NOT started
		{
			if(!anyCombatants && (CMLib.flags().isAnimalIntelligence(targetM)))
			{
				if(CMLib.law().doesHavePriviledgesHere(mob, R))
				{
					targetM.makePeace(false);
					for(final MOB M : allMyFam)
					{
						if(M.getVictim()==targetM)
							M.makePeace(true);
					}
				}
			}
			if(this.beneficialAffect(mob, lasso, 0, -1)!=null)
			{
				final Ability A=CMClass.getAbility("Thief_Bind");
				if(A!=null)
				{
					A.setAffectedOne(targetM);
					A.setMiscText("ROPENAME=\""+lasso.Name()+"\"");
					A.invoke(mob,targetM,true,asLevel);
				}
			}
		}
		else
			maliciousFizzle(mob,targetM,L("<S-NAME> attempt(s) to lasso <T-NAME>, but miss(es)."));

		// return whether it worked
		return success;
	}

}

