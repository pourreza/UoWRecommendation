package utilities;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;

import static utilities.Printer.*;

public class Crawler {

    public static void main(String[] args) throws IOException {
//        testURLConnection("https://dl.acm.org/citation.cfm?doid=2365316.2365320");
        createJSONFile();
//        getAbstracts();
////        getListOfIEEEConferencesJournals();
//        Set<String> notFoundArticles = new HashSet<String>();
//        ArrayList<String> allLinks = new ArrayList<String>();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(new File("LinkToPapers5.txt")));
//            String line;
//            int count =0;
//            while ((line = br.readLine()) != null) {
//                allLinks.add(line);
//                if(line.contains("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"))
//                    notFoundArticles.add(line.substring("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&".length()));
//            }
//            print("all not solved: "+notFoundArticles.size());
//            print("all: "+allLinks.size());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        ArrayList<JSONObject> jsons = JSONReader.readJsons("SOCpapersFinal2.txt");
////        ArrayList<String> linksToPapers = new ArrayList<String>();
////        int paperNum = 0;
//        int foundCount =0;
////        int techCount = 0;
////        int foundTech =0;
////        int numPingGoogle= 0;
////        int countSoftware = 0;
////        int countAllSoftware = 0;
////        int countTechnology = 0;
////        int allTechnoloty = 0;
////        int hasURL = 0;
//        int countPing = 0;
//        ArrayList<String> pingedTitles = new ArrayList<String>();
//        for(JSONObject jsonObject: jsons) {
//            String title = (String) jsonObject.get("title");
//            JSONArray urls = (JSONArray) jsonObject.get("url");
//            String venue = (String) jsonObject.get("venue");
//            if (countPing<80 && notFoundArticles.contains(title)) {
//                String url = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C5&q=" + title;
//                try {
//                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10 * 1000000).get();
//                    pingedTitles.add(title);
//                    Elements searchResult = document.select("div[class=gs_ri]");
//                    boolean found = false;
//                    for(int i=0; i<4; i++){
//                        if(searchResult.size()<=i)
//                            break;
//                        String ref = searchResult.get(i).select("a").attr("href");
//                        if(searchResult.size()<4 && ref.contains("ieeexplore.ieee.org")|| ref.contains("dx.doi.org")|| ref.contains("thinkmind.org")|| ref.contains("sciencedirect.com") || ref.contains("dl.acm.org") || ref.contains("link.springer.com")|| ref.contains("en.cnki.com.cn")) {
//                            String notFoundTitle = "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + title;
//                            int indexOfNotFound = allLinks.indexOf(notFoundTitle);
//                            if(indexOfNotFound>=0){
//                                allLinks.set(indexOfNotFound, ref);
//                                foundCount++;
//                            }
//                            found = true;
//                            break;
//                        }
//                    }
//                    if(!found){
//                        print("not Found paper: "+title );
//                    }
//                    countPing++;
//                }catch (Exception e){
//                    countPing++;
//                    e.printStackTrace();
//                }
//            }
//        }
//        print("found added: "+foundCount);
//                if(venue.toLowerCase().equals("ieee software"))
//                    countSoftware++;
//                else if(venue.toLowerCase().equals("ieee international conference on cloud computing technology and science")){
//                    countTechnology++;
//                }
//                else
//                    print(venue);
//                if (urls != null && urls.size() > 0) {
//                    for (int i = 0; i < urls.size(); i++) {
//                        String url = (String) urls.get(i);
//                        if (url.contains("en.cnki.com.cn")) {
//                            String notFoundTitle = "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + title;
//                            int indexOfNotFound = allLinks.indexOf(notFoundTitle);
//                            if(indexOfNotFound>=0)
//                                allLinks.set(indexOfNotFound, url);
//                            hasURL++;
//                            print(urls.toString()+" : "+title);
//                            break;
//                        }
//////                        linksToPapers.add(url);
//////                        found = true;
//////                        foundCount++;
//////                        print("found: " + paperNum);
//////                        break;
//////                    }
//////                }
//                    }
//                }
////                    print(urls.toString());
//            }
//            if(venue.toLowerCase().equals("ieee software")){
//                countAllSoftware++;
//            }
//            else if(venue.toLowerCase().equals("ieee international conference on cloud computing technology and science"))
//                allTechnoloty++;
//        }
//        print("all software "+countAllSoftware);
//        print("not found software "+countSoftware);
//        print("all technology "+allTechnoloty);
//        print("not found technology "+countTechnology);
//        print("has urls "+hasURL);

//            boolean found = false;
//            if(urls!=null) {
//                for (int i = 0; i < urls.size(); i++) {
//                    String url = (String) urls.get(i);
//                    if (url.contains("ieeexplore.ieee.org")|| url.contains("dx.doi.org")|| url.contains("thinkmind.org")|| url.contains("sciencedirect.com")) {
//                        linksToPapers.add(url);
//                        found = true;
//                        foundCount++;
//                        print("found: " + paperNum);
//                        break;
//                    }
//                }
//            }
//            if(found){
//                if(venue.contains("ieee international conference on cloud computing technology and science")){
//                    foundTech++;
//                }
//            }
//            if(!found){
//                String url = "https://scholar.google.com/scholar?hl=en&as_sdt=0%2C5&q="+title;
//                try {
//                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10 * 1000000).get();
//                    Elements searchResult = document.select("div[class=gs_ri]");
//                    String ref = searchResult.get(0).select("a").attr("href");
//                    if(ref.contains("ieeexplore.ieee.org")|| ref.contains("dx.doi.org")|| ref.contains("thinkmind.org")|| ref.contains("sciencedirect.com")) {
//                        linksToPapers.add(url);
//                        found = true;
//                        foundCount++;
//                        print("found: " + paperNum);
//                    }
//                    else{
//                        if(venue.contains("ieee international conference on cloud computing technology and science")){
//                            techCount++;
//                        }
//                        linksToPapers.add("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + title);
//                    }
//                    numPingGoogle++;
//                }catch (Exception e){
//                    numPingGoogle++;
//                    e.printStackTrace();
//                    if(venue.contains("ieee international conference on cloud computing technology and science")){
//                        techCount++;
//                    }
//                    linksToPapers.add("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + title);
//                }
//            }
//            paperNum++;
//            if(numPingGoogle>1000)
//                break;
////            if(!found) {
////                String url = "http://ieeexplore.ieee.org/search/searchresult.jsp?newsearch=true&queryText=" + title;
////                try {
////                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10*1000000).get();
////                    Elements referenecs = document.select("div[class=col-22-24]");
////                    int numRetry = 0;
////                    while(referenecs.size()==0){
////                        document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10*1000000).get();
////                        referenecs = document.select("div[class=col-22-24]");
////                        numRetry++;
////                        if(numRetry>10)
////                            break;
////                    }
////                    boolean ieeeFound = false;
////                    print(referenecs.size()+"got results");
////                    for(int i=0; i< referenecs.size(); i++){
////                        Elements ref = referenecs.get(0).select("a");
////                        if(ref.get(0).text().equals(title)){
////                            String link = ref.get(0).attr("href");
////                            linksToPapers.add(link);
////                            print(paperNum++ + "");
////                            ieeeFound = true;
////                            break;
////                        }
////                    }
////                    if(!ieeeFound){
////                        linksToPapers.add("NULL");
////                        print("NOT FOUND" + paperNum++ + "");
////                    }
////                } catch (Exception e) {
////                    e.printStackTrace();
////                    linksToPapers.add(title);
////                    print(paperNum++ + "");
////                }
////            }
//        }
//        print(paperNum+" paper num");
//        print(numPingGoogle+" google ping num");
//        print(foundCount+"found");
//        print(techCount+"tech");
//        print(foundTech+"found tech");
//        // in neveshtan tu file xub nist chon man ye seri null negah midaram ham in tabdil be set mikone ham alefbaayi minevise
//        printListInFile(linksToPapers, "LinkToPapers1.txt");
//        printListInFile(allLinks, "LinkToPapers6.txt");
//        printListInFile(pingedTitles, "PingedTitles.txt");
    }
    private static void createJSONFile(){
        ArrayList<JSONObject> jsonLinks = new ArrayList<JSONObject>();
        ArrayList<String> allLinks = getAllLinks();
        ArrayList<JSONObject> jsons = JSONReader.readJsons("SOCpapersFinal2.txt");
        int index=0;
        print("all jsons: "+jsons.size());
        print("all links: "+allLinks.size());
        for(JSONObject jsonObject: jsons) {
            String title = (String) jsonObject.get("title");
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("link", allLinks.get(index));
            jsonLinks.add(json);
            index++;
        }
        printJSONListInFile(jsonLinks, "PaperLinks.txt");
    }

