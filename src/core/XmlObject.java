package core;

import exceptions.AttributeExistsException;
import flags.TagFlag;
import interfaces.XmlObjectInterface;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static helpers.Regex.*;

/**
 * Author : Tarek Mohamed
 * 8/2/2018
 *
 * This is my new core object, The XML object is intended for creating objects out of the XML scripts
 * Helping the user to make objects out of it for better usage.
 */
public class XmlObject extends BaseTreeObject implements XmlObjectInterface<XmlObject> {

    /**
     * Section 3: The global variables
     */
    private String text;
    private String tagName;
    private TagFlag tagType;
    private XmlObject parentObject;
    private ArrayList<ObjectAttribute> attributeList;
    private ArrayList<XmlObject> nodesList;
    private static boolean isInParsingMode = false;
    private  static XmlObject currentXmlObject;
    private static XmlObject lastXmlObject;
    private static boolean isInComment = false;

    /**
     * A global constructor
     * @param tagName The title of the tag
     * @param parentObject The parent tag of that tag
     */
    public XmlObject(String tagName, XmlObject parentObject) {
        this.tagName = tagName;
        this.parentObject = parentObject;
        this.text = "";
        this.attributeList = new ArrayList<>();
        this.nodesList = new ArrayList<>();
        tagType = TagFlag.Open_Close;
    }

    /**
     * A global constructor
     * @param tagName The title of the tag
     * @param parentObject The parent tag of that tag
     * @param text The text that should be in that tag
     */
    public XmlObject(String tagName, String text, XmlObject parentObject) {
        this.tagName = tagName;
        this.text = text;
        this.parentObject = parentObject;
        this.attributeList = new ArrayList<>();
        this.nodesList = new ArrayList<>();
        tagType = TagFlag.Open_Close;
    }

    /**
     * A global constructor
     * @param tagName The title of the tag
     * @param text The text that should be in that tag
     * @param parentObject The parent tag of that tag
     * @param attributeList The list of attributes for this object
     */
    public XmlObject(String tagName, String text, XmlObject parentObject, ArrayList<ObjectAttribute> attributeList) {
        this.tagName = tagName;
        this.text = text;
        this.parentObject = parentObject;
        this.attributeList = attributeList;
        this.nodesList = new ArrayList<>();
        tagType = TagFlag.Open_Close;
    }

    /**
     * Get the type of the tag
     * 0- Open
     * 1- Open-close
     * @return An integer indicating the type of the tag
     */
    public TagFlag getTagType() {
        return tagType;
    }

    /**
     * Gets the tag name.
     * @return the tag name.
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets the parent of the current object.
     * @return A XML object referencing the parent object.
     */
    public XmlObject getParentObject() {
        return parentObject;
    }

    /**
     * Looping through the tree to get the root object.
     * @return A XML object referencing the root object.
     */
    public XmlObject getRootObject() {
        if (parentObject == null) return this;
        XmlObject tmp = this;

        while (tmp.getParentObject() != null)
            tmp = tmp.getParentObject();

        return tmp;
    }

    /**
     * Gets the xml text.
     * @return the xml inner text with {TAG} replacing the node objects of the current object
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the list of attributes of the object
     * @return An ArrayList of ObjectAttribute that contains the attributes of the object.
     */
    public ArrayList<ObjectAttribute> getAttributeList() {
        return attributeList;
    }

    /**
     * Gets the list of child objects.
     * @return An ArrayList of XmlObject (The children of the current object).
     */
    public ArrayList<XmlObject> getXmlNodesList() {
        return nodesList;
    }


    /**
     * Gets the nodes count
     * @return the number of tags within this tag
     */
    @Override
    public int getNodesCount() {
        return nodesList.size();
    }

    /**
     * Gets a node at a known position.
     * @param position The position of the required object.
     * @return The object item in the given position.
     */
    public XmlObject getNode(int position) {
        return nodesList.get(position);
    }

    /**
     * Gets the last node of the current XmlObject
     * @return A XmlObject indicating the last Object in the NodesList
     */
    public XmlObject getLastNode() {
        return nodesList.get(nodesList.size() - 1);
    }

