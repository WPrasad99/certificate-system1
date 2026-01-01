import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import './Auth.css';
import LoadingOverlay from './LoadingOverlay';

function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        instituteName: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [showRedirectLoading, setShowRedirectLoading] = useState(false);

    // Typing animation logic for Register
    const fullText = "Empower your organization with\nseamless certificate management.";
    const [displayText, setDisplayText] = useState('');
    const [index, setIndex] = useState(0);

    useEffect(() => {
        if (index < fullText.length) {
            const timeout = setTimeout(() => {
                setDisplayText(prev => prev + fullText.charAt(index));
                setIndex(prev => prev + 1);
            }, 50);
            return () => clearTimeout(timeout);
        }
    }, [index]);

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
            await authService.register(formData);
            setShowRedirectLoading(true);
            setTimeout(() => {
                navigate('/dashboard');
            }, 4000);
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Please try again.');
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            {showRedirectLoading && <LoadingOverlay />}
            <div className="auth-card-landscape">
                <div className="login-left">
                    <div className="brand-section">
                        <div className="logo-placeholder">
                            <img src="/assets/bharti_logo.png" alt="Logo" className="logo-image" />
                        </div>
                        <h1 className="welcome-text">Join Us !</h1>
                        <div className="typing-container">
                            <p className="brand-tagline">
                                {displayText}
                                <span className="typing-cursor"></span>
                            </p>
                        </div>
                    </div>

                    <div className="features-grid">
                        <div className="feature-card">
                            <span className="feature-icon">🚀</span>
                            <h3>Scale Effortlessly</h3>
                            <p>Handle any number of participants</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">🎨</span>
                            <h3>Custom Designs</h3>
                            <p>Beautiful templates for every event</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">📈</span>
                            <h3>Growth Tools</h3>
                            <p>Manage multiple events with ease</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">🤝</span>
                            <h3>Collaboration</h3>
                            <p>Work together with your team</p>
                        </div>
                    </div>
                </div>

                <div className="login-right">
                    <div className="form-header">
                        <h2>Create Account</h2>
                        <p className="auth-subtitle">Get started with CertiCraft today.</p>
                    </div>

                    {error && <div className="alert alert-error">{error}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Full Name</label>
                            <input
                                type="text"
                                name="fullName"
                                className="form-input"
                                placeholder="Enter your full name"
                                value={formData.fullName}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Email Address</label>
                            <input
                                type="email"
                                name="email"
                                className="form-input"
                                placeholder="name@example.com"
                                value={formData.email}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <input
                                type="password"
                                name="password"
                                className="form-input"
                                placeholder="Create a strong password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                                minLength="6"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Institute Name (Optional)</label>
                            <input
                                type="text"
                                name="instituteName"
                                className="form-input"
                                placeholder="University or Organization Name"
                                value={formData.instituteName}
                                onChange={handleChange}
                            />
                        </div>

                        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                            {loading ? 'Creating account...' : 'Create Account'}
                        </button>
                    </form>

                    <p className="auth-footer">
                        Already have an account? <Link to="/login">Sign in</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Register;
