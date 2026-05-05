
// this is the main CI pipeline in the shared library which means ki this the main logic of CI pipeline the stages used in CI are defined here so basically we can say the common logic is defined in jenkins Shared library and service depenent logic is written in the jenkinsfile in repo specific branch
// multibranch pipeline bnaenge because for each microservice i have created separate repository and each repo contains dev feature stage and prod branches so instead of writing and managing the pipeline jobs for each branch and each PR i opted jenkins multibranch job type
// so this will basically detect the branches and PRs and create a pipeline against each PR and each branch
// also the condition is ki there must be jenkinsfile present in each branch 
// scm is the builtin plugin which basically detects the repo the branch and the credentials 
// 

// this is the CIpipepline for sharedlibrary
//ciPipeline.groovy

// 




// def call(Map config = [:]) {

//     pipeline {

//         //////////////////////////////////////////////
//         /// Agent
//         //////////////////////////////////////////////
//         agent {
//             docker {
//                 image config.dockerCiPrebakedImage
//                 args config.dockerCIPrebakedImageArgs
//             }
//         }

//         //////////////////////////////////////////////
//         /// ENV
//         //////////////////////////////////////////////
//         environment {
//             HOME = "${WORKSPACE}"
//             GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
//             SONAR_USER_HOME  = "${WORKSPACE}/.sonar"
//             TRIVY_CACHE_DIR  = "${WORKSPACE}/.trivy-cache"

//             GIT_URL   = "${config.gitUrl}"
//             ECR_REPO  = "${config.ecrRepo}"
//             AWS_REGION = "us-east-1"

//             IMAGE_NAME = "${config.ecrRepo}:${env.GIT_COMMIT?.take(7)}-${env.BUILD_NUMBER}"
//         }

//         //////////////////////////////////////////////
//         /// Options
//         //////////////////////////////////////////////
//         options {
//             skipDefaultCheckout(true)  // required if cleaning before checkout
//         }

//         //////////////////////////////////////////////
//         /// Stages
//         //////////////////////////////////////////////
//         stages {

//             //////////////////////////////////////////////
//             /// Clean Workspace (BEFORE BUILD)
//             // //////////////////////////////////////////////
//             // stage('Clean Workspace') {
//             //     steps {
//             //         cleanWs()
//             //     }
//             // }

//             //////////////////////////////////////////////
//             /// Checkout
//             //////////////////////////////////////////////
//             stage('Checkout') {
//                 steps {
//                     script {
//                         checkoutCode(config)
//                     }
//                 }
//             }

//             //////////////////////////////////////////////
//             /// Build & Test
//             //////////////////////////////////////////////
//             stage('Build & Test') {
//                 steps {
//                     script {
//                         buildAndTest(config)
//                     }
//                 }
//             }

//             //////////////////////////////////////////////
//             /// Sonar Scan
//             //////////////////////////////////////////////
//             stage('Sonar Scan') {
//                 steps {
//                     script {
//                         sonarScan(config)
//                     }
//                 }
//             }

//             //////////////////////////////////////////////
//             /// Trivy Scan
//             //////////////////////////////////////////////
//             stage('Trivy Scan') {
//                 steps {
//                     script {
//                         trivyScan(config)
//                     }
//                 }
//             }

//             //////////////////////////////////////////////
//             /// Archive Reports
//             //////////////////////////////////////////////
//             stage('Archive Reports') {
//                 steps {
//                     script {
//                         archiveReports(config)
//                     }
//                 }
//             }
//         }

//         //////////////////////////////////////////////
//         /// Post Actions
//         //////////////////////////////////////////////
//         post {

//             //////////////////////////////////////////////
//             /// Clean AFTER BUILD
//             //////////////////////////////////////////////
//             // always {
//             //     echo "=== CLEANING WORKSPACE ==="

//             //     cleanWs(
//             //         deleteDirs: true,
//             //         disableDeferredWipeout: true,
//             //         notFailBuild: true
//             //     )
//             // }

