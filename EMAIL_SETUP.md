# Email Configuration Guide for Certificate System

## 🎯 Goal
Enable the system to send certificates via email to participants.

---

## 📧 Email Setup Options

### Option 1: Gmail (Recommended for Testing)
### Option 2: Outlook/Hotmail
### Option 3: Other SMTP Services (SendGrid, Mailgun, etc.)

---

## 🔐 Option 1: Gmail Setup (Step-by-Step)

### Step 1: Enable 2-Factor Authentication
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Click on "2-Step Verification"
3. Follow steps to enable 2FA (you'll need your phone)

### Step 2: Generate App Password
1. Go to [App Passwords](https://myaccount.google.com/apppasswords)
2. You may be asked to sign in again
3. Select app: **Mail**
4. Select device: **Other (Custom name)**
5. Enter name: **Certificate System**
6. Click **Generate**
7. **COPY THE 16-CHARACTER PASSWORD** (e.g., `abcd efgh ijkl mnop`)

### Step 3: Update application.properties

**File**: `backend/src/main/resources/application.properties`

Replace these lines (around line 29-35):
```properties
# Mail Configuration (Optional - Configure with your SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

With:
```properties
# Mail Configuration - Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

**Example**:
```properties
spring.mail.username=john.doe@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

### Step 4: Restart Backend

**Important**: You MUST restart the backend for changes to take effect!

```powershell
# Stop current backend (Ctrl+C in the terminal)
# Then restart:
cd backend
mvn spring-boot:run
```

---

## 📧 Option 2: Outlook/Hotmail Setup

```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Note**: Outlook also requires App Password for security

---

## 📧 Option 3: Other Services

### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY
```

### Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=postmaster@YOUR_DOMAIN
spring.mail.password=YOUR_MAILGUN_PASSWORD
```

---

## ✅ Testing Email Functionality

### Method 1: Through UI

1. Open http://localhost:5173
2. Login/Register
3. Create event
4. Upload participants (with REAL email addresses)
5. Generate certificates
6. Go to "Certificates" tab
7. Click **"Email"** button for any participant
8. Check recipient's inbox

### Method 2: Check Backend Logs

When you click "Email", you should see in backend console:
```
Sending email to: participant@email.com
Email sent successfully
```

Or if failed:
```
Failed to send email: [error message]
```

---

## 🐛 Troubleshooting

### Error: "Authentication failed"
**Solution**: 
- Double-check App Password (no spaces)
- Ensure 2FA is enabled
- Try regenerating App Password

### Error: "Connection timeout"
**Solution**:
- Check internet connection
- Verify Gmail is not blocked by firewall
- Try port 465 with SSL instead:
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

### Error: "Mail server connection failed"
**Solution**:
- Restart backend after config changes
- Check firewall/antivirus blocking port 587
- Try different email provider

### Emails go to Spam
**Solution**:
- Check spam folder
- Add sender to contacts
- Configure SPF/DKIM (advanced)

---

## 📝 Current Configuration Template

Here's what needs to be in your `application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/certificate_system
spring.datasource.username=postgres
spring.datasource.password=1234

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# JWT Configuration
jwt.secret=5a7c8b9d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b
jwt.expiration=86400000

# Certificate Storage
certificate.storage.path=./certificates/

# ⚡ EMAIL CONFIGURATION - UPDATE THESE! ⚡
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Logging
logging.level.com.certificate=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.mail=DEBUG
```

---

## 📧 Email Content

The system will send emails like this:

**Subject**: Your Certificate of Participation

**Body**:
```
Dear [Participant Name],

Please find attached your certificate of participation.

Best regards,
Event Organizer
```

**Attachment**: certificate.pdf

---

## 🎯 Quick Start Checklist

- [ ] Enable 2FA on Gmail
- [ ] Generate App Password
- [ ] Copy 16-character password
- [ ] Update `application.properties` with:
  - [ ] Your Gmail address
  - [ ] Your App Password
- [ ] Save file
- [ ] Restart backend server
- [ ] Test by sending one certificate
- [ ] Check recipient inbox (and spam)

---

## 🔒 Security Notes

- ✅ Never commit App Password to Git
- ✅ Use environment variables in production
- ✅ App Password is safer than regular password
- ✅ Revoke App Password if compromised

---

**Ready to configure? Update your application.properties file now!**
