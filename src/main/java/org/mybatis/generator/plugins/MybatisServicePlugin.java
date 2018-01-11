package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * generate service class
 *
 * @author Liyb
 * @date 2016-03-17 10:34:45
 */
public class MybatisServicePlugin extends PluginAdapter {

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType interfaceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType pojoCriteriaType;
    private FullyQualifiedJavaType listType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private String servicePack;
    private String serviceImplPack;
    private String project;
    private String pojoUrl;
    private SimpleDateFormat dateFormat1=new SimpleDateFormat("yyyy年MM月dd日 上午HH:mm");
    private SimpleDateFormat dateFormat2=new SimpleDateFormat("yyyy年MM月dd日 下午HH:mm");
    /**
     * 所有的方法
     */
    private List<Method> methods;

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getContext().getJavaServiceGeneratorConfiguration();
        this.servicePack = javaServiceGeneratorConfiguration.getTargetPackage();
        this.serviceImplPack = javaServiceGeneratorConfiguration.getImplementationPackage();
        this.project = javaServiceGeneratorConfiguration.getTargetProject();
        this.pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
        service = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
    }

    public MybatisServicePlugin() {
        super();
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
        methods = new ArrayList<Method>();
    }

    /**
     * 读取配置文件
     */
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     *
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.pojoUrl + ".", "");
        interfaceType = new FullyQualifiedJavaType(servicePack + "." + tableName + "Service");
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + tableName + "ServiceImpl");
        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
        listType = new FullyQualifiedJavaType("java.util.List");
        Interface interface1 = new Interface(interfaceType);
        TopLevelClass topLevelClass = new TopLevelClass(serviceType);
        TopLevelClass topLevelClass2 = new TopLevelClass(interfaceType);

        addJavaFileComment(interface1);
        addJavaFileComment(topLevelClass);
        addClassComment(topLevelClass2,introspectedTable);
        addClassComment(topLevelClass,introspectedTable);

        // 导入必须的类
        addImport(interface1, topLevelClass);
        // 接口
        addService(interface1, introspectedTable, tableName, files);
        // 实现类
        addServiceImpl(topLevelClass, introspectedTable, tableName, files);
        addLogger(topLevelClass);

        return files;
    }

    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine("* xnh.com Inc.");
        compilationUnit.addFileCommentLine("* Copyright (c) 2017-2018 All Rights Reserved.");
        compilationUnit.addFileCommentLine("*/");
    }

    public void addClassComment(InnerClass innerClass,
                                IntrospectedTable introspectedTable) {
        String username=System.getProperty("user.name");
        String dateStr="";
        int hour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour>=0 && hour<=12){
            dateStr=dateFormat1.format(new Date());
        }else{
            dateStr=dateFormat2.format(new Date());
        }
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getRemarks()+"服务实现方");
        sb.append("\n");
        sb.append(" * ");
        sb.append("\n");
        sb.append(" * ");
        sb.append("@author ").append(username).append("\n");
        sb.append(" * ").append("@version ")
                .append(String.format("$: %s.java, v 0.1 %s %s Exp $ ",
                        innerClass.getType().getShortName(),dateStr,username));
        innerClass.addJavaDocLine(sb.toString());
        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
    }


    /**
     * add interface
     *
     * @param tableName
     * @param files
     */
    protected void addService(Interface interface1, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        interface1.setVisibility(JavaVisibility.PUBLIC);

        Method method = deleteById(introspectedTable, tableName);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = queryById(introspectedTable, tableName);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("add", introspectedTable, tableName);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("updateByParams", introspectedTable, tableName);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("updateById",introspectedTable, tableName);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getJavaFormatter());

        files.add(file);
    }

    /**
     * add implements class
     *
     * @param introspectedTable
     * @param tableName
     * @param files
     */
    protected void addServiceImpl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.addSuperInterface(interfaceType);

        topLevelClass.addAnnotation("@Service");
        topLevelClass.addImportedType(service);
        // add import dao
        addField(topLevelClass, tableName);
        // add method
        topLevelClass.addMethod(queryById(introspectedTable, tableName));
        topLevelClass.addMethod(deleteById(introspectedTable, tableName));
        topLevelClass.addMethod(getOtherboolean("updateByParams", introspectedTable, tableName));
        topLevelClass.addMethod(getOtherboolean("updateById",introspectedTable, tableName));
        topLevelClass.addMethod(getOtherboolean("add", introspectedTable, tableName));

        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 添加字段
     *
     * @param topLevelClass
     */
    protected void addField(TopLevelClass topLevelClass, String tableName) {
        // add dao
        Field field = new Field();
        field.setName(toLowerCase(daoType.getShortName())); // set var name
        topLevelClass.addImportedType(daoType);
        field.setType(daoType); // type
        field.setVisibility(JavaVisibility.PRIVATE);
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);
    }

    /**
     * 添加方法
     */
    protected Method queryById(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.setName("queryById");
        method.setReturnType(pojoType);
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            method.addParameter(new Parameter(type, "key"));
        } else {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
            }
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this.");
        sb.append(getDaoShort());
        sb.append("queryById");
        sb.append("(");
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    protected Method deleteById(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.setName("deleteById");
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            method.addParameter(new Parameter(type, "key"));
        } else {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
            }
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("if(this.");
        sb.append(getDaoShort());
        sb.append("deleteById");
        sb.append("(");
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")>0){return true;}else{return false;}");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * add method
     */
    protected Method getOtherInteger(String methodName, IntrospectedTable introspectedTable, String tableName, int type) {
        Method method = new Method();
        method.setName(methodName);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        String params = addParams(introspectedTable, method, type);
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this.");
        sb.append(getDaoShort());
        if (introspectedTable.hasBLOBColumns() && (!"updateByParams".equals(methodName)
                && !"deleteById".equals(methodName) && !"deleteByExample".equals(methodName)
                && !"updateById".equals(methodName))) {
            sb.append(methodName + "WithoutBLOBs");
        } else {
            sb.append(methodName);
        }
        sb.append("(");
        sb.append(params);
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * add method
     */
    protected Method getOtherboolean(String methodName, IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.setName(methodName);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.addParameter(new Parameter(pojoType, "record"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        if (returnType == null) {
            sb.append("this.");
        } else {
            sb.append("if(this.");
        }
        sb.append(getDaoShort());
        sb.append(methodName);
        sb.append("(");
        sb.append("record");
        sb.append(")>0){return true;}else{return false;}");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * type: pojo 1 key 2 example 3 pojo+example 4
     */
    protected String addParams(IntrospectedTable introspectedTable, Method method, int type1) {
        switch (type1) {
            case 1:
                method.addParameter(new Parameter(pojoType, "record"));
                return "record";
            case 2:
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    method.addParameter(new Parameter(type, "key"));
                } else {
                    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                        method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
                    }
                }
                StringBuffer sb = new StringBuffer();
                for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append(",");
                }
                sb.setLength(sb.length() - 1);
                return sb.toString();
            case 3:
                method.addParameter(new Parameter(pojoCriteriaType, "example"));
                return "example";
            case 4:
                method.addParameter(0, new Parameter(pojoType, "record"));
                method.addParameter(1, new Parameter(pojoCriteriaType, "example"));
                return "record, example";
            default:
                break;
        }
        return null;
    }

    protected void addComment(JavaElement field, String comment) {
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        comment = comment.replaceAll("\n", "<br>\n\t * ");
        sb.append(comment);
        field.addJavaDocLine(sb.toString());
        field.addJavaDocLine(" */");
    }

    /**
     * add field
     *
     * @param topLevelClass
     */
    protected void addField(TopLevelClass topLevelClass) {
        // add success
        Field field = new Field();
        field.setName("success"); // set var name
        field.setType(FullyQualifiedJavaType.getBooleanPrimitiveInstance()); // type
        field.setVisibility(JavaVisibility.PRIVATE);
        addComment(field, "excute result");
        topLevelClass.addField(field);
        // set result
        field = new Field();
        field.setName("message"); // set result
        field.setType(FullyQualifiedJavaType.getStringInstance()); // type
        field.setVisibility(JavaVisibility.PRIVATE);
        addComment(field, "message result");
        topLevelClass.addField(field);
    }

    /**
     * add method
     */
    protected void addMethod(TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setSuccess");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "success"));
        method.addBodyLine("this.success = success;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName("isSuccess");
        method.addBodyLine("return success;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setMessage");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "message"));
        method.addBodyLine("this.message = message;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("getMessage");
        method.addBodyLine("return message;");
        topLevelClass.addMethod(method);
    }

    /**
     * add method
     */
    protected void addMethod(TopLevelClass topLevelClass, String tableName) {
        Method method2 = new Method();
        for (int i = 0; i < methods.size(); i++) {
            Method method = new Method();
            method2 = methods.get(i);
            method = method2;
            //method.removeAllBodyLines();
            //method.removeAnnotation();
            StringBuilder sb = new StringBuilder();
            sb.append("return this.");
            sb.append(getDaoShort());
            sb.append(method.getName());
            sb.append("(");
            List<Parameter> list = method.getParameters();
            for (int j = 0; j < list.size(); j++) {
                sb.append(list.get(j).getName());
                sb.append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(");");
            method.addBodyLine(sb.toString());
            topLevelClass.addMethod(method);
        }
        methods.clear();
    }

    /**
     * BaseUsers to baseUsers
     *
     * @param tableName
     * @return
     */
    protected String toLowerCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * BaseUsers to baseUsers
     *
     * @param tableName
     * @return
     */
    protected String toUpperCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * import must class
     */
    private void addImport(Interface interfaces, TopLevelClass topLevelClass) {
        interfaces.addImportedType(pojoType);
        //interfaces.addImportedType(listType);
        topLevelClass.addImportedType(daoType);
        topLevelClass.addImportedType(interfaceType);
        topLevelClass.addImportedType(pojoType);
        //topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType(slf4jLoggerFactory);
        topLevelClass.addImportedType(service);
        topLevelClass.addImportedType(autowired);
    }

    /**
     * import logger
     */
    private void addLogger(TopLevelClass topLevelClass) {
        Field field = new Field();
        field.setFinal(true);
        field.setInitializationString("LoggerFactory.getLogger(" + topLevelClass.getType().getShortName() + ".class)"); // set value
        field.setName("LOGGER"); // set var name
        field.setStatic(true);
        field.setType(new FullyQualifiedJavaType("Logger")); // type
        field.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(field);
    }

    private String getDaoShort() {
        return toLowerCase(daoType.getShortName()) + ".";
    }

    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
