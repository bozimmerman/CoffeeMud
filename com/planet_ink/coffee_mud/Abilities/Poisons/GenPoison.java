package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
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
   Copyright 2025-2025 Bo Zimmerman

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
public class GenPoison extends Poison implements SpellHolder
{
	public String	ID	= "GenPoison";

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

	@Override
	public String[] triggerStrings()
	{
		return (String[]) V(ID, V_TRIG);
	}

	protected Object V(final String ID, final int varNum)
	{
		if(vars.containsKey(ID))
			return vars.get(ID)[varNum];
		final Object[] O=makeEmpty();
		vars.put(ID,O);
		return O[varNum];
	}

	protected void SV(final String ID,final int varNum,final Object O)
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

	public GenPoison()
	{
		super();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenPoison A=this.getClass().getDeclaredConstructor().newInstance();
			cloneFix(A);
			return A;
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenPoison();
	}

	@Override
	protected void cloneFix(final Ability E)
	{
		if(E instanceof GenPoison)
		{
			((GenPoison)E).ID=ID;
			((GenPoison)E).adjusterA = null;
			((GenPoison)E).effects = null;
			((GenPoison)E).mood = null;
		}
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	protected static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	protected static final int	V_NAME			= 0;	// S
	protected static final int	V_TRIG			= 1;	// S[]
	protected static final int	V_HELP			= 2;	// S
	protected static final int	V_TICKS			= 3;	// P<S,F>
	protected static final int	V_DELAY			= 4;	// P<S,F>
	protected static final int 	V_DONE			= 5;	// S
	protected static final int	V_START			= 6;	// S
	protected static final int	V_TTELL			= 7;	// S
	protected static final int	V_TRGB			= 8;	// B
	protected static final int	V_ACHA			= 9;	// P<S,F>
	protected static final int	V_AMSG			= 10;	// S
	protected static final int	V_CMSG			= 11;	// S
	protected static final int	V_FMSG			= 12;	// S
	protected static final int	V_ADJS			= 13;	// S
	protected static final int	V_PEAC			= 14;	// B
	protected static final int	V_DAMG			= 15;	// P<S,F>
	protected static final int	V_MOOD			= 16;	// S
	protected static final int	V_EFFS			= 17;	// P<S,List<Ability>>

	protected static final int	NUM_VS			= 18;	//

	protected Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="Generic Poison";
		O[V_TRIG]=new String[]{"GPOISON"};
		O[V_HELP]="<ABILITY>This poison is not yet documented.";
		O[V_TICKS]=makeFormulaPair("0");
		O[V_DELAY]=makeFormulaPair("3");
		O[V_DONE]=CMLib.lang().L("The poison runs its course.");
		O[V_START]=CMLib.lang().L("^G<S-NAME> turn(s) green.^?");
		O[V_TTELL]="";
		O[V_TRGB]=Boolean.valueOf(true);
		O[V_ACHA]=makeFormulaPair("0");
		O[V_AMSG] = CMLib.lang().L("<S-NAME> cringe(s) as the poison courses through <S-HIS-HER> blood.");
		O[V_CMSG] = CMLib.lang().L("^F^<FIGHT^><S-NAME> attempt(s) to poison <T-NAMESELF>!^</FIGHT^>^?");
		O[V_FMSG] = CMLib.lang().L("<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).");
		O[V_ADJS]="";
		O[V_PEAC]=Boolean.valueOf(false);
		O[V_DAMG]=makeFormulaPair("@x4 + 1?@x1");
		O[V_MOOD]="";
		O[V_EFFS]=new Pair<String,List<Ability>>("",new ArrayList<Ability>(0));
		return O;
	}

	protected static Pair<String,CompiledFormula> makeFormulaPair(final String formula)
	{
		final CompiledFormula expr = CMath.compileMathExpression(formula);
		return new Pair<String,CompiledFormula>(formula, expr);
	}

	protected static Pair<String,List<Ability>> makeEffectsPair(final String effects)
	{
		final List<Ability> aV = new LinkedList<Ability>();
		for(final CMObject o : CMLib.coffeeMaker().getCodedSpellsOrBehaviors(effects))
			if(o instanceof Ability)
				aV.add((Ability)o);
		return new Pair<String,List<Ability>>(effects, aV);
	}

	protected Ability adjusterA = null;
	protected List<Ability> effects = null;
	protected Ability mood = null;

	protected Ability getAdjusterA()
	{
		if(adjusterA == null)
		{
			adjusterA = CMClass.getAbility("Prop_Adjuster");
			adjusterA.setAffectedOne(affected);
			adjusterA.setMiscText(CMStrings.replaceVariables((String)V(ID,V_ADJS),formulaVarss()));
		}
		return adjusterA;
	}

	protected final static List<Ability> emptyEffects = new ArrayList<Ability>(0);

