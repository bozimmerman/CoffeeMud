package com.planet_ink.coffee_web.server;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.*;

import javax.net.ssl.SSLContext;

import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.MimeConverterManager;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPReader;
import com.planet_ink.coffee_web.http.HTTPReqProcessor;
import com.planet_ink.coffee_web.http.HTTPSReader;
import com.planet_ink.coffee_web.http.FileCache;
import com.planet_ink.coffee_web.http.MimeConverter;
import com.planet_ink.coffee_web.http.ServletManager;
import com.planet_ink.coffee_web.http.SessionManager;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_web.util.RunWrap;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWThreadExecutor;
import com.planet_ink.coffee_web.util.CWConfig;

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
 * Both the main() kickoff for the coffeewebserver AND the main thread class for the same.
 * This class handles all the socket listening and request management/timeout.
 * When requests are received, they are passed off to an HTTPReader for processing,
 * though a separate thread may timeout those readers at its discretion and shut
 * them down.
 * @author Bo Zimmerman
 *
 */
public class WebServer extends Thread
{
	public static final	String	  NAME				= "CoffeeWebServer";
	public static final String	  POMVERSION		= "2.4";
	public static 		double	  VERSION;
	static { try { VERSION=Double.parseDouble(POMVERSION); } catch(final Exception e){ VERSION=0.0;} }
	
	private volatile boolean	  	shutdownRequested	= false;// notice of external shutdown request
	private volatile String	  		lastErrorMsg		= "";	// spam prevention for error reporting
	private Selector			  	servSelector 		= null; // server io selector
	private final CWThreadExecutor 	executor;					// request handler thread pool
	private Thread				  	timeoutThread		= null; // thread to timeout connected but idle channels
	private final CWConfig			config;					 	// list of all standard http ports to listen on
	private SSLContext	 		  	sslContext;
	private final String			serverName;				 	// a friendly name for this server instance
	private final LinkedList<HTTPIOHandler>  		handlers;	// list of connected channels.. managed by timeoutthread
	private final Map<ServerSocketChannel, Boolean>	servChannels; // map of socket channels to an SSL boolean
	private final LinkedList<Runnable>				registerOps;
	
