// Custom toggle switch theme button
(function() {
  function init() {
    // Create container
    const container = document.createElement('div');
    container.className = 'theme-toggle-switch';
    container.innerHTML = `
      <style>
        .theme-toggle-switch {
          position: fixed;
          bottom: 20px;
          left: 20px;
          width: 60px;
          height: 30px;
          background: #ccc;
          border-radius: 15px;
          cursor: pointer;
          transition: background 0.3s;
          z-index: 9999;
          box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        
        .theme-toggle-switch::before {
          content: '';
          position: absolute;
          width: 26px;
          height: 26px;
          border-radius: 50%;
          background: white;
          top: 2px;
          left: 2px;
          transition: transform 0.3s;
          box-shadow: 0 2px 3px rgba(0,0,0,0.3);
        }
        
        html[data-theme="dark"] .theme-toggle-switch {
          background: #4a5568;
        }
        
        html[data-theme="dark"] .theme-toggle-switch::before {
          transform: translateX(30px);
          background: #2d3748;
        }
        
        .theme-toggle-switch::after {
          content: '☀️';
          position: absolute;
          left: 6px;
          top: 4px;
          font-size: 16px;
          line-height: 22px;
          filter: grayscale(100%) brightness(0);
        }
        
        html[data-theme="dark"] .theme-toggle-switch::after {
          content: '🌙';
          left: 36px;
          filter: grayscale(100%) brightness(100);
        }
      </style>
    `;
    
    // Toggle function
    container.onclick = function() {
      const doc = document.documentElement;
      const current = doc.getAttribute('data-theme') || 'light';
      const next = current === 'dark' ? 'light' : 'dark';
      doc.setAttribute('data-theme', next);
      try {
        localStorage.setItem('theme', next);
      } catch(e) {}
      
      // Force logo update
      updateLogos(next);
    };
    
    // Function to update logos
    function updateLogos(theme) {
      // Update all possible logo selectors
      const logoSelectors = [
        '.navbar__logo img',
        '.navbar__brand img',
        'img[alt*="SELF Logo"]',
        'img[alt*="logo"]',
        '[class*="logo"] img'
      ];
      
      logoSelectors.forEach(selector => {
        const logos = document.querySelectorAll(selector);
        logos.forEach(img => {
          if (img && img.src) {
            if (theme === 'dark') {
              img.src = '/img/SELFwhitelogo.png';
              img.srcset = '/img/SELFwhitelogo.png';
            } else {
              img.src = '/img/SELF BLACK.png';
              img.srcset = '/img/SELF BLACK.png';
            }
          }
        });
      });
    }
    
    // Update logos on initial load
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    updateLogos(currentTheme);
    
    // Also update after a delay to catch any late-loading elements
    setTimeout(() => updateLogos(currentTheme), 100);
    setTimeout(() => updateLogos(currentTheme), 500);
    setTimeout(() => updateLogos(currentTheme), 1000);
    
    // Watch for theme changes
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.attributeName === 'data-theme') {
          const theme = document.documentElement.getAttribute('data-theme');
          updateLogos(theme);
          // Update again after a short delay
          setTimeout(() => updateLogos(theme), 100);
        }
      });
    });
    
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme']
    });
    
    // Add to page
    if (!document.querySelector('.theme-toggle-switch')) {
      document.body.appendChild(container);
    }
  }
  
  // Run when DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();