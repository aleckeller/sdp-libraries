import org.boozallen.plugins.jte.config.*
import jenkins.model.Jenkins;

void call(String path, String config_file){
    TemplateConfigObject obj = TemplateConfigDsl.parse(config_file)
    Map config = obj.getConfig()
    if (config.type){
        if (!(config.type instanceof String)){
            error "type key must be a String, received [${config.type}]"
        }
    }
    else{
        error "type key needs to be specified in order to determine what kind of job to create. Options: multibranch or github_org"
    }
    switch(config.type){
        case "multibranch":
            multibranch_options(path,config)
            break
        case "github_org":
            gh_org_options(path,config)
            break
        case "on_demand":
            def folder = Jenkins.instance.getItemByFullName(path.substring(0, path.lastIndexOf("/")))
            create_on_demand(path.substring(path.lastIndexOf("/") + 1), config_file, folder)
            break
    }
}
def multibranch_options(path,config){
    String error_msg = """
    This step has the following library parameters:
      project_repository:  [String] // required
      credentials: [String]  // required
      branches: [String] // required
      jte: [Map] // optional
      periodic_trigger: [String] //optional
      clone_option : [Map] //optional
    """
    if (config.project_repository){
        if (!(config.project_repository instanceof String)){
            error """
            project_repository key must be a String, received [${config.project_repository}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        project_repository was not specified
        --
        ${error_msg}
        """
    }
    if (config.credentials){
        if (!(config.credentials instanceof String)){
            error """
            credentials key must be a String, received [${config.credentials}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        credentials was not specified
        --
        ${error_msg}
        """
    }
    if (config.branches){
        if (!(config.branches instanceof String)){
            error """
            branches key must be a String, received [${config.branches}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        branches was not specified
        --
        ${error_msg}
        """
    }
    if (config.jte){
        if (!(config.jte instanceof Map)){
            error """
            jte key must be a Map, received [${config.jte}]
            --
            ${error_msg}
            """
        }
    }
    if (config.periodic_trigger){
        if (!(config.periodic_trigger instanceof String)){
            error """
            periodic_trigger key must be a String, received [${config.periodic_trigger}]
            --
            ${error_msg}
            """
        }
    }
    if (config.clone_option){
        if (!(config.clone_option instanceof Map)){
            error """
            clone_option key must be a Map, received [${config.clone_option}]
            --
            ${error_msg}
            """
        }
    }
    create_multibranch(path,config)
}

def gh_org_options(path,config){
    String error_msg = """
    This step has the following library parameters:
      organization:  [String] // required
      credentials_id: [String]  // required
      api_uri: [String] // required
      includes: [String] // optional
      jte: [Map] // optional
    """
    if (config.organization){
        if (!(config.organization instanceof String)){
            error """
            organization key must be a String, received [${config.organization}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        organization not specified
        --
        ${error_msg}
        """
    }
    if (config.credentials_id){
        if (!(config.credentials_id instanceof String)){
            error """
            credentials_id key must be a String, received [${config.credentials_id}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        credentials_id not specified
        --
        ${error_msg}
        """
    }
    if (config.api_uri){
        if (!(config.api_uri instanceof String)){
            error """
            api_uri key must be a String, received [${config.api_uri}]
            --
            ${error_msg}
            """
        }
    }
    else{
        error """
        api_uri not specified
        --
        ${error_msg}
        """
    }
    if (config.includes){
        if (!(config.includes instanceof String)){
            error """
            includes key must be a String, received [${config.includes}]
            --
            ${error_msg}
            """
        }
    }
    else{
        config.put("includes","*")
    }
    if (config.jte){
        if (!(config.jte instanceof Map)){
            error """
            jte key must be a Map, received [${config.jte}]
            --
            ${error_msg}
            """
        }
    }
    create_github_org(path,config)
}
