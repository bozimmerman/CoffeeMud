package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.GenAbility;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class GenTrap extends StdTrap
{
	// data should be stored in a common instance object .. something common to all genability of same id,
	// but diff to others.n  I'm thinking like a DVector, and just have
	private String ID="GenTrap";

	@Override
	public String ID()
	{
		return ID;
	}

	protected List<AbilityComponent> componentsRequirements=new ArrayList<AbilityComponent>();

	private ScriptingEngine	scriptObj		= null;
	private long			scriptParmHash	= 0;

	private static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int V_NAME=0;//S
	private static final int V_DMGF=1;//S
	private static final int V_ACOD=2;//I
	private static final int V_CAFF=3;//I
	private static final int V_CTAR=4;//I
	private static final int V_BOMB=5;//I
	private static final int V_MSGA=6;//S
	private static final int V_MSGT=7;//S
	private static final int V_MSGD=8;//S
	private static final int V_COMP=9;//S
	private static final int V_DMGT=10;//I
	private static final int V_DMGM=11;//I
	private static final int V_ABLA=12;//S
	private static final int V_ABLM=13;//S
	private static final int V_ABLT=14;//I
	private static final int V_SCRP=15;//S
	private static final int V_HELP=16;//S
	private static final int V_LEVL=17;//I

	private static final int NUM_VS=18;//S

	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="an ability";
		// @x1=trapLevel, @x2=abilityCode, @x3=invokerLevel, @x4=targetLevel
		O[V_DMGF]="((@x1+@x2)*(1?6))+1";
		O[V_ACOD]=Integer.valueOf(0);
		O[V_CAFF]=Integer.valueOf(Ability.CAN_ITEMS);
		O[V_CTAR]=Integer.valueOf(0);
		O[V_LEVL]=Integer.valueOf(0);
		O[V_BOMB]=Boolean.FALSE;
		O[V_MSGA]="<S-NAME> avoid(s) setting off a trap!";
		O[V_MSGT]="<S-NAME> set(s) off a trap!";
		O[V_MSGD]="The trap <DAMAGE> <T-NAME>!";
		O[V_COMP]="(inventory:consumed:1:METAL:)";
		O[V_DMGT]=Integer.valueOf(Weapon.TYPE_BURSTING);
		O[V_DMGM]=Integer.valueOf(CMMsg.TYP_FIRE);
		O[V_ABLA]="";
		O[V_ABLM]="";
		O[V_ABLT]=Integer.valueOf(0);
		O[V_SCRP]="";
		O[V_HELP]="<ABILITY>This trap is not yet documented.";
		return O;
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

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenTrap A = this.getClass().getDeclaredConstructor().newInstance();
			A.ID=ID;
			getScripter();
			A.scriptParmHash=scriptParmHash;
			A.trapLevel=((Integer)V(ID,V_LEVL)).intValue();
			if(scriptObj!=null)
			{
				A.scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				A.scriptObj.setScript(scriptObj.getScript());
			}
			else
				A.scriptObj = null;
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenAbility();
	}

	@Override
	protected void cloneFix(final Ability E)
	{
		if(E instanceof GenTrap)
		{
			final GenTrap A=(GenTrap)E;
			A.scriptParmHash=scriptParmHash;
			if(A.scriptObj!=null)
			{
				scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				scriptObj.setScript(A.scriptObj.getScript());
			}
			else
				scriptObj=null;
		}
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	protected int canAffectCode()
	{
		return ((Integer) V(ID, V_CAFF)).intValue();
	}

	@Override
	protected int canTargetCode()
	{
		return ((Integer) V(ID, V_CTAR)).intValue();
	}

	@Override
	public String requiresToSet()
	{
		if(this.componentsRequirements.size()==0)
			return "";
		return CMLib.ableComponents().getAbilityComponentDesc(null,componentsRequirements);
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		if(P==null)
			return null;
		if((mob!=null)
		&&(this.componentsRequirements.size()>0))
		{
			final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, false);
			if(components!=null)
				CMLib.ableComponents().destroyAbilityComponents(components);
		}
		return super.setTrap(mob,P,trapBonus,qualifyingClassLevel,perm);
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new ReadOnlyVector<Item>(1);
		if(this.componentsRequirements.size()==0)
			return V;
		final List<Item> newV=CMLib.ableComponents().makeComponents(null, componentsRequirements);
		if(newV==null)
			return V;
		return newV;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		if(!super.canSetTrapOn(mob,P))
			return false;
		if((mob!=null)
		&&(this.componentsRequirements.size()>0))
		{
			final List<Object> components=CMLib.ableComponents().componentCheck(mob,componentsRequirements, false);
			if(components==null)
			{
				failureTell(mob,mob,false,
						L("You lack the necessary materials to set @x1, the requirements are: @x2.",
						name(),
						CMLib.ableComponents().getAbilityComponentDesc(mob,componentsRequirements)));
				return false;
			}
		}
		return true;
	}

	public ScriptingEngine getScripter()
	{
		if(((String)V(ID,V_SCRP)).hashCode()!=scriptParmHash)
		{
			final String parm=(String)V(ID,V_SCRP);
			scriptParmHash=parm.hashCode();
			if(parm.trim().length()==0)
				scriptObj=null;
			else
			{
				scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				if(scriptObj!=null)
					scriptObj.setScript(parm);
				else
					scriptParmHash=-1;
			}
		}
		return scriptObj;
	}

	@Override
	public void spring(final MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((!canInvokeTrapOn(invoker(),target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet<MOB>()).contains(target))
			||(target==invoker())
			||(doesSaveVsTraps(target)))
			{
				target.location().show(target,null,null,
						CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,getAvoidMsg((String)V(ID,V_MSGA)));
			}
			else
			{
				final String triggerMsg = getTrigMsg((String)V(ID,V_MSGT));
				final String damageMsg = getDamMsg((String)V(ID,V_MSGD));
				if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,triggerMsg))
				{
					super.spring(target);
					// @x1=trapLevel, @x2=abilityCode, @x3=invokerLevel, @x4=targetLevel
					final String form=(String)V(ID,V_DMGF);
					final Integer weaponType=(Integer)V(ID,V_DMGT);
					final Integer wMsgType=(Integer)V(ID,V_DMGM);
					if((form.trim().length()>0)
					&&(weaponType.intValue()>=0)
					&&(wMsgType.intValue()>=0))
					{
						final double[] parms=new double[] {
							trapLevel(),
							abilityCode(),
							invoker().phyStats().level(),
							target.phyStats().level()
						};
						final int dmg = CMath.parseIntExpression(form, parms);
						CMLib.combat().postDamage(invoker(),target,null,
												  dmg, CMMsg.MASK_ALWAYS|wMsgType.intValue(), weaponType.intValue(),
												  damageMsg);
					}
					final String able=(String)V(ID,V_ABLA);
					if(able.trim().length()>0)
					{
						final String t=(String)V(ID,V_ABLM);
						Ability A=(miscText.length()>0)?CMClass.getAbility(miscText):null;
						if(A==null)
							A=CMClass.getAbility(able);
						Vector<String> V2=new Vector<String>();
						if(t.length()>0)
						{
							final int x=t.indexOf('/');
							if(x<0)
							{
								V2=CMParms.parse(t);
								A.setMiscText("");
							}
							else
							{
								V2=CMParms.parse(t.substring(0,x));
								A.setMiscText(t.substring(x+1));
							}
						}
						if((target instanceof Item)
						||(A.canTarget(target))
						||(A.canAffect(target)))
						{
							final Integer k=(Integer)V(ID,V_ABLT);
							A.invoke(invoker(),V2,target,true,trapLevel()+abilityCode());
							if((k.intValue()>0)&&(k.intValue()<Short.MAX_VALUE))
							{
								final Ability EA=target.fetchEffect(A.ID());
								if((EA!=null)&&(CMath.s_int(EA.getStat("TICKDOWN"))>k.intValue()))
									EA.setStat("TICKDOWN", Integer.toString(k.intValue()));
							}
						}
						final ScriptingEngine S=getScripter();
						if(S!=null)
						{
							final CMMsg msg3=CMClass.getMsg(invoker(),target,this,CMMsg.MSG_OK_VISUAL,null,null,ID);
							S.executeMsg(target, msg3);
							S.dequeResponses();
							target.location().recoverRoomStats();
						}
					}
					if((canBeUninvoked())
					&&(affected instanceof Item))
						disable();
				}
			}
		}
	}

	// lots of work to be done here
	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private String convert(final String[] options, final int val, final boolean mask)
	{
		if(mask)
		{
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<options.length;i++)
			{
				if((val&(1<<i))>0)
					str.append(options[i]+",");
			}
			if(str.length()>0)
			{
				String sstr=str.toString();
				if(sstr.endsWith(","))
					sstr=sstr.substring(0,sstr.length()-1);
				return sstr;
			}
		}
		else
		if((val>=0)&&(val<options.length))
			return options[val];
		return ""+val;
	}

	private static final String[] CODES={"CLASS",//0
										 "TEXT",//1
										 "NAME",//2S
										 "LEVEL",//3I
										 "ACODE",//4I
										 "CANAFFECTMASK",//5I
										 "CANTARGETMASK",//6I
										 "ISBOMB",//7B
										 "AVOIDMSG",//8S
										 "TRIGMSG",//9S
										 "DAMMSG",//10S
										 "ACOMP",//11S
										 "DMGF",//12S
										 "DMGT",//13I
										 "DMGM",//14I
										 "ABILITY",//15S
										 "ABILTXT",//16S
										 "ABILTIK",//17I
										 "SCRIPT",//18S
										 "HELP",//19S
										 "BASELEVEL",//22I
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
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return text();
		case 2:
			return (String) V(ID, V_NAME);
		case 3:
			return ""+super.trapLevel;
		case 4:
			return ((Integer)V(ID, V_ACOD)).toString();
		case 5:
			return convert(Ability.CAN_DESCS, ((Integer) V(ID, V_CAFF)).intValue(), true);
		case 6:
			return convert(Ability.CAN_DESCS, ((Integer) V(ID, V_CTAR)).intValue(), true);
		case 7:
			return ((Boolean)V(ID, V_BOMB)).toString();
		case 8:
			return (String)V(ID, V_MSGA);
		case 9:
			return (String)V(ID, V_MSGT);
		case 10:
			return (String)V(ID, V_MSGD);
		case 11:
			return (String)V(ID, V_COMP);
		case 12:
			return (String)V(ID, V_DMGF);
		case 13:
			return Weapon.TYPE_DESCS[((Integer)V(ID, V_DMGT)).intValue()%Weapon.TYPE_DESCS.length];
		case 14:
			return CMMsg.TYPE_DESCS[((Integer)V(ID, V_DMGM)).intValue()%CMMsg.TYPE_DESCS.length];
		case 15:
			return (String)V(ID, V_ABLA);
		case 16:
			return (String)V(ID, V_ABLM);
		case 17:
			return ((Integer)V(ID, V_ABLT)).toString();
		case 18:
			return (String)V(ID, V_SCRP);
		case 19:
			return (String)V(ID, V_HELP);
		case 20:
			return ((Integer)V(ID, V_LEVL)).toString();
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenTrap";
			else
			if (code.equalsIgnoreCase("allxml"))
			{
				final String str=getAllXML();
				return str;
			}
			else
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
			if (val.trim().length() > 0)
			{
				V(ID, V_NAME); // force creation, if necc
				final Object[] O = vars.get(ID);
				vars.remove(ID);
				vars.put(val, O);
				if (num != 9)
					CMClass.delClass(CMObjectType.ABILITY, this);
				ID = val;
				if (num != 9)
					CMClass.addClass(CMObjectType.ABILITY, this);
			}
			break;
		case 1:
			setMiscText(val);
			break;
		case 2:
			SV(ID, V_NAME, val);
			if (ID.equalsIgnoreCase("GenTrap"))
				break;
			break;
		case 3:
			super.trapLevel = CMath.s_int(val);
			break;
		case 4:
			SV(ID, V_ACOD, Integer.valueOf(CMath.s_int(val)));
			super.ableCode = CMath.s_int(val);
			break;
		case 5:
			SV(ID, V_CAFF, Integer.valueOf(convert(Ability.CAN_DESCS, val, true)));
			break;
		case 6:
			SV(ID, V_CTAR, Integer.valueOf(convert(Ability.CAN_DESCS, val, true)));
			break;
		case 7:
			SV(ID, V_BOMB, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 8:
			SV(ID, V_MSGA, val);
			break;
		case 9:
			SV(ID, V_MSGT, val);
			break;
		case 10:
			SV(ID, V_MSGD, val);
			break;
		case 11:
		{
			SV(ID, V_COMP, val);
			final Map<String,List<AbilityComponent>> h=new HashMap<String,List<AbilityComponent>>();
			CMLib.ableComponents().addAbilityComponent(ID.toUpperCase()+"="+val, h);
			if(h.containsKey(ID.toUpperCase()))
				this.componentsRequirements=h.get(ID.toUpperCase());
			else
				this.componentsRequirements=new ArrayList<AbilityComponent>(1);
			break;
		}
		case 12:
			SV(ID, V_DMGF, val);
			break;
		case 13:
		{
			if(CMath.isInteger(val))
				SV(ID, V_DMGT, Integer.valueOf(CMath.s_int(val)));
			else
			{
				final int x=CMParms.indexOf(Weapon.TYPE_DESCS, val.toUpperCase().trim());
				if(x>=0)
					SV(ID, V_DMGT, Integer.valueOf(x));
			}
			break;
		}
		case 14:
		{
			if(CMath.isInteger(val))
				SV(ID, V_DMGM, Integer.valueOf(CMath.s_int(val)));
			else
			{
				final int x=CMParms.indexOf(CMMsg.TYPE_DESCS, val.toUpperCase().trim());
				if(x>=0)
					SV(ID, V_DMGM, Integer.valueOf(x));
			}
			break;
		}
		case 15:
			SV(ID, V_ABLA, val);
			break;
		case 16:
			SV(ID, V_ABLM, val);
			break;
		case 17:
			SV(ID, V_ABLT, Integer.valueOf(CMath.s_int(val)));
			break;
		case 18:
			SV(ID, V_SCRP, val);
			break;
		case 19:
			SV(ID, V_HELP, val);
			break;
		case 20:
			SV(ID, V_LEVL, Integer.valueOf(CMath.s_int(val)));
			break;
		default:
			if (code.equalsIgnoreCase("allxml") && ID.equalsIgnoreCase("GenTrap"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenTrap))
			return false;
		if(!((GenTrap)E).ID().equals(ID))
			return false;
		if(!((GenTrap)E).text().equals(text()))
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
			final String statCode = getStatCodes()[c];
			final String value = CMLib.xml().getValFromPieces(V, statCode);
			if(statCode.equals("CLASS"))
				ID=CMLib.xml().restoreAngleBrackets(value);
			else
			if(!statCode.equals("TEXT"))
				setStat(statCode,CMLib.xml().restoreAngleBrackets(value));
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

	private int convert(final String[] options, final String val, final boolean mask)
	{
		if(CMath.isInteger(val))
			return CMath.s_int(val);
		for(int i=0;i<options.length;i++)
		{
			if(val.equalsIgnoreCase(options[i]))
				return mask?(1<<i):i;
		}
		if(val.length()>0)
		{
			for(int i=0;i<options.length;i++)
			{
				if(options[i].toUpperCase().startsWith(val.toUpperCase()))
					return mask?(1<<i):i;
			}
		}
		if(mask)
		{
			final List<String> V=CMParms.parseCommas(val,true);
			int num=0;
			for(int v=0;v<V.size();v++)
				num=num|(1<<convert(options,V.get(v),false));
			return num;
		}
		return 0;
	}

}
