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
public class EditorMaterialsRequired extends AbilityParmEditorImpl
{
	public EditorMaterialsRequired()
	{
		super("MATERIALS_REQUIRED",CMLib.lang().L("Amount/Cmp"),ParmType.SPECIAL);
	}

	protected final static String[] ADJUSTER_TOKENS = new String[]{"+","-","="};
	protected final static String[] RESISTER_IMMUNER_TOKENS = new String[]{"%",";"};

	protected final static int[] ALL_BUCKET_MATERIAL_CHOICES = new int[]{RawMaterial.MATERIAL_CLOTH, RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_LEATHER,
			RawMaterial.MATERIAL_LIQUID, RawMaterial.MATERIAL_WOODEN, RawMaterial.MATERIAL_PRECIOUS,RawMaterial.MATERIAL_VEGETATION, RawMaterial.MATERIAL_ROCK };
	protected final static int[] ALLOWED_BUCKET_ACODES = new int[]{Ability.ACODE_CHANT,Ability.ACODE_SPELL,Ability.ACODE_PRAYER,Ability.ACODE_SONG,Ability.ACODE_SKILL,
			Ability.ACODE_THIEF_SKILL};
	protected final static int[] ALLOWED_BUCKET_QUALITIES = new int[]{Ability.QUALITY_BENEFICIAL_OTHERS,Ability.QUALITY_BENEFICIAL_SELF,Ability.QUALITY_INDIFFERENT,
			Ability.QUALITY_OK_OTHERS,Ability.QUALITY_OK_SELF};

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return true;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		if(httpReq.isUrlParameter(fieldName+"_WHICH"))
		{
			final String which=httpReq.getUrlParameter(fieldName+"_WHICH");
			if((which.trim().length()==0)||(which.trim().equalsIgnoreCase("AMOUNT")))
				return httpReq.getUrlParameter(fieldName+"_AMOUNT");
			if(which.trim().equalsIgnoreCase("COMPONENT"))
				return httpReq.getUrlParameter(fieldName+"_COMPONENT");
			int x=1;
			final List<AbilityComponent> comps=new Vector<AbilityComponent>();
			while(httpReq.isUrlParameter(fieldName+"_CUST_TYPE_"+x))
			{
				String connector=httpReq.getUrlParameter(fieldName+"_CUST_CONN_"+x);
				final String amt=httpReq.getUrlParameter(fieldName+"_CUST_AMT_"+x);
				final String strVal=httpReq.getUrlParameter(fieldName+"_CUST_STR_"+x);
				final String loc=httpReq.getUrlParameter(fieldName+"_CUST_LOC_"+x);
				final String def=httpReq.getUrlParameter(fieldName+"_CUST_DEF_"+x);
				final String typ=httpReq.getUrlParameter(fieldName+"_CUST_TYPE_"+x);
				final String styp=httpReq.getUrlParameter(fieldName+"_CUST_STYPE_"+x);
				final String con=httpReq.getUrlParameter(fieldName+"_CUST_CON_"+x);
				if(connector==null)
					connector="AND";
				if(connector.equalsIgnoreCase("DEL")||(connector.length()==0))
				{
					x++;
					continue;
				}
				try
				{
					final AbilityComponent able=CMLib.ableComponents().createBlankAbilityComponent("");
					able.setConnector(AbilityComponent.CompConnector.valueOf(connector));
					if(able.getConnector()==CompConnector.MESSAGE)
						able.setMask((def==null)?"":def);
					else
					{
						able.setAmount(CMath.s_int(amt));
						able.setMask((def==null)?"":def);
						able.setTriggererDef("");
						able.setConsumed((con!=null) && con.equalsIgnoreCase("on"));
						able.setLocation(AbilityComponent.CompLocation.valueOf(loc));
						if(CMath.s_valueOf(CompType.class, typ)!=null)
							able.setType(CompType.valueOf(typ), strVal, styp);
					}
					comps.add(able);
				}
				catch(final Exception e)
				{
				}
				x++;
			}
			if(comps.size()>0)
				return CMLib.ableComponents().getAbilityComponentCodedString(comps);
		}
		return oldVal;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		int amt=(int)Math.round(CMath.mul(I.basePhyStats().weight()-1,(A!=null)?A.getItemWeightMultiplier(false):1.0));
		if(amt<1)
			amt=1;
		final Map<Integer,int[]> extraMatsM = extraMaterial( I );
		if((extraMatsM == null) || (extraMatsM.size()==0))
		{
			return ""+amt;
		}
		final String subType = (I instanceof RawMaterial)?((RawMaterial)I).getSubType():"";
		final List<AbilityComponent> comps=new Vector<AbilityComponent>();
		AbilityComponent able=CMLib.ableComponents().createBlankAbilityComponent("");
		able.setConnector(CompConnector.AND);
		able.setAmount(amt);
		able.setMask("");
		able.setTriggererDef("");
		able.setConsumed(true);
		able.setLocation(CompLocation.ONGROUND);
		able.setType(CompType.MATERIAL, Integer.valueOf(I.material() & RawMaterial.MATERIAL_MASK), subType);
		comps.add(able);
		for(final Integer resourceCode : extraMatsM.keySet())
		{
			able=CMLib.ableComponents().createBlankAbilityComponent("");
			able.setConnector(CompConnector.AND);
			able.setAmount(extraMatsM.get(resourceCode)[0]);
			able.setMask("");
			able.setTriggererDef("");
			able.setConsumed(true);
			able.setLocation(CompLocation.ONGROUND);
			able.setType(CompType.RESOURCE, resourceCode, "");
			comps.add(able);
		}
		return CMLib.ableComponents().getAbilityComponentCodedString(comps);
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		String value=webValue(httpReq,parms,oldVal,fieldName);
		if(value.endsWith("$"))
			value = value.substring(0,oldVal.length()-1);
		value = value.trim();
		final String curWhich=httpReq.getUrlParameter(fieldName+"_WHICH");
		int type=0;
		if("COMPONENT".equalsIgnoreCase(curWhich))
			type=1;
		else
		if("EMBEDDED".equalsIgnoreCase(curWhich))
			type=2;
		else
		if("AMOUNT".equalsIgnoreCase(curWhich))
			type=0;
		else
		if(CMLib.ableComponents().getAbilityComponentMap().containsKey(value.toUpperCase().trim()))
			type=1;
		else
		if(value.startsWith("("))
			type=2;
		else
			type=0;

