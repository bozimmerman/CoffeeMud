package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.exceptions.*;

// quick & dirty web server for CoffeeMUD
// (c) 2002 Jeff Kamenek

// largely based on info from the relevant RFCs
//  and http://www.jmarshall.com/easy/http/

// TODO / NOT IMPLEMENTED:
// POST!

// although this technically doesn't *need* to be a thread,
//  (ie. it only runs once, responding to request)
//  if it wasn't one the webserver would get bogged down every time
//  there were multiple simultaenous requests.
public class ProcessHTTPrequest extends Thread implements ExternalHTTPRequests
{
	private INI page;
	private Socket sock;

	private static int instanceCnt = 0;

	private String command = null;
	private String request = null;
	private String requestMain = null;

	private String requestParametersEncoded = null;	// I keep the encoded form
	// I've called it Table to distinguish it from Encoded string...
	private Hashtable requestParametersTable = null;

	// default mime type
	private String mimetype = "text/html";
	private final static String mimePrefix = "MIME";

	private boolean headersOnly = false;

	private boolean isAdminServer = false;

	// these are all the HTTP states this class can return
	private final static String S_200 = "200 OK";
	private final static String S_301 = "301 Moved Permanently";
	private final static String S_302 = "302 Found";
	private final static String S_303 = "303 See Other";
	private final static String S_307 = "307 Temporary Redirect";

	private final static String S_400 = "400 Bad Request";
	private final static String S_401 = "401 Unauthorized";
	private final static String S_404 = "404 Not Found";
	private final static String S_500 = "500 Internal Server Error";
	private final static String S_501 = "501 Not Implemented";

	// not sure which order is expected - I think the first
	private final static String cr = "\r\n";
	//private final static String cr = "\n\r";

	private String status = S_500;
	private String statusExtra = "...";
	HTTPserver webServer;

	public boolean virtualPage;
	
	private Vector theCache=new Vector();
	
	private Hashtable objects=null;

	public ProcessHTTPrequest(Socket a_sock, HTTPserver a_webServer, INI a_page, boolean a_isAdminServer)
	{
		// thread name contains both an instance counter and the client's IP address
		//  (too long)
		//		super( "HTTPrq-"+ instanceCnt++ +"-" + a_sock.getInetAddress().toString() );
		// thread name contains just the instance counter (faster)
		//  and short enough to use in log
		super( "HTTPrq-"+a_webServer.getPartialName()+ instanceCnt++ );
		page = a_page;
		webServer = a_webServer;
		sock = a_sock;
		isAdminServer = a_isAdminServer;

		if (page != null && sock != null)
			this.start();
	}

	public HTTPserver getWebServer()	{return webServer;}
	public String getHTTPstatus()	{return status;}
	public String getHTTPstatusInfo()	{return statusExtra==null?"":statusExtra;}


	public String getMimeType(String a_extension)
	{
		String lookFor = new String (mimePrefix + a_extension);
		return page.getStr(lookFor.toUpperCase());
	}
	
	public Vector cache(){return theCache;}

