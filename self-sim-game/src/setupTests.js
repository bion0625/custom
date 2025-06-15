// src/setupTests.js

// 1) TextEncoder/TextDecoder 폴리필 (Node.js util)
const util = require('util')
global.TextEncoder = util.TextEncoder
global.TextDecoder = util.TextDecoder

// 2) jest-dom 매처
require('@testing-library/jest-dom')
