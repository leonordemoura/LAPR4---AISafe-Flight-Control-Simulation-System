var currentPage = 'events';
var refreshTimer = null;

function init() {
    showPage('events');
}

function showPage(page) {
    currentPage = page;
    if (refreshTimer) clearTimeout(refreshTimer);
    refresh();
}

function refresh() {
    var url = (currentPage === 'events') ? '/api/events' : '/api/active';
    var req = new XMLHttpRequest();

    req.onload = function() {
        if (this.status === 200) {
            try {
                var data = JSON.parse(this.responseText);
                if (currentPage === 'events') renderEvents(data);
                else renderActive(data);
                setStatus('Last updated: ' + new Date().toLocaleTimeString());
            } catch (e) {
                setStatus('Parse error: ' + e.message);
            }
        } else {
            setStatus('HTTP error: ' + this.status);
        }
        refreshTimer = setTimeout(refresh, 2000);
    };

    req.ontimeout = function() {
        setStatus('Timeout – retrying...');
        refreshTimer = setTimeout(refresh, 1000);
    };

    req.onerror = function() {
        setStatus('Connection error – retrying...');
        refreshTimer = setTimeout(refresh, 1000);
    };

    req.open('GET', url);
    req.timeout = 5000;
    req.send();
}

function renderEvents(data) {
    var html = '<h2>Last Recorded Events (' + data.length + ')</h2>';
    if (data.length === 0) {
        html += '<p><em>No events yet.</em></p>';
    } else {
        html += '<table><tr><th>Type</th><th>Timestamp</th>'
            + '<th>Username</th><th>IP</th><th>Port</th><th>Service</th></tr>';
        for (var i = 0; i < data.length; i++) {
            var e = data[i];
            html += '<tr>'
                + '<td class="' + e.type + '">' + e.type + '</td>'
                + '<td>' + e.timestamp + '</td>'
                + '<td>' + e.username + '</td>'
                + '<td>' + e.ip + '</td>'
                + '<td>' + e.port + '</td>'
                + '<td>' + e.service + '</td>'
                + '</tr>';
        }
        html += '</table>';
    }
    document.getElementById('content').innerHTML = html;
}

function renderActive(data) {
    var html = '<h2>Currently Active Users (' + data.length + ')</h2>';
    if (data.length === 0) {
        html += '<p><em>No active sessions.</em></p>';
    } else {
        html += '<table><tr><th>Username</th><th>IP</th>'
            + '<th>Port</th><th>Service</th><th>Login Time</th></tr>';
        for (var i = 0; i < data.length; i++) {
            var s = data[i];
            html += '<tr>'
                + '<td>' + s.username + '</td>'
                + '<td>' + s.ip + '</td>'
                + '<td>' + s.port + '</td>'
                + '<td>' + s.service + '</td>'
                + '<td>' + s.login_time + '</td>'
                + '</tr>';
        }
        html += '</table>';
    }
    document.getElementById('content').innerHTML = html;
}

function setStatus(msg) {
    document.getElementById('status').innerText = msg;
}