package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro;
import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/*
   Copyright 2013-2024 Bo Zimmerman

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
public class WebMacroCreamer extends StdLibrary implements WebMacroLibrary, SimpleServlet
{
	@Override
	public String ID()
	{
		return "WebMacroCreamer";
	}

	private static final Set<String> internalTags = new XHashSet<String>(new String[] {
		"if", "block", "/block", "trim", "/trim", "elif", "for?",
		"insert", "else", "loop", "back", "next", "endif", "/jscript", "jscript"
	});

	@Override
	public ByteBuffer convertOutput(final CWConfig config, final HTTPRequest request, final File pageFile, final HTTPStatus status, final ByteBuffer buffer) throws HTTPException
	{
		if (request.getRequestObjects().get("SYSTEM_HTTP_STATUS") == null)
		{
			request.getRequestObjects().put("SYSTEM_HTTP_STATUS", Integer.toString(status.getStatusCode()));
			request.getRequestObjects().put("SYSTEM_HTTP_STATUS_INFO", status.description());
		}
		final long[] systemStartTime = new long[] { System.currentTimeMillis() };
		if ((pageFile.getParent() != null)
		&& (CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&& (!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
		{
			final Clan mappedClan = CMLib.clans().getWebPathClan(pageFile.getParent());
			if (mappedClan != null)
				request.addFakeUrlParameter("CLAN", mappedClan.clanID());
		}

		try
		{
			return ByteBuffer.wrap(virtualPageFilter(request, request.getRequestObjects(), systemStartTime, new String[] { "" }, new StringBuffer(new String(buffer.array(),"UTF-8"))).toString().getBytes("UTF-8"));
		}
		catch (final UnsupportedEncodingException e)
		{
			throw new HTTPException(HTTPStatus.S500_INTERNAL_ERROR, e.getMessage());
		}
		catch (final HTTPRedirectException he)
		{
			throw new HTTPException(HTTPStatus.S307_TEMPORARY_REDIRECT, he.getMessage());
		}
	}

	@Override
	public byte[] virtualPageFilter(final byte[] data) throws HTTPRedirectException
	{
		return virtualPageFilter(new StringBuffer(new String(data))).toString().getBytes();
	}

	@Override
	public String virtualPageFilter(final String data) throws HTTPRedirectException
	{
		return virtualPageFilter(new StringBuffer(data)).toString();
	}

	@Override
	public StringBuffer virtualPageFilter(final StringBuffer data) throws HTTPRedirectException
	{
		return virtualPageFilter(data,new HashMap<String,String>(),new HashMap<String,Object>());
	}

	@Override
	public StringBuffer virtualPageFilter(final StringBuffer data, final Map<String,String> parms, final Map<String,Object> objs) throws HTTPRedirectException
	{
		return virtualPageFilter(new HTTPRequest()
		{
			public final Map<String, String>	params	= parms;
			public final Map<String, Object>	objects	= objs;

			@Override
			public String getHost()
			{
				return "localhost";
			}

			@Override
			public String getUrlPath()
			{
				return "localhost/file";
			}

			@Override
			public String getFullRequest()
			{
				return "GET " + getUrlPath();
			}

			@Override
			public String getUrlParameter(final String name)
			{
				return params.get(name.toLowerCase());
			}

			@Override
			public boolean isUrlParameter(final String name)
			{
				return params.containsKey(name.toLowerCase());
			}

			@Override
			public Map<String, String> getUrlParametersCopy()
			{
				return new XHashtable<String, String>(params);
			}

			@Override
			public Set<String> getUrlParameters()
			{
				return params.keySet();
			}

			@Override
			public HTTPMethod getMethod()
			{
				return HTTPMethod.GET;
			}

			@Override
			public String getHeader(final String name)
			{
				return null;
			}

			@Override
			public InetAddress getClientAddress()
			{
				try
				{
					return InetAddress.getLocalHost();
				}
				catch (final UnknownHostException e)
				{
					return null;
				}
			}

			@Override
			public int getClientPort()
			{
				return 0;
			}

			@Override
			public InputStream getBody()
			{
				return null;
			}

			@Override
			public String getCookie(final String name)
			{
				return null;
			}

			@Override
			public Set<String> getCookieNames()
			{
				return objects.keySet();
			}

			@Override
			public List<MultiPartData> getMultiParts()
			{
				return new XVector<MultiPartData>();
			}

			@Override
			public double getSpecialEncodingAcceptability(final String type)
			{
				return 0;
			}

			@Override
			public String getFullHost()
			{
				return "localhost";
			}

			@Override
			public List<long[]> getRangeAZ()
			{
				return null;
			}

			@Override
			public void addFakeUrlParameter(final String name, final String value)
			{
				params.put(name.toLowerCase(), value);
			}

			@Override
			public void removeUrlParameter(final String name)
			{
				params.remove(name.toLowerCase());
			}

			@Override
			public Map<String, Object> getRequestObjects()
			{
				return objects;
			}

			@Override
			public float getHttpVer()
			{
				return (float) 1.1;
			}

			@Override
			public String getQueryString()
			{
				return "";
			}
		}, new Hashtable<String, Object>(), new long[] { System.currentTimeMillis() }, new String[] { "" }, data);
	}

	@Override
	public String clearWebMacros(final String s)
	{
		if (s.length() == 0)
			return "";
		return clearWebMacros(new StringBuffer(s));
	}

	@Override
	public String parseFoundMacro(final StringBuffer s, final int i, final boolean lookOnly)
	{
		String foundMacro = null;
		boolean extend = false;
		for (int x = i + 1; x < s.length(); x++)
		{
			if ((s.charAt(x) == '@')
			&& (extend)
			&& (x < (s.length() - 1))
			&& (s.charAt(x + 1) == '@'))
			{
				if (!lookOnly)
					s.deleteCharAt(x);
				while ((x < s.length()) && (s.charAt(x) == '@'))
					x++;
				x--;
			}
			else
			if ((s.charAt(x) == '@')
			&& ((!extend) || (x >= s.length() - 1) || (s.charAt(x + 1) != '@')))
			{
				foundMacro = s.substring(i + 1, x);
				break;
			}
			else
			if ((s.charAt(x) == '?')
			&& (Character.isLetterOrDigit(s.charAt(x - 1))))
				extend = true;
			else
			if (((x - i) > CMClass.longestWebMacro) && (!extend))
				break;
		}
		return foundMacro;
	}

	@Override
	public String clearWebMacros(final StringBuffer s)
	{
		if (s.length() == 0)
			return "";
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				String foundMacro = parseFoundMacro(s, i, false);
				if ((foundMacro != null) && (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase("break"))
						i += (foundMacro.length() + 2);
					else
					{
						final int len = foundMacro.length();
						final int x = foundMacro.indexOf('?');
						if (x >= 0)
							foundMacro = foundMacro.substring(0, x);
						if(internalTags.contains(foundMacro.toLowerCase()))
							s.replace(i, i + len + 2, foundMacro);
						else
						{
							final WebMacro W = CMClass.getWebMacro(foundMacro.toUpperCase());
							if (W != null)
								s.replace(i, i + len + 2, foundMacro);
						}
					}
				}
			}
		}
		return s.toString();
	}

	// OK - this parser is getting a bit ugly now;
	// I'm probably gonna replace it soon
	@Override
	public StringBuffer virtualPageFilter(final HTTPRequest request, final Map<String, Object> objects, final long[] processStartTime, final String[] lastFoundMacro, final StringBuffer s) throws HTTPRedirectException
	{
		String redirectTo = null;
		final boolean debugMacros = CMSecurity.isDebugging(CMSecurity.DbgFlag.HTTPMACROS);
		boolean isAdminServer = false;
		if (Thread.currentThread() instanceof CWThread)
			isAdminServer = CMath.s_bool(((CWThread) Thread.currentThread()).getConfig().getMiscProp("ADMIN"));
		if ((!isAdminServer)
		&& (processStartTime[0] > 0)
		&& (System.currentTimeMillis() - processStartTime[0]) > (120 * 1000))
		{
			if (debugMacros)
				Log.infoOut(Thread.currentThread().getName(), "Encountered TIMEOUT!");
			return new StringBuffer("");
		}
		try
		{
			for (int i = 0; i < s.length(); i++)
			{
				if (s.charAt(i) == '@')
				{
					String foundMacro = parseFoundMacro(s, i, lastFoundMacro, false);
					if ((foundMacro != null) && (foundMacro.length() > 0))
					{
						if (debugMacros)
							Log.debugOut("ProcessHTTPRequest", "Found macro: " + foundMacro);
						if (foundMacro.startsWith("if?") || foundMacro.startsWith("IF?"))
						{
							final int macroLen = foundMacro.length() + 2;
							final int endIfIndex = myEndif(s, i + macroLen, lastFoundMacro);
							if (endIfIndex < 0)
							{
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "if without endif");
								s.replace(i, i + macroLen, "[if without endif]");
							}
							else
							{
								final int elseIndex = myElse(s, i + macroLen, endIfIndex, lastFoundMacro);
								foundMacro = foundMacro.substring(3);
								try
								{
									String compare = "true";
									if (foundMacro.startsWith("!"))
									{
										foundMacro = foundMacro.substring(1);
										compare = "false";
									}
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "Found IF macro: " + foundMacro);
									final String q = runMacro(request, foundMacro, lastFoundMacro, isAdminServer);
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "Ran IF macro: " + foundMacro + "=" + q);
									if ((q != null) && (q.equalsIgnoreCase(compare)))
									{
										if (debugMacros)
											Log.debugOut("ProcessHTTPRequest", "Result IF macro: TRUE");
										if (elseIndex >= 0)
											s.replace(elseIndex, endIfIndex + 7, "");
										else
											s.replace(endIfIndex, endIfIndex + 7, "");
										s.replace(i, i + macroLen, "");
									}
									else
									{
										if (debugMacros)
											Log.debugOut("ProcessHTTPRequest", "Result IF macro: FALSE");
										if (elseIndex >= 0)
										{
											if(Character.toLowerCase(s.charAt(elseIndex+3))=='i')
												s.replace(i--, elseIndex, "");
											else
												s.replace(i, elseIndex + 6, "");
										}
										else
											s.replace(i, endIfIndex + 7, "");
									}
								}
								catch (final HTTPRedirectException e)
								{
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "if exception: " + e.getMessage());
									redirectTo = e.getMessage();
								}
							}
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("/jscript"))
						{
							final int l = foundMacro.length() + 2;
							s.replace(i, i + l, "[/jscript without jscript]");
						}
						else
						if (foundMacro.equalsIgnoreCase("/html"))
						{
							final int l = foundMacro.length() + 2;
							s.replace(i, i + l, "[/html without html]");
						}
						else
						if (foundMacro.equalsIgnoreCase("jscript"))
						{
							final int l = foundMacro.length() + 2;
							final int v = myEndBlock(s, i + l, lastFoundMacro, "/jscript");
							if (v < 0)
								s.replace(i, i + l, "[jscript without /jscript]");
							else
							{
								final Context cx = Context.enter();
								try
								{
									final String script = s.substring(i + l, v);
									final JScriptablePage scope = new JScriptablePage(request);
									cx.initStandardObjects(scope);
									scope.defineFunctionProperties(JScriptablePage.functions,
																   JScriptablePage.class,
																   ScriptableObject.DONTENUM);
									cx.evaluateString(scope, script, "<cmd>", 1, null);
									s.replace(i, v + l + 1, scope.getBuffer());
									i = i + scope.getBuffer().length();
								}
								catch (final Exception e)
								{
									s.replace(i, v + l + 1, "[jscript error: " + e.getMessage() + "]");
								}
								Context.exit();
							}
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("<!--"))
						{
							final int l = foundMacro.length() + 2;
							final int v = myEndBlock(s, i + l, lastFoundMacro, "-->");
							if (v < 0)
								s.replace(i, i + l, "[<!-- macro without --> macro]");
							else
							{
								s.delete(i, v + l + 1);
							}
							continue;
						}
						else
						if (foundMacro.startsWith("block?") || foundMacro.startsWith("BLOCK?"))
						{
							final int l = foundMacro.length() + 2;
							final int v = myEndBlock(s, i + l, lastFoundMacro,"/block");
							if (v < 0)
								s.replace(i, i + l, "[block without /block]");
							else
							{
								final String name = foundMacro.substring(6).trim().toUpperCase();
								objects.put(name, s.substring(i + l, v));
								s.delete(i, v + 8);
							}
							continue;
						}
						else
						if (foundMacro.startsWith("trim?") || foundMacro.startsWith("TRIM?"))
						{
							final int l = foundMacro.length() + 2;
							final int v = myEndBlock(s, i + l, lastFoundMacro, "/trim");
							if (v < 0)
								s.replace(i, i + l, "[trim without /trim]");
							else
							{
								final String parms = foundMacro.substring(5).trim().toUpperCase();
								String part = s.substring(i + l, v);
								if(parms.indexOf("CR")>=0)
									part=CMStrings.replaceAll(part, "\n", "");
								if(parms.indexOf("LF")>=0)
									part=CMStrings.replaceAll(part, "\r", "");
								if(parms.indexOf("TAB")>=0)
									part=CMStrings.replaceAll(part, "\t", "");
								if(parms.indexOf("SPACE")>=0)
									part=CMStrings.replaceAll(part, " ", "");
								s.replace(i, v+8, part);
								i--;
							}
							continue;
						}
						else
						if (foundMacro.startsWith("insert?") || foundMacro.startsWith("INSERT?"))
						{
							final int l = foundMacro.length() + 2;
							final String name = foundMacro.substring(7).trim().toUpperCase();
							final Object o = objects.get(name);
							if (o != null)
								s.replace(i, i + l, o.toString());
							else
								s.delete(i, i + l);
							continue;
						}
						else
						if (foundMacro.startsWith("elif?") || foundMacro.startsWith("ELIF?"))
						{
							final int macroLen = foundMacro.length() + 2;
							final int endIfIndex = myEndif(s, i + macroLen, lastFoundMacro);
							if (endIfIndex < 0)
							{
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "if without endif");
								s.replace(i, i + macroLen, "[if without endif]");
							}
							else
							{
								final int elseIndex = myElse(s, i + macroLen, endIfIndex, lastFoundMacro);
								foundMacro = foundMacro.substring(5);
								try
								{
									String compare = "true";
									if (foundMacro.startsWith("!"))
									{
										foundMacro = foundMacro.substring(1);
										compare = "false";
									}
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "Found ELIF macro: " + foundMacro);
									final String q = runMacro(request, foundMacro, lastFoundMacro, isAdminServer);
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "Ran ELIF macro: " + foundMacro + "=" + q);
									if ((q != null) && (q.equalsIgnoreCase(compare)))
									{
										if (debugMacros)
											Log.debugOut("ProcessHTTPRequest", "Result ELIF macro: TRUE");
										if (elseIndex >= 0)
											s.replace(elseIndex, endIfIndex + 7, "");
										else
											s.replace(endIfIndex, endIfIndex + 7, "");
										s.replace(i, i + macroLen, "");
									}
									else
									{
										if (debugMacros)
											Log.debugOut("ProcessHTTPRequest", "Result ELIF macro: FALSE");
										if (elseIndex >= 0)
										{
											if(Character.toLowerCase(s.charAt(elseIndex+3))=='i')
												s.replace(i--, elseIndex, "");
											else
												s.replace(i, elseIndex + 6, "");
										}
										else
											s.replace(i, endIfIndex + 7, "");
									}
								}
								catch (final HTTPRedirectException e)
								{
									if (debugMacros)
										Log.debugOut("ProcessHTTPRequest", "elif exception: " + e.getMessage());
									redirectTo = e.getMessage();
								}
							}
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("endif"))
						{
							s.delete(i, i + 7);
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("else"))
						{
							s.delete(i, i + 6);
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("loop"))
						{
							final int v = myBack(s, i + 6, lastFoundMacro);
							if (v < 0)
								s.replace(i, i + 6, "[loop without back]");
							else
							{
								final String s2 = s.substring(i + 6, v);
								s.replace(i, v + 6, "");
								int ldex = i;
								String s3 = " ";
								while (s3.length() > 0)
								{
									if(s.length() > 1024 * 1024 * 200)
									{
										Log.errOut("WebMacroCreamer", "200mb loop size failure on "+request.getFullRequest());
										s.insert(ldex, "[error]");
										ldex += 7;
										break;
									}
									try
									{
										s3 = new String(virtualPageFilter(request, objects, processStartTime, lastFoundMacro, new StringBuffer(s2)));
									}
									catch (final HTTPRedirectException e)
									{
										s3 = " ";
										redirectTo = e.getMessage();
									}
									s.insert(ldex, s3);
									ldex += s3.length();
								}
								if ((!isAdminServer)
								&& (processStartTime[0] > 0)
								&& (System.currentTimeMillis() - processStartTime[0]) > (120 * 1000))
								{
									if (debugMacros)
										Log.infoOut(Thread.currentThread().getName(), "Encountered TIMEOUT!");
									return new StringBuffer("");
								}
							}
							continue;
						}
						else
						if (foundMacro.startsWith("for?") || foundMacro.startsWith("FOR?"))
						{
							final String forCond = foundMacro.substring(4);
							final int fc = forCond.indexOf('=');
							final int v = myNext(s, i + foundMacro.length() + 2, lastFoundMacro);
							if (fc < 0)
								s.replace(i, i + foundMacro.length() + 2, "[for without variabledef=]");
							else
							if (v < 0)
								s.replace(i, i + foundMacro.length() + 2, "[for without next]");
							else
							{
								final String s2 = s.substring(i + foundMacro.length() + 2, v);
								s.replace(i, v + 6, "");
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "Found FOR macro: " + foundMacro);
								final String varName = forCond.substring(0, fc).trim().toUpperCase();
								String q = runMacro(request, forCond.substring(fc + 1).trim(), lastFoundMacro, isAdminServer);
								if (q == null)
									q = forCond.substring(fc + 1).trim();
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "Ran FOR macro: " + foundMacro + "=" + q);
								int ldex = i;
								String s3 = " ";
								List<String> set;
								if ((q == null) || (q.trim().length() == 0) || (q.toLowerCase().indexOf("@break@") >= 0))
									set = new XVector<String>();
								else
									set = CMParms.parseCommas(q, false);
								for (final String qq : set)
								{
									if(s.length() > 1024 * 1024 * 200)
									{
										Log.errOut("WebMacroCreamer", "200mb loop size failure on "+request.getFullRequest());
										s.insert(ldex, "[error]");
										ldex += 7;
										break;
									}
									try
									{
										request.addFakeUrlParameter(varName.toUpperCase(), qq);
										request.addFakeUrlParameter(varName.toLowerCase(), qq);
										s3 = new String(virtualPageFilter(request, objects, processStartTime, lastFoundMacro, new StringBuffer(s2)));
									}
									catch (final HTTPRedirectException e)
									{
										s3 = " ";
										redirectTo = e.getMessage();
									}

									s.insert(ldex, s3);
									ldex += s3.length();
								}
								if ((!isAdminServer)
								&& (processStartTime[0] > 0)
								&& (System.currentTimeMillis() - processStartTime[0]) > (120 * 1000))
								{
									if (debugMacros)
										Log.infoOut(Thread.currentThread().getName(), "Encountered TIMEOUT!");
									return new StringBuffer("");
								}
							}
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("break"))
						{
							if (debugMacros)
								Log.infoOut(Thread.currentThread().getName(), "Encountered BREAK! at " + i);
							return new StringBuffer("");
						}
						else
						if (foundMacro.equalsIgnoreCase("back"))
						{
							s.replace(i, i + 6, "[back without loop]");
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("if"))
						{
							s.replace(i, i + 6, "[naked if condition]");
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("block"))
						{
							s.replace(i, i + 6, "[unnamed block]");
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("insert"))
						{
							s.replace(i, i + 6, "[naked block insert]");
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("for"))
						{
							s.replace(i, i + 6, "[naked for iterator]");
							continue;
						}
						else
						if (foundMacro.equalsIgnoreCase("next"))
						{
							s.replace(i, i + 6, "[next without for]");
							continue;
						}

						if (foundMacro.length() > 0)
						{
							try
							{
								final int l = foundMacro.length();
								final String q = runMacro(request, foundMacro, lastFoundMacro, isAdminServer);
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "Ran Macro: " + foundMacro + "=" + q);
								if (q != null)
								{
									if ((debugMacros) && (q.toUpperCase().indexOf("@BREAK@") >= 0))
										Log.infoOut(Thread.currentThread().getName(), "WebMacro:" + foundMacro + " generated a BREAK! at " + i);
									s.replace(i, i + l + 2, q);
								}
								else
									s.replace(i, i + l + 2, "[error]");
							}
							catch (final HTTPRedirectException e)
							{
								// can't just do this:
								// throw e;
								// since we want ALL macros on the page to run
								redirectTo = e.getMessage();
								// doesn't bother to replace original
								// macro text; page will never be seen
								// (replaced by redirection page)
							}
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			final String errMsg = e.getMessage() == null ? e.toString() : e.getMessage();
			if ((errMsg != null)
			&& ((!Log.isMaskedErrMsg(errMsg)) || (CMSecurity.isDebugging(CMSecurity.DbgFlag.HTTPMACROS)))
			&&(e.getStackTrace()!=null)
			&&(e.getStackTrace().length>0))
			{
				Log.errOut(Thread.currentThread().getName(), "Exception in virtualPageFilter() - " + errMsg);
				Log.errOut(Thread.currentThread().getName(), e);
			}
		}

		if (redirectTo != null)
		{
			throw new HTTPRedirectException(redirectTo);
		}

		return s;
	}

	protected String runMacro(final HTTPRequest request, String foundMacro, final String[] lastFoundMacro, final boolean isAdminServer) throws HTTPRedirectException, HTTPServerException
	{
		final int x = foundMacro.indexOf('?');
		StringBuffer parms = null;
		if (x >= 0)
		{
			parms = new StringBuffer(foundMacro.substring(x + 1));
			foundMacro = foundMacro.substring(0, x);
			int y = parms.indexOf("@");
			while (y >= 0)
			{
				final String newFoundMacro = parseFoundMacro(parms, y, lastFoundMacro, false);
				if ((newFoundMacro != null) && (newFoundMacro.length() > 0))
				{
					final int l = newFoundMacro.length();
					final String qq = runMacro(request, newFoundMacro, lastFoundMacro, isAdminServer);
					if (qq != null)
						parms.replace(y, y + l + 2, qq);
					else
						parms.replace(y, y + l + 2, "[error]");
				}
				else
					break;
				y = parms.indexOf("@");
			}
		}
		if (foundMacro.length() == 0)
			return "";
		final WebMacro W = CMClass.getWebMacro(foundMacro.toUpperCase());
		if (W != null)
		{
			String q = null;
			if (!isAdminServer && W.isAdminMacro())
			{
				Log.errOut(Thread.currentThread().getName(), "Non-admin cannot access admin macro '" + W.name() + "'; client IP: " + request.getClientAddress());
				q = "[error]";
			}
			else
			if (W.preferBinary())
			{
				final byte[] bin = W.runBinaryMacro(request, (parms == null) ? null : parms.toString(), null);
				if (bin == null)
					q = " @break@";
				else
					q = new String(bin);
			}
			else
				q = W.runMacro(request, (parms == null) ? null : parms.toString(), null);
			if (q != null)
				return q;
			return "[error]";
		}
		return null;
	}

	protected String parseFoundMacro(final StringBuffer s, final int i, final String[] lastFoundMacro, final boolean lookOnly)
	{
		String foundMacro = null;
		boolean extend = false;
		if ((i < s.length() - 2)
		&& ((s.charAt(i + 1) == 'X') || (s.charAt(i + 1) == 'x'))
		&& (Character.isDigit(s.charAt(i + 2)) || (s.charAt(i + 1) == 'x') || (s.charAt(i + 1) == 'X')))
			return null;
		for (int x = i + 1; x < s.length(); x++)
		{
			if ((s.charAt(x) == '@')
			&& (extend)
			&& (x < (s.length() - 1))
			&& (s.charAt(x + 1) == '@'))
			{
				if (!lookOnly)
					s.deleteCharAt(x);
				while ((x < s.length()) && (s.charAt(x) == '@'))
					x++;
				x--;
			}
			else
			if ((s.charAt(x) == '@')
			&& ((!extend) || (x >= s.length() - 1) || (s.charAt(x + 1) != '@')))
			{
				foundMacro = s.substring(i + 1, x);
				break;
			}
			else
			if ((s.charAt(x) == '?')
			&& (Character.isLetterOrDigit(s.charAt(x - 1))))
				extend = true;
			else
			if (((x - i) > CMClass.longestWebMacro) && (!extend))
				break;
		}
		lastFoundMacro[0] = foundMacro;
		return foundMacro;
	}

	protected int myElse(final StringBuffer s, int i, final int end, final String[] lastFoundMacro)
	{
		int endifsToFind = 1;
		for (; i < end; i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null)
				&& (foundMacro.length() > 0))
				{
					if (foundMacro.startsWith("if?"))
						endifsToFind++;
					else
					if (foundMacro.equalsIgnoreCase("endif"))
					{
						endifsToFind--;
						if (endifsToFind <= 0)
							return -1;
					}
					else
					if ((foundMacro.startsWith("elif?"))
					&& (endifsToFind == 1))
						return i;
					else
					if ((foundMacro.equalsIgnoreCase("else"))
					&& (endifsToFind == 1))
						return i;
					i += foundMacro.length() + 1;
				}
			}
		}
		return -1;
	}

	protected int myBack(final StringBuffer s, int i, final String[] lastFoundMacro)
	{
		int backsToFind = 1;
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null)
				&& (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase("loop"))
						backsToFind++;
					else
					if (foundMacro.equalsIgnoreCase("back"))
					{
						backsToFind--;
						if (backsToFind <= 0)
							return i;
					}
					i += foundMacro.length() + 1;
				}
			}
		}
		return -1;
	}

	protected int myNext(final StringBuffer s, int i, final String[] lastFoundMacro)
	{
		int nextsToFind = 1;
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null)
				&& (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase("for"))
						nextsToFind++;
					else
					if (foundMacro.equalsIgnoreCase("next"))
					{
						nextsToFind--;
						if (nextsToFind <= 0)
							return i;
					}
					i += foundMacro.length() + 1;
				}
			}
		}
		return -1;
	}

	protected int myEndif(final StringBuffer s, int i, final String[] lastFoundMacro)
	{
		int endifsToFind = 1;
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null) && (foundMacro.length() > 0))
				{
					if (foundMacro.startsWith("if?"))
						endifsToFind++;
					else
					if (foundMacro.equalsIgnoreCase("endif"))
					{
						endifsToFind--;
						if (endifsToFind <= 0)
							return i;
					}
				}
			}
		}
		return -1;
	}

	private int myEndBlock(final StringBuffer s, int i, final String[] lastFoundMacro, final String endTag)
	{
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null) && (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase(endTag))
						return i;
					i += foundMacro.length() + 1;
				}
			}
		}
		return -1;
	}

	protected static class JScriptablePage extends ScriptableObject
	{
		@Override
		public String getClassName()
		{
			return "JScriptablePage";
		}

		static final long	serialVersionUID	= 43;
		StringBuffer		buf					= new StringBuffer("");

		public void write(final Object O)
		{
			buf.append(Context.toString(O));
		}

		public String getBuffer()
		{
			return buf.toString();
		}

		HTTPRequest	req	= null;

		public HTTPRequest request()
		{
			return req;
		}

		public JScriptablePage(final HTTPRequest requests)
		{
			req = requests;
		}

		public static String[]	functions	= { "request", "write", "toJavaString", "getCMType" };

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
	}

	@Override
	public void init()
	{
	}

	@Override
	public void doGet(final SimpleServletRequest request, final SimpleServletResponse response) throws HTTPException
	{
		final String[] url = request.getUrlPath().split("/");
		if (url.length > 0)
		{
			String macroName = url[url.length - 1];
			final int x = macroName.indexOf('?');
			if (x > 0)
				macroName = macroName.substring(0, x);
			final WebMacro W = CMClass.getWebMacro(macroName.toUpperCase());
			if (W == null)
			{
				Log.errOut("WebMacroCreamer", "No web macro: '" + macroName + "'");
			}
			else
			if (!W.isAWebPath())
			{
				Log.errOut("WebMacroCreamer", "Macro " + macroName + " is not a web path.");
			}
			else
			{
				try
				{
					try
					{
						response.setHeader("Content-Type", MIMEType.All.getMIMEType("").getType());
						byte[] responseData;
						if (W.preferBinary())
							responseData = W.runBinaryMacro(request, "", response);
						else
							responseData = W.runMacro(request, "", response).getBytes();
						if(responseData!=null)
							response.getOutputStream().write(responseData);
					}
					catch (final HTTPServerException e)
					{
						if(e.getCause() instanceof HTTPException)
							throw (HTTPException)e.getCause();
						try
						{
							response.getOutputStream().write(HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR).generateOutput(request).flushToBuffer().array());
						}
						catch (final HTTPException e1)
						{
						}
					}
				}
				catch (final IOException e2)
				{
				}
			}
		}
	}

	@Override
	public void doPost(final SimpleServletRequest request, final SimpleServletResponse response) throws HTTPException
	{
		doGet(request, response);
	}

	@Override
	public void service(final HTTPMethod method, final SimpleServletRequest request, final SimpleServletResponse response) throws HTTPException
	{
	}

	@Override
	public boolean activate()
	{
		return true;
	}

	@Override
	public boolean shutdown()
	{
		if (CMLib.threads().isTicking(this, TICKID_SUPPORT | Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT | Tickable.TICKID_SOLITARYMASK);
			serviceClient = null;
		}
		return true;
	}

	@Override
	public TickClient getServiceClient()
	{
		return serviceClient;
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		try
		{
			tickStatus = Tickable.STATUS_ALIVE;
			final String timesPerRunStr = Resources.getPropResource("WEBMACROCREAMER", "TIMESPERRUN");
			if (!CMath.isInteger(timesPerRunStr))
			{
				Log.errOut("WebMacroCreamer var TIMESPERRUN not properly set.");
				return true;
			}
		}
		finally
		{
			tickStatus = Tickable.STATUS_NOT;
			Resources.savePropResources();
		}
		return true;
	}

	private Collection<MOB> getMOBCache()
	{
		@SuppressWarnings("unchecked")
		Collection<MOB> mobSet=(Collection<MOB>)Resources.getResource("SYSTEM_WEB_MOB_CACHE");
		if(mobSet==null)
		{
			mobSet=new SLinkedList<MOB>();
			Resources.submitResource("SYSTEM_WEB_MOB_CACHE", mobSet);
		}
		return mobSet;
	}

	@Override
	public Iterable<MOB> getMOBWebCacheIterable()
	{
		return getMOBCache();
	}

	@Override
	public boolean isWebCachedMOB(final Object M)
	{
		if(M!=null)
			return getMOBCache().contains(M);
		return false;
	}

	private Collection<Item> getItemCache()
	{
		@SuppressWarnings("unchecked")
		Collection<Item> itemSet=(Collection<Item>)Resources.getResource("SYSTEM_WEB_ITEM_CACHE");
		if(itemSet==null)
		{
			itemSet=new SLinkedList<Item>();
			Resources.submitResource("SYSTEM_WEB_ITEM_CACHE", itemSet);
		}
		return itemSet;
	}

	@Override
	public Iterable<Item> getItemWebCacheIterable()
	{
		return getItemCache();
	}

	@Override
	public boolean isWebCachedItem(final Object I)
	{
		if(I!=null)
			return getItemCache().contains(I);
		return false;
	}

	@Override
	public String findItemWebCacheCode(final Room R, final Item I)
	{
		if(I==null)
			return "";
		for(int i=0;i<R.numItems();i++)
		{
			if(R.getItem(i)==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+i;
		}
		return "";
	}

	@Override
	public String findItemWebCacheCode(final Item I)
	{
		return findItemWebCacheCode(getItemCache(), I);
	}

	@Override
	public String findItemWebCacheCode(final Collection<Item> allitems, final Item I)
	{
		if(I==null)
			return "";
		int x=0;
		for (final Item I2 : allitems)
		{
			if(I2==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+x;
			x++;
		}
		x=0;
		for (final Item I2 : allitems)
		{
			if(I2.sameAs(I))
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+x;
			x++;
		}
		return "";
	}

	@Override
	public String findItemWebCacheCode(final MOB M, final Item I)
	{
		if(I==null)
			return "";
		for(int i=0;i<M.numItems();i++)
		{
			if(M.getItem(i)==I)
				return Long.toString( ( I.ID() + "/" + I.Name() + "/" + I.displayText() ).hashCode() << 5 ) + i;
		}
		if(M instanceof ShopKeeper)
		{
			final ShopKeeper shopK=(ShopKeeper)M;
			final CoffeeShop shop=shopK.getShop();
			int x=0;
			for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
			{
				final Environmental E=i.next();
				if(E==I)
					return Long.toString( ( I.ID() + "/" + I.Name() + "/" + I.displayText() ).hashCode() << 5 ) + x;
				x++;
			}
		}
		return "";
	}

	@Override
	public String findMOBWebCacheCode(final Room R, final MOB M)
	{
		if(M==null)
			return "";
		int code=0;
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M2=R.fetchInhabitant(i);
			if(M==M2)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + code;
			else
			if((M2!=null)
			&&(M2.isSavable()||(!R.isSavable())))
				code++;
		}
		return "";
	}

	@Override
	public String findMOBWebCacheCode(final MOB M)
	{
		final String code = findMOBWebCacheCode(getMOBCache(), M);
		return code;
	}

	@Override
	public String findMOBWebCacheCode(final Collection<MOB> mobs, final MOB M)
	{
		if(M==null)
			return "";
		int i=0;
		for (final MOB M2 : mobs)
		{
			if(M2==M)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + i;
			i++;
		}
		i=0;
		for (final MOB M2 : mobs)
		{
			if(M2.sameAs(M))
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + i;
			i++;
		}
		return "";
	}

	@Override
	public Item getItemFromWebCache(final MOB M, String code)
	{
		if(M==null)
			return getItemFromWebCache(getItemCache(),code);
		final String origCode=code;
		for(int i=0;i<M.numItems();i++)
		{
			final Item I=M.getItem(i);
			if((I!=null)&&(findItemWebCacheCode(M,I).equals(code)))
				return I;
		}
		if(code.length()>2)
			code=code.substring(0,code.length()-2);
		for(int i=0;i<M.numItems();i++)
		{
			final Item I=M.getItem(i);
			if((I!=null)&&(findItemWebCacheCode(M,I).startsWith(code)))
				return I;
		}
		if(M instanceof ShopKeeper)
		{
			final CoffeeShop shop=((ShopKeeper)M).getShop();
			code=origCode;
			for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
			{
				final Environmental E=i.next();
				if(E instanceof Item)
				{
					if(findItemWebCacheCode(M,(Item)E).equals(code))
						return (Item)E;
				}
			}
			if(code.length()>2)
				code=code.substring(0,code.length()-2);
			for(final Iterator<Environmental> i=shop.getStoreInventory();i.hasNext();)
			{
				final Environmental E=i.next();
				if(E instanceof Item)
				{
					if(findItemWebCacheCode(M,(Item)E).startsWith(code))
						return (Item)E;
				}
			}
		}
		return null;
	}

	@Override
	public Item getItemFromWebCache(final Room R, String code)
	{
		if(R==null)
			return getItemFromWebCache(getItemCache(),code);
		for(int i=0;i<R.numItems();i++)
		{
			if(findItemWebCacheCode(R,R.getItem(i)).equals(code))
				return R.getItem(i);
		}
		if(code.length()>2)
			code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numItems();i++)
		{
			if(findItemWebCacheCode(R,R.getItem(i)).startsWith(code))
				return R.getItem(i);
		}
		return null;
	}

	@Override
	public Item getItemFromWebCache(final String code)
	{
		return getItemFromWebCache(getItemCache(), code);
	}

	@Override
	public Item getItemFromWebCache(final Collection<Item> allitems, String code)
	{
		if(code.startsWith("CATALOG-"))
			return getItemFromCatalog(code);
		for (final Item I : allitems)
		{
			if(findItemWebCacheCode(allitems,I).equals(code))
				return I;
		}
		if(code.length()>2)
			code=code.substring(0,code.length()-2);
		for (final Item I : allitems)
		{
			if(findItemWebCacheCode(allitems,I).startsWith(code))
				return I;
		}
		return null;
	}

	@Override
	public MOB getMOBFromWebCache(final Room R, String code)
	{
		if(R==null)
			return getMOBFromWebCache(code);
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(findMOBWebCacheCode(R,R.fetchInhabitant(i)).equals(code))
				return R.fetchInhabitant(i);
		}
		if(code.length()>2)
			code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(findMOBWebCacheCode(R,R.fetchInhabitant(i)).startsWith(code))
				return R.fetchInhabitant(i);
		}
		return null;
	}

	@Override
	public MOB getMOBFromWebCache(final String code)
	{
		return getMOBFromWebCache(getMOBCache(), code);
	}

	@Override
	public MOB getMOBFromWebCache(final Collection<MOB> allmobs, String code)
	{
		if(code.startsWith("CATALOG-"))
			return getMOBFromCatalog(code);
		for (final MOB M2 : allmobs)
		{
			if(findMOBWebCacheCode(allmobs,M2).equals(code))
				return M2;
		}
		if(code.length()>2)
			code=code.substring(0,code.length()-2);
		for (final MOB M2 : allmobs)
		{
			if(findMOBWebCacheCode(allmobs,M2).startsWith(code))
				return M2;
		}
		return null;
	}

	@Override
	public MOB getMOBFromCatalog(final String MATCHING)
	{
		if(!MATCHING.startsWith("CATALOG-"))
			return null;
		MOB M2=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
		if(M2!=null)
		{
			M2=(MOB)M2.copyOf();
			CMLib.catalog().changeCatalogUsage(M2,true);
		}
		return M2;
	}

	@Override
	public Item getItemFromCatalog(final String MATCHING)
	{
		if(!MATCHING.startsWith("CATALOG-"))
			return null;
		Item I=CMLib.catalog().getCatalogItem(MATCHING.substring(8));
		if(I!=null)
		{
			I=(Item)I.copyOf();
			CMLib.catalog().changeCatalogUsage(I,true);
		}
		return I;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String getAppropriateCode(final PhysicalAgent E, final Physical RorM, final Collection classes)
	{
		if(CMLib.flags().isCataloged(E))
			return "CATALOG-"+E.Name();
		else
		if(isWebCachedItem(E)||isWebCachedMOB(E))
			return ""+E;
		else
		if(((RorM instanceof Room)&&(((Room)RorM).isHere(E)))
		||((RorM instanceof MOB)&&(((MOB)RorM).isMine(E))))
			return (E instanceof Item)?findItemWebCacheCode(classes,(Item)E):findMOBWebCacheCode(classes,(MOB)E);
		return E.ID();
	}

	@Override
	public Item findItemInWebCache(final String MATCHING)
	{
		return findItemInAnything(getItemCache(), MATCHING);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Item findItemInAnything(final Object allitems, final String MATCHING)
	{
		if(isAllNum(MATCHING))
		{
			if(allitems instanceof Room)
				return getItemFromWebCache((Room)allitems,MATCHING);
			else
			if(allitems instanceof MOB)
				return getItemFromWebCache((MOB)allitems,MATCHING);
			else
			if(allitems instanceof Collection)
				return getItemFromWebCache((Collection)allitems,MATCHING);
		}
		else
		if(MATCHING.startsWith("CATALOG-"))
			return getItemFromCatalog(MATCHING);
		else
		if(MATCHING.indexOf('@')>0)
		{
			for (final Item I2 : getItemCache())
			{
				if(MATCHING.equals(""+I2))
					return I2;
			}
		}
		else
		{
			final Item I=CMClass.getItem(MATCHING);
			if((I!=null)
			&&(!(I instanceof ArchonOnly)))
				return I;
			for(final Enumeration<Item> m=CMClass.allItems();m.hasMoreElements();)
			{
				final Item I2=m.nextElement();
				if(CMClass.classID(I2).equals(MATCHING)
				&&(!I2.isGeneric()))
					return (Item)I2.copyOf();
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MOB getMOBFromAnywhere(final String MATCHING)
	{
		if(isAllNum(MATCHING))
			return getMOBFromWebCache((Collection)null,MATCHING);
		else
		if(MATCHING.startsWith("CATALOG-"))
			return getMOBFromCatalog(MATCHING);
		else
		if(MATCHING.indexOf('@')>0)
		{
			for (final MOB M2 : getMOBCache())
			{
				if(MATCHING.equals(""+M2))
					return M2;
			}
		}
		return CMClass.getMOB(MATCHING);
	}

	@Override
	public MOB findMOBMatchInWebCache(final MOB M)
	{
		if(M==null)
			return null;
		for (final MOB M2 : getMOBCache())
		{
			if(M.sameAs(M2))
				return M2;
		}
		return null;
	}

	@Override
	public Item findItemMatchInWebCache(final Item I)
	{
		if(I==null)
			return null;
		for (final Item I2 : getItemCache())
		{
			if(I.sameAs(I2))
				return I2;
		}
		return null;
	}

	@Override
	public Collection<MOB> contributeMOBsToWebCache(final Collection<MOB> inhabs)
	{
		for (final MOB M : inhabs)
		{
			if(M.isGeneric() || true)
			{
				if((findMOBMatchInWebCache(M)==null)
				&&((M.isSavable())
					||((M.location()!=null)&&(!M.location().isSavable()))))
				{
					final MOB M3=(MOB)M.copyOf();
					M3.setExpirationDate(System.currentTimeMillis());
					getMOBCache().add(M3);
					for(int i3=0;i3<M3.numItems();i3++)
					{
						final Item I3=M3.getItem(i3);
						if(I3!=null)
							I3.stopTicking();
					}
				}
			}
		}
		return getMOBCache();
	}

	@Override
	public boolean isAllNum(final String str)
	{
		if(str.length()==0)
			return false;
		for(int c=0;c<str.length();c++)
		{
			if((!Character.isDigit(str.charAt(c)))
			&&(str.charAt(c)!='-'))
				return false;
		}
		return true;
	}

	@Override
	public Collection<Item> contributeItemsToWebCache(final Collection<Item> items)
	{
		for (final Item I : items)
		{
			if(I.isGeneric())
			{
				boolean found=false;
				for (final Item I2 : getItemCache())
				{
					if(I.sameAs(I2))
					{
						found = true;
						break;
					}
				}
				if(!found)
				{
					final Item I2=(Item)I.copyOf();
					I.sameAs(I2);
					I2.setContainer(null);
					I2.wearAt(Wearable.IN_INVENTORY);
					I2.setExpirationDate(System.currentTimeMillis());
					getItemCache().add(I2);
					I2.stopTicking();
				}
			}
		}
		return getItemCache();
	}

	@Override
	public String getWebCacheSuffix(final Environmental E)
	{
		if((E.expirationDate() > (System.currentTimeMillis() - TimeManager.MILI_DAY))
		&&(E.expirationDate() < System.currentTimeMillis()))
		{
			final String time = CMLib.time().date2EllapsedTime(System.currentTimeMillis() - E.expirationDate(),TimeUnit.MINUTES, true);
			if(time.length()==0)
				return " (cached new)";
			else
				return " (cached "+time+")";
		}
		else
			return " ("+E.ID()+")";
	}
}
