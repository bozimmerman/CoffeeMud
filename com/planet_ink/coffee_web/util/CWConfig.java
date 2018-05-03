package com.planet_ink.coffee_web.util;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.FileCacheManager;
import com.planet_ink.coffee_web.interfaces.FileManager;
import com.planet_ink.coffee_web.interfaces.HTTPFileGetter;
import com.planet_ink.coffee_web.interfaces.MimeConverterManager;
import com.planet_ink.coffee_web.interfaces.ServletSessionManager;
import com.planet_ink.coffee_web.interfaces.SimpleServletManager;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_mud.core.collections.KeyPairWildSearchTree;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.Triad;
import com.planet_ink.coffee_mud.core.collections.KeyPairSearchTree;

/*
   Copyright 2012-2018 Bo Zimmerman

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

/**
 * Configuration for coffeewebserver
 * @author Bo Zimmerman
 *
 */
public class CWConfig implements Cloneable
{
	public  static final int	  DEFAULT_HTP_LISTEN_PORT 		= 80;
	public  static final int	  DEFAULT_SSL_PORT 				= 443;

	private static final String   DEFAULT_PAGE 					= "index.html"; // this would normally be configurable as a list
	private static final String   DEFAULT_ERROR_PAGE 			= "root\\errorpage.cwhtml";
	private static final String   DEFAULT_BROWSE_PAGE 			= "root\\browsepage.cwhtml";
	
	public  static final long  	  DEFAULT_THROTTLE_BYTES 		= Long.MAX_VALUE / 2;
	
	// configuration for the request thread pool
	private static final int	  DEFAULT_CORE_THREAD_POOL_SIZE	= 1;
	private static final int	  DEFAULT_MAX_THREAD_POOL_SIZE 	= 10;
	private static final int	  DEFAULT_THREAD_KEEP_ALIVE_MS 	= 60 * 1000; // max age of idle threads
	private static final int	  DEFAULT_THREAD_TIMEOUT_SECS	= 30; //Timeout for active request threads
	private static final int	  DEFAULT_THREAD_QUEUE_SIZE		= 500;//Above this and they start getting rejected
	
	private static final long	  DEFAULT_FILECACHE_EXPIRE_MS	= 5 * 60 * 1000; 		// 5 minutes -- how long a cache entry lived
	private static final long	  DEFAULT_FILECACHE_MAX_BYTES	= 10 * 1024 * 1024;	// the maximum number of bytes this cache will hold total
	private static final long	  DEFAULT_FILECACHE_MAX_FBYTES	= 2 * 1024 * 1024;	// the maximum size of file the cache will hold

	private static final long	  DEFAULT_FILECOMP_MAX_FBYTES	= 16 * 1024 * 1024;	// the maximum size of file the cache will hold
	
	private static final long	  DEFAULT_MAX_BODY_BYTES   		= 1024 * 1024 * 2; // maximum size of a request body
	private static final long	  DEFAULT_MAX_IDLE_MILLIS  		= 30 * 1000;		// maximum time a request can be idle (between reads)
	private static final int	  DEFAULT_LINE_BUFFER_SIZE		= 4096; // maximum length of a single line in the main request
	private static final int	  DEFAULT_MAX_ALIVE_SECS 		= 15;	// maximum age, in seconds, of a request connection
	private static final int	  DEFAULT_MAX_PIPELINED_REQUESTS= 10;	// maximum number of requests per connection
	
	private static final String   DEFAULT_SSL_KEYSTORE_TYPE		= "JKS";
	private static final String   DEFAULT_SSL_KEYMANAGER_ENC	= "SunX509";
	
	private static final String   DEFAULT_DEBUG_FLAG			= "OFF";
	
	private static final String   DEFAULT_ACCESSLOG_FLAG		= "OFF";

	private static final long	  DEFAULT_SESSION_IDLE_MILLIS 	= 30 * 60 * 1000;		// maximum time a session can be idle (between requests)
	private static final long	  DEFAULT_SESSION_AGE_MILLIS  	= 24 *60 * 60 * 1000;	// maximum time a session can be in existence
	
	private static final Integer  ALL_PORTS						= Integer.valueOf(-1);
	private static final String   ALL_HOSTS						= "";
	
	private Map<String,String>	 	 servlets					= new HashMap<String,String>();
	private Map<String,String>	  	 fileConverts				= new HashMap<String,String>();
	
	private final Map<String,String> miscFlags					= new HashMap<String,String>();
	private final Set<DisableFlag>	 disableFlags				= new HashSet<DisableFlag>();
	
	private SimpleServletManager  servletMan					= null;
	private ServletSessionManager sessions						= null;
	private MimeConverterManager  converters					= null;
	private FileCacheManager	  fileCache						= null;
	private Logger				  logger						= null;
	private HTTPFileGetter		  fileGetter					= null;
	private WebServer			  coffeeWebServer				= null;
	private FileManager			  fileManager					= new CWFileManager();
	
	private String  sslKeystorePath		 = null;
	private String  sslKeystorePassword  = null;
	private String	sslKeystoreType		 = DEFAULT_SSL_KEYSTORE_TYPE;
	private String	sslKeyManagerEncoding= DEFAULT_SSL_KEYMANAGER_ENC;
	
	private int[]	httpListenPorts		 = new int[]{DEFAULT_HTP_LISTEN_PORT};
	private int[]	httpsListenPorts	 = new int[]{DEFAULT_SSL_PORT};
	private String 	bindAddress			 = null;
	