		List<AbilityComponent> comps=null;
		if(type==2)
		{
			final Hashtable<String,List<AbilityComponent>> H=new Hashtable<String,List<AbilityComponent>>();
			final String s="ID="+value;
			CMLib.ableComponents().addAbilityComponent(s, H);
			comps=H.get("ID");
		}
		if(comps==null)
			comps=new ArrayList<AbilityComponent>(1);

		final StringBuffer str = new StringBuffer("<FONT SIZE=-1>");
		str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==0?"CHECKED ":"")+"VALUE=\"AMOUNT\">");
		str.append("\n\rAmount: <INPUT TYPE=TEXT SIZE=3 NAME="+fieldName+"_AMOUNT VALUE=\""+(type!=0?"":value)+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[0].checked=true;\">");
		str.append("\n\r<BR>");
		str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==1?"CHECKED ":"")+"VALUE=\"COMPONENT\">");
		str.append(L("\n\rSkill Components:"));
		str.append("\n\r<SELECT NAME="+fieldName+"_COMPONENT ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[1].checked=true;\">");
		str.append("<OPTION VALUE=\"0\"");
		if((type!=1)||(value.length()==0)||(value.equalsIgnoreCase("0")))
			str.append(" SELECTED");
		str.append(">&nbsp;");
		for(final String S : CMLib.ableComponents().getAbilityComponentMap().keySet())
		{
			str.append("<OPTION VALUE=\""+S+"\"");
			if((type==1)&&(value.equalsIgnoreCase(S)))
				str.append(" SELECTED");
			str.append(">"+S);
		}
		str.append("</SELECT>");
		str.append("\n\r<BR>");
		str.append("<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH "+(type==2?"CHECKED ":"")+"VALUE=\"EMBEDDED\">");
		str.append("\n\rCustom:");
		str.append("\n\r<BR>");
		AbilityComponent comp;
		for(int i=0;i<=comps.size();i++)
		{
			comp=(i<comps.size())?comps.get(i):null;
			if(i>0)
			{
				str.append("\n\r<SELECT NAME="+fieldName+"_CUST_CONN_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
				if(comp!=null)
					str.append("<OPTION VALUE=\"DEL\">DEL");
				else
				if(type==2)
					str.append("<OPTION VALUE=\"\" SELECTED>");
				for(final CompConnector conector : CompConnector.values())
				{
					str.append("<OPTION VALUE=\""+conector.toString()+"\" ");
					if((type==2)&&(comp!=null)&&(conector==comp.getConnector()))
						str.append("SELECTED ");
					str.append(">"+CMStrings.capitalizeAndLower(conector.toString()));
				}
				str.append("</SELECT>");
			}
			str.append("\n\rAmt: <INPUT TYPE=TEXT SIZE=2 NAME="+fieldName+"_CUST_AMT_"+(i+1)+" VALUE=\""+(((type!=2)||(comp==null))?"":Integer.toString(comp.getAmount()))+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
			str.append("\n\r<SELECT NAME="+fieldName+"_CUST_TYPE_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true; ReShow();\">");
			final CompType compType=(comp!=null)?comp.getType():CompType.STRING;
			final String subType=(comp != null)?comp.getSubType():"";
			for(final CompType conn : CompType.values())
			{
				str.append("<OPTION VALUE=\""+conn.toString()+"\" ");
				if(conn==compType)
					str.append("SELECTED ");
				str.append(">"+CMStrings.capitalizeAndLower(conn.toString()));
			}
			str.append("</SELECT>");
			if(compType==CompType.STRING)
				str.append("\n\r<INPUT TYPE=TEXT SIZE=10 NAME="+fieldName+"_CUST_STR_"+(i+1)+" VALUE=\""+(((type!=2)||(comp==null))?"":comp.getStringType())+"\"  ONKEYDOWN=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
			else
			{
				str.append("\n\r<SELECT NAME="+fieldName+"_CUST_STR_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
				if(compType==CompType.MATERIAL)
				{
					final RawMaterial.Material[] M=RawMaterial.Material.values();
					Arrays.sort(M,new Comparator<RawMaterial.Material>()
					{
						@Override
						public int compare(final Material o1, final Material o2)
						{
							return o1.name().compareToIgnoreCase(o2.name());
						}
					});
					for(final RawMaterial.Material m : M)
					{
						str.append("<OPTION VALUE="+m.mask());
						if((type==2)&&(comp!=null)&&(m.mask()==comp.getLongType()))
							str.append(" SELECTED");
						str.append(">"+m.noun());
					}
				}
				else
				if(compType==CompType.RESOURCE)
				{
					final List<Pair<String,Integer>> L=new Vector<Pair<String,Integer>>();
					for(int x=0;x<RawMaterial.CODES.TOTAL();x++)
						L.add(new Pair<String,Integer>(RawMaterial.CODES.NAME(x),Integer.valueOf(RawMaterial.CODES.GET(x))));
					Collections.sort(L,new Comparator<Pair<String,Integer>>()
					{
						@Override
						public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2)
						{
							return o1.first.compareToIgnoreCase(o2.first);
						}
					});
					for(final Pair<String,Integer> p : L)
					{
						str.append("<OPTION VALUE="+p.second);
						if((type==2)&&(comp!=null)&&(p.second.longValue()==comp.getLongType()))
							str.append(" SELECTED");
						str.append(">"+p.first);
					}
				}
				str.append("</SELECT>");
				str.append(" <INPUT TYPE=TEXT SIZE=2 NAME="+fieldName+"_CUST_STYPE_"+(i+1)+" VALUE=\""+subType+"\">");
			}
			str.append("\n\r<SELECT NAME="+fieldName+"_CUST_LOC_"+(i+1)+" ONCHANGE=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
			for(final CompLocation conn : CompLocation.values())
			{
				str.append("<OPTION VALUE=\""+conn.toString()+"\" ");
				if((type==2)&&(comp!=null)&&(conn==comp.getLocation()))
					str.append("SELECTED ");
				str.append(">"+CMStrings.capitalizeAndLower(conn.toString()));
			}
			str.append("</SELECT>");
			str.append("\n\rConsumed:<INPUT TYPE=CHECKBOX NAME="+fieldName+"_CUST_CON_"+(i+1)+" "+((type!=2)||(comp==null)||(!comp.isConsumed())?"":"CHECKED")+"  ONCLICK=\"document.RESOURCES."+fieldName+"_WHICH[2].checked=true;\">");
			if(i<comps.size())
				str.append("\n\r<BR>\n\r");
			else
				str.append("\n\r<a href=\"javascript:ReShow();\">&lt;*&gt;</a>\n\r");
		}
		str.append("<BR>");
		str.append("</FONT>");
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		return new String[] { oldVal };
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		++showNumber[0];
		String str = oldVal;
		while((mob.session()!=null)&&(!mob.session().isStopped()))
		{
			final String help="<AMOUNT>"
				+"\n\rSkill Component: "+CMParms.toListString(CMLib.ableComponents().getAbilityComponentMap().keySet())
				+"\n\rCustom Component: ([DISPOSITION]:[FATE]:[AMOUNT]:[COMPONENT ID]:[MASK]) && ...";
			str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,help).trim();
			if(str.equals(oldVal))
				return oldVal;
			if(CMath.isInteger(str))
				return Integer.toString(CMath.s_int(str));
			if(CMLib.ableComponents().getAbilityComponentMap().containsKey(str.toUpperCase().trim()))
				return str.toUpperCase().trim();
			String error=null;
			if(str.trim().startsWith("("))
			{
				error=CMLib.ableComponents().addAbilityComponent("ID="+str, new Hashtable<String,List<AbilityComponent>>());
				if(error==null)
					return str;
			}
			mob.session().println(L("'@x1' is not an amount of material, a component key, or custom component list@x2.  Please use ? for help.",str,(error==null?"":"("+error+")")));
		}
		return str;
	}

	@Override
	public String defaultValue()
	{
		return "1";
	}

	protected static void addExtraMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Object A, double weight)
	{
		int times = 1;
		if(weight >= 1.0)
		{
			times = (int)Math.round(weight / 1.0);
			weight=.99;
		}
		final MaterialLibrary mats=CMLib.materials();
		final int myBucket = getAppropriateResourceBucket(I,A);
		if(myBucket != RawMaterial.RESOURCE_NOTHING)
		{
			final PairList<Integer,Double> bucket = RawMaterial.CODES.instance().getValueSortedResources(myBucket);
			Integer resourceCode = (bucket.size()==0)
					? Integer.valueOf(CMLib.dice().pick( RawMaterial.CODES.ALL(), I.material() ))
					: bucket.get( (weight>=.99) ? bucket.size()-1 : 0 ).first;
			for (final Pair<Integer, Double> p : bucket)
			{
				if((weight <= p.second.doubleValue())&&(mats.isResourceCodeRoomMapped(p.first.intValue())))
				{
					resourceCode = p.first;
					break;
				}
			}
			int tries=100;
			while((--tries>0)&&(!mats.isResourceCodeRoomMapped(resourceCode.intValue())))
				resourceCode=bucket.get(CMLib.dice().roll(1, bucket.size(), -1)).first;
			resourceCode = Integer.valueOf( resourceCode.intValue() );
			for(int x=0;x<times;x++)
			{
				final int[] amt = extraMatsM.get( resourceCode );
				if(amt == null)
					extraMatsM.put( resourceCode, new int[]{1} );
				else
					amt[0]++;
			}
		}
	}

	protected static void addExtraAbilityMaterial(final Map<Integer,int[]> extraMatsM, final Item I, final Ability A)
	{
		double level = CMLib.ableMapper().lowestQualifyingLevel( A.ID() );
		if( level <= 0.0 )
		{
			level = I.basePhyStats().level();
			if( level <= 0.0 )
				level = 1.0;
			addExtraMaterial(extraMatsM, I, A, CMath.div( level, CMProps.getIntVar( CMProps.Int.LASTPLAYERLEVEL ) ));
		}
		else
		{
			final double levelCap = CMLib.ableMapper().getCalculatedMedianLowestQualifyingLevel();
			addExtraMaterial(extraMatsM, I, A, CMath.div(level , ( levelCap * 2.0)));
		}
	}

	public static Map<Integer,int[]> extraMaterial(final Item I)
	{
		final Map<Integer,int[]> extraMatsM=new TreeMap<Integer,int[]>();
		/*
		 * behaviors/properties of the item
		 */
		for(final Enumeration<Ability> a=I.effects(); a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A.isSavable())
			{
				if((A.abilityCode() & Ability.ALL_ACODES) == Ability.ACODE_PROPERTY)
				{
					if(A instanceof AbilityContainer)
					{
						for(final Enumeration<Ability> a1=((AbilityContainer)A).allAbilities(); a1.hasMoreElements(); )
						{
							addExtraAbilityMaterial(extraMatsM,I,a1.nextElement());
						}
					}
					if(A instanceof TriggeredAffect)
					{
						if((A.flags() & Ability.FLAG_ADJUSTER) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, ADJUSTER_TOKENS );
							if(count == 0)
								count = 1;
							for(int i=0;i<count;i++)
								addExtraAbilityMaterial(extraMatsM,I,A);
						}
						else
						if((A.flags() & (Ability.FLAG_RESISTER | Ability.FLAG_IMMUNER)) != 0)
						{
							int count = CMStrings.countSubstrings( new String[]{A.text()}, RESISTER_IMMUNER_TOKENS );
							if(count == 0)
								count = 1;
							for(int i=0;i<count;i++)
								addExtraAbilityMaterial(extraMatsM,I,A);
						}
					}
				}
				else
				if((CMParms.indexOf(ALLOWED_BUCKET_ACODES, A.abilityCode() & Ability.ALL_ACODES ) >=0)
				&&(CMParms.indexOf( ALLOWED_BUCKET_QUALITIES, A.abstractQuality()) >=0 ))
				{
					addExtraAbilityMaterial(extraMatsM,I,A);
				}
			}
		}
		for(final Enumeration<Behavior> b=I.behaviors(); b.hasMoreElements();)
		{
			final Behavior B=b.nextElement();
			if(B.isSavable())
			{
				addExtraMaterial(extraMatsM, I, B, CMath.div( CMProps.getIntVar( CMProps.Int.LASTPLAYERLEVEL ), I.basePhyStats().level() ));
			}
		}
		return extraMatsM;
	}

	protected static int getAppropriateResourceBucket(final Item I, final Object A)
	{
		final int myMaterial = ((I.material() & RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL) ? RawMaterial.MATERIAL_METAL : (I.material() & RawMaterial.MATERIAL_MASK);
		if(A instanceof Behavior)
			return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_LEATHER, RawMaterial.MATERIAL_VEGETATION}, myMaterial );
		if(A instanceof Ability)
		{
			switch(((Ability)A).abilityCode() & Ability.ALL_ACODES)
			{
				case Ability.ACODE_CHANT:
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_VEGETATION, RawMaterial.MATERIAL_ROCK}, myMaterial );
				case Ability.ACODE_SPELL:
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_WOODEN, RawMaterial.MATERIAL_PRECIOUS}, myMaterial );
				case Ability.ACODE_PRAYER:
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_ROCK}, myMaterial );
				case Ability.ACODE_SONG:
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_LIQUID, RawMaterial.MATERIAL_WOODEN}, myMaterial );
				case Ability.ACODE_THIEF_SKILL:
				case Ability.ACODE_SKILL:
					return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_CLOTH, RawMaterial.MATERIAL_METAL}, myMaterial );
				case Ability.ACODE_PROPERTY:
					if(A instanceof TriggeredAffect)
						return CMLib.dice().pick( new int[]{RawMaterial.MATERIAL_PRECIOUS, RawMaterial.MATERIAL_METAL}, myMaterial );
					break;
				default:
					break;
			}
		}
		return CMLib.dice().pick( ALL_BUCKET_MATERIAL_CHOICES, myMaterial );
	}


}
