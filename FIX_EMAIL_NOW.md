# 🔴 EMAIL NOT WORKING - FIX NOW

## Problem Identified
Your email configuration still has **placeholder values**. That's why it shows "FAILED".

Current config (WRONG):
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD_HERE
```

---

## 🔧 Fix in 3 Steps (5 minutes)

### Step 1: Generate Gmail App Password

1. **Open this link**: https://myaccount.google.com/apppasswords
   
2. **If it says "You need 2FA"**:
   - Go to https://myaccount.google.com/security
   - Enable "2-Step Verification" first
   - Then go back to App Passwords link
   
3. **Generate password**:
   - Select app: **Mail**
   - Select device: **Other (Custom name)**
   - Type: **Certificate System**
   - Click **Generate**
   
4. **Copy the password** (example: `abcd efgh ijkl mnop`)
   - It's 16 characters with spaces
   - Copy it exactly as shown

---

### Step 2: Update Configuration File

**File**: `certificate-system\backend\src\main\resources\application.properties`

**Find lines 35-36** and replace with YOUR real Gmail details:

**BEFORE** (what you have now):
```properties
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD_HERE
```

**AFTER** (example - use YOUR email and password):
```properties
spring.mail.username=prasadwadkar18@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

**IMPORTANT**: 
- Use YOUR Gmail address
- Use the 16-character password you just copied
- Keep the spaces in the password

---

### Step 3: Restart Backend

**This is CRITICAL - you MUST restart for changes to work!**

1. Go to the terminal running backend
2. Press **Ctrl+C** to stop it
3. Start again:
```powershell
cd C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\certificate-system\backend
mvn spring-boot:run
```

4. Wait ~30-60 seconds for startup

---

## ✅ Test Email

1. Go to http://localhost:5173
2. Navigate to your event certificates
3. Click **"Email"** button for a participant
4. Check email status:
   - ✅ Should show "SENT" (green)
   - ❌ If still "FAILED" - see troubleshooting below

---

## 🐛 Troubleshooting

### Still showing FAILED?

**Check 1: Did you restart backend?**
- Changes only work after restart
- Stop with Ctrl+C and start again

**Check 2: Is password correct?**
- Must be App Password (16 chars)
- NOT your regular Gmail password
- Copy exactly with spaces

**Check 3: Check backend logs**
Look for errors in backend terminal:
- "Authentication failed" → Wrong password
- "Connection timeout" → Internet/firewall issue
- "Could not connect" → SMTP port blocked

**Check 4: Try testing email**
Use a REAL email address you can check:
- Add your own email to participants
- Generate certificates
- Send to yourself first
- Check inbox and spam folder

---

## 📋 Quick Checklist

- [ ] Generated Gmail App Password
- [ ] Copied 16-character password
- [ ] Updated `application.properties` line 35 (username)
- [ ] Updated `application.properties` line 36 (password)
- [ ] Saved the file
- [ ] Stopped backend (Ctrl+C)
- [ ] Started backend again
- [ ] Waited for startup to complete
- [ ] Tested email sending
- [ ] Checked email received

---

## 🎯 Example Configuration

Here's a complete example (use YOUR credentials):

```properties
# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=john.doe@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🔒 Security Note

⚠️ **NEVER share your App Password with anyone!**
⚠️ **Don't commit this file to GitHub!**

---

**After following these steps, email will work! 📧**
