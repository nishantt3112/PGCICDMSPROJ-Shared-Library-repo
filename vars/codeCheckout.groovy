// // def call(){
// //     def fullHistory = (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'prod' || env.BRANCH_NAME == 'stage')

// //     checkout([
// //         $class: 'GitSCM',
// //         branches: [[name: env.BRANCH_NAME ]],
// //         extensions: fullHistory
// //             ? []   // full history
// //             : [[$class: 'CloneOption', depth: 1, shallow: true, noTags: false]],
// //         userRemoteConfigs: [[
// //             url: env.GIT_URL,
// //             credentialsId: 'git-credentials-id'
// //         ]]
// //     ])
// //  }


// def call(Map config = [:]) {

//     def servicePath = config.servicePath  // e.g. src/adservice

//     def fullHistory = (env.BRANCH_NAME == 'main' || 
//                        env.BRANCH_NAME == 'prod' || 
//                        env.BRANCH_NAME == 'stage')

//     if (fullHistory) {

//         echo "=== FULL CLONE (for sonar accuracy) ==="

//         checkout([
//             $class: 'GitSCM',
//             branches: [[name: env.BRANCH_NAME]],
//             extensions: [],   // full history
//             userRemoteConfigs: [[
//                 url: env.GIT_URL,
//                 credentialsId: 'git-credentials-id'
//             ]]
//         ])

//     } else {

//         echo "=== SHALLOW + SPARSE CHECKOUT (fast CI) ==="

//         withCredentials([usernamePassword(
//             credentialsId: 'git-credentials-id',
//             usernameVariable: 'GIT_USER',
//             passwordVariable: 'GIT_PASS'
//         )]) {

//             sh """
//                 set -e

//                 rm -rf .git

//                 git init

//                 git remote add origin https://${GIT_USER}:${GIT_PASS}@${env.GIT_URL.replace('https://', '')}

//                 git fetch --depth=1 origin ${env.BRANCH_NAME}

//                 git sparse-checkout init --cone

//                 git sparse-checkout set ${servicePath}

//                 git checkout ${env.BRANCH_NAME}

//                 echo "=== CHECKOUT DONE ==="
//             """
//         }
//     }
// }


    // def call(Map config = [:]) {

    //     def servicePath = config.servicePath
    //     def servicePath = config.servicePath

    //     def branch =
    //     env.GIT_BRANCH?.replace("origin/", "") ?: "main"

    //     echo "BRANCH=${branch}"

    //     def fullHistory = (env.BRANCH_NAME == 'main' || 
    //                     env.BRANCH_NAME == 'prod' || 
    //                     env.BRANCH_NAME == 'stage')

    //     withCredentials([usernamePassword(
    //         credentialsId: 'git-credentials-id',
    //         usernameVariable: 'GIT_USER',
    //         passwordVariable: 'GIT_PASS'
    //     )]) {

    //         sh """
    //             set -e

    //             rm -rf .git

    //             git init

    //             git remote add origin https://${GIT_USER}:${GIT_PASS}@${env.GIT_URL.replace('https://', '')}

    //             echo "=== FETCH ==="
                
    //             ${fullHistory 
    //                 ? "git fetch origin ${env.BRANCH_NAME}" 
    //                 : "git fetch --depth=1 origin ${env.BRANCH_NAME}"}

    //             echo "=== SPARSE INIT ==="

    //             git sparse-checkout init --cone

    //             echo "=== SET SERVICE ==="

    //             git sparse-checkout set ${servicePath}

    //             echo "=== CHECKOUT ==="

    //             git checkout ${env.BRANCH_NAME}

    //             echo "=== DONE ==="
    //         """
    //     }
    // }


// def call(Map config = [:]) {

//     def servicePath = config.servicePath

//     // Normal pipeline ke liye GIT_BRANCH use karo
//     // Example: origin/main -> main
//     def branch =
//         env.GIT_LOCAL_BRANCH ?:
//         env.GIT_BRANCH?.replace("origin/", "") ?:
//         "main"

//     echo "CHECKOUT BRANCH = ${branch}"

//     def fullHistory = (
//         branch == 'main'  ||
//         branch == 'prod'  ||
//         branch == 'stage'
//     )

//     withCredentials([usernamePassword(
//         credentialsId: 'git-credentials-id',
//         usernameVariable: 'GIT_USER',
//         passwordVariable: 'GIT_PASS'
//     )]) {

//         sh '''
//             set -e
            
            
//             echo "=== SAFE GIT CONFIG ==="
//             git config --global --add safe.directory "$PWD" || true

//             echo "=== FETCH ==="
//         '''

//         // Groovy interpolation yahan intentionally outside shell secret warning avoid karne ke liye
//         def repoUrl =
//             env.GIT_URL.replace("https://", "")

//         sh """
//             git remote add origin https://\$GIT_USER:\$GIT_PASS@${repoUrl}

//             echo "=== FETCH ==="
//         """

//         if(fullHistory) {

