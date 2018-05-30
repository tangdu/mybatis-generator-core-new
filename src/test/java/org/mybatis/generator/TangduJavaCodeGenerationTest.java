/**
 *    Copyright 2006-2018 the original author or authors.
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
package org.mybatis.generator;

import org.junit.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试生成代码
 */
public class TangduJavaCodeGenerationTest {

    //是否生成文件
    private static boolean writerflag=false;
    @Test
    public void testJavaParse() {
        try {
            List<GeneratedJavaFile> generatedJavaFiles = generateJavaFiles();
            for (GeneratedJavaFile generatedJavaFile : generatedJavaFiles) {
//                System.out.println(generatedJavaFile);
            }

            List<GeneratedXmlFile> generatedJavaFiles2 = generateXMLFiles();
            for (GeneratedXmlFile generatedJavaFile : generatedJavaFiles2) {
                System.out.println(generatedJavaFile);
            }
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    public static List<GeneratedJavaFile> generateJavaFiles() throws Exception {
        List<GeneratedJavaFile> generatedFiles = new ArrayList<GeneratedJavaFile>();
        generatedFiles.addAll(generateJavaFilesMybatis());
        return generatedFiles;
    }

    private static List<GeneratedJavaFile> generateJavaFilesMybatis() throws Exception {
        createDatabase();
        return generateJavaFiles("/scripts/generatorConfig.xml");
    }

    public static List<GeneratedXmlFile> generateXMLFiles() throws Exception {
        return generateXMLFiles("/scripts/generatorConfig.xml");
    }

    private static List<GeneratedJavaFile> generateJavaFiles(String configFile) throws Exception {
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(TangduJavaCodeGenerationTest.class.getResourceAsStream(configFile));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, writerflag);
        return myBatisGenerator.getGeneratedJavaFiles();
    }

    private static List<GeneratedXmlFile> generateXMLFiles(String configFile) throws Exception {
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(TangduJavaCodeGenerationTest.class.getResourceAsStream(configFile));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, writerflag);
        return myBatisGenerator.getGeneratedXmlFiles();
    }

    public static void createDatabase() throws Exception {
        SqlScriptRunner scriptRunner = new SqlScriptRunner(TangduJavaCodeGenerationTest.class.getResourceAsStream("/scripts/CreateDB.sql"), "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:aname", "sa", "");
        scriptRunner.executeScript();
    }
}
