// def call(config){
//     dir(config.servicePath){
//         sh """
//         echo "IMAGE_NAME=$IMAGE_NAME"
//         ${config.dockerbuildCmd}
//         """
//     }
// }

def call(Map config = [:]) {

  def gitSha = sh(
        script: "git rev-parse --short=7 HEAD",
        returnStdout: true
    ).trim()

    def imageTag = "${config.ecrRepo}:${gitSha}-${env.BUILD_NUMBER}"

    def dockerfilePath = config.dockerFilePath ?: "${config.servicePath}/Dockerfile"

    def buildContext   = config.buildContext ?: "${config.servicePath}/."

    echo "=== DOCKER BUILD START ==="
    echo "Dockerfile: ${dockerfilePath}"
    echo "Context: ${buildContext}"

     sh """
        ls -l /usr/bin/docker

/usr/bin/docker --version

/usr/bin/docker build -t ${imageTag} -f ${dockerfilePath} ${buildContext}
    """

    echo "=== DOCKER BUILD END ==="
    echo "=== DOCKER BUILD END ==="
}