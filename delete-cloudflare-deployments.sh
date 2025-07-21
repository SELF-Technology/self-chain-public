#!/bin/bash

# Script to bulk delete Cloudflare Pages deployments
# Usage: ./delete-cloudflare-deployments.sh <PROJECT_NAME>

PROJECT_NAME=$1

if [ -z "$PROJECT_NAME" ]; then
    echo "Usage: $0 <PROJECT_NAME>"
    echo "First, list your projects with: wrangler pages project list"
    exit 1
fi

echo "Fetching deployments for project: $PROJECT_NAME"

# Get account ID
ACCOUNT_ID=$(wrangler whoami 2>/dev/null | grep "Account ID" | awk '{print $3}')

if [ -z "$ACCOUNT_ID" ]; then
    echo "Could not get account ID. Please run 'wrangler login' first"
    exit 1
fi

echo "Account ID: $ACCOUNT_ID"

# Function to delete deployments
delete_deployments() {
    echo "Fetching deployment list..."
    
    # Get deployments and delete them in batches
    while true; do
        # Get list of deployments (excluding production)
        DEPLOYMENTS=$(wrangler pages deployment list "$PROJECT_NAME" --json 2>/dev/null | jq -r '.deployments[] | select(.environment != "production") | .id' | head -100)
        
        if [ -z "$DEPLOYMENTS" ]; then
            echo "No more deployments to delete (or all remaining are production)"
            break
        fi
        
        # Count deployments
        COUNT=$(echo "$DEPLOYMENTS" | wc -l | tr -d ' ')
        echo "Found $COUNT deployments to delete..."
        
        # Delete each deployment
        echo "$DEPLOYMENTS" | while read -r deployment_id; do
            if [ ! -z "$deployment_id" ]; then
                echo "Deleting deployment: $deployment_id"
                wrangler pages deployment delete "$deployment_id" --project-name "$PROJECT_NAME" --force 2>/dev/null || echo "Failed to delete $deployment_id"
            fi
        done
        
        echo "Batch complete. Checking for more deployments..."
        sleep 2
    done
}

# Alternative method using Cloudflare API directly
delete_deployments_api() {
    echo "Using direct API method..."
    
    # Get API token
    API_TOKEN=$(wrangler config list | grep CLOUDFLARE_API_TOKEN | cut -d'=' -f2 | tr -d ' ')
    
    if [ -z "$API_TOKEN" ]; then
        echo "No API token found. Using wrangler method instead."
        delete_deployments
        return
    fi
    
    # Get project ID
    PROJECT_ID=$(curl -s -H "Authorization: Bearer $API_TOKEN" \
        "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME" | \
        jq -r '.result.id')
    
    if [ -z "$PROJECT_ID" ] || [ "$PROJECT_ID" = "null" ]; then
        echo "Could not find project ID for $PROJECT_NAME"
        exit 1
    fi
    
    echo "Project ID: $PROJECT_ID"
    
    while true; do
        # Get deployments
        RESPONSE=$(curl -s -H "Authorization: Bearer $API_TOKEN" \
            "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME/deployments?per_page=100")
        
        DEPLOYMENTS=$(echo "$RESPONSE" | jq -r '.result[] | select(.environment != "production") | .id')
        
        if [ -z "$DEPLOYMENTS" ]; then
            echo "No more deployments to delete"
            break
        fi
        
        COUNT=$(echo "$DEPLOYMENTS" | wc -l | tr -d ' ')
        echo "Deleting $COUNT deployments..."
        
        # Delete in parallel for speed
        echo "$DEPLOYMENTS" | xargs -P 10 -I {} sh -c '
            echo "Deleting {}"
            curl -s -X DELETE \
                -H "Authorization: Bearer '"$API_TOKEN"'" \
                "https://api.cloudflare.com/client/v4/accounts/'"$ACCOUNT_ID"'/pages/projects/'"$PROJECT_NAME"'/deployments/{}" \
                > /dev/null
        '
        
        echo "Batch complete. Waiting before next batch..."
        sleep 5
    done
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq is required but not installed. Installing..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install jq
    else
        echo "Please install jq manually"
        exit 1
    fi
fi

echo "Starting deployment deletion for project: $PROJECT_NAME"
echo "This may take a while if you have many deployments..."

# Try API method first, fall back to wrangler if needed
delete_deployments_api

echo "Deployment deletion complete!"
echo "You should now be able to delete the project with:"
echo "wrangler pages project delete $PROJECT_NAME"