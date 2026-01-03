#!/bin/bash
# Quick deployment script for Render

echo "========================================="
echo "   CertiCraft Deployment Helper"
echo "========================================="
echo ""

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "Initializing Git repository..."
    git init
    git add .
    git commit -m "Initial commit for deployment"
    echo "✓ Git initialized"
else
    echo "✓ Git already initialized"
fi

# Check if remote is set
if ! git remote | grep -q "origin"; then
    echo ""
    echo "Please enter your GitHub repository URL:"
    echo "Example: https://github.com/username/certicraft.git"
    read -p "Repository URL: " REPO_URL
    
    git remote add origin $REPO_URL
    echo "✓ Remote added"
fi

# Push to GitHub
echo ""
echo "Pushing to GitHub..."
git branch -M main
git push -u origin main

echo ""
echo "========================================="
echo "   Next Steps:"
echo "========================================="
echo "1. Go to https://render.com and sign up/login"
echo "2. Click 'New +' → 'Web Service'"
echo "3. Connect your GitHub repository"
echo "4. Follow instructions in DEPLOYMENT.md"
echo ""
echo "For detailed guide, see deployment_guide.md"
echo "========================================="
