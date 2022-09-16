package worksmobile.intern.apigateway.utils;


public class BeanUtils {
    public static Object getBean(String beanName) {
        return ApplicationContextProvider.getApplicationContext().getBean(beanName);
    }
}
