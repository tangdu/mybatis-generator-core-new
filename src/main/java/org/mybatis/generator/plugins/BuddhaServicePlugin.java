package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.config.JavaFacadeGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * generate service class
 *
 * @author tangdu
 * @date 2016-03-17 10:34:45
 */
public class BuddhaServicePlugin extends PluginAdapter {
    public BuddhaServicePlugin() {
        super();
    }
    String facadePack;
    FullyQualifiedJavaType facadeInterfaceType;

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        JavaFacadeGeneratorConfiguration javaFacadeGeneratorConfiguration = introspectedTable.getContext().getJavaFacadeGeneratorConfiguration();
        this.facadePack = javaFacadeGeneratorConfiguration.getTargetPackage();
    }

    /**
     * 读取配置文件
     */
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    protected String toLowerCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     *
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        String entityName = introspectedTable.getDomainReplaceObjectName();
        facadeInterfaceType = new FullyQualifiedJavaType(facadePack + "." + entityName + "Facade");
        String facadeName=toLowerCase(entityName)+"Facade";
        StringBuilder sb=new StringBuilder();
        sb.append("<dubbo:service\n" + "\t\tinterface=\""+facadeInterfaceType.getFullyQualifiedName()+"\"\n" + "\t\tref=\""+facadeName+"\" version=\"1.0.0\" delay=\"-1\" protocol=\"dubbo\" retries=\"0\"/>");
        System.out.println(sb.toString());
        return new ArrayList<>();
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }
}
