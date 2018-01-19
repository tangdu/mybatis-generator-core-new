/**
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

/**
 *
 * @author Jeff Butler
 *
 */
public class SimpleSelectAllElementGenerator extends AbstractXmlElementGenerator {

    public SimpleSelectAllElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        String pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();
        String replaceObjectName = this.introspectedTable.getDomainReplaceObjectName();
        FullyQualifiedJavaType pageDOType = new FullyQualifiedJavaType(pojoUrl + "." + replaceObjectName + "PageQueryDO");


        answer.addAttribute(new Attribute("id", introspectedTable.getSelectAllStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultMap", //$NON-NLS-1$
                introspectedTable.getBaseResultMapId()));
        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                pageDOType.getFullyQualifiedName()));


        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("select "); //$NON-NLS-1$
//        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns()
//                .iterator();
//        while (iter.hasNext()) {
//            sb.append(MyBatis3FormattingUtilities.getSelectListPhrase(iter
//                    .next()));
//
//            if (iter.hasNext()) {
//                sb.append(", "); //$NON-NLS-1$
//            }
//
//            if (sb.length() > 80) {
//                answer.addElement(new TextElement(sb.toString()));
//                sb.setLength(0);
//            }
//        }
//
//        if (sb.length() > 0) {
//            answer.addElement(new TextElement(sb.toString()));
//        }
//
//        sb.setLength(0);
        sb.append(" <include refid=\"Base_Column_List\" /> ");
        sb.append("from "); //$NON-NLS-1$
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("where"); //$NON-NLS-1$
        dynamicElement.addElement(new TextElement("is_delete=0 "));
        for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {
            if (introspectedColumn.getActualColumnName().toLowerCase().equals("is_delete")) {
                continue;
            }
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(" and ");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }

        answer.addElement(dynamicElement);
        String orderByClause = introspectedTable.getTableConfigurationProperty(PropertyRegistry.TABLE_SELECT_ALL_ORDER_BY_CLAUSE);
        boolean hasOrderBy = StringUtility.stringHasValue(orderByClause);
        if (hasOrderBy) {
            sb.setLength(0);
            sb.append("order by "); //$NON-NLS-1$
            sb.append(orderByClause);
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapSelectAllElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
