#!/bin/bash

# Force delete Cloudflare Pages project with many deployments
# This script uses the API directly to bypass the production deployment restriction

PROJECT_NAME="docs"
ACCOUNT_ID="63db00e6154a90527b4763891bb07260"

echo "This script will delete ALL deployments from the '$PROJECT_NAME' project"
echo "You'll need your Cloudflare API token with Pages:Edit permissions"
echo ""
echo "Get your API token from: https://dash.cloudflare.com/profile/api-tokens"
echo "Create a new token with 'Cloudflare Pages:Edit' permission for your account"
echo ""
read -p "Enter your Cloudflare API Token: " API_TOKEN

if [ -z "$API_TOKEN" ]; then
    echo "API token is required"
    exit 1
fi

echo ""
echo "Fetching deployments..."

# Function to delete deployments
delete_all_deployments() {
    local page=1
    local per_page=50
    local total_deleted=0
    
    while true; do
        echo "Fetching page $page of deployments..."
        
        # Get deployments (Cloudflare Pages uses different pagination)
        if [ $page -eq 1 ]; then
            RESPONSE=$(curl -s -H "Authorization: Bearer $API_TOKEN" \
                "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME/deployments")
        else
            # Use cursor-based pagination if available
            if [ ! -z "$CURSOR" ]; then
                RESPONSE=$(curl -s -H "Authorization: Bearer $API_TOKEN" \
                    "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME/deployments?cursor=$CURSOR")
            else
                break
            fi
        fi
        
        # Check if request was successful
        SUCCESS=$(echo "$RESPONSE" | jq -r '.success')
        if [ "$SUCCESS" != "true" ]; then
            echo "Error fetching deployments:"
            echo "$RESPONSE" | jq -r '.errors'
            exit 1
        fi
        
        # Get deployment IDs
        DEPLOYMENTS=$(echo "$RESPONSE" | jq -r '.result[].id')
        
        if [ -z "$DEPLOYMENTS" ]; then
            echo "No more deployments found"
            break
        fi
        
        # Count deployments on this page
        COUNT=$(echo "$DEPLOYMENTS" | wc -l | tr -d ' ')
        echo "Found $COUNT deployments on page $page"
        
        # Delete each deployment
        echo "$DEPLOYMENTS" | while read -r deployment_id; do
            if [ ! -z "$deployment_id" ]; then
                echo -n "Deleting deployment $deployment_id... "
                DELETE_RESPONSE=$(curl -s -X DELETE \
                    -H "Authorization: Bearer $API_TOKEN" \
                    "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME/deployments/$deployment_id")
                
                DELETE_SUCCESS=$(echo "$DELETE_RESPONSE" | jq -r '.success')
                if [ "$DELETE_SUCCESS" == "true" ]; then
                    echo "✓"
                    ((total_deleted++))
                else
                    echo "✗ (may be active deployment)"
                fi
            fi
        done
        
        # Check for cursor for next page
        CURSOR=$(echo "$RESPONSE" | jq -r '.result_info.cursor // empty')
        
        if [ -z "$CURSOR" ]; then
            echo "No more pages"
            break
        fi
        
        ((page++))
        sleep 1  # Rate limiting
    done
    
    echo ""
    echo "Deployment deletion complete!"
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "jq is required. Installing..."
    brew install jq
fi

# Start deletion process
delete_all_deployments

echo ""
echo "Now attempting to delete the project..."
sleep 2

# Try to delete the project
DELETE_PROJECT=$(curl -s -X DELETE \
    -H "Authorization: Bearer $API_TOKEN" \
    "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/pages/projects/$PROJECT_NAME")

PROJECT_DELETE_SUCCESS=$(echo "$DELETE_PROJECT" | jq -r '.success')
if [ "$PROJECT_DELETE_SUCCESS" == "true" ]; then
    echo "✓ Project '$PROJECT_NAME' has been successfully deleted!"
else
    echo "Failed to delete project. Error:"
    echo "$DELETE_PROJECT" | jq -r '.errors'
    echo ""
    echo "You may need to:"
    echo "1. Wait a few minutes and try again"
    echo "2. Delete remaining deployments manually in the Cloudflare dashboard"
    echo "3. Contact Cloudflare support if the issue persists"
fi