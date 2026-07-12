# Stage 1: Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy dependency configs
COPY package*.json ./
RUN npm ci

# Copy sources and build the Angular application
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Serve stage
FROM nginx:alpine

# Copy built application assets to Nginx default public directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
