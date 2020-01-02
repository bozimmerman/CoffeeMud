package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
public class Decorating extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Decorating";
	}

	private final static String	localizedName	= CMLib.lang().L("Decorating");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DECORATE", "DECORATING"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	public Decorating()
	{
		super();
		displayText=L("You are decorating...");
		verb=L("mounting");
	}

	protected String	mountWord	= "mounted";
	protected Item		mountingI	= null;
	protected Room		mountingR	= null;
	protected boolean	messedUp	= false;

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
					if((mountingI==null)||(mob.location()==null))
					{
						messedUp=true;
						unInvoke();
					}
					if(!mob.isContent(mountingI))
					{
						messedUp=true;
						unInvoke();
					}
					else
					if((tickUp%4)==2)
					{
						switch(CMLib.dice().roll(1, 10, 0))
						{
						case 1:
							mob.tell(L("Hmmm, no, you think it might look better over there."));
							break;
						case 2:
							mob.tell(L("Oh, wait, it's crooked now."));
							break;
						case 3:
							mob.tell(L("Actually, it would look better over here."));
							break;
						case 4:
							mob.tell(L("Now where did you put those hooks?"));
							break;
						case 5:
							mob.tell(L("You can't quite reach up there."));
							break;
						case 6:
							mob.tell(L("No, you don't think the light is not good over here."));
							break;
						case 7:
							mob.tell(L("You've almost got it mounted perfectly now."));
							break;
						case 8:
							mob.tell(L("No, it clashes with everything over here."));
							break;
						case 9:
							break;
						case 10:
							break;
						}
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
				if((mountingI!=null)&&(!aborted))
				{
					final Item I=mountingI;
					if((messedUp)||(I==null))
						commonTell(mob,L("You've failed to "+mountWord+"!"));
					else
					{
						final Room room=CMLib.map().roomLocation(I);
						final String ownerName=CMLib.law().getLandOwnerName(room);
						if((messedUp)||(room==null)||(ownerName.length()==0))
							commonTell(mob,L("You've messed up "+mountWord+"ing @x1!",I.name()));
						else
						{
							I.delEffect(I.fetchEffect("Decorating"));
							final Decorating mount=(Decorating)this.copyOf();
							mount.setMiscText(I.displayText());
							mount.canBeUninvoked = false;
							I.addNonUninvokableEffect(mount);
							if(mountWord.equals("mount"))
								I.setDisplayText(I.name()+" is mounted here.");
							else
								I.setDisplayText(I.name()+" is hanging here.");
							room.moveItemTo(I, Expire.Never);
							room.show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to "+mountWord+" @x1.",I.name()));
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
		&& (affected instanceof Item)
		&& (msg.target()==affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_SELL:
			{
				msg.source().tell(L("You can't do that to @x1 while it's mounted.",affected.name(msg.source())));
				return false;
			}
			case CMMsg.TYP_GET:
				if(CMath.bset(msg.targetMajor(), CMMsg.MASK_OPTIMIZE))
					return false;
				break;
			case CMMsg.TYP_COMMANDFAIL:
				// consider remove command some day
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
		if((!super.canBeUninvoked)
		&& (affected instanceof Item)
		&& (msg.target()==affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			{
				final Room R=CMLib.map().roomLocation(affected);
				R.show(msg.source(), affected, CMMsg.MSG_DELICATE_HANDS_ACT, L("<S-NAME> remove(s) <T-NAME> from the wall."));
				if(text().length()>0)
					affected.setDisplayText(text());
				affected.delEffect(this);
				this.destroy();
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Item)
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED);
			affectableStats.setName(affected.name());
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if((auto)&&(commands.size()==0))
			commands.add("hang");
		if((commands.size()==0)
		||((!commands.get(0).equalsIgnoreCase("hang"))
			&&(!commands.get(0).equalsIgnoreCase("mount"))
			&&(!commands.get(0).equalsIgnoreCase("stick"))))
		{
			mob.tell(L("Decorate what, how?  Try decorate HANG [item name], or decorate MOUNT [item name], or decorate STICK [item name]."));
			return false;
		}
		final String word = commands.remove(0).toLowerCase();
		final Item I=super.getTarget(mob, null, givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if(I==null)
			return false;
		mountingI = I;
		this.mountWord = word;
		if(word.equals("hang"))
			verb=L("hanging @x1",I.name());
		else
		if(word.equals("mount"))
			verb=L("mounting @x1",I.name());
		else
			verb=L("stickup @x1 up",I.name());

		if(!CMLib.law().doesHavePriviledgesHere(mob, mob.location()))
		{
			commonTell(mob,L("You can't decorate here."));
			return false;
		}

		switch(mob.location().domainType())
		{
		case Room.DOMAIN_INDOORS_CAVE:
		case Room.DOMAIN_INDOORS_METAL:
		case Room.DOMAIN_INDOORS_STONE:
		case Room.DOMAIN_INDOORS_WOOD:
			break;
		default:
		{
			commonTell(mob,L("You can't mount anything here."));
			return false;
		}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		messedUp=!proficiencyCheck(mob,0,auto);
		final int duration=getDuration(15,mob,I.phyStats().level(),2);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) "+verb+".",I.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
