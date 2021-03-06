package com.ppdai.das.client.transaction;


import java.lang.reflect.Method;

import com.ppdai.das.client.annotation.DasTransactional;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


@Component(DasAnnotationValidator.VALIDATOR_NAME)
public class DasAnnotationValidator implements BeanPostProcessor {
    public static final String VALIDATOR_NAME = "com.ppdai.das.client.transaction.DasAnnotationValidator";
    public static final String VALIDATION_MSG = "Bean annotated by @DasTransactional or @Transactional(deprecated) must be created through DalTransactionManager.create()";
    private static final String CGLIB_SIGNATURE = "$$EnhancerByCGLIB$$";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class targetClass = bean.getClass();

        while(targetClass.getName().contains(CGLIB_SIGNATURE)) {
            for(Class interf: targetClass.getInterfaces()) {
                if(interf == TransactionalIntercepted.class) {
                    return bean;
                }
            }

            targetClass = targetClass.getSuperclass();
        }

        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            validate(targetClass, method, DasTransactional.class);
        }

        return bean;
    }

    private void validate(Class targetClass, Method method, Class annotationClass) {
        if (method.isAnnotationPresent(annotationClass)) {
            throw new BeanInstantiationException(targetClass, VALIDATION_MSG);
        }
    }
}
