var Siplet = function()
{
	this.socket = null;
	window.console.info("Siplet occurred!");
	this.connected = false;
	this.randomNumber = Math.random();
	this.mudport=23;
	this.callback = null;
	this.token = null;
	this.host = '';
};

Siplet.prototype.connectToURL = function(host, port, mudport, callback)
{
	this.socket = new WebSocket("ws://"+host+":"+port+"/SipletInterface");
	var self=this;
	this.host = host;
	this.mudport = mudport;
	this.socket.onmessage = function(event){ self.receivedData.apply(self,[event]); };
	this.socket.onopen = function(event){ self.openOccurred.apply(self,[event]); }; 
	this.socket.onclose = function(event){ self.closeOccurred.apply(self,[event]); }; 
	this.socket.onerror = function(event){ self.errorOccurred.apply(self,[event]); };
	if(callback)
		this.callback = callback;
	window.console.info(this.randomNumber+": "+this.connected+": connectToURL "+host+":"+port);
	return true;
};

Siplet.prototype.receivedData = function(event)
{
	if(this.callback)
		this.callback(this.connected, event.data);
	this.callback = null;
	window.console.info(this.randomNumber+": "+this.connected+": "+this.socket.readyState+": receivedData");
};

Siplet.prototype.closeOccurred = function(event)
{
	window.console.info(this.randomNumber+": "+this.connected+": close ");
	this.connected = false;
	this.callback = null;
};

Siplet.prototype.errorOccurred = function(event)
{
	window.console.info(this.randomNumber+": "+this.connected+": error ");
	this.connected = false;
	if(this.callback)
		this.callback(false,'');
	this.callback = null;
};

Siplet.prototype.openOccurred = function(event)
{
	this.connected = true;
	var safeurl=encodeURIComponent(this.host);
	var safeport=encodeURIComponent(this.mudport);
	var cmd = 'CONNECT&URL='+safeurl+'&PORT='+safeport;
	this.socket.send(cmd);
	window.console.info(this.randomNumber+": "+this.connected+": "+this.socket.readyState+": open ");
};

Siplet.prototype.disconnectFromURL = function()
{
	if(this.connected === true)
	{
		window.console.info(this.randomNumber+": "+this.connected+": disconnectFromURL ");
		if(this.token)
			this.socket.send('DISCONNECT&TOKEN='+this.token);
		this.socket.close();
		this.connected = false;
		this.callback = null;
	}
};

Siplet.prototype.isConnectedToURL = function()
{
	return this.connected;
};

Siplet.prototype.getURLData = function(callback)
{
	if(this.connected === true)
	{
		this.callback = callback;
		window.console.info(this.randomNumber+": "+this.token+": "+this.socket.readyState+": getURLData");
		this.socket.send('POLL&TOKEN='+this.token);
	}
};

Siplet.prototype.sendData = function(s, callback)
{
	if(this.connected === true)
	{
		this.callback = callback;
		s=encodeURIComponent(''+s);
		this.socket.send('SENDDATA&TOKEN='+this.token+'&DATA='+s);
	}
};

