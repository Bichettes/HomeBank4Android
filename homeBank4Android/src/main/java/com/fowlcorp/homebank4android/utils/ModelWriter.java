package com.fowlcorp.homebank4android.utils;

import com.fowlcorp.homebank4android.model.Account;
import com.fowlcorp.homebank4android.model.Category;
import com.fowlcorp.homebank4android.model.Model;
import com.fowlcorp.homebank4android.model.Operation;
import com.fowlcorp.homebank4android.model.Payee;
import com.fowlcorp.homebank4android.model.Properties;
import com.fowlcorp.homebank4android.model.Tag;
import com.fowlcorp.homebank4android.model.Template;
import com.fowlcorp.homebank4android.model.Triplet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Ced
 */
public class ModelWriter {

    private Model model;
    private String filePath;

    public ModelWriter(Model model, String filePath) {
        this.model = model;
        this.filePath = filePath;
    }

    public boolean writeToFile() throws IOException {

        File file = new File(filePath+"bb");

        FileOutputStream stream = new FileOutputStream(file);
        try {
            String xmlString = generateXML();
            stream.write(xmlString.getBytes());
        }
        finally {
            stream.close();
        }

        return true;
    }

    public String generateXML() {
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        String xml = "";
        try {
            icBuilder = icFactory.newDocumentBuilder();
            Document doc = icBuilder.newDocument();
            Element mainRootElement = doc.createElement("homebank");
            mainRootElement.setAttribute("v", "1.1000000000000001");
            doc.appendChild(mainRootElement);

            // Properties
            Properties properties = model.getProperties();
            Element propertiesElement = doc.createElement("properties");
            propertiesElement.setAttribute("title", properties.getTitle());
            propertiesElement.setAttribute("car_category", Integer.toString(properties.getCarCategory().getKey()));
            propertiesElement.setAttribute("auto_smode", Integer.toString(properties.getAutoSmode()));
            propertiesElement.setAttribute("auto_weekday", Integer.toString(properties.getAutoWeekday()));
            propertiesElement.setAttribute("auto_nbdays", Integer.toString(properties.getAutoNbdays()));
            mainRootElement.appendChild(propertiesElement);

            // append accounts elements to root element
            for(Account acc : model.getAccounts().values()) {
                mainRootElement.appendChild(getAccountNode(doc, acc));
            }

            for(Payee payee : model.getPayees().values()) {
                mainRootElement.appendChild(getPayeeNode(doc, payee));
            }

            for(Category category : model.getCategories().values()) {
                mainRootElement.appendChild(getCategoryNode(doc, category));
            }

            for(Tag tag : model.getTags().values()) {
                mainRootElement.appendChild(getTagNode(doc, tag));
            }

            for(Template template : model.getTemplates()) {
                mainRootElement.appendChild(getTemplateNode(doc, template));
            }

            for(Account acc : model.getAccounts().values()) {
                for(Operation op : model.getOperations(acc)) {
                    mainRootElement.appendChild(getOperationNode(doc, op));
                }
            }

            // output DOM XML to console
            xml = getStringFromNode(doc);
            xml = xml.replaceAll("  ", " "); // Homebank does not accept xml files with double space
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xml;
    }

    public String getStringFromNode(Node root) throws IOException {

        StringBuilder result = new StringBuilder();

        if (root.getNodeType() == 3)
            result.append(root.getNodeValue());
        else {
            if (root.getNodeType() != 9) {
                StringBuffer attrs = new StringBuffer();
                for (int k = 0; k < root.getAttributes().getLength(); ++k) {
                    attrs.append(" ").append(
                            root.getAttributes().item(k).getNodeName()).append(
                            "=\"").append(
                            root.getAttributes().item(k).getNodeValue())
                            .append("\" ");
                }
                result.append("<").append(root.getNodeName()).append(" ")
                        .append(attrs).append(">");
            } else {
                result.append("<?xml version=\"1.0\" ?>");
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0, j = nodes.getLength(); i < j; i++) {
                Node node = nodes.item(i);
                result.append(getStringFromNode(node));
            }

            if (root.getNodeType() != 9) {
                result.append("</").append(root.getNodeName()).append(">");
            }
        }
        return result.toString();
    }


    private Element getAccountNode(Document doc, Account acc) {
        Element accElement = doc.createElement("account");
        accElement.setAttribute("key", Integer.toString(acc.getKey()));
        accElement.setAttribute("pos", Integer.toString(acc.getPos()));
        accElement.setAttribute("type", Integer.toString(acc.getType()));
        accElement.setAttribute("name", acc.getName());
        if(acc.getAccountNumber() != null) {
            accElement.setAttribute("number", acc.getAccountNumber());
        }
        if(acc.getBankName() != null) {
            accElement.setAttribute("bankname", acc.getBankName());
        }
        accElement.setAttribute("initial", Double.toString(acc.getInitBalance()));
        accElement.setAttribute("minimum", Double.toString(acc.getMinimumBalance()));

        return accElement;
    }

    private Element getPayeeNode(Document doc, Payee payee) {
        Element payeeElement = doc.createElement("pay");
        payeeElement.setAttribute("key", Integer.toString(payee.getKey()));
        payeeElement.setAttribute("name", payee.getName());

        return payeeElement;
    }

    private Element getCategoryNode(Document doc, Category category) {
        Element categoryElement = doc.createElement("cat");
        categoryElement.setAttribute("key", Integer.toString(category.getKey()));
        if(category.getParent() != null) {
            categoryElement.setAttribute("parent", Integer.toString(category.getParent().getKey()));
        }
        categoryElement.setAttribute("flags", Integer.toString(category.getFlags()));
        categoryElement.setAttribute("name", category.getName());

        return categoryElement;
    }

    private Element getTagNode(Document doc, Tag tag) {
        Element tagElement = doc.createElement("tag");
        tagElement.setAttribute("key", Integer.toString(tag.getKey()));
        tagElement.setAttribute("name", tag.getName());

        return tagElement;
    }

    private Element getTemplateNode(Document doc, Template template) {
        Element templateElement = doc.createElement("fav");
        templateElement.setAttribute("amount", Double.toString(template.getAmount()));
        templateElement.setAttribute("wording", template.getWording());
        templateElement.setAttribute("nextdate", Integer.toString(template.getNextDateXml()));
        templateElement.setAttribute("every", Integer.toString(template.getEvery()));
        templateElement.setAttribute("unit", Integer.toString(template.getUnit()));
        templateElement.setAttribute("limit", Integer.toString(template.getLimit()));
        if(template.getPaymode() != 0) {
            templateElement.setAttribute("paymode", Integer.toString(template.getPaymode()));
        }
        if(template.getCategory() != null) {
            templateElement.setAttribute("category", Integer.toString(template.getCategory().getKey()));
        }
        if(template.getPayee() != null) {
            templateElement.setAttribute("payee", Integer.toString(template.getPayee().getKey()));
        }
        if(template.getFlags() != 0) {
            templateElement.setAttribute("flags", Integer.toString(template.getFlags()));
        }
        if(template.getWeekend() != 0) {
            templateElement.setAttribute("weekend", Integer.toString(template.getWeekend()));
        }

        return templateElement;
    }

    private Element getOperationNode(Document doc, Operation operation) {
        Element operationElement = doc.createElement("ope");
        operationElement.setAttribute("date", Integer.toString(operation.getXmlDate()));
        operationElement.setAttribute("amount", Double.toString(operation.getAmount()));
        operationElement.setAttribute("account", Integer.toString(operation.getAccount().getKey()));
        if(operation.getPayMode() != 0) {
            operationElement.setAttribute("paymode", Integer.toString(operation.getPayMode()));
        }
        if(operation.getState() != 0) {
            operationElement.setAttribute("st", Integer.toString(operation.getState()));
        }
        if(operation.getFlags() != 0) {
            operationElement.setAttribute("flags", Integer.toString(operation.getFlags()));
        }
        if(operation.getPayee() != null) {
            operationElement.setAttribute("payee", Integer.toString(operation.getPayee().getKey()));
        }
        if(operation.getCategory() != null) {
            operationElement.setAttribute("category", Integer.toString(operation.getCategory().getKey()));
        }
        if(operation.getWording() != null) {
            operationElement.setAttribute("wording", operation.getWording());
        }
        if(operation.getTags() != null) {
            operationElement.setAttribute("tags", operation.getTags());
        }
        if(operation.isSplit()) {
            String amounts = "", mems = "", cats = "";
            for(Triplet triplet : operation.getSplits()) {
                amounts += triplet.getAmount() + "||";
                mems += triplet.getWording() + "||";
                cats += triplet.getCategory().getKey() + "||";
            }
            amounts = amounts.substring(0, amounts.length()-2);
            mems = mems.substring(0, mems.length()-2);
            cats = cats.substring(0, cats.length()-2);
            operationElement.setAttribute("scat", cats);
            operationElement.setAttribute("samt", amounts);
            operationElement.setAttribute("smem", mems);
        }
        return operationElement;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
