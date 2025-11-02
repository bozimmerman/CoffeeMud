package com.planet_ink.coffee_mud.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

import com.planet_ink.coffee_mud.Common.DefaultQuest.QuestState;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Common.interfaces.Quest;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.ScriptLn;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.SubScript;
import com.planet_ink.coffee_mud.Exits.interfaces.Exit;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.*;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.SHashSet;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.PhysicalAgent;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

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
public class JavaScriptingEngine extends ScriptableObject implements ScriptingEngine, Cloneable
{
	private static final long serialVersionUID = 7866852658454850983L;
	public static final Set<String> methH=new SHashSet<String>(ScriptingEngine.methods); // JavaScripting includes MOBPROG support built-in
	public static final Set<String> funcH=new SHashSet<String>(ScriptingEngine.funcs); // JavaScripting includes MOBPROG support built-in

	protected String		script				= "";
	protected Quest			quest				= null;
	protected String		defaultQuestName	= "";
	protected boolean		isSavable			= false;
	protected String		scope				= "";
	protected String		scriptKey			= null;
	protected boolean		approvedScripts		= false;
	protected AtomicBoolean jsscopeLock			= new AtomicBoolean(false);

	protected final ScriptingEngine mpEngine	= (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
	protected transient MPContext	mpContext	= null;
	protected volatile MOB			factoryMOB	= null;

	@Override
	public String ID()
	{
		return "JavaScriptingEngine";
	}

	@Override
	public CMObject newInstance()
	{
		return new JavaScriptingEngine();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	public static String[] functions = {
		"quest",
		"setupState",
		"toJavaString",
		"getCMType",
		"mob",
		"getVar",
		"setVar",
		"host",
		"source",
		"target",
		"monster",
		"item",
		"item1",
		"item2",
		"message",
		"objs",
	};

	private Scriptable getScope()
	{
		if(!this.jsscopeLock.getAndSet(true))
		{
			delete("getClass");
			delete("JavaAdapter");
			delete("JavaImporter");
			defineFunctionProperties(functions,
					this.getClass(),
					ScriptableObject.DONTENUM);
			final LimitedContextFactory factory = new LimitedContextFactory(10);
			factory.call(new ContextAction()
			{
				@Override
				public Object run(final Context cx)
				{
					return cx.evaluateString(JavaScriptingEngine.this, script, "<cmd>", 1, null);
				}
			});
		}
		return this;
	}

	public Environmental getArgumentItem(final String name, final MPContext ctx)
	{
		if (name.startsWith("$") && (ctx != null) && (name.length()<4))
		{
			Environmental value = null;
			final Room lastKnownLocation = CMLib.map().roomLocation(ctx.monster);
			switch(name.charAt(1))
			{
				case 'a':
					value = (lastKnownLocation != null) ? lastKnownLocation.getArea() : null;
					break;
				case 'B':
				case 'b':
					break;
				case 'N':
				case 'n':
					value = ctx.source;
					break;
				case 'I':
				case 'i':
				case 'Q':
				case 'q':
					value = ctx.scripted;
					break;
				case 'T':
				case 't':
					value = ctx.target;
					break;
				case 'O':
				case 'o':
					value = ctx.primaryItem;
					break;
				case 'P':
				case 'p':
					value = ctx.secondaryItem;
					break;
				case 'd':
				case 'D':
					value = lastKnownLocation;
					break;
				case 'F':
				case 'f':
					if ((ctx.monster != null) && (ctx.monster.amFollowing() != null))
						value = ctx.monster.amFollowing();
					break;
				case 'r':
				case 'R':
					value = getRandPC(ctx.monster, lastKnownLocation);
					break;
				case 'c':
				case 'C':
					value = (lastKnownLocation!=null)?lastKnownLocation.fetchRandomInhabitant():null;
					break;
				case 'w':
					value = ctx.primaryItem != null ? ctx.primaryItem.owner() : null;
					break;
				case 'W':
					value =  ctx.secondaryItem != null ? ctx.secondaryItem.owner() : null;
					break;
				case 'x':
				case 'X':
					if (lastKnownLocation != null)
					{
						if ((name.length() > 2) && (CMLib.directions().getGoodDirectionCode("" + name.charAt(2)) >= 0))
							return lastKnownLocation.getExitInDir(CMLib.directions().getGoodDirectionCode("" + name.charAt(2)));
						int i = 0;
						Exit E = null;
						while (((++i) < 100) && (E == null))
							E = lastKnownLocation.getExitInDir(CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1));
						if(E!=null)
						return E;
					}
					return null;
				default:
					break;
			}
			return value;
		}
		return null;
	}

	@Override
	public Object get(final String name, final Scriptable start)
	{
		if (super.has(name, start))
			return super.get(name, start);
		if (name.startsWith("$") && (name.length()<4))
		{
			final Object value = this.getArgumentItem(name, mpContext);
			if (value != null)
			{
				final Context cx = Context.getCurrentContext();
				if (cx != null)
					return Context.javaToJS(value, this);
			}
		}
		if (super.has(name, start) || (mpContext==null))
			return super.get(name, start);
		if (methH.contains(name)
		|| funcH.contains(name)
		|| (name.endsWith("$")&&(funcH.contains(name.substring(0,name.length()-1)))))
		{
			return new Function()
			{
				@Override
				public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args)
				{
					if(methH.contains(name))
					{
						final StringBuilder strb=new StringBuilder(name);
						if(args.length==1)
							strb.append(" ").append(String.valueOf(args[0]));
						else
						for(int i=0;i<args.length;i++)
							if(i==args.length-1)
								strb.append(" ").append(String.valueOf(args[i]));
							else
								strb.append(" ").append("'"+String.valueOf(args[i])+"'");
						final SubScript DV=new SubScript()
						{
							private static final long serialVersionUID = -8744471025411719363L;
							@Override
							public int getTriggerCode()
							{
								return 0;
							}

							@Override
							public String[] getTriggerArgs()
							{
								return (size()>0)?get(0).second:null;
							}

							@Override
							public String getTriggerLine()
							{
								return (size()>0)?get(0).first:"";
							}

							@Override
							public String[] getTriggerBits()
							{
								return CMParms.getCleanBits(getTriggerLine());
							}

							@Override
							public void setFlag(final ScriptFlag flag)
							{
							}

							@Override
							public boolean isFlagSet(final ScriptFlag flag)
							{
								return false;
							}

						};
						DV.add(new ScriptLn("JS_PROG",null,null));
						DV.add(new ScriptLn(strb.toString(),null,null));
						final MPContext ctx = new MPContext(mpContext);
						return mpEngine.execute(ctx.push(DV));
					}
					if(name.endsWith("$"))
					{
						final StringBuilder strb=new StringBuilder(name.substring(0,name.length()-1)).append("(");
						if(args.length==1)
							strb.append(" ").append(String.valueOf(args[0]));
						else
						for(int i=0;i<args.length;i++)
							if(i==args.length-1)
								strb.append(" ").append(String.valueOf(args[i]));
							else
								strb.append(" ").append("'"+String.valueOf(args[i])+"'");
						strb.append(" ) ");
						final MPContext ctx = new MPContext(mpContext);
						return mpEngine.functify(ctx,strb.toString());
					}
					final String[] sargs=new String[args.length+3];
					sargs[0]=name;
					sargs[1]="(";
					for(int i=0;i<args.length;i++)
						sargs[i+2]=String.valueOf(args[i]);
					sargs[sargs.length-1]=")";
					final String[][] eval={sargs};
					final MPContext ctx = new MPContext(mpContext);
					return Boolean.valueOf(mpEngine.eval(ctx,eval,0));
				}

				@Override
				public void delete(final String arg0)
				{
				}

				@Override
				public void delete(final int arg0)
				{
				}

				@Override
				public Object get(final String arg0, final Scriptable arg1)
				{
					return null;
				}

				@Override
				public Object get(final int arg0, final Scriptable arg1)
				{
					return null;
				}

				@Override
				public String getClassName()
				{
					return null;
				}

				@Override
				public Object getDefaultValue(final Class<?> arg0)
				{
					return null;
				}

				@Override
				public Object[] getIds()
				{
					return null;
				}

				@Override
				public Scriptable getParentScope()
				{
					return null;
				}

				@Override
				public Scriptable getPrototype()
				{
					return null;
				}

				@Override
				public boolean has(final String arg0, final Scriptable arg1)
				{
					return false;
				}

				@Override
				public boolean has(final int arg0, final Scriptable arg1)
				{
					return false;
				}

				@Override
				public boolean hasInstance(final Scriptable arg0)
				{
					return false;
				}

				@Override
				public void put(final String arg0, final Scriptable arg1, final Object arg2)
				{
				}

				@Override
				public void put(final int arg0, final Scriptable arg1, final Object arg2)
				{
				}

				@Override
				public void setParentScope(final Scriptable arg0)
				{
				}

				@Override
				public void setPrototype(final Scriptable arg0)
				{
				}

				@Override
				public Scriptable construct(final Context arg0, final Scriptable arg1, final Object[] arg2)
				{
					return null;
				}
			};
		}
		return super.get(name, start); // Standard for other props
	}

