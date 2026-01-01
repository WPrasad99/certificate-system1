import React, { useState, useEffect } from 'react';
import { collaborationService, messageService, authService } from '../services/authService';
import './MessagesTab.css';

function MessagesTab({ eventId, event, isOwner }) {
    const [members, setMembers] = useState([]);
    const [selectedMembers, setSelectedMembers] = useState([]);
    const [messageContent, setMessageContent] = useState('');
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showSent, setShowSent] = useState(false);
    const currentUser = authService.getCurrentUser();

    useEffect(() => {
        loadMembers();
        loadMessages();
        const interval = setInterval(loadMessages, 10000); // Poll for new messages
        return () => clearInterval(interval);
    }, [eventId]);

    const loadMembers = async () => {
        try {
            const collaborators = await collaborationService.getCollaborators(eventId);
            // Filter only accepted collaborators
            const acceptedCollaborators = collaborators.filter(c => c.status === 'ACCEPTED');

            // Add owner to the list if current user is not owner
            const membersList = [...acceptedCollaborators];
            if (!isOwner) {
                membersList.push({
                    userId: event.organizerId,
                    name: `${event.organizerName} (Owner)`,
                    email: event.organizerEmail,
                    role: 'OWNER'
                });
            } else {
                // Owner is current user, so don't add to list, but maybe collaborators want to see each other?
                // The collaborators list already has all other collaborators.
            }

            // Filter out current user from members list
            const others = membersList.filter(m => m.userId !== currentUser.id);
            setMembers(others);
        } catch (error) {
            console.error('Failed to load members:', error);
        }
    };

    const loadMessages = async () => {
        try {
            const data = await messageService.getMessages(eventId);
            setMessages(data || []);
            // Mark as read when viewing
            if (data && data.some(m => !m.isRead)) {
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
            setShowSent(true);
            setTimeout(() => setShowSent(false), 3000);
            loadMessages(); // Reload list to see my own sent message? 
            // Actually the current getMessages only returns messages RECEIVED by current user.
            // If we want to see SENT messages we might need another endpoint or list.
            // User only asked for 'Sent' indicator in box for now.
        } catch (error) {
            console.error('Failed to send message:', error);
            alert('Failed to send message');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="messages-tab">
            <div className="messages-container">
                <div className="composer-section">
                    <h3>Send Team Message</h3>
                    <div className="member-selector">
                        <p className="selector-label">To:</p>
                        <div className="members-chips">
                            {members.map(member => (
                                <button
                                    key={member.userId}
                                    className={`member-chip ${selectedMembers.includes(member.userId) ? 'selected' : ''}`}
                                    onClick={() => toggleMember(member.userId)}
                                >
                                    {member.name}
                                </button>
                            ))}
                        </div>
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
                    <h3>Team Messages</h3>
                    <div className="messages-list">
                        {messages.length === 0 ? (
                            <div className="empty-messages">
                                <p>No messages yet.</p>
                            </div>
                        ) : (
                            messages.map(msg => (
                                <div key={msg.id} className={`message-item ${msg.isRead ? 'read' : 'unread'}`}>
                                    <div className="message-header">
                                        <span className="sender-name">{msg.senderName}</span>
                                        <span className="message-time">
                                            {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                        </span>
                                    </div>
                                    <p className="message-content">{msg.content}</p>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default MessagesTab;
