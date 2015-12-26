var Siplet = function()
{
	this.socket = null;
	//window.console.info("Siplet occurred!");
	this.randomNumber = Math.random();
	this.mudport=23;
	this.callback = null;
	this.token = null;
	this.host = '';
	this.vulnerableState = false;
};

Siplet.prototype.connectToURL = function(host, port, mudport, callback)
{
	if(this.socket == null)
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
		//window.console.info(this.randomNumber+": "+this.socket.readyState+": connectToURL "+host+":"+port);
	}
	return true;
};

Siplet.prototype.isConnecting = function()
{
	if(this.socket)
	{
		return this.socket.readyState === 0;
	}
	return false;
}

Siplet.prototype.receivedData = function(event)
{
	if(this.callback)
		this.callback(this.socket.readyState == 1, event.data);
	this.callback = null;
//	window.console.info(this.randomNumber+": "+this.socket.readyState+": receivedData");
};

Siplet.prototype.closeOccurred = function(event)
{
	//window.console.info(this.randomNumber+": "+this.socket.readyState+": close ");
	this.cleanUpClose();
};

Siplet.prototype.errorOccurred = function(event)
{
	//window.console.info(this.randomNumber+": "+this.socket.readyState+": error ");
	if(this.callback)
		this.callback(false,'');
	this.disconnectFromURL();
};

Siplet.prototype.openOccurred = function(event)
{
	var self=this;
	var finishConnect;
	finishConnect = function(tries)
	{
		//window.console.info(self.randomNumber+": "+self.socket.readyState+": attempt open ");
		if(self.socket && self.socket.readyState == 1)
		{
			var safeurl=encodeURIComponent(self.host);
			var safeport=encodeURIComponent(self.mudport);
			var cmd = 'CONNECT&URL='+safeurl+'&PORT='+safeport;
			self.socket.send(cmd);
			//window.console.info(self.randomNumber+": "+self.socket.readyState+": open ");
		}
		else
		if(self.socket && (self.socket.readyState == 0) && (tries < 10))
			setTimeout(function(){finishConnect(tries+1);},500);
	};
	finishConnect(0);
};

Siplet.prototype.cleanUpClose = function()
{
	this.socket.onmessage = null;
	this.socket.onopen = null; 
	this.socket.onclose = null; 
	this.socket.onerror = null;
	this.callback = null;
}

Siplet.prototype.disconnectFromURL = function()
{
	if(this.socket)
	{
		this.cleanUpClose();
		if(this.socket.readyState < 2)
		{
			//window.console.info(this.randomNumber+": "+this.socket.readyState+": disconnectFromURL ");
			if(this.token)
				this.socket.send('DISCONNECT&TOKEN='+this.token);
			this.socket.close();
		}
	}
};

Siplet.prototype.isConnectedToURL = function()
{
	return (this.socket && this.socket.readyState  === 1);
};

Siplet.prototype.getURLData = function(callback)
{
	if(this.socket && this.socket.readyState  === 1)
	{
		this.callback = callback;
//		window.console.info(this.randomNumber+": "+this.token+": "+this.socket.readyState+": getURLData");
		this.socket.send('POLL&TOKEN='+this.token);
	}
};

Siplet.prototype.sendData = function(s, callback)
{
	if(this.socket && this.socket.readyState  === 1)
	{
		this.callback = callback;
		s=encodeURIComponent(''+s);
		this.socket.send('SENDDATA&TOKEN='+this.token+'&DATA='+s);
	}
};

