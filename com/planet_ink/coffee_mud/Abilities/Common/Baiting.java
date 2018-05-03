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
   Copyright 2017-2018 Bo Zimmerman

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
public class Baiting extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Baiting";
	}

	private final static String localizedName = CMLib.lang().L("Baiting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] { "BAIT", "BAITING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "VEGETATION|COTTON|HEMP|WOODEN";
	}

	protected int		foundCode		= -1;
	protected Room		fishRoom		= null;
	protected String	foundShortName	= "";

	public Baiting()
	{
		super();
		displayText=L("You are baiting...");
		verb=L("baiting");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(45,mob,level,15);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof Room)
		{
			if(foundCode>0)
				((Room)affected).setResource(foundCode);
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		final boolean isaborted=aborted;
		final Environmental aff=affected;
		super.unInvoke();
		if((canBeUninvoked)
		&&(aff instanceof MOB)
		&&(fishRoom!=null))
		{
			if(foundCode<0)
				commonTell((MOB)aff,L("Your @x1 baiting has failed.\n\r",foundShortName));
			else
			if((foundCode>0)&&(!isaborted))
			{
				fishRoom.showHappens(CMMsg.MSG_OK_VISUAL,L("Some @x1 can be seen swimming around here.",foundShortName));
				if(((MOB)aff).location()!=fishRoom)
					((MOB)aff).location().showHappens(CMMsg.MSG_OK_VISUAL,L("Some @x1 can be seen swimming around out there.",foundShortName));
				fishRoom.setResource(foundCode);
				final Baiting F=((Baiting)copyOf());
				F.unInvoked=false;
				F.tickUp=0;
				F.tickDown=50;
				F.startTickDown(invoker,fishRoom,50);
			}
		}
	}

	public boolean isPotentialCrop(Room R, int code)
	{
		if(R==null)
			return false;
		if(R.resourceChoices()==null)
			return false;
		for(int i=0;i<R.resourceChoices().size();i++)
		{
			if(R.resourceChoices().get(i).intValue()==code)
				return true;
		}
		return false;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(super.checkStop(mob, commands) || (R==null))
			return true;
		
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("list")))
		{
			final StringBuilder str=new StringBuilder("Types of fishes:\n\r");
			for(int fishCode : RawMaterial.CODES.FISHES())
				str.append(RawMaterial.CODES.NAME(fishCode)).append("\n\r");
			mob.tell(str.toString());
			return false;
		}
		
		verb=L("baiting");
		fishRoom=null;
		if(!auto)
		{
			if(CMLib.flags().isWateryRoom(R))
				fishRoom=R;
			else
			if((R.getArea() instanceof BoardableShip)
			&&((R.domainType()&Room.INDOORS)==0))
				fishRoom=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
			
			if((fishRoom==null)||(!CMLib.flags().isWateryRoom(fishRoom)))
			{
				this.commonTell(mob, L("You need to be on the water, or in a boat to use this skill."));
				return false;
			}
		}
		if(fishRoom==null)
			fishRoom=R;
		if(fishRoom.fetchEffect(ID())!=null)
		{
			commonTell(mob,L("It looks like bait has already been dropped here."));
			return false;
		}
		
		Item mine=null;
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((I instanceof RawMaterial)
			&&(I.container()==null)
			&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH))
			{
				mine = I;
				break;
			}
		}
		if(mob.isMonster()
		&&(!auto)
		&&(!CMLib.flags().isAnimalIntelligence(mob))
		&&(commands.size()==0))
		{
			commands.add(RawMaterial.CODES.NAME(CMLib.dice().pick(RawMaterial.CODES.FISHES())));
			if(mine == null)
				mob.addItem(CMLib.materials().makeItemResource(RawMaterial.CODES.MOST_FREQUENT(RawMaterial.MATERIAL_FLESH)));
		}
		else
		if(commands.size()==0)
		{
			commonTell(mob,L("Bait for what kind of fish?"));
			return false;
		}
		int code=-1;
		final String what=CMParms.combine(commands,0).toUpperCase();
		final RawMaterial.CODES codes = RawMaterial.CODES.instance();
		for(final int cd : codes.all())
		{
			final String str=codes.name(cd).toUpperCase();
			if((str.equals(what))
			&&(CMParms.contains(codes.fishes(), cd)))
			{
				code=cd;
				foundShortName=CMStrings.capitalizeAndLower(str);
				break;
			}
		}
		if(code<0)
		{
			for(final int cd : codes.all())
			{
				final String str=codes.name(cd).toUpperCase();
				if((str.toUpperCase().startsWith(what)||(what.startsWith(str)))
				&&(CMParms.contains(codes.fishes(), cd)))
				{
					code=cd;
					foundShortName=CMStrings.capitalizeAndLower(str);
					break;
				}
			}
		}
		if(code<0)
		{
			commonTell(mob,L("You've never heard of a fish called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		if(mine==null)
		{
			commonTell(mob,L("You'll need to have some @x1 on the ground first if you want to use it as bait.",foundShortName));
			return false;
		}
		final String mineName=mine.name();
		mine=(Item)CMLib.materials().unbundle(mine,-1,null);
		if(mine==null)
		{
			commonTell(mob,L("'@x1' is not suitable for use as bait.",mineName));
			return false;
		}
		if(!(isPotentialCrop(fishRoom,code)))
		{
			commonTell(mob,L("'@x1' does not seem to be of any use as bait here.",mineName));
			return false;
		}

		foundCode=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((proficiencyCheck(mob,0,auto))&&(isPotentialCrop(fishRoom,code)))
			foundCode=code;

		mine.destroy();
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,fishRoom,this,getActivityMessageType(),L("<S-NAME> start(s) baiting @x1.",foundShortName));
		verb=L("baiting @x1",foundShortName);
		displayText=L("You are baiting @x1",foundShortName);
		if(R.okMessage(mob,msg) && fishRoom.okMessage(mob, msg))
		{
			R.send(mob,msg);
			fishRoom.sendOthers(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
