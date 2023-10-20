/*
This Pipeline example demonstrates how to use the Gremlin API to check the Gremlin Score of a service
before promoting it to production. The Gremlin Score is a measure of the reliability of a service.
If the Gremlin Score is less than the value set, the pipeline will fail and the service will not be promoted to production,
 */

pipeline {
    agent any

    stages {
        stage('Check Gremlin Score') {
            steps {
                script {
                    def serviceId = 'Replace with your service ID'
                    def teamId = 'Replace with your team ID'
                    def apiUrl = "https://api.staging.gremlin.com/v1/services/${serviceId}/score?teamId=${teamId}"
                    def apiToken = 'Bearer Replace with your Bearer token or API token'
                    def minScore = 80.0 // Replace with your minimum Gremlin Score

                    def response = sh(script: "curl -s -X GET '${apiUrl}' -H 'Authorization: ${apiToken}' -H 'accept: application/json'", returnStatus: true)

                    if (response != 0) {
                        error("API call to Gremlin failed with status code: ${response}")
                    } else {
                        def apiResponse = sh(script: "curl -s -X GET '${apiUrl}' -H 'Authorization: ${apiToken}' -H 'accept: application/json'", returnStdout: true).trim()

                        echo "API Response: ${apiResponse}" // Debug logging

                        // Attempt to capture numbers using a permissive regex
                        def scoreMatches = (apiResponse =~ /(\d+(\.\d+)?)/)

                        if (scoreMatches) {
                            def score = null

                            for (match in scoreMatches) {
                                def potentialScore = match[0]
                                try {
                                    score = Float.parseFloat(potentialScore)
                                    break
                                } catch (NumberFormatException e) {
                                    // Continue searching for a valid score
                                }
                            }

                            if (score != null) {
                                echo "Gremlin Score: ${score}" // Debug logging

                                if (score < minScore)
                                    error("Gremlin Score ${score} is less than defined ${minScore}. Cannot promote to production.")
                                }
                            } else {
                                echo "No valid score found in API response." // Debug logging
                                error("Unable to extract Gremlin Score from the API response.")
                            }
                        }
                    }
                }
            }
        }

        stage('Promote to Production') {
            steps {
                // Add the steps to promote to production here
                // This could involve deployment and other production-related tasks
                // You can replace this comment with the actual steps for your deployment process
                echo "Promoting to production..."
            }
        }
    }

    post {
        failure {
            echo "The pipeline has failed. Not promoting to production."
        }
        success {
            echo "The pipeline has succeeded. Promoting to production."
        }
    }
}

