/**
 * imaginary.net.Interactive
 * Copyright (c) 1996 George Reese
 * The Imaginary interactive implementation
 * of interactive connections.
 */

package com.planet_ink.coffee_mud.system.I3.net;

import com.planet_ink.coffee_mud.system.I3.server.ServerUser;
import com.planet_ink.coffee_mud.system.I3.server.Server;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

/**
 * This class provides an implementation of the Imaginary server
 * interactive module.  It is responsible for handling the login
 * of an individual user and processing its input as directed
 * by the server.<BR>
 * Created: 27 September 1996<BR>
 * Last modified: 27 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public abstract class Interactive implements ServerUser {
    /**
     * Given a user name, this method will build a unique
     * key.  This unique key has nothing to do with the
     * unique object id.  The idea behind a key name is to
     * ensure that you do not end up with a user named
     * Descartes and a user named Des Cartes.  It removes
     * all non-alphabetic characters and makes the string
     * lower case.
     * @exception InvalidNameException thrown if the name produces an unuseable key
     * @param nom the visual name to create a key from
     */
    static public String createKeyName(String nom) throws InvalidNameException {
        StringBuffer buff = new StringBuffer(nom.toLowerCase());
        String key = new String();
        int i;

        for(i=0; i<buff.length(); i++) {
            char c = buff.charAt(i);

            if( c >= 'a' && c <= 'z' ) {
                key = key + c;
            }
            else if( c != '\'' && c != '-' && c != ' ' ) {
                throw new InvalidNameException(c + " is an invalid character for names.");
            }
        }
        if( key.length() < 3 ) {
            throw new InvalidNameException("Your name must have at least three alphabetic characters.");
        }
        else if( key.length() > 29 ) {
            throw new InvalidNameException("Your name is too long.");
        }
        return key;
    }

    /**
     * Given a user name, this method will find the Interactive
     * instance associated with that user name.  If no such user is
     * currently logged in, this method will return null.
     * @param nom the name of the desired user
     * @return the Interactive object for the specified name or null if no such user exists
     */
    static public Interactive findUser(String nom) {
        ServerUser[] users = Server.getInteractives();
        int i;

        try {
            nom = createKeyName(nom);
        }
        catch( InvalidNameException e ) {
            return null;
        }
        for(i=0; i<users.length; i++) {
            Interactive user = (Interactive)users[i];

            if( user.getKeyName().equals(nom) ) {
                return user;
            }
        }
        return null;
    }

    private InteractiveBody body;
    private Date            current_login_time;
    private boolean         destructed;
    private String          display_name;
    private String          email;
    private InputThread     input_thread;
    private String          key_name;
    private Date            last_command_time;
    private String          last_login_site;
    private Date            last_login_time;
    private String          object_id;
    private PrintStream     output_stream;
    private String          password;
    private String          real_name;
    private Vector          redirect;
    private Socket          socket;

    /**
     * Constructs a new interactive object and initializes
     * its data.
     */
    public Interactive() {
        super();
        destructed = false;
        input_thread = null;
        object_id = null;
        output_stream = null;
        redirect = new Vector();
    }

    /**
     * Implementation of the ServerUser connect method.
     * A mudlib will want to display a welcome screen
     * and ask for a user name by extending this method.
     * Here, the login time is set.
     */
    public synchronized void connect() {
        current_login_time = new Date();
        last_command_time = new Date();
    }

    /**
     * Stops any running I/O threads for this interactive, closes the
     * user socket, and marks the object for destruction according to
     * the requirements of the ServerObject interface.
     * @see imaginary.server.ServerObject#getDestructed
     */
    public synchronized void destruct() {
        output_stream.flush();
        if( input_thread != null ) {
            input_thread.stop();
            input_thread = null;
        }
        try {
            socket.close();
        }
        catch( java.io.IOException e ) {
            e.printStackTrace();
        }
        destructed = true;
    }

    /**
     * Called whenever a command is pulled off the incoming
     * command stack.  If there is an instance of the Input
     * class to which input is supposed to be redirected,
     * then the command is sent there.  Otherwise it is sent
     * to the parser.  Muds wishing to implement their own
     * parser system should
     * @param cmd the command to be executed
     * @see #processInput
     */
    protected synchronized void input(String cmd) {
        Input ob = null;

        if( redirect.size() > 0 ) {
            ob = (Input)redirect.elementAt(0);
            redirect.removeElementAt(0);
        }
        if( ob != null ) {
            ob.input(this, cmd);
        }
        else if( body != null ) {
            body.executeCommand(cmd);
        }
        if( redirect.size() < 1 ) {
            sendMessage(getPrompt(), true);
        }
    }

    /**
     * This method is triggered by the input thread when it detects
     * that the user has lost their link.  It will tell the body object
     * that the link is lost, then destruct itself.
     */
    protected void loseLink() {
        socket = null;
        if( body != null ) {
            body.loseLink();
        }
        destruct();
    }

    /**
     * Does event handling for the user object.  Each
     * server cycle, the server triggers this method.  If
     * the user has periodic events which occur to it,
     * the event processor will flag that the event() method
     * should be called.
     * @see imaginary.server.ServerObject#processEvent
     */
    public void processEvent() {
    }

    /**
     * The server triggers this method once each server cycle to see
     * if the user has any input waiting to be processed.  This method
     * checks the input queue.  If there is input waiting, it updates
     * the last command time and calls the input() method with the
     * waiting command.  Otherwise it simply returns.
     * @see #input
     * @see imaginary.server.ServerUser#processInput
     * @see imaginary.server.ServerThread#run
     */
    public synchronized final void processInput() {
        if( input_thread != null ) {
            String msg = input_thread.nextMessage();

            if( msg != null ) {
                last_command_time = new java.util.Date();
                input(msg);
            }
        }
    }

    /**
     * Redirects user input to the input object passed to it.
     * This will create a LIFO chain of input redirection.  For
     * example, if I have my input currently redirected to a
     * mud created editor, then I wish to get help from inside
     * the editor, my next input will be directed to the help
     * prompt.  If I enter something at that point with no further
     * input redirection, my next input will then go back to the
     * editor.
     * @param ob the instance of imaginary.net.Input to which input will be redirected
     * @see imaginary.net.Input
     * @see #input
     */
    public synchronized final void redirectInput(Input ob) {
        redirect.addElement(ob);
    }

    /**
     * Sends a message across to the client with a newline appended
     * to the message.
     * @param msg the message to send to the client machine
     */
    public final void sendMessage(String msg) {
        if( socket == null ) {
            return;
        }
        sendMessage(msg, false);
    }

    /**
     * Sends a message across to the client.  It will append
     * nowrap is true, no newline will be appended.
     * @param msg the message to send to the client
     * @param nowrap if true, no newline is attached
     */
    public final void sendMessage(String msg, boolean nowrap) {
        if( !nowrap ) {
            msg += "\n";
        }
        output_stream.print(msg);
        output_stream.flush();
    }

    /**
     * Validates a user password against a random string.
     * @return true if the two passwords match
     */
    public final boolean validatePassword(String other) {
        return other.equals(password);
    }

    /**
     * Provides the address from which this user is connected.
     * @return the host name for this user's current site
     */
    public final String getAddressName() {
        return socket.getInetAddress().getHostName();
    }

    /**
     * Provides the body to which this user is connected.
     * @return the body to which this user is connected, or null if no body exists
     */
    public final InteractiveBody getBody() {
        return body;
    }

    /**
     * Sets the body to which this interactive connection
     * is connected.  Any mudlib using this system for
     * interactive management must implement the InteractiveBody
     * interface for any body to be used by a user.
     * @param ob the body to which this interactive is being connected
     * @see imaginary.net.InteractiveBody
     */
    public void setBody(InteractiveBody ob) {
        body = ob;
    }

    /**
     * Provides the time at which the user logged in for this session
     * @return the time of login for the current session
     */
    public final Date getCurrentLoginTime() {
        return current_login_time;
    }

    /**
     * Tells whether or not the user is marked for destruction.
     * @return true if the user is marked for destruction
     */
    public boolean getDestructed() {
        return destructed;
    }

    /**
     * Provides the user's name as they wish it to appear
     * with mixed capitalization, spaces, hyphens, etc.
     * @return the user's display name
     */
    public String getDisplayName() {
        return display_name;
    }

    /**
     * Sets the user's display name.  Prevents the operation
     * if the display name is not a permutation of the key
     * name.
     * @param str the new display name
     */
    public final void setDisplayName(String str) {
        try {
            if( !getKeyName().equals(Interactive.createKeyName(str)) ) {
                return;
            }
            display_name = str;
        }
        catch( InvalidNameException e ) {
            return;
        }
    }

    /**
     * Provides the user's email address
     * @return the email address for this user
     */
    public final String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address
     * @param str the new email address
     */
    public final void setEmail(String str) {
        email = str;
    }

    /**
     * Provides the number of seconds which have elapsed since the user
     * last entered a command.
     * @return the idle time in seconds
     */
    public final int getIdle() {
        return (int)(((new Date()).getTime() - last_command_time.getTime())/1000);
    }

    /**
     * Provides the key name for this user.  The key name is a
     * play on the user name to create a unique identifier for this
     * user that will always work.  For example, the following
     * command should work for a user:<BR>
     * <PRE>
     * tell descartes hi!
     * tell deScartes hi!
     * tell des cartes hi!
     * </PRE>
     * The key name thus creates a common denomenator to which a name
     * can be reduced for comparison.
     * @see #createKeyName
     */
    public final String getKeyName() {
        return key_name;
    }

    /**
     * Sets the key name during user creation.  This prevents resetting
     * of the key name.
     * @param str the key name being set
     * @see #getKeyName
     */
    protected void setKeyName(String str) {
        if( key_name != null ) {
            return;
        }
        key_name = str;
    }

    /**
     * Provides the name of the site from which the user logged in
     * at their last login.
     * @return the last login site
     */
    public final String getLastLoginSite() {
        return last_login_site;
    }

    /**
     * Sets the last login site.  Used by a subclass
     * during login.
     * @param site the last login site
     */
    public void setLastLoginSite(String site) {
        if( last_login_site != null ) {
            return;
        }
        last_login_site = site;
    }

    /**
     * Provides the time of the user's last login.
     * @return the last login time
     */
    public final Date getLastLoginTime() {
        return last_login_time;
    }

    /**
     * Used by the login process to set the last login
     * time.
     * @param time the time the user last logged in
     */
    public void setLastLoginTime(Date time) {
        if( last_login_time != null ) {
            return;
        }
        last_login_time = time;
    }

    /**
     * Gives the user object's object id.
     * @return the object id
     * @see imaginary.server.ServerObject#getObjectId
     */
    public final String getObjectId() {
        return object_id;
    }

    /**
     * Allows the server to set the object id.
     * @param id the object id assigned to this object
     * @see imaginary.server.ServerObject#setObjectId
     */
    public final void setObjectId(String id) {
        if( object_id != null ) {
            return;
        }
        object_id = id;
    }

    /**
     * Allows a subclass to get the password.
     * @return the user's password
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * @param pass the new password
     */
    protected void setPassword(String pass) {
        password = pass;
    }

    /**
     * Provides the user's command prompt.
     * @return the command prompt
     */
    public String getPrompt() {
        return "> ";
    }

    /**
     * Provides the user's real name, or null if they never entered
     * a real name.
     * @return the user's real name or null
     */
    public final String getRealName() {
        return real_name;
    }

    /**
     * Sets the user's real name.
     * @param nom the real name for the user
     */
    public void setRealName(String nom) {
        real_name = nom;
    }

    /**
     * Called by the server before connect() is called to assign
     * the socket for this Interactive to it.
     * @param s the socket for this connection
     * @see imaginary.server.ServerUser#setSocket
     */
    public final void setSocket(Socket s) throws java.io.IOException {
        socket = s;
        input_thread = new InputThread(socket, this);
        output_stream = new PrintStream(s.getOutputStream());
    }
}

