package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Flag;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.EnhancedExpertise;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class GenWrightSkill extends CraftingSkill implements ItemCraftor, MendingSkill
{
	public String	ID	= "GenWrightSkill";

	@Override
	public String ID()
	{

		return ID;
	}

	private static final Object V(final String ID, final int varNum)
	{
		if(vars.containsKey(ID))
			return vars.get(ID)[varNum];
		final Object[] O=makeEmpty();
		vars.put(ID,O);
		return O[varNum];
	}

	private static final void SV(final String ID,final int varNum,final Object O)
	{
		if(vars.containsKey(ID))
			vars.get(ID)[varNum]=O;
		else
		{
			final Object[] O2=makeEmpty();
			vars.put(ID,O2);
			O2[varNum]=O;
		}
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String name()
	{
		return (String) V(ID, V_NAME);
	}

	private static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int	V_NAME			= 0;	// S
	private static final int	V_TRIG			= 1;	// S[]
	private static final int	V_HELP			= 2;	// S
	private static final int	V_FNAM			= 3;	// S
	private static final int	V_RSCS			= 4;	// S
	private static final int	V_VERB			= 5;	// S
	private static final int	V_CNMN			= 6;	// B
	private static final int	V_CNDO			= 7;	// B
	private static final int	V_CNTI			= 8;	// B
	private static final int	V_SOND			= 9;	// S
	private static final int	V_CNDE			= 10;	// B
	private static final int	NUM_VS			= 11;	// S

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.LargeConstructions;
	}

	@Override
	public String parametersFormat()
	{
		return "ITEM_CMARE";
	}

	@Override
	public String[] triggerStrings()
	{
		return (String[]) V(ID, V_TRIG);
	}

	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="Wrighting Skill";
		O[V_TRIG]=new String[]{"WRIGHT"};
		O[V_HELP]="<ABILITY>This skill is not yet documented.";
		O[V_FNAM]="";
		O[V_RSCS]="WOODEN";
		O[V_VERB]="building";
		O[V_CNMN]=Boolean.valueOf(true);
		O[V_CNDO]=Boolean.valueOf(true);
		O[V_CNTI]=Boolean.valueOf(true);
		O[V_SOND]="sawing.wav";
		O[V_CNDE]=Boolean.valueOf(false);
		return O;
	}

	private int					doorDir			= -1;
	private String				reTitle			= null;
	private String				reDesc			= null;

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_AMOUNTMATS		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_SHIPINDEX	= 5;

	protected Item key=null;

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenWrightSkill A=this.getClass().newInstance();
			A.ID=ID;
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenCraftSkill();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if((buildingI==null)
			&&(activity != CraftingActivity.RETITLING)
			&&(activity != CraftingActivity.DOORING)
			&&(activity != CraftingActivity.DEMOLISH))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected String getTempRecipeName()
	{
		final String cmareName = (String)V(ID,V_FNAM);
		final int x=cmareName.lastIndexOf('.');
		if(x<=0)
			return cmareName+".txt";
		else
			return cmareName.subSequence(0, x)+".txt";
	}

	protected List<Item> getWrightables()
	{
		final String allItemID = ID().toUpperCase()+"_PARSED";
		@SuppressWarnings("unchecked")
		List<Item> itemPrototypes = (List<Item>)Resources.getResource(allItemID);
		if(itemPrototypes == null)
		{
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/"+(String)V(ID,V_FNAM)),null);
			if(F.exists())
			{
				final String xml = F.textUnformatted().toString().trim();
				if(xml.length()>0)
				{
					itemPrototypes=new Vector<Item>();
					CMLib.coffeeMaker().addItemsFromXML(F.textUnformatted().toString(), itemPrototypes, null);
					for(final Item I : itemPrototypes)
						CMLib.threads().deleteAllTicks(I);
					if(itemPrototypes.size()>0)
						Resources.submitResource(allItemID, itemPrototypes);
				}
			}
		}
		return itemPrototypes;
	}

	@Override
	public String supportedResourceString()
	{
		return (String) V(ID, V_RSCS);
	}

	@Override
	public String parametersFile()
	{
		return (String)V(ID,V_FNAM);
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		if(ID().equals("GenWrightSkill")||(parametersFile().length()==0))
			return new ArrayList<List<String>>();
		if((!Resources.isResource("PARSED_RECIPE: "+parametersFile()))
		||(!Resources.isResource("PARSED_RECIPE: "+getTempRecipeName())))
		{
			Resources.removeResource(ID().toUpperCase()+"_PARSED");
			final CMFile F=new CMFile(Resources.makeFileResourceName("::skills/"+getTempRecipeName()),null);
			final List<Item> ships = getWrightables();
			if(ships != null)
			{
				final StringBuilder recipes = new StringBuilder("");
				int x=0;
				for(final Item I : ships)
				{
					recipes.append(I.Name()).append("\t")
							.append(""+I.basePhyStats().level()).append("\t")
							.append(""+I.basePhyStats().weight()/10).append("\t")
							.append(""+I.basePhyStats().weight()).append("\t")
							.append(""+I.baseGoldValue()).append("\t")
							.append(""+(x++)).append("\r\n");
				}
				F.saveText(recipes.toString());
			}
			else
			if(F.exists())
				F.delete();
		}
		final List<List<String>> recipes = super.loadRecipes(getTempRecipeName());
		Resources.submitResource("PARSED_RECIPE: "+parametersFile(), Resources.getResource("PARSED_RECIPE: "+getTempRecipeName()));
		return recipes;
	}

	protected void buildDoor(Room room, final int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			//int size = CMath.s_int(recipe[DAT_MISC]);
			String closeWord=null;
			String openWord=null;
			String closedWord=null;
			final String displayText="";
			//if(closeWord == null)
				closeWord="close";
			//if(openWord == null)
				openWord="open";
			//if(closedWord == null)
				closedWord=CMLib.english().startWithAorAn("closed door");
			room=CMLib.map().getRoom(room);
			final Exit X=CMClass.getExit("GenDoor");
			X.setName(CMLib.english().startWithAorAn("a door"));
			X.setDescription("");
			X.setDisplayText(displayText);
			X.setOpenDelayTicks(9999);
			X.setExitParams("door",closeWord,openWord,closedWord);
			if(X.defaultsClosed() && X.hasADoor())
				X.setDoorsNLocks(X.hasADoor(), !X.defaultsClosed(), X.defaultsClosed(), X.hasALock(), X.hasALock(), X.defaultsLocked());
			X.recoverPhyStats();
			X.text();
			room.setRawExit(dir,X);
			if(room.rawDoors()[dir]!=null)
			{
				final Exit X2=(Exit)X.copyOf();
				X2.recoverPhyStats();
				X2.text();
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
		}
	}

	protected void doTitleTransfer(final Boardable buildingI, final MOB buyer)
	{
		final MOB shopKeeper = CMClass.getMOB("StdShopkeeper");
		try
		{
			((ShopKeeper)shopKeeper).setWhatIsSoldMask(ShopKeeper.DEAL_SHIPSELLER);
			final CMMsg msg=CMClass.getMsg(buyer,buildingI,shopKeeper,CMMsg.MSG_GET,null);
			buildingI.executeMsg(buyer, msg);
		}
		finally
		{
			shopKeeper.destroy();
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((activity == CraftingActivity.RETITLING)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on @x1.",activityRoom.displayText()));
					else
					{
						activityRoom.setDisplayText(reTitle);
						activityRoom.setDescription(reDesc);
						reTitle=null;
						reDesc=null;
					}
				}
				else
				if((activity == CraftingActivity.DOORING)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on the door in @x1.",activityRoom.displayText()));
					else
						buildDoor(activityRoom,doorDir);
				}
				else
				if((activity == CraftingActivity.DEMOLISH)
				&&(!aborted))
				{
					if((messedUp)||(mob.location()!=activityRoom))
						commonEmote(mob,L("<S-NAME> mess(es) up <S-HIS-HER> work on demolishing the door in @x1.",activityRoom.displayText()));
					else
					{
						activityRoom.setRawExit(doorDir,CMClass.getExit("Open"));
						if(activityRoom.rawDoors()[doorDir]!=null)
						{
							activityRoom.rawDoors()[doorDir].setRawExit(Directions.getOpDirectionCode(doorDir),CMClass.getExit("Open"));
							CMLib.database().DBUpdateExits(activityRoom.rawDoors()[doorDir]);
						}
						CMLib.database().DBUpdateExits(activityRoom);
					}
				}
				else
				if((buildingI!=null)
				&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.MENDING)
							messedUpCrafting(mob);
						else
						if(activity == CraftingActivity.LEARNING)
						{
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
							buildingI.destroy();
						}
						else
						{
							commonEmote(mob,L("<S-NAME> mess(es) up @x2 @x1.",buildingI.name(),(String)V(ID,V_VERB)));
							buildingI.destroy();
						}
					}
					else
					{
						if(activity == CraftingActivity.MENDING)
						{
							if((buildingI instanceof Boardable)
							&&(buildingI.usesRemaining()<95))
								buildingI.setUsesRemaining(buildingI.usesRemaining()+5);
							else
								buildingI.setUsesRemaining(100);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.MENDER, 1, this, buildingI);
						}
						else
						if(activity==CraftingActivity.LEARNING)
						{
							deconstructRecipeInto(mob, buildingI, recipeHolder );
							buildingI.destroy();
						}
						else
						{
							dropAWinner(mob,buildingI);
							CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this, buildingI);
							if(key!=null)
							{
								dropAWinner(mob,key);
								if(key instanceof Container)
									key.setContainer((Container)buildingI);
							}
							if(buildingI instanceof Boardable)
							{
								final Boardable boardableI=(Boardable)buildingI;
								MOB buyer = mob;
								if(buyer.isMonster() && (buyer.amFollowing()!=null))
									buyer = buyer.amUltimatelyFollowing();
								if(buyer.isMonster())
									((Boardable)buildingI).rename(""+CMLib.dice().roll(1, 999, 0));
								else
									doTitleTransfer(boardableI, buyer);
								if(boardableI instanceof PrivateProperty)
								{
									final PrivateProperty boardableP=(PrivateProperty)boardableI;
									if(boardableP.getOwnerName().length()>0)
									{
										final LandTitle titleI=(LandTitle)CMClass.getItem("GenTitle");
										titleI.setLandPropertyID(boardableI.Name());
										titleI.text(); // everything else is derived from the ship itself
										((Item)titleI).recoverPhyStats();
										mob.addItem((Item)titleI);
									}
								}
								if(buildingI.subjectToWearAndTear())
									buildingI.setUsesRemaining(100);
								if(boardableI.getIsDocked() != mob.location())
									boardableI.dockHere(mob.location());
								boardableI.setHomePortID(mob.location().roomID());
							}
						}
					}
				}
				buildingI=null;
				key=null;
				activity = CraftingActivity.CRAFTING;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		return mayICraft(null,I);
	}

	@Override
	public boolean mayICraft(final MOB mob, final Item I)
	{
		if(I==null)
			return false;
		if(!super.isMadeOfSupportedResource(I))
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		for(final List<String> recipe : recipes)
		{
			String finalName=recipe.get(RCP_FINALNAME);
			finalName=CMStrings.replaceAll(finalName, "%", ".*").toLowerCase();
			for(final String word : Boardable.NAME_REPL_STRINGS)
			{
				if(finalName.indexOf(word)>0)
				{
					for(final String rubs : Boardable.NAME_REPL_MARKERS)
						finalName=CMStrings.replaceAll(finalName, rubs.charAt(0)+word+rubs.charAt(1), ".*").toLowerCase();
				}
			}
			if(Pattern.matches(finalName, I.Name().toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public boolean supportsMending(final Physical item)
	{
		return canMend(null, item, true);
	}

	@Override
	protected boolean canMend(final MOB mob, final Environmental E, final boolean quiet)
	{
		final Boolean canMendB=(Boolean)V(ID,V_CNMN);
		if(!canMendB.booleanValue())
			return false;
		if(!super.canMend(mob,E,quiet))
			return false;
		if(!(E instanceof Item))
			return false;
		final Item IE=(Item)E;
		if(mayICraft(mob, IE))
			return true;
		if(!super.isMadeOfSupportedResource(IE))
		{
			if(!quiet)
			{
				commonTell(mob,L("That can't be mended with this skill."));
			}
			return false;
		}
		return true;
	}


	protected String getIdentifierCommandWord()
	{
		if((triggerStrings()==null)||(triggerStrings().length==0))
			return "wright";
		return triggerStrings()[0].toLowerCase();
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_AMOUNTMATS );
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	// lots of work to be done here
	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[] CODES={"CLASS",//0
										 "TEXT",//1
										 "NAME",//2S
										 "HELP",//3I
										 "TRIGSTR",//4S[]
										 "FILENAME",//5S
										 "MATLIST",//6S
										 "VERB",//7S
										 "CANMEND",//8S
										 "CANDOOR",//9S
										 "CANTITLE",//10S
										 "SOUND",//11S
										 "CANDESC",//12S
										};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		/*
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		*/
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		case 2:
			return (String) V(ID, V_NAME);
		case 3:
			return (String) V(ID, V_HELP);
		case 4:
			return CMParms.toListString((String[]) V(ID, V_TRIG));
		case 5:
			return (String) V(ID, V_FNAM);
		case 6:
			return ((String) V(ID, V_RSCS)).replace('|', ',');
		case 7:
			return (String) V(ID, V_VERB);
		case 8:
			return Boolean.toString(((Boolean) V(ID, V_CNMN)).booleanValue());
		case 9:
			return Boolean.toString(((Boolean) V(ID, V_CNDO)).booleanValue());
		case 10:
			return Boolean.toString(((Boolean) V(ID, V_CNTI)).booleanValue());
		case 11:
			return (String) V(ID, V_SOND);
		case 12:
			return Boolean.toString(((Boolean) V(ID, V_CNDE)).booleanValue());
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenWrightSkill";
			else
			if(code.equalsIgnoreCase("allxml"))
				return getAllXML();
			return super.getStat(code);
		}
	}

	@Override
	public void setStat(String code, final String val)
	{
		int num=0;
		int numDex=code.length();
		while((numDex>0)&&(Character.isDigit(code.charAt(numDex-1))))
			numDex--;
		if(numDex<code.length())
		{
			num=CMath.s_int(code.substring(numDex));
			code=code.substring(0,numDex);
		}
		switch(getCodeNum(code))
		{
		case 0:
			if(val.trim().length()>0)
			{
				V(ID,V_NAME); // force creation, if necc
				final Object[] O=vars.get(ID);
				vars.remove(ID);
				vars.put(val,O);
				if(num!=9)
					CMClass.delClass(CMObjectType.ABILITY,this);
				this.ID=val;
				if(num!=9)
					CMClass.addClass(CMObjectType.ABILITY,this);
			}
			break;
		case 1:
			setMiscText(val);
			break;
		case 2:
			SV(ID, V_NAME, val);
			if (ID.equalsIgnoreCase("GenWrightSkill"))
				break;
			break;
		case 3:
			SV(ID, V_HELP, val);
			break;
		case 4:
			SV(ID, V_TRIG, CMParms.parseCommas(val, true).toArray(new String[0]));
			break;
		case 5:
			SV(ID, V_FNAM, val);
			break;
		case 6:
			SV(ID, V_RSCS, val.toUpperCase().replace(',', '|'));
			break;
		case 7:
			SV(ID, V_VERB, val);
			break;
		case 8:
			SV(ID, V_CNMN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 9:
			SV(ID, V_CNDO, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 10:
			SV(ID, V_CNTI, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 11:
			SV(ID, V_SOND, val);
			break;
		case 12:
			SV(ID, V_CNDE, Boolean.valueOf(CMath.s_bool(val)));
			break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenWrightSkill"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenWrightSkill))
			return false;
		if(!((GenWrightSkill)E).ID().equals(ID))
			return false;
		if(!((GenWrightSkill)E).text().equals(text()))
			return false;
		return true;
	}

	private void parseAllXML(final String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if((V==null)||(V.size()==0))
			return;
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(getStatCodes()[c].equals("CLASS"))
				this.ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
			else
			if(!getStatCodes()[c].equals("TEXT"))
				setStat(getStatCodes()[c],CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c])));
		}
	}

	private String getAllXML()
	{
		final StringBuffer str=new StringBuffer("");
		for(int c=0;c<getStatCodes().length;c++)
		{
			if(!getStatCodes()[c].equals("TEXT"))
			{
				str.append("<"+getStatCodes()[c]+">"
						+CMLib.xml().parseOutAngleBrackets(getStat(getStatCodes()[c]))
						+"</"+getStatCodes()[c]+">");
			}
		}
		return str.toString();
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new ArrayList<CraftedItem>(0));
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 final int asLevel, final int autoGenerate, final boolean forceLevels, final List<CraftedItem> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		@SuppressWarnings("unused")
		int recipeLevel=1;
		final Boolean canMendB=(Boolean)V(ID,V_CNMN);
		final Boolean canDoorB=(Boolean)V(ID,V_CNDO);
		final Boolean canTitleB=(Boolean)V(ID,V_CNTI);
		final Boolean canDescB=(Boolean)V(ID,V_CNDE);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			final StringBuilder cmdsList = new StringBuilder("");
			cmdsList.append("@x1 what? Enter \"@x2 list\" for a list, \"@x2 info <item>\", \"@x2 scan\","
						+ " \"@x2 learn <item>\", ");
			if(canMendB.booleanValue())
				cmdsList.append("\"@x2 mend <item>\", ");
			if(canTitleB.booleanValue())
				cmdsList.append("\"@x2 title <text>\", ");
			if(canDescB.booleanValue())
				cmdsList.append("\"@x2 desc <text>\", ");
			if(canDoorB.booleanValue())
				cmdsList.append(" \"@x2 door <dir>\", \"@x2 demolish <dir>\", ");

			cmdsList.append("or \"@x2 stop\" to cancel.");

			commonTell(mob,L(cmdsList.toString(), CMStrings.capitalizeFirstLetter(getIdentifierCommandWord()),getIdentifierCommandWord()));
			return false;
		}
		final String verbing=V(ID,V_VERB).toString();
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		String startStr=null;
		int duration=4;
		bundling=false;
		helpingAbility=null;
		helping=false;
		if(str.equalsIgnoreCase("list") && (autoGenerate <= 0))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
				CMLib.lister().fixColWidth(40,mob.session()),
				CMLib.lister().fixColWidth(10,mob.session()),
				CMLib.lister().fixColWidth(10,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 Mats required\n\r",
					CMStrings.padRight(L("Item"),cols[0]),
					CMStrings.padRight(L("Level"),cols[1])));
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipeNames(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String amountMats=getComponentDescription(mob,V,RCP_AMOUNTMATS);
					if((level<=xlevel(mob))||allFlag)
					{
						buf.append(CMStrings.padRight(item,cols[0])
								+" "+CMStrings.padRight(""+level,cols[1])
								+" "+amountMats+"\n\r");
					}
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if(((commands.get(0))).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			final Vector<String> newCommands=CMParms.parse(CMParms.combine(commands,1));
			final Room R=mob.location();
			if(R.getArea() instanceof Boardable)
			{
				final Room boardR=CMLib.map().roomLocation(((Boardable)R.getArea()).getBoardableItem());
				buildingI=getTarget(mob,boardR,givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
				if(buildingI != ((Boardable)R.getArea()).getBoardableItem())
					buildingI=null;
			}
			if(buildingI==null)
				buildingI=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(!canMend(mob,buildingI,false))
				return false;
			/*
			if((buildingI instanceof SiegableItem)
			&&(mob.isPlayer()))
			{
				final double pctDamage = 100.0 - CMath.div(buildingI.usesRemaining(), 100.0);
				final int hullPointsDamage = (int)Math.round(CMath.mul(pctDamage,((SiegableItem)buildingI).getMaxHullPoints()));
				final Integer[] ipm=super.supportedResourcesMap();
				final int[] pm=new int[ipm.length];
				for(int i=0;i<ipm.length;i++)
					pm[i]=ipm[i].intValue();
				int matRequired=hullPointsDamage * 10;
				matRequired=adjustWoodRequired(matRequired,mob);
				final int[][] data=fetchFoundResourceData(mob,
														matRequired,"wood",pm,
														0,null,null,
														false,
														autoGenerate,
														null);
				if(data==null)
					return false;
				matRequired=data[0][FOUND_AMT];
				if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
					return false;
				if(autoGenerate<=0)
				{
					CMLib.materials().destroyResources(mob.location(),matRequired,
							data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
				}
			}
			*/
			activity = CraftingActivity.MENDING;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr=L("<S-NAME> start(s) mending @x1.",buildingI.name());
			displayText=L("You are mending @x1",buildingI.name());
			verb=L("mending @x1",buildingI.name());
		}
		else
		if(str.equalsIgnoreCase("help"))
		{
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=25;
			commands.remove(0);
			final MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null)
				return false;
			if(targetMOB==mob)
			{
				commonTell(mob,L("You can not do that."));
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,L("@x1 is not @x2 anything.",targetMOB.Name(),verbing));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb=L("helping @x1 with @x2",targetMOB.name(),helpingAbility.name());
			startStr=L("<S-NAME> start(s) @x1",verb);
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
			}
			return true;
		}
		else
		if(str.equalsIgnoreCase("title") && canTitleB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof Boardable))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A title must be specified."));
				return false;
			}
			if(title.length()>250)
			{
				commonTell(mob,L("That title is too long."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,20);
			for (final Room room2 : checkSet)
			{
				final Room R2=CMLib.map().getRoom(room2);
				if(R2.displayText(mob).equalsIgnoreCase(title))
				{
					commonTell(mob,L("That title has already been taken.  Choose another."));
					return false;
				}
			}
			reTitle=title;
			reDesc=R.description();
			activity = CraftingActivity.RETITLING;
			activityRoom=R;
			duration=getDuration(10,mob,mob.phyStats().level(),3);
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		if(str.equalsIgnoreCase("desc") && canDescB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof Boardable))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			if(commands.size()<2)
			{
				commonTell(mob,L("You must specify a description for it."));
				return false;
			}

			final String newDescription=CMParms.combine(commands,1);
			if(newDescription.length()==0)
			{
				commonTell(mob,L("A description must be specified."));
				return false;
			}
			reTitle=R.displayText();
			reDesc=newDescription;
			activity = CraftingActivity.RETITLING;
			activityRoom=R;
			duration=getDuration(40,mob,mob.phyStats().level(),10);
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		if(str.equalsIgnoreCase("door") && canDoorB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof Boardable))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String dirName=commands.get(commands.size()-1);
			final int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
			if(dir <0)
			{
				commonTell(mob,L("You must specify a direction in which to build the door."));
				return false;
			}
			if((dir<0)
			||(dir==Directions.UP)
			||(dir==Directions.DOWN))
			{
				commonTell(mob,L("A valid direction in which to build the door must be specified."));
				return false;
			}

			if((R.domainType()&Room.INDOORS)==0)
			{
				commonTell(mob,L("You can only build a door below decks."));
				return false;
			}

			final Room R1=R.getRoomInDir(dir);
			final Exit E1=R.getExitInDir(dir);
			if((R1==null)||(E1==null))
			{
				commonTell(mob,L("There is nowhere to build a door that way."));
				return false;
			}
			if(E1.hasADoor())
			{
				commonTell(mob,L("There is already a door that way."));
				return false;
			}

			int matsRequired=125 ;
			matsRequired=adjustWoodRequired(matsRequired,mob);
			final Integer[] ipm=super.supportedResourcesMap();
			final int[] pm=new int[ipm.length];
			for(int i=0;i<ipm.length;i++)
				pm[i]=ipm[i].intValue();
			final int[][] data=fetchFoundResourceData(mob,
													matsRequired,"material",pm,
													0,null,null,
													false,
													autoGenerate,
													null);
			if(data==null)
				return false;
			matsRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			if(autoGenerate<=0)
			{
				CMLib.materials().destroyResources(mob.location(),matsRequired,
					data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
			}

			doorDir = dir;
			activity = CraftingActivity.DOORING;
			activityRoom=R;
			duration=getDuration(25,mob,mob.phyStats().level(),10);
		}
		else
		if(str.equalsIgnoreCase("demolish") && canDoorB.booleanValue())
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			final Room R=mob.location();
			if((R==null)
			||(!CMLib.law().doesOwnThisProperty(mob,R)))
			{
				commonTell(mob,L("You are not permitted to do that here."));
				return false;
			}
			if(!(R.getArea() instanceof Boardable))
			{
				commonTell(mob,L("You don't know how to do that here."));
				return false;
			}

			final String dirName=commands.get(commands.size()-1);
			final int dir=CMLib.directions().getGoodShipDirectionCode(dirName);
			if(dir <0)
			{
				commonTell(mob,L("You must specify a direction in which to demolish a door."));
				return false;
			}
			if((dir<0)
			||(dir==Directions.UP)
			||(dir==Directions.DOWN))
			{
				commonTell(mob,L("A valid direction in which to demolish a door must be specified."));
				return false;
			}

			if((R.domainType()&Room.INDOORS)==0)
			{
				commonTell(mob,L("You can only demolish a door below decks."));
				return false;
			}

			final Room R1=R.getRoomInDir(dir);
			final Exit E1=R.getExitInDir(dir);
			if((R1==null)||(E1==null))
			{
				commonTell(mob,L("There is nowhere to demolish a door that way."));
				return false;
			}
			if(!E1.hasADoor())
			{
				commonTell(mob,L("There is not a door that way to demolish."));
				return false;
			}

			doorDir = dir;
			activity = CraftingActivity.DEMOLISH;
			activityRoom=R;
			duration=getDuration(25,mob,mob.phyStats().level(),10);
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		{
			buildingI=null;
			activity = CraftingActivity.CRAFTING;
			key=null;
			messedUp=false;
			aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
			{
				amount=CMath.s_int(commands.get(commands.size()-1));
				commands.remove(commands.size()-1);
			}
			final Integer[] ipm=super.supportedResourcesMap();
			int[] pm=new int[ipm.length];
			for(int i=0;i<ipm.length;i++)
				pm[i]=ipm[i].intValue();
			pm=checkMaterialFrom(mob,commands,pm);
			if(pm==null)
				return false;
			final String recipeName=CMParms.combine(commands,0);
			List<String> foundRecipe=null;
			final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				final List<String> V=matches.get(r);
				if(V.size()>0)
				{
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					if((autoGenerate>0)||(level<=xlevel(mob)))
					{
						foundRecipe=V;
						recipeLevel=level;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,L("You don't know how to make a '@x1'.  Try \"@x2 list\" for a list.",recipeName,getIdentifierCommandWord()));
				return false;
			}

			final String matsRequiredStr = foundRecipe.get(RCP_AMOUNTMATS);
			final int[] compData = new int[CF_TOTAL];
			final String realRecipeName=replacePercent(foundRecipe.get(RCP_FINALNAME),"");
			final List<Object> componentsFoundList=getAbilityComponents(mob, matsRequiredStr, "make "+CMLib.english().startWithAorAn(realRecipeName),autoGenerate,compData,1);
			if(componentsFoundList==null)
				return false;
			int matRequired=CMath.s_int(matsRequiredStr);
			matRequired=adjustWoodRequired(matRequired,mob);

			if(amount>matRequired)
				matRequired=amount;
			final int[][] data=fetchFoundResourceData(mob,
													matRequired,"material",pm,
													0,null,null,
													false,
													autoGenerate,
													null);
			if(data==null)
				return false;
			matRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			final MaterialLibrary.DeadResourceRecord deadMats;
			if((componentsFoundList.size() > 0)||(autoGenerate>0))
				deadMats = deadRecord;
			else
			{
				deadMats = CMLib.materials().destroyResources(mob.location(),matRequired,
						data[0][FOUND_CODE],data[0][FOUND_SUB],data[1][FOUND_CODE],data[1][FOUND_SUB]);
			}
			final MaterialLibrary.DeadResourceRecord deadComps = CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
			final int lostValue=autoGenerate>0?0:(deadMats.getLostValue() + deadComps.getLostValue());
			final String shipIndexStr = foundRecipe.get(RCP_SHIPINDEX);
			final List<Item> shipPrototypes = getWrightables();
			if(shipPrototypes != null)
			{
				if(CMath.isInteger(shipIndexStr))
				{
					final int dex=CMath.s_int(shipIndexStr);
					if((dex>=0)&&(dex<shipPrototypes.size()))
						buildingI=shipPrototypes.get(dex);
				}
				else
				{
					for(final Item I : shipPrototypes)
					{
						if(CMLib.english().containsString(I.Name(), super.replacePercent(foundRecipe.get(RCP_FINALNAME), "")))
						{
							buildingI=I;
							break;
						}
					}
				}
				//buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
			}
			if(buildingI==null)
			{
				commonTell(mob,L("There's no such thing as a @x1!!!","("+shipIndexStr)+")");
				return false;
			}
			buildingI=(Item)buildingI.copyOf();
			duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),6);
			buildingI.setMaterial(super.getBuildingMaterial(matRequired, data, compData));
			//String itemName=determineFinalName(foundRecipe.get(RCP_FINALNAME),buildingI.material(),deadMats,deadComps);
			//itemName=CMLib.english().startWithAorAn(itemName);
			//buildingI.setName(itemName);
			startStr=L("<S-NAME> start(s) @x2 @x1.",buildingI.name(),verbing);
			displayText=L("You are @x2 @x1",buildingI.name(),verbing);
			verb=L("@x2 @x1",buildingI.name(),verbing);
			playSound="saw.wav";
			//buildingI.setDisplayText(L("@x1 lies here",itemName));
			//buildingI.setDescription(itemName+". ");
			//buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
			buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)+lostValue));
			//buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
			setBrand(mob, buildingI);
			key=null;
			buildingI.recoverPhyStats();
			buildingI.text();
			buildingI.recoverPhyStats();
		}

		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb=L("bundling @x1",RawMaterial.CODES.NAME(buildingI.material()).toLowerCase());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}
		else
		if(activity == CraftingActivity.RETITLING)
		{
			messedUp=false;
			verb=L("working on @x1",mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}
		else
		if(activity == CraftingActivity.DOORING)
		{
			messedUp=false;
			final String dirName=CMLib.directions().getDirectionName(doorDir, CMLib.flags().getDirType(mob.location()));
			verb=L("working on a @x1 door in @x2",dirName,mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}
		else
		if(activity == CraftingActivity.DEMOLISH)
		{
			messedUp=false;
			final String dirName=CMLib.directions().getDirectionName(doorDir, CMLib.flags().getDirType(mob.location()));
			verb=L("working on demolishing the @x1 door in @x2",dirName,mob.location().displayText());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}

		if((autoGenerate>0)
		&& (activity != CraftingActivity.RETITLING)
		&& (activity != CraftingActivity.DOORING)
		&& (activity != CraftingActivity.DEMOLISH))
		{
			crafted.add(new CraftedItem(buildingI,null,duration));
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			buildingI=null;
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
