package com.planet_ink.miniweb.server;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.*;

import javax.net.ssl.SSLContext;

import com.planet_ink.coffee_mud.core.Log;

import com.planet_ink.miniweb.interfaces.HTTPIOHandler;
import com.planet_ink.miniweb.http.HTTPHeader;
import com.planet_ink.miniweb.http.HTTPReader;
import com.planet_ink.miniweb.http.HTTPReqProcessor;
import com.planet_ink.miniweb.http.HTTPSReader;
import com.planet_ink.miniweb.http.MWFileCache;
import com.planet_ink.miniweb.http.MWMimeConverterManager;
import com.planet_ink.miniweb.http.MWServletManager;
import com.planet_ink.miniweb.http.MWSessionManager;
import com.planet_ink.miniweb.util.MWRunWrap;
import com.planet_ink.miniweb.util.MWThread;
import com.planet_ink.miniweb.util.MWThreadExecutor;
import com.planet_ink.miniweb.util.MiniWebConfig;

/*
Copyright 2012-2013 Bo Zimmerman

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
 * Both the main() kickoff for the miniwebserver AND the main thread class for the same.
 * This class handles all the socket listening and request management/timeout.
 * When requests are received, they are passed off to an HTTPReader for processing,
 * though a separate thread may timeout those readers at its discretion and shut
 * them down.
 * @author Bo Zimmerman
 *
 */
public class MiniWebServer extends Thread
{
	public static final	String	  NAME				= "MiniWebServer";
	public static final String	  POMVERSION		= "2.0";
	public static 		double	  VERSION;
	static { try { VERSION=Double.parseDouble(POMVERSION); } catch(Exception e){ VERSION=0.0;} }
	
	private volatile boolean	  shutdownRequested	= false; // notice of external shutdown request
	private Selector			  servSelector 		= null;  // server io selector
	private MWThreadExecutor 	  executor;					 // request handler thread pool
	private Thread				  timeoutThread		= null;  // thread to timeout connected but idle channels
	private MiniWebConfig		  config;					 // list of all standard http ports to listen on
	private SSLContext	 		  sslContext;
	private String				  serverName;				 // a friendly name for this server instance
	private final LinkedList<HTTPIOHandler>  		handlers;			// list of connected channels.. managed by timeoutthread
	private final Map<ServerSocketChannel, Boolean>	servChannels; 		// map of socket channels to an SSL boolean
	private final LinkedList<Runnable>				registerOps;
	
	public MiniWebServer(String serverName, MiniWebConfig config)
	{
		super(serverName);
		this.config=config;
		this.serverName=serverName;
		
		// setup the thread pool
		handlers = new LinkedList<HTTPIOHandler>();
		executor = new MWThreadExecutor(serverName,
										config,
										config.getCoreThreadPoolSize(), config.getMaxThreadPoolSize(), config.getMaxThreadIdleMs(), 
										TimeUnit.MILLISECONDS, config.getMaxThreadTimeoutSecs(), config.getMaxThreadQueueSize());

		servChannels = new HashMap<ServerSocketChannel, Boolean>();
		registerOps = new LinkedList<Runnable>();
		
		setDaemon(true);
		
		// if we are going to be listening on ssl, generate a global ssl context to use
		if((config.getHttpsListenPorts()==null)
		||(config.getHttpsListenPorts().length==0))
			sslContext=null;
		else
			sslContext=HTTPSReader.generateNewContext(config);
	}

	/**
	 * Returns the version of this miniwebweb server
	 * @return the version
	 */
	public final String getVersion()
	{
		return Double.toString(VERSION);
	}
	
	/**
	 * Return list of threads that have timed out according to server settings.
	 * @return a collection of runnables
	 */
	public List<Runnable> getOverdueThreads()
	{
		Collection<MWRunWrap> wraps=executor.getTimeoutOutRuns(Integer.MAX_VALUE);
		Vector<Runnable> overDueV=new Vector<Runnable>(wraps.size());
		for(MWRunWrap wrap : wraps)
			overDueV.add(wrap.getRunnable());
		return overDueV;
	}

