// __mocks__/axios.js
module.exports = {
    __esModule: true,
    default: {
        create: () => ({
            interceptors: { request: { use: () => {} } },
            get: jest.fn(),
            post: jest.fn(),
            put: jest.fn(),
            delete: jest.fn(),
        }),
    },
}
