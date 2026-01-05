pipeline {
    agent any

    environment {
        EC2_HOST = credentials('EC2_HOST')
    }

    stages {

        stage('Deploy to EC2') {
            steps {
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'SSH_KEY64',
                        keyFileVariable: 'SSH_KEY_FILE',
                        usernameVariable: 'EC2_USER'
                    )
                ]) {
                    sh '''
                    echo "Starting deployment to $EC2_HOST"

                    ssh -i "$SSH_KEY_FILE" \
                        -o StrictHostKeyChecking=no \
                        $EC2_USER@$EC2_HOST << 'EOF'

                      set -e

                      # ---- EC2-local config ----
                      APP_NAME="aayush-portfolio"
                      APP_DIR="/home/ubuntu/Aayush786-21.github.io"
                      REPO_URL="https://github.com/Aayush786-21/Aayush786-21.github.io.git"

                      echo "Connected to EC2"
                      echo "Deploying $APP_NAME"
                      echo "App directory: $APP_DIR"

                      # ---- Fetch code ----
                      if [ ! -d "$APP_DIR" ]; then
                        git clone "$REPO_URL" "$APP_DIR"
                      else
                        cd "$APP_DIR"
                        git pull origin main
                      fi

                      cd "$APP_DIR"

                      # ---- Docker build & run ----
                      docker build -t "$APP_NAME" .

                      docker stop "$APP_NAME" || true
                      docker rm "$APP_NAME" || true

                      docker run -d \
                        --name "$APP_NAME" \
                        -p 80:80 \
                        --restart unless-stopped \
                        "$APP_NAME"

                      echo "Deploy finished successfully"

EOF
                    '''
                }
            }
        }
    }
}
