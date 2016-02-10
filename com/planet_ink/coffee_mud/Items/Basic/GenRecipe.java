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

/*
   Copyright 2005-2016 Bo Zimmerman

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
public class GenRecipe extends GenReadable implements Recipe
{
	@Override
	public String ID()
	{
		return "GenRecipe";
	}

	protected String	commonSkillID	= "";
	protected String[]	recipeLines		= new String[0];
	protected String	replaceName		= null;

	public GenRecipe()
	{
		super();
		setName("a generic recipe");
		setDisplayText("a generic recipe sits here.");
		setMaterial(RawMaterial.RESOURCE_PAPER);
		setUsesRemaining(1);
		setReadableText("");
		recoverPhyStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget( this ) && (msg.targetMinor()==CMMsg.TYP_READ) && (super.readableText().length()==0) && (recipeLines.length>0))
		{
			final StringBuilder str = new StringBuilder("");
			final Ability A=CMClass.getAbility( getCommonSkillID() );
			if(getTotalRecipePages() > 1)
			{
				str.append( L("@x1 contains @x2 recipe(s)/schematic(s) out of @x3 total entries.\n\r",name(),""+recipeLines.length,""+getTotalRecipePages()));
				if(A!=null)
					str.append( L("The following recipes are for the @x1 skill:\n\r",A.name()));
			}
			else
			{
				if(A!=null)
					str.append( L("The following recipe is for the @x1 skill:\n\r",A.name()));
			}
			if(A instanceof CraftorAbility)
			{
				final CraftorAbility C=(CraftorAbility)A;
				for(final String line : recipeLines)
				{
					final List<String> V=CMParms.parseTabs( line+" ", false );
					final Pair<String,Integer> nameAndLevel = C.getDecodedItemNameAndLevel( V );
					final String components = C.getDecodedComponentsDescription( msg.source(), V );
					final String name=CMStrings.replaceAll( nameAndLevel.first, "% ", "");

					str.append( name).append(", level "+nameAndLevel.second);
					if(CMath.s_int(components)>0)
						str.append( L(", which requires @x1 standard components.\n\r",components));
					else
						str.append( L(", which requires: @x1.\n\r",components));
				}
			}
			super.setReadableText( str.substring( 0, str.length()-2 ));
		}
		super.executeMsg( myHost, msg );
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this,true);
		super.recoverPhyStats();
		if(this.getTotalRecipePages()==1)
		{
			if(replaceName==null)
			{
				replaceName="";
				final int x=Name().indexOf( '%' );
				if((recipeLines!=null)&&(recipeLines.length==1)&&(this.getCommonSkillID().length()>0))
				{
					final Ability A=CMClass.getAbility(this.getCommonSkillID());
					if(A instanceof CraftorAbility)
					{
						final List<String> V=CMParms.parseTabs( recipeLines[0], false );
						final Pair<String,Integer> nameAndLevel = ((CraftorAbility)A).getDecodedItemNameAndLevel( V );
						String itemName=CMStrings.replaceAll( nameAndLevel.first, "% ","");
						itemName=CMStrings.replaceAll( itemName, " % ","");
						if(x>=0)
							replaceName=CMStrings.replaceAll( Name(), "%", itemName );
						else
						if(Name().indexOf(" of ")<0)
							replaceName=Name()+" of "+itemName;
					}
				}
				else
				if(x>=0)
				{
					replaceName=CMStrings.replaceAll( Name(), "%", "" );
				}
			}
			if((replaceName!=null)&&(replaceName.length()>0)&&(phyStats().newName()==null))
				phyStats().setName(replaceName);
		}
	}

	@Override
	public String getCommonSkillID()
	{
		return commonSkillID;
	}

	@Override
	public void setCommonSkillID(String ID)
	{
		commonSkillID = ID;
	}

	@Override
	public String[] getRecipeCodeLines()
	{
		return recipeLines;
	}

	@Override
	public void setRecipeCodeLines(String[] lines)
	{
		recipeLines=lines;
		setReadableText("");
		replaceName = null;
	}

	@Override
	public int getTotalRecipePages()
	{
		return super.usesRemaining();
	}

	@Override
	public void setTotalRecipePages(int numRemaining)
	{
		super.setUsesRemaining(numRemaining);
	}

	private final static String[] MYCODES={"SKILLID","RECIPES","NUMRECIPES"};

	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: 
			return ""+getCommonSkillID();
		case 1:
			{
				final StringBuilder str=new StringBuilder("");
				for(final String s : recipeLines)
					str.append(s).append("\n");
				if(str.length()==0)
					return "";
				final String recipeStr = str.toString();
				return recipeStr.substring(0,recipeStr.length()-1);
			}
		case 2: 
			return ""+this.getTotalRecipePages();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
			case 0:
				setCommonSkillID(val);
				break;
			case 1:
				setRecipeCodeLines(CMParms.parseAny(val, '\n', true).toArray(new String[0]));
				break;
			case 2:
				final int x = CMath.s_int(val);
				setTotalRecipePages(x > 0 ? x : 1);
				break;
			default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenRecipe.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenRecipe))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}

