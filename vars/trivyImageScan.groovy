stage("Trivy Image Scan") {

    docker.image(config.dockerCiPrebakedImage)
    .inside(config.dockerCIPrebakedImageArgs ?: '') {

        sh """
            trivy image ${env.FULL_IMAGE} \
            --severity HIGH,CRITICAL \
            --exit-code 1 \
            --cache-dir \$TRIVY_CACHE_DIR
        """
    }
}