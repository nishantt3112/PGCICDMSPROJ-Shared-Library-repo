// def call(Map config = [:]) {

//  if (config.runHelm == true )
//     echo "=== HELM TEMPLATE GENERATION ==="

//     def env = config.environment ?: "dev"
//     def envValuesFile= "helm-chart/env/values-${env}.yaml"
//     if (!fileExists(envValuesFile)) {
//         error("Environment values file not found: ${envValuesFile}")
//     }
//     sh """
//         set -e

//         helm template helm-chart \
//         -f helm-chart/values.yaml
//         -f ${envValuesFile} \
//         --set images.${config.helmChartImageName}.tag=${env.imageTag} \
//         --set images.${config.helmChartImageName}.digest=${env.imageDigest} \
//         > rendered.yaml
//     """

//     echo "=== CHECKOUT GITOPS REPO ==="

//     dir("gitops") {

//         def gitopsRepoUel = "git@github.com:nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git"
//         git branch: config.gitBranch ?: 'main',
//             credentialsId: "jenkins-gitops",
//             url: config.gitopsRepoUrl

//         echo "=== COPY RENDERED MANIFEST ==="

//         sh """
//             set -e

//             cp ../rendered.yaml kustom/base/rendered.yaml

//         """

//         echo "=== GIT CONFIG ==="

//         sh """
//             set -e

//             git config user.name "jenkins-bot"
//             git config user.email "jenkins@company.com"
//         """

//         echo "=== COMMIT CHANGES ==="

//         sh """
//             set -e

//             git add .
//             git commit -m "${config.serviceName}: updated image ${env.imageTag} and imageDigest ${env.imageDigest}" || echo "No changes to commit"
//         """

//         echo "=== PUSH TO GITOPS ==="

//         sh """
//             set -e

//             git push origin ${config.gitBranch ?: 'main'}
//         """
//     }

//     echo "=== GITOPS UPDATE DONE ==="
// }


// ##########################################################################
// def call(Map config = [:]) {

//     if (!config.runHelm) {
//         echo "=== HELM SKIPPED (runHelm=false) ==="
//         return
//     }

//     echo "=== HELM TEMPLATE GENERATION START ==="

//     def targetEnv = config.environment ?: "dev"
//     def envValuesFile = "helm-chart/env/values-${targetEnv}.yaml"
    

//     if (!fileExists(envValuesFile)) {
//         error("Environment values file not found: ${envValuesFile}")
//     }

//     if (!env.IMAGE_TAG || !env.IMAGE_DIGEST) {
//         error("IMAGE_TAG or IMAGE_DIGEST not found. Docker build must set them.")
//     }

//     sh """
//         set -e

//         helm template helm-chart \
//           -f helm-chart/values.yaml \
//           -f ${envValuesFile} \
//           --set images.${config.helmChartImageName}.tag=${env.IMAGE_TAG} \
//           --set images.${config.helmChartImageName}.digest=${env.IMAGE_DIGEST} \
//           > rendered.yaml
//     """

//     echo "=== HELM TEMPLATE DONE ==="

//     echo "=== CHECKOUT GITOPS REPO ==="

//     def gitBranch = config.gitBranch ?: "main"
//     def gitopsRepoUrl = "https://github.com/nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git"


//     dir("gitops") {

//         checkout([
//             $class: 'GitSCM',
//             branches: [[name: "*/${gitBranch}"]],
//             userRemoteConfigs: [[
//                 url: gitopsRepoUrl,
//                 credentialsId: "jenkins-gitops"
//             ]]
//         ])

//         echo "=== COPY RENDERED MANIFEST ==="

//         sh """
//             set -e
//             git checkout -B ${gitBranch} origin/${gitBranch}
//             cp ../rendered.yaml kustom/base/rendered.yaml
//         """

//         echo "=== GIT CONFIG ==="

//         sh """
//             git config user.name "jenkins-bot"
//             git config user.email "jenkins@company.com"
//         """

//         echo "=== COMMIT CHANGES ==="

//         sh """
            
//             git status
//             git add .
//             git commit -m "${config.serviceName}: updated image ${env.IMAGE_TAG} digest ${env.IMAGE_DIGEST}" || echo "No changes"
//         """

//         echo "=== PUSH TO GITOPS ==="

//          withCredentials([usernamePassword(
//         credentialsId: "jenkins-gitops",
//         usernameVariable: "GIT_USER",
//         passwordVariable: "GIT_PASS"
//         )]) {

//         sh """
//             git remote set-url origin https://${GIT_USER}:${GIT_PASS}@github.com/nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git
//             git push origin ${gitBranch}
//         """
//         }
//     }

//     echo "=== GITOPS UPDATE DONE ==="
// }.  

// ######################################################################

def call(Map config = [:]) {

if (!config.runHelm) {
    echo "=== HELM SKIPPED (runHelm=false) ==="
    return
}

echo "=== HELM GITOPS FLOW START ==="

def targetEnv = config.environment ?: "dev"
def envValuesFile = "helm-chart/env/values-${targetEnv}.yaml"

if (!fileExists(envValuesFile)) {
    error("Environment values file not found: ${envValuesFile}")
}

if (!env.IMAGE_TAG || !env.IMAGE_DIGEST) {
    error("IMAGE_TAG or IMAGE_DIGEST not found")
}

def gitBranch = config.gitBranch ?: "main"
def gitopsRepoUrl =
    "https://github.com/nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git"

echo "=== CHECKOUT GITOPS REPO ==="

dir("gitops") {

    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${gitBranch}"]],
        userRemoteConfigs: [[
            url: gitopsRepoUrl,
            credentialsId: "jenkins-gitops"
        ]]
    ])

    sh """
        git checkout -B ${gitBranch} origin/${gitBranch}

        git config user.name "jenkins-bot"
        git config user.email "jenkins@company.com"
    """

    echo "=== UPDATE DESIRED STATE ==="

    sh """
        set -e

        yq eval -i '
          .images.${config.helmChartImageName}.tag =
          "${env.IMAGE_TAG}"
        ' kustom/helm-values/helm-desired-state-values.yaml

        yq eval -i '
          .images.${config.helmChartImageName}.digest =
          "${env.IMAGE_DIGEST}"
        ' kustom/helm-values/helm-desired-state-values.yaml

        echo "=== CURRENT VALUES ==="
        cat kustom/helm-values/helm-desired-state-values.yaml
    """
}

echo "=== RENDER HELM USING GITOPS STATE ==="

sh """
    set -e

    helm template helm-chart \
      -f helm-chart/values.yaml \
      -f ${envValuesFile} \
      -f gitops/kustom/helm-values/helm-desired-state-values.yaml \
      > rendered.yaml
"""

echo "=== COPY RENDERED MANIFEST ==="

sh """
    cp rendered.yaml gitops/kustom/base/rendered.yaml
"""

dir("gitops") {

    echo "=== COMMIT CHANGES ==="

    sh """
        git status

        git add \
            kustom/helm-values/helm-desired-state-values.yaml \
            kustom/base/rendered.yaml

        git commit -m \
        "${config.serviceName}: update image ${env.IMAGE_TAG}" \
        || echo "No changes"
    """

    echo "=== PUSH TO GITOPS ==="

    withCredentials([
        usernamePassword(
            credentialsId: "jenkins-gitops",
            usernameVariable: "GIT_USER",
            passwordVariable: "GIT_PASS"
        )
    ]) {

        sh """
            git remote set-url origin \
            https://${GIT_USER}:${GIT_PASS}@github.com/nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git

            git push origin ${gitBranch}
        """
    }
}

echo "=== GITOPS UPDATE COMPLETE ==="
```

}