    /**
     * Adds a new node to the current object and manages the text part after adding the new object.
     * This method is used to insert a new child object to the current XmlObject.
     * @param node The new XmlObject to be added to this object.
     */
    public void addNode(XmlObject node) {
        node.parentObject = this;
        nodesList.add(node);
        tagType = TagFlag.Open;
        if (!isInParsingMode)
            this.text = this.text + "{TAG}\n";
    }

    /**
     * Removes a node from the current object and deletes the {TAG} from the text.
     * @param position The position of the desired XmlObject.
     */
    @Override
    public void removeNode(int position) {
        nodesList.remove(position);

        String[] textParts = text.split("\\{TAG\\}");
        StringBuilder textTmp = new StringBuilder(textParts[0]);
        //The deleted node place should be less than the text parts.

        if (position <= textParts.length){
            for (int i = 1; i < textParts.length; i++){
                if (i-1 != position) textTmp.append("{TAG}\n");
                textTmp.append(textParts[i]);
            }
        }

        text = textTmp.toString();

        if (nodesList.size() == 0) tagType = TagFlag.Open_Close;
        else tagType = TagFlag.Open;
    }

    /**
     * Clears all the nodes in the current object.
     */
    @Override
    public void clearNodes() {
        for (XmlObject object : nodesList) object.parentObject = null;
        nodesList.clear();
        tagType = TagFlag.Open_Close;
    }

    /**
     * Adds a new text part for the current object.
     * @param text The text part required for adding.
     */
    public void addText(String text){
        this.text = this.text + text;
    }

    /**
     * Adds a new attribute to the current object.
     * @param attribute The new attribute to be added.
     */
    public void addAttribute(ObjectAttribute attribute) {
        for (ObjectAttribute objectAttribute : attributeList)
            if (objectAttribute.getName().equals(attribute.getName())) {
                throw new AttributeExistsException();
            }
        this.attributeList.add(attribute);
    }

    /**
     * Removes an attribute by position.
     * @param position The position of the attribute to be removed.
     */
    public void removeAttribute(int position) {
        this.attributeList.remove(position);
    }

    /**
     * Modifies an attribute by position.
     * @param position The position of the required attribute.
     * @param value The new value of that attribute.
     */
    public void modifyAttribute(int position, String value) {
        this.attributeList.get(position).setValue(value);
    }

    /**
     * Gets an attribute by position
     * @param position The position of the acquired attribute.
     * @return The ObjectAttribute required.
     */
    public ObjectAttribute getAttribute(int position) {
        return attributeList.get(position);
    }

    /**
     * Gets an attribute by name
     * @param attributeName The name of the acquired attribute.
     * @return The ObjectAttribute required.
     */
    public ObjectAttribute getAttribute(String attributeName) {
        for (ObjectAttribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) return attribute;
        }

