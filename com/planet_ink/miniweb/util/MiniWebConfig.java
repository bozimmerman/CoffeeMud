package com.planet_ink.miniweb.util;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import com.planet_ink.miniweb.interfaces.FileCacheManager;
import com.planet_ink.miniweb.interfaces.FileManager;
import com.planet_ink.miniweb.interfaces.HTTPFileGetter;
import com.planet_ink.miniweb.interfaces.MimeConverterManager;
import com.planet_ink.miniweb.interfaces.ServletSessionManager;
import com.planet_ink.miniweb.interfaces.SimpleServletManager;
import com.planet_ink.miniweb.server.MiniWebServer;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.Triad;

/*
Copyright 2012-2014 Bo Zimmerman

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
 * Configuration for miniwebserver
 * @author Bo Zimmerman
 *
 */
public class MiniWebConfig implements Cloneable
{
	public  static final int  	  DEFAULT_HTP_LISTEN_PORT 		= 80;
	public  static final int  	  DEFAULT_SSL_PORT 				= 443;

	private static final String   DEFAULT_PAGE 					= "index.html"; // this would normally be configurable as a list
	private static final String   ERROR_PAGE 					= "root\\errorpage.mwhtml";
	
	// configuration for the request thread pool
	private static final int      DEFAULT_CORE_THREAD_POOL_SIZE	= 1;
	private static final int      DEFAULT_MAX_THREAD_POOL_SIZE 	= 10;
	private static final int      DEFAULT_THREAD_KEEP_ALIVE_MS 	= 60 * 1000; // max age of idle threads
	private static final int      DEFAULT_THREAD_TIMEOUT_SECS		= 30; //Timeout for active request threads
	private static final int      DEFAULT_THREAD_QUEUE_SIZE		= 500;//Above this and they start getting rejected
	
	private static final long     DEFAULT_FILECACHE_EXPIRE_MS	= 5 * 60 * 1000; 		// 5 minutes -- how long a cache entry lived
	private static final long     DEFAULT_FILECACHE_MAX_BYTES	= 10 * 1024 * 1024;	// the maximum number of bytes this cache will hold total
	private static final long     DEFAULT_FILECACHE_MAX_FBYTES	= 2 * 1024 * 1024;	// the maximum size of file the cache will hold

	private static final long     DEFAULT_FILECOMP_MAX_FBYTES	= 16 * 1024 * 1024;	// the maximum size of file the cache will hold
	
	private static final long     DEFAULT_MAX_BODY_BYTES   		= 1024 * 1024 * 2; // maximum size of a request body
	private static final long     DEFAULT_MAX_IDLE_MILLIS  		= 30 * 1000;		// maximum time a request can be idle (between reads)
	private static final int 	  DEFAULT_LINE_BUFFER_SIZE		= 4096; // maximum length of a single line in the main request
	private static final int  	  DEFAULT_MAX_ALIVE_SECS 		= 15;	// maximum age, in seconds, of a request connection
	private static final int	  DEFAULT_MAX_PIPELINED_REQUESTS= 10;	// maximum number of requests per connection
	
	private static final String   DEFAULT_SSL_KEYSTORE_TYPE		= "JKS";
	private static final String   DEFAULT_SSL_KEYMANAGER_ENC	= "SunX509";
	
	private static final String   DEFAULT_DEBUG_FLAG			= "OFF";
	
	private static final String   DEFAULT_ACCESSLOG_FLAG		= "OFF";

	private static final long     DEFAULT_SESSION_IDLE_MILLIS 	= 30 * 60 * 1000;		// maximum time a session can be idle (between requests)
	private static final long     DEFAULT_SESSION_AGE_MILLIS  	= 24 *60 * 60 * 1000;	// maximum time a session can be in existence
	
	private static final Integer  ALL_PORTS						= Integer.valueOf(-1);
	private static final String   ALL_HOSTS						= "";
	
	private Map<String,String> 	  servlets 						= new HashMap<String,String>();
	private Map<String,String>    fileConverts					= new HashMap<String,String>();
	
	private Map<String,String> 	  miscFlags						= new HashMap<String,String>();
	
	private SimpleServletManager  servletMan					= null;
	private ServletSessionManager sessions						= null;
	private MimeConverterManager  converters					= null;
	private FileCacheManager      fileCache						= null;
	private Logger				  logger						= null;
	private HTTPFileGetter		  fileGetter					= null;
	private MiniWebServer		  miniWebServer					= null;
	private FileManager			  fileManager					= new MWFileManager();
	
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
	private String	errorPage			 = ERROR_PAGE;
	
	private String  debugFlag			 = DEFAULT_DEBUG_FLAG;
	private boolean isDebugging			 = false;

	private String  accessLogFlag		 = DEFAULT_ACCESSLOG_FLAG;
	
	private Map<String,Map<Integer,KeyPairSearchTree<String>>> 		mounts	= new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
	private Map<String,Map<Integer,KeyPairSearchTree<WebAddress>>>  fwds	= new HashMap<String,Map<Integer,KeyPairSearchTree<WebAddress>>>();
	