	protected List<Ability> getOtherEffects()
	{
		if(effects == null)
		{
			if(!(affected instanceof MOB))
				return emptyEffects;
			@SuppressWarnings("unchecked")
			final Pair<String,List<Ability>> peffects = (Pair<String,List<Ability>>)V(ID,V_EFFS);
			if(peffects.second.size()==0)
				return emptyEffects;
			final Physical affected=this.affected;
			if(!(affected instanceof MOB))
				return emptyEffects;
			final List<Ability> neffects = new LinkedList<Ability>();
			for(final Ability effA : peffects.second)
			{
				final Ability A = (Ability)effA.copyOf();
				A.setMiscText(effA.text());
				A.makeNonUninvokable();
				A.makeLongLasting();
				A.setAffectedOne(affected);
			}
			effects = neffects;
		}
		return effects;
	}

	protected Ability getMood()
	{
		if(mood == null)
		{
			final String moodStr = (String)V(ID,V_MOOD);
			if(moodStr.length()==0)
				return null;
			final Physical affected=this.affected;
			if(!(affected instanceof MOB))
				return null;
			mood = CMClass.getAbility("Mood");
			if((mood == null)
			||(affected.phyStats().isAmbiance(PhyStats.Ambiance.SUPPRESS_MOOD)))
				return null;
			mood.setMiscText(moodStr);
			mood.setAffectedOne(affected);
		}
		mood.setAffectedOne(affected);
		return mood;
	}

	protected double[] formulaVars()
	{
		final double[] vars = new double[10];
		if(invoker != null)
		{
			vars[0] = invoker.phyStats().level();
			vars[1] = super.getXLEVELLevel(invoker);
		}
		if(affected != null)
		{
			vars[2] = affected.phyStats().level();
		}
		vars[3] = super.rank;
		return vars;
	}

	protected int[] formulaVaris()
	{
		final int[] vars = new int[10];
		if(invoker != null)
		{
			vars[0] = invoker.phyStats().level();
			vars[1] = super.getXLEVELLevel(invoker);
		}
		if(affected != null)
		{
			vars[2] = affected.phyStats().level();
		}
		vars[3] = (int)Math.round(super.rank);
		return vars;
	}

	protected String[] formulaVarss()
	{
		final String[] vars = new String[10];
		if(invoker != null)
		{
			vars[0] = ""+invoker.phyStats().level();
			vars[1] = ""+super.getXLEVELLevel(invoker);
		}
		if(affected != null)
		{
			vars[2] = ""+affected.phyStats().level();
		}
		vars[3] = ""+(int)Math.round(super.rank);
		return vars;
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		final Ability adjusterA = getAdjusterA();
		super.setAffectedOne(P);
		adjusterA.setMiscText(CMStrings.replaceVariables((String)V(ID,V_ADJS),formulaVarss()));
	}

	@Override
	public void setInvoker(final MOB M)
	{
		final Ability adjusterA = getAdjusterA();
		super.setInvoker(M);
		adjusterA.setMiscText(CMStrings.replaceVariables((String)V(ID,V_ADJS),formulaVarss()));
	}

	@Override
	protected int POISON_TICKS()
	{
		@SuppressWarnings("rawtypes")
		final Pair P = (Pair)V(ID,V_TICKS);
		final CompiledFormula expr = (CompiledFormula)P.second;
		 // 0 means no adjustment!
		return (int)Math.round(CMath.parseMathExpression(expr, formulaVars(), 0));
	}

	@Override
	protected int POISON_DELAY()
	{
		@SuppressWarnings("rawtypes")
		final Pair P = (Pair)V(ID,V_DELAY);
		final CompiledFormula expr = (CompiledFormula)P.second;
		return (int)Math.round(CMath.parseMathExpression(expr, formulaVars(), 0));
	}

	@Override
	protected String POISON_DONE()
	{
		return (String)V(ID,V_DONE);
	}

	@Override
	protected String POISON_START()
	{
		return (String)V(ID,V_START);
	}

	@Override
	protected String POISON_START_TARGETONLY()
	{
		return (String)V(ID,V_TTELL);
	}

	@Override
	protected boolean POISON_AFFECTTARGET()
	{
		return ((Boolean)V(ID,V_TRGB)).booleanValue();
	}

	@Override
	protected int POISON_ADDICTION_CHANCE()
	{
		// 0 - 100
		@SuppressWarnings("rawtypes")
		final Pair P = (Pair)V(ID,V_ACHA);
		final CompiledFormula expr = (CompiledFormula)P.second;
		return (int)Math.round(CMath.parseMathExpression(expr, formulaVars(), 0));
	}

	@Override
	protected String POISON_AFFECT()
	{
		return (String)V(ID,V_AMSG);
	}

	@Override
	protected String POISON_CAST()
	{
		return (String)V(ID,V_CMSG);
	}

	@Override
	protected String POISON_FAIL()
	{
		return (String)V(ID,V_FMSG);
	}

	@Override
	protected int POISON_DAMAGE()
	{
		@SuppressWarnings("rawtypes")
		final Pair P = (Pair)V(ID,V_DAMG);
		final CompiledFormula expr = (CompiledFormula)P.second;
		return (int)Math.round(CMath.parseMathExpression(expr, formulaVars(), 0));
	}

