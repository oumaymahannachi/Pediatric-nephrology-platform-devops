# Utilisation de Nginx pour servir l'application
FROM nginx:stable-alpine

# Copie des fichiers compilés par Jenkins vers le dossier Nginx
# Note : Vérifiez bien le chemin 'dist/pedialink-frontend/browser' 
# selon votre version d'Angular
COPY dist/pedialink-frontend/browser /usr/share/nginx/html

# Copie de la configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
