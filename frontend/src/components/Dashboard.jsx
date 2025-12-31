import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService, eventService, analyticsService } from '../services/authService';
import EventManagement from './EventManagement';
import Modal from './Modal';
import AnalyticsCharts from './AnalyticsCharts';
import './Dashboard.css';

function Dashboard() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [events, setEvents] = useState([]);
    const [stats, setStats] = useState({ totalEvents: 0, totalCertificates: 0 });
    const [loading, setLoading] = useState(true);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [modal, setModal] = useState({ isOpen: false, eventId: null });
    const [notifications, setNotifications] = useState([]);
    const [showNotifications, setShowNotifications] = useState(false);
    const [isNotifVibrating, setIsNotifVibrating] = useState(false);

    useEffect(() => {
        loadData();
    }, []);

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

    const addNotification = (type, message) => {
        const newNotification = {
            id: Date.now(),
            type,
            message,
            time: 'Just now'
        };
        setNotifications(prev => [newNotification, ...prev].slice(0, 10));
        setIsNotifVibrating(true);
        setTimeout(() => setIsNotifVibrating(false), 500);
    };

    const handleEventSelect = (event) => {
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
                            <div className="notifications-container">
                                <button
                                    className={`notifications-btn ${isNotifVibrating ? 'vibrate-bt' : ''}`}
                                    onClick={() => setShowNotifications(!showNotifications)}
                                >
                                    <img
                                        src="https://cdn-icons-png.flaticon.com/512/3119/3119338.png"
                                        alt="Notifications"
                                        className="bell-icon-img"
                                    />
                                    {notifications.length > 0 && <span className="notification-badge">{notifications.length}</span>}
                                </button>

                                {showNotifications && (
                                    <div className="notifications-dropdown">
                                        <div className="notifications-header">
                                            <h3>Recent Alerts</h3>
                                        </div>
                                        <div className="notifications-list">
                                            {notifications.map(n => (
                                                <div key={n.id} className={`notification-item ${n.type}`}>
                                                    <div className="notification-message">{n.message}</div>
                                                    <div className="notification-time">{n.time}</div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                            <button onClick={handleLogout} className="btn btn-secondary btn-sm">
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <div className="container">
                <div className="dashboard-header">
                    <div className="welcome-banner">
                        <span className="welcome-label">Welcome back,</span>
                        <h2 className="welcome-user">{user?.fullName}</h2>
                    </div>
                    <div className="header-title-row">
                        <h1>Dashboard</h1>
                        <p className="subtitle">Manage your events and certificates</p>
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
