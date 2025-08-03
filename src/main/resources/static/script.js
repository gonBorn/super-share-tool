const ws = new WebSocket("ws://" + location.host + "/ws");
const chatMessages = document.getElementById("chat-messages");
const messageInput = document.getElementById("message-input");
const sendButton = document.getElementById("send-button");
const clearButton = document.getElementById("clear-button");

function ipToColor(ip) {
    let hash = 0;
    for (let i = 0; i < ip.length; i++) {
        hash = ip.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = '#';
    for (let i = 0; i < 3; i++) {
        const value = (hash >> (i * 8)) & 0xFF;
        color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
}

function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).catch(err => {
            console.error('Failed to copy text using navigator: ', err);
            fallbackCopyTextToClipboard(text);
        });
    } else {
        fallbackCopyTextToClipboard(text);
    }
}

function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement("textarea");
    textArea.value = text;

    // Avoid scrolling to bottom
    textArea.style.top = "0";
    textArea.style.left = "0";
    textArea.style.position = "fixed";

    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        const successful = document.execCommand('copy');
        if (!successful) {
            console.error('Fallback: Copying text command was not successful');
        }
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
    }

    document.body.removeChild(textArea);
}

ws.onmessage = function(event) {
    if (event.data === "CLEAR") {
        chatMessages.innerHTML = "";
    } else {
        // Use the same pattern as ChatPanel
        const match = event.data.match(/^(?:.|)*?\[(.*?)\] \[(.*?)\]: (.*)$/s);
        if (match) {
            const timestampStr = match[1];
            const ip = match[2];
            const message = match[3];

            // Format timestamp as yyyy/MM/dd HH:mm:ss
            const date = new Date(timestampStr);
            const formattedTime =
                date.getFullYear() + '/' +
                ('0' + (date.getMonth() + 1)).slice(-2) + '/' +
                ('0' + date.getDate()).slice(-2) + ' ' +
                ('0' + date.getHours()).slice(-2) + ':' + 
                ('0' + date.getMinutes()).slice(-2) + ':' + 
                ('0' + date.getSeconds()).slice(-2);

            const messageContainer = document.createElement("div");
            messageContainer.style.position = "relative";
            messageContainer.style.marginBottom = "10px";

            const copyButton = document.createElement("button");
            copyButton.textContent = "Copy";
            copyButton.style.position = "absolute";
            copyButton.style.top = "0";
            copyButton.style.right = "0";
            copyButton.style.display = "none";
            copyButton.className = "btn btn-secondary btn-sm";

            copyButton.addEventListener("click", () => {
                copyToClipboard(message);
            });

            messageContainer.addEventListener("mouseover", () => {
                copyButton.style.display = "block";
            });

            messageContainer.addEventListener("mouseout", () => {
                copyButton.style.display = "none";
            });
            
            const infoContainer = document.createElement("div");
            
            const timeElement = document.createElement("span");
            timeElement.textContent = formattedTime + " ";
            timeElement.style.fontSize = "0.8em";
            timeElement.style.color = "#888";
            
            const ipElement = document.createElement("span");
            ipElement.textContent = "[" + ip + "]:";
            ipElement.style.color = ipToColor(ip);
            ipElement.style.fontSize = "0.8em";

            const messageElement = document.createElement("div");
            messageElement.textContent = message;
            messageElement.style.color = ipToColor(ip);

            infoContainer.appendChild(timeElement);
            infoContainer.appendChild(ipElement);

            messageContainer.appendChild(infoContainer);
            messageContainer.appendChild(messageElement);
            messageContainer.appendChild(copyButton);
            chatMessages.appendChild(messageContainer);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
    }
};

sendButton.onclick = function() {
    const message = messageInput.value;
    if (message.trim() !== "") {
        ws.send(message);
        messageInput.value = "";
    }
};

clearButton.onclick = function() {
    ws.send("CLEAR");
};

messageInput.addEventListener("keyup", function(event) {
    if (event.key === "Enter") {
        sendButton.click();
    }
});

const uploadForm = document.getElementById("upload-form");
const fileInput = document.getElementById("file-input");
const progressContainer = document.getElementById("upload-progress-container");
const progressBar = document.getElementById("progress-bar");
const progressText = document.getElementById("progress-text");

uploadForm.addEventListener("submit", function(event) {
    event.preventDefault();

    if (fileInput.files.length === 0) {
        alert("Please select a file to upload.");
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("file", file);

    const xhr = new XMLHttpRequest();
    let lastLoaded = 0;
    let lastTime = Date.now();

    xhr.upload.addEventListener("progress", function(e) {
        if (e.lengthComputable) {
            progressContainer.style.display = "block";
            const percentComplete = (e.loaded / e.total) * 100;
            progressBar.style.width = percentComplete + "%";
            progressBar.setAttribute("aria-valuenow", percentComplete);

            const currentTime = Date.now();
            const timeDiff = (currentTime - lastTime) / 1000;
            const bytesDiff = e.loaded - lastLoaded;
            const speed = bytesDiff / timeDiff;

            lastLoaded = e.loaded;
            lastTime = currentTime;

            let speedStr;
            if (speed > 1024 * 1024) {
                speedStr = (speed / (1024 * 1024)).toFixed(2) + " MB/s";
            } else if (speed > 1024) {
                speedStr = (speed / 1024).toFixed(2) + " KB/s";
            } else {
                speedStr = speed.toFixed(2) + " B/s";
            }
            
            progressText.textContent = Math.round(percentComplete) + "% - " + speedStr;
        }
    });

    xhr.addEventListener("load", function() {
        progressBar.classList.add("bg-success");
        progressText.textContent = "Upload complete!";
        setTimeout(() => location.reload(), 1000);
    });

    xhr.addEventListener("error", function() {
        progressBar.classList.add("bg-danger");
        progressText.textContent = "Upload failed.";
    });

    xhr.open("POST", "/upload", true);
    xhr.send(formData);
});
