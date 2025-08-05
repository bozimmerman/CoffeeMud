package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorCodedWearLocation extends AbilityParmEditorImpl
{
	public EditorCodedWearLocation()
	{
		super("CODED_WEAR_LOCATION",CMLib.lang().L("Wear Locs"),ParmType.SPECIAL);
	}

	@Override
	public int appliesToClass(final Object o)
	{
		if(o instanceof FalseLimb)
			return -1;
		return ((o instanceof Armor) || (o instanceof MusicalInstrument)) ? 2 : -1;
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return oldVal.trim().length() > 0;
	}

	@Override
	public String defaultValue()
	{
		return "NECK";
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final short[] layerAtt = new short[1];
		final short[] layers = new short[1];
		final long[] wornLoc = new long[1];
		final boolean[] logicalAnd = new boolean[1];
		final double[] hardBonus=new double[1];
		CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
		if(httpReq.isUrlParameter(fieldName+"_WORNDATA"))
		{
			wornLoc[0]=CMath.s_long(httpReq.getUrlParameter(fieldName+"_WORNDATA"));
			for(int i=1;;i++)
				if(httpReq.isUrlParameter(fieldName+"_WORNDATA"+(Integer.toString(i))))
					wornLoc[0]=wornLoc[0]|CMath.s_long(httpReq.getUrlParameter(fieldName+"_WORNDATA"+(Integer.toString(i))));
				else
					break;
			logicalAnd[0] = httpReq.getUrlParameter(fieldName+"_ISTWOHANDED").equalsIgnoreCase("on");
			layers[0] = CMath.s_short(httpReq.getUrlParameter(fieldName+"_LAYER"));
			layerAtt[0] = 0;
			if((httpReq.isUrlParameter(fieldName+"_SEETHRU"))
			&&(httpReq.getUrlParameter(fieldName+"_SEETHRU").equalsIgnoreCase("on")))
				layerAtt[0] |= Armor.LAYERMASK_SEETHROUGH;
			if((httpReq.isUrlParameter(fieldName+"_MULTIWEAR"))
			&&(httpReq.getUrlParameter(fieldName+"_MULTIWEAR").equalsIgnoreCase("on")))
				layerAtt[0] |= Armor.LAYERMASK_MULTIWEAR;
		}
		return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String value = webValue(httpReq,parms,oldVal,fieldName);
		final short[] layerAtt = new short[1];
		final short[] layers = new short[1];
		final long[] wornLoc = new long[1];
		final boolean[] logicalAnd = new boolean[1];
		final double[] hardBonus=new double[1];
		CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,value);
		final StringBuffer str = new StringBuffer("");
		str.append("\n\r<SELECT NAME="+fieldName+"_WORNDATA MULTIPLE>");
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=1;i<codes.total();i++)
		{
			final String climstr=codes.name(i);
			final int mask=(int)CMath.pow(2,i-1);
			str.append("<OPTION VALUE="+mask);
			if((wornLoc[0]&mask)>0)
				str.append(" SELECTED");
			str.append(">"+climstr);
		}
		str.append("</SELECT>");
		str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"on\" "+(logicalAnd[0]?"CHECKED":"")+">Is worn on All above Locations.");
		str.append("<BR>\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_ISTWOHANDED value=\"\" "+(logicalAnd[0]?"":"CHECKED")+">Is worn on ANY of the above Locations.");
		str.append("<BR>\n\rLayer: <INPUT TYPE=TEXT NAME="+fieldName+"_LAYER SIZE=5 VALUE=\""+layers[0]+"\">");
		final boolean seeThru = CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH);
		final boolean multiWear = CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR);
		str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_SEETHRU value=\"on\" "+(seeThru?"CHECKED":"")+">Is see-through.");
		str.append("&nbsp;&nbsp;\n\r<INPUT TYPE=CHECKBOX NAME="+fieldName+"_MULTIWEAR value=\"on\" "+(multiWear?"CHECKED":"")+">Is multi-wear.");
		return str.toString();
	}

	public String reconvert(final short[] layerAtt, final short[] layers, final long[] wornLoc, final boolean[] logicalAnd, final double[] hardBonus)
	{
		final StringBuffer newVal = new StringBuffer("");
		if((layerAtt[0]!=0)||(layers[0]!=0))
		{
			if(CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR))
				newVal.append('M');
			if(CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH))
				newVal.append('S');
			newVal.append(layers[0]);
			newVal.append(':');
		}
		boolean needLink=false;
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int wo=1;wo<codes.total();wo++)
		{
			if(CMath.bset(wornLoc[0],CMath.pow(2,wo-1)))
			{
				if(needLink)
					newVal.append(logicalAnd[0]?"&&":"||");
				needLink = true;
				newVal.append(codes.name(wo).toUpperCase());
			}
		}
		return newVal.toString();
	}

	@Override
	public String convertFromItem(final ItemCraftor C, final Item I)
	{
		if(!(I instanceof Armor))
			return "HELD";
		final Armor A=(Armor)I;
		final boolean[] logicalAnd=new boolean[]{I.rawLogicalAnd()};
		final long[] wornLoc=new long[]{I.rawProperLocationBitmap()};
		final double[] hardBonus=new double[]{0.0};
		final short[] layerAtt=new short[]{A.getLayerAttributes()};
		final short[] layers=new short[]{A.getClothingLayer()};
		return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final ArrayList<String> V = new ArrayList<String>();
		final short[] layerAtt = new short[1];
		final short[] layers = new short[1];
		final long[] wornLoc = new long[1];
		final boolean[] logicalAnd = new boolean[1];
		final double[] hardBonus=new double[1];
		CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
		V.add(""+layers[0]);
		if(CMath.bset(layerAtt[0],Armor.LAYERMASK_SEETHROUGH))
			V.add("Y");
		else
			V.add("N");
		if(CMath.bset(layerAtt[0],Armor.LAYERMASK_MULTIWEAR))
			V.add("Y");
		else
			V.add("N");
		V.add("1");
		V.add("1");
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=0;i<codes.total();i++)
		{
			if(CMath.bset(wornLoc[0],codes.get(i)))
			{
				V.add(""+(i+2));
				V.add(""+(i+2));
			}
		}
		V.add("0");
		return CMParms.toStringArray(V);
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final short[] layerAtt = new short[1];
		final short[] layers = new short[1];
		final long[] wornLoc = new long[1];
		final boolean[] logicalAnd = new boolean[1];
		final double[] hardBonus=new double[1];
		CMLib.ableParms().parseWearLocation(layerAtt,layers,wornLoc,logicalAnd,hardBonus,oldVal);
		CMLib.genEd().wornLayer(mob,layerAtt,layers,++showNumber[0],showFlag);
		CMLib.genEd().wornLocation(mob,wornLoc,logicalAnd,++showNumber[0],showFlag);
		return reconvert(layerAtt,layers,wornLoc,logicalAnd,hardBonus);
	}
}
