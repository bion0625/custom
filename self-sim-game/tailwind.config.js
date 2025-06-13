/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,ts,jsx,tsx}"  // ✅ src 내부의 모든 TS/JS/JSX/TSX 파일을 감시
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}