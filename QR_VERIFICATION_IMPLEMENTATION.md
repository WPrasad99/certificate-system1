# QR Code Certificate Verification System - Implementation Summary

## Overview
This implementation adds a complete QR code-based certificate verification system to CertiCraft. Each generated certificate now contains a unique QR code in the top-right corner that can be scanned to verify authenticity.

## Key Features Implemented

### 1. Backend Components

#### Database Changes
- **Certificate Entity**: Added `verificationId` field (UUID) to uniquely identify each certificate
- **Repository**: Added `findByVerificationId()` method for verification lookups

#### QR Code Generation
- **QRCodeGenerator.java**: Utility class using ZXing library to generate QR codes
  - High error correction (Level H) for reliability
  - Minimal margins for compact scanning
  - Configurable size (200x200 pixels generated, displayed as 100x100)

#### Certificate Generation Updates
- **PngCertificateGenerator.java**: 
  - Updated to accept and embed QR codes
  - QR code positioned at (template.width - 120, 20) - top-right corner
  - White background border for contrast
  - Size: 100x100 pixels on certificate

- **CertificateServicePng.java**:
  - Generates UUID for each certificate
  - Creates verification URL: `http://<local-ip>:8080/api/verify/{verificationId}`
  - Automatically detects local IP address for demo purposes
  - Passes QR code to PNG generator

#### Verification Endpoint
- **CertificateController.java**:
  - Public endpoint: `GET /certificates/verify/{verificationId}`
  - Returns: CertificateVerificationDTO with all details
  - No authentication required

- **CertificateVerificationDTO.java**:
  - Contains: participantName, eventName, eventDate, organizerName, instituteName, generatedAt, verificationId

- **SecurityConfig.java**:
  - Added `/certificates/verify/**` to permitted paths

### 2. Frontend Components

#### Verification Page
- **CertificateVerification.jsx**: 
  - Reads verificationId from URL params
  - Calls backend API to verify certificate
  - Displays loading state while verifying
  - Shows success/error states with appropriate UI

- **CertificateVerification.css**:
  - Clean, minimal white theme
  - Responsive design (mobile-optimized)
  - Success indicator (green checkmark)
  - Error indicator (red X)
  - Professional layout with clear information hierarchy

#### Routing
- **App.jsx**: Added public route `/verify/:verificationId`

### 3. Dependencies Added

#### Backend (pom.xml)
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

## How It Works

### Certificate Generation Flow
1. User uploads participants and clicks "Generate Certificates"
2. System generates unique UUID for each certificate (e.g., `550e8400-e29b-41d4-a716-446655440000`)
3. System detects local IP address
4. Creates verification URL: `http://192.168.1.100:8080/api/verify/550e8400-e29b-41d4-a716-446655440000`
5. Generates QR code containing this URL
6. Embeds participant name and QR code on certificate template
7. Saves certificate as PNG with all data in database

### Verification Flow
1. User scans QR code with mobile phone
2. Mobile browser opens: `http://192.168.1.100:8080/api/verify/550e8400-e29b-41d4-a716-446655440000`
3. Frontend redirects to: `http://192.168.1.100:5173/verify/550e8400-e29b-41d4-a716-446655440000`
4. React app calls: `GET http://192.168.1.100:8080/api/certificates/verify/550e8400-...`
5. Backend validates verification ID and returns certificate details
6. Frontend displays:
   - ✓ Certificate Verified (if valid)
   - Participant name
   - Event name and date
   - Organizer and institute
   - Generation timestamp
   - Verification ID

## QR Code Specifications

- **Position**: Top-right corner (20px margin from edges)
- **Size**: 100x100 pixels on certificate
- **Background**: White (5px padding) for contrast
- **Error Correction**: Level H (30% recovery capability)
- **Encoding**: UTF-8
- **Format**: QR Code (2D barcode)

## Local Network Setup

### For Demos Without Hosting
The system automatically detects your local IP address. To access from mobile:

1. Ensure your computer and phone are on the same Wi-Fi network
2. Backend runs on: `http://<your-local-ip>:8080`
3. Frontend runs on: `http://<your-local-ip>:5173`
4. QR codes encode: `http://<your-local-ip>:8080/api/verify/{id}`

