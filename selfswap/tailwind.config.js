/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#4F46E5',
        secondary: '#64748B',
        success: '#22C55E',
        error: '#EF4444',
        warning: '#F59E0B',
        background: '#111827',
        surface: '#1F2937',
      },
    },
  },
  plugins: [],
}
