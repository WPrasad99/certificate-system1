import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import './Auth.css';
import LoadingOverlay from './LoadingOverlay';

function Login() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [showRedirectLoading, setShowRedirectLoading] = useState(false);

    // Typing animation logic
    const fullText = "Streamline your certificate workflow\nwith automated generation and dispatch.";
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
            await authService.login(formData);
            setShowRedirectLoading(true);
            setTimeout(() => {
                navigate('/dashboard');
            }, 4000);
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
            setLoading(false);
        }
    };

    const handleGoogleLogin = () => {
        // Use relative path so it goes through Vite proxy (works on mobile/network)
        window.location.href = '/api/oauth2/authorization/google';
    };

    return (
        <div className="auth-container">
            {showRedirectLoading && <LoadingOverlay />}
            <div className="login-card-landscape">
                <div className="login-left">
                    <div className="brand-section">
                        <div className="logo-placeholder">
                            <img src="/assets/bharti_logo.png" alt="Bharati Vidyapeeth Logo" className="logo-image" />
                        </div>
                        <h1 className="welcome-text">Welcome !</h1>
                        <div className="typing-container">
                            <p className="brand-tagline">
                                {displayText}
                                <span className="typing-cursor"></span>
                            </p>
                        </div>
                    </div>

                    <div className="features-grid">
                        <div className="feature-card">
                            <span className="feature-icon">⚡</span>
                            <h3>Fast Generation</h3>
                            <p>Generate certificates instantly</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">🔒</span>
                            <h3>Secure</h3>
                            <p>Tamper-proof verification</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">📤</span>
                            <h3>Easy Dispatch</h3>
                            <p>Send via email in one click</p>
                        </div>
                        <div className="feature-card">
                            <span className="feature-icon">📊</span>
                            <h3>Analytics</h3>
                            <p>Track your event success</p>
                        </div>
                    </div>
                </div>

                <div className="login-right">
                    <div className="form-header">
                        <h2>Login Account</h2>
                        <p className="auth-subtitle">Welcome back! Please enter your details.</p>
                    </div>

                    {error && <div className="alert alert-error">{error}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Email</label>
                            <input
                                type="email"
                                name="email"
                                className="form-input"
                                placeholder="Enter your email"
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
                                placeholder="••••••••"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                            {loading ? 'Logging in...' : 'Sign in'}
                        </button>
                    </form>

                    <p className="auth-footer">
                        Don't have an account? <Link to="/register">Sign up</Link>
                    </p>

                    <div className="btn-divider">OR</div>

                    <button onClick={handleGoogleLogin} className="google-btn">
                        <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="Google" width="18" height="18" />
                        Sign in with Google
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Login;
