void call(def app_env=null){
    stage("Approval") {
        String error_msg = """
        This step has the following library parameters:
          message: [String]  // required
          approval_group: [String]  // optional
        """
        if (app_env) {
            if (app_env.approval){
                approval_config = app_env.approval
            }
            else{
                error """
                must provide approval key in application environments -> app_env
                --
                ${error_msg}
                """
            }
        }
        else{
            approval_config = config
        }
        // validate message
        if (approval_config.message){
            if (!(approval_config.message instanceof String)){
                error """
                message parameter must be a String, received [${approval_config.message}]
                --
                ${error_msg}
                """
            }
        }else{
            error """
            must provide message parameter in approval key
            --
            ${error_msg}
            """
        }

        input_map = [id: "${env.BUILD_NUMBER}-approval",
                    message: approval_config.message,
                    parameters: []]
        if (approval_config.approval_group){
            input_map.put("submitter", approval_config.approval_group)
            println "Note: The following group(s) or user(s) are allowed to proceed: ${approval_config.approval_group}"
        }
        input(input_map)
    }
}
