package com.suntimes.cl.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.suntimes.cl.util.CLClassUtil;
import com.suntimes.cl.util.CLStringUtil;


public class CLXMLParser {
	
	public void parseXmlToBean(String filePath, Object obj){
		File file = new File(filePath);
		this.parseXmlToBean(file, obj);
	}
	
	public void parseXmlToBean(File file, Object obj){
		try {
			InputStream inputStream = new FileInputStream(file);
			this.parseXmlToBean(inputStream, obj);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public void parseXmlToBean(InputStream inputStream, Object obj){
		Element element = parseXml(inputStream);
		this.parseXmlToBean(element, obj);
	}
	
	/*public void parseXmlToList(File file, List list, Class genCs, String tagPath){
		Element element = parseXml(file);
		this.parseXmlToList(element, list, genCs, tagPath);
	}*/
	
	protected final static Element parseXml(InputStream inputStream){
		Element docEle = null;
		DocumentBuilderFactory dbfactory=DocumentBuilderFactory.newInstance(); 
		DocumentBuilder db = null;
		try {
			db = dbfactory.newDocumentBuilder();
			Document dom=db.parse(inputStream);
			docEle=dom.getDocumentElement();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return docEle;
	}
	
	
	private NodeList getNodeListByPath(Element element, String fullPath){
		if(fullPath.indexOf("/")>0){
			String[] paths = fullPath.split("/");
			Element childElement = element;
			NodeList nodeList = null;
			for(String path : paths){
				if(childElement!=null){
					nodeList = childElement.getElementsByTagName(path);
					if(nodeList.getLength()>0){
						childElement = (Element)nodeList.item(0);
					}
				}
			}
			return nodeList;
		}else{
			return element.getElementsByTagName(fullPath);
		}
	}
	
	private List<Element> getElementListByTagName(Element element, String tagName){
		List<Element> elementList = new ArrayList<Element>();
		NodeList nodeList = element.getElementsByTagName(tagName);
		for(int i=0;i<nodeList.getLength();i++){
			Element tmp = (Element)nodeList.item(i);
			if(tmp.getParentNode()==element){
				elementList.add(tmp);
			}
		}
		return elementList;
	}
	
	private List<Element> getElementListByPath(Element element, String fullPath){
		
		if(fullPath.indexOf("/")>0){
			String[] paths = fullPath.split("/");
			Element childElement = element;
			//NodeList nodeList = null;
			List<Element> elementList = null;
			for(String path : paths){
				if(childElement!=null){
					/*nodeList = childElement.getElementsByTagName(path);
					
					if(nodeList.getLength()>0){
						childElement = (Element)nodeList.item(0);
					}*/
					elementList = this.getElementListByTagName(childElement, path);
					if(elementList.size()>0){
						childElement = elementList.get(0);
					}
				}
			}
			return elementList;
		}else{
			//return element.getElementsByTagName(fullPath);
			return this.getElementListByTagName(element, fullPath);
		}
	}
	
	private Element getElementByPath(Element element, String fullPath){
		Element ele = null;
		/*NodeList nodeList = getNodeListByPath(element, fullPath);
		
		//Log.i("xml", "getElementByPath  >>> fullPath >> " + fullPath + " nodeList >>  " + nodeList.getLength());
		for(int i=0;i<nodeList.getLength();i++){
			Element tmp = (Element)nodeList.item(i);
			if(tmp.getParentNode()==element){
				ele = tmp;
				break;
			}
			//Log.i("xml", "getElementByPath >>>  ele.parent:["+ele.getParentNode()+"], element:["+element+"]");
		}
		if(nodeList.getLength()>0 && ele==null){
			ele = (Element)nodeList.item(0);
		}*/
		List<Element> elementList = this.getElementListByPath(element, fullPath);
		if(elementList.size()>0){
			ele = elementList.get(0);
		}
		return ele;
	}
	
	
	
	
	/**
	 * parse the xml to java bean
	 * @param element
	 * @param obj
	 */
	protected final void parseXmlToBean(Element element, Object obj){
		Class<?> cs = obj.getClass();
		Field[] fields = CLClassUtil.getFields(cs);
		
		for(Field field : fields){
			CLXmlMapping xmlInfo = null;
			xmlInfo = field.getAnnotation(CLXmlMapping.class);
			if(xmlInfo==null){
				Method getter = CLClassUtil.getGetterMethod(cs, field.getName());
				if(getter!=null){
					xmlInfo = getter.getAnnotation(CLXmlMapping.class);
				}
			}
			
			if(xmlInfo!=null){
				String tagPath = xmlInfo.tagPath();
				//Log.i("xml", "parseXmlToBean >>>  fieldName:["+field.getName()+"], path:["+xmlInfo.tagPath()+"], genericType:["+xmlInfo.genericType()+"]");
				if(xmlInfo.type() == CLXmlMapping.TYPE_COLLECTION){
					String genericType = xmlInfo.genericType();
					
					
					
					try {
						List<Object> list = new ArrayList<Object>();
						Class<?> genCs = Class.forName(genericType);
						this.parseXmlToList(element, list, genCs, tagPath);
						CLClassUtil.setValue(obj, field, list);
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}else{
					Element ele = null;
					if(".".equals(tagPath)){
						ele = element;
					}else{
						ele = getElementByPath(element, tagPath);
					}
					//Log.i("xml", "parseXmlToBean >> ele:["+ele+"]");
					//Log.i("xml", "parseXmlToBean >> value:["+getNodeValue(ele)+"]");
					boolean isXMLPojo = (obj instanceof CLXMLPojo);
					CLXMLPojo xmlPojo = null;
					if(isXMLPojo){
						xmlPojo = (CLXMLPojo)obj;
					}
					if(ele!=null){
						String attributeName = xmlInfo.attributeName();
						Class<?> fieldClass = field.getType();
						if(fieldClass.isAssignableFrom(String.class)){
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
								value = CLStringUtil.replaceEscapeSequence(value);
							}else{
								value = getNodeValue(ele);
								value = CLStringUtil.replaceEscapeSequence(value);
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, value);
							}else{
								CLClassUtil.setValue(obj, field, value);
							}
							
						}else if(fieldClass.isAssignableFrom(Integer.class) ||  fieldClass.isAssignableFrom(int.class)){
							Integer integerValue = null;
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
							}else{
								value = getNodeValue(ele);
							}
							if(value!=null){
								try{
									integerValue = Integer.parseInt(value);
								}catch (Exception e) {
									integerValue = null;
								}
								
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, integerValue);
							}else{
								CLClassUtil.setValue(obj, field, integerValue);
							}
							
						}else if(fieldClass.isAssignableFrom(Float.class) ||  fieldClass.isAssignableFrom(float.class)){
							Float floatValue = null;
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
							}else{
								value = getNodeValue(ele);
							}
							if(value!=null){
								try{
									floatValue = Float.parseFloat(value);
								}catch (Exception e) {
									floatValue = null;
								}
								
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, floatValue);
							}else{
								CLClassUtil.setValue(obj, field, floatValue);
							}
							
						}else if(fieldClass.isAssignableFrom(Long.class) ||  fieldClass.isAssignableFrom(long.class)){
							Long longValue = null;
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
							}else{
								value = getNodeValue(ele);
							}
							if(value!=null){
								try{
									longValue = Long.parseLong(value);
								}catch (Exception e) {
									longValue = null;
								}
								
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, longValue);
							}else{
								CLClassUtil.setValue(obj, field, longValue);
							}
							
						}else if(fieldClass.isAssignableFrom(Boolean.class) ||  fieldClass.isAssignableFrom(boolean.class)){
							Boolean booleanValue = new Boolean(false);
							String trueValue = xmlInfo.trueValue();
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
							}else{
								value = getNodeValue(ele);
							}
							if(value!=null){
								try{
									if(trueValue.equals(value)){
										booleanValue = new Boolean(true);
									}
								}catch (Exception e) {
									booleanValue = false;
								}
								
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, booleanValue);
							}else{
								CLClassUtil.setValue(obj, field, booleanValue);
							}
							
						}else if(fieldClass.isAssignableFrom(Date.class)){
							Date date = null;
							String dateFormat = xmlInfo.dateFormat();
							String value = null;
							if(!"".equals(attributeName)){
								value = getNodeAttributeValue(ele, attributeName);
							}else{
								value = getNodeValue(ele);
							}
							if(value!=null){
								try{
									SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
									date = sdf.parse(value);
								}catch (Exception e) {
									
								}
								
							}
							if(isXMLPojo){
								xmlPojo.set(attributeName, date);
							}else{
								CLClassUtil.setValue(obj, field, date);
							}
							
						}else if(field.getType().isEnum()){
							try{
								String value = null;
								if(!"".equals(attributeName)){
									value = getNodeAttributeValue(ele, attributeName);
								}else{
									value = getNodeValue(ele);
								}
								Method method = field.getType().getDeclaredMethod("getByString", new Class[]{String.class});
								if(method!=null){
									Object enumValue = method.invoke(null, new String[]{value});
									CLClassUtil.setValue(obj, field, enumValue);
								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}else{
							try {
								Object value = null;
								if(xmlInfo.genericType()!=null && !"".equals(xmlInfo.genericType())){
									Class<?> genericClass = Class.forName(xmlInfo.genericType());
									value = genericClass.newInstance();
								}else{
									value = field.getType().newInstance();
								}
								parseXmlToBean(ele, value);
								if(isXMLPojo){
									xmlPojo.set(attributeName, value);
								}else{
									CLClassUtil.setValue(obj, field, value);
								}
								
							} catch (Exception e) {
								//e.printStackTrace();
							}
							
						}
					}
					
				}
			}
		}
	}
	
	
	private void parseXmlToList(Element element, List<Object> list, Class<?> genCs, String tagPath){
		NodeList nodeList = getNodeListByPath(element, tagPath);
		if(nodeList!=null){
			try {
				if(genCs.isAssignableFrom(String.class)){
					for(int i=0;i<nodeList.getLength();i++){
						Element ele = (Element)nodeList.item(i);
						String value = getNodeValue(ele);
						value = CLStringUtil.replaceEscapeSequence(value);
						list.add(value);
					}
				}else if(genCs.isAssignableFrom(Integer.class) || genCs.isAssignableFrom(int.class)){
					for(int i=0;i<nodeList.getLength();i++){
						Element ele = (Element)nodeList.item(i);
						String value = getNodeValue(ele);
						list.add(Integer.parseInt(value));
					}
				}else{
					for(int i=0;i<nodeList.getLength();i++){
						Element ele = (Element)nodeList.item(i);
						Object genObj = genCs.newInstance();
						this.parseXmlToBean(ele, genObj);
						list.add(genObj);
					}
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}
	
	
	
	private String getNodeValue(Element ele){
		StringBuffer value = new StringBuffer("");
		if(ele!=null){
			NodeList nodeList = ele.getChildNodes();
			for(int i=0;i<nodeList.getLength();i++){
				Node node = nodeList.item(i);
				value.append(node.getNodeValue());
			}
			
		}
		return value.toString();
	}
	
	private String getNodeAttributeValue(Element ele, String attributeName){
		return ele.getAttribute(attributeName);
	}
	
}
