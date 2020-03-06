package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
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
import java.util.regex.Pattern;

/*
   Copyright 2011-2020 Bo Zimmerman

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
public class GenGatheringSkill extends GatheringSkill implements ItemCollection
{
	public String	ID	= "GenGatheringSkill";

	@Override
	public String ID()
	{
		return ID;
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

	private static final Map<String,List<Item>> items = new Hashtable<String,List<Item>>();
	private static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int	V_NAME			= 0;	// S
	private static final int	V_TRIG			= 1;	// S[]
	private static final int	V_HELP			= 2;	// S
	private static final int	V_VERB			= 3;	// S
	private static final int	V_RSCS			= 4;	// S
	private static final int	V_SOND			= 5;	// S
	private static final int	V_CNST			= 6;	// B
	private static final int	V_CNBN			= 7;	// B
	private static final int	V_BDUR			= 8;	// I
	private static final int	V_MDUR			= 9;	// I
	private static final int	V_FDUR			= 10;	// I
	private static final int	V_RMSK			= 11;	// S
	private static final int	V_PMSK			= 12;	// S
	private static final int	V_YILD			= 13;	// S
	private static final int	V_MSG1			= 14;	// S
	private static final int	V_MSG2			= 15;	// S
	private static final int	V_MSG3			= 16;	// S
	private static final int	V_MSG4			= 17;	// S
	private static final int	V_IXML			= 18;	// S
	private static final int	V_COSM			= 19;	// B
	private static final int	NUM_VS			= 20;	// S

	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="Gathering Skill";
		O[V_TRIG]=new String[]{"GATHER"};
		O[V_HELP]="<ABILITY>This skill is not yet documented.";
		O[V_VERB]="gathering";
		O[V_SOND]="sawing.wav";
		O[V_RSCS]="WOODEN";
		O[V_CNST]=Boolean.valueOf(false);
		O[V_CNBN]=Boolean.valueOf(true);
		O[V_BDUR]=Integer.valueOf(40);
		O[V_MDUR]=Integer.valueOf(12);
		O[V_FDUR]=Integer.valueOf(6);
		O[V_RMSK]="";
		O[V_PMSK]="";
		O[V_YILD]="(1?5)+3";
		O[V_MSG1]="<S-NAME> start(s) trying to do something.";
		O[V_MSG2]="<S-NAME> find(s) @x1.";
		O[V_MSG3]="You don't find anything useful to gather here.";
		O[V_MSG4]="<S-NAME> manage(s) to gather @x1 @x2.";
		O[V_IXML]="";
		O[V_COSM]=Boolean.valueOf(false);
		return O;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	public String[] triggerStrings()
	{
		return (String[]) V(ID, V_TRIG);
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return ((Boolean) V(ID, V_CNST)).booleanValue();
	}

	@Override
	public String supportedResourceString()
	{
		return (String) V(ID, V_RSCS);
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

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public GenGatheringSkill()
	{
		super();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenGatheringSkill A=this.getClass().newInstance();
			A.ID=ID;
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenGatheringSkill();
	}

	@Override
	protected void cloneFix(final Ability E)
	{
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
										 "MATLIST",//5S
										 "VERB",//6S
										 "SOUND",//7S
										 "CANSIT",//8B
										 "CANBUNDLE",//9B
										 "ROOMMASK", //10S
										 "PLAYMASK", //11S
										 "YIELDFORMULA",//12S
										 "MSGSTART",//13S
										 "MSGFOUND",//14S
										 "MSGNOTFOUND",//15S
										 "MSGCOMPLETE",//16S
										 "ITEMXML",//17S
										 "ISCOSMETIC",//18B
										 "MINDUR",//19I
										 "BASEDUR",//20I
										 "FINDTICK",//21I
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
			return ((String) V(ID, V_RSCS)).replace('|', ',');
		case 6:
			return (String) V(ID, V_VERB);
		case 7:
			return (String) V(ID, V_SOND);
		case 8:
			return Boolean.toString(((Boolean) V(ID, V_CNST)).booleanValue());
		case 9:
			return Boolean.toString(((Boolean) V(ID, V_CNBN)).booleanValue());
		case 10:
			return (String) V(ID, V_RMSK);
		case 11:
			return (String) V(ID, V_PMSK);
		case 12:
			return (String) V(ID, V_YILD);
		case 13:
			return (String) V(ID, V_MSG1);
		case 14:
			return (String) V(ID, V_MSG2);
		case 15:
			return (String) V(ID, V_MSG3);
		case 16:
			return (String) V(ID, V_MSG4);
		case 17:
			return (String) V(ID, V_IXML);
		case 18:
			return Boolean.toString(((Boolean) V(ID, V_COSM)).booleanValue());
		case 19:
			return ((Integer)V(ID, V_MDUR)).toString();
		case 20:
			return ((Integer)V(ID, V_BDUR)).toString();
		case 21:
			return ((Integer)V(ID, V_FDUR)).toString();
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenGatheringSkill";
			else
			if(code.equalsIgnoreCase("allxml"))
				return getAllXML();
			break;
		}
		return "";
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
				items.remove(ID);
				vars.put(val,O);
				if(num!=9)
					CMClass.delClass(CMObjectType.ABILITY,this);
				ID=val;
				if(num!=9)
					CMClass.addClass(CMObjectType.ABILITY,this);
			}
			break;
		case 1:
			setMiscText(val);
			break;
		case 2:
			SV(ID, V_NAME, val);
			if (ID.equalsIgnoreCase("GenGatheringSkill"))
				break;
			break;
		case 3:
			SV(ID, V_HELP, val);
			break;
		case 4:
			SV(ID, V_TRIG, CMParms.parseCommas(val, true).toArray(new String[0]));
			break;
		case 5:
			SV(ID, V_RSCS, val.toUpperCase().replace(',', '|'));
			break;
		case 6:
			SV(ID, V_VERB, val);
			break;
		case 7:
			SV(ID, V_SOND, val);
			break;
		case 8:
			SV(ID, V_CNST, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 9:
			SV(ID, V_CNBN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 10:
			SV(ID, V_RMSK, val);
			break;
		case 11:
			SV(ID, V_PMSK, val);
			break;
		case 12:
			SV(ID, V_YILD, val);
			break;
		case 13:
			SV(ID, V_MSG1, val);
			break;
		case 14:
			SV(ID, V_MSG2, val);
			break;
		case 15:
			SV(ID, V_MSG3, val);
			break;
		case 16:
			SV(ID, V_MSG4, val);
			break;
		case 17:
		{
			items.remove(ID);
			String xml=val;
			final String start=(val.length()<10)?"":val.substring(0, 10).toUpperCase().trim();
			if((!start.startsWith("<ITEMS>")) && (!start.startsWith("<ITEM>")))
			{
				CMFile F=new CMFile(val, null);
				if(!F.exists())
					F=new CMFile(Resources.makeFileResourceName(val), null);
				if(!F.exists())
					xml = "";
				else
				{
					xml=F.textUnformatted().toString();
					if(val.length()<=10)
						xml="";
				}
			}
			SV(ID, V_IXML, xml);
			break;
		}
		case 18:
			SV(ID, V_COSM, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 19:
			if(CMath.s_int(val)>0)
				SV(ID, V_MDUR, Integer.valueOf(CMath.s_int(val)));
			break;
		case 20:
			if(CMath.s_int(val)>=((Integer)V(ID, V_MDUR)).intValue())
				SV(ID, V_BDUR, Integer.valueOf(CMath.s_int(val)));
			break;
		case 21:
			if(CMath.s_int(val)<=((Integer)V(ID, V_MDUR)).intValue())
				SV(ID, V_FDUR, Integer.valueOf(CMath.s_int(val)));
			break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID.equalsIgnoreCase("GenGatheringSkill"))
				parseAllXML(val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenGatheringSkill))
			return false;
		if(!((GenGatheringSkill)E).ID().equals(ID))
			return false;
		if(!((GenGatheringSkill)E).text().equals(text()))
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
				ID=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(V, getStatCodes()[c]));
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

	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(((Integer) V(ID, V_BDUR)).intValue(),mob,level,((Integer) V(ID, V_MDUR)).intValue());
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==((Integer) V(ID, V_FDUR)).intValue())
			{
				if(found!=null)
				{
					commonTell(mob,L((String) V(ID, V_MSG2),foundShortName));
					if(!((Boolean) V(ID, V_COSM)).booleanValue())
					{
						displayText=L("You are @x1 @x2",(String)V(ID, V_VERB),foundShortName);
						final String plural = CMLib.english().makePlural(CMLib.english().removeArticleLead(foundShortName));
						verb=L("@x1 @x2",(String)V(ID, V_VERB),plural);
					}
					playSound=(String)V(ID, V_SOND);
				}
				else
				{
					final StringBuffer str=new StringBuffer(L((String) V(ID, V_MSG3)+"\n\r"));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
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
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					final String yieldFormula = (String)V(ID, V_YILD);
					final int skillYield = CMath.s_parseIntExpression(yieldFormula);
					if(skillYield > 0)
					{
						final int yield = super.adjustYieldBasedOnRoomSpam(skillYield*(baseYield()+abilityCode()), mob.location());
						msg.setValue(yield);
						if(mob.location().okMessage(mob, msg))
						{
							final String plural = (msg.value()==1)
									? CMLib.english().removeArticleLead(foundShortName)
									: CMLib.english().makePlural(CMLib.english().removeArticleLead(foundShortName));
							msg.modify(L((String)V(ID, V_MSG4),""+msg.value(),plural));
							mob.location().send(mob, msg);
							if(!((Boolean) V(ID, V_COSM)).booleanValue())
							{
								for(int i=0;i<msg.value();i++)
								{
									final Item newFound=(Item)found.copyOf();
									if(!dropAWinner(mob,newFound))
									{
										break;
									}
								}
							}
						}
					}
					else
					{
						msg.modify(L((String)V(ID, V_MSG4),""+msg.value()));
						mob.location().send(mob, msg);
					}
				}
			}
		}
		super.unInvoke();
	}

	protected List<Item> getItems()
	{
		List<Item> choices = items.get(ID);
		if(choices == null)
		{
			choices=new Vector<Item>(1);
			final String origXml=((String)V(ID,V_IXML)).trim();
			final String xml=origXml;
			if(xml.length()>0)
			{
				String error="";
				String start;
				if(xml.length()>10)
					start=xml.substring(0,10).toUpperCase();
				else
					start=xml.toUpperCase();
				if(start.startsWith("<ITEMS>"))
					error=CMLib.coffeeMaker().addItemsFromXML(xml,choices,null);
				else
				if(start.startsWith("<ITEM>"))
				{
					final Item I=CMLib.coffeeMaker().getItemFromXML(xml);
					if(I==null)
						error="Gathering skill "+ID+" had bad data: "+xml;
					else
					{
						CMLib.threads().deleteAllTicks(I);
						choices.add(I);
					}
				}
				else
					error="Gathering skill "+ID+" had bad data: "+xml;
				if(error.length()>0)
				{
					Log.errOut(ID, error);
				}
			}
			items.put(ID, choices);
		}
		return choices;
	}

	protected void rebuildItemXML()
	{
		final StringBuilder str=new StringBuilder("<ITEMS>");
		for(final Item I : getItems())
			str.append(CMLib.coffeeMaker().getItemXML(I));
		str.append("</ITEMS>");
		SV(ID,V_IXML,str.toString());
	}

	@Override
	public void addItem(final Item item)
	{
		getItems().add(item);
		rebuildItemXML();
	}

	@Override
	public void delItem(final Item item)
	{
		getItems().remove(item);
		rebuildItemXML();
	}

	@Override
	public void delAllItems(final boolean destroy)
	{
		getItems().clear();
		rebuildItemXML();
	}

	@Override
	public int numItems()
	{
		return getItems().size();
	}

	@Override
	public Item getItem(final int i)
	{
		final List<Item> items = getItems();
		if((items.size()==0)||(i>=items.size())||(i<0))
			return null;
		return items.get(i);
	}

	@Override
	public Item getRandomItem()
	{
		if(numItems()==0)
			return null;
		return getItem(CMLib.dice().roll(1, numItems(), -1));
	}

	@Override
	public Enumeration<Item> items()
	{
		return new IteratorEnumeration<Item>(getItems().iterator());
	}

	@Override
	public Item findItem(final Item goodLocation, final String itemID)
	{
		Item I=CMLib.english().fetchAvailableItem(getItems(), itemID, goodLocation, Wearable.FILTER_ANY, true);
		if(I==null)
			I=CMLib.english().fetchAvailableItem(getItems(), itemID, goodLocation, Wearable.FILTER_ANY, false);
		return I;
	}

	@Override
	public Item findItem(final String itemID)
	{
		return findItem(null,itemID);
	}

	@Override
	public List<Item> findItems(final Item goodLocation, final String itemID)
	{
		List<Item> some=CMLib.english().fetchAvailableItems(getItems(), itemID, goodLocation, Wearable.FILTER_ANY, true);
		if((some==null)||(some.size()==0))
			some=CMLib.english().fetchAvailableItems(getItems(), itemID, goodLocation, Wearable.FILTER_ANY, false);
		return some;
	}

	@Override
	public List<Item> findItems(final String itemID)
	{
		return findItems(null,itemID);
	}

	@Override
	public boolean isContent(final Item item)
	{
		for(final Item I : getItems())
		{
			if(I.sameAs(item))
				return true;
		}
		return false;
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
		if(applier != null)
		{
			for(final Item I : getItems())
				applier.apply(I);
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((Boolean) V(ID, V_CNBN)).booleanValue()
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		verb=(String) V(ID, V_VERB);
		playSound=null;
		found=null;
		foundShortName="";

		final String playerMask = (String) V(ID, V_PMSK);
		if((playerMask.length()>0)
		&&(!auto)
		&&(!CMLib.masking().maskCheck(playerMask, mob, true)))
		{
			mob.tell(L("To do this, you must meet these requirements:  @x1.",CMLib.masking().maskDesc(playerMask, false)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final String roomMask = (String) V(ID, V_RMSK);
		if((roomMask.length()==0)
		||(CMLib.masking().maskCheck(roomMask, mob.location(), true)))
		{
			if(proficiencyCheck(mob,0,auto))
			{
				final List<Item> choices = getItems();
				if(choices.size()>0)
				{
					found=choices.get(CMLib.dice().roll(1, choices.size(), -1));
					foundShortName=found.name();
				}
				else
				if(((Boolean) V(ID, V_COSM)).booleanValue())
				{
					found=CMClass.getBasicItem("StdItem");
					foundShortName=L("Something");
					found.setName(foundShortName);
				}
			}
		}
		final int duration=getDuration(mob,mob.basePhyStats().level());
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L((String)V(ID, V_MSG1)));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