	@Override
	protected boolean POISON_MAKE_PEACE()
	{
		return ((Boolean)V(ID,V_PEAC)).booleanValue();
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		getAdjusterA().affectCharStats(affected, affectableStats);
		for(final Ability A : getOtherEffects())
			A.affectCharStats(affected, affectableStats);
	}


	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		getAdjusterA().affectPhyStats(affected, affectableStats);
		for(final Ability A : getOtherEffects())
			A.affectPhyStats(affected, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		getAdjusterA().affectCharState(affectedMob, affectableMaxState);
		for(final Ability A : getOtherEffects())
			A.affectCharState(affectedMob, affectableMaxState);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		getAdjusterA().tick(ticking, tickID);
		final Ability mood = getMood();
		if(mood != null)
			mood.tick(affected, tickID);
		for(final Ability A : getOtherEffects())
			A.tick(ticking, tickID);
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		final Ability mood = getMood();
		if((mood != null)&&(!mood.okMessage(myHost, msg)))
			return false;
		for(final Ability A : getOtherEffects())
			if(!A.okMessage(myHost, msg))
				return false;
		return true;
	}


	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		final Ability mood = getMood();
		if(mood != null)
			mood.executeMsg(myHost, msg);
		for(final Ability A : getOtherEffects())
			A.executeMsg(myHost, msg);
	}

	@Override
	public List<Ability> getSpells()
	{
		final List<Ability> spells = new ArrayList<Ability>();
		spells.add(this);
		if(this.effects != null)
			spells.addAll(this.effects);
		return spells;
	}

	@Override
	public String getSpellList()
	{
		return "";
	}

	@Override
	public void setSpellList(final String list)
	{
	}

	// lots of work to be done here
	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[] CODES={"CLASS", // 0
										 "TEXT", // 1
										 "NAME", // 2S
										 "HELP", // 3I
										 "TRIGSTR",// 4S[]
										 "TICKS", // 5P<S,F>
										 "DELAY", // 6P<S,F>
										 "DONEMSG", // 7S
										 "STARTMSG", // 8S
										 "TARGTELLMSG", // 9S
										 "AFFECTTARG", // 10B
										 "ADDCHANCE", // 11P<S,F>
										 "AFFECTMSG", // 12S
										 "CASTMSG", // 13S
										 "FAILMSG", // 14S
										 "ADJUSTMENTS", //15 S
										 "MAKEPEACE", // 16B
										 "DAMAGE", // 17P<S,F>
										 "MOOD", // 18S
										 "EFFECTS", // 19S
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

	@SuppressWarnings("rawtypes")
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
			return ((Pair) V(ID, V_TICKS)).first.toString();
		case 6:
			return ((Pair) V(ID, V_DELAY)).first.toString();
		case 7:
			return (String) V(ID, V_DONE);
		case 8:
			return (String) V(ID, V_START);
		case 9:
			return (String) V(ID, V_TTELL);
		case 10:
			return ((Boolean) V(ID, V_TRGB)).toString();
		case 11:
			return ((Pair) V(ID, V_ACHA)).first.toString();
		case 12:
			return (String) V(ID, V_AMSG);
		case 13:
			return (String) V(ID, V_CMSG);
		case 14:
			return (String) V(ID, V_FMSG);
		case 15:
			return (String) V(ID, V_ADJS);
		case 16:
			return ((Boolean) V(ID, V_PEAC)).toString();
		case 17:
			return ((Pair) V(ID, V_DAMG)).first.toString();
		case 18:
			return (String) V(ID, V_MOOD);
		case 19:
			return ((Pair) V(ID, V_EFFS)).first.toString();
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenPoison";
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
			break;
		case 3:
			SV(ID, V_HELP, val);
			break;
		case 4:
			SV(ID, V_TRIG, CMParms.parseCommas(val, true).toArray(new String[0]));
			break;
		case 5:
			SV(ID, V_TICKS, GenPoison.makeFormulaPair(val));
			break;
		case 6:
			SV(ID, V_DELAY, GenPoison.makeFormulaPair(val));
			break;
		case 7:
			SV(ID, V_DONE, val);
			break;
		case 8:
			SV(ID, V_START, val);
			break;
		case 9:
			SV(ID, V_TTELL, val);
			break;
		case 10:
			SV(ID, V_TRGB, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 11:
			SV(ID, V_ACHA, GenPoison.makeFormulaPair(val));
			break;
		case 12:
			SV(ID, V_AMSG, val);
			break;
		case 13:
			SV(ID, V_CMSG, val);
			break;
		case 14:
			SV(ID, V_FMSG, val);
			break;
		case 15:
			SV(ID, V_ADJS, val);
			break;
		case 16:
			SV(ID, V_PEAC, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 17:
			SV(ID, V_DAMG, GenPoison.makeFormulaPair(val));
			break;
		case 18:
			SV(ID, V_MOOD, val);
			break;
		case 19:
			SV(ID, V_EFFS, GenPoison.makeEffectsPair(val));
			break;
		default:
			if(code.equalsIgnoreCase("allxml")&&ID().equalsIgnoreCase("GenPoison"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenPoison))
			return false;
		if(!((GenPoison)E).ID().equals(ID))
			return false;
		if(!((GenPoison)E).text().equals(text()))
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

}
