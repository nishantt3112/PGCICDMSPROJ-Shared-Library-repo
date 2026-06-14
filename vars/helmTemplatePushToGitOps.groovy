def call(Map config = [:]) {

 if (config.runHelm == true )
    echo "=== HELM TEMPLATE GENERATION ==="

    def env = config.environment ?: "dev"
    def envValuesFile= "helm-chart/env/values-${env}.yaml"
    if (!fileExists(envValuesFile)) {
        error("Environment values file not found: ${envValuesFile}")
    }
    sh """
        set -e

        helm template helm-chart \
        -f helm-chart/values.yaml
        -f ${envValuesFile} \
        --set images.${config.helmChartImageName}.tag=${env.imageTag} \
        --set images.${config.helmChartImageName}.digest=${env.imageDigest} \
        > rendered.yaml
    """

    echo "=== CHECKOUT GITOPS REPO ==="

    dir("gitops") {

        def gitopsRepoUel = "git@github.com:nishantt3112/ProdGrade_CICD_DevSecOps_Microservices_Project.git"
        git branch: config.gitBranch ?: 'main',
            credentialsId: "jenkins-gitops",
            url: config.gitopsRepoUrl

        echo "=== COPY RENDERED MANIFEST ==="

        sh """
            set -e

            cp ../rendered.yaml kustom/base/rendered.yaml

        """

        echo "=== GIT CONFIG ==="

        sh """
            set -e

            git config user.name "jenkins-bot"
            git config user.email "jenkins@company.com"
        """

        echo "=== COMMIT CHANGES ==="

        sh """
            set -e

            git add .
            git commit -m "${config.serviceName}: updated image ${env.imageTag} and imageDigest ${env.imageDigest}" || echo "No changes to commit"
        """

        echo "=== PUSH TO GITOPS ==="

        sh """
            set -e

            git push origin ${config.gitBranch ?: 'main'}
        """
    }

    echo "=== GITOPS UPDATE DONE ==="
}