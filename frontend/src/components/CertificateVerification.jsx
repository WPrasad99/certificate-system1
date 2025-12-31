import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import './CertificateVerification.css';

function CertificateVerification() {
    const { verificationId } = useParams();
    const [certificate, setCertificate] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (verificationId) {
            verifyCertificate();
        }
    }, [verificationId]);

    const verifyCertificate = async () => {
        try {
            setLoading(true);
            const response = await fetch(`/api/certificates/verify/${verificationId}`);

            if (!response.ok) {
                throw new Error('Certificate not found or invalid');
            }

            const data = await response.json();
            setCertificate(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="verification-container">
                <div className="verification-card" style={{ display: 'block', padding: '60px' }}>
                    <div className="loader-container">
                        <div className="loader"></div>
                        <p className="loading-text">Verifying Authenticity...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="verification-container">
                <div className="verification-card" style={{ display: 'block', maxWidth: '500px', textAlign: 'center', padding: '40px' }}>
                    <div style={{ fontSize: '48px', marginBottom: '20px' }}>⚠️</div>
                    <div className="verification-status">
                        <h1>Certificate Invalid</h1>
                        <p className="error-message">{error}</p>
                        <span className="error-badge">Verification Failed</span>
                        <p style={{ marginTop: '20px', fontSize: '13px', color: '#94a3b8' }}>
                            We could not verify this certificate. It may be invalid or revoked.
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="verification-container">
            <div className="verification-card">
                {/* Left Side: Logo & Description */}
                <div className="card-left">
                    <div className="logo-container">
                        <img src="/bv_full_logo_v2.png" alt="Bharati Vidyapeeth Logo" onError={(e) => e.target.style.display = 'none'} />
                    </div>

                    <div className="university-info">
                        <p className="university-description">
                            Bharati Vidyapeeth (Deemed to be University) is a premier institute of higher learning in India. Established in 1964, it is known for its academic excellence and innovative research across disciplined fields.
                            The National Institutional Ranking Framework (NIRF) ranked Bharati Vidyapeeth 78th among universities for 2024. The university's constituent engineering college, Bharati Vidyapeeth Deemed University College of Engineering, Pune was ranked in the 151-200 band in 2024.
                        </p>
                    </div>

                    <div className="trusted-text-container">
                        <p>Trusted verification by CertiCraft</p>
                    </div>
                </div>

                {/* Right Side: Details */}
                <div className="card-right">
                    <div className="detail-group">
                        <span className="detail-label">Certified Participant</span>
                        <span className="detail-value highlight">{certificate.participantName}</span>
                    </div>

                    <div className="detail-group">
                        <span className="detail-label">Event Name</span>
                        <span className="detail-value">{certificate.eventName}</span>
                    </div>

                    <div style={{ display: 'flex', gap: '20px' }}>
                        <div className="detail-group" style={{ flex: 1 }}>
                            <span className="detail-label">Issue Date</span>
                            <span className="detail-value">{new Date(certificate.generatedAt).toLocaleDateString()}</span>
                        </div>
                        <div className="detail-group" style={{ flex: 1 }}>
                            {/* Placeholder for future expansion */}
                        </div>
                    </div>

                    <div className="detail-group">
                        <span className="detail-label">Organized By</span>
                        <span className="detail-value">{certificate.organizerName}</span>
                    </div>

                    <div className="detail-group">
                        <span className="detail-label">Verification ID</span>
                        <span className="verification-id">{certificate.verificationId}</span>
                    </div>

                    <div className="verification-footer">
                        Securely verified. Reference ID is unique to this document.
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CertificateVerification;
