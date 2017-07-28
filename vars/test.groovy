def call(String nodeName = 'test', String composerAuth = 'unset') {
    println "Beginning test runner"
    def testFunctions = new org.swiftotter.TestFunctions()
    
    node (nodeName) {
        withEnv(["COMPOSER_AUTH=" + composerAuth]) {
            stage ('\u26CF Checkout') {
                checkout scm
            }

            stage ('Testing') {
                sh 'rm -rf scripts/'
                sh 'git clone https://github.com/SwiftOtter/Magento1CI.git scripts'

                sh 'sudo chmod --recursive +x scripts/'
                withCredentials([string(credentialsId: params.PACKAGIST, variable: 'COMPOSER_AUTH')]) {
                    withCredentials([string(credentialsId: params.CRYPT_KEY, variable: 'CRYPT_KEY')]) {
                        println 'Running: sh scripts/test.sh'+' -i '+env.BUILD_NUMBER+' -c '+params.CRYPT_KEY+' -t '+params.TABLE_PREFIX+' -b '+params.S3_DB_BUCKET+' -p . -f '+params.S3_DB_FILE
                        sh 'scripts/test.sh'+' -i '+env.BUILD_NUMBER+' -c '+params.CRYPT_KEY+' -t '+params.TABLE_PREFIX+' -b '+params.S3_DB_BUCKET+' -p . -f '+params.S3_DB_FILE
                    }
                }
            }

            stage('\u2795 Pushing Artifacts') {
                archive "output/*"
                junit "output/*.xml"
            }
        }

        testFunctions.updateGithubCommitStatus(currentBuild);
    }
}