	private void initializeContext(final CMObject scripted, final CMMsg msg)
	{
		if((mpContext == null)&&(scripted instanceof PhysicalAgent))
		{
			final MOB monster = getMakeMOB((Tickable)scripted);
			if(msg == null)
				mpContext = new MPContext((PhysicalAgent)scripted, monster, monster,
						null, null, null, "", null);
			else
			{
				final Item I = (msg.tool() instanceof Item) ? (Item)msg.tool() : null;
				final Item I2 = (msg.target() instanceof Item) ? (Item)msg.tool() : null;
				mpContext = new MPContext((PhysicalAgent)scripted, monster, msg.source(),
						msg.target(), I, I2, "", null);
			}
		}
	}

	private void updateContext(final CMObject scripted, final CMMsg msg)
	{
		if(mpContext == null)
			initializeContext(scripted, msg);
		else
		if(scripted instanceof PhysicalAgent)
		{
			if (scripted != mpContext.scripted)
			{
				mpContext.scripted = (PhysicalAgent) scripted;
				mpContext.monster = getMakeMOB((Tickable)scripted);
			}
			if(msg != null)
			{
				final Item I = (msg.tool() instanceof Item) ? (Item)msg.tool() : null;
				final Item I2 = (msg.target() instanceof Item) ? (Item)msg.tool() : null;
				mpContext.source = msg.source();
				mpContext.target = msg.target();
				mpContext.primaryItem = I;
				mpContext.secondaryItem = I2;
			}
		}
	}
	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		getScope(); // since this runs ONCE_PROG, its appropriate here.
		updateContext(ticking, null);
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		getScope(); // since this runs ONCE_PROG, its appropriate here.
		updateContext(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		getScope(); // since this runs ONCE_PROG, its appropriate here.
		updateContext(myHost, msg);
		return true;
	}

