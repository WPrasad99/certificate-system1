# Certificate Generation System

## Overview
A comprehensive system for generating, managing, and distributing digital certificates with QR code verification.

## Features
- ✅ Event Management
- ✅ Bulk Certificate Generation
- ✅ QR Code Verification
- ✅ Email Distribution
- ✅ Real-time Status Tracking
- ✅ OAuth Authentication

## Technology Stack
- **Backend**: Spring Boot, PostgreSQL
- **Frontend**: React + Vite
- **Email**: JavaMail with Gmail SMTP
- **Authentication**: JWT + OAuth2

## Quick Start

### Local Development

1. **Prerequisites**:
   - Java 17+
   - PostgreSQL
   - Node.js 16+

2. **Database Setup**:
   ```sql
   CREATE DATABASE certificate_system;
   ```

3. **Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

Visit: `http://localhost:5173`

## Deployment

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for complete deployment instructions.

## Environment Variables

See `.env.example` files in backend and frontend directories.

## License

MIT
