package serviceWorkflowNetwork;

import org.w3c.dom.Node;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import static utilities.Printer.print;

public class AnalyzeData {

    public ArrayList<SService> services = new ArrayList<SService>();
    public ArrayList<OOperation> operations = new ArrayList<OOperation>();
    public ArrayList<WorkflowVersion> workflowVersions = new ArrayList<WorkflowVersion>();
    public ArrayList<String> people = new ArrayList<String>();

    public ArrayList<SORelation> soRelations = new ArrayList<SORelation>();
    public ArrayList<OWRelation> owRelations = new ArrayList<OWRelation>();

    public static void main(String[] arg) {

//        File dir = new File("/Users/Maryam/MyExperimentDataset/");
//        File[] directoryListing = dir.listFiles();
//        if (directoryListing != null) {
//            for (File child : directoryListing) {
//                String workflowURL = "https://www.myexperiment.org/workflows/" + child.getName().substring(0, child.getName().indexOf(".")) + "html";
//                try {
//                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//                    Document doc = dBuilder.parse(child);
//                    doc.getDocumentElement().normalize();
//
//                    XPathFactory xPathfactory = XPathFactory.newInstance();
//                    XPath xpath = xPathfactory.newXPath();
//
//                    print(child.getName());
//
//                    String serviceType = null;
//                    String serviceURL;
//                    String operationName;
//
//                    NodeList processorNode;
//                    if (child.getName().contains(".xml")) {
//                        processorNode = (NodeList) xpath.compile("/scufl/processor").evaluate(doc, XPathConstants.NODESET);
////                        NodeList test = (NodeList) xpath.compile("/scufl/processor").evaluate(doc, XPathConstants.NODESET);
////                        for (int i = 0; i < test.getLength(); i++) {
////                            NodeList childNodes = test.item(i).getChildNodes();
////                            //////// for every element in the processor
////                            for (int j = 0; j < childNodes.getLength(); j++) {
////                                NodeList children = childNodes.item(j).getChildNodes();
//////                                if ( !childNodes.item(j).getNodeName().equals("s:stringconstant") &&!childNodes.item(j).getNodeName().equals("s:soaplabwsdl") && (childNodes.item(j).getTextContent().startsWith("http://") || childNodes.item(j).getTextContent().startsWith("https://"))) {
//////                                    print("&&&&&&&&&&&&&&&&&&&" + childNodes.item(j).getNodeName());
//////                                    print(test.item(i).getNodeName());
//////                                }
////                                for (int k = 0; k < children.getLength(); k++) {
////                                    NodeList grandchildren = children.item(k).getChildNodes();
////
////                                    for(int kk=0; kk<grandchildren.getLength(); kk++)
////                                        if ( (grandchildren.item(kk).getTextContent().startsWith("http://") || grandchildren.item(kk).getTextContent().startsWith("https://"))) {
//////                                            print("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+grandchildren.item(kk).getNodeName() );
//////                                            print(children.item(k).getNodeName());
////                                        }
//////                                    if (children.item(k).getTextContent().startsWith("http://") || children.item(k).getTextContent().startsWith("https://")) {
//////                                        print("&&&&&&&&&&&&&&&&&&&" + children.item(k).getNodeName());
//////                                        print(childNodes.item(j).getNodeName());
//////                                    }
//////
////                                }
////                            }
////                        }
//
//                    } else {
//                        String xpathStr = "/workflowVersion/dataflow/processors/processor/activities/activity";//"/configBean";
//                        processorNode = (NodeList) xpath.compile(xpathStr).evaluate(doc, XPathConstants.NODESET);
//                    }
//                    ///////for every processor
//                    for (int i = 0; i < processorNode.getLength(); i++) {
//                        NodeList childNodes = processorNode.item(i).getChildNodes();
//                        //////// for every element in the activity
//                        for (int j = 0; j < childNodes.getLength(); j++) {
//                            ///// if the inside element is raven then it includes the artifact which means the type of service
//                            if (childNodes.item(j).getNodeName().equals("raven")) {
//                                NodeList childNodes1 = childNodes.item(j).getChildNodes();
//                                for (int k = 0; k < childNodes1.getLength(); k++) {
//                                    if (childNodes1.item(k).getNodeName().equals("artifact")) {
//                                        serviceType = childNodes1.item(k).getTextContent().trim().toLowerCase();
//                                    }
//                                }
//                                if (serviceType.equals("stringconstant-activity"))
//                                    break;
//                                ////////////////// else if the inside element is configBean then we can check if the inside element has http to some external service
//                            }
//                        }
////                        if (childNodes.item(j).getNodeName().equals("configBean")) {
////                            NodeList insideNodes = childNodes.item(j).getChildNodes().item(0).getChildNodes();
////                            for (int k = 0; k < insideNodes.getLength(); k++) {
////                                Node URLnode = insideNodes.item(k);
////                                if (URLnode.getTextContent().startsWith("http://") || URLnode.getTextContent().startsWith("https://")) {
////                                    createOperationService(serviceType, k, URLnode);
////                                }
////                            }
////                        }
//                    }
//
//
////                    for (int i = 0; i < wsdloperationNode.getLength(); i++) {
////                        String operationName = wsdloperationNode.item(i).getTextContent();
////                        String serviceURL = wsdlurlNode.item(i).getTextContent();
////
////                        SService sservice = new SService(serviceURL, ServiceType.arbitrarywsdl);
////
////                        OOperation operation = new OOperation(workflowURL, serviceURL, operationName);
////
////                        if(child.getName().contains(".t2flow")){
////                            print(sservice.toString());
////                            print(operation.toString());
////                        }
////                    }
//                } catch (ParserConfigurationException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (XPathExpressionException e) {
//                    e.printStackTrace();
//                } catch (SAXException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        downloadXMLs();
        //        analyzeData();

    }

