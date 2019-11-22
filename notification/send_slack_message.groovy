import org.boozallen.plugins.jte.config.*

@NotificationType
String config_name(){
  return "slack"
}

void initilize(Map attributes){
  println "init stuff for slack"
}

void call(){
  stage("Send Slack Message") {
    node{
      println "Sending Slack Message.."
    }
  }
}

Map get_attribute_parameters(){
  String parameters = """
    color{
      required = false
      type = String
    }
  """
  return TemplateConfigDsl.parse(parameters).getConfig()
}