	private int		coreThreadPoolSize	 = DEFAULT_CORE_THREAD_POOL_SIZE;
	private int		maxThreadPoolSize	 = DEFAULT_MAX_THREAD_POOL_SIZE;
	private int		maxThreadIdleMs		 = DEFAULT_THREAD_KEEP_ALIVE_MS;
	private int		maxThreadTimeoutSecs = DEFAULT_THREAD_TIMEOUT_SECS;
	private int		maxThreadQueueSize	 = DEFAULT_THREAD_QUEUE_SIZE;
	
	private long	fileCacheExpireMs	 = DEFAULT_FILECACHE_EXPIRE_MS;
	private long	fileCacheMaxBytes	 = DEFAULT_FILECACHE_MAX_BYTES;
	private long	fileCacheMaxFileBytes= DEFAULT_FILECACHE_MAX_FBYTES;
	private long	fileCompMaxFileBytes = DEFAULT_FILECOMP_MAX_FBYTES;
	
	private long	sessionMaxIdleMs	 = DEFAULT_SESSION_IDLE_MILLIS;
	private long	sessionMaxAgeMs		 = DEFAULT_SESSION_AGE_MILLIS;
	
	private long	requestMaxBodyBytes	 = DEFAULT_MAX_BODY_BYTES;
	private long	requestMaxIdleMs	 = DEFAULT_MAX_IDLE_MILLIS;
	private long	requestLineBufBytes	 = DEFAULT_LINE_BUFFER_SIZE;
	private int		requestMaxAliveSecs	 = DEFAULT_MAX_ALIVE_SECS;
	private int		requestMaxPerConn	 = DEFAULT_MAX_PIPELINED_REQUESTS;
	
	private String	defaultPage			 = DEFAULT_PAGE;
	private String	errorPage			 = DEFAULT_ERROR_PAGE;
	private String	browsePage			 = DEFAULT_BROWSE_PAGE;
	
	private String  debugFlag			 = DEFAULT_DEBUG_FLAG;
	private boolean isDebugging			 = false;

	private String  accessLogFlag		 = DEFAULT_ACCESSLOG_FLAG;
	
	private Map<String,Map<Integer,KeyPairSearchTree<String>>> 		mounts	= new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<WebAddress>>>  fwds	= new HashMap<String,Map<Integer,KeyPairSearchTree<WebAddress>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>>outs	= new HashMap<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<ChunkSpec>>>	chunks	= new HashMap<String,Map<Integer,KeyPairSearchTree<ChunkSpec>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<String>>> 		browse	= new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<String>>> 		cgimnts	= new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
	
	public enum DupPolicy { ENUMERATE, OVERWRITE }
	
	public enum DisableFlag { RANGED }
	
	private DupPolicy dupPolicy = DupPolicy.OVERWRITE;
	
	/**
	 * @return the debugFlag
	 */
	public final String getDebugFlag()
	{
		return debugFlag;
	}
	/**
	 * @param debugFlag the defaultDebugFlag to set
	 */
	public final void setDebugFlag(String debugFlag)
	{
		this.debugFlag = debugFlag;
		isDebugging=!debugFlag.equalsIgnoreCase("OFF");
	}
	/**
	 * @return the isDebugging
	 */
	public final boolean isDebugging()
	{
		return isDebugging;
	}
	
	/**
	 * @return the dupPolicy
	 */
	public final DupPolicy getDupPolicy()
	{
		return dupPolicy;
	}
	/**
	 * @param dupPolicy the dupPolicy to set
	 */
	public final void setDupPolicy(DupPolicy dupPolicy)
	{
		this.dupPolicy = dupPolicy;
	}
	/**
	 * @param dupPolicy the dupPolicy to set
	 */
	public final void setDupPolicy(String dupPolicy)
	{
		try
		{
			this.dupPolicy=DupPolicy.valueOf(dupPolicy.toUpperCase().trim());
		}
		catch(final Exception e)
		{
			this.dupPolicy = DupPolicy.OVERWRITE;
		}
	}
	
	/**
	 * @return the accessLogFlag
	 */
	public final String getAccessLogFlag()
	{
		return accessLogFlag;
	}
	/**
	 * @param accessLogFlag the accessLogFlag to set
	 */
	public final void setAccessLogFlag(String accessLogFlag)
	{
		this.accessLogFlag = accessLogFlag;
	}
	
	/**
	 * @return the defaultPage
	 */
	public final String getDefaultPage()
	{
		return defaultPage;
	}
	
	/**
	 * @return the errorPage
	 */
	public final String getErrorPage()
	{
		return errorPage;
	}
	
	/**
	 * @return the directory browsePage
	 */
	public final String getBrowsePage()
	{
		return browsePage;
	}
	
	/**
	 * @param defaultPage the defaultPage to set
	 */
	public final void setDefaultPage(String defaultPage)
	{
		this.defaultPage = defaultPage;
	}
	/**
	 * @return the fileCacheExpireMs
	 */
	public final long getFileCacheExpireMs()
	{
		return fileCacheExpireMs;
	}
	/**
	 * @param fileCacheExpireMs the fileCacheExpireMs to set
	 */
	public final void setFileCacheExpireMs(long fileCacheExpireMs)
	{
		this.fileCacheExpireMs = fileCacheExpireMs;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger()
	{
		return logger;
	}
	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}
	/**
	 * @return the fileCache
	 */
	public final FileCacheManager getFileCache()
	{
		return fileCache;
	}
	/**
	 * @param fileCache the fileCache to set
	 */
	public final void setFileCache(FileCacheManager fileCache)
	{
		this.fileCache = fileCache;
	}
	/**
	 * @return the fileManager
	 */
	public final FileManager getFileManager()
	{
		return fileManager;
	}
	/**
	 * @param fileManager the fileManager to set
	 */
	public final void setFileManager(FileManager fileManager)
	{
		this.fileManager = fileManager;
	}
	