//             sh """
//                 git fetch origin ${branch}
//             """

//         } else {

//             sh """
//                 git fetch --depth=1 origin ${branch}
//             """
//         }

//         sh """
//             echo "=== SPARSE INIT ==="

//             git sparse-checkout init --cone

//             echo "=== SET SERVICE ==="

//             git sparse-checkout set ${servicePath}

//             echo "=== CHECKOUT ==="

//             git checkout ${branch}

//             echo "=== DONE ==="
//         """
//     }
// }


////////
def call(Map config = [:]) {

    def servicePath = config.servicePath

    def branch =
        env.GIT_LOCAL_BRANCH ?:
        env.GIT_BRANCH?.replace("origin/", "") ?:
        "main"
    echo "=== CLEANING OLD WORKSPACE ==="

    
    echo "CHECKOUT BRANCH = ${branch}"

    def fullHistory = (
        branch == 'dev' ||
        branch == 'prod' ||
        branch == 'stage'
    )

    

    // SCM checkout (NO git init, NO manual repo setup)
    def scmVars = checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        userRemoteConfigs: [[
            url: config.gitUrl
        ]],
        extensions: [
            // optional shallow clone for speed
            [$class: 'CloneOption',
                depth: fullHistory ? 0 : 1,
                noTags: false,
                shallow: !fullHistory
            ]
        ],

        [$class: 'SparseCheckoutPaths',
        sparseCheckoutPaths: [
            [path: "${servicePath}"],
            [path: "helm-chart"]
        ]
        ]
    ])

    echo "=== SCM CHECKOUT DONE ==="

    NOW APPLY SPARSE CHECKOUT ON TOP
    // sh """
    //     set -e
    //      git config --global --add safe.directory "\$(pwd)"
    //     echo "=== ENABLE SPARSE CHECKOUT ==="

    //     git sparse-checkout init --cone

    //     echo "=== SET SERVICE PATH ==="
    //     git sparse-checkout set ${servicePath} \ 
    //     helm-chart

    //     echo "=== DONE ==="
    // """
}

///////////////


// pipeline {
//     agent any

//     options {
//         disableConcurrentBuilds()
//     }

//     environment {
//         FETCH_DEPTH = "50"   // 20 bhi kar sakta hai (risk vs speed)
//         BASE_BRANCH = "main"
//     }

//     stages {

//         stage('Checkout (Lightweight for Diff)') {
//             steps {
//                 script {
//                     sh '''
//                         set -e

//                         echo "=== CLEAN OLD REPO ==="
//                         rm -rf .git

//                         echo "=== INIT NEW REPO ==="
//                         git init

//                         echo "=== ADD REMOTE ==="
//                         git remote add origin ${GIT_URL}

//                         echo "=== FETCH CURRENT BRANCH ==="
//                         git fetch origin ${BRANCH_NAME} --depth=${FETCH_DEPTH}
//                         git checkout FETCH_HEAD

//                         echo "=== FETCH BASE BRANCH FOR DIFF ==="
//                         git fetch origin ${BASE_BRANCH} --depth=${FETCH_DEPTH}
//                     '''
//                 }
//             }
//         }

//         stage('Detect Changed Services') {
//             steps {
//                 script {

//                     echo "=== RUNNING GIT DIFF ==="

//                     def changedFiles = sh(
//                         script: "git diff origin/${BASE_BRANCH}...HEAD --name-only",
//                         returnStdout: true
//                     ).trim()

//                     if (!changedFiles) {
//                         echo "No changes detected"
//                         return
//                     }

//                     def fileList = changedFiles.split("\\n")

//                     echo "Changed Files: ${fileList}"

//                     def services = [] as Set

//                     fileList.each { file ->
//                         if (file.startsWith("src/")) {
//                             def parts = file.split("/")
//                             if (parts.size() > 1) {
//                                 services.add(parts[1])
//                             }
//                         }
//                     }

//                     if (services.isEmpty()) {
//                         echo "No service-level changes detected"
//                         return
//                     }

//                     env.CHANGED_SERVICES = services.join(",")

//                     echo "Changed services: ${env.CHANGED_SERVICES}"
//                 }
//             }
//         }

//         stage('Run Service Pipelines') {
//             when {
//                 expression { return env.CHANGED_SERVICES }
//             }

//             steps {
//                 script {

//                     def services = env.CHANGED_SERVICES.split(",")

//                     echo "Services to run: ${services}"

//                     def parallelJobs = services.collectEntries { svc ->

//                         ["${svc}": {

//                             echo "=== Running ${svc} pipeline ==="

//                             dir("src/${svc}") {
//                                 load "Jenkinsfile"
//                             }
//                         }]
//                     }

//                     echo "=== RUNNING IN PARALLEL ==="

//                     parallel parallelJobs
//                 }
//             }
//         }
//     }

//     // post {
//     //     always {
//     //         echo "=== CLEAN WORKSPACE ==="
//     //         cleanWs()
//     //     }
//     // }
// }