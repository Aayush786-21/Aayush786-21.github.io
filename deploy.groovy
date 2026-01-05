pipeline {
    agent any

    environment {
        EC2_HOST = '100.30.94.53'
        EC2_USER = 'ubuntu'
        APP_NAME = 'aayush-portfolio'
    }

    stages {
        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: ['SSH_KEY64']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST << 'EOF'
                      set -e

                      if [ ! -d "$HOME/aayush-portfolio" ]; then
                        git clone https://github.com/Aayush786-21/Aayush786-21.github.io.git $HOME/aayush-portfolio
                      else
                        cd $HOME/aayush-portfolio
                        git pull origin main
                      fi

                      cd $HOME/aayush-portfolio
                      docker build -t $APP_NAME .
                      docker stop $APP_NAME || true
                      docker rm $APP_NAME || true
                      docker run -d -p 80:80 --restart unless-stopped --name $APP_NAME $APP_NAME
                    EOF
                    '''
                }
            }
        }
    }
}