	protected MOB getRandPC(final MOB monster, final Room room)
	{
		MOB M=null;
		if(room!=null)
		{
			final List<MOB> choices = new ArrayList<MOB>(room.numInhabitants());
			for(int p=0;p<room.numInhabitants();p++)
			{
				M=room.fetchInhabitant(p);
				if((!M.isMonster())&&(M!=monster))
				{
					final HashSet<MOB> seen=new HashSet<MOB>();
					while((M.amFollowing()!=null)
					&&(!M.amFollowing().isMonster())
					&&(!seen.contains(M)))
					{
						seen.add(M);
						M=M.amFollowing();
					}
					choices.add(M);
				}
			}
			if(choices.size() > 0)
				return choices.get(CMLib.dice().roll(1,choices.size(),-1));
		}
		return null;
	}

	@Override
	public String execute(final MPContext ctx)
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=ctx.line;i<ctx.script.size();i++)
			str.append(ctx.script.get(i).first).append("\n");
		mpContext = ctx;
		getScope(); // make sure the scope is ready
		final LimitedContextFactory factory = new LimitedContextFactory(10);
		return (String)factory.call(new ContextAction()
		{
			@Override
			public Object run(final Context cx)
			{
				return cx.evaluateString(JavaScriptingEngine.this, str.toString(), "<cmd>", 1, null);
			}
		});
	}

	private Quest getQuest(String named)
	{
		if((defaultQuestName.length()>0)
		&&(named.equals("*")||named.equalsIgnoreCase(defaultQuestName)))
		{
			if(this.quest != null)
				return quest;
			named = defaultQuestName;
		}

		Quest Q=null;
		for(int i=0;i<CMLib.quests().numQuests();i++)
		{
			try
			{
				Q = CMLib.quests().fetchQuest(i);
			}
			catch (final Exception e)
			{
			}
			if(Q!=null)
			{
				if(Q.name().equalsIgnoreCase(named))
					break;
				else
				if(Q.running())
					break;
			}
		}
		if(Q == null)
			Q = CMLib.quests().fetchQuest(named);
		if((this.quest==null)
		&&(defaultQuestName.length()>0)
		&&(named.equals("*")||named.equalsIgnoreCase(defaultQuestName)))
			this.quest=Q;
		return Q;
	}

	@Override
	public String varify(final MPContext ctx, final String varifyableStr)
	{
		final Room lastKnownLocation = CMLib.map().roomLocation(ctx.monster);
		int t=varifyableStr.indexOf('$');
		if(t<0)
			return varifyableStr;
		MOB randMOB=null;
		final StringBuilder vchrs = new StringBuilder(varifyableStr);
		while((t>=0)&&(t<vchrs.length()-1))
		{
			if((t>0)&&(vchrs.charAt(t-1)=='\\'))
			{
				t=vchrs.indexOf("$",t+1);
				continue;
			}
			int replLen = 2;
			final char c=vchrs.charAt(t+1);
			String middle="";
			switch(c)
			{
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					break;
				case '$':
					middle="";
					t=t+1;
					vchrs.deleteCharAt(t);
					replLen=0;
					break;
				case '@':
					if ((t < vchrs.length() - 2)
					&& (Character.isLetter(vchrs.charAt(t + 2))||Character.isDigit(vchrs.charAt(t + 2))))
					{
						final Environmental E = getArgumentItem("$" + vchrs.charAt(t + 2), ctx);
						middle = (E == null) ? "null" : "" + E;
						replLen++;
					}
					break;
				case 'a':
					if (lastKnownLocation != null)
						middle = lastKnownLocation.getArea().name();
					break;
				// case 'a':
				case 'A':
					// unnecessary, since, in coffeemud, this is part of the
					// name
					break;
				case 'b':
					break;
				case 'B':
					break;
				case 'c':
				case 'C':
					randMOB = (lastKnownLocation!=null)?lastKnownLocation.fetchRandomInhabitant():null;
					if (randMOB != null)
						middle = randMOB.name();
					break;
				case 'd':
					middle = (lastKnownLocation != null) ? lastKnownLocation.displayText(ctx.monster) : "";
					break;
				case 'D':
					middle = (lastKnownLocation != null) ? lastKnownLocation.description(ctx.monster) : "";
					break;
				case 'e':
					if (ctx.source != null)
						middle = ctx.source.charStats().heshe();
					break;
				case 'E':
					if ((ctx.target != null) && (ctx.target instanceof MOB))
						middle = ((MOB) ctx.target).charStats().heshe();
					break;
				case 'f':
					if ((ctx.monster != null) && (ctx.monster.amFollowing() != null))
						middle = ctx.monster.amFollowing().name();
					break;
				case 'F':
					if ((ctx.monster != null) && (ctx.monster.amFollowing() != null))
						middle = ctx.monster.amFollowing().charStats().heshe();
					break;
				case 'g':
					middle = ((ctx.msg == null) ? "" : ctx.msg.toLowerCase());
					break;
				case 'G':
					middle = ((ctx.msg == null) ? "" : ctx.msg);
					break;
				case 'h':
					if (ctx.monster != null)
						middle = ctx.monster.charStats().himher();
					break;
				case 'H':
					randMOB = getRandPC(ctx.monster, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().himher();
					break;
				case 'i':
					if (ctx.monster != null)
						middle = ctx.monster.name();
					break;
				case 'I':
					if (ctx.monster != null)
						middle = ctx.monster.displayText();
					break;
				case 'j':
					if (ctx.monster != null)
						middle = ctx.monster.charStats().heshe();
					break;
				case 'J':
					randMOB = getRandPC(ctx.monster, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().heshe();
					break;
				case 'k':
					if (ctx.monster != null)
						middle = ctx.monster.charStats().hisher();
					break;
				case 'K':
					randMOB = getRandPC(ctx.monster, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.charStats().hisher();
					break;
				case 'l':
					if (lastKnownLocation != null)
					{
						final StringBuffer str = new StringBuffer("");
						for (int i = 0; i < lastKnownLocation.numInhabitants(); i++)
						{
							final MOB M = lastKnownLocation.fetchInhabitant(i);
							if ((M != null) && (M != ctx.monster) && (CMLib.flags().canBeSeenBy(M, ctx.monster)))
								str.append("\"" + M.name() + "\" ");
						}
						middle = str.toString();
					}
					break;
				case 'L':
					if (lastKnownLocation != null)
					{
						final StringBuffer str = new StringBuffer("");
						for (int i = 0; i < lastKnownLocation.numItems(); i++)
						{
							final Item I = lastKnownLocation.getItem(i);
							if ((I != null) && (I.container() == null) && (CMLib.flags().canBeSeenBy(I, ctx.monster)))
								str.append("\"" + I.name() + "\" ");
						}
						middle = str.toString();
					}
					break;
				case 'm':
					if (ctx.source != null)
						middle = ctx.source.charStats().hisher();
					break;
				case 'M':
					if ((ctx.target != null) && (ctx.target instanceof MOB))
						middle = ((MOB)ctx.target).charStats().hisher();
					break;
				case 'n':
				case 'N':
					if (ctx.source != null)
						middle = ctx.source.name();
					break;
				case 'o':
				case 'O':
					if (ctx.primaryItem != null)
						middle = ctx.primaryItem.name();
					break;
				case 'p':
				case 'P':
					if (ctx.secondaryItem != null)
						middle = ctx.secondaryItem.name();
					break;
				case 'q':
					if (ctx.scripted != null)
						middle = ctx.scripted.name();
					break;
				case 'Q':
					if (ctx.scripted != null)
						middle = ctx.scripted.displayText();
					break;
				case 'r':
				case 'R':
					randMOB = getRandPC(ctx.monster, lastKnownLocation);
					if (randMOB != null)
						middle = randMOB.name();
					break;
				case 's':
					if (ctx.source != null)
						middle = ctx.source.charStats().himher();
					break;
				case 'S':
					if ((ctx.target != null) && (ctx.target instanceof MOB))
						middle = ((MOB) ctx.target).charStats().himher();
					break;
				case 't':
				case 'T':
					if (ctx.target != null)
						middle = ctx.target.name();
					break;
				case 'w':
					middle = ctx.primaryItem != null ? ctx.primaryItem.owner().Name() : middle;
					break;
				case 'W':
					middle = ctx.secondaryItem != null ? ctx.secondaryItem.owner().Name() : middle;
					break;
				case 'x':
				case 'X':
					if (lastKnownLocation != null)
					{
						middle = "";
						Exit E = null;
						int dir = -1;
						if ((t < vchrs.length() - 2) && (CMLib.directions().getGoodDirectionCode("" + vchrs.charAt(t + 2)) >= 0))
						{
							dir = CMLib.directions().getGoodDirectionCode("" + vchrs.charAt(t + 2));
							E = lastKnownLocation.getExitInDir(dir);
							vchrs.deleteCharAt(t+2);
						}
						else
						{
							int i = 0;
							while (((++i) < 100) && (E == null))
							{
								dir = CMLib.dice().roll(1, Directions.NUM_DIRECTIONS(), -1);
								E = lastKnownLocation.getExitInDir(dir);
							}
						}
						if ((dir >= 0) && (E != null))
						{
							if (c == 'x')
								middle = CMLib.directions().getDirectionName(dir);
							else
								middle = E.name();
						}
					}
					break;
				case 'y':
					if (ctx.source != null)
						middle = ctx.source.charStats().sirmadam();
					break;
				case 'Y':
					if ((ctx.target != null) && (ctx.target instanceof MOB))
						middle = ((MOB)ctx.target).charStats().sirmadam();
					break;
				case '<':
				{
					final int x = vchrs.indexOf(">",t+2);
					if (x > t)
					{
						String mid = vchrs.substring(t+2, x);
						final int y = mid.indexOf(' ');
						Environmental E = null;
						String arg1 = "";
						if (y >= 0)
						{
							arg1 = mid.substring(0, y).trim();
							E = getArgumentItem(arg1, ctx);
							mid = mid.substring(y + 1).trim();
						}
						if (arg1.length() > 0)
							middle = this.getVar(E, arg1, ctx, mid);
						replLen=x-t+1;
					}
					break;
				}
				case '[':
				{
					middle = "";
					final int x = vchrs.indexOf("]",t+2);
					if (x > t)
					{
						String mid = vchrs.substring(t+2, x);
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								middle = Q.getQuestItemName(num);
						}
						replLen=x-t+1;
					}
					break;
				}
				case '{':
				{
					middle = "";
					final int x = vchrs.indexOf("}",t+2);
					if (x > t)
					{
						String mid = vchrs.substring(t+2, x);
						final int y = mid.indexOf(' ');
						if (y > 0)
						{
							final int num = CMath.s_int(mid.substring(0, y).trim());
							mid = mid.substring(y + 1).trim();
							final Quest Q = getQuest(mid);
							if (Q != null)
								middle = Q.getQuestMobName(num);
						}
						replLen=x-t+1;
					}
					break;
				}
				case '%':
				{
					middle = "";
					final int x = vchrs.indexOf("%",t+2);
					if (x > t)
					{
						middle = functify(ctx, vchrs.substring(t+2, x).trim());
						replLen=x-t+1;
					}
					break;
				}
			}
			if((t+replLen<vchrs.length()-1)
			&&(vchrs.charAt(t+replLen)=='.'))
			{
				if(vchrs.charAt(t+replLen+1)=='$')
					vchrs.replace(t+replLen+1, vchrs.length(), varify(ctx,vchrs.substring(t+replLen+1, vchrs.length())));
				if(vchrs.substring(t+replLen).startsWith(".LENGTH#"))
				{
					middle=""+CMParms.parse(middle).size();
					replLen += 8;
				}
				else
				if((t+replLen<vchrs.length()-1)
				&&(Character.isDigit(vchrs.charAt(t+replLen+1))))
				{
					final int digStartOffset=replLen+1;
					replLen+=2;
					while((t+replLen<vchrs.length())
					&&(Character.isDigit(vchrs.charAt(t+replLen))))
						replLen++;
					final int y=CMath.s_int(vchrs.substring(t+digStartOffset,t+replLen).trim());
					final boolean rest=vchrs.substring(t+replLen).startsWith("..");
					if(rest)
						replLen+=2;
					final List<String> V=CMParms.parse(middle);
					if((V.size()>0)&&(y>=0))
					{
						if(y>=V.size())
							middle="";
						else
						if(rest)
							middle=CMParms.combine(V,y);
						else
							middle=V.get(y);
					}
				}
			}
			if((middle.length()>0)&&(middle.indexOf('$')>=0))
				middle=CMStrings.replaceAll(middle, "$"+c, "_"+c); // prevent recursion!
			vchrs.replace(t, t+replLen, middle);
			t=vchrs.indexOf("$",t);
		}
		return CMStrings.deEscape(vchrs.toString());
	}

	@Override
	public void dequeResponses(final Object[] objects)
	{
		// this is called when reactions to triggers
		// have been confirmed, and are waiting to be executed.
		// as such, I don't think this really applies atm
	}

	@Override
	public MOB getMakeMOB(final Tickable ticking)
	{
		if(ticking instanceof MOB)
			return (MOB)ticking;
		final Room R = (ticking instanceof Environmental)?CMLib.map().roomLocation((Environmental)ticking):null;
		if(factoryMOB == null)
			factoryMOB = CMClass.getMOB("StdMOB");
		factoryMOB.setName(ticking.name());
		if(ticking instanceof Environmental)
			factoryMOB.setDisplayText(((Environmental)ticking).displayText());
		factoryMOB.setDescription("");
		factoryMOB.setAgeMinutes(-1);
		if(R!=null)
			factoryMOB.setLocation(R);
		return null;
	}

	@Override
	public String[] parseEval(final String evaluable) throws ScriptParseException
	{
		return new String[] {evaluable};
	}

	@Override
	public boolean eval(final MPContext ctx, final String[][] eval, final int startEval)
	{
		if (eval == null || startEval >= eval.length || eval[startEval].length == 0)
			return false; // Or throw, per your needs
		final String expression = eval[startEval][0];
		mpContext = ctx;
		getScope(); // make sure the scope is ready
		final LimitedContextFactory factory = new LimitedContextFactory(4);
		return ((Boolean)factory.call(new ContextAction()
		{
			@Override
			public Object run(final Context cx)
			{
				final Object result = cx.evaluateString(JavaScriptingEngine.this, "(" + expression + ")", "<eval>", 1, null);
				return Boolean.valueOf(Context.toBoolean(result));
			}
		})).booleanValue();
	}

	@Override
	public String functify(final MPContext ctx, final String evaluable)
	{
		mpContext = ctx;
		getScope(); // make sure the scope is ready
		final LimitedContextFactory factory = new LimitedContextFactory(4);
		return ((String)factory.call(new ContextAction()
		{
			@Override
			public Object run(final Context cx)
			{
				final Object result = cx.evaluateString(JavaScriptingEngine.this, evaluable, "<eval>", 1, null);
				return Context.toString(result);
			}
		}));
	}

	@Override
	public String callFunc(final String named, final String parms, final MPContext ctx)
	{
		final Object funcObj = get(named, this);
		if (funcObj instanceof Function)
		{
			final Function myFunc = (Function) funcObj;
			final Object[] args = { Context.toObject(parms, this) };
			mpContext = ctx;
			getScope(); // make sure the scope is ready
			final LimitedContextFactory factory = new LimitedContextFactory(10);
			return (String)factory.call(new ContextAction()
			{
				@Override
				public Object run(final Context cx)
				{
					final Object result = myFunc.call(cx, JavaScriptingEngine.this, JavaScriptingEngine.this, args);
					return Context.jsToJava(result, String.class);
				}
			});
		}
		return null;
	}

	@Override
	public boolean isFunc(final String named)
	{
		final Object[] keys = getIds();
		for (final Object key : keys)
		{
			if (key instanceof String)
			{
				final Object value = get((String) key, this);
				if (value instanceof Function)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean endQuest(final PhysicalAgent hostObj, final MOB mob, final String quest)
	{
		return false;
	}

	@Override
	public boolean stepQuest(final PhysicalAgent hostObj, final MOB mob, final String quest)
	{
		return false;
	}

	@Override
	public String getScript()
	{
		return script;
	}

	@Override
	public String getScriptResourceKey()
	{
		if(scriptKey == null)
			return ""+this;
		return scriptKey;
	}

	@Override
	public void setScript(final String newParms)
	{
		script = newParms;
		if(script.length()>100)
			scriptKey="PARSEDPRG: "+script.substring(0,100)+script.length()+script.hashCode();
		else
			scriptKey="PARSEDPRG: "+script;
		jsscopeLock.set(false);
	}

	@Override
	public List<String> externalFiles()
	{
		return new ArrayList<String>(1);
	}

	@Override
	public void registerDefaultQuest(final Object quest)
	{
		final String qName;
		if(quest instanceof String)
			qName=(String)quest;
		else
		if(quest instanceof Quest)
		{
			this.quest = (Quest)quest;
			qName=this.quest.name();
		}
		else
			return;

		if((qName==null)||(qName.trim().length()==0))
			defaultQuestName="";
		else
			defaultQuestName=qName.trim();
	}

	@Override
	public String defaultQuestName()
	{
		return defaultQuestName;
	}

	@Override
	public void setVarScope(final String scope)
	{
		this.scope = scope;
	}

	@Override
	public String getVarScope()
	{
		return scope;
	}

	@Override
	public String getLocalVarXML()
	{
		final StringBuilder xml = new StringBuilder();
		final XMLLibrary xmlLib = CMLib.xml();
		final Object[] keys = getIds();
		for (final Object key : keys)
		{
			if((key instanceof String) && (((String)key).indexOf('_')>0))
			{
				final Object value = get((String) key, this);
				if(!(value instanceof Function))
				{
					xml.append("<VAR NAME=\"").append(xmlLib.parseOutAngleBrackets(key.toString())).append("\">")
						.append(xmlLib.parseOutAngleBrackets(Context.jsToJava(value, String.class).toString()))
						.append("</VAR>");
				}
			}
		}
		return xml.toString();
	}

	@Override
	public void setLocalVarXML(final String xml)
	{
		final XMLLibrary xmlLib = CMLib.xml();
		final List<XMLTag> V = xmlLib.parseAllXML(xml);
		for (int v = 0; v < V.size(); v++)
		{
			final XMLTag tag = V.get(v);
			if (tag.tag().equalsIgnoreCase("VAR"))
			{
				final String name = tag.getParmValue("NAME");
				if (name.length() > 0)
					setVar("", name, xmlLib.restoreAngleBrackets(tag.value()));
			}
		}
	}

	@Override
	public boolean isSavable()
	{
		return isSavable;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
		isSavable = truefalse;
	}


	protected String getVar(final Environmental E, String rawHost, final MPContext ctx, final String variable)
	{
		if(!rawHost.equals("*"))
		{
			if(E==null)
				rawHost=varify(ctx,rawHost);
			else
			if(E instanceof Room)
				rawHost=CMLib.map().getExtendedRoomID((Room)E);
			else
				rawHost=E.Name();
		}
		return getVar(rawHost, variable);
	}

	@Override
	public String getVar(final String context, final String variable)
	{
		final String varName = (context.length()>0)?context+"_"+variable:variable;
		final Object myVarValue = get(varName, this);
		if(myVarValue != Scriptable.NOT_FOUND)
			return Context.jsToJava(myVarValue, String.class).toString();
		return mpEngine. getVar(context, variable);
	}

	@Override
	public boolean isVar(final String context, final String variable)
	{
		final String varName = (context.length()>0)?context+"_"+variable:variable;
		final Object myVarValue = get(varName, this);
		if(myVarValue == Scriptable.NOT_FOUND)
			return mpEngine.isVar(context, variable);
		return true;
	}

	@Override
	public void setVar(final String context, final String variable, final String value)
	{
		mpEngine.setVar(context, variable, value);
		final String varName = (context.length()>0)?context+"_"+variable:variable;
		if(value == null)
			this.delete(varName);
		else
		{
			final Object o = Context.javaToJS(value, this);
			this.put(varName, this, o);
		}
	}

	@Override
	public void preApproveScripts()
	{
		this.approvedScripts = true;
	}

	public class LimitedContextFactory extends ContextFactory
	{
		private final int secs;

		public LimitedContextFactory(final int secs)
		{
			this.secs = secs;
		}

		@Override
		protected Context makeContext()
		{
			final LimitedContext cx = new LimitedContext(this, secs);
			cx.setInstructionObserverThreshold(100);
			cx.initStandardObjects(JavaScriptingEngine.this);
			cx.setClassShutter(new CustomClassShutter());
			cx.setWrapFactory(new SandboxWrapFactory());
			return cx;
		}
	}

	public static class LimitedContext extends Context
	{
		private final long expirationDate;
		private final int  seconds;

		public LimitedContext(final ContextFactory factory, final int ellapsedSeconds)
		{
			super(factory);
			this.expirationDate = System.currentTimeMillis() + (ellapsedSeconds * 1000L);
			this.seconds = ellapsedSeconds;
		}

		@Override
		protected void observeInstructionCount(final int count)
		{
			if (System.currentTimeMillis() > expirationDate)
				throw new RuntimeException("Script execution timed out, violated "+seconds+" second limit.");
		}
	}

	public static class SandboxNativeJavaObject extends NativeJavaObject
	{
		private static final long serialVersionUID = 5046285725866712571L;

		public SandboxNativeJavaObject(final Scriptable scope, final Object javaObject, final Class<?> staticType)
		{
			super(scope, javaObject, staticType);
		}

		@Override
		public Object get(final String name, final Scriptable start)
		{
			if (javaObject instanceof Class<?>)
			{
				if (name.equals("getName")
				|| name.equals("getSimpleName")
				|| name.equals("getCanonicalName")
				|| name.equals("isInterface")
				|| name.equals("isArray")
				|| name.equals("isPrimitive")
				|| name.equals("getTypeName"))
					return super.get(name, start);
				return Scriptable.NOT_FOUND;
			}
			return super.get(name, start);
		}
	}

	public class SandboxWrapFactory extends WrapFactory
	{
		@Override
		public Object wrap(final Context cx, final Scriptable scope, final Object obj, final Class<?> staticType)
		{
			if ((obj instanceof Scriptable) || (obj == null))
				return super.wrap(cx, scope, obj, staticType);
			// Wrap Java objects with the sandboxed version
			return new SandboxNativeJavaObject(scope, obj, staticType);
		}

		@Override
		public Scriptable wrapAsJavaObject(final Context cx, final Scriptable scope, final Object javaObject, final Class<?> staticType)
		{
			return new SandboxNativeJavaObject(scope, javaObject, staticType);
		}
	}

	public class CustomClassShutter implements ClassShutter
	{
		@Override
		public boolean visibleToScripts(final String fullClassName)
		{
			if(fullClassName.startsWith("java.io.")
			||fullClassName.startsWith("java.nio.")
			||fullClassName.startsWith("java.lang.Runtime")
			||fullClassName.startsWith("java.lang.ProcessBuilder")
			||fullClassName.startsWith("java.lang.reflect")
			||fullClassName.startsWith("java.security")
			||fullClassName.startsWith("sun.")
			||fullClassName.startsWith("com.sun.")
			||fullClassName.startsWith("org.mozilla.javascript")
			||fullClassName.startsWith("java.rmi."))
				return false;
			return true;
		}
	}

	public String toJavaString(final Object O)
	{
		return Context.toString(O);
	}

	public String getCMType(final Object O)
	{
		if(O == null)
			return "null";
		final CMObjectType typ = CMClass.getObjectType(O);
		if(typ == null)
			return "unknown";
		return typ.name().toLowerCase();
	}

	public MOB mob()
	{
		if(this.mpContext != null)
			return this.mpContext.monster;
		return null;
	}

	public Quest quest()
	{
		return quest;
	}

	public Environmental host()
	{
		if (mpContext == null)
			return null;
		return mpContext.scripted;
	}

	public MOB source()
	{
		if (mpContext == null)
			return null;
		return mpContext.source;
	}

	public Environmental target()
	{
		if (mpContext == null)
			return null;
		return mpContext.target;
	}

	public MOB monster()
	{
		if (mpContext == null)
			return null;
		return mpContext.monster;
	}

	public Item item()
	{
		if (mpContext == null)
			return null;
		return mpContext.primaryItem;
	}

	public Item item2()
	{
		if (mpContext == null)
			return null;
		return mpContext.secondaryItem;
	}

	public Item item1()
	{
		if (mpContext == null)
			return null;
		return mpContext.primaryItem;
	}

	public Object[] objs()
	{
		if (mpContext == null)
			return null;
		return mpContext.tmp;
	}

	public String message()
	{
		return mpContext.msg;
	}

	public QuestState setupState()
	{
		if (quest instanceof DefaultQuest)
			return ((DefaultQuest)quest).questState;
		return null;
	}

	@Override
	public String getClassName()
	{
		return ID();
	}
}
