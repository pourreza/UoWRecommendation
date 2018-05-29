package serviceWorkflowNetwork;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static utilities.Printer.print;
import static serviceWorkflowNetwork.ServiceType.*;

public class AnalyzeDataOptimized {

    public static Set<SService> services = new HashSet<SService>();
    public static Set<OOperation> operations = new HashSet<OOperation>();
    public static Set<WorkflowVersion> workflowVersions = new HashSet<WorkflowVersion>();
    public static Set<Workflow> workflows = new HashSet<Workflow>();
    public static Set<Person> people = new HashSet<Person>();

    public static Set<SORelation> soRelations = new HashSet<SORelation>();
    public static Set<OWRelation> owRelations = new HashSet<OWRelation>();

    public static Graph<String, DefaultEdge>[] directedServiceServiceGraph = new Graph[12];
    public static Graph<OOperation, DefaultEdge>[] directedOperationOperationGraph = new Graph[12];

    public static int notFoundUsers = 0;
    public static int useless = 0;

    public static void main(String[] arg) throws ParserConfigurationException {

        extractDataFromWorkflows();
//        findServicesInBiocatalogue();
//        Graph<Integer, DefaultWeightedEdge> workflowWorkflowGraph = createWorkflowWorkflowGraph();
//        Graph<String, DefaultWeightedEdge> serviceServiceGraph = createServiceServiceGraph();
//        Graph<String, DefaultEdge> workflowServiceGraph = createWorkflowServiceGraph();
    }

