import org.boozallen.plugins.jte.config.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.boozallen.plugins.jte.job.TemplateFlowDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.BooleanParameterDefinition
import org.biouno.unochoice.ChoiceParameter
import org.biouno.unochoice.model.GroovyScript
import hudson.model.PasswordParameterDefinition
import hudson.triggers.Trigger
import hudson.triggers.TimerTrigger
import org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty

void call(String job_name,String config_file,def folder){
    TemplateConfigObject obj = TemplateConfigDsl.parse(config_file)
    Map config = obj.getConfig()

    String error_msg = """
    This step has the following library parameters:
      parameters:  [Map] // optional
      template: [String]  // optional
      pipeline_config: [String] // optional
    """
    def has_parameters = false
    def has_template = false
    def has_pipeline_config = false
    def has_properties = false
    if (config.parameters){
        if (!(config.parameters instanceof Map)){
            error """
            parameters key must be a Map, received [${config.parameters}]
            --
            ${error_msg}
            """
        }
        has_parameters = true
    }
    if (config.properties){
        if (!(config.properties instanceof Map)){
            error """
            properties key must be a Map, received [${config.properties}]
            --
            ${error_msg}
            """
        }
        has_properties = true
    }
    if (config.template){
        if (!(config.template instanceof String)){
            error """
            template parameter must be a String, received [${config.template}]
            --
            ${error_msg}
            """
        }
        has_template = true
    }
    if (config.pipeline_config){
        if (!(config.pipeline_config instanceof String)){
            error """
            pipeline_config parameter must be a String, received [${config.pipeline_config}]
            --
            ${error_msg}
            """
        }
        has_pipeline_config = true
    }
    println "Creating ${job_name}.."
    def job = folder.createProject(WorkflowJob, job_name)
    if (has_parameters){
        def parameter_definitions = []
        config.parameters.each { name, attributes ->
            if (attributes.type){
                switch(attributes.type){
                    case "boolean":
                        parameter_definitions.add(boolean_parameter(name, attributes))
                        break
                    case "string":
                        parameter_definitions.add(string_parameter(name, attributes))
                        break
                    case "choice":
                        parameter_definitions.add(choice_parameter(name, attributes))
                        break
                    case "choice_script":
                        parameter_definitions.add(choice_script_parameter(name, attributes))
                        break
                    case "password":
                        parameter_definitions.add(password_parameter(name, attributes))
                        break
                }
            }
            else{
                error """
                ${name} is missing the type attribute
                """
            }
        }
        ParametersDefinitionProperty properties = new ParametersDefinitionProperty(parameter_definitions)
        job.addProperty(properties)
    }
    if (has_properties){
        config.properties.each { name, attributes ->
            switch(name){
                case "build_periodically":
                    job.addProperty(build_periodically_property(attributes))
                    break
            }
        }
    }
    //Setup template and config
    def flowDef = new TemplateFlowDefinition(config.template, config.pipeline_config)
    job.setDefinition(flowDef)
    println "Finished creating ${job_name}!"
}

def boolean_parameter(String name, Map attributes){
    Boolean default_value = false
    String description = ""
    if (attributes.default_value){
        if (!(attributes.default_value instanceof Boolean)){
            error """
            default value must be a Boolean, received [${attributes.default_value}]
            """
        }
        default_value = attributes.default_value
    }
    if (attributes.description){
        if (!(attributes.description instanceof String)){
            error """
            description must be a String, received [${attributes.description}]
            """
        }
        description = attributes.description
    }
    BooleanParameterDefinition param = new BooleanParameterDefinition(name,default_value,description)
    return param
}

def string_parameter(String name, Map attributes){
    String default_value = ""
    String description = ""
    if (attributes.default_value){
        if (!(attributes.default_value instanceof String)){
            error """
            default value must be a String, received [${attributes.default_value}]
            """
        }
        default_value = attributes.default_value
    }
    if (attributes.description){
        if (!(attributes.description instanceof String)){
            error """
            description must be a String, received [${attributes.description}]
            """
        }
        description = attributes.description
    }
    StringParameterDefinition param = new StringParameterDefinition(name,default_value,description)
    return param
}

