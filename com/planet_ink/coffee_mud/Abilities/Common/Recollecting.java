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
public class Recollecting extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Recollecting";
	}

	private final static String	localizedName	= CMLib.lang().L("Recollecting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RECOLLECT", "RECOLLECTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	public Recollecting()
	{
		super();
		displayText=L("You are trying to recollect...");
		verb=L("recollecting");
	}

	protected boolean success=false;
	protected String searchFor="";

	protected final static int charLimit = 100000;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(success==false)
				{
					final StringBuffer str=new StringBuffer(L("Your recollection attempt failed.\n\r"));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
	}

	protected List<Item> getApplicableItems(final MOB mob)
	{
		final ArrayList<Item> list=new ArrayList<Item>();
		if(mob==null)
			return list;
		final Room R=mob.location();
		if(R==null)
			return list;
		for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I != null)
			&&(I.isReadable())
			&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER)
			&&(!(I instanceof Scroll)))
				list.add(I);
		}
		boolean isMyProperty = CMLib.law().doesHavePriviledgesHere(mob, R);
		for(Enumeration<Item> i=R.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I != null)
			&&(I.isReadable())
			&&(this.getBrand(I).equals(mob.Name())||isMyProperty)
			&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER)
			&&(!(I instanceof Scroll)))
				list.add(I);
		}
		return list;
	}
	
	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				final Room room=mob.location();
				if((success)
				&&(!aborted)
				&&(room!=null))
				{
					final MOB fakeMOB=CMClass.getFactoryMOB(mob.Name(), mob.phyStats().level(), mob.location());
					try
					{
						final List<String> founds=new ArrayList<String>();
						final List<Item> items=this.getApplicableItems(mob);
						long remain=charLimit;
						CMLib.dice().scramble(items);
						for(Item foundI : items)
						{
							if(remain < 0)
								break;
							if(foundI instanceof Book)
							{
								final int total=((Book)foundI).getUsedPages();
								final int[] pages = new int[total];
								for(int i=0;i<total;i++)
									pages[i]=i;
								CMLib.dice().scramble(pages);
								for(int i : pages)
								{
									String pageContent=((Book)foundI).getRawContent(i+1);
									remain -=pageContent.length();
									if(CMLib.english().containsString(pageContent, this.searchFor))
									{
										if(foundI.container()!=null)
											founds.add(L("on page @x1 of @x2 in @x3",""+(i+1),foundI.name(mob),foundI.ultimateContainer(null).name(mob)));
										else
											founds.add(L("on page @x1 or @x2",""+(i+1),foundI.name(mob)));
									}
								}
							}
							else
							{
								final CMMsg rmsg=CMClass.getMsg(fakeMOB,foundI,this,CMMsg.TYP_READ,null,"",null);
								foundI.executeMsg(foundI, rmsg);
								String tmsg="";
								if(rmsg.trailerMsgs()!=null)
								{
									for(CMMsg m2 : rmsg.trailerMsgs())
									{
										if((m2.source()==fakeMOB)
										&&(m2.target()==foundI)
										&&(m2.targetMessage().length()>0)
										&&(m2.sourceMinor()==CMMsg.TYP_WASREAD))
											tmsg+=m2.targetMessage();
									}
								}
								remain -=tmsg.length();
								if(CMLib.english().containsString(tmsg, this.searchFor))
								{
									if(foundI.container()!=null)
										founds.add(L("@x1 in @x2",foundI.name(mob),foundI.ultimateContainer(null).name(mob)));
									else
										founds.add(foundI.name(mob));
								}
							}
						}
						if(founds.size() == 0)
							commonTell(mob,L("You are pretty sure there is nothing here that mentions that."));
						else
							commonTell(mob,L("You seem to recall reading that in @x1.",CMLib.english().toEnglishStringList(founds)));
					}
					finally
					{
						fakeMOB.destroy();
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("recollecting");
		success=false;
		if(this.getApplicableItems(mob).size()==0)
		{
			commonTell(mob,L("There are no writings here!"));
			return false;
		}
		if(commands.size()==0)
		{
			commonTell(mob,L("Recollect something about what?"));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(proficiencyCheck(mob,0,auto))
			success=true;
		final int duration=getDuration(30,mob,1,1);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) trying to recollect something about the writings here."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Recollecting R=(Recollecting)beneficialAffect(mob,mob,asLevel,duration);
			if(R!=null)
				R.searchFor=CMParms.combine(commands);
		}
		return true;
	}
}
