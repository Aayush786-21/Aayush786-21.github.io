pipeline {
    agent any

    environment {
        EC2_HOST = credentials('EC2_HOST')
        EC2_USER = credentials('EC2_USER')
        APP_NAME = credntials('APP_NAME')
        SSH_KEY64 = credentials('SSH_KEY64')
    }

    stages {

        stage('Configure SSH') {
            steps {
                sh '''
                mkdir -p ~/.ssh
                echo "$SSH_KEY64" > ~/.ssh/id_rsa
                chmod 600 ~/.ssh/id_rsa
                ssh-keyscan -H $EC2_HOST >> ~/.ssh/known_hosts
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                sh '''
                ssh $EC2_USER@$EC2_HOST << 'EOF'
                  set -e

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

                  echo "Deploy finished"
                EOF
                '''
            }
        }
    }
}
