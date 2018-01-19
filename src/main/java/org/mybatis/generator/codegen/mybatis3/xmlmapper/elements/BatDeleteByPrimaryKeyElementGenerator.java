/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class BatDeleteByPrimaryKeyElementGenerator extends
        AbstractXmlElementGenerator {

    private boolean isSimple;

    public BatDeleteByPrimaryKeyElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$
        String pojoUrl=context.getJavaModelGeneratorConfiguration().getTargetPackage();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(pojoUrl + ".", "");
        FullyQualifiedJavaType pojoType = new FullyQualifiedJavaType(pojoUrl+"."+tableName);


        answer.addAttribute(new Attribute(
                "id", introspectedTable.getBatDeleteByPrimaryKeyStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                pojoType.getFullyQualifiedName()));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update  "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        sb.append(" set is_delete = 1,update_person = #{updatePerson} ");
        answer.addElement(new TextElement(sb.toString()));
        XmlElement dynamicElement = new XmlElement("where");
        StringBuilder sb2 = new StringBuilder();
        //sb2.append("\tis_delete=0");
        sb2.append("\tand id in");
        sb2.append("\n");
        sb2.append("\t\t<foreach collection=\"ids\" item=\"item\" index=\"index\" open=\"(\" separator=\",\" close=\")\">");
        sb2.append("\n");
        sb2.append("\t\t\t#{item}");
        sb2.append("\n");
        sb2.append("\t\t</foreach>");

        dynamicElement.addElement(new TextElement(sb2.toString()));
        answer.addElement(dynamicElement);

        if (context.getPlugins()
                .sqlMapDeleteByPrimaryKeyElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
