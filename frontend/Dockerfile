# ─── Etapa de compilación ────────────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# npm ci (no install): instala exactamente lo del package-lock, sin resolver
# versiones nuevas. Un build reproducible es el punto de tener el lock.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# OJO: la URL del backend se congela en el bundle EN TIEMPO DE COMPILACIÓN, no
# en runtime. Y tiene que ser la que ve el NAVEGADOR del usuario (localhost),
# no el nombre del servicio de Docker: el JS corre en la máquina de quien mira,
# no dentro de la red de contenedores.
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ─── Imagen final ────────────────────────────────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
