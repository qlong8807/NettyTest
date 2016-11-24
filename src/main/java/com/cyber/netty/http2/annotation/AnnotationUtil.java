/**
 * 
 */
package com.cyber.netty.http2.annotation;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyber.netty.http2.util.ClassUtil;

/**
 * @author zyl
 * @date 2016年11月24日
 * 
 */
public class AnnotationUtil {

	public static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
	private static Logger logger = LoggerFactory.getLogger(AnnotationUtil.class);

	public static Class<?> getControllerClass(String mapName) {
		Class<?> c = classMap.get(mapName);
		return c;
	}

	public static void initMap(String basePackage) {
		List<Class<?>> classes = ClassUtil.getClasses(basePackage);
		for (Class<?> clazz : classes) {
			Annotation[] annotations = clazz.getAnnotations();
			for (Annotation a : annotations) {
				if (a instanceof NController) {
					NController anno = clazz.getAnnotation(NController.class);
					System.out.println(anno.value() + "---" + clazz.getName());
					classMap.put(anno.value(), clazz);
				}
			}
		}
		if (classMap.size() == 0) {
			logger.error("包"+basePackage+"下没有找到有注解NController的类");
		}
	}

}