	/**
	 * Open a single web server listening sockets and
	 * register a selector for accepting connections.
	 * @param listenPort the port to listen on
	 * @throws IOException
	 */
	private void openChannel(int listenPort, Boolean isSSL) throws IOException
	{
		ServerSocketChannel servChan = ServerSocketChannel.open();
		ServerSocket serverSocket = servChan.socket();
		serverSocket.bind (new InetSocketAddress (listenPort));
		servChan.configureBlocking (false);
		servChan.register (servSelector, SelectionKey.OP_ACCEPT);
		servChannels.put(servChan, isSSL);
		config.getLogger().info("Started "+(isSSL.booleanValue()?"ssl ":"http ")+serverName+" on port "+listenPort);
	}
	
	/**
	 * Open the main web server listening sockets and
	 * register a selector for accepting connections.
	 * @throws IOException
	 */
	private void openChannels() throws IOException
	{
		servSelector = Selector.open();
		boolean portOpen=false;
		IOException lastException=null;
		
		for(int listenPort : config.getHttpListenPorts())
		{
			try
			{
				openChannel(listenPort, Boolean.FALSE);
				portOpen=true;
			}
			catch(IOException e)
			{
				lastException=e;
				config.getLogger().severe("Port "+listenPort+": "+e.getMessage());
			}
		}
		if(sslContext != null)
		{
			for(int listenPort : config.getHttpsListenPorts())
			{
				try
				{
					openChannel(listenPort, Boolean.TRUE);
					portOpen=true;
				}
				catch(IOException e)
				{
					lastException=e;
					config.getLogger().severe("Port "+listenPort+": "+e.getMessage());
				}
			}
		}
		if((!portOpen)&&(lastException!=null))
			throw lastException;
	}

	/**
	 * Initialize the timeoutthread.  It wakes up every second to look for timed out connections
	 * It does this by calling a local final method to scan the open connections.
	 * TODO: an executor thread pool that allows both scheduled and timeoutable entries would
	 * 		 have allowed us to run this process on the pool.  Since that's not supported atm...
	 */
	private void startTimeoutThread()
	{
		timeoutThread=new Thread("Timeout")
		{
			public void run()
			{
				try
				{
					config.getLogger().finer("Timeout Thread started");
					while(!shutdownRequested)
					{
						Thread.sleep(1000);
						timeOutStrayHandlers();
						config.getSessions().cleanUpSessions();
					}
				}
				catch(InterruptedException e) {}
				finally
				{
					config.getLogger().info( "Timeout Thread shutdown");
				}
			}
		};
		timeoutThread.start();
	}
	
	/**
	 * Handles a particular channel event from its given selectionkey. 
	 * So far, only accepted connections and readable keys are managed here.
	 * @param key the channel event key
	 * @throws IOException
	 */
	private void handleSelectionKey(final SelectionKey key) throws IOException
	{
		if (key.isAcceptable()) // a connection was made, so add the handler
		{
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel channel = server.accept();
			if (channel != null) 
			{
				HTTPIOHandler handler;
				if(servChannels.get(server).booleanValue())
					handler=new HTTPSReader(this, channel, sslContext);
				else
					handler=new HTTPReader(this, channel);
				channel.configureBlocking (false);
				channel.register (servSelector, SelectionKey.OP_READ, handler);
				synchronized(handlers) // synched because you can't iterate and modify, and because its a linkedlist
				{
					handlers.add(handler);
				}
			}
		}
		if(key.isReadable() // bytes were received on one of the channel .. so read!
		|| (((key.interestOps() & SelectionKey.OP_WRITE)==SelectionKey.OP_WRITE) && key.isWritable())) 
		{
			HTTPIOHandler handler = (HTTPIOHandler)key.attachment();
			//config.getLogger().finer("Read/Write: "+handler.getName());
			if(!handler.isCloseable())
			{
				key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
				executor.execute(handler);
			}
		}
		else
		if(key.attachment() instanceof HTTPIOHandler)
		{
			HTTPIOHandler handler = (HTTPIOHandler)key.attachment();
			config.getLogger().finer("Rejected handler key for "+handler.getName());
		}
	}
	
