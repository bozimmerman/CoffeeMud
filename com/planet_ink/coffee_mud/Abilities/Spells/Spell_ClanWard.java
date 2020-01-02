package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Spell_ClanWard extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ClanWard";
	}

	private final static String	localizedName	= CMLib.lang().L("Clan Ward");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ABJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_CLANMAGIC;
	}

	@Override
	public boolean disregardsArmorCheck(final MOB mob)
	{
		return true;
	}

	protected String	clan		= "";
	protected int		rank		= 0;
	protected boolean	noSneak		= false;
	private boolean		noFollow	= false;

	@Override
	public void setMiscText(final String txt)
	{
		super.setMiscText(txt);
		rank=CMParms.getParmInt(txt, "RANK", 0);
		clan=CMParms.getParmStr(txt, "CLAN", "");
		noSneak=CMParms.getParmBool(txt, "NOSNEAK",false);
		noFollow=CMParms.getParmBool(txt, "NOFOLLOW",false);
	}

	public boolean passesMuster(final MOB mob)
	{
		if(mob==null)
			return false;
		if((clan.length()==0)||(rank==0))
			return true;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(!noSneak))
			return true;
		for(final Pair<Clan,Integer> C : mob.clans())
		{
			if(C.first.clanID().equalsIgnoreCase(clan))
			{
				if(C.second.intValue() < rank)
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected!=null)
		{
			if((msg.target() instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!CMLib.flags().isFalling(msg.source()))
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				final HashSet<MOB> H=new HashSet<MOB>();
				H.add(msg.source());
				if(!noFollow)
				{
					final MOB M=msg.source().amUltimatelyFollowing();
					if(M!=null)
						H.add(M);
				}
				for(final Iterator<MOB> e=H.iterator();e.hasNext();)
				{
					if(passesMuster(e.next()))
						return super.okMessage(myHost,msg);
				}
				msg.source().tell(L("You may not go that way."));
				return false;
			}
			else
			if((msg.target() instanceof Rideable)
			&&(msg.amITarget(affected)))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_MOUNT:
					{
						final HashSet<MOB> H=new HashSet<MOB>();
						H.add(msg.source());
						if(!noFollow)
						{
							final MOB M=msg.source().amUltimatelyFollowing();
							if(M!=null)
								H.add(M);
						}
						for(final Iterator<MOB> e=H.iterator();e.hasNext();)
						{
							final MOB E=e.next();
							if(passesMuster(E))
								return super.okMessage(myHost,msg);
						}
						msg.source().tell(L("You are not permitted in there."));
						return false;
					}
				default:
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.clans().iterator().hasNext())
		{
			mob.tell(L("You aren't even a member of a clan."));
			return false;
		}

		final LandTitle T=CMLib.law().getLandTitle(mob.location());
		if((T==null)||(T.getOwnerName().length()==0))
		{
			mob.tell(L("Your clan does not own this room."));
			return false;
		}
		final Clan C=CMLib.clans().getClanExact(T.getOwnerName());
		if(C==null)
		{
			mob.tell(L("Your clan does not own this room."));
			return false;
		}

		final Pair<Clan,Integer> clanPair=mob.getClanRole(C.clanID());
		if(clanPair==null)
		{
			mob.tell(L("You are not authorized to draw from the power of your clan."));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;
		if(!CMLib.law().doesOwnThisLand(C.clanID(), R))
		{
			mob.tell(L("Your clan does not own this room."));
			return false;
		}
		if(!CMLib.flags().canAccess(mob,R))
		{
			mob.tell(L("You can't use this magic to get there from here."));
			return false;
		}

		if(C.getAuthority(clanPair.second.intValue(), Clan.Function.CLAN_BENEFITS) == Clan.Authority.CAN_NOT_DO)
		{
			mob.tell(L("You aren't authorized to draw from the power of '@x1'.",C.clanID()));
			return false;
		}

		Ability wardA=null;
		for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A instanceof Spell_ClanWard)
				wardA=A;
		}

		final Room target=R;
		final String rank=CMParms.combine(commands).toUpperCase().trim();
		if(rank.equals("UNINVOKE")||rank.equals("REVOKE"))
		{
			if(wardA==null)
			{
				mob.tell(L("@x1 is not warded.",target.name(mob)));
				return false;
			}
			else
			{
				final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_HANDS,L("<S-NAME> revoke(s) @x1 from @x2",name(),target.name()));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					target.delEffect(wardA);
				}
				return true;
			}
		}
		final int points=C.getRoleFromName(rank);
		if(points < 0)
		{
			final StringBuilder str=new StringBuilder("'"+CMParms.combine(commands)+"' is not a proper rank in your clan.  Try one of the following: ");
			for(final ClanPosition pos : C.getGovernment().getPositions())
				str.append(pos.getName()).append(", ");
			str.append(", or REVOKE to remove a ward.");
			mob.tell(L(str.toString()));
			return true;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final long exp=(1+points)*CMProps.getIntVar(CMProps.Int.CLANENCHCOST);
		if((C.getExp()<exp)||(exp<0))
		{
			mob.tell(L("You need @x1 to do that, but your @x2 has only @x3 experience points.",""+exp,C.getGovernmentName(),""+C.getExp()));
			return false;
		}

		if(points > C.getClanLevel())
		{
			mob.tell(L("Your can not enchant above your clans level, which is @x1.",""+C.getClanLevel()));
			return false;
		}

		// Add clan power check end
		if(wardA!=null)
		{
			mob.tell(L("@x1 is already warded.",target.name(mob)));
			return false;
		}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		C.adjExp(mob,(int)-exp);
		C.update();

		wardA=CMClass.getAbility(ID());
		wardA.setMiscText("CLAN=\""+C.clanID()+"\" RANK="+points);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,R,this,CMMsg.MASK_MOVE|verbalCastCode(mob,mob,auto),L("^S<S-NAME> invoke(s) a warding spell here.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().addNonUninvokableEffect(wardA);
				CMLib.database().DBUpdateRoom(R);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to invoke a clan ward, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
