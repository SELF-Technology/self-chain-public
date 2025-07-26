module.exports = function (context, options) {
  return {
    name: 'docusaurus-plugin-algolia-v4',
    injectHtmlTags() {
      return {
        headTags: [
          {
            tagName: 'link',
            attributes: {
              rel: 'stylesheet',
              href: 'https://cdn.jsdelivr.net/npm/@docsearch/css@beta',
            },
          },
        ],
        postBodyTags: [
          {
            tagName: 'script',
            attributes: {
              src: 'https://cdn.jsdelivr.net/npm/@docsearch/js@beta',
            },
          },
          {
            tagName: 'script',
            innerHTML: `
              window.addEventListener('load', function() {
                // Check if docsearch is available
                if (typeof docsearch === 'undefined') {
                  console.warn('DocSearch not loaded');
                  return;
                }
                
                const appId = '${process.env.ALGOLIA_APP_ID || ''}';
                const apiKey = '${process.env.ALGOLIA_SEARCH_API_KEY || ''}';
                const assistantId = '${process.env.ALGOLIA_ASSISTANT_ID || ''}';
                
                if (!appId || !apiKey) {
                  console.warn('Algolia credentials not configured');
                  return;
                }
                
                // Add mobile search icon
                const searchContainer = document.getElementById('docsearch');
                if (searchContainer && window.innerWidth <= 996) {
                  searchContainer.innerHTML = '<button class="mobile-search-button" aria-label="Search"><svg width="20" height="20" viewBox="0 0 20 20"><path d="M14.386 14.386l4.0877 4.0877-4.0877-4.0877c-2.9418 2.9419-7.7115 2.9419-10.6533 0-2.9419-2.9418-2.9419-7.7115 0-10.6533 2.9418-2.9419 7.7115-2.9419 10.6533 0 2.9419 2.9418 2.9419 7.7115 0 10.6533z" stroke="currentColor" fill="none" fillRule="evenodd" strokeLinecap="round" strokeLinejoin="round"></path></svg></button>';
                }
                
                // Initialize DocSearch v4 with Ask AI
                docsearch({
                  container: '#docsearch',
                  appId: appId,
                  apiKey: apiKey,
                  indexName: 'self',
                  askAi: assistantId || undefined,
                  searchParameters: {
                    facetFilters: ['type:content']
                  },
                  placeholder: 'Search documentation...',
                });
              });
            `,
          },
        ],
      };
    },
  };
};