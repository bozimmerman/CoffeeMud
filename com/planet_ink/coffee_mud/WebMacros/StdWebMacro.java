package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWConfig;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.Color;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class StdWebMacro implements WebMacro
{
	@Override
	public String ID()
	{
		return name();
	}

	@Override
	public String name()
	{
		return "UNKNOWN";
	}

	@Override
	public boolean isAWebPath()
	{
		return false;
	}

	@Override
	public boolean preferBinary()
	{
		return false;
	}

	@Override
	public boolean isAdminMacro()
	{
		return false;
	}

	@Override
	public CMObject newInstance()
	{
		return this;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		return this;
	}

	@Override
	public byte[] runBinaryMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		return runMacro(httpReq,parm, null).getBytes();
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		return "[Unimplemented macro!]";
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	protected static StringBuffer colorwebifyOnly(final StringBuffer s)
	{
		if(s==null)
			return null;
		int i=0;
		final String[] lookup=CMLib.color().standardHTMLlookups();
		boolean priorFont = false;
		while(i<s.length())
		{
			if(s.charAt(i)=='^')
			{
				if(i<(s.length()-1))
				{
					final char c=s.charAt(i+1);
					//TODO: handle ~ and # here?
					final String code=lookup[c];
					if(code!=null)
					{
						s.delete(i,i+2);
						if(code.startsWith("<FONT"))
						{
							if(priorFont)
								s.insert(i,"</FONT>"+code+">");
							else
								s.insert(i,code+">");
							priorFont=true;
							i+=code.length();
						}
						else
						if(code.startsWith("<"))
						{
							s.insert(i,code+">");
							i+=code.length();
						}
						else
						{
							s.insert(i,code);
							i+=code.length()-1;
						}
					}
					else
					if(c=='?')
					{
						s.delete(i,i+2);
						s.insert(i,"</FONT>");
						i+=7;
					}
				}
			}
			i++;
		}
		if(priorFont)
			s.append("</FONT>");
		return s;
	}

	protected static String webify(final String s)
	{
		return webify(new StringBuffer(s)).toString();
	}

	protected static StringBuffer webify(StringBuffer s)
	{
		if(s==null)
			return null;
		int i=0;
		while(i<s.length())
		{
			switch(s.charAt(i))
			{
			case '\n':
			case '\r':
				if((i<s.length()-1)
				&&(s.charAt(i+1)!=s.charAt(i))
				&&((s.charAt(i+1)=='\r')||(s.charAt(i+1)=='\n')))
				{
					s.delete(i,i+2);
					s.insert(i,"<BR>");
					i+=3;
				}
				else
				{
					s.delete(i,i+1);
					s.insert(i,"<BR>");
					i+=3;
				}
				break;
			case ' ':
				s.setCharAt(i,'&');
				s.insert(i+1,"nbsp;");
				i+=5;
				break;
			case '>':
				s.setCharAt(i,'&');
				s.insert(i+1,"gt;");
				i+=3;
				break;
			case '<':
				s.setCharAt(i,'&');
				s.insert(i+1,"lt;");
				i+=3;
				break;
			}
			i++;
		}
		s=colorwebifyOnly(s);
		return s;
	}

	protected String clearWebMacros(final String s)
	{
		return CMLib.webMacroFilter().clearWebMacros(s);
	}

	protected String clearWebMacros(final StringBuffer s)
	{
		return CMLib.webMacroFilter().clearWebMacros(s);
	}

	protected String helpHelp(final String s)
	{
		return helpHelp(s, 70);
	}

	protected String helpHelp(final String s1, final int limit)
	{
		if(s1!=null)
		{
			final String[] lookup=CMLib.color().standardHTMLlookups();
			final StringBuilder s=new StringBuilder(s1.toString());
			int x=0;
			int count=0;
			x=0;
			int lastSpace=0;
			boolean priorFont = false;
			while((x>=0)&&(x<s.length()))
			{
				count++;
				switch(s.charAt(x))
				{
				case '\\':
					if(x<s.length()-1)
					{
						if(s.charAt(x+1)=='n')
						{
							s.delete(x, x+2);
							s.insert(x, " <BR>");
							lastSpace=x;
						}
					}
					break;
				case '\n':
					if(x<s.length()-1)
					{
						if(s.charAt(x+1)=='\r')
						{
							s.delete(x, x+2);
							s.insert(x, " <BR>");
						}
						else
						{
							s.delete(x, x+1);
							s.insert(x, " <BR>");
						}
						lastSpace=x;
					}
					break;
				case '\r':
					if(x<s.length()-1)
					{
						if(s.charAt(x+1)=='\n')
						{
							s.delete(x, x+2);
							s.insert(x, " <BR>");
						}
						else
						{
							s.delete(x, x+1);
							s.insert(x, " <BR>");
						}
						lastSpace=x;
					}
					break;
				case ' ':
					lastSpace=x;
					break;
				case '<':
					if((x<=s.length()-4)
					&&(s.substring(x,x+4).equalsIgnoreCase("<BR>")))
					{
						count=0;
						x=x+3;
						lastSpace=x+4;
					}
					else
					{
						s.setCharAt(x,'&');
						s.insert(x+1,"lt;");
						x+=3;
					}
					break;
				case '-':
					if((x>4)
					&&(s.charAt(x-1)=='-')
					&&(s.charAt(x-2)=='-')
					&&(s.charAt(x-3)=='-'))
					{
						count=0;
						lastSpace=x;
					}
					break;
				case '!':
					if((x>4)
					&&(s.charAt(x-1)==' ')
					&&(s.charAt(x-2)==' ')
					&&(s.charAt(x-3)==' '))
					{
						count=0;
						lastSpace=x;
					}
					else
					if((x<s.length()-10) // remove music
					&&(s.charAt(x+1)=='!')
					&&((s.substring(x+2,x+7).equalsIgnoreCase("sound"))
					   ||(s.substring(x+2,x+7).equalsIgnoreCase("music"))))
					{
						final int x1=s.indexOf("(",x+7);
						final int y1=s.indexOf(")",x+7);
						if((x1>=0)&&(y1>=x1))
						{
							s.delete(x,y1+1);
							x--;
						}
					}
					break;
				case '^':
					if(x<(s.length()-1))
					{
						char c=s.charAt(x+1);
						if(c=='?')
							c='w';
						final String code;
						if((c==ColorLibrary.COLORCODE_BACKGROUND)
						&&(x<(s.length()-2)))
						{
							c=s.charAt(x+2);
							s.delete(x, x+1);
							code=CMLib.color().getBackgroundHtmlTag(c);
						}
						else
						if(((c==ColorLibrary.COLORCODE_FANSI256)||(c==ColorLibrary.COLORCODE_BANSI256))
						&&(x<(s.length()-4)))
						{
							final StringBuilder finalHex = new StringBuilder("");
							int num=s.charAt(x+2)-'0';
							if((num>=0)&&(num<=5))
							{
								num = 256+(int)Math.round(CMath.div(num, 5) * 256.0);
								finalHex.append(Integer.toHexString(num).toUpperCase().substring(1));
								num=s.charAt(x+3)-'0';
								if((num>=0)&&(num<=5))
								{
									num = 256+(int)Math.round(CMath.div(num, 5) * 256.0);
									finalHex.append(Integer.toHexString(num).toUpperCase().substring(1));
									num=s.charAt(x+4)-'0';
									if((num>=0)&&(num<=5))
									{
										num = 256+(int)Math.round(CMath.div(num, 5) * 256.0);
										finalHex.append(Integer.toHexString(num).toUpperCase().substring(1));
										s.delete(x,x+3);
									}
								}
							}
							if(finalHex.length()==6)
							{
								final boolean isFg = (c==ColorLibrary.COLORCODE_FANSI256);
								if(isFg)
									code = "<FONT COLOR=\"#" +finalHex.toString()+"\"";
								else
									code = "<FONT STYLE=\"background-color: #" +finalHex.toString()+";\"";
							}
							else
								code=null;
						}
						else
							code=lookup[c];
						if(code!=null)
						{
							s.delete(x,x+2);
							if(code.startsWith("<FONT"))
							{
								if(priorFont)
								{
									s.insert(x,"</FONT>"+code+">");
									x+=7;
								}
								else
									s.insert(x,code+">");
								priorFont = true;
								x+=code.length();
							}
							else
							if(code.startsWith("<"))
							{
								s.insert(x,code+">");
								x+=code.length();
							}
							else
							{
								s.insert(x-1,code);
								x+=code.length()-1;
							}
						}
						count--;
					}
					break;
				}
				if(count==limit)
				{
					//int brx=s.indexOf("<BR>",lastSpace);
					//if((brx<0)||(brx>lastSpace+12))
						s.replace(lastSpace,lastSpace+1,"<BR>");
					lastSpace=lastSpace+4;
					x=lastSpace;
					count=0;
				}
				else
					x++;
			}
			if(priorFont)
				s.append("</FONT>");
			return s.toString();
		}
		return "";
	}

	protected PairSVector<String,String> parseOrderedParms(String parm, final boolean preserveCase)
	{
		final PairSVector<String,String> requestParms=new PairSVector<String,String>();
		if((parm!=null)&&(parm.length()>0))
		{
			int lastDex=0;
			CharSequence varSeq=null;
			for(int i=0;i<parm.length();i++)
			{
				switch(parm.charAt(i))
				{
				case '\\':
					i++;
					break;
				case '&':
				{
					if((i<parm.length()-4)
					&&(parm.charAt(i+1)=='a')
					&&(parm.substring(i+1,i+5).equals("amp;")))
					{
						parm=parm.substring(0,i+1)+parm.substring(i+5);
						break;
					}
					else
					if(varSeq==null)
					{
						if(preserveCase)
							requestParms.add(parm.substring(lastDex,i),parm.substring(lastDex,i));
						else
							requestParms.add(parm.substring(lastDex,i).toUpperCase().trim(),parm.substring(lastDex,i).trim());
					}
					else
					{
						if(preserveCase)
							requestParms.add(varSeq.toString(),parm.substring(lastDex,i));
						else
							requestParms.add(varSeq.toString().trim().toUpperCase(),parm.substring(lastDex,i).trim());
					}
					lastDex=i+1;
					varSeq=null;
					break;
				}
				case '=':
				{
					if(varSeq==null)
					{
						varSeq=parm.subSequence(lastDex,i);
						lastDex=i+1;
					}
					break;
				}
				}
			}
			final int i=parm.length();
			if(varSeq==null)
			{
				if(preserveCase)
					requestParms.add(parm.substring(lastDex,i),parm.substring(lastDex,i));
				else
					requestParms.add(parm.substring(lastDex,i).trim().toUpperCase(),parm.substring(lastDex,i).trim());
			}
			else
			{
				if(preserveCase)
					requestParms.add(varSeq.toString(),parm.substring(lastDex,i));
				else
					requestParms.add(varSeq.toString().trim().toUpperCase(),parm.substring(lastDex,i).trim());
			}
		}
		return requestParms;
	}

	protected String safeIncomingfilter(final String buf)
	{
		if(buf==null)
			return null;
		if(buf.length()==0)
			return "";
		return CMLib.coffeeFilter().simpleInFilter(new StringBuilder(buf));
	}

	protected String htmlIncomingFilter(final String buf)
	{
		return htmlIncomingFilter(new StringBuffer(buf)).toString();
	}

	protected StringBuffer htmlIncomingFilter(final StringBuffer buf)
	{
		int loop=0;

		while(buf.length()>loop)
		{
			if((buf.charAt(loop)=='&')
			&&(loop<buf.length()-3))
			{
				int endloop=loop+1;
				while((endloop<buf.length())&&(endloop<loop+10)&&(buf.charAt(endloop)!=';'))
					endloop++;
				if(endloop<buf.length())
				{
					final String s=buf.substring(loop,endloop+1);
					if(s.equalsIgnoreCase("&gt;"))
					{
						buf.setCharAt(loop,'>');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&lt;"))
					{
						buf.setCharAt(loop,'<');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&amp;"))
					{
						buf.setCharAt(loop,'&');
						buf.delete(loop+1,endloop+1);
					}
					else
					if(s.equalsIgnoreCase("&quot;"))
					{
						buf.setCharAt(loop,'\"');
						buf.delete(loop+1,endloop+1);
					}
				}
			}
			loop++;
		}
		return buf;
	}

	protected static String htmlOutgoingFilter(final String buf)
	{
		if(buf == null)
			return null;
		return htmlOutgoingFilter(new StringBuffer(buf)).toString();
	}

	protected static StringBuffer htmlOutgoingFilter(final StringBuffer buf)
	{
		int loop=0;

		while(buf.length()>loop)
		{
			switch(buf.charAt(loop))
			{
			case '>':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&gt;".toCharArray());
				loop+=3;
				break;
			case '"':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&quot;".toCharArray());
				loop+=5;
				break;
			case '&':
				if((loop+3>=buf.length())
				||((!buf.substring(loop,loop+3).equalsIgnoreCase("lt;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("amp;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("quot;"))
					&&(!buf.substring(loop,loop+3).equalsIgnoreCase("gt;"))))
				{
					buf.delete(loop,loop+1);
					buf.insert(loop,"&amp;".toCharArray());
					loop+=4;
				}
				else
					loop++;
				break;
			case '<':
				buf.delete(loop,loop+1);
				buf.insert(loop,"&lt;".toCharArray());
				loop+=3;
				break;
			default:
				loop++;
			}
		}
		return buf;
	}

	protected byte[] getHTTPFileData(final HTTPRequest httpReq, final String file) throws HTTPException
	{
		if(Thread.currentThread() instanceof CWThread)
		{
			final CWConfig config=((CWThread)Thread.currentThread()).getConfig();
			final HTTPRequest newReq=new HTTPRequest()
			{
				final Hashtable<String,String> params=new XHashtable<String,String>(httpReq.getUrlParametersCopy());

				@Override
				public String getHost()
				{
					return httpReq.getHost();
				}

				@Override
				public String getUrlPath()
				{
					return file;
				}

				@Override
				public String getFullRequest()
				{
					return httpReq.getMethod().name() + " " + getUrlPath();
				}

				@Override
				public String getUrlParameter(final String name)
				{
					return params.get(name.toLowerCase());
				}

				@Override
				public Map<String, String> getUrlParametersCopy()
				{
					return new XHashtable<String, String>(params);
				}

				@Override
				public boolean isUrlParameter(final String name)
				{
					return params.containsKey(name.toLowerCase());
				}

				@Override
				public Set<String> getUrlParameters()
				{
					return params.keySet();
				}

				@Override
				public HTTPMethod getMethod()
				{
					return httpReq.getMethod();
				}

				@Override
				public String getHeader(final String name)
				{
					return httpReq.getHeader(name);
				}

				@Override
				public InetAddress getClientAddress()
				{
					return httpReq.getClientAddress();
				}

				@Override
				public int getClientPort()
				{
					return httpReq.getClientPort();
				}

				@Override
				public InputStream getBody()
				{
					return httpReq.getBody();
				}

				@Override
				public String getCookie(final String name)
				{
					return httpReq.getCookie(name);
				}

				@Override
				public Set<String> getCookieNames()
				{
					return httpReq.getCookieNames();
				}

				@Override
				public List<MultiPartData> getMultiParts()
				{
					return httpReq.getMultiParts();
				}

				@Override
				public double getSpecialEncodingAcceptability(final String type)
				{
					return httpReq.getSpecialEncodingAcceptability(type);
				}

				@Override
				public String getFullHost()
				{
					return httpReq.getFullHost();
				}

				@Override
				public List<long[]> getRangeAZ()
				{
					return httpReq.getRangeAZ();
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
					return httpReq.getRequestObjects();
				}

				@Override
				public float getHttpVer()
				{
					return httpReq.getHttpVer();
				}

				@Override
				public String getQueryString()
				{
					return httpReq.getQueryString();
				}
			};

			final DataBuffers data=config.getFileGetter().getFileData(newReq);
			return data.flushToBuffer().array();
		}
		return new byte[0];
	}

	protected File grabFile(final HTTPRequest httpReq, String filename)
	{
		if(Thread.currentThread() instanceof CWThread)
		{
			filename=filename.replace(File.separatorChar,'/');
			if (!filename.startsWith("/")) filename = '/' + filename;
			final String file=filename;
			final CWConfig config=((CWThread)Thread.currentThread()).getConfig();
			final HTTPRequest newReq=new HTTPRequest()
			{
				public final Hashtable<String,String> params=new XHashtable<String,String>(httpReq.getUrlParametersCopy());

				@Override
				public String getHost()
				{
					return httpReq.getHost();
				}

				@Override
				public String getUrlPath()
				{
					return file;
				}

				@Override
				public String getFullRequest()
				{
					return httpReq.getMethod().name() + " " + getUrlPath();
				}

				@Override
				public String getUrlParameter(final String name)
				{
					return params.get(name.toUpperCase());
				}

				@Override
				public boolean isUrlParameter(final String name)
				{
					return params.containsKey(name.toUpperCase());
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
					return httpReq.getMethod();
				}

				@Override
				public String getHeader(final String name)
				{
					return httpReq.getHeader(name);
				}

				@Override
				public InetAddress getClientAddress()
				{
					return httpReq.getClientAddress();
				}

				@Override
				public int getClientPort()
				{
					return httpReq.getClientPort();
				}

				@Override
				public InputStream getBody()
				{
					return httpReq.getBody();
				}

				@Override
				public String getCookie(final String name)
				{
					return httpReq.getCookie(name);
				}

				@Override
				public Set<String> getCookieNames()
				{
					return httpReq.getCookieNames();
				}

				@Override
				public List<MultiPartData> getMultiParts()
				{
					return httpReq.getMultiParts();
				}

				@Override
				public double getSpecialEncodingAcceptability(final String type)
				{
					return httpReq.getSpecialEncodingAcceptability(type);
				}

				@Override
				public String getFullHost()
				{
					return httpReq.getFullHost();
				}

				@Override
				public List<long[]> getRangeAZ()
				{
					return httpReq.getRangeAZ();
				}

				@Override
				public void addFakeUrlParameter(final String name, final String value)
				{
					params.put(name.toUpperCase(), value);
				}

				@Override
				public void removeUrlParameter(final String name)
				{
					params.remove(name.toUpperCase());
				}

				@Override
				public Map<String, Object> getRequestObjects()
				{
					return httpReq.getRequestObjects();
				}

				@Override
				public float getHttpVer()
				{
					return httpReq.getHttpVer();
				}

				@Override
				public String getQueryString()
				{
					return httpReq.getQueryString();
				}
			};

			return config.getFileGetter().createFile(newReq,config.getFileGetter().assembleFilePath(newReq));
		}
		return null;
	}

	protected java.util.Map<String,String> parseParms(final String parm)
	{
		final Hashtable<String,String> requestParms=new Hashtable<String,String>();
		final PairSVector<String,String> requestParsed = parseOrderedParms(parm,false);
		for(final Pair<String,String> P : requestParsed)
			requestParms.put(P.first,P.second);
		return requestParms;
	}

	protected java.util.Map<String,String> parseParms(final String parm, final boolean preserveCase)
	{
		final Hashtable<String,String> requestParms=new Hashtable<String,String>();
		final PairSVector<String,String> requestParsed = parseOrderedParms(parm,preserveCase);
		for(final Pair<String,String> P : requestParsed)
			requestParms.put(P.first,P.second);
		return requestParms;
	}

	public String L(final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}
}
