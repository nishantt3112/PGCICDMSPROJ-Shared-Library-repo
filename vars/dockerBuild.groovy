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

    def fullImage = "${config.ecrRepo}:${gitSha}-${env.BUILD_NUMBER}"
    def imageTag = "${gitSha}-${env.BUILD_NUMBER}"

    def dockerfilePath = config.dockerFilePath ?: "${config.servicePath}/Dockerfile"

    def buildContext   = config.buildContext ?: "${config.servicePath}/."

    echo "=== DOCKER BUILD START ==="
    echo "Dockerfile: ${dockerfilePath}"
    echo "Context: ${buildContext}"

     sh """
        ls -l /usr/bin/docker

/usr/bin/docker --version

/usr/bin/docker build -t ${fullImage} -f ${dockerfilePath} ${buildContext}

    """
    withCredentials([
    [$class: 'AmazonWebServicesCredentialsBinding',
     credentialsId: 'aws-creds']
    ])
    {

    echo "=== ECR LOGIN ==="
    sh """
        aws ecr get-login-password --region us-east-1 \
        | /usr/bin/docker login --username AWS --password-stdin ${config.ecrRepo.split('/')[0]}
    """

    echo "=== PUSH IMAGE ==="
    sh """
       /usr/bin/docker push ${fullImage}
    """

    echo "=== DONE ==="

    echo "=== GET IMAGE DIGEST ==="

    def imageDigest = sh(
        script: """
            /usr/bin/docker inspect \
            --format='{{index .RepoDigests 0}}' \
            ${fullImage} | cut -d'@' -f2
        """,
        returnStdout: true
    ).trim()

    echo "IMAGE DIGEST = ${imageDigest}"

    env.IMAGE_DIGEST = imageDigest

    return [
        image : fullImage,
        digest: imageDigest
    ]

    echo "=== DOCKER BUILD END ==="
    echo "=== DOCKER BUILD END ==="
}
}