/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,ts,css,scss}'],
  darkMode: ['class', '[data-pc-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        'theme-cardbg': 'rgb(var(--color-theme-cardbg) / <alpha-value>)',
        'themedark-cardbg': 'rgb(var(--color-themedark-cardbg) / <alpha-value>)',
        'theme-border': 'rgb(var(--color-theme-border) / <alpha-value>)',
        'themedark-border': 'rgb(var(--color-themedark-border) / <alpha-value>)',
        'theme-bodycolor': 'rgb(var(--color-theme-bodycolor) / <alpha-value>)',
        'themedark-bodycolor': 'rgb(var(--color-themedark-bodycolor) / <alpha-value>)'
      },
    },
  },
  plugins: [],
};
