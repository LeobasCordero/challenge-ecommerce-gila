# Stage 1: Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy dependency configs
COPY package*.json ./

# Install Dependencies with NPM cache mount and offline preference
RUN --mount=type=cache,target=/root/.npm npm ci --prefer-offline --no-audit --no-fund

# Copy sources and build the Angular application with Angular cache mount
COPY . .
RUN --mount=type=cache,target=/app/.angular/cache npm run build -- --configuration=production

# Stage 2: Runtime stage
FROM nginx:alpine

# Copy built application from build stage
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
