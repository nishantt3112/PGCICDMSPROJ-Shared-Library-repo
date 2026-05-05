// def call(config){
//     dir(config.servicePath){
//         sh """
//         echo "IMAGE_NAME=$IMAGE_NAME"
//         ${config.dockerbuildCmd}
//         """
//     }
// }

def call(Map config = [:]) {

    def imageTag = "${config.ecrRepo}:${env.GIT_COMMIT.take(7)}-${env.BUILD_NUMBER}"

    def dockerfilePath =
        "${config.servicePath}/Dockerfile"

    def buildContext =
        config.servicePath

    echo "=== DOCKER BUILD START ==="
    echo "Dockerfile: ${dockerfilePath}"
    echo "Context: ${buildContext}"

    docker.build(
        imageTag,
        "-f ${dockerfilePath} ${buildContext}"
    )

    echo "=== DOCKER BUILD END ==="
}