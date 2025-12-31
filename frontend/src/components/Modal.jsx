import React from 'react';
import './Modal.css';

function Modal({ isOpen, onClose, onConfirm, title, message, type = 'confirm' }) {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h3>{title}</h3>
                    <button className="modal-close" onClick={onClose}>&times;</button>
                </div>
                <div className="modal-body">
                    <p>{message}</p>
                </div>
                <div className="modal-footer">
                    {type === 'confirm' ? (
                        <>
                            <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                            <button className="btn btn-danger" onClick={() => { onConfirm(); onClose(); }}>Delete</button>
                        </>
                    ) : (
                        <button className="btn btn-primary" onClick={onClose}>OK</button>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Modal;
