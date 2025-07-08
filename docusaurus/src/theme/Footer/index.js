import React from 'react';
import Footer from '@theme-original/Footer';
import {useColorMode} from '@docusaurus/theme-common';

function FooterWrapper(props) {
  const {colorMode, setColorMode} = useColorMode();

  React.useEffect(() => {
    // Add theme toggle to footer after it's rendered
    const footerThemeToggleWrapper = document.querySelector('.custom-theme-toggle-wrapper');
    if (footerThemeToggleWrapper && !footerThemeToggleWrapper.hasChildNodes()) {
      const button = document.createElement('button');
      button.className = 'custom-theme-toggle';
      button.setAttribute('aria-label', 'Toggle theme');
      button.setAttribute('title', `Switch to ${colorMode === 'dark' ? 'light' : 'dark'} mode`);
      
      const updateButton = () => {
        const currentMode = document.documentElement.getAttribute('data-theme');
        button.innerHTML = currentMode === 'dark' ? 
          '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2.25a.75.75 0 01.75.75v2.25a.75.75 0 01-1.5 0V3a.75.75 0 01.75-.75zM7.5 12a4.5 4.5 0 119 0 4.5 4.5 0 01-9 0zM18.894 6.166a.75.75 0 00-1.06-1.06l-1.591 1.59a.75.75 0 101.06 1.061l1.591-1.59zM21.75 12a.75.75 0 01-.75.75h-2.25a.75.75 0 010-1.5H21a.75.75 0 01.75.75zM17.834 18.844a.75.75 0 001.06-1.06l-1.59-1.591a.75.75 0 10-1.061 1.06l1.59 1.591zM12 18.75a.75.75 0 01.75.75V21a.75.75 0 01-1.5 0v-2.25a.75.75 0 01.75-.75zM7.758 17.303a.75.75 0 00-1.061-1.06l-1.591 1.59a.75.75 0 001.06 1.061l1.591-1.59zM6 12a.75.75 0 01-.75.75H3a.75.75 0 010-1.5h2.25A.75.75 0 016 12zM6.697 7.757a.75.75 0 001.06-1.06l-1.59-1.591a.75.75 0 00-1.061 1.06l1.59 1.591z" /></svg>' :
          '<svg viewBox="0 0 24 24" fill="currentColor"><path fillRule="evenodd" d="M9.528 1.718a.75.75 0 01.162.819A8.97 8.97 0 009 6a9 9 0 009 9 8.97 8.97 0 003.463-.69.75.75 0 01.981.98 10.503 10.503 0 01-9.694 6.46c-5.799 0-10.5-4.701-10.5-10.5 0-4.368 2.667-8.112 6.46-9.694a.75.75 0 01.818.162z" clipRule="evenodd" /></svg>';
      };
      
      updateButton();
      
      button.onclick = () => {
        setColorMode(colorMode === 'dark' ? 'light' : 'dark');
      };
      
      footerThemeToggleWrapper.appendChild(button);
      
      // Listen for theme changes
      const observer = new MutationObserver(updateButton);
      observer.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['data-theme']
      });
      
      return () => observer.disconnect();
    }
  }, [colorMode, setColorMode]);

  return <Footer {...props} />;
}

export default FooterWrapper;