package com.bytedompteur.documentfinder.filewalker.core;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

@Slf4j
public class FileEndingMatcher implements FileMatcher {

  private final Set<String> fileEndings;
  private final Pattern pattern;

  public FileEndingMatcher(Set<String> fileEndings) {
    checkArgument(nonNull(fileEndings) && !fileEndings.isEmpty(), "'fileEndings' is null or empty");
    this.fileEndings = fileEndings;
    pattern = buildPattern(fileEndings);
  }

  public Set<String> getFileEndings() {
    return Collections.unmodifiableSet(fileEndings);
  }

  @Override
  public boolean matches(Path path) {
    String pathStr = path.toString();
    boolean result = pattern.matcher(pathStr).matches();
    log.debug("'{}' matches file endings: {}", pathStr, result);
    return result;
  }

  private Pattern buildPattern(Set<String> fileEndings) {
    final Pattern pattern;
    String fileEndingsStr = String.join("|", fileEndings);
    String expression = String.format("^.*(%s)$", fileEndingsStr);
    pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    return pattern;
  }
}
