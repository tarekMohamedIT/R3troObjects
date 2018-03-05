package core;

import abstraction.BaseTreeObject;
import exceptions.AttributeExistsException;
import helpers.Regex;
import helpers.TagFlags;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tarek on 6/29/17.
 */
public class HtmlObject extends BaseTreeObject {

    private static int tagCount = 0;

    private String text;
    private String tagName;
    private int tagType;
    private HtmlObject parentObject;
    private HtmlObject rootObject;
    private ArrayList<HtmlObjectAttribute> attributeList;
    private ArrayList<HtmlObject> htmlNodesList;
    private static boolean isInParsingMode = false;
    private  static HtmlObject currentHtmlObject;
    private static HtmlObject lastHtmlObject;
    private static boolean isInScript = false; //For checking if the script tag is activated.
    private static boolean isInComment = false; //For checking if the comment tag is active.

    public HtmlObject(String tagName, HtmlObject parentObject) {
        this.tagName = tagName;
        this.parentObject = parentObject;
        this.text = "";
        this.attributeList = new ArrayList<>();
        this.htmlNodesList = new ArrayList<>();
        tagType = TagFlags.TAG_OPEN_CLOSE;
    }

    public HtmlObject(String tagName, String text, HtmlObject parentObject) {
        this.tagName = tagName;
        this.text = text;
        this.parentObject = parentObject;
        this.attributeList = new ArrayList<>();
        this.htmlNodesList = new ArrayList<>();
        tagType = TagFlags.TAG_OPEN_CLOSE;
    }

    public HtmlObject(String tagName, String text, HtmlObject parentObject, ArrayList<HtmlObjectAttribute> attributeList) {
        this.tagName = tagName;
        this.text = text;
        this.parentObject = parentObject;
        this.attributeList = attributeList;
        this.htmlNodesList = new ArrayList<>();
        tagType = TagFlags.TAG_OPEN_CLOSE;
    }

    public static int getTagCount() {
        return tagCount;
    }

    public int getTagType() {
        return tagType;
    }

    public String getTagName() {
        return tagName;
    }

    public HtmlObject getParentObject() {
        return parentObject;
    }

    public HtmlObject getRootObject() {
        return rootObject;
    }

    public ArrayList<HtmlObjectAttribute> getAttributeList() {
        return attributeList;
    }

    public ArrayList<HtmlObject> getHtmlNodesList() {
        return htmlNodesList;
    }

    @Override
    public int getNodesCount() {
        return htmlNodesList.size();
    }

    public HtmlObject getNode(int position) {
        return htmlNodesList.get(position);
    }

    public HtmlObject getLastNode() {
        return htmlNodesList.get(htmlNodesList.size() - 1);
    }

    public String getText() {
        return text;
    }

    public void addText(String text){
        this.text = this.text + text;
    }

    public void addNode(HtmlObject node) {
        node.parentObject = this;
        htmlNodesList.add(node);
        tagCount++;
        tagType = TagFlags.TAG_OPEN;
        if (!isInParsingMode)
            this.text = this.text + "{TagFlags.TAG}\n";
    }

    @Override
    public void removeNode(int position) {
        htmlNodesList.remove(position);
        tagCount--;

        if (htmlNodesList.size() == 0) tagType = TagFlags.TAG_OPEN_CLOSE;
        else tagType = TagFlags.TAG_OPEN;
    }

    @Override
    public void clearNodes() {
        htmlNodesList.clear();
        tagType = TagFlags.TAG_OPEN_CLOSE;
    }

    public void addAttribute(HtmlObjectAttribute attribute) {
        for (HtmlObjectAttribute objectAttribute : attributeList)
            if (objectAttribute.getName().equals(attribute.getName())) {
                throw new AttributeExistsException();
            }
        this.attributeList.add(attribute);
    }

    public void removeAttribute(int position) {
        this.attributeList.remove(position);
    }

    public void modifyAttribute(int position, String value) {
        this.attributeList.get(position).setValue(value);
    }

    public HtmlObjectAttribute getAttribute(int position) {
        return attributeList.get(position);
    }

    public HtmlObjectAttribute getAttribute(String attributeName) {
        for (HtmlObjectAttribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) return attribute;
        }

