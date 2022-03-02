package com.github.pjfanning.mavenwalker;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.InputStream;

public class PomUtils {
    private static final String UNKNOWN_GROUPID = "unknow-groupId";
    private static final String UNKNOWN_VERSION = "unknow-version";

    public static String evalLog4jv1Dependency(InputStream stream) throws Exception {
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        final Model model = reader.read(stream);
        for (Dependency dependency : model.getDependencies()) {
            if ("log4j".equals(dependency.getGroupId()) && "log4j".equals(dependency.getArtifactId())) {
                return createIdentifier(model, dependency.getScope());
            }
        }
        return null;
    }

    private static String createIdentifier(final Model model, final String scope) {
        final StringBuilder sb = new StringBuilder();
        String groupId = evalWithDefault(model.getGroupId(), UNKNOWN_GROUPID);
        if (UNKNOWN_GROUPID.equals(groupId) && model.getParent() != null) {
            groupId = evalWithDefault(model.getParent().getGroupId(), UNKNOWN_GROUPID);
        }
        String version = evalWithDefault(model.getVersion(), UNKNOWN_VERSION);
        if (UNKNOWN_VERSION.equals(version) && model.getParent() != null) {
            version = evalWithDefault(model.getParent().getVersion(), UNKNOWN_VERSION);
        }
        sb.append(groupId)
                .append(':')
                .append(evalWithDefault(model.getArtifactId(), "unknown-artifactId"))
                .append(':')
                .append(version);
        final String evalScope = evalWithDefault(scope, "");
        if (!"".equals(evalScope)) {
            sb.append(" (scope=")
                    .append(evalScope)
                    .append(')');
        }
        return sb.toString();
    }

    private static String evalWithDefault(final String str, final String defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            final String trimmed = str.trim();
            if (trimmed.isEmpty()) {
                return defaultValue;
            } else {
                return trimmed;
            }
        }
    }
}
