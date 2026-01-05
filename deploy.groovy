pipeline {
    agent any

    environment {
        EC2_HOST = credentials('EC2_HOST')
        APP_NAME = ('aayush-portfolio')
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

                      echo "Connected to EC2"
                      echo "Deploy started"

                      if [ ! -d "$HOME/aayush-portfolio" ]; then
                        git clone https://github.com/aayushadhikari/aayush-portfolio.git $HOME/aayush-portfolio
                      else
                        cd $HOME/aayush-portfolio
                        git pull origin main
                      fi

                      cd $HOME/aayush-portfolio

                      docker build -t $APP_NAME .

                      docker stop $APP_NAME || true
                      docker rm $APP_NAME || true

                      docker run -d \
                        --name $APP_NAME \
                        -p 80:80 \
                        --restart unless-stopped \
                        $APP_NAME

                      echo "Deploy finished successfully"

                    EOF
                    '''
                }
            }
        }
    }
}
