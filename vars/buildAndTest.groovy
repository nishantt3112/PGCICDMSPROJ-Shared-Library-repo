// // // def call(config){
// // //         dir(config.servicePath){
// // //         sh """
// // //         ${config.buildCmd}
// // //          """
// // //         sh """
// // //         ${config.testCmd}
// // //         """
// // //         }
// // //  }

// // def call(Map config = [:]) {

// //     dir(config.servicePath) {

// //         stage('Build & Test') {
// //             steps {
// //                 script {

// //                     sh """
// //                         set -e

// //                         echo "=== BUILD START ==="

// //                         ${config.buildAndTestCmd?.trim() ?: '''
// //                             chmod +x ./gradlew

// //                             ./gradlew dependencies --no-daemon || true

// //                             ./gradlew clean build \
// //                                 -x verifyGoogleJavaFormat \
// //                                 -x distZip \
// //                                 -x distTar \
// //                                 --no-daemon
// //                         '''}

// //                         echo "=== BUILD END ==="
// //                     """

// //                 }
// //             }
// //         }

// //         stage('JUnit Reports') {
// //             steps {
// //                 script {
// //                     junit allowEmptyResults: true,
// //                           testResults: config.jUnitReportPath ?: 'build/test-results/test/*.xml'
// //                 }
// //             }
// //         }
// //     }
// // }

// ///cant use groovy script in shared lib helper 

// // def call(Map config = [:]) {

// //     dir(config.servicePath) {

// //         stage('Build & Test') {

// //             sh """
// //                 set -e

// //                 echo "=== BUILD START ==="

// //                 ${config.buildAndTestCmd?.trim() ?: '''
// //                     chmod +x ./gradlew

// //                     ./gradlew dependencies --no-daemon || true

// //                     ./gradlew clean build \
// //                         -x verifyGoogleJavaFormat \
// //                         -x distZip \
// //                         -x distTar \
// //                         --no-daemon
// //                 '''}

// //                 echo "=== BUILD END ==="
// //             """
// //         }

// //         stage('JUnit Reports') {

// //             junit(
// //                 allowEmptyResults: true,
// //                 testResults:
// //                     config.jUnitReportPath
// //                     ?: 'build/test-results/test/*.xml'
// //             )
// //         }
// //     }
// // }

// // def call(Map config = [:]) {

// //     dir(config.servicePath) {

// //         stage('Build & Test') {

// //             /*
// //              * Self-healing cache folders
// //              * Every build ensures correct ownership/permissions
// //              */

// //             sh '''
// //                 set -e

// //                 echo "=== USER INFO ==="
// //                 id

// //                 echo "=== PREPARE CACHE DIRS ==="

// //                 mkdir -p "$GRADLE_USER_HOME"
// //                 mkdir -p "$SONAR_USER_HOME"
// //                 mkdir -p "$TRIVY_CACHE_DIR"

// //                 /*
// //                  * In case old root-owned dirs exist
// //                  * don't fail if chown not permitted
// //                  */
// //                 chown -R $(id -u):$(id -g) \
// //                     "$GRADLE_USER_HOME" \
// //                     "$SONAR_USER_HOME" \
// //                     "$TRIVY_CACHE_DIR" || true

// //                 chmod -R u+rwx \
// //                     "$GRADLE_USER_HOME" \
// //                     "$SONAR_USER_HOME" \
// //                     "$TRIVY_CACHE_DIR"

// //                 echo "=== CACHE DIRS READY ==="

// //                 ls -ld "$GRADLE_USER_HOME"
// //             '''

// //             sh """
// //                 set -e

// //                 echo "=== BUILD START ==="

// //                 ${config.buildAndTestCmd?.trim() ?: '''
// //                     chmod +x ./gradlew

// //                     ./gradlew dependencies --no-daemon || true

// //                     ./gradlew clean build \
// //                         -x verifyGoogleJavaFormat \
// //                         -x distZip \
// //                         -x distTar \
// //                         --no-daemon
// //                 '''}

// //                 echo "=== BUILD END ==="
// //             """
// //         }

// //         stage('JUnit Reports') {

// //             junit(
// //                 allowEmptyResults: true,
// //                 testResults:
// //                     config.jUnitReportPath
// //                     ?: 'build/test-results/test/*.xml'
// //             )
// //         }
// //     }
// // } // 
// // interpolation ki dikkat thi is code mein uid and gid ki

// def call(Map config = [:]) {

//     dir(config.servicePath) {

//         stage('Build & Test') {

//             sh '''
//                 set -e

//                 echo "=== USER INFO ==="
//                 id

//                 echo "=== PREPARE CACHE DIRS ==="

//                 mkdir -p "$GRADLE_USER_HOME"
//                 mkdir -p "$SONAR_USER_HOME"
//                 mkdir -p "$TRIVY_CACHE_DIR"

//                 CURRENT_UID=`id -u`
//                 CURRENT_GID=`id -g`

//                 chown -R ${CURRENT_UID}:${CURRENT_GID} \
//                     "$GRADLE_USER_HOME" \
//                     "$SONAR_USER_HOME" \
//                     "$TRIVY_CACHE_DIR" || true

//                 chmod -R u+rwx \
//                     "$GRADLE_USER_HOME" \
//                     "$SONAR_USER_HOME" \
//                     "$TRIVY_CACHE_DIR"

//                 ls -ld "$GRADLE_USER_HOME"

//                 echo "=== CACHE READY ==="
//             '''

//             sh """
//                 set -e

//                 echo "=== BUILD START ==="

//                 ${config.buildAndTestCmd?.trim() ?: '''
//                     chmod +x ./gradlew

//                     ./gradlew dependencies --no-daemon || true

//                     ./gradlew clean build \
//                         -x verifyGoogleJavaFormat \
//                         -x distZip \
//                         -x distTar \
//                         --no-daemon
//                 '''}

//                 echo "=== BUILD END ==="
//             """
//         }

//         stage('JUnit Reports') {

//             junit(
//                 allowEmptyResults: true,
//                 testResults:
//                     config.jUnitReportPath
//                     ?: 'build/test-results/test/*.xml'
//             )
//         }
//     }
// }

def call(Map config = [:]) {

    dir(config.servicePath) {

        stage('Build & Test') {

            sh '''
                set -e

                echo "=== USER INFO ==="
                id

                echo "=== CACHE SETUP ==="

                mkdir -p "$GRADLE_USER_HOME" 2>/dev/null || true
                mkdir -p "$NPM_CONFIG_CACHE" 2>/dev/null || true
                mkdir -p "$PIP_CACHE_DIR" 2>/dev/null || true
                mkdir -p "$GOCACHE" 2>/dev/null || true
                mkdir -p "$SONAR_USER_HOME" 2>/dev/null || true
                mkdir -p "$TRIVY_CACHE_DIR" 2>/dev/null || true

                CURRENT_UID=$(id -u)
                CURRENT_GID=$(id -g)

                chown -R ${CURRENT_UID}:${CURRENT_GID} . 2>/dev/null || true

                echo "=== CACHE READY ==="
            '''

            sh """
                set -e

                echo "=== BUILD START (${config.programmingLanguage}) ==="

                ${config.buildAndTestCmd?.trim() ?: generateDefaultBuild(config)}

                echo "=== BUILD END ==="
            """
        }

        stage('JUnit Reports') {

            junit(
                allowEmptyResults: true,
                testResults: config.jUnitReportPath ?: defaultTestPath(config)
            )
        }
    }
}