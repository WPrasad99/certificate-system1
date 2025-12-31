# Windows Firewall Configuration for QR Code Verification

## Problem
When scanning QR codes, your phone cannot connect to your computer because Windows Firewall is blocking the connection.

Error: `ERR_CONNECTION_TIMED_OUT` or "This site can't be reached"

## Solution: Allow Ports 5173 and 8080 Through Firewall

### Quick Fix (Recommended for Testing)

**Option 1: Turn Off Public Network Firewall Temporarily**
1. Press `Windows + I` to open Settings
2. Go to **Network & Internet** → **Status**
3. Click **Windows Firewall**
4. Click **Turn Windows Defender Firewall on or off**
5. Under "Public network settings", select **Turn off Windows Defender Firewall**
6. Click **OK**

⚠️ **Note**: Remember to turn it back on after your demo!

---

### Permanent Fix (for Production/Regular Use)

#### Step 1: Allow Port 5173 (Frontend - React)

1. Open **Windows Defender Firewall with Advanced Security**
   - Press `Windows + R`
   - Type: `wf.msc`
   - Press Enter

2. Click **Inbound Rules** in the left panel

3. Click **New Rule...** in the right panel

4. Select **Port** → Click **Next**

5. Select **TCP** and enter **5173** in "Specific local ports"
   - Click **Next**

6. Select **Allow the connection**
   - Click **Next**

7. Check all boxes: **Domain**, **Private**, **Public**
   - Click **Next**

8. Name: `React Dev Server (Port 5173)`
   - Click **Finish**

#### Step 2: Allow Port 8080 (Backend - Spring Boot)

Repeat the same process for port 8080:

1. **Inbound Rules** → **New Rule**
2. **Port** → **TCP** → **8080**
3. **Allow the connection**
4. Check all: **Domain**, **Private**, **Public**
5. Name: `Spring Boot API (Port 8080)`
6. **Finish**

---

### Alternative: Command Line Method (Fast!)

Open **PowerShell as Administrator** and run:

```powershell
# Allow port 5173 (React)
New-NetFirewallRule -DisplayName "React Dev Server" -Direction Inbound -LocalPort 5173 -Protocol TCP -Action Allow

# Allow port 8080 (Spring Boot)
New-NetFirewallRule -DisplayName "Spring Boot API" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

---

## Verify Configuration

### Step 1: Check Your Local IP Address
```powershell
ipconfig
```
Look for "IPv4 Address" under your Wi-Fi adapter (e.g., `192.168.1.100`)

### Step 2: Test from Your Phone

1. **Connect your phone to the SAME Wi-Fi network**
2. Open browser on phone
3. Try accessing: `http://YOUR_IP:5173`
   - Example: `http://192.168.1.100:5173`
4. You should see the login page

### Step 3: Test Backend API
```
http://YOUR_IP:8080/api/auth/login
```
Should show a connection (even if it says "Method Not Allowed" - that's OK!)

---

## Common Issues

### Issue: Still can't connect after opening ports

**Solution 1: Check Network Profile**
Your network might be set to "Public". Change it to "Private":
1. Settings → Network & Internet
2. Click your Wi-Fi network
3. Under "Network profile", select **Private**

**Solution 2: Disable Antivirus Firewall**
Some antivirus software has its own firewall:
- Temporarily disable antivirus
- Test if QR code works
- Add exceptions for ports 5173 and 8080 in antivirus settings

**Solution 3: Check Router Settings**
Some routers block device-to-device communication:
- Look for "AP Isolation" or "Client Isolation" in router settings
- Disable it

---

## Testing QR Code Verification

Once firewall is configured:

1. **Start your services**
   ```powershell
   cd C:\Users\Pratamesh\OneDrive\Desktop\gdghackathon\certificate-system
   .\start-services.bat
   ```

2. **Generate a new certificate** (on your computer)

3. **Scan the QR code** with your phone

4. **Expected behavior**:
   - QR code opens: `http://YOUR_IP:5173/verify/xxxxx-xxxx-xxxx`
   - Browser loads verification page
   - Shows green checkmark with certificate details

---

## Security Note

- **For demos**: Turning off public firewall is acceptable
- **For production**: Use specific port rules
- **Always**: Re-enable firewall after your presentation

---

## Port Reference

| Port | Service | Purpose |
|------|---------|---------|
| 5173 | Vite (React) | Frontend verification page |
| 8080 | Spring Boot | Backend API |

Both ports need to be accessible from your local network for QR code scanning to work!
