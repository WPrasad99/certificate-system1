import api from './api';

export const authService = {
    register: async (data) => {
        const response = await api.post('/auth/register', data);
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify({
                email: response.data.email,
                fullName: response.data.fullName,
            }));
        }
        return response.data;
    },

    login: async (data) => {
        const response = await api.post('/auth/login', data);
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify({
                email: response.data.email,
                fullName: response.data.fullName,
            }));
        }
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    isAuthenticated: () => {
        return !!localStorage.getItem('token');
    },
};

export const eventService = {
    getAllEvents: async () => {
        const response = await api.get('/events');
        return response.data;
    },

    getEventById: async (id) => {
        const response = await api.get(`/events/${id}`);
        return response.data;
    },

    createEvent: async (data) => {
        const response = await api.post('/events', data);
        return response.data;
    },

    updateEvent: async (id, data) => {
        const response = await api.put(`/events/${id}`, data);
        return response.data;
    },

    deleteEvent: async (id) => {
        const response = await api.delete(`/events/${id}`);
        return response.data;
    },
};

export const participantService = {
    uploadParticipants: async (eventId, file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post(`/events/${eventId}/participants/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    getParticipants: async (eventId) => {
        const response = await api.get(`/events/${eventId}/participants`);
        return response.data;
    },

    deleteParticipant: async (eventId, participantId) => {
        const response = await api.delete(`/events/${eventId}/participants/${participantId}`);
        return response.data;
    },

    deleteAllParticipants: async (eventId) => {
        const response = await api.delete(`/events/${eventId}/participants/all`);
        return response.data;
    },
};

export const certificateService = {
    generateCertificates: async (eventId) => {
        const response = await api.post(`/certificates/events/${eventId}/generate`);
        return response.data;
    },

    getCertificateStatus: async (eventId) => {
        const response = await api.get(`/certificates/events/${eventId}/status`);
        return response.data;
    },

    downloadCertificate: async (certificateId) => {
        const response = await api.get(`/certificates/${certificateId}/download`, {
            responseType: 'blob',
        });
        return response.data;
    },

    downloadAllCertificates: async (eventId) => {
        const response = await api.get(`/certificates/events/${eventId}/download-all`, {
            responseType: 'blob',
        });
        return response.data;
    },

    sendCertificateEmail: async (certificateId) => {
        const response = await api.post(`/certificates/${certificateId}/send-email`);
        return response.data;
    },

    sendAllEmails: async (eventId) => {
        const response = await api.post(`/certificates/events/${eventId}/send-all`);
        return response.data;
    },

    sendUpdateEmails: async (eventId, subject, content) => {
        const response = await api.post(`/certificates/events/${eventId}/send-updates`, { subject, content });
        return response.data;
    },
};
export const templateService = {
    uploadTemplate: async (eventId, file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post(`/events/${eventId}/template/upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    getTemplate: async (eventId) => {
        try {
            const response = await api.get(`/events/${eventId}/template`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.status === 404) {
                return null;
            }
            throw error;
        }
    },

    deleteTemplate: async (eventId) => {
        const response = await api.delete(`/events/${eventId}/template`);
        return response.data;
    }
};

export const collaborationService = {
    inviteCollaborator: async (eventId, email) => {
        const response = await api.post(`/events/${eventId}/collaborators/invite`, { email });
        return response.data;
    },

    getRequests: async () => {
        const response = await api.get('/collaboration/requests');
        return response.data;
    },

    acceptRequest: async (requestId) => {
        const response = await api.post(`/collaboration/requests/${requestId}/accept`);
        return response.data;
    },

    declineRequest: async (requestId) => {
        const response = await api.post(`/collaboration/requests/${requestId}/decline`);
        return response.data;
    },

    getCollaborators: async (eventId) => {
        const response = await api.get(`/events/${eventId}/collaborators`);
        return response.data;
    },

    removeCollaborator: async (eventId, userId) => {
        const response = await api.delete(`/events/${eventId}/collaborators/${userId}`);
        return response.data;
    },

    searchUsers: async (email) => {
        const response = await api.get(`/users/search?email=${encodeURIComponent(email)}`);
        return response.data;
    },

    resendInvitation: async (eventId, userId) => {
        const response = await api.post(`/events/${eventId}/collaborators/${userId}/resend`);
        return response.data;
    },

    getSentRequests: async () => {
        const response = await api.get('/collaboration/sent-requests');
        return response.data;
    },

    getCollaboratorLogs: async (eventId, userId) => {
        const response = await api.get(`/events/${eventId}/collaborators/${userId}/logs`);
        return response.data;
    }
};

export const analyticsService = {
    getStats: async () => {
        const response = await api.get('/analytics/stats');
        return response.data;
    }
};
