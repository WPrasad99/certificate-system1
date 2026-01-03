# Deployment Instructions for Render + Supabase

## Quick Start (5 Steps)

### 1. Set Up Supabase (Database + Storage)
1. Go to [supabase.com](https://supabase.com) → Create new project
2. Copy database credentials from Settings → Database
3. Create storage bucket named `certificates` (make it public)
4. Copy your project URL and service key

### 2. Prepare Environment Variables
Create a file `render-env.txt` with these values (DO NOT COMMIT):
```
DATABASE_URL=postgresql://postgres:[PASSWORD]@db.xxxxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password
JWT_SECRET=your-super-secret-256-bit-key-change-this
EMAIL_USERNAME=your.email@gmail.com
EMAIL_APP_PASSWORD=your_gmail_app_password
SUPABASE_URL=https://xxxxx.supabase.co
SUPABASE_SERVICE_KEY=your_supabase_service_key
CORS_ORIGINS=https://your-frontend.onrender.com
FRONTEND_URL=https://your-frontend.onrender.com
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### 3. Push to GitHub
```bash
git init
git add .
git commit -m "Initial commit for deployment"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/certicraft.git
git push -u origin main
```

### 4. Deploy Backend on Render
1. Go to [render.com](https://render.com) → New → Web Service
2. Connect GitHub repo
3. **Root Directory**: `backend`
4. **Build Command**: `./mvnw clean package -DskipTests`
5. **Start Command**: `java -jar target/certificate-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
6. Add all environment variables from step 2
7. Click "Create Web Service"

### 5. Deploy Frontend on Render
1. Render → New → Static Site
2. Select same GitHub repo
3. **Root Directory**: `frontend`
4. **Build Command**: `npm install && npm run build`
5. **Publish Directory**: `dist`
6. Add environment variable:
   - `VITE_API_BASE_URL=https://your-backend.onrender.com`
7. Click "Create Static Site"

## Testing on Mobile
- Open frontend URL on mobile browser
- Test login, create event, upload files
- Test QR scanning with camera
- Verify all features work

## Troubleshooting
- **CORS errors**: Update `CORS_ORIGINS` in backend env vars
- **Database connection**: Verify Supabase credentials
- **File upload fails**: Check Supabase storage bucket is public
- **Slow loading**: Render free tier spins down after 15min inactivity

For detailed instructions, see `deployment_guide.md` in the artifacts folder.
