import React, { useState, useEffect } from 'react';
import { collaborationService } from '../services/authService';
import './CollaboratorsTab.css';

function CollaboratorsTab({ eventId }) {
    const [collaborators, setCollaborators] = useState([]);
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        loadCollaborators();
    }, [eventId]);

    const loadCollaborators = async () => {
        try {
            const data = await collaborationService.getCollaborators(eventId);
            setCollaborators(data || []);
        } catch (error) {
            console.error('Failed to load collaborators:', error);
        }
    };

    const handleSearch = async (query) => {
        setSearchQuery(query);
        setError('');

        if (query.trim().length < 3) {
            setSearchResults([]);
            return;
        }

        try {
            const results = await collaborationService.searchUsers(query);
            setSearchResults(results || []);
        } catch (error) {
            console.error('Search failed:', error);
            setSearchResults([]);
        }
    };

    const handleInvite = async (userEmail) => {
        setLoading(true);
        setError('');

        try {
            await collaborationService.inviteCollaborator(eventId, userEmail);
            setShowInviteModal(false);
            setSearchQuery('');
            setSearchResults([]);
            alert('Invitation sent successfully!');
        } catch (error) {
            setError(error.response?.data?.message || 'Failed to send invitation');
        } finally {
            setLoading(false);
        }
    };

    const handleRemove = async (userId) => {
        if (!window.confirm('Are you sure you want to remove this collaborator?')) {
            return;
        }

        try {
            await collaborationService.removeCollaborator(eventId, userId);
            await loadCollaborators();
        } catch (error) {
            alert(error.response?.data?.message || 'Failed to remove collaborator');
        }
    };

    return (
        <div className="collaborators-tab">
            <div className="tab-header">
                <h3>Team Collaborators</h3>
                <button
                    className="btn btn-primary"
                    onClick={() => setShowInviteModal(true)}
                >
                    + Invite Collaborator
                </button>
            </div>

            {collaborators.length === 0 ? (
                <div className="empty-state">
                    <p>No collaborators yet. Invite team members to help manage this event!</p>
                </div>
            ) : (
                <div className="collaborators-list">
                    {collaborators.map(collab => (
                        <div key={collab.userId} className="collaborator-card">
                            <div className="collaborator-info">
                                <div className="avatar">👤</div>
                                <div className="details">
                                    <h4>{collab.name}</h4>
                                    <p>{collab.email}</p>
                                    <span className="role-badge">{collab.role}</span>
                                </div>
                            </div>
                            <button
                                className="btn-remove"
                                onClick={() => handleRemove(collab.userId)}
                            >
                                Remove
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {showInviteModal && (
                <div className="modal-overlay" onClick={() => setShowInviteModal(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Invite Collaborator</h3>
                            <button
                                className="close-btn"
                                onClick={() => setShowInviteModal(false)}
                            >
                                ×
                            </button>
                        </div>

                        <div className="modal-body">
                            <input
                                type="email"
                                className="search-input"
                                placeholder="Search by email..."
                                value={searchQuery}
                                onChange={(e) => handleSearch(e.target.value)}
                                autoFocus
                            />

                            {error && <div className="error-message">{error}</div>}

                            {searchResults.length > 0 && (
                                <div className="search-results">
                                    {searchResults.map(user => (
                                        <div key={user.id} className="user-result">
                                            <div className="user-info">
                                                <div className="avatar-small">👤</div>
                                                <div>
                                                    <div className="user-name">{user.name}</div>
                                                    <div className="user-email">{user.email}</div>
                                                </div>
                                            </div>
                                            <button
                                                className="btn btn-sm btn-primary"
                                                onClick={() => handleInvite(user.email)}
                                                disabled={loading}
                                            >
                                                {loading ? 'Sending...' : 'Invite'}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {searchQuery.trim().length >= 3 && searchResults.length === 0 && (
                                <div className="no-results">No users found</div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CollaboratorsTab;
