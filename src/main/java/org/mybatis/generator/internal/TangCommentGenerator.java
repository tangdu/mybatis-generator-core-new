package org.mybatis.generator.internal;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.PropertyRegistry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * 添加MySQL注释生成器
 *
 * @author tangdu
 * @version $: TangCommentGenerator.java, v 0.1 2018年01月10日 下午4:38 tangdu Exp $
 */
public class TangCommentGenerator implements CommentGenerator {
    private Properties properties;

    private boolean suppressDate;

    private SimpleDateFormat dateFormat1=new SimpleDateFormat("yyyy年MM月dd日 上午HH:mm");
    private SimpleDateFormat dateFormat2=new SimpleDateFormat("yyyy年MM月dd日 下午HH:mm");

    public TangCommentGenerator() {
        super();
        properties = new Properties();
        suppressDate = false;
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/**");
        compilationUnit.addFileCommentLine("* xnh.com Inc.");
        compilationUnit.addFileCommentLine("* Copyright (c) 2017-2018 All Rights Reserved.");
        compilationUnit.addFileCommentLine("*/");
    }

    @Override
    public void addComment(XmlElement xmlElement) {
    }

    @Override
    public void addRootComment(XmlElement rootElement) {
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        suppressDate = isTrue(properties
                .getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));
    }


    @Override
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
        sb.append(introspectedTable.getRemarks());
        sb.append("(").append(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()).append(")");
        sb.append("\n");
        sb.append(" * ");
        sb.append("\n");
        sb.append(" * ");
        sb.append("@author ").append(username).append("\n");
        sb.append(" * ").append("@version ")
                .append(String.format("$: %s.java, v 0.1 %s %s Exp $ ",
                        introspectedTable.getFullyQualifiedTable().getDomainObjectName(),dateStr,username));
        innerClass.addJavaDocLine(sb.toString());
        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
    }

    @Override
    public void addClassComment(InnerClass innerClass,
                                IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        addClassComment(innerClass,introspectedTable);
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass,
                                     IntrospectedTable introspectedTable) {
        addClassComment(topLevelClass,introspectedTable);
    }

    @Override
    public void addEnumComment(InnerEnum innerEnum,
                               IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("/** ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        sb.append(" **/");
        innerEnum.addJavaDocLine(sb.toString());
    }

    @Override
    public void addFieldComment(Field field,
                                IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("/**");
        sb.append(introspectedColumn.getRemarks());
        sb.append("**/");
        field.addJavaDocLine(sb.toString());
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("/**");
        sb.append(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        sb.append("**/");
        field.addJavaDocLine(sb.toString());
    }

    @Override
    public void addGeneralMethodComment(Method method,
                                        IntrospectedTable introspectedTable) {
    }

    @Override
    public void addGetterComment(Method method,
                                 IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {

    }

    @Override
    public void addSetterComment(Method method,
                                 IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {

    }
}
