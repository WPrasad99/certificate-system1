import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import LoadingOverlay from './LoadingOverlay';

function OAuthCallback() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        const token = searchParams.get('token');
        const fullName = searchParams.get('fullName');
        const email = searchParams.get('email');
        const id = searchParams.get('id');

        if (token) {
            // Store the token and user info in localStorage
            localStorage.setItem('token', token);
            if (fullName && email) {
                localStorage.setItem('user', JSON.stringify({
                    id: id ? parseInt(id) : null,
                    fullName,
                    email
                }));
            }

            // Redirect to dashboard with a small delay for the animation
            setTimeout(() => {
                navigate('/dashboard');
            }, 3000);
        } else {
            // If no token, redirect to login with error
            navigate('/login?error=oauth_failed');
        }
    }, [searchParams, navigate]);

    return (
        <LoadingOverlay />
    );
}

export default OAuthCallback;
