FROM node:20-alpine AS builder
WORKDIR /app

# 1) docker-compose build 에서 받은 ARG
ARG REACT_APP_API_BASE_URL
# 2) ENV 로 설정 → CRA가 process.env.REACT_APP_API_BASE_URL 읽음
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

COPY . .
RUN npm install && npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
