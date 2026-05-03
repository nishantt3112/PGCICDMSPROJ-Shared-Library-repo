// def call(config){
//     dir(config.servicePath){
//             withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]){
//                withSonarQubeEnv('sonarQubeServer'){
                      
//                     // def scannerHome = tool 'SonarScanner'
//                     if(config.useGenericScanner == true){
                    
//                    sh """
//                    set -e
//                    ${scannerHome}/bin/sonar-scanner \
//                    -Dsonar.token=${SONAR_TOKEN} \
//                    -Dsonar.host.url=${SONAR_HOST_URL} \
//                    -Dsonar.projectKey=${config.serviceName} \
//                    ${config.scanCmd?.trim() ?: ''}
//                    """        
//                     }
//                     else {
                        
//                         sh """
//                             set -e
//                             ${config.scanCmd?.trim() ?: ''}
//                            """
//                     }
//                }
//                } 
//             }
// }

def call(Map config = [:]) {
    dir(config.servicePath) {

        withSonarQubeEnv('sonarQubeServer') {

            sh """
                set -e

                echo "=== SONAR SCAN START ==="

                ${config.sonarCmd?.trim() ?: """
                    ./gradlew sonar \
                    -Dsonar.projectKey=${config.serviceName}
                """}

                echo "=== SONAR SCAN END ==="
            """

            
        }
             echo "=== WAITING FOR QUALITY GATE ==="


        timeout(time: config.qualityGateTimeout ?: 2, unit: 'MINUTES') {

            def qg = waitForQualityGate()   //  result capture

            echo "=== QUALITY GATE STATUS: ${qg.status} ==="

            if (qg.status != 'OK') {
                error " Quality Gate FAILED: ${qg.status}"
            }
        }

        echo " QUALITY GATE PASSED"
        
    
    
        
    }
}

