# 📧 Email Setup - Quick Reference

## ⚡ Quick Steps (5 minutes)

### 1. Get Gmail App Password
1. Visit: https://myaccount.google.com/apppasswords
   - (Enable 2FA first if not done)
2. Select: **Mail** and **Other (Certificate System)**
3. Click **Generate**
4. **Copy the 16-character password** (e.g., `abcd efgh ijkl mnop`)

### 2. Update Configuration
**File**: `backend/src/main/resources/application.properties`

Find these lines and update:
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD_HERE
```

**Example**:
```properties
spring.mail.username=john.doe@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

### 3. Restart Backend
```powershell
# Stop backend (Ctrl+C in terminal)
# Start again:
cd backend
mvn spring-boot:run
```

### 4. Test Email
1. Open http://localhost:5173
2. Create event → Upload participants
3. Generate certificates
4. Click "Email" button
5. Check recipient's inbox

---

## ✅ Current Status

- ✅ Email code: Already implemented
- ✅ Configuration file: Updated with template
- ⚠️ **You need to**: Add your Gmail credentials
- ⏳ Then: Restart backend

---

## 🐛 Common Issues

| Problem | Solution |
|---------|----------|
| Authentication failed | Check App Password (no spaces) |
| Connection timeout | Check internet, try port 465 |
| Emails not sending | Restart backend after config |
| Emails in spam | Normal, add to contacts |

---

## 📝 Files Modified

1. ✅ `application.properties` - Added email config template
2. ✅ `EMAIL_SETUP.md` - Complete setup guide

---

## 🎯 Next Steps

1. **Generate Gmail App Password** (2 min)
2. **Update application.properties** (1 min)
3. **Restart backend** (1 min)
4. **Test email sending** (1 min)

**Total time**: ~5 minutes

---

**See EMAIL_SETUP.md for detailed instructions!**