    public static void extractDataFromWorkflows() throws ParserConfigurationException {
        for(int i=0; i<12; i++){
            directedServiceServiceGraph[i] = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
            directedOperationOperationGraph[i] = new DefaultDirectedGraph<OOperation, DefaultEdge>(DefaultEdge.class);
        }

        File dir = new File("/Users/Maryam/MyExperimentDataset/");
        File[] directoryListing = dir.listFiles();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        for (int directoryIndex = 0; directoryIndex < directoryListing.length; directoryIndex++) {
            File child = directoryListing[directoryIndex];
            print(child.getName());
            if (child.getName().equals("214.xml")) {
                print("hi");
            }
            String worflowIndexAndVersion = child.getName().substring(0, child.getName().indexOf("."));
            String worflowIndexAndVersionNextFile = "";
            if ((directoryIndex + 1) < directoryListing.length)
                worflowIndexAndVersionNextFile = directoryListing[directoryIndex + 1].getName().substring(0, directoryListing[directoryIndex + 1].getName().indexOf("."));

            int workflowIndex = Integer.parseInt(worflowIndexAndVersion.substring(0, worflowIndexAndVersion.lastIndexOf("-")));
            int workflowIndexNextFile = 9999; // this number is used for the last file in the directory
            if ((directoryIndex + 1) < directoryListing.length)
                workflowIndexNextFile = Integer.parseInt(worflowIndexAndVersionNextFile.substring(0, worflowIndexAndVersionNextFile.lastIndexOf("-")));

            String workflowURL = "http://www.myexperiment.org/workflows/" + workflowIndex + ".html";
            int versionIndex = Integer.parseInt(worflowIndexAndVersion.substring(worflowIndexAndVersion.lastIndexOf("-") + 1));
//            if (workflowIndexNextFile != (workflowIndex)) { // this means that this version is the last version of the current workflow

                String workflowVersionURL = "http://www.myexperiment.org/workflows/" + workflowIndex + "/versions/" + versionIndex + ".html";
                Date creationDate = findDate(workflowVersionURL, true);
                Person person = findPerson(workflowVersionURL);
                String description = findDescription(workflowVersionURL);
                people.add(person);
                Workflow workflow = new Workflow(workflowURL, workflowIndex, person);
                WorkflowVersion workflowVersion = new WorkflowVersion(workflow, versionIndex, creationDate, workflowVersionURL, description);
                Date updateDate = findDate(workflowVersionURL, false);
                workflowVersion.setUpdateDate(updateDate);
                workflowVersions.add(workflowVersion);
                if(workflows.contains(workflow)){
                    for(Workflow w: workflows){
                        if(w.equals(workflow)){
                            w.addVersion(workflowVersion);
                            addContributors(w, workflowVersionURL);
                        }
                    }
                }else {
                    workflows.add(workflow);
                    workflow.addVersion(workflowVersion);
                    addContributors(workflow, workflowVersionURL);
                }

                try {
                    Document doc = dBuilder.parse(child);
                    doc.getDocumentElement().normalize();

                    boolean isXMl = false;
                    NodeList processorNode;

                    DirectedGraph<String, DefaultEdge> directedProcessorGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

                    if (child.getName().contains(".xml")) {
                        processorNode = (NodeList) xpath.compile("/scufl/processor").evaluate(doc, XPathConstants.NODESET);
                        isXMl = true;
                    } else {
                        String xpathStr = "/workflow/dataflow/processors/processor/activities/activity";
                        processorNode = (NodeList) xpath.compile(xpathStr).evaluate(doc, XPathConstants.NODESET);
                    }
                    workflowVersion.setNumberOfService(processorNode.getLength());
                    workflowVersion.setNumberOfExternalServices(retrieveExternalServices(isXMl, processorNode, workflowVersion, directedProcessorGraph));

                    //created directed graph - add edges to the graph
                    NodeList links;
                    if (child.getName().contains(".xml")) {
                        links = (NodeList) xpath.compile("/scufl/link").evaluate(doc, XPathConstants.NODESET);
                    } else {
                        links = (NodeList) xpath.compile("/workflow/dataflow/datalinks/datalink").evaluate(doc, XPathConstants.NODESET);
                    }
                    workflowVersion.setDirectedProcessorGraph(directedProcessorGraph);
                    createEdges(directedProcessorGraph, links, isXMl);
                    createEdgesForDirectedGraphs(directedProcessorGraph, workflowVersion);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
//            }
        }

        ArrayList<WorkflowVersion> workflowVersionList = new ArrayList<WorkflowVersion>(workflowVersions);
        print("number of workflows: " + workflows.size());
        print("number of workflowVersions: " + workflowVersionList.size());
        int countMore = 0;
        int countOnes = 0;
        int countTwos = 0;
        int countThrees = 0;
        int countFours = 0;
        int countFives = 0;
        int countTens = 0;
        int countMoreThan10 = 0;
        int countMoreThan100 = 0;
        int countVersions = 0;
        int countMore5 = 0;
        for (int i = 0; i < workflowVersionList.size(); i++) {
            WorkflowVersion workflowVersion = workflowVersionList.get(i);
//            if(workflowVersion.getWorkflow().getVersions().size()==workflowVersion.getVersionIndex()) {
            countVersions++;
            if (workflowVersion.getNumExternalServices() >= 1) {
                countMore++;
            }
            if (workflowVersion.getNumExternalServices() == 1)
                countOnes++;
            if (workflowVersion.getNumExternalServices() == 2)
                countTwos++;
            if (workflowVersion.getNumExternalServices() == 3)
                countThrees++;
            if (workflowVersion.getNumExternalServices() == 4)
                countFours++;
            if (workflowVersion.getNumExternalServices() == 5)
                countFives++;

            if (workflowVersion.getNumExternalServices() > 5 && workflowVersion.getNumExternalServices() < 10)
                countMore5++;
            if (workflowVersion.getNumExternalServices() == 10)
                countTens++;
            if (workflowVersion.getNumExternalServices() > 10) {
                print("More than 10 : " + workflowVersion.getNumExternalServices() + " ");
                countMoreThan10++;
            }
            if (workflowVersion.getNumExternalServices() > 100) {
                print("More than 100 : " + workflowVersion.getNumExternalServices());//+ " " + workflowVersion.getWorkflowIndex() + " versionIndex: " + workflowVersion.getVersionIndex());
                countMoreThan100++;
            }
//            }
        }

        print("All versions considered: " + countVersions);
        print("numb external services more than one: " + countMore);

        print("ones: " + countOnes);
        print("Twos: " + countTwos);
        print("Threes: " + countThrees);
        print("Fours: " + countFours);
        print("Fives: " + countFives);
        print("Tens: " + countTens);
        print("More than 5: " + countMore5);
        print("More than 10: " + countMoreThan10);
        print("More than 100: " + countMoreThan100);

        print("Number of services: " + services.size());
        print("Number of operations: " + operations.size());

        double sumVersions = 0;
        double sumServices = 0;
        double sumOperations = 0;
        double hasMoreOperations = 0;
        for(Workflow workflow: workflows){
            sumVersions+=workflow.getVersions().size();
            if(workflow.getVersions().size()>1)
                hasMoreOperations++;
            //external operations for last version
            ArrayList<OOperation> externalOperations = workflow.getVersions().get(workflow.getVersions().size() - 1).getExternalOperations();
            sumOperations+=externalOperations.size();
            Set<SService> serviceSet= new HashSet<SService>();
            for(OOperation operation: externalOperations){
                serviceSet.add(operation.getService());
            }
            sumServices+=serviceSet.size();
        }

        print(hasMoreOperations+"has more operations");

        print(people.size()+" peopless ");
        print(sumVersions/workflows.size()+" avg version per workflow ");
        print(sumOperations/workflows.size()+" avg operation per workflow ");
        print(sumServices/workflows.size()+" avg services per workflow ");

        print(minMaxDate(workflows));
        double sumWorkflows = 0;
        for(Person person: people){
            int workflowsForperson = 0;
            for(Workflow workflow: workflows){
                if(workflow.getContributors().contains(person)) {
                    workflowsForperson++;
                }
            }
            sumWorkflows+= workflowsForperson;
        }
        print(sumWorkflows/people.size()+" avg workflows per person ");

        print("not useless: "+ useless);
        print("not Found Users: "+ notFoundUsers);
    }

    private static String minMaxDate(Set<Workflow> workflows) {
        int minyear = 2018;
        int maxYear = 1800;
        for(WorkflowVersion workflow: workflowVersions){
            if(workflow.getDate().getYear()<minyear)
                minyear = workflow.getDate().getYear();
            if(workflow.getDate().getYear()>maxYear)
                maxYear = workflow.getDate().getYear();
        }
        return "Min Year: "+ minyear + " Max Year: "+ maxYear;
    }

    private static Graph<String, DefaultEdge> createWorkflowServiceGraph() {
        Graph<String, DefaultEdge> workflowServiceGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        ArrayList<OWRelation> owRelationsList = new ArrayList<OWRelation>(owRelations);
        for (int i = 0; i < owRelationsList.size(); i++) {
            String service = owRelationsList.get(i).operation.getService().getURL();
            workflowServiceGraph.addVertex(service);
            String workflow = owRelationsList.get(i).workflowVersion.getWorkflow().getIndex().toString();
            workflowServiceGraph.addVertex(workflow);
            workflowServiceGraph.addEdge(service, workflow);
        }
        return workflowServiceGraph;
    }

    private static Graph<String, DefaultWeightedEdge> createServiceServiceGraph() {
        Graph<String, DefaultWeightedEdge> serviceServiceGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        ArrayList<SService> serviceList = new ArrayList<SService>(services);
        for (int i = 0; i < serviceList.size(); i++) {
            SService service1 = serviceList.get(i);
            serviceServiceGraph.addVertex(service1.getURL());
            for (int j = i + 1; j < serviceList.size(); j++) {
                SService service2 = serviceList.get(j);
                if (!service1.equals(service2)) {
                    serviceServiceGraph.addVertex(service2.getURL());
                    Set<Integer> intersectedWorkflows = new HashSet<Integer>();
                    ArrayList<WorkflowVersion> workflowVersionsList = new ArrayList<WorkflowVersion>(workflowVersions);
                    for (int k = 0; k < workflowVersionsList.size(); k++) {
                        ArrayList<OOperation> externalOperations = workflowVersionsList.get(k).getExternalOperations();
                        ArrayList<SService> servicesInWorkflow = new ArrayList<SService>();
                        for (int kk = 0; kk < externalOperations.size(); kk++) {
                            servicesInWorkflow.add(externalOperations.get(kk).getService());
                        }
                        if (servicesInWorkflow.contains(service1) && servicesInWorkflow.contains(service2))
                            intersectedWorkflows.add(workflowVersionsList.get(k).getWorkflow().getIndex());
                    }
                    if (intersectedWorkflows.size() > 0) {
                        DefaultWeightedEdge e1 = serviceServiceGraph.addEdge(service1.getURL(), service2.getURL());
                        serviceServiceGraph.setEdgeWeight(e1, intersectedWorkflows.size());
                    }
                }
            }
        }
        return serviceServiceGraph;
    }

    private static Graph<Integer, DefaultWeightedEdge> createWorkflowWorkflowGraph() {
        Graph<Integer, DefaultWeightedEdge> workflowWorkflowGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        ArrayList<WorkflowVersion> workflowVersionsList = new ArrayList<WorkflowVersion>(workflowVersions);
        for (int i = 0; i < workflowVersionsList.size(); i++) {
            ArrayList<OOperation> externalOperations = workflowVersionsList.get(i).getExternalOperations();
            Integer workflowIndex1 = workflowVersionsList.get(i).getWorkflow().getIndex();
            workflowWorkflowGraph.addVertex(workflowIndex1);
            for (int j = i + 1; j < workflowVersionsList.size(); j++) {
                Integer workflowIndex2 = workflowVersionsList.get(j).getWorkflow().getIndex();
                if (!workflowIndex2.equals(workflowIndex1)) {
                    workflowWorkflowGraph.addVertex(workflowIndex2);
                    ArrayList<OOperation> externalOperations2 = workflowVersionsList.get(j).getExternalOperations();
                    int intersectionSize = intersectionSize(externalOperations, externalOperations2);
                    if (intersectionSize > 0) {
                        DefaultWeightedEdge e1 = workflowWorkflowGraph.addEdge(workflowIndex1, workflowIndex2);
                        if (e1 != null)
                            workflowWorkflowGraph.setEdgeWeight(e1, intersectionSize);
                        else {
                            DefaultWeightedEdge e2 = workflowWorkflowGraph.getEdge(workflowIndex1, workflowIndex2);
                            double edgeWeight = ((SimpleWeightedGraph) workflowWorkflowGraph).getEdgeWeight(e2);
                            if (edgeWeight < intersectionSize)
                                workflowWorkflowGraph.setEdgeWeight(e2, intersectionSize);
                        }
                    }
                }
            }
        }
        return workflowWorkflowGraph;
    }

    public static Graph<String,DefaultEdge>[] getDirectedServiceGraph() {
        try {
            extractDataFromWorkflows();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return directedServiceServiceGraph;
    }

    public static Set<SService> getAllServices() {
        try {
            extractDataFromWorkflows();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return services;
    }

    public static Set<WorkflowVersion> getAllWorkflowVersions() {
        try {
            extractDataFromWorkflows();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return workflowVersions;
    }

    public static Set<OOperation> getAllOperations() {
        try {
            extractDataFromWorkflows();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return operations;
    }

    class MyWeightedEdge extends DefaultWeightedEdge {

        @Override
        public String toString() {
            return Double.toString(getWeight());
        }

    }
    private static int intersectionSize(ArrayList<OOperation> externalOperations, ArrayList<OOperation> externalOperations2) {
        int intersectionSize = 0;
        for (int i = 0; i < externalOperations.size(); i++) {
            for (int j = 0; j < externalOperations2.size(); j++) {
                if (!externalOperations.get(i).equals(externalOperations2.get(j)))
                    if (externalOperations.get(i).getService().getURL().equals(externalOperations2.get(j).getService().getURL())) {
                        intersectionSize++;
                        break;
                    }
            }
        }
        return intersectionSize;
    }

    private static void createEdgesForDirectedGraphs(Graph<String, DefaultEdge> directedProcessorGraph, WorkflowVersion workflowVersion) {
        for (int i = 0; i < workflowVersion.getExternalOperations().size(); i++) {
            OOperation source = workflowVersion.getExternalOperations().get(i);
            for (int j = 0; j < workflowVersion.getExternalOperations().size(); j++) {
                if (!workflowVersion.getExternalOperations().get(j).equals(workflowVersion.getExternalOperations().get(i))) {
                    OOperation sink = workflowVersion.getExternalOperations().get(j);
                    GraphPath<String, DefaultEdge> path = DijkstraShortestPath.findPathBetween(directedProcessorGraph, source.getProcessorName(), sink.getProcessorName());
                    if (path != null) {
                        directedServiceServiceGraph[yearIndex(workflowVersion)].addEdge(source.getService().getURL(), sink.getService().getURL());
                        directedOperationOperationGraph[yearIndex(workflowVersion)].addEdge(source, sink);
                    }
                }
            }
        }
    }

    private static void createEdges(Graph<String, DefaultEdge> directedProcessorGraph, NodeList links, boolean isXML) {
        ///////for every link
        for (int i = 0; i < links.getLength(); i++) {
            String sink = null;
            String source = null;
            if (isXML) {
                String sinkStr = links.item(i).getAttributes().getNamedItem("sink").getTextContent().trim();
                if (sinkStr.contains(":"))
                    sink = sinkStr.substring(0, sinkStr.lastIndexOf(":"));
                else
                    sink = sinkStr;
                String sourceStr = links.item(i).getAttributes().getNamedItem("source").getTextContent().trim();
                if (sourceStr.contains(":"))
                    source = sourceStr.substring(0, sourceStr.lastIndexOf(":"));
                else
                    source = sourceStr;

            } else {
                NodeList childNodes = links.item(i).getChildNodes();
                //////// for every element in the activity
                for (int j = 0; j < childNodes.getLength(); j++) {
                    NodeList grandChildren = childNodes.item(j).getChildNodes();
                    for (int k = 0; k < grandChildren.getLength(); k++) {
                        if (grandChildren.item(k).getNodeName().equals("processor")) {
                            if (childNodes.item(j).getNodeName().equals("sink")) {
                                sink = grandChildren.item(k).getTextContent().trim();
                            } else if (childNodes.item(j).getNodeName().equals("source")) {
                                source = grandChildren.item(k).getTextContent().trim();
                            }
                        }
                    }
                }
            }
            if (directedProcessorGraph.containsVertex(source) && directedProcessorGraph.containsVertex(sink))
                directedProcessorGraph.addEdge(source, sink);
        }
    }

    private static void findServicesInBiocatalogue() {
        ArrayList<OOperation> operationList = new ArrayList<OOperation>(operations);
        int countFoundServices = 0;
        int operationNumber = 0;
        for (OOperation operation : operationList) {
            print("service: " + (++operationNumber));
            if (findServiceName(operation.getService().getURL())) {
                print("Found URL: " + operation.toString());
                countFoundServices++;
            } else if (operation.getService().getType().equals(SOAPLAB)) {
                if (findServiceName(operation.getName())) {
                    countFoundServices++;
                    print("Found Name: " + operation.toString());
                }
            }
        }
        print("Found Operations in Biocatalogue: " + countFoundServices);
    }

    private static boolean findServiceName(String serviceName) {
        String url = "https://www.biocatalogue.org/search?utf8=✓&q=" + serviceName;
        try {
            org.jsoup.nodes.Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10 * 1000000).get();
            Elements searchResult = document.select("div[id=search_results]");
            if (searchResult.size() > 0)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Person findPerson(String workflowURL) {
        String csvFile = "feedmeWithVersions";
        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                String[] data2 = line.split("\"");
                if (workflowURL.equals(data2[1])) {
                    int id = Integer.parseInt(data2[11].substring(data2[11].lastIndexOf("/") + 1));
                    String name = data2[13];
                    return new Person(name, id);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        print("Person NOT FOUNDDDDDDDDDDDDDDDDDDDDDD");
        return null;
    }

    private static String findDescription(String workflowURL) {
        String csvFile = "feedmeWithVersions";
        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                String[] data2 = line.split("\"");
                if (workflowURL.equals(data2[1])) {
                    if(data2.length>21) {
                        String description = data2[21];
                        return description;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void addContributors(Workflow workflow, String workflowVersionURL) {
        String csvFile = "feedmeWithVersions";
        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                String[] data2 = line.split("\"");
                //Creation date
                if (workflowVersionURL.equals(data2[1])) {
                    //update date
                    if (!data2[17].equals("")) {
                        String[] ids = data2[17].split(",");
                        String[] names = data2[19].split(",");
                        for(int i=0; i<ids.length; i++){
                            int userID = Integer.parseInt(ids[i].substring(ids[i].lastIndexOf("/") + 1));
                            String name = names[i];
                            Person person = new Person(name, userID);
                            if(workflow.getContributors().contains(person) && ids.length==1)
                                useless++;
                            workflow.addContributor(person);
                            people.add(person);
                        }
                    }else
                        notFoundUsers++;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Date findDate(String workflowURL, boolean isCreation) {
        String csvFile = "feedmeWithVersions";
        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                String[] data2 = line.split("\"");
                //Creation date
                if (workflowURL.equals(data2[1])) {
                    //update date
                    String date = data2[5];

                    if (!isCreation) {
                        date = data2[7];
                        if (date == null || date.trim().equals(""))
                            date = data2[5].trim();
                    }

                    int year = Integer.parseInt(date.substring(0, 4));
                    int month = Integer.parseInt(date.substring(5, 7));
                    int days = Integer.parseInt(date.substring(8, 10));
                    return new Date(year, month, days);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        print("NOT FOUUUUUUUUUUUUUUUUUUUUUUUUD DATE");
        return null;
    }

    private static int retrieveExternalServices(boolean isXMl, NodeList processorNode, WorkflowVersion workflowVersion, Graph<String, DefaultEdge> directedProcessorGraph) {
        int numExternalServices = 0;
        ///////for every processor
        for (int i = 0; i < processorNode.getLength(); i++) {
            String serviceTypeStr = null;
            String processorName = null;
            NodeList childNodes = processorNode.item(i).getChildNodes();
            if (isXMl) {
                processorName = processorNode.item(i).getAttributes().getNamedItem("name").getTextContent();
                directedProcessorGraph.addVertex(processorName);
            } else {
                processorName = findProcessorName(processorNode.item(i));
                directedProcessorGraph.addVertex(processorName);
            }
            workflowVersion.addProcessorNames(processorName);
            //////// for every element in the activity
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (isXMl) {
                    serviceTypeStr = childNodes.item(j).getNodeName();
                } else {
                    ///// if the inside element is raven then it includes the artifact which means the type of service
                    if (childNodes.item(j).getNodeName().equals("raven")) {
                        NodeList childNodes1 = childNodes.item(j).getChildNodes();
                        for (int k = 0; k < childNodes1.getLength(); k++) {
                            if (childNodes1.item(k).getNodeName().equals("artifact")) {
                                serviceTypeStr = childNodes1.item(k).getTextContent().trim().toLowerCase();
                            }
                        }
                    }
                }
                if (childNodes.item(j) != null) {
                    NodeList insideNodes = childNodes.item(j).getChildNodes();
                    numExternalServices = digForURL(workflowVersion, numExternalServices, serviceTypeStr, insideNodes, processorName);
//                    print(childNodes.item(j).getNodeName());
                }
                if (childNodes.item(j).getChildNodes().item(0) != null) {
                    NodeList insideNodes = childNodes.item(j).getChildNodes().item(0).getChildNodes();
                    numExternalServices = digForURL(workflowVersion, numExternalServices, serviceTypeStr, insideNodes, processorName);
//                    print(childNodes.item(j).getChildNodes().item(0).getNodeName());
                }
            }

        }
        return numExternalServices;
    }

    private static String findProcessorName(Node grandchildNode) {
        Node processorNode = grandchildNode.getParentNode().getParentNode();
        NodeList childrenNodes = processorNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            if (childrenNodes.item(i).getNodeName().equals("name")) {
                return childrenNodes.item(i).getTextContent();
            }
        }
        return null;
    }

    private static int digForURL(WorkflowVersion workflowVersion, int numExternalServices, String serviceTypeStr, NodeList insideNodes, String processorName) {
        for (int k = 0; k < insideNodes.getLength(); k++) {
            Node URLnode = insideNodes.item(k);
            if (URLnode.getTextContent().startsWith("http://") || URLnode.getTextContent().startsWith("https://")) {
//                print("&&&&&&&&&&&&&&&&&&&&&&&&"+URLnode.getNodeName());
                ServiceType serviceType = findServiceType(serviceTypeStr);
                if (serviceType != null)
                    if (createOperationService(serviceType, processorName, k, URLnode, workflowVersion)) {
                        numExternalServices++;
                        break;
                    }
            }
        }
        return numExternalServices;
    }

    private static ServiceType findServiceType(String serviceTypeStr) {
        serviceTypeStr = serviceTypeStr.toLowerCase().trim();
        if (serviceTypeStr.contains("soaplab"))
            return SOAPLAB;
        if (serviceTypeStr.contains("biomoby"))
            return BIOMOBY;
        if (serviceTypeStr.contains("rest"))
            return REST;
        if (serviceTypeStr.contains("sadi"))
            return SADI;
        if (serviceTypeStr.contains("arbitrarygt4"))
            return ARBITRARYGT4;
        if (serviceTypeStr.contains("wsdl"))
            return WSDL;
        return null;
    }

    private static boolean createOperationService(ServiceType serviceType, String processorName, int k, Node URLnode, WorkflowVersion workflowVersion) {
        String serviceURL = URLnode.getTextContent();
        String operationName = null;

        if (serviceType.equals(WSDL) || serviceType.equals(BIOMOBY) || serviceType.equals(ARBITRARYGT4)) {
            operationName = URLnode.getParentNode().getChildNodes().item(k + 2).getTextContent();
        } else if (serviceType.equals(SOAPLAB)) {
            operationName = serviceURL.substring(serviceURL.lastIndexOf(".") + 1);
            serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("."));
        } else if (serviceType.equals(REST)) {
            String initialURL = serviceURL;
            boolean hasSpecialMark = false;
            if (serviceURL.lastIndexOf("?") != -1) {
                serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("?"));
                hasSpecialMark = true;
            }
            if (serviceURL.indexOf("{") != -1) {
                serviceURL = serviceURL.substring(0, serviceURL.indexOf("{"));
                hasSpecialMark = true;
            }
            if (serviceURL.charAt(serviceURL.length() - 1) == '/')
                serviceURL = serviceURL.substring(0, serviceURL.length() - 1);

            if (hasSpecialMark && serviceURL.length() > 7 && serviceURL.substring(7).lastIndexOf("/") == serviceURL.substring(7).indexOf("/")) {// this means that we have ? or { and before these marks and after http:// there is NO more than one change of directory with more than one slash
                operationName = serviceURL;
            } else {
                operationName = serviceURL.substring(serviceURL.lastIndexOf("/") + 1);
                serviceURL = serviceURL.substring(0, serviceURL.lastIndexOf("/"));
                if (operationName.trim().equals("")) {
                    print("Initial URL: " + initialURL);
                    print("Operation Name: " + operationName);
                    print("Service URL: " + serviceURL);
                }
            }
        } else if (serviceType.equals(SADI)) {
            operationName = serviceURL.substring(serviceURL.lastIndexOf("/") + 1);
        }

        if (createNetworkNodesRelations(serviceType, processorName, workflowVersion, serviceURL, operationName))
            return true;
        return false;
    }

    private static boolean createNetworkNodesRelations(ServiceType serviceType, String processorName, WorkflowVersion workflowVersion, String serviceURL, String operationName) {
        if (operationName != null && !operationName.equals("") && serviceURL != null) {
            SService service = new SService(serviceURL, serviceType);
            services.add(service);
            directedServiceServiceGraph[yearIndex(workflowVersion)].addVertex(serviceURL);
            OOperation operation = new OOperation(service, operationName, processorName);
            operations.add(operation);
            directedOperationOperationGraph[yearIndex(workflowVersion)].addVertex(operation);

            workflowVersion.addExternalOperation(operation);

            SORelation soRelation = new SORelation(operation, service, workflowVersion.getUpdateDate());
            soRelations.add(soRelation);
            OWRelation owRelation = new OWRelation(operation, workflowVersion, workflowVersion.getUpdateDate());
            owRelations.add(owRelation);
            return true;
        }
        return false;
    }

    private static int yearIndex(WorkflowVersion workflowVersion) {
        return workflowVersion.getDate().getYear()-2007;
    }

}
