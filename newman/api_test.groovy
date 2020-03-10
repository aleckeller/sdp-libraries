void call(app_env){
    node{
        def userInput = true
        def didTimeout = false
        try{
            timeout(time: 10, unit: 'MINUTES') {
                userInput = input( 
                    id: "API-Testing", 
                    message: "API-Testing",
                    parameters: [
                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: "Run API Testing?"]
                    ]
                )
            }
            userInput = true
        }catch(err){
                def user = err.getCauses()[0].getUser()
                if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
                    didTimeout = true
                } else {
                    userInput = false
                }
        }

        if (userInput || didTimeout){
            run_api_tests(app_env)
        }
        else{
            echo "Not running API Tests due to input.."
        }
    }
}

void run_api_tests(app_env){   
    // get directory of where the collections are stored
    collections_directory = config.collections_directory ?:
            "collections"
    newman_config = app_env.newman_config
    println newman_config
    stage "API Testing", {
        echo "running api tests"
        try{
            sh "docker rm --force risk-engine-newman-container"
        }
        catch(Exception e){}
        try{
            sh "docker build -t risk-engine-newman-image -f Dockerfile.newman ."
            sh "docker run --name risk-engine-newman-container -d risk-engine-newman-image tail -f /dev/null"
            def files = findFiles(glob: "${collections_directory}/*.json")
            files.each{
                echo "Running ${it.name} collection"
                def collection_name = it.name.take(it.name.lastIndexOf('.'))
                def command = "newman run ${collections_directory}/${it.name} --insecure -r cli,html --reporter-html-export ${collection_name}-report.html"
                if (newman_config && newman_config.env_file){
                    echo "Using ${newman_config.env_file} for environment variables with ${collection_name}"
                    command += " -e collections/data/${newman_config.env_file}"
                }
                else{
                    echo "Env_file key not specified in config. Not using environment variables file.."
                }
                if (newman_config && newman_config.collections){
                    if (newman_config.collections["$collection_name"]){
                        echo "Using ${newman_config.collections["$collection_name"]} for data with ${collection_name}"
                        command += " -d collections/data/${newman_config.collections["$collection_name"]}"
                    }
                    else{
                        echo "Data CSV for ${collection_name} not specified in config. Not using data.."
                    }
                }
                else{
                    echo "Collections key not specified in config. Not using data.."
                }
                def api_results = sh (
                    script: "docker exec risk-engine-newman-container ${command}",
                    returnStdout: true
                )
                sh "docker cp risk-engine-newman-container:${collection_name}-report.html ."
                archiveArtifacts artifacts: "${collection_name}-report.html"
                echo api_results
                echo "RESULTS FOR ${it.name} ALSO AVAILABLE HERE: ${env.BUILD_URL}artifact/${collection_name}-report.html"
            }
        }
        catch(ex){
            println "Newman tests failed with: ${ex}"
        }
        finally{
            sh "docker rm --force risk-engine-newman-container"
        }
    }
}
