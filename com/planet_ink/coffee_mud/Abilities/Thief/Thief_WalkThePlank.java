package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_WalkThePlank extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_WalkThePlank";
	}

	private final static String	localizedName	= CMLib.lang().L("Walk The Plank");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "WALKTHEPLANK", "PLANK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public String displayText()
	{
		return "";
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
	public int usageType()
	{
		return USAGE_MANA;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((!(R.getArea() instanceof BoardableShip))
		||((R.domainType()&Room.INDOORS)!=0)
		||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR))
		{
			mob.tell(L("You must be on the deck of a ship to make someone walk the plank."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final Item myShipItem=myShip.getShipItem();
		final Area myShipArea=myShip.getShipArea();
		if((myShipItem==null)
		||(myShipArea==null)
		||(!(myShipItem.owner() instanceof Room))
		||(!CMLib.flags().isWateryRoom((Room)myShipItem.owner())))
		{
			mob.tell(L("Your ship must be at sea to make someone walk the plank."));
			return false;
		}
		final Room splashR=(Room)myShipItem.owner();
		
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		
		boolean allowedToWalkThem=false;
		LegalBehavior B=CMLib.law().getLegalBehavior(R);
		if(B!=null)
		{
			List<LegalWarrant> warrants=B.getWarrantsOf(CMLib.law().getLegalObject(R),target);
			if((warrants.size()>0)&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.ABOVELAW)))
			{
				allowedToWalkThem=true;
			}
		}
		
		if(CMLib.flags().isBoundOrHeld(target))
		{
			allowedToWalkThem=true;
		}
		
		if(!allowedToWalkThem)
		{
			Set<MOB> mobsConnectedToThisShip=new HashSet<MOB>();
			for(Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				if(R2!=null)
				{
					String owner = CMLib.law().getPropertyOwnerName(R2);
					Clan C=CMLib.clans().getClan(owner);
					for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(M==null)
							continue;
						String startOwner=CMLib.law().getPropertyOwnerName(M.getStartRoom());
						if((CMLib.law().doesHavePriviledgesHere(mob, R2))
						||(M.getStartRoom() == R2)
						||(M.getStartRoom().getArea() == myShipArea))
							mobsConnectedToThisShip.add(M);
						else
						if((M.getStartRoom().getArea() instanceof BoardableShip)
						&&(owner.equals(startOwner) && (owner.length()>0)))
							mobsConnectedToThisShip.add(M);
						else
						if((C!=null)&&(M.getClanRole(C.clanID())!=null))
							mobsConnectedToThisShip.add(M);
					}
				}
			}
			for(Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				if(R2!=null)
				{
					for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(M==null)
							continue;
						for(MOB M2 : M.getGroupMembers(new HashSet<MOB>()))
						{
							if((M2 != M)
							&&(mobsConnectedToThisShip.contains(M2)))
								mobsConnectedToThisShip.add(M);
						}
						if(M.isMarriedToLiege())
						{
							final MOB M2=CMLib.players().getLoadPlayer(M.getLiegeID());
							if((M2!=null)&&(mobsConnectedToThisShip.contains(M2)))
								mobsConnectedToThisShip.add(M);
						}
					}
				}
			}
			Set<MOB> mobsNotConnectedToThisShip=new HashSet<MOB>();
			for(Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				if(R2!=null)
				{
					for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(M==null)
							continue;
						for(MOB M2 : M.getGroupMembers(new HashSet<MOB>()))
						{
							if((M2 != M)
							&&(mobsConnectedToThisShip.contains(M2)))
							{
								mobsConnectedToThisShip.add(M2);
							}
						}
						if(!mobsConnectedToThisShip.contains(M))
							mobsNotConnectedToThisShip.add(M);
					}
				}
			}
			
			if(mobsConnectedToThisShip.contains(mob)
			&&(mobsNotConnectedToThisShip.contains(target))
			&&(!mob.isInCombat())
			&&(!target.isInCombat()))
				allowedToWalkThem=true;
			if(mobsConnectedToThisShip.contains(mob)
			&&(mobsNotConnectedToThisShip.contains(target))
			&&(mobsNotConnectedToThisShip.size()<2))
				allowedToWalkThem=true;
			else
			if(mobsConnectedToThisShip.contains(target)
			&&(mobsNotConnectedToThisShip.contains(mob))
			&&(mobsConnectedToThisShip.size()<2)
			&&(mobsNotConnectedToThisShip.size()>1))
				allowedToWalkThem=true;
		}
		
		if(!allowedToWalkThem)
		{
			mob.tell(L("You can't make @x1 walk the plank.",target.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			String str=auto?"":L("^S<S-NAME> make(s) <T-NAME> walk the plank..^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.MSG_THIEF_ACT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE,null,CMMsg.MASK_MALICIOUS|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null,CMMsg.NO_EFFECT,null);
			if(R.okMessage(mob,msg) && R.okMessage(mob,msg2))
			{
				R.send(mob,msg);
				R.send(mob,msg2);
				if((msg.value()<=0) && (msg2.value()<=0))
				{
					CMLib.threads().scheduleRunnable(new Runnable(){
						@Override
						public void run()
						{
							CMLib.tracking().walkForced(target, R, splashR, false, true, L("<S-NAME> walks off the plank on @x1 and go(es) kersplash!",myShipItem.name()));
						}
					}, 1000);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> call(s) for <T-NAME> to walk the plank, but <T-HIM-HER> won't budge."));

		// return whether it worked
		return success;
	}
}
