const API_URL = "http://localhost:8081/api";
let currentUser = null;

function showPage(pageId) {
    document.querySelectorAll('.page').forEach(el => el.classList.add('hidden'));
    document.getElementById(pageId).classList.remove('hidden');
}

async function register() {
    const user = document.getElementById('reg-user').value;
    const pass = document.getElementById('reg-pass').value;

    if (!user || !pass) return alert("Fill all fields");

    const res = await fetch(`${API_URL}/register`, {
        method: 'POST',
        body: JSON.stringify({ username: user, password: pass })
    });

    if (res.status === 201) {
        alert("Registration successful! Please Login.");
        showPage('login-page');
    } else {
        const data = await res.json();
        alert(data.message);
    }
}

async function login() {
    const user = document.getElementById('login-user').value;
    const pass = document.getElementById('login-pass').value;

    const res = await fetch(`${API_URL}/login`, {
        method: 'POST',
        body: JSON.stringify({ username: user, password: pass })
    });

    const data = await res.json();
    if (res.ok) {
        currentUser = data.username;
        loadDashboard();
        showPage('dashboard-page');
    } else {
        alert(data.message);
    }
}

async function loadDashboard() {
    if (!currentUser) return;
    document.getElementById('user-name').innerText = currentUser;

    const res = await fetch(`${API_URL}/details?username=${currentUser}`);
    const data = await res.json();

    document.getElementById('balance').innerText = `$${data.balance.toFixed(2)}`;
    document.getElementById('account-num').innerText = data.accountNumber;

    const tbody = document.getElementById('tx-body');
    tbody.innerHTML = '';
    data.history.reverse().forEach(tx => {
        const row = document.createElement('tr');
        const isDebit = tx.from === currentUser;
        const type = isDebit ? 'Sent' : 'Received';
        const other = isDebit ? tx.to : tx.from;
        const riskClass = tx.risk > 0.5 ? '<span class="risk-high">Fraud Check</span>' : '';

        row.innerHTML = `
            <td>${type}</td>
            <td>${other}</td>
            <td>$${tx.amount.toFixed(2)}</td>
            <td>${riskClass}</td>
        `;
        tbody.appendChild(row);
    });
}

async function transfer() {
    const to = document.getElementById('trans-to').value;
    const amount = document.getElementById('trans-amount').value;

    if (!to || !amount) return alert("Fill all fields");

    const res = await fetch(`${API_URL}/transfer`, {
        method: 'POST',
        body: JSON.stringify({ from: currentUser, to: to, amount: amount })
    });

    const data = await res.json();
    alert(data.message);
    if (data.risk > 0.5) {
        alert("WARNING: This transaction was flagged by our Fraud Detection System.");
    }

    loadDashboard();
}

function logout() {
    currentUser = null;
    showPage('login-page');
    document.getElementById('login-user').value = '';
    document.getElementById('login-pass').value = '';
}
