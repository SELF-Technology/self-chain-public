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