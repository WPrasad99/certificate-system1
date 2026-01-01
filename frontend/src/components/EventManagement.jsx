import React, { useState, useEffect } from 'react';
import { participantService, certificateService } from '../services/authService';
import './EventManagement.css';
import CollaboratorsTab from './CollaboratorsTab';

import Toast from './Toast';

function EventManagement({ event, onBack, onNotify }) {
    const [activeTab, setActiveTab] = useState('participants');
    const [participants, setParticipants] = useState([]);
    const [certificateStatus, setCertificateStatus] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isVibrating, setIsVibrating] = useState(false);

    // Toast State
    const [toast, setToast] = useState({ show: false, message: '', type: '' });

    const showToast = (message, type = 'success') => {
        setToast({ show: true, message, type });
    };

    const hideToast = () => {
        setToast({ ...toast, show: false });
    };

    const triggerVibration = () => {
        setIsVibrating(true);
        showToast('First upload participants list', 'error');
        setTimeout(() => setIsVibrating(false), 400);
    };

    useEffect(() => {
        loadParticipants();
        loadCertificateStatus();
    }, [event.id]);

    // Polling for certificate status if any are PENDING or SENDING
    useEffect(() => {
        if (!Array.isArray(certificateStatus)) return;

        const needsPolling = certificateStatus.some(
            cert => cert.generationStatus === 'PENDING' ||
                cert.emailStatus === 'SENDING' ||
                cert.updateEmailStatus === 'SENDING'
        );

        if (needsPolling) {
            const interval = setInterval(() => {
                loadCertificateStatus();
            }, 3000); // Poll every 3 seconds
            return () => clearInterval(interval);
        }
    }, [certificateStatus]);

    const loadParticipants = async () => {
        try {
            const data = await participantService.getParticipants(event.id);
            setParticipants(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Failed to load participants:', error);
            setParticipants([]);
        }
    };

    const handleDeleteParticipant = async (participantId) => {
        try {
            await participantService.deleteParticipant(event.id, participantId);
            showToast('Participant removed', 'success');
            await loadParticipants();
            await loadCertificateStatus();
        } catch (error) {
            showToast('Failed to remove participant', 'error');
        }
    };

    const handleDeleteAllParticipants = async () => {
        if (!window.confirm('Are you sure you want to remove ALL participants? This will also delete any generated certificates.')) return;

        setLoading(true);
        try {
            await participantService.deleteAllParticipants(event.id);
            showToast('All participants removed', 'success');
            await loadParticipants();
            await loadCertificateStatus();
        } catch (error) {
            showToast('Failed to remove all participants', 'error');
        } finally {
            setLoading(false);
        }
    };

    const loadCertificateStatus = async () => {
        try {
            const data = await certificateService.getCertificateStatus(event.id);
            setCertificateStatus(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Failed to load certificate status:', error);
            setCertificateStatus([]);
        }
    };

    const handleFileUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setLoading(true);
        // Toast handled in success/catch

        try {
            await participantService.uploadParticipants(event.id, file);
            showToast('Participants uploaded successfully!', 'success');
            onNotify?.('success', `Participants uploaded for ${event.eventName}`);
            await loadParticipants();
            await loadCertificateStatus();
        } catch (error) {
            const msg = error.response?.data?.error || 'Failed to upload participants';
            showToast(msg, 'error');
            onNotify?.('error', msg);
        } finally {
            setLoading(false);
            e.target.value = '';
        }
    };

    const handleGenerateCertificates = async () => {
        if (participants.length === 0) {
            triggerVibration();
            return;
        }

        setLoading(true);

        try {
            await certificateService.generateCertificates(event.id);
            showToast('Certificates generated successfully!', 'success');
            onNotify?.('success', `Certificates generated for ${event.eventName}`);
            await loadCertificateStatus();
            setActiveTab('certificates');
        } catch (error) {
            const msg = error.response?.data?.error || 'Failed to generate certificates';
            showToast(msg, 'error');
            onNotify?.('error', msg);
        } finally {
            setLoading(false);
        }
    };

    const handleDownloadCertificate = async (certificateId) => {
        try {
            const blob = await certificateService.downloadCertificate(certificateId);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `certificate_${certificateId}.pdf`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            showToast('Failed to download certificate', 'error');
        }
    };

    const handleDownloadAll = async () => {
        setLoading(true);
        try {
            const blob = await certificateService.downloadAllCertificates(event.id);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${event.eventName}_certificates.zip`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            showToast('All certificates downloaded!', 'success');
        } catch (error) {
            showToast(error.response?.data?.error || 'Failed to download certificates', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleSendEmail = async (certificateId) => {
        // Optimistic UI update: Set status to SENDING locally
        setCertificateStatus(prev => prev.map(cert =>
            cert.id === certificateId ? { ...cert, emailStatus: 'SENDING' } : cert
        ));

        try {
            await certificateService.sendCertificateEmail(certificateId);
            // After triggering, load actual status from backend
            await loadCertificateStatus();
        } catch (error) {
            showToast('Failed to send email. Check SMTP configuration.', 'error');
            await loadCertificateStatus(); // Restore actual status
        }
    };

    const handleSendAllEmails = async () => {
        // Optimistic UI update for all generated certificates
        setCertificateStatus(prev => prev.map(cert =>
            cert.generationStatus === 'GENERATED' ? { ...cert, emailStatus: 'SENDING' } : cert
        ));

        setLoading(true);
        try {
            await certificateService.sendAllEmails(event.id);
            // No alert as requested
            await loadCertificateStatus();
        } catch (error) {
            const msg = 'Failed to send emails. Check SMTP configuration.';
            showToast(msg, 'error');
            onNotify?.('error', msg);
            await loadCertificateStatus();
        } finally {
            setLoading(false);
        }
    };

    const handleSendUpdates = async (updateData) => {
        // Optimistic UI update: Set status to SENDING for all participants
        setCertificateStatus(prev => {
            if (!Array.isArray(prev)) return prev;
            return prev.map(cert => ({
                ...cert,
                updateEmailStatus: 'SENDING'
            }));
        });

        setLoading(true);
        try {
            await certificateService.sendUpdateEmails(event.id, updateData.subject, updateData.content);
            // No alert as requested
            onNotify?.('success', `Updates sent for ${event.eventName}`);
            // Trigger a reload to catch up with backend state
            setTimeout(loadCertificateStatus, 1000);
        } catch (error) {
            const msg = error.response?.data?.error || 'Failed to send updates';
            showToast(msg, 'error');
            onNotify?.('error', msg);
            // Revert status on failure (optional, but good practice is to reload)
            await loadCertificateStatus();
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="dashboard-container">
            {toast.show && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={hideToast}
                />
            )}
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
                            <button onClick={onBack} className="btn btn-secondary btn-sm">
                                ← Back
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <div className={`container ${isVibrating ? 'vibrate' : ''}`}>
                <div className="event-header">
                    <h1>{event.eventName}</h1>
                    <p className="event-meta">
                        {new Date(event.eventDate).toLocaleDateString()} • {event.organizerName}
                    </p>
                </div>

                <div className="tabs">
                    <button
                        className={`tab ${activeTab === 'participants' ? 'active' : ''}`}
                        onClick={() => setActiveTab('participants')}
                    >
                        Participants
                    </button>
                    <button
                        className={`tab ${activeTab === 'certificates' ? 'active' : ''}`}
                        onClick={() => setActiveTab('certificates')}
                    >
                        Certificates
                    </button>
                    <button
                        className={`tab ${activeTab === 'updates' ? 'active' : ''}`}
                        onClick={() => setActiveTab('updates')}
                    >
                        Send Updates
                    </button>
                    <button
                        className={`tab ${activeTab === 'team' ? 'active' : ''}`}
                        onClick={() => setActiveTab('team')}
                    >
                        Team
                    </button>
                </div>

                {activeTab === 'participants' && (
                    <ParticipantsTab
                        participants={participants}
                        certificateStatus={certificateStatus}
                        onFileUpload={handleFileUpload}
                        onGenerateCertificates={handleGenerateCertificates}
                        onDeleteParticipant={handleDeleteParticipant}
                        onDeleteAllParticipants={handleDeleteAllParticipants}
                        onGoToUpdates={() => participants.length > 0 ? setActiveTab('updates') : triggerVibration()}
                        loading={loading}
                    />
                )}

                {activeTab === 'certificates' && (
                    <CertificatesTab
                        certificates={certificateStatus}
                        onDownloadCertificate={handleDownloadCertificate}
                        onDownloadAll={handleDownloadAll}
                        onSendEmail={handleSendEmail}
                        onSendAllEmails={handleSendAllEmails}
                        loading={loading}
                    />
                )}

                {activeTab === 'updates' && (
                    <UpdatesTab
                        onSendUpdates={handleSendUpdates}
                        loading={loading}
                        participantCount={participants.length}
                        certificateStatus={certificateStatus}
                    />
                )}

                {activeTab === 'team' && (
                    <CollaboratorsTab eventId={event.id} />
                )}
            </div>
        </div>
    );
}

function ParticipantsTab({
    participants,
    certificateStatus = [],
    onFileUpload,
    onGenerateCertificates,
    onDeleteParticipant,
    onDeleteAllParticipants,
    onGoToUpdates,
    loading
}) {
    return (
        <div className="tab-content">
            <div className="card">
                <h3>Upload Participants</h3>
                <p className="help-text">
                    Upload a CSV or Excel file with columns: <strong>Name</strong> and <strong>Email</strong>
                </p>

                <div className="upload-section action-grid">
                    <input
                        type="file"
                        accept=".csv,.xlsx,.xls"
                        onChange={onFileUpload}
                        disabled={loading}
                        id="file-upload"
                        className="file-input"
                    />
                    <label htmlFor="file-upload" className="btn btn-primary action-btn">
                        Choose File
                    </label>

                    <button
                        onClick={onGenerateCertificates}
                        className="btn btn-primary action-btn"
                        disabled={loading}
                    >
                        Generate Certificates
                    </button>

                    <button
                        onClick={onGoToUpdates}
                        className="btn btn-primary action-btn"
                        disabled={loading}
                    >
                        Send Updates
                    </button>
                </div>
            </div>

            {participants.length > 0 && (
                <div className="card">
                    <div className="participant-actions">
                        <h3>Participants List ({participants.length})</h3>
                        <button
                            onClick={onDeleteAllParticipants}
                            className="delete-all-btn"
                            disabled={loading}
                        >
                            Remove All Participants
                        </button>
                    </div>
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {participants.map((p) => (
                                    <tr key={p.id}>
                                        <td>{p.name}</td>
                                        <td>{p.email}</td>
                                        <td>
                                            <button
                                                onClick={() => onDeleteParticipant(p.id)}
                                                className="btn-remove"
                                                disabled={loading}
                                            >
                                                Remove
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}

function CertificatesTab({ certificates, onDownloadCertificate, onDownloadAll, onSendEmail, onSendAllEmails, loading }) {
    const generatedCount = certificates.filter(c => c.generationStatus === 'GENERATED').length;

    return (
        <div className="tab-content">
            <div className="card">
                <div className="certificate-actions">
                    <h3>Certificate Actions</h3>
                    <div className="button-group">
                        <button
                            onClick={onDownloadAll}
                            className="btn btn-primary"
                            disabled={loading || generatedCount === 0}
                        >
                            Download All as ZIP
                        </button>
                        <button
                            onClick={onSendAllEmails}
                            className="btn btn-secondary"
                            disabled={loading || generatedCount === 0}
                        >
                            Email All Certificates
                        </button>
                    </div>
                </div>
            </div>

            {certificates.length > 0 && (
                <div className="card">
                    <h3>Certificate Status</h3>
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Participant Name</th>
                                    <th>Email</th>
                                    <th>Generation Status</th>
                                    <th>Email Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {certificates.map((cert, index) => (
                                    <tr key={index}>
                                        <td>{cert.participantName}</td>
                                        <td>{cert.email}</td>
                                        <td>
                                            <span className={`badge badge-${getStatusClass(cert.generationStatus)}`}>
                                                {cert.generationStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`badge badge-${getStatusClass(cert.emailStatus)}`}>
                                                {cert.emailStatus}
                                            </span>
                                        </td>
                                        <td>
                                            {cert.generationStatus === 'GENERATED' && (
                                                <div className="action-buttons">
                                                    <button
                                                        onClick={() => onDownloadCertificate(cert.id)}
                                                        className="btn btn-primary btn-sm"
                                                    >
                                                        Download
                                                    </button>
                                                    <button
                                                        onClick={() => onSendEmail(cert.id)}
                                                        className="btn btn-secondary btn-sm"
                                                    >
                                                        Email
                                                    </button>
                                                </div>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
}

function getStatusClass(status) {
    const statusMap = {
        'GENERATED': 'success',
        'SENT': 'success',
        'FAILED': 'error',
        'PENDING': 'warning',
        'SENDING': 'info',
        'NOT_GENERATED': 'warning',
        'NOT_SENT': 'neutral',
    };
    return statusMap[status] || 'info';
}

function UpdatesTab({ onSendUpdates, loading, participantCount, certificateStatus = [] }) {
    const [updateData, setUpdateData] = useState({
        subject: '',
        content: ''
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onSendUpdates(updateData);
    };

    return (
        <div className="tab-content updates-grid">
            <div className="status-card">
                <h3>Live Status ({certificateStatus.length})</h3>
                <div className="status-list-container">
                    <table className="status-list-table">
                        <thead>
                            <tr>
                                <th>Participant</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {certificateStatus.map((cert) => (
                                <tr key={cert.id}>
                                    <td>{cert.participantName}</td>
                                    <td>
                                        <span className={`status-badge status-${(cert.updateEmailStatus || 'NOT_SENT').toLowerCase()}`}>
                                            {cert.updateEmailStatus || 'NOT_SENT'}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                            {certificateStatus.length === 0 && (
                                <tr>
                                    <td colSpan="2" style={{ textAlign: 'center', color: '#888' }}>
                                        No participants loaded.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="card">
                <h3>Send Mass Updates</h3>
                <p className="help-text">
                    This message will be sent to all <strong>{participantCount}</strong> participants.
                </p>

                <form onSubmit={handleSubmit} className="update-form">
                    <div className="form-group">
                        <label className="form-label">Email Subject</label>
                        <input
                            type="text"
                            className="form-input"
                            placeholder="e.g., Important Update: Event Date Changed"
                            value={updateData.subject}
                            onChange={(e) => setUpdateData({ ...updateData, subject: e.target.value })}
                            required
                            disabled={loading}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Message Content</label>
                        <textarea
                            className="form-input"
                            style={{ minHeight: '300px', resize: 'vertical' }}
                            placeholder="Write your update message here..."
                            value={updateData.content}
                            onChange={(e) => setUpdateData({ ...updateData, content: e.target.value })}
                            required
                            disabled={loading}
                        />
                        <p className="small-text mt-1">
                            * Note: The organizer's name will be automatically added as a signature.
                        </p>
                    </div>

                    <button
                        type="submit"
                        className="btn btn-primary"
                        style={{ width: '100%', marginTop: '16px' }}
                        disabled={loading || participantCount === 0}
                    >
                        {loading ? 'Sending Emails...' : 'Send Updates to All'}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default EventManagement;
