def call(Map config = [:]) {


    env.TMP_DIR = "${env.WORKSPACE}/.tmp-${env.BUILD_ID}"

        sh """
        mkdir -p ${env.TMP_DIR}
        mkdir -p ${env.TMP_DIR}/trivy
        """

    echo "=== TRIVY IMAGE SCAN START ==="

    sh """
        set -e

        mkdir -p "\$TRIVY_CACHE_DIR"

        trivy image ${env.FULL_IMAGE} \
          --severity HIGH,CRITICAL \
          --exit-code 0 \
          --cache-dir "\$TRIVY_CACHE_DIR"
    """

    echo "=== TRIVY IMAGE SCAN END ==="
}