	/**
	 * Scan the list of active channel connections for any that are timed out,
	 * or otherwise need to be removed from this list.  If found, do so, and
	 * queue up for actual closing (if necc).  After the handlers are all
	 * scanned, then close any that need closing.
	 */
	private final void timeOutStrayHandlers()
	{
		List<HTTPIOHandler> handlersToShutDown = null;
		synchronized(handlers)
		{
			// remove any stray handlers from time to time
			for(Iterator<HTTPIOHandler> i = handlers.iterator(); i.hasNext(); )
			{
				HTTPIOHandler handler=i.next();
				if(handler.isCloseable())
				{
					if(handlersToShutDown == null)
					{
						handlersToShutDown = new LinkedList<HTTPIOHandler>();
					}
					handlersToShutDown.add(handler);
					i.remove();
				}
			}
		}
		if(handlersToShutDown != null)
		{
			for(HTTPIOHandler handler : handlersToShutDown)
			{
				handler.closeAndWait();
			}
		}
	}

	/**
	 * The main web server loop
	 * It blocks on its selector waiting for either accepted connections,
	 * or data to be read, which it then farms out to another thread.
	 * This is repeats until something external requests it to shut down.
	 */
	public void run()
	{
		try
		{
			openChannels(); // open the socket channel
			startTimeoutThread(); // start the channel timeout thread
		}
		catch(IOException e)
		{
			config.getLogger().throwing("", "", e); // this is also fatal
			close();
			return;
		}
		while (!shutdownRequested)
		{
			try
			{
				int n = servSelector.select();
				synchronized(registerOps)
				{
					while(!registerOps.isEmpty())
					{
						Runnable registerOp=registerOps.removeFirst();
						registerOp.run();
					}
				}
				if (n == 0) 
				{
					continue;
				}

				Iterator<SelectionKey> it = servSelector.selectedKeys().iterator();
				while (it.hasNext()) 
				{
					SelectionKey key = it.next();
					try
					{
						handleSelectionKey(key);
					}
					finally
					{
						it.remove();
					}
				}
			}
			catch(CancelledKeyException t)
			{
				// ignore
			}
			catch(IOException e)
			{
				config.getLogger().severe(e.getMessage());
			}
			catch(Exception e)
			{
				config.getLogger().throwing("","",e);
			}
		}
		close();
		config.getLogger().info("Shutdown complete");
	}