/**
 * The InputThread class handles asynchronous user input and queues
 * it up to be picked up by the user synchronously.  In English,
 * the user can be entering information at any point in time
 * while the server is running.  You want, however, that a command
 * be executed in a specific order.  This class therefore stuffs commands
 * into a queue when they arrive.  When the user is ready, it pulls a
 * single command off to be executed.<BR>
 * Created: 27 September 1996
 * Last modified 27 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 * @see imaginary.net.Interactive
 */
class InputThread implements Runnable {
    private Vector                  input_buffer;
    private java.io.DataInputStream stream;
    private boolean                 destructed;
    private Thread                  thread;
    private Interactive             user;

    /**
     * Constructs and starts the thread which accepts user
     * input.  As a user enters a command, the command is
     * added to a input_buffer.  During each server cycle, the
     * Interactive object for this thread pulls off one
     * command and executes it.
     * @exception java.io.IOException thrown if no input stream can be created
     * @param s the socket connected to the user's machine
     * @param u the Interactive attached to this thread
     */
    public InputThread(Socket s, Interactive u) throws java.io.IOException {
        destructed = false;
        user = u;
        input_buffer = new Vector(10);
        stream = new java.io.DataInputStream(s.getInputStream());
        thread = new Thread(this);
        thread.start();
    }

    /**
     * As long as the user is connected, this thread accepts
     * input from the user machine.  If the user drops link,
     * this will call loseLink() in the interactive object.
     * @see imaginary.net.Interactive#loseLink
     */
    public void run() {
        while( !destructed ) {
            String msg;

            try {
                msg = stream.readLine();
            }
            catch( java.io.IOException e ) {
                synchronized( user ) {
                    user.loseLink();
                }
                return;
            }
            synchronized( this ) {
                input_buffer.addElement(msg);
            }
            try { Thread.sleep(10); }
            catch( InterruptedException e ) { }
        }
    }

    /**
     * The interactive object for this input thread will
     * call stop if the interactive is destructed for
     * any reason.
     */
    public void stop() {
        thread.stop();
        destructed = true;
    }

    protected synchronized String nextMessage() {
        String msg;

        synchronized( input_buffer ) {
            if( input_buffer.size() > 0 ) {
                msg = (String)input_buffer.elementAt(0);
                input_buffer.removeElementAt(0);
            }
            else {
                msg = null;
            }
        }
        return msg;
    }
}

