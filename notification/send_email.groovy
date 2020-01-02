import org.boozallen.plugins.jte.config.*

@NotificationType
String config_name(){
  return "email"
}

void initilize(Map attributes){
  env.EMAIL_RECIPIENTS = attributes.recipients
  env.EMAIL_BODY = attributes.body ?: "\${JELLY_SCRIPT, template=\"html-with-health-and-console\"}"
  env.EMAIL_SUBJECT = attributes.subject ?: "${env.JOB_NAME} Status"
  env.MIME_TYPE = attributes.mime_type ?: "text/html"
}

void call(){
  stage("Send Email") {
    node{
      emailext mimeType: env.MIME_TYPE, body: env.EMAIL_BODY, subject: env.EMAIL_SUBJECT, to: env.EMAIL_RECIPIENTS
    }
  }
}

void set_body(String body){
  env.EMAIL_BODY = body
}

void set_subject(String subject){
  env.EMAIL_SUBJECT = subject
}

void set_recipients(String recipients){
  env.EMAIL_RECIPIENTS = recipients
}

void set_mime_type(String type){
  env.MIME_TYPE = type
}

Map get_attribute_parameters(){
  String parameters = """
    body{
      required = false
      type = String
    }
    subject{
      required = false
      type = String
    }
    mime_type{
      required = false
      type = String
    }
    recipients{
      required = true
      type = String
    }
  """
  return TemplateConfigDsl.parse(parameters).getConfig()
}
