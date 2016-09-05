/**
 * 
 */
package com.cyber.encry;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;




/**
 * @author Administrator
 *
 */
public class JsonUtil {

//	private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	
	private static ObjectMapper mapper = null;
	static {
		mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	
	/**
	 * 将对象转换为json字符串
	 * @param object
	 * @return
	 */
	public static String objectToJson(Object object){

		Writer strWriter = new StringWriter();
		try {
			mapper.writeValue(strWriter, object);
			return strWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
//			logger.error("转换错误!", e.getCause());
		}
		return "";
	}
	
	/**
	 * 将对象json转换为对象，针对单个对象，也就是对象里面没有嵌套对象的
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static Object jsonToObject(String object,Class<?> clazz){
		try {
			
			return mapper.readValue(new StringReader(object), clazz);
		} catch (Exception e) {
			e.printStackTrace();
//			logger.error("转换错误!", e.getCause());
		} 
		return null;
	}
	
	/**
     * 根据json串和节点名返回节点
     * 
     * @param json
     * @param nodeName
     * @return
     */
    public static JsonNode getNode(String json, String nodeName) {
        JsonNode node = null;
        try {
            node = mapper.readTree(json);
            return node.get(nodeName);
        } catch (JsonProcessingException e) {
//        	logger.warn("json error:" + e.getMessage());
        } catch (IOException e) {
//        	logger.warn("json error:" + e.getMessage());
        }
        return node;
    }
	
    /**
     * 根据json串和节点名返回节点
     * 
     * @param json
     * @param nodeName
     * @return
     */
    public static <T>T jsonNodetoObject(JsonNode jsonnode,TypeReference<T> typeReference) {
        try {
        	mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(jsonnode,typeReference);
        } catch (JsonProcessingException e) {
        	e.printStackTrace();
//        	logger.warn("json error:" + e.getMessage());
        } catch (IOException e) {
        	e.printStackTrace();
//        	logger.warn("json error:" + e.getMessage());
        }
        return null;
    }
    
    /**
     * 根据json串和节点名返回节点
     * 
     * @param json
     * @param nodeName
     * @return
     */
    public static <T>T jsontoObject(String jsonnode,TypeReference<T> typeReference) {
        try {
        	mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(jsonnode,typeReference);
        } catch (JsonProcessingException e) {
        	e.printStackTrace();
//        	logger.warn("json error:" + e.getMessage());
        } catch (IOException e) {
        	e.printStackTrace();
//        	logger.warn("json error:" + e.getMessage());
        }
        return null;
    }
	
	

}
