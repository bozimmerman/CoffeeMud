function SipletFileSystem(dbName) 
{
	var self = this;
	this.dbName = dbName || 'SipFileSystem';
	this.storeName = 'files';
	this.db = null;
	this.enabled = ('indexedDB' in window);
	this.blobCache = {};
	this.iconsPath = '/icons';
	this.iconFiles = [];
	for (i=1; i<=2192; i++) 
		this.iconFiles.push('fc'+i+'.png');

	this.init = function(callBack) 
	{
		if(!this.enabled)
			callBack('not supported');
		var request = indexedDB.open(this.dbName, 1);
		request.onupgradeneeded = function(event) {
			var db = event.target.result;
			var store = db.createObjectStore(self.storeName, { keyPath: 'path' });
			store.createIndex('parent', 'parent', { unique: false });
		};
		request.onsuccess = function(event) {
			self.db = event.target.result;
			callBack(null);
		};
		request.onerror = function(event) {
			callBack(event.target.error);
		};
	};

	this.load = function(path, callBack, noCache) 
	{
		if(!this.enabled)
			callBack('not supported');
		if (this.db)
			this._load(path, callBack, noCache);
		else 
		{
			this.init(function(err) {
				if (err) return callBack(err);
				self._load(path, callBack, noCache);
			});
		}
	};

	this._load = function(path, callBack, noCache) 
	{
		path = this.normalizePath(path);
		if (path.startsWith(this.iconsPath + '/')) 
		{
			var filename = path.substring(this.iconsPath.length + 1);
			if (this.iconFiles.indexOf(filename)>=0)
			{
				if ((path in this.blobCache) && (true !== noCache)) 
				{
					callBack(null, this.blobCache[path].url);
					return;
				}
				fetch(path.substring(1)).then(function(response) 
				{
					if (!response.ok)
						throw new Error('File not found');
					return response.blob();
				}).then(function(blob) 
				{
					var url = URL.createObjectURL(blob);
					if (true !== noCache)
						self.blobCache[path] = { url: url, blob: blob };
					callBack(null, url);
				}).catch(function(err) 
				{
					callBack(err);
				});
				return;
			} 
			else 
			{
				callBack(new Error('no data'));
				return;
			}
		}

		
		if((path in this.blobCache) && (true !== noCache))
			callBack(null, this.blobCache[path].url);
		var tx = this.db.transaction(this.storeName, 'readonly');
		var store = tx.objectStore(this.storeName);
		var request = store.get(path);
		request.onsuccess = function() {
			if(request.result && request.result.data)
			{
				if(request.result.data.startsWith('data:')&&(true !== noCache))
				{
					var dataUrl = request.result.data;
					var mimeType = dataUrl.match(/^data:([^;]+);base64,/);
					var binary = atob(dataUrl.split(',')[1]);
					var array = new Uint8Array(binary.length);
					for (var i = 0; i < binary.length; i++) {
						array[i] = binary.charCodeAt(i);
					}
					var blob = new Blob([array], { type: mimeType });
					var url = URL.createObjectURL(blob);
					self.blobCache[path] = { url: url, blob: blob };
					callBack(null, url);
				}
				else
					callBack(null, request.result.data);
			}
			else
				callBack(new Error('no data'));
		};
		request.onerror = function() {
			callBack(request.error);
		};
	};

	this.trimBlobCache = function(path)
	{
		path = this.normalizePath(path);
		if(path in this.blobCache)
		{
			URL.revokeObjectURL(this.blobCache[path].url);
			delete this.blobCache[path];
		}
	}

	this.save = function(path, data, callBack) 
	{
		if(!this.enabled)
			callBack('not supported');
		if (this.db)
			this._save(path, data, callBack);
		else 
		{
			this.init(function(err) 
			{
				if (err) 
					return callBack(err);
				self._save(path, data, callBack);
			});
		}
	};
	
	this.normalizeData = function(path, data, callBack)
	{
		var ext = path.split('.').pop().toLowerCase();
		if((data instanceof ArrayBuffer || data instanceof Blob))
		{ 
			var blob = data instanceof ArrayBuffer ? new Blob([data]) : data;
			var reader = new FileReader();
			reader.onload = function(e) {
				callBack(null, e.target.result);
			};
			reader.onerror = function() {
				callBack(reader.error);
			}
			if (ext in window.mimeTypes)
				reader.readAsDataURL(data)
			else
				reader.readAsText(data);
		}
		else
		if(typeof data === 'string')
			callBack(null,data);
		else
			callBack(null,JSON.stringify(data));
	};

	this._save = function(path, data, callBack) 
	{
		path = this.normalizePath(path);
		if(path.startsWith(this.iconsPath)) 
		{
			callBack(new Error('Cannot modify protected directory'));
			return;
		}
		var parent = this.getParentPath(path);
		this.normalizeData(path, data, function(err, finalData)
		{
			if(err)
			{
				callBack(err);
				return;
			}
			var tx = self.db.transaction(self.storeName, 'readwrite');
			var store = tx.objectStore(self.storeName);
			var request = store.put({ path: path, data: finalData, parent: parent });
			request.onsuccess = function() 
			{
				callBack(null);
			};
			request.onerror = function() 
			{
				callBack(request.error);
			};
		});
	};

	this.exists = function(path, callBack) 
	{
		if (!this.enabled) 
		{
			callBack('not supported');
			return;
		}
		if (this.db)
			this._exists(path, callBack);
		else 
		{
			var self = this;
			this.init(function(err) 
			{
				if (err) 
					return callBack(err);
				self._exists(path, callBack);
			});
		}
	};
	
	this._exists = function(path, callBack) 
	{
		path = this.normalizePath(path);
		if(path === this.iconsPath) 
		{
			callBack(null, true);
			return;
		}
		if(path.startsWith(this.iconsPath+'/')) 
		{
			var filename = path.substring(this.iconsPath.length+1);
			if((!filename.includes('/'))&&(this.iconFiles.indexOf(filename)>=0))
				callBack(null, true);
			else
				callBack(null, false);
			return;
		}
		var tx = this.db.transaction(this.storeName, 'readonly');
		var store = tx.objectStore(this.storeName);
		var request = store.get(path);
		request.onsuccess = function() 
		{
			callBack(null, !!request.result);
		};
		request.onerror = function() 
		{
			callBack(request.error);
		};
	};

	this.getMediaTree = function(callBack) 
	{
		if (!this.enabled) 
		{
			callBack('not supported');
			return;
		}
		if (this.db)
			this._getMediaTree(callBack);
		else 
		{
			var self = this;
			this.init(function(err) 
			{
				if (err) return callBack(err);
				self._getMediaTree(callBack);
			});
		}
	};

	this._getMediaTree = function(callBack) 
	{
		var tx = this.db.transaction(this.storeName, 'readonly');
		var store = tx.objectStore(this.storeName);
		var request = store.getAll();
		var me = this;
		request.onsuccess = function() 
		{
			var entries = request.result;
			me.iconFiles.forEach(function(filename) 
			{
				var iconPath = me.iconsPath+'/'+filename;
				entries.push({ path: iconPath, parent: this.iconsPath });
			});
			var tree = buildTree(entries);
			callBack(null, tree);
		};
		request.onerror = function() 
		{
			callBack(request.error);
		};
		function buildTree(entries) 
		{
			var root = { name: '', children: [], entries: [], isFolder: true, path: '', parent: null };
			entries.forEach(function(entry) 
			{
				var parts = entry.path.split('/').filter(part => part);
				var current = root;
				var currentPath = '';
				for (var i = 0; i < parts.length - 1; i++) 
				{
					var part = parts[i];
					currentPath = currentPath ? `${currentPath}/${part}` : part;
					var folder = current.children.find(node => node.name === part && node.isFolder);
					if (!folder) 
					{
						folder = { name: part, path: currentPath, isFolder: true, children: [], entries: [], parent: current};
						current.children.push(folder);
					}
					current = folder;
				}
				var fileName = parts[parts.length - 1];
				current.entries.push({
					name: fileName,
					id: entry.id,
					path: entry.path,
					type: entry.type,
					parent: current
				});
			});
			sortNode(root);
			return root;
			function sortNode(node) 
			{
				node.children.sort((a, b) => a.name.localeCompare(b.name));
				node.entries.sort((a, b) => a.name.localeCompare(b.name));
				node.children.forEach(sortNode);
			}
		}
	};

	this.read = function(path, callBack) 
	{
		this.load(path, callBack);
	};

	this.normalizePath = function(path) 
	{
		if (!path) 
			return '/';
		path = path.replace(/\/+/g, '/').replace(/\/$/, '');
		return path.startsWith('/') ? path : '/' + path;
	};

	this.getParentPath = function(path) 
	{
		var parts = path.split('/').filter(function(p) { return p; });
		return parts.length > 1 ? '/' + parts.slice(0, -1).join('/') : '/';
	};

	this.delete = function(path, callBack) {
		if (!this.enabled) 
		{
			callBack('not supported');
			return;
		}
		if (this.db) 
			this._delete(path, callBack);
		else 
		{
			var self = this;
			this.init(function(err) 
			{
				if (err) 
					return callBack(err);
				self._delete(path, callBack);
			});
		}
	};
	
	this._delete = function(path, callBack) 
	{
		path = this.normalizePath(path);
		if (path.startsWith(this.iconsPath)) 
		{
			callBack(new Error('Cannot delete from protected directory'));
			return;
		}
		var tx = this.db.transaction(this.storeName, 'readwrite');
		var store = tx.objectStore(this.storeName);
		var request = store.delete(path);
		request.onsuccess = function() 
		{
			callBack(null);
		};
		request.onerror = function() 
		{
			callBack(request.error);
		};
	};
	
}