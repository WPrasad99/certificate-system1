import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, eventService, analyticsService, collaborationService, messageService } from '../services/authService';
import EventManagement from './EventManagement';
import Modal from './Modal';
import AnalyticsCharts from './AnalyticsCharts';
import CollaborationRequests from './CollaborationRequests';
import './Dashboard.css';

function Dashboard() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [events, setEvents] = useState([]);
    const [stats, setStats] = useState({ totalEvents: 0, totalCertificates: 0 });
    const [loading, setLoading] = useState(true);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [initialTab, setInitialTab] = useState('participants');
    const [modal, setModal] = useState({ isOpen: false, eventId: null });
    const [notifications, setNotifications] = useState([]);
    const [showNotifications, setShowNotifications] = useState(false);
    const [isNotifVibrating, setIsNotifVibrating] = useState(false);

    // Collaboration Request State
    const [pendingRequests, setPendingRequests] = useState([]);
    const [showRequestsDropdown, setShowRequestsDropdown] = useState(false);

    const requestsDropdownRef = useRef(null);
    const notificationsDropdownRef = useRef(null);

    useEffect(() => {
        function handleClickOutside(event) {
            if (requestsDropdownRef.current && !requestsDropdownRef.current.contains(event.target)) {
                setShowRequestsDropdown(false);
            }
            if (notificationsDropdownRef.current && !notificationsDropdownRef.current.contains(event.target)) {
                setShowNotifications(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // Vibrate on new requests
    useEffect(() => {
        if (pendingRequests.length > 0) {
            if (window.navigator.vibrate) {
                window.navigator.vibrate([100, 50, 100]);
            }
        }
    }, [pendingRequests.length]);

    useEffect(() => {
        loadData();
        loadRequests();
        const interval = setInterval(loadRequests, 10000);
        return () => clearInterval(interval);
    }, []);

    const loadRequests = async () => {
        try {
            const [reqs, sentReqs, actionLogs, unreadMsgs] = await Promise.all([
                collaborationService.getRequests(),
                collaborationService.getSentRequests(),
                collaborationService.getOwnedEventsLogs(),
                messageService.getUnreadMessages()
            ]);
            setPendingRequests(reqs || []);

            // Process unread messages as notifications
            if (Array.isArray(unreadMsgs)) {
                unreadMsgs.forEach(msg => {
                    const notifKey = `msg_${msg.id}`;
                    const alreadyToastShown = localStorage.getItem(notifKey);

                    // Add to notifications list if not already present
                    setNotifications(prev => {
                        if (prev.some(n => n.id === msg.id || n.msgId === msg.id)) return prev;

                        const newNotif = {
                            id: msg.id,
                            msgId: msg.id,
                            type: 'info',
                            message: `New message from ${msg.senderName}: ${msg.content.substring(0, 30)}${msg.content.length > 30 ? '...' : ''}`,
                            time: new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
                            eventId: msg.eventId,
                            targetTab: 'messages'
                        };

                        if (!alreadyToastShown) {
                            setIsNotifVibrating(true);
                            setTimeout(() => setIsNotifVibrating(false), 500);
                            localStorage.setItem(notifKey, 'true');
                        }

                        return [newNotif, ...prev].slice(0, 10);
                    });
                });
            }

            // Process sent requests as notifications for the owner
            if (Array.isArray(sentReqs)) {
                sentReqs.forEach(req => {
                    const statusMsg = req.status === 'ACCEPTED' ? 'accepted' : 'declined';
                    const notifKey = `sent_req_${req.id}_${req.status}`;
                    const alreadyToastShown = localStorage.getItem(notifKey);

                    setNotifications(prev => {
                        const uniqueId = `req_${req.id}_${req.status}`;
                        if (prev.some(n => n.id === uniqueId)) return prev;

                        const newNotif = {
                            id: uniqueId,
                            type: req.status === 'ACCEPTED' ? 'success' : 'info',
                            message: `${req.senderName} ${statusMsg} your invitation for ${req.eventName}`,
                            time: 'Update',
                            eventId: req.eventId,
                            targetTab: 'team'
                        };

                        if (!alreadyToastShown) {
                            setIsNotifVibrating(true);
                            setTimeout(() => setIsNotifVibrating(false), 500);
                            localStorage.setItem(notifKey, 'true');
                        }

                        return [newNotif, ...prev].slice(0, 10);
                    });
                });
            }

            // Process collaborator actions as notifications for the owner
            if (Array.isArray(actionLogs)) {
                actionLogs.forEach(log => {
                    const notifKey = `action_log_${log.id}`;
                    const alreadyToastShown = localStorage.getItem(notifKey);

                    setNotifications(prev => {
                        if (prev.some(n => n.id === `log_${log.id}`)) return prev;

                        const newNotif = {
                            id: `log_${log.id}`,
                            type: 'info',
                            message: `${log.userName}: ${log.action.replace(/_/g, ' ')}`,
                            time: 'Log',
                            eventId: log.eventId,
                            targetTab: 'team'
                        };

                        if (!alreadyToastShown) {
                            setIsNotifVibrating(true);
                            setTimeout(() => setIsNotifVibrating(false), 500);
                            localStorage.setItem(notifKey, 'true');
                        }

                        return [newNotif, ...prev].slice(0, 10);
                    });
                });
            }
        } catch (error) {
            console.error('Failed to load requests:', error);
        }
    };

    const handleAcceptRequest = async (id) => {
        try {
            await collaborationService.acceptRequest(id);
            addNotification('success', 'Invitation accepted!');
            loadRequests();
            loadData(); // Reload events to see the new one
        } catch (error) {
            addNotification('error', 'Failed to accept invitation');
        }
    };

    const handleDeclineRequest = async (id) => {
        try {
            await collaborationService.declineRequest(id);
            addNotification('info', 'Invitation declined');
            loadRequests();
        } catch (error) {
            addNotification('error', 'Failed to decline invitation');
        }
    };

    const loadData = async () => {
        setLoading(true);
        try {
            const currentUser = authService.getCurrentUser();
            setUser(currentUser);
            const [eventsData, statsData] = await Promise.all([
                eventService.getAllEvents(),
                analyticsService.getStats()
            ]);
            setEvents(eventsData);
            setStats(statsData);
        } catch (error) {
            console.error('Failed to load data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    const addNotification = (type, message, eventId = null, targetTab = null) => {
        const newNotification = {
            id: Date.now(),
            type,
            message,
            time: 'Just now',
            eventId,
            targetTab
        };
        setNotifications(prev => [newNotification, ...prev].slice(0, 10));
        setIsNotifVibrating(true);
        setTimeout(() => setIsNotifVibrating(false), 500);
    };

    const handleNotificationClick = (notif) => {
        if (notif.eventId) {
            // Find the event in our list
            const event = events.find(e => Number(e.id) === Number(notif.eventId));
            if (event) {
                setInitialTab(notif.targetTab || 'participants');
                setSelectedEvent(event);
                setShowNotifications(false);
            }
        }
    };

    const handleEventSelect = (event) => {
        setInitialTab('participants');
        setSelectedEvent(event);
    };

    const handleBackToDashboard = () => {
        setSelectedEvent(null);
        loadData();
    };

    const confirmDeletion = async () => {
        try {
            await eventService.deleteEvent(modal.eventId);
            addNotification('success', `Event "${events.find(e => e.id === modal.eventId)?.eventName}" deleted`);
            loadData();
        } catch (error) {
            addNotification('error', error.response?.data?.error || 'Failed to delete event');
        }
    };

    if (loading) {
        return <div className="spinner"></div>;
    }

    if (selectedEvent) {
        return <EventManagement
            event={selectedEvent}
            initialTab={initialTab}
            onBack={handleBackToDashboard}
            onNotify={addNotification}
        />;
    }

    return (
        <div className="dashboard-container">
            <nav className="navbar">
                <div className="navbar-content">
                    <div className="navbar-brand">
                        <div className="brand-logo-container">
                            <img src="/assets/bharti_logo.png" alt="Logo" className="navbar-logo" />
                            <div className="brand-text-container">
                                <h2>CertiCraft</h2>
                                <div className="brand-line"></div>
                            </div>
                        </div>
                    </div>
                    <div className="secondary-actions">
                        <div className="navbar-actions">
                            {/* Notification Bell Icon */}
                            <div className="notifications-container" ref={notificationsDropdownRef}>
                                <button
                                    className={`notifications-btn ${isNotifVibrating ? 'vibrate-bt' : ''}`}
                                    onClick={() => {
                                        setShowNotifications(!showNotifications);
                                        setShowRequestsDropdown(false);
                                    }}
                                    title="Notifications"
                                >
                                    <i className="fa-solid fa-bell" style={{ fontSize: '18px', color: '#1e3a8a' }}></i>
                                    {notifications.length > 0 &&
                                        <span className="notification-badge">{notifications.length}</span>
                                    }
                                </button>

                                {showNotifications && (
                                    <div className="notifications-dropdown">
                                        <div className="notifications-header">
                                            <h3>Notifications</h3>
                                        </div>
                                        <div className="notifications-list">
                                            {notifications.length === 0 ? (
                                                <div className="notification-item" style={{ textAlign: 'center', color: '#888' }}>
                                                    No new notifications
                                                </div>
                                            ) : (
                                                notifications.map(notif => (
                                                    <div
                                                        key={notif.id}
                                                        className={`notification-item ${notif.type} ${notif.eventId ? 'clickable' : ''}`}
                                                        onClick={() => handleNotificationClick(notif)}
                                                    >
                                                        <div className="notification-message">{notif.message}</div>
                                                        <div className="notification-time">{notif.time}</div>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>

                            {/* Team Collaboration Icon - Shifted closer to Bell */}
                            <div className="notifications-container" style={{ marginLeft: '12px' }} ref={requestsDropdownRef}>
                                <button
                                    className={`notifications-btn ${pendingRequests.length > 0 ? 'vibrate-bt' : ''}`}
                                    onClick={() => {
                                        setShowRequestsDropdown(!showRequestsDropdown);
                                        setShowNotifications(false);
                                    }}
                                    title="Collaboration Requests"
                                >
                                    <i className="fa-solid fa-user-plus" style={{ fontSize: '18px', color: '#1e3a8a' }}></i>
                                    {Array.isArray(pendingRequests) && pendingRequests.length > 0 &&
                                        <span className="notification-badge" style={{ background: '#333', color: 'white' }}>{pendingRequests.length}</span>
                                    }
                                </button>

                                {showRequestsDropdown && (
                                    <div className="notifications-dropdown minimal-dropdown" style={{ width: '320px', right: '0' }}>
                                        <div className="notifications-header minimal-header">
                                            <h3>Team invitations</h3>
                                        </div>
                                        <div className="notifications-list">
                                            {(!Array.isArray(pendingRequests) || pendingRequests.length === 0) ? (
                                                <div className="notification-item" style={{ justifyContent: 'center', color: '#888' }}>
                                                    No pending invitations
                                                </div>
                                            ) : (
                                                pendingRequests.map(req => (
                                                    <div key={req.id} className="notification-item" style={{ display: 'block' }}>
                                                        <div className="notification-message" style={{ marginBottom: '8px' }}>
                                                            <strong>{req.eventName}</strong>
                                                            <div style={{ fontSize: '12px', color: '#666' }}>From: {req.senderName}</div>
                                                        </div>
                                                        <div style={{ display: 'flex', gap: '8px' }}>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#4caf50', color: 'white', flex: 1 }}
                                                                onClick={() => handleAcceptRequest(req.id)}
                                                            >
                                                                Accept
                                                            </button>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#f44336', color: 'white', flex: 1 }}
                                                                onClick={() => handleDeclineRequest(req.id)}
                                                            >
                                                                Decline
                                                            </button>
                                                        </div>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                        <button onClick={handleLogout} className="btn btn-secondary btn-sm">
                            Logout
                        </button>
                    </div>
                </div>
            </nav>

            <div className="container">
                <div className="dashboard-header">
                    <div className="welcome-banner">
                        <span className="welcome-label">Welcome back,</span>
                        <h2 className="welcome-user">{user?.fullName}</h2>
                    </div>
                </div>

                <EventList
                    events={events}
                    onEventSelect={handleEventSelect}
                    onDeleteRequest={(id) => setModal({ isOpen: true, eventId: id })}
                    onRefresh={loadData}
                    onNotify={addNotification}
                />

                <div className="stats-container">
                    <div className="stats-card">
                        <h4>Total Events</h4>
                        <div className="stats-value">{stats.totalEvents}</div>
                    </div>
                    <div className="stats-card">
                        <h4>Certificates Generated</h4>
                        <div className="stats-value">{stats.totalCertificates}</div>
                    </div>
                </div>

                <CollaborationRequests />

                <AnalyticsCharts stats={stats} events={events} />
            </div>

            <Modal
                isOpen={modal.isOpen}
                onClose={() => setModal({ isOpen: false, eventId: null })}
                onConfirm={confirmDeletion}
                title="Delete Event"
                message="Are you sure you want to delete this event? All participants and certificates will be permanently removed."
            />
        </div>
    );
}

function EventList({ events, onEventSelect, onDeleteRequest, onRefresh, onNotify }) {
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [formData, setFormData] = useState({
        eventName: '',
        eventDate: '',
        organizerName: '',
        instituteName: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await eventService.createEvent(formData);
            onRefresh();
            onNotify?.('success', `Event "${formData.eventName}" created successfully`);
            setShowCreateForm(false);
            setFormData({
                eventName: '',
                eventDate: '',
                organizerName: '',
                instituteName: '',
            });
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to create event');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteClick = (e, eventId) => {
        e.stopPropagation();
        onDeleteRequest(eventId);
    };

    return (
        <div className="event-section">
            <div className="section-header">
                <h2>My Events</h2>
                <button
                    onClick={() => setShowCreateForm(!showCreateForm)}
                    className="btn btn-primary"
                >
                    {showCreateForm ? 'Cancel' : '+ Create Event'}
                </button>
            </div>

            {showCreateForm && (
                <div className="card">
                    <h3>Create New Event</h3>
                    {error && <div className="alert alert-error">{error}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Event Name</label>
                                <input
                                    type="text"
                                    name="eventName"
                                    className="form-input"
                                    placeholder="Annual Tech Summit 2024"
                                    value={formData.eventName}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Event Date</label>
                                <input
                                    type="date"
                                    name="eventDate"
                                    className="form-input"
                                    min={new Date().toISOString().split('T')[0]}
                                    value={formData.eventDate}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Organizer Name</label>
                                <input
                                    type="text"
                                    name="organizerName"
                                    className="form-input"
                                    placeholder="Dr. John Smith"
                                    value={formData.organizerName}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Institute Name (Optional)</label>
                                <input
                                    type="text"
                                    name="instituteName"
                                    className="form-input"
                                    placeholder="ABC University"
                                    value={formData.instituteName}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Event'}
                        </button>
                    </form>
                </div>
            )}

            <div className="events-grid">
                {events.length === 0 ? (
                    <div className="card empty-state">
                        <p>No events yet. Create your first event to get started!</p>
                    </div>
                ) : (
                    events.map((event) => (
                        <div key={event.id} className="event-card">
                            <button
                                className="delete-event-btn"
                                onClick={(e) => handleDeleteClick(e, event.id)}
                                title="Delete Event"
                            >
                                &times;
                            </button>

                            <div className="event-card-header">
                                <div className="event-date-badge">
                                    <span className="event-date-day">{new Date(event.eventDate).getDate()}</span>
                                    <span className="event-date-month">{new Date(event.eventDate).toLocaleString('default', { month: 'short' })}</span>
                                </div>
                                <div className="event-card-title-group">
                                    <h3>{event.eventName}</h3>
                                    <p className="event-institute-text">{event.instituteName || 'General Event'}</p>
                                </div>
                            </div>

                            <div className="event-card-body">
                                <div className="event-info-item">
                                    <span className="info-icon">👤</span>
                                    <span className="event-organizer">{event.organizerName}</span>
                                </div>
                                <div className="event-info-item">
                                    <span className="info-icon">📅</span>
                                    <span className="event-full-date">{new Date(event.eventDate).toLocaleDateString()}</span>
                                </div>
                            </div>

                            <div className="card-footer">
                                <span className="manage-link" onClick={() => onEventSelect(event)}>Manage Event <span>→</span></span>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default Dashboard;
