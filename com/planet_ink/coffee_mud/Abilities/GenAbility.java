package com.planet_ink.coffee_mud.Abilities;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2025 Bo Zimmerman

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
public class GenAbility extends StdAbility
{
	// data should be stored in a common instance object .. something common to all genability of same id,
	// but diff to others.n  I'm thinking like a DVector, and just have
	private String ID="GenAbility";

	@Override
	public String ID()
	{
		return ID;
	}

	private static final Map<String,Object[]> vars=new Hashtable<String,Object[]>();
	private static final int V_NAME=0;//S
	private static final int V_DISP=1;//S
	private static final int V_TRIG=2;//S[]
	private static final int V_MAXR=3;//I
	private static final int V_MINR=4;//I
	private static final int V_AUTO=5;//B
	private static final int V_FLAG=6;//I
	private static final int V_CLAS=7;//I
	private static final int V_OMAN=8;//I
	private static final int V_USAG=9;//I
	private static final int V_CAFF=10;//I
	private static final int V_CTAR=11;//I
	private static final int V_QUAL=12;//I
	private static final int V_HERE=13;//A
	private static final int V_CMSK=14;//S
	private static final int V_SCRP=15;//S
	private static final int V_TMSK=16;//S
	private static final int V_FZZL=17;//S
	private static final int V_ACST=18;//S
	private static final int V_CAST=19;//S
	private static final int V_PCST=20;//S
	private static final int V_ATT2=21;//I
	private static final int V_PAFF=22;//S
	private static final int V_PABL=23;//S
	private static final int V_PDMG=24;//S
	private static final int V_HELP=25;//S
	private static final int V_TKBC=26;//I
	private static final int V_TKOV=27;//I
	private static final int V_TKAF=28;//B
	private static final int V_CHAN=29;//B
	private static final int V_UNIN=30;//S
	private static final int V_MOCK=31;//S
	private static final int V_MOKT=32;//S
	private static final int V_TMSF=33;//S
	private static final int V_NARG=34;//I

	private static final int NUM_VS=35;//S

	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="an ability";
		O[V_DISP]="(An Affect)";
		O[V_TRIG]=new String[]{"CAST","CA","C"};
		O[V_MAXR]=Integer.valueOf(0);
		O[V_MINR]=Integer.valueOf(0);
		O[V_AUTO]=Boolean.FALSE;
		O[V_FLAG]=Long.valueOf(0);
		O[V_CLAS]=Integer.valueOf(Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION);
		O[V_OMAN]=Integer.valueOf(-1);
		O[V_USAG]=Integer.valueOf(Ability.USAGE_MANA);
		O[V_CAFF]=Integer.valueOf(Ability.CAN_MOBS);
		O[V_CTAR]=Integer.valueOf(Ability.CAN_MOBS);
		O[V_QUAL]=Integer.valueOf(Ability.QUALITY_BENEFICIAL_OTHERS);
		O[V_HERE]="";
		O[V_SCRP]="";
		O[V_CMSK]="";
		O[V_TMSK]="";
		O[V_FZZL]="<S-NAME> attempts to use this ability against <T-NAME>, and fails";
		O[V_ACST]="An amazing thing happens to <T-NAME>!";
		O[V_CAST]="<S-NAME> uses an ability against <T-NAME>";
		O[V_PCST]="<T-NAME> is <DAMAGE> by an ability from <S-NAME>!";
		O[V_ATT2]=Integer.valueOf(0);
		O[V_PAFF]="";
		O[V_PABL]="";
		O[V_PDMG]="0";
		O[V_HELP]="<ABILITY>This ability is not yet documented.";
		O[V_TKBC]=Integer.valueOf(0);
		O[V_TKOV]="0";
		O[V_TKAF]=Boolean.FALSE;
		O[V_CHAN]=Boolean.FALSE;
		O[V_UNIN]="";
		O[V_MOCK]="";
		O[V_MOKT]="";
		O[V_TMSF]="";
		O[V_NARG]=Integer.valueOf(0);
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

	private ScriptingEngine	scriptObj		= null;
	private long			scriptParmHash	= 0;
	private Runnable		periodicEffect	= null;
	protected long			timeToNextCast	= 0;
	protected boolean		oneTimeChecked	= false;
	protected List<Ability>	postEffects		= new Vector<Ability>(1);
	protected Ability		quietEffect		= null;
	protected Ability		hereEffect		= null;

	public Ability getQuietAffect()
	{
		return quietEffect;
	}

