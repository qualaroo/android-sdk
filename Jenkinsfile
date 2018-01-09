pipeline {
  agent any
  stages {
    stage('Test debug') {
      steps {
        sh './gradlew clean testDebug -PQUALAROO_S3_ACCESS_KEY=0 -PQUALAROO_S3_SECRET_KEY=0'
      }
    }
  }
  post {
    always {
        junit 'qualaroo-sdk/build/test-results/**/*.xml'
    }
  }
}