    private static void getAbstracts(){
        ArrayList<String> allLinks = getAllFoundLinks();
        ArrayList<String> abstracts = new ArrayList<String>();
        ArrayList<String> notFoundLinks = new ArrayList<String>();
        for(String link: allLinks){
            if(!link.contains("https")) {
                link = "https" + link.substring(4);
            }
            String papserAbstract = null;
            Document document = null;
            boolean found = false;
            print(link);
            try {
                document = Jsoup.connect(link).userAgent("Mozilla/5.0").timeout(1000000).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(document!=null) {
                if (link.contains("ieeexplore.ieee.org") || link.contains("dx.doi.org")) {
                    papserAbstract = document.select("div[class=abstract-text ng-binding]").text();
                } else if (link.contains("thinkmind.org")) {
                    papserAbstract = document.select("p[class=a3]").get(3).text();
                    papserAbstract = papserAbstract.substring(papserAbstract.indexOf("<br>") + 4);
                } else if (link.contains("sciencedirect.com")) {
                    Elements abstractElement = document.select("div[class=abstract svAbstract ]");
                    if (abstractElement == null || abstractElement.size() < 1) {
                        abstractElement = document.select("div[class=abstract author]");
                    }
                    if (abstractElement != null && abstractElement.size() > 0) {
                        papserAbstract = abstractElement.get(0).select("p").get(0).text();
                        for (int i = 1; i < abstractElement.size(); i++) {
                            papserAbstract += abstractElement.get(i).text();
                        }
                    }
                } else if (link.contains("dl.acm.org")) {
//                    papserAbstract = document.select("div[class=cf_layoutareaabstract]").get(0).select("p").get(0).text();
                } else if (link.contains("en.cnki.com.cn")) {
                    papserAbstract = document.select("div[id=content]").get(0).select("div[style=text-indent:2em;text-align:left;word-break:break-all;line-height:20px;]").get(0).text();
                } else if (link.contains("link.springer.com")) {
                    Elements abstractElement = document.select("section[class=Abstract]").get(0).select("p");
                    if (abstractElement != null && abstractElement.size() > 0) {
                        papserAbstract = abstractElement.get(0).text();
                        for (int i = 1; i < abstractElement.size(); i++) {
                            papserAbstract += abstractElement.get(i).text();
                        }
                    }
                } else if (link.contains("search.proquest.com")) {
                    papserAbstract = document.select("table").get(0).select("p").get(0).text();
                } else if (link.contains("elar.urfu.ru")) {
                    papserAbstract = document.select("table").get(0).select("tr").get(5).select("td").get(1).text();
                } else if (link.contains("arxiv.org/abs")) {
                    papserAbstract = document.select("blockquote[class=abstract mathjax]").get(0).select("p").get(0).text();
                    papserAbstract = papserAbstract.substring(papserAbstract.indexOf("</span>") + 7);
                } else if (link.contains("iris.unicampania.it")) {
                    papserAbstract = document.select("table").get(0).select("tr").get(3).select("td").get(1).text();
                } else if (link.contains("technical.cloud-journals.com")) {
                    papserAbstract = document.select("div[class=articleAbstract]").get(0).select("p").get(0).text();
                } else if (link.contains("research.utwente.nl")) {
                    papserAbstract = document.select("div[class=textblock]").get(0).text();
                } else if (link.contains("atlantis-press.com")) {
                    papserAbstract = document.select("dd[class=src-components-Article-detailDefinition]").get(4).select("div").get(0).text();
                } else if (link.contains("digital-library.theiet.org")) {
                    papserAbstract = document.select("p[class=description]").get(0).text();
                }
            }
            if(papserAbstract!=null) {
                found = true;
                abstracts.add(papserAbstract+"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }
            if(!found)
                notFoundLinks.add(link);
        }
        print("number of abstracts: "+ abstracts.size());
        print("number of notfounds: "+ notFoundLinks.size());
        printListInFile(abstracts, "AllAbstracts.txt");
        printListInFile(notFoundLinks, "notFoundLinks.txt");
    }

    private static ArrayList<String> getAllFoundLinks() {
        ArrayList<String> allLinks = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("LinkToPapers5.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.contains("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&")) {
                    allLinks.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allLinks;
    }
    private static ArrayList<String> getAllLinks() {
        ArrayList<String> allLinks = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("LinkToPapers5.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                allLinks.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allLinks;
    }

    private static void testURLConnection(String url){
        try {
            String text = null;
            Document document = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100 * 1000000).get();
            print("found document");
            Elements elements = document.select("div[class=large-text]");
            if(elements.size()>0){
                elements = elements.get(0).select("p");
                if(elements.size()>0){
                    text = elements.get(0).text();
                }else
                    print("No p found");
            }else
                print("No div found");
            print(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getListOfIEEEConferencesJournals() {
        String outputPathName = "ListOfIEEEConferencesJournalssss.txt";
        try {
            PrintWriter writer = new PrintWriter(outputPathName);
            for(char i='a'; i<='z'; i++){
                print(i+" ");
                String url = "https://www.computer.org/csdl/proceedings/"+i+"/list.html";
                Document doc = Jsoup.connect(url).timeout(10*1000000).get();
                Elements text = doc.select("div[class=proceedingNameLabel]");
                for(int j=0; j<text.size(); j++){
                    writer.println(text.get(j).text());
                }
            }
            for(int i=0; i<10; i++){
                print(i+" ");
                String url = "https://www.computer.org/csdl/proceedings/"+i+"/list.html";
                Document doc = Jsoup.connect(url).get();
                Elements text = doc.select("div[class=proceedingNameLabel]");
                for(int j=0; j<text.size(); j++){
                    writer.println(text.get(j).text());
                }
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
