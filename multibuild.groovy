pipeline {
    agent any

    environment {
        GIT_URL_APP1 = 'https://github.com/pyroman30/job1'
        GIT_URL_APP2 = 'https://github.com/pyroman30/job2'
        GIT_URL_APP3 = 'https://github.com/pyroman30/job3'
        BRANCH_PATTERN = 'release/*'
        GITHUB_TOKEN_CREDENTIAL_ID = 'github_token' // ID учетных данных Jenkins с токеном GitHub
        GIT_EXECUTABLE = '/usr/bin/git' // Замените на путь к исполняемому файлу Git на вашем сервере
    }

    stages {
        stage('Get Latest Release Branches') {
            steps {
                script {
                    // Функция для получения последней релизной ветки
                    def getLatestReleaseBranch = { gitUrl ->
                        def branches = sh(script: "${GIT_EXECUTABLE} ls-remote --heads ${gitUrl}", returnStdout: true).split("\n")
                        def releaseBranches = branches.findAll { it.contains(BRANCH_PATTERN) }
                        def latestReleaseBranch = releaseBranches.collect { it.split()[1].replace('refs/heads/', '') }.sort().last()
                        return latestReleaseBranch
                    }

                    // Получение последней релизной ветки для каждого приложения
                    env.LATEST_RELEASE_BRANCH_APP1 = getLatestReleaseBranch(GIT_URL_APP1)
                    env.LATEST_RELEASE_BRANCH_APP2 = getLatestReleaseBranch(GIT_URL_APP2)
                    env.LATEST_RELEASE_BRANCH_APP3 = getLatestReleaseBranch(GIT_URL_APP3)

                    echo "Latest release branch for App1: ${env.LATEST_RELEASE_BRANCH_APP1}"
                    echo "Latest release branch for App2: ${env.LATEST_RELEASE_BRANCH_APP2}"
                    echo "Latest release branch for App3: ${env.LATEST_RELEASE_BRANCH_APP3}"
                }
            }
        }

        stage('Parallel Jobs') {
            parallel {
                stage('Job for App1') {
                    steps {
                        script {
                            // Получение токена GitHub из учетных данных Jenkins
                            def githubToken = credentials(GITHUB_TOKEN_CREDENTIAL_ID)

                            // Запуск Job1 с параметром BRANCH
                            build job: 'Job1', parameters: [
                                string(name: 'BRANCH', value: "${env.LATEST_RELEASE_BRANCH_APP1}"),
                                string(name: 'GITHUB_TOKEN', value: githubToken)
                            ]
                        }
                    }
                }

                stage('Job for App2') {
                    steps {
                        script {
                            // Получение токена GitHub из учетных данных Jenkins
                            def githubToken = credentials(GITHUB_TOKEN_CREDENTIAL_ID)

                            // Запуск Job2 с параметром BRANCH
                            build job: 'Job2', parameters: [
                                string(name: 'BRANCH', value: "${env.LATEST_RELEASE_BRANCH_APP2}"),
                                string(name: 'GITHUB_TOKEN', value: githubToken)
                            ]
                        }
                    }
                }

                stage('Job for App3') {
                    steps {
                        script {
                            // Получение токена GitHub из учетных данных Jenkins
                            def githubToken = credentials(GITHUB_TOKEN_CREDENTIAL_ID)

                            // Запуск Job3 с параметром BRANCH
                            build job: 'Job3', parameters: [
                                string(name: 'BRANCH', value: "${env.LATEST_RELEASE_BRANCH_APP3}"),
                                string(name: 'GITHUB_TOKEN', value: githubToken)
                            ]
                        }
                    }
                }
            }
        }
    }
}
