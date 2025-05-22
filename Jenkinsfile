pipeline {
    agent any

    /* 若已在 Manage Jenkins → Global Tool Configuration
       添加名为 “M3” 的 Maven（自动安装或指定本地路径），就保留这行；
       否则删掉 tools{} 并把下方 bat 'mvn …' 换成完整路径 */
    tools {
        maven 'M3'                       // Maven 3.x
    }

    environment {
        /* —— 代码仓库 —— */
        REPO_URL = 'https://github.com/3226nanana/TeedyP.git'//改成你自己的
        BRANCH   = 'master'

        /* —— Docker 镜像 —— */
        IMAGE_NAME = 'lk9050/teedy-app'//改成你自己的
        IMAGE_TAG  = "${env.BUILD_NUMBER}"

        /* —— Docker Hub 凭据 ID —— */
        REGISTRY_CREDENTIALS = 'dockerhub_credentials'//同lab的示例
    }

    stages {

        /*1️⃣ 拉取代码 */
        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH}",
                    url:    "${env.REPO_URL}"
            }
        }

        /* 2️⃣ Maven 打包（跳过测试） */
        stage('Build (Maven)') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        /* 3️⃣ 构建镜像 */
        stage('Build Docker image') {
            steps {
                sh "docker build -t \${IMAGE_NAME}:\${IMAGE_TAG} ."
            }
        }

        /* 4️⃣ 登录并推送到 Docker Hub */
        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.REGISTRY_CREDENTIALS,
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS')]) {

                    sh '''
                      echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                      docker push ${IMAGE_NAME}:${IMAGE_TAG}
                      docker tag  ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                      docker push ${IMAGE_NAME}:latest
                    '''
                }
            }
        }

        /* 5️⃣ 本机同时跑 3 个副本：8082 / 8083 / 8084 */
        stage('Run 3 containers') {
            steps {
                sh '''
                  for PORT in 8082 8083 8081; do
                    docker rm -f teedy-$PORT || true
                    docker run -d --name teedy-$PORT -p $PORT:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                  done
                  docker ps --filter "name=teedy-"
                '''
            }
        }
        //第13周
        stage('Deploy to K8s') {
            steps {
                sh """
                  kubectl set image deployment/teedy-deploy \\
                    teedy-app=\${IMAGE_NAME}:\${IMAGE_TAG} --record
                  kubectl rollout status deployment/teedy-deploy
                """
            }
        }
    }

    /* 6️⃣ 无论成功失败都打印最近镜像列表，方便调试 */
    post {
        always {
            sh '''
              echo "===== Latest images ====="
              docker images --format "{{.Repository}}:{{.Tag}}  {{.CreatedSince}}" | grep "^${IMAGE_NAME}"
            '''
        }
    }
}
