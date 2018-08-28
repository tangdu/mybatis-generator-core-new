package org.mybatis.generator.plugins;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaFacadeGeneratorConfiguration;
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
public class MybatisFacadePlugin extends PluginAdapter {

    private static FullyQualifiedJavaType longInstance = new FullyQualifiedJavaType("Result<Long>");
    String entityName = "";
    FullyQualifiedJavaType statecodeExtType = new FullyQualifiedJavaType("cn.luban.commons.result.StateCode");
    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType facadeType;
    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType facadeInterfaceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private FullyQualifiedJavaType statecodeType;
    private FullyQualifiedJavaType roType;
    private FullyQualifiedJavaType uptROType;
    private FullyQualifiedJavaType pageDOType;
    private FullyQualifiedJavaType pageROType;
    private String servicePack;
    private String serviceImplPack;
    private String facadePack;
    private String facadeImplPack;
    private String stateCodePackage;
    private String roPackage;
    private String project;
    private String pojoUrl;
    private FullyQualifiedJavaType resultType       = new FullyQualifiedJavaType("cn.luban.commons.result.Result");
    private FullyQualifiedJavaType listType         = new FullyQualifiedJavaType("java.util.List");
    private FullyQualifiedJavaType serializableType = new FullyQualifiedJavaType("java.io.Serializable");
    /**
     * 所有的方法
     */
    private List<Method> methods;

    public MybatisFacadePlugin() {
        super();
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
        methods = new ArrayList<Method>();
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        if(introspectedTable.getContext().getJavaServiceGeneratorConfiguration()==null
                || introspectedTable.getContext().getJavaFacadeGeneratorConfiguration()==null){
            return;
        }
        JavaFacadeGeneratorConfiguration javaFacadeGeneratorConfiguration = introspectedTable.getContext().getJavaFacadeGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getContext().getJavaServiceGeneratorConfiguration();
        this.servicePack = javaServiceGeneratorConfiguration.getTargetPackage();
        this.facadePack = javaFacadeGeneratorConfiguration.getTargetPackage();
        this.serviceImplPack = javaServiceGeneratorConfiguration.getImplementationPackage();
        this.facadeImplPack = javaFacadeGeneratorConfiguration.getImplementationPackage();
        this.project = javaServiceGeneratorConfiguration.getTargetProject();
        this.stateCodePackage = javaFacadeGeneratorConfiguration.getStateCodePackage();
        this.roPackage = javaFacadeGeneratorConfiguration.getRoPackage();

        this.pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
        service = new FullyQualifiedJavaType("org.springframework.stereotype.Component");
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
        entityName = introspectedTable.getDomainReplaceObjectName();

        facadeInterfaceType = new FullyQualifiedJavaType(facadePack + "." + entityName + "Facade");
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        facadeType = new FullyQualifiedJavaType(facadeImplPack + "." + entityName + "FacadeImpl");
        serviceType = new FullyQualifiedJavaType(servicePack + "." + entityName + "Service");

        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
        uptROType = new FullyQualifiedJavaType(roPackage + "." + entityName + "DelRO");
        roType = new FullyQualifiedJavaType(roPackage + "." + entityName + "RO");
        pageROType = new FullyQualifiedJavaType(roPackage + "." + entityName + "PageQueryRO");
        pageDOType = new FullyQualifiedJavaType(pojoUrl + "." + entityName + "PageQueryDO");
        statecodeType = new FullyQualifiedJavaType(stateCodePackage + "." + entityName + "StateCode");

        //导入操作类
        TopLevelClass pageDOTypeClss = new TopLevelClass(pageROType);
        TopLevelClass uptDOTypeClss = new TopLevelClass(uptROType);
        TopLevelClass roTypeClss = new TopLevelClass(roType);
        addPageRO(pageDOTypeClss, introspectedTable, tableName, files);
        addDelRO(uptDOTypeClss, introspectedTable, tableName, files);
        addPojoRO(roTypeClss, introspectedTable, tableName, files);


        //facade相关操作
        Interface facadeInterface = new Interface(facadeInterfaceType);
        Interface stateCodeInterface = new Interface(statecodeType);
        TopLevelClass topLevelFacadeClass = new TopLevelClass(facadeType);


        addJavaFileComment(facadeInterface);
        addJavaFileComment(stateCodeInterface);
        addJavaFileComment(topLevelFacadeClass);

        addClassComment(stateCodeInterface, statecodeType, introspectedTable, "状态码,从200开始(此code全局不可重复)");
        addClassComment(facadeInterface, facadeInterfaceType, introspectedTable, "服务消费者");
        addClassComment(topLevelFacadeClass, facadeType, introspectedTable, "服务生产者");

        // 导入必须的类
        addImport(facadeInterface, topLevelFacadeClass);

        // 状态码
        addStateCode(stateCodeInterface, introspectedTable, tableName, files);
        // 接口
        addService(facadeInterface, introspectedTable, tableName, files);
        // 实现类
        addServiceImpl(topLevelFacadeClass, introspectedTable, tableName, files);

        return files;
    }

    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine("* www.luckincoffee.com Inc.");
        compilationUnit.addFileCommentLine("* Copyright (c) 2018 All Rights Reserved.");
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
        addMethodComment(method, "添加" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "信息");

