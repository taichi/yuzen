if ("WebSocket" in window) {
	var url = "ws://localhost:8080";
	console.log("connect to " + url);
	var ws = new WebSocket(url);
	ws.onopen = function() {
		console.log("connected...");
	};
	ws.onmessage = function(event) {
		console.log(event.data);
		location.reload();
	};
	ws.onclose = function() {
		console.log("closed");
	};
} else {
	alert("Auto reloading is unsupported because WebSocket is unsupported.")
}
