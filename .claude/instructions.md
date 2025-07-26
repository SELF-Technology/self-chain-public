
# Claude Instructions for self-chain-public Repository

## Repository Information

- This repository publishes to https://docs.self.app
- The site is built using Docusaurus
- Documentation pages are located in: `/Users/jmac/Documents/GitHub/self-chain-public/docs`
- Docusaurus configuration is located in: `/Users/jmac/Documents/GitHub/self-chain-public/docusaurus.config.js`

## Localhost Requirements

There are often localhost ChunkLoadError errors such as "Loading chunk node_modules_docsearch_react_modal_js failed" due to node_modules_docsearch_react_modal_js.js. This requires clearning the Docusaurus cache and node-modules cache, then rebuilding the project and restarting the development server. You may need to do this to get a successful localhost build.

- **ALWAYS** Know that /localhost means the user wants to see the changes in Localhost. 
- **ALWAYS** Know that you don't need to ask for permission to open the localhost.
- **ALWAYS** Open whatever port is available. If 3000 isn't, try 3001 etc
- **ALWAYS** Test to see if the connection is running BEFORE telling the user that it is (see the typical error detailed above)
- **CRITICAL**: **NEVER** claim localhost is running without verifying it first. This is a complete waste of time.
- **ALWAYS** verify localhost is actually running by:
  1. Starting the server and waiting for it to fully initialize
  2. Testing with curl or similar to confirm HTTP 200 response
  3. Only then inform the user that localhost is running with the correct port number
- **NEVER** say "The site is running on http://localhost:XXXX" unless you have confirmed it with an actual HTTP request

## Git Commit Requirements

When committing changes (either by explicit request or using the /commit command):
- **ALWAYS** know that /commit means commit to the main remote branch
- **ALWAYS** execute the command without asking for further permission, if the user has said /commit that should be taken as permission
- **ALWAYS** commit as "SELF" 
- **NEVER** include any attribution to Claude
- **DO NOT** include "🤖 Generated with Claude Code" or similar messages
- **DO NOT** include "Co-Authored-By: Claude" or similar attributions

## Example Commit Format

```bash
git commit -m "Update documentation for feature X"
```

The commit author should be configured as SELF with no AI attribution.