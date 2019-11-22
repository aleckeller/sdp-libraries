import org.boozallen.plugins.jte.hooks.*
import java.lang.annotation.Annotation

@Validate
void call(Map context){
  // Map that holds the types of notifications that are going to be used in the pipeline
  // This map is used later on to determine what types of notifications to send out based on cause
  notifications_used = [:]
  
  // Map that holds the types of notifications that are supported by the notification library
  // [notification type, method to be called for type]
  supported_methods = [:]
  Hooks.discover(NotificationType, getBinding()).each{ method ->
    supported_methods.put(method.invoke(), method.stepWrapper)
  }
  
  supported_methods.each { type, method ->
    if (config.containsKey(type)){
      def attributes = method.get_attribute_parameters()
      // Check if cause attribute and give warning that no notification will be sent unless
      // method is called somewhere else in the pipeline
      if (!config[type].cause){
        println """
                WARNING: The cause attribute has not been specified for ${type} notification!
                ${type} notification will not be sent out unless directly called in the pipeline.
                """
      }
      attributes.each { attribute, details ->
        if (details.required && !config[type][attribute]){
          error """
          must provide ${attribute} parameter
          --
          error_msg
          """
        }
        if (config[type][attribute]){
          if (!(config[type][attribute].getClass() in [ details.type ])){
            error """
            ${attribute} parameter must be an ${details.type}, received ${config[type][attribute].getClass()}
            --
            error_msg
            """
          }
        }
      }
      notifications_used.put(type, config[type])
      method.initilize(config[type])
    }
  }
}

@Notify
void send_based_on_cause(Map context){
  notifications_used.each { type, attributes ->
    if (!attributes.cause || context.step) return
    switch(attributes.cause) {
      case "always":
        supported_methods.get(type).call()
        break;
      case "success":
        if (context.status == "SUCCESS") supported_methods.get(type).call()
        break;
      case "failure":
        if (context.status == "FAILURE") supported_methods.get(type).call()
        break;
    }
  }
}