	private boolean process(String inLine) throws Exception
	{
		virtualPage = false;
		try
		{
			StringTokenizer inTok = new StringTokenizer(inLine," ");
			try
			{
				command = inTok.nextToken();
			}
			catch (NoSuchElementException e)
			{
				status = S_400;
				statusExtra = "Empty request";
				return false;
			}


			// should always be uppercase, but I allow for mixed-case anyway
			// only handles GET & HEAD requests, not POSTS
			// (or the obscure ones: PUT, DELETE, OPTIONS and TRACE)
			if (command.equalsIgnoreCase("HEAD"))
			{
				headersOnly = true;
				command = "GET";
			}
			else
			if (command.equalsIgnoreCase("GET"))
			{

				try
				{
					request = inTok.nextToken();
				}
				catch (NoSuchElementException e)
				{
					request = "/";
				}
				int p = request.indexOf("?");
				if (p == -1)
				{
					requestMain = request;
				}
				else
				{
					if (p == 0)
					{
						requestMain = "/";
						requestParametersEncoded = request.substring(1);
					}
					else
					{
						requestMain = request.substring(0,p);
						if (p < request.length())
							requestParametersEncoded = request.substring(p+1);
					}


				}
				try
				{
					requestMain = URLDecoder.decode(requestMain,"UTF-8");
				}
				catch(UnsupportedEncodingException e)
				{
					Log.errOut(getName(),"Received wrong encoding");
				}
			}
			else
			if (command.equalsIgnoreCase("POST"))
			{
				requestMain = inTok.nextToken();
				String postText = "";
				while(inTok.hasMoreTokens())
				{
					postText = inTok.nextToken();
				}
				requestParametersEncoded = postText;
			}
			else
			{
				// must reply with 501 if unsupported
				status = S_501;
				statusExtra = "Unimplemented HTTP request: <i>" + command + "</i>";
				return false;
			}

			return true;
		}
		catch (Exception e)
		{
			status = S_500;
			statusExtra = "D'OH! An internal exception occured: <i>" + e.getMessage()+"</i>";
			return false;
		}

	}
	
	public Hashtable getRequestObjects()
	{
		if(objects==null) objects=new Hashtable();
		return objects;
	}
	
	public void resetRequestEncodedParameters()
	{
		StringBuffer buf=new StringBuffer("");
		for(Enumeration e=getRequestParameters().keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String value=(String)getRequestParameters().get(key);
			if(buf.length()>0) buf.append("&");
			try
			{
				buf.append(URLEncoder.encode(key,"UTF-8")+"="+URLEncoder.encode(value,"UTF-8"));
			}  
			catch(java.io.UnsupportedEncodingException es)
			{
				Log.errOut(getName(),"Wrong Encoding");
			}
		}
		requestParametersEncoded=buf.toString();
	}

	public void addRequestParameters(String key, String value)
	{
		requestParametersTable.put(key,value);
		resetRequestEncodedParameters();
	}
	
	public Hashtable getRequestParameters()
	{
		// have we already parsed the parameters?
		if (requestParametersTable != null)
			return requestParametersTable;

		requestParametersTable = new Hashtable();

		// do we have any parameters to parse?
		if (requestParametersEncoded == null)
			return requestParametersTable;

		String pStr = new String(requestParametersEncoded);
		String thisParam;
		String thisParamName;
		String thisParamValue;

		while (pStr != null)
		{
			int p = pStr.indexOf("&");
			if (p == -1)
			{
				thisParam = pStr;
				pStr = null;
			}
			else
			{
				thisParam = pStr.substring(0,p);
				if (p < pStr.length())
					pStr = pStr.substring(p+1);
				else
					pStr = null;
			}

			int eq=thisParam.indexOf("=");
			if (eq == -1)
			{
				// a null parameter (ie a parameter name with no value,
				//  which is valid - I give it a value of "" rather than null tho!
				thisParamName = thisParam;
				thisParamValue = "";
			}
			else
			{
				thisParamName = thisParam.substring(0,eq);
				thisParamValue = "";
				if (eq < thisParam.length())
				{
					try
					{
						thisParamValue=preFilter(new StringBuffer(URLDecoder.decode(thisParam.substring(eq+1), "UTF-8")));
					}
					catch(UnsupportedEncodingException e)
					{
						Log.errOut(getName(),"Received wrong encoding.2");
					}
				}
			}
			if(!requestParametersTable.containsKey(thisParamName.toUpperCase()))
				requestParametersTable.put(thisParamName.toUpperCase(),thisParamValue);
			else
			for(int i=1;;i++)
				if(!requestParametersTable.containsKey(thisParamName.toUpperCase()+(new Integer(i).toString())))
				{
					requestParametersTable.put(thisParamName.toUpperCase()+(new Integer(i).toString()),thisParamValue);
					break;
				}
		}

		return requestParametersTable;

	}

