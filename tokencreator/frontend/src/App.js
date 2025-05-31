import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './theme';
import Navbar from './components/Navbar';
import TokenWizard from './components/TokenWizard';
import NFTWizard from './components/NFTWizard';
import Dashboard from './components/Dashboard';
import TokenList from './components/TokenList';
import NFTList from './components/NFTList';
import Footer from './components/Footer';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Navbar />
        <div className="container">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/create/token" element={<TokenWizard />} />
            <Route path="/create/nft" element={<NFTWizard />} />
            <Route path="/tokens" element={<TokenList />} />
            <Route path="/nfts" element={<NFTList />} />
          </Routes>
        </div>
        <Footer />
      </Router>
    </ThemeProvider>
  );
}

export default App;
