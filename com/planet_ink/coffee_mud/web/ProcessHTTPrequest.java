package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

// quick & dirty web server for CoffeeMUD
// (c) 2002 Jeff Kamenek

// largely based on info from the relevant RFCs
//  and http://www.jmarshall.com/easy/http/

// TODO / NOT IMPLEMENTED:
//   doesn't handle URL parameters yet (strips them out)
//   Well, it does now, but it doesn't convert them from Funky Form(tm)
//   - Bo



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
	private Hashtable requestParameters = null;
	// default mime type
	private String mimetype = "text/html";
	private final static String mimePrefix = "MIME";

	private boolean headersOnly = false;

	// these are all the HTTP states this class can return
	private final static String S_200 = "200 OK";
	private final static String S_301 = "301 Moved Permanently";
	private final static String S_302 = "302 Moved Temporarily";
	private final static String S_400 = "400 Bad Request";
	private final static String S_401 = "401 Unauthorized";
	private final static String S_404 = "404 Not Found";
	private final static String S_500 = "500 Internal Server Error";
	private final static String S_501 = "501 Not Implemented";

	// not sure which order is expected - I think the first
	private final static String cr = "\r\n";
//	private final static String cr = "\n\r";

	private String status = S_500;
	private String statusExtra = "...";
	HTTPserver webServer;
	
	public boolean virtualPage;


	
	public ProcessHTTPrequest(Socket a_sock, HTTPserver a_webServer, INI a_page)
	{
		// thread name contains both an instance counter and the client's IP address
		//  (too long)
//		super( new String("HTTPrq-"+ instanceCnt++ +"-" + a_sock.getInetAddress().toString() ));
		// thread name contains just the instance counter (faster)
		//  and short enough to use in log
		super( "HTTPrq-"+a_webServer.getPartialName()+ instanceCnt++ );
		page = a_page;
		webServer = a_webServer;
		sock = a_sock;
		
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


	private boolean process(String inLine) throws Exception
	{
//		virtualPage = false;
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
			}
			else if (!command.equalsIgnoreCase("GET"))
			{
				// must reply with 501 if unsupported
				status = S_501;
				statusExtra = "Unimplemented HTTP request: <i>" + command + "</i>";
				return false;
			}

			
			try
			{
				request = inTok.nextToken();
			}
			catch (NoSuchElementException e)
			{
				request = new String("/");
			}

			int p = request.indexOf("?");
			if (p == -1)
			{
				requestMain = request;
			}
			else
			{
				String reqParms=null;
				if (p == 0)
				{
					requestMain = "/";
					reqParms = request.substring(1);
				}
				else
				{
					requestMain = request.substring(0,p);
					if (p < request.length())
						reqParms = request.substring(p+1);
				}
				if((reqParms!=null)&&(reqParms.length()>0))
				{
					requestParameters=new Hashtable();
					while(reqParms.length()>0)
					{
						int x=reqParms.indexOf("&");
						String req=null;
						if(x>=0)
						{
							req=reqParms.substring(0,x);
							reqParms=reqParms.substring(x+1);
						}
						else
						{
							req=reqParms;
							reqParms="";
						}
						if(req!=null)
						{
							x=req.indexOf("=");
							if(x>=0)
								requestParameters.put(req.substring(0,x).trim().toUpperCase(),req.substring(x+1).trim());
							else
								requestParameters.put(req.trim().toUpperCase(),req.trim());
						}
					}
				}
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

	public Hashtable getRequestParameters()
	{
		if(requestParameters==null)
			requestParameters=new Hashtable();
		return requestParameters;
	}
	private byte [] doVirtualPage(byte [] data)
	{
		if((webServer.webMacros==null)
		   ||(webServer.webMacros.size()==0))
			return data;
		StringBuffer s = new StringBuffer(new String(data));
		try
		{
			for(int i=0;i<s.length();i++)
			{
				if(s.charAt(i)=='@')
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
						if(s.charAt(x)=='=')
							extend=true;
						else
						if(((x-i)>webServer.longestMacro)&&(!extend))
							break;
					}
					if(foundMacro!=null)
					{
						if(foundMacro.equalsIgnoreCase("loop"))
						{
							int v=s.toString().toUpperCase().indexOf("@BACK@");
							if(v<0)
								s.replace(i,i+6, "[loop without back]" );
							else
							{
								String s2=s.substring(i+7,v);
								s.replace(i,v+6,"");
								int ldex=i;
								String s3=" ";
								while(s3.length()>0)
								{
									s3=new String(doVirtualPage(s2.getBytes()));
									s.insert(ldex,s3);
									ldex+=s3.length();
								}
							}
																					 
						}
						else
						if(foundMacro.equalsIgnoreCase("break"))
							return ("").getBytes();
						else
						{
							int l=foundMacro.length();
							int x=foundMacro.indexOf("=");
							String parms=null;
							if(x>=0)
							{
								parms=foundMacro.substring(x+1);
								foundMacro=foundMacro.substring(0,x);
							}
							WebMacro W=(WebMacro)webServer.webMacros.get(foundMacro.toUpperCase());
							if(W!=null)
							{
								String q=W.runMacro(this,parms);
								if (q != null)
									s.replace(i,i+l+2, q );
								else
									s.replace(i,i+l+2, "[error]" );
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.errOut(getName(), "Exception in doVirtualPage() - " + e.getMessage() );
		}
		
		return s.toString().getBytes();
	}
	
	
	public void run()
	{
		BufferedReader sin = null;
//		PrintWriter sout;
		DataOutputStream sout = null;
		
		byte[] replyData = null;
		
		status = S_200;

		try
		{	
//			sout = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			sout = new DataOutputStream(sock.getOutputStream());
			sin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String inLine = sin.readLine();

			GrabbedFile requestedFile;
			headersOnly = false;
			
			virtualPage = false;
			boolean processOK = process(inLine);

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

//					case GrabbedFile.INTERNAL_ERROR:
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
						mimetype = new String("application/octet-stream");	// default to raw binary
						
					if (page.getStr("VIRTUALPAGEEXTENSION").equalsIgnoreCase(exten) )
						virtualPage = true;

					try
					{
						DataInputStream fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(requestedFile.file) ) );
//						replyData = new byte [ requestedFile.length() ];
						replyData = new byte [ fileIn.available() ];
						
						fileIn.readFully(replyData);
						fileIn.close();
						fileIn = null;
					}
					catch (IOException e)
					{
						status = S_500;
						statusExtra = new String("IO error while reading URL <I>" + request +"</I>");
						processOK = false;
					}
				}
			}

			// build error page
			if (!processOK || replyData == null)
			{
//				mimetype = new String("text/html");
				mimetype = getMimeType(page.getStr("VIRTUALPAGEEXTENSION"));
			
				if (mimetype.length() == 0)
					mimetype = new String("application/octet-stream");	// default to raw binary
					
				// try to get an error page from the template directory
				//  if it doesn't exist, make a simple error page and return that
				try
				{
//					requestedFile = new File("web" + File.separatorChar + "error" + page.getStr("VIRTUALPAGEEXTENSION") );
///					requestedFile = new File(webServer.getServerTemplateDir() + File.separatorChar + "error" + page.getStr("VIRTUALPAGEEXTENSION") );
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
					mimetype = new String("text/html");
					replyData = WebHelper.makeErrorPage(status,statusExtra);
				}
				
			}

			if (virtualPage)
			{
				replyData = doVirtualPage(replyData);
			}

			// first the status header
			sout.writeBytes("HTTP/1.0 " + status + cr);

			// other headers
			// may add content-length at some point, shouldn't
			//  be necassary though
			// should also probably add Last-Modified
			
			sout.writeBytes("Server: " + HTTPserver.ServerVersionString + cr);
			sout.writeBytes("MIME-Version: 1.0" + cr);
//			sout.writeBytes("Content-Type: " + mimetype + cr);
			sout.writeBytes("Content-Type: " + mimetype);
			sout.writeBytes( cr );
			
			
			if (!headersOnly)
			{
				if ((replyData != null))
				{
					sout.writeBytes("Content-Length: " + replyData.length);
					sout.writeBytes( cr );

					// must insert a blank line before message body
					sout.writeBytes( cr );
					sout.write(replyData);
				}
			}
		}
		catch (Exception e)
		{
			Log.errOut(getName(),"Exception: " + e.getMessage() );
		}
/*
		Log.sysOut(getName(), "IP=" +  sock.getInetAddress().toString());
		Log.sysOut(getName(), "Request='" + (command==null?"(null)":command + " " + (request==null?"(null)":request)) + "'");
		Log.sysOut(getName(), "Replied='" + status + "'" );
*/
		Log.debugOut(getName(), sock.getInetAddress().getHostAddress() + ":" + (command==null?"(null)":command + " " + (request==null?"(null)":request)) + 
				":" + status);
		
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
			if (sout != null)
			{
				sout.flush();
				sout.close();
				sout = null;
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
		{
//			return sock.getInetAddress().toString();
			// no reverse DNS (just dotted quad)
			return sock.getInetAddress().getHostAddress();
		}
		return "[NOT CONNECTED]";
	}
	public String ServerVersionString(){return HTTPserver.ServerVersionString;}
	public String getWebServerPortStr(){return getWebServer().getPortStr();}
	public String getWebServerPartialName(){ return getWebServer().getPartialName();}
	public Host getMUD(){return getWebServer().getMUD();}
	public String WebHelperhtmlPlayerList(){return WebHelper.htmlPlayerList();}
	public String WebHelperhtmlAreaTbl(){return WebHelper.htmlAreaTbl(getWebServer());}
}