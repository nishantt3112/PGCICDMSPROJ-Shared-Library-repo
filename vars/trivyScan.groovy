def call(Map config = [:]) {

    dir(config.servicePath) {

        echo "=== TRIVY SCAN START ==="

        sh """
            set -e

            ${config.trivyScanCmd?.trim() ?: '''
                trivy fs . \
                    --db-repository public.ecr.aws/aquasecurity/trivy-db \
                    --cache-dir ${WORKSPACE}/.trivy-cache \
                    --format table \
                    --severity HIGH,CRITICAL \
                    --exit-code 1 \
                    --output trivy-report.txt
            '''}
        """

        echo "=== TRIVY SCAN COMPLETE ==="

        echo "=== ARCHIVING TRIVY REPORT ==="

        archiveArtifacts artifacts: config.trivyReportPath ?: 'trivy-report.txt',
                         allowEmptyArchive: true
    }
}