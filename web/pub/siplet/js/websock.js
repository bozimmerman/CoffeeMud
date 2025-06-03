window.isElectron = typeof window.process === 'object' && window.process.versions && window.process.versions.electron;

if (isElectron) 
{
	window.WebSocket = function(url) {
		var net = require('net'); // Use Electron's net module
		this.socket = null;
		this.onopen = null;
		this.onmessage = null;
		this.onerror = null;
		this.onclose = null;
		if(!url || ((!url.startsWith('ws://')) && (!url.startsWith('wss://'))))
			throw new Error('Invalid WebSocket URL: ' + url);
		this.ssl = url.startsWith('wss');
		url = url.substr(url.indexOf('//')+2);
		var x = url.indexOf('?port=');
		if(x<0)
			throw new Error('Invalid WebSocket URL (missing port): ' + url);
		this.host = url.substr(0,x);
		this.port = parseInt(url.substr(x+6));
		// Initialize TCP socket
		this.socket = net.createConnection({ host: this.host, port: this.port }, function() 
		{
			this.socket.readyState = WebSocket.OPEN;
			if (this.onopen) {
				this.onopen(new Event('open'));
			}
		});
	
		this.socket.on('data', function(data) 
		{
			this.socket.readyState = WebSocket.OPEN;
			if (this.onmessage) {
				var arrayBuffer = data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength);
				this.onmessage({ data: arrayBuffer });
			}
		});
	
		this.socket.on('error', function(err) 
		{
			if (this.onerror) {
				this.onerror(new ErrorEvent('error', { error: err }));
			}
		});
	
		this.socket.on('close', function() 
		{
			if (this.socket.readyState !== WebSocket.CLOSED) 
			{
				this.socket.readyState = WebSocket.CLOSED;
				if (this.onclose) {
					this.onclose(new Event('close'));
				}
				this.cleanup();
			}
		});
	
		this.send = function(data) 
		{
			if (this.socket) {
				var toSend = typeof data === 'string'?data:Buffer.from(data);
				this.socket.write(toSend);
			}
		};
		
		this.cleanup = function()
		{
			if (this.socket) 
			{
				this.socket.removeAllListeners('data');
				this.socket.removeAllListeners('error');
				this.socket.removeAllListeners('close');
				this.socket.removeAllListeners('end');
				this.socket.end();
				this.socket.destroy();
				this.socket = null;
				this.onopen = null;
				this.onmessage = null;
				this.onerror = null;
				this.onclose = null;
			}
		};
	
		this.close = function() 
		{
			if (this.socket && this.socket.readyState !== WebSocket.CLOSED) 
			{
				this.socket.readyState = WebSocket.CLOSED;
				if (this.onclose) {
					this.onclose(new Event('close'));
				}
				this.cleanup();
			}
		};
	};
}