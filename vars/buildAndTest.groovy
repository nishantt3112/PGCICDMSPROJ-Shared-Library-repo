// // def call(config){
// //         dir(config.servicePath){
// //         sh """
// //         ${config.buildCmd}
// //          """
// //         sh """
// //         ${config.testCmd}
// //         """
// //         }
// //  }

// def call(Map config = [:]) {

//     dir(config.servicePath) {

//         stage('Build & Test') {
//             steps {
//                 script {

//                     sh """
//                         set -e

//                         echo "=== BUILD START ==="

//                         ${config.buildAndTestCmd?.trim() ?: '''
//                             chmod +x ./gradlew

//                             ./gradlew dependencies --no-daemon || true

//                             ./gradlew clean build \
//                                 -x verifyGoogleJavaFormat \
//                                 -x distZip \
//                                 -x distTar \
//                                 --no-daemon
//                         '''}

//                         echo "=== BUILD END ==="
//                     """

//                 }
//             }
//         }

//         stage('JUnit Reports') {
//             steps {
//                 script {
//                     junit allowEmptyResults: true,
//                           testResults: config.jUnitReportPath ?: 'build/test-results/test/*.xml'
//                 }
//             }
//         }
//     }
// }

///cant use groovy script in shared lib helper 

def call(Map config = [:]) {

    dir(config.servicePath) {

        stage('Build & Test') {

            sh """
                set -e

                echo "=== BUILD START ==="

                ${config.buildAndTestCmd?.trim() ?: '''
                    chmod +x ./gradlew

                    ./gradlew dependencies --no-daemon || true

                    ./gradlew clean build \
                        -x verifyGoogleJavaFormat \
                        -x distZip \
                        -x distTar \
                        --no-daemon
                '''}

                echo "=== BUILD END ==="
            """
        }

        stage('JUnit Reports') {

            junit(
                allowEmptyResults: true,
                testResults:
                    config.jUnitReportPath
                    ?: 'build/test-results/test/*.xml'
            )
        }
    }
}