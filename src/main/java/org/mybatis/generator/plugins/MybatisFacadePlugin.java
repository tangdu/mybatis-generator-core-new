package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaFacadeGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * generate service class
 *
 * @author tangdu
 * @date 2016-03-17 10:34:45
 */
public class MybatisFacadePlugin extends PluginAdapter {

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType facadeType;

    private FullyQualifiedJavaType daoType;
    private FullyQualifiedJavaType facadeInterfaceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType listType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType service;
    private FullyQualifiedJavaType returnType;
    private FullyQualifiedJavaType statecodeType;

    private FullyQualifiedJavaType uptDOType;
    private FullyQualifiedJavaType uptBatDOType;
    private FullyQualifiedJavaType pageDOType;
    private FullyQualifiedJavaType pageRTDOType;

    private String servicePack;
    private String serviceImplPack;
    private String facadePack;
    private String facadeImplPack;
    private String stateCodePackage;
    private String project;
    private String pojoUrl;
    private SimpleDateFormat       dateFormat1      = new SimpleDateFormat("yyyy年MM月dd日 上午HH:mm");
    private SimpleDateFormat       dateFormat2      = new SimpleDateFormat("yyyy年MM月dd日 下午HH:mm");
    private FullyQualifiedJavaType serializableType = new FullyQualifiedJavaType("java.io.Serializable");
    private FullyQualifiedJavaType resultType       = new FullyQualifiedJavaType("cn.luban.commons.result.Result");
    /**
     * 所有的方法
     */
    private List<Method> methods;

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        JavaFacadeGeneratorConfiguration javaFacadeGeneratorConfiguration = introspectedTable.getContext().getJavaFacadeGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getContext().getJavaServiceGeneratorConfiguration();
        this.servicePack = javaServiceGeneratorConfiguration.getTargetPackage();
        this.facadePack = javaFacadeGeneratorConfiguration.getTargetPackage();
        this.serviceImplPack = javaServiceGeneratorConfiguration.getImplementationPackage();
        this.facadeImplPack = javaFacadeGeneratorConfiguration.getImplementationPackage();
        this.project = javaServiceGeneratorConfiguration.getTargetProject();
        this.stateCodePackage=javaFacadeGeneratorConfiguration.getStateCodePackage();

