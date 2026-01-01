import React, { useState, useEffect, useRef } from 'react';
import { collaborationService, messageService, authService } from '../services/authService';
import './MessagesTab.css';

function MessagesTab({ eventId, event, isOwner }) {
    const [members, setMembers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedMembers, setSelectedMembers] = useState([]);
    const [messageContent, setMessageContent] = useState('');
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showSent, setShowSent] = useState(false);
    const currentUser = authService.getCurrentUser();
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        loadMembers();
        loadMessages();
        const interval = setInterval(loadMessages, 5000); // More frequent updates for chat
        return () => clearInterval(interval);
    }, [eventId]);

    const loadMembers = async () => {
        try {
            const collaborators = await collaborationService.getCollaborators(eventId);
            const acceptedCollaborators = collaborators.filter(c => c.status === 'ACCEPTED');

            const membersList = [...acceptedCollaborators];
            if (!isOwner) {
                membersList.push({
                    userId: event.organizerId,
                    name: `${event.organizerName} (Owner)`,
                    email: event.organizerEmail,
                    role: 'OWNER'
                });
            }

            const others = membersList.filter(m => m.userId !== currentUser.id);
            setMembers(others);
        } catch (error) {
            console.error('Failed to load members:', error);
        }
    };

    const loadMessages = async () => {
        try {
            const data = await messageService.getMessages(eventId);
            // Data now includes both sent and received
            setMessages(data || []);

            // Mark received messages as read
            const unreadCount = data ? data.filter(m => !m.isRead && m.receiverId === currentUser.id).length : 0;
            if (unreadCount > 0) {
                await messageService.markAsRead(eventId);
            }
        } catch (error) {
            console.error('Failed to load messages:', error);
        }
    };

    const toggleMember = (userId) => {
        setSelectedMembers(prev =>
            prev.includes(userId)
                ? prev.filter(id => id !== userId)
                : [...prev, userId]
        );
    };

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (selectedMembers.length === 0 || !messageContent.trim()) return;

        setLoading(true);
        try {
            await messageService.sendMessages({
                eventId,
                receiverIds: selectedMembers,
                content: messageContent
            });
            setMessageContent('');
            setSelectedMembers([]);
            setSearchTerm(''); // Clear search
            setShowSent(true);
            setTimeout(() => setShowSent(false), 3000);
            loadMessages();
        } catch (error) {
            console.error('Failed to send message:', error);
            alert('Failed to send message');
        } finally {
            setLoading(false);
        }
    };

    const filteredMembers = searchTerm.trim()
        ? members.filter(m =>
            m.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            m.email.toLowerCase().includes(searchTerm.toLowerCase())
        )
        : [];

    return (
        <div className="messages-tab">
            <div className="messages-container">
                <div className="composer-section">
                    <h3>Send Message</h3>
                    <div className="member-selector">
                        <div className="search-box">
                            <i className="fa-solid fa-magnifying-glass"></i>
                            <input
                                type="text"
                                placeholder="Search teammates to message..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>

                        {(searchTerm.trim() !== '' || selectedMembers.length > 0) && (
                            <div className="members-chips">
                                {members.filter(m => selectedMembers.includes(m.userId)).map(member => (
                                    <button
                                        key={member.userId}
                                        className="member-chip selected"
                                        onClick={() => toggleMember(member.userId)}
                                    >
                                        {member.name} <i className="fa-solid fa-xmark"></i>
                                    </button>
                                ))}
                                {filteredMembers.filter(m => !selectedMembers.includes(m.userId)).map(member => (
                                    <button
                                        key={member.userId}
                                        className="member-chip"
                                        onClick={() => toggleMember(member.userId)}
                                    >
                                        {member.name}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    <form onSubmit={handleSendMessage} className="message-form">
                        <textarea
                            placeholder="Type your message here..."
                            value={messageContent}
                            onChange={(e) => setMessageContent(e.target.value)}
                            required
                        />
                        <button
                            type="submit"
                            className={`btn btn-primary btn-send ${showSent ? 'sent-success' : ''}`}
                            disabled={loading || selectedMembers.length === 0 || !messageContent.trim()}
                        >
                            {loading ? 'Sending...' : (showSent ? '✓ Sent!' : 'Send Message')}
                        </button>
                    </form>
                </div>

                <div className="inbox-section">
                    <h3>Team Chat</h3>
                    <div className="chat-viewport">
                        {messages.length === 0 ? (
                            <div className="empty-messages">
                                <p>No messages yet. Start the conversation!</p>
                            </div>
                        ) : (
                            <div className="messages-viewport">
                                {messages.map((msg, index) => {
                                    const isSent = Number(msg.senderId) === Number(currentUser.id);
                                    return (
                                        <div key={index} className={`chat-bubble-wrapper ${isSent ? 'sent' : 'received'}`}>
                                            <div className="chat-bubble">
                                                {!isSent && <div className="bubble-sender">{msg.senderName}</div>}
                                                <div className="bubble-content">{msg.content}</div>
                                                <div className="bubble-meta">
                                                    <span className="bubble-time">
                                                        {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                    </span>
                                                    {isSent && (
                                                        <span className={`status-ticks ${msg.isRead ? 'read' : 'sent'}`}>
                                                            {msg.isRead ? (
                                                                <i className="fa-solid fa-check-double"></i>
                                                            ) : (
                                                                <i className="fa-solid fa-check"></i>
                                                            )}
                                                        </span>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                                <div ref={messagesEndRef} />
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default MessagesTab;
