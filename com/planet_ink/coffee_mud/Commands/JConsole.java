package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.ScriptLn;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.SubScript;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.InProto;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/*
   Copyright 2013-2025 Bo Zimmerman

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
public class JConsole extends StdCommand
{
	public JConsole()
	{
	}

	private final String[] access=I(new String[]{"JCONSOLE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static final Set<String> methH=new SHashSet<String>(ScriptingEngine.methods);
	public static final Set<String> funcH=new SHashSet<String>(ScriptingEngine.funcs);

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String rest="";
		if(commands.size()>1)
			rest=CMParms.combine(commands,1);
		final Session session=mob.session();
		if(session==null)
			return false;

		JScriptEvent scope=(JScriptEvent)Resources.getResource("JCONSOLE_"+mob.Name());
		final Context cx=Context.enter();
		try
		{
			if(scope==null)
			{
				try
				{
					scope = new JScriptEvent(mob);
					cx.initStandardObjects(scope);
					final String[] names = { "mob" ,"getVar", "setVar", "toJavaString", "getCMType"};
					scope.defineFunctionProperties(names, JScriptEvent.class, ScriptableObject.DONTENUM);
					Resources.submitResource("JCONSOLE_"+mob.Name(), scope);
				}
				catch(final Exception e)
				{
					mob.tell(L("Error: @x1",e.getMessage()));
					return false;
				}
			}
			if(rest.length()>0)
			{
				try
				{
					mob.tell(cx.evaluateString(scope, rest.toString(),"<cmd>", 1, null).toString());
					return true;
				}
				catch(final Exception e)
				{
					mob.tell(L("Error: @x1",e.getMessage()));
					return false;
				}
			}
			final JScriptEvent myScope=scope;
			final InputCallback IC[]=new InputCallback[1];
			final boolean[] addMode=new boolean[1];
			final StringBuilder inpBuilder=new StringBuilder("");
			myScope.c.tick(mob, Tickable.TICKID_MOB); // set lastknownlocation
			IC[0]=new InputCallback(InputCallback.Type.PROMPT)
			{
				@Override
				public void showPrompt()
				{
					session.print(addMode[0]?".":">");
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					if(this.input.equalsIgnoreCase("exit"))
						return;
					if(this.input.equals("<") && !addMode[0])
					{
						addMode[0]=true;
						if(session.isInlineAllowed(InProto.GMCP, "Siplet.Input", 0))
						{
							final String oldVal = inpBuilder.toString();
							session.sendInlineCommand(InProto.GMCP,
									"Siplet.Input", "{\"title\":\"JConsole JavaScript\","
													+ "\"text\":\""+MiniJSON.toJSONString(oldVal.toString())+"\"}");
							inpBuilder.setLength(0);
						}
						session.prompt(IC[0].reset());
						return;
					}
					if(this.input.equals(">") && addMode[0])
					{
						addMode[0]=false;
						this.input=inpBuilder.toString();
						inpBuilder.setLength(0);
					}
					if (addMode[0] && session.isInlineAllowed(InProto.GMCP, "Siplet.Input", 0))
					{
						this.input = CMStrings.replaceAll(this.input, "%0D", "\n");
						this.input += "\n";
						inpBuilder.setLength(0);
						inpBuilder.append(this.input);
						addMode[0]=false;
					}
					if(addMode[0])
						inpBuilder.append(this.input).append("\n");
					else
					if(this.input.length()>0)
					{
						try
						{
							final Context cx=Context.enter();
							session.safeRawPrintln(cx.evaluateString(myScope, this.input.replace('`','\''),"<cmd>", 1, null).toString());
						}
						catch(final Exception e)
						{
							session.println(e.getMessage());
						}
						finally
						{
							Context.exit();
						}
					}
					session.prompt(IC[0].reset());
				}
			};
			mob.tell(L("JConsole! Enter \"exit\" to exit."));
			session.prompt(IC[0]);
		}
		finally
		{
			Context.exit();
		}
		return false;
	}

	protected static class JScriptEvent extends ScriptableObject
	{
		@Override
		public String getClassName()
		{
			return "JScriptEvent";
		}
		static final long serialVersionUID=4223;
		final MOB mob;
		final public ScriptingEngine c;
		final Object[] objs=new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];

		public MOB mob()
		{
			return mob;
		}

		public void setVar(final String host, final String var, final String value)
		{
			c.setVar(host,var.toUpperCase(),value);
		}

		public String getVar(final String host, final String var)
		{
			return c.getVar(host,var);
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

		@Override
		public Object get(final String name, final Scriptable start)
		{
			if (super.has(name, start))
				return super.get(name, start);
			if (methH.contains(name) || funcH.contains(name)
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
								public void setFlag(final String flag)
								{
								}

								@Override
								public boolean isFlagSet(final String flag)
								{
									return false;
								}

							};
							DV.add(new ScriptLn("JS_PROG",null,null));
							DV.add(new ScriptLn(strb.toString(),null,null));
							final MPContext ctx = new MPContext(mob,mob,mob,null,null,null,"", objs);
							return c.execute(ctx.push(DV));
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
							final MPContext ctx = new MPContext(mob,mob,mob,null,null,null,"",objs);
							return c.functify(ctx,strb.toString());
						}
						final String[] sargs=new String[args.length+3];
						sargs[0]=name;
						sargs[1]="(";
						for(int i=0;i<args.length;i++)
							sargs[i+2]=String.valueOf(args[i]);
						sargs[sargs.length-1]=")";
						final String[][] eval={sargs};
						final MPContext ctx = new MPContext(mob,mob,mob,null,null,null,"",objs);
						return Boolean.valueOf(c.eval(ctx,eval,0));
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
			return super.get(name, start);
		}

		public JScriptEvent(final MOB mob)
		{
			c=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
			c.setScript("");
			this.mob=mob;
			c.tick(mob, Tickable.TICKID_MOB); // this sets lastknownlocation
		}
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JSCRIPTS);
	}
}
