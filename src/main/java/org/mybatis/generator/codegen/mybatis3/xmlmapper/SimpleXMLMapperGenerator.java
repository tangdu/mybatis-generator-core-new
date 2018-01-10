/**
 *    Copyright 2006-2016 the original author or authors.
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
package org.mybatis.generator.codegen.mybatis3.xmlmapper;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.*;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class SimpleXMLMapperGenerator extends AbstractXmlGenerator {

    public SimpleXMLMapperGenerator() {
        super();
    }

    protected XmlElement getSqlMapElement() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.12", table.toString())); //$NON-NLS-1$
        XmlElement answer = new XmlElement("mapper"); //$NON-NLS-1$
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace();
        answer.addAttribute(new Attribute("namespace", //$NON-NLS-1$
                namespace));

        context.getCommentGenerator().addRootComment(answer);

        addResultMapElement(answer);
        addBaseColumnListElement(answer);
        addDeleteByPrimaryKeyElement(answer);
        addInsertElement(answer);
        addSelectByPrimaryKeyElement(answer);
        addQueryPageElement(answer);
        addUpdateByPrimaryKeyElement(answer);
        addUpdateByPrimaryKeySelectiveElement(answer);
        answer.addElement(new TextElement(""));
        answer.addElement(new TextElement(""));
        return answer;
    }

    void addLineElement(XmlElement parentElement,String content){
        for (int i = 0; i < 2; i++) {
            parentElement.addElement(new TextElement(""));
        }
        parentElement.addElement(new TextElement(String.format("<!--%s-->",content)));
    }

    protected void addResultMapElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateBaseResultMap()) {
            addLineElement(parentElement,"ResultMap");
            AbstractXmlElementGenerator elementGenerator = new ResultMapWithoutBLOBsElementGenerator(
                    true);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
            addLineElement(parentElement,"根据ID查询");
            AbstractXmlElementGenerator elementGenerator = new SimpleSelectByPrimaryKeyElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addQueryPageElement(XmlElement parentElement) {
        addLineElement(parentElement,"根据参数分页查询");
        AbstractXmlElementGenerator elementGenerator = new SimpleSelectAllElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);

    }

    protected void addDeleteByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            addLineElement(parentElement,"根据ID删除");
            AbstractXmlElementGenerator elementGenerator = new DeleteByPrimaryKeyElementGenerator(true);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsert()) {
            addLineElement(parentElement,"添加");
            AbstractXmlElementGenerator elementGenerator = new InsertElementGenerator(true);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            addLineElement(parentElement,"根据ID更新");
            AbstractXmlElementGenerator elementGenerator = new UpdateByPrimaryKeyWithoutBLOBsElementGenerator(
                    true);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByPrimaryKeySelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            addLineElement(parentElement,"根据ID选择性更新");
            AbstractXmlElementGenerator elementGenerator = new UpdateByPrimaryKeySelectiveElementGenerator(
            );
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addBaseColumnListElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateBaseColumnList()) {
            addLineElement(parentElement,"基础列");
            AbstractXmlElementGenerator elementGenerator = new BaseColumnListElementGenerator(
            );
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }


    protected void initializeAndExecuteGenerator(
            AbstractXmlElementGenerator elementGenerator,
            XmlElement parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.addElements(parentElement);
    }

    @Override
    public Document getDocument() {
        Document document = new Document(
                XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
                XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
        document.setRootElement(getSqlMapElement());

        if (!context.getPlugins().sqlMapDocumentGenerated(document,
                introspectedTable)) {
            document = null;
        }

        return document;
    }
}
