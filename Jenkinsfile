node {
    stage('Checkout') {
        checkout scm
    }
    stage('Build and test') {
        sh './gradlew clean qualaroo-sdk:build jacocoTestReport'
        junit 'qualaroo-sdk/build/test-results/**/*.xml'
    }
    stage('Android tests') {
        def error
        parallel (
          emulator: {
            sh "$ANDROID_HOME/tools/emulator -avd NexusSAPI19 -no-audio -no-boot-anim -no-snapshot-load -no-snapshot-save &"
          },
          androidTest: {
            timeout(time: 20, unit: 'SECONDS') {
                sh 'adb wait-for-device'
                echo "Turning off animations on emulators..."
                sh 'adb devices | grep emulator | cut -f1 | while read emulator; do \
                  adb -s $emulator shell settings put global window_animation_scale 0 && \
                  adb -s $emulator shell settings put global transition_animation_scale && \
                  adb -s $emulator shell settings put global animator_duration_scale 0; \
                  done'                                           
            }
            try { 
              sh './gradlew qualaroo-sdk:connectedCheck'
            } catch(e) {
              error = e
            }
            echo "Shutting down all emulators..."
            sh 'adb devices | grep emulator | cut -f1 | while read emulator; do adb -s $emulator emu kill; done'          
          }
        )
        if (error != null) {
          throw error
        }
    }
    stage('SonarQube analysis') {    
          def PULL_REQUEST = env.CHANGE_ID
          def scannerHome = tool 'SonarQubeScanner'
          withSonarQubeEnv('QualarooSonarQube') {
             echo "Running analysis on ${env.BRANCH_NAME}"        
             if (env.BRANCH_NAME == "dev") {            
                echo "Running full analysis because it's a dev branch" 
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectName='Android SDK' -Dsonar.projectKey=android-sdk -Dsonar.sources=qualaroo-sdk/src/main/java -Dsonar.language=java -Dsonar.java.binaries=qualaroo-sdk/build/intermediates/classes/debug -Dsonar.junit.reportPaths=qualaroo-sdk/build/test-results/testDebugUnitTest -Dsonar.jacoco.reportPaths=qualaroo-sdk/build/jacoco/testDebugUnitTest.exec -Dsonar.java.source=1.7"
             } else if (env.BRANCH_NAME != "master") {
                echo "Running preview analysis because it's probably a PR" 
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectName='Android SDK' -Dsonar.projectKey=android-sdk -Dsonar.sources=qualaroo-sdk/src/main/java -Dsonar.language=java -Dsonar.java.binaries=qualaroo-sdk/build/intermediates/classes/debug -Dsonar.junit.reportPaths=qualaroo-sdk/build/test-results/testDebugUnitTest -Dsonar.jacoco.reportPaths=qualaroo-sdk/build/jacoco/testDebugUnitTest.exec -Dsonar.java.source=1.7 -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.github.repository=qualaroo/android-mobile-sdk-src -Dsonar.github.oauth=$GITHUB_ACCESS_TOKEN -Dsonar.analysis.mode=preview -Dsonar.github.disableInlineComments=true"
             }             
          }
    }
}
