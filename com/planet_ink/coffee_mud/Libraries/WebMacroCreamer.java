package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/*
   Copyright 2013-2018 Bo Zimmerman

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

	@Override
	public ByteBuffer convertOutput(CWConfig config, HTTPRequest request, File pageFile, HTTPStatus status, ByteBuffer buffer) throws HTTPException
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
			return ByteBuffer.wrap(virtualPageFilter(request, request.getRequestObjects(), systemStartTime, new String[] { "" }, new StringBuffer(new String(buffer.array()))).toString().getBytes());
		}
		catch (final HTTPRedirectException he)
		{
			throw new HTTPException(HTTPStatus.S307_TEMPORARY_REDIRECT, he.getMessage());
		}
	}

	@Override
	public byte[] virtualPageFilter(byte[] data) throws HTTPRedirectException
	{
		return virtualPageFilter(new StringBuffer(new String(data))).toString().getBytes();
	}

	@Override
	public String virtualPageFilter(String s) throws HTTPRedirectException
	{
		return virtualPageFilter(new StringBuffer(s)).toString();
	}

	@Override
	public StringBuffer virtualPageFilter(StringBuffer s) throws HTTPRedirectException
	{
		return virtualPageFilter(new HTTPRequest()
		{
			public final Hashtable<String, String>	params	= new Hashtable<String, String>();
			public final Hashtable<String, Object>	objects	= new Hashtable<String, Object>();

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
			public String getUrlParameter(String name)
			{
				return params.get(name.toLowerCase());
			}

			@Override
			public boolean isUrlParameter(String name)
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
			public String getHeader(String name)
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
			public String getCookie(String name)
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
			public double getSpecialEncodingAcceptability(String type)
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
			public void addFakeUrlParameter(String name, String value)
			{
				params.put(name.toLowerCase(), value);
			}

			@Override
			public void removeUrlParameter(String name)
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
		}, new Hashtable<String, Object>(), new long[] { System.currentTimeMillis() }, new String[] { "" }, s);
	}

	@Override
	public String clearWebMacros(String s)
	{
		if (s.length() == 0)
			return "";
		return clearWebMacros(new StringBuffer(s));
	}

	@Override
	public String parseFoundMacro(StringBuffer s, int i, boolean lookOnly)
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
	public String clearWebMacros(StringBuffer s)
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
					if ((foundMacro.startsWith("if?")) 
					|| (foundMacro.equalsIgnoreCase("else")) 
					|| (foundMacro.equalsIgnoreCase("loop")) 
					|| (foundMacro.equalsIgnoreCase("back"))
					|| (foundMacro.equalsIgnoreCase("endif")) 
					|| (foundMacro.equalsIgnoreCase("/jscript")) 
					|| (foundMacro.equalsIgnoreCase("jscript")))
						s.replace(i, i + foundMacro.length() + 2, foundMacro);
					else
					{
						final int x = foundMacro.indexOf('?');
						final int len = foundMacro.length();
						if (x >= 0)
							foundMacro = foundMacro.substring(0, x);
						if (foundMacro != null)
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
	public StringBuffer virtualPageFilter(HTTPRequest request, Map<String, Object> objects, long[] processStartTime, String[] lastFoundMacro, StringBuffer s) throws HTTPRedirectException
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
							final int l = foundMacro.length() + 2;
							final int v = myEndif(s, i + l, lastFoundMacro);
							if (v < 0)
							{
								if (debugMacros)
									Log.debugOut("ProcessHTTPRequest", "if without endif");
								s.replace(i, i + l, "[if without endif]");
							}
							else
							{
								final int v2 = myElse(s, i + l, v, lastFoundMacro);
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
										if (v2 >= 0)
											s.replace(v2, v + 7, "");
										else
											s.replace(v, v + 7, "");
										s.replace(i, i + l, "");
									}
									else
									{
										if (debugMacros)
											Log.debugOut("ProcessHTTPRequest", "Result IF macro: FALSE");
										if (v2 >= 0)
											s.replace(i, v2 + 6, "");
										else
											s.replace(i, v + 7, "");
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
							final int v = myEndJScript(s, i + l, lastFoundMacro);
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
						if (foundMacro.startsWith("block?") || foundMacro.startsWith("BLOCK?"))
						{
							final int l = foundMacro.length() + 2;
							final int v = myEndBlock(s, i + l, lastFoundMacro);
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
									try
									{
										request.addFakeUrlParameter(varName, qq);
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
			&& ((!Log.isMaskedErrMsg(errMsg)) || (CMSecurity.isDebugging(CMSecurity.DbgFlag.HTTPMACROS))))
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

	protected String runMacro(HTTPRequest request, String foundMacro, final String[] lastFoundMacro, final boolean isAdminServer) throws HTTPRedirectException, HTTPServerException
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

	protected String parseFoundMacro(StringBuffer s, int i, String[] lastFoundMacro, boolean lookOnly)
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

	protected int myElse(StringBuffer s, int i, int end, String[] lastFoundMacro)
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
					if ((foundMacro.equalsIgnoreCase("else")) 
					&& (endifsToFind == 1))
						return i;
				}
			}
		}
		return -1;
	}

	protected int myBack(StringBuffer s, int i, String[] lastFoundMacro)
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
				}
			}
		}
		return -1;
	}

	protected int myNext(StringBuffer s, int i, String[] lastFoundMacro)
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
				}
			}
		}
		return -1;
	}

	protected int myEndif(StringBuffer s, int i, String[] lastFoundMacro)
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

	private int myEndJScript(StringBuffer s, int i, String[] lastFoundMacro)
	{
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null) && (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase("/jscript"))
						return i;
				}
			}
		}
		return -1;
	}

	private int myEndBlock(StringBuffer s, int i, String[] lastFoundMacro)
	{
		for (; i < s.length(); i++)
		{
			if (s.charAt(i) == '@')
			{
				final String foundMacro = parseFoundMacro(s, i, lastFoundMacro, true);
				if ((foundMacro != null) && (foundMacro.length() > 0))
				{
					if (foundMacro.equalsIgnoreCase("/block"))
						return i;
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

		public void write(Object O)
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

		public JScriptablePage(HTTPRequest requests)
		{
			req = requests;
		}

		public static String[]	functions	= { "request", "write", "toJavaString" };

		public String toJavaString(Object O)
		{
			return Context.toString(O);
		}
	}

	@Override
	public void init()
	{
	}

	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response) throws HTTPException
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
	public void doPost(SimpleServletRequest request, SimpleServletResponse response) throws HTTPException
	{
		doGet(request, response);
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response) throws HTTPException
	{
	}

	@Override
	public boolean activate()
	{
		final String flag = Resources.getPropResource("WEBMACROCREAMER", "RUNYAHOOMSGGRABBER");
		if (flag.equalsIgnoreCase("TRUE"))
		{
			int tickCount = CMath.s_int(Resources.getPropResource("WEBMACROCREAMER", "YAHOOTICKSPERRUN"));
			if (tickCount < 2)
				tickCount = 2;
			name = "THCreamer" + Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient = CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT | Tickable.TICKID_SOLITARYMASK, tickCount);
		}
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

	protected YahooGroupSession	yahooSession	= null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
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
			if (yahooSession == null)
			{
				final String yahooUsername = Resources.getPropResource("WEBMACROCREAMER", "YAHOOUSER");
				if (yahooUsername.length() == 0)
				{
					Log.errOut("WebMacroCreamer var YAHOOUSER not properly set.");
					return true;
				}
				final String yahooPassword = Resources.getPropResource("WEBMACROCREAMER", "YAHOOPASSWORD");
				if (yahooUsername.length() == 0)
				{
					Log.errOut("WebMacroCreamer var YAHOOPASSWORD not properly set.");
					return true;
				}
				final String yahooUrl = Resources.getPropResource("WEBMACROCREAMER", "YAHOOURL");
				if (yahooUrl.length() == 0)
				{
					Log.errOut("WebMacroCreamer var YAHOOURL not properly set.");
					return true;
				}
				final String forumName = Resources.getPropResource("WEBMACROCREAMER", "FORUM");
				if (forumName.length() == 0)
				{
					Log.errOut("WebMacroCreamer var FORUM not properly set.");
					return true;
				}
				final String skipList = Resources.getPropResource("WEBMACROCREAMER", "SKIPLIST");
				final int[] skipInts = CMParms.toIntArray(CMParms.parseCommas(skipList, true));
				yahooSession = new YahooGroupSession();
				yahooSession.url = yahooUrl;
				yahooSession.user = yahooUsername;
				yahooSession.password = yahooPassword;
				yahooSession.numTotalTimes = -1;
				yahooSession.numTimes = 1;
				yahooSession.journal = forumName;
				yahooSession.skipList = skipInts;
				Arrays.sort(yahooSession.skipList);
			}
			if (yahooSession.numTotalTimes <= 0)
			{
				if (yahooSession.H != null)
				{
					yahooSession.H.finished();
					yahooSession.H = null;
				}
				yahooSession.H = (HttpClient) CMClass.getCommon("DefaultHttpClient");
				try
				{
					final String resp = this.loginToYahooSession(yahooSession);
					if (resp.length() > 0)
					{
						Log.warnOut("Yahoo Groups Copier failure reported: " + resp);
						yahooSession.H.finished();
						yahooSession.H = null;
						return true;
					}
					yahooSession.numTotalTimes = CMath.s_int(timesPerRunStr);
				}
				catch (final UnsupportedEncodingException e)
				{
					Log.errOut(Thread.currentThread().getName(), e);
					yahooSession.H.finished();
					yahooSession.H = null;
					return true;
				}
				finally
				{
				}
			}

			yahooSession.numTotalTimes--;
			yahooSession.numTimes = 1;
			final String resp = copyYahooGroupMsg(yahooSession);
			if ((resp != null) && (resp.toLowerCase().startsWith("fail")))
			{
				yahooSession.lastFailedNum = yahooSession.lastMsgNum;
				Log.warnOut("Yahoo Groups Copier failure reported: " + resp);
				yahooSession.lastMsgNum--;
				yahooSession.numTotalTimes = -1;
			}
			else 
			if (yahooSession.lastFailedNum == yahooSession.lastMsgNum)
				Log.warnOut("Yahoo Groups Copier recovered on message " + yahooSession.lastMsgNum);
			Resources.setPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER", Integer.toString(yahooSession.lastMsgNum));
		}
		finally
		{
			tickStatus = Tickable.STATUS_NOT;
			Resources.savePropResources();
		}
		return true;
	}

	// better message url:
	// http://groups.yahoo.com/api/v1/groups/coffeemud/messages/1/raw
	// all you need is a login token and its done.
	private static class YahooGroupSession
	{
		protected HttpClient	H				= null;
		protected int			numTimes		= -1;
		protected int			numTotalTimes	= -1;
		protected int			lastMsgNum		= -1;
		protected int			numTotal		= -1;
		protected int[]			skipList		= new int[0];
		protected String		url				= "";
		protected String		journal			= "";
		protected String		cookieSet		= "";
		protected String		user			= "";
		protected String		password		= "";
		protected int			lastFailedNum	= -1;
	}

	public String copyYahooGroupMsg(YahooGroupSession sess)
	{
		while ((--sess.numTimes) >= 0)
		{
			sess.lastMsgNum++;
			if (sess.lastMsgNum > sess.numTotal)
			{
				sess.lastMsgNum = sess.numTotal;
				return sess.lastMsgNum + "of " + sess.numTotal + " messages already processed";
			}
			if (Arrays.binarySearch(sess.skipList, sess.lastMsgNum) >= 0)
				continue;
			final byte[] b = sess.H.getRawUrl(sess.url + "/message/" + sess.lastMsgNum, sess.cookieSet);
			if (b == null)
				return "Failed: to read message from url:" + sess.url + "/message/" + sess.lastMsgNum;
			final String msgPage = new String(b);
			int startOfSubject = msgPage.indexOf("<em class=\"msg-bg msg-bd\"");
			if (startOfSubject < 0)
				startOfSubject = msgPage.indexOf("<em class=\"msg-newfont\"");
			if (startOfSubject < 0)
			{
				final int x = msgPage.indexOf("Message  does not exist in ");
				if ((x > 0) && (msgPage.substring(0, x).trim().endsWith("<div class=\"ygrp-contentblock\">")))
				{
					Resources.setPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER", Integer.toString(sess.lastMsgNum));
					continue;
				}
				return "Failed: to find subject start in url:" + sess.url + "/message/" + sess.lastMsgNum;
			}
			startOfSubject = msgPage.indexOf(">", startOfSubject);
			final int endOfSubject = msgPage.indexOf("</em>", startOfSubject);
			if (endOfSubject < 0)
				return "Failed: to find subject end in url:" + sess.url + "/message/" + sess.lastMsgNum;
			String subject = msgPage.substring(startOfSubject + 1, endOfSubject).trim();
			if ((subject.length() == 0) || (subject.length() > 200) || (subject.indexOf('<') >= 0) || (subject.indexOf('>') >= 0))
				return "Failed: to find VALID subject '" + subject + "' in url:" + sess.url + "/message/" + sess.lastMsgNum;
			int startOfDate = msgPage.indexOf("<span class=\"msg-newfont\" title=\"");
			int endOfDate;
			if (startOfDate > 0)
				startOfDate += 33;// MAGIC NUMBER
			else
			{
				startOfDate = msgPage.indexOf("<span class=\"msg-newfont\" title=\"");
				if (startOfDate < 0)
					return "Failed: to find date start in url:" + sess.url + "/message/" + sess.lastMsgNum;
				startOfDate += 33;// MAGIC NUMBER
			}
			endOfDate = msgPage.indexOf("\"", startOfDate + 1);
			if (endOfDate < 0)
				return "Failed: to find date end in url:" + sess.url + "/message/" + sess.lastMsgNum;
			final String dateStr = msgPage.substring(startOfDate, endOfDate).trim();
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d'T'HH:mm:ss'Z'");
			Date postDate;
			try
			{
				postDate = format.parse(dateStr);
			}
			catch (final ParseException p)
			{
				return "Failed: to parse date '" + dateStr + "' in url:" + sess.url + "/message/" + sess.lastMsgNum;
			}
			int startOfAuthor = msgPage.indexOf("<span class=\"name\">");
			if (startOfAuthor < 0)
				return "Failed: to find author start in url:" + sess.url + "/message/" + sess.lastMsgNum;
			startOfAuthor = msgPage.indexOf(">", startOfAuthor + 4);
			final int endOfAuthor = msgPage.indexOf("</span>", startOfAuthor);
			if (endOfAuthor < 0)
				return "Failed: to find author end in url:" + sess.url + "/message/" + sess.lastMsgNum;
			String author = msgPage.substring(startOfAuthor + 1, endOfAuthor).trim();
			author = CMStrings.replaceAll(author, "<wbr>", "").trim();
			if (author.indexOf("profiles.yahoo.com") > 0)
				author = author.substring(author.indexOf("\">") + 2, author.lastIndexOf("</a>"));
			if ((author.length() == 0) || (author.length() > 100))
				return "Failed: to find VALID author '" + author + "' in url:" + sess.url + "/message/" + sess.lastMsgNum;
			int startOfMsg = msgPage.indexOf("entry-content");
			if (startOfMsg < 0)
				return "Failed: to find message in url:" + sess.url + "/message/" + sess.lastMsgNum;
			startOfMsg = msgPage.indexOf(">", startOfMsg);
			int endOfMsg = msgPage.indexOf("<tr style=\"height:35px\">", startOfMsg);
			if (endOfMsg < 0)
				return "Failed: to find end of msg in url:" + sess.url + "/message/" + sess.lastMsgNum;
			endOfMsg = msgPage.lastIndexOf("</div>", endOfMsg);
			if (endOfMsg < 0)
				return "Failed: to find end2 of msg in url:" + sess.url + "/message/" + sess.lastMsgNum;
			String theMessage = msgPage.substring(startOfMsg + 1, endOfMsg).trim();
			while (theMessage.startsWith("<br>"))
				theMessage = theMessage.substring(4).trim();
			while (theMessage.endsWith("<br>"))
				theMessage = theMessage.substring(0, theMessage.length() - 4).trim();
			theMessage = CMStrings.replaceAll(theMessage, "\n", "");
			theMessage = CMStrings.replaceAll(theMessage, "\r", "");
			if (theMessage.trim().length() == 0)
				return "Failed: to find lengthy msg in url:" + sess.url + "/message/" + sess.lastMsgNum;
			final JournalsLibrary.ForumJournal forum = CMLib.journals().getForumJournal(sess.journal);
			if (forum == null)
				return "Failed: bad forum given";
			if (author.indexOf('@') >= 0)
			{
				final MOB aM = CMLib.players().getLoadPlayerByEmail(author);
				if (aM != null)
					author = aM.Name();
				else 
				if (CMProps.isUsingAccountSystem())
				{
					final PlayerAccount A = CMLib.players().getLoadAccountByEmail(author);
					if (A == null)
						author = author.substring(0, author.indexOf('@'));
					else
						author = A.getAccountName();
				}
				else
					author = author.substring(0, author.indexOf('@'));
			}
			else 
			if (CMLib.login().isOkName(author, false))
				author = "_" + author;

			String parent = "";
			if (subject.toLowerCase().startsWith("[coffeemud]"))
				subject = subject.substring(11).trim();
			if (subject.toUpperCase().startsWith("RE:"))
			{
				String subj = subject;
				while (subj.toUpperCase().startsWith("RE:") || subj.toLowerCase().startsWith("[coffeemud]"))
				{
					if (subj.toUpperCase().startsWith("RE:"))
						subj = subj.substring(3).trim();
					if (subj.toLowerCase().startsWith("[coffeemud]"))
						subj = subj.substring(11).trim();
				}

				final List<JournalEntry> journalEntries = CMLib.database().DBSearchAllJournalEntries(forum.NAME(), subj);
				if ((journalEntries != null) && (journalEntries.size() > 0))
				{
					JournalEntry WIN = null;
					for (final JournalEntry J : journalEntries)
					{
						if (J.subj().trim().equals(subj))
							WIN = J;
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().trim().equalsIgnoreCase(subj))
								WIN = J;
						}
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().trim().indexOf(subj) >= 0)
								WIN = J;
						}
					}
					if (WIN == null)
					{
						for (final JournalEntry J : journalEntries)
						{
							if (J.subj().toLowerCase().trim().indexOf(subj.toLowerCase()) >= 0)
								WIN = J;
						}
					}

					if (WIN != null)
						parent = WIN.key();
				}
				if (parent.length() == 0)
					subject = subj;
			}
			final JournalEntry msg = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
			msg.from (author);
			msg.subj (CMLib.webMacroFilter().clearWebMacros(subject));
			msg.msg (CMLib.webMacroFilter().clearWebMacros(theMessage));
			msg.date (postDate.getTime());
			msg.update (postDate.getTime());
			msg.parent (parent);
			msg.msgIcon ("");
			msg.data ("");
			msg.to ("ALL");
			// check for dups
			final List<JournalEntry> chckEntries = CMLib.database().DBReadJournalMsgsNewerThan(forum.NAME(), "ALL", msg.date() - 1);
			for (final JournalEntry entry : chckEntries)
			{
				if ((entry.date() == msg.date()) 
				&& (entry.from().equals(msg.from())) 
				&& (entry.subj().equals(msg.subj())) 
				&& (entry.parent().equals(msg.parent())))
				{
					Resources.setPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER", Integer.toString(sess.lastMsgNum));
					return "Msg#" + sess.lastMsgNum + " was a dup!";
				}
			}
			CMLib.database().DBWriteJournal(forum.NAME(), msg);
			if (parent.length() > 0)
				CMLib.database().DBTouchJournalMessage(parent, msg.date());
			CMLib.journals().clearJournalSummaryStats(forum);
			Resources.setPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER", Integer.toString(sess.lastMsgNum));
		}
		return "Post " + sess.lastMsgNum + " submitted.";
	}

	protected String loginToYahooSession(YahooGroupSession sess) throws UnsupportedEncodingException
	{
		final String loginUrl = "http://login.yahoo.com?login=" + URLEncoder.encode(sess.user, "UTF8") + "&passwd=" + URLEncoder.encode(sess.password, "UTF8");
		final Map<String, List<String>> M = sess.H.getHeaders(loginUrl);
		if (M == null)
			return "Fail: Http error hitting " + loginUrl;
		final StringBuilder cookieSet = new StringBuilder("");
		List<String> cookies = M.get("Set-Cookie");
		if (cookies == null)
			return "Fail: Http get cookies from " + loginUrl;
		for (final String val : cookies)
		{
			if (cookieSet.length() > 0)
				cookieSet.append(" ; ");
			final int x = val.indexOf(';');
			cookieSet.append((x >= 0) ? val.substring(0, x).trim() : val.trim());
		}
		final byte[] b = sess.H.getRawUrl(sess.url + "/messages", cookieSet.toString());
		if (b == null)
			return "Failed: to read page: " + sess.url + "/messages";
		cookies = M.get("Set-Cookie");
		if (cookies != null)
		{
			for (final String val : cookies)
			{
				if (cookieSet.length() > 0)
					cookieSet.append(" ; ");
				final int x = val.indexOf(';');
				cookieSet.append((x >= 0) ? val.substring(0, x).trim() : val.trim());
			}
		}
		sess.cookieSet = cookieSet.toString();
		final StringBuilder s = new StringBuilder(new String(b));
		CMStrings.convertHtmlToText(s);
		final String txt = s.toString();
		int x = txt.indexOf(" of ");
		int num = -1;
		while ((num < 0) && (x >= 0))
		{
			if (Character.isDigit(txt.charAt(x + 4)))
			{
				int y = 4;
				while (Character.isDigit(txt.charAt(x + y)))
					y++;
				num = CMath.s_int(txt.substring(x + 4, x + y));
			}
			else
				x = txt.indexOf(" of ", x + 1);
		}
		if (num < 0)
			return "Failed: No numbers found in " + sess.url + "/messages";
		sess.numTotal = num;
		if (Resources.isPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER"))
			sess.lastMsgNum = CMath.s_int(Resources.getPropResource("WEBMACROCREAMER", "LASTYAHOOMSGNUMBER"));
		return "";
	}

	@Override
	public String copyYahooGroupMsgs(String user, String password, String url, int numTimes, int[] skipList, String journal)
	{
		final YahooGroupSession sess = new YahooGroupSession();
		sess.url = url;
		sess.user = user;
		sess.password = password;
		sess.numTimes = numTimes;
		sess.skipList = skipList;
		sess.journal = journal;
		sess.H = (HttpClient) CMClass.getCommon("DefaultHttpClient");
		Arrays.sort(sess.skipList);

		try
		{
			final String resp = this.loginToYahooSession(sess);
			if (resp.length() > 0)
				return resp;
			return copyYahooGroupMsg(sess);
		}
		catch (final UnsupportedEncodingException e)
		{
			Log.errOut(Thread.currentThread().getName(), e);
		}
		finally
		{
			sess.H.finished();
		}
		return " @break@";
	}
}