def choice_parameter(String name, Map attributes){
    String [] choices = []
    String description = ""
    if (attributes.description){
        if (!(attributes.description instanceof String)){
            error """
            description must be a String, received [${attributes.description}]
            """
        }
        description = attributes.description
    }
    String[] choice_array = new String[attributes.choices.size()];
    for (int i = 0; i < attributes.choices.size(); i++){
        if (attributes.choices.get(i) instanceof String){
            choice_array[i] = attributes.choices.get(i)
        }
        else{
            error """
            ${attributes.choices.get(i)} needs to be a string
            """
        }
    }
    ChoiceParameterDefinition param = new ChoiceParameterDefinition(name,choice_array,description)
    return param
}

def choice_script_parameter(String name, Map attributes){
    String description = ""
    String random_name = ""
    String script = ""
    String fallback_script = ""
    String choice_type = ""
    Boolean filterable = false
    Integer filter_length = 1
    if (attributes.description){
        if (!(attributes.description instanceof String)){
            error """
            description must be a String, received [${attributes.description}]
            """
        }
        description = attributes.description
    }
    if (attributes.random_name){
        if (!(attributes.random_name instanceof String)){
            error """
            random_name must be a String, received [${attributes.random_name}]
            """
        }
        random_name = attributes.random_name
    }
    if (attributes.script){
        if (!(attributes.script instanceof String)){
            error """
            script must be a String, received [${attributes.script}]
            """
        }
        script = attributes.script
    }
    if (attributes.fallback_script){
        if (!(attributes.fallback_script instanceof String)){
            error """
            fallback_script must be a String, received [${attributes.fallback_script}]
            """
        }
        fallback_script = attributes.fallback_script
    }
    GroovyScript groovy_script = new GroovyScript(script,fallback_script)
    if (attributes.choice_type){
        if (!(attributes.choice_type instanceof String)){
            error """
            choice_type must be a String, received [${attributes.choice_type}]
            """
        }
        choice_type = attributes.choice_type
    }
    if (attributes.filterable){
        if (!(attributes.filterable instanceof Boolean)){
            error """
            filterable must be a Boolean, received [${attributes.filterable}]
            """
        }
        filterable = attributes.filterable
    }
    if (attributes.filter_length){
        if (!(attributes.filter_length instanceof Integer)){
            error """
            filter_length must be a Integer, received [${attributes.filter_length}]
            """
        }
        filter_length = attributes.filter_length
    }
    ChoiceParameter param = new ChoiceParameter(name,description,random_name,groovy_script,choice_type,filterable,filter_length)
    return param
}
def password_parameter(String name, Map attributes){
    String default_value = ""
    String description = ""
    if (attributes.default_value){
        if (!(attributes.default_value instanceof String)){
            error """
            default value must be a String, received [${attributes.default_value}]
            """
        }
        default_value = attributes.default_value
    }
    if (attributes.description){
        if (!(attributes.description instanceof String)){
            error """
            description must be a String, received [${attributes.description}]
            """
        }
        description = attributes.description
    }
    PasswordParameterDefinition param = new PasswordParameterDefinition(name,default_value,description)
    return param
}

def build_periodically_property(Map attributes){
    String spec = ""
    if (attributes.spec){
        if (!(attributes.spec instanceof String)){
            error """
            spec must be a String, received [${attributes.spec}]
            """
        }
        spec = attributes.spec
    }
    TimerTrigger trigger_timer = new TimerTrigger(spec)
    List<Trigger> trigger_list = new ArrayList<>()
    trigger_list.add(trigger_timer)
    PipelineTriggersJobProperty trigger = new PipelineTriggersJobProperty(trigger_list)
    return trigger
}
