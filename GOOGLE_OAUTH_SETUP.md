# Google OAuth2 Setup Guide

Follow these steps to create proper Google OAuth 2.0 credentials:

## Step 1: Access Google Cloud Console
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Sign in with your Google account

## Step 2: Create or Select a Project
1. Click the project dropdown at the top
2. Click "NEW PROJECT"
3. Enter project name: `Certificate System`
4. Click "CREATE"

## Step 3: Enable Google+ API
1. In the left sidebar, go to **APIs & Services** > **Library**
2. Search for "Google+ API"
3. Click on it and click "ENABLE"

## Step 4: Configure OAuth Consent Screen
1. Go to **APIs & Services** > **OAuth consent screen**
2. Select **External** user type
3. Click "CREATE"
4. Fill in the application details:
   - **App name**: Certificate Generation System
   - **User support email**: [Your email]
   - **Developer contact**: [Your email]
5. Click "SAVE AND CONTINUE"
6. Skip the Scopes page (click "SAVE AND CONTINUE")
7. Add test users if needed
8. Click "SAVE AND CONTINUE"

## Step 5: Create OAuth 2.0 Credentials
1. Go to **APIs & Services** > **Credentials**
2. Click "CREATE CREDENTIALS" > "OAuth client ID"
3. Select **Web application** as application type
4. Configure:
   - **Name**: Certificate System Web Client
   - **Authorized JavaScript origins**:
     - `http://localhost:5173`
     - `http://localhost:8080`
   - **Authorized redirect URIs**:
     - `http://localhost:8080/api/login/oauth2/code/google`
5. Click "CREATE"

## Step 6: Copy Your Credentials
You'll see a dialog with:
- **Client ID**: Copy this (looks like: `xxxxx.apps.googleusercontent.com`)
- **Client Secret**: Copy this (looks like: `GOCSPX-xxxxx`)

## Step 7: Update application.properties
Replace the existing OAuth configuration in your `application.properties` with:

```properties
# OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID_HERE
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET_HERE
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/api/login/oauth2/code/google
```

## Important Notes:
- Make sure the redirect URI **exactly matches**: `http://localhost:8080/api/login/oauth2/code/google`
- Don't add trailing slashes
- The `/api` prefix is important (it matches your server context path)
- Restart your backend server after updating the credentials

## Testing:
1. Restart both backend and frontend
2. Go to `http://localhost:5173/login`
3. Click "Sign in with Google"
4. You should be redirected to Google's consent page
