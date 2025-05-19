# 빌드
FROM node:20 AS builder
WORKDIR /app
COPY . .

ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_CLOUDFRONT_URL
ARG NEXT_PUBLIC_AUTH_API_URL

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_CLOUDFRONT_URL=$NEXT_PUBLIC_CLOUDFRONT_URL
ENV NEXT_PUBLIC_AUTH_API_URL=$NEXT_PUBLIC_AUTH_API_URL

# RUN npm install --legacy-peer-deps
RUN npm i
RUN npm run build

# 실행
FROM node:20-alpine AS runner
WORKDIR /app

COPY --from=builder /app/package.json .
COPY --from=builder /app/.next .next
COPY --from=builder /app/public public
COPY --from=builder /app/node_modules node_modules

EXPOSE 3000
CMD ["npm", "run", "start"]