        method = getOtherboolean(wrapperName("update%sById"), introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");

        method = getOtherboolean(wrapperName("update%sByParams"), introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "选择更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");

        method = getOtherboolean(wrapperName("delete%sById"), introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "根据ID删除" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "删除对象", "成功或失败");

        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getJavaFormatter());
        files.add(file);
    }

    protected void addStateCode(Interface inter, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        inter.setVisibility(JavaVisibility.PUBLIC);
        addStateCodeField(inter, introspectedTable);
        inter.addImportedType(statecodeExtType);
        GeneratedJavaFile file = new GeneratedJavaFile(inter, project, context.getJavaFormatter());
        files.add(file);
    }

    protected void addStateCodeField(Interface inter, IntrospectedTable introspectedTable) {
        Field field = new Field();
        ;
        field.setName((pojoType.getShortName()).toUpperCase() + "_NOT_FOUND");
        field.setType(statecodeExtType); // type
        String desc = String.format("%s数据不存在", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase() + "_ADD_FAIL");
        field.setType(statecodeExtType); // type
        desc = String.format("%s添加失败", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase() + "_UPT_FAIL");
        field.setType(statecodeExtType); // type
        desc = String.format("%s更新失败", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase() + "_DEL_FAIL");
        field.setType(statecodeExtType); // type
        desc = String.format("%s删除失败", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase() + "_ID_NULL");
        field.setType(statecodeExtType); // type
        desc = String.format("%sID不可为空", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase() + "_QUERY_FAIL");
        field.setType(statecodeExtType); // type
        desc = String.format("%s查询失败", introspectedTable.getRemarks());
        field.setInitializationString("new " + statecodeExtType.getShortName() + "(-200,\"" + desc + "\")");
        inter.addField(field);
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
        topLevelClass.addSuperInterface(facadeInterfaceType);

        topLevelClass.addAnnotation("@Component(\"" + toLowerCase(facadeInterfaceType.getShortName()) + "\")");
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
        addMethodComment(method, "分页查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "查询对象", remarks + "分页");
        topLevelClass.addMethod(method);

        method = queryAll(introspectedTable, tableName, true);
        addMethodComment(method, "查询所有" + remarks + "信息", "", "", remarks + "列表");
        topLevelClass.addMethod(method);


        method = add(introspectedTable, tableName, true);
        addMethodComment(method, "添加" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "信息");
        topLevelClass.addMethod(method);


        method = getOtherboolean(wrapperName("update%sById"), introspectedTable, tableName, true);
        addMethodComment(method, "更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");
        topLevelClass.addMethod(method);

        method = getOtherboolean(wrapperName("update%sByParams"), introspectedTable, tableName, true);
        addMethodComment(method, "选择更新" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "更新信息", "成功或失败");
        topLevelClass.addMethod(method);

        method = getOtherboolean(wrapperName("delete%sById"), introspectedTable, tableName, true);
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
        field.setName(toLowerCase(serviceType.getShortName())); // set var name
        topLevelClass.addImportedType(serviceType);
        field.setType(serviceType); // type
        field.setVisibility(JavaVisibility.PRIVATE);
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);
    }

    private String wrapperName(String name) {
        return toLowerCase(String.format(name, entityName));
    }

    /**
     * 添加方法
     */
    protected Method queryById(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("query%sById"));
        method.setReturnType(new FullyQualifiedJavaType("Result<" + roType.getShortName() + ">"));
        if (f) method.addAnnotation("@Override");
        IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
        String name = wrapperName("%s" + toUpperCase(introspectedColumn.getJavaProperty()));
        method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), name));

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("ValidateTools.checkNull(" + name + ", \"ID不能为空\");");
        sb.append("\n");
        sb.append("\t\t" + pojoType.getShortName() + " " + toLowerCase(pojoType.getShortName()) + " = this." + getDaoShort() + "queryById(" + name + ");");
        sb.append("\n");
        sb.append("\t\tif (" + toLowerCase(pojoType.getShortName()) + " != null) {");
        sb.append("\n");
        sb.append("\t\t\treturn Results.success(ObjectUtils.copy(" + toLowerCase(pojoType.getShortName()) + "," + roType.getShortName() + ".class));");
        sb.append("\n");
        sb.append("\t\t}");
        sb.append("\n");
        sb.append("\t\treturn Results.failed(" + statecodeType.getShortName() + "." + (pojoType.getShortName()).toUpperCase() + "_NOT_FOUND);");
        method.addBodyLine(sb.toString());
        return method;
    }

    protected Method queryAll(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName("queryAll");
        method.setReturnType(new FullyQualifiedJavaType("Result<List<" + roType.getShortName() + ">>"));
        if (f) method.addAnnotation("@Override");
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("List<"+pojoType.getShortName()+"> list = this." + getDaoShort() + "queryAll();");
        sb.append("\n");
        sb.append("\t\treturn Results.success(ObjectUtils.copyList(list,"+roType.getShortName()+".class));");
        method.addBodyLine(sb.toString());
        return method;
    }

    /** 分页 **/
    protected Method pageQuery(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("page%sQuery"));
        method.setReturnType(new FullyQualifiedJavaType("Result<PageData<" + roType.getShortName() + ">>"));
        if (f) method.addAnnotation("@Override");
        method.addParameter(new Parameter(pageROType, toLowerCase(pageROType.getShortName())));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();

        sb.append("ValidateTools.validate(" + method.getParameters().get(0).getName() + ");");
        sb.append("\n");

        //先将查询对象转化
        sb.append("\t\t" + pageDOType.getShortName()).append(" ").append(toLowerCase(pageDOType.getShortName())).append(" = ");
        sb.append("ObjectUtils.copy(").append(toLowerCase(pageROType.getShortName())).append(",");
        sb.append(pageDOType.getShortName()).append(".class);");
        sb.append("\n");

        sb.append("\t\tPageData<" + pojoType.getShortName() + "> " + wrapperName("%sPageData") + " = this." + getDaoShort() + "pageQuery(" + toLowerCase(pageDOType.getShortName()) + ");");
        sb.append("\n");
        //转化结果
        sb.append("\t\tPageData<" + roType.getShortName() + "> pageData = ObjectUtils.copy(" + wrapperName("%sPageData") + ", PageData.class);");
        sb.append("\n");
        sb.append("\t\tpageData.setResultList(ObjectUtils.copyList(" + wrapperName("%sPageData") + ".getResultList(), " + roType.getShortName() + ".class));");
        sb.append("\n");
        sb.append("\t\treturn Results.success(pageData);");
        method.addBodyLine(sb.toString());
        return method;
    }

    protected Method add(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("add%s"));
        if (f) method.addAnnotation("@Override");
        method.setReturnType(longInstance);
        method.addParameter(new Parameter(roType, toLowerCase(roType.getShortName())));

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        String name = toLowerCase(pojoType.getShortName());
        sb.append("ValidateTools.validate(" + toLowerCase(roType.getShortName()) + ");");
        sb.append("\n");
        sb.append("\t\t").append(pojoType.getShortName()).append(" ").append(toLowerCase(pojoType.getShortName())).append(" = ");
        sb.append("ObjectUtils.copy(").append(toLowerCase(roType.getShortName())).append(",");
        sb.append(pojoType.getShortName()).append(".class);");
        sb.append("\n");
        sb.append("\t\tLong result=this." + getDaoShort() + "add(" + name + ");");
        sb.append("\n");
        sb.append("\t\tif(result==null || result<1) {");
        sb.append("\n");
        sb.append("\t\t\treturn Results.failed(" + statecodeType.getShortName() + "." + (pojoType.getShortName()).toUpperCase() + "_NOT_FOUND);");
        sb.append("\n");
        sb.append("\t\t}");
        sb.append("\n");
        sb.append("\t\treturn Results.success(result);");
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
        method.setReturnType(new FullyQualifiedJavaType("Result<Boolean>"));
        if (methodName.equals(wrapperName("update%sById")) || methodName.equals(wrapperName("update%sByParams"))) {
            method.addParameter(new Parameter(roType, toLowerCase(roType.getShortName())));
        } else if (methodName.equals(wrapperName("delete%sById"))) {
            method.addParameter(new Parameter(uptROType, toLowerCase(uptROType.getShortName())));
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("ValidateTools.validate(" + method.getParameters().get(0).getName() + ");");
        sb.append("\n");
        sb.append("\t\t").append(pojoType.getShortName()).append(" ").append(toLowerCase(pojoType.getShortName())).append(" = ");
        sb.append("ObjectUtils.copy(").append(toLowerCase(method.getParameters().get(0).getType().getShortName())).append(",");
        sb.append(pojoType.getShortName()).append(".class);");
        sb.append("\n");

        sb.append("\t\treturn Results.success(this." + getDaoShort() + "");
        String serviceMethodName = methodName.replaceAll(introspectedTable.getDomainReplaceObjectName(), "");
        sb.append(serviceMethodName);
        sb.append("(");

        if (methodName.equals(wrapperName("update%sById")) || methodName.equals(wrapperName("update%sByParams"))) {
            sb.append(toLowerCase(pojoType.getShortName()));
        } else if (methodName.equals(wrapperName("delete%sById"))) {
            sb.append(toLowerCase(pojoType.getShortName()));
        }
        sb.append(")");
        sb.append(");");

        method.addBodyLine(sb.toString());
        return method;
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
        interfaces.addImportedType(listType);
        interfaces.addImportedType(uptROType);
        //interfaces.addImportedType(pageDOType);
        interfaces.addImportedType(pageROType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.result.Result"));
        interfaces.addImportedType(roType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.model.PageData"));

        topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(facadeInterfaceType);
        topLevelClass.addImportedType(pojoType);
        topLevelClass.addImportedType(resultType);
        topLevelClass.addImportedType(roType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType("cn.luban.commons.result.Results");
        topLevelClass.addImportedType("cn.luban.commons.result.Result");
        topLevelClass.addImportedType("cn.luban.commons.validate.ValidateTools");
        topLevelClass.addImportedType("cn.luban.commons.model.PageData");
        topLevelClass.addImportedType("cn.luban.commons.utils.ObjectUtils");
        topLevelClass.addImportedType(statecodeType);
        topLevelClass.addImportedType(slf4jLoggerFactory);
        topLevelClass.addImportedType(service);
        topLevelClass.addImportedType(autowired);
        topLevelClass.addImportedType(uptROType);
        topLevelClass.addImportedType(pageDOType);
        topLevelClass.addImportedType(pageROType);
    }

    protected void addDelRO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.addSuperInterface(serializableType);
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addImportedType("java.io.Serializable");
        topLevelClass.addImportedType("cn.luban.commons.validate.Validate");
        topLevelClass.addAnnotation("@Getter");
        topLevelClass.addAnnotation("@Setter");
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setName("id");
        field.addAnnotation("@Validate(required = true)");
        field.setType(new FullyQualifiedJavaType("java.lang.Long"));
        field.addJavaDocLine("/** " + introspectedTable.getRemarks() + "Id **/");
        topLevelClass.addField(field);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setName("updatePerson");
        field.addAnnotation("@Validate(isNotBlank = true)");
        field.setType(new FullyQualifiedJavaType("java.lang.String"));
        field.addJavaDocLine("/** 变更人 **/");
        topLevelClass.addField(field);

        Method toString=new Method("toString");
        toString.addBodyLine("return ToStringBuilder.reflectionToString (this, ToStringStyle.JSON_STYLE);");
        toString.setReturnType(new FullyQualifiedJavaType("java.lang.String"));
        toString.addAnnotation("@Override");
        toString.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.commons.lang3.builder.ToStringBuilder"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.commons.lang3.builder.ToStringStyle"));
        topLevelClass.addMethod(toString);

        addClassComment(topLevelClass, topLevelClass.getType(), introspectedTable, "更新RO");
        addJavaFileComment(topLevelClass);
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    protected void addPojoRO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addAnnotation("@Setter");
        topLevelClass.addAnnotation("@Getter");
        topLevelClass.addImportedType(serializableType);
        topLevelClass.addSuperInterface(serializableType);
        addClassComment(topLevelClass, topLevelClass.getType(), introspectedTable, "模型RO");
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

        Method toString=new Method("toString");
        toString.addBodyLine("return ToStringBuilder.reflectionToString (this, ToStringStyle.JSON_STYLE);");
        toString.setReturnType(new FullyQualifiedJavaType("java.lang.String"));
        toString.addAnnotation("@Override");
        toString.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.commons.lang3.builder.ToStringBuilder"));
        topLevelClass.addImportedType(new FullyQualifiedJavaType("org.apache.commons.lang3.builder.ToStringStyle"));
        topLevelClass.addMethod(toString);

        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }


    protected void addPageRO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        // set implements interface
        topLevelClass.setSuperClass(new FullyQualifiedJavaType("cn.luban.commons.model.PageQuery"));
        topLevelClass.addImportedType("cn.luban.commons.model.PageQuery");
        topLevelClass.addImportedType("lombok.Getter");
        topLevelClass.addImportedType("lombok.Setter");
        topLevelClass.addAnnotation("@Getter");
        topLevelClass.addAnnotation("@Setter");
        addClassComment(topLevelClass, topLevelClass.getType(), introspectedTable, "分页RO");
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
        return toLowerCase(serviceType.getShortName()) + ".";
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