	public String getRequestEncodedParameters()
	{
		return (requestParametersEncoded!=null?requestParametersEncoded:"");
	}


	private String parseFoundMacro(StringBuffer s, int i)
	{
		String foundMacro=null;
		boolean extend=false;
		for(int x=i+1;x<s.length();x++)
		{
			if(s.charAt(x)=='@')
			{
				foundMacro=s.substring(i+1,x);
				break;
			}
			else
			if(s.charAt(x)=='?')
				extend=true;
			else
			if(((x-i)>webServer.longestMacro)&&(!extend))
				break;
		}
		return foundMacro;
	}
	private int myBack(StringBuffer s, int i)
	{
		int backsToFind=1;
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i);
				if(foundMacro!=null)
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


	private String preFilter(StringBuffer input)
	{
		if(input==null) return null;

		int x=0;
		while(x<input.length())
		{
			char c=input.charAt(x);
			if(c=='\'')
				input.setCharAt(x,'`');
			else
			if(c==8)
			{
				String newStr=input.toString();
				if(x==0)
					input=new StringBuffer(newStr.substring(x+1));
				else
				{
					input=new StringBuffer(newStr.substring(0,x-1)+newStr.substring(x+1));
					x--;
				}
				x--;
			}
			x++;
		}
		return input.toString();
	}
	
