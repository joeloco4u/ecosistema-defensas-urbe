/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        background: {
          dark: '#0a0a0a',
          DEFAULT: '#121212',
          elevated: '#1a1a1a',
          card: '#1e1e1e',
        },
        surface: {
          dark: '#141414',
          DEFAULT: '#1c1c1c',
          light: '#252525',
          border: '#2a2a2a',
          hover: '#333333',
        },
        accent: {
          white: '#f5f5f5',
          DEFAULT: '#e0e0e0',
          muted: '#a0a0a0',
          subtle: '#6b6b6b',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
