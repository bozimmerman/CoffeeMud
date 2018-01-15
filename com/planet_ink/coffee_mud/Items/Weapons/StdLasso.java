package com.planet_ink.coffee_mud.Items.Weapons;
import java.util.*;

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
public class StdLasso extends StdWeapon
{
	@Override
	public String ID()
	{
		return "StdLasso";
	}

	public StdLasso()
	{
		super();
		setName("a lasso");
		setDisplayText("a lasso has been left here.");
		setDescription("Its a rope with a big stiff loop on one end!");
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(0);
		basePhyStats.setWeight(1);
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(0);
		baseGoldValue=10;
		recoverPhyStats();
		minRange=1;
		maxRange=1;
		weaponDamageType=Weapon.TYPE_NATURAL;
		material=RawMaterial.RESOURCE_HEMP;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(true);
	}

	protected MOB lastBinder = null;

	protected void untieIfPoss(final MOB binder, final MOB boundM)
	{
		final Room R=boundM.location();
		if(R==null)
			return;
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
					if((CMLib.flags().isAnimalIntelligence(boundM))&&(boundM.getVictim()==binder))
						boundM.makePeace(true);
					if(!CMLib.flags().isBound(boundM))
					{
						msg2.addTrailerMsg(CMClass.getMsg(boundM,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
						msg2.addTrailerMsg(CMClass.getMsg(binder,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
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
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.value()>0)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			msg.setValue(0);
			final MOB targetMOB=(MOB)msg.target();
			if((CMLib.flags().isAnimalIntelligence(targetMOB)))
			{
				final Set<MOB> allMyFam=msg.source().getGroupMembers(new HashSet<MOB>());
				boolean anyCombatants=false;
				for(final MOB M : allMyFam)
				{
					if(M.isInCombat())
						anyCombatants=true;
				}
				if(!anyCombatants)
				{
					if(CMLib.law().doesHavePriviledgesHere(msg.source(), msg.source().location()))
						targetMOB.makePeace(false);
					msg.addTrailerRunnable(new Runnable(){
						@Override
						public void run()
						{
							for(final MOB M : allMyFam)
							{
								if(M.getVictim()==targetMOB)
									M.makePeace(true);
							}
						}
					});
				}
			}
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_HUH)||(msg.sourceMinor()==CMMsg.TYP_COMMANDFAIL))
		&&(msg.source()==lastBinder)
		&&(owner() instanceof MOB)
		&&(owner() != msg.source())
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().length()>0)
		&&(CMLib.flags().isBound(owner())))
		{
			String tmsg=msg.targetMessage().toLowerCase();
			if(tmsg.startsWith("get ")||tmsg.startsWith("take "))
			{
				final MOB boundM=(MOB)owner();
				final Room R=boundM.location();
				if(R!=null)
				{
					tmsg=tmsg.substring(4).trim();
					List<String> rest=CMParms.parse(tmsg);
					String itemName=tmsg;
					int x=rest.indexOf("from");
					if((x>0)&&(x<rest.size()-1))
					{
						itemName=CMParms.combine(rest, 0, x);
						String mobName=CMParms.combine(rest,x+1);
						final MOB M=R.fetchInhabitant(mobName);
						if((M!=null)
						&&(M==boundM)
						&&(CMLib.english().containsString(name(), itemName)))
						{
							untieIfPoss(msg.source(), boundM);
							return false;
						}
						return true;
					}
					if(CMLib.english().containsString(name(), itemName))
					{
						untieIfPoss(msg.source(), boundM);
						return false;
					}
					return true;
				}
				else
					lastBinder=null;
			}
			else
			if(tmsg.startsWith("untie "))
			{
				final MOB boundM=(MOB)owner();
				tmsg=tmsg.substring(6).trim();
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
				else
					lastBinder=null;
			}
		}
		return true;
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			return;
			//msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_DROP,null));
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.target() !=null)
		&&(msg.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			unWear();
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,null));
			msg.addTrailerMsg(CMClass.getMsg((MOB)msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null));
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),this,CMMsg.MASK_ALWAYS|CMMsg.TYP_GENERAL,null));
		}
		else
		if((msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_GENERAL)
		&&(((MOB)msg.target()).isMine(this))
		&&(msg.sourceMessage()==null))
		{
			final MOB tmob=(MOB)msg.target();
			Ability A=CMClass.getAbility("Thief_Bind");
			if(A!=null)
			{
				A.setAffectedOne(this);
				A.invoke(msg.source(),tmob,true,phyStats().level());
				A=tmob.fetchEffect("Thief_Bind");
				if(A!=null)
				{
					lastBinder=A.invoker();
					if(lastBinder==null)
						lastBinder=msg.source();
				}
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

}
