def call(Map config = [:]) {

    dir(config.servicePath) {

        sh """
            set -e

            echo "=== TRIVY START (${config.serviceName}) ==="

            ${config.trivyScanCmd?.trim()}

            echo "=== TRIVY END ==="
        """
    }
}