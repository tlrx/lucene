/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.index;

import org.apache.lucene.index.MergePolicy.MergeAbortedException;

/**
 * Thread-local context for checking merge abort status during long-running I/O operations.
 *
 * @lucene.internal
 */
public final class MergeAbortThreadLocalChecker {

  public static final String ENABLED_SYSPROP =
      "org.apache.lucene.index.MergeAbortThreadLocalChecker.enabled";

  static final boolean ENABLED;

  static {
    boolean enabled = false;
    try {
      enabled = Boolean.parseBoolean(System.getProperty(ENABLED_SYSPROP, "false"));
    } catch (@SuppressWarnings("unused") SecurityException ignored) {
      // Ignore security exceptions in restricted environments
    }
    ENABLED = enabled;
  }

  /** Functional interface for abort checking that can throw MergeAbortedException. */
  @FunctionalInterface
  public interface AbortChecker {
    /** Checks if the merge should be aborted, throwing MergeAbortedException if so. */
    void checkAborted() throws MergeAbortedException;
  }

  private static final ThreadLocal<AbortChecker> ABORT_CHECKER = ENABLED ? new ThreadLocal<>() : null;

  private MergeAbortThreadLocalChecker() {}

  /** Returns true if abortable checksum is enabled via system property. */
  public static boolean isEnabled() {
    return ENABLED;
  }

  public static void set(AbortChecker checker) {
    if (ABORT_CHECKER != null) {
      ABORT_CHECKER.set(checker);
    }
  }

  public static void checkAborted() throws MergeAbortedException {
    if (ABORT_CHECKER != null) {
      AbortChecker checker = ABORT_CHECKER.get();
      if (checker != null) {
        checker.checkAborted();
      }
    }
  }

  public static void clear() {
    if (ABORT_CHECKER != null) {
      ABORT_CHECKER.remove();
    }
  }
}
