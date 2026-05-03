def call(Map config = [:]) {

    dir(config.servicePath) {

        echo "=== ARCHIVING REPORTS START ==="

        archiveArtifacts artifacts: config.archiveReports?.trim() ?: '''
            dependency-check-report.xml,
            trivy-report.txt,
            build/reports/**,
            build/test-results/**,
            build/jacoco/**
        ''',
        allowEmptyArchive: true

        echo "=== ARCHIVING REPORTS DONE ==="
    }
}