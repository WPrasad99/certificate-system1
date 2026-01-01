import React, { useState, useEffect } from 'react';
import { collaborationService } from '../services/authService';
import './CollaborationRequests.css';

function CollaborationRequests() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadRequests();
        // Poll for new requests every 30 seconds
        const interval = setInterval(loadRequests, 30000);
        return () => clearInterval(interval);
    }, []);

    const loadRequests = async () => {
        try {
            const data = await collaborationService.getRequests();
            setRequests(data || []);
        } catch (error) {
            console.error('Failed to load requests:', error);
        }
    };

    const handleAccept = async (requestId) => {
        setLoading(true);
        try {
            await collaborationService.acceptRequest(requestId);
            await loadRequests();
            window.location.reload(); // Reload to show new event
        } catch (error) {
            alert(error.response?.data?.error || 'Failed to accept request');
        } finally {
            setLoading(false);
        }
    };

    const handleDecline = async (requestId) => {
        setLoading(true);
        try {
            await collaborationService.declineRequest(requestId);
            await loadRequests();
        } catch (error) {
            alert(error.response?.data?.error || 'Failed to decline request');
        } finally {
            setLoading(false);
        }
    };

    if (requests.length === 0) {
        return null; // Don't show empty widget
    }

    return (
        <div className="collaboration-requests-widget">
            <h3>📩 Collaboration Requests ({requests.length})</h3>
            <div className="requests-list">
                {requests.map(request => (
                    <div key={request.id} className="request-card">
                        <div className="request-info">
                            <h4>{request.eventName}</h4>
                            <p>From: <strong>{request.senderName}</strong> ({request.senderEmail})</p>
                            <span className="request-date">
                                {new Date(request.createdAt).toLocaleDateString()}
                            </span>
                        </div>
                        <div className="request-actions">
                            <button
                                className="btn-accept"
                                onClick={() => handleAccept(request.id)}
                                disabled={loading}
                            >
                                ✓ Accept
                            </button>
                            <button
                                className="btn-decline"
                                onClick={() => handleDecline(request.id)}
                                disabled={loading}
                            >
                                ✗ Decline
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default CollaborationRequests;
