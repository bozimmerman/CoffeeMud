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
   Copyright 2005-2024 Bo Zimmerman

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
public class GenRecipe extends GenReadable implements RecipesBook
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		if(msg.amITarget( this )
		&& (msg.targetMinor()==CMMsg.TYP_READ))
		{
			if((msg.targetMessage()!=null)
			&& (recipeLines.length>0)
			&&(CMath.isInteger(msg.targetMessage())))
			{
				final int x=CMath.s_int(msg.targetMessage());
				final int z=this.getRecipeCodeLines().length;
				if(x<1)
				{
					msg.source().tell(L("Which recipe?"));
					return false;
				}
				else
				if(x>z)
				{
					if(z==1)
						msg.source().tell(L("There is only 1 recipe."));
					else
						msg.source().tell(L("There are only 1 recipes.",""+z));
					return false;
				}
			}
			else
			if(this.commonSkillID.trim().length()>0)
			{
				final Ability A=CMClass.getAbility(this.commonSkillID);
				if(A==null)
					this.commonSkillID="";
				else
					msg.source().tell(L("This book is only for @x1 recipes.",A.name()));
			}
		}
		if(msg.amITarget(this)
		&&(msg.targetMinor()==CMMsg.TYP_REWRITE))
		{
			if((msg.targetMessage()!=null)
			&&(msg.targetMessage().toUpperCase().startsWith("DELETE "))
			&&(CMath.isInteger(msg.targetMessage().substring(7).trim())))
			{
				final int totRecipes = getRecipeCodeLines().length;
				final int msgNum=CMath.s_int(msg.targetMessage().substring(7).trim());
				if((msgNum <1)||(msgNum>totRecipes))
				{
					msg.source().tell(L("There is no Recipe #@x1",""+(msgNum)));
					return false;
				}
			}
		}
		if(msg.amITarget( this )
		&& (msg.targetMinor()==CMMsg.TYP_WRITE))
		{
			final List<String> recipes=CMParms.parseAny(msg.targetMessage(), "\n\r", true);
			if(recipes.size()>0)
			{
				final int max=this.getTotalRecipePages();
				final int num=this.getRecipeCodeLines().length;
				if((max-num)==0)
				{
					msg.source().tell(L("There are no more pages in this recipe book."));
					return false;
				}
				else
				if((max-num)<recipes.size())
				{
					if((max-num)==1)
						msg.source().tell(L("There is only one more page in this recipe book."));
					else
						msg.source().tell(L("There are only @x1 more pages in this recipe book.",""+(max-num)));
					return false;
				}
				final Ability A=CMClass.getAbility( getCommonSkillID() );
				if(!(A instanceof RecipeDriven))
				{
					msg.source().tell(L("This recipe book is un-prepped."));
					return false;
				}
				for(final String r : recipes)
				{
					final List<String> V=CMParms.parseTabs(r+" ", false);
					if(V.size()<2)
					{
						msg.source().tell(L("Your recipes are clearly bad."));
						return false;
					}
					final RecipeDriven C=(RecipeDriven)A;
					final String components;
					if(C instanceof CraftorAbility)
						components = ((CraftorAbility)C).getDecodedComponentsDescription( msg.source(), V );
					else
						components="";
					if(components.equals("?"))
					{
						msg.source().tell(L("Your recipes are clearly bad."));
						return false;
					}
					else
					{
						if(this.getRecipeCodeLines().length>0)
						{
							boolean found=false;
							for(int i=0;i<this.getRecipeCodeLines().length;i++)
							{
								if(CMParms.parseTabs(this.getRecipeCodeLines()[i]+" ", false).size() == V.size())
								{
									found=true;
									break;
								}
							}
							if(!found)
							{
								msg.source().tell(L("Your recipes might be bad?"));
								return false;
							}
						}
					}
				}
			}
			else
			{
				msg.source().tell(L("Write what recipe?"));
				return false;
			}
		}
		return true;
	}

	protected void executeMsg(final CMMsg msg)
	{
		// the order that these things are checked in should
		// be holy, and etched in stone.
		if(numBehaviors()>0)
		{
			eachBehavior(new EachApplicable<Behavior>()
			{
				@Override
				public final void apply(final Behavior B)
				{
					B.executeMsg(me,msg);
				}
			});
		}
		if(numScripts()>0)
		{
			eachScript(new EachApplicable<ScriptingEngine>()
			{
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.executeMsg(me,msg);
				}
			});
		}
		if(numEffects()>0)
		{
			eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public final void apply(final Ability A)
				{
					A.executeMsg(me, msg);
				}
			});
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget( this )
		&& (msg.targetMinor()==CMMsg.TYP_READ)
		&& (recipeLines.length>0))
		{
			this.executeMsg(msg);
			if((msg.targetMessage()!=null)
			&&(CMath.isInteger(msg.targetMessage())))
			{
				final StringBuilder str = new StringBuilder("");
				final int x=CMath.s_int(msg.targetMessage());
				if((x>0)
				&&(x<=this.getRecipeCodeLines().length))
				{
					final Ability A=CMClass.getAbility( getCommonSkillID() );
					if(A!=null)
						str.append( L("The following recipe is for the @x1 skill:\n\r",A.name()));
					if(A instanceof RecipeDriven)
					{
						final RecipeDriven C=(RecipeDriven)A;
						final List<String> V=CMParms.parseTabs( this.getRecipeCodeLines()[x-1]+" ", false );
						final Pair<String,Integer> nameAndLevel = C.getDecodedItemNameAndLevel( V );
						final String components;
						if(C instanceof CraftorAbility)
							components = ((CraftorAbility)C).getDecodedComponentsDescription( msg.source(), V );
						else
							components = "nothing";

						final String name=CMStrings.replaceAll( nameAndLevel.first, "% ", "");

						str.append( name).append(", level "+nameAndLevel.second);
						if(CMath.s_int(components)>0)
							str.append( L(", which requires @x1 standard components.\n\r",components));
						else
							str.append( L(", which requires: @x1.\n\r",components));
					}
				}
				final CMMsg readMsg=CMClass.getMsg(msg.source(), msg.target(), msg.tool(),
						 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, L("It says '@x1'.",str.toString()),
						 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, this.getRecipeCodeLines()[x-1]+" ",
						 CMMsg.NO_EFFECT, null);
				msg.addTrailerMsg(readMsg);
			}
			else
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
				final StringBuilder rawCodes=new StringBuilder("");
				if(A instanceof CraftorAbility)
				{
					final CraftorAbility C=(CraftorAbility)A;
					int lineNum=1;
					for(final String line : recipeLines)
					{
						rawCodes.append(line).append("\n\r");
						final List<String> V=CMParms.parseTabs( line+" ", false );
						final Pair<String,Integer> nameAndLevel = C.getDecodedItemNameAndLevel( V );
						final String components = C.getDecodedComponentsDescription( msg.source(), V );
						final String name=CMStrings.replaceAll( nameAndLevel.first, "% ", "");

						str.append(lineNum).append(") ").append( name).append(", level "+nameAndLevel.second);
						if(CMath.s_int(components)>0)
							str.append( L(", which requires @x1 standard components.\n\r",components));
						else
							str.append( L(", which requires: @x1.\n\r",components));
						lineNum++;
					}
				}
				final String writing=str.substring( 0, str.length()-2 );
				final CMMsg readMsg=CMClass.getMsg(msg.source(), msg.target(), msg.tool(),
						 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, L("It says '@x1'.",writing),
						 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, rawCodes.toString()+" ",
						 CMMsg.NO_EFFECT, null);
				msg.addTrailerMsg(readMsg);
			}
			return;
		}
		if(msg.amITarget(this)
		&&(msg.targetMinor()==CMMsg.TYP_REWRITE))
		{
			if((msg.targetMessage()!=null)
			&&(msg.targetMessage().toUpperCase().startsWith("DELETE "))
			&&(CMath.isInteger(msg.targetMessage().substring(7).trim())))
			{
				final int totRecipes = getRecipeCodeLines().length;
				final int msgNum=CMath.s_int(msg.targetMessage().substring(7).trim());
				if((msgNum >0)&&(msgNum<=totRecipes))
				{
					final List<String> newLines = new ArrayList<String>(getRecipeCodeLines().length-1);
					for(int i = 0;i<getRecipeCodeLines().length;i++)
					{
						if(i!=(msgNum-1))
							newLines.add(getRecipeCodeLines()[i]);
					}
					setRecipeCodeLines(newLines.toArray(new String[0]));
				}
			}
			return;
		}
		if(msg.amITarget( this )
		&& (msg.targetMinor()==CMMsg.TYP_WRITE))
		{
			this.executeMsg(msg);
			final List<String> recipes=CMParms.parseAny(msg.targetMessage(), "\n\r", true);
			if(recipes.size()>0)
			{
				final int max=this.getTotalRecipePages();
				final int num=this.getRecipeCodeLines().length;
				for(int i=recipes.size()-1;i>=0;i--)
				{
					if(recipes.get(i).trim().length()==0)
						recipes.remove(i);
				}
				if((max-num)>=recipes.size())
				{
					final int startPos = this.recipeLines.length;
					final String[] newRecipeLines = Arrays.copyOf(this.recipeLines, this.recipeLines.length + recipes.size());
					for(int i=0;i<recipes.size();i++)
						newRecipeLines[i+startPos]=recipes.get(i);
					this.setRecipeCodeLines(newRecipeLines);
				}
			}
			return;
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
					if(A instanceof RecipeDriven)
					{
						final List<String> V=CMParms.parseTabs( recipeLines[0], false );
						final Pair<String,Integer> nameAndLevel = ((RecipeDriven)A).getDecodedItemNameAndLevel( V );
						String itemName=CMStrings.replaceAll( nameAndLevel.first, "% ","");
						itemName=CMStrings.replaceAll( itemName, " % ","");
						if(CMClass.getAbilityPrototype(itemName) != null)
							itemName=CMClass.getAbilityPrototype(itemName).Name();
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
	public void setCommonSkillID(final String ID)
	{
		commonSkillID = ID;
	}

	@Override
	public String[] getRecipeCodeLines()
	{
		return recipeLines;
	}

	@Override
	public void setRecipeCodeLines(final String[] lines)
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
	public void setTotalRecipePages(final int numRemaining)
	{
		super.setUsesRemaining(numRemaining);
	}

	private final static String[] MYCODES={"SKILLID","RECIPES","NUMRECIPES"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
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
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
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

	private int getInternalCodeNum(final String code)
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
	public boolean sameAs(final Environmental E)
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

