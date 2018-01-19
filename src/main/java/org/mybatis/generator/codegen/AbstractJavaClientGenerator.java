/**
 * Copyright 2006-2017 the original author or authors.
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
package org.mybatis.generator.codegen;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaElement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class exists to that Java client generators can specify whether
 * an XML generator is required to match the methods in the
 * Java client.  For example, a Java client built entirely with
 * annotations does not need matching XML.
 *
 * @author Jeff Butler
 *
 */
public abstract class AbstractJavaClientGenerator extends AbstractJavaGenerator {

    private boolean requiresXMLGenerator;
    private SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy年MM月dd日 上午HH:mm");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy年MM月dd日 下午HH:mm");

    public AbstractJavaClientGenerator(boolean requiresXMLGenerator) {
        super();
        this.requiresXMLGenerator = requiresXMLGenerator;
    }

    /**
     * Returns true is a matching XML generator is required.
     *
     * @return true if matching XML is generator required
     */
    public boolean requiresXMLGenerator() {
        return requiresXMLGenerator;
    }

    /**
     * Returns an instance of the XML generator associated
     * with this client generator.
     *
     * @return the matched XML generator.  May return null if no
     *     XML is required by this generator
     */
    public abstract AbstractXmlGenerator getMatchedXMLGenerator();

    public void addClassComment(JavaElement innerClass, FullyQualifiedJavaType type, IntrospectedTable introspectedTable, String desc) {
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
        sb.append(introspectedTable.getRemarks() + desc);
        sb.append("\n");
        sb.append(" * ");
        sb.append("\n");
        sb.append(" * ");
        sb.append("@author ").append(username).append("\n");
        sb.append(" * ").append("@version ").append(String.format("$: %s.java, v 0.1 %s %s Exp $ ", type.getShortName(), dateStr, username));
        innerClass.addJavaDocLine(sb.toString());
        innerClass.addJavaDocLine(" */"); //$NON-NLS-1$
    }

}