        return null;
    }

    public void clearAttributes() {
        this.attributeList.clear();
    }

    public void showTreeHierarchy() {
        showTreeHierarchy(this, this, 0);
    }

    public void showTreeHierarchy(OnTreeNodeShowingListener onTreeNodeShowingListener) {
        showTreeHierarchy(this, this, 0, onTreeNodeShowingListener);
    }

    @Override
    protected void showTreeHierarchy(BaseTreeObject startNode, BaseTreeObject currentNode, int startSpacing) {
        if (startNode == null) startNode = rootObject;
        if (currentNode == null) currentNode = startNode;
        System.out.println(makeSpaces(startSpacing) + currentNode.toString());
        //if (currentNode.getNodesCount() == 0) return;

        for (int i = 0; i < currentNode.getNodesCount(); i++) {
            showTreeHierarchy(startNode, ((HtmlObject) currentNode).getNode(i), startSpacing + 1);
        }

        System.out.println(makeSpaces(startSpacing) + "</" + ((HtmlObject) currentNode).getTagName() + ">");
    }

    @Override
    protected void showTreeHierarchy(BaseTreeObject startNode, BaseTreeObject currentNode, int startSpacing, OnTreeNodeShowingListener onTreeNodeShowingListener) {
        if (startNode == null) startNode = rootObject;
        if (currentNode == null) currentNode = startNode;
        if (onTreeNodeShowingListener != null) onTreeNodeShowingListener.showTreeNode(currentNode);
        if (currentNode.getNodesCount() == 0) return;

        for (int i = 0; i < currentNode.getNodesCount(); i++) {
            showTreeHierarchy(startNode, ((HtmlObject) currentNode).getNode(i), startSpacing + 1);
        }
    }

    @Override
    public String toString() {
        return buildTagOpen();
    }

    public String buildTagOpen() {
        if (getTagName().equals("comment")) {
            return "<!--";
        }
        StringBuilder tag = new StringBuilder("<" + getTagName());
        for (HtmlObjectAttribute anAttributeList : attributeList) {
            tag.append(" ").append(anAttributeList.getName()).append("=\"").append(anAttributeList.getValue()).append("\"");
        }
        tag.append(">");
        return tag.toString();
    }

    public String buildTagClose() {
        if (getTagName().equals("comment")) {
            return "-->";
        }
        return "</" + tagName + ">";
    }

    public String buildTagOpenClose() {

        if (getTagName().matches(Regex.REGEX_TAG_NON_VOID)) return buildTagOpen() + buildTagClose();

        StringBuilder tag = new StringBuilder("<" + getTagName());
        for (HtmlObjectAttribute anAttributeList : attributeList) {
            tag.append(" ").append(anAttributeList.getName()).append("=\"").append(anAttributeList.getValue()).append("\"");
        }
        tag.append("/>");
        return tag.toString();
    }

    public String buildHtmlScript(int depthLevel) {

        if (text.equals("") && getNodesCount() == 0)
            return writeTag(buildTagOpenClose(), depthLevel);

        else {
            StringBuilder builder = new StringBuilder();

            if (!text.equals("") && getNodesCount() == 0) {

                builder.append(writeTag(buildTagOpen(), depthLevel));
                builder.append(optimizeText(text, depthLevel + 1));
                builder.append(writeTag(buildTagClose(), depthLevel));
            } else {
                String[] textBlocks = text.split("\\{TagFlags.TAG\\}");
                builder.append(writeTag(buildTagOpen(), depthLevel));

                if (textBlocks.length < getNodesCount()) {

                    for (int i = 0; i < textBlocks.length; i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(htmlNodesList.get(i).buildHtmlScript(depthLevel + 1)).append("\n");
                    }

                    for (int i = textBlocks.length; i < getNodesCount(); i++) {
                        builder.append(htmlNodesList.get(i).buildHtmlScript(depthLevel + 1)).append("\n");
                    }

                } else if (textBlocks.length > getNodesCount()) {

                    for (int i = 0; i < getNodesCount(); i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(htmlNodesList.get(i).buildHtmlScript(depthLevel + 1)).append("\n");
                    }

                    for (int i = getNodesCount(); i < textBlocks.length; i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                    }
                } else {
                    for (int i = 0; i < getNodesCount(); i++) {
                        builder.append(optimizeText(textBlocks[i], depthLevel + 1));
                        builder.append(htmlNodesList.get(i).buildHtmlScript(depthLevel + 1)).append("\n");
                    }
                }

                builder.append(writeTag(buildTagClose(), depthLevel));

            }
            return builder.toString();
        }
    }

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

    public String writeTag(String tag, int depthLevel) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < depthLevel; i++)
            builder.append("\t");
        builder.append(tag.trim()).append("\n");

        return builder.toString();
    }

    public static HtmlObject parseHtml(String urlString) {

        isInParsingMode = true;
        String htmlScript = HelperMethods.downloadWebPage(urlString); //for downloading the web page using the website address.

        Matcher matcher = Pattern.compile(Regex.REGEX_TAG_ALL, Pattern.DOTALL).matcher(htmlScript);
        matcher.find();

        currentHtmlObject = new HtmlObject(
                HelperMethods.getHtmlTagName(matcher.group())
                , ""
                , null
                , HelperMethods.getHtmlTagAttributes(matcher.group())); //gets the first tag (<html>) and use it as the root tag.
        lastHtmlObject = null;

        int startIndex = matcher.end();
        int endIndex;
        int currentTagState;
        int lastTagState = TagFlags.TAG_OPEN;

        while (matcher.find()) { //for each tag exists in the html page :
            String tag = matcher.group(); // store the tag in a string (html tag parsing)

            //Step 1 : tags parsing
            currentTagState = handleHtmlTag(tag);
            if (currentTagState != TagFlags.TAG_USELESS) {

                endIndex = matcher.start();
                if (currentTagState == TagFlags.TAG_COMMENT_END || currentTagState == TagFlags.TAG_SCRIPT_END)
                    currentTagState = TagFlags.TAG_CLOSE;
                else if (currentTagState == TagFlags.TAG_COMMENT || currentTagState == TagFlags.TAG_SCRIPT)
                    currentTagState = TagFlags.TAG_OPEN;

                parseText(htmlScript.substring(startIndex, endIndex), currentTagState, lastTagState);
                startIndex = matcher.end();
                lastTagState = currentTagState;
            }

        }

        isInParsingMode = false;
        return currentHtmlObject;
    }

    private static int handleHtmlTag(String tag) {

        if (!isInComment && !isInScript) {

            //Step 1-A : Opened-Closed tags
            if (tag.matches(Regex.REGEX_TAG_OPEN_CLOSED)) { //if it ends with /> :
                currentHtmlObject.addNode( // add it to the current html object and stay on the current object.
                        new HtmlObject(HelperMethods.getHtmlTagName(tag)
                                , ""
                                , currentHtmlObject
                                , HelperMethods.getHtmlTagAttributes(tag)));

                return TagFlags.TAG_OPEN_CLOSE;
            }

            //Step 1-B : Opened tags
            else if (tag.matches(Regex.REGEX_TAG_OPEN)) { //if the tag is the open tag (<tag>):
                //saving the current html object
                lastHtmlObject = currentHtmlObject;

                //if the head tag isn't closed and the body tag is opened
                //close the head tag and make the parent object the current tag.
                if (HelperMethods.getHtmlTagName(tag).equals("body") && currentHtmlObject.getTagName().equals("head")) {
                    currentHtmlObject = currentHtmlObject.getParentObject();
                }

                //if it is the comment tag :
                //activate the isInComment bool and mark it as a comment tag.
                if (HelperMethods.getHtmlTagName(tag).toLowerCase().equals("comment") || tag.equals("<!--")) {
                    isInComment = true;
                    currentHtmlObject.addNode(new HtmlObject("comment", "", currentHtmlObject, new ArrayList<>()));
                    currentHtmlObject = currentHtmlObject.getLastNode();
                    return TagFlags.TAG_COMMENT;
                }

                //if it is the comment or script tag :
                //mark it as a tag
                //mark isInScript as true.
                else if (HelperMethods.getHtmlTagName(tag).equals("script")) {
                    currentHtmlObject.addNode(
                            new HtmlObject(HelperMethods.getHtmlTagName(tag)
                                    , ""
                                    , currentHtmlObject
                                    , HelperMethods.getHtmlTagAttributes(tag)));

                    isInScript = true;
                    currentHtmlObject = currentHtmlObject.getLastNode();
                    return TagFlags.TAG_SCRIPT;
                } else { //else if it is a normal tag :
                    currentHtmlObject.addNode( //mark it as a tag.
                            new HtmlObject(HelperMethods.getHtmlTagName(tag)
                                    , ""
                                    , currentHtmlObject
                                    , HelperMethods.getHtmlTagAttributes(tag)));

                    if (HelperMethods.getHtmlTagName(tag).matches(Regex.REGEX_TAG_OPEN_ONLY)) {
                        return TagFlags.TAG_OPEN_CLOSE; //if it ends with /> then continue.
                    }
                    currentHtmlObject = currentHtmlObject.getLastNode(); //else move to the last added node and mark that as the current node.
                }

                return TagFlags.TAG_OPEN;

            }

            //if it is a close tag :
            else if (tag.matches(Regex.REGEX_TAG_CLOSE)) {

                //if the closing tag matches the current tag :
                // if it is not the root object :
                //return to the parent object
                if (HelperMethods.getHtmlTagName(tag).equals(currentHtmlObject.getTagName())) {
                    lastHtmlObject = currentHtmlObject;
                    if (currentHtmlObject.getParentObject() != null)
                        currentHtmlObject = currentHtmlObject.getParentObject();
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

                    HtmlObject tmp = currentHtmlObject;
                    while (true) {
                        if (HelperMethods.getHtmlTagName(tag).equals(tmp.getTagName())) {
                            lastHtmlObject = currentHtmlObject;

                            if (tmp.getParentObject() != null)
                                currentHtmlObject = tmp.getParentObject();

                            else
                                currentHtmlObject = tmp;

                            break;
                        }

                        if (tmp.getParentObject() != null)
                            tmp = tmp.getParentObject();

                        else {
                            return TagFlags.TAG_USELESS;
                        }
                    }
                }

                return TagFlags.TAG_CLOSE;
            }

            return TagFlags.TAG_USELESS;

        } else if (isInScript && !isInComment) {
            if (HelperMethods.getHtmlTagName(tag).equals("script") && HelperMethods.getTagState(tag) == TagFlags.TAG_CLOSE) {
                isInScript = false;
                lastHtmlObject = currentHtmlObject;
                currentHtmlObject = currentHtmlObject.getParentObject();
                return TagFlags.TAG_SCRIPT_END;
            } else {
                return TagFlags.TAG_USELESS;
            }
        } else {
            if ((HelperMethods.getHtmlTagName(tag).equals("comment") || tag.equals("-->")) && HelperMethods.getTagState(tag) == TagFlags.TAG_CLOSE) {
                isInComment = false;
                lastHtmlObject = currentHtmlObject;
                currentHtmlObject = currentHtmlObject.getParentObject();
                return TagFlags.TAG_COMMENT_END;
            } else {
                return TagFlags.TAG_USELESS;
            }
        }

    }

    private static void parseText(String text, int currentTagState, int lastTagState) {

        if (lastTagState == TagFlags.TAG_OPEN) { //Open tag

            if (currentTagState == TagFlags.TAG_OPEN) {
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + text;
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_OPEN_CLOSE) {
                currentHtmlObject.text = currentHtmlObject.text + text;
                currentHtmlObject.text = currentHtmlObject.text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_CLOSE) {
                lastHtmlObject.text = lastHtmlObject.text + text;
            }
        } else if (lastTagState == TagFlags.TAG_CLOSE) {
            if (currentTagState == TagFlags.TAG_OPEN) {
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + text;
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_OPEN_CLOSE) {
                currentHtmlObject.text = currentHtmlObject.text + text;
                currentHtmlObject.text = currentHtmlObject.text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_CLOSE) {
                lastHtmlObject.text = lastHtmlObject.text + text;
            }
        } else if (lastTagState == TagFlags.TAG_OPEN_CLOSE) {
            if (currentTagState == TagFlags.TAG_OPEN) {
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + text;
                currentHtmlObject.getParentObject().text = currentHtmlObject.getParentObject().text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_OPEN_CLOSE) {
                currentHtmlObject.text = currentHtmlObject.text + text;
                currentHtmlObject.text = currentHtmlObject.text + "{TagFlags.TAG}";
            } else if (currentTagState == TagFlags.TAG_CLOSE) {
                lastHtmlObject.text = lastHtmlObject.text + text;
            }
        }
    }

    public void debug() {
        System.out.println("------------------------------------------------------------");
        System.out.println(buildTagOpen());
        System.out.println(text);
        System.out.println(buildTagClose());

        for (int i = 0; i < htmlNodesList.size(); i++) {
            htmlNodesList.get(i).debug();

        }
        System.out.println("------------------------------------------------------------");
    }

    public static class HelperMethods extends helpers.HelperMethods {

    }
}