	/**
	 * Called either internally, or can be called externally to shutdown this
	 * server instance.  Closes all the channels and cleans up any stray
	 * activity.
	 */
	public void close()
	{
		
		shutdownRequested=true;
		executor.shutdown();
		try {
			servSelector.close();
		}catch(Exception e){} // ugh, why can't there be an "i don't care" exception syntax in java
		for(ServerSocketChannel servChan : servChannels.keySet())
		{
			try {
				servChan.close();
			}catch(Exception e){}
		}
		if(!executor.isShutdown())
		{
			try
			{
				executor.awaitTermination(10, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				executor.shutdownNow();
			}
		}
		if(timeoutThread != null)
		{
			timeoutThread.interrupt();
		}
		synchronized(handlers)
		{
			for(HTTPIOHandler handler : handlers)
			{
				handler.closeAndWait();
			}
			handlers.clear();
		}
	}
	
	/**
	 * Enqueue a new socket channel to be registered for read notifications.
	 * Does not do the action at once, but will, soon.
	 * @param chan the socket channel to register
	 * @param handler the handler to handle it.
	 */
	public void registerNewHandler(final SocketChannel channel, final HTTPIOHandler handler)
	{
		synchronized(this.registerOps)
		{
			final Selector servSelector=this.servSelector;
			this.registerOps.add(new Runnable(){
				@Override
				public void run() {
					try {
						channel.configureBlocking (false);
						channel.register (servSelector, SelectionKey.OP_READ, handler);
						synchronized(handlers) // synched because you can't iterate and modify, and because its a linkedlist
						{
							handlers.add(handler);
						}
					} catch (Exception e) {
						config.getLogger().throwing("", "", e);
					}
				}
			});
			servSelector.wakeup();
		}
	}

	
	/**
	 * Enqueue a new socket channel to be registered for read notifications.
	 * Does not do the action at once, but will, soon.
	 * @param chan the socket channel to register
	 * @param newOp the new operations for this channel
	 */
	public void registerChannelInterest(final SocketChannel channel, final int newOp)
	{
		synchronized(this.registerOps)
		{
			final Selector servSelector=this.servSelector;
			this.registerOps.add(new Runnable(){
				@Override
				public void run() {
					final SelectionKey key = channel.keyFor(servSelector);
					if(key != null)
					{
						key.interestOps(newOp);
					}
				}
			});
			servSelector.wakeup();
		}
	}

	/**
	 * Return the configuration for this web server instance
	 * @return the config
	 */
	public MiniWebConfig getConfig()
	{
		return config;
	}
	
	/**
	 * Create, Initialize, load, and create a web server configuration based around the given
	 * ini filename and the given java logger.
	 * @param log the java logger to use
	 * @param iniInputStream the ini data to load further settings from
	 * @return a populated configuration object to create a miniwebserver from
	 */
	public static MiniWebConfig createConfig(java.util.logging.Logger log, InputStream iniInputStream) throws IOException
	{
		final MiniWebConfig config=new MiniWebConfig();
		return initConfig(config,log,iniInputStream);
	}
	
	
	/**
	 * Initialize, load, and create a web server configuration based around the given
	 * ini filename and the given java logger.
	 * @param log the java logger to use
	 * @param iniInputStream the ini data to load further settings from
	 * @return a populated configuration object to create a miniwebserver from
	 */
	public static MiniWebConfig initConfig(final MiniWebConfig config, java.util.logging.Logger log, InputStream iniInputStream) throws IOException
	{
		config.setLogger(log);
		final Properties props=new Properties();
		props.load(iniInputStream);
		
		config.load(props);
		
		final MWServletManager servletsManager = new MWServletManager(config);
		final MWSessionManager sessionsManager = new MWSessionManager(config);
		final MWFileCache fileCacheManager = new MWFileCache(config,config.getFileManager());
		final MWMimeConverterManager mimeConverterManager = new MWMimeConverterManager(config);
		final HTTPReqProcessor fileGetter = new HTTPReqProcessor(config);
		config.setSessions(sessionsManager);
		config.setServletMan(servletsManager);
		config.setFileCache(fileCacheManager);
		config.setConverters(mimeConverterManager);
		config.setFileGetter(fileGetter);
		
		HTTPHeader.setKeepAliveHeader(HTTPHeader.KEEP_ALIVE.makeLine(
										String.format(HTTPHeader.KEEP_ALIVE_FMT, 
										Integer.valueOf(config.getRequestMaxAliveSecs()),
										Integer.valueOf(config.getRequestMaxPerConn()))));
		return config;
	}
	
	/**
	 * Good olde main.  It does nothing but initialize logging, spawn a new web server
	 * and then join its thread until it is gone.  I suppose I could just create the
	 * web server and call run() on it, but somehow this feels better.
	 * @param args As no external configuration is permitted, no args are accepted
	 */
	public static void main(String[] args)
	{
		
		Log.instance().startLogging("miniweb", 2);
		String debug="OFF";
		String iniFilename="mw.ini";
		for(String arg : args)
		{
			if(arg.startsWith("BOOT="))
				iniFilename=arg.substring(5);
		}
		
		MiniWebConfig config;
		try
		{
			config=MiniWebServer.createConfig(Log.instance(), new FileInputStream(iniFilename));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
			return; // an unhit operation, but my ide is argueing with me over it.
		}
		
		debug=config.getDebugFlag();
		
		for(String arg : args)
		{
			if(arg.equalsIgnoreCase("DEBUG"))
			{
				debug="BOTH";
				config.setDebugFlag(debug);
			}
		}
		Log.instance().configureLog(Log.Type.info, "BOTH");
		Log.instance().configureLog(Log.Type.error, "BOTH");
		Log.instance().configureLog(Log.Type.warning, "BOTH");
		Log.instance().configureLog(Log.Type.debug, debug);
		Log.instance().configureLog(Log.Type.access, config.getAccessLogFlag());
		config.getLogger().info("Starting "+NAME+" "+VERSION);
		
		final MiniWebServer server = new MiniWebServer("miniweb", config);
		config.setMiniWebServer(server);
		final Thread t = new MWThread(config, server, NAME);
		t.start();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace(System.err);
		}
	}
}
