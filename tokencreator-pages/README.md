# SELF Chain Token Creator - Cloudflare Pages Version

This is a simplified version of the Token Creator interface, built specifically for Cloudflare Pages. It provides a user-friendly interface for creating ERC20 tokens and NFT collections on SELF Chain.

## Features

- Simple, intuitive interface
- Support for both ERC20 and NFT creation
- Built with Tailwind CSS for modern styling
- Lightweight and fast loading
- No backend required (uses API endpoints)

## Deployment

1. Create a new Cloudflare Pages project
2. Push this repository to your GitHub account
3. Connect your GitHub repository to Cloudflare Pages
4. Configure the build settings to use `pages.json`

## Usage

1. Visit the deployed Cloudflare Pages URL
2. Select either "ERC20 Token" or "NFT Collection"
3. Fill in the required fields
4. Click "Create Token" to deploy your token

## API Integration

The frontend makes API calls to `/api/create-token`. You'll need to:
1. Set up the backend API endpoint
2. Configure CORS to allow requests from your Cloudflare Pages domain
3. Deploy the API to a server that can handle token creation

## Security

- All sensitive operations are handled by the backend API
- Frontend only collects user input
- No private keys or sensitive data stored in frontend

## Customization

To customize the interface:
1. Modify the `index.html` file
2. Update the Tailwind CSS classes
3. Add additional fields or validation as needed

## License

This project is licensed under the Apache License 2.0. See LICENSE for details.
