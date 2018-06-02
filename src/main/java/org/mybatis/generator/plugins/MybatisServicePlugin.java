package org.mybatis.generator.plugins;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;

/**
 * generate service class
 *
 * @author tangdu
 * @date 2016-03-17 10:34:45
 */
public class MybatisServicePlugin extends PluginAdapter {

    private static FullyQualifiedJavaType longInstance     = new FullyQualifiedJavaType("java.lang.Long");
    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType interfaceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType listType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private FullyQualifiedJavaType pageDOType;
    private String servicePack;
    private String serviceImplPack;
    private String project;
    private String pojoUrl;
    private        FullyQualifiedJavaType serializableType = new FullyQualifiedJavaType("java.io.Serializable");
    /**
     * 所有的方法
     */
    private List<Method> methods;

    public MybatisServicePlugin() {
        super();
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
        methods = new ArrayList<Method>();
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        if(introspectedTable.getContext().getJavaServiceGeneratorConfiguration()==null){
            return;
        }
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getContext().getJavaServiceGeneratorConfiguration();
        this.servicePack = javaServiceGeneratorConfiguration.getTargetPackage();
        this.serviceImplPack = javaServiceGeneratorConfiguration.getImplementationPackage();
        this.project = javaServiceGeneratorConfiguration.getTargetProject();
        this.pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
        service = new FullyQualifiedJavaType("org.springframework.stereotype.Service");
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
        String entityName = introspectedTable.getDomainReplaceObjectName();
        interfaceType = new FullyQualifiedJavaType(servicePack + "." + entityName + "Service");
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + entityName + "ServiceImpl");
        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
        listType = new FullyQualifiedJavaType("java.util.List");
        pageDOType = new FullyQualifiedJavaType(pojoUrl + "." + entityName + "PageQueryDO");

        Interface serviceInterface = new Interface(interfaceType);
        TopLevelClass serviceClass = new TopLevelClass(serviceType);

        addJavaFileComment(serviceInterface);
        addJavaFileComment(serviceClass);

        addClassComment(serviceInterface, interfaceType, introspectedTable, "服务接口");
        addClassComment(serviceClass, serviceType, introspectedTable, "服务实现");


        //导入操作类
        TopLevelClass pageDOTypeClss = new TopLevelClass(pageDOType);
        addPageDO(pageDOTypeClss, introspectedTable, tableName, files);

        // 导入必须的类
        addImport(serviceInterface, serviceClass);
        // 接口
        addService(serviceInterface, introspectedTable, tableName, files);
        // 实现类
        addServiceImpl(serviceClass, introspectedTable, tableName, files);

