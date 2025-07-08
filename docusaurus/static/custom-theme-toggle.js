// Custom theme toggle based on the CodePen design
(function() {
  // Wait for DOM to be ready
  function init() {
    // Create the toggle HTML
    const toggleHTML = `
      <style>
        .custom-theme-toggle-container {
          position: fixed;
          bottom: 20px;
          left: 20px;
          z-index: 9999;
        }
        
        .toggle-checkbox {
          opacity: 0;
          position: absolute;
        }
        
        .toggle-label {
          background-color: #111;
          border-radius: 50px;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 5px;
          position: relative;
          height: 26px;
          width: 50px;
          transform: scale(1.5);
        }
        
        .toggle-label .ball {
          background-color: #fff;
          border-radius: 50%;
          position: absolute;
          top: 2px;
          left: 2px;
          height: 22px;
          width: 22px;
          transform: translateX(0px);
          transition: transform 0.2s linear;
        }
        
        .toggle-checkbox:checked + .toggle-label .ball {
          transform: translateX(24px);
        }
        
        .fa-moon {
          color: #f1c40f;
        }
        
        .fa-sun {
          color: #f39c12;
        }
      </style>
      
      <div class="custom-theme-toggle-container">
        <input type="checkbox" class="toggle-checkbox" id="custom-theme-toggle" />
        <label for="custom-theme-toggle" class="toggle-label">
          <i class="fas fa-moon"></i>
          <i class="fas fa-sun"></i>
          <span class="ball"></span>
        </label>
      </div>
    `;
    
    // Add the toggle to the page
    const div = document.createElement('div');
    div.innerHTML = toggleHTML;
    document.body.appendChild(div);
    
    // Get references
    const checkbox = document.getElementById('custom-theme-toggle');
    const html = document.documentElement;
    
    // Set initial state based on current theme
    // Default to 'dark' if no theme is set (matching Docusaurus config)
    const currentTheme = html.getAttribute('data-theme') || localStorage.getItem('theme') || 'dark';
    
    // Ensure the theme is applied
    if (!html.getAttribute('data-theme')) {
      html.setAttribute('data-theme', currentTheme);
      localStorage.setItem('theme', currentTheme);
    }
    
    checkbox.checked = currentTheme === 'light';
    
    // Handle toggle
    checkbox.addEventListener('change', function() {
      const newTheme = this.checked ? 'light' : 'dark';
      html.setAttribute('data-theme', newTheme);
      localStorage.setItem('theme', newTheme);
      
      // Update logo
      updateLogos(newTheme);
    });
    
    // Function to update logos
    function updateLogos(theme) {
      const logos = document.querySelectorAll('.navbar__logo img, img[alt*="SELF Logo"]');
      logos.forEach(img => {
        if (theme === 'dark') {
          img.src = '/img/SELFwhitelogo.png';
        } else {
          img.src = '/img/SELF-BLACK.png';
        }
      });
    }
    
    // Watch for external theme changes
    const observer = new MutationObserver(() => {
      const theme = html.getAttribute('data-theme') || 'light';
      checkbox.checked = theme === 'light';
      updateLogos(theme);
    });
    
    observer.observe(html, {
      attributes: true,
      attributeFilter: ['data-theme']
    });
    
    // Initial logo update
    updateLogos(currentTheme);
  }
  
  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();