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
   Copyright 2018-2024 Bo Zimmerman

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
public class Decorating extends CommonSkill implements RecipeDriven
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

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_VERB		= 3;
	protected static final int	RCP_DISPLAY		= 4;
	protected static final int	RCP_XLEVEL		= 5;
	protected static final int	RCP_MISC		= 6;

	protected String	mountWord		= "mounted";
	protected String	mountedPhrase	= "@x1 is mounted here.";
	protected Item		mountingI		= null;
	protected Room		mountingR		= null;
	protected boolean	messedUp		= false;
	protected boolean	blended			= true;
	protected boolean	hips			= false;

	@Override
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes(getRecipeFilename());
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\t"
		+ "ACTIVE_VERB\tDISPLAY_MASK\tXLEVEL\tDECORATION_FLAG";
	}

	@Override
	public String getRecipeFilename()
	{
		return "decorations.txt";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			if(name.equalsIgnoreCase(recipeName)
			||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
				matches.add(name);
		}
		return matches;
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RecipeDriven.RCP_FINALNAME ),
				Integer.valueOf(CMath.s_int(recipe.get( RecipeDriven.RCP_LEVEL ))));
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
						commonTelL(mob,"You've failed to "+mountWord+"!");
					else
					{
						final Room room=CMLib.map().roomLocation(I);
						final String ownerName=CMLib.law().getPropertyOwnerName(room);
						if((messedUp)
						||(room==null)
						||(ownerName.length()==0))
							commonTelL(mob,"You've messed up @x1!",verb);
						else
						{
							I.delEffect(I.fetchEffect("Decorating"));
							final Decorating mount=(Decorating)this.copyOf();
							final StringBuilder str = new StringBuilder(";");
							str.append(" BLENDED=").append(blended);
							str.append(" HIPS=").append(hips);
							str.append(" ;").append(I.displayText());
							mount.setMiscText(str.toString());
							mount.canBeUninvoked = false;
							I.addNonUninvokableEffect(mount);
							if(mountedPhrase.trim().length()>0)
								I.setDisplayText(L(mountedPhrase,I.name()));
							room.moveItemTo(I, Expire.Never);
							room.show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to "+mountWord+" @x1.",I.name()));
							room.recoverRoomStats();
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
	public void setMiscText(final String text)
	{
		super.setMiscText(text);
		blended = true;
		hips = false;
		if((text!=null)&&(text.startsWith(";")))
		{
			blended = false;
			final int x = text.indexOf(';',1);
			if(x>1)
			{
				final String parms = text.substring(1,x);
				blended = CMParms.getParmBool(parms, "BLENDED", false);
				hips = CMParms.getParmBool(parms, "HIPS", false);
			}
		}
	}

	protected String getOldDisplayText()
	{
		if(text().startsWith(";"))
		{
			final int x = text().indexOf(';',1);
			if(x<0)
				return text();
			return text().substring(x+1);
		}
		else
			return text();
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
				if(msg.tool() == null)
				{
					final Room R=CMLib.map().roomLocation(affected);
					R.show(msg.source(), affected, CMMsg.MSG_DELICATE_HANDS_ACT, L("<S-NAME> remove(s) <T-NAME> from the wall."));
					if(text().length()>0)
						affected.setDisplayText(this.getOldDisplayText());
					affected.delEffect(this);
					this.destroy();
				}
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
			if(blended)
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED);
			if(hips)
				affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_HIDDENINPLAINSIGHT);
			affectableStats.setName(affected.name()); //??
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if((auto)
		&&(commands.size()==0))
			commands.add("hang");
		if(commands.size()==0)
		{
			commonTelL(mob,"Decorate what, how?  Try DECORATE LIST.");
			return false;
		}
		final List<List<String>> recipes = CMLib.utensils().addExtRecipes(mob,ID(),fetchRecipes());
		final String word = commands.remove(0).toLowerCase();
		if(word.equals("list"))
		{
			final StringBuilder words=new StringBuilder(L("^NDecoration terms: "));
			for(final List<String> list : recipes)
			{
				final String name=list.get(RCP_FINALNAME);
				final int level=CMath.s_int(list.get(RCP_LEVEL));
				final int xlevel=CMath.s_int(list.get(RCP_XLEVEL));
				if((level <= adjustedLevel(mob,asLevel))
				&&(super.getXLEVELLevel(mob) >= xlevel))
					words.append(name).append(", ");
			}
			commonTell(mob,words.substring(0,words.length()-2)+".\n\r");
			return false;
		}
		List<String> matche = null;
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			final int level=CMath.s_int(list.get(RCP_LEVEL));
			final int xlevel=CMath.s_int(list.get(RCP_XLEVEL));
			if((level <= adjustedLevel(mob,asLevel))
			&&(super.getXLEVELLevel(mob) >= xlevel)
			&&(name.equalsIgnoreCase(word)))
				matche = list;
		}
		if(matche == null)
		{
			commonTelL(mob,"Decorate what? '@x1' is unknown. Try DECORATE LIST.",word);
			return false;
		}
		if(commands.size()==0)
		{
			commonTelL(mob,"Decorate what, how?  Try DECORATE LIST.");
			return false;
		}
		final Item I=super.getTarget(mob, null, givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if(I==null)
			return false;
		mountingI = I;
		this.mountWord = word;
		this.mountedPhrase=matche.get(RCP_DISPLAY);
		this.verb=L(matche.get(RCP_VERB),I.name());
		this.blended = true;
		this.hips = false;
		if(matche.size()>RCP_MISC)
		{
			final String misc=matche.get(RCP_MISC);
			blended = (misc.toUpperCase().indexOf("BLENDED")>=0);
			hips = (misc.toUpperCase().indexOf("HIPS")>=0);
		}

		if(!CMLib.law().doesHavePriviledgesHere(mob, mob.location()))
		{
			commonTelL(mob,"You can't decorate here.");
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
			commonTelL(mob,"You can't mount anything here.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		messedUp=!proficiencyCheck(mob,0,auto);
		final int duration=getDuration(CMath.s_int(matche.get(RCP_TICKS)),mob,I.phyStats().level(),2);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) @x1.",verb));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
