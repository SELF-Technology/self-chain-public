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
} from '@mui/material';

const steps = ['Basic Info', 'Token Settings', 'Advanced Features', 'Review'];

function TokenWizard() {
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState({
    name: '',
    symbol: '',
    decimals: '18',
    totalSupply: '',
    description: '',
    website: '',
    logoUrl: '',
    features: [],
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

  const validateForm = () => {
    if (!formData.name || !formData.symbol || !formData.totalSupply) {
      setError('Please fill in all required fields');
      return false;
    }
    if (!/^[A-Z]{1,5}$/.test(formData.symbol)) {
      setError('Token symbol must be 1-5 uppercase letters');
      return false;
    }
    setError(null);
    setActiveStep((prev) => prev + 1);
    return true;
  };

  const handleSubmit = async () => {
    try {
      // Submit to backend
      const response = await fetch('/api/tokens/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        window.location.href = '/tokens';
      } else {
        setError('Failed to create token');
      }
    } catch (error) {
      setError('An error occurred while creating the token');
    }
  };

  const renderStepContent = (step) => {
    switch (step) {
      case 'Basic Info':
        return (
          <Box sx={{ mt: 4 }}>
            <TextField
              fullWidth
              label="Token Name"
              value={formData.name}
              onChange={handleChange('name')}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Token Symbol (e.g. SELF)"
              value={formData.symbol}
              onChange={handleChange('symbol')}
              margin="normal"
              required
              helperText="1-5 uppercase letters"
            />
          </Box>
        );
      case 'Token Settings':
        return (
          <Box sx={{ mt: 4 }}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Decimals</InputLabel>
              <Select
                value={formData.decimals}
                onChange={handleChange('decimals')}
                label="Decimals"
              >
                <MenuItem value="0">0</MenuItem>
                <MenuItem value="18">18 (Recommended)</MenuItem>
                <MenuItem value="6">6</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Total Supply"
              type="number"
              value={formData.totalSupply}
              onChange={handleChange('totalSupply')}
              margin="normal"
              required
              helperText="Enter total number of tokens"
            />
          </Box>
        );
      case 'Advanced Features':
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
          </Box>
        );
      case 'Review':
        return (
          <Box sx={{ mt: 4 }}>
            <Typography variant="h6" gutterBottom>
              Review Token Details
            </Typography>
            <Typography>Name: {formData.name}</Typography>
            <Typography>Symbol: {formData.symbol}</Typography>
            <Typography>Decimals: {formData.decimals}</Typography>
            <Typography>Total Supply: {formData.totalSupply}</Typography>
            <Typography>Description: {formData.description}</Typography>
            <Typography sx={{ mt: 2 }}>
              <Button
                variant="contained"
                onClick={handleSubmit}
                sx={{ mr: 1 }}
              >
                Create Token
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

export default TokenWizard;