//             success {
//                 echo " BUILD SUCCESS"
//             }

//             failure {
//                 echo " BUILD FAILED"
//             }
//         }
//     }
// }

////////////

// def call(Map config = [:]) {

//     node {

//         docker.image(
//             config.dockerCiPrebakedImage
//         ).inside(
//             config.dockerCIPrebakedImageArgs ?: ''
//         ) {

//             env.HOME = "${pwd()}"
//             env.WORKSPACE_ROOT = "${pwd()}"

//             // ONLY SAFE TEMP CACHE BASE (language independent)
//             env.TMP_DIR = "/tmp/jenkins-${env.BUILD_ID}"
//             sh "mkdir -p $TMP_DIR"


//             env.HOME = "${pwd()}"
//             env.GRADLE_USER_HOME = "${pwd()}/.gradle"
//             env.SONAR_USER_HOME  = "${pwd()}/.sonar"
//             env.TRIVY_CACHE_DIR  = "${pwd()}/.trivy-cache"
//             env.GIT_URL = "${config.gitUrl}"
//             env.ECR_REPO = config.ecrRepo
//             env.AWS_REGION = "us-east-1"
//             TRIVY_CACHE_DIR   = "/tmp/trivy-cache"

//             try {

//                 stage("Checkout") {
//                     codeCheckout(config)
//                 }

//                 stage("Build & Test") {
//                     buildAndTest(config)
//                 }

//                 stage("Sonar Scan") {
//                     sonarScan(config)
//                 }

//                 stage("Trivy Scan") {
//                     trivyScan(config)
//                 }

//                 stage("Archive Reports") {
//                     archiveReports(config)
//                 }

//                 echo "BUILD SUCCESS"

//             } catch(Exception e) {

//                 echo "BUILD FAILED"

//                 throw e
//             }
//         }
//     }
// }

def call(Map config = [:]) {

    node {
        deleteDir()
        docker.image(config.dockerCiPrebakedImage)
        .inside(config.dockerCIPrebakedImageArgs ?: '') {

            stage("Init Env") {
                script {

                    // Base workspace
                    env.HOME = pwd()
                    env.WORKSPACE_ROOT = pwd()

                    // ONLY SAFE TEMP CACHE BASE (language independent)
                    env.TMP_DIR = "/tmp/jenkins-${env.BUILD_ID}"
                    sh "mkdir -p ${env.TMP_DIR}"

                    // language agnostic caches
                    env.SONAR_USER_HOME = "${env.TMP_DIR}/sonar"
                    env.TRIVY_CACHE_DIR  = "${env.TMP_DIR}/trivy"

                    // shared external configs
                    env.GIT_URL   = config.gitUrl ?: ""
                    env.ECR_REPO  = config.ecrRepo ?: ""
                    env.AWS_REGION = "us-east-1"

                    // -----------------------------
                    // language specific (UNCHANGED)
                    // -----------------------------
                    if (config.programmingLanguage == "java") {
                        env.GRADLE_USER_HOME = "${env.TMP_DIR}/gradle"
                    }

                    if (config.programmingLanguage == "dotnet") {
                        env.DOTNET_CLI_HOME = "${env.TMP_DIR}/dotnet"
                        env.NUGET_PACKAGES = "${env.TMP_DIR}/nuget"
                    }

                    if (config.programmingLanguage == "node") {
                        env.NPM_CONFIG_CACHE = "${env.TMP_DIR}/npm"
                    }

                    if (config.programmingLanguage == "python") {
                        env.PIP_CACHE_DIR = "${env.TMP_DIR}/pip"
                    }

                    if (config.programmingLanguage == "go") {
                        env.GOCACHE = "${env.TMP_DIR}/go-cache"
                        env.GOMODCACHE = "${env.TMP_DIR}/gomod"
                        env.GOTOOLCHAIN ="auto"
                    }
                }
            }

            try {

                stage("Checkout") {
                    codeCheckout(config)
                }

                stage("Build & Test") {
                    buildAndTest(config)
                }

                stage("Sonar Scan") {
                    sonarScan(config)
                }

                stage("Trivy Scan") {
                    trivyScan(config)
                }

                stage("Archive Reports") {
                    archiveReports(config)
                }

                echo "BUILD SUCCESS"

            } catch (Exception e) {

                echo "BUILD FAILED: ${e.message}"
                throw e
            }
        }
    }
}