	public Ability getHereAffect()
	{
		return hereEffect;
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
	public String description()
	{
		return "&";
	}

	@Override
	public String displayText()
	{
		return (String) V(ID, V_DISP);
	}

	@Override
	public String[] triggerStrings()
	{
		return (String[]) V(ID, V_TRIG);
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(((Integer) V(ID, V_MAXR)).intValue());
	}

	@Override
	public int minRange()
	{
		return ((Integer) V(ID, V_MINR)).intValue();
	}

	@Override
	public boolean isAutoInvoked()
	{
		return ((Boolean) V(ID, V_AUTO)).booleanValue();
	}

	@Override
	public long flags()
	{
		return ((Long) V(ID, V_FLAG)).longValue();
	}

	@Override
	public int usageType()
	{
		return ((Integer) V(ID, V_USAG)).intValue();
	}

	@Override
	protected int overrideMana()
	{
		return ((Integer) V(ID, V_OMAN)).intValue();
	} // -1=normal, Ability.COST_ALL=all, Ability.COST_PCT

	@Override
	public int classificationCode()
	{
		return ((Integer) V(ID, V_CLAS)).intValue();
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
	public int abstractQuality()
	{
		return ((Integer) V(ID, V_QUAL)).intValue();
	}

	@Override
	public int getTicksBetweenCasts()
	{
		return ((Integer) V(ID, V_TKBC)).intValue();
	}

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(final long absoluteTime)
	{
		timeToNextCast = absoluteTime;
	}

	protected boolean isChannelingSkill()
	{
		return ((Boolean) V(ID, V_CHAN)).booleanValue();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			final GenAbility A = this.getClass().getDeclaredConstructor().newInstance();
			A.ID=ID;
			getScripter();
			A.scriptParmHash=scriptParmHash;
			if(scriptObj!=null)
			{
				A.scriptObj=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
				A.scriptObj.setScript(scriptObj.getScript());
			}
			else
				A.scriptObj = null;
			A.postEffects = new ArrayList<Ability>(postEffects);
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
		if(E instanceof GenAbility)
		{
			final GenAbility A=(GenAbility)E;
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

	protected Set<Ability> getEffectsList(final Physical mob)
	{
		final Set<Ability> effects = new HashSet<Ability>();
		if(mob != null)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null)
					effects.add(A);
			}
		}
		return effects;
	}

	protected List<Ability> getEffectsDiff(final Physical mob, final Set<Ability> oldEffects)
	{
		final ArrayList<Ability> effects = new ArrayList<Ability>();
		if(mob != null)
		{
			final Set<Ability> currentEffects=getEffectsList(mob);
			for(final Iterator<Ability> a=currentEffects.iterator();a.hasNext();)
			{
				final Ability A=a.next();
				if((A!=null)&&(!oldEffects.contains(A)))
					effects.add(A);
			}
		}
		return effects;
	}

	protected void prepHereAffect(final MOB mob, final Physical target, final int asLevel)
	{
		this.hereEffect=null;
		if(((String)V(ID(), V_HERE)).trim().length()>0)
		{
			this.hereEffect = CMClass.getAbility("Prop_HereAdjuster");
			if(this.hereEffect != null)
			{
				final String[] vars = new String[] {
					""+mob.phyStats().level(),
					""+target.phyStats().level(),
					""+super.getXLEVELLevel(mob),
					""+super.getX1Level(mob),
					""+super.getX2Level(mob),
					""+super.getX3Level(mob),
					""+super.getX4Level(mob),
					""+super.getX5Level(mob),
					""+adjustedLevel(mob,asLevel)
				};
				final String miscText= CMStrings.replaceVariables((String)V(ID,V_HERE), vars);
				this.hereEffect.setMiscText(miscText);
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		oneTimeChecked = false;
		postEffects.clear();
		if((!auto)
		&&(((String)V(ID,V_CMSK)).length()>0)
		&&(!CMLib.masking().maskCheck((String)V(ID,V_CMSK), mob,true)))
		{
			mob.tell(L("You do not meet the requirements: @x1",CMLib.masking().maskDesc((String)V(ID,V_CMSK))));
			return false;
		}
		// dont forget to allow super. calls to Spell.invoke, Chant.invoke, etc.. based on classification?
		final int nargs = ((Integer)V(ID,V_NARG)).intValue();
		Physical target=givenTarget;
		if((this.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
		||(this.abstractQuality()==Ability.QUALITY_OK_SELF))
		{
			target=mob;
			if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
				target=givenTarget;
			if(target.fetchEffect(this.ID())!=null)
			{
				mob.tell((MOB)target,null,null,L("<S-NAME> <S-IS-ARE> already affected by @x1.",name()));
				return false;
			}
		}
		else
		{
			List<String> targWords = commands;
			if(nargs > 1)
			{
				targWords = new XVector<String>(commands.get(0));
				if(commands.size()>nargs)
				{
					commands.set(nargs-1, CMParms.combine(commands,nargs-1));
					while(commands.size()>nargs)
						commands.remove(commands.size()-1);
				}
			}
			switch(canTargetCode())
			{
			case Ability.CAN_MOBS:
				target=super.getTarget(mob, targWords, givenTarget);
				if(target==null)
					return false;
				break;
			case Ability.CAN_ITEMS:
				target=super.getTarget(mob, mob.location(), givenTarget, targWords, Wearable.FILTER_ANY);
				if(target==null)
					return false;
				break;
			case Ability.CAN_ROOMS:
				target=mob;
				if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
					target=givenTarget;
				if(target.fetchEffect(this.ID())!=null)
				{
					mob.tell(L("This place is already affected by @x1.",name()));
					return false;
				}
				break;
			case Ability.CAN_EXITS:
			{
				final String whatToOpen=CMParms.combine(targWords,0);
				Environmental openThis=null;
				final int dirCode=CMLib.directions().getGoodDirectionCode(whatToOpen);
				if(dirCode>=0)
					openThis=mob.location().getExitInDir(dirCode);
				if(openThis==null)
					openThis=mob.location().fetchFromRoomFavorItems(null, whatToOpen);
				if((openThis==null)||(!(openThis instanceof Exit)))
					return false;
				break;
			}
			case 0:
				break;
			default:
				target=super.getAnyTarget(mob,targWords, givenTarget, Wearable.FILTER_ANY);
				if(target==null)
					return false;
				break;
			}
		}
		if((!auto)
		&&(target!=null)
		&&(((String)V(ID,V_TMSK)).length()>0)
		&&(!CMLib.masking().maskCheck((String)V(ID,V_TMSK), target,true)))
		{
			if(((String)V(ID,V_TMSF)).length()>0)
				mob.tell(mob,target,null,(String)V(ID,V_TMSF));
			else
				mob.tell(L("The target is invalid: @x1",CMLib.masking().maskDesc((String)V(ID,V_TMSK))));
			return false;
		}

		int armorCheck=0;
		switch(classificationCode()&Ability.ALL_ACODES)
		{
		case Ability.ACODE_CHANT:
			armorCheck = CharClass.ARMOR_LEATHER;
			break;
		case Ability.ACODE_COMMON_SKILL:
			break;
		case Ability.ACODE_DISEASE:
			break;
		case Ability.ACODE_LANGUAGE:
			break;
		case Ability.ACODE_POISON:
			break;
		case Ability.ACODE_PRAYER:
			break;
		case Ability.ACODE_PROPERTY:
			break;
		case Ability.ACODE_SKILL:
			break;
		case Ability.ACODE_SPELL:
			armorCheck = CharClass.ARMOR_CLOTH;
			break;
		case Ability.ACODE_SUPERPOWER:
			break;
		case Ability.ACODE_TECH:
			break;
		case Ability.ACODE_THIEF_SKILL:
			armorCheck = CharClass.ARMOR_LEATHER;
			break;
		case Ability.ACODE_TRAP:
			break;
		default:
			break;
		}
		if((armorCheck>0)
		&&(!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,armorCheck))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fumble(s) @x1 due to <S-HIS-HER> armor!",name()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget, auto, asLevel))
			return false;

		final boolean success[]={proficiencyCheck(mob,0,auto)};

		if(success[0])
		{
			int castCode=0;
			switch(classificationCode()&Ability.ALL_ACODES)
			{
			case Ability.ACODE_CHANT:
				castCode = CMMsg.MSG_CAST_VERBAL_SPELL;
				break;
			case Ability.ACODE_COMMON_SKILL:
				castCode = CMMsg.MSG_NOISYMOVEMENT;
				break;
			case Ability.ACODE_DISEASE:
				castCode = CMMsg.MSG_NOISYMOVEMENT;
				break;
			case Ability.ACODE_LANGUAGE:
				castCode = CMMsg.MSG_OK_VISUAL;
				break;
			case Ability.ACODE_POISON:
				castCode = CMMsg.MSG_NOISYMOVEMENT;
				break;
			case Ability.ACODE_PRAYER:
				castCode = CMMsg.MSG_CAST_SOMANTIC_SPELL;
				break;
			case Ability.ACODE_PROPERTY:
				castCode = CMMsg.MSG_OK_VISUAL;
				break;
			case Ability.ACODE_SKILL:
				castCode = CMMsg.MSG_NOISYMOVEMENT;
				break;
			case Ability.ACODE_SPELL:
				castCode = CMMsg.MSG_CAST_SOMANTIC_SPELL;
				break;
			case Ability.ACODE_SUPERPOWER:
				castCode = CMMsg.MSG_CAST_SOMANTIC_SPELL;
				break;
			case Ability.ACODE_THIEF_SKILL:
				castCode = CMMsg.MSG_THIEF_ACT;
				break;
			case Ability.ACODE_TRAP:
				castCode = CMMsg.MSG_NOISYMOVEMENT;
				break;
			default:
				castCode=CMMsg.MSG_CAST_SOMANTIC_SPELL;
				break;
			}
			if(castingQuality(mob,target)==Ability.QUALITY_MALICIOUS)
				castCode|=CMMsg.MASK_MALICIOUS;
			if(auto)
				castCode|=CMMsg.MASK_ALWAYS;

			final CMMsg msg = CMClass.getMsg(mob, target, this, castCode, (auto?(String)V(ID,V_ACST):(String)V(ID,V_CAST)));
			final CMMsg msg2;
			final Integer OTH=(Integer)V(ID,V_ATT2);
			if(OTH.intValue()>0)
				msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS|OTH.intValue()|(auto?CMMsg.MASK_ALWAYS:0),null);
			else
				msg2=null;

			if(mob.location().okMessage(mob,msg)
			&&(this.okMessage(mob, msg))
			&&((msg2==null)||(mob.location().okMessage(mob,msg2)&&(this.okMessage(mob, msg2)))))
			{
				mob.location().send(mob,msg);
				this.executeMsg(mob, msg);
				if (msg2 != null)
				{
					mob.location().send(mob, msg2);
					this.executeMsg(mob, msg2);
				}
				final double[] dvars = new double[] {
					mob.phyStats().level(),
					(target==null)?0:target.phyStats().level(),
					super.getXLEVELLevel(mob),
					super.getX1Level(mob),
					super.getX2Level(mob),
					super.getX3Level(mob),
					super.getX4Level(mob),
					super.getX5Level(mob),
					adjustedLevel(mob,asLevel)
				};
				final int tickOverride = CMath.parseIntExpression((String)V(ID, V_TKOV), dvars);
				if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
				{
					if((canAffectCode()!=0)&&(target!=null))
					{
						this.quietEffect = null;
						prepHereAffect(mob, target, asLevel);
						final GenAbility affectA;
						if(abstractQuality()==Ability.QUALITY_MALICIOUS)
							affectA=(GenAbility)maliciousAffect(mob,target,asLevel,tickOverride,-1);
						else
							affectA=(GenAbility)beneficialAffect(mob,target,asLevel,tickOverride);
						success[0]=affectA!=null;
						if(success[0])
						{
							if((affectA != null)
							&& (affectA.affecting()!=null))
							{
								final String SID=(String)V(ID,V_MOCK);
								if(SID.length()>0)
								{
									final Ability A=CMClass.getAbility(SID);
									if(A!=null)
									{
										A.setAffectedOne(affectA.affecting());
										final String miscText=
											CMStrings.replaceVariables((String)V(ID,V_MOKT), commands.toArray(new String[commands.size()]));
										A.setMiscText(miscText);
										A.makeLongLasting();
										A.setSavable(false);
										A.setProficiency(100);
										affectA.quietEffect=A;
									}
								}
							}
						}
					}
					setTimeOfNextCast(mob);

					final String afterAffect=(String)V(ID,V_PAFF);
					if((afterAffect.length()>0)&&(success[0]))
					{
						final Ability P=CMClass.getAbility("Prop_SpellAdder");
						if(P!=null)
						{
							final Vector<String> V=new XVector<String>(afterAffect);
							P.invoke(mob,V,null,true,asLevel); // spell adder will have addable affects after
							Ability A=null;
							if(target!=null)
							{
								final Set<Ability> oldEffects = getEffectsList(target);
								for(final Enumeration<Ability> a2= ((AbilityContainer)P).abilities();a2.hasMoreElements();)
								{
									A=a2.nextElement();
									if(target.fetchEffect(A.ID())==null)
									{
										final String t=A.text();
										A=(Ability)A.copyOf();
										if(A!=null)
										{
											if(t.length()>0)
											{
												final int x=t.indexOf('/');
												if(x<0)
													A.setMiscText("");
												else
												{
													final String miscText=
															CMStrings.replaceVariables(t.substring(x+1), commands.toArray(new String[commands.size()]));
													A.setMiscText(miscText);
												}
											}
											final int tickDown=(abstractQuality()==Ability.QUALITY_MALICIOUS)?
													getMaliciousTickdownTime(mob,target,tickOverride,asLevel):
													getBeneficialTickdownTime(mob,target,tickOverride,asLevel);
											A.startTickDown(mob,target,tickDown);
										}
									}
								}
								postEffects.addAll(getEffectsDiff(target, oldEffects));
							}
						}
					}
				}
				final Physical finalTarget = target;
				final MOB finalTargetMOB = (finalTarget instanceof MOB) ? (MOB) finalTarget : mob;
				final int finalCastCode = castCode;
				final GenAbility me = this;
				final Runnable skillAction = new Runnable()
				{
					@Override
					public void run()
					{
						final String DMG=(String)V(ID,V_PDMG);
						int dmg=0;
						if(DMG.trim().length()>0)
						{
							dmg=CMath.parseIntExpression(DMG,
								new double[]
								{
									mob.phyStats().level(),
									((finalTarget==null)?mob.phyStats().level():finalTarget.phyStats().level()),
									me.getXLEVELLevel(mob),
									me.getX1Level(mob),
									me.getX2Level(mob),
									me.getX3Level(mob),
									me.getX4Level(mob),
									me.getX5Level(mob),
									adjustedLevel(mob,asLevel)
								});
						}
						if(((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
						||(dmg>0))
						{
							if((msg.value()>0)||((msg2!=null)&&(msg2.value()>0))&&(dmg>0))
								dmg=dmg/2;
							if((!oneTimeChecked) || (((Boolean)V(ID,V_TKAF)).booleanValue()))
							{
								oneTimeChecked = true;
								if((success[0])&&(((String)V(ID,V_PCST)).length()>0))
								{
									if((finalTarget==null)||(finalTarget instanceof Exit)||(finalTarget instanceof Area)
									||(mob.location()==CMLib.map().roomLocation(finalTarget)))
									{
										if(dmg>0)
										{
											CMLib.combat().postDamage(mob,finalTargetMOB,me,dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?finalCastCode:OTH.intValue()),Weapon.TYPE_BURSTING,(String)V(ID,V_PCST));
											dmg=0;
										}
										else
										if(dmg<0)
										{
											CMLib.combat().postHealing(mob,finalTargetMOB,me,-dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?finalCastCode:OTH.intValue()),(String)V(ID,V_PCST));
											dmg=0;
										}
										else
										if(finalTarget != null)
											CMLib.map().roomLocation(finalTarget).show(mob,finalTarget,CMMsg.MSG_OK_ACTION,(String)V(ID,V_PCST));
										else
											CMLib.map().roomLocation(mob).show(mob,finalTarget,CMMsg.MSG_OK_ACTION,(String)V(ID,V_PCST));
									}
								}
							}
							if(dmg>0)
							{
								CMLib.combat().postDamage(mob,finalTargetMOB,me,dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?finalCastCode:OTH.intValue()),Weapon.TYPE_BURSTING,null);
							}
							else
							if(dmg<0)
							{
								CMLib.combat().postHealing(mob,finalTargetMOB,me,-dmg,CMMsg.MASK_ALWAYS|((OTH.intValue()<=0)?finalCastCode:OTH.intValue()),null);
							}
							if(CMLib.flags().isInTheGame(mob,true)
							&&((canTargetCode()==0)
								||((finalTarget != null)
								&&((!(finalTarget instanceof MOB))||CMLib.flags().isInTheGame((MOB)finalTarget,true)))))
							{
								final ScriptingEngine S=getScripter();
								if((success[0])&&(S!=null))
								{
									final CMMsg msg3=CMClass.getMsg(mob,finalTarget,me,CMMsg.MSG_OK_VISUAL,null,null,ID);
									final Object[] args = commands.toArray(new Object[12]);
									S.executeMsg(mob, msg3);
									S.dequeResponses(args);
								}
								mob.location().recoverRoomStats();
							}
						}
						if(((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
						&&(CMLib.flags().isInTheGame(mob,true)
						&&((finalTarget==null)||CMLib.flags().isInTheGame(finalTarget,true))))
						{
							final String afterCast=(String)V(ID,V_PABL);
							if(afterCast.length()>0)
							{
								final Ability P=CMClass.getAbility("Prop_SpellAdder");
								if(P!=null)
								{
									final Set<Ability> oldEffects = getEffectsList(finalTarget);
									P.invoke(mob,new XVector<String>(afterCast),finalTarget,true,asLevel);
									postEffects.addAll(getEffectsDiff(finalTarget, oldEffects));
								}
							}
						}
						if((canAffectCode()!=0)&&(finalTarget!=null)&&(finalTargetMOB.amDead()||(!CMLib.flags().isInTheGame(finalTarget, true))))
						{
							Ability A=finalTarget.fetchEffect(ID());
							if((!(A instanceof GenAbility))||(A.invoker()!=mob))
								A=null;
							if(A!=null)
								A.unInvoke();
						}
					}
				};
				this.periodicEffect=null;
				skillAction.run();
				if((canAffectCode()!=0)
				&&(finalTarget!=null)
				&&(!finalTargetMOB.amDead())
				&&(CMLib.flags().isInTheGame(finalTarget, true)))
				{
					Ability A=finalTarget.fetchEffect(ID());
					if((!(A instanceof GenAbility))||(A.invoker()!=mob))
						A=null;
					if(A instanceof GenAbility)
						((GenAbility)A).periodicEffect = skillAction;
				}
			}
		}
		else
		if(abstractQuality()==Ability.QUALITY_MALICIOUS)
			return maliciousFizzle(mob,target,(String)V(ID,V_FZZL));
		else
			return beneficialVisualFizzle(mob,target,(String)V(ID,V_FZZL));

		return true;
	}

	@Override
	public boolean preInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel, final int secondsElapsed, final double actionsRemaining)
	{
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final ScriptingEngine S=getScripter();
		if(S!=null)
			S.executeMsg(myHost,msg);
		final Ability A=this.getQuietAffect();
		if(A != null)
			A.executeMsg(myHost, msg);

		if(isChannelingSkill()
		&&(affecting()==invoker())
		&&msg.amISource(invoker())
		&&(abilityCode()==0)
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.sourceMajor()>0)
		&&(msg.othersMinor()!=CMMsg.TYP_LOOK)
		&&(msg.othersMinor()!=CMMsg.TYP_EXAMINE)
		&&(msg.othersMajor()>0)
		&&(msg.tool()!=this)
		&&((msg.othersMajor(CMMsg.MASK_SOUND)&&msg.othersMajor(CMMsg.MASK_MOUTH))
			||msg.othersMajor(CMMsg.MASK_HANDS)
			||msg.othersMajor(CMMsg.MASK_MOVE))
		&&((!(msg.tool() instanceof Ability))||(((Ability)msg.tool()).isNowAnAutoEffect())))
		{
			unInvoke();
			msg.source().recoverPhyStats();
		}
		return;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		final Ability A=getHereAffect();
		if(A!=null)
			A.affectPhyStats(affectedEnv,affectableStats);
		if(isChannelingSkill())
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_AUTO_ATTACK);
		final Ability A2=this.getQuietAffect();
		if(A2 != null)
			A2.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		final Ability A=getHereAffect();
		if(A!=null)
			A.affectCharStats(affectedMob,affectableStats);
		final Ability A2=this.getQuietAffect();
		if(A2 != null)
			A2.affectCharStats(affectedMob, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		final Ability A=getHereAffect();
		if(A!=null)
			A.affectCharState(affectedMob,affectableMaxState);
		final Ability A2=this.getQuietAffect();
		if(A2 != null)
			A2.affectCharState(affectedMob, affectableMaxState);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final ScriptingEngine S=getScripter();
		if(S!=null)
		{
			if(!S.okMessage(myHost,msg))
				return false;
		}
		final Ability A=this.getQuietAffect();
		if(A != null)
		{
			if(!A.okMessage(myHost, msg))
				return false;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final boolean can=this.canBeUninvoked();
		final Physical aff = this.affecting();
		super.unInvoke();
		if(can)
		{
			final List<Ability> affs = new ArrayList<Ability>(postEffects);
			postEffects.clear();
			for(final Ability A : affs)
			{
				if((A!=null) && (aff.fetchEffect(A.ID())==A))
				{
					A.unInvoke();
					aff.delEffect(A);
				}
			}
			affs.clear();
			if(CMLib.flags().isInTheGame(aff, true))
			{
				final ScriptingEngine S=getScripter();
				if(S!=null)
				{
					final CMMsg msg3=CMClass.getMsg(invoker(),aff,this,CMMsg.MSG_OK_VISUAL,null,null,"UNINVOKE-"+ID);
					S.executeMsg(aff, msg3);
					S.dequeResponses(null);
				}
			}
			final String uninMsg = (String)V(ID,V_UNIN);
			if(uninMsg.length()>0)
			{
				if(aff instanceof MOB)
				{
					final MOB mob=(MOB)aff;
					if((mob.location()!=null)&&(!mob.amDead()))
						mob.tell(uninMsg);
				}
				else
				if(aff instanceof Item)
				{
					final Item I=(Item)aff;
					if(I.owner() instanceof Room)
						((Room)I.owner()).showHappens(CMMsg.MSG_OK_VISUAL,uninMsg);
					else
					if(I.owner() instanceof MOB)
					{
						final MOB mob=(MOB)aff;
						if((mob.location()!=null)&&(!mob.amDead()))
							mob.tell(uninMsg);
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((unInvoked)&&(canBeUninvoked()))
			return false;
		if(!super.tick(ticking,tickID))
			return false;
		final ScriptingEngine S=getScripter();
		if(S!=null)
		{
			if(!S.tick(ticking,tickID))
				return false;
		}
		final Ability qA=getQuietAffect();
		if(qA!=null)
		{
			qA.tick(ticking, tickID);
		}
		if(this.periodicEffect!=null)
			this.periodicEffect.run();
		return true;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		if(super.autoInvocation(mob, force))
		{
			final GenAbility affectA=(GenAbility)mob.fetchEffect(ID());
			if(affectA!=null)
			{
				final String hereParms=(String)V(ID,V_HERE);
				if((hereParms!=null)&&(hereParms.length()>0))
					affectA.prepHereAffect(mob, mob, 0);
				final String SID=(String)V(ID,V_MOCK);
				if(SID.length()>0)
				{
					final Ability A=CMClass.getAbility(SID);
					if(A!=null)
					{
						A.setAffectedOne(affectA.affecting());
						A.setMiscText((String)V(ID,V_MOKT));
						A.makeLongLasting();
						A.setSavable(false);
						A.setProficiency(100);
						affectA.quietEffect=A;
					}
				}
				mob.recoverCharStats();
				mob.recoverMaxState();
				mob.recoverPhyStats();
			}
			return true;
		}
		return false;
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
										 "DISPLAY",//3S
										 "TRIGSTR",//4S[]
										 "MAXRANGE",//5I
										 "MINRANGE",//6I
										 "AUTOINVOKE",//7B
										 "FLAGS",//8I
										 "CLASSIFICATION",//9I
										 "OVERRIDEMANA",//10I
										 "USAGEMASK",//11I
										 "CANAFFECTMASK",//12I
										 "CANTARGETMASK",//13I
										 "QUALITY",//14I
										 "HERESTATS",//15A
										 "CASTMASK",//16S
										 "SCRIPT",//17S
										 "TARGETMASK", //18S
										 "FIZZLEMSG", //19S
										 "AUTOCASTMSG", //20S
										 "CASTMSG",//21S
										 "POSTCASTMSG",//22S
										 "ATTACKCODE",//23I
										 "POSTCASTAFFECT",//24S
										 "POSTCASTABILITY",//25S
										 "POSTCASTDAMAGE",//26I
										 "HELP",//27I
										 "TICKSBETWEENCASTS",//28I
										 "TICKSOVERRIDE",//29S
										 "TICKAFFECTS", //30B
										 "CHANNELING", //31B
										 "UNINVOKEMSG", //32S
										 "MOCKABILITY", //33A
										 "MOCKABLETEXT", //34S
										 "TARGETFAILMSG", //35S
										 "NUMARGS"//36I
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
			return (String) V(ID, V_DISP);
		case 4:
			return CMParms.toListString((String[]) V(ID, V_TRIG));
		case 5:
			return convert(Ability.RANGE_CHOICES, ((Integer) V(ID, V_MAXR)).intValue(), false);
		case 6:
			return convert(Ability.RANGE_CHOICES, ((Integer) V(ID, V_MINR)).intValue(), false);
		case 7:
			return ((Boolean) V(ID, V_AUTO)).toString();
		case 8:
			return convert(Ability.FLAG_DESCS, ((Long) V(ID, V_FLAG)).longValue(), true);
		case 9:
			return convertClassAndDomain(((Integer) V(ID, V_CLAS)).intValue());
		case 10:
			return ((Integer) V(ID, V_OMAN)).toString();
		case 11:
			return convert(Ability.USAGE_DESCS, ((Integer) V(ID, V_USAG)).intValue(), true);
		case 12:
			return convert(Ability.CAN_DESCS, ((Integer) V(ID, V_CAFF)).intValue(), true);
		case 13:
			return convert(Ability.CAN_DESCS, ((Integer) V(ID, V_CTAR)).intValue(), true);
		case 14:
			return convert(Ability.QUALITY_DESCS, ((Integer) V(ID, V_QUAL)).intValue(), false);
		case 15:
			return ((String) V(ID, V_HERE));
		case 16:
			return (String) V(ID, V_CMSK);
		case 17:
			return (String) V(ID, V_SCRP);
		case 18:
			return (String) V(ID, V_TMSK);
		case 19:
			return (String) V(ID, V_FZZL);
		case 20:
			return (String) V(ID, V_ACST);
		case 21:
			return (String) V(ID, V_CAST);
		case 22:
			return (String) V(ID, V_PCST);
		case 23:
			return convert(CMMsg.TYPE_DESCS, ((Integer) V(ID, V_ATT2)).intValue(), false);
		case 24:
			return (String) V(ID, V_PAFF);
		case 25:
			return (String) V(ID, V_PABL);
		case 26:
			return (String) V(ID, V_PDMG);
		case 27:
			return (String) V(ID, V_HELP);
		case 28:
			return ((Integer) V(ID, V_TKBC)).toString();
		case 29:
			return ((String) V(ID, V_TKOV)).toString();
		case 30:
			return ((Boolean) V(ID, V_TKAF)).toString();
		case 31:
			return ((Boolean) V(ID, V_CHAN)).toString();
		case 32:
			return (String) V(ID,V_UNIN);
		case 33:
			return (String) V(ID,V_MOCK);
		case 34:
			return (String) V(ID,V_MOKT);
		case 35:
			return (String) V(ID, V_TMSF);
		case 36:
			return ((Integer) V(ID, V_NARG)).toString();
		default:
			if (code.equalsIgnoreCase("javaclass"))
				return "GenAbility";
			else
			if (code.equalsIgnoreCase("allxml"))
				return getAllXML();
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
			if (ID.equalsIgnoreCase("GenAbility"))
				break;
			break;
		case 3:
			SV(ID, V_DISP, val);
			break;
		case 4:
			SV(ID, V_TRIG, CMParms.toStringArray(CMParms.parseCommas(val.toUpperCase(), true)));
			break;
		case 5:
			SV(ID, V_MAXR, Integer.valueOf((int)convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 6:
			SV(ID, V_MINR, Integer.valueOf((int)convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 7:
			SV(ID, V_AUTO, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 8:
			SV(ID, V_FLAG, Long.valueOf(convert(Ability.FLAG_DESCS, val, true)));
			break;
		case 9:
			SV(ID, V_CLAS, Integer.valueOf(convertClassAndDomain(val)));
			break;
		case 10:
			SV(ID, V_OMAN, Integer.valueOf(CMath.s_parseIntExpression(val)));
			getHardOverrideManaCache().remove(ID());
			break;
		case 11:
			SV(ID, V_USAG, Integer.valueOf((int)convert(Ability.USAGE_DESCS, val, true)));
			break;
		case 12:
			SV(ID, V_CAFF, Integer.valueOf((int)convert(Ability.CAN_DESCS, val, true)));
			break;
		case 13:
			SV(ID, V_CTAR, Integer.valueOf((int)convert(Ability.CAN_DESCS, val, true)));
			break;
		case 14:
			SV(ID, V_QUAL, Integer.valueOf((int)convert(Ability.QUALITY_DESCS, val, false)));
			break;
		case 15:
			SV(ID, V_HERE, val);
			break;
		case 16:
			SV(ID, V_CMSK, val);
			break;
		case 17:
			SV(ID, V_SCRP, val);
			break;
		case 18:
			SV(ID, V_TMSK, val);
			break;
		case 19:
			SV(ID, V_FZZL, val);
			break;
		case 20:
			SV(ID, V_ACST, val);
			break;
		case 21:
			SV(ID, V_CAST, val);
			break;
		case 22:
			SV(ID, V_PCST, val);
			break;
		case 23:
			SV(ID, V_ATT2, Integer.valueOf((int)convert(CMMsg.TYPE_DESCS, val, false)));
			break;
		case 24:
			SV(ID, V_PAFF, val);
			break;
		case 25:
			SV(ID, V_PABL, val);
			break;
		case 26:
			SV(ID, V_PDMG, val);
			break;
		case 27:
			SV(ID, V_HELP, val);
			break;
		case 28:
			SV(ID, V_TKBC, Integer.valueOf(CMath.s_int(val)));
			break;
		case 29:
			SV(ID, V_TKOV, val.trim());
			break;
		case 30:
			SV(ID, V_TKAF, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 31:
			SV(ID, V_CHAN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 32:
			SV(ID, V_UNIN, val);
			break;
		case 33:
			SV(ID, V_MOCK, val);
			break;
		case 34:
			SV(ID, V_MOKT, val);
			break;
		case 35:
			SV(ID, V_TMSF, val);
			break;
		case 36:
			SV(ID, V_NARG, Integer.valueOf(CMath.s_int(val)));
			break;
		default:
			if (code.equalsIgnoreCase("allxml") && ID.equalsIgnoreCase("GenAbility"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	private String convert(final String[] options, final long val, final boolean mask)
	{
		if(mask)
		{
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<options.length;i++)
			{
				if((val&(1L<<i))>0)
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
			return options[(int)val];
		return ""+val;
	}

	private int convertClassAndDomain(String val)
	{
		if(CMath.isInteger(val))
			return CMath.s_int(val);
		int dom=0;
		int acod=Ability.ACODE_SKILL;
		final List<String> V=CMParms.parseCommas(val,true);
		for(int v=0;v<V.size();v++)
		{
			val=V.get(v);
			int tacod=-1;
			for(int a=0;a<Ability.ACODE.DESCS.size();a++)
			{
				if(val.equalsIgnoreCase(Ability.ACODE.DESCS.get(a)))
					tacod=a;
			}
			if(tacod<0)
			{
				for(int i=0;i<Ability.ACODE.DESCS.size();i++)
				{
					if(Ability.ACODE.DESCS.get(i).toUpperCase().startsWith(val.toUpperCase()))
						tacod=i;
				}
				if(tacod<0)
				{
					int tdom=-1;
					for(int a=0;a<Ability.DOMAIN.DESCS.size();a++)
					{
						if(val.equalsIgnoreCase(Ability.DOMAIN.DESCS.get(a)))
							tdom=a<<5;
					}
					if(tdom<0)
					{
						for(int i=0;i<Ability.DOMAIN.DESCS.size();i++)
						{
							if(Ability.DOMAIN.DESCS.get(i).toUpperCase().startsWith(val.toUpperCase())
									||Ability.DOMAIN.DESCS.get(i).toUpperCase().endsWith(val.toUpperCase()))
							{
								tdom = i << 5;
								break;
							}
						}
					}
					if(tdom>=0)
						dom=tdom;
				}
			}
			else
				acod=tacod;
		}
		return acod|dom;
	}

	private String convertClassAndDomain(final int val)
	{
		final int dom=(val&Ability.ALL_DOMAINS)>>5;
		final int acod=val&Ability.ALL_ACODES;
		if((acod>=0)&&(acod<Ability.ACODE.DESCS.size())
		&&(dom>=0)&&(dom<Ability.DOMAIN.DESCS.size()))
			return Ability.ACODE.DESCS.get(acod)+","+Ability.DOMAIN.DESCS.get(dom);
		return ""+val;
	}

	private long convert(final String[] options, final String val, final boolean mask)
	{
		if(CMath.isLong(val))
			return CMath.s_long(val);
		for(int i=0;i<options.length;i++)
		{
			if(val.equalsIgnoreCase(options[i]))
				return mask?(1L<<i):i;
		}
		if(val.length()>0)
		{
			for(int i=0;i<options.length;i++)
			{
				if(options[i].toUpperCase().startsWith(val.toUpperCase()))
					return mask?(1L<<i):i;
			}
		}
		if(mask)
		{
			final List<String> V=CMParms.parseCommas(val,true);
			long num=0;
			for(int v=0;v<V.size();v++)
				num=num|(1L<<convert(options,V.get(v),false));
			return num;
		}
		return 0;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenAbility))
			return false;
		if(!((GenAbility)E).ID().equals(ID))
			return false;
		if(!((GenAbility)E).text().equals(text()))
			return false;
		return true;
	}

	private void parseAllXML(final String xml)
	{
		final List<XMLTag> V=CMLib.xml().parseAllXML(xml);
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
}
