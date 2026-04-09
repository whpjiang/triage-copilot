const form = document.getElementById("chatForm");
const input = document.getElementById("messageInput");
const list = document.getElementById("messageList");
const sendBtn = document.getElementById("sendBtn");
const statusText = document.getElementById("statusText");

let sending = false;

function scrollToBottom() {
    list.scrollTop = list.scrollHeight;
}

function appendMessage(role, text, error = false) {
    const div = document.createElement("div");
    div.className = `message ${role}${error ? " error" : ""}`;
    div.textContent = text;
    list.appendChild(div);
    scrollToBottom();
}

function setSending(flag) {
    sending = flag;
    sendBtn.disabled = flag;
    input.disabled = flag;
    statusText.textContent = flag ? "Generating triage suggestion..." : "Press Enter to send, Shift + Enter for a new line";
}

async function sendMessage() {
    const message = input.value.trim();
    if (!message || sending) {
        return;
    }

    appendMessage("user", message);
    input.value = "";
    setSending(true);

    try {
        const resp = await fetch("/api/chat/send", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message })
        });

        if (!resp.ok) {
            appendMessage("bot", `API error (HTTP ${resp.status})`, true);
            return;
        }

        const payload = await resp.json();
        if (payload?.code !== 200) {
            appendMessage("bot", payload?.message || "Request failed. Please try again later.", true);
            return;
        }

        appendMessage("bot", payload?.data || "No model response received.", false);
    } catch (err) {
        appendMessage("bot", "Network error. Please check whether the service is running.", true);
    } finally {
        setSending(false);
        input.focus();
    }
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    await sendMessage();
});

input.addEventListener("keydown", async (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        await sendMessage();
    }
});

scrollToBottom();