// def call(Map config=[:]){

//     def changes = [:]

//     pipeline{
//         agent any 
//         environment {
//             ECR_REPO = "${config.ecrRepo}"
//             IMAGE_NAME = "${ECR_REPO}:${env.GIT_COMMIT.take(7)}-${env.BUILD_NUMBER}"//ecr_repo/service_name:commit_id-build_number 
//             AWS_REGION = "ap-south-1"
//         }

//         tools {
//         go 'go-1.26'
//         jdk 'java21'
//         nodejs 'node20'
//         }
    

//         stages{

//             stage('Code Checkout'){
//                 steps{
//                     script{
//                         codeCheckout()
//                     }
//                 }
//             }

//             stage('Detect Changes'){
//                 steps {
//                     script{
//                         changes detectChanges()

//                         echo "==== CHANGE SUMMARY ===="
//                         echo "Code Changed : ${changes.codeChanged}"
//                         echo "Helm Changed : ${changes.helmChanged}"
//                         echo "========================"
//                     }
//                 }
//             }

//             stage('Build & Test'){
//                 when {
//                     expression { changes.codeChanged }
//                 }
//                 steps{
//                     script{
//                         buildAndTest(config)
//                     }
//                 }
//             }

//             // stage('Sonar & QualityGates'){
//             //     steps{
//             //         script{
//             //             sonarScan(config)
//             //         }
//             //     }
//             // }
//             stage('dockerBuild'){
//                 when {
//                     expression { changes.codeChanged }
//                 }
//                 steps{
//                     script{
//                         dockerBuild(config)
//                     }
//                 }
//             }

//             stage('dockerPush'){
//                 steps{
//                     script{
//                         dockerPush(config)
//                     }
//                 }
//             }

//             stage('Helm Lint') {
//                 when {
//                     expression { changes.helmChanged }
//                 }
//                 steps {
//                     sh "helm lint helm/"
//                 }
//             }

//             stage('Helm Template Validate') {
//                 when {
//                     expression { changes.helmChanged }
//                 }
//                 steps {
//                     sh "helm template helm/ > output.yaml"
//                     sh "kubectl apply --dry-run=client -f output.yaml"
//                 }
//             }
        
//         }
//         }
// }
 

//  // codeCheckout.groovy
//  def call(){
//     def fullHistory = (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'prod' || env.BRANCH_NAME == 'stage')

//     checkout([
//         $class: 'GitSCM',
//         branches: [[name: env.BRANCH_NAME ]],
//         extensions: fullHistory
//             ? []   // full history
//             : [[$class: 'CloneOption', depth: 1, shallow: true, noTags: false]],
//         userRemoteConfigs: [[
//             url: env.GIT_URL,
//             credentialsId: 'git-credentials-id'
//         ]]
//     ])
//  }

//  //buildAndTest.groovy

 

//  // sonarScan.groovy

// def call(config){
//     dir(config.servicePath){
//             withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]){
//                withSonarQubeEnv('sonarQubeServer'){
                    
//                     if(config.useGenericScanner == true){
//                     def scannerHome = tool 'SonarScanner'

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


// //// qualityGateWait

// def call(){
//     timeout(time: 2 , unit: 'MINUTES') {
//         def qg = waitForQualityGate()
        
//         if (qg.status != 'OK') {
//             currentBuild.result = 'FAILURE'
//             error "Quality Gates failed: ${qg.status}"
//         }
//     }
// }
//  // ecr login

//  // docker build 
//  // trivy image scan
//  // docker push