    private static void createOperationService(String serviceType, int k, Node URLnode) {
        String serviceURL;
        String operationName;
        serviceURL = URLnode.getTextContent();
        if (serviceType.equals("WSDL-activity") || serviceType.equals("BIOMOBY-activity")) {
            operationName = URLnode.getParentNode().getChildNodes().item(k + 2).getTextContent();
        } else if (serviceType.equals("SOAPLAB-activity")) {
            operationName = serviceURL.substring(serviceURL.lastIndexOf(".") + 1);
            serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("."));
        } else if (serviceType.equals("REST-activity")) {
            String initialURL = serviceURL;
            if (serviceURL.lastIndexOf("?") != -1) {
                serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("?"));
            }
            if (serviceURL.indexOf("{") != -1) {
                serviceURL = serviceURL.substring(0, serviceURL.indexOf("{"));
            }
            if (serviceURL.charAt(serviceURL.length() - 1) == '/')
                serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
            operationName = serviceURL.substring(serviceURL.lastIndexOf("/") + 1);
            serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("/"));
            if (operationName.trim().equals("")) {
                print("Initial URL: " + initialURL);
                print("Operation Name: " + operationName);
                print("Service URL: " + serviceURL);
                print(serviceType);
            }
        }
    }

    private static void downloadXMLs() {
        String csvFile = "feedmeWithVersionsAll";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));

            line = br.readLine();

            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                print(lineNumber + "");
                if(lineNumber>83) {
                    // use comma as separator
                    String[] data2 = line.split(",");
                    String workflowIndex = data2[0].substring(38, data2[0].indexOf("/versions"));
                    String versionIndex = data2[0].substring(data2[0].lastIndexOf("/") + 1, data2[0].lastIndexOf(".html"));
                    String url = data2[4];
                    print(url);
                    if (!url.trim().equals("")) {
                        if (url.contains(".xml?"))
                            downloadUsingNIO(url, workflowIndex + "-" + versionIndex + ".xml");
                        else if (url.contains(".t2flow?"))
                            downloadUsingNIO(url, workflowIndex + "-" + versionIndex + ".t2flow");
                        else
                            print("NOT FOUND" + url);
//                    downloadUsingNIO(url, "/Users/Maryam/MyExperimentDataset/"+count+".xml");
                    }
                }
                lineNumber++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static void analyzeData() {
        String csvFile = "feedme";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));

            int countAtleast1 = 0;
            int countOnes = 0;
            int count2 = 0;
            int count3 = 0;
            int count4 = 0;
            int count5 = 0;
            int count10 = 0;
            line = br.readLine();

            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] data2 = line.split("\"");
                String[] data = data2[8].split(",");
//                for(int i=6; i<data.length-1; i++)
//                    print(data[i]);
//                print(lineNumber+"");
//                print("$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//                print(data2[1]+"-------"+data2[3]+"-------");
                if (data.length > 2) {
//                    print(data[2]);
                    int externalServicesNum = Integer.parseInt(data[2].trim());

                    if (externalServicesNum > 0)
                        countAtleast1++;

                    if (externalServicesNum == 1)
                        countOnes++;
                    else if (externalServicesNum == 2)
                        count2++;
                    else if (externalServicesNum == 3)
                        count3++;
                    else if (externalServicesNum == 4)
                        count4++;
                    else if (externalServicesNum == 5)
                        count5++;
                    else if (externalServicesNum == 10)
                        count10++;
                }
                lineNumber++;

            }

            print("count 1: " + countOnes + "");
            print("count 2: " + count2 + "");
            print("count 3: " + count3 + "");
            print("count 4: " + count4 + "");
            print("count 5: " + count5 + "");
            print("count 10: " + count10 + "");

            print("count at least one: " + countAtleast1);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
