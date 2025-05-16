# Vite 앱 빌드
FROM node:20 AS build  # 18에서 20으로 변경
WORKDIR /app
COPY . .
RUN npm install --legacy-peer-deps  # legacy-peer-deps 플래그 추가

RUN npm run build

# nginx로 정적 파일 서빙
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html

# nginx 설정 추가
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