	private int myEndif(StringBuffer s, int i)
	{
		int endifsToFind=1;
		for(;i<s.length();i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i);
				if(foundMacro!=null)
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
	
	private String runMacro(String foundMacro)
		throws HTTPRedirectException, HTTPServerException
	{
		int x=foundMacro.indexOf("?");
		String parms=null;
		if(x>=0)
		{
			parms=foundMacro.substring(x+1);
			foundMacro=foundMacro.substring(0,x);
		}
		if(foundMacro.length()==0)
			return "";
		WebMacro W=(WebMacro)webServer.webMacros.get(foundMacro.toUpperCase());
		if(W!=null)
		{
			String q=null;
			if (!isAdminServer && W.isAdminMacro())
			{
				Log.errOut(getName(), "Non-admin cannot access admin macro '" + W.name() + "'; client IP: " + sock.getInetAddress());
				q = "[error]";
			}
			else
				q=W.runMacro(this,parms);
			if (q != null)
				return q;
			else
				return "[error]";
		}
		return null;
	}

	private int myElse(StringBuffer s, int i, int end)
	{
		int endifsToFind=1;
		for(;i<end;i++)
		{
			if(s.charAt(i)=='@')
			{
				String foundMacro=parseFoundMacro(s,i);
				if(foundMacro!=null)
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

	// OK - this parser is getting a bit ugly now;
	//  I'm probably gonna replace it soon
	private byte [] doVirtualPage(byte [] data) throws HTTPRedirectException
	{
		if((webServer.webMacros==null)
		   ||(webServer.webMacros.size()==0))
			return data;
		StringBuffer s = new StringBuffer(new String(data));
		String redirectTo = null;

		try
		{
			for(int i=0;i<s.length();i++)
			{
				if(s.charAt(i)=='@')
				{
					String foundMacro=parseFoundMacro(s,i);
					if(foundMacro!=null)
					{
						if(foundMacro.startsWith("if?"))
						{
							int l=foundMacro.length()+2;
							int v=myEndif(s,i+l);
							if(v<0)
								s.replace(i,i+l,"[if without endif]");
							else
							{
								int v2=myElse(s,i+l,v);
								foundMacro=foundMacro.substring(3);
								try
								{
									String compare="true";
									if(foundMacro.startsWith("!"))
									{
										foundMacro=foundMacro.substring(1);
										compare="false";
									}
									String q=runMacro(foundMacro);
									if((q!=null)&&(q.equalsIgnoreCase(compare)))
									{
										if(v2>=0)
											s.replace(v2,v+7,"");
										else
											s.replace(v,v+7,"");
										s.replace(i,i+l,"");
									}
									else
									{
										if(v2>=0)
											s.replace(i,v2+6,"");
										else
											s.replace(i,v+7,"");
									}
								}
								catch (HTTPRedirectException e)
								{
									redirectTo = e.getMessage();
								}
							}
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("endif"))
						{
							s.replace(i,i+7,"");
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("else"))
						{
							s.replace(i,i+6,"");
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("loop"))
						{
							int v=myBack(s,i+6);
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
										s3=new String(doVirtualPage(s2.getBytes()));
									}
									catch (HTTPRedirectException e)
									{
										s3 = " ";
										redirectTo = e.getMessage();
									}

									s.insert(ldex,s3);
									ldex+=s3.length();
								}
							}
							continue;
						}
						else
						if(foundMacro.equalsIgnoreCase("break"))
							return ("").getBytes();
						else
						if(foundMacro.equalsIgnoreCase("back"))
						{
							s.replace(i,i+6, "[back without loop]" );
							continue;
						}
						
						if(foundMacro.length()>0)
						{
							try
							{
								int l=foundMacro.length();
								String q=runMacro(foundMacro);
								if (q != null)
									s.replace(i,i+l+2, q );
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
			if((e.getMessage()==null)||(e.getMessage().indexOf("reset by peer")<0))
			{
				Log.errOut(getName(), "Exception in doVirtualPage() - " + e.getMessage() );
				Log.errOut(getName(),e);
			}
		}

		if (redirectTo != null)
		{
			throw new HTTPRedirectException(redirectTo);
		}
		
		return s.toString().getBytes();
	}


	// if the client's browser does not support or honour 3xx redirects,
	//  then this attached page attempts to use javascript to redirect
	// if that doesn't work, the meta refresh will hopefully refresh to the new url;
	//  if *that* doesn't work, we present the user with a link to click.
	// the page is also marked as uncacheable with an invalid expiry date
	//
	// *** this will be replaced by a template page in the near future ***
	//     (when I can be bothered doing it that is)
	public String makeRedirectPage(String url)
	{
		return "<html><head>"
				+"<META HTTP-EQUIV=\"Expires\" CONTENT=\"Mon, 06 Jan 1990 00:00:01 GMT\">"
				+"<meta HTTP-EQUIV=\"Refresh\" CONTENT=\"2; URL=" + url + "\">"
				+"<script>self.location.href = \"" + url +"\";</script>"
				+"</head><body>"
				+"<br>Redirecting in 2 seconds; if redirection does not work, "
				+"<a href=\"" + url + "\">click here!</a><br></body></html>";
	}

	public void run()
	{
		String hdrRedirectTo = null;


		BufferedReader sin = null;
		DataOutputStream sout = null;

		byte[] replyData = null;

		status = S_200;

		try
		{
			//sout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			sout = new DataOutputStream(sock.getOutputStream());
			sin = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			GrabbedFile requestedFile;
			headersOnly = false;

			virtualPage = false;
			boolean processOK = process(getHTTPRequest(sin));

			if (processOK)
			{
				String filename = new String(requestMain);
				requestedFile = webServer.pageGrabber.grabFile(filename);

				switch (requestedFile.state)
				{
					case GrabbedFile.OK:
						break;
					case GrabbedFile.IS_DIRECTORY:
						if (!filename.endsWith( "/" ))
							filename += '/';
						filename += page.getStr("DEFAULTFILE");
						requestedFile = webServer.pageGrabber.grabFile(filename);
						if (requestedFile.state != GrabbedFile.OK)
						{
							status = S_401;
							statusExtra = "Directory listing for <i>" + requestMain + "</i> denied.";
							processOK = false;
						}
						break;

					case GrabbedFile.BAD_FILENAME:
						status = S_400;
						statusExtra = "The requested URL <i>" + requestMain + "</i> is invalid.";
						processOK = false;
						break;
					case GrabbedFile.NOT_FOUND:
						status = S_404;
						statusExtra = "The requested URL <i>" + requestMain + "</i> was not found on this server.";
						processOK = false;
						break;
					case GrabbedFile.SECURITY_VIOLATION:
						status = S_401;
						statusExtra = "Denied access to <i>" + requestMain + "</i>. WARNING: I will never be your best friend.";
						processOK = false;
						break;

					//case GrabbedFile.INTERNAL_ERROR:
					default:
						status = S_500;
						statusExtra = "An internal error occured.";
						processOK = false;
						break;
				}


				if (processOK)
				{
					String exten;
					try { exten = filename.substring(filename.lastIndexOf(".")); }
					catch (Exception e) {exten = "";}
					if (exten==null) exten = "";

					mimetype = getMimeType(exten);

					if (mimetype.length() == 0)
						mimetype = "application/octet-stream";	// default to raw binary

					if (page.getStr("VIRTUALPAGEEXTENSION").equalsIgnoreCase(exten) )
						virtualPage = true;

					try
					{
						DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(requestedFile.file) ) );
						replyData = new byte [ fileIn.available() ];

						fileIn.readFully(replyData);
						fileIn.close();
						fileIn = null;
					}
					catch (IOException e)
					{
						status = S_500;
						statusExtra = "IO error while reading URL <I>" + request +"</I>";
						processOK = false;
					}
				}
			}

			// build error page
			if (!processOK || replyData == null)
			{
				//mimetype = "text/html";
				mimetype = getMimeType(page.getStr("VIRTUALPAGEEXTENSION"));

				if (mimetype.length() == 0)
					mimetype = "application/octet-stream";	// default to raw binary

				// try to get an error page from the template directory
				//  if it doesn't exist, make a simple error page and return that
				try
				{
					//requestedFile = new File("web" + File.separatorChar + "error" + page.getStr("VIRTUALPAGEEXTENSION") );
					///requestedFile = new File(webServer.getServerTemplateDir() + File.separatorChar + "error" + page.getStr("VIRTUALPAGEEXTENSION") );
					requestedFile = webServer.templateGrabber.grabFile("error" + page.getStr("VIRTUALPAGEEXTENSION"));

					if (requestedFile.state == GrabbedFile.OK)
					{
						virtualPage = true;
						DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(requestedFile.file) ) );
						replyData = new byte [ fileIn.available() ];
						fileIn.readFully(replyData);
						fileIn.close();
						fileIn = null;
					}
					else
						replyData = null;
				}
				catch (Exception e)
				{
					replyData = null;
				}

				if (replyData == null)
				{
					// make the builtin error page
					virtualPage = false;
					mimetype = "text/html";
					replyData = WebHelper.makeErrorPage(status,statusExtra);
				}

			}


			if (virtualPage)
			{
				try
				{
					replyData = doVirtualPage(replyData);
				}
				catch (HTTPRedirectException e)
				{
					status = S_303;
					hdrRedirectTo = e.getMessage();
					replyData = makeRedirectPage(hdrRedirectTo).getBytes();

				}
			}

			// first the status header
			sout.writeBytes("HTTP/1.0 " + status + cr);

			// other headers
			// may add content-length at some point, shouldn't
			//  be necassary though
			// should also probably add Last-Modified
			if (hdrRedirectTo != null)
			{
				sout.writeBytes("Location: " + hdrRedirectTo + cr);
			}


			sout.writeBytes("Server: " + HTTPserver.ServerVersionString + cr);
			sout.writeBytes("MIME-Version: 1.0" + cr);
			sout.writeBytes("Content-Type: " + mimetype + cr);
			if ((replyData != null))
			{
				sout.writeBytes("Content-Length: " + replyData.length);
				sout.writeBytes( cr );
			}
			if (!headersOnly)
			{
				if ((replyData != null))
				{
					// must insert a blank line before message body
					sout.writeBytes( cr );
					sout.write(replyData);
				}
			}

		}
		catch (Exception e)
		{
			if((e.getMessage()==null)||(e.getMessage().indexOf("reset by peer")<0))
			{
				Log.errOut(getName(),"Exception: " + e.getMessage() );
				if(!(e instanceof java.net.SocketException))
					Log.errOut(getName(),e);
			}
		}
		Log.debugOut(getName(), sock.getInetAddress().getHostAddress() + ":" + (command==null?"(null)":command + " " + (request==null?"(null)":request)) +
				":" + status);

		try
		{
			if (sout != null)
			{
				sout.flush();
				try{Thread.sleep(500);}catch(Exception e){}
				sout.close();
				sout = null;
			}
		}
		catch (Exception e)	{}

		try
		{
			if (sin != null)
			{
				sin.close();
				sin = null;
			}
		}
		catch (Exception e)	{}

		try
		{
			if (sock != null)
			{
				sock.close();
				sock = null;
			}
		}
		catch (Exception e)	{}
	}
	public String getHTTPclientIP()
	{
		if (sock != null)
			return sock.getInetAddress().getHostAddress();
		return "[NOT CONNECTED]";
	}

	// gets the InetAddress of the server this request connected to;
	// this is because HTTP redirects must specify complete path (doh!)
	public InetAddress getServerAddress()
	{
		if (sock != null)
			return sock.getLocalAddress();
		return null;
	}

	public String getPageContent(String filename)
	{
		try
		{
			GrabbedFile requestedFile = webServer.pageGrabber.grabFile(filename);
			if((requestedFile==null)
			||(requestedFile.state!=GrabbedFile.OK))
				return "";
			DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(requestedFile.file) ) );
			byte[] replyData = new byte [ fileIn.available() ];

			fileIn.readFully(replyData);
			fileIn.close();
			fileIn = null;
			String exten="";
			try { exten = filename.substring(filename.lastIndexOf(".")); }
			catch (Exception e) {}
			if(page.getStr("VIRTUALPAGEEXTENSION").equalsIgnoreCase(exten) )
				replyData=doVirtualPage(replyData);
			return new String(replyData);
		}
		catch (Exception e)
		{
			if((e.getMessage()==null)||(e.getMessage().indexOf("reset by peer")<0))
			{
				Log.errOut(getName(),"Exception: " + e.getMessage() );
				if(!(e instanceof java.net.SocketException))
					Log.errOut(getName(),e);
			}
		}
		return "";
	}

