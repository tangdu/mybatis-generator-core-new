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
 * @author tangdu
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

    private FullyQualifiedJavaType uptDOType;
    private FullyQualifiedJavaType uptBatDOType;
    private FullyQualifiedJavaType pageDOType;
    private FullyQualifiedJavaType pageRTDOType;

    private String servicePack;
    private String serviceImplPack;
    private String project;
    private String pojoUrl;
    private SimpleDateFormat       dateFormat1      = new SimpleDateFormat("yyyy年MM月dd日 上午HH:mm");
    private SimpleDateFormat       dateFormat2      = new SimpleDateFormat("yyyy年MM月dd日 下午HH:mm");
    private FullyQualifiedJavaType serializableType = new FullyQualifiedJavaType("java.io.Serializable");
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
        uptDOType = new FullyQualifiedJavaType(pojoUrl+"."+tableName + "DelDO");
        uptBatDOType = new FullyQualifiedJavaType(pojoUrl+"."+tableName + "BatDelDO");
        pageDOType = new FullyQualifiedJavaType(pojoUrl+"."+tableName + "PageQueryDO");
        pageRTDOType = new FullyQualifiedJavaType("PageData<" +tableName+ ">");

        Interface interface1 = new Interface(interfaceType);
        TopLevelClass topLevelClass = new TopLevelClass(serviceType);
        TopLevelClass topLevelClass2 = new TopLevelClass(interfaceType);

        addJavaFileComment(topLevelClass2);
        addJavaFileComment(topLevelClass);
        addClassComment(topLevelClass2, introspectedTable);
        addClassComment(topLevelClass, introspectedTable);


        //导入操作类
        TopLevelClass uptDOTypeClass = new TopLevelClass(uptDOType);
        TopLevelClass uptBatDOTypeClass = new TopLevelClass(uptBatDOType);
        TopLevelClass pageDOTypeClss = new TopLevelClass(pageDOType);
        addDO(uptDOTypeClass, introspectedTable, tableName, files);
        addDO(uptBatDOTypeClass, introspectedTable, tableName, files);
        addPageDO(pageDOTypeClss, introspectedTable, tableName, files);

        // 导入必须的类
        addImport(interface1, topLevelClass);
        // 接口
        addService(interface1, introspectedTable, tableName, files);
        // 实现类
        addServiceImpl(topLevelClass, introspectedTable, tableName, files);

        return files;
    }

    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine("* xnh.com Inc.");
        compilationUnit.addFileCommentLine("* Copyright (c) 2017-2018 All Rights Reserved.");
        compilationUnit.addFileCommentLine("*/");
    }

    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        String username = System.getProperty("user.name");
        String dateStr = "";
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour <= 12) {
            dateStr = dateFormat1.format(new Date());
        } else {
            dateStr = dateFormat2.format(new Date());
        }
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getRemarks() + "服务实现方");
        sb.append("\n");
        sb.append(" * ");
        sb.append("\n");
        sb.append(" * ");
        sb.append("@author ").append(username).append("\n");
        sb.append(" * ").append("@version ").append(String.format("$: %s.java, v 0.1 %s %s Exp $ ", innerClass.getType().getShortName(), dateStr, username));
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
        addJavaFileComment(interface1);

        Method method = getOtherboolean("deleteById", introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("batchDeleteById", introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = add(introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = queryById(introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = pageQuery(introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("updateByParams", introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        method = getOtherboolean("updateById", introspectedTable, tableName,false);
        method.removeAllBodyLines();
        interface1.addMethod(method);

        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getJavaFormatter());

        files.add(file);
    }

    protected void addDO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.addSuperInterface(serializableType);
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addImportedType("java.io.Serializable");
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");
        addDOComment(topLevelClass,introspectedTable);
        addJavaFileComment(topLevelClass);
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    protected void addPageDO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("cn.luban.commons.ro.PageQuery"));
        topLevelClass.addImportedType("cn.luban.commons.ro.PageQuery");
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");
        addDOComment(topLevelClass,introspectedTable);
        addJavaFileComment(topLevelClass);

        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    public void addDOComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        String username = System.getProperty("user.name");
        String dateStr = "";
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour <= 12) {
            dateStr = dateFormat1.format(new Date());
        } else {
            dateStr = dateFormat2.format(new Date());
        }
        StringBuilder sb = new StringBuilder();
        innerClass.addJavaDocLine("/**");
        sb.append(" * ");
        sb.append(introspectedTable.getRemarks() + "操作DO");
        sb.append("\n");
        sb.append(" * ");
        sb.append("\n");
        sb.append(" * ");
        sb.append("@author ").append(username).append("\n");
        sb.append(" * ").append("@version ").append(String.format("$: %s.java, v 0.1 %s %s Exp $ ", innerClass.getType().getShortName(), dateStr, username));
        innerClass.addJavaDocLine(sb.toString());
        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
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

        addLogger(topLevelClass);
        // add import dao
        addField(topLevelClass, tableName);
        // add method
        topLevelClass.addMethod(queryById(introspectedTable, tableName,true));
        topLevelClass.addMethod(add(introspectedTable, tableName,true));
        topLevelClass.addMethod(pageQuery(introspectedTable, tableName,true));
        topLevelClass.addMethod(getOtherboolean("deleteById", introspectedTable, tableName,true));
        topLevelClass.addMethod(getOtherboolean("batchDeleteById", introspectedTable, tableName,true));
        topLevelClass.addMethod(getOtherboolean("updateByParams", introspectedTable, tableName,true));
        topLevelClass.addMethod(getOtherboolean("updateById", introspectedTable, tableName,true));

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
    protected Method queryById(IntrospectedTable introspectedTable, String tableName,boolean f) {
        Method method = new Method();
        method.setName("queryById");
        method.setReturnType(pojoType);
        if(f)
            method.addAnnotation("@Override");
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

    /** 分页 **/
    protected Method pageQuery(IntrospectedTable introspectedTable, String tableName,boolean f) {
        Method method = new Method();
        method.setName("pageQuery");
        method.setReturnType(pageRTDOType);
        if(f)
            method.addAnnotation("@Override");
        method.addParameter(new Parameter(pageDOType, "pageDO"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("PageHelper.startPage(pageDO.getPageNo(), pageDO.getPageSize());");
        sb.append("\n");
        sb.append("\t\tPage<" + pojoType.getShortName() + "> page = this." + getDaoShort() + ".queryPage(pageDO);");
        sb.append("\n");
        sb.append("\t\tPageData<" + pojoType.getShortName() + "> pageData = ObjectUtils.copy(pageDO, PageData.class);");
        sb.append("\n");
        sb.append("\t\tpageData.setResultList(page.getResult());");
        sb.append("\n");
        sb.append("\t\tpageData.setTotalPage(page.getPages());");
        sb.append("\n");
        sb.append("\t\tpageData.setTotalSize((int) page.getTotal());");
        sb.append("\n");
        sb.append("\t\treturn pageData;");
        method.addBodyLine(sb.toString());
        return method;
    }

    private static FullyQualifiedJavaType longInstance = new FullyQualifiedJavaType("java.lang.Long");

    protected Method add(IntrospectedTable introspectedTable, String tableName,boolean f) {
        Method method = new Method();
        method.setName("add");
        if(f)
            method.addAnnotation("@Override");
        method.setReturnType(longInstance);
        method.addParameter(new Parameter(pojoType,toLowerCase(pojoType.getShortName())));

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("boolean flag = this.");
        sb.append(getDaoShort());
        sb.append("add");
        sb.append("(");
        sb.append(toLowerCase(pojoType.getShortName()));
        sb.append(") > 0;");
        sb.append("\n");
        sb.append("\t\tif (flag) {");
        sb.append("\n");
        sb.append("\t\t\t").append("return "+toLowerCase(pojoType.getShortName())+".getId();");
        sb.append("\n");
        sb.append("\t\t}");
        sb.append("\n");
        sb.append("\t\treturn 0L;");
        method.addBodyLine(sb.toString());
        return method;
    }


    /**
     * add method
     */
    protected Method getOtherboolean(String methodName, IntrospectedTable introspectedTable, String tableName,boolean f) {
        Method method = new Method();
        method.setName(methodName);
        if(f)
            method.addAnnotation("@Override");
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        if(methodName.equals("updateById")||methodName.equals("updateByParams")){
            method.addParameter(new Parameter(pojoType,toLowerCase(pojoType.getShortName())));
        }else if(methodName.equals("queryById")){
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
            }
        }else if(methodName.equals("deleteById")){
            method.addParameter(new Parameter(uptDOType, toLowerCase(uptDOType.getShortName())));
        }else if(methodName.equals("batchDeleteById")){
            method.addParameter(new Parameter(uptBatDOType, toLowerCase(uptBatDOType.getShortName())));
        }

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("boolean flag = this.");
        sb.append(getDaoShort());
        sb.append(methodName);
        sb.append("(");
        if(methodName.equals("updateById")||methodName.equals("updateByParams")){
            sb.append(toLowerCase(pojoType.getShortName()));
        }else if(methodName.equals("queryById")){
            sb.append("id");
        }else if(methodName.equals("deleteById")){
            sb.append(toLowerCase(uptDOType.getShortName()));
        }else if(methodName.equals("batchDeleteById")){
            sb.append(toLowerCase(uptBatDOType.getShortName()));
        }

        sb.append(") > 0;");
        sb.append("\n");
        sb.append("\t\treturn flag;");
        method.addBodyLine(sb.toString());
        return method;
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
        interfaces.addImportedType(uptDOType);
        interfaces.addImportedType(pageDOType);
        interfaces.addImportedType(uptBatDOType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.ro.PageData"));

        //topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType("com.github.pagehelper.Page");
        topLevelClass.addImportedType("com.github.pagehelper.PageHelper");
        topLevelClass.addImportedType("cn.luban.commons.ro.PageData");
        topLevelClass.addImportedType("cn.luban.commons.object.ObjectUtils");
        topLevelClass.addImportedType(slf4jLoggerFactory);
        topLevelClass.addImportedType(service);
        topLevelClass.addImportedType(autowired);
        topLevelClass.addImportedType(uptDOType);
        topLevelClass.addImportedType(pageDOType);
        topLevelClass.addImportedType(uptBatDOType);
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

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
