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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
	
	private static final int NUM_VS=30;//S
	
	private static final Object[] makeEmpty()
	{
		final Object[] O=new Object[NUM_VS];
		O[V_NAME]="an ability";
		O[V_DISP]="(An Affect)";
		O[V_TRIG]=new String[]{"CAST","CA","C"};
		O[V_MAXR]=Integer.valueOf(0);
		O[V_MINR]=Integer.valueOf(0);
		O[V_AUTO]=Boolean.FALSE;
		O[V_FLAG]=Integer.valueOf(0);
		O[V_CLAS]=Integer.valueOf(Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION);
		O[V_OMAN]=Integer.valueOf(-1);
		O[V_USAG]=Integer.valueOf(Ability.USAGE_MANA);
		O[V_CAFF]=Integer.valueOf(Ability.CAN_MOBS);
		O[V_CTAR]=Integer.valueOf(Ability.CAN_MOBS);
		O[V_QUAL]=Integer.valueOf(Ability.QUALITY_BENEFICIAL_OTHERS);
		O[V_HERE]=CMClass.getAbility("Prop_HereAdjuster");
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
		O[V_PDMG]="1";
		O[V_HELP]="<ABILITY>This ability is not yet documented.";
		O[V_TKBC]=Integer.valueOf(0);
		O[V_TKOV]=Integer.valueOf(0);
		O[V_TKAF]=Boolean.FALSE;
		O[V_CHAN]=Boolean.FALSE;
		return O;
	}

	private static final Object V(String ID, int varNum)
	{
		if(vars.containsKey(ID))
			return vars.get(ID)[varNum];
		final Object[] O=makeEmpty();
		vars.put(ID,O);
		return O[varNum];
	}

	private static final void SV(String ID,int varNum,Object O)
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
		return ((Integer) V(ID, V_FLAG)).intValue();
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

	public int tickOverride()
	{
		return ((Integer) V(ID, V_TKOV)).intValue();
	}

	@Override
	protected int getTicksBetweenCasts()
	{
		return ((Integer) V(ID, V_TKBC)).intValue();
	}

	@Override
	protected long getTimeOfNextCast()
	{
		return timeToNextCast;
	}

	@Override
	protected void setTimeOfNextCast(long absoluteTime)
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
			final GenAbility A = this.getClass().newInstance();
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
	protected void cloneFix(Ability E)
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
			for(Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
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
			for(Iterator<Ability> a=currentEffects.iterator();a.hasNext();)
			{
				final Ability A=a.next();
				if((A!=null)&&(!oldEffects.contains(A)))
					effects.add(A);
			}
		}
		return effects;
	}

	@Override
	public boolean invoke(final MOB mob, List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
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
			switch(canTargetCode())
			{
			case Ability.CAN_MOBS:
				target=super.getTarget(mob, commands, givenTarget);
				if(target==null)
					return false;
				break;
			case Ability.CAN_ITEMS:
				target=super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_ANY);
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
				final String whatToOpen=CMParms.combine(commands,0);
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
				target=super.getAnyTarget(mob,commands, givenTarget, Wearable.FILTER_ANY);
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
				if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
				{
					if((canAffectCode()!=0)&&(target!=null))
					{
						final Ability affectA;
						if(abstractQuality()==Ability.QUALITY_MALICIOUS)
							affectA=maliciousAffect(mob,target,asLevel,tickOverride(),-1);
						else
							affectA=beneficialAffect(mob,target,asLevel,tickOverride());
						success[0]=affectA!=null;
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
								for(Enumeration<Ability> a2= ((AbilityContainer)P).abilities();a2.hasMoreElements();)
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
													A.setMiscText(t.substring(x+1));
											}
											final int tickDown=(abstractQuality()==Ability.QUALITY_MALICIOUS)?
													getMaliciousTickdownTime(mob,target,tickOverride(),asLevel):
													getBeneficialTickdownTime(mob,target,tickOverride(),asLevel);
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
				final Ability me = this;
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
									(finalTarget==null)?mob.phyStats().level():finalTarget.phyStats().level()
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
											CMLib.map().roomLocation(finalTarget).show(mob,finalTarget,CMMsg.MSG_OK_ACTION,(String)V(ID,V_PCST));
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
							if(CMLib.flags().isInTheGame(mob,true)&&((finalTarget==null)||CMLib.flags().isInTheGame((MOB)finalTarget,true)))
							{
								final ScriptingEngine S=getScripter();
								if((success[0])&&(S!=null))
								{
									final CMMsg msg3=CMClass.getMsg(mob,finalTarget,me,CMMsg.MSG_OK_VISUAL,null,null,ID);
									S.executeMsg(mob, msg3);
									S.dequeResponses();
								}
								mob.location().recoverRoomStats();
							}
						}
						if(((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
						&&(CMLib.flags().isInTheGame(mob,true)&&((finalTarget==null)||CMLib.flags().isInTheGame(finalTarget,true))))
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
				if((canAffectCode()!=0)&&(finalTarget!=null)&&(!finalTargetMOB.amDead())&&(CMLib.flags().isInTheGame(finalTarget, true)))
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
	public boolean preInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
	{
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final ScriptingEngine S=getScripter();
		if(S!=null)
			S.executeMsg(myHost,msg);
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
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		final Ability A=(Ability)V(ID,V_HERE);
		if(A!=null)
			A.affectPhyStats(affectedEnv,affectableStats);
		if(isChannelingSkill())
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_AUTO_ATTACK);
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		final Ability A=(Ability)V(ID,V_HERE);
		if(A!=null)
			A.affectCharStats(affectedMob,affectableStats);

	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		final Ability A=(Ability)V(ID,V_HERE);
		if(A!=null)
			A.affectCharState(affectedMob,affectableMaxState);
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
			for(Ability A : affs)
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
					S.dequeResponses();
				}
			}
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
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
		if(this.periodicEffect!=null)
			this.periodicEffect.run();
		return true;
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
										 "TICKSOVERRIDE",//29I
										 "TICKAFFECTS", //30B
										 "CHANNELING", //31B
										};

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
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
			return convert(Ability.FLAG_DESCS, ((Integer) V(ID, V_FLAG)).intValue(), true);
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
			return ((Ability) V(ID, V_HERE)).text();
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
			return ((Integer) V(ID, V_TKOV)).toString();
		case 30:
			return ((Boolean) V(ID, V_TKAF)).toString();
		case 31:
			return ((Boolean) V(ID, V_CHAN)).toString();
		default:
			if (code.equalsIgnoreCase("allxml"))
				return getAllXML();
			else
				return super.getStat(code);
		}
	}

	@Override
	public void setStat(String code, String val)
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
			SV(ID, V_MAXR, Integer.valueOf(convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 6:
			SV(ID, V_MINR, Integer.valueOf(convert(Ability.RANGE_CHOICES, val, false)));
			break;
		case 7:
			SV(ID, V_AUTO, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 8:
			SV(ID, V_FLAG, Integer.valueOf(convert(Ability.FLAG_DESCS, val, true)));
			break;
		case 9:
			SV(ID, V_CLAS, Integer.valueOf(convertClassAndDomain(val)));
			break;
		case 10:
			SV(ID, V_OMAN, Integer.valueOf(CMath.s_parseIntExpression(val)));
			getHardOverrideManaCache().remove(ID());
			break;
		case 11:
			SV(ID, V_USAG, Integer.valueOf(convert(Ability.USAGE_DESCS, val, true)));
			break;
		case 12:
			SV(ID, V_CAFF, Integer.valueOf(convert(Ability.CAN_DESCS, val, true)));
			break;
		case 13:
			SV(ID, V_CTAR, Integer.valueOf(convert(Ability.CAN_DESCS, val, true)));
			break;
		case 14:
			SV(ID, V_QUAL, Integer.valueOf(convert(Ability.QUALITY_DESCS, val, false)));
			break;
		case 15:
			((Ability) V(ID, V_HERE)).setMiscText(val);
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
			SV(ID, V_ATT2, Integer.valueOf(convert(CMMsg.TYPE_DESCS, val, false)));
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
			SV(ID, V_TKOV, Integer.valueOf(CMath.s_int(val)));
			break;
		case 30:
			SV(ID, V_TKAF, Boolean.valueOf(CMath.s_bool(val)));
			break;
		case 31:
			SV(ID, V_CHAN, Boolean.valueOf(CMath.s_bool(val)));
			break;
		default:
			if (code.equalsIgnoreCase("allxml") && ID.equalsIgnoreCase("GenAbility"))
				parseAllXML(val);
			else
				super.setStat(code, val);
			break;
		}
	}

	private String convert(String[] options, int val, boolean mask)
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
			for(int a=0;a<Ability.ACODE_DESCS.length;a++)
			{
				if(val.equalsIgnoreCase(Ability.ACODE_DESCS[a]))
					tacod=a;
			}
			if(tacod<0)
			{
				for(int i=0;i<Ability.ACODE_DESCS.length;i++)
				{
					if(Ability.ACODE_DESCS[i].toUpperCase().startsWith(val.toUpperCase()))
						tacod=i;
				}
				if(tacod<0)
				{
					int tdom=-1;
					for(int a=0;a<Ability.DOMAIN_DESCS.length;a++)
					{
						if(val.equalsIgnoreCase(Ability.DOMAIN_DESCS[a]))
							tdom=a<<5;
					}
					if(tdom<0)
					{
						for(int i=0;i<Ability.DOMAIN_DESCS.length;i++)
						{
							if(Ability.DOMAIN_DESCS[i].toUpperCase().startsWith(val.toUpperCase())
									||Ability.DOMAIN_DESCS[i].toUpperCase().endsWith(val.toUpperCase()))
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

	private String convertClassAndDomain(int val)
	{
		final int dom=(val&Ability.ALL_DOMAINS)>>5;
		final int acod=val&Ability.ALL_ACODES;
		if((acod>=0)&&(acod<Ability.ACODE_DESCS.length)
		&&(dom>=0)&&(dom<Ability.DOMAIN_DESCS.length))
			return Ability.ACODE_DESCS[acod]+","+Ability.DOMAIN_DESCS[dom];
		return ""+val;
	}

	private int convert(String[] options, String val, boolean mask)
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

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenAbility))
			return false;
		if(!((GenAbility)E).ID().equals(ID))
			return false;
		if(!((GenAbility)E).text().equals(text()))
			return false;
		return true;
	}

	private void parseAllXML(String xml)
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
}
