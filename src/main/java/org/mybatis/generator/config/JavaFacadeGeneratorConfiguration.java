package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * JavaServiceGeneratorConfiguration
 *
 * @author tangdu
 * @version $: JavaServiceGeneratorConfiguration.java, v 0.1 2018年01月11日 下午5:30 tangdu Exp $
 */
public class JavaFacadeGeneratorConfiguration extends PropertyHolder {
    private String targetPackage;
    private String targetProject;
    private String implementationPackage;
    private String stateCodePackage;

    public JavaFacadeGeneratorConfiguration() {
        super();
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getImplementationPackage() {
        return implementationPackage;
    }

    public void setImplementationPackage(String implementationPackage) {
        this.implementationPackage = implementationPackage;
    }

    public String getStateCodePackage() {
        return stateCodePackage;
    }

    public void setStateCodePackage(String stateCodePackage) {
        this.stateCodePackage = stateCodePackage;
    }

    public XmlElement toXmlElement() {
        XmlElement answer = new XmlElement("javaFacadeGenerator"); //$NON-NLS-1$
        if (targetPackage != null) {
            answer.addAttribute(new Attribute("targetPackage", targetPackage)); //$NON-NLS-1$
        }
        if (targetProject != null) {
            answer.addAttribute(new Attribute("targetProject", targetProject)); //$NON-NLS-1$
        }
        if (implementationPackage != null) {
            answer.addAttribute(new Attribute("implementationPackage", implementationPackage)); //$NON-NLS-1$
        }
        if (stateCodePackage != null) {
            answer.addAttribute(new Attribute("stateCodePackage", stateCodePackage)); //$NON-NLS-1$
        }
        addPropertyXmlElements(answer);
        return answer;
    }

}
