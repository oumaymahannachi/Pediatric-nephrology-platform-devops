# 📖 Guide d'Installation PediaLink (Nouveau PC)

Ce guide explique comment réinstaller et lancer toute l'infrastructure (CI/CD, Kubernetes, Monitoring) après avoir transféré ce dossier.

## 1. Prérequis Logiciels
Installez les outils suivants sur le nouveau PC :
*   **Docker Desktop** (Version récente)
*   **Java JDK 17** & **Maven**
*   **Git**
*   **VS Code** (optionnel)

---

## 2. Configuration de Kubernetes (KubeAdm)
Docker Desktop n'active pas Kubernetes par défaut. Suivez ces étapes :
1.  Ouvrez **Docker Desktop**.
2.  Allez dans **Settings** (roue crantée) > **Kubernetes**.
3.  Cochez **Enable Kubernetes**.
4.  Sélectionnez **Kubeadm** comme méthode de provisioning (important pour le cahier des charges).
5.  Cliquez sur **Apply & Restart**.
6.  Attendez que l'icône Kubernetes en bas à gauche de la fenêtre Docker soit **verte**.

---

## 3. Configuration du Kubeconfig pour Jenkins
Jenkins tourne dans un conteneur et a besoin d'une adresse spéciale pour parler à Kubernetes.

Ouvrez un terminal **PowerShell** dans ce dossier et lancez ces commandes :
```powershell
# 1. Générer le fichier kubeconfig réel
kubectl config view --raw > devops/kubeconfig

# 2. Remplacer l'adresse locale (127.0.0.1) par l'alias Docker interne
(Get-Content devops/kubeconfig) -replace '127.0.0.1', 'kubernetes.docker.internal' | Set-Content devops/kubeconfig
```

---

## 4. Lancement de l'Infrastructure CI/CD
Lancez tous les outils (Jenkins, SonarQube, MailDev) avec Docker Compose :
```bash
docker-compose -f docker-compose-devops.yml up -d
```
*   **Jenkins** : http://localhost:8081
*   **SonarQube** : http://localhost:9000

---

## 5. Déploiement de l'Application via Jenkins
1.  Allez sur votre Jenkins.
2.  Configurez ou lancez le Job existant.
3.  Le pipeline va automatiquement :
    *   Compiler le code Java.
    *   Builder les images Docker.
    *   Déployer sur votre Kubernetes local via le fichier `devops/kubeconfig`.

---

## 6. Commandes Kubernetes Utiles (Cheat Sheet)

### Vérifier l'état du cluster :
```bash
kubectl get nodes
kubectl get ns  # Liste les namespaces (voir 'pedialink' et 'monitoring')
```

### Voir vos Pods (Microservices) :
```bash
kubectl get pods -n pedialink
kubectl get pods -n monitoring
```

### Voir les logs d'un service (en cas de problème) :
```bash
kubectl logs -f deployment/auth-service -n pedialink
```

### Supprimer et relancer tout le déploiement :
```bash
kubectl delete ns pedialink monitoring
# Puis relancez le build Jenkins
```

---

## 7. Accès aux Interfaces (Ports Fixes)

| Service | URL / Port |
| :--- | :--- |
| **Frontend Angular** | [http://localhost:30042](http://localhost:30042) |
| **API Gateway** | [http://localhost:30080](http://localhost:30080) |
| **Grafana (Monitoring)** | [http://localhost:30030](http://localhost:30030) (admin / admin) |
| **Prometheus** | [http://localhost:30090](http://localhost:30090) |
| **Eureka Server** | [http://localhost:8761](http://localhost:8761) |

---
*Note : Si vous changez de PC, assurez-vous de mettre à jour le fichier `devops/kubeconfig` comme indiqué à l'étape 3 avant de lancer Jenkins.*
