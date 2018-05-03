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

public class Prayer_PeaceRitual extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_PeaceRitual";
	}

	private final static String localizedName = CMLib.lang().L("Peace Ritual");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Peace Ritual)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	public Clan clan1=null;
	public Clan clan2=null;
	public Iterable<Pair<Clan,Integer>> clan2Set=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return false;

		if(invoker==null)
			return false;

		final MOB mob=(MOB)affected;
		if(clan2Set==null)
		{
			if(clan2==null)
				return super.tick(ticking, tickID);
			final Vector<Pair<Clan,Integer>> V=new Vector<Pair<Clan,Integer>>();
			V.add(new Pair<Clan,Integer>(clan2,Integer.valueOf(clan2.getGovernment().getAcceptPos())));
			clan2Set=V;
		}
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),clan2Set,L("@x1 located in '@x2' is performing a peace ritual on behalf of @x3.",mob.name(),mob.location().displayText(mob),clan1.name()),false);
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected==null)
			return true;
		if(!(affected instanceof MOB))
			return true;

		if((msg.target()==affected)
		&&(msg.source()!=affected)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS)))
		{
			msg.source().location().show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The peace ritual is disrupted!"));
			clan1=null;
			clan2=null;
			unInvoke();
		}
		else
		if(msg.amISource((MOB)affected)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)||(msg.targetMinor()==CMMsg.TYP_LEAVE)))
		{
			msg.source().location().show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The peace ritual is disrupted!"));
			clan1=null;
			clan2=null;
			unInvoke();
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		super.unInvoke();

		if((canBeUninvoked())&&(clan1!=null)&&(clan2!=null))
		{
			final Clan C1=clan1;
			final Clan C2=clan2;
			if((C1!=null)&&(C2!=null))
			{
				if(C1.getClanRelations(C2.clanID())==Clan.REL_WAR)
				{
					C1.setClanRelations(C2.clanID(),Clan.REL_HOSTILE,System.currentTimeMillis());
					C1.update();
				}
				if(C2.getClanRelations(C1.clanID())==Clan.REL_WAR)
				{
					C2.setClanRelations(C1.clanID(),Clan.REL_HOSTILE,System.currentTimeMillis());
					C2.update();
				}
				final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),CMLib.clans().clanRoles(),L("There is now peace between @x1 and @x2.",C1.name(),C2.name()),false);
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,target,null,L("<T-NAME> <T-IS-ARE> already affected by @x1.",name()));
			return false;
		}
		clan1=CMLib.clans().findRivalrousClan(mob);
		if(clan1==null)
		{
			mob.tell(L("You must belong to a clan to use this prayer."));
			return false;
		}
		if(commands.size()<1)
		{
			mob.tell(L("You must specify the clan you wish to see peace with."));
			return false;
		}
		final String clan2Name=CMParms.combine(commands,0);
		clan2=CMLib.clans().findClan(clan2Name);
		if((clan2==null)
		||((clan1.getClanRelations(clan2.clanID())!=Clan.REL_WAR)&&(clan2.getClanRelations(clan1.clanID())!=Clan.REL_WAR)))
		{
			mob.tell(L("Your @x1 is not at war with @x2!",clan1.getGovernmentName(),clan2.name()));
			return false;
		}
		boolean found=false;
		for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
		{
			final MOB M=e.nextElement();
			if(M.getClanRole(clan2.clanID())!=null)
			{
				found = true;
				break;
			}
		}
		if(!found)
		{
			mob.tell(L("You must wait until a member of @x1 is online before beginning the ritual.",clan2.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> begin(s) a peace ritual."):L("^S<S-NAME> @x1 for peace between @x2 and @x3.^?",prayWord(mob),clan1.name(),clan2.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,(int)CMProps.getTicksPerMinute()*5);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 for peace between @x2 and @x3, but there is no answer.",prayWord(mob),clan1.name(),clan2.name()));

		// return whether it worked
		return success;
	}
}