	public enum DupPolicy { ENUMERATE, OVERWRITE }
	
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
		catch(Exception e)
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
	 * @return the miniWebServer
	 */
	public MiniWebServer getMiniWebServer() 
	{
		return miniWebServer;
	}
	/**
	 * @param miniWebServer the miniWebServer to set
	 */
	public void setMiniWebServer(MiniWebServer miniWebServer) 
	{
		this.miniWebServer = miniWebServer;
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
	 * return the proper mount for the given host and context and port
	 * @param host the host name to search for, or "" for all hosts
	 * @param port the port to search for, or -1 for all ports
	 * @param context the context to search for -- NOT OPTIONAL!
	 * @return the proper mount for the given host and context and port
	 */
	public final Pair<String,String> getMount(final String host, final int port, final String context)
	{
		Map<Integer,KeyPairSearchTree<String>> portMap=mounts.get(host);
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
		portMap=mounts.get(ALL_HOSTS);
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
				Pair<String,WebAddress> pair=contexts.findLongestValue(context);
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
	public long getFileCacheMaxFileBytes() {
		return fileCacheMaxFileBytes;
	}
	/**
	 * @param fileCacheMaxFileBytes the fileCacheMaxFileBytes to set
	 */
	public void setFileCacheMaxFileBytes(long fileCacheMaxFileBytes) {
		this.fileCacheMaxFileBytes = fileCacheMaxFileBytes;
	}
	/**
	 * @return the fileCompMaxFileBytes
	 */
	public long getFileCompMaxFileBytes() {
		return fileCompMaxFileBytes;
	}
	/**
	 * @param fileCompMaxFileBytes the fileCompMaxFileBytes to set
	 */
	public void setFileCompMaxFileBytes(long fileCompMaxFileBytes) {
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
		catch(Exception e)
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
		catch(Exception e)
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
	 * 
	 * @return
	 */
	public MiniWebConfig copyOf()
	{
		try
		{
			return (MiniWebConfig)clone();
		}
		catch(Exception e)
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
			StringBuilder str=new StringBuilder("");
			for(int i=0;i<defaultPorts.length;i++)
			{
				if(i>0) str.append(",");
				str.append(defaultPorts[0]);
			}
			String[] prop=getString(props,portListPropName,str.toString()).split(",");
			int numPorts=0;
			defaultPorts=new int[prop.length];
			for(int i=0;i<prop.length;i++)
				try {
					defaultPorts[numPorts]=Integer.parseInt(prop[i].trim());
					numPorts++;
				}catch(Exception e) {}
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
		for(Object p : props.keySet())
		{
			if((p instanceof String)
			&&((String)p).toUpperCase().startsWith(prefix+separatorChar))
			{
				if(newMounts==null) newMounts = new HashMap<String,String>();
				String key=(String)p;
				String value=props.getProperty(key);
				String mountPoint=key.substring(prefix.length()+1);
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
		int slashDex=value.indexOf('/');
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
			String possPort=value.substring(portDex+1);
			if(possPort.equals("*"))
				port=ALL_PORTS;
			else
			{
				try
				{
					port=Integer.valueOf(possPort);
				}
				catch(NumberFormatException ne)
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
			catch(Exception e)
			{
				port=ALL_PORTS;
			}
		}
		return new Triad<String,Integer,String>(value,port,context);
	}

	/**
	 * Returns one of the named properties, even ones that don't
	 * mean anything to miniwebserve per se.
	 * @param propName the name of the property
	 * @return the value of the property, or null if not found
	 */
	public String getMiscProp(String propName)
	{
		return miscFlags.get(propName.toUpperCase());
	}
	
	/**
	 * 
	 * @param props
	 */
	public void load(Properties props)
	{
		miscFlags.clear();
		for(Object propName : props.keySet())
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
		setDebugFlag(getString(props,"DEBUGFLAG",debugFlag));
		setDupPolicy(getString(props,"DUPPOLICY",dupPolicy.toString()));
		setAccessLogFlag(getString(props,"ACCESSLOGS",accessLogFlag));
		
		Map<String,String> newServlets=getPrefixedPairs(props,"SERVLET",'/');
		if(newServlets != null)
			servlets=newServlets;
		Map<String,String> newConverts=getPrefixedPairs(props,"MIMECONVERT",'.');
		if(newConverts != null)
			fileConverts=newConverts;
		Map<String,String> newMounts=getPrefixedPairs(props,"MOUNT",'/');
		if(newMounts != null)
		{
			mounts=new HashMap<String,Map<Integer,KeyPairSearchTree<String>>>();
			for(Entry<String,String> p : newMounts.entrySet())
			{
				String key=p.getKey();
				Triad<String,Integer,String> from=findHostPortContext(key);
				if(from == null) continue;
				Map<Integer,KeyPairSearchTree<String>> portMap=mounts.get(from.first);
				if(portMap == null)
				{
					portMap=new HashMap<Integer,KeyPairSearchTree<String>>();
					mounts.put(from.first, portMap);
				}
				KeyPairSearchTree<String> tree=portMap.get(from.second);
				if(tree == null)
				{
					tree=new KeyPairSearchTree<String>();
					portMap.put(from.second, tree);
				}
				tree.addEntry(from.third, p.getValue());
			}
		}
		Map<String,String> newForwards=getPrefixedPairs(props,"FORWARD",'/');
		if(newForwards != null)
		{
			fwds=new HashMap<String,Map<Integer,KeyPairSearchTree<WebAddress>>>();
			for(Entry<String,String> p : newForwards.entrySet())
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
	}
}