### Finding Your Local IP
- **Windows**: Run `ipconfig` → Look for "IPv4 Address"
- **Mac/Linux**: Run `ifconfig` or `ip addr`
- **Example**: `192.168.1.100`

### Mobile Access
1. Open certificate image on phone
2. Scan QR code with camera app or QR scanner
3. Browser opens verification page automatically
4. See certificate details instantly

## Security Considerations

1. **UUID Verification IDs**: Non-sequential, hard to guess
2. **Status Validation**: Only "GENERATED" certificates are verified as valid
3. **Public Read-Only**: Verification endpoint is read-only, no write access
4. **Local Network**: For demos, uses local IP (not exposed to internet)
5. **CORS Configured**: Allows frontend access from localhost

## Testing the System

### Step 1: Generate Certificates
1. Login to dashboard
2. Create an event and upload participants
3. Click "Generate Certificates"
4. Download a certificate

### Step 2: Verify via QR Code
1. Open certificate image on your computer
2. Use your phone to scan the QR code in top-right corner
3. Verification page should open automatically
4. Confirm all details are displayed correctly

### Step 3: Test Invalid Certificate
1. Try accessing: `http://localhost:5173/verify/invalid-id-12345`
2. Should show "Certificate Invalid" error

## Database Schema Update

The `certificates` table now includes:
```sql
verification_id VARCHAR(255) UNIQUE NOT NULL
```

If you have existing certificates, they will need verification IDs. Run this SQL:
```sql
UPDATE certificates 
SET verification_id = gen_random_uuid()::text 
WHERE verification_id IS NULL;
```

## Files Modified/Created

### Backend
- `pom.xml` - Added ZXing dependencies
- `Certificate.java` - Added verificationId field
- `CertificateRepository.java` - Added findByVerificationId method
- `QRCodeGenerator.java` - NEW: QR code generation utility
- `PngCertificateGenerator.java` - Updated to embed QR codes
- `CertificateServicePng.java` - Added verification logic and QR generation
- `CertificateVerificationDTO.java` - NEW: Verification response DTO
- `CertificateController.java` - Added verification endpoint
- `SecurityConfig.java` - Permitted verification endpoint
- `EventService.java` - Added getEventByIdInternal method

### Frontend
- `CertificateVerification.jsx` - NEW: Verification page component
- `CertificateVerification.css` - NEW: Verification page styles
- `App.jsx` - Added verification route

## Future Enhancements (Optional)

1. **Production URL**: Replace local IP with actual domain when deployed
2. **Bulk Verification**: Allow scanning multiple certificates
3. **Verification History**: Track who verified when
4. **Expiry Dates**: Add certificate expiration
5. **Revocation**: Allow administrators to revoke certificates
6. **Analytics**: Track verification counts per certificate
7. **Customizable QR Position**: Allow organizers to choose QR placement

## Troubleshooting

### Issue: QR Code Not Scanning
- **Solution**: Ensure QR code is at least 1cm x 1cm when printed
- Increase `qrSize` in PngCertificateGenerator.java

### Issue: Verification Page Shows "Certificate Invalid"
- **Solution**: Check that certificate was generated after adding verification system
- Ensure database has verification_id column
- Verify backend is running on correct port

### Issue: Mobile Can't Access Verification URL
- **Solution**: 
  - Confirm phone and computer on same Wi-Fi
  - Check firewall allows port 8080 and 5173
  - Use actual IP address, not "localhost"

### Issue: Local IP Not Detected
- **Solution**: Check `getLocalVerificationUrl()` method logs
- Manually set IP in application.properties if needed

## Deployment Considerations

For production deployment:
1. Update `getLocalVerificationUrl()` to use your domain
2. Configure HTTPS for secure verification
3. Update CORS settings to allow your domain
4. Consider rate limiting on verification endpoint
5. Add monitoring for verification endpoint usage

---

**Implementation Complete**: The QR code verification system is fully functional and ready for testing. All certificates generated from this point forward will contain scannable QR codes in the top-right corner.
