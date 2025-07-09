
import React from 'react';
import ExecutionEnvironment from '@docusaurus/ExecutionEnvironment';

// PWA Service Worker Registration
if (ExecutionEnvironment.canUseDOM && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(error => 
      console.log('ServiceWorker registration failed:', error)
    );
  });
}

export default function Root({children}) {
  return children;
}
