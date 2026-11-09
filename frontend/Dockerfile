# --- STAGE 1: Build the application using Node.js ---
FROM node:22-alpine AS build

WORKDIR /app

# Copy package files first to leverage Docker cache for dependencies
COPY package*.json ./
RUN npm ci

# Copy the rest of the source code and build
COPY . .
RUN npm run build

# --- STAGE 2: Serve the application using Nginx ---
FROM nginx:alpine

# Copy the Nginx configuration and generated static files
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 for the web server
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
