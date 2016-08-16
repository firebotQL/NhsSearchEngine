package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.AverageHolder;
import domain.Page;
import domain.ProxyAddress;
import domain.ScrapperAndIndexerConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ScrappingService {
    private Logger logger = LoggerFactory.getLogger(ScrappingService.class);

    private static List<ProxyAddress> proxies = new ArrayList();
    private static Map<ProxyAddress, AverageHolder> successList = new HashMap();

    public ScrappingService() {
        proxies.add(new ProxyAddress(null, -1));    // dummy for localhost
        proxies.add(new ProxyAddress("217.156.252.118", 8080));
        proxies.add(new ProxyAddress("185.5.64.70", 8080));
    }

    public void scrape() throws IOException, InterruptedException {
        Document doc = Jsoup.connect(ScrapperAndIndexerConstants.START_URI).get();
        final Elements pages = doc.select("#haz-mod1 a");

        ExecutorService executor = Executors.newFixedThreadPool(pages.size());
        List<Callable<Void>> todo = new ArrayList(pages.size());
        final AtomicInteger finishedJobCounter = new AtomicInteger(0);

        for(int elementIdx = 0 ; elementIdx < pages.size(); elementIdx++) {
            Element element = pages.get(elementIdx);
            final String href = element.attr("abs:href");
            final String section = href.split("=")[1];

            final int proxyIdx = elementIdx % proxies.size();

            Callable<Void> task = new Callable<Void>() {
                public Void call() throws Exception {
                    visitConditionsListPage(href, section, proxyIdx);
                    double percentageFinished = ((double)finishedJobCounter.incrementAndGet()*100)/pages.size();
                    logger.warn(String.format("Scrapped %.2f%%", percentageFinished));
                    return null;
                }
            };
            todo.add(task);
        }
        executor.invokeAll(todo);
    }

    private void visitConditionsListPage(String uri, String section, int proxyIdx) throws IOException {
        Document doc = getURIThrottled(uri, proxyIdx);
        Elements pages = doc.select("#haz-mod5 a");
        for(Element element : pages) {
            String href = element.attr("abs:href");
            visitConditionPage(href, section, proxyIdx);
        }
    }

    private void visitConditionPage(String uri, String section, int proxyIdx) throws IOException {
        Document doc = getURIThrottled(uri, proxyIdx);
        Elements categories = doc.select("#ctl00_PlaceHolderMain_articles .sub-nav a");
        for(Element element : categories) {
            String href = element.attr("abs:href");
            visitSubConditionsPage(href, section, proxyIdx);
        }
    }

    private void visitSubConditionsPage(String uri, String section, int proxyIdx) throws IOException {
        Document doc = getURIThrottled(uri, proxyIdx);
        Elements content = doc.select(".main-content");

        Page page = new Page();
        page.setUrl(doc.location());
        page.setTitle(doc.title());
        page.setContent(content.text());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = null;
        try {
            String[] splitURI = uri.split("/");
            File dir = new File(ScrapperAndIndexerConstants.CACHE_PATH + "/" + section  + "/" + page.getTitle() + "/" + splitURI[splitURI.length-3]);
            dir.mkdirs();
            File file = new File(dir, "Page.json");
            writer = new FileWriter(file);
            gson.toJson(page, writer);
        } finally {
            writer.flush();
            writer.close();
        }
        // System.out.println(gson.toJson(page));
    }

    private Document getURIThrottled(String uri, int proxyIdx) {
        Document document = null;
        long startTime = System.currentTimeMillis();
        ProxyAddress proxyAddress = proxies.get(proxyIdx);
        Proxy proxy = null;
        if (proxyAddress.getIp() != null) {
            proxy = new Proxy(
                    Proxy.Type.HTTP,
                    InetSocketAddress.createUnresolved(proxyAddress.getIp(), proxyAddress.getPort())
            );
        }
        int tryCnt = 100;
        while(tryCnt > 0) {
            try {
                if (proxy != null) {
                    document = Jsoup.connect(uri).proxy(proxy).timeout(200).get();
                } else {
                    document = Jsoup.connect(uri).timeout(200).get();
                }
                break;
            } catch (IOException e) {
                tryCnt--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
       // printSuccessList(proxyAddress, duration);
        return document;
    }

    /**
     * Use for testing/benchmarkign purpose
     * @param proxyAddress
     * @param ping
     */
    private void printSuccessList(ProxyAddress proxyAddress, Long ping) {
        AverageHolder averageHolder = successList.get(proxyAddress);
        double total = ping;
        if (averageHolder == null) {
            averageHolder = new AverageHolder();
        } else {
            total += averageHolder.getAvg() * averageHolder.getCnt();
        }
        int cnt = averageHolder.getCnt() + 1;
        averageHolder.setAvg(total/cnt);
        averageHolder.setCnt(cnt);
        successList.put(proxyAddress, averageHolder);
        for(Map.Entry<ProxyAddress, AverageHolder> listProxyAddress : successList.entrySet()) {
            ProxyAddress key = listProxyAddress.getKey();
            String ip = key.getIp() == null ? "localhost" : key.getIp();
            System.out.println(ip + ":" + key.getPort() + " Time taken: " + listProxyAddress.getValue() + "s.");
        }
    }
}
