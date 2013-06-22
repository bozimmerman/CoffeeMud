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
import com.planet_ink.miniweb.http.HTTPException;
import com.planet_ink.miniweb.http.HTTPMethod;
import com.planet_ink.miniweb.http.HTTPStatus;
import com.planet_ink.miniweb.http.MultiPartData;
import com.planet_ink.miniweb.interfaces.HTTPRequest;
import com.planet_ink.miniweb.interfaces.SimpleServlet;
import com.planet_ink.miniweb.interfaces.SimpleServletRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletResponse;
import com.planet_ink.miniweb.server.MiniWebServer;
import com.planet_ink.miniweb.util.MWThread;
import com.planet_ink.miniweb.util.MiniWebConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/*
   Copyright 2000-2013 Bo Zimmerman

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
	public String ID(){return "WebMacroCreamer";}
	
	@Override
	public ByteBuffer convertOutput(MiniWebConfig config, HTTPRequest request, HTTPStatus status, ByteBuffer buffer) throws HTTPException {
		if(request.getRequestObjects().get("SYSTEM_HTTP_STATUS")==null)
		{
			request.getRequestObjects().put("SYSTEM_HTTP_STATUS", Integer.toString(status.getStatusCode()));
			request.getRequestObjects().put("SYSTEM_HTTP_STATUS_INFO", status.description());
		}
		long[] systemStartTime=new long[]{System.currentTimeMillis()};
		try
		{
			return ByteBuffer.wrap(virtualPageFilter(request, request.getRequestObjects(), systemStartTime,  new String[]{""}, new StringBuffer(new String(buffer.array()))).toString().getBytes());
		}
		catch(HTTPRedirectException he)
		{
			throw new HTTPException(HTTPStatus.S307_TEMPORARY_REDIRECT,he.getMessage());
		}
	}

	@Override
	public byte[] virtualPageFilter(byte[] data) throws HTTPRedirectException {
		return virtualPageFilter(new StringBuffer(new String(data))).toString().getBytes();
	}

	@Override
	public String virtualPageFilter(String s) throws HTTPRedirectException {
		return virtualPageFilter(new StringBuffer(s)).toString();
	}

	@Override
	public StringBuffer virtualPageFilter(StringBuffer s) throws HTTPRedirectException
	{
		return virtualPageFilter(new HTTPRequest(){
			public final Hashtable<String,String> params=new Hashtable<String,String>();
			public final Hashtable<String,Object> objects=new Hashtable<String,Object>();
			@Override public String getHost() { return "localhost"; }
			@Override public String getUrlPath() { return "localhost/file"; }
			@Override public String getFullRequest() { return "GET "+getUrlPath(); }
			@Override public String getUrlParameter(String name) { return params.get(name.toLowerCase()); }
			@Override public boolean isUrlParameter(String name) { return params.containsKey(name.toLowerCase()); }
			@Override public Map<String,String> getUrlParametersCopy() { return new XHashtable<String,String>(params); }
			@Override public Set<String> getUrlParameters() { return params.keySet(); }
			@Override public HTTPMethod getMethod() { return HTTPMethod.GET; }
			@Override public String getHeader(String name) { return null; }
			@Override public InetAddress getClientAddress() { try { return InetAddress.getLocalHost(); } catch (UnknownHostException e) { return null; } }
			@Override public int getClientPort() { return 0; }
			@Override public InputStream getBody() { return null; }
			@Override public String getCookie(String name) { return null; }
			@Override public Set<String> getCookieNames() { return objects.keySet(); }
			@Override public List<MultiPartData> getMultiParts() { return new XVector<MultiPartData>(); }
			@Override public double getSpecialEncodingAcceptability(String type) { return 0; }
			@Override public String getFullHost() { return "localhost"; }
			@Override public List<int[]> getRangeAZ() { return null; }
			@Override public void addFakeUrlParameter(String name, String value) { params.put(name.toUpperCase(), value); }
			@Override public void removeUrlParameter(String name) { params.remove(name.toUpperCase()); }
			@Override public Map<String,Object> getRequestObjects() { return objects; }
			@Override public float getHttpVer() { return (float)1.1; }
		},new Hashtable<String,Object>(), new long[]{System.currentTimeMillis()}, new String[]{""}, s);
	}
	
	// OK - this parser is getting a bit ugly now;
	//  I'm probably gonna replace it soon
	public StringBuffer virtualPageFilter(HTTPRequest request, Map<String, Object> objects, long[] processStartTime, String[] lastFoundMacro, StringBuffer s) throws HTTPRedirectException
	{
		String redirectTo = null;
		boolean debugMacros=CMSecurity.isDebugging(CMSecurity.DbgFlag.HTTPMACROS);
		boolean isAdminServer=false;
		if(Thread.currentThread() instanceof MWThread)
			isAdminServer=CMath.s_bool(((MWThread)Thread.currentThread()).getConfig().getMiscProp("ADMIN"));
		if((!isAdminServer)
		&&(processStartTime[0]>0)
		&&(System.currentTimeMillis()-processStartTime[0])>(120*1000))
		{
			if(debugMacros) Log.infoOut(Thread.currentThread().getName(),"Encountered TIMEOUT!");
			return new StringBuffer("");
		}
		try
		{
			for(int i=0;i<s.length();i++)
			{
				if(s.charAt(i)=='@')
				{
					String foundMacro=parseFoundMacro(s,i,lastFoundMacro,false);
					if((foundMacro!=null)&&(foundMacro.length()>0))
					{
						if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Found macro: "+foundMacro);
						if(foundMacro.startsWith("if?")||foundMacro.startsWith("IF?"))
						{
							int l=foundMacro.length()+2;
							int v=myEndif(s,i+l,lastFoundMacro);
							if(v<0)
							{
								if(debugMacros) Log.debugOut("ProcessHTTPRequest", "if without endif");
								s.replace(i,i+l,"[if without endif]");
							}
							else
							{
								int v2=myElse(s,i+l,v,lastFoundMacro);
								foundMacro=foundMacro.substring(3);
								try
								{
									String compare="true";
									if(foundMacro.startsWith("!"))
									{
										foundMacro=foundMacro.substring(1);
										compare="false";
									}
									if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Found IF macro: "+foundMacro);
									String q=runMacro(request,foundMacro,lastFoundMacro,isAdminServer);
									if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Ran IF macro: "+foundMacro+"="+q);
									if((q!=null)&&(q.equalsIgnoreCase(compare)))
									{
										if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Result IF macro: TRUE");
										if(v2>=0)
											s.replace(v2,v+7,"");
										else
											s.replace(v,v+7,"");
										s.replace(i,i+l,"");
									}
									else
									{
										if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Result IF macro: FALSE");
										if(v2>=0)
											s.replace(i,v2+6,"");
										else
											s.replace(i,v+7,"");
									}
								}
								catch (HTTPRedirectException e)
								{
									if(debugMacros) Log.debugOut("ProcessHTTPRequest", "if exception: "+e.getMessage());
									redirectTo = e.getMessage();
								}
							}
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("/jscript"))
						{
							int l=foundMacro.length()+2;
							s.replace(i,i+l,"[/jscript without jscript]");
						}
						else
						if(foundMacro.equalsIgnoreCase("/html"))
						{
							int l=foundMacro.length()+2;
							s.replace(i,i+l,"[/html without html]");
						}
						else
						if(foundMacro.equalsIgnoreCase("jscript"))
						{
							int l=foundMacro.length()+2;
							int v=myEndJScript(s,i+l,lastFoundMacro);
							if(v<0)
								s.replace(i,i+l,"[jscript without /jscript]");
							else
							{
								Context cx = Context.enter();
								try
								{
									String script=s.substring(i+l,v);
									JScriptablePage scope = new JScriptablePage(request);
									cx.initStandardObjects(scope);
									scope.defineFunctionProperties(JScriptablePage.functions,
																   JScriptablePage.class,
																   ScriptableObject.DONTENUM);
									cx.evaluateString(scope, script,"<cmd>", 1, null);
									s.replace(i,v+l+1,scope.getBuffer());
									i=i+scope.getBuffer().length();
								}
								catch(Exception e)
								{
									s.replace(i,v+l+1,"[jscript error: "+e.getMessage()+"]");
								}
								Context.exit();
							}
							continue;
						}
						else
						if(foundMacro.startsWith("block?")||foundMacro.startsWith("BLOCK?"))
						{
							int l=foundMacro.length()+2;
							int v=myEndBlock(s,i+l,lastFoundMacro);
							if(v<0)
								s.replace(i,i+l,"[block without /block]");
							else
							{
								String name=foundMacro.substring(6).trim().toUpperCase();
								objects.put(name,s.substring(i+l,v));
								s.delete(i,v+8);
							}
							continue;
						}
						else
						if(foundMacro.startsWith("insert?")||foundMacro.startsWith("INSERT?"))
						{
							int l=foundMacro.length()+2;
							String name=foundMacro.substring(7).trim().toUpperCase();
							Object o=objects.get(name);
							if(o!=null) 
								s.replace(i,i+l,o.toString());
							else
								s.delete(i, i+l);
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("endif"))
						{
							s.delete(i,i+7);
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("else"))
						{
							s.delete(i,i+6);
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("loop"))
						{
							int v=myBack(s,i+6,lastFoundMacro);
							if(v<0)
								s.replace(i,i+6, "[loop without back]" );
							else
							{
								String s2=s.substring(i+6,v);
								s.replace(i,v+6,"");
								int ldex=i;
								String s3=" ";
								while(s3.length()>0)
								{
									try
									{
										s3=new String(virtualPageFilter( request, objects, processStartTime, lastFoundMacro, new StringBuffer(s2)));
									}
									catch (HTTPRedirectException e)
									{
										s3 = " ";
										redirectTo = e.getMessage();
									}

									s.insert(ldex,s3);
									ldex+=s3.length();
								}
								if((!isAdminServer)
								&&(processStartTime[0]>0)
								&&(System.currentTimeMillis()-processStartTime[0])>(120*1000))
								{
									if(debugMacros) Log.infoOut(Thread.currentThread().getName(),"Encountered TIMEOUT!");
									return new StringBuffer("");
								}
							}
							continue;
						}
						else
						if(foundMacro.startsWith("for?")||foundMacro.startsWith("FOR?"))
						{
							String forCond=foundMacro.substring(4);
							int fc=forCond.indexOf('=');
							int v=myNext(s,i+foundMacro.length()+2,lastFoundMacro);
							if(fc<0)
								s.replace(i,i+foundMacro.length()+2, "[for without variabledef=]" );
							else
							if(v<0)
								s.replace(i,i+foundMacro.length()+2, "[for without next]" );
							else
							{
								String s2=s.substring(i+foundMacro.length()+2,v);
								s.replace(i,v+6,"");
								if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Found FOR macro: "+foundMacro);
								String varName=forCond.substring(0,fc).trim().toUpperCase();
								String q=runMacro(request,forCond.substring(fc+1).trim(),lastFoundMacro,isAdminServer);
								if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Ran FOR macro: "+foundMacro+"="+q);
								int ldex=i;
								String s3=" ";
								List<String> set;
								if((q==null)||(q.trim().length()==0)||(q.toLowerCase().indexOf("@break@")>=0))
									set=new XVector<String>();
								else
									set=CMParms.parseCommas(q, false);
								for(String qq : set)
								{
									try
									{
										request.addFakeUrlParameter(varName, qq);
										s3=new String(virtualPageFilter( request, objects, processStartTime, lastFoundMacro, new StringBuffer(s2)));
									}
									catch (HTTPRedirectException e)
									{
										s3 = " ";
										redirectTo = e.getMessage();
									}

									s.insert(ldex,s3);
									ldex+=s3.length();
								}
								if((!isAdminServer)
								&&(processStartTime[0]>0)
								&&(System.currentTimeMillis()-processStartTime[0])>(120*1000))
								{
									if(debugMacros) Log.infoOut(Thread.currentThread().getName(),"Encountered TIMEOUT!");
									return new StringBuffer("");
								}
							}
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("break"))
						{
							if(debugMacros) Log.infoOut(Thread.currentThread().getName(),"Encountered BREAK! at "+i);
							return new StringBuffer("");
						}
						else
						if(foundMacro.equalsIgnoreCase("back"))
						{
							s.replace(i,i+6, "[back without loop]" );
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("if"))
						{
							s.replace(i,i+6, "[naked if condition]" );
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("block"))
						{
							s.replace(i,i+6, "[unnamed block]" );
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("insert"))
						{
							s.replace(i,i+6, "[naked block insert]" );
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("for"))
						{
							s.replace(i,i+6, "[naked for iterator]" );
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("next"))
						{
							s.replace(i,i+6, "[next without for]" );
							continue;
						}

						if(foundMacro.length()>0)
						{
							try
							{
								int l=foundMacro.length();
								String q=runMacro(request, foundMacro,lastFoundMacro, isAdminServer);
								if(debugMacros) Log.debugOut("ProcessHTTPRequest", "Ran Macro: "+foundMacro+"="+q);
								if (q != null)
								{
									if((debugMacros)&&(q.toUpperCase().indexOf("@BREAK@")>=0))
										Log.infoOut(Thread.currentThread().getName(),"WebMacro:"+foundMacro+" generated a BREAK! at "+i);
									s.replace(i,i+l+2, q );
								}
								else
									s.replace(i,i+l+2, "[error]" );
							}
							catch (HTTPRedirectException e)
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
		catch (Exception e)
		{
			String errMsg=e.getMessage()==null?e.toString():e.getMessage();
			if((errMsg!=null)
			&&((!Log.isMaskedErrMsg(errMsg))||(CMSecurity.isDebugging(CMSecurity.DbgFlag.HTTPMACROS))))
			{
				Log.errOut(Thread.currentThread().getName(), "Exception in virtualPageFilter() - " + errMsg );
				Log.errOut(Thread.currentThread().getName(),e);
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
		int x=foundMacro.indexOf('?');
		StringBuffer parms=null;
		if(x>=0)
		{
			parms=new StringBuffer(foundMacro.substring(x+1));
			foundMacro=foundMacro.substring(0,x);
			int y=parms.indexOf("@");
			while(y>=0)
			{
				String newFoundMacro=parseFoundMacro(parms,y,lastFoundMacro,false);
				if((newFoundMacro!=null)&&(newFoundMacro.length()>0))
				{
					int l=newFoundMacro.length();
					String qq=runMacro(request, newFoundMacro, lastFoundMacro, isAdminServer);
					if (qq != null)
						parms.replace(y,y+l+2, qq );
					else
						parms.replace(y,y+l+2, "[error]" );
				}
				else
					break;
				y=parms.indexOf("@");
			}
		}
		if(foundMacro.length()==0)
			return "";
		WebMacro W=CMClass.getWebMacro(foundMacro.toUpperCase());
		if(W!=null)
		{
			String q=null;
			if (!isAdminServer && W.isAdminMacro())
			{
				Log.errOut(Thread.currentThread().getName(), "Non-admin cannot access admin macro '" + W.name() + "'; client IP: " + request.getClientAddress());
				q = "[error]";
			}
			else
			if(W.preferBinary())
			{
				byte[] bin=W.runBinaryMacro(request,(parms==null)?null:parms.toString());
				if(bin==null)
					q=" @break@";
				else
					q=new String(bin);
			}
			else
				q=W.runMacro(request,(parms==null)?null:parms.toString());
			if (q != null)
				return q;
			return "[error]";
		}
		return null;
	}
	
	protected String parseFoundMacro(StringBuffer s, int i, String[] lastFoundMacro, boolean lookOnly)
	{
		String foundMacro=null;
		boolean extend=false;
		if((i<s.length()-2)
		&&((s.charAt(i+1)=='X')||(s.charAt(i+1)=='x'))
		&&(Character.isDigit(s.charAt(i+2))||(s.charAt(i+1)=='x')||(s.charAt(i+1)=='X')))
			return null;
		for(int x=i+1;x<s.length();x++)
		{
			if((s.charAt(x)=='@')
			&&(extend)
			&&(x<(s.length()-1))
			&&(s.charAt(x+1)=='@'))
			{
				if(!lookOnly)
					s.deleteCharAt(x);
				while((x<s.length())&&(s.charAt(x)=='@'))
					x++;
				x--;
			}
			else
			if((s.charAt(x)=='@')
			&&((!extend)||(x>=s.length()-1)||(s.charAt(x+1)!='@')))
			{
				foundMacro=s.substring(i+1,x);
				break;
			}
			else
			if((s.charAt(x)=='?')&&(Character.isLetterOrDigit(s.charAt(x-1))))
				extend=true;
			else
			if(((x-i)>CMClass.longestWebMacro)&&(!extend))
				break;
		}
		lastFoundMacro[0]=foundMacro;
		return foundMacro;
	}

	protected int myElse(StringBuffer s, int i, int end, String[] lastFoundMacro)
	{
		int endifsToFind=1;
		for(;i<end;i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.startsWith("if?"))
					   endifsToFind++;
					else
					if(foundMacro.equalsIgnoreCase("endif"))
					{
						endifsToFind--;
						if(endifsToFind<=0)
							return -1;
					}
					else
					if((foundMacro.equalsIgnoreCase("else"))&&(endifsToFind==1))
					   return i;
				}
			}
		}
		return -1;
	}

	protected int myBack(StringBuffer s, int i, String[] lastFoundMacro)
	{
		int backsToFind=1;
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.equalsIgnoreCase("loop"))
					   backsToFind++;
					else
					if(foundMacro.equalsIgnoreCase("back"))
					{
						backsToFind--;
						if(backsToFind<=0)
							return i;
					}
				}
			}
		}
		return -1;
	}

	protected int myNext(StringBuffer s, int i, String[] lastFoundMacro)
	{
		int nextsToFind=1;
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.equalsIgnoreCase("for"))
					   nextsToFind++;
					else
					if(foundMacro.equalsIgnoreCase("next"))
					{
						nextsToFind--;
						if(nextsToFind<=0)
							return i;
					}
				}
			}
		}
		return -1;
	}

	protected int myEndif(StringBuffer s, int i, String[] lastFoundMacro)
	{
		int endifsToFind=1;
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.startsWith("if?"))
					   endifsToFind++;
					else
					if(foundMacro.equalsIgnoreCase("endif"))
					{
						endifsToFind--;
						if(endifsToFind<=0)
							return i;
					}
				}
			}
		}
		return -1;
	}

	private int myEndJScript(StringBuffer s, int i, String[] lastFoundMacro)
	{
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.equalsIgnoreCase("/jscript"))
						return i;
				}
			}
		}
		return -1;
	}

	private int myEndBlock(StringBuffer s, int i, String[] lastFoundMacro)
	{
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i,lastFoundMacro,true);
				if((foundMacro!=null)&&(foundMacro.length()>0))
				{
					if(foundMacro.equalsIgnoreCase("/block"))
						return i;
				}
			}
		}
		return -1;
	}

	protected static class JScriptablePage extends ScriptableObject
	{
		public String getClassName(){ return "JScriptablePage";}
		static final long serialVersionUID=43;
		StringBuffer buf=new StringBuffer("");
		public void write(Object O){buf.append( Context.toString(O));}
		public String getBuffer(){return buf.toString();}
		HTTPRequest req=null;
		public HTTPRequest request(){return req;}
		public JScriptablePage(HTTPRequest requests){req=requests;}
		public static String[] functions = { "request", "write", "toJavaString"};
		public String toJavaString(Object O){return Context.toString(O);}
	}

	@Override
	public void init() {}

	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response) 
	{
		String[] url=request.getUrlPath().split("/");
		if(url.length>0)
		{
			String macroName=url[url.length-1];
			int x=macroName.indexOf('?');
			if(x>0)
				macroName=macroName.substring(0,x);
			WebMacro W=CMClass.getWebMacro(macroName.toUpperCase());
			if(W==null)
			{
				Log.errOut("WebMacroCreamer","No web macro: '"+macroName+"'");
			}
			else
			if(!W.isAWebPath())
			{
				Log.errOut("WebMacroCreamer","Macro "+macroName+" is not a web path.");
			}
			else
			{
				String filename=W.getFilename(request, "");
				try
				{
					try
					{
						W.setServletResponse(response, filename);
						byte[] responseData;
						if(W.preferBinary())
							responseData=W.runBinaryMacro(request, "");
						else
							responseData=W.runMacro(request, "").getBytes();
						response.getOutputStream().write(responseData);
					} 
					catch (HTTPServerException e){
						try
						{
							response.getOutputStream().write(HTTPException.standardException(HTTPStatus.S500_INTERNAL_ERROR).generateOutput(request).flushToBuffer().array());
						} catch (HTTPException e1)	{ }
					} 
				}
				catch (IOException e2)
				{} 
			}
		}
	}

	@Override
	public void doPost(SimpleServletRequest request, SimpleServletResponse response) { 
		doGet(request,response);
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response) {
	}

}
