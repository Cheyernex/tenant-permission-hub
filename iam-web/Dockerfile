FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci || npm install
COPY . .
ARG VITE_API_BASE=""
ARG VITE_AUTH_BASE=""
ENV VITE_API_BASE=$VITE_API_BASE
ENV VITE_AUTH_BASE=$VITE_AUTH_BASE
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=build /app/dist ./dist
RUN npm install -g serve
EXPOSE 80
CMD ["serve", "-s", "dist", "-l", "80"]
