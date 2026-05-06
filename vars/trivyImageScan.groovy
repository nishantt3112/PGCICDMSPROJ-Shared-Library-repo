def call(Map config = [:]) {

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