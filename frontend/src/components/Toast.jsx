import React, { useEffect } from 'react';
import './Toast.css';

const Toast = ({ message, type, onClose, duration = 3000 }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);

        return () => clearTimeout(timer);
    }, [onClose, duration]);

    return (
        <div className={`toast-overlay show`}>
            <div className={`toast-container toast-${type}`}>
                <div className="toast-icon">
                    {type === 'success' && '✅'}
                    {type === 'error' && '❌'}
                    {type === 'info' && 'ℹ️'}
                </div>
                <div className="toast-message">{message}</div>
                <button className="toast-close" onClick={onClose}>&times;</button>
            </div>
        </div>
    );
};

export default Toast;