        this.pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
        service = new FullyQualifiedJavaType("org.springframework.stereotype.Component");
    }

    public MybatisFacadePlugin() {
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
        facadeInterfaceType = new FullyQualifiedJavaType(facadePack + "." + tableName + "Facade");
        daoType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        facadeType = new FullyQualifiedJavaType(facadeImplPack + "." + tableName + "FacadeImpl");
        serviceType = new FullyQualifiedJavaType(servicePack + "." + tableName + "Service");

        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);
        listType = new FullyQualifiedJavaType("java.util.List");
        uptDOType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "DelDO");
        uptBatDOType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "BatDelDO");
        pageDOType = new FullyQualifiedJavaType(pojoUrl + "." + tableName + "PageQueryDO");
        pageRTDOType = new FullyQualifiedJavaType("Page<" + tableName + ">");
        statecodeType=new FullyQualifiedJavaType(stateCodePackage+"."+pojoType+"StateCode");

        Interface facadeInterface = new Interface(facadeInterfaceType);
        Interface stateCodeInterface = new Interface(statecodeType);
        TopLevelClass topLevelFacadeClass = new TopLevelClass(facadeType);


        addJavaFileComment(facadeInterface);
        addJavaFileComment(stateCodeInterface);
        addJavaFileComment(topLevelFacadeClass);

        addClassComment(stateCodeInterface,statecodeType, introspectedTable,"状态码(从-200开始)");
        addClassComment(facadeInterface,facadeInterfaceType,introspectedTable,"服务消费者");
        addClassComment(topLevelFacadeClass,facadeType, introspectedTable,"服务生产者");

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
        compilationUnit.addFileCommentLine("* xnh.com Inc.");
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
        if(remarks.endsWith("表")){
            remarks=remarks.substring(0,remarks.length()-1);
        }
        if(remarks.endsWith("信息")){
            remarks=remarks.substring(0,remarks.length()-1);
        }

        Method method = null;
        method = queryById(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "根据ID查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "ID", remarks + "信息");

        method = pageQuery(introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "分页查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "分页结果");

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

        method = getOtherboolean(wrapperName("batchDelete%sById"), introspectedTable, tableName, false);
        method.removeAllBodyLines();
        interface1.addMethod(method);
        addMethodComment(method, "根据ID批量删除" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "删除对象", "成功或失败");

        GeneratedJavaFile file = new GeneratedJavaFile(interface1, project, context.getJavaFormatter());
        files.add(file);
    }

    protected void addStateCode(Interface inter, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        inter.setVisibility(JavaVisibility.PUBLIC);
        addStateCodeField(inter, introspectedTable);
        inter.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.result.StateCode"));

        GeneratedJavaFile file = new GeneratedJavaFile(inter, project, context.getJavaFormatter());
        files.add(file);
    }
    protected void addStateCodeField(Interface inter,IntrospectedTable introspectedTable) {
        Field field = new Field();;
        field.setName((pojoType.getShortName()).toUpperCase()+"_NOT_FOUND");
        field.setType(statecodeType); // type
        String desc=String.format("%s数据不存在",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase()+"_ADD_FAIL");
        field.setType(statecodeType); // type
        desc=String.format("%s添加失败",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase()+"_UPT_FAIL");
        field.setType(statecodeType); // type
        desc=String.format("%s更新失败",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase()+"_DEL_FAIL");
        field.setType(statecodeType); // type
        desc=String.format("%s删除失败",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase()+"_ID_NULL");
        field.setType(statecodeType); // type
        desc=String.format("%sID不可为空",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
        inter.addField(field);
        //
        field = new Field();
        field.setName((pojoType.getShortName()).toUpperCase()+"_QUERY_FAIL");
        field.setType(statecodeType); // type
        desc=String.format("%s查询失败",introspectedTable.getRemarks());
        field.setInitializationString("new "+statecodeType.getShortName()+"(-200,\""+desc+"\")");
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
        addMethodComment(method,"根据ID查询"+remarks+"信息",method.getParameters().get(0).getName(),remarks+"ID",remarks+"信息");
        topLevelClass.addMethod(method);

        method = pageQuery(introspectedTable, tableName, true);
        addMethodComment(method, "分页查询" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "信息", remarks + "分页");
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

        method = getOtherboolean(wrapperName("batchDelete%sById"), introspectedTable, tableName, true);
        addMethodComment(method, "根据ID批量删除" + remarks + "信息", method.getParameters().get(0).getName(), remarks + "删除对象", "成功或失败");
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
        return toLowerCase(String.format(name, pojoType.getShortName()));
    }

    /**
     * 添加方法
     */
    protected Method queryById(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("query%sById"));
        method.setReturnType(new FullyQualifiedJavaType("Result<" + pojoType.getShortName() + ">"));
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
        sb.append("\t\t\treturn Results.success(" + toLowerCase(pojoType.getShortName()) + ");");
        sb.append("\n");
        sb.append("\t\t}");
        sb.append("\n");
        sb.append("\t\treturn Results.failed(UserStateCode.USER_NOT_FOUND);");
        method.addBodyLine(sb.toString());
        return method;
    }

    /** 分页 **/
    protected Method pageQuery(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("page%sQuery"));
        method.setReturnType(new FullyQualifiedJavaType("Result<PageData<" + pojoType.getShortName() + ">>"));
        if (f) method.addAnnotation("@Override");
        method.addParameter(new Parameter(pageDOType, toLowerCase(pageDOType.getShortName())));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("PageData<" + pojoType.getShortName() + "> " + wrapperName("%sPageData") + " = this." + getDaoShort() + "pageQuery(" + toLowerCase(pageDOType.getShortName()) + ");");
        sb.append("\n");
        sb.append("\t\treturn Results.success(" + wrapperName("%sPageData") + ");");
        method.addBodyLine(sb.toString());
        return method;
    }

    private static FullyQualifiedJavaType longInstance = new FullyQualifiedJavaType("Result<Long>");

    protected Method add(IntrospectedTable introspectedTable, String tableName, boolean f) {
        Method method = new Method();
        method.setName(wrapperName("add%s"));
        if (f) method.addAnnotation("@Override");
        method.setReturnType(longInstance);
        String name = toLowerCase(pojoType.getShortName());
        method.addParameter(new Parameter(pojoType, name));

        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("ValidateTools.validate(" + name + ");");
        sb.append("\n");
        sb.append("\t\tLong result=this."+getDaoShort()+"add("+name+");");
        sb.append("\n");
        sb.append("\t\tif(result==null || result<1) {");
        sb.append("\n");
        sb.append("\t\t\treturn Results.failed(UserStateCode.USER_ADD_ERROR);");
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
            method.addParameter(new Parameter(pojoType, toLowerCase(pojoType.getShortName())));
        } else if (methodName.equals(wrapperName("delete%sById"))) {
            method.addParameter(new Parameter(uptDOType, toLowerCase(uptDOType.getShortName())));
        } else if (methodName.equals(wrapperName("batchDelete%sById"))) {
            method.addParameter(new Parameter(uptBatDOType, toLowerCase(uptBatDOType.getShortName())));
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("ValidateTools.validate(" + method.getParameters().get(0).getName() + ");");
        sb.append("\n");
        sb.append("\t\treturn Results.success(this." + getDaoShort() + "");
        String serviceMethodName = methodName.replaceAll(pojoType.getShortName(), "");
        sb.append(serviceMethodName);
        sb.append("(");
        if (methodName.equals(wrapperName("update%sById")) || methodName.equals(wrapperName("update%sByParams"))) {
            sb.append(toLowerCase(pojoType.getShortName()));
        } else if (methodName.equals(wrapperName("delete%sById"))) {
            sb.append(toLowerCase(uptDOType.getShortName()));
        } else if (methodName.equals(wrapperName("batchDelete%sById"))) {
            sb.append(toLowerCase(uptBatDOType.getShortName()));
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
        interfaces.addImportedType(pojoType);
        interfaces.addImportedType(uptDOType);
        interfaces.addImportedType(pageDOType);
        interfaces.addImportedType(uptBatDOType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.result.Result"));
        interfaces.addImportedType(resultType);
        interfaces.addImportedType(new FullyQualifiedJavaType("cn.luban.commons.ro.PageData"));


        topLevelClass.addImportedType(facadeInterfaceType);
        topLevelClass.addImportedType(pojoType);
        topLevelClass.addImportedType(resultType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType("cn.luban.commons.result.Results");
        topLevelClass.addImportedType("cn.luban.commons.result.Result");
        topLevelClass.addImportedType("cn.luban.commons.validate.ValidateTools");
        topLevelClass.addImportedType("cn.luban.commons.ro.PageData");
        topLevelClass.addImportedType("cn.xnh.datacenter.user.facade.statecode.UserStateCode");
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
        return toLowerCase(serviceType.getShortName()) + ".";
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