        return null;
    }

    /**
     * Clears all the attributes in the object.
     */
    public void clearAttributes() {
        this.attributeList.clear();
    }

    /**
     * Shows the hierarchy of the current object starting from the current object.
     */
    public void showTreeHierarchy() {
        showTreeHierarchy(this, this, 0);
    }

    /**
     *  Shows the hierarchy of the current object starting from the current object.
     * @param onTreeNodeShowingListener An event that will be triggered when a new node is shown.
     */
    public void showTreeHierarchy(BaseTreeObject.OnTreeNodeShowingListener onTreeNodeShowingListener) {
        showTreeHierarchy(this, this, 0, onTreeNodeShowingListener);
    }

    /**
     * Protected method that defines how the showing process is done.
     * @param startNode The start node of the showing process.
     * @param currentNode The node that is shown currently.
     * @param startSpacing The spaces (incremented by the hierarchy level).
     */
    @Override
    protected void showTreeHierarchy(BaseTreeObject startNode, BaseTreeObject currentNode, int startSpacing) {
        if (startNode == null) startNode = getRootObject();
        if (currentNode == null) currentNode = startNode;
        System.out.println(makeSpaces(startSpacing) + currentNode.toString());
        //if (currentNode.getNodesCount() == 0) return;

        for (int i = 0; i < currentNode.getNodesCount(); i++) {
            showTreeHierarchy(startNode, ((XmlObject) currentNode).getNode(i), startSpacing + 1);
        }

        System.out.println(makeSpaces(startSpacing) + "</" + ((XmlObject) currentNode).getTagName() + ">");
    }

    /**
     * Protected method that defines how the showing process is done.
     * @param startNode The start node of the showing process.
     * @param currentNode The node that is shown currently.
     * @param startSpacing The spaces (incremented by the hierarchy level).
     * @param onTreeNodeShowingListener An event that will be triggered when a new node is shown.
     */
    @Override
    protected void showTreeHierarchy(BaseTreeObject startNode, BaseTreeObject currentNode, int startSpacing, BaseTreeObject.OnTreeNodeShowingListener onTreeNodeShowingListener) {
        if (startNode == null) startNode = getRootObject();
        if (currentNode == null) currentNode = startNode;
        if (onTreeNodeShowingListener != null) onTreeNodeShowingListener.showTreeNode(currentNode);
        if (currentNode.getNodesCount() == 0) return;

        for (int i = 0; i < currentNode.getNodesCount(); i++) {
            showTreeHierarchy(startNode, ((XmlObject) currentNode).getNode(i), startSpacing + 1);
        }
    }

    /**
     * Overriding the toString.
     * @return A string showing the open tag of the object.
     */
    @Override
    public String toString() {
        return buildTagOpen();
    }

    /**
     * Creates the open tag for the current object with all the attributes defined.
     * @return A string containing the full open tag of the current object.
     */
    @Override
    public String buildTagOpen() {
        if (getTagName().equals("comment")) {
            return "<!--";
        }
        StringBuilder tag = new StringBuilder("<" + getTagName());
        for (ObjectAttribute anAttributeList : attributeList) {
            tag.append(" ").append(anAttributeList.getName()).append("=\"").append(anAttributeList.getValue()).append("\"");
        }
        tag.append(">");
        return tag.toString();
    }

    /**
     * Creates the close tag for the current object.
     * @return A string containing the close tag of the current object.
     */
    @Override
    public String buildTagClose() {
        if (getTagName().equals("comment")) {
            return "-->";
        }
        return "</" + tagName + ">";
    }

    /**
     * Creates the open-close tag for the current object with all the attributes defined.
     * @return A string containing the full open-close tag of the current object.
     */
    @Override
    public String buildTagOpenClose() {

        StringBuilder tag = new StringBuilder("<" + getTagName());
        for (ObjectAttribute anAttributeList : attributeList) {
            tag.append(" ").append(anAttributeList.getName()).append("=\"").append(anAttributeList.getValue()).append("\"");
        }
        tag.append("/>");
        return tag.toString();
    }

    /**
     * This method is for creating an xml script out of the current tag and all the child nodes recursively
     * and return it in a string.
     * @param depthLevel The starting depth level.
     * @return A string containing the xml script.
     */
    @Override
    public String buildScript(int depthLevel) {

        if (text.equals("") && getNodesCount() == 0)
            return writeTag(buildTagOpenClose(), depthLevel);

        else {
            StringBuilder builder = new StringBuilder();

            if (!text.equals("") && getNodesCount() == 0) {

                builder.append(writeTag(buildTagOpen(), depthLevel));
                builder.append(optimizeText(text, depthLevel + 1));
                builder.append(writeTag(buildTagClose(), depthLevel));
            } else {
                String[] textBlocks = text.split("\\{TAG\\}");
                builder.append(writeTag(buildTagOpen(), depthLevel));

                if (textBlocks.length < getNodesCount()) {

                    for (int i = 0; i < textBlocks.length; i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(nodesList.get(i).buildScript(depthLevel + 1));
                    }

                    for (int i = textBlocks.length; i < getNodesCount(); i++) {
                        builder.append(nodesList.get(i).buildScript(depthLevel + 1));
                    }

                } else if (textBlocks.length > getNodesCount()) {

                    for (int i = 0; i < getNodesCount(); i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(nodesList.get(i).buildScript(depthLevel + 1));
                    }

                    for (int i = getNodesCount(); i < textBlocks.length; i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                    }
                } else {
                    for (int i = 0; i < getNodesCount(); i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(nodesList.get(i).buildScript(depthLevel + 1));
                    }
                }

                builder.append(writeTag(buildTagClose(), depthLevel));

            }
            return builder.toString();
        }
    }

    /**
     * Adjusts a text part according to the depth level specified and deletes the redundant spaces and new lines.
     * @param text The text part required to be optimized.
     * @param depthLevel The depth level required (the number of tabs needed).
     * @return The optimized text.
     */
    public String optimizeText(String text, int depthLevel) {
        StringBuilder builder = new StringBuilder();

        String[] data = text.split("\\n+?");

        for (String line : data) {
            if (line.equals("")) continue;
            else if (line.matches("(\\n|\\s|\\r)+")) continue;
            for (int i = 0; i < depthLevel; i++)
                builder.append("\t");
            builder.append(line.trim()).append("\n");
        }

        return builder.toString();
    }

    /**
     * Writes a tag at a specified depth.
     * @param tag The tag string required to be written.
     * @param depthLevel The level required for the tag to be written in.
     * @return A string containing the tag written at the specified level.
     */
    public String writeTag(String tag, int depthLevel) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < depthLevel; i++)
            builder.append("\t");
        builder.append(tag.trim()).append("\n");

        return builder.toString();
    }

    /**
     * A local method that parses a xml script string into the current object.
     * @param xmlScript The script required to be parsed into this object.
     */
    public void parseXmlIntoThis(String xmlScript){
        addNode(parseXml(xmlScript));
    }

    /**
     * The main parsing method for converting xml script into objects.
     * This method aims to convert xml scripts into XmlObject using regex.
     * @param xmlScript The script required for parsing into a new object.
     * @return An object that was created from the script.
     */
    public static XmlObject parseXml(String xmlScript) {
        isInParsingMode = true;
        Matcher matcher = Pattern.compile(REGEX_TAG_ALL, Pattern.DOTALL).matcher(xmlScript);
        matcher.find();

        currentXmlObject = new XmlObject(
                XmlObject.HelperMethods.getXmlTagName(matcher.group())
                , ""
                , null
                , XmlObject.HelperMethods.getXmlTagAttributes(matcher.group())); //gets the first tag (<html>) and use it as the root tag.
        lastXmlObject = null;

        int startIndex = matcher.end();
        int endIndex;
        TagFlag currentTagState;
        TagFlag lastTagState = TagFlag.Open;

        while (matcher.find()) { //for each tag exists in the html page :

            String tag = matcher.group(); // store the tag in a string (html tag parsing)

            //Step 1 : tags parsing
            currentTagState = handleXmlTag(tag);
            if (currentTagState != TagFlag.Useless) {

                endIndex = matcher.start();

                if (currentTagState == TagFlag.Comment_Close)
                    currentTagState = TagFlag.Close;
                else if (currentTagState == TagFlag.Comment_Open)
                    currentTagState = TagFlag.Open;

                parseText(xmlScript.substring(startIndex, endIndex), currentTagState, lastTagState);
                startIndex = matcher.end();
                lastTagState = currentTagState;
            }

        }

        isInParsingMode = false;
        return currentXmlObject;
    }

    /**
     * A method to handle the current xml tag in the script.
     * This method define the current xml tag whether it is open, close, open-close, comment or useless.
     * Used for creating new objects, discarding useless tags and declaring the comment sessions start or end.
     * The return integers :
     * -1 : useless
     * 0 : open
     * 1 : open-close
     * 2 : close
     * 3 : comment-open
     * 4 : comment-close
     * @param tag The tag string required for handling.
     * @return An integer indicating the current tag flag.
     */
    private static TagFlag handleXmlTag(String tag) {

        if (!isInComment) {

            //Step 1-A : Opened-Closed tags
            if (tag.matches(REGEX_TAG_OPEN_CLOSED)) { //if it ends with /> :
                currentXmlObject.addNode( // add it to the current html object and stay on the current object.
                        new XmlObject(XmlObject.HelperMethods.getXmlTagName(tag)
                                , ""
                                , currentXmlObject
                                , XmlObject.HelperMethods.getXmlTagAttributes(tag)));

                return TagFlag.Open_Close;
            }

            //Step 1-B : Opened tags
            else if (tag.matches(REGEX_TAG_OPEN)) { //if the tag is the open tag (<tag>):

                //saving the current html object
                lastXmlObject = currentXmlObject;

                //if it is the comment tag :
                //activate the isInComment bool and mark it as a comment tag.
                if (XmlObject.HelperMethods.getXmlTagName(tag).toLowerCase().equals("comment") || tag.equals("<!--")) {
                    isInComment = true;
                    currentXmlObject.addNode(new XmlObject("comment", "", currentXmlObject, new ArrayList<>()));
                    currentXmlObject = currentXmlObject.getLastNode();
                    return TagFlag.Comment_Open;
                }

                else { //else if it is a normal tag :
                    currentXmlObject.addNode( //mark it as a tag.
                            new XmlObject(XmlObject.HelperMethods.getXmlTagName(tag)
                                    , ""
                                    , currentXmlObject
                                    , XmlObject.HelperMethods.getXmlTagAttributes(tag)));

                    currentXmlObject = currentXmlObject.getLastNode(); //else move to the last added node and mark that as the current node.
                }

                return TagFlag.Open;

            }

            //if it is a close tag :
            else if (tag.matches(REGEX_TAG_CLOSE)) {

                //if the closing tag matches the current tag :
                // if it is not the root object :
                //return to the parent object
                if (XmlObject.HelperMethods.getXmlTagName(tag).equals(currentXmlObject.getTagName())) {
                    lastXmlObject = currentXmlObject;
                    if (currentXmlObject.getParentObject() != null)
                        currentXmlObject = currentXmlObject.getParentObject();
                }

                //else if the closing tag doesn't match the current tag :
                //Make a tmp object for searching for the right tag
                // And while true :
                //If the right tag is found :
                //If it is not the root tag :
                //Make the current object the parent of that object.
                //And end the search.
                //Else if it is the root tag :
                //Make the current tag is the root tag.
                //And end the search.
                //Else :
                //If it is not the root object (more tags to search in) :
                //Make the tmp point to the parent tag.
                //Else if the tmp points to the root tag (useless closing tag without an opening tag) :
                //Mark this tag as useless tag!
                //Then ends the search.
                else {

                    XmlObject tmp = currentXmlObject;
                    while (true) {
                        if (XmlObject.HelperMethods.getXmlTagName(tag).equals(tmp.getTagName())) {
                            lastXmlObject = currentXmlObject;

                            if (tmp.getParentObject() != null)
                                currentXmlObject = tmp.getParentObject();

                            else
                                currentXmlObject = tmp;

                            break;
                        }

                        if (tmp.getParentObject() != null)
                            tmp = tmp.getParentObject();

                        else {
                            return TagFlag.Useless;
                        }
                    }
                }

                return TagFlag.Close;
            }

            return TagFlag.Useless;

        } else {
            if ((XmlObject.HelperMethods.getXmlTagName(tag).equals("comment") || tag.equals("-->")) && XmlObject.HelperMethods.getTagState(tag) == TagFlag.Close) {
                isInComment = false;
                lastXmlObject = currentXmlObject;
                currentXmlObject = currentXmlObject.getParentObject();
                return TagFlag.Comment_Close;
            } else {
                return TagFlag.Useless;
            }
        }
    }

    /**
     * A method to parse a text part.
     * Starts triggering after the second tag is parsed.
     * It takes the text between the current and last tag and stores it in it's right tag.
     * The text is stored depending on the current and last tag states.
     * Description :
     *
     * case number : last tag state, current tag state : stored in
     *
     * case 1 : open, open : stored in the parent of the current tag.
     * case 2 : open, open-close : stored in the current tag.
     * case 3 : open, close : stored in the last(closed) tag.
     *
     * case 4 : close, open : stored in the parent of the current tag.
     * case 5 : close, open-close : stored in the current tag.
     * case 6 : close, close : stored in the last(closed) tag.
     *
     * case 7 : open-close, open : stored in the parent of the current tag.
     * case 8 : open-close, open-close : stored in the current tag.
     * case 9 : open-close, close : stored in the last(closed) tag.
     *
     *
     * @param text The text required to be parsed.
     * @param currentTagState The current tag state flag.
     * @param lastTagState The last tag state flag.
     */
    private static void parseText(String text, TagFlag currentTagState, TagFlag lastTagState) {

        if (lastTagState == TagFlag.Open) { //Open tag

            if (currentTagState == TagFlag.Open) {
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + text;
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Open_Close) {
                currentXmlObject.text = currentXmlObject.text + text;
                currentXmlObject.text = currentXmlObject.text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Close) {
                lastXmlObject.text = lastXmlObject.text + text;
            }
        } else if (lastTagState == TagFlag.Close) {

            if (currentTagState == TagFlag.Open) {
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + text;
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Open_Close) {
                currentXmlObject.text = currentXmlObject.text + text;
                currentXmlObject.text = currentXmlObject.text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Close) {
                lastXmlObject.text = lastXmlObject.text + text;
            }
        } else if (lastTagState == TagFlag.Open_Close) {
            if (currentTagState == TagFlag.Open) {
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + text;
                currentXmlObject.getParentObject().text = currentXmlObject.getParentObject().text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Open_Close) {
                currentXmlObject.text = currentXmlObject.text + text;
                currentXmlObject.text = currentXmlObject.text + "{TAG}\n";
            } else if (currentTagState == TagFlag.Close) {
                lastXmlObject.text = lastXmlObject.text + text;
            }
        }
    }

    public static class HelperMethods{
        /**
         * @param tag tag string
         * @return integer representing the state of the current tag
         * 1 => open-closed tag
         * 2 => closed tag
         * 0 => open tag
         */
        public static TagFlag getTagState(String tag) {
            if (tag.matches(".*\\s*/>$")) return TagFlag.Open_Close;
            else if (tag.startsWith("</") || tag.equals("-->")) return TagFlag.Close;
            else return TagFlag.Open;
        }

        /**
         * Public method to download html web page from the internet
         *
         * @param urlString The website required for downloading
         * @return The html script of the website
         */
        public static String downloadWebPage(String urlString) {
            ExecutorService executors = Executors.newSingleThreadExecutor();
            try {

                Future<String> stringFuture = executors.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        HttpURLConnection httpURLConnection = null;
                        FileURLConnection fileURLConnection = null;
                        BufferedReader reader = null;
                        StringBuilder builder = new StringBuilder();

                        try {
                            URL url = new URL(urlString);
                            try {
                                httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0");
                                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                            } catch (ClassCastException e) {
                                fileURLConnection = (FileURLConnection) url.openConnection();
                                reader = new BufferedReader(new InputStreamReader(fileURLConnection.getInputStream()));
                            }
                            String line;

                            while ((line = reader.readLine()) != null) {
                                builder.append(line).append("\n");
                            }

                            if (httpURLConnection != null)
                                httpURLConnection.disconnect();

                            if (fileURLConnection != null)
                                fileURLConnection.close();

                            return builder.toString();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (httpURLConnection != null) httpURLConnection.disconnect();
                        }

                        return "";
                    }
                });
                executors.shutdown();
                String string = stringFuture.get();
                stringFuture.cancel(true);
                return string;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            return "";
        }

        /**
         * Public method to get the html tag name out of html tag string
         *
         * @param htmlTag the html tag
         * @return the tag name
         */
        public static String getXmlTagName(String htmlTag) {
            if (htmlTag.matches(REGEX_TAG_ALL)) {
                if (htmlTag.equals("<!--") || htmlTag.equals("-->")) return "comment";
                Matcher matcher = Pattern.compile("\\w(\\w|\\d)*").matcher(htmlTag);
                matcher.find();
                return matcher.group();
            }

            return "";
        }

        /**
         * Public method to return the html attributes out of html tag script
         *
         * @param htmlTag The html tag
         * @return an ArrayList containing all the html tag attributes
         */
        public static ArrayList<ObjectAttribute> getXmlTagAttributes(String htmlTag) {
            ArrayList<ObjectAttribute> attributes = new ArrayList<>();
            Matcher matcher = Pattern.compile("\\s[^\\s=<>/]+(\\s?=\\s?((\"[\\S\\s]*?\")|('[\\S\\s]*?')))?").matcher(htmlTag);
            while (matcher.find()) {
                String[] attr = matcher.group().split("=", 2);
                try {

                    if (attr.length == 2 && attr[1].length() > 0 && !attr[1].equals("\"\""))
                        attributes.add(
                                new ObjectAttribute(
                                        attr[0].replace(" ", "")
                                        , attr[1].substring(1, attr[1].length() - 1)
                                )
                        );

                    else {

                        attributes.add(
                                new ObjectAttribute(
                                        attr[0]
                                        , ""
                                )
                        );
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println(attr[1]);
                    e.printStackTrace();
                }
            }

            return attributes;

        }
    }

    public static class ObjectAttribute{
        String name;
        String value;

        public ObjectAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