	/**
	 * @return the fileGetter
	 */
	public HTTPFileGetter getFileGetter() 
	{
		return fileGetter;
	}
	
	/**
	 * @param fileGetter the fileGetter to set
	 */
	public void setFileGetter(HTTPFileGetter fileGetter) 
	{
		this.fileGetter = fileGetter;
	}
	
	/**
	 * @return the coffeeWebServer
	 */
	public WebServer getCoffeeWebServer() 
	{
		return coffeeWebServer;
	}
	
	/**
	 * @param coffeeWebServer the coffeeWebServer to set
	 */
	public void setCoffeeWebServer(WebServer coffeeWebServer) 
	{
		this.coffeeWebServer = coffeeWebServer;
	}
	/**
	 * @return the sessionMaxIdleMs
	 */
	public final long getSessionMaxIdleMs()
	{
		return sessionMaxIdleMs;
	}
	/**
	 * @param sessionMaxIdleMs the sessionMaxIdleMs to set
	 */
	public final void setSessionMaxIdleMs(long sessionMaxIdleMs)
	{
		this.sessionMaxIdleMs = sessionMaxIdleMs;
	}
	/**
	 * @return the sessionMaxAgeMs
	 */
	public final long getSessionMaxAgeMs()
	{
		return sessionMaxAgeMs;
	}
	/**
	 * @param sessionMaxAgeMs the sessionMaxAgeMs to set
	 */
	public final void setSessionMaxAgeMs(long sessionMaxAgeMs)
	{
		this.sessionMaxAgeMs = sessionMaxAgeMs;
	}
	/**
	 * @return the servletMan
	 */
	public final SimpleServletManager getServletMan()
	{
		return servletMan;
	}
	/**
	 * @param servletMan the servletMan to set
	 */
	public final void setServletMan(SimpleServletManager servletMan)
	{
		this.servletMan = servletMan;
	}
	/**
	 * @return the sessions
	 */
	public final ServletSessionManager getSessions()
	{
		return sessions;
	}
	/**
	 * @param sessions the sessions to set
	 */
	public final void setSessions(ServletSessionManager sessions)
	{
		this.sessions = sessions;
	}
	/**
	 * @return the converters
	 */
	public final MimeConverterManager getConverters()
	{
		return converters;
	}
	/**
	 * @param converters the converters to set
	 */
	public final void setConverters(MimeConverterManager converters)
	{
		this.converters = converters;
	}
	
	/**
	 * @return the bindAddress
	 */
	public final String getBindAddress()
	{
		return bindAddress;
	}
	
	/**
	 * @param bindAddress the bindAddress to set
	 */
	public final void setBindAddress(String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/**
	 * return the proper pair for the given host and context and port
	 * and the given map of string pairs
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper pair for the given host and context and port
	 */
	private final Pair<String,String> getContextPair(final Map<String,Map<Integer,KeyPairSearchTree<String>>> map, 
													 final String host, final int port, final String context)
	{
		Map<Integer,KeyPairSearchTree<String>> portMap=map.get(host);
		if(portMap != null)
		{
			KeyPairSearchTree<String> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,String> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,String> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
		}
		portMap=map.get(ALL_HOSTS);
		if(portMap != null)
		{
			KeyPairSearchTree<String> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,String> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,String> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
		}
		return null;
	}

	/**
	 * return the proper mount for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper mount for the given host and context and port
	 */
	public final Pair<String,String> getMount(final String host, final int port, final String context)
	{
		return this.getContextPair(mounts, host, port, context);
	}

	/**
	 * return the proper cgi-enabled mount for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper mount for the given host and context and port
	 */
	public final Pair<String,String> getCGIMount(final String host, final int port, final String context)
	{
		return this.getContextPair(cgimnts, host, port, context);
	}

	/**
	 * return the proper browse code for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper browse code for the given host and context and port
	 */
	public final String getBrowseCode(final String host, final int port, final String context)
	{
		final Pair<String,String> p = this.getContextPair(browse, host, port, context);
		if(p == null)
			return null;
		return p.second;
	}

	/**
	 * return the proper forward for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper forward for the given host and context and port
	 */
	public final Pair<String,WebAddress> getPortForward(final String host, final int port, final String context)
	{
		Map<Integer,KeyPairSearchTree<WebAddress>> portMap=fwds.get(host);
		if(portMap != null)
		{
			KeyPairSearchTree<WebAddress> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,WebAddress> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,WebAddress> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
		}
		portMap=fwds.get(ALL_HOSTS);
		if(portMap != null)
		{
			KeyPairSearchTree<WebAddress> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,WebAddress> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,WebAddress> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair;
			}
		}
		return null;
	}
	
	/**
	 * return the chunked encoding for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the chunk for the given host and context and port
	 */
	public final ChunkSpec getChunkSpec(final String host, final int port, final String context)
	{
		Map<Integer,KeyPairSearchTree<ChunkSpec>> portMap=chunks.get(host);
		if(portMap != null)
		{
			KeyPairSearchTree<ChunkSpec> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,ChunkSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,ChunkSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
		}
		portMap=chunks.get(ALL_HOSTS);
		if(portMap != null)
		{
			KeyPairSearchTree<ChunkSpec> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,ChunkSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,ChunkSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
		}
		return null;
	}
	