	public WebServer(String serverName, CWConfig config)
	{
		super("cweb-"+serverName);
		this.config=config;
		this.serverName=serverName;
		
		// setup the thread pool
		handlers = new LinkedList<HTTPIOHandler>();
		executor = new CWThreadExecutor(serverName,
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
	 * Returns the version of this web server
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
		final Collection<RunWrap> wraps=executor.getTimeoutOutRuns(Integer.MAX_VALUE);
		final Vector<Runnable> overDueV=new Vector<Runnable>(wraps.size());
		for(final RunWrap wrap : wraps)
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
		final ServerSocketChannel servChan = ServerSocketChannel.open();
		final ServerSocket serverSocket = servChan.socket();
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
		
		for(final int listenPort : config.getHttpListenPorts())
		{
			try
			{
				openChannel(listenPort, Boolean.FALSE);
				portOpen=true;
			}
			catch(final IOException e)
			{
				lastException=e;
				config.getLogger().severe("Port "+listenPort+": "+e.getMessage());
			}
		}
		if(sslContext != null)
		{
			for(final int listenPort : config.getHttpsListenPorts())
			{
				try
				{
					openChannel(listenPort, Boolean.TRUE);
					portOpen=true;
				}
				catch(final IOException e)
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
		timeoutThread=new Thread(Thread.currentThread().getThreadGroup(),getName()+"Timeout")
		{
			@Override
			public void run()
			{
				try
				{
					config.getLogger().finer("Timeout Thread started");
					while(!shutdownRequested)
					{
						Thread.sleep(1000);
						try
						{
							timeOutStrayHandlers();
							config.getSessions().cleanUpSessions();
						}
						catch(Exception e)
						{
							if(!lastErrorMsg.equals(e.toString()) && (e.toString()!=null))
							{
								config.getLogger().log(Level.SEVERE, e.toString(), e);
								lastErrorMsg = e.toString();
							}
							else
								config.getLogger().severe(e.toString());
						}
					}
				}
				catch(final InterruptedException e) {}
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
			final ServerSocketChannel server = (ServerSocketChannel) key.channel();
			final SocketChannel channel = server.accept();
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
			final HTTPIOHandler handler = (HTTPIOHandler)key.attachment();
			//config.getLogger().finer("Read/Write: "+handler.getName());
			try
			{
				if(!handler.isCloseable())
				{
					if(key.isValid())
					{
						try
						{
							key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
							executor.execute(handler);
						}
						catch(CancelledKeyException x)
						{
							synchronized(handlers) // synched because you can't iterate and modify, and because its a linkedlist
							{
								handlers.remove(handler);
							}
						}
					}
				}
				else
				{
					key.cancel();
					synchronized(handlers) // synched because you can't iterate and modify, and because its a linkedlist
					{
						handlers.remove(handler);
					}
				}
			}
			catch(Exception e)
			{
				config.getLogger().log(Level.SEVERE, e.getMessage(), e);
			}
		}
		else
		if(key.attachment() instanceof HTTPIOHandler)
		{
			final HTTPIOHandler handler = (HTTPIOHandler)key.attachment();
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
			if(handlers.size() == 0)
				return;
			final Iterator<HTTPIOHandler> i;
			try
			{
				i=handlers.iterator();
			}
			catch(java.lang.IndexOutOfBoundsException x)
			{
				handlers.clear();
				throw x;
			}
			for(; i.hasNext(); )
			{
				try
				{
					final HTTPIOHandler handler=i.next();
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
				catch(NullPointerException e)
				{
					try
					{
						i.remove();
					}
					catch(Exception xe){ }
				}
			}
		}
		if(handlersToShutDown != null)
		{
			for(final HTTPIOHandler handler : handlersToShutDown)
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
	@Override
	public void run()
	{
		try
		{
			openChannels(); // open the socket channel
			startTimeoutThread(); // start the channel timeout thread
		}
		catch(final IOException e)
		{
			config.getLogger().throwing("", "", e); // this is also fatal
			close();
			return;
		}
		while (!shutdownRequested)
		{
			try
			{
				final int n = servSelector.select();
				synchronized(registerOps)
				{
					while(!registerOps.isEmpty())
					{
						final Runnable registerOp=registerOps.removeFirst();
						registerOp.run();
					}
				}
				if (n == 0) 
				{
					continue;
				}

				final Iterator<SelectionKey> it = servSelector.selectedKeys().iterator();
				while (it.hasNext()) 
				{
					final SelectionKey key = it.next();
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
			catch(final CancelledKeyException t)
			{
				// ignore
			}
			catch(final IOException e)
			{
				config.getLogger().severe(e.getMessage());
			}
			catch(final Exception e)
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
		try
		{
			servSelector.close();
		}catch(final Exception e){} // ugh, why can't there be an "i don't care" exception syntax in java
		for(final ServerSocketChannel servChan : servChannels.keySet())
		{
			try
			{
				servChan.close();
			}catch(final Exception e){}
		}
		if(!executor.isShutdown())
		{
			try
			{
				executor.awaitTermination(10, TimeUnit.SECONDS);
			}
			catch (final InterruptedException e)
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
			for(final HTTPIOHandler handler : handlers)
			{
				try
				{
					handler.closeAndWait();
				}catch(final Exception e){}
			}
			handlers.clear();
		}
	}
	
	/**
	 * Enqueue a new socket channel to be registered for read notifications.
	 * Does not do the action at once, but will, soon.
	 * @param channel the socket channel to register
	 * @param handler the handler to handle it.
	 */
	public void registerNewHandler(final SocketChannel channel, final HTTPIOHandler handler)
	{
		synchronized(this.registerOps)
		{
			final Selector servSelector=this.servSelector;
			this.registerOps.add(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						channel.configureBlocking (false);
						channel.register (servSelector, SelectionKey.OP_READ, handler);
						synchronized(handlers) // synched because you can't iterate and modify, and because its a linkedlist
						{
							handlers.add(handler);
						}
					}
					catch (final Exception e)
					{
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
	 * @param channel the socket channel to register
	 * @param newOp the new operations for this channel
	 */
	public void registerChannelInterest(final SocketChannel channel, final int newOp)
	{
		synchronized(this.registerOps)
		{
			final Selector servSelector=this.servSelector;
			this.registerOps.add(new Runnable()
			{
				@Override
				public void run()
				{
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
	public CWConfig getConfig()
	{
		return config;
	}
	
	/**
	 * Create, Initialize, load, and create a web server configuration based around the given
	 * ini filename and the given java logger.
	 * @param log the java logger to use
	 * @param iniInputStream the ini data to load further settings from
	 * @return a populated configuration object to create a server from
	 */
	public static CWConfig createConfig(java.util.logging.Logger log, InputStream iniInputStream) throws IOException
	{
		final CWConfig config=new CWConfig();
		return initConfig(config,log,iniInputStream);
	}
	
	
	/**
	 * Initialize, load, and create a web server configuration based around the given
	 * ini filename and the given java logger.
	 * @param log the java logger to use
	 * @param iniInputStream the ini data to load further settings from
	 * @return a populated configuration object to create a server from
	 */
	public static CWConfig initConfig(final CWConfig config, java.util.logging.Logger log, InputStream iniInputStream) throws IOException
	{
		config.setLogger(log);
		final Properties props=new Properties();
		props.load(iniInputStream);
		
		config.load(props);
		
		final ServletManager servletsManager = new ServletManager(config);
		final SessionManager sessionsManager = new SessionManager(config);
		final FileCache fileCacheManager = new FileCache(config,config.getFileManager());
		final MimeConverterManager mimeConverterManager = new MimeConverter(config);
		final HTTPReqProcessor fileGetter = new HTTPReqProcessor(config);
		config.setSessions(sessionsManager);
		config.setServletMan(servletsManager);
		config.setFileCache(fileCacheManager);
		config.setConverters(mimeConverterManager);
		config.setFileGetter(fileGetter);
		
		HTTPHeader.Common.setKeepAliveHeader(HTTPHeader.Common.KEEP_ALIVE.makeLine(
											 String.format(HTTPHeader.Common.KEEP_ALIVE_FMT, 
											 Integer.valueOf((int)(config.getRequestMaxIdleMs()/1000)),
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
		
		Log.instance().configureLogFile("web", 2);
		String debug="OFF";
		String iniFilename="coffeeweb.ini";
		for(final String arg : args)
		{
			if(arg.startsWith("BOOT="))
				iniFilename=arg.substring(5);
		}
		
		CWConfig config;
		try
		{
			config=WebServer.createConfig(Log.instance(), new FileInputStream(iniFilename));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
			return; // an unhit operation, but my ide is argueing with me over it.
		}
		
		debug=config.getDebugFlag();
		
		for(final String arg : args)
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
		
		final WebServer server = new WebServer("server", config);
		config.setCoffeeWebServer(server);
		final Thread t = new CWThread(config, server, NAME);
		t.start();
		try
		{
			t.join();
		}
		catch(final InterruptedException e)
		{
			e.printStackTrace(System.err);
		}
	}
}
