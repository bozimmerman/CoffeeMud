package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Branding extends CommonSkill implements PrivateProperty
{
	@Override
	public String ID()
	{
		return "Branding";
	}

	private final static String	localizedName	= CMLib.lang().L("Branding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BRANDING", "BRAND"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String displayText()
	{
		if(!this.canBeUninvoked())
		{
			if(owner.length()>0)
				return L("Branded by @x1",owner);
			return "";
		}
		return displayText;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	public Branding()
	{
		super();
		displayText=L("You are branding...");
		verb=L("branding");
	}

	protected MOB		branding	= null;
	protected boolean	messedUp	= false;
	protected int		price		= -1;
	protected String	owner		= "";

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText!=null)
		{
			price = CMParms.getParmInt(newMiscText, "PRICE", -1);
			owner = CMParms.getParmStr(newMiscText, "OWNER", "");
		}
	}

	@Override
	public int getPrice()
	{
		if((affected instanceof Item)&&(price<0))
			return ((Item)affected).value();
		if(price<0)
			return 0;
		return price;
	}

	@Override
	public void setPrice(final int price)
	{
		this.price=price;
	}

	@Override
	public String getOwnerName()
	{
		return owner;
	}

	@Override
	public void setOwnerName(final String owner)
	{
		if(owner==null)
			this.owner="";
		else
			this.owner=owner;
	}

	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return null;
		final Clan C=CMLib.clans().getClanExact(owner);
		if(C!=null)
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}

	@Override
	public String getTitleID()
	{
		return affected.toString();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(text().length()==0)
		{
			if(canBeUninvoked())
			{
				if((affected!=null)
				&&(affected instanceof MOB)
				&&(tickID==Tickable.TICKID_MOB))
				{
					final MOB mob=(MOB)affected;
					if((branding==null)||(mob.location()==null))
					{
						messedUp=true;
						unInvoke();
					}
					if(!mob.location().isInhabitant(branding))
					{
						messedUp=true;
						unInvoke();
					}
				}
			}
			return super.tick(ticking,tickID);
		}
		return ! this.unInvoked;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((branding!=null)&&(!aborted))
				{
					final MOB animal=branding;
					if((messedUp)||(animal==null))
						commonTell(mob,L("You've failed to brand @x1!",branding.name()));
					else
					{
						final Room room=animal.location();
						final String ownerName=CMLib.law().getLandOwnerName(room);
						if((messedUp)||(room==null)||(ownerName.length()==0))
							commonTell(mob,L("You've messed up branding @x1!",branding.name()));
						else
						{
							animal.delEffect(animal.fetchEffect("Branding"));
							final Branding bonding=(Branding)this.copyOf();
							bonding.setMiscText("OWNER=\""+ownerName+"\"");
							bonding.canBeUninvoked = false;
							animal.addNonUninvokableEffect(bonding);
							animal.setStartRoom(room);
							room.show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to brand @x1.",animal.name()));
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!super.canBeUninvoked)
		&& (affected instanceof MOB))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			{
				final MOB M=(MOB)affected;
				if((M.amFollowing()==null)
				&&(msg.target()==M)
				&&(CMLib.law().doesHavePriviledgesHere(msg.source(), M.getStartRoom())))
				{
					CMLib.commands().postFollow(M, msg.source(), false);
					return false;
				}
				break;
			}
			case CMMsg.TYP_HUH:
				break;
			case CMMsg.TYP_COMMANDFAIL:
				if((msg.targetMessage()!=null)
				&&(msg.targetMessage().length()>0)
				&&("pP".indexOf(msg.targetMessage().charAt(0))>=0))
				{
					final MOB M=(MOB)affected;
					if(M.amFollowing()==null)
					{
						final List<String> ml=CMParms.parse(msg.targetMessage());
						if((ml.size()>1)
						&&("PULL".startsWith(ml.get(0))||"PUSH".startsWith(ml.get(0))))
						{
							MOB M1=M.location().fetchInhabitant(ml.get(1));
							if((M1!=M)&&(ml.size()>1))
								M1=M.location().fetchInhabitant(CMParms.combine(ml,1));
							if((M1==M)
							&&(CMLib.law().doesHavePriviledgesHere(msg.source(), M.getStartRoom())))
							{
								CMLib.commands().postFollow(M, msg.source(), false);
								return false;
							}
						}
					}
				}
				break;
			case CMMsg.TYP_SELL:
				if((msg.tool()==affected)
				&&(affected instanceof MOB))
				{
					final Room R=msg.source().location();
					R.showHappens(CMMsg.MSG_OK_VISUAL, L("The brand on @x1 fades away.",affected.name()));
					return false;
				}
				break;
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((!super.canBeUninvoked)
		&& (affected instanceof MOB))
		{
			if((msg.source()==affected)
			&&(msg.sourceMinor()==CMMsg.TYP_NOFOLLOW)
			&&(CMLib.law().doesOwnThisLand(this.owner, msg.source().location())))
				((MOB)affected).setStartRoom(((MOB)affected).location());
			else
			if((msg.target()==affected)
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			&&(CMLib.flags().canBeSeenBy(affected,msg.source())))
			{
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,
											  CMMsg.MSG_OK_VISUAL,L("\n\r@x1 bears the @x2 brand.\n\r",affected.name(msg.source()),owner),
											  CMMsg.NO_EFFECT,null,
											  CMMsg.NO_EFFECT,null));
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("branding");
		branding=null;
		final String str=CMParms.combine(commands,0);
		final MOB M=super.getTarget(mob, commands, givenTarget, false, true);
		if(M==null)
			return false;
		branding=null;
		if(!CMLib.flags().canBeSeenBy(M,mob))
		{
			commonTell(mob,L("You don't see anyone called '@x1' here.",str));
			return false;
		}
		if((!M.isMonster())
		||(!CMLib.flags().isAnimalIntelligence(M)))
		{
			commonTell(mob,L("You can't brand @x1.",M.name(mob)));
			return false;
		}
		if(!CMLib.law().doesOwnThisLand(mob, mob.location()))
		{
			commonTell(mob,L("You can't brand @x1 here.",M.name(mob)));
			return false;
		}
		branding=M;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		messedUp=!proficiencyCheck(mob,0,auto);
		final int duration=getDuration(35,mob,branding.phyStats().level(),10);
		verb=L("branding @x1",M.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) branding @x1.",M.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
