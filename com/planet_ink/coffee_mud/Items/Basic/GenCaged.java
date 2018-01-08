package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;

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
public class GenCaged extends GenItem implements CagedAnimal
{
	@Override
	public String ID()
	{
		return "GenCaged";
	}

	public GenCaged()
	{
		super();
		setName("a caged creature");
		basePhyStats.setWeight(150);
		setDisplayText("a caged creature sits here.");
		setDescription("");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		setMaterial(RawMaterial.RESOURCE_MEAT);
		recoverPhyStats();
	}

	protected byte[]	readableText=null;

	@Override
	public String readableText()
	{
		return readableText==null?"":CMLib.encoder().decompressString(readableText);
	}

	@Override
	public void setReadableText(String text)
	{
		readableText=(text.trim().length()==0)?null:CMLib.encoder().compressString(text);
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		phyStats().setSensesMask(phyStats.sensesMask() | PhyStats.SENSE_ITEMNOWISH |PhyStats.SENSE_ITEMNORUIN);
	}
	
	@Override
	public boolean cageMe(MOB M)
	{
		if(M==null)
			return false;
		if(!M.isMonster())
			return false;
		name=M.Name();
		displayText=M.displayText();
		setDescription(M.description());
		basePhyStats().setLevel(M.basePhyStats().level());
		basePhyStats().setWeight(M.basePhyStats().weight());
		basePhyStats().setHeight(M.basePhyStats().height());
		final StringBuffer itemstr=new StringBuffer("");
		itemstr.append("<MOBITEM>");
		itemstr.append(CMLib.xml().convertXMLtoTag("MICLASS",CMClass.classID(M)));
		itemstr.append(CMLib.xml().convertXMLtoTag("MISTART",CMLib.map().getExtendedRoomID(M.getStartRoom())));
		itemstr.append(CMLib.xml().convertXMLtoTag("MIDATA",CMLib.coffeeMaker().getPropertiesStr(M,true)));
		itemstr.append("</MOBITEM>");
		setCageText(itemstr.toString());
		recoverPhyStats();
		return true;
	}

	@Override
	public void destroy()
	{
		if((CMSecurity.isDebugging(CMSecurity.DbgFlag.MISSINGKIDS))
		&&(fetchEffect("Age")!=null)
		&&CMath.isInteger(fetchEffect("Age").text())
		&&(CMath.s_int(fetchEffect("Age").text())>Short.MAX_VALUE))
			Log.debugOut("MISSKIDS",new Exception(Name()+" went missing form "+CMLib.map().getDescriptiveExtendedRoomID(CMLib.map().roomLocation(this))));
		super.destroy();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)
			||((msg.tool()==this)&&(msg.target()==container())&&(container()!=null)))
		&&((getCageFlagsBitmap()&CagedAnimal.CAGEFLAG_TO_MOB_PROGRAMMATICALLY)==0)
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_DROP)))
		{
			final MOB M=unCageMe();
			if((M!=null)&&(msg.source().location()!=null))
				M.bringToLife(msg.source().location(),true);
			destroy();
			return;
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public MOB unCageMe()
	{
		MOB M=null;
		if(cageText().length()==0)
			return M;
		final List<XMLLibrary.XMLTag> buf=CMLib.xml().parseAllXML(cageText());
		if(buf==null)
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		final XMLTag iblk=CMLib.xml().getPieceFromPieces(buf,"MOBITEM");
		if((iblk==null)||(iblk.contents()==null))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM'.");
			return M;
		}
		final String itemi=iblk.getValFromPieces("MICLASS");
		final String startr=iblk.getValFromPieces("MISTART");
		final Environmental newOne=CMClass.getMOB(itemi);
		final List<XMLLibrary.XMLTag> idat=iblk.getContentsFromPieces("MIDATA");
		if((idat==null)||(newOne==null)||(!(newOne instanceof MOB)))
		{
			Log.errOut("Caged","Error parsing 'MOBITEM' data.");
			return M;
		}
		CMLib.coffeeMaker().setPropertiesStr(newOne,idat,true);
		M=(MOB)newOne;
		M.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		M.setStartRoom(null);
		if(M.isGeneric())
			CMLib.coffeeMaker().resetGenMOB(M,M.text());
		if((startr.length()>0)&&(!startr.equalsIgnoreCase("null")))
		{
			final Room R=CMLib.map().getRoom(startr);
			if(R!=null)
				M.setStartRoom(R);
		}
		return M;
	}

	@Override
	public String cageText()
	{
		return CMLib.xml().restoreAngleBrackets(readableText());
	}

	@Override
	public void setCageText(String text)
	{
		setReadableText(CMLib.xml().parseOutAngleBrackets(text));
		CMLib.flags().setReadable(this,false);
	}
	
	@Override
	public int getCageFlagsBitmap()
	{
		return basePhyStats().ability();
	}

	@Override
	public void setCageFlagsBitmap(int bitmap)
	{
		basePhyStats.setAbility(bitmap);
		phyStats.setAbility(bitmap);
	}
}
