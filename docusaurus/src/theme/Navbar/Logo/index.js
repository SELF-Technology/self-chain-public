import React from 'react';
import Logo from '@theme-original/Navbar/Logo';
import {useColorMode} from '@docusaurus/theme-common';

export default function LogoWrapper(props) {
  const {colorMode} = useColorMode();
  
  // Override the logo src based on theme
  const logoProps = {
    ...props,
    logo: {
      ...props.logo,
      src: colorMode === 'dark' ? '/img/SELFwhitelogo.png' : '/img/SELF-BLACK.png'
    }
  };
  
  return <Logo {...logoProps} />;
}