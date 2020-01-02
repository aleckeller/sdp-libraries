import java.lang.annotation.Retention
import static java.lang.annotation.ElementType.*
import static java.lang.annotation.RetentionPolicy.RUNTIME

@Retention(RUNTIME)
public @interface NotificationType{
    Class value() default { true } 
}

void call(){}
