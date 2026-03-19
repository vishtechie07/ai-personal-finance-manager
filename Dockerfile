# syntax=docker/dockerfile:1

FROM node:20-alpine AS frontend-build
WORKDIR /workspace/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-build
WORKDIR /workspace/backend

COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY backend/ ./
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache nginx

WORKDIR /app

COPY --from=frontend-build /workspace/frontend/dist /usr/share/nginx/html

COPY --from=backend-build /workspace/backend/target/personal-finance-manager-1.0.0.jar /app/app.jar

COPY nginx/default.conf /etc/nginx/http.d/default.conf

COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENV SERVER_PORT=8081

EXPOSE 80
CMD ["/entrypoint.sh"]

