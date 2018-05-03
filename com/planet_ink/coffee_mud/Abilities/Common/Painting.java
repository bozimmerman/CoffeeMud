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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class Painting extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Painting";
	}

	private final static String	localizedName	= CMLib.lang().L("Painting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PAINT", "PAINTING" });

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

	protected Item		building	= null;
	protected boolean	messedUp	= false;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,L("<S-NAME> mess(es) up painting @x1.",building.name()));
					else
						mob.location().addItem(building,ItemPossessor.Expire.Player_Drop);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, final int asLevel)
	{
		final List<String> originalCommands = new XVector<String>(commands);
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()==0)
		{
			commonTell(mob,L("Paint on what? Enter \"paint [canvas name]\" or paint \"wall\"."));
			return false;
		}
		String paintingKeyWords=null;
		String paintingDesc=null;
		while(commands.size()>1)
		{
			final String last=(commands.get(commands.size()-1));
			if(last.startsWith("PAINTINGKEYWORDS="))
			{
				paintingKeyWords=last.substring(17).trim();
				if(paintingKeyWords.length()>0)
					commands.remove(commands.size()-1);
				else
					paintingKeyWords=null;
			}
			else
			if(last.startsWith("PAINTINGDESC="))
			{
				paintingDesc=last.substring(13).trim();
				if(paintingDesc.length()>0)
					commands.remove(commands.size()-1);
				else
					paintingDesc=null;
			}
			else
				break;
		}

		final String str=CMParms.combine(commands,0);
		building=null;
		messedUp=false;
		Session S=mob.session();
		if((S==null)&&(mob.amFollowing()!=null))
			S=mob.amFollowing().session();
		if(S==null)
		{
			commonTell(mob,L("I can't work! I need a player to follow!"));
			return false;
		}

		Item canvasI=null;
		if(str.equalsIgnoreCase("wall"))
		{
			if(!CMLib.law().doesOwnThisProperty(mob,mob.location()))
			{
				commonTell(mob,L("You need the owners permission to paint the walls here."));
				return false;
			}
		}
		else
		{
			canvasI=mob.location().findItem(null,str);
			if((canvasI==null)||(!CMLib.flags().canBeSeenBy(canvasI,mob)))
			{
				commonTell(mob,L("You don't see any canvases called '@x1' sitting here.",str));
				return false;
			}
			if((canvasI.material()!=RawMaterial.RESOURCE_COTTON)
			&&(canvasI.material()!=RawMaterial.RESOURCE_SILK)
			&&(!canvasI.Name().toUpperCase().endsWith("CANVAS"))
			&&(!canvasI.Name().toUpperCase().endsWith("SILKSCREEN")))
			{
				commonTell(mob,L("You cannot paint on '@x1'.",str));
				return false;
			}
		}

		int duration=25;
		final Session session=mob.session();
		final Ability me=this;
		final Physical target=givenTarget;
		if(str.equalsIgnoreCase("wall"))
		{
			if((paintingKeyWords!=null)&&(paintingDesc!=null))
			{
				building=CMClass.getItem("GenWallpaper");
				building.setName(paintingKeyWords);
				building.setDescription(paintingDesc);
				building.setSecretIdentity(getBrand(mob));
			}
			else
			{
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("Enter the key words (not the description) for this work.\n\r: "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String name=input.trim();
						if(name.length()==0)
							return;
						final Vector<String> V=CMParms.parse(name.toUpperCase());
						for(int v=0;v<V.size();v++)
						{
							final String vstr=" "+(V.elementAt(v))+" ";
							for(int i=0;i<mob.location().numItems();i++)
							{
								final Item I=mob.location().getItem(i);
								if((I!=null)
								&&(I.displayText().length()==0)
								&&(!CMLib.flags().isGettable(I))
								&&((" "+I.name().toUpperCase()+" ").indexOf(vstr)>=0))
								{
									final Item dupI=I;
									final String dupWord=vstr.trim().toLowerCase();
									session.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
									{
										@Override
										public void showPrompt()
										{
											session.promptPrint(L("\n\r'@x1' already shares one of these key words ('@x2').  Would you like to destroy it (y/N)? ", dupI.name(), dupWord));
										}

										@Override
										public void timedOut()
										{
										}

										@Override
										public void callBack()
										{
											if(this.input.equals("Y"))
											{
												dupI.destroy();
											}
										}
									});
									return;
								}
							}
						}
						session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("\n\rEnter a description for this.\n\r:"));
							}

							@Override
							public void timedOut()
							{
							}

							@Override
							public void callBack()
							{
								final String desc=this.input.trim();
								if(desc.length()==0)
									return;
								session.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
								{
									@Override
									public void showPrompt()
									{
										session.promptPrint(L("Wall art key words: '@x1', description: '@x2'.  Correct (Y/n)? ", name, desc));
									}

									@Override
									public void timedOut()
									{
									}

									@Override
									public void callBack()
									{
										if(this.input.equals("Y"))
										{
											final Vector<String> newCommands=new XVector<String>(originalCommands);
											newCommands.add("PAINTINGKEYWORDS="+name);
											newCommands.add("PAINTINGDESC="+desc);
											me.invoke(mob, newCommands, target, auto, asLevel);
										}
									}
								});
							}
						});
					}
				});
				return true;
			}
		}
		else
		if(canvasI!=null)
		{
			if((paintingKeyWords!=null)&&(paintingDesc!=null))
			{
				building=CMClass.getItem("GenItem");
				building.setName(L("a painting of @x1",paintingKeyWords));
				building.setDisplayText(L("a painting of @x1 is here.",paintingKeyWords));
				building.setDescription(paintingDesc);
				building.basePhyStats().setWeight(canvasI.basePhyStats().weight());
				building.setBaseValue(canvasI.baseGoldValue()*(CMLib.dice().roll(1,5,0)));
				building.setMaterial(canvasI.material());
				building.basePhyStats().setLevel(canvasI.basePhyStats().level());
				building.setSecretIdentity(getBrand(mob));
				canvasI.destroy();
			}
			else
			{
				session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
				{
					@Override
					public void showPrompt()
					{
						session.promptPrint(L("\n\rIn brief, what is this a painting of?\n\r: "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String name=this.input.trim();
						if(name.length()==0)
							return;
						session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
						{
							@Override
							public void showPrompt()
							{
								session.promptPrint(L("\n\rPlease describe this painting.\n\r: "));
							}

							@Override
							public void timedOut()
							{
							}

							@Override
							public void callBack()
							{
								final String desc=this.input.trim();
								if(desc.length()==0)
									return;
								final Vector<String> newCommands=new XVector<String>(originalCommands);
								newCommands.add("PAINTINGKEYWORDS="+name);
								newCommands.add("PAINTINGDESC="+desc);
								me.invoke(mob, newCommands, target, auto, asLevel);
							}
						});
					}
				});
				return true;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			building.destroy();
			building=null;
			return false;
		}

		final String startStr=L("<S-NAME> start(s) painting @x1.",building.name());
		displayText=L("You are painting @x1",building.name());
		verb=L("painting @x1",building.name());
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		duration=getDuration(25,mob,1,2);

		final CMMsg msg=CMClass.getMsg(mob,building,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
