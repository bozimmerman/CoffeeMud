var Siplet =function()
{
	this.socket = null;
	this.connected = false;
	this.VERSION_MAJOR=3.0;
	this.VERSION_MINOR=0;
};

Siplet.prototype.connectToURL = function(host, port)
{
	socket = new WebSocket("ws://"+host+":"+port+"/SipletInterface");
	socket.onmessage = this.receivedData;
	socket.onopen = this.openOccurred;
	socket.onclose = this.closeOccurred;
	socket.onerror = this.errorOccurred;
	return true;
};

Siplet.prototype.receivedData = function(event)
{
	//event.data
};

Siplet.prototype.closeOccurred = function(event)
{
	this.connected = false;
};

Siplet.prototype.errorOccurred = function(event)
{
	this.connected = false;
};

Siplet.prototype.openOccurred = function(event)
{
	this.connected = true;
};

Siplet.prototype.disconnectFromURL = function()
{
	if(this.connected === true)
	{
		socket.close();
	}
};

Siplet.prototype.isConnectedToURL = function()
{
	return this.connected;
};

Siplet.prototype.readURLData = function()
{
	
};

Siplet.prototype.getURLData = function()
{
	
};

Siplet.prototype.getJScriptCommands = function()
{
	
};

Siplet.prototype.sendData = function(s)
{
	if(connected === true)
	{
	}
};

Siplet.prototype.info = function()
{
	return "Siplet V"+this.VERSION_MAJOR+"."+this.VERSION_MINOR+" (C)2005-2015 Bo Zimmerman";
};



