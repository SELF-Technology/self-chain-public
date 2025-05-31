import React, { useState } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  Button,
  Typography,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Switch,
  FormControlLabel,
} from '@mui/material';

const steps = ['Basic Info', 'Collection Settings', 'Features', 'Review'];

function NFTWizard() {
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState({
    name: '',
    symbol: '',
    description: '',
    website: '',
    logoUrl: '',
    baseURI: '',
    isEnumerable: true,
    isBurnable: true,
    isMintable: true,
  });
  const [error, setError] = useState(null);

  const handleNext = () => {
    if (activeStep === steps.length - 2) {
      // Last step before review
      validateForm();
    } else {
      setActiveStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  const handleChange = (field) => (event) => {
    setFormData((prev) => ({
      ...prev,
      [field]: event.target.value,
    }));
  };

  const handleSwitchChange = (field) => (event) => {
    setFormData((prev) => ({
      ...prev,
      [field]: event.target.checked,
    }));
  };

  const validateForm = () => {
    if (!formData.name || !formData.symbol) {
      setError('Please fill in all required fields');
      return false;
    }
    if (!/^[A-Z]{1,5}$/.test(formData.symbol)) {
      setError('Collection symbol must be 1-5 uppercase letters');
      return false;
    }
    setError(null);
    setActiveStep((prev) => prev + 1);
    return true;
  };

  const handleSubmit = async () => {
    try {
      // Submit to backend
      const response = await fetch('/api/nfts/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        window.location.href = '/nfts';
      } else {
        setError('Failed to create NFT collection');
      }
    } catch (error) {
      setError('An error occurred while creating the collection');
    }
  };

  const renderStepContent = (step) => {
    switch (step) {
      case 'Basic Info':
        return (
          <Box sx={{ mt: 4 }}>
            <TextField
              fullWidth
              label="Collection Name"
              value={formData.name}
              onChange={handleChange('name')}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Collection Symbol (e.g. SELF)"
              value={formData.symbol}
              onChange={handleChange('symbol')}
              margin="normal"
              required
              helperText="1-5 uppercase letters"
            />
          </Box>
        );
      case 'Collection Settings':
        return (
          <Box sx={{ mt: 4 }}>
            <TextField
              fullWidth
              label="Description"
              multiline
              rows={4}
              value={formData.description}
              onChange={handleChange('description')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Website"
              value={formData.website}
              onChange={handleChange('website')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Logo URL"
              value={formData.logoUrl}
              onChange={handleChange('logoUrl')}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Base URI"
              value={formData.baseURI}
              onChange={handleChange('baseURI')}
              margin="normal"
              helperText="Base URI for NFT metadata"
            />
          </Box>
        );
      case 'Features':
        return (
          <Box sx={{ mt: 4 }}>
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isEnumerable}
                  onChange={handleSwitchChange('isEnumerable')}
                />
              }
              label="Enumerable"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isBurnable}
                  onChange={handleSwitchChange('isBurnable')}
                />
              }
              label="Burnable"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isMintable}
                  onChange={handleSwitchChange('isMintable')}
                />
              }
              label="Mintable"
            />
          </Box>
        );
      case 'Review':
        return (
          <Box sx={{ mt: 4 }}>
            <Typography variant="h6" gutterBottom>
              Review Collection Details
            </Typography>
            <Typography>Name: {formData.name}</Typography>
            <Typography>Symbol: {formData.symbol}</Typography>
            <Typography>Description: {formData.description}</Typography>
            <Typography sx={{ mt: 2 }}>
              <Button
                variant="contained"
                onClick={handleSubmit}
                sx={{ mr: 1 }}
              >
                Create Collection
              </Button>
              <Button onClick={handleBack}>Back</Button>
            </Typography>
          </Box>
        );
      default:
        return null;
    }
  };

  return (
    <Box sx={{ width: '100%', maxWidth: 800, mx: 'auto', mt: 4 }}>
      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      
      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}
      
      {renderStepContent(steps[activeStep])}
      
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 4 }}>
        {activeStep !== 0 && (
          <Button onClick={handleBack} sx={{ mr: 1 }}>
            Back
          </Button>
        )}
        <Button
          variant="contained"
          onClick={handleNext}
        >
          {activeStep === steps.length - 1 ? 'Finish' : 'Next'}
        </Button>
      </Box>
    </Box>
  );
}

export default NFTWizard;