        return files;
    }

    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine("* tangdu Inc.");
        compilationUnit.addFileCommentLine("* Copyright (c) 2017-2018 All Rights Reserved.");
        compilationUnit.addFileCommentLine("*/");
    }


    /**
     * add interface
     *
     * @param tableName
     * @param files
     */
    protected void addService(Interface interface1, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        interface1.setVisibility(JavaVisibility.PUBLIC);
        String remarks = introspectedTable.getRemarks();
        if (remarks.endsWith("表")) {
            remarks = remarks.substring(0, remarks.length() - 1);
        }
        if (remarks.endsWith("信息")) {
            remarks = remarks.substring(0, remarks.length() - 1);
        }
        Method method = null;
        method = queryById(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "根据ID查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "ID", remarks + "信息");

        method = pageQuery(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "分页查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "查询对象", remarks + "分页结果");

        method = queryAll(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "查询所有" + remarks + "信息", "", "", remarks + "列表");

        method = add(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "添加" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "记录ID");

        method = getOtherboolean("updateById", introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");

        method = getOtherboolean("updateByParams", introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "选择更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");

        method = getOtherboolean("deleteById", introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "根据ID删除" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "删除对象", "成功或失败");


        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getJavaFormatter());
        files.add(file);
    }


    protected void addPageDO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.setSuperClass(new FullyQualifiedJavaType("cn.luban.commons.model.PageQuery"));
        topLevelClass.addImportedType("cn.luban.commons.model.PageQuery");
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");
        addClassComment(topLevelClass, topLevelClass.getType(), introspectedTable, "分页DO");
        addJavaFileComment(topLevelClass);
        List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
        Plugin plugins = context.getPlugins();

        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, Plugin.ModelClassType.BASE_RECORD)) {
                if (field.getName().equals("isDelete")) {
                    continue;
                }
                topLevelClass.addField(field);
                topLevelClass.addImportedType(field.getType());
            }
        }

        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
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

        addLogger(topLevelClass);
        // add import dao
        addField(topLevelClass, tableName);
        // add method

        String remarks = introspectedTable.getRemarks();
        Method method = queryById(introspectedTable, tableName, true);
        addMethodComment(method, "根据ID查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "ID", remarks + "信息");
        topLevelClass.addMethod(method);

        method = pageQuery(introspectedTable, tableName, true);
        addMethodComment(method, "分页查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "分页结果");
        topLevelClass.addMethod(method);

        method = queryAll(introspectedTable, tableName, true);
        addMethodComment(method, "查询所有" + remarks + "信息", "", "", remarks + "列表");
        topLevelClass.addMethod(method);


        method = add(introspectedTable, tableName, true);
        addMethodComment(method, "添加" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "记录ID");
        topLevelClass.addMethod(method);


        method = getOtherboolean(("updateById"), introspectedTable, tableName, true);
        addMethodComment(method, "更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");
        topLevelClass.addMethod(method);

        method = getOtherboolean(("updateByParams"), introspectedTable, tableName, true);
        addMethodComment(method, "选择更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");
        topLevelClass.addMethod(method);

        method = getOtherboolean(("deleteById"), introspectedTable, tableName, true);
        addMethodComment(method, "根据ID删除" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "删除对象", "成功或失败");
        topLevelClass.addMethod(method);

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
    protected Method queryById(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName("queryById");
        method.setReturnType(pojoType);
        if (f) method.addAnnotation("@Override");
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

    protected Method queryAll(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName("queryAll");
        if (f) method.addAnnotation("@Override");
        method.setReturnType(new FullyQualifiedJavaType("List<" + pojoType.getShortName() + ">"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this." + getDaoShort() + "queryAll();");
        method.addBodyLine(sb.toString());
        return method;
    }


    /** 分页 **/
    protected Method pageQuery(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName("pageQuery");
        method.setReturnType(new FullyQualifiedJavaType("PageData<" + pojoType.getShortName() + ">"));
        if (f) method.addAnnotation("@Override");
        method.addParameter(new Parameter(pageDOType, "pageDO"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("PageHelper.startPage(pageDO.getPageNo(), pageDO.getPageSize());");
        sb.append("\n");
        sb.append("\t\tPage<" + pojoType.getShortName() + "> page = this." + getDaoShort() + "queryPage(pageDO);");
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

    protected Method add(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName("add");
        if (f) method.addAnnotation("@Override");
        method.setReturnType(longInstance);
        method.addParameter(new Parameter(pojoType, toLowerCase(pojoType.getShortName())));

        String primaryKey="getId";
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            primaryKey="get"+toUpperCase(introspectedColumn.getJavaProperty());
        }

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
        sb.append("\t\t\t").append("return " + toLowerCase(pojoType.getShortName()) + "."+primaryKey+"();");
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
    protected Method getOtherboolean(String methodName, IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(methodName);
        if (f) method.addAnnotation("@Override");
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        if (methodName.equals("updateById") || methodName.equals("updateByParams")) {
            method.addParameter(new Parameter(pojoType, toLowerCase(pojoType.getShortName())));
        } else if (methodName.equals("queryById")) {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
            }
        } else if (methodName.equals("deleteById")) {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), toLowerCase(introspectedColumn.getJavaProperty())));
            }

        }

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("boolean flag = this.");
        sb.append(getDaoShort());
        sb.append(methodName);
        sb.append("(");
        if (methodName.equals("updateById") || methodName.equals("updateByParams")) {
            sb.append(toLowerCase(pojoType.getShortName()));
        } else if (methodName.equals("queryById")) {
            sb.append("id");
        } else if (methodName.equals("deleteById")) {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                sb.append(toLowerCase(introspectedColumn.getJavaProperty()));
            }
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
        topLevelClass.addImportedType(daoType);
        topLevelClass.addImportedType(interfaceType);
        topLevelClass.addImportedType(pojoType);
        interfaces.addImportedType(pageDOType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.model.PageData"));
        interfaces.addImportedType(listType);

        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType("com.github.pagehelper.Page");
        topLevelClass.addImportedType("com.github.pagehelper.PageHelper");
        topLevelClass.addImportedType("cn.luban.commons.model.PageData");
        topLevelClass.addImportedType("cn.luban.commons.utils.ObjectUtils");
        topLevelClass.addImportedType(slf4jLoggerFactory);
        topLevelClass.addImportedType(service);
        topLevelClass.addImportedType(autowired);
        topLevelClass.addImportedType(pageDOType);
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