	/**
	 * return the throttle bytes, in or out, for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the throttle bytes, in or out
	 */
	private final ThrottleSpec getThrottleBytes(final String host, final int port, final String context,
			Map<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>> spec)
	{
		Map<Integer,KeyPairSearchTree<ThrottleSpec>> portMap=spec.get(host);
		if(portMap != null)
		{
			KeyPairSearchTree<ThrottleSpec> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,ThrottleSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,ThrottleSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
		}
		portMap=spec.get(ALL_HOSTS);
		if(portMap != null)
		{
			KeyPairSearchTree<ThrottleSpec> contexts=portMap.get(Integer.valueOf(port));
			if(contexts != null)
			{
				final Pair<String,ThrottleSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
			contexts=portMap.get(ALL_PORTS);
			if(contexts != null)
			{
				final Pair<String,ThrottleSpec> pair=contexts.findLongestValue(context);
				if(pair != null)
					return pair.second;
			}
		}
		return null;
	}
	
	/**
	 * return the response (out) throttle bytes spec, for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the throttle bytes, in or out
	 */
	public final ThrottleSpec getResponseThrottle(final String host, final int port, final String context)
	{
		return getThrottleBytes(host,port,context,outs);
	}
	
	/**
	 * @return the sslKeystorePath
	 */
	public final String getSslKeystorePath()
	{
		return sslKeystorePath;
	}
	/**
	 * @param sslKeystorePath the sslKeystorePath to set
	 */
	public final void setSslKeystorePath(String sslKeystorePath)
	{
		this.sslKeystorePath = sslKeystorePath;
	}
	/**
	 * @return the sslKeystorePassword
	 */
	public final String getSslKeystorePassword()
	{
		return sslKeystorePassword;
	}
	/**
	 * @param sslKeystorePassword the sslKeystorePassword to set
	 */
	public final void setSslKeystorePassword(String sslKeystorePassword)
	{
		this.sslKeystorePassword = sslKeystorePassword;
	}
	/**
	 * @return the sslKeystoreType
	 */
	public final String getSslKeystoreType()
	{
		return sslKeystoreType;
	}
	/**
	 * @param sslKeystoreType the sslKeystoreType to set
	 */
	public final void setSslKeystoreType(String sslKeystoreType)
	{
		this.sslKeystoreType = sslKeystoreType;
	}
	/**
	 * @return the sslKeyManagerEncoding
	 */
	public final String getSslKeyManagerEncoding()
	{
		return sslKeyManagerEncoding;
	}
	/**
	 * @param sslKeyManagerEncoding the sslKeyManagerEncoding to set
	 */
	public final void setSslKeyManagerEncoding(String sslKeyManagerEncoding)
	{
		this.sslKeyManagerEncoding = sslKeyManagerEncoding;
	}
	/**
	 * @return the httpListenPorts
	 */
	public final int[] getHttpListenPorts()
	{
		return httpListenPorts;
	}
	/**
	 * @param httpListenPorts the httpListenPorts to set
	 */
	public final void setHttpListenPorts(int[] httpListenPorts)
	{
		this.httpListenPorts = httpListenPorts;
	}
	/**
	 * @return the httpsListenPorts
	 */
	public final int[] getHttpsListenPorts()
	{
		return httpsListenPorts;
	}
	/**
	 * @param httpsListenPorts the httpsListenPorts to set
	 */
	public final void setHttpsListenPorts(int[] httpsListenPorts)
	{
		this.httpsListenPorts = httpsListenPorts;
	}
	/**
	 * @return the coreThreadPoolSize
	 */
	public final int getCoreThreadPoolSize()
	{
		return coreThreadPoolSize;
	}
	/**
	 * @param coreThreadPoolSize the coreThreadPoolSize to set
	 */
	public final void setCoreThreadPoolSize(int coreThreadPoolSize)
	{
		this.coreThreadPoolSize = coreThreadPoolSize;
	}
	/**
	 * @return the maxThreadPoolSize
	 */
	public final int getMaxThreadPoolSize()
	{
		return maxThreadPoolSize;
	}
	/**
	 * @param maxThreadPoolSize the maxThreadPoolSize to set
	 */
	public final void setMaxThreadPoolSize(int maxThreadPoolSize)
	{
		this.maxThreadPoolSize = maxThreadPoolSize;
	}
	/**
	 * @return the maxThreadIdleMs
	 */
	public final int getMaxThreadIdleMs()
	{
		return maxThreadIdleMs;
	}
	/**
	 * @param maxThreadIdleMs the maxThreadIdleMs to set
	 */
	public final void setMaxThreadIdleMs(int maxThreadIdleMs)
	{
		this.maxThreadIdleMs = maxThreadIdleMs;
	}
	/**
	 * @return the maxThreadTimeoutSecs
	 */
	public final int getMaxThreadTimeoutSecs()
	{
		return maxThreadTimeoutSecs;
	}
	/**
	 * @param maxThreadTimeoutSecs the maxThreadTimeoutSecs to set
	 */
	public final void setMaxThreadTimeoutSecs(int maxThreadTimeoutSecs)
	{
		this.maxThreadTimeoutSecs = maxThreadTimeoutSecs;
	}
	/**
	 * @return the maxThreadQueueSize
	 */
	public final int getMaxThreadQueueSize()
	{
		return maxThreadQueueSize;
	}
	/**
	 * @param maxThreadQueueSize the maxThreadQueueSize to set
	 */
	public final void setMaxThreadQueueSize(int maxThreadQueueSize)
	{
		this.maxThreadQueueSize = maxThreadQueueSize;
	}
	
	/**
	 * @param flag the flag to check
	 * @return true if its disabled, false otherwise
	 */
	public final boolean isDisabled(DisableFlag flag)
	{
		return (flag != null) && disableFlags.contains(flag);
	}
	
	/**
	 * @return the disable flags
	 */
	public Set<DisableFlag> getDisableFlags()
	{
		return disableFlags;
	}
	
	/**
	 * @return the fileCacheMaxBytes
	 */
	public final long getFileCacheMaxBytes()
	{
		return fileCacheMaxBytes;
	}
	/**
	 * @param fileCacheMaxBytes the fileCacheMaxBytes to set
	 */
	public final void setFileCacheMaxBytes(long fileCacheMaxBytes)
	{
		this.fileCacheMaxBytes = fileCacheMaxBytes;
	}
	/**
	 * @return the requestMaxBodyBytes
	 */
	public final long getRequestMaxBodyBytes()
	{
		return requestMaxBodyBytes;
	}
	/**
	 * @return the fileCacheMaxFileBytes
	 */
	public long getFileCacheMaxFileBytes()
	{
		return fileCacheMaxFileBytes;
	}
	/**
	 * @param fileCacheMaxFileBytes the fileCacheMaxFileBytes to set
	 */
	public void setFileCacheMaxFileBytes(long fileCacheMaxFileBytes)
	{
		this.fileCacheMaxFileBytes = fileCacheMaxFileBytes;
	}
	/**
	 * @return the fileCompMaxFileBytes
	 */
	public long getFileCompMaxFileBytes()
	{
		return fileCompMaxFileBytes;
	}
	/**
	 * @param fileCompMaxFileBytes the fileCompMaxFileBytes to set
	 */
	public void setFileCompMaxFileBytes(long fileCompMaxFileBytes)
	{
		this.fileCompMaxFileBytes = fileCompMaxFileBytes;
	}
	/**
	 * @param requestMaxBodyBytes the requestMaxBodyBytes to set
	 */
	public final void setRequestMaxBodyBytes(long requestMaxBodyBytes)
	{
		this.requestMaxBodyBytes = requestMaxBodyBytes;
	}
	/**
	 * @return the requestMaxIdleMs
	 */
	public final long getRequestMaxIdleMs()
	{
		return requestMaxIdleMs;
	}
	/**
	 * @param requestMaxIdleMs the requestMaxIdleMs to set
	 */
	public final void setRequestMaxIdleMs(long requestMaxIdleMs)
	{
		this.requestMaxIdleMs = requestMaxIdleMs;
	}
	/**
	 * @return the requestLineBufBytes
	 */
	public final long getRequestLineBufBytes()
	{
		return requestLineBufBytes;
	}
	/**
	 * @param requestLineBufBytes the requestLineBufBytes to set
	 */
	public final void setRequestLineBufBytes(long requestLineBufBytes)
	{
		this.requestLineBufBytes = requestLineBufBytes;
	}
	/**
	 * @return the requestMaxAliveSecs
	 */
	public final int getRequestMaxAliveSecs()
	{
		return requestMaxAliveSecs;
	}
	/**
	 * @param requestMaxAliveSecs the requestMaxAliveSecs to set
	 */
	public final void setRequestMaxAliveSecs(int requestMaxAliveSecs)
	{
		this.requestMaxAliveSecs = requestMaxAliveSecs;
	}
	/**
	 * @return the requestMaxPerConn
	 */
	public final int getRequestMaxPerConn()
	{
		return requestMaxPerConn;
	}
	
	/**
	 * @param requestMaxPerConn the requestMaxPerConn to set
	 */
	public final void setRequestMaxPerConn(int requestMaxPerConn)
	{
		this.requestMaxPerConn = requestMaxPerConn;
	}
	
	/**
	 * @return the servlets
	 */
	public final Map<String, String> getServlets()
	{
		return servlets;
	}
	
	/**
	 * @return the fileConverts
	 */
	public final Map<String, String> getFileConverts()
	{
		return fileConverts;
	}

	/**
	 * Get a property as an integer
	 * @param props the props to look in
	 * @param propName the name of the prop
	 * @param defaultVal what to return if its not there, or isn't an int
	 * @return 
	 */
	private int getInt(final Properties props, final String propName, final int defaultVal)
	{
		try
		{
			return Integer.parseInt(getString(props,propName,""));
		}
		catch(final Exception e)
		{
			return defaultVal;
		}
	}

	/**
	 * Get a property as an long
	 * @param props the props to look in
	 * @param propName the name of the prop
	 * @param defaultVal what to return if its not there, or isn't an long
	 * @return 
	 */
	private long getLong(final Properties props, final String propName, final long defaultVal)
	{
		try
		{
			return Long.parseLong(getString(props,propName,""));
		}
		catch(final Exception e)
		{
			return defaultVal;
		}
	}

	/**
	 * Get a property as a String
	 * @param props the props to look in
	 * @param propName the name of the prop
	 * @param defaultVal what to return if its not there
	 * @return 
	 */
	private String getString(final Properties props, final String propName, final String defaultVal)
	{
		if(props.containsKey(propName))
		{
			return ((String)props.get(propName)).trim();
		}
		return defaultVal;
	}
	
	/**
	 * Returns a copy of this configuration
	 * @return a copy of this object
	 */
	public CWConfig copyOf()
	{
		try
		{
			return (CWConfig)clone();
		}
		catch(final Exception e)
		{
			return this;
		}
	}

	/**
	 * Get integer ports from a comma-delimited list
	 * @param props the properties
	 * @param portListPropName the port list property name
	 * @param defaultPorts the default to use when none found
	 * @return the new ports
	 */
	private int[] getPorts(Properties props, String portListPropName, int[] defaultPorts)
	{
		if(props.containsKey(portListPropName))
		{
			final StringBuilder str=new StringBuilder("");
			for(int i=0;i<defaultPorts.length;i++)
			{
				if(i>0) str.append(",");
				str.append(defaultPorts[0]);
			}
			final String[] prop=getString(props,portListPropName,str.toString()).split(",");
			int numPorts=0;
			defaultPorts=new int[prop.length];
			for (final String element : prop)
				try
				{
					defaultPorts[numPorts]=Integer.parseInt(element.trim());
					numPorts++;
				}catch(final Exception e) {}
			defaultPorts=Arrays.copyOf(defaultPorts, numPorts);
		}
		return defaultPorts;
	}

	/**
	 * Returns property keypairs, assuming any exist.  They are formatted with a prefix and a separator
	 * followed by the first being mounted, which is equal to the value.
	 * @param props the properties
	 * @param prefix the prefix that is shared amongst all entries
	 * @return a map of all keypairs found
	 */
	private Map<String,String> getPrefixedPairs(Properties props, String prefix, char separatorChar)
	{
		Map<String,String> newMounts=null;
		for(final Object p : props.keySet())
		{
			if((p instanceof String)
			&&((String)p).toUpperCase().startsWith(prefix+separatorChar))
			{
				if(newMounts==null) newMounts = new HashMap<String,String>();
				final String key=(String)p;
				final String value=props.getProperty(key);
				final String mountPoint=key.substring(prefix.length()+1);
				newMounts.put(mountPoint,value.trim());
			}
		}
		return newMounts;
	}
	
	/**
	 * Parses a url host:port/context into its constituent parts and returns them
	 * @param value the url
	 * @return the pieces
	 */
	private Triad<String,Integer,String> findHostPortContext(String value)
	{
		int portDex=value.indexOf(':');
		if(portDex<0)
			return new Triad<String,Integer,String>("",ALL_PORTS,"/"+value);
		final int slashDex=value.indexOf('/');
		Integer port=ALL_PORTS;
		final String context;
		if(slashDex>0)
		{
			if(slashDex==value.length()-1)
				context="/";
			else
				context=value.substring(slashDex);
			value=value.substring(0, slashDex);
		}
		else
			context="/";
		portDex=value.indexOf(':');
		if(portDex>0)
		{
			final String possPort=value.substring(portDex+1);
			if(possPort.equals("*"))
				port=ALL_PORTS;
			else
			{
				try
				{
					port=Integer.valueOf(possPort);
				}
				catch(final NumberFormatException ne)
				{
					if(logger != null)
						logger.severe("Illegal port in forward address: "+value);
					return null;
				}
			}
			value=value.substring(0, portDex);
			if(value.equals("*"))
				value=ALL_HOSTS;
		}
		else
		if(value.equals("*"))
		{
			value=ALL_HOSTS;
			port=ALL_PORTS;
		}
		else
		{
			try
			{
				port=Integer.valueOf(value);
				value=ALL_HOSTS;
			}
			catch(final Exception e)
			{
				port=ALL_PORTS;
			}
		}
		return new Triad<String,Integer,String>(value,port,context);
	}

	/**
	 * Returns one of the named properties, even ones that don't
	 * mean anything to coffeewebserver per se.
	 * @param propName the name of the property
	 * @return the value of the property, or null if not found
	 */
	public String getMiscProp(String propName)
	{
		return miscFlags.get(propName.toUpperCase());
	}
	
	/**
	 * Parses a mime type list : file size entry
	 * @param value the mime type list, file size entry
	 * @return the pieces
	 */
	private Pair<Set<MIMEType>,Long> findMimesAndFileSizes(String value)
	{
		int sizeDex=value.indexOf(':');
		if(sizeDex<0)
			return null;
		final String mimesStr = value.substring(0,sizeDex).trim().toLowerCase();
		final String fileSize = value.substring(sizeDex+1).trim();
		final Long maxFileSize;
		try
		{
			maxFileSize=Long.valueOf(fileSize.trim());
		}
		catch(final NumberFormatException ne)
		{
			if(logger != null)
				logger.severe("Illegal file size in chunk spec: "+value);
			return null;
		}
		final Set<MIMEType> mimeTypes = new HashSet<MIMEType>();
		if((mimesStr.length()>0)&&(!mimesStr.equals("*")))
		{
			String[] typesSet = mimesStr.split(",");
			for(String type : typesSet)
			{
				MIMEType mtype = null;
				for(MIMEType m : MIMEType.All.getValues())
					if((m.name().equals(type))
					||(type.endsWith("*") && m.name().startsWith(type.substring(0,type.length()-1)))
					||(type.startsWith("*") && m.name().endsWith(type.substring(1)))
					||(type.equals(m.getType()))
					||(type.endsWith("*") && m.getType().startsWith(type.substring(0,type.length()-1)))
					||(type.startsWith("*") && m.getType().endsWith(type.substring(1))))
					{
						mtype=m;
						break;
					}
				if(mtype == null)
				{
					for(MIMEType m : MIMEType.All.getValues())
						if((m.getExt().equals(type))
						||(type.endsWith("*") && m.getExt().startsWith(type.substring(0,type.length()-1)))
						||(type.startsWith("*") && m.getExt().endsWith(type.substring(1))))
						{
							mtype=m;
							break;
						}
				}
				if(mtype == null)
				{
					if(logger != null)
						logger.severe("Illegal mime type spec in chunk spec: "+type);
				}
				else
				if(!mimeTypes.contains(mtype))
					mimeTypes.add(mtype);
			}
		}
		return new Pair<Set<MIMEType>,Long>(mimeTypes,maxFileSize);
	}
	
	/**
	 * Build throttle spec from given properties file prefix
	 * @param specCache cache of throttle specs for a given host mask
	 * @param props the main props
	 * @param prefix the throttle spec prefix, THROTTLEOUT
	 * @return null for no spec, or a new throttle spec
	 */
	private Map<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>> getThrottleBytes(final Map<String,ThrottleSpec> specCache, final Properties props, final String prefix)
	{
		final Map<String,String> newThrottles=getPrefixedPairs(props,prefix,'/');
		if(newThrottles != null)
		{
			HashMap<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>> throts=new HashMap<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>>();
			for(final Entry<String,String> p : newThrottles.entrySet())
			{
				String key=p.getKey();
				Triad<String,Integer,String> from=findHostPortContext(key);
				if(from == null) continue;
				String value=p.getValue();
				Map<Integer,KeyPairSearchTree<ThrottleSpec>> portMap=throts.get(from.first);
				if(portMap == null)
				{
					portMap=new HashMap<Integer,KeyPairSearchTree<ThrottleSpec>>();
					throts.put(from.first, portMap);
				}
				KeyPairSearchTree<ThrottleSpec> tree=portMap.get(from.second);
				if(tree == null)
				{
					tree=new KeyPairSearchTree<ThrottleSpec>();
					portMap.put(from.second, tree);
				}
				ThrottleSpec spec = (specCache != null)?specCache.get(key):null;
				if(spec == null)
				{
					long throttleBytes;
					try
					{
						throttleBytes = Long.valueOf(value).longValue();
					}
					catch(Exception e)
					{
						throttleBytes = DEFAULT_THROTTLE_BYTES;
					}
					spec = new ThrottleSpec(throttleBytes);
					if(specCache != null)
					{
						specCache.put(key, spec);
					}
				}
				
				tree.addEntry(from.third,  spec);
			}
			return throts;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String,Map<Integer,KeyPairSearchTree<String>>> getContextMap(final String prefix, final Properties props, final Class<? extends KeyPairSearchTree> treeClass)
	{
		Map<String,Map<Integer,KeyPairSearchTree<String>>> map = null;
		final Map<String,String> pairs=getPrefixedPairs(props,prefix,'/');
		if(pairs != null)
		{
			map=new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
			for(final Entry<String,String> p : pairs.entrySet())
			{
				final String key=p.getKey();
				final Triad<String,Integer,String> from=findHostPortContext(key);
				if(from == null) continue;
				Map<Integer,KeyPairSearchTree<String>> portMap=map.get(from.first);
				if(portMap == null)
				{
					portMap=new HashMap<Integer,KeyPairSearchTree<String>>();
					map.put(from.first, portMap);
				}
				KeyPairSearchTree<String> tree=portMap.get(from.second);
				if(tree == null)
				{
					try 
					{
						tree=treeClass.newInstance();
						portMap.put(from.second, tree);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
				if(tree != null)
				{
					tree.addEntry(from.third, p.getValue());
				}
			}
		}
		return map;
	}
	
	
	/**
	 * 
	 * @param props
	 */
	public void load(Properties props)
	{
		miscFlags.clear();
		for(final Object propName : props.keySet())
			miscFlags.put(propName.toString().toUpperCase(), getString(props,propName.toString(),""));
		sslKeystorePath=getString(props,"SSLKEYSTOREPATH",sslKeystorePath);
		sslKeystorePassword=getString(props,"SSLKEYSTOREPASSWORD",sslKeystorePassword);
		sslKeystoreType=getString(props,"SSLKEYSTORETYPE",sslKeystoreType);
		sslKeyManagerEncoding=getString(props,"SSLKEYMANAGERENCODING",sslKeyManagerEncoding);
		
		httpListenPorts=getPorts(props,"PORT",httpListenPorts);
		httpsListenPorts=getPorts(props,"SSLPORT",httpsListenPorts);
		bindAddress=getString(props,"BIND",null);
		
		coreThreadPoolSize=getInt(props,"CORETHREADPOOLSIZE",coreThreadPoolSize);
		maxThreadPoolSize=getInt(props,"MAXTHREADS",maxThreadPoolSize);
		maxThreadIdleMs=getInt(props,"MAXTHREADIDLEMILLIS",maxThreadIdleMs);
		maxThreadQueueSize=getInt(props,"MAXTHREADQUEUESIZE",maxThreadIdleMs);
		if(!props.containsKey("MAXTHREADTIMEOUTSECS") && props.containsKey("REQUESTTIMEOUTMINS"))
			maxThreadTimeoutSecs=getInt(props,"REQUESTTIMEOUTMINS",1)*60;
		else
			maxThreadTimeoutSecs=getInt(props,"MAXTHREADTIMEOUTSECS",maxThreadTimeoutSecs);
		fileCacheExpireMs=getLong(props,"FILECACHEEXPIREMS",fileCacheExpireMs);
		fileCacheMaxBytes=getLong(props,"FILECACHEMAXBYTES",fileCacheMaxBytes);
		fileCacheMaxFileBytes=getLong(props,"FILECACHEMAXFILEBYTES",fileCacheMaxFileBytes);
		fileCompMaxFileBytes=getLong(props,"FILECOMPMAXBYTES",fileCompMaxFileBytes);
		
		requestMaxBodyBytes=getLong(props,"REQUESTMAXBODYBYTES",requestMaxBodyBytes);
		requestMaxIdleMs=getLong(props,"REQUESTMAXIDLEMS",requestMaxIdleMs);
		requestLineBufBytes=getLong(props,"REQUESTLINEBUFBYTES",requestLineBufBytes);
		if(!props.containsKey("REQUESTMAXALIVESECS") && props.containsKey("REQUESTTIMEOUTMINS"))
			requestMaxAliveSecs=getInt(props,"REQUESTTIMEOUTMINS",1)*60;
		else
			requestMaxAliveSecs=getInt(props,"REQUESTMAXALIVESECS",requestMaxAliveSecs);
		requestMaxPerConn=getInt(props,"REQUESTMAXPERCONN",requestMaxPerConn);
		defaultPage=getString(props,"DEFAULTPAGE",defaultPage);
		errorPage=getString(props,"ERRORPAGE",errorPage);
		browsePage=getString(props,"BROWSEPAGE",browsePage);
		setDebugFlag(getString(props,"DEBUGFLAG",debugFlag));
		setDupPolicy(getString(props,"DUPPOLICY",dupPolicy.toString()));
		setAccessLogFlag(getString(props,"ACCESSLOGS",accessLogFlag));
		
		final String[] disableStrs=getString(props,"DISABLE","").split(",");
		disableFlags.clear();
		if((disableStrs.length>0)&&(disableStrs[0].trim().length()>0))
		for(String disable : disableStrs)
		{
			disable=disable.toUpperCase().trim();
			try 
			{
				disableFlags.add(DisableFlag.valueOf(disable));
			} 
			catch(final Exception e)
			{
				getLogger().severe("Unknown DISABLE flag in coffeeweb.ini: "+disable);
			}
		}
		
		final Map<String,String> newServlets=getPrefixedPairs(props,"SERVLET",'/');
		if(newServlets != null)
			servlets=newServlets;
		final Map<String,String> newConverts=getPrefixedPairs(props,"MIMECONVERT",'.');
		if(newConverts != null)
			fileConverts=newConverts;
		final Map<String,Map<Integer,KeyPairSearchTree<String>>> newMounts = getContextMap("MOUNT",props,KeyPairSearchTree.class);
		if(newMounts != null)
			mounts = newMounts;
		final Map<String,Map<Integer,KeyPairSearchTree<String>>> newCGIMounts = getContextMap("CGIMOUNT",props,KeyPairWildSearchTree.class);
		if(newCGIMounts != null)
			cgimnts = newCGIMounts;

		final Map<String,Map<Integer,KeyPairSearchTree<String>>> newBrowse = getContextMap("BROWSE",props,KeyPairSearchTree.class);
		if(newBrowse != null)
			browse = newBrowse;

		final Map<String,String> extraMimeTypes=getPrefixedPairs(props,"MIME",'.');
		if(extraMimeTypes != null)
			for(String key : extraMimeTypes.keySet())
			{
				final String type=extraMimeTypes.get(key);
				if(type.indexOf('/')>0)
					MIMEType.All.addMIMEType(key.toLowerCase(), type);
			}

		final Map<String,String> newForwards=getPrefixedPairs(props,"FORWARD",'/');
		if(newForwards != null)
		{
			fwds=new HashMap<String,Map<Integer,KeyPairSearchTree<WebAddress>>>();
			for(final Entry<String,String> p : newForwards.entrySet())
			{
				String key=p.getKey();
				Triad<String,Integer,String> from=findHostPortContext(key);
				if(from == null) continue;
				String value=p.getValue();
				Triad<String,Integer,String> to=findHostPortContext(value);
				if(to == null) continue;
				if(to.second==ALL_PORTS)
					to.second=Integer.valueOf(DEFAULT_HTP_LISTEN_PORT);
				Map<Integer,KeyPairSearchTree<WebAddress>> portMap=fwds.get(from.first);
				if(portMap == null)
				{
					portMap=new HashMap<Integer,KeyPairSearchTree<WebAddress>>();
					fwds.put(from.first, portMap);
				}
				KeyPairSearchTree<WebAddress> tree=portMap.get(from.second);
				if(tree == null)
				{
					tree=new KeyPairSearchTree<WebAddress>();
					portMap.put(from.second, tree);
				}
				try
				{
					tree.addEntry(from.third,  new WebAddress(to.first,to.second.intValue(),to.third));
				}
				catch(UnknownHostException ue)
				{
					getLogger().severe("Unresolved host in forward address: "+value);
					continue;
				}
			}
		}
		
		final Map<String,ThrottleSpec> specCache = new HashMap<String, ThrottleSpec>();
		final Map<String,Map<Integer,KeyPairSearchTree<ThrottleSpec>>> throttleOutSpec = this.getThrottleBytes(specCache, props, "THROTTLEOUTPUT");
		if(throttleOutSpec != null)
		{
			outs = throttleOutSpec;
		}
		
		final int chunkSize = getInt(props,"CHUNKSIZE",0);
		final Map<String,String> newChunks=getPrefixedPairs(props,"CHUNKALLOW",'/');
		if((newChunks != null) && (chunkSize > 0))
		{
			chunks=new HashMap<String,Map<Integer,KeyPairSearchTree<ChunkSpec>>>();
			for(final Entry<String,String> p : newChunks.entrySet())
			{
				String key=p.getKey();
				Triad<String,Integer,String> from=findHostPortContext(key);
				if(from == null) continue;
				String value=p.getValue();
				Pair<Set<MIMEType>,Long> to=findMimesAndFileSizes(value);
				if(to == null) continue;
				Map<Integer,KeyPairSearchTree<ChunkSpec>> portMap=chunks.get(from.first);
				if(portMap == null)
				{
					portMap=new HashMap<Integer,KeyPairSearchTree<ChunkSpec>>();
					chunks.put(from.first, portMap);
				}
				KeyPairSearchTree<ChunkSpec> tree=portMap.get(from.second);
				if(tree == null)
				{
					tree=new KeyPairSearchTree<ChunkSpec>();
					portMap.put(from.second, tree);
				}
				tree.addEntry(from.third,  new ChunkSpec(chunkSize,to.first,to.second.longValue()));
			}
		}
		
	}
}