	public String getHTTPRequest(BufferedReader sin)
	{
		try
		{
			String inLine = sin.readLine();
			//Log.sysOut("HTTP",inLine);
			if(inLine==null) return "";
			
			if((inLine.startsWith("GET")||inLine.startsWith("HEAD")))
			{
				return inLine;
			}
			StringBuffer inData = new StringBuffer("");
			inData.append(inLine+" ");
			
			while(inLine.toUpperCase().indexOf("SUBMIT")<0)
			{
				inLine = sin.readLine();
				//Log.sysOut("HTTP",inLine);
			}
			if(inLine.toUpperCase().indexOf("REFERER")>0)
			{
				int x = 0;
				x = inLine.indexOf("?");
				inLine = inLine.substring(x+1);
			}
			inData.append(inLine);
			//Log.sysOut("HTTPParms",inData.toString());
			return inData.toString();
		}
		catch (Exception e)
		{
			if((e.getMessage()==null)||(e.getMessage().indexOf("reset by peer")<0))
			{
				Log.errOut(getName(),"Exception: " + e.getMessage() );
				if(!(e instanceof java.net.SocketException))
					Log.errOut(getName(),e);
			}
		}
		return "err";
	}
	
	
	public String ServerVersionString(){return HTTPserver.ServerVersionString;}
	public String getWebServerPortStr(){return getWebServer().getPortStr();}
	public String getWebServerPartialName(){ return getWebServer().getPartialName();}
	public Host getMUD(){return getWebServer().getMUD();}
	public String WebHelperhtmlPlayerList(){return WebHelper.htmlPlayerList();}
	public String WebHelperhtmlAreaTbl(){return WebHelper.htmlAreaTbl(getWebServer());}
}