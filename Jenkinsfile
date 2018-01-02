pipeline {
  agent any
  stages {
    stage('Test Debug') {
      steps {
        sh './gradlew testDebug -PQUALAROO_S3_ACCESS_KEY=0 -PQUALAROO_S3_SECRET_KEY=0'
      }
    }
  }
}
