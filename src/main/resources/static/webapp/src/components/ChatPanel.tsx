import React, { useState, useEffect, useRef } from 'react';

function ChatPanel() {
    const [messages, setMessages] = useState<string[]>([]);
    const [input, setInput] = useState('');
    const ws = useRef<WebSocket | null>(null);

    useEffect(() => {
        ws.current = new WebSocket(`ws://${window.location.host}/ws`);

        ws.current.onmessage = (event) => {
            if (event.data === 'CLEAR') {
                setMessages([]);
            } else {
                setMessages(prevMessages => [...prevMessages, event.data]);
            }
        };

        return () => {
            ws.current?.close();
        };
    }, []);

    const sendMessage = () => {
        if (input.trim() !== '') {
            ws.current?.send(input);
            setInput('');
        }
    };

    const clearChat = () => {
        ws.current?.send('CLEAR');
    };

    const ipToColor = (ip: string) => {
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
    };

    const formatMessage = (message: string) => {
        const match = message.match(/^(?:.|)*?\[(.*?)\] \[(.*?)\]: (.*)$/s);
        if (match) {
            const [, timestampStr, ip, msg] = match;
            const date = new Date(timestampStr);
            const formattedTime =
                date.getFullYear() + '/' +
                ('0' + (date.getMonth() + 1)).slice(-2) + '/' +
                ('0' + date.getDate()).slice(-2) + ' ' +
                ('0' + date.getHours()).slice(-2) + ':' +
                ('0' + date.getMinutes()).slice(-2) + ':' +
                ('0' + date.getSeconds()).slice(-2);

            return (
                <div style={{ marginBottom: '10px' }}>
                    <div style={{ fontSize: '0.8em', color: '#888' }}>
                        <span>{formattedTime} </span>
                        <span style={{ color: ipToColor(ip) }}>[{ip}]:</span>
                    </div>
                    <div style={{ color: ipToColor(ip), whiteSpace: 'pre-wrap' }}>{msg}</div>
                </div>
            );
        }
        return <div>{message}</div>;
    };

    return (
        <div>
            <h2>Chat</h2>
            <div className="card">
                <div className="card-body d-flex flex-column">
                    <div
                        className="mb-3 flex-grow-1"
                        style={{
                            overflowY: 'scroll',
                            padding: '10px',
                            fontSize: '0.9em',
                            maxHeight: '70vh',
                            minHeight: '300px',
                        }}
                    >
                        {messages.map((msg, index) => (
                            <div key={index}>{formatMessage(msg)}</div>
                        ))}
                    </div>
                    <div className="input-group mt-auto">
                        <textarea
                            className="form-control"
                            rows={3}
                            placeholder="Type here..."
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyUp={(e) => e.key === 'Enter' && sendMessage()}
                        ></textarea>
                        <div className="input-group-append">
                            <button className="btn btn-primary" onClick={sendMessage}>
                                Send
                            </button>
                        </div>
                    </div>
                    <button className="btn btn-secondary btn-sm mt-2" onClick={clearChat}>
                        Clear Chat
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ChatPanel;
