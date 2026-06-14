
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
    // sh """
    //     aws ecr get-login-password --region us-east-1 \
    //     | /usr/bin/docker login --username AWS --password-stdin ${config.ecrRepo.split('/')[0]}
    // """
    sh """
    set -e

    echo "=== ECR LOGIN ==="

    TOKEN=\$(aws ecr get-login-password --region us-east-1)

    echo "\$TOKEN" | /usr/bin/docker login \
        --username AWS \
        --password-stdin ${config.ecrRepo.split('/')[0]}
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
    env.IMAGE_TAG = imageTag
    

    return [
        image : fullImage,
        digest: imageDigest,
        tag : imageTag
    ]
}
}