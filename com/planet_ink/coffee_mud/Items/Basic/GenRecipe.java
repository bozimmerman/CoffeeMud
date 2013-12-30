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
   Copyright 2000-2014 Bo Zimmerman

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
	public String ID(){	return "GenRecipe";}
	protected String commonSkillID="";
	protected String[] recipeLines=new String[0];
	protected String replaceName = null;
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

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget( this ) && (msg.targetMinor()==CMMsg.TYP_READ) && (super.readableText().length()==0) && (recipeLines.length>0))
		{
			StringBuilder str = new StringBuilder("");
			Ability A=CMClass.getAbility( getCommonSkillID() );
			if(getTotalRecipePages() > 1)
			{
				str.append( name()+" contains "+recipeLines.length+" recipe(s)/schematic(s) out of "+getTotalRecipePages()+" total entries.\n\r");
				if(A!=null) str.append( "The following recipes are for the "+A.name()+" skill:\n\r" );
			}
			else
				if(A!=null) str.append( "The following recipe is for the "+A.name()+" skill:\n\r" );
			if(A instanceof ItemCraftor)
			{
				ItemCraftor C=(ItemCraftor)A;
				for(String line : recipeLines)
				{
					List<String> V=CMParms.parseTabs( line+" ", false );
					Pair<String,Integer> nameAndLevel = C.getDecodedItemNameAndLevel( V );
					String components = C.getDecodedComponentsDescription( msg.source(), V );
					String name=CMStrings.replaceAll( nameAndLevel.first, "% ", "");
					
					str.append( name).append(", level "+nameAndLevel.second);
					if(CMath.s_int(components)>0)
						str.append( ", which requires "+components+" standard components.\n\r");
					else
						str.append( ", which requires: "+components+".\n\r");
				}
			}
			super.setReadableText( str.substring( 0, str.length()-2 ));
		}
		super.executeMsg( myHost, msg );
	}

	public boolean isGeneric(){return true;}
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this,true); 
		super.recoverPhyStats();
		if(this.getTotalRecipePages()==1)
		{
			if(replaceName==null)
			{
				replaceName="";
				int x=Name().indexOf( '%' );
				if((recipeLines!=null)&&(recipeLines.length==1)&&(this.getCommonSkillID().length()>0))
				{
					Ability A=CMClass.getAbility(this.getCommonSkillID());
					if(A instanceof ItemCraftor)
					{
						List<String> V=CMParms.parseTabs( recipeLines[0], false );
						Pair<String,Integer> nameAndLevel = ((ItemCraftor)A).getDecodedItemNameAndLevel( V );
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
	public String getCommonSkillID(){return commonSkillID;}
	public void setCommonSkillID(String ID){commonSkillID=ID;}
	public String[] getRecipeCodeLines(){return recipeLines;}
	public void setRecipeCodeLines(String[] lines)
	{
		recipeLines=lines;
		setReadableText("");
		replaceName = null;
	}
    public int getTotalRecipePages() { return super.usesRemaining(); }
    public void setTotalRecipePages(int numRemaining) { super.setUsesRemaining(numRemaining); }
    
	private final static String[] MYCODES={"SKILLID","RECIPES","NUMRECIPES"};
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+getCommonSkillID();
		case 1: 
			{
				StringBuilder str=new StringBuilder("");
				for(String s : recipeLines)
					str.append(s).append("\n");
				if(str.length()==0) return "";
				String recipeStr = str.toString();
				return recipeStr.substring(0,recipeStr.length()-1);
			}
		case 2: return ""+this.getTotalRecipePages();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setCommonSkillID(val); break;
		case 1: setRecipeCodeLines(CMParms.parseAny(val, '\n', true).toArray(new String[0])); break;
		case 2: int x=CMath.s_int(val); setTotalRecipePages(x>0?x:1); break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] MYCODES=CMProps.getStatCodesList(GenRecipe.MYCODES,this);
		String[] superCodes=GenericBuilder.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenRecipe)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}

