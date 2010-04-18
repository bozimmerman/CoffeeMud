package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class CoffeeMaker extends StdLibrary implements GenericBuilder
{
    public String ID(){return "CoffeeMaker";}
    public Hashtable GENMOBCODESHASH=new Hashtable();
    public Hashtable GENITEMCODESHASH=new Hashtable();

	public boolean get(int x, int m)
	{
		return (x&m)==m;
	}

	public String getGenMOBTextUnpacked(MOB mob, String newText)
	{
		if((newText!=null)&&((newText.length()>10)||newText.startsWith("%DBID>")))
		{
			if(newText.startsWith("%DBID>"))
			{
				String dbstr=CMLib.database().DBReadRoomMOBData(newText.substring(6,newText.indexOf("@")),
																  ((Object)mob).getClass().getName()+newText.substring(newText.indexOf("@")).trim());
				if(dbstr!=null)
					return dbstr;
				Log.errOut("Unable to re-read mob data: "+newText);
				return null;
			}
			return newText;
		}
		return null;
	}

	public void resetGenMOB(MOB mob, String newText)
	{
		newText=getGenMOBTextUnpacked(mob,newText);
		if(newText!=null)
			setPropertiesStr(mob,newText,false);

		mob.recoverEnvStats();
		mob.recoverCharStats();
		mob.baseState().setHitPoints(CMLib.dice().rollHP(mob.baseEnvStats().level(),mob.baseEnvStats().ability()));
		mob.baseState().setMana(CMLib.leveler().getLevelMana(mob));
		mob.baseState().setMovement(CMLib.leveler().getLevelMove(mob));
		mob.recoverMaxState();
		mob.resetToMaxState();
		if(mob.getWimpHitPoint()>0)
			mob.setWimpHitPoint((int)Math.round(CMath.mul(mob.curState().getHitPoints(),.10)));
		mob.setExperience(CMLib.leveler().getLevelExperience(mob.envStats().level()-1)+500);
	}

	public int envFlags(Environmental E)
	{
		int f=0;
		if(E instanceof Item)
		{
			Item item=(Item)E;
			if(!CMath.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP))
				f=f|1;
			if(!CMath.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOTGET))
				f=f|2;
			if(CMath.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE))
				f=f|4;
			if(!CMath.bset(item.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE))
				f=f|8;
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;

			if(container.hasALid())
				f=f|32;
			if(container.hasALock())
				f=f|64;
			// defaultsclosed 128
			// defaultslocked 256
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			if(exit.isReadable())
				f=f|4;
			//if(exit.isTrapped())
			//	f=f|16;
			if(exit.hasADoor())
				f=f|32;
			if(exit.hasALock())
				f=f|64;
			if(exit.defaultsClosed())
				f=f|128;
			if(exit.defaultsLocked())
				f=f|256;
			//if(exit.levelRestricted())
			//	f=f|512;
			//if(exit.classRestricted())
			//	f=f|1024;
			//if(exit.alignmentRestricted())
			//	f=f|2048;
		}
		return f;
	}

	public void setEnvFlags(Environmental E, int f)
	{
		if(E instanceof Item)
		{
			Item item=(Item)E;
			// deprecated, but unfortunately, its here to stay.
			CMLib.flags().setDroppable(item,get(f,1));
			CMLib.flags().setGettable(item,get(f,2));
			CMLib.flags().setReadable(item,get(f,4));
			CMLib.flags().setRemovable(item,get(f,8));
		}
		if(E instanceof Container)
		{
			Container container=(Container)E;
			container.setLidsNLocks(get(f,32),!get(f,32),get(f,64),get(f,64));
		}
		else
		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			exit.setReadable(get(f,4));
			if(get(f,16)) Log.errOut("CoffeeMaker","Exit "+identifier(E,null)+" has deprecated trap flag set!");
			boolean HasDoor=get(f,32);
			boolean HasLock=get(f,64);
			boolean DefaultsClosed=get(f,128);
			boolean DefaultsLocked=get(f,256);
			if(get(f,512)) Log.errOut("CoffeeMaker","Exit "+identifier(E,null)+" has deprecated level restriction flag set!");
			if(get(f,1024)) Log.errOut("CoffeeMaker","Exit "+identifier(E,null)+" has deprecated class restriction flag set!");
			if(get(f,2048)) Log.errOut("CoffeeMaker","Exit "+identifier(E,null)+" has deprecated alignment restriction flag set!");
			exit.setDoorsNLocks(HasDoor,(!HasDoor)||(!DefaultsClosed),DefaultsClosed,HasLock,HasLock&&DefaultsLocked,DefaultsLocked);
		}
	}

	public String getPropertiesStr(Environmental E, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","getPropertiesStr: null 'E'");
			return "";
		}
		return (E.isGeneric()?getGenPropertiesStr(E):"") + (fromTop?getOrdPropertiesStr(E):"");
	}

	public String getOrdPropertiesStr(Environmental E)
	{
		if(E instanceof Room)
		{
			if(E instanceof GridLocale)
				return CMLib.xml().convertXMLtoTag("XGRID",((GridLocale)E).xGridSize())
					  +CMLib.xml().convertXMLtoTag("YGRID",((GridLocale)E).yGridSize())
					  +getExtraEnvPropertiesStr(E)
                      +getGenScripts(E,false);
			return getExtraEnvPropertiesStr(E)+getGenScripts(E,false);
		}
		else
		if(E instanceof Area)
		{
            Area myArea=(Area)E;
		    StringBuffer str = new StringBuffer();
		    StringBuffer parentstr = new StringBuffer();
		    StringBuffer childrenstr = new StringBuffer();
		    str.append(CMLib.xml().convertXMLtoTag("ARCHP",myArea.getArchivePath()));
		    for(Enumeration e=myArea.getParents(); e.hasMoreElements();)
			{
		        Area A=(Area)e.nextElement();
		        parentstr.append("<PARENT>");
		        parentstr.append(CMLib.xml().convertXMLtoTag("PARENTNAMED", A.name()));
		        parentstr.append("</PARENT>");
		    }
		    str.append(CMLib.xml().convertXMLtoTag("PARENTS",parentstr.toString()));
		    for(Enumeration e=myArea.getChildren(); e.hasMoreElements();)
			{
		        Area A=(Area)e.nextElement();
		        childrenstr.append("<CHILD>");
		        childrenstr.append(CMLib.xml().convertXMLtoTag("CHILDNAMED", A.name()));
		        childrenstr.append("</CHILD>");
		    }
		    str.append(CMLib.xml().convertXMLtoTag("CHILDREN",childrenstr.toString()));
		    str.append(getExtraEnvPropertiesStr(E));
            str.append(getGenScripts(E,false));
			str.append(CMLib.xml().convertXMLtoTag("AUTHOR",myArea.getAuthorID()));
			str.append(CMLib.xml().convertXMLtoTag("CURRENCY",myArea.getCurrency()));
            Vector V=new Vector();
            String flag=null;
            for(int i=0;i<myArea.numBlurbFlags();i++)
            {
                flag=myArea.getBlurbFlag(i);
                V.addElement((flag+" "+myArea.getBlurbFlag(flag)).trim());
            }
            str.append(CMLib.xml().convertXMLtoTag("BLURBS",CMLib.xml().getXMLList(V)));
			if(E instanceof GridZones)
	            str.append(CMLib.xml().convertXMLtoTag("XGRID",((GridZones)E).xGridSize())
						  +CMLib.xml().convertXMLtoTag("YGRID",((GridZones)E).yGridSize()));
		    return str.toString();
		}
		else
		if(E instanceof Ability)
			return CMLib.xml().convertXMLtoTag("AWRAP",E.text());
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			String xml=
				(((item instanceof Container)&&(((Container)item).capacity()>0))
				?CMLib.xml().convertXMLtoTag("IID",""+E):"")
				+CMLib.xml().convertXMLtoTag("IWORN",""+item.rawWornCode())
				+CMLib.xml().convertXMLtoTag("ILOC",""+((item.container()!=null)?(""+item.container()):""))
				+CMLib.xml().convertXMLtoTag("IUSES",""+item.usesRemaining())
				+CMLib.xml().convertXMLtoTag("ILEVL",""+E.baseEnvStats().level())
				+CMLib.xml().convertXMLtoTag("IABLE",""+E.baseEnvStats().ability())
				+((E.isGeneric()?"":CMLib.xml().convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		else
		if(E instanceof MOB)
		{
			String xml=
				 CMLib.xml().convertXMLtoTag("MLEVL",""+E.baseEnvStats().level())
				+CMLib.xml().convertXMLtoTag("MABLE",""+E.baseEnvStats().ability())
				+CMLib.xml().convertXMLtoTag("MREJV",""+E.baseEnvStats().rejuv())
				+((E.isGeneric()?"":CMLib.xml().convertXMLtoTag("ITEXT",""+E.text())));
			return xml;
		}
		return "";
	}

	public String getGenMobAbilities(MOB M)
	{
		StringBuffer abilitystr=new StringBuffer("");
		for(int b=0;b<M.numLearnedAbilities();b++)
		{
			Ability A=M.fetchAbility(b);
			if((A!=null)&&(A.savable()))
			{
				abilitystr.append("<ABLTY>");
				abilitystr.append(CMLib.xml().convertXMLtoTag("ACLASS",CMClass.classID(A)));
				abilitystr.append(CMLib.xml().convertXMLtoTag("APROF",""+A.proficiency()));
				abilitystr.append(CMLib.xml().convertXMLtoTag("ADATA",getPropertiesStr(A,true)));
				abilitystr.append("</ABLTY>");
			}
		}
		return (CMLib.xml().convertXMLtoTag("ABLTYS",abilitystr.toString()));
	}

    public String getGenScripts(Environmental E, boolean includeVars)
    {
        StringBuffer scriptstr=new StringBuffer("");
        for(int b=0;b<E.numScripts();b++)
        {
            ScriptingEngine S=E.fetchScript(b);
            if((S!=null)&&(S.isSavable()))
            {
                scriptstr.append("<SCRPT>");
                scriptstr.append(CMLib.xml().convertXMLtoTag("SCRIPT",CMLib.xml().parseOutAngleBrackets(S.getScript())));
                scriptstr.append(CMLib.xml().convertXMLtoTag("SQN",""+S.defaultQuestName()));
                scriptstr.append(CMLib.xml().convertXMLtoTag("SSCOP",S.getVarScope()));
                if((includeVars)&&(S.getVarScope().equals("*")))
                    scriptstr.append(CMLib.xml().convertXMLtoTag("SSVAR",S.getLocalVarXML()));
                scriptstr.append("</SCRPT>");
            }
        }
        if(scriptstr.length()>0)
            return (CMLib.xml().convertXMLtoTag("SCRPTS",scriptstr.toString()));
        return "";
    }

	public String getGenMobInventory(MOB M)
	{
		StringBuffer itemstr=new StringBuffer("");
		for(int b=0;b<M.inventorySize();b++)
		{
			Item I=M.fetchInventory(b);
			if((I!=null)&&(I.savable()))
			{
				itemstr.append("<ITEM>");
				itemstr.append(CMLib.xml().convertXMLtoTag("ICLASS",CMClass.classID(I)));
				itemstr.append(CMLib.xml().convertXMLtoTag("IDATA",getPropertiesStr(I,true)));
				itemstr.append("</ITEM>");
			}
		}
		return (CMLib.xml().convertXMLtoTag("INVEN",itemstr.toString()));
	}

	public String getGenPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(getEnvPropertiesStr(E));

		text.append(CMLib.xml().convertXMLtoTag("FLAG",envFlags(E)));

		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			text.append(
			 CMLib.xml().convertXMLtoTag("CLOSTX",exit.closedText())
			+CMLib.xml().convertXMLtoTag("DOORNM",exit.doorName())
			+CMLib.xml().convertXMLtoTag("OPENNM",exit.openWord())
			+CMLib.xml().convertXMLtoTag("CLOSNM",exit.closeWord())
			+CMLib.xml().convertXMLtoTag("KEYNM",exit.keyName())
			+CMLib.xml().convertXMLtoTag("OPENTK",exit.openDelayTicks()));
		}

		if(E instanceof ClanItem)
		{
			text.append(CMLib.xml().convertXMLtoTag("CLANID",""+((ClanItem)E).clanID()));
			text.append(CMLib.xml().convertXMLtoTag("CITYPE",""+((ClanItem)E).ciType()));
		}

		if(E instanceof Item)
		{
			Item item=(Item)E;
			text.append(
			 CMLib.xml().convertXMLtoTag("IDENT",item.rawSecretIdentity())
			+CMLib.xml().convertXMLtoTag("VALUE",item.baseGoldValue())
			//+CMLib.xml().convertXMLtoTag("USES",item.usesRemaining()) // handled 'from top' & in db
			+CMLib.xml().convertXMLtoTag("MTRAL",item.material())
			+CMLib.xml().convertXMLtoTag("READ",item.readableText())
			+CMLib.xml().convertXMLtoTag("WORNL",item.rawLogicalAnd())
			+CMLib.xml().convertXMLtoTag("WORNB",item.rawProperLocationBitmap()));
			if(E instanceof Container)
			{
				text.append(CMLib.xml().convertXMLtoTag("CAPA",((Container)item).capacity()));
				text.append(CMLib.xml().convertXMLtoTag("CONT",((Container)item).containTypes()));
			}
			if(E instanceof Weapon)
				text.append(CMLib.xml().convertXMLtoTag("CAPA",((Weapon)item).ammunitionCapacity()));
		}

		if(E instanceof Coins)
		{
		    text.append(CMLib.xml().convertXMLtoTag("CRNC",((Coins)E).getCurrency()));
		    text.append(CMLib.xml().convertXMLtoTag("DENOM",""+((Coins)E).getDenomination()));
		}
		if(E instanceof Electronics)
		{
		    text.append(CMLib.xml().convertXMLtoTag("FUELT",((Electronics)E).fuelType()));
		    text.append(CMLib.xml().convertXMLtoTag("POWC",""+((Electronics)E).powerCapacity()));
		    text.append(CMLib.xml().convertXMLtoTag("POWR",""+((Electronics)E).powerRemaining()));
		}
	    if(E instanceof ShipComponent)
	    {
	        if(E instanceof ShipComponent.ShipPanel)
	        {
			    text.append(CMLib.xml().convertXMLtoTag("SSPANELT",""+((ShipComponent.ShipPanel)E).panelType()));
	        }
	        if(E instanceof ShipComponent.ShipEngine)
	        {
			    text.append(CMLib.xml().convertXMLtoTag("SSTHRUST",""+((ShipComponent.ShipEngine)E).getMaxThrust()));
	        }
	    }
		if(E instanceof Recipe)
		{
		    text.append(CMLib.xml().convertXMLtoTag("SKILLID",((Recipe)E).getCommonSkillID()));
		    text.append(CMLib.xml().convertXMLtoTag("RECIPE",""+((Recipe)E).getRecipeCodeLine()));
		}

		if(E instanceof Light)
			text.append(CMLib.xml().convertXMLtoTag("BURNOUT",((Light)E).destroyedWhenBurnedOut()));

		if(E instanceof Wand)
			text.append(CMLib.xml().convertXMLtoTag("MAXUSE",((Wand)E).maxUses()));

		if(E instanceof Rideable)
		{
			text.append(CMLib.xml().convertXMLtoTag("RIDET",((Rideable)E).rideBasis()));
			text.append(CMLib.xml().convertXMLtoTag("RIDEC",((Rideable)E).riderCapacity()));
		}

		if(E instanceof RawMaterial)
			text.append(CMLib.xml().convertXMLtoTag("DOMN",((RawMaterial)E).domainSource()+""));

		if(E instanceof Food)
		{
			text.append(CMLib.xml().convertXMLtoTag("CAPA2",((Food)E).nourishment()));
            text.append(CMLib.xml().convertXMLtoTag("BITE",((Food)E).bite()));
		}

		if(E instanceof Drink)
		{
			text.append(CMLib.xml().convertXMLtoTag("CAPA2",((Drink)E).liquidHeld()));
			text.append(CMLib.xml().convertXMLtoTag("DRINK",((Drink)E).thirstQuenched()));
		}

		if(E instanceof Weapon)
		{
			text.append(CMLib.xml().convertXMLtoTag("TYPE",((Weapon)E).weaponType()));
			text.append(CMLib.xml().convertXMLtoTag("CLASS",((Weapon)E).weaponClassification()));
			text.append(CMLib.xml().convertXMLtoTag("MINR",((Weapon)E).minRange()));
			text.append(CMLib.xml().convertXMLtoTag("MAXR",((Weapon)E).maxRange()));
		}

		if(E instanceof Armor)
		{
			text.append(CMLib.xml().convertXMLtoTag("LAYR",((Armor)E).getClothingLayer()));
			text.append(CMLib.xml().convertXMLtoTag("LAYA",((Armor)E).getLayerAttributes()));
		}

		if(E instanceof LandTitle)
			text.append(CMLib.xml().convertXMLtoTag("LANDID",((LandTitle)E).landPropertyID()));

		if(E instanceof Perfume)
			text.append(CMLib.xml().convertXMLtoTag("SMELLLST",((Perfume)E).getSmellList()));

		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()!=null)
			{
				text.append(CMLib.xml().convertXMLtoTag("GENDER",""+(char)((DeadBody)E).charStats().getStat(CharStats.STAT_GENDER)));
				text.append(CMLib.xml().convertXMLtoTag("MRACE",""+((DeadBody)E).charStats().getMyRace().ID()));
				text.append(CMLib.xml().convertXMLtoTag("MDNAME",""+((DeadBody)E).mobName()));
				text.append(CMLib.xml().convertXMLtoTag("MDDESC",""+((DeadBody)E).mobDescription()));
				text.append(CMLib.xml().convertXMLtoTag("MKNAME",""+((DeadBody)E).killerName()));
				text.append(CMLib.xml().convertXMLtoTag("MTOD",""+((DeadBody)E).timeOfDeath()));
				text.append(CMLib.xml().convertXMLtoTag("MKPLAY",""+((DeadBody)E).killerPlayer()));
				text.append(CMLib.xml().convertXMLtoTag("MDLMSG",""+((DeadBody)E).lastMessage()));
				text.append(CMLib.xml().convertXMLtoTag("MBREAL",""+((DeadBody)E).destroyAfterLooting()));
				text.append(CMLib.xml().convertXMLtoTag("MPLAYR",""+((DeadBody)E).playerCorpse()));
				text.append(CMLib.xml().convertXMLtoTag("MPKILL",""+((DeadBody)E).mobPKFlag()));
                if(((DeadBody)E).savedMOB()!=null)
                    text.append("<MOBS>"+getMobXML(((DeadBody)E).savedMOB())+"</MOBS>");
				if(((DeadBody)E).killingTool()==null) text.append("<KLTOOL />");
				else
				{
					text.append("<KLTOOL>");
					text.append(CMLib.xml().convertXMLtoTag("KLCLASS",CMClass.classID(((DeadBody)E).killingTool())));
					text.append(CMLib.xml().convertXMLtoTag("KLDATA",getPropertiesStr(((DeadBody)E).killingTool(),true)));
					text.append("</KLTOOL>");
				}
			}
			else
			{
				text.append(CMLib.xml().convertXMLtoTag("GENDER","M"));
				text.append(CMLib.xml().convertXMLtoTag("MRACE","Human"));
				text.append(CMLib.xml().convertXMLtoTag("MPLAYR","false"));
			}
		}

		if(E instanceof MOB)
		{
			text.append(CMLib.xml().convertXMLtoTag("MONEY",CMLib.beanCounter().getMoney((MOB)E)));
            text.append(CMLib.xml().convertXMLtoTag("VARMONEY",""+((MOB)E).getMoneyVariation()));
			CMLib.beanCounter().clearInventoryMoney((MOB)E,null);
			text.append(CMLib.xml().convertXMLtoTag("CLAN",((MOB)E).getClanID()));
			text.append(CMLib.xml().convertXMLtoTag("GENDER",""+(char)((MOB)E).baseCharStats().getStat(CharStats.STAT_GENDER)));
			text.append(CMLib.xml().convertXMLtoTag("MRACE",""+((MOB)E).baseCharStats().getMyRace().ID()));
            text.append(getFactionXML((MOB)E));
			text.append(getGenMobInventory((MOB)E));
			text.append(getGenMobAbilities((MOB)E));

			if(E instanceof Banker)
			{
				text.append(CMLib.xml().convertXMLtoTag("BANK",""+((Banker)E).bankChain()));
				text.append(CMLib.xml().convertXMLtoTag("COININT",""+((Banker)E).getCoinInterest()));
				text.append(CMLib.xml().convertXMLtoTag("ITEMINT",""+((Banker)E).getItemInterest()));
				text.append(CMLib.xml().convertXMLtoTag("LOANINT",""+((Banker)E).getLoanInterest()));
			}
            if(E instanceof PostOffice)
            {
                text.append(CMLib.xml().convertXMLtoTag("POSTCHAIN",""+((PostOffice)E).postalChain()));
                text.append(CMLib.xml().convertXMLtoTag("POSTMIN",""+((PostOffice)E).minimumPostage()));
                text.append(CMLib.xml().convertXMLtoTag("POSTLBS",""+((PostOffice)E).postagePerPound()));
                text.append(CMLib.xml().convertXMLtoTag("POSTHOLD",""+((PostOffice)E).holdFeePerPound()));
                text.append(CMLib.xml().convertXMLtoTag("POSTNEW",""+((PostOffice)E).feeForNewBox()));
                text.append(CMLib.xml().convertXMLtoTag("POSTHELD",""+((PostOffice)E).maxMudMonthsHeld()));
            }
            if(E instanceof Auctioneer)
            {
                text.append(CMLib.xml().convertXMLtoTag("AUCHOUSE",""+((Auctioneer)E).auctionHouse()));
                //text.append(CMLib.xml().convertXMLtoTag("LIVEPRICE",""+((Auctioneer)E).liveListingPrice()));
                text.append(CMLib.xml().convertXMLtoTag("TIMEPRICE",""+((Auctioneer)E).timedListingPrice()));
                text.append(CMLib.xml().convertXMLtoTag("TIMEPCT",""+((Auctioneer)E).timedListingPct()));
                //text.append(CMLib.xml().convertXMLtoTag("LIVECUT",""+((Auctioneer)E).liveFinalCutPct()));
                text.append(CMLib.xml().convertXMLtoTag("TIMECUT",""+((Auctioneer)E).timedFinalCutPct()));
                text.append(CMLib.xml().convertXMLtoTag("MAXADAYS",""+((Auctioneer)E).maxTimedAuctionDays()));
                text.append(CMLib.xml().convertXMLtoTag("MINADAYS",""+((Auctioneer)E).minTimedAuctionDays()));
            }
			if(E instanceof Deity)
			{
				text.append(CMLib.xml().convertXMLtoTag("CLEREQ",((Deity)E).getClericRequirements()));
				text.append(CMLib.xml().convertXMLtoTag("WORREQ",((Deity)E).getWorshipRequirements()));
				text.append(CMLib.xml().convertXMLtoTag("CLERIT",((Deity)E).getClericRitual()));
				text.append(CMLib.xml().convertXMLtoTag("WORRIT",((Deity)E).getWorshipRitual()));
				text.append(CMLib.xml().convertXMLtoTag("CLERSIT",((Deity)E).getClericSin()));
				text.append(CMLib.xml().convertXMLtoTag("WORRSIT",((Deity)E).getWorshipSin()));
				text.append(CMLib.xml().convertXMLtoTag("CLERPOW",((Deity)E).getClericPowerup()));
                text.append(CMLib.xml().convertXMLtoTag("SVCRIT",((Deity)E).getServiceRitual()));

				StringBuffer itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numBlessings();b++)
				{
					Ability A=((Deity)E).fetchBlessing(b);
					if(A==null) continue;
					itemstr.append("<BLESS>");
					itemstr.append(CMLib.xml().convertXMLtoTag("BLCLASS",CMClass.classID(A)));
                    itemstr.append(CMLib.xml().convertXMLtoTag("BLONLY",""+((Deity)E).fetchBlessingCleric(b)));
					itemstr.append(CMLib.xml().convertXMLtoTag("BLDATA",getPropertiesStr(A,true)));
					itemstr.append("</BLESS>");
				}
				text.append(CMLib.xml().convertXMLtoTag("BLESSINGS",itemstr.toString()));

				itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numCurses();b++)
				{
					Ability A=((Deity)E).fetchCurse(b);
					if(A==null) continue;
					itemstr.append("<CURSE>");
					itemstr.append(CMLib.xml().convertXMLtoTag("CUCLASS",CMClass.classID(A)));
                    itemstr.append(CMLib.xml().convertXMLtoTag("CUONLY",""+((Deity)E).fetchCurseCleric(b)));
					itemstr.append(CMLib.xml().convertXMLtoTag("CUDATA",getPropertiesStr(A,true)));
					itemstr.append("</CURSE>");
				}
				text.append(CMLib.xml().convertXMLtoTag("CURSES",itemstr.toString()));

				itemstr=new StringBuffer("");
				for(int b=0;b<((Deity)E).numPowers();b++)
				{
					Ability A=((Deity)E).fetchPower(b);
					if(A==null) continue;
					itemstr.append("<POWER>");
					itemstr.append(CMLib.xml().convertXMLtoTag("POCLASS",CMClass.classID(A)));
					itemstr.append(CMLib.xml().convertXMLtoTag("PODATA",getPropertiesStr(A,true)));
					itemstr.append("</POWER>");
				}
				text.append(CMLib.xml().convertXMLtoTag("POWERS",itemstr.toString()));
			}
			if(E instanceof ShopKeeper)
			{
				text.append(CMLib.xml().convertXMLtoTag("SELLCD",((ShopKeeper)E).getWhatIsSoldMask()));
				Vector V=((ShopKeeper)E).getShop().getStoreInventory();
				StringBuffer itemstr=new StringBuffer("");
				for(int b=0;b<V.size();b++)
				{
					Environmental Env=(Environmental)V.elementAt(b);
					itemstr.append("<SHITEM>");
					itemstr.append(CMLib.xml().convertXMLtoTag("SICLASS",CMClass.classID(Env)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SITYPE",CMClass.getType(Env)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SISTOCK",((ShopKeeper)E).getShop().numberInStock(Env)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SIPRICE",((ShopKeeper)E).getShop().stockPrice(Env)));
					itemstr.append(CMLib.xml().convertXMLtoTag("SIDATA",getPropertiesStr(Env,true)));
					itemstr.append("</SHITEM>");
				}
				text.append(CMLib.xml().convertXMLtoTag("STORE",itemstr.toString()));
			}
			if(((MOB)E).numTattoos()>0)
			{
				text.append("<TATTS>");
				for(int i=0;i<((MOB)E).numTattoos();i++)
					text.append(((MOB)E).fetchTattoo(i)+";");
				text.append("</TATTS>");
			}
			if(((MOB)E).numExpertises()>0)
			{
				text.append("<EDUS>");
				for(int i=0;i<((MOB)E).numExpertises();i++)
					text.append(((MOB)E).fetchExpertise(i)+";");
				text.append("</EDUS>");
			}
		}
		return text.toString();
	}

	public String unpackErr(String where, String msg)
	{
		Log.errOut("CoffeeMaker","unpack"+where+"FromXML: "+msg);
		return msg;
	}

	public String unpackRoomFromXML(String buf, boolean andContent)
	{
		Vector xml=CMLib.xml().parseAllXML(buf);
		if(xml==null) return unpackErr("Room","null 'xml'");
		Vector roomData=CMLib.xml().getRealContentsFromPieces(xml,"AROOM");
		if(roomData==null) return unpackErr("Room","null 'roomData'");
		return unpackRoomFromXML(roomData,andContent);
	}

	public String unpackRoomFromXML(Vector xml, boolean andContent)
	{
		Area myArea=CMLib.map().getArea(CMLib.xml().getValFromPieces(xml,"RAREA"));
		if(myArea==null) return unpackErr("Room","null 'myArea'");
		String roomClass=CMLib.xml().getValFromPieces(xml,"RCLAS");
		Room newRoom=CMClass.getLocale(roomClass);
		if(newRoom==null) return unpackErr("Room","null 'newRoom'");
		newRoom.setRoomID(CMLib.xml().getValFromPieces(xml,"ROOMID"));
		if(newRoom.roomID().equals("NEW")) newRoom.setRoomID(myArea.getNewRoomID(newRoom,-1));
		if(CMLib.map().getRoom(newRoom.roomID())!=null) return "Room Exists: "+newRoom.roomID();
		newRoom.setArea(myArea);
		CMLib.database().DBCreateRoom(newRoom);
		newRoom.setDisplayText(CMLib.xml().getValFromPieces(xml,"RDISP"));
		newRoom.setDescription(CMLib.xml().getValFromPieces(xml,"RDESC"));
		newRoom.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xml,"RTEXT")));

		// now EXITS!
		Vector xV=CMLib.xml().getRealContentsFromPieces(xml,"ROOMEXITS");
		if(xV==null) return unpackErr("Room","null 'xV' in room "+newRoom.roomID());
		for(int x=0;x<xV.size();x++)
		{
			XMLLibrary.XMLpiece xblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
			if((!xblk.tag.equalsIgnoreCase("REXIT"))||(xblk.contents==null))
				return unpackErr("Room","??"+xblk.tag+" in room "+newRoom.roomID());
			int dir=CMLib.xml().getIntFromPieces(xblk.contents,"XDIRE");
			String doorID=CMLib.xml().getValFromPieces(xblk.contents,"XDOOR");
			if((dir<0)||(dir>=Directions.NUM_DIRECTIONS()))
			{
				if((dir>255)&&(!(newRoom instanceof GridLocale)))
					return unpackErr("Room","Not GridLocale, tried "+dir+" exit for room '"+newRoom.roomID()+"'");
				else
				if(dir>255)
				{
					String xdata=CMLib.xml().getValFromPieces(xblk.contents,"XDATA");
					Vector CEs=CMParms.parseSemicolons(xdata.trim(),true);
					for(int ces=0;ces<CEs.size();ces++)
					{
						Vector SCE=CMParms.parse(((String)CEs.elementAt(ces)).trim());
						WorldMap.CrossExit CE=new WorldMap.CrossExit();
						if(SCE.size()<3) continue;
						CE.x=CMath.s_int((String)SCE.elementAt(0));
						CE.y=CMath.s_int((String)SCE.elementAt(1));
						int codeddir=CMath.s_int((String)SCE.elementAt(2));
						if(SCE.size()>=4)
							CE.destRoomID=doorID+(String)SCE.elementAt(3);
						else
							CE.destRoomID=doorID;
						CE.out=(codeddir&256)==256;
						CE.dir=codeddir&255;
						((GridLocale)newRoom).addOuterExit(CE);
						Room link=CMLib.map().getRoom(doorID);
						if((!CE.out)&&(link!=null)&&(!(link instanceof GridLocale)))
						{
						    link.rawDoors()[CE.dir]=newRoom;
						    link.setRawExit(CE.dir,CMClass.getExit("Open"));
							CMLib.database().DBUpdateExits(link);
						}
					}
				}
				else
					return unpackErr("Room","Unknown direction: "+dir+" in room "+newRoom.roomID());
			}
			else
			{
				Vector xxV=CMLib.xml().getContentsFromPieces(xblk.contents,"XEXIT");
				if(xxV==null) return unpackErr("Room","null 'xxV' in room "+newRoom.roomID());
				Exit exit=null;
				if(xxV.size()>0)
				{
					exit=CMClass.getExit(CMLib.xml().getValFromPieces(xxV,"EXID"));
					if(exit==null) return unpackErr("Room","null 'exit' in room "+newRoom.roomID());
					exit.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xxV,"EXDAT")));
					newRoom.setRawExit(dir,exit);
				}
				else
					exit=CMClass.getExit("GenExit");
				exit.recoverEnvStats();
				if(doorID.length()>0)
				{
					Room link=CMLib.map().getRoom(doorID);
					if(link!=null)
						newRoom.rawDoors()[dir]=link;
					else
					{
						newRoom.setRawExit(dir,exit); // get will get the fake one too!
						exit.setTemporaryDoorLink(doorID);
					}
				}
			}
		}

		// find any mis-linked exits and fix them!
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					boolean changed=false;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit exit=R.getRawExit(d);
						if((exit!=null)&&(exit.temporaryDoorLink().equalsIgnoreCase(newRoom.roomID())))
						{
							exit.setTemporaryDoorLink("");
							R.rawDoors()[d]=newRoom;
							changed=true;
						}
						else
						if((R.rawDoors()[d]!=null)&&(R.rawDoors()[d].roomID().equals(newRoom.roomID())))
						{
							R.rawDoors()[d]=newRoom;
							changed=true;
						}
					}
					if(changed) CMLib.database().DBUpdateExits(R);
				}
			}
	    }catch(NoSuchElementException e){}
		CMLib.database().DBUpdateRoom(newRoom);
		CMLib.database().DBUpdateExits(newRoom);
		if(andContent)
		{
			Hashtable identTable=new Hashtable();

			Vector cV=CMLib.xml().getRealContentsFromPieces(xml,"ROOMCONTENT");
			if(cV==null) return unpackErr("Room","null 'cV' in room "+newRoom.roomID());
			if(cV.size()>0)
			{
				Hashtable mobRideTable=new Hashtable();
				Vector mV=CMLib.xml().getRealContentsFromPieces(cV,"ROOMMOBS");
				if(mV!=null) //return unpackErr("Room","null 'mV' in room "+newRoom.roomID());
				for(int m=0;m<mV.size();m++)
				{
					XMLLibrary.XMLpiece mblk=(XMLLibrary.XMLpiece)mV.elementAt(m);
					if((!mblk.tag.equalsIgnoreCase("RMOB"))||(mblk.contents==null))
						return unpackErr("Room","bad 'mblk' in room "+newRoom.roomID());
					String mClass=CMLib.xml().getValFromPieces(mblk.contents,"MCLAS");
					MOB newMOB=CMClass.getMOB(mClass);
					if(newMOB==null) return unpackErr("Room","null 'mClass': "+mClass+" in room "+newRoom.roomID());

					// for rideables AND leaders now!
					String iden=CMLib.xml().getValFromPieces(mblk.contents,"MIDEN");
					if((iden!=null)&&(iden.length()>0)) identTable.put(iden,newMOB);

					newMOB.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(mblk.contents,"MTEXT")));
					newMOB.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(mblk.contents,"MLEVL"));
					newMOB.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(mblk.contents,"MABLE"));
					newMOB.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(mblk.contents,"MREJV"));
					String ride=CMLib.xml().getValFromPieces(mblk.contents,"MRIDE");
					if((ride!=null)&&(ride.length()>0))
						mobRideTable.put(newMOB,ride);
					newMOB.setStartRoom(newRoom);
					newMOB.setLocation(newRoom);
					newMOB.recoverCharStats();
					newMOB.recoverEnvStats();
					newMOB.recoverMaxState();
					newMOB.resetToMaxState();
					newMOB.bringToLife(newRoom,true);
				}

				Hashtable itemLocTable=new Hashtable();
				Vector iV=CMLib.xml().getRealContentsFromPieces(cV,"ROOMITEMS");
				if(iV!=null) //return unpackErr("Room","null 'iV' in room "+newRoom.roomID());
				for(int i=0;i<iV.size();i++)
				{
					XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)iV.elementAt(i);
					if((!iblk.tag.equalsIgnoreCase("RITEM"))||(iblk.contents==null))
						return unpackErr("Room","bad 'iblk' in room "+newRoom.roomID());
					String iClass=CMLib.xml().getValFromPieces(iblk.contents,"ICLAS");
					Item newItem=CMClass.getItem(iClass);
					if(newItem instanceof ArchonOnly) continue;
					if(newItem==null) return unpackErr("Room","null 'iClass': "+iClass+" in room "+newRoom.roomID());
					if((newItem instanceof Container)||(newItem instanceof Rideable))
					{
						String iden=CMLib.xml().getValFromPieces(iblk.contents,"IIDEN");
						if((iden!=null)&&(iden.length()>0)) identTable.put(iden,newItem);
					}
					String iloc=CMLib.xml().getValFromPieces(iblk.contents,"ILOCA");
					if(iloc.length()>0) itemLocTable.put(newItem,iloc);
					newItem.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(iblk.contents,"ILEVL"));
					newItem.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(iblk.contents,"IABLE"));
					newItem.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(iblk.contents,"IREJV"));
					newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(iblk.contents,"IUSES"));
					newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(iblk.contents,"ITEXT")));
					newItem.setContainer(null);
					newItem.recoverEnvStats();
					newRoom.addItem(newItem);
					newItem.recoverEnvStats();
				}
				for(Enumeration e=itemLocTable.keys();e.hasMoreElements();)
				{
					Item childI=(Item)e.nextElement();
					String loc=(String)itemLocTable.get(childI);
					Item parentI=(Item)identTable.get(loc);
					if(parentI!=null)
					{
						childI.setContainer(parentI);
						childI.recoverEnvStats();
						parentI.recoverEnvStats();
					}
				}
				for(Enumeration e=mobRideTable.keys();e.hasMoreElements();)
				{
					MOB M=(MOB)e.nextElement();
					String ride=(String)mobRideTable.get(M);
					if((ride!=null)&&(ride.length()>0))
					{
						Environmental E=(Environmental)identTable.get(ride);
						if(E instanceof Rideable)
							M.setRiding((Rideable)E);
						else
						if(E instanceof MOB)
							M.setFollowing((MOB)E);
					}
				}
			}
		}
		// equivalent to clear debriandrestart
		CMLib.threads().clearDebri(newRoom,0);
		CMLib.database().DBUpdateItems(newRoom);
		newRoom.startItemRejuv();
		CMLib.database().DBUpdateMOBs(newRoom);
		return "";
	}

	public String fillAreaAndCustomVectorFromXML(String buf,
    											 Vector area,
    											 Vector custom,
    											 Hashtable externalFiles)
	{
		Vector xml=CMLib.xml().parseAllXML(buf);
		if(xml==null) return unpackErr("Fill","null 'xml'");
		String error=fillCustomVectorFromXML(xml,custom,externalFiles);
		if(error.length()>0) return error;
		Vector areaData=CMLib.xml().getRealContentsFromPieces(xml,"AREA");
		if(areaData==null) return unpackErr("Fill","null 'aV'");
		for(int a=0;a<areaData.size();a++)
			area.addElement(areaData.elementAt(a));
		return "";
	}
	public String fillCustomVectorFromXML(String xml,
	        									 Vector custom,
     											 Hashtable externalFiles)
	{
		Vector xmlv=CMLib.xml().parseAllXML(xml);
		if(xmlv==null) return unpackErr("Custom","null 'xmlv'");
		return fillCustomVectorFromXML(xmlv,custom,externalFiles);
	}
	public String fillCustomVectorFromXML(Vector xml,
	        									 Vector custom,
     											 Hashtable externalFiles)
	{
		Vector aV=CMLib.xml().getRealContentsFromPieces(xml,"CUSTOM");
		if(aV!=null)
		{
			for(int r=0;r<aV.size();r++)
			{
				XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)aV.elementAt(r);
				if(ablk.tag.equalsIgnoreCase("RACE"))
				{
					Race R=CMClass.getRace("GenRace");
					if(R!=null)
					{
						R=(Race)R.copyOf();
						R.setRacialParms("<RACE>"+ablk.value+"</RACE>");
						if(!R.ID().equals("GenRace"))
							custom.addElement(R);
					}
				}
				else
				if(ablk.tag.equalsIgnoreCase("CCLASS"))
				{
					CharClass C=CMClass.getCharClass("GenCharClass");
					if(C!=null)
					{
						C=(CharClass)C.copyOf();
						C.setClassParms("<CCLASS>"+ablk.value+"</CCLASS>");
						if(!C.ID().equals("GenCharClass"))
							custom.addElement(C);
					}
				}
				else
					return unpackErr("Custom","??"+ablk.tag);
			}
		}
		aV=CMLib.xml().getRealContentsFromPieces(xml,"FILES");
		if(aV!=null)
		{
			for(int r=0;r<aV.size();r++)
			{
				XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)aV.elementAt(r);
				if(!ablk.tag.equalsIgnoreCase("FILE"))
					return unpackErr("Custom","Wrong tag in custome file! "+ablk.value);
				String filename=CMLib.xml().getParmValue(ablk.parms,"NAME");
				if((filename==null)||(filename.length()==0))
					return unpackErr("Custom","No custom file filename! "+ablk.value);
				if(!externalFiles.containsKey(filename))
					externalFiles.put(filename,ablk.value);
			}
		}
		return "";
	}

	public String fillAreasVectorFromXML(String buf,
	        									Vector areas,
	        									Vector custom,
    											Hashtable externalFiles)
	{
		Vector xml=CMLib.xml().parseAllXML(buf);
		if(xml==null) return unpackErr("Areas","null 'xml'");
		fillCustomVectorFromXML(xml,custom,externalFiles);
		Vector aV=CMLib.xml().getRealContentsFromPieces(xml,"AREAS");
		if(aV==null) return unpackErr("Areas","null 'aV'");
		for(int r=0;r<aV.size();r++)
		{
			XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)aV.elementAt(r);
			if((!ablk.tag.equalsIgnoreCase("AREA"))||(ablk.contents==null))
				return unpackErr("Areas","??"+ablk.tag);
			areas.addElement(ablk.contents);
		}
		return "";
	}

    public void addAutoPropsToAreaIfNecessary(Area newArea)
    {
        if((newArea!=null)
        &&(newArea.ID().equals("StdArea")))
        {
	        if(!CMProps.getVar(CMProps.SYSTEM_AUTOWEATHERPARMS).equalsIgnoreCase("no"))
	        {
	            Behavior B=newArea.fetchBehavior("WeatherAffects");
	            if(B==null){ B=CMClass.getBehavior("WeatherAffects"); B.setSavable(false); newArea.addBehavior(B);}
	            B.setParms(CMProps.getVar(CMProps.SYSTEM_AUTOWEATHERPARMS));
	        }
	        if(CMProps.getVar(CMProps.SYSTEM_AUTOAREAPROPS).trim().length()>0)
	        {
	        	String props=CMProps.getVar(CMProps.SYSTEM_AUTOAREAPROPS).trim();
	        	Vector allProps=CMParms.parseSemicolons(props,true);
	        	String prop=null;
	        	String parms=null;
	        	Ability A=null;
	        	Behavior B=null;
	        	for(int v=0;v<allProps.size();v++)
	        	{
	        		prop=(String)allProps.elementAt(v);
	        		parms="";
	        		int x=prop.indexOf("(");
	        		if(x>=0)
	        		{
	        			parms=prop.substring(x+1).trim();
	        			prop=prop.substring(0,x).trim();
	        			if(parms.endsWith(")")) parms=parms.substring(0,parms.length()-1);
	        		}
	        		B=CMClass.getBehavior(prop);
	        		if((B!=null)&&(newArea.fetchBehavior(B.ID())==null))
	        		{
	        			B.setSavable(false);
	        			newArea.addBehavior(B);
	        			B.setParms(parms);
	        		}
	        		else
	        		{
	        			A=CMClass.getAbility(prop);
	        			if((A!=null)&&(newArea.fetchEffect(A.ID())==null))
	        			{
	        				A.setSavable(false);
	        				newArea.addNonUninvokableEffect(A);
	        				A.setMiscText(parms);
	        			}
	        		}
	        	}
	        }
        }
    }

	public String unpackAreaFromXML(Vector aV, Session S, boolean andRooms)
	{
		String areaClass=CMLib.xml().getValFromPieces(aV,"ACLAS");
		String areaName=CMLib.xml().getValFromPieces(aV,"ANAME");

		if(CMLib.map().getArea(areaName)!=null) return "Area Exists: "+areaName;
		Area newArea=CMClass.getAreaType(areaClass);
		if(newArea==null) return unpackErr("Area","No class: "+areaClass);
		newArea.setName(areaName);
		CMLib.map().addArea(newArea);
		CMLib.database().DBCreateArea(newArea);

		newArea.setDescription(CMLib.coffeeFilter().safetyFilter(CMLib.xml().getValFromPieces(aV,"ADESC")));
		newArea.setClimateType(CMLib.xml().getIntFromPieces(aV,"ACLIM"));
		newArea.setTechLevel(CMLib.xml().getIntFromPieces(aV,"ATECH"));
		newArea.setSubOpList(CMLib.xml().getValFromPieces(aV,"ASUBS"));
		newArea.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(aV,"ADATA")));
		CMLib.database().DBUpdateArea(newArea.Name(),newArea);
		if(andRooms)
		{
			Vector rV=CMLib.xml().getRealContentsFromPieces(aV,"AROOMS");
			if(rV==null) return unpackErr("Area","null 'rV'");
			for(int r=0;r<rV.size();r++)
			{
				XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)rV.elementAt(r);
				if((!ablk.tag.equalsIgnoreCase("AROOM"))||(ablk.contents==null))
					return unpackErr("Area","??"+ablk.tag);
				//if(S!=null) S.rawPrint(".");
				String err=unpackRoomFromXML(ablk.contents,true);
				if(err.length()>0) return err;
			}
		}
		return "";
	}
	public String unpackAreaFromXML(String buf, Session S, boolean andRooms)
	{
		Vector xml=CMLib.xml().parseAllXML(buf);
		if(xml==null) return unpackErr("Area","null 'xml'");
		Vector aV=CMLib.xml().getRealContentsFromPieces(xml,"AREA");
		if(aV==null) return unpackErr("Area","null 'aV'");
		return unpackAreaFromXML(aV,S,andRooms);
	}

	public StringBuffer getAreaXML(Area area,
    							   Session S,
    							   HashSet custom,
    							   HashSet files,
    							   boolean andRooms)
	{
		StringBuffer buf=new StringBuffer("");
		if(area==null) return buf;
		int oldFlag=area.getAreaState();
		area.setAreaState(Area.STATE_FROZEN);
		buf.append("<AREA>");
		buf.append(CMLib.xml().convertXMLtoTag("ACLAS",area.ID()));
		buf.append(CMLib.xml().convertXMLtoTag("ANAME",area.Name()));
		buf.append(CMLib.xml().convertXMLtoTag("ADESC",area.description()));
		buf.append(CMLib.xml().convertXMLtoTag("ACLIM",area.climateType()));
		buf.append(CMLib.xml().convertXMLtoTag("ASUBS",area.getSubOpList()));
		buf.append(CMLib.xml().convertXMLtoTag("ATECH",area.getTechLevel()));
		buf.append(CMLib.xml().convertXMLtoTag("ADATA",area.text()));
		if(andRooms)
		{
			Enumeration r=area.getCompleteMap();
			if(!r.hasMoreElements())
				buf.append("<AROOMS />");
			else
			{
				buf.append("<AROOMS>");
				Room R=null;
				for(;r.hasMoreElements();)
				{
					R=(Room)r.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						//if(S!=null) S.rawPrint(".");
						if((R!=null)&&(R.roomID()!=null)&&(R.roomID().length()>0))
							buf.append(getRoomXML(R,custom,files,true)+"\n\r");
					}
				}
				buf.append("</AROOMS>");
			}
		}
		fillFileSet(area,files);
		buf.append("</AREA>");
        area.setAreaState(oldFlag);
		return buf;
	}

	public StringBuffer logTextDiff(String e1, String e2)
	{
		int start=0;
		int end=e1.length()-1;
		int end2=e2.length()-1;
		boolean stopStart=false;
		boolean stopEnd=false;
		while((!stopStart)||(!stopEnd))
		{
			if(!stopStart)
			{
				if((start>=end)
				 ||(start>=end2)
				 ||(e1.charAt(start)!=e2.charAt(start)))
					stopStart=true;
				else
					start++;
			}

			if(!stopEnd)
			{
				if((end<=start)
				||(end2<=start)
				||(e1.charAt(end)!=e2.charAt(end2)))
					stopEnd=true;
				else
				{
					end--;
					end2--;
				}
			}
		}
		StringBuffer str=new StringBuffer("*1>");
		if(end<start) str.append("");
		else str.append(e1.substring(start,end));
		str.append("\n\r*2>");
		if(end2<start) str.append("");
		else str.append(e2.substring(start,end2));
		return str;
	}

	public void logDiff(Environmental E1, Environmental E2)
	{
		StringBuffer str=new StringBuffer("Unmatched - "+E1.Name()+"\n\r");
		if(E1 instanceof MOB)
		{
			MOB mob=(MOB)E1;
			MOB dup=(MOB)E2;
			if(!CMClass.classID(mob).equals(CMClass.classID(dup)))
			   str.append(CMClass.classID(mob)+"!="+CMClass.classID(dup)+"\n\r");
			if(mob.baseEnvStats().level()!=dup.baseEnvStats().level())
			   str.append("Level- "+mob.baseEnvStats().level()+"!="+dup.baseEnvStats().level()+"\n\r");
			if(mob.baseEnvStats().ability()!=dup.baseEnvStats().ability())
			   str.append("Ability- "+mob.baseEnvStats().ability()+"!="+dup.baseEnvStats().ability()+"\n\r");
			if(!mob.text().equals(dup.text()))
				str.append(logTextDiff(mob.text(),dup.text()));
		}
		else
		if(E1 instanceof Item)
		{
			Item item=(Item)E1;
			Item dup=(Item)E2;
			if(!CMClass.classID(item).equals(CMClass.classID(dup)))
			   str.append(CMClass.classID(item)+"!="+CMClass.classID(dup)+"\n\r");
			if(item.baseEnvStats().level()!=dup.baseEnvStats().level())
			   str.append("Level- "+item.baseEnvStats().level()+"!="+dup.baseEnvStats().level()+"\n\r");
			if(item.baseEnvStats().ability()!=dup.baseEnvStats().ability())
			   str.append("Ability- "+item.baseEnvStats().ability()+"!="+dup.baseEnvStats().ability()+"\n\r");
			if(item.usesRemaining()!=dup.usesRemaining())
			   str.append("Uses- "+item.usesRemaining()+"!="+dup.usesRemaining()+"\n\r");
			if(!item.text().equals(dup.text()))
				str.append(logTextDiff(item.text(),dup.text()));
		}
		if(Log.debugChannelOn())
			Log.debugOut("CoffeeMaker",str.toString());
	}

    public Room makeNewRoomContent(Room room)
    {
        if(room==null) return null;
		room=CMLib.map().getRoom(room);
        Room R=CMClass.getLocale(room.ID());
        if(R==null) return null;
        R.setRoomID(room.roomID());
        CMLib.database().DBReadContent(R,null);
        return R;
    }


    public StringBuffer getMobXML(MOB mob)
    {
        StringBuffer buf=new StringBuffer("");
        buf.append("<MOB>");
        buf.append(CMLib.xml().convertXMLtoTag("MCLAS",CMClass.classID(mob)));
        buf.append(CMLib.xml().convertXMLtoTag("MLEVL",mob.baseEnvStats().level()));
        buf.append(CMLib.xml().convertXMLtoTag("MABLE",mob.baseEnvStats().ability()));
        buf.append(CMLib.xml().convertXMLtoTag("MREJV",mob.baseEnvStats().rejuv()));
        buf.append(CMLib.xml().convertXMLtoTag("MTEXT",CMLib.xml().parseOutAngleBrackets(mob.text())));
        buf.append("</MOB>\n\r");
        return buf;
    }

    public StringBuffer getMobsXML(Vector mobs,
                                   HashSet custom,
                                   HashSet files,
                                   Hashtable found)
    {
        StringBuffer buf=new StringBuffer("");
        for(int i=0;i<mobs.size();i++)
        {
            MOB mob=(MOB)mobs.elementAt(i);
            if(mob.savable())
            {
                Vector dups=(Vector)found.get(mob.Name()+mob.displayText());
                if(dups==null)
                {
                    dups=new Vector();
                    found.put(mob.Name()+mob.displayText(),dups);
                    dups.addElement(mob);
                }
                else
                {
                    boolean matched=false;
                    for(int v=0;v<dups.size();v++)
                    {
                        MOB dup=(MOB)dups.elementAt(v);
                        int oldHeight=mob.baseEnvStats().height();
                        int oldWeight=mob.baseEnvStats().weight();
                        int oldGender=mob.baseCharStats().getStat(CharStats.STAT_GENDER);
                        dup.baseEnvStats().setHeight(mob.baseEnvStats().height());
                        dup.baseEnvStats().setWeight(mob.baseEnvStats().weight());
                        dup.baseCharStats().setStat(CharStats.STAT_GENDER,mob.baseCharStats().getStat(CharStats.STAT_GENDER));
                        if(CMClass.classID(mob).equals(CMClass.classID(dup))
                        &&(mob.baseEnvStats().level()==dup.baseEnvStats().level())
                        &&(mob.baseEnvStats().ability()==dup.baseEnvStats().ability())
                        &&(mob.text().equals(dup.text())))
                            matched=true;
                        dup.baseEnvStats().setHeight(oldHeight);
                        dup.baseEnvStats().setWeight(oldWeight);
                        dup.baseCharStats().setStat(CharStats.STAT_GENDER,oldGender);
                        if(matched) break;
                    }
                    if(!matched)
                    {
                        for(int v=0;v<dups.size();v++)
                        {
                            MOB dup=(MOB)dups.elementAt(v);
                            int oldHeight=mob.baseEnvStats().height();
                            int oldWeight=mob.baseEnvStats().weight();
                            int oldGender=mob.baseCharStats().getStat(CharStats.STAT_GENDER);
                            dup.baseEnvStats().setHeight(mob.baseEnvStats().height());
                            dup.baseEnvStats().setWeight(mob.baseEnvStats().weight());
                            dup.baseCharStats().setStat(CharStats.STAT_GENDER,mob.baseCharStats().getStat(CharStats.STAT_GENDER));
                            if(Log.debugChannelOn()&&CMSecurity.isDebugging("EXPORT"))
                                logDiff(mob,dup);
                            dup.baseEnvStats().setHeight(oldHeight);
                            dup.baseEnvStats().setWeight(oldWeight);
                            dup.baseCharStats().setStat(CharStats.STAT_GENDER,oldGender);
                        }
                        dups.addElement(mob);
                    }
                    else
                        continue;
                }
                buf.append(getMobXML(mob));
                if((mob.baseCharStats().getMyRace().isGeneric())
                &&(!custom.contains(mob.baseCharStats().getMyRace())))
                   custom.add(mob.baseCharStats().getMyRace());
                fillFileSet(mob,files);
            }
        }
        return buf;
    }

	public StringBuffer getRoomMobs(Room room,
    							    HashSet custom,
    							    HashSet files,
    							    Hashtable found)
	{
		StringBuffer buf=new StringBuffer("");
        room=makeNewRoomContent(room);
		if(room==null) return buf;
		Vector mobs=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.addElement(room.fetchInhabitant(i));
        buf.append(getMobsXML(mobs,custom,files,found));
        room.destroy();
		return buf;
	}

	public StringBuffer getUniqueItemXML(Item item,
    									 int type,
    									 Hashtable found,
    									 HashSet files)
	{
		StringBuffer buf=new StringBuffer("");
		switch(type)
		{
		case 1: if(!(item instanceof Weapon)) return buf;
				break;
		case 2: if(!(item instanceof Armor)) return buf;
				break;
		}
		if(item.displayText().length()>0)
		{
			Vector dups=(Vector)found.get(item.Name()+item.displayText());
			if(dups==null)
			{
				dups=new Vector();
				found.put(item.Name()+item.displayText(),dups);
				dups.addElement(item);
			}
			else
			{
				for(int v=0;v<dups.size();v++)
				{
					Item dup=(Item)dups.elementAt(v);
					int oldHeight=item.baseEnvStats().height();
					item.baseEnvStats().setHeight(dup.baseEnvStats().height());
					if(CMClass.classID(item).equals(CMClass.classID(dup))
					&&(item.baseEnvStats().level()==dup.baseEnvStats().level())
					&&(item.usesRemaining()==dup.usesRemaining())
					&&(item.baseEnvStats().ability()==dup.baseEnvStats().ability())
					&&(item.text().equals(dup.text())))
					{
						item.baseEnvStats().setHeight(oldHeight);
						return buf;
					}
					item.baseEnvStats().setHeight(oldHeight);
				}
				for(int v=0;v<dups.size();v++)
				{
					Item dup=(Item)dups.elementAt(v);
					int oldHeight=item.baseEnvStats().height();
					item.baseEnvStats().setHeight(dup.baseEnvStats().height());
					if(Log.debugChannelOn()&&CMSecurity.isDebugging("EXPORT"))
						logDiff(item,dup);
					item.baseEnvStats().setHeight(oldHeight);
				}
				dups.addElement(item);
			}
            buf.append(getItemXML(item));
			fillFileSet(item,files);
		}
		return buf;
	}

    public StringBuffer getItemXML(Item item)
    {
        StringBuffer buf=new StringBuffer("");
        buf.append("<ITEM>");
        buf.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(item)));
        buf.append(CMLib.xml().convertXMLtoTag("IUSES",item.usesRemaining()));
        buf.append(CMLib.xml().convertXMLtoTag("ILEVL",item.baseEnvStats().level()));
        buf.append(CMLib.xml().convertXMLtoTag("IABLE",item.baseEnvStats().ability()));
        buf.append(CMLib.xml().convertXMLtoTag("IREJV",item.baseEnvStats().rejuv()));
        buf.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(item.text())));
        buf.append("</ITEM>\n\r");
        return buf;
    }

    public Item getItemFromXML(String xmlBuffer)
    {
        Vector xml=CMLib.xml().parseAllXML(xmlBuffer);
        if((xml==null)||(xml.size()==0)) return null;
        XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xml.firstElement();
        if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
            return null;
        String itemClass=CMLib.xml().getValFromPieces(iblk.contents,"ICLAS");
        Item newItem=CMClass.getItem(itemClass);
        if(newItem==null) return null;
        newItem.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(iblk.contents,"ILEVL"));
        newItem.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(iblk.contents,"IABLE"));
        newItem.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(iblk.contents,"IREJV"));
        newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(iblk.contents,"IUSES"));
        newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(iblk.contents,"ITEXT")));
        newItem.setContainer(null);
        newItem.recoverEnvStats();
        return newItem;
    }

	public String addItemsFromXML(String xmlBuffer,
								  Vector addHere,
								  Session S)
	{
		Vector xml=CMLib.xml().parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("Items","null 'xml'");
		Vector iV=CMLib.xml().getRealContentsFromPieces(xml,"ITEMS");
		if(iV==null) return unpackErr("Items","null 'iV'");
		for(int i=0;i<iV.size();i++)
		{
			XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)iV.elementAt(i);
			if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
				return unpackErr("Items","??"+iblk.tag);
			//if(S!=null) S.rawPrint(".");
			String itemClass=CMLib.xml().getValFromPieces(iblk.contents,"ICLAS");
			Item newItem=CMClass.getItem(itemClass);
			if((newItem instanceof ArchonOnly)
			&&((S==null)||(S.mob()==null)||(!CMSecurity.isASysOp(S.mob()))))
				continue;
			if(newItem==null) return unpackErr("Items","null 'iClass': "+itemClass);
			newItem.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(iblk.contents,"ILEVL"));
			newItem.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(iblk.contents,"IABLE"));
			newItem.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(iblk.contents,"IREJV"));
			newItem.setUsesRemaining(CMLib.xml().getIntFromPieces(iblk.contents,"IUSES"));
			newItem.setMiscText(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(iblk.contents,"ITEXT")));
			newItem.setContainer(null);
			newItem.recoverEnvStats();
			addHere.addElement(newItem);
		}
		return "";
	}

    public MOB getMobFromXML(String xmlBuffer)
    {
        Vector xml=CMLib.xml().parseAllXML(xmlBuffer);
        if((xml==null)||(xml.size()==0)) return null;
        XMLLibrary.XMLpiece mblk=(XMLLibrary.XMLpiece)xml.firstElement();
        if((!mblk.tag.equalsIgnoreCase("MOB"))||(mblk.contents==null))
            return null;
        String mClass=CMLib.xml().getValFromPieces(mblk.contents,"MCLAS");
        MOB newMOB=CMClass.getMOB(mClass);
        if(newMOB==null) return null;
        String text=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(mblk.contents,"MTEXT"));
        newMOB.setMiscText(text);
        newMOB.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(mblk.contents,"MLEVL"));
        newMOB.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(mblk.contents,"MABLE"));
        newMOB.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(mblk.contents,"MREJV"));
        newMOB.recoverCharStats();
        newMOB.recoverEnvStats();
        newMOB.recoverMaxState();
        newMOB.resetToMaxState();
        return newMOB;
    }


	public String addMOBsFromXML(String xmlBuffer,
								 Vector addHere,
								 Session S)
	{
		Vector xml=CMLib.xml().parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("MOBs","null 'xml'");
		Vector mV=CMLib.xml().getRealContentsFromPieces(xml,"MOBS");
		if(mV==null) return unpackErr("MOBs","null 'mV'");
		for(int m=0;m<mV.size();m++)
		{
			XMLLibrary.XMLpiece mblk=(XMLLibrary.XMLpiece)mV.elementAt(m);
			if((!mblk.tag.equalsIgnoreCase("MOB"))||(mblk.contents==null))
				return unpackErr("MOBs","bad 'mblk'");
			String mClass=CMLib.xml().getValFromPieces(mblk.contents,"MCLAS");
			MOB newMOB=CMClass.getMOB(mClass);
			if(newMOB==null) return unpackErr("MOBs","null 'mClass': "+mClass);
			String text=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(mblk.contents,"MTEXT"));
			newMOB.setMiscText(text);
			newMOB.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(mblk.contents,"MLEVL"));
			newMOB.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(mblk.contents,"MABLE"));
			newMOB.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(mblk.contents,"MREJV"));
			newMOB.recoverCharStats();
			newMOB.recoverEnvStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			addHere.addElement(newMOB);
		}
		return "";
	}

    public StringBuffer getItemsXML(Vector items, Hashtable found, HashSet files, int type)
    {
        StringBuffer buf=new StringBuffer("");
        for(int i=0;i<items.size();i++)
            buf.append(getUniqueItemXML((Item)items.elementAt(i),type,found,files));
        return buf;
    }
    
	public StringBuffer getRoomItems(Room room,
									 Hashtable found,
									 HashSet files,
									 int type) // 0=item, 1=weapon, 2=armor
	{
		StringBuffer buf=new StringBuffer("");
        room=makeNewRoomContent(room);
		if(room==null) return buf;
		Vector items=new Vector();
		for(int i=0;i<room.numItems();i++)
			items.addElement(room.fetchItem(i));
		Vector mobs=new Vector();
		for(int i=0;i<room.numInhabitants();i++)
			mobs.addElement(room.fetchInhabitant(i));
		for(int i=0;i<items.size();i++)
		{
			Item item=(Item)items.elementAt(i);
            if(item.savable())
    			buf.append(getUniqueItemXML(item,type,found,files));
		}
		for(int m=0;m<mobs.size();m++)
		{
			MOB M=(MOB)mobs.elementAt(m);
			if((M!=null)&&(M.savable()))
			{
				for(int i=0;i<M.inventorySize();i++)
				{
					Item item=M.fetchInventory(i);
					buf.append(getUniqueItemXML(item,type,found,files));
				}
				if(CMLib.coffeeShops().getShopKeeper(M)!=null)
				{
					Vector V=CMLib.coffeeShops().getShopKeeper(M).getShop().getStoreInventory();
					for(int v=0;v<V.size();v++)
					{
						Environmental E=(Environmental)V.elementAt(v);
						if(E instanceof Item)
							buf.append(getUniqueItemXML((Item)E,type,found,files));
					}
				}
			}
		}
        room.destroy();
		return buf;
	}

	public StringBuffer getRoomXML(Room room,
        							HashSet custom,
        							HashSet files,
        							boolean andContent)
	{
		StringBuffer buf=new StringBuffer("");
		if(room==null) return buf;
		// do this quick before a tick messes it up!
		Vector inhabs=new Vector();
        Room croom=makeNewRoomContent(room);
		if(andContent)
		for(int i=0;i<croom.numInhabitants();i++)
			inhabs.addElement(croom.fetchInhabitant(i));
		Vector items=new Vector();
		if(andContent)
		for(int i=0;i<croom.numItems();i++)
			items.addElement(croom.fetchItem(i));

		buf.append("<AROOM>");
		buf.append(CMLib.xml().convertXMLtoTag("ROOMID",room.roomID()));
		buf.append(CMLib.xml().convertXMLtoTag("RAREA",room.getArea().Name()));
		buf.append(CMLib.xml().convertXMLtoTag("RCLAS",CMClass.classID(room)));
		buf.append(CMLib.xml().convertXMLtoTag("RDISP",room.displayText()));
		buf.append(CMLib.xml().convertXMLtoTag("RDESC",room.description()));
		buf.append(CMLib.xml().convertXMLtoTag("RTEXT",CMLib.xml().parseOutAngleBrackets(room.text())));
		fillFileSet(room,files);
		buf.append("<ROOMEXITS>");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Room door=room.rawDoors()[d];
			Exit exit=room.getRawExit(d);
			if(((door!=null)&&(door.roomID().length()>0))||((door==null)&&(exit!=null)))
			{
				buf.append("<REXIT>");
				buf.append(CMLib.xml().convertXMLtoTag("XDIRE",d));
				if(door==null)
					buf.append("<XDOOR />");
				else
					buf.append(CMLib.xml().convertXMLtoTag("XDOOR",door.roomID()));
				if(exit==null)
					buf.append("<XEXIT />");
				else
				{
					buf.append("<XEXIT>");
					buf.append(CMLib.xml().convertXMLtoTag("EXID",exit.ID()));
					buf.append(CMLib.xml().convertXMLtoTag("EXDAT",CMLib.xml().parseOutAngleBrackets(exit.text())));
					buf.append("</XEXIT>");
				}
				fillFileSet(exit,files);
				buf.append("</REXIT>");
			}
		}
		if(room instanceof GridLocale)
		{
			Vector exits=((GridLocale)room).outerExits();
			HashSet done=new HashSet();
			int ordinal=0;
			for(int v=0;v<exits.size();v++)
			{
				WorldMap.CrossExit CE=(WorldMap.CrossExit)exits.elementAt(v);
				Room R=CMLib.map().getRoom(CE.destRoomID);
				if(R==null) continue;
				if(R.getGridParent()!=null) R=R.getGridParent();
				if((R.roomID().length()>0)&&(!done.contains(R.roomID())))
				{
					done.add(R.roomID());
					HashSet oldStrs=new HashSet();
					for(int v2=0;v2<exits.size();v2++)
					{
						WorldMap.CrossExit CE2=(WorldMap.CrossExit)exits.elementAt(v2);
						if((CE2.destRoomID.equals(R.roomID())
						||(CE2.destRoomID.startsWith(R.roomID()+"#("))))
						{
							String str=CE2.x+" "+CE2.y+" "+((CE2.out?256:512)|CE2.dir)+" "+CE2.destRoomID.substring(R.roomID().length())+";";
							if(!oldStrs.contains(str))
								oldStrs.add(str);
						}
					}
					StringBuffer exitStr=new StringBuffer("");
					for(Iterator a=oldStrs.iterator();a.hasNext();)
						exitStr.append((String)a.next());
					buf.append("<REXIT>");
					buf.append(CMLib.xml().convertXMLtoTag("XDIRE",(256+(++ordinal))));
					buf.append(CMLib.xml().convertXMLtoTag("XDOOR",R.roomID()));
					buf.append(CMLib.xml().convertXMLtoTag("XDATA",exitStr.toString()));
					buf.append("</REXIT>");
				}
			}
		}
		buf.append("</ROOMEXITS>");
		if(andContent)
		{
			buf.append("<ROOMCONTENT>");
			if(inhabs.size()==0)
				buf.append("<ROOMMOBS />");
			else
			{
				buf.append("<ROOMMOBS>");
				for(int i=0;i<inhabs.size();i++)
				{
					MOB mob=(MOB)inhabs.elementAt(i);
					if((mob.isMonster())&&((mob.amFollowing()==null)||(mob.amFollowing().isMonster())))
					{
						if((mob.charStats().getMyRace().isGeneric())
						&&(!custom.contains(mob.charStats().getMyRace())))
						   custom.add(mob.charStats().getMyRace());

						buf.append("<RMOB>");
						buf.append(CMLib.xml().convertXMLtoTag("MCLAS",CMClass.classID(mob)));
						if((((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0)))||(mob.numFollowers()>0))
							buf.append(CMLib.xml().convertXMLtoTag("MIDEN",""+mob));
						buf.append(CMLib.xml().convertXMLtoTag("MLEVL",mob.baseEnvStats().level()));
						buf.append(CMLib.xml().convertXMLtoTag("MABLE",mob.baseEnvStats().ability()));
						buf.append(CMLib.xml().convertXMLtoTag("MREJV",mob.baseEnvStats().rejuv()));
						buf.append(CMLib.xml().convertXMLtoTag("MTEXT",CMLib.xml().parseOutAngleBrackets(mob.text())));
						if(mob.riding()!=null)
							buf.append(CMLib.xml().convertXMLtoTag("MRIDE",""+mob.riding()));
						else
						if(mob.amFollowing()!=null)
							buf.append(CMLib.xml().convertXMLtoTag("MRIDE",""+mob.amFollowing()));
						else
							buf.append("<MRIDE />");
						fillFileSet(mob,files);
						buf.append("</RMOB>");
					}
				}
				buf.append("</ROOMMOBS>");
			}
			if(items.size()==0)
				buf.append("<ROOMITEMS />");
			else
			{
				buf.append("<ROOMITEMS>");
				for(int i=0;i<items.size();i++)
				{
					buf.append("<RITEM>");
					Item item=(Item)items.elementAt(i);
                    if(item.savable())
                    {
    					buf.append(CMLib.xml().convertXMLtoTag("ICLAS",CMClass.classID(item)));
    					if(((item instanceof Container)&&(((Container)item).capacity()>0))
    					||((item instanceof Rideable)&&(((Rideable)item).numRiders()>0)))
    						buf.append(CMLib.xml().convertXMLtoTag("IIDEN",""+item));
    					if(item.container()==null)
    						buf.append("<ILOCA />");
    					else
    						buf.append(CMLib.xml().convertXMLtoTag("ILOCA",""+item.container()));
    					buf.append(CMLib.xml().convertXMLtoTag("IREJV",item.baseEnvStats().rejuv()));
    					buf.append(CMLib.xml().convertXMLtoTag("IUSES",item.usesRemaining()));
    					buf.append(CMLib.xml().convertXMLtoTag("ILEVL",item.baseEnvStats().level()));
    					buf.append(CMLib.xml().convertXMLtoTag("IABLE",item.baseEnvStats().ability()));
    					buf.append(CMLib.xml().convertXMLtoTag("ITEXT",CMLib.xml().parseOutAngleBrackets(item.text())));
    					buf.append("</RITEM>");
    					fillFileSet(item,files);
                    }
				}
				buf.append("</ROOMITEMS>");
			}
			buf.append("</ROOMCONTENT>");
		}
		buf.append("</AROOM>");
        croom.destroy();
		return buf;
	}

	public void setPropertiesStr(Environmental E, String buf, boolean fromTop)
	{
		Vector V=CMLib.xml().parseAllXML(buf);
		if(V==null)
			Log.errOut("CoffeeMaker","setPropertiesStr: null 'V': "+((E==null)?"":E.Name()));
		else
			setPropertiesStr(E,V,fromTop);
	}

	public void recoverEnvironmental(Environmental E)
	{
		if(E==null) return;
		E.recoverEnvStats();
		if(E instanceof MOB)
		{
			((MOB)E).recoverCharStats();
			((MOB)E).recoverMaxState();
			((MOB)E).resetToMaxState();
		}
	}

	public void setPropertiesStr(Environmental E, Vector V, boolean fromTop)
	{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","setPropertiesStr2: null 'E'");
			return;
		}
		if(!handleCatalogItem(E, V, fromTop))
		{
			if(E.isGeneric())
				setGenPropertiesStr(E,V);
			if(fromTop)
				setOrdPropertiesStr(E,V);
		}
		recoverEnvironmental(E);
	}

	public void setOrdPropertiesStr(Environmental E, Vector V)
	{
		if(V==null)
		{
			Log.errOut("CoffeeMaker","null XML returned on "+identifier(E,null)+" parse. Load aborted.");
			return;
		}

		if(E instanceof Room)
		{
			setExtraEnvProperties(E,V);
            setGenScripts(E,V,false);
			if(E instanceof GridLocale)
			{
				((GridLocale)E).setXGridSize(CMLib.xml().getIntFromPieces(V,"XGRID"));
				((GridLocale)E).setYGridSize(CMLib.xml().getIntFromPieces(V,"YGRID"));
			}
		}
		else
		if(E instanceof Area)
		{
			((Area)E).setArchivePath(CMLib.xml().getValFromPieces(V,"ARCHP"));
			((Area)E).setAuthorID(CMLib.xml().getValFromPieces(V,"AUTHOR"));
			((Area)E).setCurrency(CMLib.xml().getValFromPieces(V,"CURRENCY"));
            Vector VP=CMLib.xml().getRealContentsFromPieces(V,"PARENTS");
            if(VP!=null)
            {
                for(int i=0;i<VP.size();i++)
                {
                    XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)VP.elementAt(i);
                    if((!ablk.tag.equalsIgnoreCase("PARENT"))||(ablk.contents==null))
                    {
                        Log.errOut("CoffeeMaker","Error parsing 'PARENT' of "+identifier(E,null)+".  Load aborted");
                        return;
                    }
                    ((Area)E).addParentToLoad(CMLib.xml().getValFromPieces(ablk.contents,"PARENTNAMED"));
                }
            }
            Vector VC=CMLib.xml().getRealContentsFromPieces(V,"CHILDREN");
            if(VC!=null)
            {
                for(int i=0;i<VC.size();i++)
                {
                    XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)VC.elementAt(i);
                    if((!ablk.tag.equalsIgnoreCase("CHILD"))||(ablk.contents==null))
                    {
                        Log.errOut("CoffeeMaker","Error parsing 'CHILD' of "+identifier(E,null)+".  Load aborted");
                        return;
                    }
                    ((Area)E).addChildToLoad(CMLib.xml().getValFromPieces(ablk.contents,"CHILDNAMED"));
                }
            }
            for(int x=((Area)E).numBlurbFlags()-1;x>=0;x--)
                ((Area)E).delBlurbFlag(((Area)E).getBlurbFlag(x));
            Vector VB=CMLib.xml().parseXMLList(CMLib.xml().getValFromPieces(V,"BLURBS"));
            for(int i=0;i<VB.size();i++)
                ((Area)E).addBlurbFlag((String)VB.elementAt(i));
			if(E instanceof GridZones)
			{
				((GridZones)E).setXGridSize(CMLib.xml().getIntFromPieces(V,"XGRID"));
				((GridZones)E).setYGridSize(CMLib.xml().getIntFromPieces(V,"YGRID"));
			}
			setExtraEnvProperties(E,V);
            setGenScripts(E,V,false);
		}
		else
		if(E instanceof Ability)
			E.setMiscText(CMLib.xml().getValFromPieces(V,"AWRAP"));
		else
		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setUsesRemaining(CMLib.xml().getIntFromPieces(V,"IUSES"));
			item.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(V,"ILEVL"));
			item.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(V,"IABLE"));
			if(!E.isGeneric())
				item.setMiscText(CMLib.xml().getValFromPieces(V,"ITEXT"));
			//item.wearAt(CMLib.xml().getIntFromPieces(V,"USES"));
		}
		else
		if(E instanceof MOB)
		{
			E.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(V,"MLEVL"));
			E.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(V,"MABLE"));
			E.baseEnvStats().setRejuv(CMLib.xml().getIntFromPieces(V,"MREJV"));
			if(!E.isGeneric())
				E.setMiscText(CMLib.xml().getValFromPieces(V,"MTEXT"));
		}
	}

	public void setGenMobAbilities(MOB M, Vector buf)
	{
		Vector V=CMLib.xml().getRealContentsFromPieces(buf,"ABLTYS");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'ABLTYS' of "+identifier(M,null)+".  Load aborted");
			return;
		}
		for(int i=0;i<V.size();i++)
		{
			XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
			if((!ablk.tag.equalsIgnoreCase("ABLTY"))||(ablk.contents==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'ABLTY' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			Ability newOne=CMClass.getAbility(CMLib.xml().getValFromPieces(ablk.contents,"ACLASS"));
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown ability "+CMLib.xml().getValFromPieces(ablk.contents,"ACLASS")+" on "+identifier(M,null)+", skipping.");
				continue;
			}
			Vector adat=CMLib.xml().getRealContentsFromPieces(ablk.contents,"ADATA");
			if(adat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ABLTY DATA' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			String proff=CMLib.xml().getValFromPieces(ablk.contents,"APROF");
			if((proff!=null)&&(proff.length()>0))
				newOne.setProficiency(CMath.s_int(proff));
			else
				newOne.setProficiency(100);
			setPropertiesStr(newOne,adat,true);
			if(M.fetchAbility(newOne.ID())==null)
			{
				M.addAbility(newOne);
				newOne.autoInvocation(M);
			}
		}
	}

    public void setGenScripts(Environmental E, Vector buf, boolean restoreVars)
    {
        Vector V=CMLib.xml().getRealContentsFromPieces(buf,"SCRPTS");
        if(V==null) return;

        for(int i=0;i<V.size();i++)
        {
            XMLLibrary.XMLpiece sblk=(XMLLibrary.XMLpiece)V.elementAt(i);
            if((!sblk.tag.equalsIgnoreCase("SCRPT"))||(sblk.contents==null))
            {
                Log.errOut("CoffeeMaker","Error parsing 'SCRPT' of "+identifier(E,null)+".  Load aborted");
                return;
            }
            ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
            S.setSavable(true);
            String script=CMLib.xml().getValFromPieces(sblk.contents,"SCRIPT");
            if(script==null)
            {
                Log.errOut("CoffeeMaker","Error parsing 'SCRIPT' of "+identifier(E,null)+".  Load aborted");
                continue;
            }
            S.setScript(CMLib.xml().restoreAngleBrackets(script));
            String sq=CMLib.xml().getValFromPieces(sblk.contents,"SQN");
            if(sq.length()>0)
                S.registerDefaultQuest(sq);

            String scope=CMLib.xml().getValFromPieces(sblk.contents,"SSCOP");
            if(scope.length()>0)
                S.setVarScope(scope);

            if(restoreVars)
            {
                String svars=CMLib.xml().getValFromPieces(sblk.contents,"SSVAR");
                if((svars!=null)&&(svars.length()>0))
                    S.setLocalVarXML(svars);
            }
            E.addScript(S);
        }
    }

	public void setGenMobInventory(MOB M, Vector buf)
	{
		Vector V=CMLib.xml().getRealContentsFromPieces(buf,"INVEN");
		boolean variableEq=false;
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'INVEN' of "+identifier(M,null)+".  Load aborted");
			return;
		}
		Hashtable IIDmap=new Hashtable();
		Hashtable LOCmap=new Hashtable();
		for(int i=0;i<V.size();i++)
		{
			XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)V.elementAt(i);
			if((!iblk.tag.equalsIgnoreCase("ITEM"))||(iblk.contents==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			Item newOne=CMClass.getItem(CMLib.xml().getValFromPieces(iblk.contents,"ICLASS"));
			if(newOne instanceof ArchonOnly) continue;
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown item "+CMLib.xml().getValFromPieces(iblk.contents,"ICLASS")+" on "+identifier(M,null)+", skipping.");
				continue;
			}
			Vector idat=CMLib.xml().getRealContentsFromPieces(iblk.contents,"IDATA");
			if(idat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'ITEM DATA' of "+identifier(M,null)+".  Load aborted");
				return;
			}
			long wornCode=CMLib.xml().getLongFromPieces(idat,"IWORN");
			if((newOne instanceof Container)&&(((Container)newOne).capacity()>0))
				IIDmap.put(CMLib.xml().getValFromPieces(idat,"IID"),newOne);
			String ILOC=CMLib.xml().getValFromPieces(idat,"ILOC");
			M.addInventory(newOne);
			if(ILOC.length()>0)
				LOCmap.put(newOne,ILOC);
			setPropertiesStr(newOne,idat,true);
			if(newOne.baseEnvStats().rejuv()>0&&newOne.baseEnvStats().rejuv()<Integer.MAX_VALUE)
				variableEq=true;
			newOne.wearAt(wornCode);
		}
		for(int i=0;i<M.inventorySize();i++)
		{
			Item item=M.fetchInventory(i);
			if(item!=null)
			{
				String ILOC=(String)LOCmap.get(item);
				if(ILOC!=null)
					item.setContainer((Item)IIDmap.get(ILOC));
				else
				if(item.amWearingAt(Wearable.WORN_HELD)
				&&(!item.rawLogicalAnd())
				&&((item.rawProperLocationBitmap()&Wearable.WORN_WIELD)>0)
				&&(M.fetchWornItems(Wearable.WORN_WIELD,(short)0,(short)0).size()==0))
					item.wearAt(Wearable.WORN_WIELD);
			}
		}
		if(variableEq) M.flagVariableEq();
	}

	public void populateShops(Environmental E, Vector buf)
	{
		boolean variableEq=false;
		ShopKeeper shopmob=(ShopKeeper)E;
		shopmob.setWhatIsSoldMask(CMLib.xml().getLongFromPieces(buf,"SELLCD"));
		shopmob.getShop().emptyAllShelves();
		Vector V=CMLib.xml().getRealContentsFromPieces(buf,"STORE");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'STORE' of "+identifier(E,null)+".  Load aborted");
			return;
		}
		Hashtable IIDmap=new Hashtable();
		Hashtable LOCmap=new Hashtable();
		for(int i=0;i<V.size();i++)
		{
			XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)V.elementAt(i);
			if((!iblk.tag.equalsIgnoreCase("SHITEM"))||(iblk.contents==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'SHITEM' of "+identifier(E,null)+".  Load aborted");
				continue;
			}
			String itemi=CMLib.xml().getValFromPieces(iblk.contents,"SICLASS");
			XMLpiece x=CMLib.xml().getPieceFromPieces(iblk.contents,"SITYPE");
			int itemtype=-1;
			if((x!=null)&&(x.value!=null))
				itemtype=CMath.s_int(x.value);
			int numStock=CMLib.xml().getIntFromPieces(iblk.contents,"SISTOCK");
			String prc=CMLib.xml().getValFromPieces(iblk.contents,"SIPRICE");
			int stockPrice=-1;
			if((prc!=null)&&(prc.length()>0))
				stockPrice=CMath.s_int(prc);
			Environmental newOne=null;
			Vector idat=CMLib.xml().getRealContentsFromPieces(iblk.contents,"SIDATA");
			if(itemtype>=0)
				newOne=(Environmental)CMClass.getByType(itemi, itemtype);
			if((newOne==null)&&((iblk.value.indexOf("<ABLTY>")>=0)||(iblk.value.indexOf("&lt;ABLTY&gt;")>=0)))
				newOne=CMClass.getMOB(itemi);
			if(newOne==null) newOne=CMClass.getUnknown(itemi);
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown item "+itemi+" on "+identifier(E,null)+", skipping.");
				continue;
			}
			if(idat==null)
			{
				Log.errOut("CoffeeMaker","Error parsing 'SHOP DATA' of "+identifier(E,null)+".  Load aborted");
				continue;
			}
			if(newOne instanceof Item)
			{
				if(newOne instanceof Container)
					IIDmap.put(CMLib.xml().getValFromPieces(idat,"IID"),newOne);
				String ILOC=CMLib.xml().getValFromPieces(idat,"ILOC");
				if(ILOC.length()>0)
					LOCmap.put(ILOC,newOne);
			}
			setPropertiesStr(newOne,idat,true);
			if((newOne.baseEnvStats().rejuv()>0)&&(newOne.baseEnvStats().rejuv()<Integer.MAX_VALUE))
				variableEq=true;
			shopmob.getShop().addStoreInventory(newOne,numStock,stockPrice);
		}
		for(int i=0;i<shopmob.getShop().getStoreInventory().size();i++)
		{
			Environmental stE=(Environmental)shopmob.getShop().getStoreInventory().elementAt(i);
			if(stE instanceof Item)
			{
				Item item=(Item)stE;
				String ILOC=(String)LOCmap.get(item);
				if(ILOC!=null)
					item.setContainer((Item)IIDmap.get(ILOC));
			}
		}
		if(variableEq) ((MOB)E).flagVariableEq();
	}

	public boolean handleCatalogItem(Environmental E, Vector buf, boolean fromTop)
	{
		setEnvStats(E.baseEnvStats(),CMLib.xml().getValFromPieces(buf,"PROP"));
		if((CMLib.flags().isCataloged(E))
		&&(E.isGeneric()))
		{
			E.setName(CMLib.xml().getValFromPieces(buf,"NAME"));
			Environmental cataE=CMLib.catalog().getCatalogObj(E);
			if(cataE!=null)
			{
				if(CMath.bset(cataE.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
					Log.errOut("CoffeeMaker","Error with catalog object "+E.Name()+".");
				else
				if((cataE!=null)&&(cataE!=E))
				{
					if(fromTop)
						setOrdPropertiesStr(E,buf);
					setPropertiesStr(E, cataE.text(),false);
					CMLib.catalog().changeCatalogUsage(E, true);
					return true;
				}
			}
		}
		return false;
	}
	
	public void setGenPropertiesStr(Environmental E, Vector buf)
	{
		if(buf==null)
		{
			Log.errOut("CoffeeMaker","null XML returned on "+identifier(E,null)+" parse.  Load aborted.");
			return;
		}

		if((E instanceof MOB)&&(CMLib.xml().getValFromPieces(buf,"GENDER").length()==0))
		{
			Log.errOut("CoffeeMaker","MOB "+identifier(E,null)+" has malformed XML. Load aborted.");
			return;
		}

		if(E instanceof MOB)
		{
			while(((MOB)E).numLearnedAbilities()>0)
			{
				Ability A=((MOB)E).fetchAbility(0);
				if(A!=null)
					((MOB)E).delAbility(A);
			}
			while(((MOB)E).inventorySize()>0)
			{
				Item I=((MOB)E).fetchInventory(0);
				if(I!=null){ I.setOwner((MOB)E); I.destroy(); ((MOB)E).delInventory(I);}
			}
			if(E instanceof ShopKeeper)
			{
				Vector V=((ShopKeeper)E).getShop().getStoreInventory();
				for(int b=0;b<V.size();b++)
					((ShopKeeper)E).getShop().delAllStoreInventory(((Environmental)V.elementAt(b)));
			}
			if(E instanceof Deity)
			{
				while(((Deity)E).numBlessings()>0)
					((Deity)E).delBlessing(((Deity)E).fetchBlessing(0));
				while(((Deity)E).numCurses()>0)
					((Deity)E).delCurse(((Deity)E).fetchCurse(0));
				while(((Deity)E).numPowers()>0)
					((Deity)E).delPower(((Deity)E).fetchPower(0));
			}
		}
		while(E.numEffects()>0)
		{
			Ability aff=E.fetchEffect(0);
			if(aff!=null)
				E.delEffect(aff);
		}
		while(E.numBehaviors()>0)
		{
			Behavior behav=E.fetchBehavior(0);
			if(behav!=null)
				E.delBehavior(behav);
		}
        while(E.numScripts()>0)
        {
            ScriptingEngine scrpt=E.fetchScript(0);
            if(scrpt!=null)
                E.delScript(scrpt);
        }

		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.xml().getValFromPieces(buf,"GENDER").charAt(0));
			mob.setClanID(CMLib.xml().getValFromPieces(buf,"CLAN"));
			if(mob.getClanID().length()>0) mob.setClanRole(Clan.POS_MEMBER);
			String raceID=CMLib.xml().getValFromPieces(buf,"MRACE");
			Race R=(raceID.length()>0)?CMClass.getRace(raceID):null;
			if(R!=null)
			{
				mob.baseCharStats().setMyRace(R);
                mob.setTrains(0);
                mob.setPractices(0);
				R.startRacing(mob,false);
			}
		}

		setEnvProperties(E,buf);
		String deprecatedFlag=CMLib.xml().getValFromPieces(buf,"FLAG");
		if((deprecatedFlag!=null)&&(deprecatedFlag.length()>0))
			setEnvFlags(E,CMath.s_int(deprecatedFlag));

		if(E instanceof Exit)
		{
			Exit exit=(Exit)E;
			String closedText=CMLib.xml().getValFromPieces(buf,"CLOSTX");
			String doorName=CMLib.xml().getValFromPieces(buf,"DOORNM");
			String openName=CMLib.xml().getValFromPieces(buf,"OPENNM");
			String closeName=CMLib.xml().getValFromPieces(buf,"CLOSNM");
			exit.setExitParams(doorName,closeName,openName,closedText);
			exit.setKeyName(CMLib.xml().getValFromPieces(buf,"KEYNM"));
			exit.setOpenDelayTicks(CMLib.xml().getIntFromPieces(buf,"OPENTK"));
		}

		if(E instanceof ClanItem)
		{
			((ClanItem)E).setClanID(CMLib.xml().getValFromPieces(buf,"CLANID"));
			((ClanItem)E).setCIType(CMLib.xml().getIntFromPieces(buf,"CITYPE"));
		}

		if(E instanceof Item)
		{
			Item item=(Item)E;
			item.setSecretIdentity(CMLib.xml().getValFromPieces(buf,"IDENT"));
			item.setBaseValue(CMLib.xml().getIntFromPieces(buf,"VALUE"));
			item.setMaterial(CMLib.xml().getIntFromPieces(buf,"MTRAL"));
			//item.setUsesRemaining(CMath.s_int(CMLib.xml().returnXMLValue(buf,"USES")));
			if(item instanceof Container)
			{
				((Container)item).setCapacity(CMLib.xml().getIntFromPieces(buf,"CAPA"));
				((Container)item).setContainTypes(CMLib.xml().getLongFromPieces(buf,"CONT"));

			}
			if(item instanceof Weapon)
				((Weapon)item).setAmmoCapacity(CMLib.xml().getIntFromPieces(buf,"CAPA"));
			item.setRawLogicalAnd(CMLib.xml().getBoolFromPieces(buf,"WORNL"));
			item.setRawProperLocationBitmap(CMLib.xml().getIntFromPieces(buf,"WORNB"));
			item.setReadableText(CMLib.xml().getValFromPieces(buf,"READ"));

		}

		if(E instanceof Rideable)
		{
			((Rideable)E).setRideBasis(CMLib.xml().getIntFromPieces(buf,"RIDET"));
			((Rideable)E).setRiderCapacity(CMLib.xml().getIntFromPieces(buf,"RIDEC"));
		}
		if(E instanceof Electronics)
		{
			((Electronics)E).setFuelType(CMLib.xml().getIntFromPieces(buf,"FUELT"));
			((Electronics)E).setPowerCapacity(CMLib.xml().getIntFromPieces(buf,"POWC"));
			((Electronics)E).setPowerRemaining(CMLib.xml().getIntFromPieces(buf,"POWR"));
		}
	    if(E instanceof ShipComponent)
	    {
	        if(E instanceof ShipComponent.ShipPanel)
	        {
				((ShipComponent.ShipPanel)E).setPanelType(CMLib.xml().getIntFromPieces(buf,"SSPANELT"));
	        }
	        if(E instanceof ShipComponent.ShipEngine)
	        {
				((ShipComponent.ShipEngine)E).setMaxThrust(CMLib.xml().getIntFromPieces(buf,"SSTHRUST"));
	        }
	    }
		if(E instanceof Coins)
		{
			((Coins)E).setCurrency(CMLib.xml().getValFromPieces(buf,"CRNC"));
			((Coins)E).setDenomination(CMLib.xml().getDoubleFromPieces(buf,"DENOM"));
		}
		if(E instanceof Recipe)
		{
			((Recipe)E).setCommonSkillID(CMLib.xml().getValFromPieces(buf,"SKILLID"));
			((Recipe)E).setRecipeCodeLine(CMLib.xml().getValFromPieces(buf,"RECIPE"));
		}
		if(E instanceof Light)
		{
			String bo=CMLib.xml().getValFromPieces(buf,"BURNOUT");
			if((bo!=null)&&(bo.length()>0))
				((Light)E).setDestroyedWhenBurntOut(CMath.s_bool(bo));
		}

		if(E instanceof Wand)
		{
			String bo=CMLib.xml().getValFromPieces(buf,"MAXUSE");
			if((bo!=null)&&(bo.length()>0))
				((Wand)E).setMaxUses(CMath.s_int(bo));
		}

		if(E instanceof LandTitle)
			((LandTitle)E).setLandPropertyID(CMLib.xml().getValFromPieces(buf,"LANDID"));

		if(E instanceof Perfume)
			((Perfume)E).setSmellList(CMLib.xml().getValFromPieces(buf,"SMELLLST"));

		if(E instanceof Food)
		{
			((Food)E).setNourishment(CMLib.xml().getIntFromPieces(buf,"CAPA2"));
            ((Food)E).setBite(CMLib.xml().getIntFromPieces(buf,"BITE"));
		}

		if(E instanceof RawMaterial)
			((RawMaterial)E).setDomainSource(CMLib.xml().getValFromPieces(buf,"DOMN"));

		if(E instanceof Drink)
		{
			((Drink)E).setLiquidHeld(CMLib.xml().getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setLiquidRemaining(CMLib.xml().getIntFromPieces(buf,"CAPA2"));
			((Drink)E).setThirstQuenched(CMLib.xml().getIntFromPieces(buf,"DRINK"));
		}
		if(E instanceof Weapon)
		{
			((Weapon)E).setWeaponType(CMLib.xml().getIntFromPieces(buf,"TYPE"));
			((Weapon)E).setWeaponClassification(CMLib.xml().getIntFromPieces(buf,"CLASS"));
			((Weapon)E).setRanges(CMLib.xml().getIntFromPieces(buf,"MINR"),CMLib.xml().getIntFromPieces(buf,"MAXR"));
		}
		if(E instanceof Armor)
		{
			((Armor)E).setClothingLayer(CMLib.xml().getShortFromPieces(buf,"LAYR"));
			((Armor)E).setLayerAttributes(CMLib.xml().getShortFromPieces(buf,"LAYA"));
		}
		if(E instanceof DeadBody)
		{
			if(((DeadBody)E).charStats()==null)
				((DeadBody)E).setCharStats((CharStats)CMClass.getCommon("DefaultCharStats"));
			try{
				((DeadBody)E).charStats().setStat(CharStats.STAT_GENDER,CMLib.xml().getValFromPieces(buf,"GENDER").charAt(0));
				((DeadBody)E).setPlayerCorpse(CMLib.xml().getBoolFromPieces(buf,"MPLAYR"));
				String mobName=CMLib.xml().getValFromPieces(buf,"MDNAME");
				if(mobName.length()>0)
				{
					((DeadBody)E).setMobName(mobName);
					((DeadBody)E).setMobDescription(CMLib.xml().getValFromPieces(buf,"MDDESC"));
					((DeadBody)E).setTimeOfDeath(CMLib.xml().getLongFromPieces(buf,"MTOD"));
					((DeadBody)E).setKillerName(CMLib.xml().getValFromPieces(buf,"MKNAME"));
					((DeadBody)E).setKillerPlayer(CMLib.xml().getBoolFromPieces(buf,"MKPLAY"));
					((DeadBody)E).setMobPKFlag(CMLib.xml().getBoolFromPieces(buf,"MPKILL"));
					((DeadBody)E).setDestroyAfterLooting(CMLib.xml().getBoolFromPieces(buf,"MBREAL"));
					((DeadBody)E).setLastMessage(CMLib.xml().getValFromPieces(buf,"MDLMSG"));
                    String mobsXML=CMLib.xml().getValFromPieces(buf,"MOBS");
                    if((mobsXML!=null)&&(mobsXML.length()>0))
                    {
                        Vector V=new Vector();
                        String err=addMOBsFromXML("<MOBS>"+mobsXML+"</MOBS>",V,null);
                        if((err.length()==0)&&(V.size()>0))
                            ((DeadBody)E).setSavedMOB((MOB)V.firstElement());

                    }
					Vector dblk=CMLib.xml().getContentsFromPieces(buf,"KLTOOL");
					if((dblk!=null)&&(dblk.size()>0))
					{
						String itemi=CMLib.xml().getValFromPieces(dblk,"KLCLASS");
						Environmental newOne=null;
						Vector idat=CMLib.xml().getRealContentsFromPieces(dblk,"KLDATA");
						if(newOne==null) newOne=CMClass.getUnknown(itemi);
						if(newOne==null)
							Log.errOut("CoffeeMaker","Unknown tool "+itemi+" of "+identifier(E,null)+".  Skipping.");
						else
						{
							setPropertiesStr(newOne,idat,true);
							((DeadBody)E).setKillingTool(newOne);
						}
					}
					else
						((DeadBody)E).setKillingTool(null);
				}
			} catch(Exception e){}
			String raceID=CMLib.xml().getValFromPieces(buf,"MRACE");
			if((raceID.length()>0)&&(CMClass.getRace(raceID)!=null))
			{
				Race R=CMClass.getRace(raceID);
				((DeadBody)E).charStats().setMyRace(R);
			}
		}
		if(E instanceof MOB)
		{
			MOB mob=(MOB)E;
			String alignStr=CMLib.xml().getValFromPieces(buf,"ALIG");
			if((alignStr.length()>0)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
			    CMLib.factions().setAlignmentOldRange(mob,CMath.s_int(alignStr));
			CMLib.beanCounter().setMoney(mob,CMLib.xml().getIntFromPieces(buf,"MONEY"));
            mob.setMoneyVariation(CMLib.xml().getDoubleFromPieces(buf,"VARMONEY"));
			setGenMobInventory((MOB)E,buf);
			setGenMobAbilities((MOB)E,buf);
            setFactionFromXML((MOB)E,buf);

			if(E instanceof Banker)
			{
				((Banker)E).setBankChain(CMLib.xml().getValFromPieces(buf,"BANK"));
				((Banker)E).setCoinInterest(CMLib.xml().getDoubleFromPieces(buf,"COININT"));
				((Banker)E).setItemInterest(CMLib.xml().getDoubleFromPieces(buf,"ITEMINT"));
				String loanInt=CMLib.xml().getValFromPieces(buf,"LOANINT");
				if(loanInt.length()>0) ((Banker)E).setLoanInterest(CMath.s_double(loanInt));
			}

            if(E instanceof PostOffice)
            {
                ((PostOffice)E).setPostalChain(CMLib.xml().getValFromPieces(buf,"POSTCHAIN"));
                ((PostOffice)E).setMinimumPostage(CMLib.xml().getDoubleFromPieces(buf,"POSTMIN"));
                ((PostOffice)E).setPostagePerPound(CMLib.xml().getDoubleFromPieces(buf,"POSTLBS"));
                ((PostOffice)E).setHoldFeePerPound(CMLib.xml().getDoubleFromPieces(buf,"POSTHOLD"));
                ((PostOffice)E).setFeeForNewBox(CMLib.xml().getDoubleFromPieces(buf,"POSTNEW"));
                ((PostOffice)E).setMaxMudMonthsHeld(CMLib.xml().getIntFromPieces(buf,"POSTHELD"));
            }

            if(E instanceof Auctioneer)
            {
                ((Auctioneer)E).setAuctionHouse(CMLib.xml().getValFromPieces(buf,"AUCHOUSE"));
                //((Auctioneer)E).setLiveListingPrice(CMLib.xml().getDoubleFromPieces(buf,"LIVEPRICE"));
                ((Auctioneer)E).setTimedListingPrice(CMLib.xml().getDoubleFromPieces(buf,"TIMEPRICE"));
                ((Auctioneer)E).setTimedListingPct(CMLib.xml().getDoubleFromPieces(buf,"TIMEPCT"));
                //((Auctioneer)E).setLiveFinalCutPct(CMLib.xml().getDoubleFromPieces(buf,"LIVECUT"));
                ((Auctioneer)E).setTimedFinalCutPct(CMLib.xml().getDoubleFromPieces(buf,"TIMECUT"));
                ((Auctioneer)E).setMaxTimedAuctionDays(CMLib.xml().getIntFromPieces(buf,"MAXADAYS"));
                ((Auctioneer)E).setMinTimedAuctionDays(CMLib.xml().getIntFromPieces(buf,"MINADAYS"));
            }

			if(E instanceof Deity)
			{
				Deity godmob=(Deity)E;
				godmob.setClericRequirements(CMLib.xml().getValFromPieces(buf,"CLEREQ"));
				godmob.setWorshipRequirements(CMLib.xml().getValFromPieces(buf,"WORREQ"));
				godmob.setClericRitual(CMLib.xml().getValFromPieces(buf,"CLERIT"));
				godmob.setWorshipRitual(CMLib.xml().getValFromPieces(buf,"WORRIT"));
				godmob.setClericSin(CMLib.xml().getValFromPieces(buf,"CLERSIT"));
				godmob.setWorshipSin(CMLib.xml().getValFromPieces(buf,"WORRSIT"));
				godmob.setClericPowerup(CMLib.xml().getValFromPieces(buf,"CLERPOW"));
                godmob.setServiceRitual(CMLib.xml().getValFromPieces(buf,"SVCRIT"));

				Vector V=CMLib.xml().getRealContentsFromPieces(buf,"BLESSINGS");
				if(V==null)
				{
					Log.errOut("CoffeeMaker","Error parsing 'BLESSINGS' of "+identifier(E,null)+".  Load aborted");
					return;
				}
				for(int i=0;i<V.size();i++)
				{
					XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
					if((!ablk.tag.equalsIgnoreCase("BLESS"))||(ablk.contents==null))
					{
						Log.errOut("CoffeeMaker","Error parsing 'BLESS' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					Ability newOne=CMClass.getAbility(CMLib.xml().getValFromPieces(ablk.contents,"BLCLASS"));
					if(newOne==null)
					{
						Log.errOut("CoffeeMaker","Unknown bless "+CMLib.xml().getValFromPieces(ablk.contents,"BLCLASS")+" on "+identifier(E,null)+", skipping.");
						continue;
					}
                    boolean clericsOnly=CMLib.xml().getBoolFromPieces(ablk.contents,"BLONLY");
					Vector adat=CMLib.xml().getRealContentsFromPieces(ablk.contents,"BLDATA");
					if(adat==null)
					{
						Log.errOut("CoffeeMaker","Error parsing 'BLESS DATA' of "+identifier(E,null)+".  Load aborted");
						return;
					}
					setPropertiesStr(newOne,adat,true);
					godmob.addBlessing(newOne,clericsOnly);
				}
				V=CMLib.xml().getRealContentsFromPieces(buf,"CURSES");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
						if((!ablk.tag.equalsIgnoreCase("CURSE"))||(ablk.contents==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						Ability newOne=CMClass.getAbility(CMLib.xml().getValFromPieces(ablk.contents,"CUCLASS"));
						if(newOne==null)
						{
							Log.errOut("CoffeeMaker","Unknown curse "+CMLib.xml().getValFromPieces(ablk.contents,"CUCLASS")+" on "+identifier(E,null)+", skipping.");
							continue;
						}
                        boolean clericsOnly=CMLib.xml().getBoolFromPieces(ablk.contents,"CUONLY");
						Vector adat=CMLib.xml().getRealContentsFromPieces(ablk.contents,"CUDATA");
						if(adat==null)
						{
							Log.errOut("CoffeeMaker","Error parsing 'CURSE DATA' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addCurse(newOne,clericsOnly);
					}
				}
				V=CMLib.xml().getRealContentsFromPieces(buf,"POWERS");
				if(V!=null)
				{
					for(int i=0;i<V.size();i++)
					{
						XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
						if((!ablk.tag.equalsIgnoreCase("POWER"))||(ablk.contents==null))
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						Ability newOne=CMClass.getAbility(CMLib.xml().getValFromPieces(ablk.contents,"POCLASS"));
						if(newOne==null)
						{
							Log.errOut("CoffeeMaker","Unknown power "+CMLib.xml().getValFromPieces(ablk.contents,"POCLASS")+" on "+identifier(E,null)+", skipping.");
							continue;
						}
						Vector adat=CMLib.xml().getRealContentsFromPieces(ablk.contents,"PODATA");
						if(adat==null)
						{
							Log.errOut("CoffeeMaker","Error parsing 'POWER DATA' of "+identifier(E,null)+".  Load aborted");
							return;
						}
						setPropertiesStr(newOne,adat,true);
						godmob.addPower(newOne);
					}
				}
			}
			Vector V9=CMParms.parseSemicolons(CMLib.xml().getValFromPieces(buf,"TATTS"),true);
			while(((MOB)E).numTattoos()>0)((MOB)E).delTattoo(((MOB)E).fetchTattoo(0));
			for(int v=0;v<V9.size();v++) ((MOB)E).addTattoo((String)V9.elementAt(v));

			V9=CMParms.parseSemicolons(CMLib.xml().getValFromPieces(buf,"EDUS"),true);
			while(((MOB)E).numExpertises()>0)((MOB)E).delExpertise(((MOB)E).fetchExpertise(0));
			for(int v=0;v<V9.size();v++) ((MOB)E).addExpertise((String)V9.elementAt(v));

			if(E instanceof ShopKeeper)
				populateShops(E,buf);
		}
	}

	public String getPlayerXML(MOB mob,
	        						  HashSet custom,
	        						  HashSet files)
	{
		if(mob==null) return "";
		if(mob.Name().length()==0) return "";
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return "";

		String strStartRoomID=(mob.getStartRoom()!=null)?CMLib.map().getExtendedRoomID(mob.getStartRoom()):"";
		String strOtherRoomID=(mob.location()!=null)?CMLib.map().getExtendedRoomID(mob.location()):"";
		StringBuffer pfxml=new StringBuffer(pstats.getXML());
		if(mob.numTattoos()>0)
		{
			pfxml.append("<TATTS>");
			String s=null;
			for(int i=0;i<mob.numTattoos();i++)
			{
			    s=mob.fetchTattoo(i);
			    if(s.startsWith("<TATTS>"))
			        pfxml.append(s.substring(7)+";");
			    else
			        pfxml.append(s+";");
			}
			pfxml.append("</TATTS>");
		}
		if(mob.numExpertises()>0)
		{
			pfxml.append("<EDUS>");
			for(int i=0;i<mob.numExpertises();i++)
				pfxml.append(mob.fetchExpertise(i)+";");
			pfxml.append("</EDUS>");
		}
		pfxml.append(CMLib.xml().convertXMLtoTag("IMG",mob.rawImage()));

		StringBuffer str=new StringBuffer("");
		str.append(CMLib.xml().convertXMLtoTag("NAME",mob.Name()));
		str.append(CMLib.xml().convertXMLtoTag("PASS",pstats.password()));
		str.append(CMLib.xml().convertXMLtoTag("CLASS",mob.baseCharStats().getMyClassesStr()));
		str.append(CMLib.xml().convertXMLtoTag("RACE",mob.baseCharStats().getMyRace().ID()));
		str.append(CMLib.xml().convertXMLtoTag("GEND",""+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER))));
		for(int i : CharStats.CODES.BASE())
			str.append(CMLib.xml().convertXMLtoTag(CMStrings.limit(CharStats.CODES.NAME(i),3),mob.baseCharStats().getStat(i)));
		str.append(CMLib.xml().convertXMLtoTag("HIT",mob.baseState().getHitPoints()));
		str.append(CMLib.xml().convertXMLtoTag("LVL",mob.baseCharStats().getMyLevelsStr()));
		str.append(CMLib.xml().convertXMLtoTag("MANA",mob.baseState().getMana()));
		str.append(CMLib.xml().convertXMLtoTag("MOVE",mob.baseState().getMovement()));
		str.append(CMLib.xml().convertXMLtoTag("EXP",mob.getExperience()));
		str.append(CMLib.xml().convertXMLtoTag("EXLV",mob.getExpNextLevel()));
		str.append(CMLib.xml().convertXMLtoTag("WORS",mob.getWorshipCharID()));
		str.append(CMLib.xml().convertXMLtoTag("PRAC",mob.getPractices()));
		str.append(CMLib.xml().convertXMLtoTag("TRAI",mob.getTrains()));
		str.append(CMLib.xml().convertXMLtoTag("AGEH",mob.getAgeHours()));
		str.append(CMLib.xml().convertXMLtoTag("GOLD",mob.getMoney()));
		str.append(CMLib.xml().convertXMLtoTag("WIMP",mob.getWimpHitPoint()));
		str.append(CMLib.xml().convertXMLtoTag("QUES",mob.getQuestPoint()));
		str.append(CMLib.xml().convertXMLtoTag("ROID",strStartRoomID+"||"+strOtherRoomID));
		str.append(CMLib.xml().convertXMLtoTag("DATE",pstats.lastDateTime()));
		str.append(CMLib.xml().convertXMLtoTag("CHAN",pstats.getChannelMask()));
		str.append(CMLib.xml().convertXMLtoTag("ATTA",mob.baseEnvStats().attackAdjustment()));
		str.append(CMLib.xml().convertXMLtoTag("AMOR",mob.baseEnvStats().armor()));
		str.append(CMLib.xml().convertXMLtoTag("DAMG",mob.baseEnvStats().damage()));
		str.append(CMLib.xml().convertXMLtoTag("BTMP",mob.getBitmap()));
		str.append(CMLib.xml().convertXMLtoTag("LEIG",mob.getLiegeID()));
		str.append(CMLib.xml().convertXMLtoTag("HEIT",mob.baseEnvStats().height()));
		str.append(CMLib.xml().convertXMLtoTag("WEIT",mob.baseEnvStats().weight()));
		str.append(CMLib.xml().convertXMLtoTag("PRPT",CMLib.xml().parseOutAngleBrackets(pstats.getPrompt())));
		str.append(CMLib.xml().convertXMLtoTag("COLR",pstats.getColorStr()));
		str.append(CMLib.xml().convertXMLtoTag("CLAN",mob.getClanID()));
		str.append(CMLib.xml().convertXMLtoTag("LSIP",pstats.lastIP()));
		str.append(CMLib.xml().convertXMLtoTag("CLRO",mob.getClanRole()));
		str.append(CMLib.xml().convertXMLtoTag("EMAL",pstats.getEmail()));
		str.append(CMLib.xml().convertXMLtoTag("PFIL",pfxml.toString()));
		str.append(CMLib.xml().convertXMLtoTag("SAVE",mob.baseCharStats().getNonBaseStatsAsString()));
		str.append(CMLib.xml().convertXMLtoTag("DESC",mob.description()));

		str.append(getExtraEnvPropertiesStr(mob));

		str.append(getGenMobAbilities(mob));

        str.append(getGenScripts(mob,true));

		str.append(getGenMobInventory(mob));

		str.append(getFactionXML(mob));

		StringBuffer fols=new StringBuffer("");
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB thisMOB=mob.fetchFollower(f);
			if((thisMOB!=null)&&(thisMOB.isMonster())&&(!thisMOB.isPossessing()))
			{
				fols.append("<FOLLOWER>");
				fols.append(CMLib.xml().convertXMLtoTag("FCLAS",CMClass.classID(thisMOB)));
				fols.append(CMLib.xml().convertXMLtoTag("FTEXT",thisMOB.text()));
				fols.append(CMLib.xml().convertXMLtoTag("FLEVL",thisMOB.baseEnvStats().level()));
				fols.append(CMLib.xml().convertXMLtoTag("FABLE",thisMOB.baseEnvStats().ability()));
				fols.append("</FOLLOWER>");
			}
		}
		str.append(CMLib.xml().convertXMLtoTag("FOLLOWERS",fols.toString()));
		if((mob.baseCharStats().getMyRace().isGeneric())
		&&(!custom.contains(mob.baseCharStats().getMyRace())))
		   custom.add(mob.baseCharStats().getMyRace());
		for(int c=0;c<mob.baseCharStats().numClasses();c++)
		{
			CharClass C=mob.baseCharStats().getMyClass(c);
			if((C.isGeneric())&&(!custom.contains(C)))
				custom.add(C);
		}
		fillFileSet(mob,files);
		return str.toString();
	}

	public String addPLAYERsFromXML(String xmlBuffer,
										   Vector addHere,
										   Session S)
	{
		Vector xml=CMLib.xml().parseAllXML(xmlBuffer);
		if(xml==null) return unpackErr("PLAYERs","null 'xml'");
		Vector mV=CMLib.xml().getRealContentsFromPieces(xml,"PLAYERS");
		if(mV==null) return unpackErr("PLAYERs","null 'mV'");
		for(int m=0;m<mV.size();m++)
		{
			XMLLibrary.XMLpiece mblk=(XMLLibrary.XMLpiece)mV.elementAt(m);
			if((!mblk.tag.equalsIgnoreCase("PLAYER"))||(mblk.contents==null))
				return unpackErr("PLAYERs","bad 'mblk'");
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			mob.setName(CMLib.xml().getValFromPieces(mblk.contents,"NAME"));
			mob.playerStats().setPassword(CMLib.xml().getValFromPieces(mblk.contents,"PASS"));
			mob.baseCharStats().setMyClasses(CMLib.xml().getValFromPieces(mblk.contents,"CLASS"));
			mob.baseCharStats().setMyLevels(CMLib.xml().getValFromPieces(mblk.contents,"LVL"));
			int level=0;
			for(int i=0;i<mob.baseCharStats().numClasses();i++)
				level+=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(i));
			mob.baseEnvStats().setLevel(level);
			mob.baseCharStats().setMyRace(CMClass.getRace(CMLib.xml().getValFromPieces(mblk.contents,"RACE")));
			mob.baseCharStats().setStat(CharStats.STAT_GENDER,CMLib.xml().getValFromPieces(mblk.contents,"GEND").charAt(0));
			for(int i : CharStats.CODES.BASE())
				mob.baseCharStats().setStat(i,CMLib.xml().getIntFromPieces(mblk.contents,CMStrings.limit(CharStats.CODES.NAME(i),3)));
			mob.baseState().setHitPoints(CMLib.xml().getIntFromPieces(mblk.contents,"HIT"));
			mob.baseState().setMana(CMLib.xml().getIntFromPieces(mblk.contents,"MANA"));
			mob.baseState().setMovement(CMLib.xml().getIntFromPieces(mblk.contents,"MOVE"));
			String alignStr=CMLib.xml().getValFromPieces(mblk.contents,"ALIG");
			if((alignStr.length()>0)&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
			    CMLib.factions().setAlignmentOldRange(mob,CMath.s_int(alignStr));
			mob.setExperience(CMLib.xml().getIntFromPieces(mblk.contents,"EXP"));
			mob.setExpNextLevel(CMLib.xml().getIntFromPieces(mblk.contents,"EXLV"));
			mob.setWorshipCharID(CMLib.xml().getValFromPieces(mblk.contents,"WORS"));
			mob.setPractices(CMLib.xml().getIntFromPieces(mblk.contents,"PRAC"));
			mob.setTrains(CMLib.xml().getIntFromPieces(mblk.contents,"TRAI"));
			mob.setAgeHours(CMLib.xml().getIntFromPieces(mblk.contents,"AGEH"));
			mob.setWimpHitPoint(CMLib.xml().getIntFromPieces(mblk.contents,"WIMP"));
			mob.setQuestPoint(CMLib.xml().getIntFromPieces(mblk.contents,"QUES"));
			String roomID=CMLib.xml().getValFromPieces(mblk.contents,"ROID");
			if(roomID==null) roomID="";
			int x=roomID.indexOf("||");
			if(x>=0)
			{
				mob.setLocation(CMLib.map().getRoom(roomID.substring(x+2)));
				roomID=roomID.substring(0,x);
			}
			mob.setStartRoom(CMLib.map().getRoom(roomID));
			mob.playerStats().setLastDateTime(CMLib.xml().getLongFromPieces(mblk.contents,"DATE"));
			mob.playerStats().setChannelMask(CMLib.xml().getIntFromPieces(mblk.contents,"CHAN"));
			mob.baseEnvStats().setAttackAdjustment(CMLib.xml().getIntFromPieces(mblk.contents,"ATTA"));
			mob.baseEnvStats().setArmor(CMLib.xml().getIntFromPieces(mblk.contents,"AMOR"));
			mob.baseEnvStats().setDamage(CMLib.xml().getIntFromPieces(mblk.contents,"DAMG"));
			mob.setBitmap(CMLib.xml().getIntFromPieces(mblk.contents,"BTMP"));
			mob.setLiegeID(CMLib.xml().getValFromPieces(mblk.contents,"LEIG"));
			mob.baseEnvStats().setHeight(CMLib.xml().getIntFromPieces(mblk.contents,"HEIT"));
			mob.baseEnvStats().setWeight(CMLib.xml().getIntFromPieces(mblk.contents,"WEIT"));
			mob.playerStats().setPrompt(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(mblk.contents,"PRPT")));
			String colorStr=CMLib.xml().getValFromPieces(mblk.contents,"COLR");
			if((colorStr!=null)&&(colorStr.length()>0)&&(!colorStr.equalsIgnoreCase("NULL")))
				mob.playerStats().setColorStr(colorStr);
			mob.setClanID(CMLib.xml().getValFromPieces(mblk.contents,"CLAN"));
			mob.playerStats().setLastIP(CMLib.xml().getValFromPieces(mblk.contents,"LSIP"));
			mob.setClanRole(CMLib.xml().getIntFromPieces(mblk.contents,"CLRO"));
			mob.playerStats().setEmail(CMLib.xml().getValFromPieces(mblk.contents,"EMAL"));
			String buf=CMLib.xml().getValFromPieces(mblk.contents,"CMPFIL");
			mob.playerStats().setXML(buf);
			Vector V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"TATTS"),true);
			while(mob.numTattoos()>0)mob.delTattoo(mob.fetchTattoo(0));
			for(int v=0;v<V9.size();v++) mob.addTattoo((String)V9.elementAt(v));
			V9=CMParms.parseSemicolons(CMLib.xml().returnXMLValue(buf,"EDUS"),true);
			while(mob.numExpertises()>0)mob.delExpertise(mob.fetchExpertise(0));
			for(int v=0;v<V9.size();v++) mob.addExpertise((String)V9.elementAt(v));
			mob.baseCharStats().setNonBaseStatsFromString(CMLib.xml().getValFromPieces(mblk.contents,"SAVE"));
			mob.setDescription(CMLib.xml().getValFromPieces(mblk.contents,"DESC"));
			mob.setImage(CMLib.xml().returnXMLValue(buf,"IMG"));

			setExtraEnvProperties(mob,mblk.contents);

			setGenMobAbilities(mob,mblk.contents);

            setGenScripts(mob,mblk.contents,true);

			setGenMobInventory(mob,mblk.contents);

			setFactionFromXML(mob,mblk.contents);

			Vector iV=CMLib.xml().getRealContentsFromPieces(mblk.contents,"FOLLOWERS");
			if(iV==null) return unpackErr("PFols","null 'iV'");
			for(int i=0;i<iV.size();i++)
			{
				XMLLibrary.XMLpiece fblk=(XMLLibrary.XMLpiece)iV.elementAt(i);
				if((!fblk.tag.equalsIgnoreCase("FOLLOWER"))||(fblk.contents==null))
					return unpackErr("PFols","??"+fblk.tag);
				String mobClass=CMLib.xml().getValFromPieces(fblk.contents,"FCLAS");
				MOB newFollower=CMClass.getMOB(mobClass);
				if(newFollower==null) return unpackErr("PFols","null 'iClass': "+mobClass);
				newFollower.baseEnvStats().setLevel(CMLib.xml().getIntFromPieces(fblk.contents,"FLEVL"));
				newFollower.baseEnvStats().setAbility(CMLib.xml().getIntFromPieces(fblk.contents,"FABLE"));
				newFollower.setMiscText(CMLib.xml().getValFromPieces(fblk.contents,"FTEXT"));
				newFollower.recoverCharStats();
				newFollower.recoverEnvStats();
				newFollower.recoverMaxState();
				newFollower.resetToMaxState();
				mob.addFollower(newFollower,-1);
			}

			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			mob.resetToMaxState();
			addHere.addElement(mob);
		}
		return "";
	}



	public String getExtraEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");

        if(E instanceof Economics)
        {
            text.append(CMLib.xml().convertXMLtoTag("PREJFC",((Economics)E).prejudiceFactors()));
            text.append(CMLib.xml().convertXMLtoTag("IGNMSK",((Economics)E).ignoreMask()));
            text.append(CMLib.xml().convertXMLtoTag("BUDGET",((Economics)E).budget()));
            text.append(CMLib.xml().convertXMLtoTag("DEVALR",((Economics)E).devalueRate()));
            text.append(CMLib.xml().convertXMLtoTag("INVRER",((Economics)E).invResetRate()));
            String[] prics=((Economics)E).itemPricingAdjustments();
            if(prics.length==0) text.append("<IPRICS />");
            else
            {
                text.append("<IPRICS>");
                for(int p=0;p<prics.length;p++)
                    text.append(CMLib.xml().convertXMLtoTag("IPRIC",CMLib.xml().parseOutAngleBrackets(prics[p])));
                text.append("</IPRICS>");
            }

        }

		text.append(CMLib.xml().convertXMLtoTag("IMG",E.rawImage()));

		StringBuffer behaviorstr=new StringBuffer("");
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
			{
				behaviorstr.append("<BHAVE>");
				behaviorstr.append(CMLib.xml().convertXMLtoTag("BCLASS",CMClass.classID(B)));
				behaviorstr.append(CMLib.xml().convertXMLtoTag("BPARMS",CMLib.xml().parseOutAngleBrackets(B.getParms())));
				behaviorstr.append("</BHAVE>");
			}
		}
		text.append(CMLib.xml().convertXMLtoTag("BEHAVES",behaviorstr.toString()));

		StringBuffer affectstr=new StringBuffer("");
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)&&(A.savable()))
			{
				affectstr.append("<AFF>");
				affectstr.append(CMLib.xml().convertXMLtoTag("ACLASS",CMClass.classID(A)));
				affectstr.append(CMLib.xml().convertXMLtoTag("ATEXT",CMLib.xml().parseOutAngleBrackets(A.text())));
				affectstr.append("</AFF>");
			}
		}
		text.append(CMLib.xml().convertXMLtoTag("AFFECS",affectstr.toString()));
		String[] codes=E.getStatCodes();
		for(int i=E.getSaveStatIndex();i<codes.length;i++)
			text.append(CMLib.xml().convertXMLtoTag(codes[i].toUpperCase(),E.getStat(codes[i].toUpperCase())));
		return text.toString();
	}

	public void fillFileSet(Vector V, HashSet H)
	{
	    if(H==null) return;
	    if(V==null) return;
	    for(int v=0;v<V.size();v++)
	        if((!H.contains(V.elementAt(v)))
            &&(V.elementAt(v) instanceof String))
	            H.add(V.elementAt(v));
	}
	public void fillFileSet(Environmental E, HashSet H)
	{
	    if(E==null) return;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null) fillFileSet(B.externalFiles(),H);
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)&&(A.savable())) fillFileSet(A.externalFiles(),H);
		}
        for(int m=0;m<E.numScripts();m++)
        {
            ScriptingEngine S=E.fetchScript(m);
            if(S!=null) fillFileSet(S.externalFiles(),H);
        }
		if(E instanceof MOB)
		{
		    MOB M=(MOB)E;
		    for(int i=0;i<M.inventorySize();i++)
		        fillFileSet(M.fetchInventory(i),H);
		}
		if(E instanceof ShopKeeper)
		{
		    Vector shop=((ShopKeeper)E).getShop().getStoreInventory();
		    for(int i=0;i<shop.size();i++)
		        fillFileSet((Environmental)shop.elementAt(i),H);
		}
	}

	public String getEnvStatsStr(EnvStats E)
	{
		return E.ability()+"|"+
				E.armor()+"|"+
				E.attackAdjustment()+"|"+
				E.damage()+"|"+
				E.disposition()+"|"+
				E.level()+"|"+
				E.rejuv()+"|"+
				E.speed()+"|"+
				E.weight()+"|"+
				E.height()+"|"+
				E.sensesMask()+"|";
	}
	public String getCharStateStr(CharState E)
	{
		return E.getFatigue()+"|"+
				E.getHitPoints()+"|"+
				E.getHunger()+"|"+
				E.getMana()+"|"+
				E.getMovement()+"|"+
				E.getThirst()+"|";
	}
	public String getCharStatsStr(CharStats E)
	{
		StringBuffer str=new StringBuffer("");
		for(int i : CharStats.CODES.ALL())
			str.append(E.getStat(i)+"|");
		return str.toString();
	}

	public String getEnvPropertiesStr(Environmental E)
	{
		StringBuffer text=new StringBuffer("");
		text.append(CMLib.xml().convertXMLtoTag("NAME",E.Name()));
		text.append(CMLib.xml().convertXMLtoTag("DESC",E.description()));
		text.append(CMLib.xml().convertXMLtoTag("DISP",E.displayText()));
		text.append(CMLib.xml().convertXMLtoTag("PROP",getEnvStatsStr(E.baseEnvStats())));
		text.append(getExtraEnvPropertiesStr(E));
        text.append(getGenScripts(E,false));
		return text.toString();
	}

	public void setCharStats(CharStats E, String props)
	{
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
		{
			try
			{
				E.setStat(x,Integer.valueOf(props.substring(0,y)).intValue());
			}
			catch(Exception e)
			{
				E.setStat(x,CMath.s_int(props.substring(0,y)));
			}
			x++;
			props=props.substring(y+1);
		}
	}
	public void setCharState(CharState E, String props)
	{
		int[] nums=new int[6];
		int x=0;
		for(int y=props.indexOf("|");y>=0;y=props.indexOf("|"))
		{
			try
			{
				nums[x]=Integer.valueOf(props.substring(0,y)).intValue();
			}
			catch(Exception e)
			{
				nums[x]=CMath.s_int(props.substring(0,y));
			}
			x++;
			props=props.substring(y+1);
		}
		E.setFatigue(nums[0]);
		E.setHitPoints(nums[1]);
		E.setHunger(nums[2]);
		E.setMana(nums[3]);
		E.setMovement(nums[4]);
		E.setThirst(nums[5]);
	}
	public void setEnvStats(EnvStats E, String props)
	{
		double[] nums=new double[11];
		int x=0;
		int lastBar=0;
		for(int y=0;y<props.length();y++)
			if(props.charAt(y)=='|')
			{
				try{nums[x]=Double.valueOf(props.substring(lastBar,y)).doubleValue();}
				catch(Exception e){nums[x]=(double)CMath.s_int(props.substring(lastBar,y));}
				x++;
				lastBar=y+1;
			}
		if(lastBar<props.length())
		{
			try{nums[x]=Double.valueOf(props.substring(lastBar)).doubleValue();}
			catch(Exception e){nums[x]=(double)CMath.s_int(props.substring(lastBar));}
		}
		E.setAbility((int)Math.round(nums[0]));
		E.setArmor((int)Math.round(nums[1]));
		E.setAttackAdjustment((int)Math.round(nums[2]));
		E.setDamage((int)Math.round(nums[3]));
		E.setDisposition((int)Math.round(nums[4]));
		E.setLevel((int)Math.round(nums[5]));
		E.setRejuv((int)Math.round(nums[6]));
		E.setSpeed(nums[7]);
		E.setWeight((int)Math.round(nums[8]));
		E.setHeight((int)Math.round(nums[9]));
		E.setSensesMask((int)Math.round(nums[10]));
	}

	public void setEnvProperties(Environmental E, Vector buf)
	{
		E.setName(CMLib.xml().getValFromPieces(buf,"NAME"));
		E.setDescription(CMLib.xml().getValFromPieces(buf,"DESC"));
		E.setDisplayText(CMLib.xml().getValFromPieces(buf,"DISP"));
		setEnvStats(E.baseEnvStats(),CMLib.xml().getValFromPieces(buf,"PROP"));
		setExtraEnvProperties(E,buf);
        setGenScripts(E,buf,false);
	}

    public String identifier(Environmental E, Environmental parent)
    {
        StringBuffer str=new StringBuffer("");
        if((E instanceof MOB)&&(parent==null))
            parent=((MOB)E).location();
        if((E instanceof Item)&&(parent==null))
            parent=((Item)E).owner();
        if(E instanceof Area)
            return ((Area)E).Name()+" ("+((Area)E).ID()+")";
        if(E instanceof Room)
            str.append(((Room)E).roomID()+" ("+E.ID()+")");
        else
            str.append(E.Name()+" ("+E.ID()+")");
        if(parent!=null)
            return str.toString()+" of "+identifier(parent,null);
        return str.toString();
    }

	public void setExtraEnvProperties(Environmental E, Vector buf)
	{

	    E.setImage(CMLib.xml().getValFromPieces(buf,"IMG"));
        if(E instanceof Economics)
        {
            ((Economics)E).setPrejudiceFactors(CMLib.xml().getValFromPieces(buf,"PREJFC"));
            ((Economics)E).setIgnoreMask(CMLib.xml().getValFromPieces(buf,"IGNMSK"));
            ((Economics)E).setBudget(CMLib.xml().getValFromPieces(buf,"BUDGET"));
            ((Economics)E).setDevalueRate(CMLib.xml().getValFromPieces(buf,"DEVALR"));
            ((Economics)E).setInvResetRate(CMLib.xml().getIntFromPieces(buf,"INVRER"));
            Vector iV=CMLib.xml().getRealContentsFromPieces(buf,"IPRICS");
            if(iV!=null)
            {
                String[] ipric=new String[iV.size()];
                for(int i=0;i<iV.size();i++)
                {
                    XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)iV.elementAt(i);
                    if((!iblk.tag.equalsIgnoreCase("IPRIC"))||(iblk.contents==null))
                    {
                        Log.errOut("CoffeeMaker","Error parsing 'IPRICS' of "+identifier(E,null)+".  Load aborted");
                        continue;
                    }
                    ipric[i]=CMLib.xml().restoreAngleBrackets(iblk.value);
                }
                ((Economics)E).setItemPricingAdjustments(ipric);
            }
        }
		Vector V=CMLib.xml().getRealContentsFromPieces(buf,"BEHAVES");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'BEHAVES' of "+identifier(E,null)+".  Load aborted");
			return;
		}
		for(int i=0;i<V.size();i++)
		{
			XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
			if((!ablk.tag.equalsIgnoreCase("BHAVE"))||(ablk.contents==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'BHAVE' of "+identifier(E,null)+".  Load aborted");
				return;
			}
			Behavior newOne=CMClass.getBehavior(CMLib.xml().getValFromPieces(ablk.contents,"BCLASS"));
			String bparms=CMLib.xml().getValFromPieces(ablk.contents,"BPARMS");
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown behavior "+CMLib.xml().getValFromPieces(ablk.contents,"BCLASS")+" on "+identifier(E,null)+", skipping.");
				continue;
			}
			newOne.setParms(CMLib.xml().restoreAngleBrackets(bparms));
			E.addBehavior(newOne);
		}
        if(E instanceof Area)
        	addAutoPropsToAreaIfNecessary((Area)E);

		V=CMLib.xml().getRealContentsFromPieces(buf,"AFFECS");
		if(V==null)
		{
			Log.errOut("CoffeeMaker","Error parsing 'AFFECS' of "+identifier(E,null)+".  Load aborted");
			return;
		}
		for(int i=0;i<V.size();i++)
		{
			XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)V.elementAt(i);
			if((!ablk.tag.equalsIgnoreCase("AFF"))||(ablk.contents==null))
			{
				Log.errOut("CoffeeMaker","Error parsing 'AFF' of "+identifier(E,null)+".  Load aborted");
				return;
			}
			Ability newOne=CMClass.getAbility(CMLib.xml().getValFromPieces(ablk.contents,"ACLASS"));
			String aparms=CMLib.xml().getValFromPieces(ablk.contents,"ATEXT");
			if(newOne==null)
			{
				Log.errOut("CoffeeMaker","Unknown affect "+CMLib.xml().getValFromPieces(ablk.contents,"ACLASS")+" on "+identifier(E,null)+", skipping.");
				continue;
			}
			newOne.setMiscText(CMLib.xml().restoreAngleBrackets(aparms));
			E.addNonUninvokableEffect(newOne);
		}
		String[] codes=E.getStatCodes();
		for(int i=E.getSaveStatIndex();i<codes.length;i++)
		{
			String val=CMLib.xml().getValFromPieces(buf,codes[i].toUpperCase());
			if(val==null) val="";
			E.setStat(codes[i].toUpperCase(),val);
		}
	}

	public int getGenItemCodeNum(String code)
	{
		if(GENITEMCODESHASH.size()==0)
		{
			for(int i=0;i<GENITEMCODES.length;i++)
				GENITEMCODESHASH.put(GENITEMCODES[i],Integer.valueOf(i));
		}
		if(GENITEMCODESHASH.containsKey(code.toUpperCase()))
			return ((Integer)GENITEMCODESHASH.get(code.toUpperCase())).intValue();
		for(int i=0;i<GENITEMCODES.length;i++)
			if(code.toUpperCase().startsWith(GENITEMCODES[i])) return i;
		return -1;
	}
	public String getGenItemStat(Item I, String code)
	{
		switch(getGenItemCodeNum(code))
		{
		case 0: return I.ID();
		case 1: return ""+I.usesRemaining();
		case 2: return ""+I.baseEnvStats().level();
		case 3: return ""+I.baseEnvStats().ability();
		case 4: return I.Name();
		case 5: return I.displayText();
		case 6: return I.description();
		case 7: return I.rawSecretIdentity();
		case 8: return ""+I.rawProperLocationBitmap();
		case 9: return ""+I.rawLogicalAnd();
		case 10: return ""+I.baseGoldValue();
		case 11: return ""+(CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMREADABLE));
		case 12: return ""+(!CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNODROP));
		case 13: return ""+(!CMath.bset(I.baseEnvStats().sensesMask(),EnvStats.SENSE_ITEMNOREMOVE));
		case 14: return ""+I.material();
		case 15: return getExtraEnvPropertiesStr(I);
		case 16: return ""+I.baseEnvStats().disposition();
		case 17: return ""+I.baseEnvStats().weight();
		case 18: return ""+I.baseEnvStats().armor();
		case 19: return ""+I.baseEnvStats().damage();
		case 20: return ""+I.baseEnvStats().attackAdjustment();
		case 21: return I.readableText();
		case 22: return I.rawImage();
        //case 23: return getGenScripts(I,false);
		}
		return "";
	}
	public void setGenItemStat(Item I, String code, String val)
	{
		switch(getGenItemCodeNum(code))
		{
		case 0: break;
		case 1: I.setUsesRemaining(CMath.s_parseIntExpression(val)); break;
		case 2: I.baseEnvStats().setLevel(CMath.s_parseIntExpression(val)); break;
		case 3: I.baseEnvStats().setAbility(CMath.s_parseIntExpression(val)); break;
		case 4: I.setName(val); break;
		case 5: I.setDisplayText(val); break;
		case 6: I.setDescription(val); break;
		case 7: I.setSecretIdentity(val); break;
		case 8: {
				  if(CMath.isLong(val)||(val.trim().length()==0))
					  I.setRawProperLocationBitmap(CMath.s_long(val)); 
				  else
				  {
					  I.setRawProperLocationBitmap(0);
					  Vector V=CMParms.parseCommas(val,true);
					  Wearable.CODES codes = Wearable.CODES.instance();
					  for(Enumeration e=V.elements();e.hasMoreElements();)
					  {
						  val=(String)e.nextElement();
						  int wornIndex=codes.findDex_ignoreCase(val);
						  if(wornIndex>=0)
							  I.setRawProperLocationBitmap(I.rawProperLocationBitmap()|codes.get(wornIndex));
					  }
				  }
				  break;
				}
		case 9: I.setRawLogicalAnd(CMath.s_bool(val)); break;
		case 10: I.setBaseValue(CMath.s_parseIntExpression(val)); break;
		case 11: CMLib.flags().setReadable(I,CMath.s_bool(val)); break;
		case 12: CMLib.flags().setDroppable(I,CMath.s_bool(val)); break;
		case 13: CMLib.flags().setRemovable(I,CMath.s_bool(val)); break;
		case 14: if(CMath.isInteger(val)||(val.trim().length()==0))
					I.setMaterial(CMath.s_int(val)); 
				 else
				 {
					 int rsc=RawMaterial.CODES.FIND_IgnoreCase(val);
					 if(rsc>=0) I.setMaterial(rsc);
				 }
				 break;
		case 15: {
					 while(I.numEffects()>0)
					 {
						 Ability A=I.fetchEffect(0);
						 if(A!=null){ A.unInvoke(); I.delEffect(A);}
					 }
					 while(I.numBehaviors()>0)
					 {
						 Behavior B=I.fetchBehavior(0);
						 if(B!=null) I.delBehavior(B);
					 }
					 setExtraEnvProperties(I,CMLib.xml().parseAllXML(val));
					 break;
				 }
		case 16:{
				  if(CMath.isInteger(val)||(val.trim().length()==0))
					 I.baseEnvStats().setDisposition(CMath.s_parseIntExpression(val));
				  else
				  {
					  I.baseEnvStats().setDisposition(0);
					  Vector V=CMParms.parseCommas(val,true);
					  for(Enumeration e=V.elements();e.hasMoreElements();)
					  {
						  val=(String)e.nextElement();
						  int dispIndex=CMParms.indexOfIgnoreCase(EnvStats.IS_CODES,val);
						  if(dispIndex>=0)
							  I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|(int)CMath.pow(2,dispIndex));
					  }
				  }
				  break;
		}
		case 17: I.baseEnvStats().setWeight(CMath.s_parseIntExpression(val)); break;
		case 18: I.baseEnvStats().setArmor(CMath.s_parseIntExpression(val)); break;
		case 19: I.baseEnvStats().setDamage(CMath.s_parseIntExpression(val)); break;
		case 20: I.baseEnvStats().setAttackAdjustment(CMath.s_parseIntExpression(val)); break;
		case 21: I.setReadableText(val); break;
		case 22: I.setImage(val); break;
        /*case 23:
        {
            while(I.numScripts()>0)
            {
                ScriptingEngine S=I.fetchScript(0);
                if(S!=null) I.delScript(S);
            }
            setGenScripts(I,CMLib.xml().parseAllXML(val),false);
            break;
        }
        */
		}
	}

	public int getGenMobCodeNum(String code)
	{
		if(GENMOBCODESHASH.size()==0)
		{
			for(int i=0;i<GENMOBCODES.length;i++)
				GENMOBCODESHASH.put(GENMOBCODES[i],Integer.valueOf(i));
		}
		if(GENMOBCODESHASH.containsKey(code.toUpperCase()))
			return ((Integer)GENMOBCODESHASH.get(code.toUpperCase())).intValue();
		for(int i=0;i<GENMOBCODES.length;i++)
			if(code.toUpperCase().startsWith(GENMOBCODES[i])) return i;
		return -1;
	}
	public String getGenMobStat(MOB M, String code)
	{
		switch(getGenMobCodeNum(code))
		{
		case 0: return CMClass.classID(M);
		case 1: return M.baseCharStats().getMyRace().ID();
		case 2: return ""+M.baseEnvStats().level();
		case 3: return ""+M.baseEnvStats().ability();
		case 4: return M.Name();
		case 5: return M.displayText();
		case 6: return M.description();
		case 7: {
		    	String money=""+CMLib.beanCounter().getMoney(M);
		    	//CMLib.beanCounter().clearZeroMoney(M,null);  WHY THE HECK WAS THIS EVER HERE?!?!
		    	return money;
		    	}
		case 8: return ""+M.fetchFaction(CMLib.factions().AlignID());
		case 9: return ""+M.baseEnvStats().disposition();
		case 10: return ""+M.baseEnvStats().sensesMask();
		case 11: return ""+M.baseEnvStats().armor();
		case 12: return ""+M.baseEnvStats().damage();
		case 13: return ""+M.baseEnvStats().attackAdjustment();
		case 14: return ""+M.baseEnvStats().speed();
		case 15: return getExtraEnvPropertiesStr(M);
		case 16: return getGenMobAbilities(M);
		case 17:{
					StringBuffer str=new StringBuffer(getGenMobInventory(M));
					int x=str.indexOf("<IID>");
					while(x>0)
					{
						int y=str.indexOf("</IID>",x);
						if(y>x)	str.delete(x,y+6);
						else break;
						x=str.indexOf("<IID>");
					}
					x=str.indexOf("<ILOC>");
					while(x>0)
					{
						int y=str.indexOf("</ILOC>",x);
						if(y>x)	str.delete(x,y+7);
						else break;
						x=str.indexOf("<ILOC>");
					}
					return str.toString();
				}
		case 18:{StringBuffer str=new StringBuffer("");
				 for(int i=0;i<M.numTattoos();i++)
					 str.append(M.fetchTattoo(i)+";");
				 return str.toString();
				}
		case 19:{StringBuffer str=new StringBuffer("");
				 for(int i=0;i<M.numExpertises();i++)
					 str.append(M.fetchExpertise(i)+";");
				 return str.toString();
				}
		case 20: return M.rawImage();
		case 21: return M.getFactionListing();
		case 22: return ""+M.getMoneyVariation();
        //case 23: return getGenScripts(M,false);
		}
		return "";
	}
	public void setGenMobStat(MOB M, String code, String val)
	{
		switch(getGenMobCodeNum(code))
		{
		case 0: break;
		case 1: M.baseCharStats().setMyRace(CMClass.getRace(val)); break;
		case 2: M.baseEnvStats().setLevel(CMath.s_parseIntExpression(val)); break;
		case 3: M.baseEnvStats().setAbility(CMath.s_parseIntExpression(val)); break;
		case 4: M.setName(val); break;
		case 5: M.setDisplayText(val); break;
		case 6: M.setDescription(val); break;
		case 7: CMLib.beanCounter().setMoney(M,CMath.s_parseIntExpression(val)); break;
		case 8: if(CMath.s_int(val)==Integer.MAX_VALUE)
		    		M.removeFaction(CMLib.factions().AlignID());
				else
		        	M.addFaction(CMLib.factions().AlignID(),CMath.s_parseIntExpression(val));
				break;
		case 9: 
			{
				  if(CMath.isInteger(val)||(val.trim().length()==0))
					 M.baseEnvStats().setDisposition(CMath.s_parseIntExpression(val));
				  else
				  {
					  M.baseEnvStats().setDisposition(0);
					  Vector V=CMParms.parseCommas(val,true);
					  for(Enumeration e=V.elements();e.hasMoreElements();)
					  {
						  val=(String)e.nextElement();
						  int dispIndex=CMParms.indexOfIgnoreCase(EnvStats.IS_CODES,val);
						  if(dispIndex>=0)
							  M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|(int)CMath.pow(2,dispIndex));
					  }
				  }
				  break;
			}
		case 10: 
			{
				  if(CMath.isInteger(val)||(val.trim().length()==0))
					 M.baseEnvStats().setSensesMask(CMath.s_parseIntExpression(val));
				  else
				  {
					  M.baseEnvStats().setSensesMask(0);
					  Vector V=CMParms.parseCommas(val,true);
					  for(Enumeration e=V.elements();e.hasMoreElements();)
					  {
						  val=(String)e.nextElement();
						  int dispIndex=CMParms.indexOfIgnoreCase(EnvStats.CAN_SEE_CODES,val);
						  if(dispIndex>=0)
							  M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|(int)CMath.pow(2,dispIndex));
					  }
				  }
				  break;
			}
		case 11: M.baseEnvStats().setArmor(CMath.s_parseIntExpression(val)); break;
		case 12: M.baseEnvStats().setDamage(CMath.s_parseIntExpression(val)); break;
		case 13: M.baseEnvStats().setAttackAdjustment(CMath.s_parseIntExpression(val)); break;
		case 14: M.baseEnvStats().setSpeed(CMath.s_parseMathExpression(val)); break;
		case 15: {
					 while(M.numEffects()>0)
					 {
						 Ability A=M.fetchEffect(0);
						 if(A!=null){ A.unInvoke(); M.delEffect(A);}
					 }
					 while(M.numBehaviors()>0)
					 {
						 Behavior B=M.fetchBehavior(0);
						 if(B!=null) M.delBehavior(B);
					 }
					 setExtraEnvProperties(M,CMLib.xml().parseAllXML(val));
					 break;
				 }
		case 16:
			{
				String extras=getExtraEnvPropertiesStr(M);
				while(M.numLearnedAbilities()>0)
				{
					Ability A=M.fetchAbility(0);
					if(A!=null) M.delAbility(A);
				}
				setExtraEnvProperties(M,CMLib.xml().parseAllXML(extras));
				setGenMobAbilities(M,CMLib.xml().parseAllXML(val));
				break;
			}
		case 17:
			{
				while(M.inventorySize()>0)
				{
					Item I=M.fetchInventory(0);
					if(I!=null){ I.setOwner(M); I.destroy(); M.delInventory(I);}
				}
				setGenMobInventory(M,CMLib.xml().parseAllXML(val));
			}
			break;
		case 18:
			{
				Vector V9=CMParms.parseSemicolons(val,true);
				while(M.numTattoos()>0)M.delTattoo(M.fetchTattoo(0));
				for(int v=0;v<V9.size();v++) M.addTattoo((String)V9.elementAt(v));
			}
			break;
		case 19:
			{
				Vector V9=CMParms.parseSemicolons(val,true);
				while(M.numExpertises()>0)M.delExpertise(M.fetchExpertise(0));
				for(int v=0;v<V9.size();v++) M.addExpertise((String)V9.elementAt(v));
			}
			break;
		case 20: M.setImage(val); break;
		case 21:
		    {
		    	Vector V10=CMParms.parseSemicolons(val,true);
		    	for(int v=0;v<V10.size();v++)
		    	{
		    	    String s=(String)V10.elementAt(v);
		    	    int x=s.lastIndexOf("(");
		    	    int y=s.lastIndexOf(")");
		    	    if((x>0)&&(y>x))
		    	        M.addFaction(s.substring(0,x),CMath.s_int(s.substring(x+1,y)));
		    	}
		    	break;
		    }
		case 22: M.setMoneyVariation(CMath.s_parseMathExpression(val)); break;
        /*case 23:
        {
            while(M.numScripts()>0)
            {
                ScriptingEngine S=M.fetchScript(0);
                if(S!=null) M.delScript(S);
            }
            setGenScripts(M,CMLib.xml().parseAllXML(val),false);
            break;
        }*/
		}
	}

	public Area copyArea(Area A, String newName)
	{
		Area newArea=(Area)A.copyOf();
		newArea.setName(newName);
		CMLib.database().DBCreateArea(newArea);
		CMLib.map().addArea(newArea);
		Hashtable altIDs=new Hashtable();
		for(Enumeration e=A.getCompleteMap();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			synchronized(("SYNC"+room.roomID()).intern())
			{
				room=CMLib.map().getRoom(room);
				Room newRoom=(Room)room.copyOf();
				newRoom.clearSky();
				if(newRoom instanceof GridLocale)
					((GridLocale)newRoom).clearGrid(null);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					newRoom.rawDoors()[d]=null;
				newRoom.setRoomID(newArea.getNewRoomID(room,-1));
				newRoom.setArea(newArea);
				CMLib.database().DBCreateRoom(newRoom);
				altIDs.put(room.roomID(),newRoom.roomID());
				if(newRoom.numInhabitants()>0)
					CMLib.database().DBUpdateMOBs(newRoom);
				if(newRoom.numItems()>0)
					CMLib.database().DBUpdateItems(newRoom);
			}
		}
		for(Enumeration e=A.getCompleteMap();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			String altID=(String)altIDs.get(room.roomID());
			if(altID==null) continue;
			Room newRoom=CMLib.map().getRoom(altID);
			if(newRoom==null) continue;
			synchronized(("SYNC"+newRoom.roomID()).intern())
			{
				newRoom=CMLib.map().getRoom(newRoom);
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R=room.rawDoors()[d];
					String myRID=null;
					if(R!=null) myRID=(String)altIDs.get(R.roomID());
					Room myR=null;
					if(myRID!=null) myR=CMLib.map().getRoom(myRID);
					newRoom.rawDoors()[d]=myR;
				}
				CMLib.database().DBUpdateExits(newRoom);
				newRoom.getArea().fillInAreaRoom(newRoom);
			}
		}
		return newArea;
	}

	public String getFactionXML(MOB mob)
	{
		StringBuffer facts=new StringBuffer();
		for(Enumeration e=mob.fetchFactions();e.hasMoreElements();) {
			String name=(String)e.nextElement();
			facts.append("<FCTN ID=\""+name+"\">"+mob.fetchFaction(name)+"</FCTN>");
		}
		return CMLib.xml().convertXMLtoTag("FACTIONS",facts.toString());
	}

	public void setFactionFromXML(MOB mob, Vector xml)
	{
	   if(xml!=null) {
		   Vector mV = CMLib.xml().getRealContentsFromPieces(xml,"FACTIONS");
		   if (mV!=null) {
			   for (int m=0;m<mV.size();m++) {
				   XMLLibrary.XMLpiece mblk=(XMLLibrary.XMLpiece) mV.elementAt(m);
				   mob.addFaction(CMLib.xml().getParmValue(mblk.parms,"ID"),Integer.valueOf(mblk.value).intValue());
			   }
		   }
	   }
	}
}
