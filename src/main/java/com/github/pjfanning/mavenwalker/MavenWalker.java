package com.github.pjfanning.mavenwalker;

import com.vdurmont.semver4j.Semver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MavenWalker {

    private final static String BASE_URL = "https://repo1.maven.org/maven2/org/apache/";

    public static void main(final String[] args) {
        try {
            walkDirs(BASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void walkDirs(final String url) {
        try {
            final List<String> links = getLinks(url);
            final String pomUrl = getPomUrl(links);
            if (pomUrl != null) {
                try {
                    final byte[] pomData = getBytes(pomUrl);
                    try (UnsynchronizedByteArrayInputStream bis = new UnsynchronizedByteArrayInputStream(pomData)) {
                        final String projectText = PomUtils.evalLog4jv1Dependency(bis);
                        if (projectText != null) {
                            System.out.println(pomUrl + " - " + projectText);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Problem reading pom " + pomUrl + " - " + e.toString());
                }
            } else {
                String versionUrl = checkForLatestVersion(links);
                if (versionUrl == null) {
                    for (String link : links) {
                        walkDirs(link);
                    }
                } else {
                    walkDirs(versionUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Problem reading url " + url + " - " + e.toString());
        }
    }

    private static String getPomUrl(List<String> urls) {
        for (String url : urls) {
            if (url.endsWith(".pom")) {
                return url;
            }
        }
        return null;
    }

    private static String checkForLatestVersion(List<String> urls) throws MalformedURLException {
        boolean hasVersionedDirs = false;
        for (String url : urls) {
            final String dir = getDirectory(url);
            if (dir.length() > 1 && Character.isDigit(dir.charAt(0))) {
                hasVersionedDirs = true;
                break;
            }
        }
        if (hasVersionedDirs) {
            TreeMap<String, String> versions = new TreeMap<>();
            for (String url : urls) {
                if (!url.contains(".xml")) {
                    versions.put(getDirectory(url), url);
                }
            }
            if (versions.isEmpty()) {
                return null;
            } else {
                String latestVersion = null;
                Semver latestSemver = null;
                for (String version : versions.keySet()) {
                    Semver sv = new Semver(version, Semver.SemverType.LOOSE);
                    if (latestSemver == null || sv.isGreaterThan(latestSemver)) {
                        latestVersion = version;
                        latestSemver = sv;
                    }
                }
                return latestVersion == null ? null : versions.get(latestVersion);
            }
        } else {
            return null;
        }
    }

    private static String getDirectory(final String url) throws MalformedURLException {
        URL evalUrl = new URL(url);
        String path = evalUrl.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        final int idx = path.lastIndexOf('/');
        if (idx != -1) {
            return path.substring(idx + 1);
        } else {
            return path;
        }
    }

    private static List<String> getLinks(final String url) throws IOException {
        final Document doc = Jsoup.connect(url).get();
        final Elements links = doc.select("a[href]");
        final ArrayList<String> result = new ArrayList<>();
        for (Element link : links) {
            String linkAddress = link.attr("abs:href");
            if (linkAddress.startsWith(url) && !linkAddress.contains(".xml")) {
                result.add(linkAddress);
            }
        }
        return result;
    }

    private static byte[] getBytes(final String url) throws IOException {
        return IOUtils.toByteArray(new URL(url));
    }
}
